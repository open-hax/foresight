---
uuid: "dee4ddbf-c00b-4f4a-9f28-a6183056b8dc"
title: "Qualify an isolated trusted production-admission gate before deployment"
status: "incoming"
type: "task"
priority: "P1"
points: "5"
labels: "services, deployment, production-admission, trusted-gate, 5sp"
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
dependency: ["11bc1411-154d-4db7-8c27-51506ab30508", "60e85d88-3960-449f-8376-10e75eae82d9", "a7402e9d-6538-4e38-9919-93fe88af373d", "c002072c-2415-46a8-b533-6b502129f2bf"]
category: "kanban"
write-id: "1791971687369-0.ju8aia7uwu0hsq1x31"
created_at: "2026-10-14T09:54:47.369Z"
---

# Qualify an isolated trusted production-admission gate before deployment

## Context

This separately estimated Services-owned prerequisite is coordinated by Foresight epic 82c55f16-a688-4ba5-983d-c44c618cdd5d. The governing root AGENTS.md requires privileged admission/publication through a trusted App or pinned-base workflow, with gate code/configuration and required App identity outside candidate control. Proposed infrastructure, head bindings or a named approver alone do not establish that boundary.

## Outcome

Deliver an accepted protected-source gate and actual hosted qualification of its independent authority, non-execution boundary and two distinct production decisions. EC1.03 consumes its qualified gate/interface; later actual staging/promotion inputs yield pre-mutation authorization, and fresh controller-driven cutover proof yields final activation admission. This capability task never waits for a production receipt or a later live product proof.

## Scope

open-hax/services owns the production deployment-admission adapter, independently pinned gate configuration and controller integration. Reuse the canonical owning review/attestation laws rather than creating a parallel review-policy or generic publication engine in Services. Selection of an accepted contract is a required implementation input, not a claim that all proposed infrastructure is already adopted. Reuse accepted attestation contracts and existing lawful promotion where process activation is not yet verified; do not claim the proposed Promethean infrastructure is live. Pin the actual trusted App or protected base workflow, its code/configuration revisions, App identity and credential custody independently of the candidate. Candidate workflows/builds/dependency hooks, including same-repository PRs, receive only read-only tokens and no signing/deployment secrets. The privileged gate never checks out, imports, builds or executes candidate code/dependency hooks with its authority; it reads bounded non-executable qualification artifacts through the accepted verifier. Candidate edits cannot replace that verifier, configuration or required App identity.

Bind the approved operation to exact staging/promotion receipt, purpose, authorized approver, production/database identity, current C/S/G, PR head, fresh base, tested merge candidate, image and run/attempt. An authorization for bounded mutation is not final activation: after the accepted operation runs, a fresh independent check validates its actual cutover proof before activation. A fixture receipt qualifies the gate interface but cannot authorize production. Accepted protected application execution occurs only under the later authorized controller path, never by evaluating unadopted candidate code in this privileged gate.

## Non-goals

A new generic review/signing service, account-policy changes, production mutation, auto-activation of the proposed fork process, candidate-controlled verifier/publisher, manual exceptions, or replacement of application migration/source law.

## Acceptance criteria

- Actual accepted gate source/configuration and hosted route demonstrate trusted App or pinned-base execution outside candidate control, distinct authenticated App/approver identities, protected credential custody and no candidate-code execution. Missing/unknown custody, writable candidate configuration/App identity, candidate hooks or deployment/signing secrets on candidate jobs refuses both privileged pre-mutation authorization and final activation, not merely the final phase.
- Owned-fixture adversarial candidates cannot replace the gate/verifier, invoke dependency hooks with privileged authority or substitute an App identity. Native credential/permission observations and honest negative results are retained without secrets; a job name or passing unprivileged test is insufficient.
- Missing/stale/foreign qualification, unknown purpose, wrong approver/production/database, changed head/base/tested candidate, image/runtime/source mismatch or failed original owner qualification refuses before mutation. Missing or drifted fresh cutover/fence proof refuses final activation separately.
- The accepted source and genuine hosted gate qualification precede Services rollout and bind exact protected source, workflow/App identity, configuration, run/attempt and current fixture/artifact custody. Services consumes this named qualified interface; this card's completion requires no future actual production proof.

## Verification and estimate

Five provisional points: one for the bounded existing trusted route and identity/custody contract, two for isolated verifier/controller decision wiring, one for candidate-control and stale/wrong-authority refusal controls, and one for accepted hosted qualification and handoff. The estimate assumes an existing trusted App or protected workflow and accepted attestation seam can support this gate. Missing trusted infrastructure is an explicit owning prerequisite with an actual Rheos UUID, split and re-estimated before qualification; it cannot be hidden in EC1.03 or marked done with source-only proposals.

## Risks

Same-repository candidate permissions and dependency hooks are part of the hostile boundary. Until the accepted implementation and actual activation are verified, the existing authorized PR/promotion path remains controlling. Local tests supply preparation, not installed privileged authority.
