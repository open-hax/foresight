---
category: "kanban"
labels: ["shape", "json", "bson", "mongodb", "representation"]
type: "story"
title: "Define JSON BSON and document-store shapes"
priority: "P1"
status: "incoming"
uuid: "ae946b40-ea77-59fc-88a9-9594bbff4f88"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Define JSON, BSON, and document-store shapes

## Story

**As an** application developer  
**I want** APIs and document databases to consume the same domain shapes as EDN
and YAML  
**so that** transport and persistence do not create parallel schemas.

## Acceptance

- Define `clj->json` / `json->clj` and `clj->bson` / `bson->clj`
  semantics against the same domain shape.
- Keyword, set, UUID, instant, collection, namespaced-key, and tagged-value
  recovery follows the common laws.
- BSON-native types are used only through explicit representation rules.
- MongoDB `_id` does not silently replace domain identity.
- Provider metadata does not become domain authority.
- One fixture proves equivalent semantic reconstruction through EDN, YAML,
  JSON, and BSON.

## Dependency

Consumes the representation laws and portable value shape.
