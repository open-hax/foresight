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

See [`AGENTS.md`](AGENTS.md#repository-map-where-to-look) for the repository
map: what each child submodule owns and where to look for a given topic
before searching root-owned code.

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
nbb scripts/evidence.clj validate
nbb scripts/evidence.clj list --only katamorph --kind unit,static
nbb scripts/evidence.clj run --only katamorph --kind static
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

## Evidence gates

`config/quality-gates.edn` is the explicit map for child repositories whose
owning gates are not represented by an exact root `package.json` script. Every
entry cites the owning manifest or workflow and classifies the gate as static,
unit, integration, E2E, coverage, security, build, live smoke, or independent
review. The catalog also distinguishes locally runnable commands from
workflow-only and externally hosted gates.

`scripts/evidence.clj` validates and runs only mapped commands for explicitly
selected direct submodules. It repeats workspace path and checkout-identity
checks before spawning a process. Failed, blocked, unavailable, and
not-applicable are separate outcomes; a missing checkout, tool, credential, or
host never becomes a pass. See
[`docs/specs/inflight-completion-and-knoxx-lift.md`](docs/specs/inflight-completion-and-knoxx-lift.md)
for the revision-bound promotion and test-tier contract.

## Project law

`src/foresight/project.cljc` is the portable Lisp/EDN declaration of the
Foresight constellation. It lists every direct repository and root-owned
consolidation input, records ownership and actionability, and names invariants
with the repository evidence from which each law was recovered.

`src/foresight/law/project.cljc` validates the portable declaration. Runtime
checkout facts such as symlink confinement and device/inode identity remain in
the NBB workspace adapter; the pure project law validates identity, ownership,
actionability, invariant references, and agreement with `.gitmodules`.

```sh
nbb scripts/project.clj repos
nbb scripts/project.clj show
nbb scripts/project.clj validate
nbb test/project_test.cljs
```

`.gitmodules` remains Git's checkout manifest. The project declaration is the
semantic inventory and contract: changing one without the other is visible
drift, not an implicit change in ownership or authority.

## Board

The root board uses canonical `openhax.kanban.edn`; `openhax.kanban.json` is a
compatibility mirror for the published JSON-only CLI:

```sh
eta-mu kanban count
eta-mu kanban list
```

All provenance ledgers are physically stored beneath `.ημ/`.
