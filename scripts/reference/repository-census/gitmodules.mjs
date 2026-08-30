// SPDX-License-Identifier: GPL-3.0-or-later

import { posix as path } from 'node:path';
import { isGitHubFullName } from './args.mjs';

function decodeConfigValue(raw, continuationBoundaries = []) {
  const characters = [];
  const boundaries = new Set(continuationBoundaries);
  let quoted = false;

  for (let index = 0; index < raw.length; index += 1) {
    if (boundaries.has(index)) characters.push({ boundary: true });
    const character = raw[index];
    if (character === '\\') {
      const escaped = raw[++index];
      const decoded = { b: '\b', n: '\n', t: '\t', '\\': '\\', '"': '"' }[escaped];
      if (decoded === undefined) return { valid: false, value: null };
      characters.push({ character: decoded, protected: true });
    } else if (character === '"') {
      quoted = !quoted;
      // A closing quote commits preceding unquoted whitespace in Git's value parser,
      // even when the quoted segment itself is empty.
      if (!quoted) characters.push({ boundary: true });
    } else if (!quoted && (character === '#' || character === ';')) {
      break;
    } else {
      characters.push({ character, protected: quoted });
    }
  }

  if (quoted) return { valid: false, value: null };
  if (boundaries.has(raw.length)) characters.push({ boundary: true });
  while (characters.length && (characters[0].boundary
    || (!characters[0].protected && /^[ \t]$/.test(characters[0].character)))) {
    characters.shift();
  }
  while (characters.length && !characters.at(-1).boundary
    && !characters.at(-1).protected && /^[ \t]$/.test(characters.at(-1).character)) {
    characters.pop();
  }
  return {
    valid: true,
    value: characters.filter((item) => !item.boundary).map((item) => item.character).join(''),
  };
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
  const normalizedText = text.replace(/^\uFEFF/, '');
  const nulIndex = normalizedText.indexOf('\0');
  if (nulIndex !== -1) {
    // Git treats NUL contextually; rejecting the whole manifest prevents a NUL
    // suffix from turning malformed structural syntax into a traversable child.
    const line = normalizedText.slice(0, nulIndex).replace(/\r\n/g, '\n').split('\n').length;
    return [{ name: 'NUL byte in .gitmodules', line, parseStatus: 'invalid-syntax' }];
  }

  const modules = [];
  const modulesByName = new Map();
  let current = null;
  const quotedSection = /^[ \t]*\[([A-Za-z0-9][A-Za-z0-9-]*)[ \t]+"((?:[^"\\]|\\.)*)"\](.*)$/;
  const simpleSection = /^[ \t]*\[([A-Za-z0-9][A-Za-z0-9-]*)(?:\.([A-Za-z0-9.-]*))?\](.*)$/;
  const property = /^[ \t]*([A-Za-z][A-Za-z0-9-]*)[ \t]*=[ \t]*(.*)$/;
  const bareProperty = /^[ \t]*([A-Za-z][A-Za-z0-9-]*)[ \t]*$/;

  const startsValueAssignment = (candidate) => {
    let remainder = candidate;
    const quotedMatch = candidate.match(quotedSection);
    if (quotedMatch) {
      remainder = quotedMatch[3];
    } else {
      const simpleMatch = candidate.match(simpleSection);
      if (simpleMatch) remainder = simpleMatch[3];
      else if (/^[ \t]*\[/.test(candidate)) return false;
    }
    return property.test(remainder);
  };

  const flush = () => {
    if (current) {
      const previous = modulesByName.get(current.name);
      if (!previous) {
        modules.push(current);
        modulesByName.set(current.name, current);
      } else {
        // Git exposes repeated subsections as one keyspace. Preserve the first
        // declaration position, but apply only properties touched by this
        // later section so omitted values survive and assigned values win.
        previous.syntaxValid &&= current.syntaxValid;
        for (const key of current.assignedProperties) {
          if (Object.hasOwn(current, key)) previous[key] = current[key];
          else delete previous[key];
        }
      }
    }
    current = null;
  };

  const physicalLines = normalizedText.replace(/\r\n/g, '\n').split('\n');
  const logicalLines = [];
  for (let index = 0; index < physicalLines.length; index += 1) {
    let line = physicalLines[index];
    const continuationBoundaries = [];
    const firstLine = index + 1;
    // Git permits physical-line continuation only after a value assignment.
    // Section headers, variable names, and bare keys ending in a backslash are
    // malformed records rather than prefixes for the following physical line.
    const valueMayContinue = startsValueAssignment(line);
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
      if (!escaped || !valueMayContinue) break;
      line = line.slice(0, -1);
      continuationBoundaries.push(line.length);
      if (index + 1 >= physicalLines.length) break;
      line += physicalLines[++index];
    }
    logicalLines.push({ line, number: firstLine, continuationBoundaries });
  }

  const invalid = (line, number) => ({
    name: line.replace(/^[ \t]+|[ \t]+$/g, ''),
    line: number,
    syntaxValid: false,
  });

  for (const logicalLine of logicalLines) {
    let { line } = logicalLine;
    let lineOffset = 0;
    const { number } = logicalLine;
    if (/^[ \t]*$/.test(line) || /^[ \t]*[#;]/.test(line)) continue;

    const quotedMatch = line.match(quotedSection);
    if (quotedMatch) {
      flush();
      const name = decodeSubmoduleName(quotedMatch[2]);
      if (quotedMatch[1].toLowerCase() === 'submodule') {
        current = {
          name: name.value,
          line: number,
          syntaxValid: name.valid,
          assignedProperties: new Set(),
        };
      } else if (!name.valid) {
        modules.push(invalid(line, number));
      }
      lineOffset += line.length - quotedMatch[3].length;
      line = quotedMatch[3];
    } else {
      const simpleMatch = line.match(simpleSection);
      if (simpleMatch) {
        flush();
        if (simpleMatch[1].toLowerCase() === 'submodule') {
          current = simpleMatch[2] === undefined
            ? {
              name: line.replace(/^[ \t]+|[ \t]+$/g, ''),
              line: number,
              syntaxValid: false,
              assignedProperties: new Set(),
            }
            : {
              // Git's deprecated [section.subsection] syntax lowercases the
              // subsection; quoted subsection names remain case-sensitive.
              name: simpleMatch[2].toLowerCase(),
              line: number,
              syntaxValid: true,
              assignedProperties: new Set(),
            };
        }
        lineOffset += line.length - simpleMatch[3].length;
        line = simpleMatch[3];
      } else if (/^[ \t]*\[/.test(line)) {
        flush();
        modules.push(invalid(line, number));
        continue;
      }
    }

    if (/^[ \t]*$/.test(line) || /^[ \t]*[#;]/.test(line)) continue;

    const propertyMatch = line.match(property);
    if (propertyMatch) {
      const key = propertyMatch[1].toLowerCase();
      const rawValue = propertyMatch[2];
      const valueStart = lineOffset + line.length - rawValue.length;
      const value = decodeConfigValue(rawValue, logicalLine.continuationBoundaries
        .filter((boundary) => boundary >= valueStart && boundary <= valueStart + rawValue.length)
        .map((boundary) => boundary - valueStart));
      if (current) {
        current.syntaxValid &&= value.valid;
        if (value.valid && (key === 'path' || key === 'url' || key === 'branch')) {
          current.assignedProperties.add(key);
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
      if (current && (key === 'path' || key === 'url' || key === 'branch')) {
        current.assignedProperties.add(key);
        delete current[key];
      }
      continue;
    }

    if (current) current.syntaxValid = false;
    else modules.push(invalid(line, number));
  }
  flush();

  return modules.map((entry) => {
    const { syntaxValid, assignedProperties, ...module } = entry;
    const parseStatus = !syntaxValid ? 'invalid-syntax'
      : module.path && module.url ? 'valid' : 'incomplete';
    return { ...module, parseStatus };
  });
}

export function normalizeGitHubUrl(rawUrl, parentFullName = null) {
  const raw = rawUrl;
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
