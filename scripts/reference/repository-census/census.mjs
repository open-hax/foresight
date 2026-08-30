// SPDX-License-Identifier: GPL-3.0-or-later

import { createHash } from 'node:crypto';
import { parseRoot } from './args.mjs';
import { stableId } from './edn.mjs';
import { GitHubClient, isRateLimitError } from './github.mjs';
import { normalizeGitHubUrl, parseGitmodules } from './gitmodules.mjs';

async function mapLimit(items, limit, task) {
  const results = new Array(items.length);
  let cursor = 0;
  async function worker() {
    while (true) {
      const index = cursor++;
      if (index >= items.length) return;
      results[index] = await task(items[index], index);
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, () => worker()));
  return results;
}

export async function census(options, dependencies = {}) {
  const roots = options.roots.map(parseRoot);
  if (!roots.length) throw new Error('At least one --root is required');

  const client = dependencies.client || new GitHubClient(process.env.GITHUB_TOKEN);
  const queue = roots.map((root) => ({ ...root, depth: 0, root: true }));
  const scheduled = new Set(queue.map((item) => `${item.fullName.toLowerCase()}@${item.revision}`));
  const visited = new Set();
  const inspected = new Set();
  const repositories = new Map();
  const occurrences = [];
  const gaps = [];

  const addGap = (gap, { frontier = false } = {}) => gaps.push({
    'gap/id': stableId('gap', gap['gap/type'], JSON.stringify(gap)),
    ...gap,
    ...(frontier ? { 'gap/frontier?': true } : {}),
  });

  while (queue.length && visited.size < options.maxNodes) {
    const item = queue.shift();
    const visitKey = `${item.fullName.toLowerCase()}@${item.revision}`;
    if (visited.has(visitKey)) continue;
    visited.add(visitKey);

    const repoId = `github:${item.fullName.toLowerCase()}`;
    const repo = repositories.get(repoId) || {
      'repository/id': repoId,
      'repository/full-name': item.fullName,
      'repository/url': `https://github.com/${item.fullName}`,
      'repository/revisions': new Set(),
      'repository/root?': false,
      'repository/min-depth': item.depth,
      'repository/manifest-statuses': new Set(),
      'repository/manifest-sha256-at-revisions': {},
    };
    repo['repository/revisions'].add(item.revision);
    repo['repository/root?'] ||= item.root;
    repo['repository/min-depth'] = Math.min(repo['repository/min-depth'], item.depth);
    repositories.set(repoId, repo);

    let manifestText;
    try {
      manifestText = await client.manifest(item.fullName, item.revision);
    } catch (error) {
      if (isRateLimitError(error)) throw error;
      repo['repository/manifest-statuses'].add('unavailable');
      addGap({
        'gap/type': 'manifest/unavailable', 'gap/repository': repoId,
        'gap/revision': item.revision, 'gap/depth': item.depth,
        'gap/http-status': error.status ?? null, 'gap/detail': error.message,
      }, { frontier: true });
      continue;
    }
    inspected.add(visitKey);

    if (manifestText === null) {
      repo['repository/manifest-statuses'].add('absent');
      continue;
    }
    repo['repository/manifest-statuses'].add('present');
    repo['repository/manifest-sha256-at-revisions'][item.revision]
      = createHash('sha256').update(manifestText).digest('hex');

    let commit;
    try {
      commit = await client.commit(item.fullName, item.revision);
    } catch (error) {
      if (isRateLimitError(error)) throw error;
      inspected.delete(visitKey);
      addGap({
        'gap/type': 'commit/unavailable', 'gap/repository': repoId,
        'gap/revision': item.revision, 'gap/depth': item.depth,
        'gap/http-status': error.status ?? null, 'gap/detail': error.message,
      }, { frontier: true });
      continue;
    }

    const modules = parseGitmodules(manifestText);
    repo['repository/submodule-count-at-revisions'] ||= {};
    repo['repository/submodule-count-at-revisions'][item.revision] = modules.length;

    const resolved = await mapLimit(modules, options.concurrency, async (module) => {
      const normalized = normalizeGitHubUrl(module.url || '', item.fullName);
      let lookup = null;
      if (module.path) {
        try {
          lookup = await client.lookupTreePath(item.fullName, commit.tree.sha, module.path);
        } catch (error) {
          if (isRateLimitError(error)) throw error;
          lookup = { status: 'error', error };
        }
      }
      return { module, normalized, lookup };
    });

    for (const { module, normalized, lookup } of resolved) {
      const targetId = normalized.kind === 'github' ? `github:${normalized.fullName.toLowerCase()}` : null;
      const gitlink = lookup?.status === 'found' && lookup.entry.type === 'commit'
        ? lookup.entry.sha.toLowerCase() : null;

      let status;
      if (module.parseStatus !== 'valid') status = 'invalid-declaration';
      else if (normalized.kind === 'local') status = 'local-only';
      else if (normalized.kind !== 'github') status = 'unsupported-url';
      else if (lookup?.status === 'error') status = 'lookup-error';
      else if (lookup?.status !== 'found') status = 'path-unresolved';
      else if (lookup.entry.type !== 'commit') status = 'path-not-gitlink';
      else status = 'resolved';

      const occurrence = {
        'occurrence/id': stableId(
          'occurrence', repoId, item.revision, module.name, module.line,
          module.path || '', module.url || '', gitlink || '',
        ),
        'occurrence/kind': 'git-submodule', 'occurrence/status': status,
        'occurrence/parent': repoId, 'occurrence/parent-revision': item.revision,
        'occurrence/depth': item.depth + 1, 'occurrence/name': module.name,
        'occurrence/path': module.path ?? null, 'occurrence/raw-url': module.url ?? null,
        'occurrence/declared-branch': module.branch ?? null,
        'occurrence/declaration-line': module.line, 'occurrence/target': targetId,
        'occurrence/target-full-name': normalized.fullName ?? null,
        'occurrence/target-revision': gitlink,
      };
      occurrences.push(occurrence);

      if (status !== 'resolved') {
        addGap({
          'gap/type': `submodule/${status}`, 'gap/occurrence': occurrence['occurrence/id'],
          'gap/parent': repoId, 'gap/parent-revision': item.revision,
          'gap/path': module.path ?? null, 'gap/raw-url': module.url ?? null,
          'gap/detail': lookup?.error?.message ?? lookup?.status ?? module.parseStatus,
        }, { frontier: status === 'lookup-error' });
        continue;
      }

      const childKey = `${normalized.fullName.toLowerCase()}@${gitlink}`;
      if (scheduled.has(childKey)) continue;

      if (item.depth + 1 > options.maxDepth) {
        addGap({
          'gap/type': 'recursion/max-depth', 'gap/occurrence': occurrence['occurrence/id'],
          'gap/target': targetId, 'gap/target-revision': gitlink, 'gap/limit': options.maxDepth,
        }, { frontier: true });
        continue;
      }

      if (visited.size + queue.length >= options.maxNodes) {
        addGap({
          'gap/type': 'recursion/max-nodes', 'gap/occurrence': occurrence['occurrence/id'],
          'gap/target': targetId, 'gap/target-revision': gitlink, 'gap/limit': options.maxNodes,
        }, { frontier: true });
        continue;
      }
      scheduled.add(childKey);
      queue.push({ fullName: normalized.fullName, revision: gitlink, depth: item.depth + 1, root: false });
    }
  }

  for (const item of queue) {
    addGap({
      'gap/type': 'recursion/max-nodes', 'gap/repository': `github:${item.fullName.toLowerCase()}`,
      'gap/revision': item.revision, 'gap/limit': options.maxNodes,
    }, { frontier: true });
  }

  const repoRows = [...repositories.values()]
    .map((row) => ({
      ...row,
      'repository/revisions': [...row['repository/revisions']].sort(),
      'repository/manifest-statuses': new Set(row['repository/manifest-statuses']),
    }))
    .sort((a, b) => a['repository/full-name'].localeCompare(b['repository/full-name']));
  occurrences.sort((a, b) => a['occurrence/parent'].localeCompare(b['occurrence/parent'])
    || String(a['occurrence/path'] ?? '').localeCompare(String(b['occurrence/path'] ?? ''))
    || a['occurrence/parent-revision'].localeCompare(b['occurrence/parent-revision']));
  gaps.sort((a, b) => a['gap/type'].localeCompare(b['gap/type']) || a['gap/id'].localeCompare(b['gap/id']));

  return {
    roots, repositories: repoRows, occurrences, gaps,
    stats: {
      repositories: repoRows.length, repositoryRevisions: inspected.size,
      occurrences: occurrences.length,
      resolvedOccurrences: occurrences.filter((row) => row['occurrence/status'] === 'resolved').length,
      gaps: gaps.length, githubRequests: client.requests,
      rateRemaining: client.rate.remaining, rateReset: client.rate.reset,
      frontierRemaining: gaps.filter((gap) => gap['gap/frontier?'] === true).length,
      maxNodes: options.maxNodes, maxDepth: options.maxDepth,
    },
  };
}
