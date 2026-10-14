---
category: "kanban"
type: "story"
story_id: "EC1.03"
uuid: "03a057e4-3098-4fe7-a5b8-95168f45ca96"
epic: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
dependency: ["11bc1411-154d-4db7-8c27-51506ab30508"]
title: "EC1.03: Consume controller-bound embedding proof and deploy the native Knoxx CMS"
priority: "P1"
points: "8"
labels: "services, deployment, embeddings, cms, 8sp"
---

# EC1.03: Consume controller-bound embedding proof and deploy the native Knoxx CMS

## Context

Requires [EC1.02](knoxx-retained-source-embedding-migration-11bc1411.md)'s
qualified CLI/interface and complete recoverable fixture qualification. Its actual
production proof is an output of this story's accepted controller invoking that
CLI in the current fenced run/attempt, not a prerequisite from an older caller.
`open-hax/services` owns
this consumer, host exclusion and deployment orchestration; application
migration and coverage semantics remain in `open-hax/knoxx`.

[The adopted a303 gate](https://github.com/open-hax/services/blob/a3034ccddbc43aecf34a70df444ebe0e5314f83c/.github/workflows/deploy-digitalocean.yml#L336-L494)
refuses the old gte contract before inventory, even when stopped. Its recovery
receipt records prior target approval and is not a migration certificate. A new
reviewed consumption branch is required; neither successful backfill alone nor
a target receipt manually written by an operator changes that law.

## Outcome

One trusted Services controller obtains the distinct production gate's
pre-mutation authorization from exact staging/promotion qualification, then
invokes the qualified application cutover under a held host lock, exercised
database fence and closed stopped-writer scope. The gate's final check consumes
the fresh actual cutover proof before activating the accepted native CMS candidate. This story delivers exact orchestration/admission/deploy
receipts while preserving current publications and dependencies. The separate
Knoxx translation and content-product owners deliver their independent vertical
qualification and final live receipts; the epic joins them to public Website
rendering. Services does not implement those product operations or claim their
closure from one combined Spanish journey.

## Scope

Consume the Knoxx decision rather than implementing source/graph eligibility,
chunking, vector coverage or migration in Services. Use its compiled CLI from
the exact qualified image and pass explicit expected identity fields. Preserve
the existing unchanged-target/fresh-store branches and receipt recovery guards.

Keep the deployment chain explicit: **C** is the actual accepted protected
Knoxx controller; **S** is the selected immutable application source, an ancestor
of C with identical qualified executor inputs and matching physical source
closure; **G** is the accepted Services main/caller. Read these actual commits
after admission, rather than selecting future values in this card. Update the
caller pin only after C is adopted. Same-run/attempt artifact custody binds C,
S, G, image/CLI/SDK hashes, database identity, source and target model/route,
registered stopped-writer identities, held lock and fresh source commitments.
The same artifact also binds the relevant PR head, fresh protected-base SHA and
tested merge-candidate SHA. Recompute the candidate against that base; a changed
head/base/candidate invalidates qualification even when C/S/G remain unchanged.

The host lock serializes deployment and remains held through migration, final
verification, activation and admission readback. It does not by itself exclude
external Mongo writers. In addition to enumerated actual stopped process/
container identities, require a reviewed Mongo-enforced write fence that leaves
only the bounded migration/admission authority able to mutate the affected stores
throughout inventory, apply, final verification and activation. The implementation
must name and exercise its actual database-side mechanism and recovery/release
procedure; credentials and policy decisions stay local. Test an unregistered client
with otherwise valid write authority and require its write to be rejected during
the entire window. A host lock, declared process list, repeated equal counts or
an unenforced generation label is insufficient. Unknown closure, fence loss,
another attempt, restart or source/database drift refuses. Preserve the normal recoverable snapshot and
qualified journal-controlled recovery. Any application-produced new source facts
require a distinct authorized decision for the exact discovered append delta and
its source/controller/caller/database/run/attempt bindings before adoption. The
consumer verifies that decision and cannot generate its own acceptance, hide new
facts as projection-only changes or authorize their removal in rollback.
Do not enable an old writer over a
partially materialized target store without the owning verified recovery proof.

Host bootstrap installs the actual target model and qualifies its container-origin
reachability. Effective publication configuration must prove the deployed
overlay `gemma4:e2b` with thinking off through direct host Ollama; portable
31b/medium authored defaults are not that runtime observation. Preserve the
existing Proxx source/settings, pinned in-process SDK, Website image/config and
publication content root unless a separately reviewed change is necessary.

A separately named trusted production-admission phase is required after the
applicable staging and promotion steps and before production mutation/activation.
Its reviewed implementation names the actual gate and authorized approver,
consumes the immutable staging/promotion receipt and intended production/database
identity, and binds exact C/S/G plus PR head/base/tested merge candidate, image and
current run/attempt. Until Promethean activation is actually verified, retain the
repository's existing authorized promotion path and explicitly qualify this
production decision there; do not claim proposed staging/controller infrastructure
is active. Missing/foreign/stale staging receipt, wrong purpose or approver,
changed base/candidate, failed application qualification or an unknown production
admission result refuses rollout. The named gate must distinguish pre-mutation authorization from final activation
admission: fresh production cutover proof is generated by the authorized operation
and checked before activation, not demanded before the operation can run.
A host cutover success is not that decision.

The application proof owners are
[translation generation and SME review](prove-translation-generation-and-sme-review-independently.md)
(`abdd5a2d-dbad-46a0-ba3f-2149dfeafbcf`) and
[content generation, layout, publication and review](prove-content-generation-layout-publishing-and-review-independently.md)
(`3fd1fdf7-dc2e-4b2a-bb5a-7979a686529e`). Before production admission, consume
separate exact-source vertical qualification from both owners. After deployment,
retain their independent live receipts as epic closure inputs. Their existing
entry conditions and Rheos state remain unchanged. Planning must bind actual
UUID dependency edges, or author separately estimated bounded Knoxx rollout-proof
substories if these broader successor cards cannot supply this evidence. Those
product scopes are excluded from EC1.03's eight-point implementation estimate.

## Non-goals

Services application source, secrets in evidence, a second migration verifier,
manual proof/receipt overrides, controller-pin changes before protected adoption,
a zero-evidence exception, weakening original review or mutation qualification,
old compatibility CMS completion, or unreviewed OpenPlanner changes.

## Acceptance criteria

- [ ] Fresh exact C/S/G, PR head, protected base, tested merge candidate, source
  closure and image/CLI/provider/SDK identities match native qualification and
  attestations for the current run and attempt. Reconstruct the tested candidate
  from the fresh base/head rather than borrowing an older candidate receipt.
  Wrong caller/controller/source/database, unknown/colliding source identity,
  borrowed/expired evidence or a changed head refuses before mutation or activation.
  Content, title or model agreement cannot merge source identities.
- [ ] One held host lock, actual complete stopped-writer proof and the exercised
  Mongo-enforced fence cover inventory, apply, verify and activation. An actual
  unregistered-writer attempt is rejected; concurrent attempt, missing writer,
  fence/lock loss, writer restart or source drift invalidates proof. Services invokes the owning CLI
  and does not reproduce its source or graph semantics.
- [ ] Fresh recomputation verifies EC1.02's complete original-source preservation,
  the explicit accepted canonical append delta/provenance separately from
  projection writes and reconciled original-plus-delta counts, target coverage,
  exact graph index definition/readiness and effective target runtime. An old active/stopped receipt or container cannot be relabeled as
  admitted target state; update recovery evidence only through the reviewed
  successful transition.
- [ ] Original application-owned integration/e2e qualification and four batches
  of 250 mutations, with 1,000 distinct fingerprints, pass for the selected
  source and current attempt. Official controller/caller attestation checks,
  protected review and all required native gates remain satisfied. The exact
  Website exception does not apply to Knoxx.
- [ ] Existing Website, English/German publications, content-root ownership,
  Proxx configuration and SDK settings survive with fresh health/public checks.
  Recovery uses the snapshot/journal and owning compatibility proof; environment
  rollback alone cannot be credited after partial migration.
- [ ] A distinct trusted production-admission result names its authorized
  approver/gate and binds the exact staging/promotion receipt, purpose, source,
  PR head/base/tested candidate and current run/attempt before rollout. Missing,
  stale, foreign or wrong-authority inputs refuse; proposed infrastructure is
  never reported as activated deployment evidence.
- [ ] Consume separate exact-source translation and content-product vertical
  qualification for application admission. After rollout, the owning Knoxx cards
  produce separate live revision-bound translation/SME and content/publication/
  product-review receipts; Services records their IDs with its deployment receipt
  for the epic's public browser join. No combined journey closes either owner or
  adds its implementation scope to this Services story.
- [ ] Effective provider evidence confirms e2b/thinking-off/direct-Ollama for
  translation and drafting, and qwen/1024 for SDK ingest/search/repair. A host
  catalog, local canary or prose setting alone cannot establish those paths.

## Verification

Run the genuine Services owning contract/workflow/deployment tests with zero
errors/warnings, including refusal controls for stale proof, a stopped old
contract, source-preserving migration failure, writer restart, lock loss,
concurrent attempt and same-count source drift; actual unregistered Mongo writes;
wrong/missing append acceptance; stale PR base/tested candidate; and missing,
wrong-authority or stale staging/promotion production-admission inputs. Preserve
existing gate controls.
Then obtain original application qualification, official current-attempt
attestations and trusted native deploy evidence. Under the host lock, retain
fresh before/after source and index proof, actual container health and semantic
query. The separate Knoxx proof owners then retain their individual live vertical
receipts; the epic joins the accepted Spanish revision to the public browser.
Record actual
IDs, hashes, times, sources and attempt provenance; missing actions stay pending.

## Risks

Activation may populate new projections after an empty-source observation,
invalidating a reused proof. Writer closure can be broader than Compose. Host
model availability, provider dimension support, SDK binary drift, Atlas pending
index definitions and application/controller stack admission must all be
observed. Eight points covers this bounded consumer and actual rollout proof;
new platform or cross-repository changes require separate reviewed scope.
