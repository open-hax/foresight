---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["93a76dad-678a-4980-86fb-ba0fbb5e8203"]
parent: "ff5998a4-2cd6-482c-a43e-94522eb908b4"
type: "task"
write-id: "1793745068226-0.nfd1jj6w54gby6zoq"
points: "5"
title: "Oikos: Implement logical memberships and speculative EDN projection branches"
priority: "P1"
status: "done"
uuid: "b2f52c1f-bd1b-431e-a417-95c72549ba0d"
created_at: "2026-11-03T15:11:07.755Z"
---

# Oikos: Implement logical memberships and speculative EDN projection branches

## Context

Private open-hax/oikos at /home/err/spaces/foresight/oikos is a local-first shared workbench. S3A's pure logical-membership and named-branch scope is accepted at reviewed integration candidate 27adbfec5fd60302f93b1adf842dfd3f443644ab, merged through PR2 at 2026-11-03T22:27:45Z as 008b0611198d13eb503bb57d5ef69d8dec1c268c. Parent verified merge tree 06d714dd42ec2b12cd51bdc37e1b4b0fe67d9f3b equals the reviewed candidate, clean primary main fast-forwarded, and private artifact/oikos-s3a-008b0611 published normally. Evidence: .ημ/runs/oikos-program-20261103/reviews/s3a-parent-acceptance.edn and permits/s3a-accepted-008b0611.edn in that same run directory. This completes only the pure task, not E3, S3B movement admission or S2A as a whole; live effects, installation, inference, personal capture, upload and spending remain held.

## Outcome

Implement logical memberships and speculative EDN projection branches.

## Scope

Create pure CLJC named-space/membership/branch operations referencing the same journal window identity.

## Non-goals

No native i3 duplicate-window illusion, hidden-workspace security isolation guarantee, autonomous visible-workspace takeover or live movement.

## Acceptance criteria

- [x] One physical window can belong to multiple logical spaces without native duplication; qualified as a pure membership law, not a live desktop effect.
- [x] Portable .cljc branch contracts identify `:branch/id`, `:branch/base-revision` and `:branch/proposed-revision`, with memberships referring to the complete window lifetime identity. Create/edit/compare performs no physical window movement.
- [x] A mismatched observed revision returns EDN `{:result/status :conflict :conflict/reason :stale-revision :branch/id id :branch/base-revision base :branch/proposed-revision proposed :observed/revision actual}`. Conflict does not mutate the branch, membership, admitted history or physical desktop; explicit reconciliation requires a fresh proposal, never silent rebase. Qualification is against caller-supplied observations, not independently established live currentness.

### S3A branch predecessor

- [x] Each edit identifies its named branch's exact expected predecessor version independently of the desktop observation revision. A missing, wrong or stale predecessor returns an explicit branch-predecessor conflict without mutation, even when the observed desktop revision still matches. Concurrent proposals checked against the current accepted branch cannot silently overwrite one another; separate named branches remain independent. The caller MUST supply that current accepted branch. This qualifies the pure product-law precondition, not persistent CAS, an atomic store or a new Clio ordering authority.

Accepted pure verification covers competing proposals at the same predecessor with unchanged desktop revision: after one accepted successor is supplied as the current branch, the other conflicts without changing branch, memberships or admitted history. Missing/wrong predecessors deny; branch B edits leave A/shared base unchanged and repeated rejection accumulates no changes. Exact stale-observation and branch-predecessor conflict schemas are distinct. These are pure-law results, not persistent concurrency or live-currentness guarantees.

## Verification

Independent pure review reviews/s3a-code-v1.edn approved source 8642b46e70ec9dee9851b643dd53de32fb2acbf5 without findings, including 813 schedule and 734 boundary checks per runtime. Independent integration review reviews/s3a-integration-v2.edn approved exact 27adbfec5fd60302f93b1adf842dfd3f443644ab without findings: spaces BB/NBB each 11 tests/455 assertions; full BB 18/286, NBB 40/546, evaluation 9/39 and desktop BB/NBB each 16/1059, zero failures/errors and lint 0 errors/0 warnings. Seven S3A files and nine frozen S2A files were unchanged, with accepted S1A byte-identical. Parent separately reran spaces 11/455 in both runtimes and lint 0/0. This board operator records that evidence, not a fresh product test run.

https://github.com/open-hax/oikos/pull/2 merged as 008b0611198d13eb503bb57d5ef69d8dec1c268c with parents 62e6eebbf75c3f2b28e4a550b4a01881a2f5e732 and 27adbfec5fd60302f93b1adf842dfd3f443644ab; parent verified merge/candidate tree equality. Complete native pages showed zero threads/findings and exact-head Codex passing receipt 6615777023; CodeRabbit success/free summary is not full approval, and absent MiMo/Kimi routes supply no approval. User-authorized local-review admission avoided hosted cohort waiting; ordinary exact-head merge used no admin bypass. No persistent atomic store/CAS, live currentness, physical movement, S3B admission, installed promotion or epic completion is qualified. S2A whole-task work remains in_progress.

## Risks

