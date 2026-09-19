---
category: "eta-mu-breakdown"
labels: ["eta-mu-breakdown", "migration", "dependency-edges", "8sp"]
story_id: "E1.06"
write-id: "1789857177772-0.ck5s83mp799fx0rbs4q"
points: "8"
title: "E1.06 — Receivers switch transplant-to-transplant edges one at a time"
blocked_by: ["e1-05-foresight-discovers-children"]
priority: "P1"
status: "blocked"
epic: "operation-eta-mu-breakdown"
uuid: "e1-06-transplant-edges-one-at-a-time"
created_at: "2026-09-13T00:00:00Z"
---

# E1.06 — Switch edges one at a time

**Acceptance.** Each changed dependency points to its new repository's tested full SHA, and the consumer still passes clean install/build/test/packaging checks. All intended public behavior is preserved.

## Tasks

- Start with low-coupling packages: Clio, Chat UI, Session Mycology, reconciled Axxium
- Assemble and validate orchestrator separately as contract data
- Switch Rheos's Chat UI source dependency after Chat UI exposes a consumable source contract
- Stage Sol with its remaining eta-mu/Turn Processor dependencies
- Separate package-manager DAGs from runtime and source-consumption relationships

## Failing fixtures first

- Removing the old donor's selected source path from an isolated consumer must not change a successful result
- A dependency update that resolves a newer/different commit or drops source/assets fails

## Evidence

Before/after typed graph, locked resolver output, changed source paths, fresh consumer logs, and per-edge rollback coordinate.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.06).

---
PR #96 reconciliation: Dependency documentation is being corrected to full source-observed SHAs. Consumer clean build/test/packaging evidence remains required before E1.06 can complete.
---