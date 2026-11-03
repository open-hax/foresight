---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["07452a65-4a6a-4704-bf81-50408927dcc8", "93a76dad-678a-4980-86fb-ba0fbb5e8203"]
parent: "456b2c69-cb5d-417d-9aeb-ac05e0a3251a"
type: "task"
write-id: "1793736707998-0.72vgrgiqh1u9rnyikz0"
points: "5"
title: "Oikos: Define scoped named i3 and existing-Emacs capability operations"
priority: "P1"
status: "in_progress"
uuid: "29caa8e3-289a-416c-9850-8ef9cc769e88"
created_at: "2026-11-03T15:11:08.733Z"
---

# Oikos: Define scoped named i3 and existing-Emacs capability operations

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: shared Muse i3 and existing-Emacs capabilities across clients.

## Outcome

Define scoped named i3 and existing-Emacs capability operations.

## Scope

Implement one shared typed capability contract and existing-server Emacs/i3 boundary adapters for Muse compatibility.

## Non-goals

No arbitrary remote evaluation/IPC strings, second Emacs daemon, claims all integrations are live today, or clients bypassing shared authorization.

## Acceptance criteria

- [ ] Named reads enforce auth scopes, buffer allowlists and byte/range bounds; named mutations require revision-bound preview and explicit approval.
- [ ] Arbitrary remote emacsclient --eval and i3-msg inputs are rejected; no second Emacs daemon is launched.

### S4A editor revision

- [ ] An editor mutation approval binds actor, named operation and exact arguments, Emacs server epoch, actual buffer incarnation (not buffer name) and modification revision. A fixed trusted dispatcher revalidates all bindings immediately before dispatching mutation inside that dispatcher; transported preview data is not current-resource authority. A replaced buffer, intervening edit, reconnect/server epoch change or changed argument denies before mutation.

Future verification: isolated existing-server Emacs fixtures replace a buffer under the same name, edit after preview, reconnect/change server epoch and alter operation arguments before dispatch. Each fails within the fixed trusted dispatcher with zero buffer mutation. The unchanged, exactly approved operation on the same actual buffer incarnation/revision succeeds once under existing scope/approval rules; no arbitrary evaluator or second daemon is introduced.

## Verification

Contract negative tests cover scope bypass, stale preview, buffer escape/oversize and arbitrary evaluation; isolated existing-server fixture records denied operations. These are future acceptance checks, not executed implementation evidence.

## Risks

Transport support and client discovery do not prove policy parity; editor content may include secrets.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/docs/notes/design/epic-emacs-mcp.md
- /home/err/docs/notes/design/epic-i3-mcp.md
- /home/err/docs/notes/design/epic-agent-sandbox.md

## Dependencies

- 07452a65-4a6a-4704-bf81-50408927dcc8 (S1A): isolated target/render contract.
- 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A): lifetime identity and revision-bound causal contracts.

---
Consumes S1A isolated target/render manifests and S2A lifetime/revision/causal contracts, not whole E1/E2 epic completion. Produces the shared named scoped capability/preview contract consumed by S3B movement and S6A policy; parent E4 organization is not a completion prerequisite. Current native child prerequisites synchronized to the user-approved precise plan: 07452a65-4a6a-4704-bf81-50408927dcc8 (S1A), 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Consensus planning correction v1: root incoming acceptance/verification now explicitly specifies s4a/editor-revision. See the corresponding named root subsections for the contract and concrete future positive/negative fixtures. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

User-authorized precise component-prerequisite admission, not whole-producer completion: S2A remains in_progress. Parent verified exact c399b60e1676c47778445c1098eb541ab38743cb, published artifact/oikos-safe-reference-c399b60e; freeze permit .ημ/runs/oikos-program-20261103/permits/s2a-contract-c399b60e.edn pins provided portable closed plain safe-reference contract, lifetime/revision identity, pure causal desktop replay and coverage-gap/compensation laws. Contract oikos.desktop.safe-reference/v1 digest 3cf52cca7310d1bf628f555fc6ffc86d416f55ebe527a7007f0d25eaa28eaa49; independent approved review reviews/s2a-contract-fix-review-v2.edn SHA256 e329a2fa6b171795a56c1800ac12893d5b1dccd1312f3cf0c7b01d9e0232542e; Clio owner 788cdd3434615a7932b924e68520dbf7f88408c2 unchanged. Parent actual desktop tests BB 16/1059 and NBB 16/1059, inherited BB 18/286, NBB 30/392, eval 9/39, zero failures/errors, lint 0/0. These are parent observations, not rerun by this board operator. Actual Clio append/persistence is not supplied or required for this pure slice. Keep all UUID dependencies; ordinary Rheos FSM/WIP status admission is NOT native artifact attestation. Inherited 470 BB adapter is operationally HELD until accepted new source is explicitly imported; diagnostic inherited tests confer no operational protocol qualification. No live collectors, personal capture, content-store/retention success, editor/window effects, model execution, remote tool publication or spending. New code requires its own independent exact-head review before acceptance. S4A admission is ONLY pure typed named scoped capability and revision-bound preview contracts. S1A is officially done at accepted merge 62e6eebbf75c3f2b28e4a550b4a01881a2f5e732; the same five verified manifest/isolation source hashes are present at c399, alongside the reviewed S2A lifetime/revision/causal contract. No live Emacs/i3 effects, existing server buffers, dispatcher installation or operator-resource actions; remote tool publication remains held. Parent prepared its isolated worktree at c399.

---