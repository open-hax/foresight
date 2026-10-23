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
- [ ] GIVEN a retry or resume WHEN admitted THEN the canonical Knoxx/Clio contract preserves the original source; retries retain the original message ID and destination and deduplicate before side effects. The owning implementation identifies the versioned rule and its atomic admission/effect boundary.
- [ ] GIVEN a non-idempotent operation commits its effect but its response is lost WHEN the same message is retried with its original ID and destination THEN exactly one observable effect exists, including across concurrent retries; deduplication returns the recorded outcome without executing the effect again.

## Verification

Implementation supplies named upstream test runners and sanitized end-to-end
evidence for each criterion, including negative cases. Run the affected owner's
existing test suite, then the isolated adapter scenario; missing runtime access
remains an explicit blocker. This planning card claims no executed integration
test or Rheos status transition.

## Risks

Native capability availability and deployed source can differ from checked-in
code; preserve source revision and actual observed capability separately.

## Additional identity criteria

- [ ] GIVEN two new contexts WHEN bound THEN their (instance-id, actor-id) references differ, including parent and child. Only an authenticated resume of the same context may reuse its binding.
- [ ] GIVEN context A private memory WHEN context B, including its parent or sibling, calls the memory tool THEN retrieval is denied without an explicit sharing grant. Parentage alone grants no private-memory access.

## Admission and compatibility criteria

- [ ] GIVEN an authenticated producer with the wrong role or missing capability WHEN a message is admitted THEN it is denied without a valid event.
- [ ] GIVEN a requested child grant WHEN it adds a capability, broadens tenant/audience/resource scope or extends expiry beyond the parent's grant THEN delegation is rejected. Child rights must be a subset and expiry no later than the parent's.
- [ ] GIVEN a revoked binding, mismatched installation or principal, or resume after revocation WHEN resumed or used THEN admission denies; a new grant/binding requires explicit authorization.
- [ ] GIVEN a message encoded under an unknown major schema or incompatible required fields WHEN admitted THEN it fails with a version error; older compatible schemas use only an explicit versioned migration.

## Payload reference criteria

- [ ] GIVEN an opaque payload reference WHEN a different destination, wrong tenant/audience, expired/revoked grant or stale source revision attempts resolution THEN no payload content is returned; an authenticated destination with a current source-read grant, matching tenant and audience, resolves only the exact authorized source revision.

## Active contract admission criteria

- [ ] GIVEN a binding profile or binding/envelope effective-contract hash that differs from the active contract WHEN use or resume is attempted THEN admission denies it; profile and both hashes must match the active contract.
- [ ] GIVEN a parent grant without explicit delegation permission, or an issuer not authorized by that permission and bound to the authenticated principal and parent grant WHEN a child grant is requested THEN delegation is rejected and no child grant is issued.
