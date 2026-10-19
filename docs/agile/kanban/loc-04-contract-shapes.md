---
category: "kanban"
labels: "contracts, katamorph, shapes, cljc, 3sp"
type: "story"
story_id: "LOC.04"
points: "3"
title: "LOC.04 — Declare portable shapes for layers, fragments, effective contracts and projection manifests"
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-contract-shapes"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.04 — Declare portable shapes for layers, fragments, effective contracts and projection manifests

> Design: `docs/notes/design/layered-operating-contracts.md` §Layers, §Fragments, §Ownership across the constellation, open question 6
> Target repository: katamorph (contract-language), provisional pending open question 6
> Blocked by: open question 6

## Context

Foresight's repository map declares katamorph as the contract language and
muse as the compatibility compiler. The divine mandate puts shapes in `.cljc`.

## Outcome

Registry-keyed Malli schemas are defined for:

- the layer declaration: `:layer/id`, `:layer/root`, `:use`, `:drop`,
  `:targets`;
- the parsed fragment: id, tier, `absolute`, `gate`, `override`, body and
  content hash;
- the effective contract;
- the projection manifest: layer chain, fragment ids and hashes, target,
  budget and actual size.

## Scope

- [ ] Shapes in `.cljc`, with examples and counterexamples.
- [ ] A fragment reader that turns the S-expression form into the parsed shape,
  keeping the raw text for projection.
- [ ] Content hashing defined over the normalised fragment body.

## Non-goals

- The fold (LOC.05).
- File discovery (LOC.06).
- Emitting artifacts (LOC.08).

## Acceptance criteria

- [ ] GIVEN the LOC.03 fragments WHEN they are read and validated THEN every
  one conforms.
- [ ] GIVEN a fragment with no id, an unknown tier, or `override` without a
  target WHEN it is validated THEN it is rejected with an explained error.
- [ ] GIVEN a fragment whose whitespace has been reformatted WHEN it is hashed
  THEN the hash is unchanged. GIVEN a fragment whose text has changed WHEN it is
  hashed THEN the hash differs.

## Verification

```bash
# katamorph's documented test gate; the shapes must also load in nbb and bb
```

## Risks

Shapes that depend on muse internals. Mitigation: the shapes take no runtime
dependencies, and muse consumes them.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
