---
category: "eta-mu-breakdown"
labels: ["eta-mu-breakdown", "migration", "closure-proof", "8sp"]
story_id: "E1.01"
write-id: "1789857168040-0.20aikbh9rg3i7r14spk"
points: "8"
title: "E1.01 — Maintainer can inspect the exact migration surface before any write"
priority: "P0"
status: "in_progress"
epic: "operation-eta-mu-breakdown"
uuid: "e1-01-migration-surface-manifest"
created_at: "2026-09-13T00:00:00Z"
---

# E1.01 — Migration surface manifest

**Acceptance.** A machine-readable manifest identifies each donor commit/tree/path, destination, current local checkout state, and existing remote history. It records both direct and transitive dependency mechanisms and classifies every edge as observed, reported, inferred, or unresolved. Current main, Foresight gitlinks, and local working state are distinct. All affected remaining packages, shared scripts, contracts, and workflows have an owner and disposition.

## Tasks

- Read tracked package manifests and workspace configuration
- Parse `deps.edn`, `shadow-cljs.edn`, `nbb.edn`, imports/requires, include paths, lint configs, scripts, Docker/Compose files, workflow matrices, exports and resource roots
- Resolve source namespace ownership rather than using name frequency as dependency proof
- Detect strongly connected components by edge type
- Check all proposed remotes; compare existing Axxium and donor content without discarding newer work
- Count tracked source bytes/lines only if a code-share claim is needed

## Failing fixtures first

- A sibling-only CLJS import invisible to package.json must appear in the graph
- A runtime URL must not become a compile edge
- An existing repo or a dirty destination must prevent creation/overwrite
- An unavailable read must not become "no dependencies"

## Evidence

Exact input revisions, scan coverage, source locations for edges, unresolved references, and destination preflight. The observation graph in the planning pack is the seed, not the exhaustive closure proof.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.01).

---
Closure manifest written to docs/migrations/eta-mu-breakdown/closure-manifest.md. All 8 extraction targets scanned. 6 new repos needed (only axxium exists). Critical path: chat-ui → clio → protocols → rheos. Sol↔eta-mu cross-mechanism cycle preserved via immutable donor snapshots.

PR #96 reconciliation: The September 13 card said done but the canonical status stream stops at in_progress. Reopening the observed card state through lawful CLI transitions now, not backfilling historical completion. The planning snapshot is bounded, not an exhaustive donor closure proof. Seven new repositories (eight targets minus existing Axxium) corrects the earlier six-repository claim; Receipt River was added later as a ninth registration.
---