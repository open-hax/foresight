// SPDX-License-Identifier: GPL-3.0-or-later

import { Buffer } from 'node:buffer';
import { createHash } from 'node:crypto';

const API = 'https://api.github.com';
const RAW = 'https://raw.githubusercontent.com';
const USER_AGENT = 'foresight-repository-census/0.1';

export class GitHubClient {
  constructor(token, { fetchImpl = globalThis.fetch } = {}) {
    this.token = token || null;
    this.fetchImpl = fetchImpl;
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

  async request(url, { accept } = {}) {
    this.requests += 1;
    const response = await this.fetchImpl(url, { headers: this.headers(accept) });
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

    if (!response.ok) {
      const body = await response.text();
      const error = new Error(`HTTP ${response.status} for ${url}: ${body.slice(0, 300)}`);
      error.status = response.status;
      error.url = url;
      throw error;
    }
    return response.json();
  }

  async requestRaw(url) {
    this.requests += 1;
    const response = await this.fetchImpl(url, { headers: this.headers('text/plain') });
    if (!response.ok) {
      const body = await response.text();
      const error = new Error(`HTTP ${response.status} for ${url}: ${body.slice(0, 300)}`);
      error.status = response.status;
      error.url = url;
      throw error;
    }
    return Buffer.from(await response.arrayBuffer());
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
          await this.commit(fullName, revision);
          return null;
        }
        const commit = await this.commit(fullName, revision);
        const lookup = await this.lookupTreePath(fullName, commit.tree.sha, '.gitmodules');
        if (lookup.status !== 'found') {
          throw new Error(`Raw .gitmodules has no matching Git tree entry in ${fullName}@${revision}`);
        }
        if (lookup.entry.type !== 'blob' || !['100644', '100755'].includes(lookup.entry.mode)) {
          throw new Error(`.gitmodules is not a regular blob in ${fullName}@${revision}`);
        }
        const algorithm = lookup.entry.sha.length === 64 ? 'sha256' : 'sha1';
        const objectHeader = Buffer.from(`blob ${manifestBytes.length}\0`);
        const observedBlob = createHash(algorithm)
          .update(objectHeader).update(manifestBytes).digest('hex');
        if (observedBlob !== lookup.entry.sha.toLowerCase()) {
          throw new Error(`Raw .gitmodules bytes do not match Git blob identity in ${fullName}@${revision}`);
        }
        return new TextDecoder('utf-8', { fatal: true }).decode(manifestBytes);
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
    if (!recursive.truncated) {
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
