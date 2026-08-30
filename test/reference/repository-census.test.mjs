#!/usr/bin/env node
// SPDX-License-Identifier: GPL-3.0-or-later

import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import { parseArgs, parseRoot } from '../../scripts/reference/repository-census/args.mjs';
import { census } from '../../scripts/reference/repository-census/census.mjs';
import { edn } from '../../scripts/reference/repository-census/edn.mjs';
import { GitHubClient } from '../../scripts/reference/repository-census/github.mjs';
import { normalizeGitHubUrl, parseGitmodules } from '../../scripts/reference/repository-census/gitmodules.mjs';
import {
  canonicalFrontier, canonicalSummary, frontierBaselineMatches,
} from '../../scripts/reference/repository-census/output.mjs';

const sha = (character) => character.repeat(40);
const gitBlobSha = (text) => createHash('sha1')
  .update(`blob ${Buffer.byteLength(text)}\0`).update(text).digest('hex');

const modules = parseGitmodules(`
[submodule "a"]
  path = orgs/open-hax/proxx
  url = git@github.com:open-hax/proxx.git
  branch = main
[submodule "local"]
  path = packages/local
  url = file:///home/err/devel/local
`);
assert.equal(modules.length, 2);
assert.deepEqual(modules[0], {
  name: 'a', line: 2, path: 'orgs/open-hax/proxx',
  url: 'git@github.com:open-hax/proxx.git', branch: 'main', parseStatus: 'valid',
});
assert.equal(normalizeGitHubUrl(modules[0].url).fullName, 'open-hax/proxx');
assert.equal(normalizeGitHubUrl(modules[1].url).kind, 'local');
assert.equal(normalizeGitHubUrl('org-14957082@github.com:openai/codex.git').fullName, 'openai/codex');
assert.equal(normalizeGitHubUrl('https://github.com/octave-commons/pantheon').fullName, 'octave-commons/pantheon');
assert.equal(normalizeGitHubUrl('../sibling.git', 'open-hax/root').fullName, 'open-hax/sibling');
assert.equal(normalizeGitHubUrl('../../other/sibling.git', 'open-hax/root').fullName, 'other/sibling');
assert.equal(normalizeGitHubUrl('./nested.git', 'open-hax/root').kind, 'unsupported');
assert.equal(normalizeGitHubUrl('../sibling.git').kind, 'unsupported');

const quotedModules = parseGitmodules(`
[submodule "quoted"]
  path = " space dir "
  url = "../sibling.git" # Git config comment
[core]
  path = must-not-overwrite-the-submodule
[SUBMODULE "after-core"]
  path = after-core
  url = https://github.com/open-hax/after-core.git ; trailing comment
`);
assert.deepEqual(quotedModules, [
  {
    name: 'quoted', line: 2, path: ' space dir ',
    url: '../sibling.git', parseStatus: 'valid',
  },
  {
    name: 'after-core', line: 7, path: 'after-core',
    url: 'https://github.com/open-hax/after-core.git', parseStatus: 'valid',
  },
]);
assert.deepEqual(parseRoot(`open-hax/foresight@${sha('a')}`), {
  fullName: 'open-hax/foresight', revision: sha('a'),
});
assert.equal(parseArgs(['--frontier-baseline', 'known.json']).frontierBaseline, 'known.json');
assert.throws(() => parseArgs(['--frontier-baseline']), /requires a path/);
assert.throws(() => parseRoot('open-hax/foresight@fcb30c0'), /full lowercase Git commit ID/);
assert.throws(() => parseRoot(`open-hax/foresight@${sha('A')}`), /full lowercase Git commit ID/);
assert.throws(() => parseRoot(`../foresight@${sha('a')}`), /Invalid GitHub repository name/);
assert.equal(normalizeGitHubUrl('https://github.com/../foresight').kind, 'unsupported');
assert.equal(edn({ 'event/type': 'repository/observed', ok: true, xs: ['a', 1] }),
  '{:event/type "repository/observed" :ok true :xs ["a" 1]}');

function response(status, body) {
  const text = typeof body === 'string' ? body : JSON.stringify(body);
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: { get: () => null },
    arrayBuffer: async () => Buffer.from(text),
    json: async () => JSON.parse(text),
    text: async () => text,
  };
}

const manifestText = '[submodule "child"]\n  path = child\n  url = git@github.com:open-hax/child.git\n';
const fetchCalls = [];
const apiClient = new GitHubClient('token', {
  fetchImpl: async (url) => {
    fetchCalls.push(url);
    if (url.startsWith('https://raw.githubusercontent.com')) return response(200, manifestText);
    if (url.endsWith(`/git/commits/${sha('a')}`)) return response(200, { tree: { sha: 'root-tree' } });
    if (url.endsWith('/git/trees/root-tree?recursive=1')) {
      return response(200, {
        tree: [{ path: '.gitmodules', mode: '100644', type: 'blob', sha: gitBlobSha(manifestText) }],
      });
    }
    return response(500, { message: `unexpected ${url}` });
  },
});
assert.equal(await apiClient.manifest('open-hax/root', sha('a')), manifestText);
assert.equal(fetchCalls.some((url) => url.startsWith('https://raw.githubusercontent.com')), true);
assert.equal(fetchCalls.filter((url) => url.startsWith('https://api.github.com')).length, 2);

