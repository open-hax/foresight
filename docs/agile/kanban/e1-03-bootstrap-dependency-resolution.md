---
uuid: "e1-03-bootstrap-dependency-resolution"
title: "E1.03 — Each receiver can resolve its bootstrap dependencies from immutable donor revisions"
status: done
priority: P1
labels: ["eta-mu-breakdown", "migration", "dependencies", "8sp"]
created_at: "2026-09-13T00:00:00Z"
category: eta-mu-breakdown
points: 8
epic: "operation-eta-mu-breakdown"
story_id: "E1.03"
blocked_by: ["e1-02-faithful-clean-copies"]
---

# E1.03 — Bootstrap dependency resolution

**Acceptance.** Every necessary donor dependency is explicit, pinned, and available outside the donor workspace. JS dependencies, CLJ/CLJS source, Maven libraries, build tooling, contract data, and runtime configuration are all accounted for. A clean install/build fails rather than silently falling back to neighboring copies. Every receiver command is exercised individually.

## Tasks

- Replace receiver `workspace:*`, parent paths, and local roots with appropriate immutable selectors
- Use subdirectory-aware Git coordinates, not eta-mu's root package
- Run the small Git-subdirectory canary on the pinned package manager first
- Add portable dependency metadata only in receiver trees
- Where an unchanged donor lacks consumable metadata (notably Protocols), use an explicit receiver-owned bootstrap adapter
- Validate actual packed/installed files and launchers
- Test Clio as an installed CLI and library, Chat UI as consumed by Rheos, and compiler compatibility

## Failing fixtures first

- Run a consumer with no donor directory, no global tool/classpath, and no old dist
- A dist-only Protocols package must not count as satisfying its source consumer
- A required Git input that is unavailable must fail with its exact coordinate

## Evidence

Per-package cold install, resolved dependency basis/lock, packed file inventory, build/assembly/test/lint logs, test counts, and reproducible failure notes.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.03).
