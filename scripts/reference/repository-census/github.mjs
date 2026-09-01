// SPDX-License-Identifier: GPL-3.0-or-later

import { Buffer } from 'node:buffer';
import { createHash } from 'node:crypto';

const API = 'https://api.github.com';
const RAW = 'https://raw.githubusercontent.com';
const USER_AGENT = 'foresight-repository-census/0.1';
const RETRYABLE_STATUSES = new Set([429, 500, 502, 503, 504]);
const MAX_RETRY_SLEEP_MS = 120000;
const REQUEST_TIMEOUT_MS = 30000;
const OBJECT_ID_PATTERN = /^(?:[0-9a-f]{40}|[0-9a-f]{64})$/;
const TREE_ENTRY_MODES = new Map([
  ['blob', new Set(['100644', '100755', '120000'])],
  ['tree', new Set(['040000'])],
  ['commit', new Set(['160000'])],
]);

function requireObjectId(value, description, width = null) {
  if (!OBJECT_ID_PATTERN.test(value)
      || (width !== null && value.length !== width)) {
    throw new Error(`${description} must be a full lowercase Git object ID${
      width === null ? '' : ` with ${width} hexadecimal characters`
    }`);
  }
  return value;
}

function pathSegments(value) {
  if (typeof value !== 'string' || value.length === 0 || value.includes('\0')) return null;
  const segments = value.split('/');
  return segments.every((segment) => segment.length > 0 && segment !== '.' && segment !== '..')
    ? segments : null;
}

function indexTreeEntries(tree, description, { recursive }) {
  const entries = new Map();
  for (const entry of tree.tree) {
    const segments = pathSegments(entry?.path);
    if (!entry || typeof entry !== 'object' || Array.isArray(entry)
        || !segments || (!recursive && segments.length !== 1)) {
      throw new Error(`${description} contains a malformed Git tree entry path`);
    }
    if (entries.has(entry.path)) {
      throw new Error(`${description} contains duplicate Git tree path ${entry.path}`);
    }
    entries.set(entry.path, entry);
  }
  return entries;
}

export function isCoherentGitTreeEntry(entry, treeSha, expectedPath = null) {
  const modes = entry && typeof entry === 'object' && !Array.isArray(entry)
    ? TREE_ENTRY_MODES.get(entry.type) : null;
  return OBJECT_ID_PATTERN.test(treeSha)
    && pathSegments(entry?.path) !== null
    && (expectedPath === null || entry.path === expectedPath)
    && Boolean(modes?.has(entry.mode))
    && OBJECT_ID_PATTERN.test(entry.sha)
    && entry.sha.length === treeSha.length;
}

function requireCoherentTreeEntry(entry, treeSha, description) {
  if (!isCoherentGitTreeEntry(entry, treeSha)) {
    throw new Error(`${description} contains an incoherent Git tree entry at ${entry.path}`);
  }
  return entry;
}

function sameTreeEntryIdentity(left, right) {
  return left.type === right.type && left.mode === right.mode && left.sha === right.sha;
}

function requireRecursiveAncestors(
  entries, treeSha, description, entryPath, { allowMissing = false } = {},
) {
  const segments = pathSegments(entryPath);
  let complete = true;
  for (let index = 1; index < segments.length; index += 1) {
    const ancestorPath = segments.slice(0, index).join('/');
    const ancestor = entries.get(ancestorPath);
    if (!ancestor && allowMissing) {
      complete = false;
      continue;
    }
    if (!ancestor || ancestor.type !== 'tree') {
      throw new Error(`${description} contains an incoherent recursive hierarchy at ${entryPath}`);
    }
    requireCoherentTreeEntry(ancestor, treeSha, description);
  }
  return complete;
}

function requireCompleteRecursiveHierarchy(entries, treeSha, description) {
  for (const entry of entries.values()) {
    requireCoherentTreeEntry(entry, treeSha, description);
  }
  for (const entry of entries.values()) {
    requireRecursiveAncestors(entries, treeSha, description, entry.path);
  }
}

