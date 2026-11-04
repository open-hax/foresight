---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["07452a65-4a6a-4704-bf81-50408927dcc8"]
parent: "3b9279b5-aafd-431a-90a9-128ce4415de0"
type: "task"
write-id: "1793760281470-0.a0b0mddte3pzgrh6or2"
points: "5"
title: "Oikos: Admit desktop causal events through Clio and replay EDN projections"
priority: "P1"
status: "done"
uuid: "93a76dad-678a-4980-86fb-ba0fbb5e8203"
created_at: "2026-11-03T15:11:06.816Z"
---

# Oikos: Admit desktop causal events through Clio and replay EDN projections

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: Clio desktop journal and causal EDN projections.

## Outcome

Admit desktop causal events through Clio and replay EDN projections.

## Scope

Reuse Clio admission/schema/locking and canonical causal streams; implement pure CLJC desktop replay.

## Non-goals

No parallel journal kernel, invented complete deltas, guaranteed reopening of closed applications, or live collector activation.

## Acceptance criteria

- [ ] Observation, intention and outcome have distinct portable .cljc shapes and causal links; observed revision and window lifetime identity (server epoch, XID, open/incarnation identity or creation revision) are mandatory. Epoch plus XID alone is insufficient; uncertain reconnect continuity invalidates targeting.
- [ ] Replay is deterministic; stale identity and conflicting admission fail; undo is compensation and cannot guarantee reopening apps.

### S2A coverage gap

- [ ] Pure .cljc replay distinguishes observation currentness/coverage from validity of persisted Clio history. An admitted coverage-gap event makes the projection non-current; an admitted reconciled-baseline event may establish fresh state only for its justified observation cut and leaves missing intermediate observations explicitly unknown. Neither event bypasses Clio admission/canonicalization, manufactures facts or repairs absent persisted events/causes.

Future verification: replay baseline → coverage-gap → reconciled baseline and assert current → uncertain → fresh-at-cut with an explicit historical gap. Shuffled ledger partitions and exact duplicate copies yield the same lawful result; missing persisted sequence entries or causal parents still fail canonicalization rather than becoming acceptable coverage gaps. Rejected reconciliation cannot make a projection current.

### S2A safe-reference contract and handoff

- [ ] S2A owns definition and independent review of the initial versioned, closed immutable-metadata allowlist and deletable content-reference contract as portable .cljc boundary laws before admitting desktop observation/intention/outcome events through Clio. Raw captures/OCR, secret-bearing window titles/paths/labels and echoed rejection payloads cannot enter immutable events or diagnostics; hashes/IDs require sensitivity/linkability classification, not an anonymity assumption. Clio remains admission/ledger authority; this is not a second privacy engine.
- [ ] Freeze the exact contract artifact version/content digest, immutable source revision and independent review evidence as the S2A handoff. S2A admission and S5A consumption require that exact reviewed artifact; missing/mismatched/unreviewed evidence denies admission. No S5B artifact or later knowledge implementation is a prerequisite. S5B subsequently consumes this baseline through S5A and may review/compatibly extend it prospectively, never rewrite prior ledger entries or retroactively qualify earlier admissions.

Estimate: the existing 5-point S2A estimate explicitly includes this bounded initial contract definition, synthetic boundary fixtures, independent review and versioned handoff alongside Clio admission/replay. It excludes S5B knowledge/retention implementation and any general privacy engine. This allocates future work, not implementation or review-completion evidence.

Future verification: synthetic secret markers in every prohibited field and denial path never enter retained events/diagnostics; after referenced content erasure, valid causal replay retains safe provenance but cannot reconstruct the marker. Deny unknown fields, missing/mismatched contract identity and missing independent review. Verify the frozen S2A handoff can gate S5A without any S5B output; raw retention remains 24h images/7d text with deletion propagation and distinct accepted-knowledge consent.

## Verification

CLJC fixtures replay exact-event duplicates, out-of-order causal references, same-epoch close/reuse and unsuccessful compensation; use actual Clio admission tests. Old memberships/history must not attach to the reused XID, and targeting the old lifetime is denied even with a current revision. Clio runtime append generates a fresh UUID per invocation; retry the exact admitted event through ledger append for event-ID idempotency, not another runtime append call. These are future acceptance checks, not executed implementation evidence.

## Risks

Snapshot/event races and reused identities can turn a historical observation into a destructive command target.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-workspace-go.md
- /home/err/docs/notes/design/epic-i3-mcp.md

## Dependencies

- 07452a65-4a6a-4704-bf81-50408927dcc8 (S1A): generation/source manifest and isolated fixture identity.

