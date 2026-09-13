// SPDX-License-Identifier: LGPL-3.0-or-later
import {InterruptableStoppingCriteria} from '@huggingface/transformers';

const reportObserverFailure = () => console.error('[generation-observer] observer_failed');

/** Observe only native token counts; preserve the original stopping decision. */
export class ObservedStoppingCriteria extends InterruptableStoppingCriteria {
  constructor(observation) {
    super();
    this.observation = observation;
  }

  _call(inputIds, scores) {
    const stopped = super._call(inputIds, scores);
    this.observation.step(inputIds[0].length);
    return stopped;
  }
}

/** Build an optional metadata sink with no access to content, tokens, or errors. */
export function generationObservation(observer, {requestId, model, maxTokens}) {
  if (!observer) return null;
  const started = performance.now();
  let promptTokens, generatedTokens = 0, lastCount = 0, lastProgress = started;
  let interruption, completed = false, settled = false, failure = 'generation_failed';

  const emit = (stage, fields = {}) => {
    const event = Object.freeze({stage, requestId, model, maxTokens,
      elapsedMs: performance.now() - started,
      ...(promptTokens === undefined ? {} : {promptTokens, generatedTokens}), ...fields});
    try {
      // A sink must not alter inference or turn a rejected async sink into an
      // unhandled rejection. Its result is never used for model decisions.
      const result = observer(event);
      if (result && typeof result.then === 'function') Promise.resolve(result).catch(reportObserverFailure);
    } catch {
      reportObserverFailure();
    }
  };

  return {
    admitted() { emit('admitted'); },
    prefill(count) {
      promptTokens = count;
      emit('prefill');
    },
    step(sequenceLength) {
      generatedTokens = sequenceLength - promptTokens;
      const now = performance.now();
      if (generatedTokens <= 0 || settled) return;
      if (lastCount === 0 || Math.floor(generatedTokens / 64) > Math.floor(lastCount / 64)
          || now - lastProgress >= 5000) {
        emit(lastCount === 0 ? 'first-token' : 'progress');
        lastCount = generatedTokens;
        lastProgress = now;
      }
    },
    interrupt(code) {
      if (settled || interruption) return;
      interruption = code;
      emit('interruption-requested', {code});
    },
    complete(usage, finishReason) {
      if (interruption) return;
      promptTokens = usage.prompt_tokens;
      generatedTokens = usage.completion_tokens;
      completed = true;
      emit('completed', {totalTokens: usage.total_tokens, finishReason});
    },
    fail(code) {
      completed = false;
      failure = code;
    },
    settle() {
      if (settled) return;
      settled = true;
      emit('settled', interruption ? {outcome: 'interrupted', code: interruption}
        : completed ? {outcome: 'completed'} : {outcome: 'failed', code: failure});
    },
  };
}
