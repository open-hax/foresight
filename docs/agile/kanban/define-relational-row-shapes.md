---
category: "kanban"
labels: ["shape", "sql", "relational", "representation"]
type: "story"
title: "Define relational row shapes"
priority: "P1"
status: "incoming"
uuid: "7fd34592-e13c-5d50-b103-a282584a02dd"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Define relational row shapes

## Story

**As a** data or storage engineer  
**I want** a domain value to project lawfully into relational rows and
reconstruct from those rows  
**so that** SQL can store the same semantic contract without making the domain
model table-shaped.

## Acceptance

- Define pure `clj->rows` / `rows->clj` semantics using an explicit
  relational representation shape.
- Table, column, key, join, ordering, and cardinality mappings are data/contracts
  rather than hidden adapter conventions.
- Sets have explicit uniqueness semantics.
- Ordered collections retain explicit ordinals when order is semantic.
- Nested objects may normalize across tables without changing semantic identity.
- Missing or duplicate rows that violate the domain shape fail reconstruction.
- Transactions, connections, retries, and queries remain effectful adapter
  concerns.
- Arbitrary values are not automatically relationalized without a declared row
  shape.

## Dependency

Consumes the representation laws.
