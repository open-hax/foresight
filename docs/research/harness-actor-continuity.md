# Harness tools and Knoxx actor continuity

- Status: observed inventory and provisional design inputs, 2026-10-22.
- Implements no live registration or memory promotion.
- Parent: [merged PR 141](https://github.com/open-hax/foresight/pull/141).

## PR 141 review outcome

GitHub reports merged by riatzukiza at 2026-10-20T06:00:28Z, merge commit
cffc34448aa6807fc51135d7f90f0f9fe6ba7efe, head c206b937fb070bfdba324fa5a5c0722178ee6cc8.
All 37 conversations are resolved; four deterministic/workflow checks passed.
MiMo approved that head with no confirmed findings. Its remaining note asks for
live verification of Claude's proposed InstructionsLoaded event before LOC.10.
CodeRabbit's SUCCESS check says Review paused; its latest request returned a
quota response. The current pr-flow collector still blocks convergence with
zero counted rounds. Therefore merged status is observed, while full planning
convergence is unproven. Do not reopen or rewrite the historical PR.

The pr-review-settlement skill says a push invalidates approvals but preserves
completed stage history. Earlier conversation claims that pushes reset all
round history were wrong; do not use them as policy.

## Existing tool compiler

Observed in development muse: src/cljs/eta_mu/boundaries/mcp.cljs accepts the
compiled adapter and serves tools over stdio to MCP clients including Codex.
The .ημ/config/mcp/root.edn imports shared profiles and plugin exposures.
This already supplies the compiler seam. A dedicated Codex registration and
capability projection remains to be qualified. A .mcp.json output is not proof
that Codex registered it; ~/.codex/config.toml uses mcp_servers entries.
MCP handler context currently uses process cwd for directory/worktree. That
cannot authenticate a Codex chat, task, subagent or remote actor. MCP also lacks
the host hook lifecycle used by OpenCode/Claude adapters.

Codex in this chat exposes spawn/wait and task messaging interfaces. These are
observed capabilities of this desktop context; CLI/SDK availability is unknown
until each adapter is probed. An API task id is a source identity, not an
independently authenticated Knoxx actor or a second reviewer principal.

## Drive source register

Read through the authorized Drive connector on 2026-10-22. These documents are
private source material; the following is a minimal architecture synthesis,
not a copy of customer or personal material. Original bytes remain in Drive. Direct source URLs are omitted here; recover
the named sources through the authorized connector. This edit neither changes
Drive sharing permissions nor claims independent verification of them.

- Knoxx / Foresight folder (available through the authorized Drive connector).
- Semi-permeable memory: each harness is a node with its own actor; local private memory and shared identity; scheduled versus event-triggered lift remains open.
- Annotated group identity successor v0.2: private, team and organization projections require authority, provenance, revocation and explicit promotion. Group identity is an architecture metaphor.
- ChatGPT handoff v0.2: preserve predecessors and dissent; qualify assemblies with versioned evidence. Commercial and customer details are outside this plan.

## Conversation recovery

The recent ChatGPT chat Google Drive Access (6ad930a6-0f3c-83ea-941d-bbb495313176)
contains the user's request for specs and archaeology: recover original human
source as reference that can periodically enter agent context. This supports
Epiphany source recovery, with source identity and epistemic status preserved.
It does not establish that this is the exact conversation intended by the
current reference. The October 21 ChatGPT-authored Drive successor is a second,
explicitly dated source for the direction. No claim of exhaustive chat search.

## Open observations

Orbit checkout exists at /home/err/spaces/orbitdesktop and documents a native
Hermes plugin with workspace, resources and workbench tool boundaries. Its
running status needs a separate runtime probe; a checkout is not deployment.
Knoxx PM2 process names include stopped backend/ingestion processes and online
shadow/frontend processes in legacy devel paths. These names do not prove API
health or remote identity federation. Keep runtime identity, endpoints and
credentials out of source manifests until their owner and audience are known.

## Runtime probe follow-up

A separate read-only inspection on 2026-10-22 found no active local Orbit or
Hermes process/container. Orbit HEAD is 9c4069a002c1d9376d9ae06776d4090aad49fc00,
package 0.3.1. Hermes checkout is d0288be5b3330d2442e3907185b8e9d0958297bb,
matching Orbit contracts/hermes-runtime-contract.json. The Hermes lifecycle
record reports a graceful exit at 2026-10-22T04:25:16Z. GET of the default Orbit
health endpoint 127.0.0.1:4318/api/health received no HTTP response. Alternate
addresses/profiles remain unknown. No restart was performed.

Orbit's hermes-plugin exposes orbit_workspace, orbit_resources and
orbit_workbench. The runtime contract describes submission, status, events,
stop and capability discovery, with private authenticated native-task access.
The ~/.hermes/plugins directory is empty, which does not exclude another
profile's loader. Runtime ownership and actual listener addresses must be
established before activation; source capabilities do not prove a live Knoxx
bridge. Historical source tests are not current deployment evidence.
