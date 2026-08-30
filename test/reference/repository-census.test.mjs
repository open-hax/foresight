#!/usr/bin/env node
// SPDX-License-Identifier: GPL-3.0-or-later

import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
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
const quotedUrlWhitespace = parseGitmodules([
  '[submodule "quoted-url"]', '  path = child',
  '  url = " https://github.com/open-hax/child.git "', '',
].join('\n'))[0];
assert.equal(quotedUrlWhitespace.parseStatus, 'valid');
assert.equal(quotedUrlWhitespace.url, ' https://github.com/open-hax/child.git ');
assert.equal(normalizeGitHubUrl(quotedUrlWhitespace.url).kind, 'unsupported');
const nulTerminatedUrl = parseGitmodules([
  '[submodule "nul-url"]', '  path = child',
  '  url = https://github.com/open-hax/child.git\0ignored', '',
].join('\n'))[0];
assert.deepEqual(nulTerminatedUrl, {
  name: 'NUL byte in .gitmodules', line: 3, parseStatus: 'invalid-syntax',
});
assert.deepEqual(parseGitmodules([
  '[submodule "nul-header"]\0ignored', '  path = child',
  '  url = https://github.com/open-hax/child.git', '',
].join('\n')), [{
  name: 'NUL byte in .gitmodules', line: 1, parseStatus: 'invalid-syntax',
}]);
const emptyQuoteBoundaryUrl = parseGitmodules([
  '[submodule "quoted-boundary"]', '  path = child',
  '  url = https://github.com/open-hax/child.git ""', '',
].join('\n'))[0];
assert.equal(emptyQuoteBoundaryUrl.url, 'https://github.com/open-hax/child.git ');
assert.equal(normalizeGitHubUrl(emptyQuoteBoundaryUrl.url).kind, 'unsupported');
const escapedWhitespaceUrl = parseGitmodules([
  '[submodule "escaped-whitespace"]', '  path = child',
  '  url = https://github.com/open-hax/child.git\\t', '',
].join('\n'))[0];
assert.equal(escapedWhitespaceUrl.url, 'https://github.com/open-hax/child.git\t');
assert.equal(normalizeGitHubUrl(escapedWhitespaceUrl.url).kind, 'unsupported');
for (const continuedUrl of [
  '  url = https://github.com/open-hax/child.git \\',
  '  url = https://github.com/open-hax/child.git \\\n# comment',
  '  url = https://github.com/open-hax/child.git \\\n""',
]) {
  const module = parseGitmodules(`[submodule "continued"]\n  path = child\n${continuedUrl}`)[0];
  assert.equal(module.url, 'https://github.com/open-hax/child.git ');
  assert.equal(normalizeGitHubUrl(module.url).kind, 'unsupported');
}
const bareCarriageReturn = parseGitmodules(
  '[core]\nx=ignored\r[submodule "child"]\rpath=p\rurl=https://github.com/open-hax/child.git\n',
);
assert.equal(bareCarriageReturn.length, 1);
assert.equal(bareCarriageReturn[0].parseStatus, 'invalid-syntax');
assert.equal(bareCarriageReturn.some((module) => module.url), false);
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
assert.deepEqual(parseGitmodules([
  '[submodule "repeated"]',
  '  path = old-path',
  '  url = ../old.git',
  '  branch = retained-when-not-reassigned',
  '[submodule "other"]',
  '  path = other',
  '  url = ../other.git',
  '[submodule "repeated"]',
  '  path = current-path',
  '  url = ../current.git',
  '[submodule "Repeated"]',
  '  path = case-distinct',
  '  url = ../case-distinct.git',
  '',
].join('\n')), [
  {
    name: 'repeated', line: 1, path: 'current-path', url: '../current.git',
    branch: 'retained-when-not-reassigned', parseStatus: 'valid',
  },
  {
    name: 'other', line: 5, path: 'other', url: '../other.git', parseStatus: 'valid',
  },
  {
    name: 'Repeated', line: 11, path: 'case-distinct',
    url: '../case-distinct.git', parseStatus: 'valid',
  },
]);
assert.deepEqual(parseGitmodules([
  '[submodule "bare-override"]',
  '  path = old-path',
  '  url = ../retained.git',
  '[submodule "bare-override"]',
  '  path',
  '',
].join('\n')), [{
  name: 'bare-override', line: 1, url: '../retained.git', parseStatus: 'incomplete',
}]);
assert.deepEqual(parseGitmodules([
  '[submodule "repeated-invalid"]',
  '  path = old-path',
  '  url = ../old.git',
  '[submodule "repeated-invalid"]',
  '  path = current-path',
  '  url = ../current.git',
  '  not valid config syntax',
  '',
].join('\n')), [{
  name: 'repeated-invalid', line: 1, path: 'current-path', url: '../current.git',
  parseStatus: 'invalid-syntax',
}]);
assert.deepEqual(parseGitmodules('[submodule "child]\n  path = child\n  url = ../child.git\n'), [{
  name: '[submodule "child]', line: 1, parseStatus: 'invalid-syntax',
}]);
assert.deepEqual(parseGitmodules('[submodule.legacy]\n  path = child\n  url = ../child.git\n'), [{
  name: 'legacy', line: 1, path: 'child', url: '../child.git', parseStatus: 'valid',
}]);
assert.deepEqual(parseGitmodules([
  '[submodule.LegacyName]',
  '  path = old-path',
  '  url = ../old.git',
  '  branch = retained',
  '[submodule "legacyname"]',
  '  path = current-path',
  '  url = ../current.git',
  '',
].join('\n')), [{
  name: 'legacyname', line: 1, path: 'current-path', url: '../current.git',
  branch: 'retained', parseStatus: 'valid',
}]);
assert.deepEqual(parseGitmodules([
  '[submodule.LegacyName]',
  '  path = legacy-lowercase',
  '  url = ../legacy.git',
  '[submodule "LegacyName"]',
  '  path = quoted-case-sensitive',
  '  url = ../quoted.git',
  '',
].join('\n')), [
  {
    name: 'legacyname', line: 1, path: 'legacy-lowercase',
    url: '../legacy.git', parseStatus: 'valid',
  },
  {
    name: 'LegacyName', line: 4, path: 'quoted-case-sensitive',
    url: '../quoted.git', parseStatus: 'valid',
  },
]);
assert.deepEqual(parseGitmodules('[submodule]\n  path = child\n  url = ../child.git\n'), [{
  name: '[submodule]', line: 1, path: 'child', url: '../child.git',
  parseStatus: 'invalid-syntax',
}]);
assert.deepEqual(parseGitmodules('[submodule "a\\nb"]\n  path = child\n  url = ../child.git\n'), [{
  name: 'anb', line: 1, path: 'child', url: '../child.git', parseStatus: 'valid',
}]);
assert.deepEqual(parseGitmodules([
  '[core]', '  bare', '  key = value', '[submodule "child"]',
  '  path = child', '  url = https://github.com/open-hax/\\', 'child.git', '  shallow', '',
].join('\n')), [{
  name: 'child', line: 4, path: 'child',
  url: 'https://github.com/open-hax/child.git', parseStatus: 'valid',
}]);
assert.deepEqual(parseGitmodules('[submodule "terminal"]\n  url = ../child.git\n  path = child\\'), [{
  name: 'terminal', line: 1, path: 'child', url: '../child.git', parseStatus: 'valid',
}]);
assert.deepEqual(parseGitmodules(
  '[submodule "inline"] path = docs\n  url = ../docs.git\n',
), [{
  name: 'inline', line: 1, path: 'docs', url: '../docs.git', parseStatus: 'valid',
}]);
const rejectedNonValueContinuations = [
  '[submodule "synthetic"]\\\npath = child\nurl = ../child.git\n',
  '[submodule "synthetic"]\npa\\\nth = child\nurl = ../child.git\n',
  '[submodule "synthetic"]\npath\\\n = child\nurl = ../child.git\n',
  '[submod\\\nule "synthetic"]\npath = child\nurl = ../child.git\n',
  '[submodule "syn\\\nthetic"]\npath = child\nurl = ../child.git\n',
];
for (const rejectedManifest of rejectedNonValueContinuations) {
  const git = spawnSync('git', ['config', '-z', '-f', '-', '--get', 'submodule.synthetic.path'], {
    input: rejectedManifest, encoding: 'utf8',
  });
  assert.equal(git.status, 128);
  assert.equal(parseGitmodules(rejectedManifest).some(
    (module) => module.name === 'synthetic' && module.parseStatus === 'valid'
      && module.path === 'child' && module.url === '../child.git',
  ), false);
}
for (const acceptedManifest of [
  '[submodule "continued"]\npath = chil\\\nd\nurl = ../child.git\n',
  '[submodule "continued"] path = chil\\\nd\nurl = ../child.git\n',
]) {
  const git = spawnSync('git', ['config', '-z', '-f', '-', '--get', 'submodule.continued.path'], {
    input: acceptedManifest, encoding: 'utf8',
  });
  assert.equal(git.status, 0);
  assert.equal(git.stdout, 'child\0');
  assert.equal(parseGitmodules(acceptedManifest)[0].path, 'child');
  assert.equal(parseGitmodules(acceptedManifest)[0].parseStatus, 'valid');
}
for (const malformedHeader of [
  '[ submodule "child"]',
  '[submodule "child" ]',
]) {
  assert.equal(parseGitmodules(
    `${malformedHeader}\n  path = child\n  url = ../child.git\n`,
  )[0].parseStatus, 'invalid-syntax');
}
for (const nonGitWhitespace of ['\v', '\f', '\u00a0', '\u0085', '\u2003']) {
  assert.equal(parseGitmodules(
    `${nonGitWhitespace}[submodule "child"]\n  path = child\n  url = ../child.git\n`,
  )[0].parseStatus, 'invalid-syntax');
  assert.equal(parseGitmodules(
    `[submodule "child"]\n  path${nonGitWhitespace}= child\n  url = ../child.git\n`,
  )[0].parseStatus, 'invalid-syntax');
  assert.equal(parseGitmodules(
    `${nonGitWhitespace}# comment\n[submodule "child"]\n  path = child\n  url = ../child.git\n`,
  )[0].parseStatus, 'invalid-syntax');
  const valueWhitespace = parseGitmodules(
    `[submodule "child"]\n  path = child\n  url =${nonGitWhitespace}https://github.com/open-hax/child.git\n`,
  );
  assert.equal(valueWhitespace[0].parseStatus, 'valid');
  assert.equal(valueWhitespace[0].url.startsWith(nonGitWhitespace), true);
  assert.equal(normalizeGitHubUrl(valueWhitespace[0].url).kind, 'unsupported');
}
assert.equal(parseGitmodules('[core\npath = child\n')[0].parseStatus, 'invalid-syntax');
assert.equal(parseGitmodules([
  '[submodule "child"]', '  path = child', '  url = ../child.git', '  this is not config', '',
].join('\n'))[0].parseStatus, 'invalid-syntax');
assert.equal(parseGitmodules([
  '[submodule "child"]', '  path = child', '  url = ../child.git', '  invalid.key = value', '',
].join('\n'))[0].parseStatus, 'invalid-syntax');
assert.deepEqual(parseRoot(`open-hax/foresight@${sha('a')}`), {
  fullName: 'open-hax/foresight', revision: sha('a'),
});
assert.equal(parseArgs(['--frontier-baseline', 'known.json']).frontierBaseline, 'known.json');
for (const flag of [
  '--root', '--out', '--max-nodes', '--max-depth', '--concurrency', '--frontier-baseline',
]) {
  assert.throws(() => parseArgs([flag]), new RegExp(`${flag} requires`));
  assert.throws(() => parseArgs([flag, '--help']), new RegExp(`${flag} requires`));
}
assert.throws(() => parseRoot('open-hax/foresight@fcb30c0'), /full lowercase Git commit ID/);
assert.throws(() => parseRoot(`open-hax/foresight@${sha('A')}`), /full lowercase Git commit ID/);
assert.throws(() => parseRoot(`../foresight@${sha('a')}`), /Invalid GitHub repository name/);
assert.equal(normalizeGitHubUrl('https://github.com/../foresight').kind, 'unsupported');
assert.equal(edn({ 'event/type': 'repository/observed', ok: true, xs: ['a', 1] }),
  '{:event/type "repository/observed" :ok true :xs ["a" 1]}');
