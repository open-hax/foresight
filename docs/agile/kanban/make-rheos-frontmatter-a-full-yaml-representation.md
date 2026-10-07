---
category: "kanban"
labels: ["rheos", "yaml", "frontmatter", "work-item"]
type: "story"
title: "Make Rheos frontmatter a full YAML representation"
priority: "P1"
status: "incoming"
uuid: "00e11377-098d-5522-8634-bff4f9a585d8"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Make Rheos frontmatter a full YAML representation

## Story

**As a** Rheos user  
**I want** Markdown frontmatter to express the complete WorkItem semantic shape  
**so that** a card does not require a second resource merely because the current
YAML parser is weak.

## Acceptance

- Replace the current flat frontmatter subset with full YAML parsing through the
  shared YAML shape machinery.
- Nested participants, competencies, support, authority requirements, evidence,
  and relationships can be represented directly.
- Rheos board/task state becomes a projection of the semantic WorkItem rather
  than a competing schema.
- Updating a projected field preserves unrelated semantic fields.
- Legacy flat cards remain valid.
- No `work_profile` indirection is required solely to compensate for YAML
  limitations.
- Equivalent standalone EDN, standalone YAML, and Markdown frontmatter fixtures
  decode to equivalent WorkItem values.

## Dependencies

Consumes the schema-directed YAML story and the WorkItem/readiness shape story.
