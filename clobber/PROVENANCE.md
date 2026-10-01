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

- `src/pm2_clj/merge.cljs` defines `(def remove ::remove)` without excluding
  `clojure.core/remove`, then calls `(remove (fn ...) ...)` in
  `merge-apps-by-name` — that invokes the keyword, so the by-name merge is
  broken. `docs/notes/infrastructure/pm2-clj-implementation-summary.md` records
  it as never building cleanly.
- `clobber.macro` and `pm2-clj.dsl` keep evaluation state in global atom
  registries.
- `clobber.cli/render-file` uses `eval` on read forms; `pm2-clj.eval` shells out
  to nbb and writes temporary `.cjs` files.
- `package.json` still carries the placeholder name `@your-scope/pm2-clj`.

## Intended destination

The supervisor IR epic in shx/Hexis
(`shx/kanban/supervisor-ir-pm2-compose-systemd-k8s.md`): one EDN description of
process/service intent, rendered to pm2, docker compose, systemd and
Kubernetes, with `check` (diff against live state) and `apply`. It is blocked by
`port-shx-to-cljc` and `hexis-assembler`. When it is extracted, clobber's merge
law and prototype semantics move to `.cljc`; the registries, eval and
temp-file plumbing stay here.
