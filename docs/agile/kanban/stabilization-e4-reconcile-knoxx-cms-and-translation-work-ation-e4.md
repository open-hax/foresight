---
uuid: "stabilization-e4"
title: "Stabilization E4 — Reconcile Knoxx CMS and translation work"
status: "incoming"
type: "epic"
priority: "P1"
labels: "stabilization, coordination, e4"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
dependency: ["stabilization-e2", "stabilization-e3"]
category: "kanban"
write-id: "1792481172760-0.ea5pmxw0w4xzi7zo8k"
created_at: "2026-10-20T07:26:12.760Z"
---

# E4 — Reconcile Knoxx CMS and translation work

## Context

We want all existing CMS implementation work preserved and merged into a coherent application before asking an agent to continue feature development.

Stabilization coordination only. Preserve child ownership and existing story identities.

## Scope

- E4.1: Recompose PR #305: Reconcile its 23 extracted layers with current main, newer PR #387, PR #388 and personal fork #19.
- E4.2: Qualify content history: Source locale, revisions, approved media, human review decisions and stale-source refusal.
- E4.3: Qualify translations: Ensure English and Spanish use their correct independent review and publication conditions.
- E4.4: Stabilize persistence and embeddings: Durable publication receipts, Qwen migration, readback after restart and rollback.
- E4.5: Run complete application verification: Backend/frontend tests, lint, migration ratchets, protected release evidence and browser tours.
- E4.6: Merge and reconcile: Account for all CMS PRs without overwriting newer code.

The slot-by-slot exit proofs and existing-task candidates are in [the portfolio](../../stabilization/portfolio.md#e4--reconcile-knoxx-cms-and-translation-work).

## Acceptance

The CMS source is reconciled and its actual running capabilities are reported precisely. The remaining agent goal is documented if end-to-end operation is not yet proven.

- Resolve every slot to existing canonical work or an explicit owned gap before creating execution stories.
- Bind qualification evidence to exact source revisions and preserve prior attempts.
- Registration and board status do not qualify a milestone.

## Dependencies

`stabilization-e2`, `stabilization-e3`

## Evidence

[Portfolio tracker](../../stabilization/tracker.md); [source-backed work graph](../../stabilization/work-graph.json).

## Definition of done

The CMS source is reconciled and its actual running capabilities are reported precisely. The remaining agent goal is documented if end-to-end operation is not yet proven.
