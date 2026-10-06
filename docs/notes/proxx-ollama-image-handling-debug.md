# Proxx Ollama image-handling debugging session

> Split out of `open-hax/knoxx` `docs/notes/reference/ollama-multimodal-stream-reference.md` (lines 92–337) at knoxx `0fdaae13` on 2026-09-30. Primary relationship: proxx. Why it left Knoxx: [knoxx-documentation-extraction.md](../lineage/knoxx-documentation-extraction.md).

# review orgs/open-hax/proxx/src with the knoxx nrepl tool. Tell me if we are handling images correctly for ollama cloud

I now have full visibility into all the relevant files. Here is the detailed review.

***

## The Core Bug: Images Are Silently Dropped

**`ollama-compat.ts` — `chatMessagesToOllamaMessages()`** is the critical path for ollama-cloud requests, and it **drops all image content parts**.

Look at `contentToText()` in `ollama-compat.ts`:

```ts
// In ollama-compat.ts contentToText()
if (type === "text") {
  return asString(part["text"]) ?? "";
}
if (type === "input_text") {
  return asString(part["text"]) ?? "";
}
return "";  // ← image_url parts fall here and become empty string
```

The function converts each message's content to a plain `string` via `contentToText`. Any `image_url` part returns `""` and vanishes. The final message sent to `POST /api/chat` becomes:

```json
{ "role": "user", "content": "Describe this image." }
```

No `images` field. No base64 data. **Ollama native `/api/chat` expects images as a top-level `images: string[]` array on the message object**, not embedded in content parts.

***

## What the Payload Needs to Look Like

Ollama's native `/api/chat` multimodal format is:

```json
{
  "model": "llava",
  "messages": [
    {
      "role": "user",
      "content": "Describe this image.",
      "images": ["BASE64_IMAGE_DATA"]
    }
  ]
}
```

The `images` field is a sibling of `content` on the message, not a content-part type.

***

## `messages-compat.ts` Is Irrelevant Here

`messages-compat.ts` has solid image normalization logic (`normalizeImagePart`, `parseBase64DataUrl`, etc.) but it's only used for the Anthropic `/v1/messages` upstream path.  The ollama-cloud strategy calls `chatRequestToOllamaRequest` → `chatMessagesToOllamaMessages`, which never touches any of that code.

***

## `vision.ts` Routing Is Fine

`vision.ts` correctly routes `Kimi-K2.5` to `ollama-cloud` with priority ordering.  The routing decision itself isn't broken — it's the serialization that fails downstream.

***

## Where the Fix Must Live

In `ollama-compat.ts`, `chatMessagesToOllamaMessages()` needs to be upgraded to detect and extract images from multipart content. The fix shape:

```ts
function chatMessagesToOllamaMessages(messages: unknown): Array<{
  role: string;
  content: string;
  images?: string[];
}> {
  // ...for each message:
  const textParts: string[] = [];
  const images: string[] = [];

  for (const part of contentArray) {
    if (part.type === "text") textParts.push(part.text);
    if (part.type === "image_url") {
      const url = part.image_url?.url ?? part.url;
      // REST requires base64 data; reject raw URLs in this sketch.
      images.push(stripBase64DataUriPrefix(url));
    }
  }

  const msg: Record<string, unknown> = {
    role,
    content: textParts.join(""),
  };
  if (images.length > 0) msg.images = images;
```

Ollama expects raw base64 (no `data:image/...;base64,` prefix) in the `images` array.

***

## Summary of Issues

| File | Issue | Severity |
| :-- | :-- | :-- |
| `ollama-compat.ts` → `contentToText` | Image parts silently dropped to `""` | 🔴 Critical |
| `ollama-compat.ts` → `chatMessagesToOllamaMessages` | No `images` field extracted or appended to message | 🔴 Critical |
| `messages-compat.ts` | Has good image normalization but is never invoked for ollama-cloud | 🟡 Misleading (potential reuse opportunity) |
| `vision.ts` | Routing and fallback chain correct | ✅ Fine |
| `ollama-cloud.ts` | Stream/non-stream handling correct, but fed broken payload | 🟡 Downstream victim |

The `parseBase64DataUrl` utility already written in `messages-compat.ts` could be extracted to a shared module and reused in the ollama message builder — that would be the clean refactor path.

---

# we're getting 400's whenever we send images to gemma4:31b

The historical report associates image requests with HTTP 400 responses, but
contains no exact error response tying that status to a particular conversion.
The trace below establishes image loss; the reported status remains unverified.

***

## Root Cause: Two Different Code Paths, Both Broken for Images

The historical sketch shows the same image-loss path in the local
`OllamaProviderStrategy` and ollama-cloud adapters. It does not establish the
cause of the reported `gemma4:31b` HTTP 400 responses.

### Path 1: `OllamaProviderStrategy` (explicit ollama, `context.explicitOllama = true`)

