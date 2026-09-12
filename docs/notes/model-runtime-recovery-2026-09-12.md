# Local model runtime recovery, 2026-09-12

## Signal

The existing Foresight Transformers.js providers run in the restored **same Linux sandbox**, with Node **24.20.0**, Transformers.js **3.8.1**, and CPU inference. Generation and embedding requests execute locally with remote model loading disabled. No prompt or repository content was sent to a model API.

| Capability | Pinned model | Revision | Quantization |
| --- | --- | --- | --- |
| Wiki generation | onnx-community/Qwen2.5-0.5B-Instruct | cc5cc01a65cc3ff17bdb73a7de33d879f62599b0 | q4 |
| Small generation baseline | HuggingFaceTB/SmolLM2-135M-Instruct | 12fd25f77366fa6b3b4b768ec3050bf629380bac | q4 |
| Embeddings | Xenova/all-MiniLM-L6-v2 | 751bff37182d3f1213fa05d7196b954e230abad9 | q8 |

## Evidence

Source checkpoint: 30b1d321a0184862eaf5f1e41cb25aee8c7ba18a. Provider scripts were unchanged. [Machine-readable evidence](evidence/model-runtime-recovery-2026-09-12.json) records all downloaded file SHA-256 digests, byte sizes, model responses, token counts, and runtime metadata. Cached model files total 1,001,059,632 bytes.

- Actual model HTTP tests: **61 passed, zero failed or skipped**, 4.973 seconds. This includes request body deadlines, oversized/stalled sockets, malformed input, unsupported tool calls, cancellation and bind failure cleanup.
- Qwen HTTP smoke: real generated Wiki sentence, Spanish translation, EOS-versus-token-limit handling, unsupported tools/model refusal. It translated “Hello, world.” as “Hola, mundo.”
- MiniLM HTTP smoke: three finite 384-dimensional unit vectors; semantically similar cat sentences score above the unrelated engine sentence; base64 is 1,536 bytes; invalid dimensions/model/encoding/input return 400.
- SmolLM2 also completed its transport smoke, but translated the same text as **“Hola, español.”** That is a translation error despite a successful HTTP response and valid JSON envelope.
- Both Transformers.js and native fs-ext imported successfully on the final Node runtime.

## Frames

A working transport proves the system can exercise real inference and failure handling. It does not prove generated content is accurate. The tiny Smol model supplies a useful failing quality example; Qwen's one correct translation does not establish broad translation competence. Human content and translation acceptance remain separate decisions.

The optional JSON translation envelope is produced by the transport around actual generated text. It is explicitly reported as `transport_wrapped_generated_text`; the small model does not enforce the schema itself.

## Countermoves

| Obstacle | Attempt and resolution |
| --- | --- |
| Workspace maintenance removed the previous model cache | Restored only files required by the existing immutable model revisions into one shared cache, avoiding alternate formats and full repositories. Hugging Face repository metadata confirmed the selected public models. |
| Root dependencies were absent | Frozen pnpm 10.14.0 install restored 273 packages using the shared content-addressed store. Child package-manager policies were preserved. |
| pnpm reported ignored dependency install scripts | Tested actual Transformers.js and fs-ext imports. Both succeeded from supplied package binaries; no broad install-script approval or policy change was needed. |
| Final Node binary was still being extracted | One activation attempt returned “Text file busy.” Waited for the runtime owner's checksummed extraction to finish, then reran imports and Qwen/MiniLM smoke tests on Node 24.20.0. |
| The HTTP suite explicitly requires the default Smol fixture | Initial run failed closed because only Qwen and MiniLM had been restored. Warmed the already-declared immutable Smol revision, reran the unmodified suite, and got 61 actual passes. No test was skipped or changed. |
| Tiny-model quality is inadequate for automatic acceptance | Preserved the incorrect Spanish output in evidence. Use the reviewed Qwen selection for the Wiki demonstration and keep acceptance gates authoritative. |

## Next

Run the Wiki's two publishing cycles against these same local providers and inspect the accepted correction memory in the second translation cycle.
