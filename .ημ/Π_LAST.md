# Fork Tax Handoff

**Timestamp:** 2026-11-03T15:00:56Z
**Repository:** `open-hax/foresight`
**Branch:** `plan/fork-dev-origins`
**Snapshot parent head:** `cf9dfb091f04a60c07c6f05928d19736c4ba0b51`
**Snapshot commit:** `89c7e2402ff3cf4766ec6d262d8544d65147be15`
**Tag:** `Π/foresight/20261103T150056Z`
**Receipt:** `d9495e19-f3d4-4770-9075-e1363f082813`
**Note:** the tag points at the payload commit (`89c7e240`). This state file, the manifest sha256 checksums, and the final receipt row are finalized in the immediately following bookkeeping commit.

## Signal

- **Session Mycology EDN Migration**: Audited Foresight's session-mycology skill, the global skill, and the submodule. Migrated the legacy `ledger.md` (283 historical records + 1 new turn) to canonical newline-delimited EDN in `.ημ/session-mycology/ledger.edn`. Parsed all 283 records cleanly with exact float $p$-scores, vector receipt refs, nullable spores, and preserved 6 historical multi-line markdown annexes under `:raw-annex`.
- **Helper Scripts**: Added `sm-migrate.bb` to `.agents/skills/session-mycology/scripts/` (and mirrored to `~/.agents/skills/session-mycology/scripts/`). Updated `common.bb`, `sm-log.bb`, `sm-list.bb`, `CONTRACT.edn`, `SKILL.md`, and `AGENTS.md`.
- **Submodule Gitlink (`.agents`)**: Committed and pushed `.agents` changes on remote branch `riatzukiza/.agents@feat/session-mycology-edn-ledger` (`a6f6afe1d3b6845fa0eea43864c907bb6d193de5`), advancing the root `.agents` gitlink to that verified commit.
- **Append-only Ledgers**: Committed updated `.ημ/session-mycology/ledger.edn` and `.ημ/receipts.edn`.
- **Manifest Updates**: Updated `.ημ/Π_STATE.sexp`, `.ημ/Π_LAST.md`, and `.ημ/Π_MANIFEST.sha256`.

## Verification (run, not assumed)

- PASS `nbb scripts/project.clj validate` — 25 workspace sources, 24 submodules, 40 invariants, 1 lineage source, 6 provisional claims.
- PASS `nbb -cp scripts:test test/workspace_test.cljs` — 24 tests, 120 assertions, 0 failures.
- PASS `nbb test/project_test.cljs` — 12 tests, 167 assertions, 0 failures.
- PASS `clj-kondo --lint scripts test` — 0 errors, 0 warnings.
- PASS `sm-migrate.bb --dry-run` and live verification — 283/283 records round-trip verified with `clojure.edn`.
- PASS `sm-log.bb` — successfully logged 284th entry directly to `ledger.edn`.
- PASS `sm-list.bb` — verified clean output formatting and raw EDN emission from `ledger.edn`.
- PASS `git diff --check`.
- VISIBLE (not a pass): workspace tests report `ERROR opencode lint spawn ENOENT` and `ERROR jscpd spawn ENOENT` (pre-existing host tooling gap inside an otherwise green test suite).

## Submodule Truth

Only gitlinks whose new commit is already reachable from a pushed remote branch were absorbed.

| Submodule | Branch | Head | Proof |
| --- | --- | --- | --- |
| `.agents` | `feat/session-mycology-edn-ledger` | `a6f6afe1d3b6` | equals `origin/feat/session-mycology-edn-ledger` |
| `openplanner` | `device/yoga` (declared in `.gitmodules`) | `582efb7ebbdc` | equals `origin/device/stealth` |
| `axxium` | `feat/did-actor-identity-local` | `603ea4cd88f5` | ancestor of `origin/...`, local behind 2 |
| `bitch-tracker` | `device/stealth` | `693f1388e61b` | ancestor of `origin/device/stealth`, local behind 15 |
| `calliope` | `device/stealth` | `b473c21251d3` | equals `origin/device/stealth` |
| `katamorph` | `device/stealth` | `0271e84ab7b7` | equals `origin/device/stealth` |
| `knoxx` | `feat/axxium-identity-local` | `40221a69f7ff` | equals `origin/feat/axxium-identity-local` |
| `shx` | `feat/supervisor-ir-card` | `5dbac4fde8b3` | ancestor of `origin/...`, local behind 26 |
| `Truth` | `codex/truth-native-demo-tour` | `c7cc34a8248a` | equals `origin/codex/truth-native-demo-tour` |

## Excluded / Concurrent Dirt (left untouched on purpose)

- `.env` — holds a live `GH_TOKEN`. Never staged; ignored.
- `.ημ/diagnostics` — bulk CI/session evidence, ~733 MiB across ~18,100 files, ignored.
- `.github/workflows/agy-review.yml`, `scripts/agy-review.sh` — review-workflow work, byte-identical to `origin/feat/agy-gemini-3-8-flash-review-workflow` (foresight#152). Left untracked.
- `calliope` (4 modified tracked files), `openplanner` (1 modified `pnpm-lock.yaml`), `uxx` (4 modified tracked files) — live child-repository work owned by those repositories.
- Untracked agy review artifacts in submodules — generated during review-workflow PR creation and preserved on their respective branches.
- `opencode` gitlink unchanged (`cc4b4561`).
