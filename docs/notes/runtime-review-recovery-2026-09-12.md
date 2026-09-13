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

## Subsequent model and transport review

Actual CodeRabbit finding 3996948898 reproduced a refusal when an owned `::1`
listener was addressed by the equivalent literal `0:0:0:0:0:0:0:1`. Native IPv6
literals are now canonicalized before both loopback and live-listener equality
checks. The regression also refuses wildcard, mapped IPv4 and hostname forms;
listener ownership, port matching and fatal refusal behavior remain intact.
All 14 transport tests pass after the real one-test RED.

Codex finding 3997008937 reproduced HTTP 400 for empty decoded text following
valid real inference. Empty provider output now throws the provider error type,
yielding HTTP 500 / `generation_failed`; invalid requests remain HTTP 400 and
expired inference remains HTTP 504. The actual decoder-boundary regression
failed before the change and passes after it. No success output was injected.

Codex finding 3997008933 correctly identified an installation dependency on
Knoxx in the root agent smoke. Devtools now declares `@open-hax/eta-mu-ai` 0.70.7
and imports its public completions entry directly. The generator incorporates
that exact dependency in root manifests. An initial offline install could not
resolve missing registry metadata; the normal online install reused 336 packages
and downloaded 33. The subsequent frozen offline install passes. pnpm still
reports its intentionally unapproved `@google/genai` build script and four
deprecated transitive packages on resolution; no build-policy exception was
enabled. The actual SDK smoke passed without those scripts.

Codex finding 3997008938 is fixed in the main workspace guide: native SSE,
validated Qwen tools, model identities and request limits are now described
consistently. CodeRabbit finding 3996948902 is addressed by replacing the
ambiguous Proxx total with explicit revision-bound historical evidence and
synchronizing all three recovery notes. That history does not claim a fresh
application run or any interaction with the old rejected process.

CodeRabbit's separate `tools:null` observation was checked against the published
guard, which already validates the original field. The new actual Qwen case
proves HTTP 400 with zero tokenizer-template calls, while an empty array reaches
the real template. This is additional coverage without a fabricated RED.

Fresh complete root gates pass: 125 Clojure/CLJS tests / 607 assertions and 18
Node tests, zero failures; test compilation 96 inputs and release 86 inputs,
zero compiler warnings; clj-kondo and Oxlint zero errors/warnings. The expanded
model HTTP/codec suite passes 83 tests with zero skips. Actual Qwen through the
devtools SDK produced a 46-token `save_translation` call, the local tool executed
its real arguments, and the next request consumed its result and produced a
31-token streamed confirmation. Independent peer inspection found no introduced
issue in the changed transport, classification, dependency or regression paths.

The final browser publishing cycle, all-child warning-free gates, dependency
promotion and current-source external reviewer convergence remain separate
unfinished obligations.
