---
category: "eta-mu-breakdown"
labels: ["eta-mu-breakdown", "migration", "ingestion", "jvm", "8sp"]
story_id: "E1.08"
write-id: "1789857181681-0.3i2isqhp9ygfaocrpks"
points: "8"
title: "E1.08 — Osmos preserves ingestion behavior as an independent JVM service"
blocked_by: ["e1-03-bootstrap-dependency-resolution"]
priority: "P1"
status: "blocked"
epic: "operation-eta-mu-breakdown"
uuid: "e1-08-osmos-ingestion-service"
created_at: "2026-09-13T00:00:00Z"
---

# E1.08 — Osmos ingestion extraction

**Acceptance.** The extracted JVM service runs from its own root and image, with explicit configuration/contracts/resources. Unit tests need no live services; integration tests demonstrate their documented live dependencies. Existing Knoxx ingestion/translation/audio clients continue to work.

## Tasks

- Preserve current namespaces and APIs
- Declare how contract bundles and migrations are obtained
- Replace implicit parent-directory contract discovery with tested explicit binding
- Inventory database/schema ownership, mounted paths, image build context, health checks
- Resolve the missing-resources Docker input based on actual required files
- Update Knoxx and Services deployment references in coordinated PRs

## Failing fixtures first

- A fresh isolated Docker context with no parent resources must reveal missing inputs
- An incorrect contract root, missing sink, or Knoxx configuration outage must not be mistaken for successful ingestion
- Repeated delivery must preserve the existing dedup/retry contract

## Evidence

Unit/integration logs, image digest, health/API smoke, explicit service contract configuration, consumer compatibility and documented rollback image/config.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.08).

---
PR #96 reconciliation: Root Osmos pin 0c33018e27f861536822086afcdab666a60add44 predates claimed standalone corrections. Image, service smoke, and Knoxx compatibility evidence are missing. Keep incomplete until an independently reviewed/tested child revision is accepted.
---