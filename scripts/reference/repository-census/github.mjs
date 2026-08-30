// SPDX-License-Identifier: GPL-3.0-or-later

import { Buffer } from 'node:buffer';
import { createHash } from 'node:crypto';

const API = 'https://api.github.com';
const RAW = 'https://raw.githubusercontent.com';
const USER_AGENT = 'foresight-repository-census/0.1';
const RETRYABLE_STATUSES = new Set([429, 500, 502, 503, 504]);
const MAX_RETRY_SLEEP_MS = 120000;
const REQUEST_TIMEOUT_MS = 30000;

function responseIsRateLimited(url, response, bodyText = '') {
  if (!response || (!url.startsWith(API) && !url.startsWith(RAW))) return false;
  const rateRemaining = response.headers.get('x-ratelimit-remaining');
  const retryAfter = response.headers.get('retry-after');
  return response.status === 429
    || (response.status === 403
      && (rateRemaining === '0' || retryAfter !== null || /rate limit/i.test(bodyText)));
}

function annotateResponseError(error, url, response, { rateLimited }) {
  error.status = response.status;
  error.url = url;
  if (rateLimited) {
    error.code = 'github/rate-limit-exhausted';
    error.rateReset = response.headers.get('x-ratelimit-reset');
    error.message = `GitHub API rate limit exhausted${error.rateReset ? ` until ${error.rateReset}` : ''}: ${error.message}`;
  }
  return error;
}

export function isRateLimitError(error) {
  return error?.code === 'github/rate-limit-exhausted';
}

export class GitHubClient {
  constructor(token, {
    fetchImpl = globalThis.fetch,
    sleepImpl = (milliseconds) => new Promise((resolve) => setTimeout(resolve, milliseconds)),
    nowImpl = Date.now,
    maxAttempts = 3,
  } = {}) {
    this.token = token || null;
    this.fetchImpl = fetchImpl;
    this.sleepImpl = sleepImpl;
    this.nowImpl = nowImpl;
    this.maxAttempts = maxAttempts;
    this.treeCache = new Map();
    this.recursiveTreeCache = new Map();
    this.commitCache = new Map();
    this.manifestCache = new Map();
    this.requests = 0;
    this.rate = { remaining: null, reset: null };
  }

  headers(accept = 'application/vnd.github+json') {
    const headers = {
      Accept: accept,
      'User-Agent': USER_AGENT,
      'X-GitHub-Api-Version': '2022-11-28',
    };
    if (this.token) headers.Authorization = `Bearer ${this.token}`;
    return headers;
  }

  observeRate(url, response) {
    if (url.startsWith(API)) {
      const remaining = response.headers.get('x-ratelimit-remaining');
      const reset = response.headers.get('x-ratelimit-reset');
      if (remaining !== null) {
        const observed = Number(remaining);
        this.rate.remaining = this.rate.remaining === null
          ? observed : Math.min(this.rate.remaining, observed);
      }
      if (reset !== null) this.rate.reset = Number(reset);
    }
  }

  retryDelay(response, attempt, { rateLimited = false } = {}) {
    const retryAfter = response?.headers.get('retry-after');
    if (retryAfter !== null && /^\d+(?:[.]\d+)?$/.test(retryAfter)) {
      return Number(retryAfter) * 1000;
    }
    const remaining = response?.headers.get('x-ratelimit-remaining');
    const reset = response?.headers.get('x-ratelimit-reset');
    if (remaining === '0' && reset !== null && /^\d+$/.test(reset)) {
      const wait = Math.max(0, (Number(reset) * 1000) - this.nowImpl() + 1000);
      return wait;
    }
    if (rateLimited) return 60000 * (2 ** (attempt - 1));
    return 200 * (2 ** (attempt - 1));
  }

