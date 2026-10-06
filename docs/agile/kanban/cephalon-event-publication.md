---
uuid: "25e3a688-09b5-4e9a-8765-9b13944b1a03"
title: "Trigger creative work and publish accepted pieces with native receipts"
status: incoming
priority: P1
points: 3
labels: "knoxx, cephalon, triggers, bluesky, discord"
epic: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
parent: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
dependency: "25e3a688-09b5-4e9a-8765-9b13944b1a02"
---

## Context

Clock, conversation, artifact completion and arbitrary configured observations
can motivate creative work. Publication is an external effect with replay risk.

## Outcome

Clock and non-clock events use one admissible bounded work path. Original
selected pieces reach Bluesky and occasional Discord sharing, with verified IDs.

## Scope

Reusable event/resource agreements, configured publication frequency, saved
artifact identity and durable publication attempt/result records. Reuse existing
Bluesky/Discord clients and preserve labels/access policy.

## Non-goals

Reply to every third-party message, publish private context, add another event
transport, or treat a localhost artifact URL as publicly accessible.

## Acceptance criteria

1. A manually dispatched arbitrary creative-request event follows the same
   maker path as the clock, with no overlap or conversation cancellation.
2. Publication occurs only after artifact validation and records account,
   artifact identity, native URI/message ID and attempt outcome.
3. Replaying the same accepted event does not duplicate its publication.
   Ambiguous external completion is reconciled before retrying.
4. The configured initial policy permits at most one Bluesky creation post per
   30 minutes and one unsolicited Discord creative share per hour; operator
   replies have a separate initial limit of ten newly admitted head turns per
   60 seconds per operator actor/channel. Excess input receives an explicit
   rate-limit outcome or is coalesced with an observable receipt; it cannot
   silently disappear or interrupt a maker. These configurable policies are
   runtime-enforced, not only prompts.
5. Verify one real Bluesky post and Discord attachment independently, without
   exposing credentials. An unavailable outlet records a failure/backoff.

## Verification

Red replay/frequency/unknown-outcome laws; adapter failure tests; backend gates.
Human script defaults to dry-run and requires explicit publication mode against
the intended identity, then verifies native records and cleans up owned fixtures.

## Risks

Provider retries after accepted writes, media limits, inaccessible links, bot
feedback and externally deleted posts. Publication authorization is scoped to
the configured account and home channels.
