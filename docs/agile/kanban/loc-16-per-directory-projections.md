---
category: "kanban"
labels: "contracts, muse, projection, in-repository, 5sp"
type: "story"
story_id: "LOC.16"
points: "5"
title: "LOC.16 — Emit per-directory in-repository projections and refuse overrides a target cannot honour"
dependency: ["loc-layer-chain", "loc-harness-profiles", "loc-instruction-emitter"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-per-directory-projections"
created_at: "2026-10-20T00:30:00.000Z"
---

# LOC.16 — Emit per-directory in-repository projections and refuse overrides a target cannot honour

> Design: `docs/notes/design/layered-operating-contracts.md` §Projection (in-repository overrides), open question 1
> Research: `docs/research/layered-operating-contracts-inventory.md` §7, in-repository band
> Target repository: muse (projection renderer)
> Blocked by: LOC.06, LOC.07 and LOC.08

## Context

LOC.08 emits a projection for one effective contract. A repository whose
subdirectories carry their own `.ημ` layers needs a projection for each layer
directory, so that each harness's session directory selects the right one.
No file-walk harness can remove an ancestor file. An in-repository `:override`
is therefore sound only when a target has verified per-session selection
(LOC.07 `:per-session-selection`).

## Outcome

For each in-repository layer directory, muse emits that directory's
projection.

- It emits pure-union delta files where the target loads by directory walk.
- It emits the session directory's fold through the target's selection hook
  where `:per-session-selection` is true.
- An in-repository `:override` for a target without per-session selection is
  a compile error that names the layer, the fragment and the target.

## Scope

- [ ] Enumerate in-repository layer directories with the LOC.06 chain.
- [ ] Emit per-directory delta files containing only fragments added by that
  layer. No delta may contain an `:override`.
- [ ] For targets with `:per-session-selection`, emit the selection artifact,
  for example a `SessionStart` hook or a muse OpenCode plugin entry, that
  injects the fold for the session's directory. Keep contract fragments out of
  native file-walk loading for that target.
- [ ] Record every emitted file and every selection entry in the projection
  manifest.

## Non-goals

- Global and ancestor bands, which LOC.10 and LOC.11 cut over.
- Daemon fan-out (LOC.12).

## Acceptance criteria

- [ ] GIVEN a repository with a pure-union subdirectory layer WHEN projected
  THEN sessions at the root, at a sibling and in the subdirectory each load
  exactly the union of the layers on their path, in every harness, as checked
  with the LOC.07 fixture method.
- [ ] GIVEN an in-repository `:override` and a target without
  `:per-session-selection` WHEN projected THEN the build fails and names the
  layer, the fragment id and the target.
- [ ] GIVEN an in-repository `:override` and a target with
  `:per-session-selection` WHEN sessions start at the root, the sibling and
  the overriding child THEN:
  - for the overridden fragment id, the root and the sibling contain only the
    ancestor version and the child only the overriding version;
  - every other fragment of the ancestor layers is still present, unchanged, in
    all three sessions.
- [ ] GIVEN any per-directory projection WHEN emitted THEN the LOC.08 emitter
  produces it, and its manifest validates against the LOC.04 manifest schema:
  layer chain, fragment dispositions, scoped `:limits`,
  `:effective-contract/hash` and `:projection/sentinel`. A separate
  implementation that records only file lists does not satisfy this story.

## Verification

```bash
set -euo pipefail
# muse's test build is a node-test target that autoruns and exits nonzero on failure;
# it includes the per-directory projection, override-refusal and manifest-schema tests
npx shadow-cljs compile test
# the LOC.07 fixture runner: three session paths in three harnesses; nonzero on any mismatch
bb fixture/run.bb --harness claude,opencode,codex --paths root,sibling,child
```

## Risks

Selection hooks have size caps: 10,000 characters for Claude Code and 2,500
tokens for Codex. Mitigation: the LOC.08 budget check applies to the injected
fold in the hook's own unit.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
