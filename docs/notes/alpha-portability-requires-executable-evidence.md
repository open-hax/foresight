---
title: "Alpha portability requires executable evidence"
summary: "Records a revision-scoped crucible finding from Foresight PRs #23-#27: portable-looking `.cljc` and shared ownership claims remained defective until the JVM law gate actually loaded and executed them. This is a process/law candidate, not accepted policy."
category: "design"
created: "2026-08-16"
status: "candidate"
---

# Scope

This note records a bounded synthesis from the Alpha portability stack. It does not amend `AGENTS.md`, promote a new process rule, or claim that the open JVM workflow is current `main` behavior.

# Observations

Foresight already states that durable semantics should be purified into portable Clojure data and `.cljc`, while runtime-specific machinery remains at outer adapter boundaries.

The proposed Alpha JVM gate in PR #24 then exercised that claim against real JVM/Clojure execution and exposed multiple independent defects before the relevant laws could run:

- PR #23: `alpha.law.artifact/Condition` had an unmatched delimiter, so the supposedly portable source could not be read on the JVM at all.
- PR #25: `alpha.law.markdown.schema/FrontmatterPath` still referenced the removed Alpha-local `PathSegment` after Condition path semantics had moved to Katamorph, producing a JVM compile-time missing-var failure.
- PR #26: EventDraft test fixtures used reader-invalid keyword spellings, preventing the JVM suite from reaching the EventDraft laws.
- PR #27: reaction-selection fixtures had the same class of reader-invalid identity spelling and likewise failed before the reaction laws executed.

The gate itself remains open in PR #24 and is stacked on PR #23 because current `main` cannot load the Alpha suite without that source repair.

# Interpretation

A `.cljc` filename is evidence of intended portability, not proof of portability.

A conceptual ownership move is likewise incomplete if consumers retain stale local vocabulary that only one runtime or unexecuted path tolerates.

The stronger candidate law is:

```text
portable semantic claim
  + portable dependency graph
  + reader/load success in every claimed runtime
  + executable law suite in every claimed runtime
  = portability evidence
```

This is stronger than source inspection alone because execution can expose both trivial syntax/reader defects and architectural ownership drift.

# Crucible significance

The failures are useful evidence rather than merely CI cleanup. They show the crucible discovering where extracted common law is still coupled to accidental source assumptions:

```text
purify / lift
  -> retain stale or runtime-specific assumption
  -> portable source appears coherent
  -> second runtime executes
  -> hidden assumption becomes observable
  -> repair or reject the lift
```

That suggests Foresight should eventually distinguish at least:

- **portable intent** — source is written as `.cljc` / portable data;
- **portable candidate** — dependencies and boundaries appear runtime-neutral;
- **portable verified** — the declared runtime matrix loads and executes the relevant laws;
- **portable accepted** — an explicit authority accepts that verified portability for a declared scope.

These names are proposals only.

# Relationship to current policy

This finding refines, but does not replace, the existing `AGENTS.md` mandate to "purify before you port." It adds evidence about what counts as having successfully ported a purified law.

It also aligns with Epiphany's broader separation of observation, derivation, proposal, verification, and acceptance: a portability claim should not skip directly from source shape to accepted cross-runtime fact.

# Sources

- Foresight `AGENTS.md` at the current `main` revision used for this triage.
- https://github.com/open-hax/foresight/pull/23
- https://github.com/open-hax/foresight/pull/24
- https://github.com/open-hax/foresight/pull/25
- https://github.com/open-hax/foresight/pull/26
- https://github.com/open-hax/foresight/pull/27

# Disposition

**Foresight process/law candidate.** Do not promote this into `AGENTS.md`, Alpha law, or a required repository-wide gate until the JVM workflow itself lands and the runtime matrix / scope is explicitly accepted.
