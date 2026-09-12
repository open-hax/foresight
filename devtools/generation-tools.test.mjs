// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import { test } from 'node:test';
import { checkedMessages, checkedTools, decodeGeneratedTools } from './generation-tools.mjs';

const tool = { type: 'function', function: { name: 'save_translation', parameters: {
  type: 'object', additionalProperties: false, properties: { translated_text: { type: 'string', minLength: 1 },
    segment_index: { type: 'integer', minimum: 0 } }, required: ['translated_text', 'segment_index'],
} } };
const body = { tools: [tool], tool_choice: 'required' };
const tagged = args => `<tool_call>\n${JSON.stringify({ name: 'save_translation', arguments: args })}\n</tool_call>`;

test('tool history decodes arguments once and preserves admitted message text', () => {
  const messages = [{ role: 'system', content: 'Keep this prompt exactly.\n' },
    { role: 'user', content: [{ type: 'text', text: 'First ' }, { type: 'text', text: 'second\n' }] },
    { role: 'assistant', content: null, tool_calls: [{ id: 'call_one', type: 'function',
      function: { name: 'save_translation', arguments: '{"translated_text":"Hola","segment_index":0}' } }] },
    { role: 'tool', tool_call_id: 'call_one', content: 'accepted\n' }];
  const original = structuredClone(messages);
  const actual = checkedMessages(messages);
  assert.equal(actual[0].content, messages[0].content);
  assert.equal(actual[1].content, 'First second\n');
  assert.deepEqual(actual[2].tool_calls[0].function.arguments, { translated_text: 'Hola', segment_index: 0 });
  assert.equal(actual[3].content, 'accepted\n');
  assert.deepEqual(messages, original);
  assert.throws(() => checkedMessages(messages.slice(0, 3)), /invalid_messages/);
  assert.throws(() => checkedMessages([...messages, messages[3]]), /invalid_messages/);
});

test('only complete offered calls with valid arguments gain executable wire shape', () => {
  const selection = checkedTools(body, true);
  const raw = tagged({ translated_text: 'Hola', segment_index: 0 });
  const result = decodeGeneratedTools(raw, 'stop', selection);
  assert.equal(result.finishReason, 'tool_calls');
  assert.equal(result.tool_calls[0].function.name, 'save_translation');
  assert.deepEqual(JSON.parse(result.tool_calls[0].function.arguments), { translated_text: 'Hola', segment_index: 0 });
  for (const invalid of [{ translated_text: '', segment_index: 0 }, { translated_text: 'Hola', segment_index: '0' },
    { translated_text: 'Hola', segment_index: -1 }, { translated_text: 'Hola', segment_index: 0, secret: 'extra' }]) {
    assert.throws(() => decodeGeneratedTools(tagged(invalid), 'stop', selection), /invalid_generated_tool_call/);
  }
  assert.throws(() => decodeGeneratedTools(raw.replace('save_translation', 'publish_without_review'), 'stop', selection), /invalid_generated_tool_call/);
  assert.throws(() => decodeGeneratedTools(raw.slice(0, -3), 'length', selection), /invalid_generated_tool_call/);
  assert.throws(() => decodeGeneratedTools(raw, 'length', selection), /incomplete_generated_tool_calls/);
  assert.throws(() => decodeGeneratedTools('I did it.', 'stop', selection), /required_tool_call_missing/);
  assert.throws(() => decodeGeneratedTools('</tool_call>', 'stop', selection), /invalid_generated_tool_call/);
});

test('schema compilation refuses unsupported obligations instead of ignoring them', () => {
  assert.throws(() => checkedTools({ tools: [{ ...tool, function: { ...tool.function,
    parameters: { ...tool.function.parameters, unknownValidationKeyword: true } } }] }, true), /unsupported_tool_schema/);
  assert.throws(() => checkedTools({ tools: [tool, tool] }, true), /unsupported_tools_or_stream/);
  assert.throws(() => checkedTools({ tools: [tool], tool_choice: { type: 'function', function: { name: 'unoffered' } } }, true), /unsupported_tools_or_stream/);
  assert.throws(() => checkedTools({ tools: [tool] }, false), /unsupported_tools_or_stream/);
});

test('the caller can prohibit multiple generated calls without changing its prompt', () => {
  const raw = tagged({ translated_text: 'Hola', segment_index: 0 });
  const serial = checkedTools({ ...body, parallel_tool_calls: false }, true);
  assert.equal(decodeGeneratedTools(raw, 'stop', serial).tool_calls.length, 1);
  assert.throws(() => decodeGeneratedTools(`${raw}\n${raw}`, 'stop', serial), /invalid_generated_tool_call/);
  const parallel = checkedTools({ ...body, parallel_tool_calls: true }, true);
  assert.equal(decodeGeneratedTools(`${raw}\n${raw}`, 'stop', parallel).tool_calls.length, 2);
  assert.throws(() => checkedTools({ ...body, parallel_tool_calls: 'false' }, true), /unsupported_parallel_tool_calls/);
});
