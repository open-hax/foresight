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
- [ ] GIVEN verified resume and cancellation WHEN processed THEN the documented binding/revocation policy is applied and tested. On every supported adapter route, independent resume tests for mismatched instance-id, mismatched actor-id, expired binding, revoked binding, mismatched binding contract hash and mismatched envelope contract hash each deny before delivery, effects or protected outcome return, using HAC.02 admission.
- [ ] GIVEN an unavailable native message API WHEN projected THEN tool access remains explicit and message integration is reported unsupported; this records discovery but does not satisfy integration completion.
- [ ] GIVEN desktop, CLI and SDK WHEN capability discovery is performed THEN every surface has a versioned supported, unsupported or unavailable disposition with probe evidence, or evidence of the prerequisite preventing a probe; no surface is omitted and unavailable is not treated as supported.
- [ ] GIVEN each desktop, CLI or SDK surface claimed supported in the versioned capability matrix WHEN a parent/child native message and reply are sent through the adapter THEN that route enforces the canonical HAC.02 admission checks before delivery, and the intended Knoxx actors receive them with correlation intact and no duplicate effect on retry. At least one positive supported route is required for story completion.
- [ ] GIVEN a spoofed producer, unauthorized destination or revoked binding WHEN a supported native adapter route attempts delivery THEN HAC.02 admission denies it before delivery, creating no admitted message event or downstream effect; a denial audit record is permitted.

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

## Cancellation criteria

- [ ] GIVEN failed transport followed by committed parent cancellation WHEN retry dispatch runs THEN queued descendant work is terminally cancelled and no new attempt starts. A run already admitted before cancellation is stopped best-effort and its observed effects remain recorded; cancellation never fabricates rollback.

- [ ] GIVEN a native message delivery fails without cancellation WHEN the retry policy runs THEN pending work retains the original producer (instance-id, actor-id), binding identity, message ID and destination, retry follows the declared policy, and HAC.02 deduplication precedes side effects. Losing the response after a non-idempotent effect commits and retrying with current delivery and protected-outcome authorization returns the protected outcome recorded by that first committed effect without executing it again, with exactly one observable effect; expired or revoked authorization denies the retry and protected outcome return without repeating the effect.
