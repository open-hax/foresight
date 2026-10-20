---
category: "kanban"
labels: "contracts, muse, projection, 5sp"
type: "story"
story_id: "LOC.08"
points: "5"
title: "LOC.08 — Emit budgeted instruction projections with manifests from muse"
dependency: ["loc-contract-0-golden-artifacts", "loc-fold-laws", "loc-harness-profiles"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-instruction-emitter"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.08 — Emit budgeted instruction projections with manifests from muse

> Design: `docs/notes/design/layered-operating-contracts.md` §Projection
> Research: `docs/research/layered-operating-contracts-inventory.md` §4 and §7
> Target repository: muse (compatibility compiler)
> Blocked by: LOC.02, LOC.05 and LOC.07

## Context

Muse already publishes Markdown through `emit-agents!`
(`src/clj/eta_mu/opencode/build.clj:159`) and MCP config through
`claude/build.clj:155`. Nothing emits instructions.

## Outcome

A `:publish {:instructions …}` target projects an effective contract into
harness instruction artifacts, according to the LOC.07 profiles. Each
projection writes a manifest, and a receipt references it.

## Scope

- [ ] Pure projection (`.cljc`) from an effective contract and a profile to the
  artifact text and a manifest.
  - Tier-0 fragments are inlined.
  - Tier-1 fragments become pointers or skills.
  - Absolutes project by surface. `:pr-gate` becomes one line plus its gate,
    `:runtime-hook` becomes a hook or full prose, and `:interactive` and
    `:gap` keep full prose.
  - Unsupported declarations are reported, not dropped.
- [ ] Exceeding the budget is a compile error, never a truncation.
- [ ] The adapter writes the artifacts and the manifest; it lives next to
  `emit-agents!`.
- [ ] Provenance markers go in HTML comments, which Claude Code strips before
  injection.
- [ ] Each artifact ends with a visible `contract-sentinel: <nonce>` line. The
  nonce is random per build and recorded as `:projection/sentinel` in the
  manifest, together with `:effective-contract/hash`. A harness that can
  echo the nonce loaded the file through its end, so the file was not
  truncated.

## Non-goals

- Cutting over the live harness files (LOC.10 and LOC.11).
- Daemon fan-out (LOC.12).

## Acceptance criteria

- [ ] GIVEN a profile with budget N and a tier-0 set larger than N WHEN
  projected THEN the build fails and names the fragments and their sizes.
- [ ] GIVEN a projection WHEN its manifest is read THEN it lists the layer
  chain, every fragment id and hash, the target, and one `:limits` entry per
  profile limit: `{:unit :scope :max :actual}`. `:actual` is measured in that
  limit's own unit:
  - bytes as UTF-8 length;
  - characters by the counting method the LOC.07 profile records and verifies
    against the harness;
  - tokens by the harness tokenizer, or by a documented estimator with a
    stated safety margin.
- [ ] GIVEN a limit whose `:actual` exceeds `:max` in its own unit WHEN
  projected THEN the build fails and names the limit, its unit and scope, and
  both numbers. A byte count is never substituted for a character or token
  limit.
- [ ] GIVEN the same effective contract and profile WHEN projected twice THEN
  the outputs are byte-identical once the final `contract-sentinel:` line is
  removed, and the manifests are equal except for `:projection/sentinel`.
  That exclusion is the only normalisation. The nonce stays random per build,
  because LOC.11's evidence depends on it being impossible to guess.
- [ ] GIVEN a tier-1 fragment and a profile WHEN projected THEN the artifact
  contains a pointer or skill reference for it, as the profile allows, and the
  manifest marks it `:included`.
- [ ] GIVEN absolutes of each surface WHEN projected THEN:
  - each `:pr-gate` absolute appears as one line naming its gate;
  - each `:interactive` and each `:gap` absolute appears in full,
    uncompressed;
  - each `:runtime-hook` absolute appears as a hook where the profile supports
    one, and in full otherwise.
- [ ] GIVEN a declaration the target cannot carry WHEN projected THEN the build
  reports it, and the manifest marks it `:unsupported` with the missing
  capability. It is never dropped silently.
- [ ] GIVEN a profile whose budget applies across a chain (Codex) WHEN
  projected THEN the check sums the whole emitted chain.
- [ ] GIVEN the contract-0 golden test WHEN this story's code lands THEN the
  test still passes.

## Verification

```bash
npx shadow-cljs compile test   # muse test build: projection unit tests plus the contract-0 golden test
```

## Risks

Coupling to OpenCode build internals. Mitigation: keep the projection pure and
shared, with a thin adapter per target.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
