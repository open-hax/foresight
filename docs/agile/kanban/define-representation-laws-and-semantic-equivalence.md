---
category: "kanban"
labels: ["shape", "law", "representation", "cljc"]
type: "story"
title: "Define representation laws and semantic equivalence"
priority: "P1"
status: "incoming"
uuid: "aa3ca1b5-791c-59f4-930b-ec94e5e27031"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Define representation laws and semantic equivalence

## Story

**As a** contract author  
**I want** Foresight to define what it means for two representations to express
the same semantic value  
**so that** representation adapters are proved against laws rather than examples
alone.

## Acceptance

- Define semantic equivalence under a domain shape.
- Define round-trip, validity, unsupported-value, declared-loss, and
  non-silent-loss laws as pure portable functions.
- Classify values as native, schema-recoverable, portable-tagged, or
  unsupported for a target representation.
- Cover nil, booleans, strings, numbers, keywords, symbols, maps, vectors,
  lists, sets, UUIDs, instants, and nested combinations.
- Preserve order where the shape says order is semantic; do not manufacture
  order for sets.
- Representation failures remain typed/inspectable rather than degrading values
  to strings.
- Start with failing law fixtures before implementations.

## Evidence

A shared fixture corpus demonstrates semantic equality after each supported
round trip and demonstrates explicit failure for an unsupported value.
