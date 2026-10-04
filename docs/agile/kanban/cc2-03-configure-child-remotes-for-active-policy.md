---
category: "kanban"
labels: "codex-cloud, git, forks, 3sp"
type: "story"
story_id: "CC2.03"
points: "3"
title: "CC2.03 — Configure child remotes from the active fork policy without changing source law"
dependency: ["codex-cloud-fork-selection-projection", "codex-cloud-direct-child-hydration"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-repository-access"
parent: "codex-cloud-repository-access"
uuid: "codex-cloud-child-remote-configuration"
---

# CC2.03 — Configure child remotes from the active fork policy

## Context and dependencies

Requires CC2.02's selection output and CC1.03's already hydrated target. A
validated fork map is consumed transitively; verified live activation is
required only when the target uses the activated fork path.

## Outcome

A hydrated child that becomes a write target has useful local remotes for the
currently authorized development/promotion process, while the Foresight root
continues to own gitlink identity and source declarations.

## Scope

An outer Git adapter configures only the explicitly selected child's owned
development/upstream remotes. It uses CC2.02's decision, preserves the pinned
checkout and unrelated settings, and remains compatible with repeated hydration.

## Tasks within this story

- [ ] Add disposable local fixtures for preactivation and verified-activation
  decisions, existing remote conflicts, unauthorized selection, and repetition.
- [ ] Snapshot the target's relevant configuration and confirm its identity
  before applying the selected policy; refuse unexpected/conflicting settings
  instead of overwriting a user's remote.
- [ ] Implement idempotent owned-remote changes with sanitized URLs; preserve
  unrelated remotes, branch/worktree state, and root declarations.
- [ ] Define interruption/failure recovery for owned configuration; report the
  remaining partial state and never clean/reset unrelated work.
- [ ] Exercise hydrate → configure remotes → hydrate and prove that exact
  child HEAD and the authorized write remote both survive.
- [ ] Record the policy/evidence source and pass the inspected result to
  CC2.04 for live provider verification.

## Non-goals

Fork activation, broad `submodule sync`, credential storage, default-branch
changes, commits in children, or root gitlink/source-URL migration.

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
- [ ] Failure/interruption leaves unrelated configuration intact and reports
  owned partial changes; a rerun either completes safely or refuses a conflict.

## Verification

Use local fixtures for activated and preactivation decisions, inspect sanitized
remote configuration, and prove the hydrate/configure/hydrate sequence. Fixture
activation is not a live-process claim. CC2.04 then verifies the actual policy
available for its chosen repository; absent trusted activation keeps that live
case on the existing authorized path.

## Risks

An unconditional synchronization can undo the authorized write remote. A clean
worktree can still contain unpublished branch commits. Preserve both remote
intent and worktree/branch identity across setup and retries.

## Anti-patterns

- Do not make every upstream writable.
- Do not treat a local remote name as promotion authority.
