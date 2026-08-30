# Repository census: current pinned closure

## Status

This is an orientation-grade structural census over four pinned repository roots. It is a generated evidence projection, not a lifecycle, ownership, continuation, consolidation, or retirement decision.

The reachable graph is structurally stable but not fully inspectable with the
workflow's current authority. The current frontier contains 47 exact
repository/revision observations that still return HTTP 404 after bounded
retry. Those identities are checked in and compared exactly; they are not
treated as absent manifests or a count-only exception. The larger historical
and documentary census also remains open.

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
8. Mark traversal-blocking gaps explicitly and emit a canonical `frontier.json`
   projection beside the EDN and Markdown evidence.
9. Retry transport, rate-limit, and server failures a bounded number of times.
10. Pass the hosted gate only when the complete observed frontier exactly
    matches `repository-census-known-frontier.json`; any changed, added, or
    missing identity fails closed.

## Result

| Measure | Count |
| --- | ---: |
| Canonical GitHub repository names observed | 951 |
| Distinct repository revisions inspected | 912 |
| Submodule declarations observed | 1,312 |
| Declarations resolved to pinned Gitlinks | 1,260 |
| Explicit gaps | 99 |
| Unprocessed frontier | 47 |
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

- `manifest/unavailable`: 47 exact repository/revision observations.
- `submodule/local-only`: 3 occurrences.
- `submodule/path-unresolved`: 47 declarations present in `.gitmodules` but absent at the declared path in the pinned tree.
- `submodule/unsupported-url`: 2 repository-local `./` declarations.

The local-only and unsupported occurrences are:

- `devel` → `orgs/riatzukiza/desktop` via unsupported repository-local URL `./orgs/riatzukiza/desktop`.
- `devel` → `orgs/riatzukiza/book-of-shadows` via unsupported repository-local URL `./orgs/riatzukiza/book-of-shadows`.
- `devel` → `orgs/octave-commons/mythloom` via `file:///home/err/devel/orgs/octave-commons/mythloom`.
- Two observations of OpenPlanner's `packages/stores/migrations/openplanner-migration-tools` at different parent revisions, both via `file:///home/err/devel/orgs/open-hax/openplanner-migration-tools`.

The unresolved-path declarations are concentrated in:

- `riatzukiza/devel`: 24.
- `octave-commons/promethean`: the same 9 paths at each of two reached revisions, for 18 occurrence records.
- `riatzukiza/lunar`: 5.

These are structural observations only. A missing Gitlink may represent a stale manifest entry, an intentionally local checkout, a removed path, a migration in progress, or another condition that requires separate evidence.

The 47 unavailable identities are recorded in
`repository-census-known-frontier.json` and tracked for recovery or named
adjudication in Foresight issue #64. A 404 baseline entry does not establish
whether a repository is private, renamed, deleted, or contains a stale commit;
it records only the exact inaccessible observation. HTTP 500/504 responses from
the preceding run were retried and are absent from the successful baseline.

## Artifact

GitHub Actions run `33285093017` at exact head
`5cc7d1310027695571df4929b80a1a482c7978ad` produced artifact
`repository-census-current` (`9724221796`).

Artifact digest:

```text
sha256:ba97812f4d8b4cc400ab11080351ecfe26d3183afd60473f7cdab78051208ffb
```

The artifact contains:

- `repositories.edn`
- `occurrences.edn`
- `gaps.edn`
- `frontier.json`
- `index.md`
- `summary.json`

## Known exclusions

This pass does not yet establish the full historical repository union. It does not inspect prior `.gitmodules` versions, abandoned branches, symlink targets, uncommitted machine state, repository references found only in documentation, or repositories visible through connected GitHub installations but absent from the current Gitlink closure. Repositories or exact revisions unavailable to the workflow token are included as explicit frontier observations rather than exclusions.

It also does not infer authorship, ownership, product identity, lineage, lifecycle, importance, or whether any repository should be continued.
