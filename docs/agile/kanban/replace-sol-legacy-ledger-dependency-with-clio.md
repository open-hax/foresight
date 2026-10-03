---
uuid: "replace-sol-legacy-ledger-dependency-with-clio"
title: "Replace Sol legacy ledger dependency with Clio"
status: "incoming"
type: "epic"
priority: "P1"
points: "5"
labels: "sol, clio, review, runtime"
parent: "330aa63f-f697-5bd6-9fc0-3f19fbea2be4"
category: "kanban"
write-id: "1791024252404-0.nyr5se4osuu2zb2xd6"
created_at: "2026-10-03T10:44:12.404Z"
---

# Replace Sol's legacy ledger dependency with Clio

## Context

The user corrected Sol's canonical event authority on 2026-10-03: use Clio, not open-hax/event-ledger. Qualified riatzukiza/sol#2 merged as72fdecca292859d92cba48b686ec6fe9c43a9133 restores the Node22 baseline; it intentionally keeps the predecessor pin. This separate five-point prerequisite corrects that dependency and its actual adapters. The parent worker's compatibility report and real immutable-pin probe establish feasibility, not implemented Sol migration.

## Outcome

Sol uses open-hax/clio@788cdd3434615a7932b924e68520dbf7f88408c2 for canonical event envelopes and ledger operations, with no production predecessor imports after the final qualified layer.

## Scope

Break down into a three-point event-catalog/episode-adapter slice and a two-point session-ledger/final-cutover slice. Keep the existing execution lifecycle and Axxium/Katamorph authority. The intermediate slice may retain the predecessor solely for the unmigrated session adapter and must disclose that coexistence. The final layer removes it from dependency/build metadata and all production imports.

## Acceptance criteria

- Both children are reviewed and admitted through Rheos before their implementation.
- Actual Sol adapters execute append/readback/exact retry/reopen and reject collision, stream-gap, schema and failed-append causality cases through Clio.
- Legacy ledger bytes remain preserved and are never silently mixed with Clio records.
- The final frozen Node22 graph includes the required native host dependency, passes actual nonempty tests, zero-warning builds, production health and clean SIGTERM on hosted CI.
- Every observed finding is settled; review convergence follows the canonical PR skill pack. Records retain exact original and replacement pins and evidence.

## Non-goals

No hosted review-worker activation, deployment, service restart, Clio kernel copy, legacy history rewrite or changes to unrelated Git pins. Clio currently provides locked append, not fsync durability; durable job acknowledgment/outbox/recovery remains a separate upstream prerequisite.

## Verification

Use the existing Node22 runtime gates plus real filesystem-backed Sol-adapter tests. The standalone Clio probe uses exact Git788cdd3, npm ci with Node22.20.0/npm10.9.3, nativefs-ext-extra-prebuilt2.2.9, two compiled targets and1test/13assertions with zero warnings. Probe success does not replace the actual Sol integration suite.
