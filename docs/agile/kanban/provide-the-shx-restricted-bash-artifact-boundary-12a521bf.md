---
category: "kanban"
labels: "shx, safe-bash, external-prerequisite"
dependency: []
type: "task"
write-id: "1793736499450-0.ywpd48cpknjjg4jrdl4"
points: "8"
title: "Provide the shx restricted Bash artifact boundary"
priority: "P1"
status: "in_progress"
uuid: "577a41a3-3778-417b-b65d-186c12a521bf"
created_at: "2026-11-03T20:02:20.984Z"
---

# Provide the shx restricted Bash artifact boundary

## Outcome

shx publishes a reviewed restricted Bash renderer with an independently pinned, non-evaluating positive validator and an actual qualified consumer handoff for Oikos S1B.

## Context

The user explicitly authorized owner-side prerequisite work on 2026-11-03. Existing generic IR and envm behavior are not this closed safe subset; native shx `check` evaluates Bash and cannot supply the required independent validator. S1A truthfully rejects unavailable Bash. This external prerequisite is separate from the sixteen Oikos child cards.

## Scope

Use an isolated shx worktree and owner-native layer/gate conventions. Independently review the owner specification before RED/GREEN. Preserve existing generic IR, quoting, envm and PATH semantics.

## Acceptance criteria

- [ ] Closed literal/reference-only data and restricted IR reject runtime values, metadata, unknown nodes and undeclared references without host environment discovery.
- [ ] Renderer emits the exact accepted canonical grammar; the separate bounded parser/validator verifies every binding, exact bytes, descriptor and trusted revision without importing the renderer or executing generated Bash.
- [ ] Independently pinned renderer/validator bundles and a fixed non-evaluating transport are actually qualified with shared JVM/BB/NBB fixtures, hostile inputs, finite stream/deadline/cleanup bounds and zero personal discovery.
- [ ] Owner strict analysis, applicable mutation/survivor evidence and independent code review qualify the exact API/source and positive validator handoff. Baseline warnings and unavailable tooling stay visible.

## Verification

Follow the reviewed owner specification, `bin/analyze --strict`, relevant test suites and `bb mutate` obligations. Preserve RED/GREEN and independent grammar/golden/transport evidence; no mock or evaluating shell check counts as a positive owner artifact.

## Non-goals

No generated Bash execution, generic shell kernel replacement, changed envm semantics, live configuration/promotion, personal inputs, model/service effects, credentials or spending. No Oikos child/epic completion follows from this prerequisite's creation.

## References

- Oikos S1B: `3d14e0a5-2790-42b8-9a55-54d686925c3c`.
- `.ημ/runs/oikos-program-20261103/decisions/s1b-owner-prerequisites-authorization.edn`.
- Owner plan: `docs/specs/restricted-bash-v1.md` in the isolated shx task worktree.

---
User-authorized upstream prerequisite tracking under .ημ/runs/oikos-program-20261103/decisions/s1b-owner-prerequisites-authorization.edn. Actual owner planning input: shx head cbd0b4349b3f338a86e5b32638b4ac7325786686, isolated worktree shx/.worktrees/feat/oikos-safe-bash-20261103. Independent owner planning review is pending; this plan is NOT a callable or qualified API/artifact and grants no implementation/completion or consumer-admission credit. Root board records work only; owner code/architecture authority remains with shx. Separate external root task, not an Oikos child/epic or new Oikos dependency edge. Stop at breakdown pending planning review; no ready/todo/in_progress admission. Sandbox fixtures only, no installed/live effects, personal inputs, spending or model/service changes.

Parent reports independent shx planning-fix review APPROVE at exact owner plan head 79ca039d0137aca1e59ebdbf4cd16eb46f737495, specification SHA256 2dbeda258a384caa089e64ec578f57abfa85e123581559a603e55615c2abdcdc; evidence .ημ/runs/oikos-program-20261103/reviews/shx-safe-bash-plan-fix-review-v2.edn. Sole P2 operation-envelope contradiction is settled; no operator question remains. User authorizes ordinary breakdown→ready→todo→in_progress admission for owner RED/GREEN in the isolated shx sandbox only. This does NOT establish API availability, positive validator qualification or full deterministic gate passage. Existing typed.clojure deprecation warning remains a qualification hold until properly resolved; do not suppress gates or weaken the warning ratchet. Actual root-canonical owner analyze and mutation evidence plus independent code review remain required before consumer admission. No generated Bash execution, installed/live effects, personal discovery, model/service changes or spending authorized. Services prerequisite remains breakdown/pending separate review; no Oikos or other task admission implied. This board operator records parent review evidence, not an independently rerun review.

---