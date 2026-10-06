---
uuid: "25e3a688-09b5-4e9a-8765-9b13944b1a04"
title: "Supervise creative work and recover one cephalon owner across restarts"
status: incoming
priority: P1
points: 5
labels: "knoxx, cephalon, services, recovery"
epic: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
parent: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
dependency: "25e3a688-09b5-4e9a-8765-9b13944b1a03"
---

## Context

The cloud is intended as an always-available entry/worker, with up to five mesh
devices appearing and disappearing. The current recovered consumer is local;
that placement cannot create while its host is asleep.

## Outcome

The intended always-available host supervises one head identity and durable work.
Optional mesh makers can disappear without losing or duplicating accepted jobs.

## Scope

Services-owned deployment declarations, persistent contracts/output/database
storage, truthful model/embedding/voice health, bounded recovery and a placement
runbook. Reuse existing event/job replay and identity seams.

## Non-goals

Deploy a new mesh scheduler, migrate unrelated database users, change every PM2
process, or make cloud worker availability depend on an operator laptop service.

## Acceptance criteria

1. Document and verify one active gateway/clock owner; relocation disables the
   old owner persistently before enabling its successor.
2. Recreate the backend with a pending job. Within 60 seconds, the recovered
   owner must inspect persisted job identity and previous ownership. Re-admit
   pending work only after the previous owner is confirmed terminated or its
   ten-minute execution deadline expired; do not cancel a live owner. Preserve
   artifact identity. Reconcile any unknown publication outcome against the
   native outlet before retrying; if unresolved, mark the attempt ambiguous
   and require explicit retry rather than republishing. Observe three cycles.
3. Each creative execution has a ten-minute deadline and at most three total
   attempts per cycle identity. Failed attempts wait one minute, then five
   minutes before the remaining attempts; no immediate retry loop. Exhaustion
   yields a persisted failed outcome and a meaningful alert, then waits for the
   next scheduled cycle or an explicit operator retry. Inject unavailable
   provider and disappearing maker cases and inspect those outcomes. The head
   remains reachable throughout; healthy-provider response timing is measured
   separately from maker availability.
4. Runtime health measures successful creative cycles and usable dependencies,
   not merely an HTTP process. Alerts are quiet while unchanged/non-actionable.
5. Cloud placement has independently available provider, memory/search and
   artifact dependencies; state the measured host availability and remaining
   mesh capabilities rather than claiming a mesh from network reachability.

## Verification

Source/build gates in each owning repo, deployment identity checks, actual
database vector query and artifact/publication recovery drill. Ship the human
runbook and scripts with exact images/contracts and a reversible cutover.

## Risks

Host sleep, cloud capacity, worker leases, duplicate consumers, Atlas search
readiness, private credential placement and data migration consistency.
