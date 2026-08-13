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
