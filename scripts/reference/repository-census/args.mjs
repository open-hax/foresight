// SPDX-License-Identifier: GPL-3.0-or-later

export function parseArgs(argv) {
  const options = {
    roots: [],
    outDir: 'reports/repository-census/current',
    maxNodes: 10000,
    maxDepth: 32,
    concurrency: 8,
  };

  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === '--root') options.roots.push(argv[++i]);
    else if (arg === '--out') options.outDir = argv[++i];
    else if (arg === '--max-nodes') options.maxNodes = Number(argv[++i]);
    else if (arg === '--max-depth') options.maxDepth = Number(argv[++i]);
    else if (arg === '--concurrency') options.concurrency = Number(argv[++i]);
    else if (arg === '--help' || arg === '-h') options.help = true;
    else throw new Error(`Unknown argument: ${arg}`);
  }

  if (!Number.isInteger(options.maxNodes) || options.maxNodes < 1) {
    throw new Error('--max-nodes must be a positive integer');
  }
  if (!Number.isInteger(options.maxDepth) || options.maxDepth < 0) {
    throw new Error('--max-depth must be a non-negative integer');
  }
  if (!Number.isInteger(options.concurrency) || options.concurrency < 1 || options.concurrency > 32) {
    throw new Error('--concurrency must be an integer from 1 to 32');
  }
  return options;
}

export function usage() {
  return `Usage: node scripts/reference/repository-census/main.mjs \\
  --root owner/repo@COMMIT [--root owner/repo@COMMIT ...] \\
  [--out PATH] [--max-nodes N] [--max-depth N] [--concurrency N]\n`;
}

export function parseRoot(spec) {
  const at = spec.lastIndexOf('@');
  if (at <= 0 || at === spec.length - 1) {
    throw new Error(`Root must be owner/repo@revision: ${spec}`);
  }
  const fullName = spec.slice(0, at);
  const revision = spec.slice(at + 1);
  if (!/^[^/\s]+\/[^/\s]+$/.test(fullName)) {
    throw new Error(`Invalid GitHub repository name in root: ${spec}`);
  }
  if (!/^[0-9a-fA-F]{7,40}$/.test(revision)) {
    throw new Error(`Root revision must be a commit SHA: ${spec}`);
  }
  return { fullName, revision: revision.toLowerCase() };
}
