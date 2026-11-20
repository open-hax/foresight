# Foresight fork tax — 2026-11-20

This checkpoint preserves the public Git state of this checkout. It is not a
release, a deployment, or a copy of every worktree on this host.

- The [state manifest](Π_STATE.20261120T134219Z.edn) identifies each of the 25
  direct submodule commits and both OpenPlanner nested pins. For each of the
  26 initialized submodules, it records an origin ref observed at the pin or
  at a descendant. An observed branch tip may subsequently move.
- `.agents` integration commit `923a03198d02b6891c388366d93645b3638cfd85` was
  published and read back on `integration/foresight-main-f49` and the annotated
  tag `Π/foresight/20261120T134219Z` before updating the parent gitlink.
  The staged `.agents` gitlink is the only submodule pointer changed here.
  Uxx Firefox remains pinned at the previous published source checkpoint.
- `openplanner/packages/stores/migrations/openplanner-migration-tools` remains
  uninitialized: the configured local source is absent and no accessible
  remote was verified for the pinned object. No URL or pin was changed.
- 92 review scaffolding files matched existing remote branch blobs and Git
  modes. They were not staged or activated. That preservation is not approval
  of the workflow's execution or credential boundary.
- Ten residual source and documentation files, including private operational
  notes and review files without an exact remote match, were copied to a local
  owner-only GPG archive. Its ciphertext checksum was verified; decryption
  could not be tested noninteractively. No off-host copy was verified because
  the configured Drive token was rejected with `invalid_grant`. The originals
  remain in place. Runtime diagnostics, caches, lockfiles and credentials
  were not selected for publication or archival.

## Verification

- Foresight project validation: 26 workspace sources, 25 submodules, 40
  invariants. Project tests: 12 tests, 171 assertions.
- Workspace tests: 24 tests, 122 assertions; the missing-executable fixture
  emitted `ENOENT`. `clj-kondo`: zero warnings and errors.
- `.agents`: `git diff --check` against the published second parent and the
  Session Mycology conversion dry run passed. A bounded credential pattern
  scan found no hits in the merge delta. No full child CI run is claimed.
- The previous Uxx checkpoint records the Firefox build and 335 passing tests.