const absentClient = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(404, 'Not Found');
    if (url.includes('/git/commits/')) return response(200, { tree: { sha: 'empty-tree' } });
    return response(500, { message: `unexpected ${url}` });
  },
});
assert.equal(await absentClient.manifest('open-hax/root', sha('a')), null);

const exactPathClient = new GitHubClient(null, {
  fetchImpl: async () => response(200, {
    truncated: false, tree: [{ path: 'child', mode: '160000', type: 'commit', sha: sha('c') }],
  }),
});
assert.equal((await exactPathClient.lookupTreePath('open-hax/root', 'tree', '/child')).status, 'missing');

const truncatedTreeClient = new GitHubClient(null, {
  fetchImpl: async () => response(200, { truncated: true, tree: [] }),
});
await assert.rejects(
  truncatedTreeClient.lookupTreePath('open-hax/root', 'tree', '.gitmodules'),
  /truncated Git tree/,
);

const symlinkManifestClient = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(200, 'target');
    if (url.includes('/git/commits/')) return response(200, { tree: { sha: 'symlink-tree' } });
    if (url.endsWith('/git/trees/symlink-tree?recursive=1')) {
      return response(200, {
        tree: [{ path: '.gitmodules', mode: '120000', type: 'blob', sha: 'symlink-blob' }],
      });
    }
    return response(200, {
      encoding: 'base64', size: 6, content: Buffer.from('target').toString('base64'),
    });
  },
});
await assert.rejects(symlinkManifestClient.manifest('open-hax/root', sha('a')), /regular blob/);

const mismatchedManifestClient = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(200, manifestText);
    if (url.includes('/git/commits/')) return response(200, { tree: { sha: 'manifest-tree' } });
    if (url.endsWith('/git/trees/manifest-tree?recursive=1')) {
      return response(200, {
        tree: [{ path: '.gitmodules', mode: '100644', type: 'blob', sha: sha('0') }],
      });
    }
    return response(500, { message: `unexpected ${url}` });
  },
});
await assert.rejects(
  mismatchedManifestClient.manifest('open-hax/root', sha('a')),
  /do not match Git blob identity/,
);

