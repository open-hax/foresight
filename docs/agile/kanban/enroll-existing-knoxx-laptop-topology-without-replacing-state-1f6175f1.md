---
category: "kanban"
labels: "identity, deployment, knoxx"
dependency: []
parent: "e132b731-86be-4b72-b5fa-1bc158784d1f"
type: "task"
write-id: "1792945643476-0.a2y4z38ptaqqwi5c71a"
points: "5"
title: "Enroll existing Knoxx laptop topology without replacing state"
priority: "P1"
status: "breakdown"
uuid: "16a613a7-42ca-4f54-92fc-3a171f6175f1"
created_at: "2026-10-25T15:30:55.107Z"
---

# Enroll existing Knoxx laptop topology without replacing state

## Context

Services planning is stacked on PR105 at fa21a578523c062663901eb981d14485f8d22f96. It is not source adoption, deployment authorization or a lawful ready transition. Native review and required gates precede implementation; Rheos alone owns board state.

Parent e132b731-86be-4b72-b5fa-1bc158784d1f. A Proxx-specific enrollment and the isolated Knoxx database template do not establish a safe existing Knoxx migration.

## Outcome

One declarative, validated Knoxx laptop slot and existing-state enrollment binds the exact intended instance, public origin/listeners, backend/frontend topology, database resource and operator-owned state.

## Scope

Services slot declarations, native Compose projection and fixed existing-stack templates. Capture current identities and protected evidence references, not private values. Preserve the currently selected database engine/search volumes and embedding provider/model/dimensions. Both the public and social-local Stealth instances remain explicit and separate.

## Non-goals

No database creation or conversion, Atlas replacement, new model space, account merge, new bootstrap admin, shared-memory policy implementation, server restart or handwritten Rheos state.

## Acceptance criteria

- [ ] Declare each selected host/project/origin/port in one Services source; verify collisions against existing Proxx/Axxium/Knoxx reservations without selecting a free-looking port.
- [ ] Bind database URI/database name and current resource identity, all database/vector volumes, operator env fingerprint and credential continuity, contract/workspace/content mounts and exact backend/frontend service identities.
- [ ] Existing approved product account links select the actual login instance. Unknown/multiple instance selection refuses; social-local data and actor/execution runtimes are never inferred disposable.
- [ ] Backend embedding/provider/model/dimension and actor/contract settings survive; no isolated-template defaults silently disable event actors or replace existing owner credentials.
- [ ] Compose parsing and observed dollar normalization are reused. Missing/malformed/partial/unknown/symlinked state or changed private values refuses before image intake or any host mutation.
- [ ] Existing backup/readback, client-access, rollback and enrolled-topology evidence is bound and reviewable. A string naming a backup is not proof that it ran.

## Verification

RED/GREEN tests use native Compose, synthetic existing resources and explicit non-root metadata. Compare immutable original-file/volume identities before/after and exercise changed database, engine, volume, contract, secret, embedding and port cases. Actual host evidence is a separate qualification obligation; Yoga being unreachable must remain unavailable.

## Risks

Reusing the isolated mongo-data or default actor settings can lose retrieval/account continuity while health passes. A shared Axxium identity does not authorize consolidation of public and social actor stores.

---
Planning design published as https://github.com/open-hax/services/pull/109 at12874c445f941185b538dde38cf659b158eca26c, stacked on105/fa21a578523c062663901eb981d14485f8d22f96. Complete5point existingstate/topology and8point pairedreceiver/retention/rollback acceptance is reviewed there. Source service guard and fresh Mongo template are not an existing-instance migration. All cards remain incoming until native planning review, required gates and lawful readiness. Actual host/account/store/embedding/actor custody, immutable qualification and real browser/memory/revocation/rollback remain obligations; no source adoption or runtime cutover claimed. Six doc links resolve; prior receipt count7 corrected by append-only observation. Native planning request6399602252 posted. Board artifacts/events remain local/uncommitted; no remote board persistence claimed.

Planning109 passed the canonical gate twice on12874c445f941185b538dde38cf659b158eca26c: all5checksPASS,0findings, current CodeRabbit+MiMo approvals and1complete unanimous available planning cohort; Codexnativequota supplies no credit. Actual ordinary MERGE58611f91288aa34697fd29a6159b5fab09bb0b63 at2026-10-25T16:25:27Z targets PR105featurebranch, NOTmain or deployment. Merged tree equals reviewed128tree; acceptedfa21ledgerprefixes preserved. Services105itself still needs current-head fullreview/bodysettlement/adoption, so the declared parent-controller dependency remains open. Move only to accepted/breakdown for planning; do not markready/in_progress or installanything before resolving that dependency and all required gates. Existingdata/account/actor/embedding preservation and real release/host/browser/memory qualification remain obligations.

---