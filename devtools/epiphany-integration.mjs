// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import fs from 'node:fs/promises';
import net from 'node:net';
import os from 'node:os';
import path from 'node:path';
import { spawn, execFileSync } from 'node:child_process';
import { createRequire } from 'node:module';
import { createHash } from 'node:crypto';
import { fileURLToPath } from 'node:url';
import { setTimeout as delay } from 'node:timers/promises';
import S3rver from 's3rver';
import { S3Client, CreateBucketCommand, PutObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { startEmbeddingServer } from './embedding-server.mjs';

const root = fileURLToPath(new URL('../', import.meta.url));
const repo = path.join(root, 'epiphany');
const clio = path.resolve(process.env.EPIPHANY_CLIO_SOURCE || path.join(root, 'eta-mu'));
const output = path.resolve(process.env.FORESIGHT_VERIFICATION_OUTPUT || path.join(root, '.cache/verification/epiphany-integration'));
const mongoRequire = createRequire(path.join(root, 'knoxx/backend/package.json'));
const { MongoClient } = mongoRequire('mongodb');
const owned = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-epiphany-services-'));
const children = new Set();
let mongoClient, s3, s3Client, embeddings, cleaning;

async function stop(child) {
  if (!child.pid || child.exitCode !== null || child.signalCode !== null) return;
  child.kill('SIGTERM');
  for (let attempt = 0; attempt < 50 && child.exitCode === null && child.signalCode === null; attempt++) await delay(100);
  if (child.exitCode === null && child.signalCode === null) {
    child.kill('SIGKILL');
    await new Promise(resolve => child.once('exit', resolve));
  }
}

function cleanup() {
  return cleaning ||= (async () => {
    await Promise.all([...children].map(stop));
    await mongoClient?.close();
    s3Client?.destroy();
    await s3?.close();
    await embeddings?.close();
    await fs.copyFile(path.join(owned, 'mongo.log'), path.join(output, 'mongo.log')).catch(error => {
      if (error.code !== 'ENOENT') throw error;
    });
    await fs.rm(owned, { recursive: true, force: true });
  })();
}

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.once(signal, () => { cleanup().finally(() => process.exit(130)); });
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
  children.add(child);
  let transcript = '';
  for (const stream of [child.stdout, child.stderr]) stream.on('data', bytes => { transcript += bytes.toString(); });
  const timer = setTimeout(() => { stop(child); }, 480000);
  let exit;
  try {
    exit = await new Promise((resolve, reject) => {
      child.once('error', reject);
      child.once('exit', (code, signal) => resolve({ code, signal }));
    });
  } finally {
    clearTimeout(timer);
    children.delete(child);
    await fs.writeFile(path.join(output, 'integration.log'), transcript);
  }
  assert.equal(exit.code, 0, `Integration failed (${exit.code ?? exit.signal}); inspect ${path.join(output, 'integration.log')}`);
  const plain = transcript.replace(/\x1b\[[0-9;]*m/g, '');
  assert(!/\bSKIP\b|[1-9]\d*\s+(?:failures|errors|skipped)/i.test(plain), 'Integration output contains skipped or failing tests');
  const summary = plain.match(/\d+ tests, \d+ assertions, 0 failures\./)?.[0];
  assert(summary, 'Missing successful Kaocha result');
  return { command: ['clojure', ...args], ...exit, summary };
}

try {
  await fs.mkdir(output, { recursive: true });
  const before = await sourceSnapshot();
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
  const evidence = { started, finished: new Date().toISOString(), source: before, clio: clioRevision,
    embedding: { model: embeddings.model, dimensions: 384, revision: '751bff37182d3f1213fa05d7196b954e230abad9', artifact: env.EPIPHANY_TEST_EMBEDDING_DIGEST },
    mongoPing: true, s3ObjectRoundtrip: true, integration: result, skipped: false };
  await fs.writeFile(path.join(output, 'result.json'), JSON.stringify(evidence, null, 2) + '\n');
  console.log(`PASS actual JVM integration with real MiniLM: ${result.summary}`);
} finally {
  await cleanup();
}
