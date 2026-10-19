---
category: "kanban"
labels: "contracts, muse, harness, decision, 3sp"
type: "story"
story_id: "LOC.07"
points: "3"
title: "LOC.07 — Declare harness capability profiles and budgets, and decide who performs the fold"
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-harness-profiles"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.07 — Declare harness capability profiles and budgets, and decide who performs the fold

> Design: `docs/notes/design/layered-operating-contracts.md` §Projection, open question 1
> Research: `docs/research/layered-operating-contracts-inventory.md` §5 and §7
> Target repository: muse (profile data), with the decision recorded in the design note
> Blocked by: open question 1

## Context

Whether each harness composes layer deltas through its own directory walk, or
receives one folded projection from muse, depends on its loading semantics,
exclusion mechanism and injection hook. Research section 5 records those.

## Outcome

- A profile data file defines one profile each for Claude Code, OpenCode and
  Codex. A profile records:
  - inline tiers;
  - on-demand channels (skills, pointer files, MCP resources);
  - whether the harness can exclude its own ancestor files, and how;
  - its injection hook;
  - its byte budget.
- The design note records the open-question-1 decision for each harness.

## Scope

- [ ] Build a fixture tree with marked instruction files in `~`, at a git root
  and in a subdirectory. Record the actual load order and walk boundary in the
  installed Claude Code, OpenCode and Codex. This settles the OpenCode
  docs-versus-source conflict in research section 5.
- [ ] Profiles as EDN, grounded in cited research and the fixture results.
- [ ] Evaluate the three-band hybrid from research section 7 against options A
  and B.
- [ ] Budgets set from the LOC.03 tier-0 baseline, with headroom stated.
- [ ] The decision table for open question 1, per harness.

## Non-goals

- Emitting artifacts (LOC.08).

## Acceptance criteria

- [ ] GIVEN each profile field WHEN reviewed THEN it cites a research-note
  source, or is marked UNKNOWN with a verification step.
- [ ] GIVEN the tier-0 baseline WHEN compared with each budget THEN the
  baseline fits, or the overflow is reported as a finding.

## Verification

```bash
bb -e '(clojure.edn/read-string (slurp "profiles/harness.edn"))'   # parses
```

## Risks

Harness behaviour changes between versions. Mitigation: profiles record the
harness version they were verified against.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
