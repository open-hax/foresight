---
category: "kanban"
labels: "muse, knoxx, harness, 5sp"
type: "story"
story_id: "HAC.03"
points: "5"
title: "HAC.03 — Connect Codex contexts and native task messages to Knoxx"
priority: "P1"
status: "incoming"
uuid: "hac-03-codex-lifecycle"
epic: "harness-actor-continuity"
parent: "harness-actor-continuity"
dependency: ["hac-01-codex-tools", "hac-02-actor-bindings"]
---

# HAC.03 — Connect Codex contexts and native task messages to Knoxx

> Design: [harness actor continuity](../../notes/design/harness-actor-continuity.md)
> Research: [grounding](../../research/harness-actor-continuity.md)
> Target: muse and knoxx

## Context

PR 141 provides the layered-contract plan. This slice connects its selected operating contract to host tools and Knoxx identity.

## Outcome

Connect Codex contexts and native task messages to Knoxx.

## Scope

Probe desktop, CLI and SDK separately; map only verified lifecycle/message capabilities to the actor contract.

## Non-goals

No production cutover, broad credential copying, new board implementation or automatic memory lift.

## Acceptance criteria

- [ ] GIVEN a parent and spawned child WHEN registered THEN parent linkage and separate actor provenance are recorded without shared credentials.
- [ ] GIVEN verified resume and cancellation WHEN processed THEN the documented binding/revocation policy is applied and tested.
- [ ] GIVEN an unavailable native message API WHEN projected THEN tool access remains explicit and message integration is reported unsupported.

## Verification

Implementation supplies named upstream test runners and sanitized end-to-end
evidence for each criterion, including negative cases. Run the affected owner's
existing test suite, then the isolated adapter scenario; missing runtime access
remains an explicit blocker. This planning card claims no executed integration
test or Rheos status transition.

## Risks

Native capability availability and deployed source can differ from checked-in
code; preserve source revision and actual observed capability separately.
