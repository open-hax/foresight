---
title: "Layered operating contracts: muse as a contract language"
summary: "Treat muse as a language for operating contracts, fold contract layers up the directory tree to ~/.ημ, preserve the first contract (contract-0), and project the folded contract into each harness within a budget."
category: "design"
created: "2026-10-19"
status: "draft"
---

# Layered operating contracts: muse as a contract language

## Summary

- Muse is a **language** for operating contracts, not one fixed bundle of plugins.
- Every `.ημ/` directory is a **layer**. A layer selects contracts, adds
  fragments, drops fragments and overrides fragments.
- The **effective contract** at a path is the fold of every layer from `~/.ημ/`
  down to the nearest `.ημ/`.
- Each harness receives a **projection** of the effective contract. The
  projection depends on what the harness can carry: inline prose, on-demand
  skills, hooks, PR gates, or nothing.
- The current `muse/.ημ/config` tree is **contract-0**, the first operating
  contract. It becomes a named, hashed module that any layer can select. A
  golden test proves that it still compiles to byte-identical artifacts.
- Absolutes are enforced as workflow gates on pull requests. Their prose copy
  shrinks to one line.

The current token cost of agent sessions is a symptom of the missing fold.
Contract text has no identity, so nothing can recognise a duplicate. See
[the research note](../../research/layered-operating-contracts-inventory.md).

## Problem

| Observation | Consequence |
|---|---|
| 86 contract-bearing files in two lineages (`ημΠ.dev` prompt form; `~/.agents` contract form) | Drift in both directions; no canonical source |
| A Claude Code session started in `~` loads the same lineage-B contract twice (`~/.claude/CLAUDE.md` and `~/AGENTS.md`, about 24.6 KB). Every Claude Code session carries the 12.3 KB `~/.claude/CLAUDE.md`. | Always-on context cost |
| OpenCode loads lineage A through `~/.ημ/config/opencode/settings/core.edn` `:instructions` | Different harnesses run under different constitutions |
| `deep-merge` exists only across imports inside one tree (`muse/src/cljs/eta_mu/opencode/settings.cljc`) | No layering across directories |
| Stable muse (`riatzukiza/muse`) and dev muse (`octave-commons/muse`) carry diverged `.ημ/config` trees | Contract-0 is ambiguous until it is named |

## Model

### Three things that live in muse today

1. **The language and compiler**: `src/`, which holds the DSL, normalisation,
   profiles, merge and per-target backends.
2. **The standard library**: `.ημ/plugins/`, which holds actors, receipt-river,
   session-mycology, websearch, apifany, kanban-gate and the others.
3. **One program written in the language**: `.ημ/config/`, which selects those
   plugins with those profiles for those targets. This is contract-0.

Muse becomes a language when these three are separated. The plugins become a
library that contracts import. Contract-0 becomes one module among future
modules.

### Ownership across the constellation

The repository map in Foresight `AGENTS.md` already assigns roles that this
split has to respect. The map gives routing hints, not grants of ownership.

| Concern | Declared home | Proposed placement |
|---|---|---|
| Contract shapes, fragment identity, fold and merge laws (pure) | `katamorph` (contract-language) | Malli shapes and fold laws as `.cljc` |
| Well-formedness of a contract or layer before use | `alpha` (structural-integrity) | Validation of the layer chain and fragments |
| Projection to harness artifacts and backends | `muse` (compatibility-compiler) | Projection backends, publication, golden test of contract-0 |
| Contract source text (the constitution) | not yet assigned | `~/.ημ/contract/` root layer and `~/.ημ/contracts/` library |
| Board state for this work | `rheos` | Cards under `docs/agile/kanban` |

Read this way, "muse as a language" means that the language's semantics live
in katamorph and alpha as portable laws, and muse is the compiler that
implements them for each harness. Open question 6 asks whether that reading
matches the intent.

### Layers

- A layer is a `.ημ/` directory. Its contract declaration lives in
  `.ημ/contract/layer.edn`; its own fragments live in `.ημ/contract/fragments/`.
- Layers are ordered by depth. One example chain is `~/.ημ` → `~/spaces/.ημ` →
  `~/spaces/foresight/.ημ` → `~/spaces/foresight/muse/.ημ`.
- A layer may declare `:layer/root true`, which stops the upward walk in the
  same way as EditorConfig's `root = true`. A repository published for outside
  adoption uses this so that its contract does not depend on a private home
  directory.

