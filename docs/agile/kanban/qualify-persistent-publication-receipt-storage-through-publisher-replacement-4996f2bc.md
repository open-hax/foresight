---
category: "kanban"
type: "task"
uuid: "a3ca141e-865e-4c35-8053-53084996f2bc"
epic: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
status: "incoming"
title: "Qualify persistent publication receipt storage through publisher replacement"
priority: "P1"
points: "3"
labels: "services, publication, receipts, storage, replacement, durability"
dependency: ["e945de08-58ec-4028-b8cb-5dbd4a1450f4"]
---

# Qualify persistent publication receipt storage through publisher replacement

## Context and evidence

`open-hax/services` owns deployment storage configuration, provisioning and
admission. Knoxx owns the scoped receipt adapter, actual attempt recovery and
publication semantics. This source proposal addresses [native PR140 finding4277168542](https://github.com/open-hax/foresight/pull/140#discussion_r4277168542).
AFAC `afacb7b7-263a-48e6-a744-a9f4b450407b` consumes its accepted persistent
storage contract; EC1.03 consumes that complete AFAC handoff before rollout.

The existing Services Compose backend has an image-independent writable state
mount at `/app/state` and an independently mounted publication content root.
The inspected deployment source does not select
`KNOXX_PUBLICATION_RECEIPT_LEDGER_DIRECTORY`. Neither a physical directory nor
same-volume publisher process restart proves which deployed storage retains
the scoped Clio schemas/ledger after image/container replacement. Existing mounts
are reuse candidates, not an already selected or qualified receipt namespace.

The [private owning source/API discovery](/home/err/.local/share/openhax-codex/20261010/review-state/Knoxx-b1fa-publication-attempt-custody-owner-discovery-v1/root-ready-existing-publication-API-discovery-and-private-two-card-split-v1.json)
retains literal Compose/configuration evidence and the pinned Node Clio limitations.
The [current Services contract source](https://github.com/open-hax/services/blob/4cd170ad10bb752d3f8c8ecad60833400897c1f6/digitalocean/services/knoxx/compose.yaml)
is an inspected source reference; it supplies no new deployment observation.

## Outcome

One reviewed Services storage mapping selects the accepted scoped Knoxx Clio
receipt/recovery namespace outside the publisher image and retains it through
the actual deployment replacement lifecycle. An isolated fixture replaces the
publisher container/image, remounts that admitted persistent storage into a
fresh container and independently recovers the original canonical receipt and
applicable pending custody without repeating publication or dropping history.

## Entry and dependencies

Consumes the accepted source/build and qualified recovery/readback interface of
Knoxx attempt-custody owner `e945de08-58ec-4028-b8cb-5dbd4a1450f4`, plus the
existing qualified AFAC emission/readback interface. It does not depend on AFAC
completion, EC1.03 or a live Spanish proof; AFAC consumes this card's completed
storage qualification. The upstream Knoxx source card qualifies process recovery
on an owned volume and can complete without this container/deployment proof.
This keeps the plan acyclic. Actual canonical relationships and accepted source
admission remain root-owned operations.

## Scope

1. Select and review one actual existing persistent deployment storage boundary.
   Name its host or storage-backend identity, actual mount source/container path,
   provisioned owner/permissions, organization/project namespace, schema/ledger
   locations, retention and replacement/reschedule lifetime. The receipt directory
   must resolve to that boundary rather than the container writable layer or a
   temporary host directory. Reuse existing Services state/provisioning seams;
   do not silently introduce a storage platform or assume `/app/state` is selected.
2. Bind the effective `KNOXX_PUBLICATION_RECEIPT_LEDGER_DIRECTORY` selection to the
   accepted Knoxx image/source/configuration and exact adapter/recovery interface.
   Keep credentials local and prove the actual admitted filesystem/volume identity
   from the owned container, not from an environment declaration alone.
3. On an isolated recoverable deployment fixture, use authenticated actual
   `POST /api/publications/reconcile`, independently inspect the original scoped
   canonical receipt/intent and selected GETs, stop/remove the publisher container
   and start a fresh accepted image/container with the same persistent mapping.
   Recover through the actual owning interface and fresh readers. Preserve manifest,
   artifacts, historical schema/ledger bytes and original receipt identity. A
   returned response, same-process retry or process restart on one volume is not
   the replacement control.
4. Cover the actual supported replacement/reschedule policy. A same-host bind mount
   does not establish cross-host rescheduling, host-disk failure or power-loss
   retention. If the deployment requires those guarantees, qualify the real backend
   or split that concrete infrastructure before admission; leave absent proof unknown.
5. Deliver immutable storage/configuration and genuine fixture qualification to
   AFAC and EC1.03. Services verifies the actual selected candidate/mapping again
   before rollout; the card does not execute that production rollout.

## Non-goals

Application recovery/receipt semantics, a second ledger writer or outbox, copied
Knoxx code in Services, arbitrary host/storage provisioning, a distributed storage
platform, production replacement/restart/flooding, model inference, Mongo migration,
the real Spanish journey, preservation claims based solely on Compose prose,
unqualified cross-host/power-loss guarantees or general at-most-once effects.

## Acceptance criteria

- [ ] The selected directory maps to the named actual persistent storage outside
  the publisher image. Mount/backend identity, namespace, schema/ledger locations,
  permissions, retention and lifecycle agree with the effective container config
  and accepted Knoxx source/build. Missing/foreign/ephemeral mapping refuses admission.
- [ ] Real fixture container/image replacement and remount into a fresh container
  recover the exact original committed receipt, scope and complete schema/ledger
  history through independent owning reads. Both CMS GETs and receipt GET retain
  correct selected behavior; public manifest/artifact bytes remain unchanged.
- [ ] A retained incomplete attempt is recovered through the accepted Knoxx interface
  after replacement with its original custody. Services does not infer original
  success, recreate a lost receipt ID or repeat reconciliation/publication. Unknown
  effect or callback delivery remains unknown and blocks unsafe automatic repeat.
- [ ] Missing/remapped storage, wrong namespace/permissions, corrupted or incomplete
  schema/ledger custody, image/config drift and retention loss cannot qualify. Real
  failure observations preserve historical bytes and existing publications.
- [ ] Source/configuration/workflow/run/attempt, image/container identities and actual
  before/after mount/readback evidence are retained. Owning contract/workflow gates
  and genuine hosted qualification pass for that candidate; local declarations or
  old successful receipts do not substitute for actual admission evidence.
- [ ] Qualification names its exact lifetime boundary. Same-host replacement evidence
  is not relabelled as cross-host rescheduling, power loss or host-disk recovery.
  Required but missing deployment guarantees block the affected admission.

## Causal verification and estimate

Preserve a predecessor control demonstrating that an ephemeral writable-layer
selection loses the receipt when its fixture container is removed. Then qualify
the selected real persistent mapping through actual replacement/remount and a
fresh independent reader. Exercise wrong/missing mounts, namespace/permission
drift and incomplete storage custody on disposable fixtures while retaining the
initial failed captures. Verify no repeated publication and exact original
receipt/history/public bytes. Run the genuine Services contract/configuration/
workflow gates and applicable hosted admission checks. No production container,
publication or namespace is a negative-test fixture.

Provisional three points: one for exact configuration/provisioning/storage custody;
one for actual container replacement and independent readback; one for admission
refusal controls and the AFAC/EC1.03 handoff. This assumes the selected mapping fits
an existing reviewed Services state boundary. A missing backend, broader lifecycle
or new platform requires separate reviewed scope/re-estimation before readiness.

## Initial source authority

This is newly authored proposed first-class Markdown with a fresh UUID, literal
incoming and its explicit local prerequisite. It contains no invented engine
birth, created_at, write-ID or event and claims no board admission or readiness.
Root owns final scope, canonical registration, review, publication and rollout.
