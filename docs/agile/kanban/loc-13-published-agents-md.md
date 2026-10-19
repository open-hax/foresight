---
category: "kanban"
labels: "contracts, muse, publication, adoption, 3sp"
type: "story"
story_id: "LOC.13"
points: "3"
title: "LOC.13 — Publish a standalone AGENTS.md projection that adopters can use without ~/.ημ"
dependency: ["loc-instruction-emitter"]
priority: "P2"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-published-agents-md"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.13 — Publish a standalone AGENTS.md projection that adopters can use without ~/.ημ

> Design: `docs/notes/design/layered-operating-contracts.md` §Layers (`:layer/root`), open question 2
> Research: `docs/research/layered-operating-contracts-inventory.md` §7
> Target repository: muse (projection), applied per published repository
> Blocked by: LOC.08 and open question 2

## Context

The user wants repositories to publish `AGENTS.md` so that others can adopt as
much of the system as they want. An outside adopter has no `~/.ημ`. On this
machine the same file would duplicate its ancestor layers.

## Outcome

A published-repository profile projects a standalone `AGENTS.md`: tier 0 of the
effective contract, plus pointers of the form `contract-id@hash` to tier-1
material. A layer marked `:layer/root true` produces a repository whose
contract does not depend on private ancestors.

## Scope

- [ ] A published-repository profile, with its budget.
- [ ] Pointer format and resolution notes for adopters.
- [ ] A local de-duplication rule, so that this machine's harnesses do not load
  the published file in addition to their own projection.

## Non-goals

- Rewriting every repository's `AGENTS.md` in this story. Pilot it on one
  repository.

## Acceptance criteria

- [ ] GIVEN a fresh clone on a machine with no `~/.ημ` WHEN `AGENTS.md` is read
  THEN it is self-contained for tier 0 and every pointer resolves to a
  published location.
- [ ] GIVEN this machine WHEN a harness starts in the pilot repository THEN the
  contract is not loaded twice.

## Verification

```bash
git clone --depth 1 <pilot-repo> /tmp/adopter && wc -c /tmp/adopter/AGENTS.md
```

## Risks

Pointers rot when tier-1 material moves. Mitigation: pointers carry content
hashes, and the alpha well-formedness check covers them.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