```clojure
;; ~/.ημ/contract/layer.edn — root layer
{:layer/id :home
 :use      [:opmf/core :opmf/output :opmf/calibration]
 :targets  {:claude   {:inline #{:tier/0} :budget 2500}
            :opencode {:inline #{:tier/0 :tier/1} :budget 4000}
            :codex    {:inline #{:tier/0} :budget 2500}}}

;; ~/spaces/foresight/.ημ/contract/layer.edn
{:layer/id :foresight
 :use  [:foresight/law]
 :drop [:opmf/semantics]}

;; ~/spaces/muse/.ημ/contract/layer.edn
{:layer/id :muse
 :use  [:muse/contract-0]}
```

### Fragments

A fragment keeps the readable S-expression form required by
`eta-mu/docs/notes/design/prompt-contract-source-language.md`. That note says
the contract is the source language and must not become a map blob.

```clojure
(fragment :opmf/directives
  (tier 0)
  (absolute)
  (gate :pr-flow/autonomy)
  (directives
    "Autonomy: offer options when meaningful; never coerce; label uncertainty."
    "Always ask questions: when a decision is needed by the user, use the question tool."))
```

Each fragment has a stable id, a tier, and optionally an `absolute` marker and
a `gate`. The compiler records each fragment's content hash.

### The fold: one merge rule per kind of declaration

| Kind | Fold rule |
|---|---|
| settings | `deep-merge`. Maps merge, vectors concatenate in layer order, scalars take the nearest layer. This rule exists today. |
| instruction fragments | Ordered union keyed by fragment id. The same id resolves to the nearest layer only when that layer marks it `:override`. An unmarked id collision is a compile error. Text is never merged by similarity (ledger discipline rule 4). |
| absolutes | A layer below the one that declares an absolute may not drop or loosen it. Each absolute compiles to a PR gate, and its prose shrinks to one line. |
| permissions | A child layer may only tighten. |
| tools and hooks | Union, then the profile filter (`dsl/profile.cljc`). |
| modes and participants | Keyed merge, following the v2 shape in `~/.ημ/opmf/ημΠ.dev.v2/`. |

The fold is a pure `.cljc` function from a layer chain to an effective
contract. It obeys the foresight mandate: purify before you port.

### Projection

A projection maps the effective contract and a target profile to that
harness's artifacts.

- Tier-0 fragments are inlined within the target budget. Exceeding the budget
  is a compile error, never a silent truncation.
- Tier-1 fragments become on-demand material: skills, pointer files or MCP
  resources, according to the target's capabilities.
- Absolutes become gates. The prompt keeps a one-line statement of each absolute
  together with the gate that enforces it.
- Unsupported declarations are reported, not dropped silently.
- Every projection writes a manifest: the layer chain, fragment ids and hashes,
  the target, the budget and the actual size. A receipt references that
  manifest.

The existing publication mechanism extends naturally. Muse already publishes
Markdown through `emit-agents!` (`muse/src/clj/eta_mu/opencode/build.clj`) and
`:publish {:mcp-config …}` (`claude/build.clj`). A sibling `:publish
{:instructions …}` emits the projection.

## Contract-0 preservation

1. Name the tree: `:muse/contract-0`, version 1, with a content hash over
   `.ημ/config/**` and `.ημ/plugins/**`.
2. Decide which tree is contract-0 before freezing it. This is open question 0.
3. Add a golden test. It builds contract-0 alone through the layered compiler and
   compares the outputs against today's artifacts byte for byte: the plugin
   bundle, `.mcp.json`, `.claude/settings.json` hooks, OpenCode permissions and
   agents.
4. Contract-0 may evolve only as `:muse/contract-0` version 2 or later. Earlier
   versions remain selectable.

## Open questions

0. **Which tree is contract-0?** Candidates are stable `spaces/muse`
   (`riatzukiza/muse`, the running daemon) and dev `foresight/muse`
   (`octave-commons/muse`). They differ in plugins (`review-pipeline`), agents
   (`github-reviewer`), profiles and three plugin sources. Provisional
   recommendation: the tree that the running daemon builds, because it is the
   contract that actually operates today.
1. **Who performs the fold?**
   - A: each layer emits only its delta, and the harness composes through its
     native directory-walk loading.
   - B: muse folds once per leaf and emits one file per harness, and the
     harness's own ancestor loading is excluded where the harness supports
     exclusion.
   - Hybrid: B where exclusion or injection exists, A otherwise.

   Option A cannot enforce the absolute or tighten-only rules. The decision
   depends on the harness loading semantics in the research note.
2. **What shape does the published `AGENTS.md` take?** An outside adopter has no
   `~/.ημ`, so the published file must stand alone. On this machine the same
   file duplicates its ancestor layers. Proposal: publish tier-0 plus
   `contract-id@hash` pointers, and let local harnesses receive the fold from
   muse.
