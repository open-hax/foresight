---
slug: probe-four-capabilities-as-typed-steps
title: "Probe: the four capabilities as typed steps"
kind: finding
status: draft
card: "32b122a8-bbd3-4e14-85bd-7c3c6369f733"
description: "Writes Repository -> Transduction -> Evaluation -> Representation as Katamorph step data. The cut holds structurally; four things the step vocabulary cannot say; and the whole vocabulary has no path to katamorph main."
created: "2026-08-14"
labels: [capabilities, katamorph, workflow, typing, probe, finding]
---

# Probe: the four capabilities as typed steps

Card `32b122a8`. This builds nothing. It writes the existing
CMS → translation → SME review → SSR flow as Katamorph actions and steps, using
the real contracts in the tree, and reports what the type system cannot say.

Data: `docs/architecture/probes/four-capability-steps.edn`.

## Headline

**The cut holds.** All four actions validate against `ActionSemantics` and all
four steps against `ActionStep`. Nothing landed in two boxes; every
responsibility in the existing flow belongs to exactly one of the four
categories. That is the result the decomposition needed.

**The vocabulary does not carry it yet.** Five things the flow requires were
probed mechanically. One is a merge gap. Four are design gaps, and none of them
is addressed by any open Katamorph PR.

## The blocker found on the way

`katamorph` **main has no `schema/action.cljc` and no `schema/step.cljc`.**

PR #6 — typed action ports and step dataflow — was merged into
`refactor/schema-core-cljc-v2` rather than into `main`. That branch had already
reached main through PR #5, so merging #6 into it put the work on a branch that
is now 7 commits ahead of main **with no open PR to main**. The rest of the
stack chains off it:

```text
main
 └── refactor/schema-core-cljc-v2   #6 merged here · 7 ahead · NO PR to main
      └── #8   feat/workflow-composition-law      (open)
           └── #10  feat/workflow-schema-compatibility  (open)
                └── #11  feat/action-registry-cljc-v3   (open)
```

The only open Katamorph PR whose base is `main` is #4, an unrelated lint chore.
So the entire workflow vocabulary — action ports, step dataflow, composition
validation, structural port compatibility, and the action registry — currently
has no path to `main`.

This probe therefore ran against `refactor/schema-core-cljc-v2` in a scratch
worktree, so the submodule pointer was not moved.

**One-line fix: open a PR from `refactor/schema-core-cljc-v2` to `main`.**

## Findings

### 1. Port values are untyped — solved upstream, not merged

`PortMap` is `[:map-of keyword? :any]`, so `:action/requires` and
`:action/provides` name ports without typing them. Wiring
`:view <- [:step :resolve :garden]` — a `Garden` into a port requiring a view
map — still validates.

This is genuinely solved: #8 introduces `compatible-contract?` behind a seam and
#10 replaces exact equality with a structural relation. Both are open, on the
stranded branch. **Merge gap, not a design gap.**

### 2. An edge cannot be conditional — missing everywhere

Representation must not run on a rejected or deferred review. That is a guard on
an edge, and `ActionStep` has no `:step/when`, no condition, and no guard. No
open PR adds one.

`katamorph.condition` (#7) **is** merged to main and could express the predicate
perfectly well. Nothing in the step vocabulary consumes it.

The sharp part: `ActionStep` is `{:closed false}`, so a hand-written
`:step/when` **validates** while carrying no meaning and having no interpreter.
It looks like it works. This is the same class of mistake as the publication
epic's green suites — the schema says yes to something nothing will ever act on.

This is also exactly the publication-gate semantics that Knoxx #234 built and
nothing called.

### 3. There is no provider-substitution seam — missing everywhere

`:step/action` binds a concrete action id. There is no way to say *any action
whose category is `:repository` and whose `provides` satisfies `Document`*.

PR #11's `katamorph.action.registry` gets the hard half right: it bans runtime
bindings (`:action/fn`, `:action/handler`, `:action/implementation`) from the
semantic registry, so contract and implementation are properly separated. But:

- `compose` **fails closed on duplicate action ids** (`:action-id-conflict`), so
  two registries offering `:repository/resolve-document` is an error rather than
  a choice;
- `resolve-action` is `get` by id;
- no second registry for runtime bindings is defined anywhere yet.

So the seam that lets a client substitute Optimizely for the EDN provider
without editing steps **exists nowhere** — and it is the headline promise of the
entire four-way decomposition. This is the most important finding in the probe.

### 4. A port cannot take N artifacts of one role — missing everywhere

`StepInputs` is `[:map-of keyword? StepOutputRef]`: exactly one ref per key. A
vector of refs is rejected.

The Evaluation ontology's Review Case takes artifacts by semantic role —
`:source`, `:candidate`, `:reference`, `:previous`, `:evidence` — and `:evidence`
is naturally a collection. Today each artifact must be a distinct key fixed at
authoring time.

### 5. A durable fact is indistinguishable from a value — missing everywhere

`:receipt` and `:judgment` both sit in `:action/provides` as ordinary ports. The
only signal that one is a durable fact is the trait keyword `:produces-receipt`,
and `:action/traits` is `[:set keyword?]` with no registry and no assigned
meaning. Nothing prevents a step from dropping the receipt entirely.

## What this changes for the capability cards

- **`04eef2df` Evaluation** — finding 4 is a direct constraint. Either the Review
  Case is passed as one `:case` port carrying its artifact collection internally
  (probably right, and keeps roles inside the ontology where they belong), or the
  step layer grows collection-valued ports. Decide this in the card, not later.
- **`61be68be` Repository** and **`4371952f` Transduction** — finding 3 is a
  shared blocker. Both are provider-boundary cards and the substitution seam does
  not exist. Their "two providers, one contract" acceptance criteria currently
  have nowhere to live above the capability itself.
- **`1b9aaa0a` Representation** — finding 2. The one edge into representation is
  the one that must be conditional.
- **`57a07d75` housing decision** — findings 2, 3 and 4 are all **Katamorph-owned**,
  not capability-owned. The capabilities cannot fix them from outside. That is a
  real input to the dependency-direction question.

## Next facets, named not designed

Per the card's non-goals, these are named only:

1. a **guard facet** on the step edge, consuming the existing condition kernel;
2. a **capability-resolution facet** separating *which operation* from *which
   implementation*, plus the runtime-binding registry #11 implies but does not define;
3. **collection-valued ports**;
4. a **durable-fact port marking** that a step cannot silently drop.

Item 2 is the one that decides whether the decomposition delivers what it exists
to deliver.

## Reproducing

The probe runner is not committed: it depends on an unmerged Katamorph branch,
and a runner that cannot run on `main` would be its own small lie. To rebuild it,
worktree `refactor/schema-core-cljc-v2`, put it on the classpath with malli, and
validate the committed EDN against `ActionSemantics` / `ActionStep`. Once the
branch reaches main, this belongs in `alpha/test` as a real portability check.
