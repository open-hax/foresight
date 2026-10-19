---
category: "kanban"
labels: "contracts, muse, claude-code, cutover, context-budget, 3sp"
type: "story"
story_id: "LOC.10"
points: "3"
title: "LOC.10 — Cut Claude Code over to the compiled projection and remove the double load"
dependency: ["loc-fragment-source", "loc-harness-profiles", "loc-instruction-emitter"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-cutover-claude-code"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.10 — Cut Claude Code over to the compiled projection and remove the double load

> Design: `docs/notes/design/layered-operating-contracts.md` §Projection
> Research: `docs/research/layered-operating-contracts-inventory.md` §3 and §5 (Claude Code)
> Target: the `~/.claude` device repository plus muse publication
> Blocked by: LOC.03, LOC.07 (budgets and profiles) and LOC.08

## Context

- `~/.claude/CLAUDE.md` is a 12.3 KB hand-made render of lineage B.
- A session started in `~` also loads `~/AGENTS.md`, the Codex render.
- Claude Code walks to the filesystem root. `claudeMdExcludes` and the
  AGENTS.md mode can suppress duplicates.

## Outcome

- `~/.claude/CLAUDE.md` is a muse projection that carries its manifest
  reference in an HTML comment.
- No Claude Code session loads the contract twice.
- Always-on contract bytes are within the LOC.07 budget.

## Scope

- [ ] Measure the always-on instruction bytes before and after. Use `/context`
  or an equivalent, in sessions started in `~` and in `~/spaces/foresight`.
- [ ] Publish the projection to `~/.claude/CLAUDE.md` and keep the existing
  harness-only sections (scratchpad hygiene, Rheos pointer) as their own
  fragments.
- [ ] Prevent the double load with the projection's settings fragment
  (`claudeMdExcludes` or the AGENTS.md mode), owned by muse.
- [ ] Commit to the `~/.claude` repository on `device/stealth`, staging explicit
  paths only, as its `AGENTS.md` requires.

## Non-goals

- Changing the content of project `AGENTS.md` files (LOC.13).

## Acceptance criteria

- [ ] GIVEN a session started in `~` WHEN context is inspected THEN exactly one
  copy of the contract is loaded.
- [ ] GIVEN a session started in `~/spaces/foresight` WHEN context is
  inspected THEN exactly one copy of the contract is loaded there too. Claude
  Code's discovery depends on the ancestry of the working directory, so the
  `~` result does not establish this case.
- [ ] GIVEN the before and after measurements WHEN compared THEN always-on
  contract bytes are within budget, and both measurements are recorded in a
  receipt.
- [ ] GIVEN the new `CLAUDE.md` WHEN Claude Code starts THEN it starts cleanly,
  which is the `~/.claude` definition of done.

## Verification

```bash
git -C ~/.claude diff --stat
claude --version && claude -p "say ok" >/dev/null && echo starts
```

## Risks

Tier-0 trimming can change agent behaviour. Mitigation: compare one recorded
session before and after on the same task, and roll back by publishing the
previous projection.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
