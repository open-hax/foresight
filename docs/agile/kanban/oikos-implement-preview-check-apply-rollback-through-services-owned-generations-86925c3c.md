---
uuid: "3d14e0a5-2790-42b8-9a55-54d686925c3c"
title: "Oikos: Implement preview check apply rollback through Services-owned generations"
status: "incoming"
type: "task"
priority: "P1"
points: "8"
labels: "oikos, planning, local-first, task"
parent: "722906b7-77d7-4414-a976-23996f7533de"
dependency: ["07452a65-4a6a-4704-bf81-50408927dcc8"]
category: "kanban"
write-id: "1793718666166-0.jfol2bg2vycy9fgh5wi"
created_at: "2026-11-03T15:11:06.166Z"
---

# Oikos: Implement preview check apply rollback through Services-owned generations

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: isolated generations and shx/Muse configuration boundaries.

## Outcome

Implement preview check apply rollback through Services-owned generations.

## Scope

Add outer generation adapter with staged preview/check/apply/rollback and Services deployment handoff.

## Non-goals

No blanket config migration, generic supervisor replacement, secret material in manifests, or live deployment.

## Acceptance criteria

- [ ] Preview/check are effect-free; apply requires explicit generation approval and refuses local modifications.
- [ ] Injected partial write restores the exact previous owned generation; no migration of unrelated config or shared Services ownership.

## Verification

Isolated filesystem/process fixtures compare before/after hashes, secret-free plans and rollback on every interrupted stage. These are future acceptance checks, not executed implementation evidence.

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
