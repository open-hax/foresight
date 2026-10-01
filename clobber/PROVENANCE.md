# clobber — provenance

Root-owned **consolidation input**. Present for inventory and extraction only;
it gains no execution authority from being here
(`:foresight/consolidation-inputs-inventory-only`).

## Origin

| | |
| --- | --- |
| Original name | `pm2-clj-project` (binaries `pm2-clj`, `clobber`) |
| Repository | `riatzukiza/devel` (`~/devel`) |
| Path | `pm2-clj-project/` |
| Recovered from | commit `01d0f7200^` (parent of the 2026-04-17 snapshot "stale file cleanup + .gitmodules prune") |
| Tree | `90e0b2a170099547b73c0df2c3b9c78c17336dc2` |
| Recovered on | 2026-10-01, via `git archive 01d0f7200^ pm2-clj-project` |
| Copy mode | whole, byte-for-byte (user decision: "port it whole, then refactor") |

The surviving traces in `~/devel` are a broken symlink `bin/clobber ->
../pm2-clj-project/bin/clobber` and a compiled bundle in `~/devel/.clobber/`.

## What it is

A ClojureScript DSL for PM2 ecosystems: prototype apps/profiles/mixins/stacks
(`extends`, `with`, `mix`, `scope`, `each`, `on`, `where`, `only`), mode tiers
(`matrix`, `tiers`, `env-tiers`), a deep-merge law with a `::remove`
sentinel and merge-apps-by-`:name`, file imports evaluated through nbb/SCI,
and a CLI that renders to ecosystem JSON/CJS and delegates to `pm2`.

## Known defects at recovery (not fixed in this copy)

The copy stays byte-for-byte; these are recorded, not fixed. Each was checked
against the recovered source on 2026-10-01 (open-hax/foresight#120 review).

### Entry points and builds

- `bin/pm2-clj` is a pass-through to `pm2`. It only injects `PM2_CLJ_ENTRY` and
  `PM2_CLJ_MODE` and never loads `pm2-clj.cli/main`, so the DSL CLI build
  (`dist/pm2-clj.js`) is not the package bin's entrypoint. `bin/clobber` loads
  `dist/clobber.js` (`clobber.cli/main`). The last documented workflow
  (`docs/notes/infrastructure/pm2-clj-dsl-v3.md`, 2026-02-03 note) was
  `ecosystems/*.cljs` built with shadow-cljs, then `pm2 start
  ecosystem.config.cjs`; `pm2-clj` and `.pm2.edn` were already legacy.
- The `:pm2-config` build (`shadow-cljs.edn`) has `:main
  pm2-clj.runtime/-main`, and nothing in it requires `pm2.ecosystem.config`, so
  that namespace's `module.exports` assignment is never compiled in. The
  committed `ecosystem.config.cjs` contains no `module.exports`, so `pm2 start
  ecosystem.config.cjs` gets no apps.
- `package.json` still carries the placeholder name `@your-scope/pm2-clj`.

### Evaluation

- `clobber.cli/render-file` calls `clojure.reader/read-string`, which is not
  required (the tree uses `cljs.reader`). Even with that fixed, `read-string`
  reads only the first form, and `eval` has no implementation in the
  shadow-cljs `:node-script` target. `render` cannot work as written.
- `pm2-clj.eval` dispatches on `path/extname`, which returns `.clj` for
  `base.pm2.clj` and `.edn` for `x.pm2.edn` (the `.pm2.edn` comparison can
  never match). A `.pm2.clj` file therefore falls to the `:else` branch, is read
  as unevaluated data, and `apply-profile` later calls `dissoc` on a list and
  throws. None of the `ecosystems/*.pm2.clj` files evaluate.
- `eval-via-nbb!` and `eval-code-via-nbb!` check only `result.error`, not a
  nonzero exit status, so a failed `nbb` run can yield empty stdout, `nil`, and
  an empty rendered ecosystem.
- `eval-with-imports!` collects imported files' values in `:import-results`,
  but `eval-file-any` returns only `:result`, so an imported file's value is
  unreachable through the API.
- `pm2-clj.eval` shells out to nbb and writes temporary `.cjs` files.

### Merge and removal

- `remove` is shadowed in three namespaces without
  `(:refer-clojure :exclude [remove])`: `merge.cljs:3` and `internal.cljs:13`
  define it as a keyword, and `dsl.cljs:21` re-binds it to `i/remove`. Every
  call site then invokes a keyword:
  - Two-argument calls (`merge.cljs:13`, `dsl.cljs:298`, `dsl.cljs:543`) return
    their input collection unchanged, so `::skip` sentinels stay in the merged
    vector and `(remove nil?)` is a no-op.
  - The one-argument call at `internal.cljs:71` returns `nil`, which `into`
    then receives as its transducer, so `remove-internal-keys` fails.
- `merge-apps-by-name` (`merge.cljs:12`) concatenates base and override names
  without `distinct`, so every app present on both sides appears twice. Any
  profile application hits this (`--mode dev` on `ecosystems/base.pm2.edn`
  yields `api` and `worker` twice each).
- The removal keywords disagree between producers and consumer. The merge skip
  check reads `:pm2-clj/remove`, while the DSL and CLI write
  `:pm2-clj.internal/remove-app` (`i/remove-app-flag`). Key removal writes
  `:pm2-clj.internal/remove`, while `deep-merge` compares against
  `:pm2-clj.merge/remove`. Removals are therefore stored as ordinary values or
  ignored.
- `clobber.macro/defprofile` is a function, so its body's `defapp` calls run
  first, register into the global `app-registry`, and hand `defprofile` maps
  rather than forms. The `case` matches nothing, the profile is registered
  empty, and its apps leak into the base registry (`pm2/complex-test.cljs`
  shows the duplicates).
- `clobber.macro` and `pm2-clj.dsl` keep evaluation state in global atom
  registries.

### CLI and environment

- `pm2-clj.cli/set-in-eco` uses only `(first ks)` for `apps.<name>.…` keypaths,
  so `apps.api.env.PORT` is truncated to `:env`, and `apps.api=x` creates a
  stray `:value` key. Nested app keypaths are unsupported.
- `clobber.macro/env-var` looks up `[(name var-sym) ""]` with `get-in`, which
  adds an empty-string key lookup, so a set variable reads as `nil` and the
  two-argument arity returns its fallback. A later port should read the variable
  directly (`aget`) and keep the nil and fallback behaviour.

## Intended destination

The supervisor IR epic in shx/Hexis
(`shx/kanban/supervisor-ir-pm2-compose-systemd-k8s.md`): one EDN description of
process/service intent, rendered to pm2, docker compose, systemd and
Kubernetes, with `check` (diff against live state) and `apply`. It is blocked by
`port-shx-to-cljc` and `hexis-assembler`. When it is extracted, clobber's merge
law and prototype semantics move to `.cljc`; the registries, eval and
temp-file plumbing stay here.