assert.equal(edn({
  'repository/submodule-count-at-revisions': {
    [sha('0')]: 1,
    [sha('a')]: 2,
    ['b'.repeat(64)]: 3,
  },
}), `{:repository/submodule-count-at-revisions {"${sha('0')}" 1 "${sha('a')}" 2 "${'b'.repeat(64)}" 3}}`);

function response(status, body, responseHeaders = {}) {
  const bytes = Buffer.isBuffer(body)
    ? body
    : Buffer.from(typeof body === 'string' ? body : JSON.stringify(body));
  const text = bytes.toString('utf8');
  const normalizedHeaders = new Map(Object.entries(responseHeaders)
    .map(([name, value]) => [name.toLowerCase(), String(value)]));
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: { get: (name) => normalizedHeaders.get(name.toLowerCase()) ?? null },
    arrayBuffer: async () => bytes,
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
    if (url.endsWith(`/git/commits/${sha('a')}`)) {
      return response(200, { sha: sha('a'), tree: { sha: sha('d') } });
    }
    if (url.endsWith(`/git/trees/${sha('d')}?recursive=1`)) {
      return response(200, {
        tree: [{ path: '.gitmodules', mode: '100644', type: 'blob', sha: gitBlobSha(manifestText) }],
      });
    }
    return response(500, { message: `unexpected ${url}` });
  },
});
assert.deepEqual(await apiClient.manifest('open-hax/root', sha('a')), {
  text: manifestText,
  sha256: createHash('sha256').update(manifestText).digest('hex'),
  sourceManifestBlobOid: gitBlobSha(manifestText),
  parentTreeOid: sha('d'),
});
assert.equal(fetchCalls.some((url) => url.startsWith('https://raw.githubusercontent.com')), true);
assert.equal(fetchCalls.filter((url) => url.startsWith('https://api.github.com')).length, 2);

