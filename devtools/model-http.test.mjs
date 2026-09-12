// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import http from 'node:http';
import net from 'node:net';
import { setTimeout as delay } from 'node:timers/promises';
import { after, before, mock, test } from 'node:test';
import { PreTrainedTokenizer, Tensor, TextStreamer } from '@huggingface/transformers';
import { startEmbeddingServer } from './embedding-server.mjs';
import { LOCAL_GENERATION_MODEL, startGenerationServer } from './generation-server.mjs';
import { readModelRequestBody } from './model-request-body.mjs';

let generation;
let embedding;
before(async () => {
  generation = await startGenerationServer({ model: LOCAL_GENERATION_MODEL, timeoutMs: 1, requestTimeoutMs: 200 });
  embedding = await startEmbeddingServer({ requestTimeoutMs: 200 });
});
after(async () => {
  await Promise.all([generation?.close(), embedding?.close()]);
});

/** Send a literal local request and retain the HTTP classification. */
async function completion(body) {
  const response = await fetch(`${generation.baseUrl}/chat/completions`, {
    method: 'POST', headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ model: generation.model, messages: [{ role: 'user', content: 'Hello.' }], ...body }),
  });
  return { status: response.status, body: await response.json() };
}

for (const [description, response_format] of [['null response format', null], ['null translated-text schema', {
    type: 'json_schema', json_schema: { schema: {
      type: 'object', additionalProperties: false, required: ['translated_text'],
      properties: { translated_text: null },
    } },
  }]]) {
  test(`${description} is a client refusal`, async () => {
    assert.deepEqual(await completion({ response_format }), {
      status: 400, body: { error: 'unsupported_response_format' },
    });
  });
}

for (const [description, constraint] of [['maxLength', { maxLength: 0 }], ['pattern', { pattern: '^never$' }]]) {
  test(`the translation envelope refuses unsupported ${description} obligations before inference`, async () => {
    const response_format = { type: 'json_schema', json_schema: { schema: {
      type: 'object', additionalProperties: false, required: ['translated_text'],
      properties: { translated_text: { type: 'string', minLength: 1, ...constraint } },
    } } };
    assert.deepEqual(await completion({ response_format }), { status: 400, body: { error: 'unsupported_response_format' } });
  });
}

for (const tools of [{}, null, '', 'function', false, 0, [{ type: 'function' }]]) {
  test(`unsupported tools ${JSON.stringify(tools)} are refused before inference`, async () => {
    assert.deepEqual(await completion({ tools }), {
      status: 400, body: { error: 'unsupported_tools_or_stream' },
    });
  });
}

for (const stream of [null, '', 'false', 'true', 0, 1, {}, []]) {
  test(`unsupported stream ${JSON.stringify(stream)} is refused before inference`, async () => {
    assert.deepEqual(await completion({ stream }), {
      status: 400, body: { error: 'unsupported_tools_or_stream' },
    });
  });
}

test('an interrupted real local inference accepts stream false and empty tools and reports the expired deadline', async () => {
  assert.deepEqual(await completion({ max_tokens: 128, tools: [], stream: false }), {
    status: 504, body: { error: 'generation_timeout' },
  });
  const health = await (await fetch(generation.baseUrl.replace('/v1', '/health'))).json();
  assert.equal(health.busy, false);
});

for (const admitted of [{ stream: true }, { tool_choice: 'none' }]) {
  test(`an interrupted real inference admits ${JSON.stringify(admitted)} and preserves its deadline refusal`, async () => {
    assert.deepEqual(await completion({ ...admitted, max_completion_tokens: 128 }), {
      status: 504, body: { error: 'generation_timeout' },
    });
  });
}

