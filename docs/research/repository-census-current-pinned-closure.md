# Repository census: current pinned closure

## Status

This is an orientation-grade structural census over four pinned repository roots. It is a generated evidence projection, not a lifecycle, ownership, continuation, consolidation, or retirement decision.

The current structural frontier is closed for the reachable Gitlink graph represented by these revisions. The larger historical and documentary census remains open.

## Pinned roots

| Repository | Revision |
| --- | --- |
| `riatzukiza/devel` | `80a95e5638f4ee95e182ebf0a22f4735ab55964f` |
| `octave-commons/promethean` | `06a8b83312ea70dcde6d2e423369b410e6d0d3f2` |
| `open-hax/openplanner` | `8b425c1690ada78f3f1bc5dfa28c3151e3a4fede` |
| `open-hax/foresight` | `fcb30c0bbbf1b7558d465e479c0b1b34f3d275a5` |

## Method

1. Read each root's `.gitmodules` at the pinned revision.
2. Preserve every declaration as an occurrence with its parent repository, parent revision, path, raw URL, declared branch, and declaration line.
3. Resolve the path through the pinned Git tree and record the exact Gitlink commit when present.
4. Normalize supported GitHub remotes into repository names without treating the mount path as identity.
5. Recurse only through exact Gitlink commits.
6. Deduplicate traversal by `(repository name, commit SHA)`.
7. Retain local-only, unsupported, missing, and non-Gitlink declarations as explicit gaps.
8. Emit newline-delimited EDN projections for repositories, occurrences, and gaps plus a readable Markdown index.

## Result

| Measure | Count |
| --- | ---: |
| Canonical GitHub repository names observed | 951 |
| Distinct repository revisions inspected | 959 |
| Submodule declarations observed | 1,312 |
| Declarations resolved to pinned Gitlinks | 1,260 |
| Explicit gaps | 52 |
| Unprocessed frontier | 0 |
| Maximum observed occurrence depth | 3 |

The root and major nested declaration counts were:

| Parent | Revision scope | Declarations | Resolved | Gaps |
| --- | --- | ---: | ---: | ---: |
| `riatzukiza/devel` | one pinned revision | 504 | 477 | 27 |
| `octave-commons/promethean` | two revisions reached through root plus `devel` | 150 | 132 | 18 |
| `open-hax/openplanner` | two revisions reached through root plus `devel` | 5 | 3 | 2 |
| `open-hax/foresight` | one pinned revision | 13 | 13 | 0 |
| `mojomast/ussyverse` | one reached revision | 387 | 387 | 0 |
| `riatzukiza/ussyverse` | one reached revision | 232 | 232 | 0 |

Eight repository names were reached at two different revisions: `octave-commons/pantheon`, `octave-commons/promethean`, `open-hax/eta-mu`, `open-hax/knoxx`, `open-hax/openplanner`, `open-hax/proxx`, `open-hax/uxx`, and `open-hax/vexx`.

## Gap classes

- `submodule/local-only`: 5 occurrences.
- `submodule/path-unresolved`: 47 declarations present in `.gitmodules` but absent at the declared path in the pinned tree.

The local-only occurrences are:

- `devel` → `orgs/riatzukiza/desktop` via `./orgs/riatzukiza/desktop`.
- `devel` → `orgs/riatzukiza/book-of-shadows` via `./orgs/riatzukiza/book-of-shadows`.
- `devel` → `orgs/octave-commons/mythloom` via `file:///home/err/devel/orgs/octave-commons/mythloom`.
- Two observations of OpenPlanner's `packages/stores/migrations/openplanner-migration-tools` at different parent revisions, both via `file:///home/err/devel/orgs/open-hax/openplanner-migration-tools`.

The unresolved-path declarations are concentrated in:

- `riatzukiza/devel`: 24.
- `octave-commons/promethean`: the same 9 paths at each of two reached revisions, for 18 occurrence records.
- `riatzukiza/lunar`: 5.

These are structural observations only. A missing Gitlink may represent a stale manifest entry, an intentionally local checkout, a removed path, a migration in progress, or another condition that requires separate evidence.

## Artifact

GitHub Actions run `33281928707` produced artifact `repository-census-current` (`9723251515`).

Artifact digest:

```text
sha256:1ee776c337945aa46d7bfa1752b159a5b8e4a1f205a87fa9d6fe0feeb9047e7f
```

The artifact contains:

- `repositories.edn`
- `occurrences.edn`
- `gaps.edn`
- `index.md`
- `summary.json`

## Known exclusions

This pass does not yet establish the full historical repository union. It does not inspect prior `.gitmodules` versions, abandoned branches, symlink targets, uncommitted machine state, private repositories unavailable to the workflow token, repository references found only in documentation, or repositories visible through connected GitHub installations but absent from the current Gitlink closure.

It also does not infer authorship, ownership, product identity, lineage, lifecycle, importance, or whether any repository should be continued.
