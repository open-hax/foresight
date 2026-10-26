---
category: "kanban"
labels: "muse, knoxx, harness, 8sp"
story_id: "HAC.03"
dependency: ["hac-01-codex-tools", "hac-02-actor-bindings", "hac-06-layered-tool-projection"]
parent: "harness-actor-continuity"
type: "story"
write-id: "1792907515091-0.tlgspcc2pci5juffyvr"
points: "8"
title: "HAC.03 — Connect Codex contexts and native task messages to Knoxx"
priority: "P1"
status: "incoming"
epic: "harness-actor-continuity"
uuid: "hac-03-codex-lifecycle"
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

The revised planning estimate is 8 points, with all existing scope retained.
This card remains incoming pending a bounded qualification breakdown before
implementation or a ready transition. The parent records the estimate, label
and dependency changes through Rheos; this prose does not perform those changes.
The breakdown must assign every criterion below to one of two separately
reviewable qualification slices, with named owner test runners, prerequisites,
versioned environment requirements and evidence required for completion:

- Capability discovery and binding qualification: cover desktop, CLI and SDK
  dispositions, parent/child delegation, canonical context identity, use/resume,
  private-memory isolation and active-contract admission. Prerequisites are
  HAC.01 tool qualification, HAC.02 contracts and the LOC/HAC.06 evidence below;
  identify isolated harness/Knoxx versions, authenticated test principals,
  profiles and grants, and evidence for any surface that cannot be probed.
- Native message, retry and cancellation qualification: consume the qualified
  capability/binding slice and HAC.02 admission/replay contracts; identify at
  least one supported native route, isolated actors/mailboxes, a controlled
  non-idempotent effect and durable outcome store, and facilities for concurrent
  delivery, lost responses, crash/recovery and cancellation fault injection.
  Qualify every supported route and preserve all denial and recovery cases.

Neither slice alone completes HAC.03. Missing prerequisites or environments
remain explicit blockers; unsupported/unavailable dispositions do not replace
the required positive route or waive any criterion. The breakdown must preserve
all scope and establish an evidence mapping for both slices before readiness.

## Non-goals

No production cutover, broad credential copying, new board implementation or automatic memory lift.

## Acceptance criteria

- [ ] GIVEN a parent and spawned child WHEN registered THEN an issuer authorized by the parent grant's explicit delegation permission and bound to the authenticated principal and parent grant must pass HAC.02 delegation admission before registration; its receipt binds the authorized recipient principal, installation identity and harness family. Missing delegation permission, unauthorized issuer or unauthorized recipient independently denies before creating any child binding or capability grant. On admitted registration, parent linkage and separate actor provenance are recorded without shared credentials; the child can use only its explicitly delegated capability subset, requests outside that subset are denied before admission or effects, and parentage alone grants no authority.
- [ ] GIVEN verified resume and cancellation WHEN processed THEN the documented binding/revocation policy is applied and tested. On every supported adapter route, at least one authenticated resume with the unchanged canonical context tuple and a valid current grant must succeed, reuse the stored binding and create no new context. On every supported adapter route, independent resume tests for mismatched instance-id, mismatched actor-id, expired binding, revoked binding, mismatched binding contract hash and mismatched envelope contract hash each deny before delivery, effects or protected outcome return, using HAC.02 admission.
- [ ] GIVEN an unavailable native message API WHEN projected THEN tool access remains explicit and message integration is reported unsupported; this records discovery but does not satisfy integration completion.
- [ ] GIVEN desktop, CLI and SDK WHEN capability discovery is performed THEN every surface has a versioned supported, unsupported or unavailable disposition with probe evidence bound to the tested adapter/source revision and loaded artifact hash, or revision-bound evidence of the prerequisite preventing a probe; no surface is omitted and unavailable is not treated as supported. Each positive route records and validates those same adapter/source revision and loaded artifact hash values against the actual loaded adapter; any mismatch invalidates the disposition and qualification evidence.
- [ ] GIVEN each desktop, CLI or SDK surface claimed supported in the versioned capability matrix WHEN a parent/child native message and reply are sent through the adapter THEN that route enforces the canonical HAC.02 admission checks before delivery, and the intended Knoxx actors receive them with correlation intact and no duplicate effect on retry. In both request and reply directions, authenticated producer, destination, binding identity and canonical context must match the authorized parent or child expected for that direction. Independently test each mismatched reply field; deny before delivery, effects or protected-outcome return even with valid correlation. At least one positive supported route is required for story completion. That actual active-contract route pins the HAC.06 qualification receipt and its canonical LOC input revisions, profile and effective-contract hash, and demonstrates admission against that active contract; synthetic fixture inputs or copied manifest hashes cannot qualify it.
- [ ] GIVEN a spoofed producer, unauthorized destination or revoked binding WHEN a supported native adapter route attempts delivery THEN HAC.02 admission denies it before delivery or protected-outcome return, creating no admitted message event or downstream effect; a denial audit record is permitted. On every supported route, a destination instance-id mismatch and a destination actor-id mismatch are tested independently against the authenticated receiver; each denies before mailbox admission, with no admitted-message event, delivery, downstream effect or protected outcome return.

