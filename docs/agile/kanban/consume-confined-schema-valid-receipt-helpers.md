---
uuid: "4707cf30-ad07-49e7-b5e7-88bd7687ee25"
title: "Consume confined, schema-valid receipt helpers for isolated worktrees"
type: "task"
status: "incoming"
priority: "P1"
points: "3"
labels: "receipts, provenance, isolation, workflow"
parent: "0d73c22a-b018-4b8c-92fe-560127f53294"
---

# Consume confined, schema-valid receipt helpers for isolated worktrees

## Context

The October 6 sweep observed two separate helper defects in independently owned
`riatzukiza/.agents`. [Issue #19](https://github.com/riatzukiza/.agents/issues/19)
tracks newly emitted scalar manifest/reference values that failed the consumer
receipt contract. [Issue #20](https://github.com/riatzukiza/.agents/issues/20)
tracks root discovery skipping a linked worktree's `.git` file and selecting an
ancestor `.ημ` directory.

The second defect caused an actual receipt write outside its owning Axxium PR
checkout, at `/home/err/.codex/.ημ/receipts.edn`. The observed entry identifies
`child-prs-20261006/axxium-pr26` and timestamp
`2026-10-06T13:34:50.023938463Z`; its SHA-256 excluding the newline is
`90706ba68f2138a63e92d3cc92ee72eaa368a21e9913143d4c3a4e5da29adb18`.
The misplaced file is preserved. No pre-call ancestor snapshot exists, so prior
emptiness or preservation of a pre-existing prefix is not claimed. No service,
database, source, or model-execution effect was observed from this receipt write.

## Outcome

Each parallel lane writes schema-valid receipts and reflections only within its
own explicit project boundary through qualified canonical helper revisions.
Malformed unpublished preparation records remain recoverable without rewriting
historical records or publishing invalid ledger additions.

## Scope

- Consume the reviewed upstream fixes for both envelope validity and project-root
  confinement, including the matching Session Mycology discovery boundary.
- Keep the root's `.agents` consolidation checkout inventory-only; repair belongs
  to an independently isolated upstream checkout and PR.
- Verify ordinary repositories, genuine linked worktrees, nested working
  directories, local `.ημ` roots, and ancestor metadata using disposable fixtures.
- Retain exact raw faulty records and hashes as recovery evidence; use valid new
  append-only records for publication, without modifying existing ledger lines.
- Bind any Foresight qualification claim to the exact upstream helper revision
  and consumer contract used by the test.

## Non-goals

No local replacement helper, provenance parser, global skill installation,
ancestor-ledger cleanup, historical rewrite, schema weakening, or broad daemon
and shared-cache mutation. Manual incoming Markdown supplies no board admission.

## Acceptance criteria

- Linked-worktree and nested-directory calls select the owning root even when
  an ancestor has `.ημ`; invalid Git boundaries fail visibly instead of escaping.
- Receipt and reflection helper fixture calls preserve ancestor bytes exactly
  and create no files outside the disposable owning fixture root.
- Required receipt fields and collection shapes pass the actual consumer
  contract; invalid arguments fail before appending any bytes.
- Existing owning-ledger bytes are unchanged; successful calls append only valid
  new records, and failures leave the owning ledger unchanged.
- The misplaced observed record remains inspectable, with uncertainty about its
  prior file state retained explicitly.

## Verification

Current evidence is the actual misplaced record, its raw-entry hash, the clean
global helper source at `0e1101c8d76cca92d6a4321f0e880624bf5e0d14`, and the linked
upstream issues. Upstream planning review and lawful readiness precede repair.
Future qualification must exercise real temporary Git worktrees and the consumer
schema rather than assert success from helper exit status alone.

## Risks

An isolated checkout does not automatically confine upward root discovery.
Receipt River and Session Mycology share this mechanism; both must be inspected
before declaring write confinement. This card records the observed cross-lane
failure without attributing unrelated shared state to it.
