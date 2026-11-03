---
uuid: "59774be2-cc61-4708-9030-8b7b42935ec3"
title: "Oikos: Reconcile i3 subscribe snapshot races and reconnect revisions"
status: "incoming"
type: "task"
priority: "P1"
points: "8"
labels: "oikos, planning, local-first, task"
parent: "3b9279b5-aafd-431a-90a9-128ce4415de0"
dependency: ["93a76dad-678a-4980-86fb-ba0fbb5e8203"]
category: "kanban"
write-id: "1793718667122-0.qht4raxxyd808h2v6r6"
created_at: "2026-11-03T15:11:07.122Z"
---

# Oikos: Reconcile i3 subscribe snapshot races and reconnect revisions

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: Clio desktop journal and causal EDN projections.

## Outcome

Reconcile i3 subscribe snapshot races and reconnect revisions.

## Scope

Implement outer i3 collector using subscribe-before-snapshot, race reconciliation and explicit disconnect/resync state.

## Non-goals

No parallel journal kernel, invented complete deltas, guaranteed reopening of closed applications, or live collector activation.

## Acceptance criteria

- [ ] IPC events are not treated as complete tree deltas; events during snapshot cause reconciliation with an explicit observed revision.
- [ ] Disconnect/epoch change invalidates currentness and stale targets; resync yields admitted observations rather than invented continuity.

## Verification

Fake IPC harness interleaves subscription/snapshot/events, drops connections and reuses ids; sandbox integration verifies projection against a fresh tree. These are future acceptance checks, not executed implementation evidence.

## Risks

Snapshot/event races and reused identities can turn a historical observation into a destructive command target.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-workspace-go.md
- /home/err/docs/notes/design/epic-i3-mcp.md

## Dependencies

- 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A)
