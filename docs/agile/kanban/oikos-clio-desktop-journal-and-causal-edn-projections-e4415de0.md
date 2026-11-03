---
uuid: "3b9279b5-aafd-431a-90a9-128ce4415de0"
title: "Oikos: Clio desktop journal and causal EDN projections"
status: "incoming"
type: "epic"
priority: "P1"
labels: "oikos, planning, local-first, epic"
dependency: ["722906b7-77d7-4414-a976-23996f7533de"]
category: "kanban"
write-id: "1793718666490-0.spuiw5upnv94ihq0j2h"
created_at: "2026-11-03T15:11:06.490Z"
---

# Oikos: Clio desktop journal and causal EDN projections

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Replay a causally honest desktop projection from admitted immutable observations, intentions and outcomes.

## Scope

Reuse Clio schema, admission, locking and canonical causal streams. i3 subscriptions are notifications, not complete tree deltas: subscribe before snapshot, reconcile races and resync on disconnect with explicit observed revisions. Window identity is server epoch plus id because XIDs are reused. Pure CLJC replay separates observation from intent/outcome. Undo is compensating action only.

## Non-goals

No parallel journal kernel, invented complete deltas, guaranteed reopening of closed applications, or live collector activation.

## Acceptance criteria

- [ ] Clio admits distinct observation/intention/outcome schemas with causal references and immutable revision evidence.
- [ ] Subscribe-before-snapshot, concurrent events, disconnect/resync and epoch rollover cannot silently produce current-state claims.
- [ ] Deterministic replay rejects stale identities; compensation records attempted and observed outcomes without claiming resurrection.

## Verification

CLJC replay/property tests and a fake i3 IPC race harness, including reused XID, interrupted snapshot, duplicate delivery, reconnect and failed compensation. These are future acceptance checks, not executed implementation evidence.

## Risks

Snapshot/event races and reused identities can turn a historical observation into a destructive command target.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-workspace-go.md
- /home/err/docs/notes/design/epic-i3-mcp.md

## Dependencies

- 722906b7-77d7-4414-a976-23996f7533de (E1)
