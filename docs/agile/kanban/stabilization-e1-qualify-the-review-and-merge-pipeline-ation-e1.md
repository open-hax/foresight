---
uuid: "stabilization-e1"
title: "Stabilization E1 — Qualify the review and merge pipeline"
status: "incoming"
type: "epic"
priority: "P1"
labels: "stabilization, coordination, e1"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
dependency: ["stabilization-e0"]
category: "kanban"
write-id: "1792481171778-0.11q0cf84n4vvv1310xw"
created_at: "2026-10-20T07:26:11.778Z"
---

# E1 — Qualify the review and merge pipeline

## Context

We cannot close hundreds of PRs reliably if the review infrastructure itself remains inconsistent.

Stabilization coordination only. Preserve child ownership and existing story identities.

## Scope

- E1.1: Qualify Muse: Resolve complete-input review and evidence-directory handling.
- E1.2: Qualify Eta Mu and Proxx: Establish source-bound reviews, independent reviewer identity, complete verdicts and failure recovery.
- E1.3: Qualify fork promotions: Ensure accepted personal-fork work reaches the appropriate organization without dropping newer history.
- E1.4: Resolve outstanding findings: Test fixes, rerun current-head reviews, and settle actual review threads.
- E1.5: Add merge-drift verification: Recheck affected consumers after each shared dependency is accepted.

The slot-by-slot exit proofs and existing-task candidates are in [the portfolio](../../stabilization/portfolio.md#e1--qualify-the-review-and-merge-pipeline).

## Acceptance

We can demonstrate at least one complete fork → reviewed upstream PR → protected merge → verified consumer update, with no approval or evidence incorrectly transferred between revisions.

- Resolve every slot to existing canonical work or an explicit owned gap before creating execution stories.
- Bind qualification evidence to exact source revisions and preserve prior attempts.
- Registration and board status do not qualify a milestone.

## Dependencies

`stabilization-e0`

## Evidence

[Portfolio tracker](../../stabilization/tracker.md); [source-backed work graph](../../stabilization/work-graph.json).

## Definition of done

We can demonstrate at least one complete fork → reviewed upstream PR → protected merge → verified consumer update, with no approval or evidence incorrectly transferred between revisions.
