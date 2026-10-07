---
category: "kanban"
labels: ["shape", "yaml", "frontmatter", "representation"]
type: "story"
title: "Define schema-directed YAML shapes"
priority: "P1"
status: "incoming"
uuid: "6052f473-3fe5-51e3-9170-db6bab87159f"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Define schema-directed YAML shapes

## Story

**As a** human author  
**I want** the same domain value to have a clean YAML representation  
**so that** YAML and Markdown frontmatter stay readable without sacrificing
semantic fidelity.

## Acceptance

- Provide `clj->yaml` / `yaml->clj` semantics against a supplied domain
  shape.
- Schema information recovers types YAML does not natively preserve, including
  keyword and set semantics.
- Define rules for symbols, lists/vectors, maps, namespaced keys, UUIDs,
  instants, enums, unions, optional values, and nested structures.
- Ambiguous `:any` values use the portable representation or fail according to
  declared mode.
- Use a complete YAML implementation rather than Rheos' current flat subset.
- Standalone YAML and Markdown YAML frontmatter share the same semantic
  conversion machinery.
- Formatting changes do not count as semantic change.

## Dependency

Consumes the representation laws and portable value shape.
