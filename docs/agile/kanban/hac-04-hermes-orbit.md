---
category: "kanban"
labels: "muse, knoxx, harness, 3sp"
type: "story"
story_id: "HAC.04"
points: "3"
title: "HAC.04 — Qualify Hermes and Orbit as another contract consumer"
priority: "P1"
status: "incoming"
uuid: "hac-04-hermes-orbit"
epic: "harness-actor-continuity"
parent: "harness-actor-continuity"
dependency: ["hac-02-actor-bindings"]
---

# HAC.04 — Qualify Hermes and Orbit as another contract consumer

> Design: [harness actor continuity](../../notes/design/harness-actor-continuity.md)
> Research: [grounding](../../research/harness-actor-continuity.md)
> Target: muse and Orbit integration owner

## Context

PR 141 provides the layered-contract plan. This slice connects its selected operating contract to host tools and Knoxx identity.

## Outcome

Qualify Hermes and Orbit as another contract consumer.

## Scope

Record actual runtime, source and health; reuse Orbit Hermes plugin boundaries and specify an adapter with explicit capability gaps.

## Non-goals

No production cutover, broad credential copying, new board implementation or automatic memory lift.

## Acceptance criteria

- [ ] GIVEN the running deployment or its absence WHEN probed THEN endpoint, source_revision, deployed_runtime_revision, health and missing prerequisites are recorded separately without secrets. deployed_runtime_revision is explicitly unknown if no deployment is available or its revision cannot be verified; checkout source_revision never substitutes for runtime evidence.
- [ ] GIVEN a Hermes context WHEN a test adapter is qualified THEN tool and actor/message capabilities have independent evidence, and every unsupported capability is recorded as unsupported in the adapter manifest with a negative test.
- [ ] GIVEN a selected contract WHEN a Hermes/Orbit test adapter is qualified THEN its evidence identifies the consumed contract and profile, shows actor binding and message admission through HAC.02, and demonstrates supported message delivery plus wrong-context and unauthorized-message denial. Discovery or health evidence alone cannot satisfy consumer qualification; unsupported capabilities retain explicit dispositions and negative tests.
- [ ] GIVEN an isolated Orbit workspace WHEN a mutation is requested with a sufficient current grant and current workspace revision THEN the authorized mutation succeeds with the expected state change. Independently test an insufficient grant and a stale revision: each denies the mutation and leaves workspace content and revision unchanged, verified by before/after snapshots; a separate denial audit record is permitted.

## Verification

Implementation supplies named upstream test runners and sanitized end-to-end
evidence for each criterion, including negative cases. Run the affected owner's
existing test suite, then the isolated adapter scenario; missing runtime access
remains an explicit blocker. This planning card claims no executed integration
test or Rheos status transition.

## Risks

Native capability availability and deployed source can differ from checked-in
code; preserve source revision and actual observed capability separately.