---
Native findings 4456846184 and 4456856398: epoch plus XID is not a lifetime key. Require open/incarnation identity or creation revision; replay/history/membership/action targeting tests cover close/reuse within one epoch and fail-closed uncertain reconnect. Oikos owns portable lifetime laws; Clio event admission/replay is reused. Runtime append assigns a fresh UUID per call; exact-event ledger retry preserves event ID rather than calling runtime append again. Current native child prerequisites synchronized to the user-approved precise plan: 07452a65-4a6a-4704-bf81-50408927dcc8 (S1A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Consensus planning correction v1: root incoming acceptance/verification now explicitly specifies s2a/coverage-gap. See the corresponding named root subsections for the contract and concrete future positive/negative fixtures. Also propagates s5b/safe-ledger: reviewed immutable metadata allowlist and deletable content references through Clio, not raw payload persistence or a second privacy engine. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

Safe-reference order correction for native F153 findings Codex4458493223 and CR4458522612: current root acceptance assigns initial closed/versioned portable .cljc metadata/content-reference definition, independent review and exact artifact handoff to S2A within its existing 5 points. S2A gates its own Clio admission; S5A explicitly depends on S2A plus existing S2B/S7A and consumes the exact reviewed version/digest/source/review evidence without S5B output. S5B follows S5A, consumes/reviews the same baseline and may only compatibly extend it prospectively under separately reviewed versions, never rewrite immutable history or retroactively qualify admission. E2/E5 acceptance reflects this order. Privacy exclusions, raw 24h/7d retention, deletion propagation, accepted-memory consent and Clio authority remain unchanged. Historical comments assigning initial ownership to S5B are superseded, not edited. This is future planning/verification only: no implementation, review completion, native settlement, status transition or execution permission is claimed.

User-authorized artifact-scoped implementation admission: coordinator verified only generation/FS component 470c35e7d5bfdfbfd1a93bec3a1f63357015df69, published private ref artifact/oikos-generation-component-470c35e7. Scope/evidence: .ημ/runs/oikos-program-20261103/permits/generation-component-470c35e7.edn (including five source-file hashes); independent five-fix review reviews/s1a-code-five-fix-review-v2.edn APPROVE for the five manifest/isolation/input fixes; parent actual rerun BB 18/286, NBB 30/392, eval 9/39, zero failures/errors, lint 0 errors/0 warnings. These parent/coordinator observations are not rerun by this board operator. Consumer may use ONLY verified portable manifest, canonical identity, closed inputs, read-only isolation and synthetic fixture identity. S1A remains testing with two BB protocol findings held; no whole-S1A acceptance/completion or native approval is implied. Preserve S1A UUID dependency. oikos.infra.bb-render/start!, BB deadline/BOM transport qualification, positive Bash, write-boundary safety, promotion and rollback remain HELD. Inherited diagnostic regression tests grant no protocol qualification or operational transport permission. This is coordinator manual external-code prerequisite verification, NOT an engine-enforced artifact gate or new board semantics; Rheos owns ordinary FSM/WIP/status admission. Synthetic sandbox/dev-only, agent-owned resources, zero-spend, no personal capture and publication holdbacks remain unchanged. S2A may develop its own initial portable safe-reference contract and pure projection laws; actual event admission (including owned synthetic Clio fixtures) remains held until that contract has its OWN independent review. No live collector activation.

Phase-1 independent review at 34f9a4d requested recursive metadata and runtime-record closure fixes before contract freeze. Original implementer reproduced both findings in RED ea6d712 (225 failures in each BB/NBB runtime), then GREEN c399b60e1676c47778445c1098eb541ab38743cb passes desktop16/1059 each, inherited BB18/286,NBB30/392,eval9/39; lint0/0. Contract v1 replacement digest 3cf52cca7310d1bf628f555fc6ffc86d416f55ebe527a7007f0d25eaa28eaa49 and exact source are pending the SAME independent reviewer re-review. No contract freeze or actual Clio append/persistence is admitted yet. Clio owner 788cdd3434615a7932b924e68520dbf7f88408c2 remains untouched; diagnostics must remain constant and input-free. S2A remains in_progress, not accepted or done.

Parent authorizes S2A task completion after actual PR https://github.com/open-hax/oikos/pull/3 merged 2026-11-04T02:43:25Z as fed4e30272cc1186de14bcc708b3efc9736e3139, tested candidate 86bdc2f894163b05e44c83cce8f5dcf56e1b02c1 on accepted base 008b0611198d13eb503bb57d5ef69d8dec1c268c (reported candidate tree prefix 429d9). Independent exact-source prospective wire fix review .ημ/runs/oikos-program-20261103/reviews/s2a-prospective-wire-fix-review-v3.edn: 17/329; wire 15/347 each runtime, spaces 11/455 each, full BB 18/286, NBB 40/546, eval 9/39, desktop BB/NBB each 16/1059, lint 0 errors/0 warnings. All 13 frozen pins unchanged; 22 new fixture trees, 37 owner calls and five overflow refusals with zero append. Parent reports original native P2 root 4462253023 in thread PRRT_kwDOVhh8dc6zZb-R fixed by source and actual CLI settlement verified RESOLVED; historical review 5761610494 remains preserved. Fresh native Codex issue verdict 6620317546 explicitly passes exact 86bdc2f894163b05e44c83cce8f5dcf56e1b02c1; CodeRabbit current-head check SUCCESS, no required branch protection reported. Merge comment 6621108768 records user-authorized local-review/cohort override, NOT a falsely passing CLI cohort gate. No synthetic native APPROVED state, old cohort credit or separate security approval is claimed. This board operator records parent/independent evidence, not independently rerun product tests or native merge verification. Acceptance scope: closed portable laws and actual OWNED SYNTHETIC Clio fixture append/exact-event retry/persisted replay, including coverage/currentness and safe-reference handoff. Earlier comments keeping contract review and synthetic fixture admission pending are superseded by this completion evidence, not rewritten. This is NOT an operational journal, live collector, erasure-success claim or universal foreign-writer transaction guarantee; live collector activation was a task non-goal. No immutable historical rewrite or sensitive payload retention is authorized. Body remains historical and unchanged per current Rheos authoring rule; this append-only comment records current acceptance. Only S2A follows ordinary in_progress→testing→review→document→done; S1A dependency, E2 parent, labels and historical comments preserved. S2B must separately complete before E2 source integration; no E2 completion or other successor admission. All synthetic/dev/no-spend/no-personal-capture/effect holdbacks remain.

---