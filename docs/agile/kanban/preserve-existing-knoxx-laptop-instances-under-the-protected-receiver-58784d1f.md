---
category: "kanban"
labels: "identity, deployment, knoxx"
dependency: []
type: "epic"
write-id: "1792945642185-0.p6daz25nz1luzjignru"
points: "13"
title: "Preserve existing Knoxx laptop instances under the protected receiver"
priority: "P1"
status: "breakdown"
uuid: "e132b731-86be-4b72-b5fa-1bc158784d1f"
created_at: "2026-10-25T15:30:54.802Z"
---

# Preserve existing Knoxx laptop instances under the protected receiver

## Context

Services planning is stacked on PR105 at fa21a578523c062663901eb981d14485f8d22f96. It is not source adoption, deployment authorization or a lawful ready transition. Native review and required gates precede implementation; Rheos alone owns board state.

The reviewed receiver currently permits Proxx only on Stealth/Yoga. The isolated Knoxx Compose template declares a fresh mongo-data volume. Reusing it for an existing machine would not preserve its accounts, sessions or creative vectors. Observed Stealth has separate public and social-local instances; project identity alone is not authority to consolidate them.

## Outcome

Deploy an admitted immutable Knoxx backend/frontend pair into an explicitly enrolled existing Stealth/Yoga instance while preserving its database, account identity, state, embeddings and actor configuration.

## Scope

Services owns the fixed slot/topology, preservation/admission/receiver/retention/rollback adapters and ingress declarations. Knoxx owns application auth, actor policy and memory semantics; shared authority remains Axxium. Children 16a613a7-42ca-4f54-92fc-3a171f6175f1 (5 points) and 1d7c73ce-6fd2-423e-ab6b-d4059b62d33d (8 points) separate state enrollment from archive delivery.

## Non-goals

No fresh replacement database, database/model migration, application or auth policy clone, speculative port, automatic service stop, credential generation, cross-instance account merge, public data export or relaxation of production qualification. Do not treat shared login as automatic creative-memory federation.

## Acceptance criteria

- [ ] Both children have independently reviewed acceptance and lawfully reach ready through Rheos.
- [ ] Existing host/instance/account/state/embedding/ingress identity is explicit; unavailable or ambiguous custody refuses.
- [ ] Backend/frontend pair updates preserve existing account/session/API keys, contracts/workspace/content, database/vector volumes and provider/model settings.
- [ ] Archive intake, rollback and cleanup remain ownership-scoped and preserve unrelated instances and execution containers.
- [ ] Required immutable integration/E2E and four disjoint >=250 test-killed mutation batches remain mandatory before cutover, together with real human login, actor retrieval and rollback proof.

## Verification

Retain source/host/runtime/readback evidence without credentials. Native Compose/synthetic-filesystem tests qualify code boundaries; backup readback, actual operator review/enrollment, existing database identity and real browser/actor results must be obtained on the actual hosts before adoption.

## Risks

Two distinct Stealth databases, source bind mounts, actor loops, separate vector partitions, root enrollment, secret rendering, stale rollback references and host disconnection can all make a healthy empty application the wrong result.

---
Planning design published as https://github.com/open-hax/services/pull/109 at12874c445f941185b538dde38cf659b158eca26c, stacked on105/fa21a578523c062663901eb981d14485f8d22f96. Complete5point existingstate/topology and8point pairedreceiver/retention/rollback acceptance is reviewed there. Source service guard and fresh Mongo template are not an existing-instance migration. All cards remain incoming until native planning review, required gates and lawful readiness. Actual host/account/store/embedding/actor custody, immutable qualification and real browser/memory/revocation/rollback remain obligations; no source adoption or runtime cutover claimed. Six doc links resolve; prior receipt count7 corrected by append-only observation. Native planning request6399602252 posted. Board artifacts/events remain local/uncommitted; no remote board persistence claimed.

Planning109 passed the canonical gate twice on12874c445f941185b538dde38cf659b158eca26c: all5checksPASS,0findings, current CodeRabbit+MiMo approvals and1complete unanimous available planning cohort; Codexnativequota supplies no credit. Actual ordinary MERGE58611f91288aa34697fd29a6159b5fab09bb0b63 at2026-10-25T16:25:27Z targets PR105featurebranch, NOTmain or deployment. Merged tree equals reviewed128tree; acceptedfa21ledgerprefixes preserved. Services105itself still needs current-head fullreview/bodysettlement/adoption, so the declared parent-controller dependency remains open. Move only to accepted/breakdown for planning; do not markready/in_progress or installanything before resolving that dependency and all required gates. Existingdata/account/actor/embedding preservation and real release/host/browser/memory qualification remain obligations.

---