// SPDX-License-Identifier: GPL-3.0-or-later

import { Buffer } from 'node:buffer';

const API = 'https://api.github.com';
const USER_AGENT = 'foresight-repository-census/0.1';

export class GitHubClient {
  constructor(token, { fetchImpl = globalThis.fetch } = {}) {
    this.token = token || null;
    this.fetchImpl = fetchImpl;
    this.treeCache = new Map();
    this.commitCache = new Map();
    this.blobCache = new Map();
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

  async request(url, { accept } = {}) {
    this.requests += 1;
    const response = await this.fetchImpl(url, { headers: this.headers(accept) });
    const remaining = response.headers.get('x-ratelimit-remaining');
    const reset = response.headers.get('x-ratelimit-reset');
    if (remaining !== null) {
      const observed = Number(remaining);
      this.rate.remaining = this.rate.remaining === null
        ? observed : Math.min(this.rate.remaining, observed);
    }
    if (reset !== null) this.rate.reset = Number(reset);

    if (!response.ok) {
      const body = await response.text();
      const error = new Error(`HTTP ${response.status} for ${url}: ${body.slice(0, 300)}`);
      error.status = response.status;
      error.url = url;
      throw error;
    }
    return response.json();
  }

  async commit(fullName, revision) {
    const key = `${fullName}@${revision}`.toLowerCase();
    if (!this.commitCache.has(key)) {
      this.commitCache.set(key, this.request(`${API}/repos/${fullName}/git/commits/${revision}`));
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

  async blob(fullName, blobSha) {
    const key = `${fullName}:${blobSha}`.toLowerCase();
    if (!this.blobCache.has(key)) {
      this.blobCache.set(key, this.request(`${API}/repos/${fullName}/git/blobs/${blobSha}`));
    }
    return this.blobCache.get(key);
  }

  async manifest(fullName, revision) {
    const key = `${fullName}@${revision}`.toLowerCase();
    if (!this.manifestCache.has(key)) {
      const request = (async () => {
        const commit = await this.commit(fullName, revision);
        const lookup = await this.lookupTreePath(fullName, commit.tree.sha, '.gitmodules');
        if (lookup.status !== 'found') return null;
        if (lookup.entry.type !== 'blob' || !['100644', '100755'].includes(lookup.entry.mode)) {
          throw new Error(`.gitmodules is not a regular blob in ${fullName}@${revision}`);
        }
        const blob = await this.blob(fullName, lookup.entry.sha);
        if (blob.encoding !== 'base64' || typeof blob.content !== 'string') {
          throw new Error(`Unsupported .gitmodules blob encoding in ${fullName}@${revision}`);
        }
        const bytes = Buffer.from(blob.content.replace(/\s/g, ''), 'base64');
        if (Number.isInteger(blob.size) && bytes.length !== blob.size) {
          throw new Error(`Truncated .gitmodules blob in ${fullName}@${revision}`);
        }
        return new TextDecoder('utf-8', { fatal: true }).decode(bytes);
      })();
      this.manifestCache.set(key, request);
    }
    return this.manifestCache.get(key);
  }

  async lookupTreePath(fullName, rootTreeSha, repositoryPath) {
    const segments = repositoryPath.split('/');
    let currentTree = rootTreeSha;
    for (let i = 0; i < segments.length; i += 1) {
      const tree = await this.tree(fullName, currentTree);
      const entry = tree.tree.find((candidate) => candidate.path === segments[i]);
      if (!entry && tree.truncated) {
        throw new Error(`Cannot resolve ${repositoryPath} through truncated Git tree ${currentTree}`);
      }
      if (!entry) return { status: 'missing', segment: segments[i], index: i };
      if (i === segments.length - 1) return { status: 'found', entry };
      if (entry.type !== 'tree') return { status: 'blocked', segment: segments[i], index: i, entry };
      currentTree = entry.sha;
    }
    return { status: 'missing' };
  }
}
