---
category: "eta-mu-breakdown"
labels: ["eta-mu-breakdown", "migration", "foresight", "submodule", "5sp"]
story_id: "E1.05"
write-id: "1789857175855-0.lni0g4ri9ci88vict6o"
points: "5"
title: "E1.05 — Foresight discovers each new product as a pinned, actionable child"
blocked_by: ["e1-04-independent-git-history"]
priority: "P1"
status: "in_progress"
epic: "operation-eta-mu-breakdown"
uuid: "e1-05-foresight-discovers-children"
created_at: "2026-09-13T00:00:00Z"
---

# E1.05 — Foresight discovers new children

**Acceptance.** `.gitmodules` and the index contain matching path/URL and gitlink entries. Each gitlink is remotely reachable and points to a tested commit. Fresh Foresight checkout obtains exactly the recorded commits without `update --remote`.

## Tasks

- Register existing local receiver repositories through Git's submodule operation
- Reconcile paths before adding; absorb Git directories as appropriate
- Update `src/foresight/project.cljc`, its law/tests, the workspace inventory, and `config/quality-gates.edn`
- Inspect `scripts/workspace.clj`, `scripts/evidence.clj`, and the public README command surface
- Supply package-owned root commands or explicit workflow-only gates for products without root npm scripts

## Failing fixtures first

- A `.gitmodules` stanza without a gitlink, an unreachable commit, a renamed mismatched remote, or a project registry omission prevents admission
- A floating branch advancement cannot silently change the recorded workspace

## Evidence

Clean submodule clone, identity checks, project-law validation, direct-child inventory and exact-revision evidence catalog.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.05).

---
PR #96 reconciliation: PR 96 registers nine children. This pass corrects routing, all nine gate entries, and fixed 24-entry inventory expectations. Registration and locally passing root tests do not prove standalone module acceptance; exact child evidence remains pending.
---