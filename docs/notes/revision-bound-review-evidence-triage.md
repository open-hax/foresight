---
title: "Revision-bound review evidence triage"
summary: "Records merged Knoxx translation dispatch/review/reconciliation behavior as current implementation evidence for a broader Mu candidate: judgments bind to scope-qualified immutable inputs and outputs reviewed, currentness is derived by joining evidence, and runtime receipts remain distinct from desired state."
category: "architecture"
created: "2026-08-23"
status: "triage"
---

# Revision-bound review evidence triage

## Scope

This note is revision-scoped synthesis. It records current Knoxx implementation evidence without declaring the candidate laws below accepted Foresight law.

The evidence changed materially after the note was first written:

- `open-hax/knoxx#253` merged as `0520b2677121f97b796a8309307c61ce418eaa6e`;
- `open-hax/knoxx#254` merged as `93dd126a5537daefc88ec151e6238f53a8506b7f`;
- `open-hax/knoxx#255` merged as `0a8f7f8961b336dd47ee77401df8d48ffe981b0d`;
- Foresight `docs/notes/four-independent-capabilities-repository-translation-review-rendering.md` remains the prior architectural hypothesis at `df95723be66f228c69e4c276dbdc0cc183ba7a08`.

The earlier review-scoped heads `b63b6ea4c87539e6d12578eef3edea7472c5a17b` (#253) and `149e6a0fa38a9a45d73afe5a4da8418ce2cae774` (#254) remain useful provenance for the review history, but they no longer describe the current source state. Merge status makes the Knoxx behavior current implementation evidence; it still does not promote it into generic Foresight law.

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

## Current Knoxx evidence

### Translation completion is an observed fact, not desired state

Merged Knoxx #253 introduces portable `.cljc` translation evidence and dispatch laws.

`CompletedTranslationReceipt` names both:

- `:translation/source-revision` — the immutable Knoxx-side source revision bound to the dispatch; the worker's actual input bytes are not independently proven by this receipt;
- `:translation/revision` — the immutable produced-output identity minted by Knoxx for the completed worker batch.

The law treats the receipt as an observed execution fact. Publication resources do not carry it as desired state.

That distinction matters because a second translation of the same Knoxx-side source revision may produce a different output identity. A downstream judgment bound only to the source revision would silently remain valid for an output nobody reviewed.

### Attempt facts are separate from product facts

Merged Knoxx #253 keeps dispatch attempts in `DispatchRecord` and completed translations in `CompletedTranslationReceipt`.

A dispatch can be accepted, duplicate, rejected, failed, completed, or unreachable. Those states describe an attempt. Only a completed translation receipt says Knoxx has recorded a completed translation product.

The publication gate therefore does not need to infer completion by filtering operational attempts. It reads translation evidence directly.

### Consumer-specific revision binding stays at the consumer boundary

The ingestion worker's existing batch request has no source-revision or idempotency field. Rather than widen the foreign worker contract, #253 records a local Knoxx `DispatchRecord` that binds the concrete Knoxx-side revision and dispatch identity to the worker's returned batch id.

When the worker later reports completion, Knoxx joins that answer back to the local binding before minting Knoxx-side translation evidence. This establishes the requested/observed revision relationship inside Knoxx; it does not independently prove which exact source bytes the worker fetched.

This preserves the worker's contract while keeping Knoxx-specific publication/revision semantics visible to the code that owns them.

### Approval is evidence about one exact produced output

Merged Knoxx #254 extends the translation evidence model with revision-specific approval.

The approval carries both the concrete source revision and the concrete translated-output revision. `approved?` is a join: approval counts only while it names the current completed translation receipt for that source revision.

A re-translation does not delete or rewrite the old approval. It makes that approval non-current because the current output revision changed.

That is materially different from treating approval as mutable document state such as `approved=true`.

### Scope is part of evidence identity

The merged Knoxx work requires tenant/organization and project scope on the evidence. The implementation history records that omitting tenant/project from dispatch identity made evidence admissible across scopes where the translated data did not exist.

For approval, identity is inherited from the validated translation receipt rather than trusted from the approval request.

### Reconciliation is now a real production caller

Merged Knoxx #255 closes an older architectural gap: the planner, gate, effects, receipts, and adapters now have an authorized runtime caller.

`law.publication-reconciler` supplies a portable trigger/correlation contract. The runtime then:

1. loads desired publication state fresh;
2. resolves the declared target through the registry;
3. observes that same target in the current run;
4. asks the pure planner for a decision;
5. executes through the target registry;
6. emits exactly one validated correlated receipt for the trigger.

Runtime-owned target observation is significant: convergence is decided against what the same adapter reports in the same run, while translation/review evidence remains provider-supplied. The reconciler does not edit desired state to make a failed effect look converged.

This reinforces the separation between desired state, provider evidence, runtime observation, and execution receipts without making the Knoxx trigger shape a generic Foresight contract.

### Current persistence gap: publication receipts are real but default storage is ephemeral

#255's default receipt sink is a bounded in-memory journal of 200 receipts. The implementation explicitly documents it as process-local and deliberately lossy past the bound; restart loses the journal.

This is actionable runtime/persistence drift now that reconciliation has a production caller. It does **not** make the receipt law provisional: the semantic distinction is sound, while durable storage remains an infra/store decision.

Until a durable sink is configured, the current runtime can prove that one reconciliation invocation emitted one lawful receipt while that receipt remains retained, but it cannot treat the default journal as durable historical audit evidence across restart or retention rollover.

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

### 5. Reconciliation should separate desired state, evidence, observation, and receipts

The current Knoxx runtime provides one concrete instance of a broader candidate separation:

```text
desired state
+ provider evidence
+ runtime-owned observation
-> pure plan
-> effect
-> immutable execution receipt
```

A failed effect does not authorize mutation of desired state to erase the mismatch. A target observation should describe the actual target being reconciled in that run rather than a provider-supplied rumor about some other surface.

This is a lift candidate only. Independent survivor evidence is still required before Foresight should generalize it.

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

Knoxx translation approval is domain-specific. Merge status proves it is current Knoxx implementation, not that this exact shape is the correct generic Foresight schema.

## Not promoted

This note does **not**:

- promote Knoxx's translation receipt, approval, trigger, or reconciliation schemas into common Foresight schemas;
- require every worker/consumer seam to use a local correlation store;
- promote Mongo persistence representation, dispatch outcome vocabulary, one-document-per-batch constraints, HTTP statuses, approval permission names, or the bounded in-memory receipt journal;
- infer review-domain ownership from implementation location;
- treat merge status as operator acceptance of generic Mu law;
- alter the accepted Clio event-ledger ownership decision, which is an explicit operator adjudication recorded separately in Epiphany's Clio triage at `0370d6ce6d4e1e9f90d17b252884fe8ee4970f76`.

## Sources

Current merged implementation evidence:

- https://github.com/open-hax/knoxx/commit/0520b2677121f97b796a8309307c61ce418eaa6e
- https://github.com/open-hax/knoxx/commit/93dd126a5537daefc88ec151e6238f53a8506b7f
- https://github.com/open-hax/knoxx/commit/0a8f7f8961b336dd47ee77401df8d48ffe981b0d
- https://github.com/open-hax/knoxx/blob/0520b2677121f97b796a8309307c61ce418eaa6e/backend/src/cljs/knoxx/backend/law/translation_evidence.cljc
- https://github.com/open-hax/knoxx/blob/0520b2677121f97b796a8309307c61ce418eaa6e/backend/src/cljs/knoxx/backend/law/translation_dispatch.cljc
- https://github.com/open-hax/knoxx/blob/0a8f7f8961b336dd47ee77401df8d48ffe981b0d/backend/src/cljs/knoxx/backend/law/publication_reconciler.cljc
- https://github.com/open-hax/knoxx/blob/0a8f7f8961b336dd47ee77401df8d48ffe981b0d/backend/src/cljs/knoxx/backend/infra/publication_reconciler.cljs

Context and adjacent decisions:

- https://github.com/open-hax/foresight/blob/df95723be66f228c69e4c276dbdc0cc183ba7a08/docs/notes/four-independent-capabilities-repository-translation-review-rendering.md
- https://github.com/octave-commons/epiphany/commit/0370d6ce6d4e1e9f90d17b252884fe8ee4970f76

Historical review provenance:

- https://github.com/open-hax/knoxx/commit/b63b6ea4c87539e6d12578eef3edea7472c5a17b
- https://github.com/open-hax/knoxx/commit/149e6a0fa38a9a45d73afe5a4da8418ce2cae774

Supplemental navigation:

- https://github.com/open-hax/knoxx/pull/253
- https://github.com/open-hax/knoxx/pull/254
- https://github.com/open-hax/knoxx/pull/255
- https://github.com/octave-commons/epiphany/pull/12

## Next evidence pass

Compare the now-merged Knoxx revision-bound judgment/reconciliation shape against Muse's evidence-first review receipts and Calliope/Studio review evidence. Separately trace whether a durable publication receipt sink already exists elsewhere in Knoxx or services before proposing new persistence machinery. Promotion should depend on independent convergence, not on Knoxx alone.
