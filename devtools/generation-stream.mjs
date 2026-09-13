// SPDX-License-Identifier: LGPL-3.0-or-later

/** Encode bounded OpenAI SSE chunks; no generated tool argument is emitted before validation. */
export function completionStream(response, metadata) {
  let started = false;
  function write(value) {
    if (response.destroyed || response.writableEnded) return;
    if (!started) {
      response.writeHead(200, { 'content-type': 'text/event-stream', 'cache-control': 'no-cache' });
      started = true;
    }
    if (response.writableLength > 262144) throw new Error('generation_stream_backpressure');
    response.write(`data: ${JSON.stringify(value)}\n\n`);
  }
  const chunk = (choices, extra = {}) => ({ ...metadata, object: 'chat.completion.chunk', choices, ...extra });
  return {
    delta(delta) { write(chunk([{ index: 0, delta, finish_reason: null }])); },
    finish(reason, usage, provenance, includeUsage) {
      write(chunk([{ index: 0, delta: {}, finish_reason: reason }], { local_generation: provenance }));
      if (includeUsage) write(chunk([], { usage }));
      if (!response.destroyed && !response.writableEnded) response.end('data: [DONE]\n\n');
    },
    fail(code) {
      if (!response.destroyed && !response.writableEnded) {
        response.end(`data: ${JSON.stringify({ error: { message: code, type: 'local_generation_error', code } })}\n\ndata: [DONE]\n\n`);
      }
    },
  };
}
