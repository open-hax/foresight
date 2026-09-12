// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import {startEmbeddingServer} from './embedding-server.mjs';
const service = await startEmbeddingServer();
try {
  const post = (body, endpoint = '/embeddings') => fetch(service.baseUrl + endpoint, {
    method: 'POST', headers: {'content-type': 'application/json'}, body: JSON.stringify(body),
  });
  const response = await post({model: service.model, input: ['A cat is sleeping on a sofa.', 'The kitten rests on a couch.', 'An engine powers a car.']});
  assert.equal(response.status, 200);
  const result = await response.json();
  assert.equal(result.data.length, 3);
  const vectors = result.data.map(item => item.embedding);
  for (const vector of vectors) {
    assert.equal(vector.length, 384);
    assert(vector.every(Number.isFinite));
    assert(Math.abs(Math.hypot(...vector) - 1) < 0.0001);
  }
  const similarity = (left, right) => left.reduce((sum, value, index) => sum + value * right[index], 0);
  assert(similarity(vectors[0], vectors[1]) > similarity(vectors[0], vectors[2]));
  const base64 = await (await post({input: 'A local draft.', encoding_format: 'base64'})).json();
  assert.equal(Buffer.from(base64.data[0].embedding, 'base64').length, 384 * 4);
  for (const invalid of [null, {input: 'x', dimensions: 0}, {input: 'x', model: 'missing'}, {input: 'x', encoding_format: 'unknown'}]) {
    assert.equal((await post(invalid)).status, 400);
  }
  console.log('PASS pinned offline MiniLM: 384 finite normalized dimensions, semantic ordering, base64, invalid-request refusal');
} finally {
  await service.close();
}
