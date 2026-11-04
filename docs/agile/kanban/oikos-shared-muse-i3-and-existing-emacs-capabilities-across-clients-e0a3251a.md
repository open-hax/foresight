---
category: "kanban"
labels: "oikos, planning, local-first, epic"
dependency: ["722906b7-77d7-4414-a976-23996f7533de", "3b9279b5-aafd-431a-90a9-128ce4415de0"]
type: "epic"
write-id: "1793724128855-0.5kx5f1cdbacm5r3d4bk"
title: "Oikos: shared Muse i3 and existing-Emacs capabilities across clients"
priority: "P1"
status: "incoming"
uuid: "456b2c69-cb5d-417d-9aeb-ac05e0a3251a"
created_at: "2026-11-03T15:11:08.376Z"
---

# Oikos: shared Muse i3 and existing-Emacs capabilities across clients

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Expose one typed capability contract safely through each supported client adapter.

## Scope

Named typed read operations and allowlisted named actions with scoped auth, revision-bound previews and explicit mutation. Use the existing Emacs server, never a second daemon; buffer allowlists and bounded reads. Forbid arbitrary remote emacsclient --eval and i3-msg. Muse compiles compatibility, not five domain copies. Host matrix: OpenCode, Claude, ChatGPT MCP remote HTTPS/auth transport gap, Emacs/gptel session adapter, Knoxx HTTP MCP (stdio not implemented).

## Non-goals

No arbitrary remote evaluation/IPC strings, second Emacs daemon, claims all integrations are live today, or clients bypassing shared authorization.

## Acceptance criteria

- [ ] Shared CLJC operation/action contracts enforce auth scopes, buffer bounds, named allowlist and observed revision across every adapter.
- [ ] Each matrix entry has separate discovery/read/preview/mutate/deny/disconnect evidence or an explicit blocking transport gap.
- [ ] ChatGPT remote HTTPS/auth and Knoxx HTTP-only constraints remain explicit until actual integration tests pass.

## Verification

Contract conformance and negative tests; isolated existing-server Emacs/i3 fixture; actual per-client integration receipts required later, not simulated approval. These are future acceptance checks, not executed implementation evidence.

## Risks

Transport support and client discovery do not prove policy parity; editor content may include secrets.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/docs/notes/design/epic-emacs-mcp.md
- /home/err/docs/notes/design/epic-i3-mcp.md
- /home/err/docs/notes/design/epic-agent-sandbox.md

## Dependencies

- 722906b7-77d7-4414-a976-23996f7533de (E1)
- 3b9279b5-aafd-431a-90a9-128ce4415de0 (E2)

---
Planning correction supersedes any interpretation that the full E4 epic must finish before GovOne security admission, or that real remote client qualification can precede it. Keep E4 parent dependencies at E1 plus E2 only; do not add S6A to this parent. The remote admission gate is represented explicitly on S4B: S4A plus S6A. S6A and E6 depend on E2 plus S4A, not the entire E4. S4A is the shared local capability slice and does not require prior completion of its parent E4 or remote client matrix. This avoids a hierarchy-induced execution cycle while gating real remote tool results through an admitted GovOne session. Only synthetic no-private-data tests may precede remote admission. This comment is an incoming planning correction, not completed review or execution evidence.

Native finding 4456856521: retain E4 parent summary dependencies E1/E2; concrete S4A execution consumes S1A/S2A, S4B consumes S4A/S6A. No parent-child gating cycle or claim that local capability requires the completed remote matrix. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

---