---
category: "kanban"
labels: "muse, knoxx, harness, 5sp"
type: "story"
story_id: "HAC.07"
points: "5"
title: "HAC.07 — Qualify permissioned memory promotion and revocation"
priority: "P1"
status: "incoming"
uuid: "hac-07-memory-promotion"
epic: "harness-actor-continuity"
parent: "harness-actor-continuity"
dependency: ["hac-02-actor-bindings", "hac-05-federation"]
---

# Qualify permissioned memory promotion and revocation

> Design: [harness actor continuity](../../notes/design/harness-actor-continuity.md)
> Research: [grounding](../../research/harness-actor-continuity.md)

## Context

Planning extends PR141; its LOC work is not implemented by the merge.

## Outcome

Qualify permissioned memory promotion and revocation.

## Scope

Use existing Knoxx/Axxium authorization and Clio events. Define the initial consistency rule as an online source-authority check for every protected read and every promotion; no offline grant cache authorizes disclosure. The check is the read authorization linearization point: revocations committed before it deny; already delivered content cannot be retracted. Revoked retained destination projections remain hidden until a new authorized promotion or rebuild passes current source-read and destination-publish checks. Non-revoked projections remain readable under the online read authorization rule without requiring a new promotion or rebuild for each read.

## Non-goals

No production activation or alternate board/event authority.

## Acceptance criteria

- [ ] GIVEN private source content WHEN promotion is proposed THEN a separately authorized event carries source identity and revision, destination scope, decision actor, transformation/redaction, reason, schema version, and revocation dependencies. Missing fields reject admission.
- [ ] GIVEN source authority is unreachable WHEN a protected read or promotion is requested THEN it fails closed without returning source-derived content.
- [ ] GIVEN revocation committed before read authorization, both before delivery and after projection creation, WHEN retrieval occurs THEN it denies disclosure and invalidates the affected materialized projection for future use.
- [ ] GIVEN a schedule or repeated references WHEN triggered THEN it may propose a lift but cannot authorize it.

- [ ] GIVEN an unauthorized lift, stale source revision or transformation bound to different source bytes WHEN promoted THEN admission rejects it before shared content becomes visible.
- [ ] GIVEN a lift WHEN authorized THEN the decision actor has current source-read and destination-publish authority, source policy permits that destination, and the transformed output hash binds the exact source revision and transformation version.

- [ ] GIVEN a revoked projection WHEN rebuild is attempted without current source-read and destination-publish authorization THEN no content is rebuilt or exposed.
- [ ] GIVEN an authorized rebuild WHEN a replacement projection is prepared THEN it reads the current source revision, revalidates source policy and revocation dependencies, recomputes the versioned transformation/redaction and output hash from those source bytes, and passes current source-read and destination-publish admission before publication. Its promotion event binds that current revision, policy/revocation state, transformation version and output hash; stale retained bytes or prior authorization cannot substitute. If the checked source or authorization state changes before publication admission, the replacement is rejected or revalidated before visibility.
- [ ] GIVEN a non-revoked projection and current read authorization WHEN read THEN content is available without rebuilding or issuing a new promotion.

## Verification

Implementation names the existing owner test runner, then records the isolated positive and negative scenarios above as version-bound receipts. Missing runtime access blocks qualification. This planning card claims no executed integration test or Rheos transition.

## Risks

Source and running runtime may diverge; verify deployed revision and actual capability.
