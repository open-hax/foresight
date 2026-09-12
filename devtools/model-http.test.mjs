// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import http from 'node:http';
import { setTimeout as delay } from 'node:timers/promises';
import { after, before, mock, test } from 'node:test';
import { PreTrainedTokenizer } from '@huggingface/transformers';
import { startEmbeddingServer } from './embedding-server.mjs';
import { LOCAL_GENERATION_MODEL, startGenerationServer } from './generation-server.mjs';

let generation;
let embedding;
before(async () => {
  generation = await startGenerationServer({ model: LOCAL_GENERATION_MODEL, timeoutMs: 1 });
  embedding = await startEmbeddingServer();
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

test('an interrupted real local inference reports the expired deadline', async () => {
  assert.deepEqual(await completion({ max_tokens: 128 }), {
    status: 504, body: { error: 'generation_timeout' },
  });
  const health = await (await fetch(generation.baseUrl.replace('/v1', '/health'))).json();
  assert.equal(health.busy, false);
});

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