const unavailableClient = new GitHubClient(null, {
  fetchImpl: async () => response(404, { message: 'Not Found' }),
});
await assert.rejects(unavailableClient.manifest('open-hax/private', sha('a')), /HTTP 404/);
const unavailableTraversal = await census({
  roots: [`open-hax/private@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: unavailableClient });
assert.equal(unavailableTraversal.stats.frontierRemaining, 1);
assert.equal(unavailableTraversal.stats.repositoryRevisions, 0);
assert.equal(unavailableTraversal.gaps[0]['gap/type'], 'manifest/unavailable');
assert.equal(unavailableTraversal.gaps[0]['gap/frontier?'], true);

const retryDelays = [];
let retryAttempt = 0;
const retryingClient = new GitHubClient(null, {
  fetchImpl: async () => {
    retryAttempt += 1;
    return retryAttempt === 1
      ? response(500, { message: 'temporary' })
      : response(200, { tree: { sha: 'after-retry' } });
  },
  sleepImpl: async (milliseconds) => retryDelays.push(milliseconds),
});
assert.deepEqual(await retryingClient.commit('open-hax/retry', sha('a')), {
  tree: { sha: 'after-retry' },
});
assert.equal(retryingClient.requests, 2);
assert.deepEqual(retryDelays, [200]);

let persistentAttempts = 0;
const persistentFailureClient = new GitHubClient(null, {
  fetchImpl: async () => {
    persistentAttempts += 1;
    return response(504, { message: 'still unavailable' });
  },
  sleepImpl: async () => {},
});
await assert.rejects(
  persistentFailureClient.commit('open-hax/retry', sha('b')),
  /HTTP 504/,
);
assert.equal(persistentAttempts, 3);

function traversalClient(manifests) {
  return {
    requests: 0,
    rate: { remaining: null, reset: null },
    manifest: async (fullName, revision) => manifests.get(`${fullName}@${revision}`) ?? null,
    commit: async (_fullName, revision) => ({ tree: { sha: `tree-${revision}` } }),
    lookupTreePath: async (_fullName, _tree, repositoryPath) => ({
      status: 'found', entry: { type: 'commit', path: repositoryPath, sha: sha('c') },
    }),
  };
}

const rootManifest = new Map([[`open-hax/root@${sha('a')}`, manifestText]]);
const bounded = await census({
  roots: [`open-hax/root@${sha('a')}`], maxNodes: 1, maxDepth: 32, concurrency: 1,
}, { client: traversalClient(rootManifest) });
assert.equal(bounded.stats.frontierRemaining, 1);
assert.equal(bounded.gaps.some((gap) => gap['gap/type'] === 'recursion/max-nodes'), true);
assert.equal(bounded.gaps.find((gap) => gap['gap/type'] === 'recursion/max-nodes')['gap/frontier?'], true);

const depthBounded = await census({
  roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 0, concurrency: 1,
}, { client: traversalClient(rootManifest) });
assert.equal(depthBounded.stats.frontierRemaining, 1);
assert.equal(depthBounded.gaps.some((gap) => gap['gap/type'] === 'recursion/max-depth'), true);

const cycleManifest = '[submodule "self"]\n  path = self\n  url = git@github.com:open-hax/root.git\n';
const cycle = await census({
  roots: [`open-hax/root@${sha('c')}`], maxNodes: 10, maxDepth: 0, concurrency: 1,
}, { client: traversalClient(new Map([[`open-hax/root@${sha('c')}`, cycleManifest]])) });
assert.equal(cycle.stats.frontierRemaining, 0);
assert.equal(cycle.gaps.some((gap) => gap['gap/type'] === 'recursion/max-depth'), false);

const duplicateManifest = [
  '[submodule "first"]', '  path = child', '  url = git@github.com:open-hax/child.git',
  '[submodule "second"]', '  path = child', '  url = git@github.com:open-hax/child.git', '',
].join('\n');
const duplicateDeclarations = await census({
  roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(new Map([[`open-hax/root@${sha('a')}`, duplicateManifest]])) });
assert.equal(duplicateDeclarations.occurrences.length, 2);
assert.equal(new Set(duplicateDeclarations.occurrences.map((row) => row['occurrence/id'])).size, 2);

const revisionManifests = new Map([
  [`open-hax/root@${sha('a')}`, ''],
  [`open-hax/root@${sha('b')}`, '# second revision\n'],
]);
const multiRevision = await census({
  roots: [`open-hax/root@${sha('a')}`, `open-hax/root@${sha('b')}`],
  maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(revisionManifests) });
const digests = multiRevision.repositories[0]['repository/manifest-sha256-at-revisions'];
assert.deepEqual(Object.keys(digests).sort(), [sha('a'), sha('b')]);
assert.notEqual(digests[sha('a')], digests[sha('b')]);

const stableStats = {
  repositories: 1, repositoryRevisions: 1, occurrences: 0,
  resolvedOccurrences: 0, gaps: 0, frontierRemaining: 0,
  maxNodes: 10, maxDepth: 1,
};
assert.deepEqual(
  canonicalSummary({ roots: [], stats: { ...stableStats, githubRequests: 4, rateRemaining: 10, rateReset: 1 } }),
  canonicalSummary({ roots: [], stats: { ...stableStats, githubRequests: 9, rateRemaining: 2, rateReset: 99 } }),
);

const frontierResult = {
  roots: [{ revision: sha('a'), fullName: 'open-hax/root' }],
  gaps: [{
    'gap/id': 'volatile-id',
    'gap/type': 'manifest/unavailable',
    'gap/repository': 'github:open-hax/private',
    'gap/revision': sha('b'),
    'gap/depth': 1,
    'gap/http-status': 404,
    'gap/detail': 'volatile response detail',
    'gap/frontier?': true,
  }, {
    'gap/type': 'submodule/local-only',
    'gap/detail': 'not traversal-blocking',
  }],
};
const expectedFrontier = {
  frontier: [{
    'gap/revision': sha('b'),
    'gap/http-status': 404,
    'gap/repository': 'github:open-hax/private',
    'gap/type': 'manifest/unavailable',
    'gap/depth': 1,
  }],
  roots: [{ fullName: 'open-hax/root', revision: sha('a') }],
};
assert.deepEqual(canonicalFrontier(frontierResult), expectedFrontier);
assert.equal(frontierBaselineMatches(frontierResult, expectedFrontier), true);
assert.equal(frontierBaselineMatches(frontierResult, {
  ...expectedFrontier,
  frontier: [{ ...expectedFrontier.frontier[0], 'gap/http-status': 500 }],
}), false);
assert.equal(frontierBaselineMatches(frontierResult, { ...expectedFrontier, extra: true }), false);

const workflow = readFileSync('.github/workflows/repository-census.yml', 'utf8');
assert.match(workflow, /pull_request:/);
assert.match(workflow, /docs\/research\/repository-census-current-pinned-closure[.]md/);
assert.match(workflow, /--frontier-baseline docs\/research\/repository-census-known-frontier[.]json/);
console.log('repository-census tests passed');
