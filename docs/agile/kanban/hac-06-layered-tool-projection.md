---
category: "kanban"
labels: "muse, knoxx, harness, 3sp"
type: "story"
story_id: "HAC.06"
points: "3"
title: "HAC.06 — Project one layered contract into tools and instructions"
priority: "P1"
status: "incoming"
uuid: "hac-06-layered-tool-projection"
epic: "harness-actor-continuity"
parent: "harness-actor-continuity"
dependency: ["hac-01-codex-tools", "loc-layer-chain", "loc-fold-laws", "loc-harness-profiles", "loc-instruction-emitter"]
---

# Project one layered contract into tools and instructions

> Design: [harness actor continuity](../../notes/design/harness-actor-continuity.md)
> Research: [grounding](../../research/harness-actor-continuity.md)

## Context

Planning extends PR141; its LOC work is not implemented by the merge.

## Outcome

Project one layered contract into tools and instructions.

## Scope

Use the LOC fold and manifest to drive the existing shared tool compiler; preserve standalone MCP qualification from HAC.01.

## Non-goals

No production activation or alternate board/event authority.

## Acceptance criteria

- [ ] GIVEN a path and pinned selected layer and profile revisions plus the module-library revision or content digest WHEN tools and instructions compile THEN each projection is independently validated against the canonical effective contract reconstructed from the LOC.06 ordered, well-formed layer chain and the LOC.07 target profile at those pinned revisions using the existing LOC.04 canonicalization and LOC.05/08 fold/hash algorithm with that exact pinned module-library input for :use/:drop resolution. Each receipt records its own recomputation, all layer/profile revisions, the module-library revision or content digest and projection validation; both manifest hashes must equal that independently computed hash. Copying a hash between manifests alone is insufficient, and no competing hash algorithm is introduced.
- [ ] GIVEN each emitted projection WHEN its receipt validates content THEN it compares the actual emitted tool allow/deny exposure, argument schemas and instruction entries (fragment identity, body and required order) against the canonical effective contract under the pinned target profile, with a disposition and reason for every canonical entry under that profile. This content comparison is separate from independent canonical hash recomputation. Omitting a required supported entry, adding an unpermitted entry or altering a supported entry fails validation even if both manifests carry the correct canonical hash. An explicitly unsupported entry may be absent from the emitted projection only with its missing-capability reason recorded; profile exclusions retain their distinct excluded/dropped disposition and cannot be mislabeled unsupported. Required fallback prose or other LOC projection rules still apply.
- [ ] GIVEN a profile exclusion or ancestor edit that changes the canonical effective contract WHEN rebuilt and activated THEN tool exposure and emitted instructions both follow that rebuilt contract, both manifests validate against its recomputed new hash, and stale projection evidence is invalidated. A use or resume carrying the previous binding or envelope hash is denied before delivery or effects, even if both old manifests agree; an unchanged effective contract need not acquire a new hash.

## Verification

Implementation names the existing owner test runner, then records the isolated positive and negative scenarios above as version-bound receipts. Missing runtime access blocks qualification. This planning card claims no executed integration test or Rheos transition.

## Risks

Source and running runtime may diverge; verify deployed revision and actual capability.

## Active contract admission criteria

- [ ] GIVEN a binding profile or binding/envelope effective-contract hash that differs from the active contract WHEN use or resume is attempted THEN admission denies it; profile and both hashes must match the active contract.
