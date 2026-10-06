---
uuid: "8d14e041-1578-4f47-8d8f-5d9f764b32d4"
title: "Route agents through the generated project guide"
status: "incoming"
priority: "P2"
points: 1
labels: documentation, onboarding, project-model
---

# Route agents through the generated project guide

## Context

[GitHub issue #81](https://github.com/open-hax/foresight/issues/81) identifies
an independently maintained repository map in `AGENTS.md`. The existing
`nbb scripts/project.clj guide` command already projects the semantic sources
and root-native components, including deduplication of shared paths.

## Outcome

An actor obtains the current routing inventory directly from the semantic
project model and still grounds local facts in the selected owner's evidence.

## Scope

- Replace the static AGENTS repository table with the existing guide command.
- Keep authority-order, ownership, promotion, and manifest-drift guidance.
- Correct the instructions for adding sources to describe the validator's
  actual manifest/model boundary.
- Point extracted-child routing assertions at the existing generated guide,
  retaining their repository-route and quality-gate checks.

## Non-goals

- Change project inventory data, child gitlinks, pure routing laws, or runtime
  behavior.
- Parse AGENTS prose into semantic authority or commit another generated table.
- Admit readiness, implementation status, or completion without Rheos and the
  review evidence required by PR Flow.

## Acceptance criteria

- AGENTS directs actors to the generated guide instead of an independently
  maintained repository table.
- Sources and native components enter the routing view through the semantic
  model; Eta's shared path remains one route with both roles.
- Child-local evidence still controls local facts; routing confers neither
  ownership nor accepted cross-repository promotion.
- Manifest/model validation and the existing routing tests pass offline.

## Verification

Run `nbb scripts/project.clj guide`, `nbb scripts/project.clj validate`,
`nbb -cp src:scripts:test test/project_test.cljs`,
`nbb -cp scripts:test test/workspace_test.cljs`, and
`clj-kondo --lint scripts test`. Inspect the documentation diff and verify
that historical receipts remain unchanged. Native PR planning review and
Rheos readiness remain separate qualifications.

## Risks

A reader needs NBB to print the routing view. The source model remains a
readable fallback when NBB is unavailable; unavailability must remain visible.
No committed projection means there is no additional stale-output gate.

## Planning provenance

This hand-authored incoming card is a supported Markdown input. It carries no
invented operational admission event, write ID, or reviewed-ready claim.
