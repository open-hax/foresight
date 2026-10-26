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

HAC.02 specifies the portable contract and admission laws with the active
capability profile and effective-contract hash as explicit authoritative inputs.
The layered hash remains the LOC.04/05/08 contract consumed with the LOC.06
chain and LOC.07 profile; this card defines no competing canonicalization, fold
or hash algorithm. Portable law tests may use explicitly synthetic contract
inputs, which establish neither a deployed active contract nor layered integration.
All admission criteria remain required. Integrated active-contract qualification,
including HAC.03 use/resume scenarios, requires the implemented LOC producer and
canonical contract/projection evidence qualified by HAC.06. That qualification
remains blocked until those inputs exist; matching fixture hashes or copied
manifest hashes cannot substitute for it.

## Non-goals

No production cutover, broad credential copying, new board implementation or automatic memory lift.

## Acceptance criteria

- [ ] GIVEN two contexts with the same human/model WHEN bound THEN they have distinct context provenance and bounded grants.
- [ ] GIVEN a spoofed producer, expired grant, independently revoked message grant or wrong tenant/audience WHEN a message is admitted THEN it is denied without creating a valid event. Test expiry and revocation independently.
- [ ] GIVEN a retry or resume WHEN admitted THEN the canonical Knoxx/Clio contract preserves the original producer (instance-id, actor-id) and binding identity; retrying a message preserves those identities, its message ID and destination and deduplicates before side effects. A changed producer or binding is rejected as a retry and requires an explicitly authorized new message. The owning implementation identifies the versioned rule and its atomic admission/effect boundary.
- [ ] GIVEN a non-idempotent operation commits its effect but its response is lost WHEN the same message is retried with its original producer (instance-id, actor-id), binding identity, message ID and destination and passes current authorization for delivery and protected outcome access THEN exactly one observable effect exists, including across concurrent retries; deduplication returns the authorized recorded outcome without executing the effect again.
- [ ] GIVEN authorization expires or is revoked after an effect commits but before retry WHEN delivery or recorded-outcome retrieval is attempted THEN current admission denies it without a second effect or protected outcome data. A recorded outcome never bypasses current authorization.
- [ ] GIVEN the original message ID and destination with a changed producer instance, producer actor or binding identity WHEN retried THEN admission denies the retry without effects or protected outcome data, even if the substituted producer has an otherwise valid grant.

- [ ] GIVEN a non-idempotent effect committed before a crash and lost response WHEN recovery retries the same message with original producer, binding, message ID and destination and current delivery/protected-outcome authorization THEN the durable deduplication record and protected outcome recover with the effect under the versioned HAC.02 atomic rule or an equivalent recoverable protocol; retry returns the first recorded outcome without repeating the effect. A crash must never expose a committed effect with a missing recovery/deduplication outcome.

## Verification

Portable contract work supplies named upstream law/schema test runners with
explicit fixture provenance. Integrated qualification follows the LOC/HAC.06
evidence boundary in Scope; portable test success alone is insufficient.
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
- [ ] GIVEN an authenticated context with either an instance-id mismatch or an actor-id mismatch against its stored binding WHEN use or resume is attempted THEN each mismatch independently denies admission before delivery, effects or protected outcome return.
- [ ] GIVEN context A private memory WHEN context B, including its parent or sibling, calls the memory tool THEN retrieval is denied without an explicit sharing grant. Parentage alone grants no private-memory access.

## Admission and compatibility criteria

- [ ] GIVEN a binding for repository A WHEN use or resume targets repository B THEN admission denies before delivery, effects or protected outcome return. An explicit operator-scope binding independently denies requests outside that scope on use and resume; cwd never establishes scope.
- [ ] GIVEN a delegated child grant WHEN any ancestor grant is revoked before first use or before resume THEN full-chain validation denies before delivery, effects or protected outcome return; an apparently valid unexpired child cannot bypass ancestor revocation.

- [ ] GIVEN an authenticated receiver whose instance-id or actor-id differs from the envelope destination WHEN mailbox admission is attempted THEN each mismatch independently denies before admission, delivery, effects or protected outcome return.

- [ ] GIVEN an authenticated producer with the wrong role or missing capability WHEN a message is admitted THEN it is denied without a valid event.
- [ ] GIVEN a requested child grant WHEN it adds a capability, broadens tenant/audience/resource scope or extends expiry beyond the parent's grant THEN delegation is rejected. Child rights must be a subset and expiry no later than the parent's.
- [ ] GIVEN a revoked binding, mismatched installation or principal, or resume after revocation WHEN resumed or used THEN admission denies; a new grant/binding requires explicit authorization.
- [ ] GIVEN a canonical binding or message WHEN encoded and decoded through transport JSON THEN it validates against the Knoxx-owned, Katamorph-defined versioned tagged EDN schema with namespaced keys and an integer major version, and the decoded canonical value equals the original without loss of tags, key namespaces, identity or payload values. Raw host objects presented as canonical contract data are rejected; only the explicit adapter codec may convert them at the boundary.
- [ ] GIVEN a message encoded under an unknown major schema or incompatible required fields WHEN admitted THEN it fails with a version error; older compatible schemas use only an explicit versioned migration.

## Payload reference criteria

- [ ] GIVEN an opaque payload reference WHEN a different destination, wrong tenant/audience, expired/revoked grant or stale source revision attempts resolution THEN no payload content is returned; an authenticated destination with a current source-read grant authorizing the referenced source identity and resource scope, matching tenant and audience, resolves only the exact authorized source revision. A grant scoped to a different source identity or resource is independently denied before returning content, even with matching tenant, audience and revision.

## Active contract admission criteria

- [ ] GIVEN a binding profile or binding/envelope effective-contract hash that differs from the active contract WHEN use or resume is attempted THEN admission denies it; profile and both hashes must match the active contract.
- [ ] GIVEN a parent grant without explicit delegation permission, or an issuer not authorized by that permission and bound to the authenticated principal and parent grant WHEN a child grant is requested THEN delegation is rejected and no child grant is issued. Independently test a recipient principal, installation identity or harness family outside the parent permission's explicit allowed recipient tuple set; each denies issuance even with a valid issuer and capability subset.


## Context identity and inline payload criteria

- [ ] GIVEN a binding lookup or equality check WHEN first use or resume is admitted THEN context identity is the same canonical immutable tuple: instance id, actor id, authenticated principal, harness family, installation id, tagged repository/operator-scope identity, native session/task id and authenticated parent-context reference (explicit nil for a root). The actor pair indexes the stored binding; it never substitutes for validating the full tuple. Capability profile and effective-contract hash are separately validated binding policy, and parent provenance grants no authority.
- [ ] GIVEN an otherwise valid actor pair and grant WHEN the authenticated harness, native session/task, principal, installation, scope or parent reference differs from the stored tuple THEN each mismatch independently denies first use and resume before delivery, effects or protected outcome return.
- [ ] GIVEN a private inline payload WHEN prepared for transport THEN the sender validates current source-read authority and the intended authenticated destination's current content-access grant authorizing the envelope's exact source identity and resource scope, with matching tenant, audience and exact source revision, before reading protected bytes or serializing/disclosing them. The envelope binds source identity/revision and authorization provenance; missing provenance or unauthorized destination emits no payload-bearing transport message.
- [ ] GIVEN an inline payload with expired/revoked grants, wrong destination/tenant/audience or stale source revision WHEN destination admission, retry or recorded-outcome retrieval occurs THEN current content authorization denies before payload delivery, effects or protected outcome return. Each negative is tested independently; source revocation cannot be bypassed by copied bytes. Public inline data requires explicit source-policy classification.
