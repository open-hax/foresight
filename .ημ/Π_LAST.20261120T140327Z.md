# Recursive Foresight fork tax — 2026-11-20

The [manifest](Π_STATE.20261120T140327Z.edn) records the exact Git commit
and annotated tag object for all 26 submodules: 25 direct entries and
OpenPlanner's nested Vexx. Each origin was read back at both the shared
recovery branch `fork-tax/20261120T140327Z-migration-cleanup` and the peeled
tag `Π/foresight/20261120T140327Z`.

This root commit advances only the OpenPlanner and Services gitlinks.
OpenPlanner removes the unavailable migration-tools submodule and its
two Compose services. Validation-triggered planning and queued migration
processing are no longer wired into these Compose profiles; the optional
external client code remains. Services only removes an obsolete checkout
comment. No deployment was requested or run.

The private residual archive contains 102 Git-visible worktree files and
two binary-capable Git diffs, including the uncommitted root and Uxx ledger
changes. It is encrypted to the owner's key. A temporary second recipient
was used to decrypt and verify every member by SHA-256; that recipient's
key was discarded. The permanent owner key was not unlocked in this run.
No private archive upload was verified. Generated caches, runtime diagnostics,
lockfiles and credential-shaped paths were excluded; originals were left in
place. Uncommitted review workflow files were not activated or published by
this checkpoint.

Verification: root project validation, 12 project tests (171 assertions),
and Clojure lint passed. OpenPlanner's build and default/dev Compose config
validation passed. Strict OpenPlanner ESLint reported 20 errors and 8,032
warnings in unchanged source; this is not an application release or a claim
that strict lint is green on the saved revision.