- [ ] GIVEN a non-idempotent effect committed before a crash and lost response WHEN recovery retries the same message with original producer, binding, message ID and destination and current delivery/protected-outcome authorization THEN the durable deduplication record and protected outcome recover with the effect under the versioned HAC.02 atomic rule or an equivalent versioned recoverable protocol; the qualification receipt identifies the exact versioned HAC.02 rule or equivalent protocol exercised. Retry returns the first recorded outcome without repeating the effect. A crash must never expose a committed effect with a missing recovery/deduplication outcome.

- [ ] On every supported request/reply route, independently test unknown, stale and mismatched in-flight correlation IDs; each denies before delivery, effects or protected outcome return even with otherwise valid context and grants.
- [ ] Native retry preserves the HAC.02 payload identity as well as producer, binding, message ID and destination. Independently substitute payload identity and deny before delivery, effects or protected outcome return; record the exercised replay rule version.
- [ ] Parent cancellation additionally requires a distinct cancellation capability or explicit operator scope authorizing cancellation. A context holding only use, read or retry authority independently denies before cancellation commit or any queued descendant work changes, even with authenticated parent identity and a valid current binding.

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
- [ ] GIVEN context A private memory WHEN context B, including its parent or sibling, calls the memory tool THEN retrieval is denied without an explicit sharing grant. Parentage alone grants no private-memory access. On each supported native route, positively verify that the owner reads its own private memory and that another context with an explicit valid sharing grant reads only the granted scope; record these alongside the independent unshared-context denial.

## Canonical context identity on use and resume

- [ ] GIVEN a supported adapter route WHEN first use or resume is admitted THEN it authenticates and compares the full canonical immutable context tuple defined by HAC.02 and the design: instance id, actor id, authenticated principal reference, harness family, installation id, tagged repository/operator-scope identity, native session/task id and authenticated parent-context reference (explicit nil for a root). The actor pair indexes the stored binding but cannot replace full-tuple equality. Capability profile and effective-contract hash remain separately authorized and validated binding policy; parent provenance grants no authority.
- [ ] GIVEN an otherwise valid actor pair and grant WHEN the authenticated principal, harness family, installation id, tagged repository/operator scope, native session/task id or parent-context reference differs from the stored tuple THEN each field mismatch is tested independently on first use and resume and denies before mailbox admission, delivery, effects or protected outcome return, with no admitted-message event. Existing independent instance-id and actor-id denials remain required on use and resume.

- [ ] GIVEN the original message ID and destination with otherwise valid current grants WHEN a native retry substitutes the producer instance-id, producer actor-id or binding identity THEN each mismatch independently denies admission under the versioned HAC.02 replay rule, with no delivery, effect or protected-outcome return. The qualification receipt identifies the exercised rule version.

## Cancellation criteria

- [ ] GIVEN a cancellation request on a supported route WHEN parent cancellation is admitted THEN HAC.02 authenticates the requesting context against the authorized parent and validates its current binding before committing cancellation. Independently test a mismatched parent context and a revoked binding: each denies without committing parent cancellation or changing queued descendant work. Existing committed-cancellation retry-dispatch behavior applies only after authorized admission.

- [ ] GIVEN failed transport followed by committed parent cancellation WHEN retry dispatch runs THEN queued descendant work is terminally cancelled and no new attempt starts. A run already admitted before cancellation is stopped best-effort and its observed effects remain recorded; cancellation never fabricates rollback.

- [ ] GIVEN a native message delivery fails without cancellation WHEN the retry policy runs THEN pending work retains the original producer (instance-id, actor-id), binding identity, message ID and destination, retry follows the declared policy, and the qualification receipt names the versioned HAC.02 atomic replay rule it exercises. Overlapping duplicate deliveries must demonstrate that deduplication admission and the non-idempotent effect form that rule’s atomic boundary, with exactly one committed effect; sequential check-then-effect evidence alone is insufficient. Losing the response after a non-idempotent effect commits and retrying with current delivery and protected-outcome authorization returns the protected outcome recorded by that first committed effect without executing it again, with exactly one observable effect; expired or revoked authorization denies the retry and protected outcome return without repeating the effect.

---
Planning review5641753204: revised estimate to8, retained incoming, and added HAC06 prerequisite for actual active-contract evidence. All existing lifecycle obligations remain. Capability/binding qualification and message/retry/cancellation qualification must be broken into bounded independently reviewable slices with named prerequisites/environments before implementation or ready admission; no runtime qualification is claimed.

---