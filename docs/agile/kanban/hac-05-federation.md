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

- [ ] GIVEN a local actor and a remote binding WHEN connected THEN both identity providers verify the explicit mapping, and each destination validates audience, tenant, role, capability and current grants independently; display-name or human-account equality grants no authority. The authenticated receiver destination instance-id and actor-id must each match the envelope. Independent instance-mismatch and actor-mismatch tests deny before mailbox admission, delivery, effects or protected-outcome return, even with otherwise valid mapping and grants.
- [ ] GIVEN replay or remote outage WHEN message delivery is attempted THEN retries retain the original producer (instance-id, actor-id), binding identity, message ID and remote (instance-id, actor-id) destination, HAC.02 deduplication runs before side effects, and failed delivery stays pending under the declared retry policy with causal identity preserved. Any destination change is rejected unless a new mapping and grant are explicitly authorized; changing destination creates a separately authorized message rather than retargeting a retry.
- [ ] GIVEN a remote non-idempotent operation commits but its response is lost WHEN delivery retries with the original producer (instance-id, actor-id), binding identity, message ID and destination and passes current delivery and protected-outcome authorization THEN exactly one observable remote effect exists and the authorized recorded outcome is returned without repeating that effect. Independently crash the remote process after effect commit and before response delivery, restart it, and retry with that same original delivery identity and current authorization: the durable deduplication record and protected outcome recover with the effect under the versioned HAC.02 atomic rule or an equivalent versioned recoverable protocol identified in the receipt; return the first recorded outcome without another effect.
- [ ] GIVEN authorization expires or is revoked after the remote effect commits WHEN retry or recorded-outcome retrieval is attempted THEN destination admission denies it without another effect or protected outcome data. A changed producer or binding identity also denies retry; a change to either producer identity or binding identity requires a separately authorized new message.
- [ ] GIVEN revoked or expired message authorization WHEN new delivery is attempted THEN destination admission denies it; no cached transport grant bypasses destination checks.

- [ ] Record each remote destination’s complete HAC.02 immutable context tuple. Independently mismatch authenticated principal, harness family, installation, tagged repository/operator scope, native session/task and parent context; each denies before mailbox admission, delivery, effects or protected outcome return, retaining the existing independent mapping, grant, instance-id and actor-id checks.
- [ ] Independently test source-read grant expiry, source-read grant revocation, destination content-access grant expiry and destination content-access grant revocation. Each denies before delivery, effects, retries and protected-outcome retrieval; retain exact-source/resource scope checks and all other independent denial cases.

- [ ] Independently validate each remote destination’s capability profile, binding effective-contract hash and envelope effective-contract hash against the selected active contract. Each mismatch independently denies before mailbox admission, delivery, effects or protected outcome return, preserving all existing tuple, mapping, grant, instance-id and actor-id checks.

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

- [ ] GIVEN an opaque payload reference WHEN a different destination, wrong tenant/audience, expired/revoked grant or stale source revision attempts resolution THEN no payload content is returned; an authenticated destination with a current source-read grant authorizing the referenced source identity and resource scope, matching tenant and audience, resolves only the exact authorized source revision. A grant scoped to a different source identity or resource is independently denied before returning content, even with matching tenant, audience and revision.


## Inline payload boundary qualification

- [ ] GIVEN a protected inline payload WHEN prepared THEN the sender validates current source-read authority for the exact source identity and resource scope, the intended authenticated destination's current content-access grant authorizing the envelope's exact source identity and resource scope, matching tenant/audience and exact source revision before accessing or serializing/disclosing protected bytes. The envelope binds source identity, revision and authorization provenance; each invalid source authority, destination grant, tenant, audience or revision independently denies with no source-byte access or payload-bearing transport emission. An explicitly public payload requires the source-policy classification to be recorded.
- [ ] GIVEN a permitted inline payload with source identity/revision and authorization provenance WHEN received THEN the destination independently validates both the current source-read grant and its current content-access grant for the envelope's exact source identity and resource scope, plus tenant, audience and exact source revision and its authenticated binding before delivery, effects or protected outcome return. Wrong destination, wrong tenant, wrong audience, expired/revoked grants, stale revision and missing provenance are separate independent denial cases, each before payload delivery, effects or protected-outcome return, including retries and recorded outcomes; Expired or revoked source-read grants are independent denial cases at delivery, effects, retries and protected-outcome retrieval; no copied bytes bypass source revocation.
