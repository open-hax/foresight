# Knoxx translation/review path and concept inventory

## Evidence boundary

This inventory describes `open-hax/knoxx` at `origin/main` on 2026-08-28 and
the deployed topology declared by `open-hax/services`. Recommendations are not
claims about current ownership. Git commit and blob identities should accompany
future extraction records; paths are selectors, not durable identity.

PR [open-hax/foresight#39](https://github.com/open-hax/foresight/pull/39),
`feat: establish Clio-backed archaeology ledgers`, is the exact recent
archaeology artifact named by the user. It is open, authored by `riatzukiza`,
and introduces five independent Clio ledgers plus a Katamorph manifest and a
portable CLJC projection package. Its seed action explicitly leaves continued
Knoxx shape inventory open. Review findings about relation typing and declared
ledger event types were addressed by later commits visible on the PR; this
document does not assert merge readiness.

## Current end-to-end path

1. Knoxx publication resources declare document, garden, locale, revision,
   review policy, and target. The pure publication gate resolves a concrete
   revision once and derives `:actions/request-translation` when evidence is
   missing or stale.
2. The translation dispatch facade loads resources, source revision, and
   evidence before dispatch. The current agent runner emits
   `publication/translation-needed`; `publication_translator.edn` subscribes
   through the publication namespace and translator capability.
3. Agent submission is normalized and written to the translation evidence
   store. If Mongo-backed evidence initialization fails, dispatch and approval
   endpoints explicitly return unavailable rather than pretending success.
4. The revision-specific review route accepts an authorized human decision;
   publication admission requires an approval for the same document, locale,
   garden, and concrete revision. A later source revision makes the prior
   approval insufficient without deleting its history.
5. The publication reconciler observes a registered target, plans before
   effects, executes idempotently, and emits one correlated receipt. Its default
   receipt journal is bounded process memory unless a durable sink is supplied.
6. Services builds and deploys Proxx, Knoxx, Caddy, and Website in dependency
   order. Its production contract says OpenPlanner HTTP is absent; Knoxx uses an
   in-process Mongo adapter for remaining legacy data and verifies translation
   producer/approval/publication surfaces.

## Product surfaces: working, duplicated, stranded

- **Working:** revision-bound translation dispatch, provider result ingestion,
  human approval evidence, stale-revision gating, target registry, static-site
  target, reconciliation and verification are implemented and tested on Knoxx
  `origin/main` (not on this checkout's older feature branch).
- **Duplicated:** `TranslationPage.tsx` is a separate segment-labeling/SFT UI
  with adequacy, fluency, terminology, risk, corrected text, and labels. It
  still says “GLM-5” and “grounded in OpenPlanner”; it does not project the
  newer publication approval conversation. A generic review queue and chat
  surfaces also exist with separate state models.
- **Stranded/incomplete:** the polished constrained conversation joining source
  evidence, agent proposal, edit/approve/reject, conflicts, and publication
  outcome is not one coherent projection. The default publication receipt sink
  is process-local; agent claims lack crash recovery; asynchronous UI updates,
  explicit conflict handling, and an accessibility acceptance gate are not
  evidenced end to end. Muse does not yet own a translation capability projected
  identically to UI and agents.

## Abstractable concept inventory and recommended homes

| Classification | Knoxx concepts | Recommended boundary / compatibility gate |
| --- | --- | --- |
| Product surface | constrained review conversation, translation queue, publication status | Keep Knoxx as the product facade while host-neutral capability and view contracts move to Muse. No route removal until parity and dependent-client inventory pass. |
| Shared domain law | concrete-revision resolution, stale evidence, publication gate, idempotency identity, target admission | Lift pure `.cljc` laws only after commit/blob-pinned conformance fixtures prove unchanged behavior. Katamorph validates both boundaries. |
| Identity/authority | authenticated reviewer, worker principal, actor/role/capability assignment | Axxium owns actor identity, tenancy, grants, and exactly-one approval owner; Knoxx consumes decisions and retains compatibility checks during migration. |
| Context/memory | source content, translation evidence, labels, session projections | Epiphany supplies historical evidence; host sessions hold bounded views. Do not copy evidence into Muse or promote generated summaries. |
| Workflow/work state | queue status, claims, retries, review state, dependencies | Rheos owns cards/workflow state and recovery checkpoints; operational projections are not evidence authority. |
| Event/receipt | translation-needed, submission, review decision, reconcile/materialization/failure receipts | Clio owns durable lifecycle facts and causal correlation. Replace the process-local receipt journal only through an explicit migration and replay gate. |
| Execution/provider | agent runner, translator capability, Proxx provider behavior, target adapters | Muse describes provider-neutral capabilities and target projection; Proxx-derived routing/policy becomes shared declarative guidance while Proxx remains a compatibility facade. |
| Translation/review | segment scoring, corrected text, revision approval/reject, review policy | Define one canonical proposal/decision contract with distinct evaluation labels and publication authority decisions rather than conflating them. |
| Publication | resource graph, locale catalog, target registry, reconciler, static-site materialization | Preserve Knoxx compatibility APIs; make adapters replaceable and receipts durable before shifting orchestration ownership. |
| UI projection | TranslationPage, TranslationReviewCard, generic review queue, chat | Compile one capability/view model into accessible Knoxx states: loading, unavailable, awaiting proposal, review, saving, conflict, accepted/rejected, publishing, complete, failed/recoverable. |
| Infrastructure | Fastify externs, Mongo stores, Docker/Services workflows, verification scripts | Services owns deploy topology and secrets. Runtime adapters remain outer layers; no infrastructure detail enters domain identity. |
| Compatibility debt | OpenPlanner namespaces/Mongo adapter and UI copy, direct Proxx deployment assumptions, multiple review models | Strangle behind current APIs, inventory consumers, dual-run conformance, publish migration receipts, then deprecate; do not sever continuous resource history. |

## Target vertical slice

The first product slice should define a single revision-pinned translation
review episode: source and EvidenceRefs enter a Muse capability request; an
Axxium-authorized actor invokes provider-neutral execution selected by shared
Proxx-derived policy; Clio records request/proposal/decision/publication facts;
Rheos projects pending work and recovery; Knoxx renders the same capability as
a constrained accessible conversation; approval or rejection produces a
durable receipt; the existing reconciler performs the downstream handoff.

Acceptance must cover keyboard and screen-reader semantics, explicit authority,
selected source/proposal revisions, edit conflicts, reconnect/resume,
idempotency, stale evidence, unavailable providers/stores, asynchronous status,
and traceability from UI decision to publication receipt.

## River City analogy and Witness Thread seam

`octave-commons/River-City` is now inventoried as a direct Foresight submodule.
Its evidenced present role is an EDN-first observability and inference system:
source-backed observations are normalized, scored and projected into reports
and charts while observations remain distinct from interpretations and latent
estimates. It is a separate application, not the receipt lake. Its relevance is
architectural: it applies Katamorph-like resource contracts and Clio-like ledger
interactions to build a data structure as a collection of continuously
identifiable resources.

**Witness Thread** is an older Calliope/lore concept now being considered for a
new architectural application. Calliope describes Receipt River and Witness
Thread as memory infrastructure and glosses Witness Thread as “carry it
through.” Later Knoxx documentation reuses the name for grounded sources,
passive hydration and evidence surfaces. None of that establishes a canonical
warehouse implementation or contract.

The next work is therefore concept archaeology, not subsystem recovery: separate
the narrative lineage from implementation evidence, then decide whether a
portable continuity contract should carry typed identity, provenance, revision
and causal relationships across independently authoritative resources and
Receipt Rivers. A lake/warehouse projection is one possible application, not
the definition of Witness Thread. Clio, Epiphany and Rheos retain their existing
authority boundaries regardless of the eventual projection.
