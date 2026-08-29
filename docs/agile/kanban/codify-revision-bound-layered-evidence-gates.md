---
uuid: "4f263499-a765-4b0f-bac0-7f8175672d60"
title: "Codify revision-bound layered evidence gates"
status: "review"
priority: "P1"
labels: ["quality", "testing", "evidence"]
created_at: "2026-08-29T15:09:34.997Z"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
write-id: "1788017683088-0.zfo6wsuamlsc8nukjc5"
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

---
