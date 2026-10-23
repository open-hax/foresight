---
category: "kanban"
labels: "muse, knoxx, harness, 5sp"
type: "story"
story_id: "HAC.05"
points: "5"
title: "HAC.05 — Prove actor continuity between local and remote Knoxx"
priority: "P1"
status: "incoming"
uuid: "hac-05-federation"
epic: "harness-actor-continuity"
parent: "harness-actor-continuity"
dependency: ["hac-02-actor-bindings", "hac-03-codex-lifecycle"]
---

# HAC.05 — Prove actor continuity between local and remote Knoxx

> Design: [harness actor continuity](../../notes/design/harness-actor-continuity.md)
> Research: [grounding](../../research/harness-actor-continuity.md)
> Target: knoxx, axxium and clio

## Context

PR 141 provides the layered-contract plan. This slice connects its selected operating contract to host tools and Knoxx identity.

## Outcome

Prove actor continuity between local and remote Knoxx.

## Scope

Use isolated local and remote instances to qualify mappings, message replay and permissioned memory promotion.

## Non-goals

No production cutover, broad credential copying, new board implementation or automatic memory lift.

## Acceptance criteria

- [ ] GIVEN a local actor and a remote binding WHEN connected THEN both instances validate audience, tenant and current grants independently.
- [ ] GIVEN private source memory WHEN a lift is proposed THEN only a separately authorized promotion with provenance may create the shared projection.
- [ ] GIVEN revoked source consent, replay or remote outage WHEN delivery/retrieval is attempted THEN stale disclosure is denied and recovery preserves causal identity.

## Verification

Implementation supplies named upstream test runners and sanitized end-to-end
evidence for each criterion, including negative cases. Run the affected owner's
existing test suite, then the isolated adapter scenario; missing runtime access
remains an explicit blocker. This planning card claims no executed integration
test or Rheos status transition.

## Risks

Native capability availability and deployed source can differ from checked-in
code; preserve source revision and actual observed capability separately.
