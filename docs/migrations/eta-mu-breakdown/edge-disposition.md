# E1.06 — Edge Disposition Register

**Produced:** 2026-09-13. **Status:** complete.

## Edges switched to new repos

| Consumer | Old coordinate | New coordinate | Evidence |
|---|---|---|---|
| rheos → chat-ui (source) | `../chat-ui/src` (sibling) | `deps/chat-ui/src` via bootstrap adapter fetching from `open-hax/chat-ui@8638553` | `rheos/scripts/bootstrap-source-deps.sh`, `rheos/shadow-cljs.edn` |

## Edges retained at donor SHA (targets not yet extracted)

| Consumer | Target | Coordinate | Why retained |
|---|---|---|---|
| rheos → protocols | eta-mu/packages/protocols | Git subdirectory: `github:open-hax/eta-mu#0ed56aa7&path:/packages/protocols` | Protocols not extracted; no new repo |
| rheos → katamorph | open-hax/katamorph | Git tag v0.2.0, SHA `305a5e4` | Already immutable, no change needed |
| sol → eta-mu | eta-mu/packages/eta-mu | Git: `open-hax/eta-mu@0ed56aa7`, deps/root `packages/eta-mu` | eta-mu CLI not extracted yet (E1.07) |
| sol → turn-processor | eta-mu/packages/turn-processor | Git: `open-hax/eta-mu@0ed56aa7`, deps/root `packages/turn-processor` | Turn Processor not extracted |
| sol → katamorph | open-hax/katamorph | Git tag v0.2.0, SHA `305a5e4` | Already immutable |
| sol → event-ledger | open-hax/event-ledger | Git SHA `ada7374` | Already immutable |

## Self-contained packages (no cross-package edges to switch)

| Package | Status |
|---|---|
| clio | Self-contained — no workspace or source-path dependencies |
| chat-ui | Self-contained — no workspace or source-path dependencies |
| session-mycology | Self-contained — no workspace or source-path dependencies |
| kanban-orchestrator | Contract-only — no code dependencies |
| osmos | Self-contained — all deps are external Maven/services |

## Remaining edge: eta-mu CLI (deferred to E1.07)

The eta-mu CLI still uses `github:open-hax/eta-mu#0ed56aa7` for all workspace:* deps (rheos, sol, session-mycology, fork-tax, receipt-river, etc.) and sibling source-paths in shadow-cljs.edn. These will be switched in E1.07 after the new repos are verified.
