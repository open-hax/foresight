---
category: "kanban"
labels: "muse, knoxx, harness, 5sp"
type: "story"
story_id: "HAC.05"
points: "5"
title: "HAC.05 — Prove actor and message continuity between local and remote Knoxx"
priority: "P1"
status: "incoming"
uuid: "hac-05-federation"
epic: "harness-actor-continuity"
parent: "harness-actor-continuity"
dependency: ["hac-02-actor-bindings", "hac-03-codex-lifecycle"]
---

# HAC.05 — Prove actor and message continuity between local and remote Knoxx

> Design: [harness actor continuity](../../notes/design/harness-actor-continuity.md)
> Research: [grounding](../../research/harness-actor-continuity.md)
> Target: knoxx, axxium and clio

## Context

PR 141 provides the layered-contract plan. This slice connects its selected operating contract to host tools and Knoxx identity.

## Outcome

Prove actor and message continuity between local and remote Knoxx.

## Scope

Use isolated local and remote instances to qualify actor mappings, addressed messages, replay and recovery. Memory promotion and revocation are HAC.07.

## Non-goals

No production cutover, broad credential copying, new board implementation or automatic memory lift.

## Acceptance criteria

- [ ] GIVEN a local actor and a remote binding WHEN connected THEN both instances validate audience, tenant and current grants independently.
- [ ] GIVEN replay or remote outage WHEN message delivery is attempted THEN duplicate effects are prevented, failed delivery stays pending under the declared retry policy, and recovery preserves causal identity and the original remote (instance-id, actor-id) destination mapping. Any destination change is rejected unless a new mapping and grant are explicitly authorized.
- [ ] GIVEN revoked or expired message authorization WHEN new delivery is attempted THEN destination admission denies it; no cached transport grant bypasses destination checks.

## Verification

Implementation supplies named upstream test runners and sanitized end-to-end
evidence for each criterion, including negative cases. Run the affected owner's
existing test suite, then the isolated adapter scenario; missing runtime access
remains an explicit blocker. This planning card claims no executed integration
test or Rheos status transition.

## Risks

Native capability availability and deployed source can differ from checked-in
code; preserve source revision and actual observed capability separately.

## Payload reference criteria

- [ ] GIVEN an opaque payload reference WHEN a different destination, wrong tenant/audience, expired/revoked grant or stale source revision attempts resolution THEN no payload content is returned; an authorized destination with a current matching grant resolves only the permitted revision.
