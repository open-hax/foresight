# Fork Tax Handoff

**Timestamp:** 2026-11-03T11:56:01Z
**Repository:** `open-hax/foresight`
**Branch:** `plan/fork-dev-origins` (169 commits ahead of `origin/plan/fork-dev-origins` before this snapshot)
**Snapshot parent head:** `de9ca9dac95ed8e73f7a4782578effd3e54150cf`

## Signal

- Absorbed the durable root work that existed only as uncommitted dirt: federated backup direction, resource-access service, Rheos markdown content loop, agent scratch-cleanup policy, the Colab/Gemma research pilot and its proposals, nine new kanban cards, three verification records, and four new root scripts plus the local identity pm2 config.
- Committed the append-only ledgers that had already accumulated rows: `.ημ/receipts.edn`, `.ημ/kanban-events/ledger.edn`, `.ημ/session-mycology/ledger.md`.
- Hardened `.gitignore`: `/.env`, `/.env.*` (with `!/.env.example`), and `/.ημ/diagnostics/`. A live `GH_TOKEN` was sitting untracked in `.env`; it was never staged.
- Absorbed 8 submodule gitlinks, each verified to be contained in an already-pushed remote branch: `.agents`, `axxium`, `bitch-tracker`, `calliope`, `katamorph`, `knoxx`, `shx`, `Truth`.
- Regenerated the Π artifacts (`.ημ/Π_STATE.sexp`, `.ημ/Π_LAST.md`, `.ημ/Π_MANIFEST.sha256`).

## Verification (run, not assumed)

- PASS `nbb scripts/project.clj validate` — 24 workspace sources, 23 submodules, 39 invariants, 1 lineage source, 6 provisional claims.
- PASS `nbb -cp scripts:test test/workspace_test.cljs` — 24 tests, 118 assertions, 0 failures.
- PASS `clj-kondo --lint scripts test` — 0 errors, 0 warnings.
- PASS `python3 -m py_compile` on `scripts/archive-scratch.py`, `scripts/restore-drive-media.py`, and `docs/research/colab-gemma-pilot/*.py`.
- PASS `node --check` on `scripts/start-knoxx-identity-local.mjs` and `ecosystem.identity.local.config.cjs`.
- PASS `git diff --check`.
- VISIBLE (not a pass): the workspace test run prints `ERROR opencode lint spawn ENOENT` and `ERROR jscpd spawn ENOENT`. Those are pre-existing host tooling gaps inside an otherwise passing run; they were not "fixed" by weakening anything.

## Submodule Truth

Only gitlinks whose new commit is already reachable from a pushed remote branch were absorbed.

| Submodule | Branch | Head | Proof |
| --- | --- | --- | --- |
| `.agents` | `device/stealth` | `2a62d58ae194` | ancestor of `origin/device/stealth`, local behind 15 |
| `axxium` | `feat/did-actor-identity-local` | `603ea4cd88f5` | ancestor of `origin/...`, local behind 2 |
| `bitch-tracker` | `device/stealth` | `693f1388e61b` | ancestor of `origin/device/stealth`, local behind 15 |
| `calliope` | `device/stealth` | `b473c21251d3` | equals `origin/device/stealth` |
| `katamorph` | `device/stealth` | `0271e84ab7b7` | equals `origin/device/stealth` |
| `knoxx` | `feat/axxium-identity-local` | `40221a69f7ff` | equals `origin/feat/axxium-identity-local` |
| `shx` | `feat/supervisor-ir-card` | `5dbac4fde8b3` | ancestor of `origin/...`, local behind 26 |
| `Truth` | `codex/truth-native-demo-tour` | `c7cc34a8248a` | equals `origin/codex/truth-native-demo-tour` |

## Excluded / Concurrent Dirt (left untouched on purpose)

- `.env` — holds a live `GH_TOKEN`. Never staged; now ignored. **Rotate if it was ever shared.**
- `.ημ/diagnostics` — bulk CI/session evidence, ~733 MiB across ~18,100 files, never tracked in this repository. Now ignored; the per-directory inventory lives in `.ημ/Π_STATE.sexp`.
- `.github/workflows/agy-review.yml`, `scripts/agy-review.sh` — the antigravity agent's review-workflow work, byte-identical to `origin/feat/agy-gemini-3-8-flash-review-workflow` (foresight#152). Already preserved on that pushed branch, so committing duplicates here would only add noise.
- `openplanner/` — 184 MiB nested clone that is **not** declared in `.gitmodules` or `src/foresight/project.cljc`. Absorbing it would grant ownership without the required manifest + project + repository-table update, so it stays untracked. Its HEAD `582efb7` is on both `origin/device/stealth` and `personal/device/stealth`; one modified `pnpm-lock.yaml` remains local to it.
- `calliope` (4 modified tracked files) and `uxx` (4 modified tracked files: `package.json`, `pnpm-lock.yaml`, `pnpm-workspace.yaml`, `receipts.edn`) — live child-repository work owned by those repositories.
- `.agents`, `bitch-tracker`, `shx`, `axxium` local checkouts sit behind their already-pushed remote branches. Nothing to publish.
- `opencode` gitlink unchanged; its HEAD `cc4b4561` has no remote branch, but no pointer update was proposed either.

## Blockers / Residual Findings

1. `docs/research/colab-gemma-pilot/budget_law.cljc` declares `research.colab-budget-law` while the filename implies `research.colab-gemma-pilot.budget-law`. `clj-kondo` reports 1 error and 1 warning, and `law_cli.clj` has an unresolved require. Pre-existing in untracked research work; recorded rather than edited outside this snapshot's scope.
2. Root lint scope is `scripts` + `test` only. `docs/research` and other `docs/**/*.clj*` are outside the gate and are **not** claimed clean.
3. `openplanner` needs a deliberate decision: declare it as a direct submodule (updating `.gitmodules`, `src/foresight/project.cljc`, and the AGENTS repository table together) or keep it out of the manifest.

## Next

Approve or reject the `openplanner` manifest decision, then rotate the `GH_TOKEN` that was sitting in `.env`.
