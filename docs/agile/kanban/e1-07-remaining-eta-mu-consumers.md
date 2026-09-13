---
uuid: "e1-07-remaining-eta-mu-consumers"
title: "E1.07 — Remaining eta-mu consumers use the new products without donor copies"
status: done
priority: P1
labels: ["eta-mu-breakdown", "migration", "eta-mu-cli", "5sp"]
created_at: "2026-09-13T00:00:00Z"
category: eta-mu-breakdown
points: 5
epic: "operation-eta-mu-breakdown"
story_id: "E1.07"
blocked_by: ["e1-06-transplant-edges-one-at-a-time"]
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
