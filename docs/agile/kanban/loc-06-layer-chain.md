---
category: "kanban"
labels: "contracts, alpha, katamorph, discovery, 3sp"
type: "story"
story_id: "LOC.06"
points: "3"
title: "LOC.06 — Discover the layer chain for a path and check it is well-formed before use"
dependency: ["loc-contract-shapes"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-layer-chain"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.06 — Discover the layer chain for a path and check it is well-formed before use

> Design: `docs/notes/design/layered-operating-contracts.md` §Layers, open questions 3 and 6
> Target repositories: pure chain logic in katamorph or alpha; the filesystem adapter at the edge
> Blocked by: LOC.04, and open question 3 for module resolution

## Context

Alpha asks whether a thing is well-formed and lawful before it is used. A
layer chain must be checked before anything folds it.

## Outcome

- A pure function returns the ordered chain of `.ημ` layers for a path, from
  `~/.ημ` down to the nearest layer, given a path and the known layer
  directories. A layer marked `:layer/root true` stops the walk.
- A well-formedness check rejects chains with unreadable layers, duplicate
  layer ids or unresolved `:use` references.

## Scope

- [ ] Pure `chain-for` and `well-formed?` in `.cljc`.
- [ ] A filesystem adapter (NBB or bb) that lists `.ημ/contract/layer.edn` along
  the ancestors of a path.
- [ ] Resolution of named modules in the order decided by open question 3.

## Non-goals

- Folding (LOC.05).
- Daemon scheduling (LOC.12).

## Acceptance criteria

- [ ] GIVEN `~/spaces/foresight/muse` WHEN `chain-for` runs THEN it returns the
  layers that exist among `~/.ημ`, `~/spaces/.ημ`, `~/spaces/foresight/.ημ` and
  `~/spaces/foresight/muse/.ημ`, ordered from the root.
- [ ] GIVEN a layer with `:layer/root true` WHEN `chain-for` runs for a path
  below it THEN no layer above it appears in the chain.
- [ ] GIVEN a `:use` reference that does not resolve WHEN `well-formed?` runs
  THEN it fails and names the layer and the reference.

## Verification

```bash
# pure tests in katamorph or alpha; one adapter smoke test against a fixture tree under test/
```

## Risks

Symlinks such as `~/.eta-mu` pointing at `.ημ` can produce duplicate layers.
Mitigation: canonicalise real paths in the adapter and test the symlink case.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
