---
status: incubating
created: 2026-11-03T12:05:00Z
source-session: fork-tax-plan-fork-dev-origins
source-task: Fork-tax snapshot of a shared workspace with 18k untracked files and a live secret
p-efficiency: 0.85
p-friction: 0.35
p-skill-candidate: 0.62
promoted-to: ""
rejected-reason: ""
---

## Problem

A "pay the fork tax" request arrived on a shared checkout whose dirt was 18,297 entries across 52 tracked changes: one live credential in `.env`, ~733 MiB of runtime diagnostics, an undeclared 184 MiB nested clone, and 23 submodule markers of which only 8 were real gitlink moves. Blind `git add -A` would have committed a token, a quarter-terabyte of CI logs, and someone else's in-flight branch content.

## Pattern

Distinguish four classes of dirt before staging anything:

1. **Owned and unpreserved** - commit it (root docs, new scripts, append-only ledgers).
2. **Owned but secret** - ignore it and say so (`/.env`, `/.env.*`, `!/.env.example`).
3. **Bulk runtime evidence** - ignore it, but publish an inventory (dir, file count, MiB) in the handoff state so "not in Git" never becomes "not accounted for".
4. **Someone else's preserved work** - compare byte-for-byte against the pushed branch (`git show FETCH_HEAD:path | sha256sum` vs `sha256sum path`); identical means commit nothing, and say which branch holds it.

For gitlinks, a `git status` marker is not a pointer move. Compare `git rev-parse HEAD:<sub>` with the child's `HEAD`, then prove preservation against the *remote*, not stale tracking refs: `git -C child fetch origin <branch>`, then `git -C child merge-base --is-ancestor <head> FETCH_HEAD`. Absorb only ancestors-or-equal branches; record the rest.

## Candidate skill outline

- Trigger: fork tax / Π / full dump in a multi-agent or submodule workspace.
- Step: enumerate dirt by class before staging; never `git add -A`.
- Step: `git rev-parse HEAD:<sub>` != child `HEAD` is the only real gitlink test.
- Step: prove the child commit is on the remote with fetch + `merge-base --is-ancestor`.
- Step: hash-compare duplicate artifacts against the remote branch before re-committing.
- Anti-pattern: treating a huge untracked tree as "must commit" - Git is not a 733 MiB evidence store.
- Anti-pattern: relying on remote-tracking refs that may be months stale.
- Anti-pattern: silently adding an undeclared nested repo as a gitlink; it grants ownership without the manifest + project + table update.

## Better path

Stage explicit paths, generate the manifest from `git diff --cached --name-only` so it cannot drift from the commit, tag the payload commit (bookkeeping commits after it, never amend), and record the push range plus receipt id in the state file only after the push actually returns success.

## Receipt refs

- a74f3b73-c340-43ae-b176-6637b0e4f25c
