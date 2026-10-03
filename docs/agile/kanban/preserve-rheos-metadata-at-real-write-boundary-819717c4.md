---
category: "content"
labels: "rheos, markdown, preservation, write-boundary, regression"
parent: "e7cdf5cf-423e-49fc-aca8-6ed6055f1cd5"
type: "story"
write-id: "1791012105115-0.6hl8ydsba8hf01j3083"
points: "5"
title: "Preserve unrelated Rheos metadata and body through real writes"
priority: "P1"
status: "review"
epic: "e7cdf5cf-423e-49fc-aca8-6ed6055f1cd5"
uuid: "819717c4-735b-4dd5-b5b7-ba860d3942f9"
---

# Preserve unrelated Rheos metadata and body through real writes

## Context

(己, p=0.99) The source probe documented in the
[content-loop brief](../../notes/rheos-markdown-content-loop.md) at Rheos
`11811264a308d406cb612aefa1dad40818675e5e` loses nested/list/multiline
frontmatter and changes fenced-body spacing when adding a priority field.
That probe exercises a pure helper, not an installed CLI, browser or durable
write. Reviewed editing depends on source preservation before it can safely
build on existing writeback.

## Outcome

A maintainer can perform a targeted frontmatter edit through the existing
Rheos writeback function without losing unrelated metadata or body text, and
can independently reread the written fixture and its task metadata.

## Scope

- Implementation owner: `open-hax/rheos`; inspect its current main and applicable
  instructions before choosing the minimal repair. Donor eta-mu compatibility
  is consumed only where its own ownership/consumer contract requires it.
- Repair the shared existing writeback path for a permitted frontmatter edit,
  preserving unrelated source bytes, including nested/unknown YAML fields,
  multiline values, quoting, comments, newline style and fenced delimiters.
- Reuse the canonical parser for task-store metadata reads so preserved valid
  YAML remains readable after reload; keep engine-owned write metadata explicit.
- Repair only asynchronous handling needed to exercise the existing exported
  writeback function in compiled tests.
- Verify a real temporary-file write and independent reread through that
  function, plus invalid-input and no-op behavior at the changed boundaries.

## Non-goals

- No new general-document editor, proposal store or read/discuss/accept UI.
- No production API provider or document-admission runtime integration.
- No root parser/serializer, alternate board mutation protocol or changed FSM.
- No universal claim that CLI/API/MCP/UI, watchers or deployed services work
  because one write path passes.
- General comment serialization, installed transport qualification, durable
  ledger recording and write/event partial-failure recovery are separate work.

## Acceptance criteria

- A fixture with explicit UUID, nested/list/multiline and unknown frontmatter,
  comments, quoting and fenced `---` content demonstrates the current
  preservation failure before the repair; the recorded failure is attributable
  to this behavior rather than missing tools or setup.
- A permitted single-field edit changes only its intended source region and
  explicitly identified Rheos-owned write metadata. Unrelated metadata and body
  bytes remain unchanged, and task UUID remains canonical.
- The canonical metadata reader sees the edited status/fields, including inline
  comments, without a second YAML interpretation in the store.
- The exported writeback function writes an actual fixture file without a
  mocked persistence layer. An independent filesystem reread sees the intended
  content and preserved source; that evidence is not called an installed CLI,
  HTTP, MCP, browser or ledger test.
- Invalid YAML, duplicate keys or unsupported updates are refused before the
  changed write boundary; no-op helper calls preserve their input.
- Relevant compiled parser/writeback tests and lint pass at the reviewed
  revision. Full-suite dependency/tooling failures remain visible and block
  qualification of the broader repository gate.
- Foresight references the reviewed upstream result instead of duplicating the
  implementation; independent review disposition is linked before completion.

## Verification

Use the historical probe as reproduction context, then add a failing fixture
beside Rheos's actual source/write boundary. Record before/after source diffs,
the exact exported-function test/configuration, filesystem reread, semantic
metadata reread and negative/no-op results. Run the owning
repository's required checks and distinguish helper, entry-point, persistence
and transport evidence. Attach exact revisions and review results to the Rheos
PR and the Foresight coordination record.

## Risks

Parsing and serialization may silently normalize source beyond the selected
field. Shared helpers can affect status/comment behavior; general comment
serialization remains outside this repair. File writes and event recording
remain separate failure boundaries requiring later qualification. If the repair
requires a broader authoring model or exceeds five points, split that additional
work rather than expanding this card into the whole content loop.