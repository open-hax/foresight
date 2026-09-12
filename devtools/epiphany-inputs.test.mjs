// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import {execFileSync, spawnSync} from 'node:child_process';
import fs from 'node:fs/promises';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import {fileURLToPath} from 'node:url';

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
