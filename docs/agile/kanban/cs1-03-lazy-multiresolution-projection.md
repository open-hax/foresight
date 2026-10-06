---
category: "kanban"
labels: "cognitive-substrate, simulation, graph, lazy-evaluation, barnes-hut, 5sp"
type: "story"
story_id: "CS1.03"
points: "5"
title: "CS1.03 — Resolve the cognitive graph lazily at multiple resolutions"
dependency: ["preserve-raw-causal-event-graph"]
status: "incoming"
epic: "cognitive-substrate-research-program"
parent: "cognitive-substrate-research-program"
uuid: "lazy-multiresolution-cognitive-projection"
---

# CS1.03 — Resolve the cognitive graph lazily at multiple resolutions

## User story

**As a** researcher,\
**I want** event-driven, lazy, multiresolution resolution over the full
provenance graph,\
**so that** the cognitive substrate can respond at useful scale without
continuously simulating every event while preserving global causal context and
exact evidence lineage.

## Model

The raw event graph remains ground truth. A secondary computational graph is a
snapshot/projection whose nodes may represent exact entities or coherent
aggregates. Distant, low-density, or low-influence regions may be summarized
analogously to Barnes-Hut gravity; nearby or decision-relevant regions are
resolved explicitly. Incoming events, queries, proposed actions, or changing
influence can promote an aggregate into finer detail.

## Acceptance criteria

- [ ] A projection can map many raw nodes into an aggregate while retaining exact references back to every contributing source region/event.
- [ ] Resolution is triggered by demand/events rather than requiring a global fixed-rate tick.
- [ ] An aggregate exposes bounded influence/error metadata sufficient to decide whether it must be opened.
- [ ] The same query/action boundary can request progressively finer resolution without changing raw history.
- [ ] Local projection updates satisfy explicit composition/invariance laws or produce a named approximation error when they do not.
- [ ] Tests compare full-resolution and aggregated runs on small fixtures and define the tolerance under which the approximation is accepted.
- [ ] The implementation distinguishes fast field/signal state from slower topology/plasticity updates.

## Tasks within this story

- [ ] Specify pure projection and refinement shapes in portable data/.cljc.
- [ ] Define aggregation/refinement laws before effectful scheduling.
- [ ] Create small exact fixtures, then add adaptive-resolution experiments.
- [ ] Record when omitted context is represented by aggregate influence versus genuinely unavailable evidence.

## Non-goals

Simulating the whole graph every frame, deleting cold evidence, or assuming a
Barnes-Hut analogue is valid before the comparison experiments pass.

## Verification

For bounded fixtures, run exact and multiresolution versions from the same raw
events and compare decisions, influence summaries, lineage, cost, and error.
