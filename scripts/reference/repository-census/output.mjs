// SPDX-License-Identifier: GPL-3.0-or-later

import { mkdir, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { edn } from './edn.mjs';

export function canonicalSummary(result) {
  const {
    githubRequests: _githubRequests,
    rateRemaining: _rateRemaining,
    rateReset: _rateReset,
    ...stats
  } = result.stats;
  return { roots: result.roots, stats };
}

function markdown(result) {
  const lines = [
    '# Repository Census — Current Pinned Closure', '',
    '> Generated evidence projection. It is not a lifecycle, ownership, or continuation decision.', '',
    '## Roots', '', '| Repository | Revision |', '| --- | --- |',
    ...result.roots.map((root) => `| \`${root.fullName}\` | \`${root.revision}\` |`), '',
    '## Coverage', '',
    `- Canonical repository names observed: **${result.stats.repositories}**`,
    `- Distinct repository revisions inspected: **${result.stats.repositoryRevisions}**`,
    `- Submodule declarations observed: **${result.stats.occurrences}**`,
    `- Declarations resolved to pinned Gitlinks: **${result.stats.resolvedOccurrences}**`,
    `- Explicit gaps: **${result.stats.gaps}**`,
    `- Unprocessed frontier: **${result.stats.frontierRemaining}**`, '',
    '## Repositories', '',
    '| Repository | Root | Depth | Revisions | Manifest | Direct declarations by revision |',
    '| --- | ---: | ---: | ---: | --- | --- |',
    ...result.repositories.map((repo) => {
      const counts = Object.entries(repo['repository/submodule-count-at-revisions'] || {})
        .map(([revision, count]) => `${revision.slice(0, 12)}: ${count}`).join('<br>') || '—';
      const states = [...repo['repository/manifest-statuses']].sort().join(', ');
      return `| \`${repo['repository/full-name']}\` | ${repo['repository/root?'] ? 'yes' : ''} | ${repo['repository/min-depth']} | ${repo['repository/revisions'].length} | ${states} | ${counts} |`;
    }), '', '## Gap classes', '',
  ];

  const byType = new Map();
  for (const gap of result.gaps) byType.set(gap['gap/type'], (byType.get(gap['gap/type']) || 0) + 1);
  if (!byType.size) lines.push('- None');
  else for (const [type, count] of [...byType.entries()].sort()) lines.push(`- \`${type}\`: ${count}`);

  lines.push('', '## Interpretation boundary', '',
    'A path is an occurrence, not repository identity. A declared branch is not the pinned revision. A fork, mirror, local alias, or inaccessible target remains distinct until evidence establishes a relation. Missing and unavailable observations are retained as gaps rather than converted into empty success.', '');
  return lines.join('\n');
}

export async function writeResults(outDir, result) {
  await mkdir(outDir, { recursive: true });
  const writeNdEdn = async (name, rows) => {
    const body = rows.map(edn).join('\n') + (rows.length ? '\n' : '');
    await writeFile(path.join(outDir, name), body, 'utf8');
  };
  await Promise.all([
    writeNdEdn('repositories.edn', result.repositories),
    writeNdEdn('occurrences.edn', result.occurrences),
    writeNdEdn('gaps.edn', result.gaps),
    writeFile(path.join(outDir, 'index.md'), markdown(result), 'utf8'),
    writeFile(path.join(outDir, 'summary.json'), `${JSON.stringify(canonicalSummary(result), null, 2)}\n`, 'utf8'),
  ]);
}
