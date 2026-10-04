---
category: "kanban"
labels: "truth, cognitive-substrate, simulation, engine, throughput, 3sp"
type: "story"
story_id: "CS1.04"
points: "3"
title: "CS1.04 — Evaluate Truth as a reusable causal simulation engine"
dependency: ["recover-cognitive-substrate-intent"]
status: "incoming"
epic: "cognitive-substrate-research-program"
parent: "cognitive-substrate-research-program"
uuid: "truth-causal-simulation-engine"
---

# CS1.04 — Evaluate Truth as a reusable causal simulation engine

## User story

**As a** simulation researcher,  
**I want** Truth evaluated and described as a reusable engine for causal,
event-sourced field/particle/graph dynamics rather than only one game,  
**so that** the same substrate can run Foresight experiments and other causal
world models without inheriting a game-specific product boundary.

## Context

The existing game and solar-system-style simulation are evidence and useful
fixtures. This story does not presume the owning Truth repository already
implements the generalized engine boundary correctly.

Latency for direct player interaction is not the primary optimization target.
The intended cognitive use is primarily event-driven and throughput-oriented.

## Acceptance criteria

- [ ] Archaeology distinguishes Truth's observed current game/API from the proposed reusable engine role.
- [ ] The reusable kernel, if supported by evidence, is described in terms of pure state transitions, event inputs, deterministic/replayable configuration, and measurable simulation outputs.
- [ ] Benchmarks report throughput and work per event/resolution step separately from frame latency.
- [ ] Existing game/solar-system behavior remains a fixture/example rather than being deleted to make the new narrative fit.
- [ ] Any proposed ownership change is implemented in the Truth repository, with Foresight retaining only suite-level routing/integration law.
- [ ] If the current code does not support a clean engine extraction, that result becomes input to a new story rather than being papered over.

## Tasks within this story

- [ ] Inspect Truth at an exact revision and recover current simulation boundaries.
- [ ] Identify the smallest reusable kernel hypothesis.
- [ ] Define benchmark fixtures consumed by CS1.08.

## Non-goals

Renaming Truth by documentation alone, optimizing a game loop that the cognitive
work does not need, or copying Truth internals into Foresight root.
