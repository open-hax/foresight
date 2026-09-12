// SPDX-License-Identifier: LGPL-3.0-or-later
import Ajv from 'ajv';
import { randomUUID } from 'node:crypto';

const object = value => value !== null && typeof value === 'object' && !Array.isArray(value);
const namePattern = /^[A-Za-z0-9_-]{1,64}$/;

/** Compile the caller's offered schemas without coercion, defaults or field removal. */
export function checkedTools(body, supported) {
  if (Object.hasOwn(body, 'parallel_tool_calls') && typeof body.parallel_tool_calls !== 'boolean') {
    throw new RangeError('unsupported_parallel_tool_calls');
  }
  const tools = body.tools ?? [];
  if ((Object.hasOwn(body, 'tools') && !Array.isArray(body.tools)) || tools.length > 32
      || (!supported && tools.length)) throw new RangeError('unsupported_tools_or_stream');
  const offered = new Map();
  const ajv = new Ajv({ allErrors: true, strict: true, allowUnionTypes: true, strictRequired: false });
  for (const tool of tools) {
    const fn = tool?.function;
    if (!object(tool) || tool.type !== 'function' || !object(fn) || typeof fn.name !== 'string' || !namePattern.test(fn.name)
        || offered.has(fn.name) || !object(fn.parameters) || fn.parameters.type !== 'object'
        || (fn.description !== undefined && typeof fn.description !== 'string')) {
      throw new RangeError('unsupported_tools_or_stream');
    }
    let validate;
    try { validate = ajv.compile(fn.parameters); }
    catch { throw new RangeError('unsupported_tool_schema'); }
    offered.set(fn.name, validate);
  }
  const choice = body.tool_choice === undefined ? 'auto' : body.tool_choice;
  const forced = object(choice) && choice.type === 'function' && object(choice.function)
    && typeof choice.function.name === 'string' ? choice.function.name : null;
  if (!['auto', 'none', 'required'].includes(choice) && !forced) throw new RangeError('unsupported_tools_or_stream');
  if ((choice === 'required' && !tools.length) || (forced && !offered.has(forced))) throw new RangeError('unsupported_tools_or_stream');
  const selected = choice === 'none' ? [] : forced ? tools.filter(tool => tool.function.name === forced) : tools;
  return { tools: selected, offered, choice, forced, required: choice === 'required' || Boolean(forced),
    parallel: body.parallel_tool_calls !== false };
}

/** Preserve text bytes while decoding the SDK's text-part representation. */
function messageText(content, nullable = false) {
  if (content === null && nullable) return '';
  if (typeof content === 'string') return content;
  if (Array.isArray(content) && content.every(part => object(part) && part.type === 'text' && typeof part.text === 'string')) {
    return content.map(part => part.text).join('');
  }
  throw new RangeError('invalid_messages');
}

/** Decode OpenAI tool history to the pinned tokenizer's native chat-template shape. */
export function checkedMessages(messages) {
  if (!Array.isArray(messages) || messages.length < 1 || messages.length > 64) throw new RangeError('invalid_messages');
  const pending = new Map();
  const seen = new Set();
  const result = messages.map(message => {
    if (!object(message) || !['system', 'user', 'assistant', 'tool'].includes(message.role)
        || Object.hasOwn(message, 'function_call')) throw new RangeError('invalid_messages');
    const calls = message.tool_calls;
    if (Object.hasOwn(message, 'tool_calls') && (message.role !== 'assistant' || !Array.isArray(calls) || !calls.length)) {
      throw new RangeError('invalid_messages');
    }
    const content = messageText(message.content, message.role === 'assistant' && Boolean(calls?.length));
    if (content.length > 200000) throw new RangeError('invalid_messages');
    if (message.role === 'tool') {
      if (!pending.has(message.tool_call_id)) throw new RangeError('invalid_messages');
      pending.delete(message.tool_call_id);
      return { role: 'tool', content, tool_call_id: message.tool_call_id };
    }
    if (pending.size) throw new RangeError('invalid_messages');
    if (!calls) return { role: message.role, content };
    const decoded = calls.map(call => {
      if (!object(call) || typeof call.id !== 'string' || !call.id || call.id.length > 128 || seen.has(call.id)
          || call.type !== 'function' || !object(call.function) || typeof call.function.name !== 'string' || !namePattern.test(call.function.name)
          || typeof call.function.arguments !== 'string') throw new RangeError('invalid_messages');
      let args;
      try { args = JSON.parse(call.function.arguments); }
      catch { throw new RangeError('invalid_messages'); }
      if (!object(args)) throw new RangeError('invalid_messages');
      seen.add(call.id);
      pending.set(call.id, call.function.name);
      return { id: call.id, type: 'function', function: { name: call.function.name, arguments: args } };
    });
    return { role: message.role, content, tool_calls: decoded };
  });
  if (pending.size) throw new RangeError('invalid_messages');
  return result;
}

/** Extract only complete model-generated tool tags and validate each offered call. */
export function decodeGeneratedTools(content, finishReason, selection) {
  const calls = [];
  const text = [];
  let position = 0;
  while (position < content.length) {
    const start = content.indexOf('<tool_call>', position);
    if (start < 0) { text.push(content.slice(position)); break; }
    text.push(content.slice(position, start));
    const end = content.indexOf('</tool_call>', start + 11);
    if (end < 0) throw new Error('invalid_generated_tool_call');
    let generated;
    try { generated = JSON.parse(content.slice(start + 11, end)); }
    catch { throw new Error('invalid_generated_tool_call'); }
    if (!object(generated) || Object.keys(generated).some(key => !['name', 'arguments'].includes(key))
        || !selection.tools.some(tool => tool.function.name === generated.name)
        || !object(generated.arguments) || !selection.offered.get(generated.name)?.(generated.arguments)) {
      throw new Error('invalid_generated_tool_call');
    }
    calls.push({ id: `call_${randomUUID().replaceAll('-', '')}`, type: 'function',
      function: { name: generated.name, arguments: JSON.stringify(generated.arguments) } });
    if (calls.length > 32 || (!selection.parallel && calls.length > 1)) throw new Error('invalid_generated_tool_call');
    position = end + 12;
  }
  const remaining = text.join('');
  if (remaining.includes('<tool_call') || remaining.includes('</tool_call>')) throw new Error('invalid_generated_tool_call');
  if (calls.length && finishReason !== 'stop') throw new Error('incomplete_generated_tool_calls');
  if (selection.required && !calls.length) throw new Error('required_tool_call_missing');
  return { content: remaining || null, ...(calls.length ? { tool_calls: calls } : {}),
    finishReason: calls.length ? 'tool_calls' : finishReason };
}