test('a failure after real streamed tokens terminates with an SSE error and releases inference', async () => {
  const service = await startGenerationServer({ model: LOCAL_GENERATION_MODEL });
  const original = TextStreamer.prototype.on_finalized_text;
  const injected = mock.method(TextStreamer.prototype, 'on_finalized_text', function (text, streamEnd) {
    original.call(this, text, streamEnd);
    if (text.trim()) throw new Error('Injected failure after the native model text callback');
  });
  try {
    const response = await fetch(`${service.baseUrl}/chat/completions`, {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ model: service.model, stream: true, max_tokens: 32,
        messages: [{ role: 'user', content: 'Write a sentence about wikis.' }] }),
    });
    assert.equal(response.status, 200);
    const wire = await response.text();
    const events = wire.split('\n\n').filter(Boolean).map(line => line.slice(6));
    assert.equal(events.pop(), '[DONE]');
    const chunks = events.map(event => JSON.parse(event));
    assert.ok(chunks.some(chunk => chunk.choices?.some(choice => choice.delta.content?.trim())));
    assert.equal(chunks.at(-1).error.code, 'generation_failed');
    assert.ok(chunks.every(chunk => chunk.choices?.every(choice => !choice.finish_reason) ?? true));
    assert.ok(injected.mock.callCount() > 0);
    const health = await (await fetch(service.baseUrl.replace('/v1', '/health'))).json();
    assert.equal(health.busy, false);
  } finally {
    injected.mock.restore();
    await service.close();
  }
});

for (const tool_choice of [false, 0, '', null, {}, []]) {
  test(`present tool_choice ${JSON.stringify(tool_choice)} is refused before inference`, async () => {
    assert.deepEqual(await completion({ tool_choice }), {
      status: 400, body: { error: 'unsupported_tools_or_stream' },
    });
  });
}

for (const field of ['tool_calls', 'function_call']) {
  for (const value of [false, 0, '', null, {}, [], 'unsupported']) {
    test(`present message ${field} ${JSON.stringify(value)} is refused before inference`, async () => {
      assert.deepEqual(await completion({ messages: [{ role: 'user', content: 'Hello.', [field]: value }] }), {
        status: 400, body: { error: 'invalid_messages' },
      });
    });
  }
}

for (const model of [false, 0, '', null, 'unknown', {}, []]) {
  test(`present invalid embedding model ${JSON.stringify(model)} is refused`, async () => {
    const response = await fetch(`${embedding.baseUrl}/embeddings`, {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ model, input: 'Hello.' }),
    });
    assert.deepEqual({ status: response.status, body: await response.json() }, {
      status: 400, body: { error: 'unknown_model' },
    });
  });
}

test('embedding model omission and exact model name select the same real vector', async () => {
  const vector = async body => {
    const response = await fetch(`${embedding.baseUrl}/embeddings`, {
      method: 'POST', headers: { 'content-type': 'application/json' },
      body: JSON.stringify({ input: 'Same sentence.', ...body }),
    });
    assert.equal(response.status, 200);
    const result = await response.json();
    assert.equal(result.model, embedding.model);
    assert.equal(result.data[0].embedding.length, 384);
    assert(result.data[0].embedding.every(Number.isFinite));
    return result.data[0].embedding;
  };
  assert.deepEqual(await vector({}), await vector({ model: embedding.model }));
});

