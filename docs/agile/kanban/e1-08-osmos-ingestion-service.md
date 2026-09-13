---
uuid: "e1-08-osmos-ingestion-service"
title: "E1.08 — Osmos preserves ingestion behavior as an independent JVM service"
status: done
priority: P1
labels: ["eta-mu-breakdown", "migration", "ingestion", "jvm", "8sp"]
created_at: "2026-09-13T00:00:00Z"
category: eta-mu-breakdown
points: 8
epic: "operation-eta-mu-breakdown"
story_id: "E1.08"
blocked_by: ["e1-03-bootstrap-dependency-resolution"]
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
