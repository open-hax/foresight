---
category: "kanban"
labels: ["codex-cloud", "git", "forks", "3sp"]
type: "story"
story_id: "CC2.03"
points: "3"
title: "CC2.03 — Configure child remotes from the active fork policy without changing source law"
blocked_by: ["codex-cloud-fork-selection-projection"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-repository-access"
uuid: "codex-cloud-child-remote-configuration"
---

# CC2.03 — Configure child remotes from the active fork policy

## Outcome

A hydrated child that becomes a write target has useful local remotes for the
currently authorized development/promotion process, while the Foresight root
continues to own gitlink identity and source declarations.

## Acceptance criteria

- [ ] GIVEN an activated development-fork mapping WHEN a child becomes writable
  THEN its local development remote points at the mapped fork and its canonical
  upstream identity remains discoverable according to the Promethean policy.
- [ ] GIVEN a child still using the pre-activation path WHEN bootstrap configures
  remotes THEN it preserves that authorized path and reports the fork policy as
  not yet activated.
- [ ] Remote setup is idempotent and does not rewrite unrelated child
  configuration.
- [ ] Bootstrap does not independently modify committed `.gitmodules`; changes
  to source URLs remain owned by the existing fork migration stories.
- [ ] No credential-bearing remote URL is persisted in tracked files.

## Verification

For one activated and one not-yet-activated repository, inspect `git remote -v`
and prove that the result matches the validated policy state.

## Anti-patterns

- Do not make every upstream writable.
- Do not treat a local remote name as promotion authority.
