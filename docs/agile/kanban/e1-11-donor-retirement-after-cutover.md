---
uuid: "e1-11-donor-retirement-after-cutover"
title: "E1.11 — Donor packages and obsolete workflow paths are retired only after the cutover proof"
status: incoming
priority: P1
labels: ["eta-mu-breakdown", "migration", "donor-retirement", "5sp"]
created_at: "2026-09-13T00:00:00Z"
category: eta-mu-breakdown
points: 5
epic: "operation-eta-mu-breakdown"
story_id: "E1.11"
blocked_by: ["e1-07-remaining-eta-mu-consumers", "e1-08-osmos-ingestion-service", "e1-09-honest-quality-gates"]
---

# E1.11 — Donor retirement

**Acceptance.** Each donor directory is deleted in its own reviewable step only after every live consumer has a verified replacement. Remaining eta-mu and Knoxx test/build behavior is clean after a new install with caches and old outputs excluded. Historical Git objects and provenance are preserved.

## Tasks

- Re-run the exhaustive reference scan, classify remaining hits (live, historical, examples, migration records)
- Test actual resolver/launcher closure
- Obtain replacement CI evidence before deleting old CI
- Remove donor package directories and their now-obsolete package-local metadata only after acceptance
- Update workspace lockfiles without unrelated mass upgrades
- Produce final graph and dependency disposition report
- Promote child PRs, then update Foresight gitlinks to their final tested commits

## Failing fixtures first

- Inject one still-live import, runtime path, workflow selector or package dependency into a temporary fixture; retirement must be rejected
- Historical quoted references must be labeled rather than blindly rewritten

## Evidence

Zero-live-inbound proof per target, final clean builds/tests, replacement workflow runs, final commit/gitlink identities, and a tested re-pin/revert procedure.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.11).
