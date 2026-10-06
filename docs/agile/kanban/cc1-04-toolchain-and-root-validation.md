---
category: "kanban"
labels: "codex-cloud, tooling, validation, 3sp"
type: "story"
story_id: "CC1.04"
points: "3"
title: "CC1.04 — Prepare the minimum bootstrap toolchain and validation launcher"
dependency: ["codex-cloud-bootstrap-contract"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-self-hydrating-foresight"
parent: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-toolchain-validation"
---

# CC1.04 — Prepare the minimum bootstrap toolchain and validation launcher

## Context and dependencies

Requires CC1.01's contract, not hydrated children or CC1.03's future command.
This is the host-preparation branch that runs alongside CC1.02. Hydration
consumes its launcher; final workspace readiness is verified in CC1.03/05.

## Outcome

Setup can reach the NBB bootstrap on a supported clean host and can invoke the
existing root validation commands with recorded versions and exit statuses.

## Scope

Git, a supported Node version, pinned NBB, and the root lint/validation tools
actually required by the bootstrap contract. Use a shell/host entry point only
to reach NBB; keep source/checkout decisions in the portable contract.

## Tasks within this story

- [ ] Document the supported host and pin/record the minimum tool versions.
- [ ] Write missing-tool and version-mismatch fixtures for the launcher before
  implementing detection/installation of Git, Node, NBB, and required lint tools.
- [ ] Verify `nbb scripts/project.clj validate`, `guide`, and workspace
  `inventory` can run against the existing root-only checkout; retain their
  exit statuses without describing that result as hydrated readiness.
- [ ] Expose the launcher/preflight interface for CC1.03's later command and
  record which commands run before checkout versus after hydration.
- [ ] Exercise repeated tool preparation and unavailable-tool failure in a
  disposable clean host; retain effective versions and sanitized diagnostics.

## Non-goals

Hydrating children, granting credentials, creating a cloud environment, or
installing every child package manager/dependency.

## Acceptance criteria

- [ ] GIVEN a supported clean Codex Cloud host WHEN setup runs THEN the required
  Node/NBB and lint/validation tooling used by the root gate is available, or the
  missing tool is reported explicitly.
- [ ] GIVEN an existing root-only checkout WHEN launcher validation runs THEN
  `nbb scripts/project.clj validate` can report declaration validity; this is
  a toolchain proof and does not mark the workspace hydrated or ready.
- [ ] GIVEN successful project validation WHEN onboarding output is requested
  THEN `nbb scripts/project.clj guide` can render the current routing view.
- [ ] GIVEN the root-only checkout WHEN inventory runs THEN
  `nbb scripts/workspace.clj inventory` reports the declared direct children
  and consolidation inputs without granting consolidation inputs execution
  authority.
- [ ] Setup records the effective tool versions needed to reproduce a failure
  without claiming that an unavailable tool passed.

## Verification

Run the same root commands documented in README/AGENTS on the prepared cloud
workspace and retain their exit status in setup diagnostics.
CC1.03 performs those commands again after hydration and combines them with
exact child revision checks. CC1.05 supplies the actual provider-host proof.

## Risks

Installing NBB through an NBB-only command would create a bootstrap cycle.
Prepare the minimum runtime first. Root tests may pass with missing children;
do not reuse that pass as CC1.03's readiness evidence.

## Anti-patterns

- Do not install every child package manager or dependency eagerly.
- Do not turn a missing optional child tool into a root-bootstrap pass/fail unless
  the root contract actually requires it.
