#!/usr/bin/env node
// SPDX-License-Identifier: GPL-3.0-or-later

import process from 'node:process';
import { readFile } from 'node:fs/promises';
import { parseArgs, usage } from './args.mjs';
import { census } from './census.mjs';
import { canonicalFrontier, frontierBaselineMatches, writeResults } from './output.mjs';

async function main() {
  const options = parseArgs(process.argv.slice(2));
  if (options.help) {
    process.stdout.write(usage());
    return;
  }
  const result = await census(options);
  await writeResults(options.outDir, result);
  process.stdout.write(`${JSON.stringify(result.stats)}\n`);

  if (options.frontierBaseline !== null) {
    const baseline = JSON.parse(await readFile(options.frontierBaseline, 'utf8'));
    if (!frontierBaselineMatches(result, baseline)) {
      console.error(`Observed frontier does not exactly match ${options.frontierBaseline}`);
      console.error(JSON.stringify(canonicalFrontier(result), null, 2));
      process.exitCode = 2;
      return;
    }
    process.stdout.write(`Observed frontier exactly matches ${options.frontierBaseline}\n`);
    return;
  }

  if (result.stats.frontierRemaining > 0) process.exitCode = 2;
}

main().catch((error) => {
  console.error(error.stack || error.message);
  process.exitCode = 1;
});
