// SPDX-License-Identifier: GPL-3.0-or-later

import { posix as path } from 'node:path';
import { isGitHubFullName } from './args.mjs';

function decodeConfigValue(raw) {
  const characters = [];
  let quoted = false;

  for (let index = 0; index < raw.length; index += 1) {
    const character = raw[index];
    if (character === '\\') {
      const escaped = raw[++index];
      const decoded = { b: '\b', n: '\n', t: '\t', '\\': '\\', '"': '"' }[escaped];
      if (decoded === undefined) return { valid: false, value: null };
      characters.push({ character: decoded, quoted });
    } else if (character === '"') {
      quoted = !quoted;
    } else if (!quoted && (character === '#' || character === ';')) {
      break;
    } else {
      characters.push({ character, quoted });
    }
  }

  if (quoted) return { valid: false, value: null };
  while (characters.length && !characters[0].quoted && /^\s$/.test(characters[0].character)) {
    characters.shift();
  }
  while (characters.length && !characters.at(-1).quoted && /^\s$/.test(characters.at(-1).character)) {
    characters.pop();
  }
  return { valid: true, value: characters.map((item) => item.character).join('') };
}

function decodeSubmoduleName(raw) {
  let value = '';
  for (let index = 0; index < raw.length; index += 1) {
    if (raw[index] !== '\\') {
      value += raw[index];
      continue;
    }
    const escaped = raw[++index];
    if (escaped !== '\\' && escaped !== '"') return { valid: false, value: raw };
    value += escaped;
  }
  return { valid: true, value };
}

export function parseGitmodules(text) {
  const modules = [];
  let current = null;
  const section = /^\s*\[\s*submodule\s+"((?:[^"\\]|\\.)*)"\s*\]\s*(?:[#;].*)?$/i;
  const property = /^\s*([A-Za-z0-9._-]+)\s*=\s*(.*?)\s*$/;

  const flush = () => {
    if (current) modules.push(current);
    current = null;
  };

  for (const [index, rawLine] of text.replace(/\r\n?/g, '\n').split('\n').entries()) {
    const line = rawLine.trimEnd();
    if (!line.trim() || /^\s*[#;]/.test(line)) continue;

    const sectionMatch = line.match(section);
    if (sectionMatch) {
      flush();
      const name = decodeSubmoduleName(sectionMatch[1]);
      current = {
        name: name.value,
        line: index + 1,
        syntaxValid: name.valid,
      };
      continue;
    }

    if (/^\s*\[/.test(line)) {
      flush();
      continue;
    }

    const propertyMatch = line.match(property);
    if (propertyMatch && current) {
      const key = propertyMatch[1].toLowerCase();
      if (key === 'path' || key === 'url' || key === 'branch') {
        const value = decodeConfigValue(propertyMatch[2]);
        current.syntaxValid &&= value.valid;
        if (value.valid) current[key] = value.value;
      }
    }
  }
  flush();

  return modules.map((entry) => {
    const { syntaxValid, ...module } = entry;
    const parseStatus = !syntaxValid ? 'invalid-syntax'
      : module.path && module.url ? 'valid' : 'incomplete';
    return { ...module, parseStatus };
  });
}

export function normalizeGitHubUrl(rawUrl, parentFullName = null) {
  const raw = rawUrl.trim();
  if (/^(file:|\/)/.test(raw)) return { kind: 'local', raw };

  if (/^(?:\.\/|\.\.\/)/.test(raw)) {
    if (!isGitHubFullName(parentFullName)) return { kind: 'unsupported', raw };
    const resolved = path.normalize(`/${parentFullName}/${raw}`)
      .replace(/^\/+/, '').replace(/\/$/, '').replace(/\.git$/i, '');
    if (!isGitHubFullName(resolved)) return { kind: 'unsupported', raw };
    return {
      kind: 'github',
      raw,
      fullName: resolved,
      canonicalUrl: `https://github.com/${resolved}`,
    };
  }

  let candidate = raw;
  const scp = candidate.match(/^(?:[^@/]+@)?github\.com:([^/]+)\/(.+)$/i);
  if (scp) candidate = `https://github.com/${scp[1]}/${scp[2]}`;

  const ssh = candidate.match(/^ssh:\/\/(?:[^@/]+@)?github\.com\/([^/]+)\/(.+)$/i);
  if (ssh) candidate = `https://github.com/${ssh[1]}/${ssh[2]}`;

  const protocol = candidate.match(/^(?:https?|git):\/\/github\.com\/([^/]+)\/(.+)$/i);
  if (!protocol) return { kind: 'unsupported', raw };

  const owner = protocol[1];
  const repo = protocol[2].replace(/[?#].*$/, '').replace(/\/$/, '').replace(/\.git$/i, '');
  const fullName = `${owner}/${repo}`;
  if (!isGitHubFullName(fullName)) return { kind: 'unsupported', raw };

  return {
    kind: 'github',
    raw,
    fullName,
    canonicalUrl: `https://github.com/${owner}/${repo}`,
  };
}
