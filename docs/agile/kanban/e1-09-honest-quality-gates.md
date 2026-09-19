---
category: "eta-mu-breakdown"
labels: ["eta-mu-breakdown", "migration", "quality-gates", "ci", "8sp"]
story_id: "E1.09"
write-id: "1789857183557-0.ksgntnlpq8gu4yxsqne"
points: "8"
title: "E1.09 — Every child has honest quality gates and self-contained agent guidance"
blocked_by: ["e1-06-transplant-edges-one-at-a-time"]
priority: "P1"
status: "blocked"
epic: "operation-eta-mu-breakdown"
uuid: "e1-09-honest-quality-gates"
created_at: "2026-09-13T00:00:00Z"
---

# E1.09 — Honest quality gates

**Acceptance.** Build/assembly, tests, lint, coverage, and applicable integration/E2E gates have stable names, real selection counts, retained reports and exact-revision identity. Every child has local `AGENTS.md` and accurate README commands. CI does not require donor checkout layout accidentally.

## Tasks

- Migrate dedicated jobs and factor mixed workflows carefully
- For contract-only orchestrator: implement EDN parsing, schema/reference conformance, host-load/MCP integration
- For source-only Clio: make assembly/packaging plus BB/NBB/Shadow consumer tests the relevant build contract
- Create a measured coverage baseline and repository-owned policy before ratcheting
- Build the Rheos browser app and smoke actual navigation
- Carry lint hooks and ignore rules into the new root

## Failing fixtures first

- A skipped/missing suite, omitted browser bundle, malformed contract, zero test count, changed locked dependency, or coverage-report loss must stop the relevant gate
- AGENTS/README command examples are checked against real scripts or tested equivalents

## Evidence

Workflows, local commands, reports with digests, test counts, coverage policy, lint output, browser screenshots where applicable, and agent-guidance review.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.09).

---
PR #96 reconciliation: Most pinned children lack the later AGENTS/workflow work claimed by historical receipts. Root gate discovery is being fixed, while child build/test/lint/coverage/browser acceptance remains incomplete; no blind repin to untested branch tips.
---