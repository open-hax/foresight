---
uuid: "c002072c-2415-46a8-b533-6b502129f2bf"
title: "Qualify a database-enforced cutover writer fence before deployment"
status: "incoming"
type: "task"
priority: "P1"
points: "5"
labels: "services, knoxx, mongo, writer-fence, 5sp"
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
dependency: []
category: "kanban"
write-id: "1791971686404-0.43i2oqs38clt9vihhhl"
created_at: "2026-10-14T09:54:46.404Z"
---

# Qualify a database-enforced cutover writer fence before deployment

## Context

This independently estimated prerequisite coordinates epic 82c55f16-a688-4ba5-983d-c44c618cdd5d. open-hax/services owns the operational exclusion mechanism, controller-facing acquisition/verification/release contract and deployment configuration. Any required application Mongo/CLI interoperability source belongs only in open-hax/knoxx. Foresight owns planning and dependency coordination. A held host lock and stopped Compose containers cannot establish database-wide exclusion.

## Outcome

Deliver one accepted, narrowly scoped Mongo-enforced fence capability with exercised exclusion and recoverable release on an isolated owned database fixture. Its contract permits only the bounded accepted migration/admission authority to mutate the affected stores and remains effective through inventory, apply, final verification and activation. EC1.03 consumes the qualified mechanism and produces the actual production acquisition/window/release evidence later. This capability task is independent of SDK staging prerequisite 6cc1e1ca-c8dd-4cf9-9cd1-24675a7ee0c0 and of EC1.02/EC1.03 completion; its owned fixture exercises real database enforcement across the phase window without claiming SDK transformation, source/graph coverage or production cutover.

## Scope

Name the concrete supported database-side mechanism, affected stores, authority identities, acquisition/renewal or loss semantics, fail-closed verification and recovery/release procedure. Credentials remain local to the trusted controller. Exercise a separate client with otherwise valid write authority, including one absent from the deployment's process/container inventory. Prove real exclusion continuously across the owned fixture inventory/apply/verification/activation phases using permitted bounded writes and rejected adversarial writes. Preserve fixture data and qualify the acquisition/verification/release interface consumed by the later accepted Services caller. Only the database-fence mechanism and its phase contract are qualified here; the accepted SDK/migration composition and complete real migration coverage remain EC1.02/EC1.03 obligations. Implementation stays in owning repositories; this card does not copy application source/graph migration semantics.

## Non-goals

A host-lock-only fence, disabling arbitrary production clients as a test, generic database-auth platform, silent privilege escalation, production migration, authorizing append deltas, or generating EC1.03's later production proof before its caller exists.

## Acceptance criteria

- Accepted source/build/configuration identifies the actual database-enforced mechanism and precise authority/store boundary. Fixture negative controls demonstrate rejection of unregistered otherwise-authorized writes throughout inventory/apply/verify/activation, while only bounded accepted authority can perform its permitted work.
- Lost/expired/changed fence, concurrent attempt, unclosed writer scope, restarted writer or source/database drift refuses admission and activation. Defined recovery/release is exercised. Snapshot the original access/fence configuration and bind any release authority to the actual database, owning controller, run/attempt and still-held lock. Release or restore ordinary writers only after a fresh separately authorized successful activation or owning verified recovery decision for that exact attempt. Error, timeout, cancellation, ownership loss, partial target, unknown recovery or stale/foreign release input cannot silently restore writers. Retain the closed state and evidence until the owning safe recovery procedure succeeds; exercise that refusal and later authorized release on the fixture.
- The fence consumes the owning recovery/activation decision without implementing application preservation, graph coverage or append-admission semantics. Fence acquisition/release cannot accept its own append delta or authorize source deletion; EC1.02/EC1.03 retain those complete proofs. The fence fixture tests its own access-state recovery and bounded release contract, not a second migration verifier.
- Genuine hosted owner qualification binds protected accepted source, caller contract, database/configuration identity, exact head/base/tested candidate where required and run/attempt. Services can consume the qualified fixture interface without this task depending on Services completion or later live proof.

## Verification and estimate

Five provisional points: one for choosing the supported database boundary and authority contract, two for bounded acquisition/verification/release wiring, one for real fixture exclusion/loss/recovery controls, and one for accepted hosted source qualification and Services handoff. Real database-side controls are required; declared generation labels and repeated equal counts cannot substitute. Before implementation verify that the existing provider/database capability supports the selected bounded fence. If new infrastructure or broader authorization semantics are needed, stop, name the owning prerequisite with an actual UUID and re-estimate through planning review/Rheos rather than claiming this five-point capability complete.

## Risks

Actual database privileges or provider limits may invalidate the selected mechanism. A fixture capability qualification is an input to the future controller-driven production window, not evidence that production is already fenced.
