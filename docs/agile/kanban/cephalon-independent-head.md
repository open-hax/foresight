---
uuid: "25e3a688-09b5-4e9a-8765-9b13944b1a01"
title: "Converse through a reserved head lane while makers continue"
status: incoming
priority: P1
points: 3
labels: "knoxx, cephalon, admission, laws"
epic: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
parent: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
---

## Context

Triggered conversation and clock work currently share one event FIFO. Ordinary
HTTP turns bypass it. Different agent contracts alone do not reserve capacity.

## Outcome

Contract-declared head work has reserved bounded admission; background work keeps
its own queue and session. This mechanism is portable data at the decision layer.

## Scope

Pure lane/queue laws, authorized trigger argument forwarding, runner admission,
queue diagnostics, scoped operator conversation agreement and live verification.
Validate delegated maker identity at the tool boundary and enforce acknowledgment
before dispatch for creative requests; prompt order alone is insufficient.

## Non-goals

Change provider execution semantics, allow event payloads to claim privileged
lane policy, cancel makers, or promise zero network/model latency.

## Acceptance criteria

1. Hold a maker unresolved; a head invocation starts before that maker resolves.
2. Head completion leaves the maker active with the same run/session identity.
3. Each lane is bounded and FIFO internally; releasing an owner promotes only
   its lane's successor. Failure and rejected admission retain existing evidence.
4. Lane choice comes from an admissible resource agreement, with invalid choices
   refused and ordinary legacy events retaining their prior behavior.
5. Scoped operator messages need no keyword or mention; unrelated channels and
   self-authored bot messages do not start head turns.
6. In the live test, admission completes within two seconds on the local host.
   Measure full reply latency separately; target 30 seconds for a warmed healthy
   provider, report failures honestly and do not enforce model latency in CI.
7. For a creative request, the native acknowledgment receipt precedes child
   admission, including a model response containing both calls in one batch.
   Invalid or unsupported delegated agent specifications are refused explicitly;
   they cannot silently fall back to a different contract. A successful child
   admission identifies the intended maker, and is never labeled completion.

## Verification

Red pure reservation and asynchronous runner tests before implementation. Full
backend tests, server compilation and warning/boundary gates. Runnable isolated
fixture starts a held maker and head, observes both, and cleans up owned data.

## Risks

Sticky conversation locking, misleading accepted responses, head flooding and
total provider capacity. Additional slots require explicit configured budgets.
