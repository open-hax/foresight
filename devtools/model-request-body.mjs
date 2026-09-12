// SPDX-License-Identifier: LGPL-3.0-or-later

/** Bound HTTP upload time and memory before either local model receives input. */
export function readModelRequestBody(request, response, timeoutMs) {
  return new Promise(resolve => {
    const chunks = [];
    const maxBytes = 1024 * 1024;
    let size = 0, settled = false;
    const discardErrorListener = () => request.off('error', disconnected);
    const finish = (body, status, error, close = false) => {
      if (settled) return;
      settled = true;
      clearTimeout(timer);
      request.off('data', data); request.off('end', ended); request.off('aborted', disconnected);
      response.off('close', disconnected);
      // Destroying an incomplete IncomingMessage can emit ECONNRESET. Keep its
      // handler until close, even after this promise has already settled.
      if (request.closed || request.complete) discardErrorListener();
      else request.once('close', discardErrorListener);
      chunks.length = 0;
      if (status && !response.destroyed && !response.writableEnded) {
        // A preceding pipelined response may prevent this response from flushing.
        // Give the refusal a short grace period, then close the socket regardless.
        const forcedClose = close ? setTimeout(() => request.destroy(), 1000) : null;
        forcedClose?.unref?.();
        if (close) request.once('close', () => clearTimeout(forcedClose));
        response.writeHead(status, { 'content-type': 'application/json', ...(close ? { connection: 'close' } : {}) });
        response.end(JSON.stringify({ error }), () => { clearTimeout(forcedClose); if (close) request.destroy(); });
      }
      resolve(body);
    };
    const disconnected = () => finish(null);
    const data = chunk => {
      size += chunk.length;
      if (size > maxBytes) chunks.length = 0;
      else chunks.push(chunk);
    };
    const ended = () => size > maxBytes
      ? finish(null, 413, 'input_too_large') : finish(Buffer.concat(chunks));
    // An absolute deadline also bounds clients that keep sending tiny chunks.
    const timer = setTimeout(() => finish(null, size > maxBytes ? 413 : 408,
      size > maxBytes ? 'input_too_large' : 'request_timeout', true), timeoutMs);
    timer.unref?.();
    request.on('error', disconnected);
    request.once('aborted', disconnected);
    response.once('close', disconnected);
    request.on('data', data);
    request.once('end', ended);
    if (request.destroyed || response.destroyed) disconnected();
  });
}
