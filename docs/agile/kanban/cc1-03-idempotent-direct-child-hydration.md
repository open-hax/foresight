---
category: "kanban"
labels: ["codex-cloud", "workspace", "submodule", "5sp"]
type: "story"
story_id: "CC1.03"
points: "5"
title: "CC1.03 — Add one idempotent command to hydrate exact direct children"
blocked_by: ["codex-cloud-bootstrap-contract", "codex-cloud-submodule-transport"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-direct-child-hydration"
---

# CC1.03 — Add one idempotent direct-child hydration command

## Outcome

A Codex environment setup step invokes one Foresight-owned command to materialize
the direct constellation at the exact revisions represented by the root commit.

## Acceptance criteria

- [ ] GIVEN a fresh root checkout WHEN the command runs THEN every required
  direct child is initialized at its recorded gitlink revision.
- [ ] GIVEN a shallow-marked direct child such as `opencode` WHEN hydration
  runs THEN its declared shallow policy is preserved without changing revision
  identity.
- [ ] GIVEN a successful hydration WHEN the same command runs again THEN no
  tracked file, gitlink, or child revision changes merely because bootstrap is
  repeated.
- [ ] GIVEN unrelated dirty state in a child WHEN hydration would overwrite or
  discard it THEN the command refuses rather than cleaning destructively.
- [ ] GIVEN an incomplete checkout WHEN the command exits THEN its structured or
  human-readable result identifies the failed source and stage.

## Design constraints

The command may wrap Git plus existing Foresight validation code, but the
semantic source set must remain derived from existing project declarations.
Do not introduce a second repository inventory.

## Verification

Start from a fresh root checkout, hydrate, capture direct child HEADs, run again,
and prove the same revisions and a clean root diff.

## Anti-patterns

- No `git submodule update --remote`.
- No recursive initialization beyond the direct Foresight boundary.
- No destructive reset/clean of child worktrees as a bootstrap convenience.