for (const [description, route, encoding, corrupt] of [
  ['missing row', '/v1/embeddings', 'float', rows => rows.slice(1)],
  ['extra row', '/embeddings', 'float', rows => [...rows, rows[0]]],
  ['short vector', '/api/embed', 'float', rows => [rows[0].slice(1), rows[1]]],
  ['long vector', '/v1/embeddings', 'base64', rows => [[...rows[0], 0], rows[1]]],
  ['NaN component', '/api/embed', 'float', rows => { rows[0][0] = NaN; return rows; }],
  ['infinite component', '/embeddings', 'float', rows => { rows[0][0] = Infinity; return rows; }],
  ['negative infinite component', '/v1/embeddings', 'base64', rows => { rows[0][0] = -Infinity; return rows; }],
  ['nonnumeric component', '/api/embed', 'float', rows => { rows[0][0] = '0'; return rows; }],
  ['sparse vector', '/embeddings', 'base64', rows => { delete rows[0][0]; return rows; }],
  ['Float32 overflow', '/v1/embeddings', 'base64', rows => { rows[0][0] = Number.MAX_VALUE; return rows; }],
  ['zero vector', '/api/embed', 'float', rows => [rows[0].map(() => 0), rows[1]]],
  ['scaled vector', '/embeddings', 'base64', rows => [rows[0], rows[1].map(value => value * 2)]],
  ['Float32 norm drift', '/v1/embeddings', 'base64', rows => [[0.9999000000001, ...rows[0].slice(1).map(() => 0)], rows[1]]],
]) {
  test(`embedding refuses a real model tensor with ${description}`, async () => {
    const original = Tensor.prototype.tolist;
    let corrupted = 0;
    // Keep tokenization and inference real; alter only the final pooled tensor conversion.
    const injected = mock.method(Tensor.prototype, 'tolist', function () {
      const rows = original.call(this);
      if (this.dims.length !== 2 || this.dims[0] !== 2 || this.dims[1] !== 384) return rows;
      corrupted++;
      return corrupt(rows);
    });
    try {
      const response = await fetch(new URL(route, embedding.baseUrl), {
        method: 'POST', headers: { 'content-type': 'application/json' },
        body: JSON.stringify({ input: ['A small red bird.', 'The database stores documents.'], encoding_format: encoding }),
      });
      assert.equal(corrupted, 1, 'The actual final MiniLM tensor conversion was exercised once');
      assert.equal(response.status, 500, 'Malformed model output must be an internal provider refusal');
      assert.deepEqual(await response.json(), { error: 'embedding_failed' });
    } finally { injected.mock.restore(); }
  });
}

for (const [description, decode] of [
  ['decoder exceptions', () => { throw new Error('Injected decoder failure'); }],
  ['empty output', () => ''],
]) {
  // Real inference still runs. Only the public decoding boundary is deliberately failed.
  test(`deadline classification survives ${description}`, async () => {
    const injected = mock.method(PreTrainedTokenizer.prototype, 'decode', decode);
    try {
      assert.deepEqual(await completion({ max_tokens: 128 }), {
        status: 504, body: { error: 'generation_timeout' },
      });
      assert.ok(injected.mock.callCount() > 0, 'The intended failure boundary was actually exercised');
    } finally { injected.mock.restore(); }
  });
}

/** Stream an oversized upload slowly enough to detect premature response/connection closure. */
async function oversizedUpload(url) {
  const agent = new http.Agent({ keepAlive: true, maxSockets: 1 });
  try {
    const result = await new Promise((resolve, reject) => {
      const request = http.request(url, { method: 'POST', agent, headers: { 'content-type': 'application/json' } });
      request.on('error', reject);
      request.on('response', response => {
        const chunks = [];
        response.on('data', chunk => chunks.push(chunk));
        response.on('error', reject);
        response.on('end', () => resolve({
          status: response.statusCode, body: JSON.parse(Buffer.concat(chunks).toString()),
          uploadFinished: request.writableFinished,
        }));
      });
      (async () => {
        request.write(Buffer.alloc(600 * 1024, 32));
        await delay(10);
        request.write(Buffer.alloc(600 * 1024, 32));
        await delay(30);
        request.end(Buffer.alloc(64 * 1024, 32));
      })().catch(reject);
    });
    assert.deepEqual(result, { status: 413, body: { error: 'input_too_large' }, uploadFinished: true });
    await new Promise((resolve, reject) => {
      const request = http.get(new URL('/health', url), { agent }, response => {
        response.resume();
        response.on('end', () => {
          try {
            assert.equal(response.statusCode, 200);
            assert.equal(request.reusedSocket, true);
            resolve();
          } catch (error) { reject(error); }
        });
      });
      request.on('error', reject);
    });
  } finally { agent.destroy(); }
}

test('generation drains oversized uploads and preserves the connection', async () => {
  await oversizedUpload(`${generation.baseUrl}/chat/completions`);
});

test('embedding drains oversized uploads and preserves the connection', async () => {
  await oversizedUpload(`${embedding.baseUrl}/embeddings`);
});

