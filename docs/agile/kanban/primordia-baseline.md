---
uuid: "bf276601-5713-4329-836d-5aec5325fe1b"
parent: "459f49be-7023-40d2-9091-92cb945ce610"
title: "Acquire standalone Primordia baseline and controls"
type: "story"
status: "incoming"
priority: "P1"
points: 3
labels: "primordia, truth, research, integration"
created_at: "2026-10-24"
---

## Context

Replace source-only expectations with reproducible simulations and inspectable evidence.

## Outcome

Acquire standalone Primordia baseline and controls.

## Scope

Build the pinned Rust source. Run golden tests, positive/null copying assays, replay a nonempty checkpoint set, and acquire matched unseeded resource-control runs. Retain configs, hashes, snapshots, logs and negative results. Do not infer heredity from the resource-free assay.

## Non-goals

No Rust rewrite, universal alive flag, inferred SI calibration, or unvalidated coarse-graining.

## Acceptance criteria

- [ ] Build the pinned Rust source. Run golden tests, positive/null copying assays, replay a nonempty checkpoint set, and acquire matched unseeded resource-control runs. Retain configs, hashes, snapshots, logs and negative results. Do not infer heredity from the resource-free assay.
- [ ] Retain exact revisions, commands, outcomes, and missing checks.

## Verification

See [integration contract](../../integrations/primordia/README.md) and retained standalone evidence. This is an initial Markdown planning input, not a Rheos admission or operational status transition.

## Risks

Native lifecycle/thread affinity, GPU availability, sampled assay coverage, incomplete lineage tracing, and unknown physical calibration.
