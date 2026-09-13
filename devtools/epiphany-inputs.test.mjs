// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import {execFileSync, spawnSync, spawn} from 'node:child_process';
import {once} from 'node:events';
import {createHash} from 'node:crypto';
import fs from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import {fileURLToPath} from 'node:url';
import {observeIntegration, superviseIntegration} from './epiphany-integration.mjs';

const source = fileURLToPath(new URL('./', import.meta.url));

async function checkout(directory) {
  await fs.mkdir(directory);
  execFileSync('git', ['init', '--quiet', directory]);
  await fs.writeFile(path.join(directory, 'fixture.edn'), '{:revision 1}\n');
  execFileSync('git', ['-C', directory, 'add', 'fixture.edn']);
  execFileSync('git', ['-C', directory, '-c', 'user.name=Fixture',
    '-c', 'user.email=fixture@example.invalid', 'commit', '--quiet', '-m', 'Owned fixture']);
}

for (const dirty of ['tracked', 'untracked', 'clean']) {
  test(`real supervisor ${dirty === 'clean' ? 'admits clean' : 'refuses ' + dirty} Epiphany source before services`, async () => {
    const owned = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-input-admission-'));
    try {
      const devtools = path.join(owned, 'devtools');
      const epiphany = path.join(owned, 'epiphany');
      const clio = path.join(owned, 'eta-mu');
      const output = path.join(owned, 'result');
      await fs.mkdir(devtools);
      const temporary = path.join(owned, 'tmp');
      await fs.mkdir(temporary);
      for (const file of ['epiphany-integration.mjs', 'embedding-server.mjs', 'model-request-body.mjs']) {
        await fs.copyFile(path.join(source, file), path.join(devtools, file));
      }
      await fs.symlink(path.resolve(source, '../node_modules'), path.join(owned, 'node_modules'), 'dir');
      await checkout(epiphany);
      await checkout(clio);
      if (dirty !== 'clean') {
        await fs.writeFile(path.join(epiphany, dirty === 'tracked' ? 'fixture.edn' : 'new.edn'), '{:unreviewed true}\n');
      }
      // An absent executable is an intentional post-admission sentinel. No real
      // model, Mongo or S3 service can start in this boundary regression.
      await fs.mkdir(output);
      await fs.writeFile(path.join(output, 'result.json'), JSON.stringify({staleSuccess: true}));
      const missingMongo = path.join(owned, 'no-mongod');
      const result = spawnSync(process.execPath, [path.join(devtools, 'epiphany-integration.mjs')], {
        encoding: 'utf8', timeout: 30000,
        env: {...process.env, TMPDIR: temporary, EPIPHANY_CLIO_SOURCE: clio, FORESIGHT_VERIFICATION_OUTPUT: output,
          FORESIGHT_MONGOD: missingMongo, FORESIGHT_MODELS_OFFLINE: '1'},
      });
      assert.equal(result.status, 1, result.stderr);
      assert.equal(result.signal, null);
      if (dirty === 'clean') {
        assert.match(result.stderr, /no-mongod ENOENT/);
        assert.doesNotMatch(result.stderr, /Selected Epiphany checkout must be immutable and clean/);
      } else {
        assert.match(result.stderr, /Selected Epiphany checkout must be immutable and clean/);
        assert.doesNotMatch(result.stderr, /no-mongod/);
      }
      await assert.rejects(fs.access(path.join(output, 'result.json')), {code: 'ENOENT'});
    } finally {
      await fs.rm(owned, {recursive: true, force: true});
    }
  });
}

test('integration retains summary written through an inherited pipe after child exit', async () => {
  const output = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-integration-pipes-'));
  const writer = `setTimeout(() => process.stdout.write('22 tests, 108 assertions, 0 failures.\\n'), 150);`;
  const launcher = `const {spawn}=require('node:child_process');
    spawn(process.execPath, ['-e', ${JSON.stringify(writer)}], {stdio: ['ignore', 1, 2]}).unref();`;
  const child = spawn(process.execPath, ['-e', launcher], {stdio: ['ignore', 'pipe', 'pipe']});
  const closed = once(child, 'close');
  try {
    const result = await observeIntegration(child, output);
    assert.equal(result.summary, '22 tests, 108 assertions, 0 failures.');
    assert.match(await fs.readFile(path.join(output, 'integration.log'), 'utf8'), /22 tests, 108 assertions/);
  } finally {
    await closed;
    await fs.rm(output, {recursive: true, force: true});
  }
});

test('integration rejects a failure written after an early successful summary', async () => {
  const output = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-integration-tail-'));
  const writer = `setTimeout(() => process.stderr.write('1 failures\\n'), 100);`;
  const launcher = `const {spawn}=require('node:child_process');
    process.stdout.write('22 tests, 108 assertions, 0 failures.\\n');
    spawn(process.execPath, ['-e', ${JSON.stringify(writer)}], {stdio: ['ignore', 1, 2]}).unref();`;
  const child = spawn(process.execPath, ['-e', launcher], {stdio: ['ignore', 'pipe', 'pipe']});
  try {
    await assert.rejects(observeIntegration(child, output), /skipped or failing tests/);
    assert.match(await fs.readFile(path.join(output, 'integration.log'), 'utf8'), /1 failures/);
  } finally { await fs.rm(output, {recursive: true, force: true}); }
});

