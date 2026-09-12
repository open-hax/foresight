# Local model recovery and agent transport

Observed on 2026-09-12 in the recovered Foresight sandbox, with Node 24.20.0,
Transformers.js 3.8.1, CPU execution (two intra-operation threads, one
inter-operation thread), and the installed Knoxx eta-mu-ai SDK 0.70.7.

## Obstacles and resolutions

| Obstacle | Actual attempt and result | Resolution or remaining limit |
| --- | --- | --- |
| Workspace cleanup removed all downloaded models | Loaded each pinned model with the existing Transformers.js warming path; Smol took 19.32 seconds, Qwen 0.5B 39.82 seconds, MiniLM 28.92 seconds, and Qwen 1.5B 95.50 seconds. | All four models loaded and disposed successfully. Startup disables remote model access; downloads happen only during explicit warming. The recovered cache occupies approximately 3.6 GiB. |
| Agent SDK always requests streaming | A real HTTP request with `stream: true` and `max_completion_tokens` failed with HTTP 400 `unsupported_tools_or_stream` before this change. | Native generated text now becomes OpenAI SSE deltas; the existing JSON completion path remains available. Both token-limit spellings are admitted and capped. |
| Tools were refused | A direct Qwen 1.5B inference generated a complete `save_translation` XML call with Spanish text, 47 tokens in 17.66 seconds. The former server could not represent it. | The pinned tokenizer's native tool template receives the caller's offered tools. Only complete generated calls with allowed names and schema-valid arguments become executable SDK tool calls. |
| The Wiki provider was configured only for its simple writing client | Translation agents use eta-mu provider registration separately. | The Wiki supervisor now registers `transformers-js` with its actual loopback endpoint and a local placeholder credential; the existing Wiki writing configuration is retained. |
| Local model contracts exaggerated capacity | Smol and Qwen 0.5B fixtures advertised 32,768 context tokens and 2,048 output tokens. | All local generation fixtures now declare the actual service limits: 8,192 total context tokens and 1,024 output tokens. The Wiki supervisor explicitly enables the 1,024-token budget. |
| Small models can produce fluent but wrong translations | In the actual Smol smoke run, `Hello, world.` became `Hola, español.`. The transport smoke correctly established only valid nonempty generated output. | The Wiki fixture selects pinned Qwen 1.5B. A successful simple Qwen translation is evidence of capability, not a guarantee for arbitrary publication work. Content and translation acceptance still require the application's review process. |

## Exact model inputs

| Model | Immutable Hub revision | Quantization |
| --- | --- | --- |
| `HuggingFaceTB/SmolLM2-135M-Instruct` | `12fd25f77366fa6b3b4b768ec3050bf629380bac` | q4 |
| `onnx-community/Qwen2.5-0.5B-Instruct` | `cc5cc01a65cc3ff17bdb73a7de33d879f62599b0` | q4 |
| `onnx-community/Qwen2.5-1.5B-Instruct` | `6287331f475a3e20e8c879be8fd4bf3551ad9d34` | q4 |
| `Xenova/all-MiniLM-L6-v2` | `751bff37182d3f1213fa05d7196b954e230abad9` | q8 |

