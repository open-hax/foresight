---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["b2f52c1f-bd1b-431e-a417-95c72549ba0d", "59774be2-cc61-4708-9030-8b7b42935ec3", "29caa8e3-289a-416c-9850-8ef9cc769e88"]
parent: "ff5998a4-2cd6-482c-a43e-94522eb908b4"
type: "task"
write-id: "1793726618216-0.b1hhc7x2zxpic1i98vs"
points: "8"
title: "Oikos: Admit sandbox runtime workspaces with leases and deliberate human joining"
priority: "P1"
status: "incoming"
uuid: "912b707f-2275-45c9-b4a9-aac143e11d9a"
created_at: "2026-11-03T15:11:08.077Z"
---

# Oikos: Admit sandbox runtime workspaces with leases and deliberate human joining

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: named collaborative projection spaces with deliberate joining.

## Outcome

Admit sandbox runtime workspaces with leases and deliberate human joining.

## Scope

Implement sandbox-first hidden runtime workspace admission, preconditions/leases and a human join/sidebar acceptance flow.

## Non-goals

No native i3 duplicate-window illusion, hidden-workspace security isolation guarantee, autonomous visible-workspace takeover or live movement.

## Acceptance criteria

- [ ] Numbered/visible workspaces and output focus are protected; movement requires explicit ownership admission for the targeted lifetime/resource, a matching observed revision, and an unexpired scoped lease. Missing/denied ownership, revision mismatch or expired lease denies mutation.
- [ ] Join requires human acceptance; leaving verifies return or records failure; hidden workspace is explicitly not security isolation.

### S3B consent and return

- [ ] Human join consent binds the exact displayed target lifetime, consequences, observed revision and scoped lease. Consume consent on its intended action; cancel/decline or a changed target, consequence, revision or lease invalidates it and prevents replay. Intervening human navigation supersedes an old return-focus intention. Missing or reused return targets yield an explicit return failure, never stale repair or focus takeover.

Future verification: synthetic nested-i3 scenarios decline/cancel with zero IPC effects; accept the exact displayed proposal once, then retry it or change its revision/lease/target and assert denial. Navigate manually after joining and assert leave does not restore obsolete focus. Close/reuse or remove the return target and assert visible failure without a compensating command against the new lifetime.

## Verification

Nested i3 sandbox tests concurrent actors, denied/missing ownership admission, stale revisions, expired leases, external moves and protected focus, each expecting zero denied IPC mutation. Accepted human join yields an observed membership/join result; declined join performs no movement; failed return remains explicit failure. No live desktop target. These are future acceptance checks, not executed implementation evidence.

## Risks

Hidden workspaces are organizational, not isolation; human or external i3 changes may invalidate a return plan.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/docs/notes/design/epic-task-centric.md
- /home/err/docs/notes/design/epic-layout-hygiene.md
- /home/err/docs/notes/design/epic-agent-sandbox.md

## Dependencies

- b2f52c1f-bd1b-431e-a417-95c72549ba0d (S3A)
- 59774be2-cc61-4708-9030-8b7b42935ec3 (S2B)
- 29caa8e3-289a-416c-9850-8ef9cc769e88 (S4A): scoped action/preview contract used by movement admission.

---
Native finding 4456856439: movement requires explicit ownership admission, a matching observed revision and an unexpired scoped lease. Negative fixtures assert zero denied mutations; accepted human join yields observed membership, declined join does not move and failed return stays failure. S4A supplies the named scoped-action contract in addition to S3A branches and S2B currentness. Current native child prerequisites synchronized to the user-approved precise plan: b2f52c1f-bd1b-431e-a417-95c72549ba0d (S3A), 59774be2-cc61-4708-9030-8b7b42935ec3 (S2B), 29caa8e3-289a-416c-9850-8ef9cc769e88 (S4A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Consensus planning correction v1: root incoming acceptance/verification now explicitly specifies s3b/consent-and-return. See the corresponding named root subsections for the contract and concrete future positive/negative fixtures. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

---