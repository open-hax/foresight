---
category: "kanban"
labels: "cognitive-substrate, benchmark, gpu, vectorization, particles, 3sp"
type: "story"
story_id: "CS1.08"
points: "3"
title: "CS1.08 — Measure the dynamics before choosing the optimization architecture"
dependency: ["lazy-multiresolution-cognitive-projection", "truth-causal-simulation-engine"]
status: "incoming"
epic: "cognitive-substrate-research-program"
parent: "cognitive-substrate-research-program"
uuid: "cognitive-substrate-performance-experiments"
---

# CS1.08 — Measure the dynamics before choosing the optimization architecture

## User story

**As a** researcher,\
**I want** reproducible benchmarks of the particle/field/graph dynamics across
dimensions and execution strategies,\
**so that** CPU vectorization, GPU kernels, spatial partitioning, or distribution
are chosen from evidence rather than from the visual appeal of an implementation.

## Context

A prior prototype reportedly sustained roughly 1,000–2,000 interactive
problem-solving particles on one older CPU core. Treat that as a historical
lead to reconstruct and measure, not a current benchmark result.

## Acceptance criteria

- [ ] A small canonical workload defines particles/signals, field influence, collisions/interactions, graph access, and topology update frequency.
- [ ] Results separate throughput, latency, memory/bandwidth cost, approximation error, and behavioral outcome.
- [ ] The same workload is compared in 2D, 3D, and at least one higher-dimensional semantic representation or a documented reason why that comparison is invalid.
- [ ] Fast signal/field steps and slower topology/plasticity updates are measured separately.
- [ ] A vectorized CPU baseline exists before GPU/distributed acceleration is claimed necessary.
- [ ] GPU suitability is evaluated for bulk math and irregular graph access separately.
- [ ] Generated equations are accompanied by an operational explanation, invariant/property test, and units/range assumptions where meaningful.

## Tasks within this story

- [ ] Recover or reconstruct the old benchmark/prototype if possible.
- [ ] Define seeded workloads and measurement format.
- [ ] Implement the simplest correct CPU reference.
- [ ] Compare candidate acceleration only after reference behavior is pinned.

## Non-goals

Choosing CUDA/WebGPU/distribution in advance, maximizing particle count while
losing the behavior under study, or accepting mathematically ornate formulas
without executable interpretation.
