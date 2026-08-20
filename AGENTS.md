# Foresight Workspace

## Boundary

Treat direct `.gitmodules` entries as independently owned repositories. Do not
rewrite a submodule's package-manager policy, recurse into nested packages, or
modify unrelated submodule dirt while changing root orchestration.

`.agents/` and `eta/` are declared consolidation inputs curated by this root;
`.agents/` retains its independent nested Git ownership while `eta/` is
root-owned.
Inventory them without following nested Git repositories, skills, symlinks, or
package manifests. Their presence in inventory does not grant execution
authority; compatibility originals may remain in their existing locations.

## Repository map: where to look

`src/foresight/project.cljc` (`sources`) is the source of truth for the current
declared inventory metadata behind this table: source identity, path,
repository, and recorded `:source/role`. It is **not** by itself promotion or
ownership authority for common Foresight law. Repository roles and the "Look
here for" column are routing hints for where to inspect evidence first, not a
grant that the child owns every similarly named concept across the constellation.

When this table and the checkout disagree, treat that as project-model drift.
When a routing hint and a child repository's own `AGENTS.md`, `README.md`,
architecture records, or current code disagree, the child evidence controls the
local fact and the Foresight declaration should be reconciled explicitly.
Cross-repository promotion remains subject to the distinction recorded in
`docs/notes/project-law-promotion-status-triage.md`: recovered claims, lift
candidates, and accepted lifts are not interchangeable.

| Path | Repository | Role | Look here for |
| --- | --- | --- | --- |
| `Truth` | octave-commons/Truth | simulation-research | ECS simulation substrate, pure domain systems/phases, single-writer components |
| `bitch-tracker` | octave-commons/bitch-tracker | betterdiscord-plugin | BetterDiscord client plugin behavior |
| `calliope` | octave-commons/calliope | corpus | Append-only ingestion truth, Receipt River accountability, corpus documents |
| `epiphany` | octave-commons/epiphany | knowledge-archaeology | Observed→derived→provisional→accepted promotion, git-history-derived knowledge |
| `eta-mu` | open-hax/eta-mu | agent-runtime-and-workflow | Kanban board (work source of truth), Rheos FSM board transitions, agent runtime |
| `katamorph` | open-hax/katamorph | contract-language | Portable shape/contract declarations and cross-host translation |
| `knoxx` | open-hax/knoxx | agent-product-runtime | CLJS-first agent product backend; raw JS interop confined to externs |
| `muse` | octave-commons/muse | compatibility-compiler | Compiler/compatibility tooling — not canonical actor/session/policy semantics |
| `opencode` | open-hax/opencode | coding-agent-host | Coding-agent hosting/integration (shallow submodule) |
| `proxx` | open-hax/proxx | model-proxy | LLM/model proxying, EDN pricing policy; provider credentials stay local |
| `services` | open-hax/services | deployment-orchestration | Deployment topology and environment schemas — never application source or secrets |
| `uxx` | open-hax/uxx | ui-kit | Canonical React components; Reagent/Helix are parity wrappers, shared design tokens |
| `.agents` | riatzukiza/.agents | skill-catalog | Canonical agent skill catalog (nested Git-owned consolidation input, not actionable here) |
| `eta` | (root-owned) | clojure-harness | Transduction harness code (consolidation input, not a submodule, not the domain model) |
| `alpha` | (root-owned native component) | structural-integrity | Artifact/reaction laws — is a thing well-formed before it is used |

When a new direct repository is added, update `.gitmodules`,
`src/foresight/project.cljc` (`sources`, plus any new invariants), and this
table together — `nbb scripts/project.clj validate` and the
`:foresight/gitmodules-match-project` invariant enforce that the three stay
in agreement. A row here with no matching source is drift, not a new grant of
authority.

## Divine mandate: purify before you port

Foresight is consolidating surviving systems by extracting their durable
semantics, not by copying their runtime baggage. When code can be made portable
with small, obvious edits, portable Clojure data and `.cljc` are the default.

- Shapes, Malli schemas, laws, identity rules, normalization, validation, graph
  algorithms, ledger/event semantics, state-transition logic, and other pure
  functions belong in `.cljc` whenever practical.
- Runtime-specific namespaces are outer adapters. `.cljs`, `.clj`, NBB scripts,
  Node objects, JVM classes, HTTP servers, databases, filesystems, and process
  APIs may depend on the pure layer; the pure layer must not depend inward on
  them.
- Keep data Clojure-shaped at semantic boundaries. Convert native JS objects or
  JVM values at the edge and do not allow them to become domain authority.
- When rescuing code from Knoxx, eta-mu, or another survivor, take shapes, laws,
  and properly pure functions first. If an otherwise-useful function is coupled
  to effects, split the effect from the decision rather than porting the
  coupling.
- Ledgers record immutable facts. Mutable operational state, projections, caches,
  database rows, UI state, and provider responses are never promoted to semantic
  authority merely because they already exist.
- Validate both sides of replaceable boundaries. Provider output is untrusted
  input. Katamorph contracts should be reusable between stages rather than
  restated by each runtime.

### Runtime ladder

Choose the lightest runtime that satisfies the actual capability; runtime choice
must not define the domain model.

1. Prefer **NBB** for Node-adjacent orchestration, tools, agents, filesystem and
   network I/O when its CLJS/SCI surface is sufficient.
2. Prefer **Babashka** for portable Clojure CLI/host scripting that does not need
   Node-specific libraries.
3. Use **JVM Clojure** when JVM libraries, concurrency, performance, long-lived
   services, or operational requirements materially justify the JVM.
4. Use compiled **ClojureScript/shadow-cljs** when the actual target requires a
   compiled JS/browser artifact; do not choose it merely because predecessor
   code already did.

Moving outward or downward on this ladder is an adapter decision, not a rewrite
of shapes and laws. A runtime migration that forces pure semantics to fork is a
signal that the boundary is wrong.

### Working vocabulary

The names are conceptual centers, not mutually exclusive taxonomic prisons:

- **Alpha (α)** — structured resource integrity: repository inputs, schemas,
  deterministic checks, canonical identity, and the question "is the thing we
  are about to use well-formed and internally lawful?"
- **Eta (η)** — transduction: a harness/worker consumes an artifact and produces
  another artifact or representation, commonly through agents and tools.
- **Mu (μ)** — evaluation: compare outcomes with intended outcomes; score, label,
  characterize, correct, approve, reject, or otherwise produce judgments.
- **Big Pi (Π)** — product/representation: content/product organization and the
  consumer-facing representations or publication surfaces derived from it.

Katamorph sits between these stages as reusable shape/contract machinery. A
workflow may compose any of them when the producer's provided shape satisfies
the consumer's required shape; no fixed linear pipeline is assumed.

## Commands

- `nbb scripts/workspace.clj inventory` discovers root manifests and scripts.
- `nbb scripts/workspace.clj report` writes aggregate JSON and Markdown.
- Mutating or executable actions require `--only <paths>` or `--all`.
- `nbb -cp scripts:test test/workspace_test.cljs` runs root unit tests.
- `clj-kondo --lint scripts test` must pass with zero warnings.

Failures, missing tools, unsupported scripts, and ambiguous package managers
must remain visible. Never convert an unavailable action into a pass.

## State

Cards live under `docs/agile/kanban`. Rheos events and receipts belong under
`.ημ/`; no provenance ledger may be created elsewhere.
