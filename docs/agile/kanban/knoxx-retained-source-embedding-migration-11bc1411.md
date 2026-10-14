---
category: "kanban"
labels: "knoxx, embeddings, migration, source-preservation, 8sp"
story_id: "EC1.02"
dependency: ["d3277a3c-6e30-42b1-8151-16c041612a6a", "6cc1e1ca-c8dd-4cf9-9cd1-24675a7ee0c0"]
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
type: "story"
write-id: "1791956986109-0.quufze9r0rkkfnbfwnh"
points: "8"
title: "EC1.02: Materialize and verify retained Knoxx sources without rewriting history"
priority: "P1"
epic: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
uuid: "11bc1411-154d-4db7-8c27-51506ab30508"
---

# EC1.02: Materialize and verify retained Knoxx sources without rewriting history

## Context

Requires [EC1.01](knoxx-validated-shared-embedding-runtime-d3277a3c.md)'s exact
validated runtime and the separately reviewed
[SDK-boundary prerequisite](select-and-qualify-the-genuine-sdk-staging-and-source-adoption-boundary-5a7ee0c0.md)
(`6cc1e1ca-c8dd-4cf9-9cd1-24675a7ee0c0`).
That prerequisite must select and prove the genuine owner operation, supported
source families, isolated database identity, complete output closure, transfer/
conflict rules and the independent append-admission phase on a recoverable
fixture before this implementation is ready or its eight-point estimate is
accepted. Rheos recorded both actual prerequisite UUIDs in this card's dependency
vector. Their existence does not admit an implementation route or readiness.

`open-hax/knoxx` owns the implementation; Services consumes its application decision later. The [epic](epic-source-preserving-knoxx-embedding-cutover-82c55f16.md)
binds the actual unlocked observations: 736 retained events, 312 hot-eligible
and 424 `graph.node` records, with initially empty derived vector stores.
Those counts are not a locked inventory or the number of required graph chunks.

The actual pinned public `shouldIndexEventHotVectors` operation must classify
hot sources. Reuse exported SDK preparation/materialization boundaries for hot
and supported graph sources without admitting historical records again into
production canonical or causal-history collections. The graph source-family and ordinary-event-derived graph
closure must be established before implementation; an inaccessible internal
algorithm is not permission to reproduce it in Knoxx. Pinned SDK07085 has no
standalone exported graph-input transform: its full `ingestEvents` owner path
uses an internal protocol constructor, always inserts base events through
`MongoEventAdmission.appendEvent`, and creates derived events, graph edges and
label nodes. This is an actual source boundary, not a settled staging design.

## Outcome

An application-owned CLJS CLI inspects, materializes and verifies every proven
retained hot and supported graph obligation using the validated target runtime,
while preserving every canonical event, source field and causal-history record.
It delivers a qualified CLI/interface, exact rollback journal and complete
source-preserving fixture proof. The actual production cutover proof is emitted
only when EC1.03's accepted controller/caller invokes that qualified CLI in its
fresh locked production run/attempt; it is not a prerequisite produced before
that caller exists. A zero-obligation-only profile cannot admit the observed case.
Any newly generated canonical/derived source fact must be exposed and admitted
through an explicit reviewed append obligation, preserving the original corpus.

## Scope

Use portable `.cljc` artifact/preservation/coverage laws and named Knoxx CLJS
Mongo/provider/CLI extern adapters. Plan bounded `inspect`, `apply`, `verify`
and journal-controlled `rollback` operations. Read-only operations open a plain
Mongo read connection and never call `createOpenPlannerSdk`/`openMongoDB`, create
indexes, write caches, generate embeddings or mutate source/derived state.

A fresh closed-writer inventory enumerates complete canonical source records,
unique IDs, types and causal references with a deterministic lossless commitment
codec. Preserve the original retained-snapshot count and full content commitments, not
selected fields or count equality. Any additional canonical facts require an
explicit accepted append delta with source provenance, separate from derived
projection writes. Whole-store totals must equal preserved originals plus that
accepted delta; unchanged total counts are not required when appends are allowed.
Resolve retained source/hydration obligations; fail on
unknown or inaccessible source rather than treating blank text as zero work.

