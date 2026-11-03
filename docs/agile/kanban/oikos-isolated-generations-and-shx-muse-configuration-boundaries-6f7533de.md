---
uuid: "722906b7-77d7-4414-a976-23996f7533de"
title: "Oikos: isolated generations and shx/Muse configuration boundaries"
status: "incoming"
type: "epic"
priority: "P1"
labels: "oikos, planning, local-first, epic"
dependency: []
category: "kanban"
write-id: "1793718486164-0.eqka0uqll26zzaonbct"
created_at: "2026-11-03T15:08:06.164Z"
---

# Oikos: isolated generations and shx/Muse configuration boundaries

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Develop and install independently identifiable Oikos generations without disturbing the working desktop.

## Scope

Services owns deployment. Separate development/installed binaries, build artifacts, state, sockets and ports. Reuse shx/envm shell/environment IR; non-shell targets use explicit target-specific render adapters. Muse owns tool compatibility only. Version preview/check/apply/rollback with secret environment references and local-change holds. Link, do not replace, the federated workstation program.

## Non-goals

No blanket config migration, generic supervisor replacement, secret material in manifests, or live deployment.

## Acceptance criteria

- [ ] A generation manifest identifies each owned binary/artifact/state/socket/port and rejects collisions before effects.
- [ ] Target adapters declare preview/check/apply/rollback and compatibility; rendered payloads contain only secret env references.
- [ ] Failure recovery and local-change holds are proven in an isolated fixture; federated-workstation-distribution remains separately owned.

## Verification

Portable CLJC manifest laws plus adapter fixtures cover collision, missing secret reference, dirty target, partial apply and exact rollback; Services reviews deployment seam. These are future acceptance checks, not executed implementation evidence.

## Risks

Shared paths or a non-shell renderer masquerading as shell IR could silently change the installed workstation.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/spaces/foresight/docs/agile/kanban/federated-workstation-distribution.md
- /home/err/docs/notes/design/epics-and-tasks.md

## Dependencies

- Related existing program (not replaced; not a blocking implementation edge): federated-workstation-distribution.
