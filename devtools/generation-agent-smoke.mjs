// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import { findPackageJSON } from 'node:module';
import { readFile, realpath } from 'node:fs/promises';
import { pathToFileURL } from 'node:url';
import { startGenerationServer } from './generation-server.mjs';

// Exercise the exact SDK installed by Knoxx, respecting its published export path.
const cliPackage = pathToFileURL(await realpath(new URL('../knoxx/backend/node_modules/@open-hax/eta-mu-cli/package.json', import.meta.url)));
const aiPackage = pathToFileURL(findPackageJSON('@open-hax/eta-mu-ai', cliPackage));
const manifest = JSON.parse(await readFile(aiPackage, 'utf8'));
const { streamOpenAICompletions } = await import(new URL(manifest.exports['./openai-completions'].import, aiPackage));
const service = await startGenerationServer({ model: 'onnx-community/Qwen2.5-1.5B-Instruct', maxNewTokens: 1024 });
try {
  const model = { id: service.model, name: service.model, api: 'openai-completions', provider: 'transformers-js',
    baseUrl: service.baseUrl, reasoning: false, input: ['text'], contextWindow: 8192, maxTokens: 1024,
    cost: { input: 0, output: 0, cacheRead: 0, cacheWrite: 0 } };
  const tool = { name: 'save_translation', description: 'Save a completed translation for the supplied segment.',
    parameters: { type: 'object', additionalProperties: false,
      properties: { translated_text: { type: 'string', minLength: 1 }, split_id: { type: 'string' },
        attempt_id: { type: 'string' }, segment_index: { type: 'integer', minimum: 0 } },
      required: ['translated_text', 'split_id', 'attempt_id', 'segment_index'] } };
  const systemPrompt = 'Translate the supplied text into Spanish, then call save_translation exactly once. Copy the supplied IDs without modification. After a successful tool result, briefly confirm that the translation was saved.';
  const userText = 'Text: Hello, world.\nsplit_id: section-one\nattempt_id: attempt-one\nsegment_index: 0';
  const context = { systemPrompt, tools: [tool], messages: [{ role: 'user', content: userText, timestamp: Date.now() }] };
  const payloads = [];
  const run = async options => {
    const types = [];
    const started = performance.now();
    const stream = streamOpenAICompletions(model, context, {
      apiKey: 'local', maxTokens: 256, maxRetries: 0, timeoutMs: 180000, ...options,
      onPayload(payload) { payloads.push(structuredClone(payload)); },
    });
    for await (const event of stream) types.push(event.type);
    const message = await stream.result();
    assert.ok(!['error', 'aborted', 'length'].includes(message.stopReason), JSON.stringify(message));
    assert.ok(message.usage.output > 0);
    return { message, event_types: types, seconds: (performance.now() - started) / 1000 };
  };
  const first = await run({ toolChoice: 'required' });
  assert.equal(first.message.stopReason, 'toolUse');
  assert.ok(first.event_types.includes('toolcall_end'));
  const calls = first.message.content.filter(part => part.type === 'toolCall');
  assert.equal(calls.length, 1);
  const call = calls[0];
  assert.equal(call.name, tool.name);
  assert.equal(call.arguments.split_id, 'section-one');
  assert.equal(call.arguments.attempt_id, 'attempt-one');
  assert.equal(call.arguments.segment_index, 0);
  assert.match(call.arguments.translated_text, /hola.*mundo/i);
  // This is the real local test tool effect, executed only from the SDK's generated call.
  const saved = new Map();
  saved.set(call.arguments.split_id, structuredClone(call.arguments));
  const toolResult = { saved: saved.has(call.arguments.split_id), split_id: call.arguments.split_id };
  context.messages.push(first.message, { role: 'toolResult', toolCallId: call.id, toolName: call.name,
    content: [{ type: 'text', text: JSON.stringify(toolResult) }], isError: false, timestamp: Date.now() });
  const second = await run({ toolChoice: 'none' });
  assert.equal(second.message.stopReason, 'stop');
  assert.ok(second.event_types.includes('text_delta'));
  assert.ok(second.message.content.some(part => part.type === 'text' && part.text.trim()));
  assert.ok(second.message.content.every(part => part.type !== 'toolCall'));
  for (const payload of payloads) {
    assert.equal(payload.stream, true);
    assert.equal(payload.messages[0].content, systemPrompt);
    assert.equal(payload.messages[1].content, userText);
  }
  assert.equal(payloads[1].messages.at(-1).role, 'tool');
  assert.equal(payloads[1].messages.at(-1).tool_call_id, call.id);
  console.log(JSON.stringify({ status: 'pass', sdk: `${manifest.name}@${manifest.version}`, model: service.model,
    first, tool_result: toolResult, second, prompts_preserved: true }, null, 2));
} finally { await service.close(); }