`buildPayload` calls `chatRequestToOllamaRequest` → `chatMessagesToOllamaMessages` → `contentToText`.  This is **identical** to the ollama-cloud path — images are silently dropped to `""`, no `images` field is appended to the message. The retained payload loses vision input. An optional `images` field is not, by
itself, proof of an HTTP 400; retain the actual upstream error body, revision,
and exact request before attributing that status to this path.

### Path 2: `LocalOllamaProviderStrategy` (`context.localOllama && !context.explicitOllama`)

This path calls `buildRequestBodyForUpstream(context)` — which passes the **OpenAI-format body through mostly unchanged**.  Ollama's `/api/chat` endpoint does **not** accept OpenAI-style `content: [{type: "image_url", url: "..."}]` content parts. That payload is incompatible with the documented native message shape; the
actual HTTP response must still be recorded to establish the reported failure.

### Path 3: `ollama-native.ts` — Correctly Passes Images (but only for native `/api/chat` → proxy direction)

Notably, `nativeChatToOpenAiRequest` in `ollama-native.ts` does preserve `images` from the message object:

```ts
images: Array.isArray(entry["images"]) ? entry["images"] : undefined,
```

But this function converts **native Ollama → OpenAI format** (for the inbound proxy direction). It never runs on the **outbound** path to Ollama. So images are preserved going *in*, but lost going *out*.

***

## The Exact Failure Sequence for `gemma4:31b`

```
Client sends:
  POST /v1/chat/completions
  { model: "gemma4:31b",
    messages: [{ role: "user", content: [
      { type: "text", text: "Describe this" },
      { type: "image_url", image_url: { url: "data:image/jpeg;base64,..." } }
    ]}]}

→ chatRequestToOllamaRequest()
→ chatMessagesToOllamaMessages()
→ contentToText() strips image_url part → ""

Sent to Ollama:
  POST /api/chat
  { model: "gemma4:31b",
    messages: [{ role: "user", content: "Describe this" }] }
                                           ↑ no images field

Established result: request became text-only and lost its image input.
HTTP result: not retained in this trace; the reported 400 cause is unresolved.
```


***

## The Fix (same fix, applied once)

In `ollama-compat.ts`, `chatMessagesToOllamaMessages` needs to extract images from content parts and hoist them to the message-level `images` array:

```ts
function chatMessagesToOllamaMessages(messages: unknown): Array<{
  role: string;
  content: string;
  images?: string[];
}> {
  if (!Array.isArray(messages)) return [];

  const normalized = [];
  for (const rawMessage of messages) {
    if (!isRecord(rawMessage)) continue;

    const role = normalizeRole(rawMessage["role"]);
    const rawContent = rawMessage["content"];
    const images: string[] = [];

    // Extract images and text separately
    const content = extractTextAndImages(rawContent, images);

    if (content.length === 0 && role !== "assistant" && images.length === 0) continue;

    const msg: Record<string, unknown> = { role, content };
    if (images.length > 0) msg["images"] = images;
    normalized.push(msg);
  }
  return normalized;
}

function extractTextAndImages(content: unknown, images: string[]): string {
  if (typeof content === "string") return content;
  if (!Array.isArray(content)) return content == null ? "" : stringifyUnknown(content);

  return content.map((part) => {
    if (typeof part === "string") return part;
    if (!isRecord(part)) return "";

    const type = asString(part["type"]);
    if (type === "text" || type === "input_text") return asString(part["text"]) ?? "";

    if (type === "image_url" || type === "image") {
      const url = asString((isRecord(part["image_url"]) ? part["image_url"] : part)["url"])
               ?? asString(part["url"]);
      if (url) images.push(stripBase64DataUriPrefix(url));
      return "";
    }
    return "";
  }).join("");
}

// Ollama wants raw base64, not the data URI wrapper
function stripBase64DataUriPrefix(url: string): string {
  const parsed = parseBase64DataUrl(url); // reuse from messages-compat or inline
  if (!parsed) throw new Error("Expected a base64 image data URI; raw URLs are unsupported");
  return parsed.data;
}
```

This proposed change preserves image input for **both** `OllamaProviderStrategy`
and `OllamaCloudProviderStrategy`, since both call `chatRequestToOllamaRequest`.
It does not prove resolution of an HTTP status without a revision-bound
reproduction. The [Ollama vision API](https://docs.ollama.com/capabilities/vision)
distinguishes SDK URL support from the REST base64 requirement.  The `LocalOllamaProviderStrategy` path (`buildRequestBodyForUpstream`) is a separate concern — that path needs a similar conversion or it needs to be routed through `chatRequestToOllamaRequest` instead.

***

## What to Test After the Fix

The existing `src/tests/ollama-compat.test.ts` is the right home for new specs. You'll want failing tests (red) for:

1. `image_url` content part → `images` field on message with stripped data URI
2. Mixed text + image content → correct `content` string + `images` array
3. Raw URL (non-base64) image → explicit rejection before upstream request; never copied into REST `images`
4. `LocalOllamaProviderStrategy` with multipart content → native schema conversion; retain actual upstream response to assess any reported 400

---
