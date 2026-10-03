---
category: "kanban"
labels: ["codex-cloud", "github", "forks", "authorization"]
type: "epic"
points: "13"
title: "EPIC: Codex Cloud repository selection expresses access, not the Foresight source graph"
priority: "P1"
status: "incoming"
uuid: "codex-cloud-repository-access"
---

# EPIC: Codex Cloud repository selection expresses authenticated access

## Outcome

Codex Cloud repository selection is intentionally smaller than the Foresight
source graph. `open-hax/foresight` is always the root workspace repository;
children are normally materialized through Foresight's pinned direct-submodule
graph. Additional repositories are selected in the environment only when a task
needs first-class authenticated access to that repository, especially push/PR
work.

Where the Promethean development-fork process is activated, Codex uses the
validated development fork mapping instead of selecting both every fork and
every upstream by habit.

## Context

Current Codex Cloud guidance says a GitHub connection and an environment are
separate concerns: the connected account must have repository permission and the
environment must include a repository when the task needs to access or push to
it:
https://help.openai.com/en/articles/20001545-using-codex-cloud

Foresight separately owns the suite topology. The existing
`fork-dev-origins` epic owns the transition to mapped `riatzukiza/*`
development forks, org promotion, and operational activation. This epic must
reuse that mapping and must not create a conflicting fork policy.

## Core identity

**Submodules answer “what source belongs to this root?”  
Codex repository selection answers “which repositories need provider-level
access in this environment?”**

## Children

- `codex-cloud-access-tiers` — define root, hydrated-child, and first-class
  authenticated repository tiers.
- `codex-cloud-fork-selection-projection` — derive writable repository choices
  from the validated development-origin mapping.
- `codex-cloud-child-remote-configuration` — configure child remotes for the
  active fork/upstream policy without inventing a second source graph.
- `codex-cloud-access-acceptance-matrix` — prove root-only work, one-child
  write work, and unauthorized failure behavior.

## Definition of done

Operators no longer need to select every Foresight member repository and every
personal fork “just in case.” A task that only reads/builds the suite uses the
root bootstrap. A task that must push to one child adds the first-class
repository identity required for that write path. Fork/upstream choices follow
the currently activated Foresight fork policy, and missing authorization fails
clearly.

## Out of scope

- Granting credentials or access beyond the operator's existing GitHub
  permissions.
- Bypassing repository protection or the Promethean promotion gates.
- Treating environment inclusion as evidence that a fork process is activated.
