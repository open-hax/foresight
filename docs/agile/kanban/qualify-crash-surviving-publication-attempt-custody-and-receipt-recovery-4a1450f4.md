---
category: "kanban"
type: "task"
uuid: "e945de08-58ec-4028-b8cb-5dbd4a1450f4"
epic: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
status: "incoming"
title: "Qualify crash-surviving publication attempt custody and receipt recovery"
priority: "P1"
points: "8"
labels: "knoxx, publication, recovery, receipts, clio, crash-custody"
---

# Qualify crash-surviving publication attempt custody and receipt recovery

## Context and evidence

Foresight owns planning and evidence coordination. `open-hax/knoxx` owns this
application implementation, its portable recovery law and its runtime gates.
This source proposal addresses [native PR140 finding4277168537](https://github.com/open-hax/foresight/pull/140#discussion_r4277168537).
It is separate from AFAC `afacb7b7-263a-48e6-a744-a9f4b450407b`'s five-point
scoped receipt emission/readback boundary and Services' storage lifetime proof.

The actual production composition uses `publication-effects/IIdempotencyStore`
and `publication-target-static-site/static-site-store`: a reservation precedes
target publication, and an in-flight claim observes the target before retrying.
Its persisted marker contains only in-flight status; its completed record stores
the target receipt before reconciliation correlation. It retains neither the
original scoped attempt inputs nor a prepared Clio envelope. The target's current
manifest and artifact can establish current materialization; they do not by
themselves attribute a historical effect to this attempt.

In the inspected source, reconciliation finishes the target operation before
`record!` correlates the receipt and `receipt-sink` calls `prepare-event!`. The
exact prepared envelope and `retry!` function currently survive only in the
original process. A callback delay enforces one delivery for that retained
closure; it is not a durable delivery record or a general at-most-once guarantee.

Reuse the actual pinned Clio388 event/schema/ledger/identity authority, the
existing publication effect reservation/target observation, and the selected
scoped receipt writer. The pinned dependency is
[`388c462b3f1a5b0c51bc93486ab954a7f89cedbe`](https://github.com/open-hax/eta-mu/tree/388c462b3f1a5b0c51bc93486ab954a7f89cedbe/packages/clio).
Clio creates and validates immutable envelopes and admits exact replay/conflict;
application attempt phase and effect recovery semantics belong to Knoxx.
The [private pinned API inventory](/home/err/.local/share/openhax-codex/20261010/review-state/Knoxx-b1fa-Clio388-durable-handoff-API-readonly-inventory-v1/root-ready-pinned-Clio388-capability-inventory.json)
and [owning discovery/split carrier](/home/err/.local/share/openhax-codex/20261010/review-state/Knoxx-b1fa-publication-attempt-custody-owner-discovery-v1/root-ready-existing-publication-API-discovery-and-private-two-card-split-v1.json)
retain exact source hashes and limits. They are private design evidence, not
accepted source, hosted qualification or an implemented recovery interface.

## Outcome

One reviewed Knoxx owning path binds an actual admitted attempt before a target
effect, and a fresh process recovers its durable custody without treating a new
reconciliation demand as the original attempt. An already prepared observation
retains its exact original Clio envelope, payload, scope and correlation through
definite append failure, lost acknowledgment and publisher termination. Missing
effect attribution or callback delivery remains explicitly unknown and cannot
authorize an unsafe automatic repeat or invented successful receipt.

## Entry and ownership

Consume the exact qualified existing AFAC emission/readback interface and the
accepted pinned Clio capability as source inputs. This card does not depend on
AFAC completion, Services rollout or a later live proof; AFAC consumes this
card's accepted source and qualification. Source preparation, native owner
review and lawful readiness are still required before implementation.

The existing actual public channel is authenticated
`POST /api/publications/reconcile`; its closed trigger contains trigger ID,
channel-fixed origin and publication ID. Its default trigger identifies the HTTP
channel and is shared by independent demands. Server-derived organization and
configured project are the admitted scope. Do not use a trigger ID, materialization
idempotency key, equal receipt body or correlation as unique attempt/event identity,
and do not add undeclared public trigger fields merely to supply recovery.

## Scope

1. Describe and qualify the existing plan/effect reservation and selected Clio
   handoff seam before choosing its minimal extension. Keep one owning custody
   boundary. Reuse Clio's codec, schema revisions, immutable identity/admission,
   inode lock and owning-FD append acknowledgment; do not copy those laws or
   introduce a second ledger authority, sidecar outbox runtime or competing writer.
2. Bind a genuine pre-effect attempt intent to admitted scope, actual channel
   correlation, selected target/configuration, admitted source/output revisions,
   approval/evidence identity, desired/prior materialization and the actual planned
   artifact identity needed for safe recovery. A pre-effect intent records an
   intended operation, never a predicted materialization receipt. It must be
   acknowledged durably before the owning path can begin the target effect.
3. Keep that intent distinct from the later actual target observation and from a
   complete Clio receipt envelope prepared after observation. Persist the exact
   prepared envelope through the owning handoff before its receipt-commit attempt;
   an exact replay obtains a fresh owning-inode acknowledgment and exact readback.
   If a process dies before an envelope exists, do not manufacture its lost UUID,
   timestamp or success payload. Recover the retained intent and make only the
   independently supported owning observation, preserving unknown historical
   effect state where current target evidence cannot attribute the original effect.
4. Resume existing recoverable pending custody before treating a matching demand
   as a new noop or a new operation. Retain immutable historical bytes and distinct
   lawful repeated/noop observations. Observe the actual originally selected target
   and its current storage/artifact identity; absent or different current state
   alone cannot prove that an earlier effect never happened. Peers, supersession,
   source/configuration drift, foreign scope and insufficient custody refuse unsafe
   publication, rematerialization or overwrite while retaining the actual unknown
   outcome and pending evidence.
5. Preserve callback errors as distinct from receipt commit/acknowledgment errors.
   Callback functions are process capabilities, not serialized facts. A durable
   receipt does not prove callback delivery. A crash during an arbitrary callback
   leaves delivery unknown unless its actual owning interface supplies independent
   evidence. Do not automatically repeat an unknown callback or claim general
   at-most-once/exactly-once delivery. The selected receipt and CMS GETs must remain
   fresh reads of owning scoped storage and must not invoke publisher effects.
6. Name the actual internal recovery interface and prove fresh-process use. No
   API named in this proposal is assumed to exist merely because its desired
   behavior is described. Keep implementation in the fixed law → shape → extern →
   domain → infra DAG. Application phase decisions remain portable `.cljc` when
   practical; native handles stay at named extern boundaries.

## Non-goals

A generic outbox/recovery platform, duplicate Clio ledger/identity/codec, a new
Foresight publisher or parser, copied application code in Services, predicted
success receipts, birth/write/event fabrication, correlation-based deduplication,
rewritten historical receipts, automatic replay of unknown external effects,
general at-most-once delivery, model inference, a live Spanish publication,
production process restart, destructive production tests, or durable deployment
mount/provisioning qualification. Document-history source revision semantics are
not repurposed into publication attempt authority. RH's private successor APIs
are not treated as adopted Clio388 capabilities.

## Acceptance criteria

- [ ] The actual selected owning path acknowledges genuine intent custody before
  target publication. Its persisted attempt identity, admitted scope, selected
  target/configuration and complete admitted inputs survive a fresh OS process.
  Failed or unacknowledged pre-effect custody produces no target effect.
- [ ] Receipt success is created only from actual supported target evidence.
  Pre-effect intent, later observation and exact prepared envelope have distinct
  lawful meanings and inspectable identity/custody; none is silently substituted
  for another. Missing effects/attribution remain unknown.
- [ ] A definite pre-append failure and a real append followed by lost acknowledgment
  retain the exact prepared envelope and original error. After terminating the
  failed publisher, the actual fresh-process recovery interface resumes that same
  custody, obtains acknowledged exact-event commit/readback and produces no repeated
  publication. Exact replay preserves one immutable event; conflicting identity
  or wrong scope refuses without replacing history.
- [ ] Process termination after intent admission but before effect, after target
  commit but before observation handoff, and after handoff but before receipt
  acknowledgment are independently exercised. Recovery completes legitimate
  recoverable cases through owning evidence; it does not replace the operation
  with blanket refusal. Cases without adequate effect attribution retain explicit
  unknown state and block unsafe automatic repetition.
- [ ] Actual target/source drift, later peer materialization, changed configuration,
  foreign tenant/project, missing custody and corrupted history preserve prior
  bytes/publications and refuse unsafe recovery writes. A current matching route
  or equal receipt body alone cannot authenticate a historical attempt.
- [ ] Callback delivery is independently classified as supported or unknown. Receipt
  commit failures and callback failures retain their distinct causes. No serialized
  native function, process-local delay or response supplies durable delivery proof.
- [ ] Both registered CMS GETs and `/api/publications/receipts` keep selected fresh
  scoped readback; unselected/custom-callback behavior and distinct genuine noop
  emissions retain their existing semantics. Reads append nothing and perform no
  publication/reconciliation or model effects.
- [ ] Accepted owner source/build, actual runtime/Clio/dependency closure and genuine
  hosted gates bind the same candidate. The human verification artifact shows the
  actual effect/custody/restart boundaries and truthful limits. Private design,
  simulated crash state or an unrelated passing suite cannot close this card.

## Causal verification and estimate

First reproduce the current loss through the actual public route/owning target on
an isolated recoverable fixture. Preserve predecessor-failing controls and all
existing assertions. Drive real append failure and lost ACK at the actual owning
inode, then terminate the actual publisher rather than resetting an atom or merely
constructing another store. Inspect exact intent/envelope/event IDs, errors, actual
target/artifact bytes, ledger prefix and callback evidence through fresh independent
processes. Exercise the distinct crash windows and positive safe recovery neighbors,
including repeated completed recovery, genuine later/noop demand and wrong-scope
refusal. Run canonical owner law/adapter/integration/E2E/build/lint and required
hosted gates, with failures and unavailable tools visible. No fixture target is
relabelled as production or provider evidence.

Provisional eight points: one for owning seam/custody discovery; one for portable
intent/observation/recovery contracts; three for actual pre-effect and exact-envelope
wiring; two for causal fresh-process recovery/refusal controls; one for owner
qualification and the Services/AFAC interface handoff. Review calibrates the estimate
before readiness. If safe async fencing, directory/power-loss durability or another
missing generic primitive requires Clio source, identify that concrete upstream
owner and split it through review rather than copying it into Knoxx or silently
widening these eight points. Publisher process interruption is required; host power
loss and distributed storage guarantees remain unqualified unless separately scoped.

## Initial source authority

This is newly authored proposed first-class Markdown with a fresh source UUID and
literal incoming. It has no invented engine birth, created_at, write-ID or event,
and it claims no board admission, readiness, implementation or completion. Root
owns final scope, actual canonical relationships, review and publication.
