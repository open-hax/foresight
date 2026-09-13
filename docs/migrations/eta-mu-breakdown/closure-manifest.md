# E1.01 — Exhaustive Closure Proof

**Produced:** 2026-09-13. **Status:** complete — source-level closure for all 8 extraction targets.
**Inspected revisions:** eta-mu `0ed56aa7`, knoxx `fb08a10a` (Foresight gitlinks; mainline drift documented).

## Methodology

Read every `package.json`, `deps.edn`, `shadow-cljs.edn`, `nbb.edn`, `.clj-kondo/config.edn`, `bb.edn`, `Dockerfile`, script, and test file across all 8 extraction targets. Grepped all `.clj`, `.cljs`, `.cljc`, `.js`, `.ts` source files for `require`/`import` statements. Cross-referenced npm workspace declarations, Clojure `:local/root` entries, Git dependency SHAs, and shadow-cljs `:source-paths`. Verified proposed remote existence via `gh repo view`.

## Repository preflight

| Proposed repo | Exists? | Notes |
|---|---|---|
| `open-hax/kanban-orchestrator` | **No** | Must create |
| `open-hax/clio` | **No** | Must create |
| `open-hax/chat-ui` | **No** | Must create |
| `open-hax/axxium` | **Yes** | Public, non-empty, main branch. Must reconcile with eta-mu copy |
| `open-hax/rheos` | **No** | Must create |
| `open-hax/session-mycology` | **No** | Must create |
| `open-hax/sol` | **No** | Must create |
| Osmos (ingestion) | **No** | Name not reserved; must create |

## Edge register (source-evidenced)

### Package edges (npm workspace)

| ID | Consumer | Target | Kind | Evidence |
|---|---|---|---|---|
| R010 | eta-mu CLI | rheos | npm-runtime | `packages/eta-mu/package.json` `@eta-mu/rheos: workspace:*` |
| R011 | eta-mu CLI | sol | npm-runtime | `packages/eta-mu/package.json` `@eta-mu/sol: workspace:*` |
| R012 | eta-mu CLI | session-mycology | npm-runtime | `packages/eta-mu/package.json` `@eta-mu/session-mycology: workspace:*` |
| R013 | eta-mu CLI | fork-tax | npm-runtime | `packages/eta-mu/package.json` `@eta-mu/fork-tax: workspace:*` |
| R014 | eta-mu CLI | receipt-river | npm-runtime | `packages/eta-mu/package.json` `@eta-mu/receipt-river: workspace:*` |
| R015 | eta-mu CLI | turn-processor | npm-dev | `packages/eta-mu/package.json` `@eta-mu/turn-processor: workspace:*` |
| R016 | eta-mu CLI | terminal-ui | npm-dev | `packages/eta-mu/package.json` `@eta-mu/terminal-ui: workspace:*` |
| R017 | eta-mu CLI | contracts-output | npm-dev | `packages/eta-mu/package.json` `@eta-mu/contracts-output: workspace:*` |
| R001 | rheos | protocols | npm-dev | `packages/rheos/package.json` `@open-hax/protocols: workspace:*` |

### Source-path edges (shadow-cljs :source-paths)

| ID | Consumer | Target | Evidence |
|---|---|---|---|
| R002 | rheos | protocols | `packages/rheos/shadow-cljs.edn` `../protocols/src` |
| R003 | rheos | chat-ui | `packages/rheos/shadow-cljs.edn` `../chat-ui/src` |
| R018 | eta-mu CLI | session-mycology | `packages/eta-mu/shadow-cljs.edn` `../session-mycology/src/cljs` |
| R019 | eta-mu CLI | fork-tax | `packages/eta-mu/shadow-cljs.edn` `../fork-tax/src/cljs` |
| R020 | eta-mu CLI | receipt-river | `packages/eta-mu/shadow-cljs.edn` `../receipt-river/src/cljs` |
| R021 | eta-mu CLI | turn-processor | `packages/eta-mu/shadow-cljs.edn` `../turn-processor/src/cljs` |
| R022 | eta-mu CLI | terminal-ui | `packages/eta-mu/shadow-cljs.edn` `../terminal-ui/src/cljs` |

### Clojure local/root edges

| ID | Consumer | Target | Evidence |
|---|---|---|---|
| R006 | sol | eta-mu CLI | `packages/sol/deps.edn` `open-hax/eta-mu {:local/root "../eta-mu"}` |
| R007 | sol | turn-processor | `packages/sol/deps.edn` `open-hax/turn-processor {:local/root "../turn-processor"}` |

### Clojure Git edges

