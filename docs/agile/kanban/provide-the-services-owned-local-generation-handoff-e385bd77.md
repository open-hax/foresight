---
category: "kanban"
labels: "services, generation, external-prerequisite"
dependency: []
type: "task"
write-id: "1793736701662-0.1uaouushnw3akx2c95o"
points: "13"
title: "Provide the Services-owned local-generation handoff"
priority: "P1"
status: "in_progress"
uuid: "3f5d0052-dda2-4089-971d-8603e385bd77"
created_at: "2026-11-03T20:02:18.944Z"
---

# Provide the Services-owned local-generation handoff

## Outcome

Services publishes an independently reviewed, callable and versioned local-generation handoff that Oikos S1B can consume without implementing its own promoter.

## Context

The user explicitly authorized owner-side prerequisite work on 2026-11-03. Source discovery found that the existing DigitalOcean receiver and deployment model do not provide a desktop/local-generation API. S1A is merged at `62e6eebbf75c3f2b28e4a550b4a01881a2f5e732`; that does not supply promotion or rollback. This external prerequisite is separate from the sixteen Oikos child cards.

## Scope

Work only in an isolated Services worktree. Review the proposed owner architecture before implementation; preserve the production deployment lane. Keep pure plan/result/approval semantics portable and runtime effects in an explicit owner adapter.

## Acceptance criteria

- [ ] Owner-reviewed architecture explicitly admits the narrowly scoped local-generation responsibility; no existing production receiver is relabelled as this API.
- [ ] Versioned closed contracts bind exact source/validator/plan/approval revisions, owned paths and prior generation identity, with input-free failures.
- [ ] Actual sandbox fixtures prove effect-free preview/check, positive admitted apply, unapproved/dirty/stale ancestor/destination/selection refusal, interrupted-stage exact rollback and uncertain-recovery holds through the real owner boundary.
- [ ] Independent implementation review and actual owner deterministic gates qualify the exact source, callable entry and explicit Oikos handoff; a proposal or mock supplies no availability credit.

## Verification

Use the reviewed Services specification and its owner-native commands. Retain baseline, RED/GREEN, race/interruption fixture results, exact source hashes and independent review. Foresight does not duplicate the owner implementation or its board engine.

## Non-goals

No live workstation writes, installed generation selection, DigitalOcean deployment, unrelated Services effects, personal inputs, secrets, model/service changes, or Oikos-owned replacement supervisor. No Oikos child/epic completion is implied by creating this prerequisite.

## References

- Oikos S1B: `3d14e0a5-2790-42b8-9a55-54d686925c3c`.
- `.ημ/runs/oikos-program-20261103/reviews/s1b-owner-prerequisite-discovery.edn`.
- `.ημ/runs/oikos-program-20261103/decisions/s1b-owner-prerequisites-authorization.edn`.

---
User-authorized upstream prerequisite tracking under .ημ/runs/oikos-program-20261103/decisions/s1b-owner-prerequisites-authorization.edn. Actual owner planning input: services head ebe5a49f00cfc3fb1501955d8707e6076cf7dc81, isolated worktree Services/.worktrees/feat/oikos-local-generations-20261103. Independent owner planning review is pending; this plan is NOT a callable or qualified API/artifact and grants no implementation/completion or consumer-admission credit. Root board records work only; owner code/architecture authority remains with services. Separate external root task, not an Oikos child/epic or new Oikos dependency edge. Stop at breakdown pending planning review; no ready/todo/in_progress admission. Sandbox fixtures only, no installed/live effects, personal inputs, spending or model/service changes.

Parent independent owner planning-fix review .ημ/runs/oikos-program-20261103/reviews/services-generation-plan-fix-review-v2.edn APPROVE at exact Services plan head f8d63271bd3d14cc6ce5ec33466c6e8a747d5d10; specification SHA256 500be9073f22ae74e880e50a55627802274911a136059f92dcf53ac023bcf112. All four planning gaps are settled. User authorizes bounded owner sandbox RED/GREEN only, not API availability or final qualification. Full baseline Caddy/JVM holds require actual zero-error resolution before handoff; no gate suppression or bypass. Root board records parent review evidence, not independently rerun verification. Code stays in the isolated Services owner worktree; no live/installed effects, deployment, personal inputs, model/service changes or spending. Actual owner deterministic tests and independent code review remain required for consumer handoff.

---