let mismatchedCommitRequests = 0;
let mismatchedCommitTreeRequests = 0;
const mismatchedCommitIdentityClient = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(200, manifestText);
    if (url.endsWith(`/git/commits/${sha('a')}`)) {
      mismatchedCommitRequests += 1;
      return response(200, { sha: sha('b'), tree: { sha: sha('d') } });
    }
    if (url.includes('/git/trees/')) {
      mismatchedCommitTreeRequests += 1;
      return response(200, {
        truncated: false,
        tree: [{
          path: '.gitmodules', mode: '100644', type: 'blob', sha: gitBlobSha(manifestText),
        }],
      });
    }
    return response(500, { message: `unexpected ${url}` });
  },
});
await assert.rejects(
  mismatchedCommitIdentityClient.manifest('open-hax/root', sha('a')),
  new RegExp(`Git returned commit ${sha('b')} for requested revision open-hax/root@${sha('a')}`),
);
await assert.rejects(
  mismatchedCommitIdentityClient.commit('open-hax/root', sha('a')),
  /Git returned commit/,
);
assert.equal(mismatchedCommitRequests, 1);
assert.equal(mismatchedCommitTreeRequests, 0);

let wrongCommitRaw404TreeRequests = 0;
const wrongCommitRaw404Client = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(404, 'Not Found');
    if (url.endsWith(`/git/commits/${sha('a')}`)) {
      return response(200, { sha: sha('b'), tree: { sha: sha('d') } });
    }
    if (url.includes('/git/trees/')) {
      wrongCommitRaw404TreeRequests += 1;
      return response(200, { truncated: false, tree: [] });
    }
    return response(500, { message: `unexpected ${url}` });
  },
});
await assert.rejects(
  wrongCommitRaw404Client.manifest('open-hax/root', sha('a')),
  /Git returned commit/,
);
assert.equal(wrongCommitRaw404TreeRequests, 0);

const bomManifestBytes = Buffer.concat([Buffer.from([0xef, 0xbb, 0xbf]), Buffer.from(manifestText)]);
const bomClient = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(200, bomManifestBytes);
    if (url.endsWith(`/git/commits/${sha('b')}`)) {
      return response(200, { sha: sha('b'), tree: { sha: sha('e') } });
    }
    if (url.endsWith(`/git/trees/${sha('e')}?recursive=1`)) {
      return response(200, {
        tree: [{ path: '.gitmodules', mode: '100644', type: 'blob', sha: gitBlobSha(bomManifestBytes) }],
      });
    }
    return response(500, { message: `unexpected ${url}` });
  },
});
const bomManifest = await bomClient.manifest('open-hax/root', sha('b'));
assert.deepEqual(bomManifest, {
  text: manifestText,
  sha256: createHash('sha256').update(bomManifestBytes).digest('hex'),
  sourceManifestBlobOid: gitBlobSha(bomManifestBytes),
  parentTreeOid: sha('e'),
});
assert.equal(parseGitmodules(`\uFEFF${manifestText}`)[0].parseStatus, 'valid');

const absentClient = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(404, 'Not Found');
    if (url.includes('/git/commits/')) {
      return response(200, { sha: sha('a'), tree: { sha: 'empty-tree' } });
    }
    if (url.endsWith('/git/trees/empty-tree?recursive=1')) {
      return response(200, { truncated: false, tree: [] });
    }
    return response(500, { message: `unexpected ${url}` });
  },
});
assert.equal(await absentClient.manifest('open-hax/root', sha('a')), null);

const misleadingRaw404Client = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(404, 'Not Found');
    if (url.includes('/git/commits/')) {
      return response(200, { sha: sha('a'), tree: { sha: 'manifest-present-tree' } });
    }
    if (url.endsWith('/git/trees/manifest-present-tree?recursive=1')) {
      return response(200, {
        truncated: false,
        tree: [{
          path: '.gitmodules', mode: '100644', type: 'blob', sha: gitBlobSha(manifestText),
        }],
      });
    }
    return response(500, { message: `unexpected ${url}` });
  },
});
await assert.rejects(
  misleadingRaw404Client.manifest('open-hax/root', sha('a')),
  /HTTP 404 despite an exact Git tree entry/,
);

const exactPathClient = new GitHubClient(null, {
  fetchImpl: async () => response(200, {
    truncated: false, tree: [{ path: 'child', mode: '160000', type: 'commit', sha: sha('c') }],
  }),
});
assert.equal((await exactPathClient.lookupTreePath('open-hax/root', 'tree', '/child')).status, 'missing');

