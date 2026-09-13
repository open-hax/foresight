# Synchronous tool validation and embedding response encoding

Current-head Codex and CodeRabbit review of root PR91 found two independent
provider-boundary defects. Both reproduced before source changes:

- An AJV schema with `$async: true` compiled to a promise-returning validator.
  The synchronous generated-call decoder could treat that promise as acceptance.
  The offered-schema boundary now refuses validators marked `$async` immediately
  after compilation, before model inference or argument validation. Automatic,
  required and disabled tool-choice modes all retain the same schema obligation.
- The Ollama-compatible `/api/embed` endpoint admitted `encoding_format: base64`
  but returned numeric vectors. It now refuses that request with HTTP400.
  Omitted encoding and explicit `float` still return equal, real MiniLM vectors;
  OpenAI-compatible embedding routes retain their supported base64 response.

The two focused regressions failed on the previous source, then passed after the
two refusal checks. The complete model HTTP/codec suite passed **85 tests, zero
failures or skips**, in 34.70 seconds on the restored Node24.20.0 runtime. This
uses actual pinned local models and preserves the existing malformed-provider,
normalization, streaming, cancellation, deadline and tool-history tests.

The old Qwen0.5B recovery measurement is now explicitly historical. The current
Wiki fixture link and publishing instructions select pinned Qwen1.5B. A working
transport and generated text do not establish publication quality or acceptance.

```sh
node --test devtools/generation-tools.test.mjs devtools/model-http.test.mjs
pnpm exec oxlint --deny-warnings devtools
```

Browser19 subsequently reached real writing assistance and source acceptance,
then failed its next delegated MCP request. The failure is retained separately;
this model-boundary suite does not claim completion of the browser workflow.
