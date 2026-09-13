# Dependency analysis — Operation Eta Mu Breakdown

**Inspection date:** 2026-09-12. **Evidence class:** revision-bound declaration/configuration inspection plus selected implementation reads. **Not performed:** complete source-namespace closure, whole-constellation scanning, actual installs/builds/tests, branch/PR archaeology, yoga inspection, or repository mutation.

## Baselines

| Repository | Inspected main revision | Foresight's recorded gitlink at its inspected main |
|---|---|---|
| open-hax/eta-mu | `476b07bd66efb84566a4159556deacb1e9407e6f` | `0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90` |
| open-hax/knoxx | `d49242b22cd8b303a2ac9807c70a3acb42a47a1a` | `fb08a10a8aa32a594cc97ae11b820113de4cf386` |
| octave-commons/epiphany | `643be698ea0d841dd19385506b272872306e456e` | `ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66` |
| open-hax/foresight | `78befe3daed9a27d79aa511848de3985602decbc` | Not applicable |
| Existing open-hax/axxium | `2439d4d6b8e546cda276f09f5c96db59226ecad6` | No Axxium gitlink in the inspected Foresight root |

The mainline manifests are not assertions about newer work on device branches or in unmerged PRs. No source-code share/LOC percentage was measured.

## Direct transplant relationships

| Target | Observed package/source relationships | Packaging or runtime concern |
|---|---|---|
| Kanban Orchestrator | No package dependencies/scripts declared; README describes Sol-style contract host, MCP Contracts loader and Rheos MCP endpoint | Contract-only assembly/conformance; reported loader relationships need direct contract/runtime verification |
| Clio | No eta-mu workspace dependencies in inspected npm/deps descriptors | NBB/BB/Shadow source/CLI modes, native `fs-ext-extra-prebuilt`, installed CLI/classpath contract |
| Chat UI | No local package/source dependency in inspected package and Shadow descriptors | Exported Sol/Knoxx/OpenCode session adapters; Rheos consumes source, not merely built JS |
| Axxium | No local package/source dependency in inspected package and Shadow descriptors | Existing standalone repo/history must be reconciled; PostgreSQL runtime; do not infer test coverage from script existence |
| Rheos | Protocols npm dev dependency plus Protocols and Chat UI source paths; Katamorph Git ref; uxx-helix npm dependency | Source availability, Maven/npm metadata, browser assets, CLI and GitHub sync targets |
| Session Mycology | Own source paths only in inspected Shadow descriptor | Root extern guard; eta-mu CLI consumes both package and source; root test.bb does not explicitly select its suite |
| Sol | eta-mu source and Turn Processor local roots; Katamorph and event-ledger Git refs | Shared kondo/contract guard; mixed source/npm cycle with eta-mu CLI; mutation/premerge harness portability |
| Knoxx ingestion | No local project dependencies in deps.edn | Explicit contracts root and resources needed; live integration depends on OpenPlanner + DB; Knoxx/Proxx/other runtime adapters stay separate from compile dependencies |

“No local dependency in inspected descriptors” does not mean “no dependencies,” or an exhaustive proof that no source file/script can reach outside the package.

## Reverse dependencies to change

The directly observed remaining package consumer is `packages/eta-mu` (the CLI/source package, **not** the monorepo root). Its npm manifest declares Rheos, Sol, and Session Mycology workspace dependencies. Its Shadow descriptor directly includes Session Mycology source. It also declares/uses remaining packages such as Receipt River, Fork Tax, Turn Processor, Terminal UI and Contracts Output; keep those relationships intact while changing the target edges.

The monorepo root has script and verification relationships with extracted products. `scripts/test.bb` explicitly selects Clio, Rheos, Sol, Chat UI and Axxium. Root dev/start dispatch to Rheos. Session Mycology's own suite must be invoked directly; it is not among that root script's explicit selections. Receipt River still uses the extern-boundary guard shared with Session Mycology, so deleting the guard when moving Mycology would break an unrelated remaining package.

These are the observed inbound edges, not a statement that no other source/runtime/workflow consumer exists. E1.01 requires the complete inbound audit before donor deletion.

## Exact edit inventory

