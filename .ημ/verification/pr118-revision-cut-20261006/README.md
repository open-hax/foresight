# PR118 canonical admission observation

The read-only observation loads every declared archaeology run resource in
this isolated worktree, reads its referenced physical ledgers through
`clio.infra.ledger/read-ledger`, loads and verifies historical schemas through
`clio.infra.schema-store/load-revisions`, and calls
`foresight.archaeology.infra/project-resources` for
`dcadf215-7b96-4bad-a11a-a5f5067546b9`. `admission.log` is the resulting
projection identity/causal continuity and finding/evidence counts. Clio owns
canonicalization, event admission, missing causes and schema verification;
this observation introduces no alternate parser, ledger or board engine.

The invocation used private clones at the Foresight pins:

- Clio `788cdd3434615a7932b924e68520dbf7f88408c2`
- Katamorph `3bd4cf26e68dc88fbe67f831baa4bc389e3363e7`

Maven dependencies were resolved from the existing archaeology metadata with
invocation-only local-root overrides into a private Maven cache. Their CLJS
source resources were extracted into private classpath directories for NBB.
Clio's declared `fs-ext-extra-prebuilt@2.2.9` dependency was installed in an
independent temporary runtime, with install scripts disabled. No child Git
worktree, root dependency metadata, global configuration, service or ledger
was mutated. Root submodules remain uninitialized in this worktree.

Result: canonical admission/projection passes, reporting 3 findings and
5 evidence references with the documented parent run/action continuity.
These provisional shape claims remain archaeological findings, not accepted
lifts. Pinned remote source references were not re-executed as Knoxx runtime
tests in this observation.

The documented JVM test route is independently broken: it still points at
retired `eta-mu/packages/clio`; with invocation-only extracted-child overrides,
6 tests/21 assertions run with 0 failures and 1 fixture error because the fake
UUID version/variant is invalid. This root test/wiring gap is tracked as
https://github.com/open-hax/foresight/issues/132 . It must stay visible and is
not normalized into a passing suite.
