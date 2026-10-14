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
failures distinct from commit acknowledgment failures.

Name and qualify the existing application-owned durable handoff/recovery boundary
for a failed or unacknowledged receipt commit. Its custody must survive publisher
termination after the publication effect and before explicit persistence retry,
including a real append with a lost acknowledgment. Retain the exact originally
prepared Clio envelope, admitted scope, immutable receipt payload/correlation and
actual observed or unknown effect state in acknowledged durable custody before
the first receipt-commit attempt. A fresh publisher/recovery process must recover
that same attempt, resume Clio exact-event admission and obtain a new acknowledgment
without re-entering reconciliation, repeating publication or duplicating an
already completed callback. Process-local exception data or a `retry!` closure
cannot supply this crash-recovery evidence. Qualification must expose any earlier
effect-to-custody crash window; it cannot infer safety from a later saved envelope.
Use the existing owning protocol and Clio authority; if no bounded recoverable
boundary fits the current seam, split and re-estimate concrete owning work before
implementation or admission under the risk rule below.

Services must bind the selected directory to its actual persistent deployment
storage: the host or storage-backend identity, mount source/container destination,
owning scope/namespace, schema and ledger custody, permissions and replacement/
reschedule lifecycle. A container writable layer or temporary fixture directory
cannot qualify deployment durability. On the isolated deployment fixture, replace
the publisher container/image, remount that same admitted storage into a fresh
container and independently recover the original canonical envelope and receipt.
Retain both runtime identities and the mount/storage readback. Knoxx owns adapter
and recovery semantics; Services owns storage provisioning and deployment custody.
New infrastructure outside those existing seams requires separate reviewed scope.

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
source in Services, generic persistence platform, new acceptance service, changed
translation/publication laws merely to satisfy storage, rewritten receipts, or
execution of the production Spanish journey. Never restart or flood production
to run negative or retention controls.

## Acceptance criteria

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
- [ ] Actual failed/unacknowledged emission custody is durably recoverable after
  terminating the original publisher before explicit retry. Exercise both a
  definite pre-append failure and a real append followed by lost acknowledgment.
  A fresh process recovers the exact original envelope/scope and observed or
  unknown effect state, obtains acknowledged exact-event commit/readback and
  preserves one publication effect and at-most-once completed callback delivery.
  Preserve the original error and every historical byte; disappearance of the
  process-local retry closure cannot become a fresh reconciliation or a fabricated
  successful receipt. Unqualified custody or an uncovered effect-to-custody window
  prevents completion.
- [ ] The qualified deployment fixture uses the named persistent storage mapping
  consumed by Services. Replacement of the publisher container/image and remount
  into a fresh container recover the same canonical receipt, scope and schema/
  ledger history through independent lookup while preserving public bytes. Actual
  storage/mount identities and permissions agree; process restart on one volume
  alone cannot supply container-replacement or reschedule-lifetime proof.
- [ ] Accepted Knoxx source/build and genuine hosted qualification bind head,
  base/tested candidate where required, compiled adapter/runtime hashes, image
  digest, workflow/run/attempt and actual fixture/database/configuration identity.
  Canonical owner review and qualification gates pass at that source. Local mocks,
  unrelated green checks or old receipts cannot supply admission.
- [ ] Before rollout, Services verifies its selected application candidate and
  effective configuration against this accepted build and qualified storage
  contract. Missing sink, stale source, changed namespace/retention or build mismatch
  fails admission. Completion needs no later live Spanish receipt or content proof.

## Verification and estimate

Write causal predecessor-failing controls in Knoxx for missing persistence,
write/readback failure, foreign scope, immutable conflict, pending-attempt loss
across publisher termination, restart/rollover and container-replacement storage
loss. Exercise the real configured emission adapter, owning recovery boundary and
storage on the isolated recoverable fixture. Terminate the publisher between
failed/unacknowledged commit and explicit retry, recover the original attempt in a
fresh process and prove no repeated publication/callback effect. Separately replace
the fixture publisher container/image, remount the admitted persistent storage and
read the original committed receipt through a fresh independent reader. These
controls must preserve actual errors and unknown outcomes rather than manufacture
a replacement success.
Retain commands, native record IDs, payload/manifest hashes, configuration identity
and genuine refusals. Run the canonical owner's law, adapter integration, vertical
E2E and required hosted qualification; missing tools and failures remain visible.
These storage controls do not claim model inference or live production proof.

| Effort | Points | Bounded deliverable |
| --- | ---: | --- |
| Select the owning boundary | 1 | Adapter, namespace, scope/correlation and configuration/readback contract. |
| Adapter and runtime wiring | 2 | Acknowledged append-only commit and independent lookup through existing Knoxx seams. |
| Causal refusal controls | 1 | Failed persistence, foreign scope, conflict and truthful outcome handling. |
| Accepted-source qualification | 1 | Actual fixture restart/rollover readback, owner evidence and Services handoff. |
| Total | 5 | Predeployment persistence capability; live Spanish proof remains separate. |

## Risks

Five points assumes one bounded adapter fits existing Knoxx emission/storage seams.
Verify that before implementation. A missing emission boundary, broader correlation/
recovery protocol or new infrastructure requires stopping the affected work and
splitting/re-estimating concrete owning work through review and Rheos coordination.
Do not widen this task silently or complete it with a journal-only proof. Services
and the content proof remain blocked until their full qualified durable contract
is available.
