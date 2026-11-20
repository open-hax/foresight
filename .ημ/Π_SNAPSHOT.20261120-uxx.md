# Fork-tax checkpoint: 2026-11-20

This is a partial checkout preservation, not a release or a complete recursive
fork tax. The root and 25 direct submodules were inventoried. Two nested
OpenPlanner submodules were uninitialized.

- `uxx` Firefox source: `9ce274a63da207c36fe704dfc602fb64d0e59e40`, published
  on `origin/device/stealth` and annotated tag
  `Π/checkout/20261120-uxx-9ce274a`; both remote refs were read back at that
  commit. The scoped source snapshot excludes Uxx receipt and reflection edits.
- Verification: Firefox Vitest 16 files, 335 tests passed; Firefox build
  passed. npm reported an existing `always-auth` configuration warning;
  tests emitted scripting-API and CSS parsing warnings. No live browser test.
- Root project validation passed (26 sources, 25 submodules, 40 invariants);
  project tests passed (12 tests, 171 assertions).
- Root `.agents` gitlink was already staged and its publication was not
  established. It remains outside this checkpoint. Root receipt/reflection
  edits and the Knoxx actor resource were not published because their
  operational or identity metadata needs an appropriate publication boundary.
- Review workflow scaffolding, runtime caches, diagnostics, and editor locks
  remain outside this checkpoint. The inspected review workflow passes secrets
  and a write-capable token to a job checking out PR code; it does not meet the
  Foresight candidate-execution security boundary. Some scaffolding may already
  exist on other remote branches; that is not verified here for all children.
- No dedicated secret scanner was available. A bounded pattern scan of the 25
  selected Firefox files returned no matches; it is not a security guarantee.
- Other child worktrees were not committed, tagged, or pushed in this run.
  Detached checkouts and branches behind their remotes were not moved or forced.
