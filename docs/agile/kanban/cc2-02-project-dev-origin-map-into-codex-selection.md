---
category: "kanban"
labels: "codex-cloud, forks, project-model, 3sp"
story_id: "CC2.02"
dependency: ["codex-cloud-access-tiers", "dev-origin-map"]
parent: "codex-cloud-repository-access"
type: "story"
write-id: "1791131652438-0.oo6i917o83sw1pc7ts"
points: "3"
title: "CC2.02 — Project the validated dev-origin map into Codex repository selection"
priority: "P1"
status: "ready"
epic: "codex-cloud-repository-access"
uuid: "codex-cloud-fork-selection-projection"
---

# CC2.02 — Project the dev-origin map into Codex repository selection

## Context and dependencies

Requires CC2.01 and the external `dev-origin-map` story in
`fork-dev-origins`. That story must deliver `config/dev-origins.edn` and its
validator; neither exists at this planning baseline. This story stays blocked
on those outputs rather than creating a substitute mapping.

## Outcome

When a task needs write access to a child repository, the repository identity
selected in Codex Cloud comes from the same validated fork↔org mapping used by
the Promethean development process.

## Scope

A pure task-scoped selection projection consumes the validated fork map,
named capability, current authorized path, and explicit per-repository
activation evidence. Runtime/provider adapters supply observations at the edge.

The activation authority remains the existing Promethean process in
`docs/notes/promethean-review-and-promotion.md` and
`docs/notes/design/promethean-pr-process.edn`: verified fork map, protection,
trusted required gate, and trusted staging controller. A map or local remote
cannot establish activation. Until trusted per-repository evidence is available,
use only a known existing authorized path; an unknown path/permission fails.

## Tasks within this story

- [ ] Consume the completed map's validator and reject invalid/missing maps
  without reconstructing names from repository basenames.
- [ ] Define projection inputs/outputs and fixtures for preactivation,
  verified activation, absent/stale/conflicting evidence, and network-name
  exceptions; label simulated activation fixtures as local test data.
- [ ] Implement the pure selection decision and a provider-facing projection
  for only the repositories needed by the named task.
- [ ] Verify unverified activation never selects a planned fork; preserve a
  known currently authorized path or report unavailable authorization.
- [ ] Document who supplies trusted activation evidence and retain provenance;
  consuming missing live evidence must not assert that activation occurred.
- [ ] Pass the selected identity and activation disposition to CC2.03/04.

## Non-goals

Creating forks, activating the fork process, inventing an activation registry,
changing source gitlinks, or granting provider permission.

## Acceptance criteria

- [ ] GIVEN a child whose fork-development process is activated WHEN Codex must
  push feature work THEN the selected writable repository is the mapped
  development fork.
- [ ] GIVEN a child whose fork-development process is not activated WHEN Codex
  must push THEN the current authorized repository path remains in force; this
  story does not infer activation from planning data.
- [ ] GIVEN an upstream repository that is already readable as a pinned
  submodule WHEN no provider-level upstream operation is required THEN upstream
  is not redundantly selected merely because the fork is selected.
- [ ] GIVEN a repository-network naming exception WHEN the fork map provides an
  explicit development repository identity THEN Codex selection uses that
  identity instead of guessing `riatzukiza/<upstream-name>`.
- [ ] The projection is derived from the validated source/fork model rather than
  maintained as a second hand-written complete table.
- [ ] GIVEN absent, stale, or conflicting activation evidence THEN the planned
  fork is not used; a known existing authorized path remains available, and an
  unknown authorized path fails explicitly.

## Dependencies

This story consumes `dev-origin-map` from the existing
`fork-dev-origins` epic. It does not replace or weaken that epic's activation
requirements.

## Verification

Run the completed map's validation and pure projection fixtures for both
activation states and explicit naming exceptions. Retain the distinction
between simulated state tests and real activation evidence. CC2.04 supplies
actual provider capability proof for the policy state available at acceptance.

## Risks

The external map and trusted activation evidence are planned dependencies.
Neither a missing file nor a proposed process is live infrastructure. Adding
an alternative fork/activation authority here would hide that dependency.

## Anti-patterns

- Do not treat a planned fork as an activated write target.
- Do not invent fork names from repository basenames.