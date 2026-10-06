# Immutable Clio compatibility evidence

This is a byte-preserved copy of the original Sol worker's isolated fixture,
manifest, frozen npm lock, machine proof and logs. It uses Clio Git revision
`788cdd3434615a7932b924e68520dbf7f88408c2`, Node 22.20.0/npm 10.9.3,
`fs-ext-extra-prebuilt` 2.2.9 and shadow-cljs 3.4.11. It is a feasibility probe,
not Sol integration code or board-state validation.

The original run reported one test / 13 assertions in each of CommonJS and ESM,
zero failures/errors and zero compiler warnings. Those logs are captured
observations. Copying them into this PR does not claim a fresh run. The original
proof's absolute source path points to the worker checkout; the same fixture is
published here as `test/probe_test.cljs`. Its SHA-256 is
`c398186d0cf6bbe341b67824f769fe31c31181b98dd134dd3217f1c4b7752c8f`.
The committed npm lock SHA-256 is
`51ac6cab08de73cb0f4d80027aa159dce1051b38bdca2a9616e5b8a2f68b024f`.

To reproduce, enter this directory with Node 22.20.0/npm 10.9.3 and a working
Clojure CLI/Java toolchain, then run:

```bash
npm ci --no-audit --no-fund
npx shadow-cljs compile test esm
node target/test.cjs
node dist/probe.js
```

Inspect compilation diagnostics and both test summaries: nonempty test counts,
zero failures/errors and zero warnings are required. Shadow's compilation exit
alone is insufficient because the autorun target may report test failures while
compilation exits zero. This directory does not add another authoritative test
gate; the actual Sol migration retains Sol's guarded runtime suite and CI.

The fixture invokes upstream Clio directly for real filesystem creation,
schema snapshots, append/readback, exact retry, reopen, partition replay,
ID/stream-slot collisions, missing partition and predecessor-envelope rejection.
Its stream-gap fixture invokes complete-history canonicalization. Append
admission does not itself prove that every causal parent or contiguous stream
history exists. Missing causes can be in another selected physical partition.

The test creates and removes temporary files through Clio. It proves sequential
locked operations and retry behavior; it does not prove concurrent contention,
fsync/fdatasync or power-loss durability. No hosted worker is qualified and no
Sol production dependency or adapter is changed by this evidence publication.

The complete original local artifact inventory and handoff remain at
`/home/err/spaces/review-repair/sol-server-workers/.ημ/node22-prerequisite/` as
`clio-admission-artifact-inventory.json` and `clio-admission-handoff.md`.
They are not asserted to be sibling files in this root PR.

Fixture and process documentation: GPL-3.0-or-later.
