---
category: "kanban"
labels: "contracts, muse, preservation, 2sp"
type: "story"
story_id: "LOC.01"
points: "2"
title: "LOC.01 — Name, hash and freeze contract-0"
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-contract-0-freeze"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.01 — Name, hash and freeze contract-0

> Design: `docs/notes/design/layered-operating-contracts.md` §Contract-0 preservation, open question 0
> Research: `docs/research/layered-operating-contracts-inventory.md` §4 "Contract-0 is ambiguous"
> Target repository: muse (the tree chosen in open question 0)
> Blocked by: open question 0

## Context

The two muse trees carry diverged `.ημ/config` and `.ημ/plugins`:

- stable `~/spaces/muse` (`riatzukiza/muse`, `a752826`), which the running
  daemon builds;
- dev `~/spaces/foresight/muse` (`octave-commons/muse`, `b4bdb0a`).

Contract-0 cannot be preserved until it has a name and an identity.

## Outcome

`:muse/contract-0` version 1 is a named module whose content hash covers the
`.ημ/config/**` and `.ημ/plugins/**` files of the chosen tree. Its manifest
lists every file and the hash of each.

## Scope

- [ ] Record the open-question-0 decision (which tree; the reconciliation path
  for the other) in the design note.
- [ ] Add `.ημ/contract-0.edn` to the chosen tree, containing `:contract/id`,
  `:contract/version`, the file list and per-file SHA-256 values. A
  deterministic script generates it.
- [ ] Bind version 1 to recoverable source bytes. Record the full 40-hex
  commit of the chosen tree and the tree path in `contract-0.edn`, and push an
  immutable tag `contract-0/v1` at that commit to the tree's remote. Each later
  version gets its own tag, so earlier versions stay selectable and can be
  reconstructed.
- [ ] Record the divergence of the other tree, file by file, as a follow-up
  input. This story does not reconcile it.

## Non-goals

- Reconciling stable and dev muse.
- Changing any contract-0 behaviour.

## Acceptance criteria

- [ ] GIVEN the chosen muse tree WHEN the manifest script runs twice THEN both
  runs produce byte-identical `contract-0.edn`.
- [ ] GIVEN any edit to a file under `.ημ/config` or `.ημ/plugins` WHEN the
  script runs THEN the content hash changes and the edited path is reported.
- [ ] GIVEN a fresh clone of the remote WHEN `contract-0/v1` is checked out
  THEN the manifest script reproduces exactly the recorded per-file hashes, so
  version 1 is recoverable from the tag alone.
- [ ] GIVEN the design note WHEN it is read THEN open question 0 is marked
  resolved with the date and the owner decision.

## Verification

```bash
# in the chosen muse tree
nbb scripts/contract_0_manifest.cljs > /tmp/a.edn
nbb scripts/contract_0_manifest.cljs > /tmp/b.edn
cmp /tmp/a.edn /tmp/b.edn
```

## Risks

Freezing the wrong tree. Mitigation: the owner decides open question 0
explicitly, and the divergence report keeps the other tree recoverable.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
