---
category: "kanban"
labels: "contracts, katamorph, laws, cljc, property-tests, 5sp"
type: "story"
story_id: "LOC.05"
points: "5"
title: "LOC.05 — Fold a layer chain into an effective contract with one merge rule per kind of declaration"
dependency: ["loc-contract-shapes"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-fold-laws"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.05 — Fold a layer chain into an effective contract with one merge rule per kind of declaration

> Design: `docs/notes/design/layered-operating-contracts.md` §The fold
> Target repository: katamorph, provisional pending open question 6
> Blocked by: LOC.04, open question 6, and open question 3 (module resolution order) for the `:use`/`:drop` scope item

## Context

`deep-merge` exists in `muse/src/cljs/eta_mu/opencode/settings.cljc` but
applies only to settings, inside one tree. Contract text needs identity-based
merging, absolutes need to be non-removable, and permissions may only tighten.

## Outcome

A pure function folds an ordered layer chain into an effective contract or
returns explained errors. Each kind of declaration has its own merge rule.

The API is defined as a left fold.

- `(extend effective layer)` applies one layer to an effective contract.
- `(fold chain)` is `(reduce extend empty-contract chain)`.

The only composition operation is `extend`, applied to an effective contract
and a layer. Effective contracts are never passed back to `fold` as layers.

## Scope

- [ ] Settings: `deep-merge`, reused or ported unchanged.
- [ ] Instruction fragments: ordered union keyed by id. The nearest layer wins
  only with `:override`; an unmarked collision is an error. Text is never
  compared for similarity.
- [ ] Absolutes: a descendant layer cannot drop or override them; an attempt is
  an error.
- [ ] Permissions: a child may only tighten them; a loosening is an error.
- [ ] Tools and hooks: union, then the profile filter.
- [ ] Modes and participants: keyed merge.
- [ ] `:use` and `:drop` resolution against the module library (the order
  follows open question 3).

## Non-goals

- Discovering layers on disk (LOC.06).
- Projection (LOC.08).

## Acceptance criteria

- [ ] GIVEN any chain `c` and layer `l` WHEN `(fold (conj c l))` and
  `(extend (fold c) l)` are compared THEN they are equal (the left-fold law).
- [ ] GIVEN any effective contract `e` WHEN extended with an empty layer THEN
  the result equals `e` (identity).
- [ ] GIVEN the same chain WHEN folded twice THEN the results are equal,
  including manifest ordering (determinism).
- [ ] GIVEN a child layer that drops an absolute or loosens a permission WHEN
  folded THEN the fold returns an error naming both layers and the declaration.
- [ ] GIVEN two layers that define the same fragment id without `:override`
  WHEN folded THEN the fold returns an error. With `:override` the nearest
  layer wins, and the manifest records the shadowed id and hash.
- [ ] Generative property tests cover each of the criteria above.

## Verification

```bash
# katamorph's documented test gate, including the generative property suite
```

## Risks

`:drop` can resolve differently depending on which modules are already in
the effective contract. Mitigation: `extend` resolves `:drop` against the
effective contract's folded `:use` set, and a property test checks it under
the left-fold law.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
