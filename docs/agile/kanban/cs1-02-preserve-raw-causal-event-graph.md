---
category: "kanban"
labels: "cognitive-substrate, clio, event-sourcing, provenance, 3sp"
type: "story"
story_id: "CS1.02"
points: "3"
title: "CS1.02 — Preserve the raw causal event graph as exact ground truth"
dependency: ["recover-cognitive-substrate-intent"]
status: "incoming"
epic: "cognitive-substrate-research-program"
parent: "cognitive-substrate-research-program"
uuid: "preserve-raw-causal-event-graph"
---

# CS1.02 — Preserve the raw causal event graph as exact ground truth

## User story

**As a** human who must audit agent behavior,  
**I want** every relevant human, agent, tool, and environment interaction
represented as an immutable causal event with exact lineage,  
**so that** later projections can be approximate without making the evidence
for an action approximate.

## Observable outcome

A decision/action can be traversed back through exact admitted events even when
the active simulation represented most of the surrounding graph only through
aggregates.

## Acceptance criteria

- [ ] The event contract identifies stable event identity, source/actor, time observation, causal parents, payload/reference identity, and confidence/status where applicable.
- [ ] Harness turns and tool effects can be admitted as discrete events without storing private model chain-of-thought.
- [ ] Replaying accepted raw events reconstructs the same evidence graph independent of any cached simulation projection.
- [ ] A projection may reference, aggregate, or omit detail but cannot rewrite or replace its source event history.
- [ ] Missing parents, collisions, contradictory identity, and malformed provenance fail visibly at the canonical event boundary.
- [ ] Exact evidence lineage remains traversable from every derived/aggregate object that can influence a consequential action.

## Tasks within this story

- [ ] Reuse Clio/event contracts where they already own these semantics; do not fork a second ledger law in Foresight.
- [ ] Write failing fixtures for collision, missing parent, malformed source identity, and aggregate-without-lineage.
- [ ] Define the smallest event-to-graph projection required by CS1.03.

## Non-goals

Capturing hidden chain-of-thought, claiming that a Git hash proves truth, or
making the raw ledger itself the high-frequency simulation structure.

## Verification

Deterministic replay/lineage tests prove the exact event graph independently of
the approximate dynamical projection.
