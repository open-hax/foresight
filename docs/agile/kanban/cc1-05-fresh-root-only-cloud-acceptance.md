---
category: "kanban"
labels: "codex-cloud, acceptance, workspace, 3sp"
type: "story"
story_id: "CC1.05"
points: "3"
title: "CC1.05 — Prove a fresh root-only Codex Cloud environment end to end"
dependency: ["codex-cloud-direct-child-hydration"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-self-hydrating-foresight"
parent: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-root-only-acceptance"
---

# CC1.05 — Prove a fresh root-only Codex Cloud environment

## Context and dependencies

Requires CC1.03, which integrates CC1.01/02/04. This is the first story that
requires an actual published cloud environment and a newly created provider
task. Access to that environment is an operational prerequisite, not code
required by the first implementation story.

## Outcome

The design question is settled with executable evidence: a newly created cloud
task can start from the Foresight root and reconstruct the source workspace
without hand-selecting every child solely for checkout.

Here “workspace” means exact source checkout and the named root gates; it does
not claim every independently owned child's package build/test passed.

## Scope

Publish and test the current Codex Cloud environment surface. Record the
environment kind explicitly: legacy Code Review/integration environments are
separate surfaces and do not supply this proof automatically.

## Tasks within this story

- [ ] Prepare an environment containing only the Foresight root as a provider
  repository; wire the reviewed launcher/bootstrap and record its revision.
- [ ] Start a fresh task, capture its root SHA, hydrate and assess readiness,
  and compare every direct child HEAD against that root's committed gitlinks.
- [ ] Run the named root gates, inspect the direct/inventory ownership
  boundary, and repeat bootstrap to prove idempotence.
- [ ] In a disposable controlled environment, make one required direct source
  unavailable; retain the failing source/stage and false readiness result.
- [ ] Retain the sanitized source/revision table and gate outcomes in `.ημ/`
  evidence, then update operator setup guidance to match the demonstrated path.

## Non-goals

All-child builds, child push authorization, fork activation, quota remediation,
or replacing/deleting the operator's legacy environments.

## Acceptance criteria

- [ ] Create a new Codex Cloud environment/task with `open-hax/foresight` as
  the project root and without selecting all child repositories merely for
  source hydration.
- [ ] Run the Foresight bootstrap from an otherwise fresh task workspace.
- [ ] Verify every required direct child exists at the root-recorded gitlink
  revision.
- [ ] Verify the project validation, generated guide, and workspace inventory all
  succeed.
- [ ] Verify a second bootstrap run is idempotent.
- [ ] Verify no nested repository became an unintended workspace root.
- [ ] Verify an intentionally unavailable direct repository produces a clear
  failure in a controlled negative test rather than a false pass.

## Evidence

Record the root commit, environment/setup revision, direct source/revision table,
tool versions, gate results, and any repository whose provider authorization was
required.
Include environment kind/identity, publication/setup revision, task identity,
expected and observed outcomes, repeat-run comparison, and negative-test
source/stage. Failed or unavailable acceptance remains unproved.

## Verification

Use actual new provider tasks, not only a warmed local checkout. Compare
expected recorded revisions with observed child HEADs and retain command exit
statuses. Local integration fixtures prepare this test but cannot replace it.

## Risks

A published prepared filesystem or cache can conceal missing setup. Capture
the starting task state and root revision. Provider network/access restrictions
can block acceptance; report them explicitly without weakening source laws.

## Follow-on boundary

This story proves source hydration and the named root validation gates. Child
push capability belongs to the sibling repository-access epic.
