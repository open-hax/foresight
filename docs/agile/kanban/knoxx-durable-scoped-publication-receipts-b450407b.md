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
publication trigger fields. Provide a separate owning storage lookup for an
independent reader/process: `/api/publications/receipts` remains the journal route
and cannot qualify a custom sink's durable records.

Closure-record retention is append-only, without TTL or bounded eviction. A failed
or unacknowledged write cannot report durable success. Publication may already
have occurred when persistence fails: preserve the actual error and observed or
unknown effect state. Do not imply that failed storage undoes publication or
manufacture a replacement success. Recovery uses the existing owning correlation
contract and preserves any committed record.

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
  recover the same original through durable lookup. Closure records have no TTL
  or bounded eviction; restart/rollover neither rewrite nor discard them.
- [ ] Missing configuration, unavailable storage, failed/unacknowledged commit,
  absent readback, changed payload/digest or foreign scope cannot yield successful
  durability evidence. Equal-looking cross-project/organization revisions cannot
  supply foreign evidence. A duplicate correlation with conflicting immutable
  payload refuses without replacing the original. Any retry preserves the owning
  correlation and committed record without fabricating another publication effect.
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
write/readback failure, foreign scope, immutable conflict and restart/rollover
loss. Exercise the real configured emission adapter and storage on the isolated
recoverable fixture; restart its actual process and read through a fresh reader.
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