Materialization uses the existing pinned SDK owner operations and EC1.01 runtime,
not production base-event reinsertion or copied chunking/graph projection algorithms.
An isolated staging Mongo database is a candidate way to use the complete owner
operation. Planning review must bind its actual distinct database identity,
immutable source copies, all emitted source/vector/edge/label outputs, existing-ID
conflicts and explicit adoption manifest before approving that route. No fake
admission result or selectively dropped projection can supply coverage.

Discovery/isolated generation, independent admission, and production adoption are
separate named phases. Discovery freezes the whole proposed append delta, source
and output identities, counts, full commitments and hash. Before production
adoption, an authorized decision-maker distinct from the generating/apply process
must issue an inspectable acceptance bound to that exact delta, source snapshot,
controller/caller/database, native run and attempt. The prerequisite design names
the actual accepting authority and verified decision channel; neither this card,
an SDK return value nor a self-authored manifest selects it. Missing authorization,
changed delta/source/binding, partial output or a decision from the generator
refuses adoption. A fresh controller phase verifies the accepted decision against
the still-fenced snapshot before any production append; rejected/unknown deltas
remain unadopted with their genuine staging evidence retained.

Support the observed graph source families through their actual owner boundary,
including graph work implied by ordinary eligible events. Prove exact expected
source-to-node/chunk/model/dimension coverage, no missing/extra/orphan chunks,
and absence of legacy query-visible partition or graph identities. Inventory
registry-discoverable physical collections as well as all four named stores.

Before mutation, capture the normal recoverable snapshot and journal exact owned
derived before-images, operation identities and source/controller bindings.
Resume and rollback must be bounded and deterministic; original canonical
source/causal history is never their mutation surface. New canonical facts, if
explicitly admitted, remain append-only and cannot be removed by rollback. The observed existing graph index can
be reused only after fresh exact definition, pending-definition, READY and
queryability checks. Unknown/populated graph or compact families refuse until a
complete owner procedure for the discovered family is planned and reviewed.

## Non-goals

Deleting/resetting canonical data, re-admitting historical events into production, blanket
collection deletion, relabeling retained sources as fresh, changing SDK source,
copying internal owner algorithms, hiding partial generation, or implementing a
universal unreviewed graph/compact migration. A count-only or preserved-unindexed
certificate cannot complete the actual production case.

## Acceptance criteria

- [ ] `inspect` classifies the fresh complete source set through actual pinned
  SDK eligibility/preparation boundaries, exposing all positive, zero and
  unknown obligations. The 312/424 observation cannot replace this enumeration.
- [ ] Under the same held lock and proven stopped writer set, `apply` generates
  every required hot and supported graph chunk through EC1.01. Partial batches,
  unsupported producer/family, unavailable hydration or incomplete graph closure
  refuse completion and retain exact recoverable progress.
- [ ] Every original canonical source ID, retained-corpus count, complete
  field/type commitment and causal-history record is identical before and after
  apply, verify, resume and rollback. No SDK ingest/admission operation reinserts
  historical sources into production. Any new canonical/derived source facts
  have an explicit accepted append delta with source provenance, identities and
  counts, separate from derived projection writes; whole-store totals reconcile
  exactly to preserved originals plus those admitted facts. Total-count equality
  is not simultaneously required when this accepted delta is nonempty.
- [ ] A selected isolated-SDK route enumerates all actual staging outputs and
  source identity, including graph edges and labels, exposes existing-ID
  conflicts, and verifies complete hot/graph obligations before adoption.
  Unknown/colliding source identities or silently discarded output refuse; never
  merge by content, title, embeddings or model agreement. A fake native append
  result cannot stand in for the real isolated owner operation.
- [ ] `verify` recomputes complete source-to-vector/chunk correspondence and
  exact qwen/1024 identity across all stores and physical partitions, rejects
  orphan/legacy rows, and proves the actual embedding/1024/cosine graph index
  and required filters READY/queryable without creating/reconciling it.
- [ ] Actual snapshot and rollback journal bind exactly the touched derived
  resources. Interruption/retry/rollback controls preserve canonical history
  and all explicitly appended facts, refuse changed source/controller/database/
  attempt, and prove a recoverable compatible state before any old-writer restart.
  Rollback cannot delete admitted source facts or rewrite their history.
