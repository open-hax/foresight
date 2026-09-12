# Local translation model follow-up

Browser run 16 completed the identity, administration, Mail, Contracts and source
review stages, then correctly refused the Qwen2.5-0.5B fallback because its
`translated_text` repeated `source_text`. The automatic agent also lacked the
separate eta-mu provider mapping. Both obstacles remain recorded; a working Wiki
text endpoint does not establish streaming agent or tool support.

The Hugging Face repository details tool confirmed that
[onnx-community/Qwen2.5-1.5B-Instruct](https://huggingface.co/onnx-community/Qwen2.5-1.5B-Instruct)
targets Transformers.js. The plugin's model-search tool returned `Tool
model_search not found`; repository details and the official Hub API provided
the metadata instead. The selected immutable revision is
`6287331f475a3e20e8c879be8fd4bf3551ad9d34`, using CPU `q4`.

Only five required files were downloaded into the shared model cache, in 75.9
seconds. The ONNX file is 1,787,566,590 bytes and its SHA256 matches the Hub LFS
object: `70c24509f760fed9a8f50391165c46a87660197028a42c4547763f1c173ddea6`.
No model inference sends repository content to the Hub; execution is offline.

The first standalone evaluation loaded the package's CommonJS namespace through
a dynamic import and therefore did not find its named `env` export. Selecting
that namespace's default export fixed the loader. The successful successor uses
the same Node24/Transformers.js3.8.1 runtime, two CPU inference threads, the actual
translator contract's system prompt, and structured source-split input. It does
not change or strip generated text.

| Source | Actual generated text | Result |
| --- | --- | --- |
| `# Shared knowledge` | A Markdown code fence containing `# Conocimientos compartidos` | Spanish, with an unwanted code fence; 17.5 seconds, 12 completion tokens |
| Humans make the final high-level acceptance decisions. Agents contribute through the same authorized review workflow. | `"Humanos realizan las decisiones de aceptación finales de alto nivel. Los agentes contribuyen a través del mismo flujo de revisión autorizado."` | Spanish, with unwanted enclosing quotes; 15.3 seconds, 35 tokens |
| A new revision from the agent requires fresh acceptance. | `Una nueva revisión del agente requiere una aceptación nueva.` | Spanish; 18.3 seconds, 15 tokens |

All three ended on the model's EOS token within the 256-token limit. This is a
small inspected quality sample, not a language-quality benchmark or a passing
agent-tool test. Quotes, fences and wording must be reviewed through the normal
correction workflow. Provider mapping, genuine generated tool calls, streaming
transport and the full two-cycle browser tour remain separate gates. The
SmolLM2-135M and Qwen0.5B pins and their earlier failure evidence are retained.
