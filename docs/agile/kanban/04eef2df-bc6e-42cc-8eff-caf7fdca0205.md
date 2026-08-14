---
category: "architecture"
labels: ["capabilities", "evaluation", "review", "cljc", "ontology"]
points: "5"
title: "Extract the Evaluation ontology so translation is a consumer, not the model"
priority: "P1"
status: "incoming"
uuid: "04eef2df-bc6e-42cc-8eff-caf7fdca0205"
created_at: "2026-08-14T01:00:00Z"
---

# Extract the Evaluation ontology so translation is a consumer, not the model

Both notes name this as the first of the four to build, for the same reason:
it is the one most likely to be quietly deformed by its first use case.

> Extract the ontology for **Review Case → Artifacts/Evidence → Rubric →
> Judgment/Correction/Decision → Receipt** first; then translation becomes the
> first concrete producer/consumer proving that #3 is genuinely
> domain-independent.

The translation review UI resembled a GitHub PR diff. That resemblance is the
clue, not the design: a GitHub review, an SME translation review, and an
agent-output labelling task are the same machinery with different artifacts.
If the core object is a `TranslationReview`, every later use pays for that.

## The model

```text
Review Case            one unit requiring judgment
  ├── Artifacts        input / candidate / reference / previous / evidence
  ├── Rubric           the questions and laws governing judgment
  └── Requested judgments
            ↓
      Review Session
            ↓
        Judgment       one atomic determination
      /     |      \
  labels  correction  decision
            ↓
         Receipt       durable fact that the evaluation occurred
```

## Scope

- Portable `.cljc` laws for Review Case, Artifact, Rubric, Judgment, Correction,
  Decision and Receipt.
- **Artifact roles are semantic, never positional.** `:source`, `:candidate`,
  `:reference`, `:previous`, `:evidence` — not `:left` / `:right`. Two-pane,
  three-way and non-comparative are all presentation choices made downstream.
- Domain keys ride alongside without entering the model: a translation case may
  carry `:translation/source-locale`, `:translation/target-locale`,
  `:translation/terminology-set`, and the generic system never reads them.
- Adjudication is defined as the resolution of *conflicting* judgments, not as
  the ordinary path.
- Judgments are durable facts. A correction that completes real work is also
  training material — that is the point, and it must not require a separate
  labelling application to produce.

## Non-goals

- No review UI. Layout is a Representation concern.
- No translation-specific schema in the ontology.
- No dataset/export pipeline yet; prove the receipts are well-shaped first.

## Done when

- A translation review and an agent-output labelling case both validate against
  the same Review Case laws, with no shared key beyond the generic model.
- A three-artifact case (`source | candidate | reference`) and a single-artifact
  case both validate — the model does not assume comparison.
- Removing every `:translation/*` key from a case leaves it valid.
- Judgment, correction and decision are distinguishable in the receipt, and a
  deferred or rejected case can never be read as an accepted one.
- **Something calls it.** A card that ships an ontology with no producer and no
  consumer repeats the publication epic; the next card in this set is that caller.

## Related

- `726e5c5f-7c9b-49e6-8567-5f3a169d85b8` — one SME through one review, end to end
- Knoxx epic `knoxx-evaluation-review-system` (PR #238) — the statement of intent
- notes: `four-independent-capabilities-...`, `naming-operation-categories-...`