- [ ] The immutable artifact binds compiled CLI/provider/SDK hashes, candidate
  image/application source, accepted controller and Services caller, database
  fingerprint, writer/lock evidence, native run/attempt, source commitments,
  inventory/index definition and refreshed completion time. The qualified fixture
  interface accepts these expected bindings; the actual accepted C/S/G and
  production run/attempt are supplied and freshly verified by EC1.03, not selected
  or minted in advance by this story. Credentials/raw
  source text are absent from public evidence; unknown inputs remain unknown.
- [ ] A fresh final read immediately before activation invalidates evidence on
  source drift, writer restart, expired lock, populated unsupported store or
  changed run/attempt/source/database. Copied artifacts cannot supply admission.

## Verification

Write causal red controls for same-width wrong-model rows, omitted/duplicate
chunks, content changes with equal counts, graph source omissions, unavailable
hydration, legacy registries, wrong/pending index definitions, interrupted apply,
partial generation, resumed attempts and exact rollback. Exercise missing/wrong
append authority, a same-length changed delta, decision-before-discovery and
self-accepted generator output: all refuse production adoption. Read-only spies require
zero constructor/index/write/cache/provider effects. Use the actual SDK export
and whole generated batch rather than lookalikes or expected declarations.

Run genuine Knoxx owner suites and qualification with zero errors/warnings;
then exercise the exact candidate image on a recoverable owned fixture with
retained ordinary and graph sources, including before/after source commitments
and semantic query. Production application remains gated by EC1.03's original
qualification and host/controller admission. No migration is executed during
planning.

## Risks

### Eight-point implementation breakdown and split condition

Eight points is a conditional estimate for one migration lifecycle after EC1.01
and the SDK-boundary prerequisite have been accepted. Those dependencies own
runtime/provider qualification and selection of the genuine staging/adoption
boundary; this story consumes their accepted interfaces. The estimate allocates
the following efforts, including the corresponding causal refusal tests:

| Effort | Points | Bounded deliverable |
| --- | ---: | --- |
| Inspect and preservation commitments | 1 | Plain-Mongo inventory, pinned source eligibility, lossless original-source commitments and unknown-family refusal. |
| Materialize and adopt accepted output | 2 | Invoke the selected owner operation in its isolated database, freeze its complete output delta, verify the independent decision, and apply only that accepted delta through the qualified interface. |
| Verify coverage and query identities | 2 | Recompute hot/graph source-to-chunk coverage, physical partition and legacy-identity checks, exact index readiness, and a semantic query on the owned fixture. |
| Journal, resume and rollback | 1 | Record owned derived before-images and operation bindings; restore a compatible derived state without removing original or newly admitted source facts. |
| Lifecycle qualification | 2 | Run the retained ordinary/graph fixture through inspect/apply/verify, interruption/resume and rollback; exercise changed snapshot/delta/authority/attempt refusals and qualify the candidate CLI/image interface. |
| Total | 8 | One complete source-preserving lifecycle; none of its required phases is deferred to claim completion. |

This budget assumes the prerequisite proves a reusable exported SDK route for
all discovered supported families, and names an already qualified independent
accepting authority and decision channel. It does not include changing SDK
exports, inventing graph/compact algorithms, building a new acceptance service,
or implementing the Services production caller. If prerequisite review or the
first complete fixture shows any of those assumptions false, or reveals a source
family needing a separate owner procedure, stop before the affected mutation.
Record the concrete additional deliverable and split it into independently
estimated, reviewed owning work through Rheos before readiness/implementation
continues. Preserve this story's full lifecycle criteria and re-estimate the
remaining work; eight points is not a hard cap that permits dropping a phase.

The 424 graph records may include multiple source families; ordinary events can
produce more graph work. Existing exports may not expose complete materialization
without source writes. An isolated-SDK staging database proposal is being
examined; it is not implemented or accepted. Planning review must settle its
source-copy, graph transformation/replay, derived-output transfer, database
identity, admission and rollback boundaries before use. Production canonical
source/causal history remains immutable in every approach. Resolve that boundary
during planning, and split/review a
new owning outcome if the supported family set cannot fit this eight-point story.
Do not claim completion from a narrowed zero-work profile or SDK progress count.
