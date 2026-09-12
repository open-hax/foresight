// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import net from 'node:net';
import os from 'node:os';
import path from 'node:path';
import { spawn, execFileSync } from 'node:child_process';
import { createHash, randomUUID } from 'node:crypto';
import { fileURLToPath } from 'node:url';
import { stripVTControlCharacters } from 'node:util';
import { setTimeout as delay } from 'node:timers/promises';
import S3rver from 's3rver';
import { S3Client, CreateBucketCommand, PutObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { MongoClient } from 'mongodb';
import { startEmbeddingServer } from './embedding-server.mjs';

const root = fileURLToPath(new URL('../', import.meta.url));
const repo = path.join(root, 'epiphany');
const clio = path.resolve(process.env.EPIPHANY_CLIO_SOURCE || path.join(root, 'eta-mu'));
const output = path.resolve(process.env.FORESIGHT_VERIFICATION_OUTPUT || path.join(root, '.cache/verification/epiphany-integration'));
let owned;
const children = new Set();
let mongoClient, s3, s3Client, embeddings, cleaning;

const childClosures = new WeakMap();

function childClosed(child) {
  if (!childClosures.has(child)) {
    childClosures.set(child, new Promise(resolve => {
      let error;
      child.once('error', cause => { error = cause; });
      child.once('close', (code, signal) => resolve({code, signal, error}));
    }));
  }
  return childClosures.get(child);
}

async function stop(child) {
  const closed = childClosed(child);
  if (child.pid && child.exitCode === null && child.signalCode === null) {
    child.kill('SIGTERM');
    for (let attempt = 0; attempt < 50 && child.exitCode === null && child.signalCode === null; attempt++) await delay(100);
    if (child.exitCode === null && child.signalCode === null) child.kill('SIGKILL');
  }
  let drainTimer;
  const drained = await Promise.race([
    closed.then(() => true),
    new Promise(resolve => { drainTimer = setTimeout(() => resolve(false), 1000); }),
  ]);
  clearTimeout(drainTimer);
  if (!drained) {
    // These are this supervisor's pipe handles. A descendant may still hold
    // their other ends after the direct child has already exited.
    child.stdout?.destroy();
    child.stderr?.destroy();
    await closed;
    throw new Error('Child output did not close within the shutdown drain bound');
  }
  await closed;
}

function cleanup() {
  return cleaning ||= (async () => {
    const operations = [
      ...[...children].map(child => () => stop(child)),
      () => mongoClient?.close(), () => s3Client?.destroy(),
      () => s3?.close(), () => embeddings?.close(),
    ];
    const results = await Promise.allSettled(operations.map(operation => Promise.resolve().then(operation)));
    try {
      await fs.copyFile(path.join(owned, 'mongo.log'), path.join(output, 'mongo.log'));
    } catch (error) {
      if (error.code !== 'ENOENT') results.push({status: 'rejected', reason: error});
    }
    try { await fs.rm(owned, {recursive: true, force: true}); }
    catch (error) { results.push({status: 'rejected', reason: error}); }
    const failures = results.filter(result => result.status === 'rejected').map(result => result.reason);
    if (failures.length) throw new AggregateError(failures, 'Owned service cleanup failed');
  })();
}

async function freePort() {
  const server = net.createServer();
  await new Promise((resolve, reject) => { server.once('error', reject); server.listen(0, '127.0.0.1', resolve); });
  const port = server.address().port;
  await new Promise((resolve, reject) => server.close(error => error ? reject(error) : resolve()));
  return port;
}

function git(directory, ...args) {
  return execFileSync('git', ['-C', directory, ...args], { encoding: 'utf8' }).trim();
}

async function sourceSnapshot() {
  const files = git(repo, 'ls-files', '--cached', '--others', '--exclude-standard', '-z').split('\0').filter(Boolean).sort();
  const hash = createHash('sha256');
  for (const file of files) {
    const location = path.join(repo, file);
    const stat = await fs.lstat(location);
    const bytes = stat.isSymbolicLink() ? await fs.readlink(location) : await fs.readFile(location);
    hash.update(file).update('\0').update(stat.isSymbolicLink() ? 'link\0' : 'file\0').update(bytes).update('\0');
  }
  return { head: git(repo, 'rev-parse', 'HEAD'), tree: git(repo, 'rev-parse', 'HEAD^{tree}'),
    dirty: Boolean(git(repo, 'status', '--porcelain')), files: files.length, sha256: hash.digest('hex') };
}

async function integration(env) {
  const override = `{:aliases {:verified-clio {:override-deps {eta-mu/clio {:local/root ${JSON.stringify(path.join(clio, 'packages/clio'))}}}}}}`;
  const args = ['-Sdeps', override, '-M:verified-clio:integration-test'];
  const child = spawn('clojure', args, { cwd: repo, env, stdio: ['ignore', 'pipe', 'pipe'] });
  return { command: ['clojure', ...args], ...await observeIntegration(child, output) };
}

export async function observeIntegration(child, outputDirectory, {timeoutMs = 480000} = {}) {
  children.add(child);
  let transcript = '';
  for (const stream of [child.stdout, child.stderr]) stream.setEncoding('utf8').on('data', text => { transcript += text; });
  let timedOut = false, stopping, shutdownFailure;
  const timer = setTimeout(() => {
    timedOut = true;
    stopping = stop(child).catch(error => { shutdownFailure = error; });
  }, timeoutMs);
  let exit;
  try {
    exit = await childClosed(child);
    if (exit.error) throw exit.error;
  } finally {
    clearTimeout(timer);
    await stopping;
    children.delete(child);
    await fs.writeFile(path.join(outputDirectory, 'integration.log'), transcript);
  }
  if (timedOut) throw new Error('Integration exceeded its deadline', {cause: shutdownFailure});
  assert.equal(exit.code, 0, `Integration failed (${exit.code ?? exit.signal}); inspect ${path.join(output, 'integration.log')}`);
  const plain = stripVTControlCharacters(transcript);
  assert(!/\bSKIP\b|[1-9]\d*\s+(?:failures|errors|skipped)/i.test(plain), 'Integration output contains skipped or failing tests');
  const summary = plain.match(/\d+ tests, \d+ assertions, 0 failures\./)?.[0];
  assert(summary, 'Missing successful Kaocha result');
  return { ...exit, summary };
}

async function runServices() {
  let evidence;
  owned = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-epiphany-services-'));
  for (const signal of ['SIGINT', 'SIGTERM']) {
    process.once(signal, () => { cleanup().finally(() => process.exit(130)); });
  }
  try {
    await fs.mkdir(output, { recursive: true });
    const before = await sourceSnapshot();
    assert.equal(before.dirty, false, 'Selected Epiphany checkout must be immutable and clean');
    const clioRevision = git(clio, 'rev-parse', 'HEAD');
    assert.equal(git(clio, 'status', '--porcelain'), '', 'Selected Clio checkout must be immutable and clean');
    const port = await freePort();
    const dbpath = path.join(owned, 'mongo');
    await fs.mkdir(dbpath);
    const mongo = spawn(process.env.FORESIGHT_MONGOD || 'mongod',
      ['--bind_ip', '127.0.0.1', '--port', String(port), '--dbpath', dbpath,
        '--nounixsocket', '--wiredTigerCacheSizeGB', '0.25', '--logpath', path.join(owned, 'mongo.log')],
      { stdio: ['ignore', 'ignore', 'pipe'] });
    children.add(mongo);
    childClosed(mongo);
    let mongoError;
    mongo.once('error', error => { mongoError = error; });
    const mongoUri = `mongodb://127.0.0.1:${port}/?directConnection=true`;
    let connected = false, lastPingError;
    for (let attempt = 0; attempt < 60 && !connected; attempt++) {
      if (mongoError) throw mongoError;
      assert(mongo.exitCode === null, 'Owned Mongo exited before readiness');
      mongoClient = new MongoClient(mongoUri, { serverSelectionTimeoutMS: 500 });
      try { await mongoClient.db('admin').command({ ping: 1 }); connected = true; }
      catch (error) {
        lastPingError = error;
        await mongoClient.close();
        await delay(100);
      }
    }
    assert(connected, `Owned Mongo did not answer its actual ping within the startup bound: ${lastPingError?.message}`);
    console.log('PASS native Mongo answered its command protocol');
    s3 = new S3rver({ port: 0, address: '127.0.0.1', directory: path.join(owned, 's3'), silent: true });
    const s3Address = await s3.run();
    const s3Endpoint = `http://127.0.0.1:${s3Address.port}`;
    s3Client = new S3Client({ endpoint: s3Endpoint, region: 'us-east-1', forcePathStyle: true,
      credentials: { accessKeyId: 'S3RVER', secretAccessKey: 'S3RVER' } });
    await s3Client.send(new CreateBucketCommand({ Bucket: 'epiphany-fixture' }));
    await s3Client.send(new PutObjectCommand({ Bucket: 'epiphany-fixture', Key: 'draft.edn', Body: '{:status :draft}' }));
    const stored = await s3Client.send(new GetObjectCommand({ Bucket: 'epiphany-fixture', Key: 'draft.edn' }));
    assert.equal(await stored.Body.transformToString(), '{:status :draft}');
    console.log('PASS S3rver create/put/get preserves actual EDN object bytes');
    embeddings = await startEmbeddingServer();
    const env = { ...process.env, EPIPHANY_TEST_MONGODB_URI: mongoUri, EPIPHANY_TEST_S3_ENDPOINT: s3Endpoint,
      EPIPHANY_TEST_EMBEDDING_BASE_URL: embeddings.baseUrl.replace(/\/v1$/, ''),
      EPIPHANY_TEST_EMBEDDING_MODEL: embeddings.model, EPIPHANY_TEST_EMBEDDING_DIMENSIONS: '384',
      EPIPHANY_TEST_EMBEDDING_DIGEST: 'sha256:afdb6f1a0e45b715d0bb9b11772f032c399babd23bfc31fed1c170afc848bdb1' };
    const started = new Date().toISOString();
    const result = await integration(env);
    assert.deepEqual(await sourceSnapshot(), before, 'Epiphany source changed during integration');
    assert.equal(git(clio, 'rev-parse', 'HEAD'), clioRevision, 'Selected Clio revision changed during integration');
    assert.equal(git(clio, 'status', '--porcelain'), '', 'Selected Clio source changed during integration');
    evidence = { started, finished: new Date().toISOString(), source: before, clio: clioRevision,
      embedding: { model: embeddings.model, dimensions: 384, revision: '751bff37182d3f1213fa05d7196b954e230abad9', artifact: env.EPIPHANY_TEST_EMBEDDING_DIGEST },
      mongoPing: true, s3ObjectRoundtrip: true, integration: result, skipped: false };
    console.log(`PASS actual JVM integration with real MiniLM: ${result.summary}`);
  } finally {
    await cleanup();
  }
  await fs.writeFile(process.env.FORESIGHT_EPIPHANY_CANDIDATE,
    JSON.stringify({...evidence, run_id: process.env.FORESIGHT_EPIPHANY_RUN_ID, cleanup_completed: true}) + '\n', {flag: 'wx'});
}

function normalizeCommand(command, clioDirectory) {
  const exactPath = JSON.stringify(path.join(clioDirectory, 'packages/clio'));
  return command.map(argument => argument.replaceAll(exactPath, JSON.stringify('<verified-clio-source>/packages/clio')));
}

export async function superviseIntegration({
  command = process.execPath, args = [fileURLToPath(import.meta.url), '--worker'],
  env = process.env, outputDirectory = output, clioDirectory = clio, forwardOutput = true,
} = {}) {
  await fs.mkdir(outputDirectory, {recursive: true});
  await fs.unlink(path.join(outputDirectory, 'result.json')).catch(error => { if (error.code !== 'ENOENT') throw error; });
  const runDirectory = await fs.mkdtemp(path.join(outputDirectory, 'run-'));
  const candidate = path.join(runDirectory, 'candidate.json');
  const runId = randomUUID();
  const sourceBytes = await fs.readFile(fileURLToPath(import.meta.url));
  const child = spawn(command, args, {stdio: ['ignore', 'pipe', 'pipe'], env: {
    ...env, FORESIGHT_VERIFICATION_OUTPUT: outputDirectory,
    FORESIGHT_EPIPHANY_CANDIDATE: candidate, FORESIGHT_EPIPHANY_RUN_ID: runId,
  }});
  const closed = childClosed(child);
  let transcript = '', interrupted = false, stopping, shutdownFailure;
  for (const [stream, destination] of [[child.stdout, process.stdout], [child.stderr, process.stderr]]) {
    stream.setEncoding('utf8').on('data', text => { transcript += text; });
    if (forwardOutput) stream.pipe(destination, {end: false});
  }
  const terminate = () => {
    interrupted = true;
    stopping ||= stop(child).catch(error => { shutdownFailure = error; });
  };
  const timer = setTimeout(terminate, 600000);
  for (const signal of ['SIGINT', 'SIGTERM']) process.once(signal, terminate);
  let exit;
  try { exit = await closed; }
  finally {
    clearTimeout(timer);
    await stopping;
    for (const signal of ['SIGINT', 'SIGTERM']) process.removeListener(signal, terminate);
    await fs.writeFile(path.join(runDirectory, 'supervisor.log'), transcript);
  }
  if (exit.error) throw exit.error;
  if (interrupted) throw new Error('Service supervisor was interrupted', {cause: shutdownFailure});
  assert.equal(exit.code, 0, `Service supervisor failed (${exit.code ?? exit.signal}); inspect ${runDirectory}`);
  assert.deepEqual(await fs.readFile(fileURLToPath(import.meta.url)), sourceBytes, 'Supervisor source changed during execution');
  const evidence = JSON.parse(await fs.readFile(candidate, 'utf8'));
  assert.equal(evidence.run_id, runId, 'Candidate belongs to a different run');
  assert.equal(evidence.cleanup_completed, true, 'Candidate lacks successful cleanup');
  assert.equal(evidence.integration.code, 0, 'Candidate lacks successful integration');
  assert.equal(evidence.skipped, false, 'Candidate contains skipped integration');
  const result = {...evidence, schema_version: 2,
    integration: {...evidence.integration, command: normalizeCommand(evidence.integration.command, clioDirectory)},
    supervisor_sha256: createHash('sha256').update(sourceBytes).digest('hex'),
    supervisor_exit_code: exit.code, supervisor_signal: exit.signal,
    supervisor_command: [command, ...args], supervisor_observed: 'child-close',
  };
  const staged = path.join(runDirectory, 'result.json');
  await fs.writeFile(staged, JSON.stringify(result, null, 2) + '\n', {flag: 'wx'});
  await fs.rename(staged, path.join(outputDirectory, 'result.json'));
  return result;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  if (process.argv[2] === '--worker') await runServices();
  else await superviseIntegration();
}