for (const incomplete of [undefined, null, 0, 'false']) {
  const incompleteRecursiveTreeClient = new GitHubClient(null, {
    fetchImpl: async () => response(200, {
      ...(incomplete === undefined ? {} : { truncated: incomplete }), tree: [],
    }),
  });
  await assert.rejects(
    incompleteRecursiveTreeClient.lookupTreePath('open-hax/root', 'tree', '.gitmodules'),
    /through incomplete Git tree/,
  );
}

for (const incomplete of [undefined, null, true, 0, 'false']) {
  const completeness = incomplete === undefined ? {} : { truncated: incomplete };
  const incompleteButFoundTreeClient = new GitHubClient(null, {
    fetchImpl: async (url) => {
      if (url.endsWith('?recursive=1')) return response(200, { ...completeness, tree: [] });
      if (url.endsWith(`/git/trees/${sha('d')}`)) {
        return response(200, {
          ...completeness,
          tree: [{ path: 'nested', mode: '040000', type: 'tree', sha: sha('e') }],
        });
      }
      return response(200, {
        ...completeness,
        tree: [{ path: '.gitmodules', mode: '100644', type: 'blob', sha: sha('f') }],
      });
    },
  });
  assert.equal(
    (await incompleteButFoundTreeClient.lookupTreePath(
      'open-hax/root', sha('d'), 'nested/.gitmodules',
    )).status,
    'found',
  );

  const incompleteButBlockedTreeClient = new GitHubClient(null, {
    fetchImpl: async (url) => response(200, url.endsWith('?recursive=1')
      ? { ...completeness, tree: [] }
      : {
        ...completeness,
        tree: [{ path: 'nested', mode: '100644', type: 'blob', sha: sha('e') }],
      }),
  });
  assert.equal(
    (await incompleteButBlockedTreeClient.lookupTreePath(
      'open-hax/root', sha('d'), 'nested/.gitmodules',
    )).status,
    'blocked',
  );
}

for (const incomplete of [undefined, null, true, 0, 'false']) {
  const incompleteFallbackTreeClient = new GitHubClient(null, {
    fetchImpl: async (url) => response(200, url.endsWith('?recursive=1')
      ? { truncated: true, tree: [] }
      : { ...(incomplete === undefined ? {} : { truncated: incomplete }), tree: [] }),
  });
  await assert.rejects(
    incompleteFallbackTreeClient.lookupTreePath('open-hax/root', 'tree', '.gitmodules'),
    /through incomplete Git tree/,
  );
}

const truncatedTreeClient = new GitHubClient(null, {
  fetchImpl: async () => response(200, { truncated: true, tree: [] }),
});
await assert.rejects(
  truncatedTreeClient.lookupTreePath('open-hax/root', 'tree', '.gitmodules'),
  /incomplete Git tree/,
);

const observedRequestSignals = [];
let requestBodyAttempts = 0;
const boundedRequestClient = new GitHubClient(null, {
  fetchImpl: async (_url, options) => {
    observedRequestSignals.push(options.signal);
    const candidate = response(200, { sha: sha('a'), tree: { sha: sha('a') } });
    const readBody = candidate.arrayBuffer;
    candidate.arrayBuffer = async () => {
      requestBodyAttempts += 1;
      if (requestBodyAttempts === 1) throw new Error('simulated body stream failure');
      return readBody();
    };
    return candidate;
  },
  sleepImpl: async () => {},
});
assert.deepEqual(
  await boundedRequestClient.commit('open-hax/request-timeout', sha('a')),
  { sha: sha('a'), tree: { sha: sha('a') } },
);
assert.equal(requestBodyAttempts, 2);
assert.equal(observedRequestSignals.length, 2);
assert.equal(observedRequestSignals.every((signal) => signal instanceof AbortSignal), true);
assert.equal(observedRequestSignals.every((signal) => !signal.aborted), true);
assert.notEqual(observedRequestSignals[0], observedRequestSignals[1]);

const bodyRateRetryDelays = [];
let bodyRateRetryAttempts = 0;
const bodyRateRetryClient = new GitHubClient(null, {
  fetchImpl: async () => {
    bodyRateRetryAttempts += 1;
    if (bodyRateRetryAttempts > 1) {
      return response(200, { sha: sha('a'), tree: { sha: 'after-body-rate-retry' } });
    }
    const candidate = response(429, { message: 'slow down' }, { 'retry-after': '60' });
    candidate.arrayBuffer = async () => { throw new Error('simulated rate-limit body failure'); };
    return candidate;
  },
  sleepImpl: async (milliseconds) => bodyRateRetryDelays.push(milliseconds),
});
assert.deepEqual(await bodyRateRetryClient.commit('open-hax/body-rate-retry', sha('a')), {
  sha: sha('a'),
  tree: { sha: 'after-body-rate-retry' },
});
assert.equal(bodyRateRetryAttempts, 2);
assert.deepEqual(bodyRateRetryDelays, [60000]);

let overlongBodyRateAttempts = 0;
const overlongBodyRateDelays = [];
const overlongBodyRateClient = new GitHubClient(null, {
  fetchImpl: async () => {
    overlongBodyRateAttempts += 1;
    const candidate = response(429, { message: 'slow down' }, { 'retry-after': '3600' });
    candidate.arrayBuffer = async () => { throw new Error('simulated rate-limit body failure'); };
    return candidate;
  },
  sleepImpl: async (milliseconds) => overlongBodyRateDelays.push(milliseconds),
});
await assert.rejects(
  overlongBodyRateClient.commit('open-hax/body-rate-bound', sha('a')),
  (error) => error.code === 'github/rate-limit-exhausted'
    && error.status === 429
    && error.retryDelayMs === 3600000
    && /refusing to retry early/.test(error.message),
);
assert.equal(overlongBodyRateAttempts, 1);
assert.deepEqual(overlongBodyRateDelays, []);

const persistentBodyRateDelays = [];
const persistentBodyRateClient = new GitHubClient(null, {
  fetchImpl: async () => {
    const candidate = response(429, { message: 'slow down' }, { 'retry-after': '1' });
    candidate.arrayBuffer = async () => { throw new Error('persistent rate-limit body failure'); };
    return candidate;
  },
  sleepImpl: async (milliseconds) => persistentBodyRateDelays.push(milliseconds),
});
await assert.rejects(
  persistentBodyRateClient.commit('open-hax/body-rate-terminal', sha('a')),
  (error) => error.code === 'github/rate-limit-exhausted'
    && error.status === 429
    && /body read failed/.test(error.message),
);
assert.equal(persistentBodyRateClient.requests, 3);
assert.deepEqual(persistentBodyRateDelays, [1000, 1000]);

