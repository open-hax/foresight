---
category: "kanban"
type: "task"
uuid: "afacb7b7-263a-48e6-a744-a9f4b450407b"
epic: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
status: "incoming"
title: "Qualify durable scoped publication receipts in the Knoxx runtime before rollout"
priority: "P1"
points: "5"
labels: "knoxx, cms, publication, receipts, durability"
---

# Qualify a durable Knoxx publication-receipt adapter before deployment

## Context

The epic `82c55f16-a688-4ba5-983d-c44c618cdd5d` owns Foresight planning and
dependency coordination. `open-hax/knoxx` exclusively owns this task's application
source, emission/storage adapters and runtime qualification. Its documented
default 200-entry publication receipt journal is process-local and loses records
on restart; a response or journal GET cannot prove durable closure evidence.

This prerequisite runs parallel to embedding runtime and SDK qualification.
Services EC1.03 (`03a057e4-3098-4fe7-a5b8-95168f45ca96`) consumes its accepted
source/build and configuration before rollout. The content proof
(`4a9f303e-63d1-48d7-a396-b93c986502d1`) consumes its qualified storage lookup.
This task never depends on either later live proof. The real Spanish receipt
remains the content proof's output, not a prerequisite for qualifying persistence.

Two separately estimated proposed owners close the newly identified durability
boundaries: [Knoxx attempt custody/recovery](qualify-crash-surviving-publication-attempt-custody-and-receipt-recovery-4a1450f4.md)
(`e945de08-58ec-4028-b8cb-5dbd4a1450f4`, eight points) and
[Services persistent storage/replacement qualification](qualify-persistent-publication-receipt-storage-through-publisher-replacement-4996f2bc.md)
(`a3ca141e-865e-4c35-8053-53084996f2bc`, three points). This five-point task
retains its scoped emission/acknowledgment/readback implementation and consumes
their accepted capabilities before full durability closure. The Knoxx owner
consumes the qualified existing receipt interface as source evidence, without
depending on this card's completion. The storage owner consumes accepted Knoxx
recovery/readback source; this task consumes both completed qualifications. Neither
owner waits on EC1.03 or a later live proof.

Canonical registration is an explicit blocking operation before AFAC readiness or
completion and before its handoff can unblock rollout. After planning review,
root reads the two first-class local sources by their actual UUID through the
qualified/adopted Rheos authority, then records AFAC's dependency vector as
`["e945de08-58ec-4028-b8cb-5dbd4a1450f4", "a3ca141e-865e-4c35-8053-53084996f2bc"]`
through its canonical metadata operation. Fresh canonical readback and actual
operation/source/completion custody must establish those exact local edges while
preserving unrelated metadata and all historical ledger bytes. Failed, missing or
uncertain registration blocks dependent operational advance and follows Rheos's
adopted recovery authority. Private adapter/source qualification may proceed in
parallel as described above; it supplies no AFAC readiness or completion. No
body reference, fabricated creation history or manual established-frontmatter
edit substitutes for the actual registration, which remains pending here.

## Outcome

Select, configure or implement one bounded durable adapter on Knoxx's actual
publication receipt emission path. Deliver accepted application source, compiled
runtime/image identity, effective configuration contract and independent scoped
storage lookup, qualified on a recoverable owned fixture before deployment.
Receipts retain their immutable payload, digest, correlation and authorized scope
through publisher restart and volatile-journal rollover.

## Scope

Inspect the existing validated receipt, emission hook and storage/configuration
seams. Name the concrete adapter, persistent namespace, acknowledged append/read
contract, immutable record identity and retention policy. Source changes, portable
`.cljc` laws, CLJS adapters and tests belong only in `open-hax/knoxx`; Foresight
retains coordination/evidence references. Services consumes the reviewed
configuration contract through its existing process. Credentials remain local.

