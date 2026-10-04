---
category: "kanban"
labels: ""
type: "story"
story_id: "EXAMPLE.01"
points: "1"
title: "EXAMPLE.01 — Name the user-meaningful outcome"
status: "incoming"
epic: "parent-epic-uuid"
parent: "parent-epic-uuid"
uuid: "stable-story-uuid"
---

# EXAMPLE.01 — Name the user-meaningful outcome

## User story

**As a** <person or actor with the need>  
**I want** <observable capability or outcome>  
**so that** <human purpose / why this matters>.

The `so that` clause is not decoration. If it cannot be stated clearly, the
work is not ready to be decomposed into implementation tasks.

## Why / context

State the problem in words a person outside the implementation can understand.
Separate observed facts from hypotheses and proposals. Link source evidence when
the story depends on an existing implementation or prior decision.

## Observable outcome

Describe what becomes true for the user when the story is complete. Prefer one
coherent vertical outcome over a list of components.

## Acceptance criteria

- [ ] GIVEN <starting state> WHEN <user-meaningful action/event> THEN <observable result>.
- [ ] GIVEN <failure/uncertainty condition> WHEN <it occurs> THEN <safe observable result>.
- [ ] Evidence can identify the exact revision, inputs, and result that satisfy the story.

## Tasks within this story

Implementation tasks are derived from this story and its acceptance criteria.
They may be checked off here or represented as linked execution work, but a
pure implementation step should not replace the user story as the unit of
intent on the board.

- [ ] Write the failing behavior/contract test or other falsifying check first.
- [ ] Implement the smallest change that satisfies the acceptance criteria.
- [ ] Run the repository-owned verification and retain the evidence.

## Non-goals

List tempting adjacent work that this story intentionally does not authorize.

## Evidence / verification

Name the exact tests, experiments, screenshots, traces, receipts, or other
observable evidence that can demonstrate the story without relying on a claim
from the implementer.

## Triage / decisions still needed

Record unresolved priority, urgency, ownership, or product decisions explicitly.
Do not infer urgency from enthusiasm or conversational intensity.
