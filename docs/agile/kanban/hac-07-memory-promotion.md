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

- [ ] GIVEN private source content WHEN promotion is proposed THEN a separately authorized event binds source identity and revision, destination identity and scope, source-policy decision and revision, both source-read and destination-publish grant identities and revisions, decision actor, transformation/redaction version, output content hash, reason, schema version, and revocation dependencies. Missing fields reject admission. Before initial publication makes output visible, hash the actual bytes being published and compare that hash with the event-bound output content hash; any mismatch rejects publication without visibility. Also compare every bound authorization value with its current state and reject publication if the source or destination, policy decision/revision, either grant identity/revision, revocation state, transformation version or output hash has changed.
- [ ] GIVEN source authority is unreachable WHEN a protected read or promotion is requested THEN it fails closed without returning source-derived content.
- [ ] GIVEN revocation committed before read authorization, both before delivery and after projection creation, WHEN retrieval occurs THEN it denies disclosure and invalidates the affected materialized projection for future use.
- [ ] GIVEN a schedule or repeated references WHEN triggered THEN it may propose a lift but cannot authorize it.

- [ ] GIVEN an unauthorized lift, stale source revision or transformation bound to different source bytes WHEN promoted THEN admission rejects it before shared content becomes visible.
- [ ] GIVEN an initial lift WHEN attempted THEN current source-read authority, destination-publish authority and source-policy approval for that destination must all pass before accessing source bytes or running the transformation. If any check fails, no source bytes are accessed, no transformation runs, and no output, promotion event or projection is produced; a content-free denial audit record is permitted. An authorized lift binds the transformed output hash to the exact source revision and transformation version.
- [ ] GIVEN a decision actor, source-read grant or destination-publish grant WHEN an initial lift, rebuild or publication is attempted THEN each local and remote actor reference is mapped through its identity provider, and each destination independently validates audience, tenant, role, capability and current grant. Display-name equality grants no authority. Missing or mismatched checks deny before source-byte access, transformation or visibility. Qualification receipts for both initial lift and rebuild include independent cross-tenant, wrong-audience and identity-mapping denial cases with no source access or publication.

- [ ] GIVEN a revoked projection WHEN rebuild is attempted THEN current source-read and destination-publish authorization and current source-policy approval for the destination must all pass before accessing source bytes or transforming content. If any check fails, no source bytes are read, no transformation runs, and no promotion event or projection is produced or exposed; a content-free denial audit record is permitted.
- [ ] GIVEN an authorized rebuild WHEN a replacement projection is prepared THEN it first passes current source-read and destination-publish authorization and current source-policy approval for the destination before source-byte access or transformation, reads the current source revision, revalidates source policy and revocation dependencies, recomputes the versioned transformation/redaction and output hash from those source bytes, and passes current source-read and destination-publish admission before publication. Its promotion event binds the source revision, source-policy decision/revision, revocation dependencies, both source-read and destination-publish grant identities/revisions, transformation version and output hash; stale retained bytes or prior authorization cannot substitute. Publication admission hashes the actual replacement bytes and checks their transformation version against the event-bound output hash and version before visibility, then compares the current source revision, source-policy decision/revision, revocation dependencies and both authorization grants against those event-bound values. Any difference rejects the replacement or requires revalidation and a newly bound promotion event before visibility. Test each changed input independently during transformation, including policy and revocation changes without source-byte changes.
- [ ] GIVEN a non-revoked projection and current read authorization WHEN read THEN content is available without rebuilding or issuing a new promotion.

## Separate qualification receipts and exits

- Initial promotion, protected reads and revocation invalidation have a dedicated receipt naming the source/destination revisions and their authorization matrix. This slice exits only after authorized promotion/read, unauthorized pre-read denial, unreachable-authority denial, scheduled-proposal non-authority, and post-materialization revocation denial all pass. The initial-promotion receipt also independently exercises output-byte/hash mismatch and every bound authorization change after event binding but before publication (source/destination, policy decision/revision, either grant identity/revision, revocation state and transformation version); each rejects or revalidates with a newly bound event before visibility. No output is visible while revalidation is pending.
- Revoked-projection rebuild has a separate receipt and authorization/publication-race matrix. This slice exits only after denied rebuild performs no source access or transformation, authorized rebuild binds current inputs/output, each source/policy/revocation/grant change before publication rejects or revalidates without premature visibility, and separate source-substitution and destination-substitution tests independently deny visibility even with otherwise matching content/hash/revision metadata and valid current grants. Both substitution results are mandatory entries in this rebuild receipt and completion matrix. Separate transformation-version-change and output-byte/hash-change cases before publication must each independently reject or revalidate without premature visibility, even when source, policy, revocation and grants are unchanged. Both results are mandatory receipt and completion entries, independent of the other race cases.
- HAC.07 completes only when both receipts pass against compatible pinned contract/runtime revisions and cross-reference the same source/projection identity. Passing one slice supplies no completion evidence for the other. These are implementation evidence slices, not new board identities or transitions.

## Verification

Implementation names the existing owner test runner, then records the isolated positive and negative scenarios above as version-bound receipts. Missing runtime access blocks qualification. This planning card claims no executed integration test or Rheos transition.

## Risks

Source and running runtime may diverge; verify deployed revision and actual capability.


## Rebuild source and destination binding

- [ ] GIVEN a rebuilt projection event WHEN publication is admitted THEN the actual source identity and destination must each equal the event-bound source and destination, in addition to all existing content, transformation, revision, policy, revocation and grant comparisons.
- [ ] GIVEN otherwise matching content/hash/revision metadata and valid current grants WHEN publication substitutes a different source identity or a different destination THEN each substitution independently denies visibility. A separately authorized source or destination requires a new correctly bound event, never retargeting an existing rebuild event.
