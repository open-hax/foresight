// SPDX-License-Identifier: GPL-3.0-or-later

const API = 'https://api.github.com';
const RAW = 'https://raw.githubusercontent.com';
const USER_AGENT = 'foresight-repository-census/0.1';

function escapeSegment(segment) {
  return encodeURIComponent(segment).replace(/%2F/gi, '/');
}

export class GitHubClient {
  constructor(token) {
    this.token = token || null;
    this.treeCache = new Map();
    this.commitCache = new Map();
    this.rawCache = new Map();
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

  async request(url, { accept, raw = false } = {}) {
    this.requests += 1;
    const response = await fetch(url, { headers: this.headers(accept) });
    const remaining = response.headers.get('x-ratelimit-remaining');
    const reset = response.headers.get('x-ratelimit-reset');
    if (remaining !== null) this.rate.remaining = Number(remaining);
    if (reset !== null) this.rate.reset = Number(reset);

    if (!response.ok) {
      const body = await response.text();
      const error = new Error(`HTTP ${response.status} for ${url}: ${body.slice(0, 300)}`);
      error.status = response.status;
      error.url = url;
      throw error;
    }
    return raw ? response.text() : response.json();
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

  async manifest(fullName, revision) {
    const key = `${fullName}@${revision}`.toLowerCase();
    if (!this.rawCache.has(key)) {
      const [owner, repo] = fullName.split('/');
      const url = `${RAW}/${escapeSegment(owner)}/${escapeSegment(repo)}/${revision}/.gitmodules`;
      const request = this.request(url, { raw: true, accept: 'text/plain' }).catch((error) => {
        if (error.status === 404) return null;
        throw error;
      });
      this.rawCache.set(key, request);
    }
    return this.rawCache.get(key);
  }

  async lookupTreePath(fullName, rootTreeSha, repositoryPath) {
    const segments = repositoryPath.split('/').filter(Boolean);
    let currentTree = rootTreeSha;
    for (let i = 0; i < segments.length; i += 1) {
      const tree = await this.tree(fullName, currentTree);
      const entry = tree.tree.find((candidate) => candidate.path === segments[i]);
      if (!entry) return { status: 'missing', segment: segments[i], index: i };
      if (i === segments.length - 1) return { status: 'found', entry };
      if (entry.type !== 'tree') return { status: 'blocked', segment: segments[i], index: i, entry };
      currentTree = entry.sha;
    }
    return { status: 'missing' };
  }
}
