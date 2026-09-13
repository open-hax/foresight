---
uuid: "e1-12-migration-documentation"
title: "E1.12 — Contributors can find the products and their migration documentation"
status: incoming
priority: P2
labels: ["eta-mu-breakdown", "migration", "documentation", "3sp"]
created_at: "2026-09-13T00:00:00Z"
category: eta-mu-breakdown
points: 3
epic: "operation-eta-mu-breakdown"
story_id: "E1.12"
blocked_by: ["e1-11-donor-retirement-after-cutover"]
---

# E1.12 — Migration documentation

**Acceptance.** Foresight scripts, root architecture/project records, quality catalog, and documentation describe the new ownership. Relevant `eta-mu/docs` material is transferred through an explicit path map into `foresight/docs`. No unresolved internal links are introduced, and outdated documents are not silently promoted into current authority.

## Tasks

- Inventory docs referring to extracted products and assign each a destination/disposition
- Transfer still-relevant suite/cross-project docs to Foresight, rewriting relative links
- Give children their own README/AGENTS/API/development references
- Mark contradictory/stale material for E2's retirement workflow
- Preserve immutable receipts as history, not rewritten guidance
- Link migration records to the final repositories and verification evidence

## Failing fixtures first

- A moved document's relative source link, inbound Markdown link, or fragment anchor must resolve at the destination
- A package README must not retain `pnpm -C packages/<old-name>` as its standalone quickstart
- A known stale build claim cannot be reissued as verified documentation

## Evidence

Source-to-destination path map, link-check report, independent README walkthrough and final migration dossier.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.12).
