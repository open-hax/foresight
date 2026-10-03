---
category: "kanban"
labels: ["codex-cloud", "forks", "project-model", "3sp"]
type: "story"
story_id: "CC2.02"
points: "3"
title: "CC2.02 — Project the validated dev-origin map into Codex repository selection"
blocked_by: ["codex-cloud-access-tiers", "dev-origin-map"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-repository-access"
uuid: "codex-cloud-fork-selection-projection"
---

# CC2.02 — Project the dev-origin map into Codex repository selection

## Outcome

When a task needs write access to a child repository, the repository identity
selected in Codex Cloud comes from the same validated fork↔org mapping used by
the Promethean development process.

## Acceptance criteria

- [ ] GIVEN a child whose fork-development process is activated WHEN Codex must
  push feature work THEN the selected writable repository is the mapped
  development fork.
- [ ] GIVEN a child whose fork-development process is not activated WHEN Codex
  must push THEN the current authorized repository path remains in force; this
  story does not infer activation from planning data.
- [ ] GIVEN an upstream repository that is already readable as a pinned
  submodule WHEN no provider-level upstream operation is required THEN upstream
  is not redundantly selected merely because the fork is selected.
- [ ] GIVEN a repository-network naming exception WHEN the fork map provides an
  explicit development repository identity THEN Codex selection uses that
  identity instead of guessing `riatzukiza/<upstream-name>`.
- [ ] The projection is derived from the validated source/fork model rather than
  maintained as a second hand-written complete table.

## Dependencies

This story consumes `dev-origin-map` from the existing
`fork-dev-origins` epic. It does not replace or weaken that epic's activation
requirements.

## Anti-patterns

- Do not treat a planned fork as an activated write target.
- Do not invent fork names from repository basenames.