  async fetchResponse(url, { accept } = {}) {
    let lastError;
    for (let attempt = 1; attempt <= this.maxAttempts; attempt += 1) {
      let response;
      let body;
      try {
        this.requests += 1;
        response = await this.fetchImpl(url, {
          headers: this.headers(accept),
          signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
        });
        this.observeRate(url, response);
        body = Buffer.from(await response.arrayBuffer());
      } catch (error) {
        const rateLimited = responseIsRateLimited(url, response);
        lastError = response
          ? annotateResponseError(
            new Error(`HTTP ${response.status} body read failed for ${url}: ${error.message}`, {
              cause: error,
            }),
            url,
            response,
            { rateLimited },
          )
          : error;
        if (attempt === this.maxAttempts) throw lastError;
        const retryDelay = this.retryDelay(response, attempt, { rateLimited });
        if (retryDelay > MAX_RETRY_SLEEP_MS) {
          lastError.retryDelayMs = retryDelay;
          lastError.message = `GitHub retry boundary ${retryDelay} ms exceeds the ${MAX_RETRY_SLEEP_MS} ms operational wait cap; refusing to retry early: ${lastError.message}`;
          throw lastError;
        }
        await this.sleepImpl(retryDelay);
        continue;
      }

      if (response.ok) return body;

      const bodyText = body.toString('utf8');
      const error = new Error(`HTTP ${response.status} for ${url}: ${bodyText.slice(0, 300)}`);
      const rateLimited = responseIsRateLimited(url, response, bodyText);
      annotateResponseError(error, url, response, { rateLimited });
      lastError = error;
      const retryable = RETRYABLE_STATUSES.has(response.status) || isRateLimitError(error);
      if (!retryable || attempt === this.maxAttempts) throw error;
      const retryDelay = this.retryDelay(response, attempt, { rateLimited });
      if (retryDelay > MAX_RETRY_SLEEP_MS) {
        error.retryDelayMs = retryDelay;
        error.message = `GitHub retry boundary ${retryDelay} ms exceeds the ${MAX_RETRY_SLEEP_MS} ms operational wait cap; refusing to retry early: ${error.message}`;
        throw error;
      }
      await this.sleepImpl(retryDelay);
    }
    throw lastError;
  }

  async request(url, { accept } = {}) {
    const body = await this.fetchResponse(url, { accept });
    return JSON.parse(new TextDecoder().decode(body));
  }

  async requestRaw(url) {
    return this.fetchResponse(url, { accept: 'text/plain' });
  }

  async commit(fullName, revision) {
    const key = `${fullName}@${revision}`.toLowerCase();
    if (!this.commitCache.has(key)) {
      const request = this.request(`${API}/repos/${fullName}/git/commits/${revision}`)
        .then((commit) => {
          if (commit?.sha !== revision) {
            throw new Error(`Git returned commit ${String(commit?.sha)} for requested revision ${fullName}@${revision}`);
          }
          return commit;
        });
      this.commitCache.set(key, request);
    }
    return this.commitCache.get(key);
  }

  async tree(fullName, treeSha) {
    const key = `${fullName}:${treeSha}`.toLowerCase();
    if (!this.treeCache.has(key)) {
      this.treeCache.set(key, this.request(`${API}/repos/${fullName}/git/trees/${treeSha}`));
    }
    return this.treeCache.get(key);
  }

  async recursiveTree(fullName, treeSha) {
    const key = `${fullName}:${treeSha}`.toLowerCase();
    if (!this.recursiveTreeCache.has(key)) {
      this.recursiveTreeCache.set(
        key,
        this.request(`${API}/repos/${fullName}/git/trees/${treeSha}?recursive=1`),
      );
    }
    return this.recursiveTreeCache.get(key);
  }

