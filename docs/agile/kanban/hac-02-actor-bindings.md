---
category: "kanban"
labels: "muse, knoxx, harness, 5sp"
type: "story"
story_id: "HAC.02"
points: "5"
title: "HAC.02 — Specify portable harness actor bindings and message admission"
priority: "P1"
status: "incoming"
uuid: "hac-02-actor-bindings"
epic: "harness-actor-continuity"
parent: "harness-actor-continuity"
---

# HAC.02 — Specify portable harness actor bindings and message admission

> Design: [harness actor continuity](../../notes/design/harness-actor-continuity.md)
> Research: [grounding](../../research/harness-actor-continuity.md)
> Target: knoxx, axxium, clio, katamorph

## Context

PR 141 provides the layered-contract plan. This slice connects its selected operating contract to host tools and Knoxx identity.

## Outcome

Specify portable harness actor bindings and message admission.

## Scope

Reuse actor/mailbox/event authority and specify identity, delegation, correlation, deduplication and revocation contracts before host adapters.

## Non-goals

No production cutover, broad credential copying, new board implementation or automatic memory lift.

## Acceptance criteria

- [ ] GIVEN two contexts with the same human/model WHEN bound THEN they have distinct context provenance and bounded grants.
- [ ] GIVEN a spoofed producer, expired grant or wrong tenant/audience WHEN a message is admitted THEN it is denied without creating a valid event.
- [ ] GIVEN a retry or resume WHEN admitted THEN existing Knoxx/Clio identity rules prevent duplicate effects and preserve the original source.

## Verification

Implementation supplies named upstream test runners and sanitized end-to-end
evidence for each criterion, including negative cases. Run the affected owner's
existing test suite, then the isolated adapter scenario; missing runtime access
remains an explicit blocker. This planning card claims no executed integration
test or Rheos status transition.

## Risks

Native capability availability and deployed source can differ from checked-in
code; preserve source revision and actual observed capability separately.
