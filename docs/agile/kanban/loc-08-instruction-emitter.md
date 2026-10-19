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
  - Each absolute becomes one line that names its gate.
  - Unsupported declarations are reported, not dropped.
- [ ] Exceeding the budget is a compile error, never a truncation.
- [ ] The adapter writes the artifacts and the manifest; it lives next to
  `emit-agents!`.
- [ ] Provenance markers go in HTML comments, which Claude Code strips before
  injection.

## Non-goals

- Cutting over the live harness files (LOC.10 and LOC.11).
- Daemon fan-out (LOC.12).

## Acceptance criteria

- [ ] GIVEN a profile with budget N and a tier-0 set larger than N WHEN
  projected THEN the build fails and names the fragments and their sizes.
- [ ] GIVEN a projection WHEN its manifest is read THEN it lists the layer
  chain, every fragment id and hash, the target, the budget and the actual
  bytes.
- [ ] GIVEN the same effective contract and profile WHEN projected twice THEN
  both outputs are byte-identical.
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
