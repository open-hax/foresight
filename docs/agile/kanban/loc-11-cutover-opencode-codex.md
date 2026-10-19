---
category: "kanban"
labels: "contracts, muse, opencode, codex, cutover, 3sp"
type: "story"
story_id: "LOC.11"
points: "3"
title: "LOC.11 — Cut OpenCode and Codex over to compiled projections of the same contract"
dependency: ["loc-fragment-source", "loc-harness-profiles", "loc-instruction-emitter"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-cutover-opencode-codex"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.11 — Cut OpenCode and Codex over to compiled projections of the same contract

> Design: `docs/notes/design/layered-operating-contracts.md` §Projection
> Research: `docs/research/layered-operating-contracts-inventory.md` §3 and §5 (OpenCode, Codex)
> Target: the `~/.ημ` root layer (`config/opencode/settings/core.edn`) and the muse projection
> Blocked by: LOC.03, LOC.07 (budgets and profiles) and LOC.08

## Context

- OpenCode loads lineage A through the `instructions` glob at
  `~/.ημ/config/opencode/settings/core.edn:37-38`, plus a separate 11.6 KB
  `~/.config/opencode/AGENTS.md`.
- Codex loads `~/.codex/AGENTS.md`, a lineage-B render. Codex truncates project
  documents at 32 KiB and never reads above the git root.

## Outcome

OpenCode and Codex load projections of the same effective contract as Claude
Code, within their budgets. Each projection has a manifest and a receipt.

## Scope

- [ ] Replace the OpenCode `instructions` glob with the compiled projection
  path. The daemon renders `opencode.jsonc` from the root layer.
- [ ] Reconcile `~/.config/opencode/AGENTS.md` ("general agent behaviors"):
  either fold it into fragments or keep it as an explicit OpenCode-only layer
  fragment.
- [ ] Publish `~/.codex/AGENTS.md` as a projection.
- [ ] Measure before and after for both harnesses.

## Non-goals

- In-repository delta files, which follow the open-question-1 decision in
  LOC.07.

## Acceptance criteria

- [ ] GIVEN a new OpenCode session WHEN its instructions are listed THEN no
  `ημΠ.dev` file is present, and the projection appears once.
- [ ] GIVEN a new Codex session WHEN its instructions are loaded THEN the
  projection is under the profile budget and is not truncated.
- [ ] GIVEN the three harness projections WHEN their manifests are compared
  THEN all three reference the same effective-contract hash.

## Verification

```bash
grep -n instructions ~/.config/opencode/opencode.jsonc
wc -c ~/.codex/AGENTS.md
```

## Risks

OpenCode silently ignores missing instruction files. Mitigation: the
acceptance criteria check presence in the session, not only in config.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
