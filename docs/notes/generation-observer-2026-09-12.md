# Native generation progress without content logging

The browser translation run stayed in flight without enough evidence to tell
whether its model request had arrived, was processing its prompt, or was still
generating tokens. The local generation service now accepts an optional
`observer(event)` callback. The worker can write these events to its owned log;
the service emits no request telemetry by default.

Each frozen event contains a server-generated request UUID, pinned model name,
effective token budget, stage, and elapsed milliseconds. Once tokenization has
completed, it also carries the prompt count and the latest observed native
generated-token count. No prompt, generated text, token IDs, credentials, tools,
headers, native error object, or arbitrary error message enters this callback.

| Stage | Evidence |
| --- | --- |
| `admitted` | Validated request owns the single inference slot. |
| `prefill` | Tokenization and context admission passed; native generation is about to begin. |
| `first-token` | The native stopping criterion observed the first generated token. |
| `progress` | A native step crossed a 64-token boundary or five seconds elapsed since the last reported step. |
| `completed` | Actual model output decoded and passed the transport's content/tool validation; exact usage and finish reason are available. |
| `interruption-requested` | Timeout, client disconnect, or service shutdown requested interruption. This does not claim native settlement. |
| `settled` | The admitted request reached its final cleanup, with a completed, failed, or interrupted outcome. |

An elapsed threshold is checked at actual native generation steps. It is not an
independent heartbeat, and an uninterrupted long native call cannot publish a
step or immediately execute a JavaScript timer. A `prefill` record with no token
record identifies that boundary without inventing progress. Likewise, a
`completed` record does not establish client delivery: a later terminal write can
fail, in which case `settled` reports failure. Interrupted/failed token counts are
the last native step observed, not a claim that a complete answer was delivered.

## Review decisions

`generation-observer.mjs` returns the original stopping criterion's decision
unchanged. With no callback configured, the server uses the original criterion
class. The observer receives new frozen primitive-only data and cannot mutate
model inputs. Synchronous sink throws and asynchronous sink rejections are
contained and reported with the fixed `observer_failed` marker. Their error
contents never enter the telemetry or alter the generation response.

The existing shutdown join remains in place. The shared close promise is assigned
before notifying an interruption observer so a reentrant callback shares that
same promise. The final event follows cleanup of inference ownership. A review
found that a failed terminal write could retain an earlier completed flag; a
native failure-first regression reproduced this and the failure transition now
clears that flag before final settlement.

## Verification

The tests use the pinned local Transformers.js models and actual ONNX inference.
They do not substitute generated tokens or a success result. Two narrow failure
fixtures throw only after real native text or after native completion at the
terminal SSE write. The no-callback comparison checks identical deterministic
choices, usage, and provenance with and without a failing observer.

```sh
node --test devtools/model-http.test.mjs devtools/generation-tools.test.mjs \
  devtools/generation-close.test.mjs devtools/generation-observer.test.mjs
pnpm exec oxlint --deny-warnings devtools/generation-server.mjs \
  devtools/generation-observer.mjs devtools/generation-observer.test.mjs
```

The first native progress/privacy regression failed against the original server
because it produced no events. The first five native observer cases then passed,
as did the existing 88 model, HTTP, tool-validation, and shutdown cases. The
independent review's terminal-write regression failed with `completed` instead of
`failed`, before its narrow repair. Final results, immutable raw receipts, and
the tested source hashes are recorded in
[`generation-observer.json`](evidence/generation-observer.json).

The final combined run passed **94 tests, zero failures, cancellations, or skips**
in 30.627 seconds. Scoped Oxlint passed with zero errors and warnings. A separate
read-only peer reviewed the three frozen source hashes and found no remaining
issue in metadata closure, sink failures, stopping decisions, repeated shutdown,
or final outcome classification. That peer review did not rerun the native tests.

Worker JSONL adoption and the next full browser translation run are separate
integration proofs. These tests establish native observation and lifecycle
behavior, not completion of the earlier timed-out translation workflow.
