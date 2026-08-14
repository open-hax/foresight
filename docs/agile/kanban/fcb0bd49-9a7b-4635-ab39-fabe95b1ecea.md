---
category: "architecture"
labels: ["alpha", "mu", "operations", "workflow", "typing"]
points: "5"
title: "Give operations requires/provides/category/traits and type-check a graph"
priority: "P1"
status: "incoming"
uuid: "fcb0bd49-9a7b-4635-ab39-fabe95b1ecea"
created_at: "2026-08-13T18:00:00Z"
---

# Give operations requires/provides/category/traits and type-check a graph

Alpha has `OperationRef` — an id plus portable `:operation/with` and
`:operation/in` maps, with a law that the id must be registered. What it does
not have is what the operation *means*: what it consumes, what it produces, and
what class of thing it is.

The design is already settled across three notes and does not need reopening:

- No new `StepContract`. Katamorph's `WorkflowStep` / `WorkflowJob` /
  `WorkflowContract` from eta-mu #181 is the composition substrate; the missing
  layer sits *underneath* it on the action/operation.
- `:step/with` stays configuration. `:step/in` is new and carries dataflow.
- The reference grammar is `[:step <step-id> <output-id>]` — boring EDN, no
  expression language, room for `[:workflow :input _]`, `[:trigger :payload]`,
  `[:literal _]` later.
- No `:step/out` until an aliasing use case actually demands it.
- Untyped steps stay legal. `{:step/run "pnpm lint"}` remains an opaque effect;
  274 CI steps do not get migrated to earn this.

## Acceptance

- `:operation/requires`, `:operation/provides`, `:operation/category`,
  `:operation/traits` exist as portable Alpha shapes.
- A validator decides composition structurally: `A : X → Y` then `B : Y → Z`
  composes; `A : X → Y` then `C : Q → Z` is refused with a structured finding
  naming the unsatisfied facet.
- Compatibility is facet satisfaction, not equality — a producer carrying extra
  keys still satisfies a consumer that requires a subset.
- An end-to-end fake graph type-checks and runs through stub providers:
  `repository/read → transduction → evaluation → representation →
  repository/write`. No HTTP, no MCP, no UI, no Knoxx dependency.
- Categories are open classification, not a closed enum that has to be amended
  before anyone can name a new kind of operation.

---
This is the card that makes the α → η → μ → Π spine executable instead of
diagrammatic. Everything in `docs/architecture/workflows/alpha-eta-mu-pi.mmd`
is currently a picture with no code behind it.
