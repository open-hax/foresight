---
uuid: "4f263499-a765-4b0f-bac0-7f8175672d60"
title: "Codify revision-bound layered evidence gates"
status: "review"
priority: "P1"
labels: ["quality", "testing", "evidence"]
created_at: "2026-08-29T15:09:34.997Z"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
write-id: "1788038338844-0.l1pwkxe6n99jcvq5gr"
---

# Codify revision-bound layered evidence gates

Make test and review evidence machine-readable, honest about scope, and bound to
the exact revision being promoted. Extend the existing non-Node quality-gate
mapping card rather than guessing child commands.

## Acceptance

- A portable law validates gate kinds, execution modes, result outcomes, unique
  identities, and promotion closure.
- The data-driven catalog cites owning repository manifests/workflows and maps
  representative Clojure, CLJS, multi-surface, and deployment repositories.
- The runner repeats direct-submodule path and checkout identity checks before
  spawning exact argument vectors with no shell interpolation.
- Passed, failed, blocked, unavailable, and explicitly approved
  not-applicable outcomes remain distinguishable.
- Unit, integration, E2E, coverage, security/destructive-path, live-smoke, and
  independent-review expectations are documented by change class.
- Positive and negative probes prove that a real mapped gate can pass and a
  missing checkout/tool cannot produce a green result.

---
Portable evidence laws, exact-command runner, gate catalog, program specification, and positive/negative probes are implemented. Awaiting revision-bound PR review.

Review findings addressed locally: promotion now requires one explicit target revision across every required gate; dirty, unverifiable, or moving child checkouts cannot emit passing revision evidence; list --only rejects unmapped repositories; spawn-error and approved-not-applicable exit paths have regression tests. Root suites pass 46 tests/178 assertions, and Katamorph static passes at 708f1bb6 with the controlled tool PATH. Awaiting exact-head CI and rereview.

Exact-head rereview exposed and local repair now closes three further evidence-boundary gaps: promotion matches results against the trusted catalog snapshot; catalog keys must be actionable direct submodules; and root orchestration cannot enter Knoxx nested packages. Full local matrix passes 52 tests/216 assertions, project/catalog validation, lint, Markdown, Rheos drift, diff hygiene, and Katamorph static at 708f1bb6 with catalog digest 0c904b66. Awaiting publication and fresh exact-head review.

Truthful reusable review authority merged as open-hax/eta-mu@d3937a2f2fa6ecf74cd525c6a0daceb5380a0e1d. Foresight PR #49 is repinned to that immutable merge and supplies the event pull-request head; local law/adapter/contract gates are green. Awaiting fresh exact-head hosted review.

---
