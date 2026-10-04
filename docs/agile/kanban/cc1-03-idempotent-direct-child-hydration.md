---
category: "kanban"
labels: "codex-cloud, workspace, submodule, 5sp"
story_id: "CC1.03"
dependency: ["codex-cloud-submodule-transport", "codex-cloud-toolchain-validation"]
parent: "codex-cloud-self-hydrating-foresight"
type: "story"
write-id: "1791131648244-0.a011ypdun3p422mbuv"
points: "5"
title: "CC1.03 — Add one idempotent command to hydrate exact direct children"
priority: "P1"
status: "ready"
epic: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-direct-child-hydration"
---

# CC1.03 — Add one idempotent direct-child hydration command

## Context and dependencies

Requires CC1.02's transport adapter and CC1.04's minimum toolchain launcher.
Both depend on CC1.01 and can be implemented independently. Existing Git plus
`scripts/project.clj` and `scripts/workspace.clj` supply the runtime seams.

## Outcome

A Codex environment setup step invokes one Foresight-owned command to materialize
the direct constellation at the exact revisions represented by the root commit.

## Scope

One Foresight NBB command, reached through the minimum-toolchain launcher,
collects committed Git facts, calls the pure plan, performs direct-child
checkout, and evaluates actual child/gate observations. The plan covers every
declared git-submodule source, including inventory-only `.agents`, without
traversing its skills/packages or executing consolidation inputs.

## Tasks within this story

- [ ] Capture one immutable root commit and read its `.gitmodules` and
  `160000` gitlink records through Git; reject dirty declarations and a root
  checkout whose model/declarations differ from that captured revision.
- [ ] Supply the committed facts to CC1.01, then fetch and check out only its
  planned direct paths through CC1.02; preserve each declared shallow policy.
- [ ] Before changing an existing child, preserve dirty work and unexpected
  HEAD/branch state, including unpublished commits. Refuse a conflicting
  checkout instead of silently switching a user's branch.
- [ ] Serialize competing bootstrap attempts or explicitly refuse the second;
  detect root/head/declaration changes before publishing readiness.
- [ ] Verify every child is initialized and its actual HEAD matches the plan;
  run project validation, guide, and inventory with available tools.
- [ ] Feed observations into the pure assessment and return per-source/stage
  outcomes. On partial failure, retain successful pinned checkouts and report
  remaining work; a retry must be safe without reset/clean/delete rollback.
- [ ] Add local integration fixtures for a fresh checkout, repetition,
  interruption, concurrent attempts, root mutation, unavailable object,
  dirty/unpublished child work, and an existing authorized write remote.

## Non-goals

Recursive child initialization, child package installation/builds, provider
write authorization, fork activation, or source-URL migration.

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
- [ ] GIVEN a root change, interruption, concurrent attempt, wrong child HEAD,
  or failed/missing required gate THEN readiness is false; no earlier successful
  readiness result is reused for the changed attempt.
- [ ] GIVEN partial failure WHEN retried THEN exact successful checkouts are
  preserved, remaining failures stay visible, and unrelated work is retained.
- [ ] GIVEN a child write remote configured under CC2.03's shared contract WHEN
  hydration repeats THEN checkout identity is verified without overwriting it.

## Design constraints

The command may wrap Git plus existing Foresight validation code, but the
semantic source set must remain derived from existing project declarations.
Do not introduce a second repository inventory.

## Verification

Start from a fresh root checkout, hydrate, capture direct child HEADs, run again,
and prove the same revisions and a clean root diff.
Run the negative fixtures, confirm nonzero outcomes with source/stage evidence,
and run the existing project/workspace suites plus CC1.01's new law suite.
All of this can run in disposable local fixtures before CC1.05 uses a provider.

## Risks

Manifest validation, guide rendering, and inventory currently succeed with
uninitialized children. Readiness therefore requires the explicit child HEAD
checks above. A clean worktree alone does not prove a child has no unpublished
branch commits. Remote configuration and fetch transport must remain compatible.

## Anti-patterns

- No `git submodule update --remote`.
- No recursive initialization beyond the direct Foresight boundary.
- No destructive reset/clean of child worktrees as a bootstrap convenience.