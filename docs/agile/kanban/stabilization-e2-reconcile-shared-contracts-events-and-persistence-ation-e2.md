---
uuid: "stabilization-e2"
title: "Stabilization E2 — Reconcile shared contracts, events and persistence"
status: "incoming"
type: "epic"
priority: "P1"
labels: "stabilization, coordination, e2"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
dependency: ["stabilization-e0", "stabilization-e1"]
category: "kanban"
write-id: "1792481172101-0.ho22sh0c2ncw1lpx4v8"
created_at: "2026-10-20T07:26:12.101Z"
---

# E2 — Reconcile shared contracts, events and persistence

## Context

Clio, Rheos, Katamorph and the receipt infrastructure are the shared foundations of both goal agents.

Stabilization coordination only. Preserve child ownership and existing story identities.

## Scope

- E2.1: Clio: Admit segmented ledgers, exact retries, canonical replay and durable recovery.
- E2.2: Rheos: Resolve document identity, duplicate UUIDs, frontmatter custody and lawful lifecycle updates.
- E2.3: Document history: Preserve concurrent revisions, source relationships and event provenance.
- E2.4: Katamorph and Foresight shapes: Reconcile portable semantic laws with representation-specific adapters.
- E2.5: Receipt River and consumers: Qualify append-only evidence, reader semantics and exact downstream pins.

The slot-by-slot exit proofs and existing-task candidates are in [the portfolio](../../stabilization/portfolio.md#e2--reconcile-shared-contracts-events-and-persistence).

## Acceptance

Shared event and data contracts have qualified implementations, and consumers use the accepted revisions without silently changing history.

- Resolve every slot to existing canonical work or an explicit owned gap before creating execution stories.
- Bind qualification evidence to exact source revisions and preserve prior attempts.
- Registration and board status do not qualify a milestone.

## Dependencies

`stabilization-e0`, `stabilization-e1`

## Evidence

[Portfolio tracker](../../stabilization/tracker.md); [source-backed work graph](../../stabilization/work-graph.json).

## Definition of done

Shared event and data contracts have qualified implementations, and consumers use the accepted revisions without silently changing history.
