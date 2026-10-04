---
category: "kanban"
labels: "cognitive-substrate, archaeology, fork-tales, open-planner, truth, epiphany, 3sp"
type: "story"
story_id: "CS1.01"
points: "3"
title: "CS1.01 — Recover the intended substrate from the prototype lineage"
status: "incoming"
epic: "cognitive-substrate-research-program"
parent: "cognitive-substrate-research-program"
uuid: "recover-cognitive-substrate-intent"
---

# CS1.01 — Recover the intended substrate from the prototype lineage

## User story

**As a** researcher maintaining Foresight,  
**I want** the useful intent and behavior in Fork Tales, OpenPlanner, Truth, and
Epiphany recovered as explicit hypotheses and stories,  
**so that** the next implementation can learn from the prototypes without
inheriting their accidental framing or technical debt.

## Context

Fork Tales is valuable primarily as a reference prototype and feature/story
mine, not as the intended product. OpenPlanner is an earlier attempt whose
limitations are evidence. Truth and Epiphany each contain durable pieces but
currently communicate narrower identities than the intended substrate.

## Acceptance criteria

- [ ] A revision-bound archaeology records observed behavior separately from inferred intent and new proposals.
- [ ] Fork Tales is treated as reference evidence rather than a production dependency by default.
- [ ] OpenPlanner failures/limitations are retained as counterevidence, not erased from the lineage.
- [ ] Truth's currently game-shaped surface and Epiphany's currently archaeology-shaped surface are described as observations, not silently rewritten identities.
- [ ] Every promoted capability maps to at least one user story and one observable experiment or acceptance criterion.
- [ ] Conflicting evidence is retained and called out rather than resolved by narrative preference.

## Tasks within this story

- [ ] Inventory the four repositories at exact revisions and identify the smallest relevant source/doc/test surfaces.
- [ ] Build an observed/inferred/proposed capability matrix.
- [ ] Extract reusable stories and anti-stories from the prototypes.
- [ ] Record ownership boundaries before proposing moves between repositories.

## Non-goals

Copying prototype code wholesale, declaring a final architecture, or treating
historical names as permanent product boundaries.

## Verification

A reviewer can trace every recovered claim to a repository/revision or identify
it clearly as a new hypothesis.