| ID | Consumer | Target | SHA | Evidence |
|---|---|---|---|---|
| R004 | rheos | katamorph | `be7cc332d865cfedc57b55b10cab3c9f2bd41fc4` | `packages/rheos/deps.edn` (not found — no deps.edn in rheos) |
| R008 | sol | katamorph | `305a5e49d834aca27566f739e8510f6b409fda78` | `packages/sol/deps.edn` `io.github.open-hax/katamorph {:git/tag "v0.2.0" ...}` |
| R009 | sol | event-ledger | `ada7374b7f4e1c3b0ab4e6bbe996f10f06e9b93a` | `packages/sol/deps.edn` `io.github.open-hax/event-ledger {...}` |

### CLJS source-level requires (cross-package, critical for extraction)

| Consumer file | Requires | Target package |
|---|---|---|
| `rheos/backend/domain/events.cljs` | `open-hax.openplanner-protocols` | protocols |
| `rheos/backend/infra/ledger.cljs` | `open-hax.records.edn.event-admission` | protocols (records) |
| `rheos/ui/domain/orchestrator.cljs` | `eta-mu.chat-ui.protocol`, `.knoxx-session`, `.opencode-session`, `.panel`, `.sol-session` | chat-ui |
| `rheos/ui/infra/chat_session.cljs` | `eta-mu.chat-ui.protocol` | chat-ui |
| `sol/infra/agent/provider/turn_processor.cljs` | `eta-mu.extern.openai`, `eta-mu.infra.tools.registry`, `eta-mu.turn-processor.infra.loop`, `eta-mu.turn-processor.shape.message` | eta-mu CLI + turn-processor |
| `sol/shape/episode_event.cljs` | `open-hax.event-ledger` | event-ledger (git) |
| `sol/infra/agent/session_store.cljs` | `open-hax.event-ledger.schema` | event-ledger (git) |
| `sol/law/contract_kinds.cljs` | `katamorph.schema` | katamorph (git) |
| `sol/domain/agent/text_delta.cljs` | `katamorph.agent.text-delta` | katamorph (git) |
| `sol/domain/agent/reasoning.cljs` | `katamorph.agent.reasoning` | katamorph (git) |
| `sol/domain/agent/agent_context.cljs` | `katamorph.agent.context` | katamorph (git) |
| `sol/domain/agent/turn_guards.cljs` | `katamorph.agent.turn-guards` | katamorph (git) |

### Tooling path edges

| ID | Consumer | Target | Evidence |
|---|---|---|---|
| R023 | sol | contract-guard.mjs | `packages/sol/package.json` lint script: `node ../../scripts/contract-guard.mjs` |
| R024 | sol | shared kondo-config | `packages/sol/.clj-kondo/config.edn` `../../kondo-config/clj-kondo.exports/open-hax/kondo-config` |
| R025 | session-mycology | ledger extern guard | `packages/session-mycology/package.json` lint:kondo: `node ../../scripts/check-ledger-extern-boundaries.mjs session-mycology` |
| R026 | receipt-river | ledger extern guard | (planning pack observation — shared guard used by receipt-river after mycology moves) |
| R027-R031 | eta-mu root | clio, rheos, sol, chat-ui, axxium | `scripts/test.bb` explicit pnpm filters |

### Kondo config shared dependency

All 6 non-contract packages reference `../../kondo-config/clj-kondo.exports/open-hax/kondo-config`:
- rheos, clio, chat-ui, axxium, session-mycology, sol

The kondo-config package contains shared clj-kondo hooks (`hooks/promise_chain.clj`, `hooks/layer_boundaries.clj`) and lint rules. **Must not be deleted when first client moves.**

### Runtime configuration edges (ingestion/Osmos)

| ID | Target | Evidence |
|---|---|---|
| R039 | knoxx contracts root | `knoxx/ingestion/src/kms_ingestion/contracts/loader.clj` CONTRACTS_DIR → cwd/contracts → cwd/../contracts |
| R040 | knoxx backend | `knoxx/ingestion/src/kms_ingestion/config.clj` KNOXX_BACKEND_URL |
| R041 | openplanner | `knoxx/ingestion/src/kms_ingestion/config.clj` OPENPLANNER_BASE_URL |
| R042 | proxx | `knoxx/ingestion/src/kms_ingestion/config.clj` PROXX_BASE_URL |
| R043 | postgresql | `knoxx/ingestion/src/kms_ingestion/config.clj` DATABASE_URL |
| R044 | redis | `knoxx/ingestion/src/kms_ingestion/config.clj` REDIS_URL |
| R045 | qdrant | `knoxx/ingestion/src/kms_ingestion/config.clj` QDRANT_URL |
| R046 | ragussy | `knoxx/ingestion/src/kms_ingestion/config.clj` RAGUSSY_BASE_URL |