const symlinkManifestClient = new GitHubClient(null, {
  fetchImpl: async (url) => {
    if (url.startsWith('https://raw.githubusercontent.com')) return response(200, 'target');
    if (url.includes('/git/commits/')) {
      return response(200, { sha: sha('a'), tree: { sha: 'symlink-tree' } });
    }
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
    if (url.includes('/git/commits/')) {
      return response(200, { sha: sha('a'), tree: { sha: sha('f') } });
    }
    if (url.endsWith(`/git/trees/${sha('f')}?recursive=1`)) {
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
      : response(200, { sha: sha('a'), tree: { sha: 'after-retry' } });
  },
  sleepImpl: async (milliseconds) => retryDelays.push(milliseconds),
});
assert.deepEqual(await retryingClient.commit('open-hax/retry', sha('a')), {
  sha: sha('a'),
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

const rateRetryDelays = [];
let rateRetryAttempt = 0;
const rateRetryClient = new GitHubClient(null, {
  fetchImpl: async () => {
    rateRetryAttempt += 1;
    return rateRetryAttempt === 1
      ? response(403, { message: 'secondary rate limit' }, {
        'retry-after': '60',
        'x-ratelimit-remaining': '4',
      })
      : response(200, { sha: sha('c'), tree: { sha: 'after-rate-retry' } });
  },
  sleepImpl: async (milliseconds) => rateRetryDelays.push(milliseconds),
});
assert.deepEqual(await rateRetryClient.commit('open-hax/rate-retry', sha('c')), {
  sha: sha('c'),
  tree: { sha: 'after-rate-retry' },
});
assert.equal(rateRetryClient.requests, 2);
assert.deepEqual(rateRetryDelays, [60000]);
assert.equal(rateRetryClient.retryDelay(response(403, {}, {
  'retry-after': '3600',
}), 1), 3600000);

const untimedRateRetryDelays = [];
let untimedRateRetryAttempt = 0;
const untimedRateRetryClient = new GitHubClient(null, {
  fetchImpl: async () => {
    untimedRateRetryAttempt += 1;
    return untimedRateRetryAttempt < 3
      ? response(403, { message: 'secondary rate limit' }, {
        'x-ratelimit-remaining': '4',
      })
      : response(200, { sha: sha('c'), tree: { sha: 'after-untimed-rate-retry' } });
  },
  sleepImpl: async (milliseconds) => untimedRateRetryDelays.push(milliseconds),
});
assert.deepEqual(await untimedRateRetryClient.commit('open-hax/untimed-rate-retry', sha('c')), {
  sha: sha('c'),
  tree: { sha: 'after-untimed-rate-retry' },
});
assert.equal(untimedRateRetryClient.requests, 3);
assert.deepEqual(untimedRateRetryDelays, [60000, 120000]);

const primaryResetClient = new GitHubClient(null, {
  nowImpl: () => 1788054689000,
});
assert.equal(primaryResetClient.retryDelay(response(403, {}, {
  'x-ratelimit-remaining': '0',
  'x-ratelimit-reset': '1788054691',
}), 1), 3000);
assert.equal(primaryResetClient.retryDelay(response(403, {}, {
  'x-ratelimit-remaining': '0',
  'x-ratelimit-reset': '1788058291',
}), 1), 3603000);

const persistentResetDelays = [];
const persistentResetClient = new GitHubClient(null, {
  fetchImpl: async () => response(403, { message: 'API rate limit exceeded' }, {
    'x-ratelimit-remaining': '0',
    'x-ratelimit-reset': '1788058291',
  }),
  nowImpl: () => 1788054689000,
  sleepImpl: async (milliseconds) => persistentResetDelays.push(milliseconds),
});
await assert.rejects(
  persistentResetClient.commit('open-hax/reset-bound', sha('c')),
  (error) => error.code === 'github/rate-limit-exhausted'
    && error.retryDelayMs === 3603000
    && /refusing to retry early/.test(error.message),
);
assert.equal(persistentResetClient.requests, 1);
assert.deepEqual(persistentResetDelays, []);

const overlongRetryAfterDelays = [];
const overlongRetryAfterClient = new GitHubClient(null, {
  fetchImpl: async () => response(429, { message: 'slow down' }, {
    'retry-after': '3600',
  }),
  sleepImpl: async (milliseconds) => overlongRetryAfterDelays.push(milliseconds),
});
await assert.rejects(
  overlongRetryAfterClient.commit('open-hax/retry-after-bound', sha('c')),
  (error) => error.code === 'github/rate-limit-exhausted'
    && error.retryDelayMs === 3600000
    && /refusing to retry early/.test(error.message),
);
assert.equal(overlongRetryAfterClient.requests, 1);
assert.deepEqual(overlongRetryAfterDelays, []);

const ordinaryForbiddenClient = new GitHubClient(null, {
  fetchImpl: async () => response(403, { message: 'Forbidden' }, {
    'x-ratelimit-remaining': '4',
  }),
});
await assert.rejects(
  ordinaryForbiddenClient.commit('open-hax/forbidden', sha('c')),
  (error) => error.status === 403 && error.code === undefined,
);
assert.equal(ordinaryForbiddenClient.requests, 1);

const rateLimitedClient = new GitHubClient(null, {
  fetchImpl: async (url) => (url.startsWith('https://raw.githubusercontent.com')
    ? response(404, { message: 'Not Found' })
    : response(403, { message: 'API rate limit exceeded' }, {
      'x-ratelimit-remaining': '0',
      'x-ratelimit-reset': '1788054691',
    })),
  maxAttempts: 1,
});
await assert.rejects(
  rateLimitedClient.manifest('open-hax/rate-limited', sha('c')),
  (error) => error.code === 'github/rate-limit-exhausted'
    && error.rateReset === '1788054691'
    && /rate limit exhausted/.test(error.message),
);
await assert.rejects(
  census({
    roots: [`open-hax/rate-limited@${sha('c')}`],
    maxNodes: 10,
    maxDepth: 1,
    concurrency: 1,
  }, { client: rateLimitedClient }),
  /rate limit exhausted/,
);

function traversalClient(manifests) {
  return {
    requests: 0,
    rate: { remaining: null, reset: null },
    manifest: async (fullName, revision) => {
      const key = `${fullName}@${revision}`;
      if (!manifests.has(key)) return null;
      const value = manifests.get(key);
      return typeof value === 'string'
        ? {
          text: value,
          sha256: createHash('sha256').update(value).digest('hex'),
          sourceManifestBlobOid: gitBlobSha(value),
          parentTreeOid: sha('d'),
        }
        : value;
    },
    commit: async () => ({ tree: { sha: sha('d') } }),
    lookupTreePath: async (_fullName, _tree, repositoryPath) => ({
      status: 'found',
      entry: {
        type: 'commit', mode: '160000', path: repositoryPath, sha: sha('c'),
      },
    }),
  };
}

const bomTraversal = await census({
  roots: [`open-hax/root@${sha('b')}`], maxNodes: 10, maxDepth: 0, concurrency: 1,
}, { client: traversalClient(new Map([[`open-hax/root@${sha('b')}`, bomManifest]])) });
assert.equal(
  bomTraversal.repositories[0]['repository/manifest-sha256-at-revisions'][sha('b')],
  createHash('sha256').update(bomManifestBytes).digest('hex'),
);
assert.equal(bomTraversal.occurrences[0]['occurrence/source-manifest-blob-oid'], gitBlobSha(bomManifestBytes));
assert.equal(bomTraversal.occurrences[0]['occurrence/parent-tree-oid'], sha('e'));
assert.equal(bomTraversal.occurrences[0]['occurrence/source-manifest-path'], '.gitmodules');
assert.equal(
  bomTraversal.occurrences[0]['occurrence/source-manifest-sha256'],
  createHash('sha256').update(bomManifestBytes).digest('hex'),
);
await assert.rejects(
  census({
    roots: [`open-hax/root@${sha('b')}`], maxNodes: 10, maxDepth: 0, concurrency: 1,
  }, { client: traversalClient(new Map([[`open-hax/root@${sha('b')}`, {
    text: manifestText,
    sha256: createHash('sha256').update(manifestText).digest('hex'),
  }]])) }),
  /Manifest observation is invalid/,
);

const malformedDeclaration = await census({
  roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(new Map([[
  `open-hax/root@${sha('a')}`,
  '[submodule "child]\n  path = child\n  url = ../child.git\n',
]])) });
assert.equal(malformedDeclaration.occurrences[0]['occurrence/status'], 'invalid-declaration');
assert.equal(malformedDeclaration.gaps[0]['gap/type'], 'submodule/invalid-declaration');
assert.equal(malformedDeclaration.gaps[0]['gap/frontier?'], true);
assert.equal(malformedDeclaration.stats.frontierRemaining, 1);

const nonGitWhitespaceDeclaration = await census({
  roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(new Map([[
  `open-hax/root@${sha('a')}`,
  '\u00a0[submodule "child"]\n  path = child\n  url = ../child.git\n',
]])) });
assert.equal(nonGitWhitespaceDeclaration.occurrences[0]['occurrence/status'], 'invalid-declaration');
assert.equal(nonGitWhitespaceDeclaration.gaps[0]['gap/type'], 'submodule/invalid-declaration');
assert.equal(nonGitWhitespaceDeclaration.gaps[0]['gap/frontier?'], true);
assert.equal(nonGitWhitespaceDeclaration.stats.frontierRemaining, 1);

const continuedHeaderDeclaration = await census({
  roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(new Map([[
  `open-hax/root@${sha('a')}`,
  '[submodule "synthetic"]\\\npath = child\nurl = ../child.git\n',
]])) });
assert.equal(continuedHeaderDeclaration.repositories.length, 1);
assert.equal(continuedHeaderDeclaration.occurrences.some(
  (row) => row['occurrence/status'] === 'resolved',
), false);
assert.equal(continuedHeaderDeclaration.gaps.some(
  (gap) => gap['gap/type'] === 'submodule/invalid-declaration' && gap['gap/frontier?'] === true,
), true);

const malformedGitlinkManifest = new Map([[
  `open-hax/root@${sha('a')}`,
  '[submodule "child"]\n  path = child\n  url = ../child.git\n',
]]);
for (const malformedGitlinkEntry of [
  undefined,
  { type: 'commit', mode: undefined, path: 'child', sha: sha('c') },
  { type: 'commit', mode: null, path: 'child', sha: sha('c') },
  { type: 'commit', mode: '100644', path: 'child', sha: sha('c') },
  { type: 'commit', mode: '0160000', path: 'child', sha: sha('c') },
  { type: 'commit', mode: 160000, path: 'child', sha: sha('c') },
  { type: 'commit', mode: '16000', path: 'child', sha: sha('c') },
  { type: 'commit', mode: '160000 ', path: 'child', sha: sha('c') },
  { type: 'commit', mode: '160000', path: 'child', sha: undefined },
  { type: 'commit', mode: '160000', path: 'child', sha: 'main' },
  { type: 'commit', mode: '160000', path: 'child', sha: sha('A') },
]) {
  const client = traversalClient(malformedGitlinkManifest);
  client.lookupTreePath = async () => ({
    status: 'found', entry: malformedGitlinkEntry,
  });
  const result = await census({
    roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
  }, { client });
  assert.equal(result.repositories.length, 1);
  assert.equal(result.occurrences[0]['occurrence/status'], 'invalid-gitlink');
  assert.equal(result.occurrences[0]['occurrence/target-revision'], null);
  assert.equal(result.gaps[0]['gap/type'], 'submodule/invalid-gitlink');
  assert.equal(result.gaps[0]['gap/frontier?'], true);
  assert.equal(result.stats.frontierRemaining, 1);
}

const unavailableWithDetail = async (message) => census({
  roots: [`open-hax/private@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: {
  requests: 1,
  rate: { remaining: 0, reset: null },
  manifest: async () => {
    const error = new Error(message);
    error.status = 404;
    throw error;
  },
} });
const firstUnavailable = await unavailableWithDetail('Not Found');
const secondUnavailable = await unavailableWithDetail('Repository not found');
assert.notEqual(firstUnavailable.gaps[0]['gap/detail'], secondUnavailable.gaps[0]['gap/detail']);
assert.equal(firstUnavailable.gaps[0]['gap/id'], secondUnavailable.gaps[0]['gap/id']);
assert.equal(firstUnavailable.gaps[0]['gap/frontier?'], true);

for (const invalidManifest of [
  '[submodule "child"]\0ignored\n  path = child\n  url = ../child.git\n',
  '[core]\nx=ignored\r[submodule "child"]\rpath=child\rurl=../child.git\n',
]) {
  const invalidTraversal = await census({
    roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
  }, { client: traversalClient(new Map([[`open-hax/root@${sha('a')}`, invalidManifest]])) });
  assert.equal(invalidTraversal.repositories.length, 1);
  assert.equal(invalidTraversal.occurrences.length, 1);
  assert.equal(invalidTraversal.occurrences[0]['occurrence/status'], 'invalid-declaration');
  assert.equal(invalidTraversal.gaps[0]['gap/type'], 'submodule/invalid-declaration');
  assert.equal(invalidTraversal.stats.frontierRemaining, 1);
}

const boundaryProtectedUrlTraversal = await census({
  roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(new Map([[
  `open-hax/root@${sha('a')}`,
  '[submodule "child"]\n  path = child\n  url = https://github.com/open-hax/child.git ""\n',
]])) });
assert.equal(boundaryProtectedUrlTraversal.repositories.length, 1);
assert.equal(boundaryProtectedUrlTraversal.occurrences[0]['occurrence/status'], 'unsupported-url');
assert.equal(boundaryProtectedUrlTraversal.occurrences[0]['occurrence/raw-url'].endsWith(' '), true);

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

const duplicateRoots = await census({
  roots: [`open-hax/root@${sha('a')}`, `OPEN-HAX/root@${sha('a')}`],
  maxNodes: 2, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(rootManifest) });
const reversedDuplicateRoots = await census({
  roots: [`OPEN-HAX/root@${sha('a')}`, `open-hax/root@${sha('a')}`],
  maxNodes: 2, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(rootManifest) });
assert.equal(duplicateRoots.roots.length, 1);
assert.deepEqual(duplicateRoots.roots, [{ fullName: 'open-hax/root', revision: sha('a') }]);
assert.deepEqual(reversedDuplicateRoots.roots, duplicateRoots.roots);
assert.equal(duplicateRoots.stats.repositories, 2);
assert.deepEqual(reversedDuplicateRoots.stats, duplicateRoots.stats);
assert.equal(duplicateRoots.gaps.some((gap) => gap['gap/type'] === 'recursion/max-nodes'), false);

const boundedRootManifests = new Map([
  [`open-hax/alpha@${sha('a')}`, ''],
  [`open-hax/beta@${sha('b')}`, ''],
]);
const orderedBoundedRoots = await census({
  roots: [`open-hax/alpha@${sha('a')}`, `open-hax/beta@${sha('b')}`],
  maxNodes: 1, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(boundedRootManifests) });
const reversedBoundedRoots = await census({
  roots: [`open-hax/beta@${sha('b')}`, `open-hax/alpha@${sha('a')}`],
  maxNodes: 1, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(boundedRootManifests) });
assert.deepEqual(reversedBoundedRoots, orderedBoundedRoots);
assert.deepEqual(orderedBoundedRoots.roots, [
  { fullName: 'open-hax/alpha', revision: sha('a') },
  { fullName: 'open-hax/beta', revision: sha('b') },
]);
assert.equal(orderedBoundedRoots.repositories[0]['repository/full-name'], 'open-hax/alpha');
assert.equal(orderedBoundedRoots.gaps[0]['gap/repository'], 'github:open-hax/beta');
assert.equal(orderedBoundedRoots.gaps[0]['gap/depth'], 0);

const caseVariantChildManifests = new Map([
  [`open-hax/alpha@${sha('a')}`, [
    '[submodule "child"]', '  path = child',
    '  url = https://github.com/OPEN-HAX/child.git', '',
  ].join('\n')],
  [`open-hax/beta@${sha('b')}`, [
    '[submodule "child"]', '  path = child',
    '  url = https://github.com/open-hax/child.git', '',
  ].join('\n')],
  [`OPEN-HAX/child@${sha('c')}`, ''],
  [`open-hax/child@${sha('c')}`, ''],
]);
const caseVariantChildren = await census({
  roots: [`open-hax/alpha@${sha('a')}`, `open-hax/beta@${sha('b')}`],
  maxNodes: 3, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(caseVariantChildManifests) });
const reversedCaseVariantChildren = await census({
  roots: [`open-hax/beta@${sha('b')}`, `open-hax/alpha@${sha('a')}`],
  maxNodes: 3, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(caseVariantChildManifests) });
assert.deepEqual(reversedCaseVariantChildren.repositories, caseVariantChildren.repositories);
assert.deepEqual(reversedCaseVariantChildren.occurrences, caseVariantChildren.occurrences);
assert.deepEqual(reversedCaseVariantChildren.stats, caseVariantChildren.stats);
assert.equal(
  caseVariantChildren.repositories.find((row) => row['repository/id'] === 'github:open-hax/child')[
    'repository/full-name'
  ],
  'open-hax/child',
);
assert.deepEqual(
  caseVariantChildren.occurrences.map((row) => row['occurrence/target-full-name']),
  ['open-hax/child', 'open-hax/child'],
);

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

const repeatedSectionManifest = [
  '[submodule.SAME]', '  path = old-path', '  url = ../old.git',
  '[submodule "same"]', '  path = current-path', '  url = ../current.git', '',
].join('\n');
const repeatedSections = await census({
  roots: [`open-hax/root@${sha('a')}`], maxNodes: 10, maxDepth: 1, concurrency: 2,
}, { client: traversalClient(new Map([[
  `open-hax/root@${sha('a')}`, repeatedSectionManifest,
]])) });
assert.equal(repeatedSections.occurrences.length, 1);
assert.equal(repeatedSections.occurrences[0]['occurrence/path'], 'current-path');
assert.equal(repeatedSections.occurrences[0]['occurrence/raw-url'], '../current.git');
assert.equal(repeatedSections.occurrences[0]['occurrence/target'], 'github:open-hax/current');
assert.equal(repeatedSections.repositories.some(
  (row) => row['repository/id'] === 'github:open-hax/old',
), false);
assert.equal(repeatedSections.repositories.some(
  (row) => row['repository/id'] === 'github:open-hax/current',
), true);

const revisionManifests = new Map([
  [`open-hax/root@${sha('a')}`, ''],
  [`open-hax/root@${sha('b')}`, '# second revision\n'],
]);
const multiRevision = await census({
  roots: [`open-hax/root@${sha('a')}`, `open-hax/root@${sha('b')}`],
  maxNodes: 10, maxDepth: 1, concurrency: 1,
}, { client: traversalClient(revisionManifests) });
const digests = multiRevision.repositories[0]['repository/manifest-sha256-at-revisions'];
assert.deepEqual(Object.keys(digests), [sha('a'), sha('b')]);
assert.notEqual(digests[sha('a')], digests[sha('b')]);
assert.deepEqual(
  Object.keys(multiRevision.repositories[0]['repository/submodule-count-at-revisions']),
  [sha('a'), sha('b')],
);

const reverseDiscoveredManifests = new Map([
  [`open-hax/root@${sha('f')}`, [
    '[submodule "later"]', '  path = later', '  url = ../child.git',
    '[submodule "earlier"]', '  path = earlier', '  url = ../child.git', '',
  ].join('\n')],
  [`open-hax/child@${sha('b')}`, '# later revision\n'],
  [`open-hax/child@${sha('a')}`, '# earlier revision\n'],
]);
const reverseDiscoveryClient = traversalClient(reverseDiscoveredManifests);
reverseDiscoveryClient.lookupTreePath = async (_fullName, _tree, repositoryPath) => ({
  status: 'found',
  entry: {
    type: 'commit', mode: '160000', path: repositoryPath,
    sha: repositoryPath === 'later' ? sha('b') : sha('a'),
  },
});
const reverseDiscoveredRevisions = await census({
  roots: [`open-hax/root@${sha('f')}`], maxNodes: 3, maxDepth: 1, concurrency: 1,
}, { client: reverseDiscoveryClient });
const reverseDiscoveredChild = reverseDiscoveredRevisions.repositories.find(
  (row) => row['repository/id'] === 'github:open-hax/child',
);
const reverseDiscoveredDigests = reverseDiscoveredChild[
  'repository/manifest-sha256-at-revisions'
];
assert.deepEqual(Object.keys(reverseDiscoveredDigests), [sha('a'), sha('b')]);
assert.equal(
  reverseDiscoveredDigests[sha('a')],
  createHash('sha256').update('# earlier revision\n').digest('hex'),
);
assert.equal(
  reverseDiscoveredDigests[sha('b')],
  createHash('sha256').update('# later revision\n').digest('hex'),
);
assert.deepEqual(
  Object.entries(reverseDiscoveredChild['repository/submodule-count-at-revisions']),
  [[sha('a'), 0], [sha('b'), 0]],
);

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
for (const status of [500, 504, null]) {
  const retryableResult = {
    ...frontierResult,
    gaps: [{ ...frontierResult.gaps[0], 'gap/http-status': status }],
  };
  assert.equal(frontierBaselineMatches(retryableResult, canonicalFrontier(retryableResult)), false);
}
assert.equal(frontierBaselineMatches(frontierResult, { ...expectedFrontier, extra: true }), false);
const reorderedRoots = {
  ...frontierResult,
  roots: [
    { fullName: 'open-hax/zeta', revision: sha('c') },
    ...frontierResult.roots,
  ],
};
const reorderedBaseline = canonicalFrontier(reorderedRoots);
assert.equal(frontierBaselineMatches({
  ...reorderedRoots,
  roots: [...reorderedRoots.roots].reverse(),
}, reorderedBaseline), true);

const workflow = readFileSync('.github/workflows/repository-census.yml', 'utf8');
assert.match(workflow, /pull_request:/);
assert.match(workflow, /docs\/research\/repository-census-current-pinned-closure[.]md/);
assert.match(workflow, /--frontier-baseline docs\/research\/repository-census-known-frontier[.]json/);
assert.match(
  workflow,
  /census-\$\{\{ github[.]event[.]pull_request[.]head[.]repo[.]full_name \|\| github[.]repository \}\}-\$\{\{ github[.]head_ref \|\| github[.]ref_name \}\}/,
);
assert.match(workflow, /concurrency:\s+[\s\S]*cancel-in-progress: true/);
assert.match(
  workflow,
  /ref: \$\{\{ github[.]event[.]pull_request[.]head[.]sha \|\| github[.]sha \}\}/,
);

const censusWorkflowIdentity = ({
  repository, pullRequestHeadRepository = null, headRef = '', refName,
  pullRequestHeadSha = null, sha: eventSha,
}) => ({
  group: `census-${pullRequestHeadRepository || repository}-${headRef || refName}`,
  checkout: pullRequestHeadSha || eventSha,
});
const sameHeadPush = censusWorkflowIdentity({
  repository: 'open-hax/foresight', refName: 'research/repository-census', sha: sha('a'),
});
const sameHeadPullRequest = censusWorkflowIdentity({
  repository: 'open-hax/foresight',
  pullRequestHeadRepository: 'open-hax/foresight',
  headRef: 'research/repository-census', refName: '60/merge',
  pullRequestHeadSha: sha('a'), sha: sha('b'),
});
assert.deepEqual(sameHeadPullRequest, sameHeadPush);
const supersedingPush = censusWorkflowIdentity({
  repository: 'open-hax/foresight', refName: 'research/repository-census', sha: sha('c'),
});
assert.equal(supersedingPush.group, sameHeadPush.group);
assert.notEqual(supersedingPush.checkout, sameHeadPush.checkout);
const forkPullRequest = censusWorkflowIdentity({
  repository: 'open-hax/foresight', pullRequestHeadRepository: 'contributor/foresight',
  headRef: 'research/repository-census', refName: '60/merge',
  pullRequestHeadSha: sha('a'), sha: sha('b'),
});
assert.notEqual(forkPullRequest.group, sameHeadPullRequest.group);
assert.equal(forkPullRequest.checkout, sameHeadPullRequest.checkout);
assert.doesNotMatch(workflow, /uses: actions\/(?:checkout|setup-node|upload-artifact)@v\d/);
assert.match(workflow, /actions\/checkout@fbc6f3992d24b796d5a048ff273f7fcc4a7b6c09/);
assert.match(workflow, /actions\/setup-node@49933ea5288caeca8642d1e84afbd3f7d6820020/);
assert.match(workflow, /actions\/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02/);
console.log('repository-census tests passed');
