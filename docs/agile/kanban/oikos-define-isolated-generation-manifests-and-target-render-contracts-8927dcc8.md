---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: []
parent: "722906b7-77d7-4414-a976-23996f7533de"
type: "task"
write-id: "1793718990977-0.0ffffm0ykr7bwtplvfuh"
points: "5"
title: "Oikos: Define isolated generation manifests and target render contracts"
priority: "P1"
status: "incoming"
uuid: "07452a65-4a6a-4704-bf81-50408927dcc8"
created_at: "2026-11-03T15:11:05.825Z"
---

# Oikos: Define isolated generation manifests and target render contracts

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: isolated generations and shx/Muse configuration boundaries.

## Outcome

Define isolated generation manifests and target render contracts.

## Scope

Implement pure CLJC generation/environment shapes and shx/envm boundary with target-specific non-shell render contracts.

## Non-goals

No blanket config migration, generic supervisor replacement, secret material in manifests, or live deployment.

## Acceptance criteria

- [ ] Manifest separates dev/installed binary, artifact, state, socket and port namespaces and rejects collisions.
- [ ] Rendered fixtures use env secret references only; unsupported target formats fail explicitly; Muse is only tool compatibility.

## Verification

Table/property tests exercise path/port collisions, missing env refs and unsupported targets; fixture diff proves installed configuration untouched. These are future acceptance checks, not executed implementation evidence.

## Risks

Shared paths or a non-shell renderer masquerading as shell IR could silently change the installed workstation.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/spaces/foresight/docs/agile/kanban/federated-workstation-distribution.md
- /home/err/docs/notes/design/epics-and-tasks.md

## Dependencies

- Related existing program (not replaced; not a blocking implementation edge): federated-workstation-distribution.

---
Planning correction supplements and supersedes any single-runtime-only interpretation of shared-law verification. Acceptance requires the same shared CLJC law fixtures to pass in both Babashka (BB) and NBB, with fixture identity and each runtime's actual result retained. Explicit BB subprocess protocol tests must cover EDN framing, success and nonzero exits, malformed/truncated output, timeout and cancellation, including bounded termination/cleanup and truthful error propagation. No malformed output, timeout or cancelled child may be accepted as a successful rendered result. Cross-runtime law parity and subprocess protocol behavior are separate required checks; neither substitutes for the other. These are future acceptance/verification requirements, not tests run or completed implementation in this planning turn.

---