| File / scope | Required treatment |
|---|---|
| Copied Rheos `package.json` | Replace `@open-hax/protocols: workspace:*` with a tested immutable source; preserve external package versions unless a specific compatibility correction is proven |
| Copied Rheos `deps.edn` | Replace `../protocols/src` and `../chat-ui/src` with declared, pinned source inputs and their dependency metadata |
| Copied Chat UI source-distribution metadata | Expose a tested source-consumption contract for Rheos, distinct from compiled library/demo exports |
| Copied Sol `deps.edn` | Replace the two `:local/root` entries with donor-SHA/subdirectory coordinates initially; retain existing Katamorph/event-ledger pins pending separately justified changes |
| Copied Sol `package.json`, `.clj-kondo/config.edn` | Replace root contract-guard and shared kondo-config path escapes without weakening their checks |
| Copied Session Mycology `package.json` | Replace root extern-boundary-guard path; preserve its own test suite and published exports |
| Remaining `packages/eta-mu/package.json` | Later switch Rheos, Sol and Session Mycology to tested new-repository SHAs |
| Remaining `packages/eta-mu/shadow-cljs.edn` | Later replace sibling Session Mycology source inclusion; audit source-level requires and CLI launch paths |
| eta-mu root scripts/workflow matrices | Remove/rewrite only migrated target dispatch; preserve checks for remaining products and shared review/test infrastructure |
| Ingestion `Dockerfile`, config/contracts loader, deployment descriptors | Provide owned build inputs and explicit contract root; preserve service routes/configuration dependencies during extraction |
| Every receiver's lock, ignores, docs, AGENTS and workflows | Establish standalone install, test/build/assembly/lint/coverage, packaging and guide contracts |
| Foresight `.gitmodules`, gitlinks, `src/foresight/project.cljc`, quality catalog and scripts/docs | Admit new direct children, exact commits and owned gates rather than expecting discovery to recurse into former donor packages |

## Git-coordinate examples

These are documented coordinate forms, **not tested installation results**. The pinned eta-mu root uses pnpm 10.14.0; verify the exact combined selector on that version before applying a loop.

A pnpm Git subdirectory selector:

```json
{
  "@open-hax/protocols": "github:open-hax/eta-mu#476b07bd66efb84566a4159556deacb1e9407e6f&path:/packages/protocols"
}
```

This selects the package location. It does **not** fix that package's source/files/build contract. Protocols currently lists `dist` and types, not its CLJS source, and its inspected subdirectory has no fetched deps.edn. A consumer-side, SHA-locked source bootstrap is an explicit interim option under the no-donor-edits rule; it needs its own tests and eventual removal/replacement where appropriate.

For Sol's two existing tools.deps local dependencies, the intended bootstrap coordinates are:

```clojure
{open-hax/eta-mu
 {:git/url "https://github.com/open-hax/eta-mu.git"
  :git/sha "476b07bd66efb84566a4159556deacb1e9407e6f"
  :deps/root "packages/eta-mu"}

 open-hax/turn-processor
 {:git/url "https://github.com/open-hax/eta-mu.git"
  :git/sha "476b07bd66efb84566a4159556deacb1e9407e6f"
  :deps/root "packages/turn-processor"}}
```

Both inspected subdirectories have minimal `deps.edn` source descriptors. This still requires testing the actual namespace closure and JavaScript requirements used by Sol. A Maven/CLJ descriptor is not an npm dependency installer.

Once a transplanted dependency has a consumable root and tested commit, change its consumer to that repository and remove the donor subdirectory selector. Keep public library symbols/package names stable. The new repository's commit hash cannot be supplied before it exists.

Official references are recorded under `official_documentation` in `data/sources.json`.

## Ordering and cycle treatment

Begin by copying all eight without donor edits. Bootstrap low-coupling packages independently (Clio, Chat UI, Session Mycology, Axxium reconciliation), and validate the orchestrator's contract bundle. Switch Rheos's Chat UI edge after source consumption is proven. Sol needs its donor-source dependencies and shared tooling explicitly resolved. Finally migrate remaining eta-mu consumers and retire target donor paths one at a time.

