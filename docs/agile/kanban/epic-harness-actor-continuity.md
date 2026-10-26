---
category: "kanban"
labels: "muse, knoxx, codex, hermes, federation, epic"
type: "epic"
write-id: "1792907536480-0.6nih56mx7stjlmna3vl"
points: "32"
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
- HAC.03: `hac-03-codex-lifecycle` — Connect Codex contexts and native task messages to Knoxx (8 points; requires bounded qualification breakdown before readiness).
- HAC.04: `hac-04-hermes-orbit` — Qualify Hermes and Orbit as another contract consumer (3 points).
- HAC.05: `hac-05-federation` — Prove actor continuity between local and remote Knoxx (5 points).
- HAC.06: `hac-06-layered-tool-projection` — layered tools/instructions agreement (3 points), depends on HAC.01 and LOC.05/06/07/08.
- HAC.07: `hac-07-memory-promotion` — online authorization and revocation qualification (5 points), depends on HAC.02 and HAC.05.

## Non-goals

No inferred equivalence of principals, silent memory sharing, production
activation or replacement of native harness scheduling.

## Acceptance criteria

- [ ] Every child completes planning review and lawful Rheos admission before implementation begins.
- [ ] Every child supplies implementation evidence before completion.
- [ ] Codex tool parity and actor integration are independently demonstrated.
- [ ] Hermes/Orbit supplies explicit HAC.04 evidence that it consumes the selected contract and preserves actor and message boundaries, alongside capability and deployment evidence. Health checks alone do not qualify a contract consumer.
- [ ] Local/remote identity and memory-boundary negative tests pass.

Each child must supply an evidence receipt mapped to its acceptance criteria, recording selected contract hash (explicitly not applicable for HAC.01 single-tree qualification), harness/runtime version, actor/destination identities where applicable, exact commands and positive/negative results. Codex actor integration specifically consumes HAC.03 native parent/child request/reply correlation, retry without duplicate effects and cancellation evidence; registration/binding alone cannot complete it. Unknown Codex CLI/SDK availability and missing lifecycle hooks gate qualification on versioned capability probes (unsupported paths cannot satisfy required positive routes); remote recovery gates on HAC.05 durable recovery evidence, and grant revocation gates on HAC.02/03/05/07 independent denial and race evidence. Preserve the existing host capability drift risk.

## Verification

Review the evidence delivered by each child through the owning project's test
and PR process. This epic is a proposed Markdown input with incoming metadata.

## Risks

Host capabilities evolve; the compatibility matrix must bind measured versions.

## References

- [Research](../../research/harness-actor-continuity.md)
- [Design](../../notes/design/harness-actor-continuity.md)
- [Layered-contract epic](epic-layered-operating-contracts.md)