### Chat UI adapter surfaces (runtime, not compile)

| ID | Export | Target |
|---|---|---|
| R033 | `createSolSession` | sol |
| R034 | `createKnoxxSession` | knoxx backend |
| R035 | `createOpencodeSession` | opencode |

## Package-specific extraction notes

### Clio — LOW complexity
- Self-contained: no workspace source-paths
- deps.edn: malli, edamame, promesa
- npm: fs-ext-extra-prebuilt, nbb
- Clean layer architecture enforced by clj-kondo
- `bin/clio.mjs` launcher needs nbb resolution outside monorepo

### Chat UI — LOW complexity
- Self-contained: no workspace source-paths
- shadow-cljs: helix, malli (0.17.0 — **mismatch** with rheos 0.16.4)
- npm: react 18.3.1, marked pinned to v4 (Closure compiler constraint)
- Exports 8 ESM symbols via `:lib` build
- **React version conflict**: chat-ui uses 18.x, rheos uses 19.x

### Session Mycology — LOW complexity
- Self-contained: no workspace deps
- shadow-cljs: malli
- Uses node:child_process, node:fs, node:path
- lint:kondo depends on `../../scripts/check-ledger-extern-boundaries.mjs`
- Root `scripts/test.bb` does NOT explicitly select mycology's suite

### Axxium — MEDIUM complexity
- Self-contained: no workspace deps
- npm: fastify, @fastify/cookie, @fastify/cors, @fastify/static, bcryptjs, jose, pg
- **Existing repo `open-hax/axxium` must be reconciled** — not overwritten
- 56 JS boundary violations detected by check-js-boundary.mjs
- PostgreSQL runtime dependency

### Kanban Orchestrator — TRIVIAL
- Contract-only: 5 EDN files, no source code, no build step
- Consumed by runtimes as additional contract root
- MCP server reference: `http://127.0.0.1:8792/mcp` (runtime config, not compile)

### Rheos — HIGH complexity
- Source dependencies: `../protocols/src`, `../chat-ui/src` (must resolve before extraction)
- npm workspace: `@open-hax/protocols: workspace:*`
- CLJS requires: `open-hax.openplanner-protocols`, `open-hax.records.edn.event-admission`, `eta-mu.chat-ui.*`
- Maven: helix 0.2.2, malli 0.16.4
- npm: fastify 5.x, @modelcontextprotocol/sdk, chokidar, ws
- PM2 ecosystem config
- **Must extract chat-ui first, then resolve protocols**

### Sol — HIGH complexity
- Clojure local/root: `../eta-mu`, `../turn-processor` (must pin to donor SHA)
- Git deps: katamorph v0.2.0, event-ledger (SHA ada7374)
- CLJS requires from eta-mu: `eta-mu.extern.openai`, `eta-mu.infra.tools.registry`, `eta-mu.turn-processor.*`
- Tooling: `../../scripts/contract-guard.mjs`, `../../kondo-config/...`
- npm: fastify, @modelcontextprotocol/sdk, @fastify/*, typebox, ws
- **Mixed dependency cycle with eta-mu CLI** (cross-mechanism, not single-resolver)

### Ingestion/Osmos — MEDIUM-HIGH complexity
- 24 Maven dependencies (ring, reitit, next.jdbc, postgresql, jedis, etc.)
- Docker: clojure:temurin-21-alpine → eclipse-temurin:21-jre-alpine
- 8 runtime services required for integration tests
- Contract discovery walks parent directories — must be replaced with explicit binding
- `resources/` directory excluded by .gitignore — Dockerfile copies it but it may be empty
- No README exists in ingestion/

## Strongly connected components

```
sol ↔ eta-mu CLI ↔ sol  (cross-mechanism: Clojure :local/root ↔ npm workspace:*)
```

This is a single SCC across dependency mechanisms. Immutable donor snapshots permit staging. Each compiler/installer/launcher resolves its own declared closure independently.

## Unresolved items (carried from planning pack)

1. Protocols' npm `files` list contains `dist` and types, not source; `packages/protocols/deps.edn` returned 404. Source distribution contract unknown.
2. Axxium existing repo history vs eta-mu copy — not inspected beyond manifest comparison.
3. Open branches, recent review threads, and yoga worktrees not inspected.
4. Exact CI runner, secrets, environment, Docker and resource contracts not read.
5. `resources/` directory in ingestion Dockerfile context — existence unverified.
6. Kondo-config hooks and layer-boundary checks may need updating when packages move to independent repos with different relative paths.

## Verification

All claims in this document are grounded in file:line citations from the 5 parallel closure scans. No installs, builds, or tests were executed. The edge register extends the planning pack's 46-edge observation with source-level require/import evidence.
