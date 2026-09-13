// SPDX-License-Identifier: LGPL-3.0-or-later
import { fileURLToPath } from 'node:url';
import { env, pipeline } from '@huggingface/transformers';
import { LOCAL_GENERATION_MODEL, LOCAL_GENERATION_MODELS } from './generation-server.mjs';

const model = process.argv[2] || process.env.FORESIGHT_GENERATION_MODEL || LOCAL_GENERATION_MODEL;
const descriptor = Object.hasOwn(LOCAL_GENERATION_MODELS, model) && LOCAL_GENERATION_MODELS[model];
if (!descriptor) throw new RangeError('Choose one of the pinned local generation models');
env.cacheDir = process.env.FORESIGHT_MODEL_CACHE || fileURLToPath(new URL('../.cache/models/', import.meta.url));
env.allowLocalModels = true;
env.allowRemoteModels = true;
const started = performance.now();
// Download/load only. No prompt or repository content is sent to Hugging Face.
const generator = await pipeline('text-generation', model, {
  ...descriptor, device: 'cpu', session_options: { intraOpNumThreads: 2, interOpNumThreads: 1 },
});
await generator.dispose();
console.log(JSON.stringify({ status: 'ready', model, ...descriptor, cacheDir: env.cacheDir, seconds: (performance.now() - started) / 1000 }));
