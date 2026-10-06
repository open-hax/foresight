---
uuid: "018783c2-0c1a-4cbb-99d1-9ca25c2da309"
title: "Coordinate shared native reviewer capacity across isolated PR lanes"
type: "task"
status: "incoming"
priority: "P1"
points: "3"
labels: "review, quota, workflow, isolation"
parent: "0d73c22a-b018-4b8c-92fe-560127f53294"
---

# Coordinate shared native reviewer capacity across isolated PR lanes

## Context

Independent worktrees do not isolate provider review allowance. During the
October 6 sweep, native CodeRabbit comments on
[Foresight #129](https://github.com/open-hax/foresight/pull/129#issuecomment-6016988986)
and [#130](https://github.com/open-hax/foresight/pull/130#issuecomment-6017059669)
reported exhausted included capacity in the same repository, with 59-minute and
47-minute reset observations. Canonical per-PR requests refused retry before the
parsed cooldown. Native Codex also reported exhausted account review quota.

These observations establish shared availability; they do not identify which
attempt consumed the last review. No checkout, configuration, board, package or
daemon interaction was observed between the isolated lanes. The remaining
external resource coupling is owned by
[Eta-Mu issue #343](https://github.com/open-hax/eta-mu/issues/343).

## Outcome

Manual native review requests from parallel PR lanes are admitted through one
canonical capacity boundary for their verified provider scope, preserving exact
PR/head identity and truthful queued, pending, unavailable, and completed states.

## Scope

- Review the upstream provider-scope and reservation contract, reusing existing
  Eta-Mu admission/publication, Sol execution and Clio event boundaries.
- Preserve native request evidence, parsed cooldowns, pending deduplication and
  distinction between automatic provider triggers and managed manual requests.
- Consume only a reviewed qualified implementation. Foresight records its
  affected PR lanes and observations; it does not implement a local scheduler.
- Exercise concurrent same-scope and independent-scope requests, interrupted
  publication, restart, failed dispatch and changed-head cases in disposable
  fixtures before claiming live integration.

## Non-goals

No new provider identity, capacity purchase, quota bypass, unverified capacity
claim, copied gate policy, fabricated approval or live hosting activation.
This is proposed Markdown input; no operational admission is claimed.

## Acceptance criteria

- Exhausted known shared capacity prevents duplicate managed manual requests
  across lanes and retains each lane's exact head and visible queued result.
- A verified independent provider scope can progress without global suppression.
- Unknown scope or reset remains explicit and cannot invent a retry deadline.
- A stale head cannot publish or consume its successor's reservation; pending
  requests retain their native IDs and are not submitted again as new work.
- Quota, cooldown and skipped reviews supply neither approval nor round credit;
  canonical deterministic gates and finding settlement remain required.
- Existing immutable events survive retries/restarts; concurrent fixtures prove
  no duplicate request or publication effect.

## Verification

Current evidence consists of the linked native availability messages and the
actual canonical CLI cooldown refusals. It proves shared review availability,
not a causal claim about a particular worker or a deployed admission service.
Future qualification belongs to the upstream implementation and its exact-head
fixtures; consume its immutable source and runtime evidence afterward.

## Risks

Provider scopes can differ across repository, organization and account and may
be unknown. Automatic provider-triggered reviews may occur outside a manual
request coordinator; the contract must disclose that limit and preserve native
observations instead of claiming complete quota control. Fairness and stale-head
reservation disposition require upstream planning review before implementation.