3. **Where are named contract modules resolved from?** Proposal: the layer chain
   first (nearest first), then a library directory at `~/.ημ/contracts/`.
4. **Who owns rendering?** A change in `~/.ημ` re-renders every descendant tree.
   The stable and dev daemons need an explicit ownership rule, so that the
   stable daemon never builds and globally publishes a dev tree.
5. **How do the 57 legacy `.ημ/PRINCIPLE.edn` bootstraps retire?** Stop
   generating them, and leave existing copies in place until the
   preservation rule in `spaces/AGENTS.md` permits removal.
6. **Where do the language's semantics live?** The repository map names
   katamorph as the contract language and muse as the compatibility compiler.
   Provisional recommendation: put the fold and merge laws and the fragment
   shapes in katamorph, keep well-formedness checks in alpha, and keep the
   projection backends in muse. The alternative is to amend the repository
   map so that muse owns the language.
7. **Where is the root layer versioned?** `~/.ημ` lies inside the home
   superproject, but its whitelist `.gitignore` tracks none of it. The root
   layer is the constitution, so it needs history and provenance. Options:
   - track `~/.ημ/contract/` in the home superproject by adding a whitelist
     entry;
   - keep the source in `~/.agents` (`.ημ/contract/`) and point the root layer
     at it;
   - give the contract its own repository.

## Risks

- **Silent behaviour change.** Moving a rule from tier 0 to tier 1 can stop
  agents from following it. Mitigation: absolutes stay in tier 0 as one line
  each and are enforced by gates, and each cutover records sessions before and
  after.
- **Shadowing becomes the new drift.** Mitigation: an unmarked fragment-id
  collision is a compile error, and `:override` is explicit and recorded in the
  projection manifest.
- **Daemon crossover.** Layer fan-out could make the stable daemon build and
  publish a dev tree. Mitigation: the ownership rule in open question 4 lands
  before the fan-out does.
- **Contract-0 regression.** Mitigation: the golden test lands before any
  layered compiler code.

## Verification

- Contract-0 golden test: byte-identical artifacts.
- Property tests for the fold: associativity of the layer chain, idempotence
  of re-folding an effective contract, absolutes never removed, permissions
  never loosened.
- For each harness, always-on instruction bytes are measured before and after
  cutover, and each projection manifest is checked against the target budget.
- The inventory command from the research note is re-run after cutover. No new
  hand-made contract copies may appear.

## Board breakdown

The epic `layered-operating-contracts` in `docs/agile/kanban` implements this
design. Its stories `LOC.01` to `LOC.15` follow the phases below and name the
open question that blocks each one. Under the unblocked-slice rule, an open
question blocks only the stories that its answer could change.

| Phase | Stories |
|---|---|
| Preserve | LOC.01 freeze contract-0, LOC.02 golden artifacts |
| Source | LOC.03 fragment source, LOC.14 absolutes to gates, LOC.15 retire the legacy bootstrap |
| Laws | LOC.04 shapes, LOC.05 fold laws, LOC.06 layer chain and well-formedness |
| Projection | LOC.07 harness profiles, LOC.08 instruction emitter, LOC.09 contract-0 as a module, LOC.13 published `AGENTS.md` |
| Cutover | LOC.10 Claude Code, LOC.11 OpenCode and Codex, LOC.12 daemon chains and ownership |

## Non-goals

- The enforcement half of the 2026-05-08 design note, which parses responses
  into a semantic AST, is separate work.
- Rewriting existing plugins.
- Changing Rheos semantics. Board behaviour stays in Rheos.
- Deleting legacy contract copies.

## Prior art

- **Nix module system:** typed options carry their own merge functions, and
  priority overrides use `mkForce` and `mkDefault`. This is the closest analogue
  to merge rules that vary by kind of declaration.
- **EditorConfig:** an upward directory walk stopped by `root = true`.
- **Git config scopes:** system, global, local and worktree scopes, with
  conditional includes.

The research note cites these.

## Related

- `eta-mu/docs/notes/design/prompt-contract-source-language.md` (2026-05-08).
- `~/.ημ/opmf/ημΠ.dev.v2/` holds the v2 modes and participants shape.
- `muse/examples/settings-merge/{global,project}.edn` illustrates the intended
  cross-layer settings merge.
- Research: [layered operating contracts inventory](../../research/layered-operating-contracts-inventory.md).
- Epic: see `docs/agile/kanban/` (uuid listed in the research note and PR).