Hidden workspaces are organizational, not isolation; human or external i3 changes may invalidate a return plan.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/docs/notes/design/epic-task-centric.md
- /home/err/docs/notes/design/epic-layout-hygiene.md
- /home/err/docs/notes/design/epic-agent-sandbox.md

## Dependencies

- 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A): admitted lifetime identities, causal event shapes and pure replay; no collector is needed for pure branch laws.

---
Native finding 4456856486: portable branch ID/base revision/proposed revision and explicit EDN stale-revision conflict shape are required before adapters. Conflict performs no branch/membership/history/desktop mutation; no silent rebase. Shared journal lifetime identity prevents same-epoch XID reassignment. Current native child prerequisites synchronized to the user-approved precise plan: 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Source-grounded adjudication of :s3a/branch-version: the root S3A branch-predecessor section now requires an exact expected predecessor for each named-branch edit, independent of observed desktop revision. Missing/wrong/stale predecessor conflicts without mutation; concurrent edits cannot silently overwrite and separate branches remain independent. Future fixtures exercise two edits at one predecessor with an unchanged desktop revision, wrong/missing predecessors and repeated rejection. This clarifies the missing normative precondition without prescribing a CAS/store algorithm, inventing Clio authority or claiming implementation. Original skeptic disagreements remain preserved in the adjudication artifact.

User-authorized precise component-prerequisite admission, not whole-producer completion: S2A remains in_progress. Parent verified exact c399b60e1676c47778445c1098eb541ab38743cb, published artifact/oikos-safe-reference-c399b60e; freeze permit .ημ/runs/oikos-program-20261103/permits/s2a-contract-c399b60e.edn pins provided portable closed plain safe-reference contract, lifetime/revision identity, pure causal desktop replay and coverage-gap/compensation laws. Contract oikos.desktop.safe-reference/v1 digest 3cf52cca7310d1bf628f555fc6ffc86d416f55ebe527a7007f0d25eaa28eaa49; independent approved review reviews/s2a-contract-fix-review-v2.edn SHA256 e329a2fa6b171795a56c1800ac12893d5b1dccd1312f3cf0c7b01d9e0232542e; Clio owner 788cdd3434615a7932b924e68520dbf7f88408c2 unchanged. Parent actual desktop tests BB 16/1059 and NBB 16/1059, inherited BB 18/286, NBB 30/392, eval 9/39, zero failures/errors, lint 0/0. These are parent observations, not rerun by this board operator. Actual Clio append/persistence is not supplied or required for this pure slice. Keep all UUID dependencies; ordinary Rheos FSM/WIP status admission is NOT native artifact attestation. Inherited 470 BB adapter is operationally HELD until accepted new source is explicitly imported; diagnostic inherited tests confer no operational protocol qualification. No live collectors, personal capture, content-store/retention success, editor/window effects, model execution, remote tool publication or spending. New code requires its own independent exact-head review before acceptance. S3A admission is ONLY pure logical membership and named-branch laws using the provided admitted lifetime/revision/causal contracts and pure Clio-backed replay; no actual ledger append prerequisite or runtime workspace admission. Parent prepared its isolated worktree at c399.

S3A qualified pure-task completion: parent acceptance .ημ/runs/oikos-program-20261103/reviews/s3a-parent-acceptance.edn and frozen permit .ημ/runs/oikos-program-20261103/permits/s3a-accepted-008b0611.edn. Independent reviews/s3a-code-v1.edn approved pure source 8642b46e70ec9dee9851b643dd53de32fb2acbf5 without findings (813 schedule/734 boundary checks per runtime); reviews/s3a-integration-v2.edn approved exact candidate 27adbfec5fd60302f93b1adf842dfd3f443644ab without findings. Actual independent integration tests: spaces BB and NBB each 11/455; full BB 18/286, NBB 40/546, eval 9/39, desktop BB/NBB each 16/1059; zero failures/errors, lint 0/0. Seven S3A and nine frozen S2A files unchanged; accepted S1A byte-identical. Parent separately reran spaces BB/NBB 11/455 and lint 0/0. PR https://github.com/open-hax/oikos/pull/2 merged 2026-11-03T22:27:45Z as 008b0611198d13eb503bb57d5ef69d8dec1c268c, parents 62e6eebbf75c3f2b28e4a550b4a01881a2f5e732 and 27adbfec5fd60302f93b1adf842dfd3f443644ab. Parent verified tree 06d714dd42ec2b12cd51bdc37e1b4b0fe67d9f3b equals reviewed candidate; clean primary main fast-forwarded and private artifact/oikos-s3a-008b0611 published by normal push. Native complete pages: zero threads/findings, exact-head Codex passing receipt 6615777023; CodeRabbit check SUCCESS/free summary is not full approval, absent MiMo/Kimi routes give no approval. User-authorized qualified local-review admission permits ordinary exact-head merge without hosted-cohort waiting or admin bypass. Current body/checks now reflect pure stale-observation and branch-predecessor laws; caller MUST supply current accepted branch. No persistent CAS/atomic store, live currentness, physical effects, S2A whole completion, S3B admission or E3 completion. Board operator records parent/independent evidence, not independently rerun tests/merge verification. Only S3A walks in_progress→testing→review→document→done; dependencies/parent and historical comments preserved.

---