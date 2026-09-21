---
category: "eta-mu-breakdown"
labels: ["eta-mu-breakdown", "migration", "git", "3sp"]
story_id: "E1.04"
write-id: "1789857174630-0.y0oa1srtuce48cu2jii"
points: "3"
title: "E1.04 — Each product has an independently addressable Git history and development branch"
blocked_by: ["e1-03-bootstrap-dependency-resolution"]
priority: "P1"
status: "blocked"
epic: "operation-eta-mu-breakdown"
uuid: "e1-04-independent-git-history"
created_at: "2026-09-13T00:00:00Z"
---

# E1.04 — Independent Git history

**Acceptance.** Every receiver has a verified open-hax remote, appropriate public visibility, a valid main branch, and a pushed `device/yoga` branch. Axxium retains its existing history. Public package names and CLI names remain stable.

## Tasks

- For genuinely new repositories: initialize clean receiver with main, commit with donor provenance, create remote, push, then create/push `device/yoga`
- For existing repos: use normal branch/import PR based on their history
- Record imported commit, remote repository ID, origin URL, and donor relationship
- Apply initial repository policy before autonomous merging is possible

## Failing fixtures first

- Re-running initialization must not create duplicates, change origin silently, lose history, or push directly to existing protected main
- A local directory name alone cannot establish destination identity

## Evidence

Remote read-back of repository identity, visibility, branch tips, published commit, and reconciled history.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.04).

---
PR #96 reconciliation: Independent remotes are cloneable. Full preflight/history/policy acceptance, including Axxium reconciliation and prerequisite E1.03, remains incomplete.
---