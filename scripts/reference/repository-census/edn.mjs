// SPDX-License-Identifier: GPL-3.0-or-later

import { createHash } from 'node:crypto';

function compareCodePointText(left, right) {
  const leftIterator = left[Symbol.iterator]();
  const rightIterator = right[Symbol.iterator]();
  while (true) {
    const leftCharacter = leftIterator.next();
    const rightCharacter = rightIterator.next();
    if (leftCharacter.done || rightCharacter.done) {
      if (leftCharacter.done && rightCharacter.done) return 0;
      return leftCharacter.done ? -1 : 1;
    }
    const leftCodePoint = leftCharacter.value.codePointAt(0);
    const rightCodePoint = rightCharacter.value.codePointAt(0);
    if (leftCodePoint !== rightCodePoint) return leftCodePoint < rightCodePoint ? -1 : 1;
  }
}

export function stableId(prefix, ...parts) {
  const digest = createHash('sha256').update(parts.join('\u0000')).digest('hex').slice(0, 24);
  return `${prefix}:${digest}`;
}

export function edn(value) {
  if (value === null || value === undefined) return 'nil';
  if (typeof value === 'string') return JSON.stringify(value);
  if (typeof value === 'number') return Number.isFinite(value) ? String(value) : 'nil';
  if (typeof value === 'boolean') return value ? 'true' : 'false';
  if (Array.isArray(value)) return `[${value.map(edn).join(' ')}]`;
  if (value instanceof Set) return `#{${[...value].sort().map(edn).join(' ')}}`;
  if (typeof value === 'object') {
    const entries = Object.entries(value)
      .filter(([, item]) => item !== undefined)
      .sort(([left], [right]) => compareCodePointText(left, right));
    return `{${entries.map(([key, item]) => {
      const encodedKey = /^(?:[0-9a-f]{40}|[0-9a-f]{64})$/.test(key)
        ? JSON.stringify(key)
        : `:${key}`;
      return `${encodedKey} ${edn(item)}`;
    }).join(' ')}}`;
  }
  throw new Error(`Cannot encode EDN value of type ${typeof value}`);
}
