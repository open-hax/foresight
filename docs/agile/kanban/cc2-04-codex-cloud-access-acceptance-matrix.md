---
category: "kanban"
labels: "codex-cloud, acceptance, github, 4sp"
type: "story"
story_id: "CC2.04"
points: "4"
title: "CC2.04 — Prove the Codex Cloud repository-access matrix"
dependency: ["codex-cloud-child-remote-configuration", "codex-cloud-root-only-acceptance"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-repository-access"
parent: "codex-cloud-repository-access"
uuid: "codex-cloud-access-acceptance-matrix"
---

# CC2.04 — Prove the Codex Cloud repository-access matrix

## Context and dependencies

Requires CC2.03 and CC1.05; their predecessors supply access tiers, the
validated map, transport, hydration, and tools. Requires actual provider tasks
and an account with the test permissions. No planned fork is treated as active.

## Outcome

The environment configuration has observable evidence for the three cases that
matter instead of relying on a giant repository checklist.

## Scope

Root inspection/validation, one authorized child branch push, and two distinct
denied provider-operation cases. Live testing follows the currently authorized
path. Activated-fork fixtures in CC2.02/03 do not replace live activation proof.

## Tasks within this story

- [ ] Create fresh tasks for Case A, Case B, Case C1, and Case C2, recording
  selected repositories and the actual connected-account capability.
- [ ] Reuse the CC1.05 source/readiness proof in Case A and confirm that no child
  is selected solely to reconstruct source topology.
- [ ] In Case B, create and push an isolated test feature branch to the named
  authorized child; record the branch/ref and resulting commit. Do not change
  protected/default branches or repository settings.
- [ ] In C1 and C2 independently test the provider operation with exactly one
  prerequisite absent; record the denial stage, repository, and no-fallback
  outcome. Do not substitute plain public Git fetch for a provider operation.
- [ ] Retain sanitized expected/observed evidence for every case under `.ημ/`
  and update operator guidance only after the cases are demonstrated.
- [ ] Record any test branch retained for inspection; branch deletion is a
  separate cleanup action requiring its own authorization.

## Non-goals

Granting new account privileges, changing branch protection, enabling paid
review credits, claiming all-child builds, or activating the fork process.

## Acceptance matrix

### Case A — Suite source inspection and root validation

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

### Case C — Missing access (both subcases required)

- **C1: environment exclusion.** The environment excludes the required child
  while the connected account has the required repository permission.
- **C2: account permission denial.** The environment includes the child while
  the connected account lacks the required operation permission (for example,
  a readable public repository without push permission). If the provider
  refuses this configuration during setup, retain that refusal and stage.
- For each subcase, the provider operation/setup fails clearly and identifies
  the unavailable repository/capability.
- For each subcase, bootstrap does not substitute a different fork/upstream,
  switch credentials, or claim the requested capability succeeded.

## Acceptance criteria

- [ ] Case A, Case B, and both C1/C2 are exercised in separate fresh provider
  task/setup attempts; one denied condition cannot stand in for the other.
- [ ] The resulting operator documentation states the minimum repository
  selections for each case.
- [ ] Evidence records the Foresight root revision, selected repository
  identities, effective child revisions, and whether fork activation was
  observed or merely planned.
- [ ] Evidence records expected and observed outcomes for the child-write and
  both missing-access cases, including the pushed branch/ref and commit, the
  unavailable-repository/capability error and stage, and confirmation that
  bootstrap did not fall back or claim success.
- [ ] The README/onboarding guidance is updated only after these cases are
  demonstrated.

## Verification

Retain task/environment identities, publication/setup revision, root/child
SHAs, selected access repositories, sanitized operation output/exit status,
and the expected/observed result of A, B, C1, and C2. A fixture or provider
acknowledgement does not count as a successful live operation. If actual fork
activation is unavailable, label Case B preactivation and prove that path;
any later claim of live activated-fork access needs its own provider evidence.

## Risks

Public source readability is different from provider push permission. Provider
configuration may reject a denied case before task creation; retain that
specific failure rather than fabricating an impossible task. Capture evidence
without exposing account tokens, private keys, or credential-bearing URLs.

## Definition of done

A maintainer can set up a Foresight Codex Cloud environment without selecting
all upstreams and all personal forks, and can tell exactly when an additional
repository must be included.
