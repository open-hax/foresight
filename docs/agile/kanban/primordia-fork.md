---
uuid: "a06c7294-5eea-4280-b75a-d833aee8e68f"
parent: "459f49be-7023-40d2-9091-92cb945ce610"
title: "Create and register the downstream Primordia fork"
type: "story"
status: "incoming"
priority: "P1"
points: 3
labels: "primordia, truth, research, integration"
created_at: "2026-10-24"
---

## Context

Preserve upstream history and MIT attribution while making our simulator work a first-class Foresight child.

## Outcome

Create and register the downstream Primordia fork.

## Scope

Verify the fork parent, default branch and exact reachable SHA. Add Primordia gitlink, .gitmodules, project sources, AGENTS routing table, README and quality-gate entry together. Run project validation and project/workspace tests. Refuse registration when the remote or pinned commit is inaccessible.

## Non-goals

No Rust rewrite, universal alive flag, inferred SI calibration, or unvalidated coarse-graining.

## Acceptance criteria

- [ ] Verify the fork parent, default branch and exact reachable SHA. Add Primordia gitlink, .gitmodules, project sources, AGENTS routing table, README and quality-gate entry together. Run project validation and project/workspace tests. Refuse registration when the remote or pinned commit is inaccessible.
- [ ] Retain exact revisions, commands, outcomes, and missing checks.

## Verification

See [integration contract](../../integrations/primordia/README.md) and retained standalone evidence. This is an initial Markdown planning input, not a Rheos admission or operational status transition.

## Risks

Native lifecycle/thread affinity, GPU availability, sampled assay coverage, incomplete lineage tracing, and unknown physical calibration.
