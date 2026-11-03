---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["07452a65-4a6a-4704-bf81-50408927dcc8"]
parent: "722906b7-77d7-4414-a976-23996f7533de"
type: "task"
write-id: "1793726847571-0.z78ugvz4yrkuivbrqxj"
points: "8"
title: "Oikos: Implement preview check apply rollback through Services-owned generations"
priority: "P1"
status: "incoming"
uuid: "3d14e0a5-2790-42b8-9a55-54d686925c3c"
created_at: "2026-11-03T15:11:06.166Z"
---

# Oikos: Implement preview check apply rollback through Services-owned generations

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: isolated generations and shx/Muse configuration boundaries.

## Outcome

Implement preview check apply rollback through Services-owned generations.

## Scope

Specify the outer preview/check/apply/rollback handoff to the Services owner before binding an adapter. Services README documents the DigitalOcean production lane, not a generic local-generation API; no such API has been established. S1A's manifest/render contract alone does not supply a generation promoter. This task's apply/rollback completion is blocked on a reviewed Services-owned local-generation contract and explicit owning handoff (entry point, versioned plan/result shapes, approval/revision binding, ownership, interrupted apply and rollback semantics). Record that external dependency as blocked until its actual owner supplies it; do not invent an available API or an Oikos supervisor. shx/envm provides environment merge/render, not promotion. BitchTracker operations is a first product precedent, not an accepted shared kernel.

## Non-goals

No blanket config migration, generic supervisor replacement, secret material in manifests, or live deployment.

## Acceptance criteria

- [ ] Preview/check are effect-free; apply requires explicit generation approval and refuses local modifications.
- [ ] Injected partial write restores the exact previous owned generation; no migration of unrelated config or shared Services ownership.
- [ ] Bind only a reviewed, available Services-owned generation seam with retained contract/version and owner handoff evidence; absent seam remains an explicit completion blocker. Oikos never substitutes its own supervisor or treats the DO deployment lane as local-generation availability.

## Verification

Isolated filesystem/process fixtures assert effect-free preview/check with unchanged target hashes and zero lifecycle/process mutation, denied unapproved apply with zero writes, dirty-target hold with local bytes preserved, and unchanged unrelated Services-owned paths/services. After the owner seam exists, inject every interrupted stage and verify exact owned-generation rollback, secret-free plans and owner-bound handoff/result evidence. A mock does not prove the absent generic API exists. These are future acceptance checks, not executed implementation evidence.

## Risks

Shared paths or a non-shell renderer masquerading as shell IR could silently change the installed workstation.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/spaces/foresight/docs/agile/kanban/federated-workstation-distribution.md
- /home/err/docs/notes/design/epics-and-tasks.md

## Dependencies

- 07452a65-4a6a-4704-bf81-50408927dcc8 (S1A)
- Related existing program (not replaced; not a blocking implementation edge): federated-workstation-distribution.

---
Native findings 4456856497 and 4456856511: explicitly test effect-free preview/check, unapproved apply denial, dirty-target holds, unrelated Services path/service preservation and exact rollback. Services README exposes the DO production lane, not a generic local-generation API. Apply/rollback qualification remains blocked on a reviewed Services-owner entry-point/versioned contract and explicit owning handoff; S1A manifests and shx/envm merge/render do not supply promotion. BitchTracker operations is a product precedent, not a shared kernel. No Oikos supervisor or unproven API is authorized. Current native child prerequisites synchronized to the user-approved precise plan: 07452a65-4a6a-4704-bf81-50408927dcc8 (S1A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Owner-artifact clarification from native Oikos PR1 findings4458124527/4458124541: S1B must obtain the real reviewed shx-owned safe bash render adapter and independently pinned positive validator before qualifying any bash generation. S1A provides an inert EDN target and truthful unavailable-bash contract, not positive bash implementation. No native shx check/eval substitution or second Oikos renderer/supervisor is authorized. S1B also owns non-vacuous synthetic effect-edge proof: after preview replace an ancestor/destination/current selection, then demonstrate serialized revalidation rejects stale evidence before an otherwise working fixture write; positive admitted writes and observed rollback must use the actual accepted Services handoff. No-write behavior of S1A cannot satisfy this proof. Missing either upstream owner artifact remains a qualification blocker; no implementation or live promotion is claimed.

---