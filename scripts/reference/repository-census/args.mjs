// SPDX-License-Identifier: GPL-3.0-or-later

export function parseArgs(argv) {
  const options = {
    roots: [],
    outDir: 'reports/repository-census/current',
    maxNodes: 10000,
    maxDepth: 32,
    concurrency: 8,
    frontierBaseline: null,
  };
  const valueAfter = (index, flag) => {
    const value = argv[index + 1];
    if (typeof value !== 'string' || value.length === 0
        || value.startsWith('--') || value === '-h') {
      throw new Error(`${flag} requires a value`);
    }
    return value;
  };

  for (let i = 0; i < argv.length; i += 1) {
    const arg = argv[i];
    if (arg === '--root') options.roots.push(valueAfter(i++, arg));
    else if (arg === '--out') options.outDir = valueAfter(i++, arg);
    else if (arg === '--max-nodes') options.maxNodes = Number(valueAfter(i++, arg));
    else if (arg === '--max-depth') options.maxDepth = Number(valueAfter(i++, arg));
    else if (arg === '--concurrency') options.concurrency = Number(valueAfter(i++, arg));
    else if (arg === '--frontier-baseline') options.frontierBaseline = valueAfter(i++, arg);
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
  if (typeof options.outDir !== 'string' || options.outDir.length === 0) {
    throw new Error('--out requires a path');
  }
  if (options.frontierBaseline !== null
      && (typeof options.frontierBaseline !== 'string' || options.frontierBaseline.length === 0)) {
    throw new Error('--frontier-baseline requires a path');
  }
  return options;
}

export function usage() {
  return `Usage: node scripts/reference/repository-census/main.mjs \\
  --root owner/repo@COMMIT [--root owner/repo@COMMIT ...] \\
  [--out PATH] [--max-nodes N] [--max-depth N] [--concurrency N] \\
  [--frontier-baseline PATH]\n`;
}

export function isGitHubFullName(value) {
  if (typeof value !== 'string') return false;
  const parts = value.split('/');
  if (parts.length !== 2) return false;

  const [owner, repository] = parts;
  const validOwner = /^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$/.test(owner);
  const validRepository = /^(?!\.{1,2}$)[A-Za-z0-9._-]{1,100}$/.test(repository);
  return validOwner && validRepository;
}

export function parseRoot(spec) {
  if (typeof spec !== 'string') {
    throw new Error('Root must be owner/repo@revision');
  }
  const at = spec.lastIndexOf('@');
  if (at <= 0 || at === spec.length - 1) {
    throw new Error(`Root must be owner/repo@revision: ${spec}`);
  }
  const fullName = spec.slice(0, at);
  const revision = spec.slice(at + 1);
  if (!isGitHubFullName(fullName)) {
    throw new Error(`Invalid GitHub repository name in root: ${spec}`);
  }
  if (!/^(?:[0-9a-f]{40}|[0-9a-f]{64})$/.test(revision)) {
    throw new Error(`Root revision must be a full lowercase Git commit ID: ${spec}`);
  }
  return { fullName, revision };
}
