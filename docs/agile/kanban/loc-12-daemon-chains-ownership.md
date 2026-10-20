---
category: "kanban"
labels: "contracts, muse, daemon, ownership, 5sp"
type: "story"
story_id: "LOC.12"
points: "5"
title: "LOC.12 — Re-render descendant layers on ancestor change without letting the stable daemon build dev trees"
dependency: ["loc-layer-chain"]
priority: "P2"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-daemon-chains-ownership"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.12 — Re-render descendant layers on ancestor change without letting the stable daemon build dev trees

> Design: `docs/notes/design/layered-operating-contracts.md` §Risks (daemon crossover), open question 4
> Research: `docs/research/layered-operating-contracts-inventory.md` §4
> Target repository: muse (`src/cljs/eta_mu/domain/daemon.cljc` and the daemon boundary)
> Blocked by: LOC.06 and open question 4

## Context

`plan-actions` works on one tree at a time. Per the muse-ecosystem map, the
stable daemon scans `~/spaces` to depth 3, which reaches the dev tree
`~/spaces/foresight/muse/.ημ`. That tree's `:publish` writes to the global
`~/.config/opencode/`. Once layers exist, a change in `~/.ημ` must re-render
every descendant layer.

## Outcome

- The daemon plans renders for every descendant of a changed layer.
- An explicit ownership rule decides which daemon may build and publish each
  tree, so the stable daemon never builds or globally publishes a dev tree.

## Scope

- [ ] Pure planning in `daemon.cljc`: given a changed layer and the known
  chains, return the affected descendants.
- [ ] An ownership declaration per tree (for example
  `:layer/owner :daemon/stable`), consulted before any `:exec` or `:publish`.
- [ ] Verify the current scan roots and depths against the live daemon log
  before changing them.

## Non-goals

- Reconciling stable and dev muse content (the LOC.01 follow-up).

## Acceptance criteria

- [ ] GIVEN a change to `~/.ημ/contract/` WHEN the daemon plans THEN every
  descendant tree it owns is planned for render, and trees it does not own are
  logged and skipped.
- [ ] GIVEN the dev tree WHEN the stable daemon sees a change in it THEN no
  build or global publish runs, and the skip is logged with its reason.
- [ ] GIVEN a tree whose `:layer/owner` is missing, malformed or names another
  daemon WHEN the daemon plans THEN ownership fails closed. An explicit match
  with the daemon's own identity is required, and otherwise the tree is logged
  and skipped before any `:exec` or `:publish`.
- [ ] Pure planning tests cover fan-out, ownership and debounce merging.

## Verification

```bash
npx shadow-cljs compile test
tail -n 20 ~/.eta-mu/state/daemon/log.jsonl
```

## Risks

A render storm when the root layer changes. Mitigation: debounce and serialise
per working directory, as the daemon already does per tree.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
