# Ollama multimodal streaming reference

> Split out of `open-hax/knoxx` `docs/notes/reference/ollama-multimodal-stream-reference.md` (lines 12–91) at knoxx `0fdaae13` on 2026-09-30. Primary relationship: proxx (model providers). Why it left Knoxx: [knoxx-documentation-extraction.md](../lineage/knoxx-documentation-extraction.md).

# ollama cloud multi modal stream processing curl example

Here’s the shortest useful answer: Ollama examples in the sources use the `/api/generate` endpoint with JSON fields like `model`, `prompt`, and `stream`, and remote setups keep that same path behind a non-local base URL.  I could verify the streaming pieces, but I could not verify an official vendor-published “Ollama Cloud multimodal” cURL example from the material I found, so the multimodal payload below is a careful template rather than a confirmed canonical sample.[^1_1][^1_2][^1_3][^1_5]

## Verified facts

Ollama is shown exposing a REST API on port `11434`, with example requests sent to `http://localhost:11434/api/generate`.  The examples consistently include `model`, `prompt`, and a `stream` flag in the JSON body.[^1_2][^1_3][^1_5][^1_1]

A remote deployment pattern is also shown: one source binds Ollama to `0.0.0.0` for external access, and another fronts it with an HTTPS tunnel while still calling `/api/generate`.  A 2026 comparison source explicitly lists streaming support for Ollama.[^1_4][^1_5]

## cURL template

If your “cloud” endpoint is just a hosted or proxied Ollama server, this is the shape to try:

```bash
curl -N https://YOUR-OLLAMA-ENDPOINT/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama3.2-vision",
    "prompt": "Describe this image and extract any visible text.",
    "images": ["BASE64_IMAGE_HERE"],
    "stream": true
  }'
```


## Why this shape

The endpoint path and core request structure come directly from the verified Ollama examples: `/api/generate` plus JSON containing `model`, `prompt`, and `stream`.  The only unverified part here is the exact multimodal fielding for your specific hosted setup, because the retrieved sources did not include a first-party multimodal cURL sample.[^1_3][^1_5][^1_1][^1_2]

Use direct `http://your-host:11434` only inside a trusted private network. If untrusted clients can reach the endpoint, place it behind an authenticated gateway and HTTPS, such as `https://your-tunnel-domain`; an unauthenticated direct port is not a public deployment boundary.  For a non-streaming test, flip `"stream": false`, which is also shown in the examples.[^1_5][^1_2][^1_4]

## Stream consumption

For basic stream processing in shell, the practical move is to keep `-N` so `curl` does not buffer the response and let your downstream tool read incrementally. That part is standard `curl` behavior, while the reason to do it here is that Ollama supports streaming responses.[^1_4]

A minimal pipeline looks like this:

```bash
curl -N https://YOUR-OLLAMA-ENDPOINT/api/generate \
  -H "Content-Type: application/json" \
  -d '{
    "model": "llama3.2-vision",
    "prompt": "Summarize this image.",
    "images": ["BASE64_IMAGE_HERE"],
    "stream": true
  }' | jq -c .
```


## Failure modes

If this fails, the most likely issue is not the path but the payload shape for images, because the sources I found validate the endpoint and streaming flag but not the multimodal request schema.  The second likely issue is that your remote host is not actually exposing Ollama directly, in which case auth headers or a gateway-specific path may differ from plain `/api/generate`.[^1_1][^1_2][^1_5][^1_4]

Would you like a Bash snippet that base64-encodes an image file and feeds it into this request?
<span style="display:none">[^1_6][^1_7][^1_8][^1_9]</span>

<div align="center">⁂</div>

[^1_1]: https://github.com/CodersSampling/ollama_model

[^1_2]: https://dev.to/trish-xd/setup-rest-api-service-of-ai-by-using-local-llms-with-ollama-2d81

[^1_3]: https://www.russ.cloud/2024/03/29/running-llms-locally-with-ollama/

[^1_4]: https://www.spheron.network/blog/ollama-vs-vllm/

[^1_5]: https://blog.raghul.in/local-ai-server-for-free-with-ollama-cloudflare-tunnel

[^1_6]: https://dev.to/daswu/ollama-juicefs-pull-once-run-anywhere-3g6

[^1_7]: https://mjrovai.github.io/EdgeML_Made_Ease_ebook/raspi/llm/slm_intro.html

[^1_8]: https://www.instagram.com/p/DW1Lj3vkkpD/

[^1_9]: https://mlsysbook.ai/kits/contents/raspi/llm/llm.html


---
