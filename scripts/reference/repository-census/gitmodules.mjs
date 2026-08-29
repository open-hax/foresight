// SPDX-License-Identifier: GPL-3.0-or-later

export function parseGitmodules(text) {
  const modules = [];
  let current = null;
  const section = /^\s*\[submodule\s+"((?:[^"\\]|\\.)*)"\]\s*$/;
  const property = /^\s*([A-Za-z0-9._-]+)\s*=\s*(.*?)\s*$/;

  for (const [index, rawLine] of text.replace(/\r\n?/g, '\n').split('\n').entries()) {
    const line = rawLine.trimEnd();
    if (!line.trim() || /^\s*[#;]/.test(line)) continue;

    const sectionMatch = line.match(section);
    if (sectionMatch) {
      if (current) modules.push(current);
      current = {
        name: sectionMatch[1].replace(/\\"/g, '"').replace(/\\\\/g, '\\'),
        line: index + 1,
      };
      continue;
    }

    const propertyMatch = line.match(property);
    if (propertyMatch && current) {
      const key = propertyMatch[1].toLowerCase();
      const value = propertyMatch[2];
      if (key === 'path' || key === 'url' || key === 'branch') current[key] = value;
    }
  }
  if (current) modules.push(current);

  return modules.map((entry) => ({
    ...entry,
    parseStatus: entry.path && entry.url ? 'valid' : 'incomplete',
  }));
}

export function normalizeGitHubUrl(rawUrl) {
  const raw = rawUrl.trim();
  if (/^(file:|\/|\.\/|\.\.\/)/.test(raw)) return { kind: 'local', raw };

  let candidate = raw;
  const scp = candidate.match(/^(?:[^@/]+@)?github\.com:([^/]+)\/(.+)$/i);
  if (scp) candidate = `https://github.com/${scp[1]}/${scp[2]}`;

  const ssh = candidate.match(/^ssh:\/\/(?:[^@/]+@)?github\.com\/([^/]+)\/(.+)$/i);
  if (ssh) candidate = `https://github.com/${ssh[1]}/${ssh[2]}`;

  const protocol = candidate.match(/^(?:https?|git):\/\/github\.com\/([^/]+)\/(.+)$/i);
  if (!protocol) return { kind: 'unsupported', raw };

  const owner = protocol[1];
  const repo = protocol[2].replace(/[?#].*$/, '').replace(/\/$/, '').replace(/\.git$/i, '');
  if (!owner || !repo || repo.includes('/')) return { kind: 'unsupported', raw };

  return {
    kind: 'github',
    raw,
    fullName: `${owner}/${repo}`,
    canonicalUrl: `https://github.com/${owner}/${repo}`,
  };
}
