---
category: "kanban"
labels: "services, deployment, embeddings, cms, 8sp"
story_id: "EC1.03"
dependency: ["11bc1411-154d-4db7-8c27-51506ab30508", "afacb7b7-263a-48e6-a744-a9f4b450407b", "60e85d88-3960-449f-8376-10e75eae82d9", "a7402e9d-6538-4e38-9919-93fe88af373d", "c002072c-2415-46a8-b533-6b502129f2bf", "dee4ddbf-c00b-4f4a-9f28-a6183056b8dc"]
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
type: "story"
write-id: "1791971867208-0.pzk37r3d841g6e2l39"
points: "8"
title: "EC1.03: Consume controller-bound embedding proof and deploy the native Knoxx CMS"
priority: "P1"
epic: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
uuid: "03a057e4-3098-4fe7-a5b8-95168f45ca96"
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

Also requires the independently qualified
[durable Knoxx publication-receipt capability](knoxx-durable-scoped-publication-receipts-b450407b.md)
(`afacb7b7-263a-48e6-a744-a9f4b450407b`). Its accepted application source/build,
effective adapter/storage configuration and independent scoped readback contract
must be part of the selected candidate before rollout. Services verifies that
handoff and retains the owning source qualification; it does not implement the
adapter or wait for the later live Spanish receipt. Missing sink, changed storage
or retention configuration, source/build mismatch or unqualified persistence
refuses admission. Its full handoff consumes separately owned Knoxx attempt recovery
`e945de08-58ec-4028-b8cb-5dbd4a1450f4` and Services persistent storage/replacement
qualification `a3ca141e-865e-4c35-8053-53084996f2bc`. This eight-point consumer
uses their accepted interfaces and actual persistent mapping; it does not absorb
their implementation. Recheck selected directory, host/backend, mount, scoped
schema/ledger and permission/lifetime identity against actual replacement readback.
Missing custody, ephemeral/remapped storage or a same-volume process-restart-only
receipt refuses admission. Unknown effect/callback outcome cannot authorize unsafe
automatic repetition. Rheos records the AFAC prerequisite alongside migration;
the newly proposed owner links await root's actual canonical registration.

Consume four other separately estimated prerequisites before production admission:
[predeployment translation qualification](qualify-selected-native-knoxx-translation-and-sme-decisions-before-deployment-5eae82d9.md) (`60e85d88-3960-449f-8376-10e75eae82d9`),
[predeployment content/publication qualification](qualify-selected-native-knoxx-content-and-publication-decisions-before-deployment-88af373d.md) (`a7402e9d-6538-4e38-9919-93fe88af373d`),
[database-enforced writer-fence capability](qualify-a-database-enforced-cutover-writer-fence-before-deployment-2129f2bf.md) (`c002072c-2415-46a8-b533-6b502129f2bf`), and
[isolated trusted production-admission gate](qualify-an-isolated-trusted-production-admission-gate-before-deployment-3056b8dc.md) (`dee4ddbf-c00b-4f4a-9f28-a6183056b8dc`).
Their accepted source/interface and genuine hosted fixture qualification are inputs;
the actual production window, authorization, cutover and activation receipts are
outputs of this Services story. None of those prerequisites waits on EC1.03 or
either later live proof. Services consumes their qualified interfaces instead of
hiding independently deliverable implementation in its eight-point estimate.

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
receipts while preserving current publications and dependencies. EC1.03 completes
on the qualified/admitted deployment, its actual deployment receipt, health and
source readback; completion does not await either later live proof card. The named predeployment translation and content qualification cards deliver
independent vertical admission evidence. Their separate rollout-proof cards
deliver final live receipts; the epic joins those to public Website rendering. Services does not implement those product operations or claim their
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
throughout inventory, apply, final verification and activation. The accepted [c002072c-2415-46a8-b533-6b502129f2bf fence capability](qualify-a-database-enforced-cutover-writer-fence-before-deployment-2129f2bf.md) names and
qualifies its database-side mechanism and acquisition/loss/recovery/release
contract independently. This consumer invokes that exact interface and records
the actual production window; credentials and policy decisions stay local. Test an unregistered client
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

Consume the independently qualified [trusted production-admission gate](qualify-an-isolated-trusted-production-admission-gate-before-deployment-3056b8dc.md)
(`dee4ddbf-c00b-4f4a-9f28-a6183056b8dc`) after applicable staging/promotion and before production
mutation or activation. Its actual trusted App or independently pinned protected-base
workflow, gate code/configuration and required App identity remain outside candidate
control. Candidate jobs/builds/hooks have read-only tokens and no deployment/signing
secrets, including same-repository candidates. Privileged gate execution never
checks out, imports, builds or executes candidate code or hooks; bounded artifacts
are checked by the accepted verifier. Unknown custody or candidate control refuses
both pre-mutation authorization and final activation.
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

