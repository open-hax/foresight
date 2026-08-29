#!/usr/bin/env node
// SPDX-License-Identifier: GPL-3.0-or-later

import assert from 'node:assert/strict';
import { parseRoot } from '../../scripts/reference/repository-census/args.mjs';
import { edn } from '../../scripts/reference/repository-census/edn.mjs';
import { normalizeGitHubUrl, parseGitmodules } from '../../scripts/reference/repository-census/gitmodules.mjs';

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
assert.deepEqual(parseRoot('open-hax/foresight@fcb30c0'), {
  fullName: 'open-hax/foresight', revision: 'fcb30c0',
});
assert.equal(edn({ 'event/type': 'repository/observed', ok: true, xs: ['a', 1] }),
  '{:event/type "repository/observed" :ok true :xs ["a" 1]}');
console.log('repository-census tests passed');
