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
