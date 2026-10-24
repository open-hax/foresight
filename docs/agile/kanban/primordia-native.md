---
uuid: "fb5291be-dd7f-4eb4-8363-6cbcd92e2f11"
parent: "459f49be-7023-40d2-9091-92cb945ce610"
blocked_by: ["a06c7294-5eea-4280-b75a-d833aee8e68f", "bf276601-5713-4329-836d-5aec5325fe1b"]
title: "Host a batched Primordia native instance from Truth"
type: "story"
status: "incoming"
priority: "P1"
points: 5
labels: "primordia, truth, research, integration"
created_at: "2026-10-24"
---

## Context

Reuse optimized Rust directly without introducing another authoritative Truth world.

## Outcome

Host a batched Primordia native instance from Truth.

## Scope

Review the native contract first. Red tests cover invalid config, unknown handle, epoch mismatch, duplicate request, bounded queue, worker failure and shutdown. One owner per native Sim; CPU explicit first, GPU opt-in later. JVM FFM/JNI adapter stays in infra. ECS owns habitat identity and consumes immutable observations only at its barrier.

## Non-goals

No Rust rewrite, universal alive flag, inferred SI calibration, or unvalidated coarse-graining.

## Acceptance criteria

- [ ] Review the native contract first. Red tests cover invalid config, unknown handle, epoch mismatch, duplicate request, bounded queue, worker failure and shutdown. One owner per native Sim; CPU explicit first, GPU opt-in later. JVM FFM/JNI adapter stays in infra. ECS owns habitat identity and consumes immutable observations only at its barrier.
- [ ] Retain exact revisions, commands, outcomes, and missing checks.

## Verification

See [integration contract](../../integrations/primordia/README.md) and retained standalone evidence. This is an initial Markdown planning input, not a Rheos admission or operational status transition.

## Risks

Native lifecycle/thread affinity, GPU availability, sampled assay coverage, incomplete lineage tracing, and unknown physical calibration.
