---
category: "kanban"
labels: "contracts, muse, preservation, 2sp"
type: "story"
story_id: "LOC.09"
points: "2"
title: "LOC.09 — Select contract-0 as a module and prove it unchanged through the layered compiler"
dependency: ["loc-contract-0-golden-artifacts", "loc-fold-laws", "loc-instruction-emitter"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-contract-0-module"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.09 — Select contract-0 as a module and prove it unchanged through the layered compiler

> Design: `docs/notes/design/layered-operating-contracts.md` §Contract-0 preservation, open question 3
> Target repository: muse
> Blocked by: LOC.02, LOC.05 and LOC.08, and open question 3 for module resolution

## Outcome

The muse tree's `.ημ/contract/layer.edn` declares `:use [:muse/contract-0]`.
Building through the layered compiler produces artifacts identical to the
LOC.02 golden set.

## Scope

- [ ] Resolve `:muse/contract-0` as a module, in the order decided by open
  question 3.
- [ ] Before selection, load the module's bytes from its pinned source (the
  `contract-0/vN` tag or commit) and verify every file against the manifest's
  per-file SHA-256. On any mismatch, refuse to select it, with an explained
  error.
- [ ] Run the golden comparison against the output of the layered path.
- [ ] Record the content hash of contract-0 version 1 in the projection
  manifest.

## Non-goals

- Evolving contract-0, which belongs to a future version 2.

## Acceptance criteria

- [ ] GIVEN the muse layer selecting contract-0 WHEN built through the layered
  compiler THEN the golden test passes with no new normalisations.
- [ ] GIVEN a layer that drops a fragment of contract-0 WHEN built THEN the
  manifest records the drop and the golden test reports the difference.
- [ ] GIVEN a module source whose bytes differ from its manifest WHEN selected
  THEN selection is refused and names the differing file.

## Verification

```bash
npx shadow-cljs compile test
```

## Risks

The layered path can change file ordering in artifacts. Mitigation: ordering is
part of the golden comparison, not a normalisation.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
