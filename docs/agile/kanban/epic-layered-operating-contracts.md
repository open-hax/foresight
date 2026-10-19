---
category: "kanban"
labels: "contracts, muse, katamorph, alpha, harness, context-budget, epic"
type: "epic"
points: "46"
title: "EPIC: One layered operating contract, folded up the directory tree to ~/.ημ and projected into every harness"
priority: "P1"
status: "incoming"
uuid: "layered-operating-contracts"
created_at: "2026-10-19T21:05:27.000Z"
---

# EPIC: One layered operating contract, folded up the directory tree to ~/.ημ and projected into every harness

> Design: [`docs/notes/design/layered-operating-contracts.md`](../../notes/design/layered-operating-contracts.md)
> Research: [`docs/research/layered-operating-contracts-inventory.md`](../../research/layered-operating-contracts-inventory.md)

## Outcome

- Every harness on the machine runs under one constitution. Today Claude Code,
  OpenCode and Codex follow different ones.
- The constitution is written once as identified fragments, folded through
  `.ημ/` layers up the directory tree to `~/.ημ`, and projected into each
  harness within a declared budget.
- Contract-0, the current muse operating contract, still builds byte-identical
  artifacts.
- No hand-made copies of the contract remain in harness instruction files.

## Context

- 86 files carry contract text in two drifting lineages.
- A Claude Code session started in `~` loads the same contract twice (about
  24.6 KB), and every Claude Code session carries the 12.3 KB hand-made copy.
- OpenCode loads the older `ημΠ.dev` lineage through an `instructions` glob.
- Muse merges settings within one tree but folds nothing across directories.
- The user reports that 44% of recent usage went to always-on context over 40k
  tokens.

The full evidence is in the research note.

## Children

Stories are listed in dependency order within each phase.

**Preserve**
- LOC.01 `loc-contract-0-freeze`: name, hash and freeze contract-0 (open question 0).
- LOC.02 `loc-contract-0-golden-artifacts`: golden test of today's artifacts. Depends on LOC.01.

**Source**
- LOC.03 `loc-fragment-source`: merge both lineages into identified tiered fragments (open question 7).
- LOC.14 `loc-absolutes-to-gates`: map every absolute to a PR gate or a recorded gap. Depends on LOC.03.
- LOC.15 `loc-retire-legacy-principle-bootstrap`: stop the pi-era `.ημ/PRINCIPLE.edn` bootstrap.

**Laws**
- LOC.04 `loc-contract-shapes`: Malli shapes for layers, fragments, effective contracts and manifests (open question 6).
- LOC.05 `loc-fold-laws`: pure fold with one merge rule per kind of declaration. Depends on LOC.04.
- LOC.06 `loc-layer-chain`: layer-chain discovery and well-formedness. Depends on LOC.04.

**Projection**
- LOC.07 `loc-harness-profiles`: harness capability profiles and budgets, and who performs the fold (open question 1).
- LOC.08 `loc-instruction-emitter`: muse `:publish {:instructions …}` with manifest. Depends on LOC.02, LOC.05 and LOC.07.
- LOC.09 `loc-contract-0-module`: contract-0 selectable as a module and golden-equal. Depends on LOC.02, LOC.05 and LOC.08.
- LOC.13 `loc-published-agents-md`: standalone published `AGENTS.md` projection (open question 2). Depends on LOC.08.

**Cutover**
- LOC.10 `loc-cutover-claude-code`: replace the Claude Code hand copies and remove the double load. Depends on LOC.03 and LOC.08.
- LOC.11 `loc-cutover-opencode-codex`: replace the OpenCode glob and the Codex copy. Depends on LOC.03 and LOC.08.
- LOC.12 `loc-daemon-chains-ownership`: daemon layer fan-out and stable/dev ownership (open question 4). Depends on LOC.06.

## Open questions and the slices they block

Each design open question blocks only the stories its answer could change.

| Question | Blocks |
|---|---|
| 0: which tree is contract-0 | LOC.01, then LOC.02 and LOC.09 |
| 1: who performs the fold | LOC.07, then LOC.08, LOC.10 and LOC.11 |
| 2: shape of the published `AGENTS.md` | LOC.13 |
| 3: where named modules are resolved from | LOC.06 and LOC.09 |
| 4: who owns rendering | LOC.12 |
| 5: retiring legacy bootstraps | LOC.15 (the proposal already answers it) |
| 6: where the language's semantics live | LOC.04, LOC.05 and LOC.06 |
| 7: where the root layer is versioned | LOC.03 |

## Definition of done

- [ ] Every child story is `done` through Rheos.
- [ ] Re-running the research-note inventory finds no hand-made contract copies
  in harness instruction files. Legacy copies may remain, but nothing generates
  new ones.
- [ ] Claude Code, OpenCode and Codex each load exactly one projection of the
  same effective contract, within their declared budgets, and each projection
  has a manifest referenced by a receipt.
- [ ] The contract-0 golden test passes through the layered compiler.

## Out of scope

- Response-AST enforcement (the second half of the 2026-05-08 design note).
- Deleting legacy copies.
- Rheos semantics.
- Rewriting plugins.

## Verification state

These are proposed Markdown inputs. Creating them claims no Rheos transition,
board validation or implementation.
