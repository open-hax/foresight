---
category: "kanban"
labels: "contracts, pr-flow, gates, 2sp"
type: "story"
story_id: "LOC.14"
points: "2"
title: "LOC.14 — Map every absolute to the PR gate that enforces it, or record the gap"
dependency: ["loc-fragment-source"]
priority: "P2"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-absolutes-to-gates"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.14 — Map every absolute to the PR gate that enforces it, or record the gap

> Design: `docs/notes/design/layered-operating-contracts.md` §The fold (absolutes), §Projection
> Target: `~/.ημ/contract` fragments plus the existing pr-flow and eta-mu review gates
> Blocked by: LOC.03

## Context

The user's rule is that anything absolute is enforced at the workflow layer, in
pull requests. Restating absolutes in full prose costs context in every
session.

## Outcome

Every fragment marked `(absolute)` names its enforcing gate, for example a
pr-flow law, a required check or an eta-mu review gate. A fragment with no
gate is listed as a gap. The projection emits each absolute as one line plus
its gate reference.

## Scope

- [ ] An inventory of absolutes in the LOC.03 fragments: the immutable
  directives, safety, license and ledger discipline.
- [ ] For each absolute, the existing gate that enforces it, cited by file and
  line, or a gap entry.
- [ ] A follow-up card for each gap. This story does not implement gates.

## Non-goals

- Writing new gates.

## Acceptance criteria

- [ ] GIVEN the fragment set WHEN filtered for absolutes THEN each one has
  either a `(gate …)` reference that resolves to an existing gate, or a gap
  entry linked to a follow-up card.

## Verification

```bash
grep -rn "(absolute)" ~/.ημ/contract/fragments | wc -l
grep -rn "(gate " ~/.ημ/contract/fragments | wc -l
```

## Risks

Some absolutes cannot be enforced as gates, for example "no faux feelings".
Mitigation: those stay as tier-0 prose and are listed as prose-only absolutes.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
