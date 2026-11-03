---
uuid: "29caa8e3-289a-416c-9850-8ef9cc769e88"
title: "Oikos: Define scoped named i3 and existing-Emacs capability operations"
status: "incoming"
type: "task"
priority: "P1"
points: "5"
labels: "oikos, planning, local-first, task"
parent: "456b2c69-cb5d-417d-9aeb-ac05e0a3251a"
dependency: ["722906b7-77d7-4414-a976-23996f7533de", "3b9279b5-aafd-431a-90a9-128ce4415de0"]
category: "kanban"
write-id: "1793718668733-0.p03jlair72gguif0nx6"
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

- 722906b7-77d7-4414-a976-23996f7533de (E1)
- 3b9279b5-aafd-431a-90a9-128ce4415de0 (E2)
