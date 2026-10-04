---
uuid: "8d16aa9f-fbad-4bbf-87d5-296196c99928"
title: "Cut Sol session ledgers over to Clio"
status: "incoming"
type: "task"
priority: "P1"
points: "2"
labels: "sol, clio, review, runtime"
parent: "replace-sol-legacy-ledger-dependency-with-clio"
category: "kanban"
write-id: "1791024565152-0.0ppv2d8jjtsrdhvk66p6"
created_at: "2026-10-03T10:49:25.152Z"
---

# Cut Sol session ledgers over to Clio

## Context

This two-point slice follows the episode/catalog child c0f3604a-9503-46d3-9e17-bc6a2c5ad649. Their dependency is proposed in this body: the installed Rheos frontmatter writer currently refuses a dependency key, so machine-admitted dependency metadata is an upstream gap and no ready transition is claimed.

## Outcome

Sol's advertised session-event API uses Clio-owned append/read admission and the final coherent dependency graph removes event-ledger and every production import.

## Scope

- Replace session_store's predecessor validation and whole-file rewrite with Clio ledger operations.
- Use an explicit new canonical partition; preserve legacy bytes and identify legacy records separately. Reject malformed/missing canonical partitions and never silently mix old envelopes with Clio events.
- Remove event-ledger from deps/build metadata and production imports after both adapters qualify; retain all unrelated immutable pins.
- Correct current AGENTS/README/design/build-evidence authority claims and append source-bound predecessor-to-Clio provenance. Preserve the 16 integration anchors and disabled hosting proposal.

## Acceptance criteria

- Real session append/read/reopen/retry and conflict fixtures use Clio; canonical reads refuse missing/corrupt partitions and legacy files stay byte-identical.
- Actual on-disk tests supplement the episode slice; mutable session/run state remains a projection, without expanding into stubbed run-store repair.
- No production import or final dependency graph references event-ledger. Historical receipts retain the old pin and evidence unchanged.
- Fresh frozen Node 22 and production-native installs, guarded nonempty tests, zero-warning builds, health and SIGTERM pass on hosted CI.
- Dependency admission is resolved through Rheos before implementation readiness; the canonical PR skill pack governs review and merge.

## Non-goals

No silent legacy migration, ledger kernel copy, provider/hosted-worker implementation, fsync durability claim, service activation, deployment or restart.

## Verification

Use the admitted episode slice as the prerequisite and the existing full Node 22 runtime CI plus real session-store filesystem tests. Preserve explicit 5-point epic completion as pending until both slices and the final cutover qualify.
