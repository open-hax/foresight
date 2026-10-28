---
category: "kanban"
labels: ["migration", "representation", "rheos", "katamorph"]
type: "story"
title: "Migrate duplicate parsers and serializers to shared shapes"
priority: "P2"
status: "incoming"
uuid: "6c462e73-97ff-5e58-b721-b4dfcbe02845"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Migrate duplicate parsers and serializers to shared shapes

## Story

**As a** maintainer  
**I want** bespoke parsers and serializers to converge on the shared shape
system  
**so that** representation semantics are not independently reimplemented across
Foresight products.

## Acceptance

- Inventory custom EDN, YAML, frontmatter, JSON, BSON/document, and row-mapping
  conversion code before replacement.
- Migrate one consumer at a time with failing regression fixtures first.
- Rheos frontmatter is an early consumer.
- No migration silently removes fields or changes semantic meaning.
- Existing external/public representations remain compatible unless an explicit
  migration is accepted.
- Preserve independently useful `.ημ` directory layouts; migrate semantics,
  not folder aesthetics.
- Remove a duplicate implementation only after the shared path supplies
  equivalent or stronger evidence.

## Dependency

Begins after the conformance suite can protect migrated boundaries.