/** Keep a real upload unfinished and require both a complete refusal and socket closure. */
async function stalledUpload(url, oversized, trickle) {
  return new Promise((resolve, reject) => {
    let request, interval, result, closed = false;
    const finish = error => {
      if (!error && (!result || !closed)) return;
      clearTimeout(deadline); clearInterval(interval);
      request.destroy();
      if (error) reject(error); else resolve(result);
    };
    const deadline = setTimeout(() => finish(new Error('Stalled upload was not refused and closed within 1500ms')), 1500);
    request = http.request(url, { method: 'POST', headers: { 'content-type': 'application/json' } });
    request.on('error', finish);
    request.on('socket', socket => socket.on('close', () => { closed = true; finish(); }));
    request.on('response', response => {
      const chunks = [];
      response.on('data', chunk => chunks.push(chunk));
      response.on('error', finish);
      response.on('end', () => {
        try {
          result = { status: response.statusCode, body: JSON.parse(Buffer.concat(chunks).toString()),
            connection: response.headers.connection, uploadFinished: request.writableFinished };
          finish();
        } catch (error) { finish(error); }
      });
    });
    request.write(oversized ? Buffer.alloc(1024 * 1024 + 1, 32) : '{');
    if (trickle) interval = setInterval(() => request.write(' '), 20);
    // Intentionally never call end(): the server must bound ingestion itself.
  });
}

for (const provider of ['generation', 'embedding']) {
  for (const [description, oversized, trickle] of [['stalled', false, false], ['stalled oversized', true, false], ['trickling', false, true]]) {
    test(`${provider} bounds ${description} request ingestion and closes its socket`, async () => {
      const service = provider === 'generation' ? generation : embedding;
      const endpoint = provider === 'generation' ? '/chat/completions' : '/embeddings';
      assert.deepEqual(await stalledUpload(service.baseUrl + endpoint, oversized, trickle), {
        status: oversized ? 413 : 408, body: { error: oversized ? 'input_too_large' : 'request_timeout' },
        connection: 'close', uploadFinished: false,
      });
      const response = await fetch(service.baseUrl.replace('/v1', '/health'));
      assert.equal(response.status, 200);
      if (provider === 'generation') assert.equal((await response.json()).busy, false);
    });
  }
  test(`${provider} releases ingestion listeners after a client disconnect`, async () => {
    const service = provider === 'generation' ? generation : embedding;
    const endpoint = provider === 'generation' ? '/chat/completions' : '/embeddings';
    const received = new Promise(resolve => service.server.once('request', resolve));
    const request = http.request(service.baseUrl + endpoint, { method: 'POST' });
    request.on('error', () => {});
    request.write('{');
    const incoming = await received;
    const closed = new Promise(resolve => incoming.once('close', resolve));
    request.destroy();
    await closed;
    for (const event of ['data', 'end', 'aborted', 'error']) assert.equal(incoming.listenerCount(event), 0, `${event} listener must be released`);
    const response = await fetch(service.baseUrl.replace('/v1', '/health'));
    assert.equal(response.status, 200);
  });
}

test('ingestion timeout closes a pipelined socket even when its refusal cannot flush', async () => {
  let admitted;
  const secondRequest = new Promise(resolve => { admitted = resolve; });
  const server = http.createServer((request, response) => {
    if (request.url === '/held') {
      response.writeHead(200); response.write('This preceding response intentionally never ends.');
    } else {
      admitted(readModelRequestBody(request, response, 20));
    }
  });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const socket = net.connect(server.address().port, '127.0.0.1');
  try {
    const closed = new Promise((resolve, reject) => {
      const deadline = setTimeout(() => reject(new Error('A blocked timeout response retained its pipelined socket')), 1800);
      socket.on('error', reject);
      socket.once('close', () => { clearTimeout(deadline); resolve(); });
    });
    socket.resume();
    socket.write('GET /held HTTP/1.1\r\nHost: localhost\r\n\r\nPOST /model HTTP/1.1\r\nHost: localhost\r\nContent-Length: 100\r\n\r\n{');
    const [body] = await Promise.all([secondRequest, closed]);
    assert.equal(body, null, 'The incomplete second request must be refused before input is admitted');
  } finally {
    socket.destroy();
    await new Promise(resolve => server.close(resolve));
  }
});