The precise predeployment qualification owners are
[translation and SME decisions](qualify-selected-native-knoxx-translation-and-sme-decisions-before-deployment-5eae82d9.md) (`60e85d88-3960-449f-8376-10e75eae82d9`) and
[content/publication decisions](qualify-selected-native-knoxx-content-and-publication-decisions-before-deployment-88af373d.md) (`a7402e9d-6538-4e38-9919-93fe88af373d`). Before production admission, consume their separate
accepted exact-source vertical qualification. The broader extraction owners
`abdd5a2d-dbad-46a0-ba3f-2149dfeafbcf` and
`3fd1fdf7-dc2e-4b2a-bb5a-7979a686529e` retain their post-inflight entry conditions,
scope and state; their incomplete broad work is not relabeled as this qualification. After deployment,
the bounded [translation rollout proof](prove-native-cms-translation-and-persisted-independent-judgments-after-rollout-3938fca6.md)
(`6890a3d4-f3d7-48f1-bd92-f6223938fca6`) and
[content/publication rollout proof](prove-native-content-judgment-publication-and-public-rendering-after-rollout-986502d1.md)
(`4a9f303e-63d1-48d7-a396-b93c986502d1`) supply independent live receipts as epic
closure inputs. Rheos recorded their dependencies on this completed deployment;
this story does not await their completion. The broader owners' existing entry
conditions and state remain unchanged. Those
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
- [ ] AFAC supplies the separately accepted attempt-recovery and persistent storage/
  replacement capabilities. The selected receipt directory resolves to their named
  actual storage outside the image, with matching host/backend, mount, scope,
  permissions and complete schema/ledger custody. Original receipt/history/public
  bytes survive fixture container/image replacement and remount through independent
  readback. Missing recovery, ephemeral selection or lifetime drift refuses rollout.
- [ ] Existing Website, English/German publications, content-root ownership,
  Proxx configuration and SDK settings survive with fresh health/public checks.
  Recovery uses the snapshot/journal and owning compatibility proof; environment
  rollback alone cannot be credited after partial migration.
- [ ] The independently qualified trusted App or pinned-base gate has actual
  hosted isolation evidence outside candidate control; candidate jobs/hooks have
  read-only tokens and no deployment/signing secrets, and privileged gate execution
  runs no candidate code. Unknown custody or candidate-controlled verifier,
  configuration or App identity refuses both authorization and activation.
  A distinct trusted production-admission result names its authorized
  approver/gate and binds the exact staging/promotion receipt, purpose, source,
  PR head/base/tested candidate and current run/attempt before rollout. Missing,
  stale, foreign or wrong-authority inputs refuse; proposed infrastructure is
  never reported as activated deployment evidence.
- [ ] Consume separate exact-source predeployment translation and content-product
  vertical qualification as application admission inputs. Complete EC1.03 on the
  qualified/admitted deployment and its own exact revision-bound deployment receipt,
  health and source readback. Completion does not await later live translation/SME,
  content/product judgment, publication or public-browser receipts. Those outputs
  belong entirely to the rollout-proof cards and epic closure; no combined journey
  closes either product owner or adds its implementation scope to this story.
- [ ] Predeployment application/provider qualification and actual deployed runtime/
  source custody bind e2b/thinking-off/direct-Ollama for translation and drafting,
  and qwen/1024 for SDK ingest/search/repair. A host catalog, local canary or prose
  setting alone cannot establish those paths. The subsequent production product
  effect receipts belong to the later rollout-proof cards, not EC1.03 completion.

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
query, then record EC1.03's qualified/admitted deployment receipt, health, source,
IDs, hashes, times and attempt provenance. These are this story's completion evidence.
After EC1.03 completes, the separate rollout-proof cards retain their actual live
vertical/product receipts and the epic joins the approved Spanish revision to the
public browser. Those later actions do not block EC1.03 completion and remain
pending epic obligations until genuinely observed.

## Risks

Activation may populate new projections after an empty-source observation,
invalidating a reused proof. Writer closure can be broader than Compose. Host
model availability, provider dimension support, SDK binary drift, Atlas pending
index definitions and application/controller stack admission must all be
observed. Eight points covers consuming the separately qualified product, fence and gate
interfaces and producing the qualified deployment receipt;
new platform or cross-repository changes require separate reviewed scope.