The [pinned Qwen 1.5B ONNX artifact](https://huggingface.co/onnx-community/Qwen2.5-1.5B-Instruct/blob/6287331f475a3e20e8c879be8fd4bf3551ad9d34/onnx/model_q4.onnx)
was independently measured at **1,787,566,590 bytes**, SHA-256
`70c24509f760fed9a8f50391165c46a87660197028a42c4547763f1c173ddea6`.
Its config, generation config, tokenizer and tokenizer config were downloaded
from that same revision. No prompt or repository content is uploaded by warming.

## Transport contract

The original system and user text is preserved. SDK text parts are concatenated
in order; historical tool arguments are decoded once from JSON strings to the
object representation required by the pinned tokenizer's chat template. The
template's own tool instructions are used. There is no substitute prompt,
repair prompt, hidden retry, replacement answer, or invented tool invocation.

AJV 8.17.1 validates the JSON Schemas actually offered by the caller. This avoids
implementing an incomplete home-grown subset of JSON Schema at the Node
transport boundary. Compilation is strict; unknown validation keywords and
unsupported formats are refused. Validation does not coerce values, add defaults,
or remove fields. Unoffered names, malformed JSON, schema-invalid arguments,
unclosed tags, and calls ending at the token limit cannot become executable
calls. Required or forced tool choices fail if the model does not comply.
An explicit `parallel_tool_calls: false` also refuses a generated completion
containing multiple calls; no prompt alteration is used to enforce this.

Plain text streams incrementally from the real Transformers.js `TextStreamer`.
When tools are offered, the service buffers the model output until EOS and
validation, then emits validated OpenAI tool-call chunks. Consequently a tool
turn can have a noticeable wait before its first byte. Tool IDs are transport
identifiers generated locally; names and arguments come from decoded model
output. Successful tool responses retain that raw generated output in
`local_generation.raw_tool_output` for inspection.

The translation JSON envelope explicitly identifies itself as
`transport_wrapped_generated_text`: it encodes actual generated text as JSON
but does not prove that the text is a correct translation. Native token usage,
EOS versus token limit, pinned revision, quantization, and CPU provenance remain
visible. A failure after streaming begins produces an SSE error, never a
successful finish marker. The server retains its single-inference lease,
loopback binding, input limits, ingestion deadline and interruptible generation
deadline. This is a local development inference adapter, not a complete
implementation of every OpenAI request parameter.

Peer source review found no confirmed blocking defect and identified the
previously ignored `parallel_tool_calls` flag; its constraint is now enforced.
Self-review also found that the legacy translation envelope could admit extra
schema constraints such as `maxLength` or `pattern` without enforcing them.
Those unsupported obligations now receive HTTP 400 before inference.

## Verification and reproduction

Warm the immutable models once, with `FORESIGHT_MODEL_CACHE` pointing at the
shared cache, then run the offline checks from the Foresight root:

```sh
node devtools/warm-generation-model.mjs onnx-community/Qwen2.5-1.5B-Instruct
node --test devtools/generation-tools.test.mjs devtools/model-http.test.mjs
node devtools/generation-smoke.mjs
node devtools/generation-agent-smoke.mjs
pnpm exec oxlint --deny-warnings devtools
```

The agent smoke requires Knoxx's installed `@open-hax/eta-mu-cli` dependency and
resolves its actual published `@open-hax/eta-mu-ai/openai-completions` export.
It does not install a second SDK or mock its HTTP client.

Observed evidence:

- Restored pre-change real HTTP suite: **74 passed, zero failures or skips**.
- The expanded native HTTP and codec suite: **81 passed, zero failures or skips**.
  It checks malformed tools, exact offered
  schema validation, history pairing, unchanged prompts, deadline classification,
  and a deliberately injected failure after actual model text has streamed.
  The injection is a failure test, not a claim of generated model behavior.
- Actual Smol writing: **29 output tokens**, **22 text deltas** received in
  **22 network chunks**, byte-for-byte equal to the deterministic nonstreamed
  completion, with equal token usage.
- Actual Qwen SDK first turn: **46 generated tokens in 15.24 seconds**, producing
  `save_translation` with `translated_text: "Hola, mundo."`, `split_id:
  "section-one"`, `attempt_id: "attempt-one"`, and `segment_index: 0`.
- The smoke's local save tool executes only from those received SDK arguments.
  The next SDK request includes that call and its real tool result. The model
  then generated a **31-token confirmation in 8.24 seconds**, streamed as text.
  Both requests retained the original system and user text exactly.

These checks establish the model/SDK transport boundary. The browser publication
run independently establishes whether the application's real prompts, review
rules, tools, and UI complete the requested workflow.