The combined declared graph contains `Sol --Clojure source--> eta-mu CLI/source --npm dependency--> Sol`. Do not misreport this as a single package-manager resolver cycle or force an unnecessary behavioral refactor. Immutable donor snapshots permit staging. The acceptance question is whether each actual compiler/installer/launcher resolves its declared closure without ambient paths or retired target inputs.

Axxium's existing history and ingestion's new name are separate preflight decisions; neither justifies overwriting a remote. The other proposed remotes were not exhaustively checked in this planning run.

## Diagrams and evidence

`graphs/01-observed-package-dependencies.*` shows the declared npm/CLJ/source relationships. `graphs/02-runtime-and-contract-relationships.*` separates configurable services and README-reported host connections. Diagram edge IDs resolve in the complete relation table below and the JSON/ND-EDN datasets. SVG/PNG previews are Graphviz renderings of the same observations; `.mmd` is provided for Mermaid users.

The later numbered diagrams are explicitly **proposed** workflow/architecture views, not claims of running integrations. Static relation records in `.nd.edn` are not Clio-admitted events.

## Relationship register

| ID | Consumer | Target | Kind / status | Detail | Source IDs |
|---|---|---|---|---|---|
| R001 | Rheos | Protocols | npm-dev / observed | devDependencies @open-hax/protocols = workspace:* | rheos.package |
| R002 | Rheos | Protocols | cljs-source-path / observed | ../protocols/src | rheos.deps |
| R003 | Rheos | Chat UI | cljs-source-path / observed | ../chat-ui/src | rheos.deps |
| R004 | Rheos | Katamorph | clojure-git / observed | Git SHA be7cc332d865cfedc57b55b10cab3c9f2bd41fc4 | rheos.deps |
| R005 | Rheos | uxx-helix | npm-dev / observed | @open-hax/uxx-helix ^0.1.0; package/repository relationship needs a package export audit | rheos.package |
| R006 | Sol | eta-mu CLI/source | clojure-local / observed | open-hax/eta-mu :local/root ../eta-mu; source library, not necessarily execution of the full CLI | sol.deps |
| R007 | Sol | Turn Processor | clojure-local / observed | open-hax/turn-processor :local/root ../turn-processor | sol.deps |
| R008 | Sol | Katamorph | clojure-git / observed | v0.2.0; Git SHA 305a5e49d834aca27566f739e8510f6b409fda78 | sol.deps |
| R009 | Sol | event-ledger | clojure-git / observed | Git SHA ada7374b7f4e1c3b0ab4e6bbe996f10f06e9b93a; no observed Sol -> Clio replacement in this mainline manifest | sol.deps |
| R010 | eta-mu CLI/source | Rheos | npm-runtime / observed | workspace:* dependency | eta-cli.package |
| R011 | eta-mu CLI/source | Sol | npm-runtime / observed | workspace:* dependency | eta-cli.package |
| R012 | eta-mu CLI/source | Session Mycology | npm-runtime / observed | workspace:* dependency | eta-cli.package |
| R013 | eta-mu CLI/source | Fork Tax | npm-runtime / observed | workspace:* dependency | eta-cli.package |
| R014 | eta-mu CLI/source | Receipt River | npm-runtime / observed | workspace:* dependency | eta-cli.package |
| R015 | eta-mu CLI/source | Turn Processor | npm-dev / observed | workspace:* devDependency | eta-cli.package |
| R016 | eta-mu CLI/source | Terminal UI | npm-dev / observed | workspace:* devDependency | eta-cli.package |
| R017 | eta-mu CLI/source | Contracts Output | npm-dev / observed | workspace:* devDependency | eta-cli.package, contracts.locator |
| R018 | eta-mu CLI/source | Session Mycology | cljs-source-path / observed | Sibling src/cljs included in shadow source-paths | eta-cli.shadow |
| R019 | eta-mu CLI/source | Fork Tax | cljs-source-path / observed | Sibling src/cljs included in shadow source-paths | eta-cli.shadow |
| R020 | eta-mu CLI/source | Receipt River | cljs-source-path / observed | Sibling src/cljs included in shadow source-paths | eta-cli.shadow |
| R021 | eta-mu CLI/source | Turn Processor | cljs-source-path / observed | Sibling src/cljs included in shadow source-paths | eta-cli.shadow |
| R022 | eta-mu CLI/source | Terminal UI | cljs-source-path / observed | Sibling src/cljs included in shadow source-paths | eta-cli.shadow |
| R023 | Sol | contract-guard.mjs | tooling-path / observed | ../../scripts/contract-guard.mjs | sol.package |
| R024 | Sol | Shared kondo-config | tooling-path / observed | ../../kondo-config/clj-kondo.exports/open-hax/kondo-config relative to .clj-kondo | sol.kondo |
| R025 | Session Mycology | ledger extern guard | tooling-path / observed | ../../scripts/check-ledger-extern-boundaries.mjs session-mycology | mycology.package |
| R026 | Receipt River | ledger extern guard | tooling-path / observed | Shared guard remains required by Receipt River after Mycology is extracted | receipt.package |
| R027 | eta-mu root scripts | Clio | test-dispatch / observed | Explicit pnpm filter in scripts/test.bb; rewrite only after new repo gate is installed | eta.test |
| R028 | eta-mu root scripts | Rheos | test-dispatch / observed | Explicit pnpm filter in scripts/test.bb; rewrite only after new repo gate is installed | eta.test |
| R029 | eta-mu root scripts | Sol | test-dispatch / observed | Explicit pnpm filter in scripts/test.bb; rewrite only after new repo gate is installed | eta.test |
| R030 | eta-mu root scripts | Chat UI | test-dispatch / observed | Explicit pnpm filter in scripts/test.bb; rewrite only after new repo gate is installed | eta.test |
| R031 | eta-mu root scripts | Axxium | test-dispatch / observed | Explicit pnpm filter in scripts/test.bb; rewrite only after new repo gate is installed | eta.test |
| R032 | eta-mu root scripts | Rheos | script-dispatch / observed | Root dev and start dispatch to Rheos | eta.root |
| R033 | Chat UI | Sol | adapter-surface / observed | Exported create session adapter; not an npm or Clojure dependency declaration | chat.shadow |
| R034 | Chat UI | Knoxx backend | adapter-surface / observed | Exported create session adapter; not an npm or Clojure dependency declaration | chat.shadow |
| R035 | Chat UI | OpenCode | adapter-surface / observed | Exported create session adapter; not an npm or Clojure dependency declaration | chat.shadow |
| R036 | Kanban Orchestrator | Rheos | mcp-service / reported | Documented rheos-kanban MCP endpoint; inspect contract bytes in implementation preflight | orchestrator.readme |
| R037 | Kanban Orchestrator | MCP Contracts | contract-loader / reported | Documented generic :mcp-server loader | orchestrator.readme |
| R038 | Kanban Orchestrator | Sol | runtime-host / reported | Documented optional Sol-style host with additional contract roots | orchestrator.readme |
| R039 | Knoxx ingestion | Knoxx contracts root | configuration-path / observed | CONTRACTS_DIR, cwd/contracts, then cwd/../contracts discovery | ingestion.contracts |
| R040 | Knoxx ingestion | Knoxx backend | runtime-configuration / observed | Knoxx backend adapter; translation configuration remains Knoxx-owned | ingestion.config |
| R041 | Knoxx ingestion | OpenPlanner | runtime-configuration / observed | Configurable sink; integration suite explicitly requires OpenPlanner and DB | ingestion.config, ingestion.deps |
| R042 | Knoxx ingestion | Proxx | runtime-configuration / observed | Configured proxx endpoint; configuration presence does not establish mandatory use in every deployment | ingestion.config |
| R043 | Knoxx ingestion | PostgreSQL | runtime-configuration / observed | Configured postgres endpoint; configuration presence does not establish mandatory use in every deployment | ingestion.config |
| R044 | Knoxx ingestion | Redis | runtime-configuration / observed | Configured redis endpoint; configuration presence does not establish mandatory use in every deployment | ingestion.config |
| R045 | Knoxx ingestion | Qdrant | runtime-configuration / observed | Configured qdrant endpoint; configuration presence does not establish mandatory use in every deployment | ingestion.config |
| R046 | Knoxx ingestion | Ragussy | runtime-configuration / observed | Configured ragussy endpoint; configuration presence does not establish mandatory use in every deployment | ingestion.config |
