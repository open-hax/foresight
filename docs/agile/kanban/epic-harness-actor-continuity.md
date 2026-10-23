---
category: "kanban"
labels: "muse, knoxx, codex, hermes, federation, epic"
type: "epic"
points: "21"
title: "EPIC: Shared Muse tools and scoped Knoxx actors across harness contexts"
priority: "P1"
status: "incoming"
uuid: "harness-actor-continuity"
---

# Shared Muse tools and scoped Knoxx actors across harness contexts

## Context

Extends merged PR 141. Research and design are linked below. The earlier merge
has unresolved review-coverage evidence; it does not prove implementation.

## Outcome

Each supported harness consumes the selected layered contract and shared tools,
with a distinct Knoxx actor for its context and explicit local/remote grants.

## Scope and children

- HAC.01: `hac-01-codex-tools` — Compile and qualify the shared Muse tools in Codex (3 points).
- HAC.02: `hac-02-actor-bindings` — Specify portable harness actor bindings and message admission (5 points).
- HAC.03: `hac-03-codex-lifecycle` — Connect Codex contexts and native task messages to Knoxx (5 points).
- HAC.04: `hac-04-hermes-orbit` — Qualify Hermes and Orbit as another contract consumer (3 points).
- HAC.05: `hac-05-federation` — Prove actor continuity between local and remote Knoxx (5 points).

## Non-goals

No inferred equivalence of principals, silent memory sharing, production
activation or replacement of native harness scheduling.

## Acceptance criteria

- [ ] Every child has implementation evidence and lawful Rheos admission before implementation begins.
- [ ] Codex tool parity and actor integration are independently demonstrated.
- [ ] Hermes/Orbit capability and deployment evidence is recorded.
- [ ] Local/remote identity and memory-boundary negative tests pass.

## Verification

Review the evidence delivered by each child through the owning project's test
and PR process. This epic is a proposed Markdown input with incoming metadata.

## Risks

Host capabilities evolve; the compatibility matrix must bind measured versions.

## References

- [Research](../../research/harness-actor-continuity.md)
- [Design](../../notes/design/harness-actor-continuity.md)
- [Layered-contract epic](epic-layered-operating-contracts.md)
