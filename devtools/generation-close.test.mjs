// SPDX-License-Identifier: LGPL-3.0-or-later
import assert from 'node:assert/strict';
import http from 'node:http';
import {once} from 'node:events';
import {setImmediate as nextTurn} from 'node:timers/promises';
import {mock, test} from 'node:test';
import {InterruptableStoppingCriteria, PreTrainedModel} from '@huggingface/transformers';
import {LOCAL_GENERATION_MODEL, startGenerationServer} from './generation-server.mjs';

function deferred() {
  let resolve;
  const promise = new Promise(done => { resolve = done; });
  return {promise, resolve};
}

function generationProbe() {
  const entered = deferred(), release = deferred(), settled = deferred();
  const state = {entered, release, settled, generationSettled: false, earlyDisposals: 0, disposals: 0};
  const generate = PreTrainedModel.prototype.generate;
  const dispose = PreTrainedModel.prototype.dispose;
  const generating = mock.method(PreTrainedModel.prototype, 'generate', async function (options) {
    state.criteria = options.stopping_criteria;
    const actual = generate.call(this, options);
    entered.resolve();
    try {
      const tokens = await actual;
      state.tokenCount = tokens.dims.at(-1);
      // Hold only the completion boundary after real ONNX inference. No token
      // or success result is substituted; release makes the native race exact.
      await release.promise;
      return tokens;
    } finally {
      state.generationSettled = true;
      settled.resolve();
    }
  });
  const disposing = mock.method(PreTrainedModel.prototype, 'dispose', async function () {
    state.disposals++;
    if (!state.generationSettled) state.earlyDisposals++;
    // Observe premature disposal without allowing the RED case to corrupt a
    // live native session or dump its tensors. The actual disposal still runs.
    await settled.promise;
    return dispose.call(this);
  });
  const interrupting = mock.method(InterruptableStoppingCriteria.prototype, 'interrupt');
  return {...state, state, restore() {
    generating.mock.restore(); disposing.mock.restore(); interrupting.mock.restore();
  }, interruptCount: () => interrupting.mock.callCount()};
}

function startRequest(service) {
  const request = http.request(`${service.baseUrl}/chat/completions`, {method: 'POST',
    headers: {'content-type': 'application/json'}});
  const finished = new Promise(resolve => {
    request.on('error', error => resolve({error}));
    request.on('response', response => {
      response.resume();
      response.on('error', error => resolve({error}));
      response.on('end', () => resolve({status: response.statusCode}));
    });
  });
  request.end(JSON.stringify({model: service.model, max_tokens: 256,
    messages: [{role: 'user', content: 'Write a detailed paragraph about maintaining a shared wiki.'}]}));
  return {request, finished};
}

test('close interrupts a connected native inference before awaiting HTTP shutdown', async () => {
  const service = await startGenerationServer({model: LOCAL_GENERATION_MODEL});
  const probe = generationProbe();
  const exchange = startRequest(service);
  let closing;
  try {
    await probe.entered.promise;
    closing = service.close();
    assert(probe.interruptCount() > 0, 'shutdown must interrupt inference even while the client stays connected');
    assert.equal(probe.state.disposals, 0, 'disposal cannot begin while generation is unsettled');
  } finally {
    probe.state.criteria?.interrupt();
    probe.release.resolve();
    await exchange.finished;
    await (closing || service.close());
    probe.restore();
  }
  assert(probe.state.tokenCount > 0, 'the actual pinned model returned native tokens');
  assert.equal(probe.state.earlyDisposals, 0);
  assert.equal(probe.state.disposals, 1);
});

test('disconnect then repeated close waits for native generation settlement before one disposal', async () => {
  const service = await startGenerationServer({model: LOCAL_GENERATION_MODEL});
  const probe = generationProbe();
  const exchange = startRequest(service);
  const networkClosed = once(service.server, 'close');
  let closures, outcomes;
  try {
    await probe.entered.promise;
    exchange.request.destroy();
    closures = Promise.allSettled([service.close(), service.close()]);
    await networkClosed;
    await nextTurn();
    assert.equal(probe.state.earlyDisposals, 0, 'socket closure is not generation settlement');
    assert.equal(probe.state.disposals, 0);
  } finally {
    probe.state.criteria?.interrupt();
    probe.release.resolve();
    await exchange.finished;
    outcomes = await (closures || Promise.allSettled([service.close()]));
    probe.restore();
  }
  assert(outcomes.every(outcome => outcome.status === 'fulfilled'), 'repeated close shares successful cleanup');
  assert(probe.state.tokenCount > 0);
  assert.equal(probe.state.earlyDisposals, 0);
  assert.equal(probe.state.disposals, 1);
  await service.close();
});

test('an existing partial HTTP request cannot admit new inference after closing begins', async () => {
  const service = await startGenerationServer({model: LOCAL_GENERATION_MODEL, requestTimeoutMs: 200});
  const generating = mock.method(PreTrainedModel.prototype, 'generate');
  const payload = JSON.stringify({model: service.model, max_tokens: 1,
    messages: [{role: 'user', content: 'Hello.'}]});
  const accepted = once(service.server, 'request');
  const request = http.request(`${service.baseUrl}/chat/completions`, {method: 'POST', headers: {
    'content-type': 'application/json', 'content-length': Buffer.byteLength(payload),
  }});
  const finished = new Promise((resolve, reject) => {
    request.once('error', reject);
    request.once('response', response => {
      let body = '';
      response.setEncoding('utf8').on('data', text => { body += text; });
      response.once('error', reject);
      response.once('end', () => resolve({status: response.statusCode, body: JSON.parse(body)}));
    });
  });
  let closing;
  try {
    request.write(payload.slice(0, -1));
    await accepted;
    closing = service.close();
    request.end(payload.slice(-1));
    assert.deepEqual(await finished, {status: 503, body: {error: 'generation_closing'}});
    assert.equal(generating.mock.callCount(), 0);
  } finally {
    request.destroy();
    await (closing || service.close());
    generating.mock.restore();
  }
});
