---
uuid: "1f0758b2-97c1-4e40-af0e-384715228f8b"
title: "Reuse Rheos cycle-checked YAML value during frontmatter reads"
type: "story"
category: "kanban"
status: "incoming"
priority: "P3"
points: 2
labels: "rheos, performance, source-admission, follow-up"
---

# Reuse Rheos cycle-checked YAML value during frontmatter reads

## Context

[CodeRabbit review 5399591772](https://github.com/riatzukiza/rheos/pull/1#pullrequestreview-5399591772)
identified a trivial performance cost at Rheos commit
`04220846166dad35995f091945a7a6f1a2a4087f`.
The [source helper](https://github.com/riatzukiza/rheos/blob/04220846166dad35995f091945a7a6f1a2a4087f/src/rheos/backend/extern/yaml.cljs#L100)
converts a YAML Document and checks the resulting graph for cycles. The
[read path](https://github.com/riatzukiza/rheos/blob/04220846166dad35995f091945a7a6f1a2a4087f/src/rheos/backend/extern/yaml.cljs#L113)
immediately converts the same unchanged Document again with identical options.
Both calls are synchronous, with no intervening mutation or callback. No cycle
check bypass or other correctness failure was demonstrated.

This is standalone initial intake, not a child of the eleven-point preparation
epic and not ready or accepted implementation work. The Rheos fork has issues
disabled, so the follow-up lives on the authoritative Foresight board without
changing repository settings.

## Outcome

Each frontmatter read reuses its already converted, cycle-checked native value,
removing one full graph conversion and allocation while preserving public data
and refusal behavior. No measured latency or memory improvement is claimed yet.

## Scope

- Implementation owner: `open-hax/rheos`.
- Keep the reuse inside `rheos.backend.extern.yaml`; a private helper can return
  the Document and its checked native value, while range callers use only the
  Document.
- Preserve Clojure-shaped public results, lexical top-level scalar values,
  nested core-schema types, standard-tag validation, alias limits and
  deterministic cycle refusal.
- Use the existing parser and writeback regression fixtures and full gates.

## Non-goals

- No new YAML parser, board engine or alternate command surface in Foresight.
- No provider/CMS implementation, general serialization or board lifecycle change.
- No refused-source visibility work; that has its own five-point follow-up.
- No change to the preservation repair's verified behavior or qualification claim.

## Acceptance criteria

- A frontmatter read performs one native Document conversion and uses the graph
  that was checked for cycles.
- Native YAML objects stay inside the extern adapter; consumers receive the
  same portable data and source ranges as before.
- Existing cycle-versus-shared-alias, typed nested metadata, scalar spelling,
  tag compatibility, BOM and unchanged-body fixtures pass.
- The declared full test and lint gates pass at the repaired revision.
- Any reported latency or memory benefit includes representative before/after
  measurement; removing a conversion alone is not a performance benchmark.

## Verification

Inspect the real read path at a pinned revision, qualify it with existing focused
fixtures and the repository's declared full gates, and record exact commands,
revisions and review dispositions. Add a new test only if a newly exposed
behavior needs coverage. Use Rheos for board reads and future transitions.

## Risks

Moving native values across the extern boundary would weaken the portable data
contract. Reusing unchecked data or changing alias options could change refusal
behavior. If profiling or broad reader changes enlarge the work, split it before
readiness.
