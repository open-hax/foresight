---
category: "kanban"
labels: ["eta-mu", "clio", "identity", "causality", "edn"]
type: "story"
title: "Make .ημ a relocatable UUID and causal graph substrate"
priority: "P1"
status: "incoming"
uuid: "caf2cefb-1c67-56ec-bec1-62621f45b62c"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Make .ημ a relocatable UUID and causal graph substrate

## Story

**As a** maintainer or agent  
**I want** durable EDN records under `.ημ/` to be identified and related by
UUID rather than filesystem path  
**so that** independently evolved packages can reconcile history without
rewriting it into one directory layout.

## Acceptance

- `.ημ/` is the repository-local home for durable EDN records governed by
  these shapes/laws.
- Semantic identity is a UUID, not a filename, directory, repository mount
  position, or ordinal.
- Causal relationships name UUIDs explicitly.
- Moving a record between files/directories does not change its semantic
  identity or graph position.
- Existing eta-mu and Muse `.ημ/` layouts remain admissible without
  normalization into one tree.
- A file is a storage container, not an identity boundary; owning shapes may
  permit one EDN value or append-only newline-delimited EDN.
- Duplicate UUID definitions fail closed.
- Missing causal references and cycles are detectable.
- Filesystem order is never causal order.
- Existing append-only facts are not rewritten merely to reconcile layout.
- A recursive/indexed projection can reconstruct UUID -> record and the causal
  DAG independently of directory structure.
- Clio remains authoritative for event/ledger mechanics rather than duplicating
  them in Foresight.

## Compatibility investigation

Characterize the valid structures already present in at least
`open-hax/eta-mu/.ημ` and `octave-commons/muse/.ημ`. Preserve useful human
and agent conventions, but promote only identity/causality invariants to law.
