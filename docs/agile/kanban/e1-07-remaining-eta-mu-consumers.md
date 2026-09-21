---
category: "eta-mu-breakdown"
labels: ["eta-mu-breakdown", "migration", "eta-mu-cli", "5sp"]
story_id: "E1.07"
write-id: "1789857179707-0.ephqu5z5fe87cm4b0qo"
points: "5"
title: "E1.07 — Remaining eta-mu consumers use the new products without donor copies"
blocked_by: ["e1-06-transplant-edges-one-at-a-time"]
priority: "P1"
status: "blocked"
epic: "operation-eta-mu-breakdown"
uuid: "e1-07-remaining-eta-mu-consumers"
created_at: "2026-09-13T00:00:00Z"
---

# E1.07 — Remaining consumers cut over

**Acceptance.** Eta-mu's remaining packages install/use the new product repositories and do not import the soon-to-be-deleted donor targets. The existing `eta-mu` command still reaches Rheos, Sol, and Session Mycology behavior. Shared tooling needed by remaining packages is retained or lawfully replaced.

## Tasks

- Change the eta-mu CLI's Rheos/Sol/Mycology npm references
- Replace its Mycology shadow source path
- Audit actual command launchers and source requires
- Repeat for every additional consumer found by E1.01
- Rewrite root test/lint/build selectors and integration fixtures
- Enforce remote dependency selection in the verification environment

## Failing fixtures first

- A clean consumer test with all extraction-target donor paths unavailable must still run
- A stale `../session-mycology/src/cljs`, root dispatch path, or packed launcher reference must be detected
- A zero-match pnpm filter must not become a green suite

## Evidence

Exhaustive inbound edge disposition, command smoke tests, standalone eta-mu checks, and no-selected-input-from-retired-targets report.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.07).

---
PR #96 reconciliation: eta-mu c2bbf7547592cb9e0c82eee01c2b01555c6cee68 switches Rheos/Sol/Mycology references, but retains Receipt River and other donor source paths. It is no longer pinned: the same two commits retire nine donor package directories, which is E1.11 work behind this card. The root gitlink stays at donor baseline 0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90. No isolated consumer proof is available; this is partial cutover, not accepted completion. See docs/migrations/eta-mu-breakdown/donor-retirement-blockers.md.

---
