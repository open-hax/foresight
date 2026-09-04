---
uuid: "4f5cb66d-4fd2-47cf-92cc-2185b057a889"
title: "Coordinate the eta-mu GitHub evidence agent"
status: "in-progress"
priority: "P1"
labels: ["github", "eta-mu", "knoxx", "sol", "evidence", "workflow"]
created_at: "2026-09-01T22:30:00.000Z"
write-id: "github-evidence-agent-20260901"
---

# Coordinate the eta-mu GitHub evidence agent

Establish one reviewable workflow in which eta-mu receives GitHub App events,
pins exact review inputs, asks Sol to execute typed evidence workers, queries the
resulting Knoxx evidence graph, applies deterministic conclusion law, and
publishes an idempotent check/review for the exact head.

## Why this card exists

The constellation already contains GitHub event classification, Axxium identity,
event-ledger, Knoxx graph, Sol execution, Katamorph resources, and revision-bound
evidence work. The missing unit is the composition and authority boundary. This
card coordinates the child repository work without moving their semantic law
into Foresight.

## Workflow graph

```text
GitHub App delivery
  -> eta-mu intake/classification
  -> Axxium bindings
  -> immutable review-input manifest
  -> Sol evidence-worker fan-out
  -> append-only findings and terminal receipts
  -> Knoxx evidence graph/query projection
  -> eta-mu exact-head synthesis
  -> GitHub check/review publication
  -> periodic reconciliation
```

Detailed data, system, code, worker, state, and operator diagrams live in
`docs/notes/design/eta-mu-github-evidence-workflow.md`.

## Child repository lanes

### Eta-mu

- Parent tracking issue: **GitHub webhook to evidence-agent workflow**.
- Define exact-head GitHub check and review synthesis law.
- Fan out evidence review into typed micro-agents.
- Reuse the existing GitHub webhook classifier and event-ledger issues rather
  than opening duplicate architecture lanes.

### Sol

- Execute eta-mu evidence workers from immutable review manifests.
- Keep process/model/tool execution behind declared resources and budgets.
- Emit complete start, finding, and terminal records; do not own GitHub
  publication or the final review conclusion.

### Knoxx

- Project the eta-mu GitHub evidence graph and expose typed query contracts.
- Build from `open-hax/knoxx#294` after its required base checks are clean.
- Keep the graph replayable and subordinate to admitted event records.

### Katamorph

- Land `open-hax/katamorph#27` as the first GitHub interaction resource profile.
- Add worker/workflow shapes only after the shared seam is demonstrated by at
  least eta-mu and Sol.

### Axxium

- Reuse the existing GitHub workflow and provider identity migration cards.
- Bind installation, repository/object, actor, execution episode, and stream
  identities without importing GitHub or evidence semantics.

### Foresight

- Pin exact child revisions.
- Provide the signed GitHub fixture and cross-repository integration harness.
- Record exact commands, inputs, outputs, skips, failures, and artifact digests.
- Require projection replay and stale-head publication rejection before the
  vertical slice can be called complete.

## Evidence worker set

Start with four required deterministic-first roles:

1. revision/provenance;
2. GitHub check-run/check-suite state;
3. actionable review-thread verification;
4. event-ledger identity/replay.

Then add test-artifact, contract-compatibility, dependency/supply-chain,
security/permission, runtime/observability, and documentation/UX workers as
bounded optional roles until their gates are promoted by explicit policy.

## Acceptance

- Every admitted review run cites repository, base/head SHA, input-manifest
  digest, contract revisions, required worker set, and Axxium execution identity.
- Required workers may run concurrently and each emits exactly one terminal
  record, including timeout, cancellation, failure, blocked, or unavailable.
- Eta-mu publishes no green conclusion while any required check, worker,
  artifact, coverage record, or review-thread state is stale, missing, skipped,
  failed, partial, blocked, or unavailable.
- Knoxx answers which records and artifacts support every conclusion and can be
  deleted/rebuilt without changing logical identity or current gate state.
- A head change during execution prevents stale publication and admits a
  replacement run while preserving the old evidence.
- Duplicate delivery and duplicate publication fixtures have no duplicate
  semantic effect.
- Exact-revision unit, law, integration, replay, and publication fixtures emit
  their own event-ledger test records.

## Completion order

1. Clear review/check blockers on Foresight #69, Katamorph #27, Knoxx #293, and
   Knoxx #294.
2. Land the portable GitHub interaction resource and disabled Knoxx contracts.
3. Implement eta-mu exact-head synthesis law with a deterministic fixture.
4. Implement Sol fan-out for the first four worker roles.
5. Implement the Knoxx evidence graph/query projection.
6. Compose the signed-delivery end-to-end fixture in Foresight.
7. Enable the GitHub App path only after replay, stale-head, duplicate-delivery,
   and publication-idempotency gates pass.

## Stop conditions

This card remains in progress while any of the following is true:

- review agents have unresolved actionable comments on the bounded PRs;
- a required check is absent, stale, skipped, failed, or only represented by a
  non-executing review workflow;
- exact revision or artifact coverage is ambiguous;
- the first signed-delivery fixture cannot survive duplicate admission and
  projection rebuild;
- the bot can publish a conclusion against a head other than the reviewed head.
