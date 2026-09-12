// SPDX-License-Identifier: LGPL-3.0-or-later
import http from 'node:http';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import { pipeline, env } from '@huggingface/transformers';
import { readModelRequestBody } from './model-request-body.mjs';

/** Start an offline loopback service producing normalized 384-dimensional MiniLM embeddings. */
export async function startEmbeddingServer({ port = 0, cacheDir = process.env.FORESIGHT_MODEL_CACHE || fileURLToPath(new URL('../.cache/models/', import.meta.url)), model = 'Xenova/all-MiniLM-L6-v2', requestTimeoutMs = 10000 } = {}) {
  if (model !== 'Xenova/all-MiniLM-L6-v2') throw new RangeError('This provider supports only the pinned MiniLM model');
  if (!Number.isInteger(requestTimeoutMs) || requestTimeoutMs < 1 || requestTimeoutMs > 30000) throw new RangeError('requestTimeoutMs must be 1..30000');
  env.cacheDir = cacheDir;
  env.allowLocalModels = true;
  env.allowRemoteModels = false;
  const extractor = await pipeline('feature-extraction', 'Xenova/all-MiniLM-L6-v2', {
    revision: '751bff37182d3f1213fa05d7196b954e230abad9', dtype: 'q8', device: 'cpu',
    session_options: { intraOpNumThreads: 2, interOpNumThreads: 1 },
  });
  const server = http.createServer(async (request, response) => {
    const reply = (status, body) => {
      if (response.destroyed || response.writableEnded) return;
      response.writeHead(status, { 'content-type': 'application/json' });
      response.end(JSON.stringify(body));
    };
    try {
      if (request.method === 'GET' && request.url === '/health') return reply(200, { status: 'ok', model, dimensions: 384, offline: true });
      if (request.method === 'GET' && request.url === '/v1/models') return reply(200, { object: 'list', data: [{ id: model, object: 'model', owned_by: 'local' }] });
      if (request.method !== 'POST' || !['/v1/embeddings', '/embeddings', '/api/embed'].includes(request.url)) return reply(404, { error: 'route_not_found' });
      const bytes = await readModelRequestBody(request, response, requestTimeoutMs);
      if (bytes === null) return;
      const body = JSON.parse(bytes.toString('utf8'));
      if (!body || typeof body !== 'object' || Array.isArray(body)) return reply(400, { error: 'invalid_input' });
      const inputs = typeof body.input === 'string' ? [body.input] : body.input;
      if (Object.hasOwn(body, 'model') && body.model !== model) return reply(400, { error: 'unknown_model' });
      if (!Array.isArray(inputs) || inputs.length < 1 || inputs.length > 32 || inputs.some(value => typeof value !== 'string' || value.length > 16384)) return reply(400, { error: 'invalid_input' });
      if (body.dimensions !== undefined && body.dimensions !== 384) return reply(400, { error: 'invalid_dimensions' });
      if (body.encoding_format !== undefined && !['float', 'base64'].includes(body.encoding_format)) return reply(400, { error: 'invalid_encoding_format' });
      const output = await extractor(inputs, { pooling: 'mean', normalize: true });
      const embeddings = output.tolist();
      if (request.url === '/api/embed') return reply(200, { model, embeddings });
      const encode = vector => body.encoding_format === 'base64' ? Buffer.from(new Float32Array(vector).buffer).toString('base64') : vector;
      return reply(200, { object: 'list', model, data: embeddings.map((vector, index) => ({ object: 'embedding', index, embedding: encode(vector) })) });
    } catch (error) {
      return reply(error instanceof SyntaxError ? 400 : 500, { error: error instanceof SyntaxError ? 'invalid_json' : 'embedding_failed' });
    }
  });
  try {
    await new Promise((resolve, reject) => { server.once('error', reject); server.listen(port, '127.0.0.1', resolve); });
  } catch (error) {
    await extractor.dispose();
    throw error;
  }
  return {
    server, model, dimensions: 384,
    baseUrl: `http://127.0.0.1:${server.address().port}/v1`,
    async close() { await new Promise((resolve, reject) => server.close(error => error ? reject(error) : resolve())); await extractor.dispose(); },
  };
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const service = await startEmbeddingServer({port: Number(process.env.FORESIGHT_EMBEDDING_PORT || 11434)});
  console.log(JSON.stringify({status: 'ready', baseUrl: service.baseUrl, model: service.model, dimensions: service.dimensions}));
  for (const signal of ['SIGINT', 'SIGTERM']) process.once(signal, async () => { await service.close(); process.exit(0); });
}
