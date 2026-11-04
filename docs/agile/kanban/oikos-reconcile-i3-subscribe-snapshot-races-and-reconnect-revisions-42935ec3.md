---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["93a76dad-678a-4980-86fb-ba0fbb5e8203"]
parent: "3b9279b5-aafd-431a-90a9-128ce4415de0"
type: "task"
write-id: "1793796219192-0.fnlfibm3xg9wjtjv2sr"
points: "8"
title: "Oikos: Reconcile i3 subscribe snapshot races and reconnect revisions"
priority: "P1"
status: "in_progress"
uuid: "59774be2-cc61-4708-9030-8b7b42935ec3"
created_at: "2026-11-03T15:11:07.122Z"
---

# Oikos: Reconcile i3 subscribe snapshot races and reconnect revisions

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: Clio desktop journal and causal EDN projections.

## Outcome

Reconcile i3 subscribe snapshot races and reconnect revisions.

## Scope

Implement outer i3 collector using subscribe-before-snapshot, race reconciliation and explicit disconnect/resync state.

## Non-goals

No parallel journal kernel, invented complete deltas, guaranteed reopening of closed applications, or live collector activation.

## Acceptance criteria

- [ ] IPC events are not treated as complete tree deltas; events during snapshot cause reconciliation with an explicit observed revision.
- [ ] Disconnect/epoch change invalidates currentness and stale targets; resync yields admitted observations rather than invented continuity. Same-epoch XID close/reuse creates a new open/incarnation identity. If a reconnect gap cannot establish lifetime continuity, invalidate the old lifetime and fail closed for targeting; never infer continuity from equal XIDs or content.

### S2B snapshot cut

- [ ] A baseline requires successful subscription acknowledgement plus a justified event-stream-fenced snapshot cut. Specify and verify a nonce-matched barrier (or an owner-supported equivalent) observed on the subscribed event stream around the snapshot; a command reply, initial tick or callback arrival order alone is not a cut. Events generated before/within the candidate cut but delivered late invalidate its clean-state claim. An unsupported barrier/ordering guarantee, subscription rejection, timeout or continual change leaves the collector uncertain, not silently current.

Future verification: a scheduled fake IPC harness delays/rejects subscription acknowledgement, delivers an earlier-generated event after the snapshot callback, substitutes initial/unmatched ticks and command replies, and withholds the event-stream nonce. Assert no qualified baseline until the protocol's ordering justification holds; bounded timeout and continually changing trees remain uncertain. Retain the schedule, nonce/cut evidence and admission result, without claiming that an unverified i3 API supplies atomic snapshots.

### S2B freshness vs history

- [ ] Freshness at an established observation cut and historical completeness are independent fields. An open connection does not establish either: local queue overflow, structural notifications or coverage loss trigger refresh/uncertainty. A refresh observes present state but cannot recover missing intermediate events. Stale cuts, rejected admission and an offline replay baseline cannot claim live currentness.

Future verification: in an actual nested synthetic i3 fixture, exercise hierarchy, floating windows, move and resize, plus open-connection queue/coverage gaps. Compare the admitted projection with an independently normalized GET_TREE oracle at a controlled, justified cut; retain the operation schedule, runtime/window identities, cut proof and both normalized trees. Assert separately freshness-at-cut and historical-gap retention; reject stale/rejected baselines and show an offline replay never supplies live-current evidence. Fake IPC tests alone do not replace this later sandbox integration proof.

## Verification

Fake IPC harness interleaves subscription/snapshot/events, drops connections and reuses XIDs both within one epoch and across epochs; uncertain reconnect denies old-lifetime targets and memberships cannot migrate to the new incarnation. Sandbox integration verifies projection against a fresh tree. These are future acceptance checks, not executed implementation evidence.

## Risks

Snapshot/event races and reused identities can turn a historical observation into a destructive command target.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-workspace-go.md
- /home/err/docs/notes/design/epic-i3-mcp.md

## Dependencies

- 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A)

---
Propagates native lifetime findings 4456846184/4456856398 into subscribe/snapshot/reconnect acceptance: same-epoch close/reuse creates a new incarnation; a reconnect gap cannot establish continuity merely from equal XIDs. Deny stale/uncertain targets and retain historical memberships without reassignment. Current native child prerequisites synchronized to the user-approved precise plan: 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Consensus planning correction v1: root incoming acceptance/verification now explicitly specifies s2b/snapshot-cut, s2b/freshness-vs-history. See the corresponding named root subsections for the contract and concrete future positive/negative fixtures. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

Source-phase admission only: permit .ημ/runs/oikos-program-20261103/permits/s2b-offline-collector-source-v1.edn; independently approved plan v3 .ημ/runs/oikos-program-20261103/reviews/s2b-collector-source-plan-fix-review-v3.edn and composition .ημ/runs/oikos-program-20261103/reviews/s2b-accepted-base-composition-v1.edn (sha256 f75be6e4c45e26eb1ef3069e3c5e18fb5d4f5be99991477444cdf44c3ac4d172; 63 paths, 13 frozen pins). Accepted base fed4e30272cc1186de14bcc708b3efc9736e3139 tree 429d9dde803b6d12ee07f692872a1b0dcba31ad1. Eight additions only as enumerated in permit; no base modifications or deletions. P1 is fake inert deterministic data-only scheduling, no timers, listeners, native i3, private fixture adapter, actual append or operational currentness. Preserve frozen event validation and replay. Follow meaningful red, portable law and inert edge green, exact-head independent code review, then parent component acceptance. Whole S2B, actual nested i3 oracle, native proof, real head resolution, operational currentness and E2 remain held; no task completion or native collector grant.

---