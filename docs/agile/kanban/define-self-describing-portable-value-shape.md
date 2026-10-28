---
category: "kanban"
labels: ["shape", "portable", "edn", "katamorph"]
type: "story"
title: "Define a self-describing portable value shape"
priority: "P1"
status: "incoming"
uuid: "7979e428-f9e4-5cbb-bf63-d205413b7053"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Define a self-describing portable value shape

## Story

**As a** shape implementer  
**I want** a self-describing representation that preserves supported Clojure
value semantics without an external schema  
**so that** values can cross weaker representation boundaries without silent
degradation.

## Acceptance

- Provide pure equivalents of `clj->portable` and `portable->clj`.
- Preserve keywords, symbols, sets, lists, vectors, maps, non-string map keys,
  UUIDs, instants, and primitive scalars.
- Define an unambiguous reserved tagged envelope and an escaping/collision law.
- Use entry representations for map keys that cannot lawfully become native
  object keys.
- Unsupported runtime/host objects fail explicitly.
- The portable representation round-trips every supported fixture.
- Deterministic normalization is sufficient for repeatable tests and hashing
  where the owning shape declares that requirement.

## Dependency

Consumes the semantic-equivalence laws from
`aa3ca1b5-791c-59f4-930b-ec94e5e27031`.
