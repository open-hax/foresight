---
category: "kanban"
labels: "contracts, katamorph, shapes, cljc, 3sp"
type: "story"
story_id: "LOC.04"
points: "3"
title: "LOC.04 — Declare portable shapes for layers, fragments, effective contracts and projection manifests"
dependency: ["loc-fragment-source"]
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
> Blocked by: LOC.03 (fragment inputs) and open question 6

## Context

Foresight's repository map declares katamorph as the contract language and
muse as the compatibility compiler. The divine mandate puts shapes in `.cljc`.

## Outcome

Registry-keyed Malli schemas are defined for:

- the layer declaration: `:layer/id`, `:layer/root`, `:use`, `:drop`,
  `:targets`;
- the parsed fragment: id, tier, `absolute` with its enforcement `:surface`
  (`:pr-gate`, `:runtime-hook` or `:interactive`), `gate`, `override`, body
  and content hash;
- the effective contract;
- the projection manifest:
  - the layer chain, the target, and the budget with its unit and actual size;
  - one entry per fragment, holding its id, hash, the declaring layer, and a
    **disposition**: `:included`, `:dropped` (with the dropping layer and
    reason), `:shadowed` (with the overriding layer and its hash) or
    `:unsupported` (with the target capability that is missing).

## Scope

- [ ] Shapes in `.cljc`, with examples and counterexamples.
- [ ] A fragment reader that turns the S-expression form into the parsed shape,
  keeping the raw text for projection.
- [ ] Content hashing defined over the normalised fragment body. The
  canonicalisation:
  1. Read the fragment with the EDN reader. This discards comments and all
     whitespace **between** tokens.
  2. Print the read form with `pr-str`, with `*print-namespace-maps*` false
     and the default print settings, using one canonical printer shared by
     every runtime (JVM, bb and nbb).
  3. Take SHA-256 of the UTF-8 bytes of that string.

  Text **inside** string literals, including whitespace and line breaks, is
  content. Changing it changes the hash. Examples, recorded as test fixtures:
  - `(rule  "a b")` and `(rule "a b")` hash the same, because only whitespace
    between tokens changed;
  - `(rule "a b")` and `(rule "a  b")` hash differently, because whitespace
    inside a string changed;
  - adding a `;; comment` line does not change the hash;
  - reordering forms inside the fragment changes the hash.

## Non-goals

- The fold (LOC.05).
- File discovery (LOC.06).
- Emitting artifacts (LOC.08).

## Acceptance criteria

- [ ] GIVEN the LOC.03 fragments WHEN they are read and validated THEN every
  one conforms.
- [ ] GIVEN a fragment with no id, an unknown tier, or `override` without a
  target WHEN it is validated THEN it is rejected with an explained error.
- [ ] GIVEN a manifest entry WHEN validated THEN its disposition is one of
  included, dropped, shadowed or unsupported, and every non-included entry
  carries its reason fields.
- [ ] GIVEN the four canonicalisation fixtures WHEN hashed on the JVM, bb and
  nbb THEN each runtime produces the same hash for each fixture, and the
  same-or-different outcome matches the fixture table.

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