function requireTreeEnvelope(tree, treeSha, description) {
  if (tree?.sha !== treeSha) {
    throw new Error(`${description} returned Git tree ${String(tree?.sha)} for requested tree ${treeSha}`);
  }
  if (!Array.isArray(tree?.tree)) {
    throw new Error(`${description} returned a malformed Git tree entry collection`);
  }
  return tree;
}

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
    requireObjectId(revision, `Requested revision ${fullName}@${String(revision)}`);
    const key = `${fullName}@${revision}`.toLowerCase();
    if (!this.commitCache.has(key)) {
      const request = this.request(`${API}/repos/${fullName}/git/commits/${revision}`)
        .then((commit) => {
          if (commit?.sha !== revision) {
            throw new Error(`Git returned commit ${String(commit?.sha)} for requested revision ${fullName}@${revision}`);
          }
          requireObjectId(
            commit?.tree?.sha,
            `Git commit tree for ${fullName}@${revision}`,
            revision.length,
          );
          return commit;
        });
      this.commitCache.set(key, request);
    }
    return this.commitCache.get(key);
  }

  async tree(fullName, treeSha) {
    requireObjectId(treeSha, `Requested tree for ${fullName}`);
    const key = `${fullName}:${treeSha}`.toLowerCase();
    if (!this.treeCache.has(key)) {
      const request = this.request(`${API}/repos/${fullName}/git/trees/${treeSha}`)
        .then((tree) => requireTreeEnvelope(
          tree,
          treeSha,
          `Git tree response for ${fullName}`,
        ));
      this.treeCache.set(key, request);
    }
    return this.treeCache.get(key);
  }

  async recursiveTree(fullName, treeSha) {
    requireObjectId(treeSha, `Requested recursive tree for ${fullName}`);
    const key = `${fullName}:${treeSha}`.toLowerCase();
    if (!this.recursiveTreeCache.has(key)) {
      const request = this.request(`${API}/repos/${fullName}/git/trees/${treeSha}?recursive=1`)
        .then((tree) => requireTreeEnvelope(
          tree,
          treeSha,
          `Recursive Git tree response for ${fullName}`,
        ));
      this.recursiveTreeCache.set(key, request);
    }
    return this.recursiveTreeCache.get(key);
  }

  async manifest(fullName, revision) {
    requireObjectId(revision, `Requested manifest revision ${fullName}@${String(revision)}`);
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
        const parentTreeOid = commit.tree.sha;
        const sourceManifestBlobOid = requireObjectId(
          lookup.entry.sha,
          `Git manifest blob for ${fullName}@${revision}`,
          parentTreeOid.length,
        );
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
    if (!pathSegments(repositoryPath)) {
      throw new Error(`Requested repository path must be a canonical relative Git path`);
    }
    const recursive = await this.recursiveTree(fullName, rootTreeSha);
    const recursiveEntries = indexTreeEntries(
      recursive,
      `Recursive Git tree response for ${fullName}@${rootTreeSha}`,
      { recursive: true },
    );
    const exact = recursiveEntries.get(repositoryPath);
    if (exact) {
      const ancestorsComplete = requireRecursiveAncestors(
        recursiveEntries,
        rootTreeSha,
        `Recursive Git tree response for ${fullName}@${rootTreeSha}`,
        repositoryPath,
        { allowMissing: recursive.truncated !== false },
      );
      if (ancestorsComplete) {
        return {
          status: 'found', entry: exact, resolvedPath: repositoryPath,
          entryPathScope: 'root-relative',
        };
      }
    }

    const segments = repositoryPath.split('/');
    if (recursive.truncated === false) {
      requireCompleteRecursiveHierarchy(
        recursiveEntries,
        rootTreeSha,
        `Recursive Git tree response for ${fullName}@${rootTreeSha}`,
      );
      for (let index = 0; index < segments.length - 1; index += 1) {
        const prefix = segments.slice(0, index + 1).join('/');
        const entry = recursiveEntries.get(prefix);
        if (entry) {
          requireCoherentTreeEntry(
            entry,
            rootTreeSha,
            `Recursive Git tree response for ${fullName}@${rootTreeSha}`,
          );
        }
        if (entry && entry.type !== 'tree') {
          return { status: 'blocked', segment: segments[index], index, entry };
        }
      }
      return { status: 'missing', segment: segments.at(-1), index: segments.length - 1 };
    }
    let currentTree = rootTreeSha;
    for (let i = 0; i < segments.length; i += 1) {
      const tree = await this.tree(fullName, currentTree);
      const entries = indexTreeEntries(
        tree,
        `Git tree response for ${fullName}@${currentTree}`,
        { recursive: false },
      );
      const entry = entries.get(segments[i]);
      if (!entry && tree.truncated !== false) {
        throw new Error(`Cannot resolve ${repositoryPath} through incomplete Git tree ${currentTree}`);
      }
      if (!entry) {
        for (const candidate of entries.values()) {
          requireCoherentTreeEntry(
            candidate,
            currentTree,
            `Git tree response for ${fullName}@${currentTree}`,
          );
        }
        if (exact) {
          throw new Error(`Cannot validate orphan recursive path ${repositoryPath} through Git tree ${currentTree}`);
        }
        return { status: 'missing', segment: segments[i], index: i };
      }
      const recursivePath = segments.slice(0, i + 1).join('/');
      const recursiveEntry = recursiveEntries.get(recursivePath);
      if (recursiveEntry && !sameTreeEntryIdentity(recursiveEntry, entry)) {
        throw new Error(`Recursive and non-recursive Git tree entries disagree at ${recursivePath}`);
      }
      if (i === segments.length - 1) {
        return {
          status: 'found', entry, resolvedPath: repositoryPath,
          entryPathScope: 'containing-tree-relative',
        };
      }
      requireCoherentTreeEntry(
        entry,
        currentTree,
        `Git tree response for ${fullName}@${currentTree}`,
      );
      if (entry.type !== 'tree') {
        if (exact) {
          throw new Error(`Cannot validate orphan recursive path ${repositoryPath} through non-tree ancestor ${segments[i]}`);
        }
        return { status: 'blocked', segment: segments[i], index: i, entry };
      }
      currentTree = entry.sha;
    }
    return { status: 'missing' };
  }
}