Persist the actual validated receipt and server-derived organization/tenant and
project provenance, joined to its trigger, publication, concrete revision, target
and operation correlation. Preserve payload bytes/digest and conflicts; caller
scope strings or similar titles cannot establish identity. Do not invent new
publication trigger fields. Provide a named owning storage lookup usable by an
independent reader/process. When the explicit
`KNOXX_PUBLICATION_RECEIPT_LEDGER_DIRECTORY` selection is configured, the runtime
persists each emitted receipt through its scoped Clio adapter, and
`/api/publications/receipts` plus both registered CMS GETs consume fresh scoped
readback from that owning adapter. When unselected, the receipt GET retains the
existing bounded process-local journal behavior. A custom callback's return,
journal response or sink declaration alone cannot qualify durability: require
acknowledged commit, exact canonical envelope/payload readback through the owning
lookup and actual publisher process restart with fresh readers. Keep callback
failures distinct from commit acknowledgment failures. A retained in-process
failed attempt keeps the exact prepared envelope and admitted scope for explicit
persistence retry without re-entering reconciliation or repeating publication.
That process capability does not qualify durable pending-attempt or callback custody.

Consume the eight-point Knoxx owner's accepted pre-effect intent and fresh-process
recovery contract. A genuine intended operation precedes publication; an actual
observed receipt and its prepared Clio envelope follow supported effect evidence.
Already-prepared envelopes retain exact identity/payload/scope across failed commit
or lost acknowledgment. Missing original effect attribution, an envelope that never
existed or unknown callback delivery must not be relabelled as a successful original
receipt or permission to repeat publication/callbacks. This task neither supplies
that recovery implementation nor claims general at-most-once delivery.

Consume the three-point Services owner's actual persistent storage mapping and
fixture container/image replacement/remount readback. Directory selection, a
container writable layer or process restart on the same volume alone cannot
establish deployment storage lifetime. The mapping retains actual host/backend,
mount, scope/namespace, schema/ledger and permissions identity outside the image;
its qualified lifetime is explicit. Application recovery belongs to Knoxx and
storage provisioning/admission to Services. Missing either accepted capability
keeps this full durability handoff and EC1.03 admission pending.

Reuse pinned Clio `388c462b3f1a5b0c51bc93486ab954a7f89cedbe` event/schema
contracts and `append-admission` for immutable event identity, exact replay and
conflict refusal. Correlation and effect idempotency keys are provenance, not
unique observation identity; retain distinct lawful emitted events even when
their correlations or receipt bodies match. Qualify the selected existing owning
writer's Node/CLJS callability, declared dependencies, native locking,
acknowledged synced append and independent readback in the actual runtime and
storage before relying on `append-event!` or `fsync`. Pinned source alone does
not qualify that capability. Do not introduce a second ledger or admission
protocol.

Closure-record retention is append-only, without TTL or bounded eviction. A failed
or unacknowledged write cannot report durable success. Publication may already
have occurred when persistence fails: preserve the actual error and observed or
unknown effect state. Do not imply that failed storage undoes publication or
manufacture a replacement success. Persistence retries retain the original event
identity, immutable receipt payload and correlation, reuse Clio's exact-event
replay admission, and preserve any committed record without repeating the
publication effect.

## Non-goals

A Foresight publication engine, second receipt/board authority, copied application
source in Services, implementation of the separately owned attempt-recovery or
deployment-storage scopes, generic persistence platform, new acceptance service, changed
translation/publication laws merely to satisfy storage, rewritten receipts, or
execution of the production Spanish journey. Never restart or flood production
to run negative or retention controls.

## Acceptance criteria

- [ ] Before AFAC readiness/completion or downstream rollout admission, canonical
  Rheos readback establishes both exact local prerequisite UUIDs above in AFAC's
  dependency vector, with actual operation/source/completion custody. Both owning
  qualifications then satisfy the full durability criteria below. Missing edges,
  prose-only dependencies or unaccepted capabilities block that operational
  advance; no creation history is manufactured for first-class Markdown sources.
- [ ] The selected runtime's actual emission path uses the named adapter and
  persistent namespace. Effective configuration, record identity, scope-qualified
  lookup and retention policy are inspectable. A detached helper or declared sink
  setting cannot pass.
- [ ] Actual owned-fixture publication/reconciliation emits its validated receipt
  through that path, receives acknowledged commit and permits independent storage
  readback. Payload bytes/digest, server-derived scope and trigger/revision/target/
  operation correlation agree. A response or journal GET alone cannot pass.
