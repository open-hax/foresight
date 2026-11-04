# Fork Tax Handoff

**Timestamp:** 2026-11-04T13:00:53Z
**Repository:** `open-hax/foresight`
**Branch:** `plan/fork-dev-origins`
**Snapshot parent head:** `b9ac3141a9799a06abb2bd0d2c8320e8b8b9ec02`
**Snapshot commit:** `3a0ed32b4634a97cfd596d4a1af6e5993db913cb`
**Tag:** `Π/foresight/20261104T130053Z`
**Previous Π tag:** `Π/foresight/20261103T150056Z`
**Receipt:** `599daaa3-7e54-459b-b304-a24feb9deb94`
**Note:** the tag points at the payload commit (`3a0ed32b`). This state file, the manifest checksums, the final receipt row and the push are finalized in the immediately following bookkeeping commit.

## Signal

- **Oikos gitlink absorbed**: the root pointer advanced `ae350737` → `fed4e302`, which equals both `refs/heads/main` and `refs/remotes/origin/main` in the child repository, with a clean child worktree. The pin follows the reviewed merge `open-hax/oikos#3` (bounded prospective history before native ledger append).
- **Two previously unpushed commits published**: `23424720` (Oikos local-first workbench plan, private NBB submodule registration, 24 incoming cards) and `b9ac3141` (reviewed Oikos bootstrap pin plus the retained receipt-gate blocker) had never reached `origin/plan/fork-dev-origins`. This snapshot publishes them.
- **Ledgers**: appended the fork-tax receipt and the turn reflection to the append-only `.ημ` ledgers.
- **Manifest rule made explicit**: the previous manifest's generation rule was undocumented, so this snapshot records its rule in the file header — every path changed in `Π/foresight/20261103T150056Z..HEAD`, excluding the manifest itself, hashed at the snapshot commit (38 entries).

## Verification (run, not assumed)

- PASS `nbb scripts/project.clj validate` — 26 workspace sources, 25 submodules, 40 invariants, 1 lineage source, 6 provisional claims.
- PASS `nbb test/project_test.cljs` — 12 tests, 171 assertions, 0 failures, 0 errors.
- PASS `nbb -cp scripts:test test/workspace_test.cljs` — 24 tests, 122 assertions, 0 failures, 0 errors.
- PASS `clj-kondo --lint scripts test` — 0 errors, 0 warnings.
- PASS `git diff --check` — clean.
- VISIBLE (not a pass): workspace tests log `ERROR opencode lint spawn ENOENT` and `ERROR jscpd spawn ENOENT` — pre-existing host tooling gap inside an otherwise green suite.
- BLOCKED (pre-existing, reproduced at base): `nbb scripts/evidence.clj verify-receipts` exits 2 with `Git invocation failed: spawnSync git ENOBUFS` on candidate `b9ac3141` **and** on unchanged base `e31b43a5`. Reported, not waived, not worked around, not locally reimplemented.

## Submodule Truth

Only the gitlink whose new commit is already contained in a pushed remote branch was absorbed. Every other child pointer is unchanged from the previous snapshot.

| Submodule | Branch | Head | Proof |
| --- | --- | --- | --- |
| `oikos` | `main` | `fed4e30272cc` | equals `refs/heads/main` and `refs/remotes/origin/main`, clean child worktree |

All other gitlinks (`Truth`, `axxium`, `bitch-tracker`, `calliope`, `chat-ui`, `clio`, `epiphany`, `eta-mu`, `kanban-orchestrator`, `katamorph`, `knoxx`, `muse`, `opencode`, `openplanner`, `osmos`, `proxx`, `receipt-river`, `rheos`, `services`, `session-mycology`, `shx`, `sol`, `uxx`, `.agents`) are byte-identical to the previous snapshot.

## Excluded / Concurrent Dirt (left untouched on purpose)

- `.github/workflows/agy-review.yml`, `scripts/agy-review.sh` — verified byte-identical to `origin/feat/agy-gemini-3-8-flash-review-workflow` (foresight#152). Left untracked rather than re-absorbed into this snapshot.
- `.env` — holds a live `GH_TOKEN`. Never staged; ignored.
- `.ημ/diagnostics` — bulk CI/session evidence, ~733 MiB across ~18,100 files, ignored.
- `calliope` (4 modified tracked files plus untracked artifacts), `openplanner` (modified `pnpm-lock.yaml`), `uxx` (4 modified tracked files plus untracked artifacts) — live child-repository work owned by those repositories.
- No `git reset`, `git restore`, `git clean`, checkout rewind or `git add -A` was used anywhere in this snapshot.
