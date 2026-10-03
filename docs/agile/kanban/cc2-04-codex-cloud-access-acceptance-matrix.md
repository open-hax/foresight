---
category: "kanban"
labels: ["codex-cloud", "acceptance", "github", "4sp"]
type: "story"
story_id: "CC2.04"
points: "4"
title: "CC2.04 — Prove the Codex Cloud repository-access matrix"
blocked_by: ["codex-cloud-access-tiers", "codex-cloud-fork-selection-projection", "codex-cloud-child-remote-configuration", "codex-cloud-root-only-acceptance"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-repository-access"
uuid: "codex-cloud-access-acceptance-matrix"
---

# CC2.04 — Prove the Codex Cloud repository-access matrix

## Outcome

The environment configuration has observable evidence for the three cases that
matter instead of relying on a giant repository checklist.

## Acceptance matrix

### Case A — Suite read/build task

- Environment includes the Foresight root.
- Direct children are hydrated by the root bootstrap.
- Project validation, guide, and inventory pass.
- No child is selected first-class solely because it is a submodule.

### Case B — Task writes one child

- Environment includes the Foresight root.
- The one required writable child repository is included according to the active
  access/fork policy.
- Codex can create and push a feature branch through the authorized path.
- Other children remain ordinary hydrated submodules unless the task needs
  first-class provider access to them.

### Case C — Missing authorization

- The connected GitHub account or environment intentionally lacks one required
  repository.
- The provider operation fails clearly and names the unavailable repository.
- Bootstrap does not substitute a different fork/upstream or claim success.

## Acceptance criteria

- [ ] All three cases are exercised from newly created cloud tasks.
- [ ] The resulting operator documentation states the minimum repository
  selections for each case.
- [ ] Evidence records the Foresight root revision, selected repository
  identities, effective child revisions, and whether fork activation was
  observed or merely planned.
- [ ] The README/onboarding guidance is updated only after these cases are
  demonstrated.

## Definition of done

A maintainer can set up a Foresight Codex Cloud environment without selecting
all upstreams and all personal forks, and can tell exactly when an additional
repository must be included.
