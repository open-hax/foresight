// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import { startGenerationServer } from './generation-server.mjs';

const service = await startGenerationServer();
try {
  const post = async body => {
    const response = await fetch(`${service.baseUrl}/chat/completions`, { method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ model: service.model, ...body }) });
    return { status: response.status, body: await response.json() };
  };
  const writing = await post({ messages: [{ role: 'user', content: 'Write one short sentence explaining what a wiki is.' }], max_tokens: 128 });
  assert.equal(writing.status, 200);
  assert.equal(writing.body.choices[0].finish_reason, 'stop');
  assert.ok(writing.body.choices[0].message.content.trim());
  assert.equal(writing.body.local_generation.offline, true);
  const translation = await post({
    messages: [{ role: 'user', content: 'Translate from English into Spanish. Return only the translated text.\n\nHello, world.' }], max_tokens: 128,
    response_format: { type: 'json_schema', json_schema: { name: 'translation', strict: true, schema: { type: 'object', additionalProperties: false, properties: { translated_text: { type: 'string', minLength: 1 } }, required: ['translated_text'] } } },
  });
  assert.equal(translation.status, 200);
  assert.equal(translation.body.choices[0].finish_reason, 'stop');
  assert.ok(JSON.parse(translation.body.choices[0].message.content).translated_text.trim());
  assert.equal(translation.body.local_generation.structured_output, 'transport_wrapped_generated_text');
  const truncated = await post({ messages: [{ role: 'user', content: 'Write a detailed essay about the history of wikis.' }], max_tokens: 1 });
  assert.equal(truncated.status, 200);
  assert.equal(truncated.body.choices[0].finish_reason, 'length');
  const invalid = await post({ messages: [{ role: 'user', content: 'Hello' }], tools: [{}] });
  assert.equal(invalid.status, 400);
  const unknown = await post({ model: 'other', messages: [{ role: 'user', content: 'Hello' }] });
  assert.equal(unknown.status, 400);
  console.log(JSON.stringify({ status: 'pass', writing: writing.body, translation: translation.body, truncated: truncated.body, rejected_tools: invalid.body, rejected_model: unknown.body }, null, 2));
} finally {
  await service.close();
}
