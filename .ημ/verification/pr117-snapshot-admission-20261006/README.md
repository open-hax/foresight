# PR117 canonical admission observation

The six original PR117 record/resource files are byte-identical to
`d2822f31ec76a8953fe13115a2852dfbcde23396`. Ordinary main integration at
`fcfc2d17f28640203066ddfe0a22f87db7372a32` refreshes the reviewed caller after
the September 24 MiMo review failed with a remote UnknownError. The historical
CodeRabbit verdict applied to the old head; it supplies no approval for this
new preparation head.

The read-only observation discovers the declared archaeology resources and
calls actual canonical `clio.infra.ledger/read-ledger`,
`clio.infra.schema-store/load-revisions`, and
`foresight.archaeology.infra/project-resources` for run
`934c8daf-50c1-4dd9-bb10-47cc40209a62`. Admission verifies the complete ledger
union, historical schema hashes, and causal relations. Its disposable
projection reports the expected parent run, continued parent action, 3 findings
and 4 evidence references in `admission.log`. No alternate parser/ledger or
board implementation is introduced.

This PR owns independent verification clones at the root pins:
Clio `788cdd3434615a7932b924e68520dbf7f88408c2` and Katamorph
`3bd4cf26e68dc88fbe67f831baa4bc389e3363e7`. They were cloned without hardlinks.
Maven source resources are extracted into private per-PR directories from the
same versioned artifacts previously resolved for the pinned dependency graph;
no shared dependency directory is executed or mutated. Clio's declared
`fs-ext-extra-prebuilt@2.2.9` lives in a separate per-PR Node runtime installed
with scripts disabled. Root submodules stay uninitialized.

The known standalone JVM route/fixture failure remains visible in
[Foresight issue132](https://github.com/open-hax/foresight/issues/132) and its
[bounded incoming plan](https://github.com/open-hax/foresight/pull/133).
No repeated fixture suite is represented as passing, and no historic event is
normalized to bypass admission. Actual-record canonical admission and the
broken synthetic fixture suite are separate observations.

Root suites pass 115 tests / 691 assertions; project/catalog contracts and
zero-warning lint pass. Native current-head review, required hosted checks,
and lawful board readiness remain separate requirements. This observation
re-executes admission of recorded archaeology, not Knoxx runtime behavior;
provisional shape vocabulary stays provisional. No service, child policy,
board state, secrets or global configuration is changed.