test('integration deadline refuses exit zero while a descendant keeps its output pipe open', async () => {
  const output = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-integration-drain-'));
  const writer = 'setTimeout(() => {}, 2500);';
  const launcher = `const {spawn}=require('node:child_process');
    process.stdout.write('22 tests, 108 assertions, 0 failures.\\n');
    spawn(process.execPath, ['-e', ${JSON.stringify(writer)}], {stdio: ['ignore', 1, 2]}).unref();`;
  const child = spawn(process.execPath, ['-e', launcher], {stdio: ['ignore', 'pipe', 'pipe']});
  try {
    await assert.rejects(observeIntegration(child, output, {timeoutMs: 700}), error => {
      assert.match(error.message, /exceeded its deadline/);
      assert.match(error.cause.message, /shutdown drain bound/);
      return true;
    });
    assert.equal(child.exitCode, 0, 'the direct child exited normally before timeout');
    assert.equal(child.stdout.destroyed, true);
    assert.equal(child.stderr.destroyed, true);
    assert.match(await fs.readFile(path.join(output, 'integration.log'), 'utf8'), /22 tests, 108 assertions/);
  } finally { await fs.rm(output, {recursive: true, force: true}); }
});

async function supervisorFixture(mode, check) {
  const output = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-supervisor-evidence-'));
  const clio = path.join(output, 'path with "quoted" name');
  const integration = {code: 0, signal: null, summary: '22 tests, 108 assertions, 0 failures.',
    command: ['clojure', '-Sdeps', `{:local/root ${JSON.stringify(path.join(clio, 'packages/clio'))}}`]};
  const program = `const fs=require('node:fs');
    const evidence={integration:${JSON.stringify(integration)}, skipped:false,
      run_id:process.env.FORESIGHT_EPIPHANY_RUN_ID, cleanup_completed:true};
    if (${JSON.stringify(mode)}==='wrong-run') evidence.run_id='abandoned-run';
    if (${JSON.stringify(mode)}==='unclean') evidence.cleanup_completed=false;
    if (${JSON.stringify(mode)}!=='absent') fs.writeFileSync(process.env.FORESIGHT_EPIPHANY_CANDIDATE, JSON.stringify(evidence));
    if (${JSON.stringify(mode)}==='failed-cleanup') throw Error('actual cleanup failed');
    if (${JSON.stringify(mode)}==='nonzero') process.exitCode=17;
    if (${JSON.stringify(mode)}==='signal') process.kill(process.pid,'SIGTERM');
    console.log('worker final output');`;
  try {
    await fs.writeFile(path.join(output, 'result.json'), '{"staleSuccess":true}');
    await fs.mkdir(path.join(output, 'run-abandoned'));
    await fs.writeFile(path.join(output, 'run-abandoned/candidate.json'), JSON.stringify({integration, cleanup_completed: true}));
    await check({output, clio, integration, run: () => superviseIntegration({
      command: process.execPath, args: ['-e', program], outputDirectory: output,
      clioDirectory: clio, forwardOutput: false,
    })});
  } finally { await fs.rm(output, {recursive: true, force: true}); }
}

test('supervisor publishes current normalized evidence only after actual worker close', async () => {
  await supervisorFixture('success', async ({output, clio, run}) => {
    const result = await run();
    assert.equal(result.schema_version, 2);
    assert.equal(result.supervisor_exit_code, 0);
    assert.equal(result.supervisor_signal, null);
    assert.equal(result.supervisor_observed, 'child-close');
    assert.equal(result.cleanup_completed, true);
    assert.equal(result.supervisor_command[0], process.execPath);
    assert.equal(result.supervisor_sha256, createHash('sha256')
      .update(await fs.readFile(new URL('./epiphany-integration.mjs', import.meta.url))).digest('hex'));
    assert.equal(result.integration.command[2], '{:local/root "<verified-clio-source>/packages/clio"}');
    assert(!JSON.stringify(result.integration).includes(clio));
    assert.deepEqual(JSON.parse(await fs.readFile(path.join(output, 'result.json'), 'utf8')), result);
    const runs = (await fs.readdir(output)).filter(name => name.startsWith('run-') && name !== 'run-abandoned');
    assert.equal(runs.length, 1);
    assert.match(await fs.readFile(path.join(output, runs[0], 'supervisor.log'), 'utf8'), /worker final output/);
  });
});

for (const [mode, expected] of [
  ['absent', /ENOENT/], ['wrong-run', /different run/], ['unclean', /successful cleanup/],
  ['failed-cleanup', /supervisor failed/], ['nonzero', /supervisor failed/], ['signal', /supervisor failed/],
]) {
  test(`supervisor refuses ${mode} current evidence and removes prior success`, async () => {
    await supervisorFixture(mode, async ({output, run}) => {
      await assert.rejects(run(), expected);
      await assert.rejects(fs.access(path.join(output, 'result.json')), {code: 'ENOENT'});
    });
  });
}
