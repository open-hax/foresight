---
uuid: "e1-02-faithful-clean-copies"
title: "E1.02 — Contributor receives faithful, clean copies without donor modification"
status: done
priority: P1
labels: ["eta-mu-breakdown", "migration", "extraction", "5sp"]
created_at: "2026-09-13T00:00:00Z"
category: eta-mu-breakdown
points: 5
epic: "operation-eta-mu-breakdown"
story_id: "E1.02"
blocked_by: ["e1-01-migration-surface-manifest"]
---

# E1.02 — Faithful clean copies

**Acceptance.** All eight receiver trees are copied from the chosen source revisions; donor paths remain byte-for-byte unchanged. A manifest records every imported file and intentional exclusion/addition. No `.git` metadata, credentials, runtime data, installed modules, build outputs or nested worktrees are unintentionally staged. Existing receiver trees remain intact.

## Tasks

- Prefer a tracked-tree export from a fixed commit over copying an arbitrary dirty working directory
- Export into a fresh destination
- Apply receiver-root ignore rules based on the donor `.gitignore`
- Copy relevant licenses and portable lint hooks where required
- Inspect `git status`, staged diff, file modes, symlinks, secret scan before first public push

## Failing fixtures first

- Plant a local `.env`, `node_modules`, `target`, and nested checkout; none may enter the published tree
- A tracked source directory named `build` must not be lost merely because its name resembles output
- A donor hash change aborts the copy acceptance

## Evidence

Before/after donor tree comparison, receiver file map, exclusion reasons, staged inspection and secret-scan result.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.02).
