// SPDX-License-Identifier: GPL-3.0-or-later

import { createHash } from 'node:crypto';

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
    const entries = Object.entries(value).filter(([, item]) => item !== undefined);
    return `{${entries.map(([key, item]) => `:${key} ${edn(item)}`).join(' ')}}`;
  }
  throw new Error(`Cannot encode EDN value of type ${typeof value}`);
}
