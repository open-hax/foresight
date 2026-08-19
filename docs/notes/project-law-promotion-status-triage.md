---
title: "Foresight project-law promotion status triage"
summary: "Distinguishes merged project-law declarations from explicitly accepted cross-repository lifts after Foresight PR #33 encoded recovered invariants as portable project data."
category: "architecture"
created: "2026-08-19"
---

# Signal

Foresight PR #33 merged `src/foresight/project.cljc` and `src/foresight/law/project.cljc` as a portable declaration/validation surface for the current source constellation.

That is current implementation evidence. It does **not**, by itself, prove that every repository-derived `:invariant/id` in the declaration has been explicitly accepted as common Foresight law.

This note preserves that distinction while the promotion process catches up with the implementation.

# Evidence

- Foresight PR #33: https://github.com/open-hax/foresight/pull/33
- merge commit: https://github.com/open-hax/foresight/commit/bb6b478bc33b52952698b6abcbf76c3f68ccc9ae
- `src/foresight/project.cljc` now declares 14 direct inventory roots and 38 recovered invariants with basis repository/path/revision provenance.
- `src/foresight/law/project.cljc` makes a subset executable: identity, path confinement, ownership/actionability, invariant-reference integrity, and `.gitmodules` drift.
- PR #33 received automated review approval; the available review trail does not contain an explicit operator adjudication accepting all recovered repository statements as cross-repository Foresight law.
- Foresight PR #29 explicitly preserved a different review-system law as a **lift candidate** rather than inferring promotion from implementation or rollout: https://github.com/open-hax/foresight/pull/29
- Foresight PR #30 likewise preserved executable portability as a **candidate law** pending explicit acceptance: https://github.com/open-hax/foresight/pull/30

# Classification

## Current project contract

The following are current executable Foresight behavior because merged code validates them directly:

- direct source identity/path uniqueness;
- source-path lexical confinement;
- `.gitmodules` agreement with declared direct submodules;
- consolidation inputs remaining non-actionable;
- actionable roots being independently owned direct submodules;
- declared invariant references resolving.

These claims describe what the merged Foresight validator currently enforces. They are not evidence that similarly named laws should automatically govern every submodule internally.

## Recovered declarative claims

Repository-scoped entries such as Epiphany promotion rules, eta-mu board rules, Knoxx extern-boundary rules, Services deployment ownership, Truth ECS rules, Calliope provenance rules, and similar entries are presently best classified as **revision-scoped recovered claims represented in the Foresight project model**.

Their presence in `foresight.project/invariants`, repetition across repositories, or merge in PR #33 does not independently establish promotion into a common cross-repository law.

## Lift candidates

A recovered declarative claim becomes a Foresight lift candidate when comparison across independent survivors shows that the same semantic constraint survives different implementations or failure modes and the source evidence is preserved.

Existing examples of this evidentiary pattern include:

- evidence-first review transition/publication laws recorded by PR #29;
- executable portability evidence recorded by PR #30;
- source-versus-projection separation appearing independently in Rheos, Alpha, Clio, and Epiphany process discipline.

Candidate status is still not acceptance.

## Accepted lifts

An accepted lift requires explicit operator adjudication or another repository-local promotion mechanism that explicitly carries that authority. The acceptance event should identify the promoted law and preserve the source/candidate provenance it supersedes or ratifies.

# Consequence for `foresight.project`

Until promotion metadata is encoded directly, consumers should not treat every map under `:project/invariants` as having identical epistemic status.

At minimum, readers should distinguish:

```clojure
:executable-current-contract
:recovered-revision-scoped-claim
:lift-candidate
:accepted-lift
```

Those keywords are vocabulary proposals in this note, not a schema change.

A later focused change may add explicit status/provenance fields to the portable declaration if that shape is accepted. This note does not modify `src/foresight/project.cljc`, rewrite recovered source claims, or infer acceptance from PR #33's merge.

# Countermoves

- Do not downgrade the executable project validator: its current enforcement is real merged behavior.
- Do not erase the 38 recovered invariants: they are valuable indexed archaeology with revision provenance.
- Do not call all 38 accepted common law without an acceptance event.
- Do not infer that a repository-specific statement is invalid merely because it has not been promoted into Foresight.
- Preserve the distinction between `basis` evidence, candidate synthesis, and accepted authority.
