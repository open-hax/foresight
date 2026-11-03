---
uuid: "93a76dad-678a-4980-86fb-ba0fbb5e8203"
title: "Oikos: Admit desktop causal events through Clio and replay EDN projections"
status: "incoming"
type: "task"
priority: "P1"
points: "5"
labels: "oikos, planning, local-first, task"
parent: "3b9279b5-aafd-431a-90a9-128ce4415de0"
dependency: ["722906b7-77d7-4414-a976-23996f7533de"]
category: "kanban"
write-id: "1793718666816-0.g65l4pvzakreofn0bu"
created_at: "2026-11-03T15:11:06.816Z"
---

# Oikos: Admit desktop causal events through Clio and replay EDN projections

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: Clio desktop journal and causal EDN projections.

## Outcome

Admit desktop causal events through Clio and replay EDN projections.

## Scope

Reuse Clio admission/schema/locking and canonical causal streams; implement pure CLJC desktop replay.

## Non-goals

No parallel journal kernel, invented complete deltas, guaranteed reopening of closed applications, or live collector activation.

## Acceptance criteria

- [ ] Observation, intention and outcome have distinct shapes and causal links; observed revision and server-epoch+id are mandatory.
- [ ] Replay is deterministic; stale identity and conflicting admission fail; undo is compensation and cannot guarantee reopening apps.

## Verification

CLJC fixtures replay duplicates, out-of-order causal references, XID reuse and unsuccessful compensation; use actual Clio admission tests. These are future acceptance checks, not executed implementation evidence.

## Risks

Snapshot/event races and reused identities can turn a historical observation into a destructive command target.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-workspace-go.md
- /home/err/docs/notes/design/epic-i3-mcp.md

## Dependencies

- 722906b7-77d7-4414-a976-23996f7533de (E1)
