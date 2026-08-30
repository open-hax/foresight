---
title: "Promethean ancestral prototype"
summary: "Pins Promethean as non-actionable ancestral evidence and records six provisional continuity claims without promoting them into Foresight law."
category: "lineage"
created: "2026-08-29"
---

# Signal

Promethean is represented in Foresight as one revision-bound
`:historical-prototype` source:

```clojure
{:source/id :promethean
 :source/type :historical-prototype
 :source/repository "octave-commons/promethean"
 :source/revision "06a8b83312ea70dcde6d2e423369b410e6d0d3f2"
 :source/role :ancestral-prototype
 :source/actionable? false
 :source/workspace-authority :none
 :source/execution-authority :none
 :source/semantic-status :proposed/ancestral-prototype
 :source/epistemic-status :provisional}
```

The recovered center is:

> Promethean is a sovereign personal continuity engine that turns human
> intent into bounded action, records what happened, and converts accepted
> outcomes into reusable capability without surrendering human authority.

That sentence is a provisional lineage interpretation. It is not represented
as a quotation from Promethean and is not an accepted Foresight law.

The executable declaration lives in `src/foresight/lineage.cljc`. Its authority
boundary is enforced by `src/foresight/law/lineage.cljc`.

# Evidence

The source record is pinned to
`octave-commons/promethean@06a8b83312ea70dcde6d2e423369b410e6d0d3f2`.

At that revision, `README.md` directly supports the narrower factual basis
recorded under `:source/evidence`:

- a modular, distributed cognitive architecture;
- embodied agents operating through perception-action loops;
- independently bounded services for language, tools, memory, and interaction;
- an immutable functional core with mutable runtime shells;
- agent-assisted kanban work;
- an operating-system identity around the Eidolon package.

The six lineage claims below are recovered interpretations of Promethean's
enduring pressure and later descendants. Their
`:claim/epistemic-status :provisional` and
`:claim/recovery :interpretation` fields prevent the evidence record from
masquerading as direct doctrine or accepted law.

The inventory can be inspected and validated with:

```sh
nbb scripts/project.clj lineage
nbb scripts/project.clj validate
nbb test/lineage_test.cljs
```

# Frames

| Claim | Continuity status | Current carrier or surviving revision |
|---|---|---|
| `:promethean/owner-sovereignty` | `:live` | `:eta-mu/autonomy`, `:epiphany/explicit-human-promotion` |
| `:promethean/intent-compiler` | `:live-with-revision` | Agents compile candidate plans; inference does not become accepted intent without an explicit authority boundary. |
| `:promethean/learn-once` | `:live-with-revision` | Preserve solutions, failures, context, contracts, and evidence; reuse only when current conditions satisfy the recorded contract. |
| `:promethean/context-field` | `:recurring-pressure` | `:openplanner/semantic-graph`, `:openplanner/epistemic-kernel` |
| `:promethean/modular-intent` | `:live` | `:eta-mu/skill-registry`, `:foresight/independent-capability-constellation` |
| `:promethean/eidolon-physics` | `:rejected-implementation` | Context must influence routing; no particular physics simulation is law. |

These statuses describe continuity, not epistemic promotion. Every claim
remains provisional until an explicit evidence-bearing promotion event says
otherwise.

A later Promethean status pulse should be a differential projection over this
record. It should report only when evidence changes, a carrier adopts or
rejects a claim, a contradiction appears, or a human identity/governance
decision becomes due. "No semantic change" is a valid pulse.

# Countermoves

- Promethean is not added to `.gitmodules`.
- The record has no `:source/path`.
- It cannot become actionable or gain workspace or execution authority.
- Repository size, open issues, dependency drift, and old failing workflows do
  not define the semantic health of its lineage.
- Descendant ledgers should preserve a typed transitive ancestry path when a
  claim depends on Promethean; unrelated events do not need ritual direct
  citations.
- Similarity, repeated vocabulary, and descent do not establish identity,
  ownership, or current architectural authority.

# Next

Project validation now checks this lineage record alongside the live workspace
declaration; a future pulse projector can consume the validated inventory
without reopening Promethean as an execution root.
