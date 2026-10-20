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
- [ ] Instruction fragments: union keyed by id. The nearest layer wins only
  with `:override`; an unmarked collision is an error. Text is never compared
  for similarity.
  - **Meaning is the set.** The effective contract's meaning is the set of
    fragment ids and hashes.
  - **Canonical order.** Order is defined as layer depth (root first), then
    fragment id. It applies wherever muse emits a single artifact.
  - **Order independence.** Harnesses that compose several files use their
    own order: Claude Code and Codex root first, OpenCode nearest first. Every
    fragment therefore has to be order-independent. A fragment may not
    restate, qualify, narrow or contradict another fragment's subject. A change
    to an ancestor's meaning must be an `:override`.
- [ ] Absolutes: a descendant layer cannot drop or override them; an attempt is
  an error.
- [ ] Permissions: a child may only tighten them; a loosening is an error.
  - **Shape:** a permission set maps exact keys (tool or action names; glob
    keys are compared literally, not by matching) to a level, plus a
    `:default` level.
  - **Order:** `:deny` < `:ask` < `:allow`.
  - **Effective level of a key in the folded child:**
    1. the child's own entry for the key, if present;
    2. otherwise, if the parent has an explicit entry or an inherited level
       for the key, that parent level is **inherited**;
    3. only otherwise, the child's `:default`.

    The child's `:default` never replaces a level the parent set for that key.
  - **Tightening:** a child tightens when, for every key in either set and for
    `:default`, its effective level is at most the parent's effective level.
  - **Boundary cases:**
    - omitting a key inherits the parent's level, which is valid;
    - adding a new key at or below the parent's `:default` is valid;
    - adding a key above the parent's `:default` is a loosening;
    - raising `:default` is a loosening;
    - an unknown level is an error.
- [ ] Overrides: an `(override :of L :hash H)` fragment replaces the ancestor
  version only when layer `L` declares that id and its current hash is `H`.
  A missing `L` or a different hash is a `:stale-override` error.
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
  including the canonical fragment order (determinism).
- [ ] GIVEN a child layer that drops an absolute or loosens a permission WHEN
  folded THEN the fold returns an error naming both layers and the declaration.
- [ ] GIVEN two layers that define the same fragment id without `:override`
  WHEN folded THEN the fold returns an error. With `:override` the nearest
  layer wins, and the manifest records the shadowed id and hash.
- [ ] GIVEN generated parent and child permission sets WHEN folded THEN the
  fold succeeds exactly when the child is below or equal to the parent in the
  order above, including the boundary cases.
- [ ] GIVEN a parent `{"tool" :deny, :default :allow}` and a child
  `{:default :ask}` WHEN folded THEN `"tool"` stays `:deny` (inherited, not the
  child's default) and the fold succeeds. GIVEN a child `{"tool" :allow}`
  under the same parent THEN the fold fails as a loosening.
- [ ] GIVEN an override whose `:hash` differs from the ancestor's current hash
  WHEN folded THEN the fold returns `:stale-override`, naming both hashes.
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
