---
status: incubating
created: 2026-10-21T01:56:09.219021+00:00
source-session: codex/stabilization-portfolio
source-task: Audit live source tuples across the 442 frozen PRs
p-efficiency: 0.86
p-friction: 0.55
p-skill-candidate: 0.85
promoted-to: ""
rejected-reason: ""
---

## Problem

A reported PR base SHA, the live base branch and a native proposed merge can
identify different revisions. Treating any one as executed source can admit
stale test evidence even while a PR looks mergeable.

## Pattern

The two-pass E0 audit observed 179 open reported-base/live-tip differences and
five proposed merges with old base parents. All PR base-ref targets matched
independently read refs; 301 proposals matched B/H but none became test proof.
These are observations at named sources, not a universal API-staleness rule.

## Candidate skill outline

- Trigger: qualify/reconcile a PR or update a frozen head/base census.
- Retain reported PR fields separately from independently read branch refs.
- Read complete proposed-commit parents and bind actual B/H; retain null/conflict.
- Obtain actual CI checkout and tree proof separately; proposed is not tested.
- Bind reviews/checks to the exact source and invalidate changed B/H/M evidence.
- Never discard unique source, synthesize a candidate or carry approval to a new head.

## Better path

Use typed observations and two source/ref passes with explicit gaps. Preserve
raw native bytes and derive an additive overlay. A green check channel or a
resolved thread cannot substitute for scope, warning or runtime qualification.

## Receipt refs

- Native evidence bc949fd90dcd05c712060938dae7a6e695c084003cc3554f3fbea65f46bd339e
- Execution attempt 835a586492376530c78af6b79eebf5645709d3dea6fd68dd4035154c8ffa0bcd
- docs/stabilization/e0-base-topology.json

License: GPL-3.0-or-later. Incubating only; no same-session promotion.
