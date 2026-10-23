---
uuid: "b5f2ba9e-8d67-4c73-9919-e275983aca87"
title: "Select the physical graph kernel owner and numeric replay contract"
status: incoming
priority: P1
points: 3
labels: "cephalon, graph, architecture, numeric, planning"
epic: "63a0e4ff-353f-4c90-ab8a-7241d958d54c"
parent: "63a0e4ff-353f-4c90-ab8a-7241d958d54c"
---

## Context

Native CodeRabbit review `5442414707`, finding `4206988928`, identifies an
unassigned kernel/numeric choice ahead of the 8-point physical-field story.
Fork Tales supplies experimental Python mechanics and later corrections;
OpenPlanner supplies current graph persistence/query seams, and Foresight favors
portable laws where practical. Those facts do not decide the owning library or
numeric contract by themselves.

## Outcome

A reviewed, revision-bound architecture decision names one owning kernel and
its numeric/replay contract before solver implementation begins. It is an
explicit proposed predecessor of `ff3e628b-cd5c-4a89-a397-42cc88dbde70`.

## Scope

The current Foresight coordinator owns recovering the source-to-law mapping and
publishing the selection for planning review. The decision must name the owning
repository, library/module and integration seams, then be acknowledged through
that repository's reviewed implementation plan. OpenPlanner retains ownership
of its persistence/query surface; Knoxx retains product integration. A shared
law promotion needs its own reviewed authority decision.

Initial Markdown is proposed input. Native Rheos retention/enforcement of the
predecessor edge must be delivered and verified through the epic's separately
owned upstream prerequisite before either story is admitted.

## Non-goals

Implement a solver before selection, assign common-law authority from a donor
name, duplicate a graph force engine, copy Fork Tales' effect runtime, migrate
board semantics locally, or treat a documentation link as native admission.

## Acceptance criteria

1. The decision names one owning repository and library/module, accountable
   implementation role, consumer adapters and exact reviewed source revisions.
   It explains which Fork Tales corrections are retained and any non-equivalent
   older formulation. Unresolved mechanics stay blocking rather than disappearing
   from the solver's acceptance criteria.
2. It specifies numeric representation on each actual target, units, timestep
   schedule and bounds, finite-value behavior, seeded exploration semantics,
   rounding/order rules and declared absolute/relative replay tolerances.
   Cross-runtime parity is required only for targets the decision actually selects.
3. It maps solver state, graph/field persistence and traced recall to their
   ownership boundaries, including authorization before traversal and feedback.
   There is one physical computation authority with outward runtime adapters.
4. Independent planning review assesses the choice and its estimates. Native
   Rheos records lawful readiness and the retained predecessor relationship
   after the upstream admission prerequisite passes. Before that, the relationship
   and 3-point estimate remain provisional.
5. The successor's RED tests and GREEN solver/replay bind this decision's revision,
   kernel configuration and numeric tolerance; a changed selection requires fresh
   review and cannot reuse incompatible replay evidence.

## Verification

Retain the decision diff, source map, native review IDs and exact source/kernel
revisions. Use native Rheos for readiness and predecessor observations. The
successor demonstrates executable numeric and coupling laws after admission;
static planning checks do not prove that the solver exists or replays correctly.

## Risks

Premature common-law promotion, a choice incompatible with persistence or host
targets, numeric tolerance hiding divergent behavior, and a nominal owner without
a reviewed owning-repository delivery plan. Three points is a planning estimate;
implementation cost remains in the separately reviewed solver story.

License: GPL-3.0-or-later.

## Concrete selection proposal for review

The [owner and numeric contract proposal](../../notes/2026-10-07-cephalon-field-kernel-selection.md)
selects the existing standalone `octave-commons/eros-eris-field` library as the
proposed physical computation owner, with OpenPlanner storage/query and Knoxx
character adapters. It binds actual source revisions, binary64 Node authority,
fixed-step/residual time, sparse velocity fields, immutable emitted ownership,
physical contacts in recall and exact discrete replay outcomes.

This is planning input. Owning-repository acknowledgement, reviewed calibration
and collision/license decisions, native Rheos readiness and RED/GREEN remain
required. Status, UUID, relationships and the provisional 3-point estimate are
unchanged; no board event or accepted owner handoff is claimed.
