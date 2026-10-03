---
category: "kanban"
labels: ["codex-cloud", "workspace", "submodule", "bootstrap"]
type: "epic"
points: "17"
title: "EPIC: Codex Cloud treats Foresight as one self-hydrating workspace"
priority: "P1"
status: "incoming"
uuid: "codex-cloud-self-hydrating-foresight"
---

# EPIC: Codex Cloud treats Foresight as one self-hydrating workspace

## Outcome

A published Codex Cloud environment can use `open-hax/foresight` as the
workspace root without manually selecting every Foresight child merely to make
the source tree exist. Foresight reconstructs its direct repository constellation
from the exact gitlinks declared by the root, installs the minimum required
tooling, and validates the resulting workspace before task execution.

The root remains the orchestrator. Direct children remain independently owned
repositories. Nested repositories do not silently become Foresight workspace
roots.

## Context

- Current Codex Cloud environments combine repositories, tools, dependencies,
  credentials, network access, and other preparation into a reusable setup:
  https://help.openai.com/en/articles/20001545-using-codex-cloud
- The same current guidance distinguishes environment repository inclusion from
  GitHub permission and notes that repository inclusion matters when a task must
  access or push to that repository.
- Foresight already declares the physical source graph in `.gitmodules`, the
  semantic source graph in `src/foresight/project.cljc`, and the ownership
  boundary in `AGENTS.md`.
- The current README already routes a new actor through
  `git submodule update --init`, `project validate`, `guide`, and
  `workspace inventory`.
- Most direct GitHub submodules currently use SSH URLs. A clean managed cloud VM
  must not depend on the operator's local SSH agent being present.
- Foresight intentionally treats direct `.gitmodules` entries as workspace
  repositories and does not recursively promote nested Git repositories.

## Core identity

**Codex selects Foresight; Foresight reconstructs Foresight.**

The Codex environment is responsible for providing a clean execution host and
authorized repository connections. The Foresight root is responsible for
declaring and validating the suite topology.

## Children

- `codex-cloud-bootstrap-contract` — define the exact root/bootstrap laws and
  fail-closed behavior.
- `codex-cloud-submodule-transport` — make GitHub submodule transport work in a
  clean cloud VM without rewriting source identity.
- `codex-cloud-direct-child-hydration` — provide one idempotent command that
  materializes exactly the direct pinned children.
- `codex-cloud-toolchain-validation` — prepare the minimum toolchain and run the
  root semantic/workspace gates.
- `codex-cloud-root-only-acceptance` — prove the complete root-only environment
  from a fresh task.

## Definition of done

From a newly prepared Codex Cloud task whose project repository is
`open-hax/foresight`:

1. every direct submodule required by the root is present at the recorded gitlink
   revision;
2. no nested repository is promoted merely because a child contains one;
3. an inaccessible required child fails visibly instead of being treated as an
   empty directory or a pass;
4. the bootstrap is safe to run again and leaves a clean workspace when nothing
   changed;
5. `nbb scripts/project.clj validate`,
   `nbb scripts/project.clj guide`, and
   `nbb scripts/workspace.clj inventory` succeed; and
6. setup does not mutate committed `.gitmodules` solely to accommodate the
   cloud host.

## Out of scope

- Granting write/push permission to every child repository.
- Selecting every upstream and every personal fork in Codex Cloud.
- Activating the still-in-progress Promethean fork-development process.
- Recursively initializing nested Git repositories that Foresight does not
  declare as direct sources.

See the sibling epic `codex-cloud-repository-access` for authenticated
repository selection and fork-aware write access.
