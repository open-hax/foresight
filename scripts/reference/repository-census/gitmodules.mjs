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
    if (escaped === undefined) return { valid: false, value: raw };
    value += escaped;
  }
  return { valid: true, value };
}

export function parseGitmodules(text) {
  const modules = [];
  let current = null;
  const quotedSection = /^\s*\[([A-Za-z0-9][A-Za-z0-9-]*)\s+"((?:[^"\\]|\\.)*)"\](.*)$/;
  const simpleSection = /^\s*\[([A-Za-z0-9][A-Za-z0-9-]*)(?:\.([A-Za-z0-9.-]*))?\](.*)$/;
  const property = /^\s*([A-Za-z][A-Za-z0-9-]*)\s*=\s*(.*?)\s*$/;
  const bareProperty = /^\s*([A-Za-z][A-Za-z0-9-]*)\s*$/;

  const flush = () => {
    if (current) modules.push(current);
    current = null;
  };

  const physicalLines = text.replace(/^\uFEFF/, '').replace(/\r\n?/g, '\n').split('\n');
  const logicalLines = [];
  for (let index = 0; index < physicalLines.length; index += 1) {
    let line = physicalLines[index];
    const firstLine = index + 1;
    while (true) {
      let quoted = false;
      let escaped = false;
      for (const character of line) {
        if (escaped) {
          escaped = false;
        } else if (character === '\\') {
          escaped = true;
        } else if (character === '"') {
          quoted = !quoted;
        } else if (!quoted && (character === '#' || character === ';')) {
          break;
        }
      }
      if (!escaped) break;
      line = line.slice(0, -1);
      if (index + 1 >= physicalLines.length) break;
      line += physicalLines[++index];
    }
    logicalLines.push({ line: line.trimEnd(), number: firstLine });
  }

  const invalid = (line, number) => ({
    name: line.trim(),
    line: number,
    syntaxValid: false,
  });

  for (const logicalLine of logicalLines) {
    let { line } = logicalLine;
    const { number } = logicalLine;
    if (!line.trim() || /^\s*[#;]/.test(line)) continue;

    const quotedMatch = line.match(quotedSection);
    if (quotedMatch) {
      flush();
      const name = decodeSubmoduleName(quotedMatch[2]);
      if (quotedMatch[1].toLowerCase() === 'submodule') {
        current = { name: name.value, line: number, syntaxValid: name.valid };
      } else if (!name.valid) {
        modules.push(invalid(line, number));
      }
      line = quotedMatch[3];
    } else {
      const simpleMatch = line.match(simpleSection);
      if (simpleMatch) {
        flush();
        if (simpleMatch[1].toLowerCase() === 'submodule') {
          current = simpleMatch[2] === undefined
            ? { name: line.trim(), line: number, syntaxValid: false }
            : { name: simpleMatch[2], line: number, syntaxValid: true };
        }
        line = simpleMatch[3];
      } else if (/^\s*\[/.test(line)) {
        flush();
        modules.push(invalid(line, number));
        continue;
      }
    }

    if (!line.trim() || /^\s*[#;]/.test(line)) continue;

    const propertyMatch = line.match(property);
    if (propertyMatch) {
      const key = propertyMatch[1].toLowerCase();
      const value = decodeConfigValue(propertyMatch[2]);
      if (current) {
        current.syntaxValid &&= value.valid;
        if (value.valid && (key === 'path' || key === 'url' || key === 'branch')) {
          current[key] = value.value;
        }
      } else if (!value.valid) {
        modules.push(invalid(line, number));
      }
      continue;
    }

    const bareMatch = line.match(bareProperty);
    if (bareMatch) {
      const key = bareMatch[1].toLowerCase();
      if (current && (key === 'path' || key === 'url' || key === 'branch')) delete current[key];
      continue;
    }

    if (current) current.syntaxValid = false;
    else modules.push(invalid(line, number));
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
