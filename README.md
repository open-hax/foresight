# Foresight

Foresight is a source-level constellation of independently owned repositories
and root-declared consolidation inputs. The root coordinates visibility and
explicit cross-repository operations; it does not replace each submodule's
package manager or quality gates.

`.agents/` and `eta/` are intentional consolidation inputs. `.agents/` carries
the canonical skill catalog in its own nested Git repository; `eta/` is a
root-owned Clojure agent harness. Their working originals may remain elsewhere
while this repository converges on a higher-quality source. Compatibility
copies are not removed implicitly.

## Setup

```sh
git submodule update --init
nbb scripts/workspace.clj inventory
```

NBB has no task registry, so commands name the script directly:

```sh
nbb scripts/workspace.clj report
nbb scripts/workspace.clj install --only eta-mu,uxx
nbb scripts/workspace.clj build --only eta-mu
nbb scripts/workspace.clj test --only muse
nbb scripts/workspace.clj lint --all
nbb scripts/workspace.clj jscpd --only muse
```

`inventory` is read-only; `report` writes only root-generated artifacts.
Inventory and reports include direct `.gitmodules` repositories plus the
explicit `.agents` and `eta` consolidation roots. Inspection stays at each
declared root; nested packages and Git repositories are not promoted into
workspace projects.

Executable actions require `--only` or `--all`, run sequentially, and use only
action-eligible direct submodule root metadata. `--all` excludes consolidation
inputs, and explicitly selecting `.agents` or `eta` fails before execution.
Missing scripts, ambiguous managers, and missing frozen-install locks are
reported as unavailable rather than guessed.

`report` writes `reports/workspace.json` and `reports/workspace.md`. `jscpd`
is baseline reporting rather than a duplication gate; broken tooling still
returns a failure.

## Board

The root board uses canonical `openhax.kanban.edn`; `openhax.kanban.json` is a
compatibility mirror for the published JSON-only CLI:

```sh
eta-mu kanban count
eta-mu kanban list
```

All provenance ledgers are physically stored beneath `.ημ/`.
