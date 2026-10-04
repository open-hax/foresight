---
category: "kanban"
labels: "cognitive-substrate, visualization, observability, causality, epiphany, 5sp"
type: "story"
story_id: "CS1.07"
points: "5"
title: "CS1.07 — Build devtools for the cognitive substrate"
dependency: ["lazy-multiresolution-cognitive-projection", "purpose-roles-bounded-authority", "epiphany-cognitive-observatory"]
status: "incoming"
epic: "cognitive-substrate-research-program"
parent: "cognitive-substrate-research-program"
uuid: "cognitive-substrate-mind-devtools"
---

# CS1.07 — Build devtools for the cognitive substrate

## User story

**As a** human supervising an agent system,  
**I want** timelines, causal traces, field/activation views, spatial projections,
and drill-down from aggregates to exact events,  
**so that** I can understand why observable behavior emerged without pretending
that I can inspect private LLM chain-of-thought.

## Acceptance criteria

- [ ] A consequential action can be traced backward to exact events, motivating goals/stories, authority decisions, and any recruited model/tool calls.
- [ ] Views visibly distinguish exact raw evidence from derived state, approximate aggregates, inferred relationships, and proposals.
- [ ] A user can select an aggregate influence and drill down until its exact underlying provenance is visible.
- [ ] A timeline and causal graph can show event order/causality without implying that temporal adjacency is causation.
- [ ] 2D, 3D, or higher-dimensional simulation state can be projected for inspection without treating the display coordinates as semantic truth.
- [ ] The UI can retain enough evidence to compare two runs/projections of the same raw history.
- [ ] No feature claims access to hidden model reasoning; model inputs/outputs/tool effects are inspectable only where they are actually retained.

## Tasks within this story

- [ ] Define an inspectable trace/query contract before choosing a rendering library.
- [ ] Build a tiny deterministic fixture that is understandable by inspection.
- [ ] Add aggregate-to-source drill-down and approximation/error overlays.
- [ ] Add a comparison view for exact versus adaptive-resolution runs.

## Non-goals

A giant 3D ball as the only interface, chain-of-thought extraction, or visual
complexity that cannot answer a concrete audit question.
