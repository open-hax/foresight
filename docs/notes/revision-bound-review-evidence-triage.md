---
title: "Revision-bound review evidence triage"
summary: "Records Knoxx's open translation dispatch/review work as revision-scoped evidence for a broader Mu candidate: judgments should bind to scope-qualified immutable inputs and outputs reviewed, and currentness should be derived by joining evidence rather than mutating historical receipts."
category: "architecture"
created: "2026-08-23"
status: "triage"
---

# Revision-bound review evidence triage

## Scope

This note is revision-scoped synthesis. It does not promote open Knoxx pull requests into current architecture and does not declare the candidate laws below accepted Foresight law.

The relevant implementation evidence is:

- `open-hax/knoxx#253` at `b63b6ea4c87539e6d12578eef3edea7472c5a17b`;
- `open-hax/knoxx#254` at `149e6a0fa38a9a45d73afe5a4da8418ce2cae774`;
- Foresight `docs/notes/four-independent-capabilities-repository-translation-review-rendering.md` at `df95723be66f228c69e4c276dbdc0cc183ba7a08`.

Both Knoxx pull requests are open at the time of this note. #254 is stacked on #253.

## Prior Foresight hypothesis

`four-independent-capabilities-repository-translation-review-rendering.md` proposed that translation, review/evaluation, rendering, and repository/CMS concerns remain independent. For review/evaluation it proposed a domain-independent shape resembling:

```text
Review Case
  -> evidence / artifacts
  -> rubric
  -> judgment / correction / decision
  -> receipt
```

That note was architectural synthesis, not executable common law.

## New Knoxx evidence

### Translation completion is an observed fact, not desired state

Knoxx PR #253 introduces portable `.cljc` translation evidence and dispatch laws.

`CompletedTranslationReceipt` names both:

- `:translation/source-revision` — the immutable Knoxx-side source revision bound to the dispatch; the worker's actual input bytes are not independently proven by this receipt;
- `:translation/revision` — the immutable produced-output identity minted by Knoxx for the completed worker batch.

The law explicitly treats the receipt as an observed execution fact. Publication resources do not carry it as desired state.

That distinction matters because a second translation of the same Knoxx-side source revision may produce a different output identity. A downstream judgment bound only to the source revision would silently remain valid for an output nobody reviewed.

### Attempt facts are separate from product facts

Knoxx PR #253 keeps dispatch attempts in `DispatchRecord` and completed translations in `CompletedTranslationReceipt`.

A dispatch can be accepted, duplicate, rejected, failed, completed, or unreachable. Those states describe an attempt. Only a completed translation receipt says Knoxx has recorded a completed translation product.

The publication gate therefore does not need to infer completion by filtering operational attempts. It reads translation evidence directly.

### Consumer-specific revision binding stays at the consumer boundary

The ingestion worker's existing batch request has no source-revision or idempotency field. Rather than widen the foreign worker contract, #253 records a local Knoxx `DispatchRecord` that binds the concrete Knoxx-side revision and dispatch identity to the worker's returned batch id.

When the worker later reports completion, Knoxx joins that answer back to the local binding before minting Knoxx-side translation evidence. This establishes the requested/observed revision relationship inside Knoxx; it does not independently prove which exact source bytes the worker fetched.

This preserves the worker's contract while keeping Knoxx-specific publication/revision semantics visible to the code that owns them.

### Approval is evidence about one exact produced output

Knoxx #254 extends the translation evidence model with revision-specific approval.

The approval carries both the concrete source revision and the concrete translated-output revision. `approved?` is described as a join: approval counts only while it names the current completed translation receipt for that source revision.

A re-translation does not delete or rewrite the old approval. It makes that approval non-current because the current output revision changed.

That is materially different from treating approval as mutable document state such as `approved=true`.

### Scope is part of evidence identity

The current Knoxx work also requires tenant/organization and project scope on the evidence. The implementation history records that omitting tenant/project from dispatch identity caused evidence to become admissible across scopes where the underlying translated data did not exist.

For approval, identity is inherited from the validated translation receipt rather than trusted from the approval request.

## Mu lift candidates

