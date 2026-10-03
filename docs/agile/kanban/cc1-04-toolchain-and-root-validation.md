---
category: "kanban"
labels: ["codex-cloud", "tooling", "validation", "3sp"]
type: "story"
story_id: "CC1.04"
points: "3"
title: "CC1.04 — Prepare the minimum cloud toolchain and run Foresight root validation"
blocked_by: ["codex-cloud-bootstrap-contract"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-toolchain-validation"
---

# CC1.04 — Prepare the minimum cloud toolchain and run root validation

## Outcome

Cloud setup installs or verifies only the tools needed to prove that the
hydrated Foresight workspace is coherent before a task starts doing project work.

## Acceptance criteria

- [ ] GIVEN a supported clean Codex Cloud host WHEN setup runs THEN the required
  Node/NBB and lint/validation tooling used by the root gate is available, or the
  missing tool is reported explicitly.
- [ ] GIVEN a hydrated workspace WHEN setup validation runs THEN
  `nbb scripts/project.clj validate` succeeds before the environment is treated
  as ready.
- [ ] GIVEN successful project validation WHEN onboarding output is requested
  THEN `nbb scripts/project.clj guide` can render the current routing view.
- [ ] GIVEN successful hydration WHEN inventory runs THEN
  `nbb scripts/workspace.clj inventory` reports the declared direct children
  and consolidation inputs without granting consolidation inputs execution
  authority.
- [ ] Setup records the effective tool versions needed to reproduce a failure
  without claiming that an unavailable tool passed.

## Verification

Run the same root commands documented in README/AGENTS on the prepared cloud
workspace and retain their exit status in setup diagnostics.

## Anti-patterns

- Do not install every child package manager or dependency eagerly.
- Do not turn a missing optional child tool into a root-bootstrap pass/fail unless
  the root contract actually requires it.
