---
category: "kanban"
labels: "stabilization, coordination, e1"
dependency: ["stabilization-e0"]
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
type: "epic"
write-id: "1792548105599-0.fwiki711pb6jk9qcil2"
title: "Stabilization E1 — Qualify the review and merge pipeline"
priority: "P1"
status: "incoming"
uuid: "stabilization-e1"
created_at: "2026-10-20T07:26:11.778Z"
---

# E1 — Qualify the review and merge pipeline

## Context

We cannot close hundreds of PRs reliably if the review infrastructure itself remains inconsistent.

Stabilization coordination only. Preserve child ownership and existing story identities.

## Scope

- E1.1: Qualify Muse: Resolve complete-input review and evidence-directory handling.
- E1.2: Qualify Eta Mu and Proxx: Establish source-bound reviews, independent reviewer identity, complete verdicts and failure recovery.
- E1.3: Qualify fork promotions: Ensure accepted personal-fork work reaches the appropriate organization without dropping newer history.
- E1.4: Resolve outstanding findings: Test fixes, rerun current-head reviews, and settle actual review threads.
- E1.5: Add merge-drift verification: Recheck affected consumers after each shared dependency is accepted.

The slot-by-slot exit proofs and existing-task candidates are in [the portfolio](../../stabilization/portfolio.md#e1--qualify-the-review-and-merge-pipeline).

## Acceptance

We can demonstrate at least one complete fork → reviewed upstream PR → protected merge → verified consumer update, with no approval or evidence incorrectly transferred between revisions.

- Resolve every slot to existing canonical work or an explicit owned gap before creating execution stories.
- Bind qualification evidence to exact source revisions and preserve prior attempts.
- Registration and board status do not qualify a milestone.

## Dependencies

`stabilization-e0`

## Evidence

[Portfolio tracker](../../stabilization/tracker.md); [source-backed work graph](../../stabilization/work-graph.json).

## Definition of done

We can demonstrate at least one complete fork → reviewed upstream PR → protected merge → verified consumer update, with no approval or evidence incorrectly transferred between revisions.

---
E0 reviewer-route source reconciliation is retained in docs/stabilization/e0-review-route.md/json, native snapshot bc949fd90dcd05c712060938dae7a6e695c084003cc3554f3fbea65f46bd339e. Existing Clio3 remains an unqualified credential-bearing caller. Current eta-mu main7112 has the accepted verifier but coupled inference/App custody; open PR328 has real isolated publisher/controller code, still divergent and unadopted with actual protected Sol behavior/settlement holds. Personal eta-mu22 has native planning PASS (exact CodeRabbit+MiMo,9passing channels,4resolved threads) and actual executed B/H/M/tree proof, but raw successful jobs retain real carrier warnings. No merge/readiness/runtime grant is inferred. Existing producer cf32641a-b336-407e-b68b-c75ec7d20dbb (2) -> publisher ed4e2a2a-6055-4d53-9cea-f3971c707551 (5) -> hosted proof164c2de7-7495-4738-94e1-a81167edefb0 (3 proposed) already own the work. Preserve main69accepted commits/current verifier and both exact four-ledger suffixes; reconcile PR328 controller CheckRun vs PR22 proposed publication status before adoption. Historical October9 same-writer acceptance is retained, not used to override current signer-isolation policy. No privileged rerun, secret value read, duplicate task or paused-agent restart. E1 stays incoming/M1 unqualified.

Enforce the supplied candidate/signing isolation policy in the existing root PR142 caller: remove only its three forwarded App/webhook secret inputs, preserving the pinned workflow, deterministic gates and read-only token permissions. Parsed before/after proof confirms that exact change; called secret inputs are optional, no called job selects an environment, and the missing-App guard precedes model inference and signing. Legacy reviewer unavailability is an explicit retained failure until the existing anonymous producer/trusted publisher chain is implemented and qualified. This bounded caller correction does not activate the new route or establish outside-candidate protection. E1 remains incoming, M1 unqualified; no child source, pin or paused-agent change.

---