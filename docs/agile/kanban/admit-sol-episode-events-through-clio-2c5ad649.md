---
uuid: "c0f3604a-9503-46d3-9e17-bc6a2c5ad649"
title: "Admit Sol episode events through Clio"
status: "incoming"
type: "task"
priority: "P1"
points: "3"
labels: "sol, clio, review, runtime"
parent: "replace-sol-legacy-ledger-dependency-with-clio"
category: "kanban"
write-id: "1791024443956-0.jze54j1ksbba5o3dfni"
created_at: "2026-10-03T10:47:23.956Z"
---

# Admit Sol episode events through Clio

## Context

The user directed Sol to consume Clio. The merged Node22 prerequisite at riatzukiza/sol#2 supplies the baseline. Exact Clio788cdd3434615a7932b924e68520dbf7f88408c2 was tested through an anonymous immutable Gitlibs install and real filesystem operations in both CommonJS and ESM. This three-point slice replaces episode event admission, not the still-unmigrated session store.

Proposed dependency, not engine-admitted: this card depends on canonical Node22 prerequisite `146f1b47-c6a7-5a37-996b-a381dd91f6b6`, whose code baseline is Sol #2 merge `72fdecca292859d92cba48b686ec6fe9c43a9133`. Before readiness, admit and read back that relationship through Rheos. The PR URL and this body text do not substitute for a validated engine edge. Canonical relationship authoring is tracked in open-hax/rheos#3.

## Outcome

Sol's episode event shape/catalog and appender consume Clio's complete event/stream/schema law and actual ledger I/O. A configured legacy Mongo capability cannot silently become a successful no-op.

## Scope

- Add the exact Clio source and frozen native host dependencyfs-ext-extra-prebuilt2.2.9; keep other Git pins.
- Define Sol-owned event-data catalog/correlation mapping for Clio envelopes. Preserve principal/contract references using Axxium/Katamorph ownership, without duplicating their laws.
- Replace episode_event and episode_ledger predecessor calls with an explicit configured Clio capability. Prepare stable event identity before append, retry the same record, and advance causality only after accepted append.
- Preserve existing blocked-start, executor-error and terminal persistence-failure behavior.
- Retain and disclose event-ledger only for the unmigrated session adapter until the final child qualifies.

## Acceptance criteria

- Append-admission fixtures demonstrate UUID/schema/basic event-identity validation, exact duplicate retry, ID/stream-slot conflict and missing partition errors through Clio.
- Complete-history canonicalization fixtures demonstrate missing parents, stream gaps and predecessor-causality failures. Missing causes may belong to another physical partition; append alone does not establish complete-history validity. Reopen/continuation consumes validated canonical history without copying upstream laws into Sol.
- Real Sol-adapter tests perform on-disk append/readback/exact retry/reopen and failed-append causality checks. Callback-only mocks do not qualify.
- All actual Node22 runtime tests/builds/native production import/health/SIGTERM checks pass locally and on CI with zero compiler warnings.
- No production episode import uses event-ledger, while the intermediate session dependency is accurately disclosed.
- Historical ledger/receipt bytes stay preserved; current pin/source catalog claims match actual dependency metadata.

## Non-goals

No Clio kernel copy, fsync claim, new hosted worker, deployment, restart, authentication authority or session migration in this first slice.

## Verification

Use Node22.20.0/npm10.9.3, frozen npm ci, Sol's guarded nonempty test suite, actual Clio adapter operations, lint, zero-warning test/server builds, production health and SIGTERM. Follow the canonical PR skill pack for review convergence; this card is incoming and does not authorize an unreviewed ready transition.
