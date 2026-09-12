'use strict';

const assert = require('node:assert/strict');
const {spawnSync} = require('node:child_process');
const {once} = require('node:events');
const fs = require('node:fs/promises');
const http = require('node:http');
const https = require('node:https');
const net = require('node:net');
const os = require('node:os');
const path = require('node:path');
const tls = require('node:tls');
const dns = require('node:dns');
const dgram = require('node:dgram');
const test = require('node:test');
const {install} = require('./node-test-transport.cjs');

test('owned native HTTP and fetch work; every non-owned path is refused before transport', async () => {
  const blocked = [];
  const boundary = install({onBlocked:error => blocked.push(error.code)});
  const server = http.createServer((request, response) => {
    if (request.url === '/redirect') response.writeHead(302, {location:'https://external.fixture.invalid/private'}).end();
    else response.end('owned fixture');
  });
  try {
    server.listen(0, '127.0.0.1');
    await once(server, 'listening');
    const port = server.address().port;
    const origin = `http://127.0.0.1:${port}`;
    assert.equal(await (await fetch(origin)).text(), 'owned fixture');
    const native = await new Promise((resolve, reject) => {
      http.get(origin, response => {
        let content = '';
        response.on('data', chunk => { content += chunk; });
        response.on('end', () => resolve(content));
      }).on('error', reject);
    });
    assert.equal(native, 'owned fixture');
    for (const attempt of [
      () => net.connect({host:'external.fixture.invalid', port:443}),
      () => net.connect({host:'127.0.0.1', port:1}),
      () => net.connect({host:'::1', port}),
      () => net.connect({path:'/tmp/unowned-fixture.sock'}),
      () => tls.connect({host:'external.fixture.invalid', port:443}),
      () => dns.lookup('external.fixture.invalid', () => {}),
      () => dns.promises.lookup('external.fixture.invalid'),
      () => new dns.Resolver().resolve4('external.fixture.invalid', () => {}),
      () => new dns.promises.Resolver().resolve4('external.fixture.invalid'),
      () => dgram.createSocket('udp4'),
      () => new dgram.Socket('udp4'),
    ]) assert.throws(attempt, {code:'ERR_TEST_TRANSPORT_NOT_OWNED'});
    await assert.rejects(fetch('https://external.fixture.invalid/private'));
    await assert.rejects(fetch(`${origin}/redirect`));
    await assert.rejects(new Promise((resolve, reject) => {
      try { https.get('https://external.fixture.invalid/private', resolve).on('error', reject); }
      catch (error) { reject(error); }
    }));
    assert.equal(blocked.length, 14);
    const closed = new Promise(resolve => server.close(resolve));
    assert.throws(() => net.connect({host:'127.0.0.1', port}), {code:'ERR_TEST_TRANSPORT_NOT_OWNED'});
    await closed;
  } finally {
    server.closeAllConnections();
    if (server.listening) await new Promise(resolve => server.close(resolve));
    boundary.close();
  }
});

test('preloaded audit fails even when application code catches the refusal', () => {
  const result = spawnSync(process.execPath, ['--require', path.join(__dirname, 'node-test-loopback.cjs'),
    '-e', 'try { require("node:net").connect({host:"external.fixture.invalid",port:443}); } catch {}'],
  {encoding:'utf8', env:{PATH:process.env.PATH}});
  assert.equal(result.status, 1);
  assert.equal(result.signal, null);
  assert.match(result.stderr, /ERR_TEST_TRANSPORT_NOT_OWNED/);
});

for (const finish of [
  'process.on("exit", () => { process.exitCode = 0; });',
  'process.on("exit", () => { process.exit(0); });',
  'process.exit(0);',
]) {
  test(`caught refusal stays fatal with later success exit: ${finish}`, () => {
    const code = 'try { require("node:net").connect({host:"external.fixture.invalid",port:443}); } catch {} ' + finish;
    const result = spawnSync(process.execPath, ['--require', path.join(__dirname, 'node-test-loopback.cjs'), '-e', code],
      {encoding:'utf8', env:{PATH:process.env.PATH}});
    assert.equal(result.status, 1);
    assert.equal(result.signal, null);
    assert.match(result.stderr, /ERR_TEST_TRANSPORT_NOT_OWNED/);
  });
}

test('Node test children inherit the preload and cannot swallow an unmatched fallback', async () => {
  const directory = await fs.mkdtemp(path.join(os.tmpdir(), 'foresight-transport-proof-'));
  try {
    const fixture = path.join(directory, 'fallback.test.cjs');
    await fs.writeFile(fixture, 'require("node:test")("caught fallback", () => { try { require("node:net").connect({host:"external.fixture.invalid",port:443}); } catch {} });\n');
    const result = spawnSync(process.execPath, ['--require', path.join(__dirname, 'node-test-loopback.cjs'),
      '--test', fixture], {encoding:'utf8', env:{PATH:process.env.PATH}});
    assert.equal(result.status, 1);
    assert.equal(result.signal, null);
    assert.match(result.stdout + result.stderr, /ERR_TEST_TRANSPORT_NOT_OWNED/);
  } finally {
    await fs.rm(directory, {recursive:true, force:true});
  }
});
