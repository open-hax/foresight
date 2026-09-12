# Runtime review repairs after workspace recovery

The published root checkpoint `e49b2e757a66b969c44760c58f3840cc45b0c8cb`
and saved report survived workspace maintenance. The prior active checkout,
installed tools, generated builds and model cache did not. All fourteen direct
children were restored through HTTPS at their declared revisions before selecting
the surviving child PR heads. Large Calliope LFS media was deferred explicitly.
Root composition still consumes the exact declared eta-mu revision; no manifest
ownership or pin check was bypassed.

Several later local Knoxx commits could not be found by their abbreviated GitHub
identities: `fb64acbc`, `014eebdd`, `6b7fc9cc`, `17083ebf`, `55516924`.
Their recorded historical test results are not proof of the restored PR source.
Those unpublished changes require recovery or reconstruction and new verification.
The latest local root embedding repair has a verified equivalent published tree,
`38e9e48bec80899b580a83c5f16945349dbabf7b`, and was restored intact.

## Refusal status must survive test cleanup

Codex finding 3996623723 reproduced successful exit after removing all exit
listeners. Three real child-process cases failed before the repair. The preload
now captures Node's native termination boundary and exits immediately with status
one on a non-owned transport attempt. It does not depend on application exception
handling or mutable exit listeners.

Independent peer review found that BigInt endpoint formatting could itself throw
before termination. An additional actual child regression failed, then passed
after placing all logging inside `try/finally`. The refusal code is synchronously
written before optional endpoint details. All thirteen transport tests pass on
Node 24.20.0, with no skips. This remains a boundary for the audited Node surface;
it is not containment of arbitrary native programs or authorization to resume
previously rejected OpenCode/Proxx sessions.

Immediate termination skips worker cleanup. Subsequent guarded suites must use
a parent-owned temporary directory and remove it only after confirmed worker
exit. The transport regression's parent owns its child fixtures.

## Reviewed source is an integration precondition

Codex finding 3996623719 reproduced acceptance of unchanged dirty Epiphany source.
The real supervisor now rejects a dirty initial snapshot before starting any
service. Before/after source equality remains a separate mutation check.

Three native subprocess regressions copy the actual supervisor into isolated
real Git fixture checkouts. Tracked and untracked changes each reproduced a
failure on the old supervisor. With the repair, both are refused; clean source
reaches an intentionally absent Mongo executable, proving it passed source
admission. All three pass. These tests prove the admission boundary, not a new
Mongo/S3/model integration result. They cannot write a successful integration
receipt and clean their parent-owned temporary directories.

## Verification

`pnpm test` now includes both regression files through generated root manifests.
The complete command passes: 125 Clojure/CLJS tests with 607 assertions, plus
17 Node tests (thirteen transport, three source-input, one census entrypoint).
The test compiler reports 96 inputs and zero warnings. `pnpm build` passes with
86 inputs and zero warnings. `pnpm lint` reports zero errors and zero warnings
in both clj-kondo and Oxlint. The pinned frozen install and generated-manifest
check pass; installed native filesystem locking and Transformers.js imports
were also exercised.

The complete browser publishing cycle, successor child repairs, all-child strict
gates, final dependency promotion and actual reviewer convergence remain pending.
None of the eight open PRs is declared merge-ready by this bounded root result.
