---
category: "kanban"
labels: "deployment, knoxx, embeddings, cms, source-preservation"
dependency: ["ada3bc51-1528-4ae3-aa46-095d217142ea"]
type: "epic"
write-id: "1791972198458-0.fetkko7sbgiutloe3p3"
points: "59"
title: "EPIC: Preserve retained sources while admitting the native Knoxx CMS embedding cutover"
priority: "P1"
uuid: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
---

# EPIC: Preserve retained sources while admitting the native Knoxx CMS embedding cutover

## Context

The Website is delivered, but the production Knoxx backend remains
`841642adffa2656edd842db72843da0a0f4d8336`, using
`gte-large-en-v1.5`/1024 through Proxx. The intended native CMS uses direct host
Ollama, `qwen3-embedding:8b`/1024, and the configured publication agents.
Equal vector dimensions do not make different models' vectors comparable.

[Services a303](https://github.com/open-hax/services/blob/a3034ccddbc43aecf34a70df444ebe0e5314f83c/digitalocean/services/knoxx/probe-embedding-migration.js)
refuses the old contract while running or stopped, before Mongo inventory.
It admits only an unchanged target or a genuinely unused store with no prior
contract and no graph vector index. It has no migration-proof consumption
branch. This epic supplies the missing application proof and its deployment
integration; it does not reinterpret the existing gate.

Two retained production observations define the starting case, not admission:

- At `2026-10-14T03:51:48.748Z`, Mongo contained 736 events; zero compacted
  memories, event chunks, compacted vectors, graph-node embeddings, vector
  partitions, graph views and graph labels. The graph index was READY and
  queryable. The old writer remained active, and source texts and index
  definition were not returned. Retained capture:
  `prod-goal-production-deployment-status-selected-Mongo-metadata-readonly-0354-v1.stdout`,
  SHA-256 `591dd9a4f6a5ee3a172a972fec700948a3a7cf2844b5a5ede3bca3b764789831`.
- At `2026-10-14T04:04:30.778Z`, the actual pinned SDK eligibility operation
  counted 312 hot-eligible events and 424 `graph.node` events, no `graph.edge`
  or unknown kinds, and 359 blank/missing texts. The index definition was
  `embedding`/1024/cosine with filters `project`, `node_id`,
  `embedding_model`, and `embedding_dimensions`, READY and queryable. Installed
  eligibility module SHA-256 was
  `c1074a3d3a54353f36d29da38254ac137eb480497bc3b0ed914f779f83892d2e`,
  matching pinned SDK source. Retained capture:
  `prod-goal-production-pinned-eligibility-and-graph-index-definition-readonly-0408-v1.stdout`,
  SHA-256 `a876a00b73c609048a79cb165e8a3808c9a6b76dbb9ce0f91b4d182f7efc1f6d`.

Both captures are retained under the operator's
`20261010/review-state/services-02128b-native-review-readback-v1` evidence root.
Their equal counts do not prove a locked snapshot, source preservation, source
reference resolution, complete graph obligations, or a completed migration.
The fresh locked source inventory must determine the actual obligations.

## Outcome

Deploy the new native Knoxx CMS through an accepted controller and Services
caller after generating every required target embedding, proving complete
retained-source coverage, and preserving canonical content and causal history.
Then demonstrate a real Spanish CMS document through Knoxx translation,
persisted review, separate whole-publication approval, reconciliation, and the
public Website browser. Existing publications survive the rollout.

## Scope

Foresight owns these planning cards and dependency coordination. Knoxx owns
portable `.cljc` shapes/laws, its CLJS provider/Mongo/CLI adapters, runtime
integration, migration, and application qualification. Services owns topology,
host exclusion, proof consumption, and deploy admission. Reuse the existing
exported cores of SDK `07085d6557b75834ce6f50e6c54b8ca47e1c7c08`; no SDK
repository change or copied chunking/graph algorithm is included.

| Story | UUID | Points | Immediate prerequisite |
| --- | --- | --- | --- |
| [Validate the shared embedding runtime](knoxx-validated-shared-embedding-runtime-d3277a3c.md) | `d3277a3c-6e30-42b1-8151-16c041612a6a` | 5 | None |
| [Qualify the SDK staging/adoption boundary](select-and-qualify-the-genuine-sdk-staging-and-source-adoption-boundary-5a7ee0c0.md) | `6cc1e1ca-c8dd-4cf9-9cd1-24675a7ee0c0` | 5 | None; parallel to runtime |
| [Materialize and verify retained sources](knoxx-retained-source-embedding-migration-11bc1411.md) | `11bc1411-154d-4db7-8c27-51506ab30508` | 8 | Runtime and SDK prerequisite |
| [Qualify durable scoped publication receipts](knoxx-durable-scoped-publication-receipts-b450407b.md) | `afacb7b7-263a-48e6-a744-a9f4b450407b` | 5 | None; parallel to runtime and SDK |
| [Qualify native translation before deployment](qualify-selected-native-knoxx-translation-and-sme-decisions-before-deployment-5eae82d9.md) | `60e85d88-3960-449f-8376-10e75eae82d9` | 5 | Runtime; owning translation stores |
| [Qualify native content/publication before deployment](qualify-selected-native-knoxx-content-and-publication-decisions-before-deployment-88af373d.md) | `a7402e9d-6538-4e38-9919-93fe88af373d` | 5 | Runtime, durable receipts and translation qualification |
| [Qualify the database-enforced writer fence](qualify-a-database-enforced-cutover-writer-fence-before-deployment-2129f2bf.md) | `c002072c-2415-46a8-b533-6b502129f2bf` | 5 | None; fixture mechanism qualification |
| [Qualify the isolated trusted production gate](qualify-an-isolated-trusted-production-admission-gate-before-deployment-3056b8dc.md) | `dee4ddbf-c00b-4f4a-9f28-a6183056b8dc` | 5 | Migration, both product qualifications and fence |
| [Consume proof and deploy the native CMS](services-controller-bound-embedding-admission-03a057e4.md) | `03a057e4-3098-4fe7-a5b8-95168f45ca96` | 8 | Migration, durable receipts, both product qualifications, fence and gate |
| [Prove live translation and its judgments](prove-native-cms-translation-and-persisted-independent-judgments-after-rollout-3938fca6.md) | `6890a3d4-f3d7-48f1-bd92-f6223938fca6` | 3 | Completed Services deployment |
| [Prove content and public Spanish publication](prove-native-content-judgment-publication-and-public-rendering-after-rollout-986502d1.md) | `4a9f303e-63d1-48d7-a396-b93c986502d1` | 5 | Deployment, translation proof and qualified durable receipts |

The 59 points are the sum of the eleven authored children, not an additional
implementation estimate. Four additional five-point prerequisites were created
through canonical Rheos, with actual incoming metadata and task-created events.
They qualify the two native product paths, the database fence and the isolated
trusted gate before Services consumes their accepted interfaces. None depends
backward on Services or a later live proof. Estimates remain subject to planning review. Rheos
created the three additional children and recorded the migration dependency
vector and aggregate estimate through its actual canonical operations. The new
durability child is an initial first-class Markdown card; no creation event or
readiness is claimed. Actual Rheos operations link its UUID into Services and
content dependencies and update the aggregate to 39. Knoxx exclusively owns its
application adapter/runtime implementation and qualification; Foresight owns this
coordination. Its accepted fixture qualification precedes rollout and never waits
on the later production Spanish receipt. The
SDK prerequisite proves the supported
source families, complete emitted output closure, identity-conflict and transfer
rules, and the separate authorized append-delta decision before choosing a route.
It supplies a qualified interface and recoverable fixture proof, not production
migration or a fabricated source-admission result.

Keep the two existing product-proof owners distinct:
[translation generation and SME review](prove-translation-generation-and-sme-review-independently.md)
(`abdd5a2d-dbad-46a0-ba3f-2149dfeafbcf`) and
[content generation, layout, publication and review](prove-content-generation-layout-publishing-and-review-independently.md)
(`3fd1fdf7-dc2e-4b2a-bb5a-7979a686529e`). The bounded predeployment qualification cards `60e85d88-3960-449f-8376-10e75eae82d9` and
`a7402e9d-6538-4e38-9919-93fe88af373d` provide this deployment's separate accepted current-source
vertical admission evidence. The broad extraction owners are not those entry
prerequisites, and a cleaned-up fixture decision cannot authorize a new fixture. Their broader entry conditions
and state remain unchanged. The two bounded rollout-proof children above own
this deployment's independent final live receipts and epic closure join.
The independent fence and trusted gate prerequisites supply accepted capabilities
and hosted fixture evidence; EC1.03 records their actual production use.
EC1.03 owns orchestration and deployment
receipts and completes on its qualified/admitted deployment receipt, health and
source readback without awaiting the later live proof cards. Those cards then
complete the live product/browser join required for epic closure. EC1.03 does not
own implementation or closure of those two product workstreams. The bounded
children retain separate tests and receipts; one combined journey cannot close
both independent slices or expand the eight-point Services story.

The separately estimated [Rheos metadata operations prerequisite](apply-adopted-rheos-metadata-repair-to-foresight-cutover-cards-217142ea.md)
(`ada3bc51-1528-4ae3-aa46-095d217142ea`, three points, standalone outside the eleven children)
consumes accepted owner source `775ab0be-90fe-469f-89e3-e6ac07b031ce` and its
qualified recovery prerequisite. It then persists the four missing initial
statuses and explicitly omits the SDK and independent-fence empty dependency
headers through canonical operations. This local prerequisite precedes cutover
planning closure; external owner source remains an explicit body reference,
not an unsupported cross-board executable edge. Those real operational effects,
readiness and the remaining initial-status finding are still pending. No fake
lifecycle round trip, manually edited established metadata or creation history
stands in for them. The 59-point child aggregate excludes this standalone
three-point operational prerequisite.

Pinned SDK07085 has no standalone exported graph-input transform. Its complete
owner `ingestEvents` operation appends base rows and emits additional derived
events, graph edges and labels. An isolated staging database is an examined,
unaccepted route to reuse that operation. Planning review must settle all staging
outputs, source identities, conflicts and any explicit new-source append/adoption
obligation. A small zero-obligation profile cannot satisfy the observed case.

## Non-goals

A fresh-store relabel, old-model completion path, manual target receipt,
deployment-gate override, reset/deletion of retained data, re-insertion of
historical events into the production canonical store, OpenPlanner companion deployment or repository changes,
review-policy change, or board-engine implementation. This planning PR does not
implement or execute migration, assign future accepted commits, or admit a
board transition.

## Acceptance criteria

- [ ] Every actual future Knoxx ingest, hot/compact query and repair path in
  scope uses one validated model/route/dimension-bound generation runtime.
  Actual provider results account for every input and contain only finite
  1024-dimensional vectors; a partial batch cannot supply qualification.
  Any generation failure refuses the qualification run, even if a later retry
  produces complete input coverage.
- [ ] A recoverable snapshot and exact rollback journal precede mutation.
  Under a held deployment lock, complete stopped-writer proof and an exercised
  Mongo-enforced fence rejecting every other writer throughout the cutover, the owning
  CLI enumerates and preserves the full canonical source and causal history,
  materializes all proven hot and supported graph obligations, and verifies
  exact source-to-vector/chunk coverage. Any new canonical/derived source facts
  have a separately authorized append decision bound to the exact delta hash,
  identities, source/controller, run and attempt before production adoption;
  discovery and apply cannot authorize their own output. This accepted delta has
  source provenance separate from derived projection writes; whole-store counts reconcile originals plus that
  delta, rather than requiring count equality. They cannot be deleted in rollback.
  Unknown/colliding source identities and unsupported families refuse; content,
  title or model agreement cannot merge identities.
- [ ] Services consumes a freshly recomputed application proof bound to the
  same database, application source, accepted controller, caller, native run
  and attempt. Writer restart, drift, stale/borrowed proof or source change
  refuses. Existing unchanged-target/fresh-store behavior retains its gates.
- [ ] Original application-owned production qualification, controller/caller
  attestation and protected review remain required. A separately named trusted
  production-admission decision follows the applicable staging/promotion path,
  consumes its exact receipt and binds the PR head, current base and tested merge
  candidate alongside C/S/G. Unknown/stale staging proof, changed base/candidate
  or missing authorized approval refuses production activation. Neither this plan
  nor the Website-only exception supplies Knoxx qualification.
- [ ] The separately owned translation and content-product slices each retain
  their own current-source vertical qualification and actual live decision/effect
  receipts. Epic closure joins their exact Spanish revision with the Services
  deployment and public browser receipt; a combined journey is not independent
  closure of either owner. The Website, existing English/German publications,
  Proxx and SDK settings are preserved unless separately reviewed changes are required.

## Verification

Planning review assesses scope, estimates, dependencies, SDK ownership seams,
coverage and recovery. Rheos owns admission and readiness after that review.
Implementation starts with meaningful red laws and controls, then the owning
Knoxx runtime/CLI and Services adapter. Run the genuine required owner suites
with zero errors/warnings, the same-source integration/e2e qualification and
four 250-mutation batches with 1,000 distinct fingerprints, followed by the
actual hosted attestations and locked deployment proof. Re-read source
commitments after materialization and verify live CMS/API/browser behavior.
No historical capture, local fixture, passing model canary or count alone is a
substitute for any required gate.

## Risks

The 424 graph records need source-family closure, and ordinary hot sources can
also imply graph materialization. Empty graph vectors are not zero graph work.
The SDK factory creates indexes and the default provider omits the requested
dimensions; neither may be used as a claimed read-only or validated boundary.
Reusing the graph owner operation requires explicit isolation and complete
output/adoption custody; graph edges, labels and new source facts cannot be
silently discarded. Revise and review any missing ownership boundary before code.