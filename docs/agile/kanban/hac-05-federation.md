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

- [ ] GIVEN a local actor and a remote binding WHEN connected THEN both identity providers verify the explicit mapping, and each destination validates audience, tenant, role, capability and current grants independently; display-name or human-account equality grants no authority.
- [ ] GIVEN replay or remote outage WHEN message delivery is attempted THEN retries retain the original producer (instance-id, actor-id), binding identity, message ID and remote (instance-id, actor-id) destination, HAC.02 deduplication runs before side effects, and failed delivery stays pending under the declared retry policy with causal identity preserved. Any destination change is rejected unless a new mapping and grant are explicitly authorized; changing destination creates a separately authorized message rather than retargeting a retry.
- [ ] GIVEN a remote non-idempotent operation commits but its response is lost WHEN delivery retries with the original producer (instance-id, actor-id), binding identity, message ID and destination and passes current delivery and protected-outcome authorization THEN exactly one observable remote effect exists and the authorized recorded outcome is returned without repeating that effect.
- [ ] GIVEN authorization expires or is revoked after the remote effect commits WHEN retry or recorded-outcome retrieval is attempted THEN destination admission denies it without another effect or protected outcome data. A changed producer or binding identity also denies retry; a producer change requires a separately authorized new message.
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

- [ ] GIVEN an opaque payload reference WHEN a different destination, wrong tenant/audience, expired/revoked grant or stale source revision attempts resolution THEN no payload content is returned; an authenticated destination with a current source-read grant, matching tenant and audience, resolves only the exact authorized source revision.


## Inline payload boundary qualification

- [ ] GIVEN a protected inline payload addressed to an unauthorized remote destination WHEN prepared THEN the sender denies before source-byte access or serialization/disclosure, with no payload-bearing transport emission. An explicitly public payload requires the source-policy classification to be recorded.
- [ ] GIVEN a permitted inline payload with source identity/revision and authorization provenance WHEN received THEN the destination independently validates current content-access grants, tenant, audience, exact source revision and its authenticated binding before delivery, effects or protected outcome return. Wrong destination, expired/revoked grants, stale revision and missing provenance are separate denial cases, including retries and recorded outcomes; no copied bytes bypass source revocation.
