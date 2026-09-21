# E1.03/E1.09 — Observed child gate execution

Produced 2026-09-20. Every child was cloned fresh from its own remote into a
directory outside any eta-mu workspace, installed from nothing, and run with the
exact commands `config/quality-gates.edn` declares for it. Exit codes are
recorded as observed.

**This does not accept any module.** It is one execution on one host. Acceptance
still requires the owning repository's merged evidence under the procedure in
[acceptance.md](acceptance.md). Rows below stay `Pending`.

Each child's default branch tip equals the revision Foresight pins, so these
results are at the registered pins.

## Results

| Child | install | build | unit | static | Observed |
|---|---|---|---|---|---|
| kanban-orchestrator | — | — | — | — | `package.json` declares no scripts at all. Confirms the catalog's two unavailable entries; there is nothing to run. |
| clio | pass | (none declared) | `test:shadow` pass | clj-kondo 0 errors / 0 warnings, then **unavailable** | `lint:kondo` chains `bb scripts/lint_extern_boundary.bb`; babashka was not installed on this host, so the second half is unavailable evidence, not a failure. |
| chat-ui | pass | pass | pass | pass | All declared gates green. `npm ci` fails separately — see lockfile drift below. |
| rheos | pass | **fail**, then pass | **fail**, then pass | pass | See bootstrap gap below. After bootstrap: 150 tests / 772 assertions, 0 failures; four release builds complete with 0 warnings. |
| session-mycology | pass | pass | pass | **fail** | Extern-boundary guard resolved the donor layout. Fixed in open-hax/session-mycology#1. |
| sol | pass | **unavailable** | **unavailable** | pass | Classpath build stops at `Unable to clone https://github.com/open-hax/event-ledger`; that repository was not reachable from this host. Unavailable evidence, not a failure. |
| osmos | — | `clojure -M:uberjar` pass | `clojure -M:test` pass | (catalog: unavailable) | 83 tests / 325 assertions, 0 failures. Packaged `target/kms-ingestion.jar`. |
| receipt-river | pass | pass | **fail** | **fail** | Unit failure is a real defect, below. Static fixed in open-hax/receipt-river#1. |
| axxium | `npm ci` pass | pass | pass | (catalog: unavailable) | Build reports **6 warnings**; the test runner reports **0 tests, 0 assertions**. |

## Findings

### Rheos: the declared build gate omits a required bootstrap

`pnpm run build` and `pnpm run test` both fail from a clean clone:

```
The required namespace "open-hax.openplanner-protocols" is not available,
it was required by "rheos/backend/domain/events.cljs".
```

`shadow-cljs.edn` compiles from `deps/protocols/src` and `deps/chat-ui/src`,
which `scripts/bootstrap-source-deps.sh` populates. The installed
`@open-hax/protocols` package contains **no ClojureScript sources at all**,
matching the planning pack's note that Protocols ships dist-only. Neither the
README nor the declared gate mentions the bootstrap step.

With `bash scripts/bootstrap-source-deps.sh` first, both gates pass. The honest
gate is therefore the bootstrap plus the build, and E1.09 should record it that
way.

The bootstrap fetches Protocols from the donor at
`0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90`. Rheos cannot currently build
without that donor revision, which is independent support for keeping the root
`eta-mu` pin at the donor baseline — see
[donor-retirement-blockers.md](donor-retirement-blockers.md).

### receipt-river: unit gate fails at the pin

Reproducible from a cleared `.shadow-cljs` cache:

```
TypeError: eta_mu.receipt_river.extern.git.exec_at.cljs$core$IFn$_invoke$arity$2
  is not a function
    at eta_mu$receipt_river$infra$local_git_provider$git_value
       (…/local_git_provider.cljs:69)
```

`extern/git.cljs` defines `exec-at` with arities 2 and 3; the caller uses the
2-arity form. The compiled `:test` build does not see the multi-arity
definition. This is a behavioural defect owned by the child repository.

### Extern-boundary guards assumed the donor layout

`session-mycology` and `receipt-river` carry byte-identical copies of
`scripts/check-ledger-extern-boundaries.mjs`, which resolved
`packages/<name>/src/cljs` and defaulted to the donor sibling list
`["receipt-river", "session-mycology", "fork-tax"]`. In an extracted repository
that path cannot exist, so the declared `lint:kondo` gate failed without
inspecting a file. Had the directory existed but been empty, it would have
reported success while checking nothing. Fixed in each repository with a
planted-violation fixture proving the guard still rejects.

### Lockfiles and package-manager policy disagree with the catalog

| Child | Lockfile | Catalog declares |
|---|---|---|
| chat-ui | `package-lock.json` | `pnpm run …` |
| sol | `package-lock.json` | `pnpm run …` |
| axxium | `package-lock.json` | `npm run …` |
| rheos, session-mycology, receipt-river, clio, kanban-orchestrator | **none** | `pnpm run …` |

Two of the three npm lockfiles are stale against their manifests, so the
reproducible install fails outright:

- chat-ui — `npm ci`: `Missing: dompurify@3.4.15 from lock file` (also `scheduler`)
- sol — `npm ci`: `lock file's shadow-cljs@3.4.4 does not satisfy shadow-cljs@3.5.3`, `Missing: buffer@6.0.3`

Five children have no lockfile at all, so no install is pinned. E1.03 requires
every bootstrap dependency to be explicit and pinned; that is not met.

Rheos additionally declares `@open-hax/protocols` with npm's unsupported
`&path:` subdirectory selector, which only pnpm resolves — `npm install` in
that repository fails. The package-manager choice is load-bearing and
undeclared.

### axxium: a green unit gate that selects nothing

`npm test` exits 0 with `Ran 0 tests containing 0 assertions`, and `npm run
build` completes with 6 warnings. This is the case E1.09's "real selection
counts" clause exists for. The repository's `main` carries **no** `*_test.cljs`
files; six exist only on `device/yoga`, which is seventeen commits behind
`main` and is the unaccepted reconciliation line.

### Guidance exists on `device/yoga`, unmerged

The register records six children as having no `AGENTS.md` at their pin. Each
does have one, on `device/yoga`, in a single commit ahead of `main`:

| Child | `device/yoga` relative to `main` |
|---|---|
| kanban-orchestrator, clio, chat-ui, session-mycology, sol | 1 ahead, 0 behind — adds `AGENTS.md` |
| osmos | 2 ahead, 0 behind — adds `AGENTS.md`, `README.md`, `.gitignore` and the E1.08 service fixes |
| rheos, receipt-river | identical to `main` |
| axxium | 1 ahead, **17 behind** — the unaccepted extraction candidate, not a fast-forward |

`.gitmodules` records `branch = device/yoga` for every child, but the gitlinks
pin `main`. For the first six this is a fast-forward away; for axxium it is not,
and merging it would drop seventeen commits including three of its four
workflows. Do not treat the branch name as a uniform instruction.

### No child has CI

Only axxium has any `.github/workflows` (4 on `main`, 1 on `device/yoga`). The
other eight have none, on either branch. E1.04 and E1.10 own this.

## Reproduction

```bash
git clone --depth 1 https://github.com/open-hax/<child> && cd <child>
pnpm install --no-frozen-lockfile     # npm ci for axxium
pnpm run build && pnpm run test && pnpm run lint:kondo
```

For rheos, run `bash scripts/bootstrap-source-deps.sh` before the build. For
osmos, `clojure -M:test` and `clojure -M:uberjar`. clj-kondo, babashka and the
Clojure CLI must be present; where a tool was absent the row says
**unavailable** rather than claiming a result.