- [ ] Restart the fixture publisher, connect a fresh reader and recover the original
  receipt unchanged. Emit more than 200 additional lawful fixture receipts and
  recover that same original through independent durable lookup. With no
  superseding publication outcome, both registered CMS GETs recover its concrete
  observed revision from the owning durable evidence instead of reverting to
  `observed: null`; the public manifest and artifact remain unchanged. Closure
  records have no TTL or bounded eviction; restart/rollover neither rewrite nor
  discard them.
- [ ] Missing configuration, unavailable storage, failed/unacknowledged commit,
  absent readback, changed payload/digest or foreign scope cannot yield successful
  durability evidence. Equal-looking cross-project/organization revisions cannot
  supply foreign evidence. Clio admits an exact replay of the same immutable event
  without another append and refuses the same event ID with different immutable
  data without replacing the original; its stream-slot conflict law remains
  enforced. Distinct lawful materialized, noop and other emitted events may share
  correlation or an effect idempotency key. Persistence retry preserves the
  committed event and its provenance without fabricating another publication
  effect.
- [ ] Consume accepted source and genuine causal qualification from Knoxx owner
  `e945de08-58ec-4028-b8cb-5dbd4a1450f4`: acknowledged intent before effect, exact
  prepared-envelope recovery after failed commit/lost ACK and actual publisher
  termination, positive legitimate recovery, and truthful unknown effect/callback
  states that block unsafe automatic repeat. A retained exception/retry closure
  or committed-record process restart cannot supply this owner's evidence.
- [ ] Consume Services owner `a3ca141e-865e-4c35-8053-53084996f2bc`'s admitted
  persistent storage mapping and actual fixture container/image replacement/remount
  readback. Original receipt/scope/schema/ledger/public bytes survive independently
  through the named lifetime; missing/foreign/ephemeral storage refuses closure.
- [ ] Accepted Knoxx source/build and genuine hosted qualification bind head,
  base/tested candidate where required, compiled adapter/runtime hashes, image
  digest, workflow/run/attempt and actual fixture/database/configuration identity.
  Canonical owner review and qualification gates pass at that source. Local mocks,
  unrelated green checks or old receipts cannot supply admission.
- [ ] Before rollout, Services verifies its selected application candidate and
  effective configuration against this accepted build and qualified storage
  contract, including the two accepted owning capabilities above. Missing sink,
  crash-recovery custody, persistent storage/replacement proof, stale source,
  changed namespace/retention or build mismatch fails admission. Completion needs
  no later live Spanish receipt or content proof.

## Verification and estimate

Write causal predecessor-failing controls in Knoxx for missing persistence,
write/readback failure, foreign scope, immutable conflict and restart/rollover
loss. Exercise the real configured emission adapter and storage on the isolated
recoverable fixture; restart its actual process and read through a fresh reader.
Retain commands, native record IDs, payload/manifest hashes, configuration identity
and genuine refusals. Run the canonical owner's law, adapter integration, vertical
E2E and required hosted qualification; missing tools and failures remain visible.
Consume the separate owners' exact source/runtime/storage/run/attempt evidence
and their actual causal crash/replacement controls; do not duplicate their
implementation in this five-point adapter. Bind their accepted interfaces to the
same selected runtime/configuration consumed by Services. These storage controls
do not claim model inference or live production proof.

| Effort | Points | Bounded deliverable |
| --- | ---: | --- |
| Select the owning boundary | 1 | Adapter, namespace, scope/correlation and configuration/readback contract. |
| Adapter and runtime wiring | 2 | Acknowledged append-only commit and independent lookup through existing Knoxx seams. |
| Causal refusal controls | 1 | Failed persistence, foreign scope, conflict and truthful outcome handling. |
| Accepted-source qualification | 1 | Actual fixture restart/rollover readback, owner evidence and Services handoff. |
| Total | 5 | Predeployment persistence capability; live Spanish proof remains separate. |

## Risks

Five points assumes one bounded emission/readback adapter fits existing Knoxx
seams. The separately estimated eight-point attempt-recovery and three-point
storage-replacement source proposals address the discovered broader boundaries;
their review, canonical registration and accepted implementation remain pending.
Verify every scope/estimate before readiness. Further missing generic primitives,
broader recovery or new infrastructure requires stopping the affected work and
splitting/re-estimating concrete owning work through review and Rheos coordination.
Do not widen this task silently or complete it with a journal-only proof. Services
and the content proof remain blocked until their full qualified durable contract
is available.
