---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["93a76dad-678a-4980-86fb-ba0fbb5e8203"]
parent: "ff5998a4-2cd6-482c-a43e-94522eb908b4"
type: "task"
write-id: "1793727206524-0.0ni5weye9zf9d9lugxby"
points: "5"
title: "Oikos: Implement logical memberships and speculative EDN projection branches"
priority: "P1"
status: "incoming"
uuid: "b2f52c1f-bd1b-431e-a417-95c72549ba0d"
created_at: "2026-11-03T15:11:07.755Z"
---

# Oikos: Implement logical memberships and speculative EDN projection branches

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: named collaborative projection spaces with deliberate joining.

## Outcome

Implement logical memberships and speculative EDN projection branches.

## Scope

Create pure CLJC named-space/membership/branch operations referencing the same journal window identity.

## Non-goals

No native i3 duplicate-window illusion, hidden-workspace security isolation guarantee, autonomous visible-workspace takeover or live movement.

## Acceptance criteria

- [ ] One physical window can belong to multiple logical spaces without native duplication.
- [ ] Portable .cljc branch contracts identify `:branch/id`, `:branch/base-revision` and `:branch/proposed-revision`, with memberships referring to the complete window lifetime identity. Create/edit/compare performs no physical window movement.
- [ ] A mismatched observed revision returns EDN `{:result/status :conflict :conflict/reason :stale-revision :branch/id id :branch/base-revision base :branch/proposed-revision proposed :observed/revision actual}`. Conflict does not mutate the branch, membership, admitted history or physical desktop; explicit reconciliation requires a fresh proposal, never silent rebase.

### S3A branch predecessor

- [ ] Each edit identifies its named branch's exact expected predecessor version independently of the desktop observation revision. A missing, wrong or stale predecessor returns an explicit branch-predecessor conflict without mutation, even when the observed desktop revision still matches. Concurrent edits cannot silently overwrite one another; separate named branches remain independent. This is a product-law precondition, not a prescribed CAS/store implementation or a new Clio ordering authority.

Future verification: two edits of branch A consume the same predecessor while the desktop revision is unchanged; after one accepted successor, the other conflicts with unchanged branch, memberships and admitted history. Missing/wrong predecessor also denies. Editing branch B cannot alter A or their shared base; repeating a rejected edit accumulates no changes. The frozen result schema must distinguish branch-predecessor conflict from the observed-revision conflict above.

## Verification

Property tests preserve one complete physical lifetime identity across memberships, including same-epoch XID reuse isolation. Zero-effect adapter spies and stale-branch fixtures assert the exact conflict shape and unchanged branch/membership/history/desktop snapshots. These are future acceptance checks, not executed implementation evidence.

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

---