The following are Foresight Mu candidates, not accepted lifts.

### 1. A judgment should bind to the exact immutable work product it evaluated

Where an output can be regenerated independently of its input, evidence should preserve scope and both identities:

```text
scope
scope-qualified input identity
scope-qualified output identity
judgment
```

Binding only the input permits a later output to inherit a judgment it never received. Omitting scope permits otherwise-correct revision evidence to cross tenant/project boundaries.

Translation is one instance. The same shape plausibly applies to agent task outputs, generated documents, code-review revisions, render artifacts, model-produced labels, and other reviewed work products.

### 2. Historical judgments remain facts; currentness is derived

A judgment receipt should not need mutation or deletion merely because newer work supersedes the thing it evaluated.

Instead:

```text
historical judgment
+ current product evidence
-> current? / stale?
```

This preserves provenance while preventing stale approval from authorizing a new work product.

### 3. Attempt evidence and successful-product evidence are different semantic categories

Queue/dispatch/retry facts describe attempts. Product receipts describe things that actually exist. A consumer should not need to know operational retry states to decide whether a work product exists.

### 4. Correlation bindings can belong at an adapter boundary without contaminating a foreign contract

When a producer does not own a consumer-specific identity dimension, widening the producer's contract is not automatically the right move. A local correlation record may preserve the missing semantic binding while keeping both contracts explicit and independently evolvable.

This is a candidate boundary law, not a universal rule: if the missing identity is actually intrinsic to the producer's domain, ownership should move instead of being hidden in correlation state.

## Relation to the existing review ontology

The prior Foresight review hypothesis becomes more concrete under this evidence:

```text
Review Case
  -> scope
  -> scope-qualified subject/input artifact identity
  -> scope-qualified candidate/output artifact identity
  -> evidence/context
  -> rubric
  -> judgment
  -> immutable judgment receipt

current judgment?
  = join(judgment receipt, current output evidence)
```

The Knoxx translation approval surface is still domain-specific. It does not prove that this exact shape is the correct generic Foresight schema.

## Not promoted

This note does **not**:

- promote Knoxx #253 or #254 into current `main` behavior;
- make Knoxx's translation receipt or approval schema a common Foresight schema;
- require every worker/consumer seam to use a local correlation store;
- promote the Mongo persistence representation, dispatch outcome vocabulary, one-document-per-batch constraint, HTTP statuses, or approval permission names;
- infer review-domain ownership from implementation location;
- alter the accepted Clio event-ledger ownership decision, which is an explicit operator adjudication recorded separately in Epiphany's Clio triage at `0370d6ce6d4e1e9f90d17b252884fe8ee4970f76`.

## Sources

Revision-bound evidence:

- https://github.com/open-hax/knoxx/commit/b63b6ea4c87539e6d12578eef3edea7472c5a17b
- https://github.com/open-hax/knoxx/blob/b63b6ea4c87539e6d12578eef3edea7472c5a17b/backend/src/cljs/knoxx/backend/law/translation_evidence.cljc
- https://github.com/open-hax/knoxx/blob/b63b6ea4c87539e6d12578eef3edea7472c5a17b/backend/src/cljs/knoxx/backend/law/translation_dispatch.cljc
- https://github.com/open-hax/knoxx/commit/149e6a0fa38a9a45d73afe5a4da8418ce2cae774
- https://github.com/open-hax/foresight/blob/df95723be66f228c69e4c276dbdc0cc183ba7a08/docs/notes/four-independent-capabilities-repository-translation-review-rendering.md
- https://github.com/octave-commons/epiphany/commit/0370d6ce6d4e1e9f90d17b252884fe8ee4970f76

Supplemental navigation:

- https://github.com/open-hax/knoxx/pull/253
- https://github.com/open-hax/knoxx/pull/254
- https://github.com/octave-commons/epiphany/pull/12

## Next evidence pass

Wait for the Knoxx review stack to stabilize, then compare the surviving revision-bound judgment shape against Muse's evidence-first review receipts and any Calliope/Studio review evidence. Promotion should depend on independent convergence, not on this translation implementation alone.
