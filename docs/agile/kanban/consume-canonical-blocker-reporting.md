---
uuid: "8359f0ea-0f80-4a3a-bcdf-514d2692fa8d"
title: "Consume canonical Rheos blocker reporting without fabricating progress"
type: "task"
status: "incoming"
priority: "P1"
points: "3"
labels: "rheos, blocking, workflow, isolation"
parent: "0d73c22a-b018-4b8c-92fe-560127f53294"
---

# Consume canonical Rheos blocker reporting without fabricating progress

## Context

The October 6 backlog sweep is authorized to put blocked work aside and mark it
blocked. The current canonical Promethean FSM permits entering `blocked` only
from `breakdown`. Real `incoming` and `todo` cards cannot report their blockers
directly. Reproductions and immutable source references are recorded in
[the observation](../../notes/blocked-transition-observation-2026-10-06.md).
The missing capability is owned by [Rheos issue #4](https://github.com/open-hax/rheos/issues/4).

## Outcome

After the upstream transition contract is reviewed and qualified, Foresight can
record an actual obstruction through Rheos from the supported unfinished stage,
without asserting acceptance, execution, readiness, or completion first.

## Scope

- Track upstream review of direct blocker reporting, including terminal and
  unknown status rejection, preserved forward gates, and unchanged blocked exits.
- Consume an immutable qualified Rheos revision through the existing runtime
  integration boundary. Do not invent a Foresight FSM or local board parser.
- Exercise real CLI and agent status-update operations against a disposable
  board in this PR's isolated worktree, with its own configuration and ledger.
- Re-inspect the affected cards and their prerequisites before any eventual
  live transition; cite concrete current unblock proof in a Rheos comment.
- Preserve historical event and receipt bytes and record the actual upstream
  revision, operation result, source/destination state, and appended event.

## Non-goals

No local shadow implementation, altered forward admission gates, fabricated
lifecycle hops, bulk status conversion, default/shared board writes, or global
CLI installation. This planning card supplies no transition or ready admission.

## Acceptance criteria

- A `todo` card with verified unfinished prerequisites can be marked blocked
  through the reviewed canonical operation; unsupported sources remain explicit
  refusals instead of metadata edits.
- An `incoming` card with a verified missing prerequisite can report obstruction
  without implying its planning has been accepted or its work started.
- The operation cannot create new readiness or completion bypasses, and upstream
  policy decides how blocked WIP capacity applies to obstruction reporting.
- Successful operations append canonical events while retaining every previous
  ledger byte. Rejected operations leave card and ledger unchanged.
- Each parallel lane uses its own worktree, configuration, board, outputs, and
  runtime resources. Any reproducible interaction between lanes is filed as
  its own upstream issue/card before claiming isolation.

## Verification

Current observation: `eta-mu kanban count` reports 60 cards; `eta-mu kanban drift`
reports no drift. Both `move` and `status-update` reject the attempted
`todo → blocked` operation; `move` also rejects `incoming → blocked`. No tracked
file changed. This is a capability limitation, not a successful blocker write.

Future verification uses the upstream implementation's own law and actual
transition/writeback fixtures, followed by a disposable Foresight CLI exercise.
Keep failed, unavailable, and unqualified results visible. Do not infer review
completion from local tests or an issue acknowledgement.

## Risks

Changing status routes is workflow policy. Review must establish the supported
source set and blocked capacity behavior upstream. Review delays and missing
reviewer infrastructure remain observed availability states; they do not supply
approval or permit rewriting status manually. A verified available reviewer may
still qualify a PR under the canonical policy.
