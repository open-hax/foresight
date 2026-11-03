---
category: "kanban"
labels: "identity, deployment, knoxx"
dependency: ["16a613a7-42ca-4f54-92fc-3a171f6175f1"]
parent: "e132b731-86be-4b72-b5fa-1bc158784d1f"
type: "task"
write-id: "1792945644643-0.ma7p6v65yq7cs6d70m"
points: "8"
title: "Deliver qualified Knoxx laptop image pairs through the existing receiver"
priority: "P1"
status: "breakdown"
uuid: "1d7c73ce-6fd2-423e-ab6b-d4059b62d33d"
created_at: "2026-10-25T15:30:55.416Z"
---

# Deliver qualified Knoxx laptop image pairs through the existing receiver

## Context

Services planning is stacked on PR105 at fa21a578523c062663901eb981d14485f8d22f96. It is not source adoption, deployment authorization or a lawful ready transition. Native review and required gates precede implementation; Rheos alone owns board state.

Parent e132b731-86be-4b72-b5fa-1bc158784d1f; dependency 16a613a7-42ca-4f54-92fc-3a171f6175f1. The workflow, receiver selection and retention inventory currently limit laptop slots to Proxx.

## Outcome

The existing protected archive receiver can update one enrolled Stealth/Yoga Knoxx backend/frontend pair and return to its previously healthy pair on failure, retaining its database and memory.

## Scope

Extend the same admitted workflow/receiver/local-operator adapter, owned image inventory, cleanup and rollback to the declared Knoxx laptop slots. Preserve all current Proxx and droplet behavior. Use the prior child existing-stack topology; do not copy a separate upload/deploy engine.

## Non-goals

No application source/auth changes, uploads containing scripts/env/mounts, laptop builds, new credentials, unrestricted SSH, first-deployment inference, automatic legacy process stop or qualification bypass.

## Acceptance criteria

- [ ] The workflow/local/forced-command route accepts only declared, root-enrolled Knoxx laptop slots after fresh exact source/controller/archive/host native owner admission; every malformed or foreign route refuses before image intake.
- [ ] Only one immutable paired backend/frontend image generation is admitted; wrong repository, mixed source revisions, missing pair, changed archive, ambiguous publication or stale review refuses.
- [ ] Healthy prior generation, private existing-state checks and actual backup/client-access/rollback evidence precede any update. Publication checks preserve original state and use real readiness plus application acceptance.
- [ ] Retention inventories cover all owned laptop services without requiring droplet-only slots, losing recorded obligations or deleting another instance/execution container/database/vector volume. Removed owned slots still block unsafe cleanup.
- [ ] Failed health/start/acceptance restores the exact previously healthy image pair with all persistent state untouched; uncertain rollback remains a failure, never health-only success.
- [ ] Operational cutover preserves legacy rollback access and exact-key/queue-drain requirements. Native immutable release plus four disjoint >=250 evaluated test-killed source mutation batches remains required.
- [ ] Actual browser sign-in maps to the approved existing human account on both machines, revocation works and an authorized actor retrieves existing creative memory using Knoxx-owned semantics. If the selected store lacks the corpus, a separately reviewed Knoxx memory-access/replication path is a blocker, not permission to create empty success or conflate login/federation with memory access.

## Verification

RED/GREEN native-Compose and receiver integration tests exercise valid paired update, isolation, archive/auth refusal, rollback and retained store identities. Real test-killed mutation receipts, host backup/readback, browser/actor output and revocation proof are qualification artifacts; synthetic receipts or a successful image build cannot substitute.

## Risks

Backend/frontend skew, foreign Compose projects, hidden image references, active social actor/execution children, stale permissions, offline Yoga and an unavailable droplet can invalidate cutover. No automated stop or data migration is inferred.

---
Planning design published as https://github.com/open-hax/services/pull/109 at12874c445f941185b538dde38cf659b158eca26c, stacked on105/fa21a578523c062663901eb981d14485f8d22f96. Complete5point existingstate/topology and8point pairedreceiver/retention/rollback acceptance is reviewed there. Source service guard and fresh Mongo template are not an existing-instance migration. All cards remain incoming until native planning review, required gates and lawful readiness. Actual host/account/store/embedding/actor custody, immutable qualification and real browser/memory/revocation/rollback remain obligations; no source adoption or runtime cutover claimed. Six doc links resolve; prior receipt count7 corrected by append-only observation. Native planning request6399602252 posted. Board artifacts/events remain local/uncommitted; no remote board persistence claimed.

Planning109 passed the canonical gate twice on12874c445f941185b538dde38cf659b158eca26c: all5checksPASS,0findings, current CodeRabbit+MiMo approvals and1complete unanimous available planning cohort; Codexnativequota supplies no credit. Actual ordinary MERGE58611f91288aa34697fd29a6159b5fab09bb0b63 at2026-10-25T16:25:27Z targets PR105featurebranch, NOTmain or deployment. Merged tree equals reviewed128tree; acceptedfa21ledgerprefixes preserved. Services105itself still needs current-head fullreview/bodysettlement/adoption, so the declared parent-controller dependency remains open. Move only to accepted/breakdown for planning; do not markready/in_progress or installanything before resolving that dependency and all required gates. Existingdata/account/actor/embedding preservation and real release/host/browser/memory qualification remain obligations.

---