  async manifest(fullName, revision) {
    const key = `${fullName}@${revision}`.toLowerCase();
    if (!this.manifestCache.has(key)) {
      const request = (async () => {
        let manifestBytes;
        try {
          manifestBytes = await this.requestRaw(`${RAW}/${fullName}/${revision}/.gitmodules`);
        } catch (error) {
          if (error.status !== 404) throw error;
          const commit = await this.commit(fullName, revision);
          const lookup = await this.lookupTreePath(fullName, commit.tree.sha, '.gitmodules');
          if (lookup.status === 'missing') return null;
          if (lookup.status !== 'found') {
            throw new Error(`Cannot prove .gitmodules absent from ${fullName}@${revision}`);
          }
          if (lookup.entry.type !== 'blob' || !['100644', '100755'].includes(lookup.entry.mode)) {
            throw new Error(`.gitmodules is not a regular blob in ${fullName}@${revision}`);
          }
          throw new Error(`Raw .gitmodules returned HTTP 404 despite an exact Git tree entry in ${fullName}@${revision}`);
        }
        const commit = await this.commit(fullName, revision);
        const lookup = await this.lookupTreePath(fullName, commit.tree.sha, '.gitmodules');
        if (lookup.status !== 'found') {
          throw new Error(`Raw .gitmodules has no matching Git tree entry in ${fullName}@${revision}`);
        }
        if (lookup.entry.type !== 'blob' || !['100644', '100755'].includes(lookup.entry.mode)) {
          throw new Error(`.gitmodules is not a regular blob in ${fullName}@${revision}`);
        }
        const parentTreeOid = commit.tree.sha.toLowerCase();
        const sourceManifestBlobOid = lookup.entry.sha.toLowerCase();
        if (!/^(?:[0-9a-f]{40}|[0-9a-f]{64})$/.test(parentTreeOid)
            || !/^(?:[0-9a-f]{40}|[0-9a-f]{64})$/.test(sourceManifestBlobOid)) {
          throw new Error(`Git returned an invalid source object ID for ${fullName}@${revision}`);
        }
        const algorithm = lookup.entry.sha.length === 64 ? 'sha256' : 'sha1';
        const objectHeader = Buffer.from(`blob ${manifestBytes.length}\0`);
        const observedBlob = createHash(algorithm)
          .update(objectHeader).update(manifestBytes).digest('hex');
        if (observedBlob !== sourceManifestBlobOid) {
          throw new Error(`Raw .gitmodules bytes do not match Git blob identity in ${fullName}@${revision}`);
        }
        return {
          text: new TextDecoder('utf-8', { fatal: true }).decode(manifestBytes),
          sha256: createHash('sha256').update(manifestBytes).digest('hex'),
          sourceManifestBlobOid,
          parentTreeOid,
        };
      })();
      this.manifestCache.set(key, request);
    }
    return this.manifestCache.get(key);
  }

  async lookupTreePath(fullName, rootTreeSha, repositoryPath) {
    const recursive = await this.recursiveTree(fullName, rootTreeSha);
    const exact = recursive.tree.find((candidate) => candidate.path === repositoryPath);
    if (exact) return { status: 'found', entry: exact };

    const segments = repositoryPath.split('/');
    if (recursive.truncated === false) {
      for (let index = 0; index < segments.length - 1; index += 1) {
        const prefix = segments.slice(0, index + 1).join('/');
        const entry = recursive.tree.find((candidate) => candidate.path === prefix);
        if (entry && entry.type !== 'tree') {
          return { status: 'blocked', segment: segments[index], index, entry };
        }
      }
      return { status: 'missing', segment: segments.at(-1), index: segments.length - 1 };
    }
    let currentTree = rootTreeSha;
    for (let i = 0; i < segments.length; i += 1) {
      const tree = await this.tree(fullName, currentTree);
      const entry = tree.tree.find((candidate) => candidate.path === segments[i]);
      if (!entry && tree.truncated !== false) {
        throw new Error(`Cannot resolve ${repositoryPath} through incomplete Git tree ${currentTree}`);
      }
      if (!entry) return { status: 'missing', segment: segments[i], index: i };
      if (i === segments.length - 1) return { status: 'found', entry };
      if (entry.type !== 'tree') return { status: 'blocked', segment: segments[i], index: i, entry };
      currentTree = entry.sha;
    }
    return { status: 'missing' };
  }
}
