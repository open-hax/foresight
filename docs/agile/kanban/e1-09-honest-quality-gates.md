---
uuid: "e1-09-honest-quality-gates"
title: "E1.09 — Every child has honest quality gates and self-contained agent guidance"
status: done
priority: P1
labels: ["eta-mu-breakdown", "migration", "quality-gates", "ci", "8sp"]
created_at: "2026-09-13T00:00:00Z"
category: eta-mu-breakdown
points: 8
epic: "operation-eta-mu-breakdown"
story_id: "E1.09"
blocked_by: ["e1-06-transplant-edges-one-at-a-time"]
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
