# Sandbox disconnection and recovery checkpoint — 12 September 2026

This is a recovery note, not an acceptance receipt. The requested complete-stack verification and final PR walkthrough are unfinished. No language or execution environment substitution is proposed.

## Infrastructure failure

The active workspace disappeared during ongoing verification for the third time. A renamed cleanup directory briefly retained its contents; selected exact source packages and archives were rescued to the same container's /tmp. The replacement scratch directory was empty. The executor then disconnected.

Independent commands using /bin/bash, login disabled, and / as the working directory returned:

```
409 Conflict, environment_offline
Environment is not connected.
```

This affects even pwd, before any application or dependency can start. An earlier transition reported exec-server transport disconnected and recovery timed out. The final bounded retry from / returned the same 409 error. Available capabilities contain no control to reconnect this Work sandbox. A subsequent attempt to read a skill's auxiliary resource also failed. GitHub and saved-document access remained available.

Changing application code, Mongo settings, model size, compiler flags, or package caches cannot fix an executor that cannot start a process. The required next action is restoring this same sandbox's executor connection. If the container survives, recover its /tmp checkpoints first. This is an infrastructure blocker, separate from the application failures below.

## Last verified behavior and remaining failures

| Surface | Evidence before disconnection | Remaining work |
| --- | --- | --- |
| Knoxx backend production build | Advertised pnpm -C backend build passed: 615 files, 0 warnings | Fresh build after recovering pending source and fixing the session prehandler |
| Knoxx frontend | 492 tests / 2156 assertions passed with the fatal async guard; production build 162 files, 0 warnings | Unpublished lint refactors need reconstruction and fresh tests |
| HTTP routing/publication | 54 tests / 211 assertions; real Fastify accepts generated 139-character Wiki identifiers with bounded 1024-character parameters | Rerun the complete Wiki tour after the routing fix |
| Mailbox/MCP/initial admission | 45 tests / 204 assertions, 527 compiled files, 0 warnings | Full backend suite was not started after fixture repairs |
| Ordered run providers | 19 tests / 131 assertions; actual native Mongo 1 test / 13 assertions including 20 concurrent events and reopen | Recover pending 8550 source exactly where possible; reconstructed bytes require new gates |
| Root local model HTTP | 61 actual HTTP tests, 0 skips; clean root lint | Root receipt-history gate and complete child promotion remain open |
| Axxium successor | 86 tests / 766 assertions, clean builds/lint/boundaries and real compiled HTTP probe | Exact full pending source survives only in rescued /tmp if present; partial remote recovery is not proof of the whole successor |
| Browser full 07 | Actual built backend/frontend, offline Qwen2.5-0.5B and MiniLM; password/passkey/PGP, signup denials, revocation/recovery, administration and real MCP catalog passed | Anonymous mailbox GET returned 500 instead of 401, stopping before the mailbox and complete two-cycle Wiki workflow |
| Full lint | Repaired scanner now exposes actual historical debt rather than silently missing files | Backend 13 errors / 273 warnings; frontend CLJS 6 errors / 632 warnings at the recorded baseline; partial fixes do not constitute a full clean lint run |

Counts describe the particular source and artifacts tested before disconnection. They do not prove newly reconstructed files, later source edits, every child, or the complete stack. The strengthened async test runner must remain enabled; compiler exit 0 alone is insufficient.

The last full backend attempt failed at stale agent timeout/thread-persistence fixtures. Focused repairs passed 45/204. The subsequent full suite never started because the workspace disappeared. Do not report this as either a passing full suite or a new test failure.

## Latest browser defect: session prehandler

The actual anonymous request to /api/actors/mailbox returned 500. The published backend/src/cljs/knoxx/backend/law/guards.cljs passes application exceptions directly to Fastify's done(error). Their CLJS ex-data status is not a native error.statusCode, so Fastify uses 500. The resolver's 401 classification was retained in the application error but lost at this transport boundary.

A partial unverified local fix was interrupted by cleanup. Restore it through a named extern.session-guard transport adapter and infra.auth.session-guards composition; keep native request mutation and callback transport out of law namespaces. The callback hook must return nil, never its async promise, and must invoke completion once. Preserve the existing optional-session behavior deliberately.

Required regression coverage after recovery:

1. Real Fastify prehandler/injection for 401, 403 and 503 resolver failures; the protected handler must not run.
2. Successful resolution attaches the expected context once. Optional resolution failure leaves nil context and continues once.
3. Do not expose unclassified internal error details. Use the established error-body/classification boundary.
4. Compile the actual server, then drive the real anonymous mailbox request from the browser supervisor. Passing an isolated error-status helper test is insufficient.
5. Continue real human mailbox compose/full-message reading, agent-tool send with explicit operation_id, live changes with a dirty draft open, and human acknowledgement.
6. Continue both Wiki content/review/translation/publication cycles, including accepted correction memory on the new source revision and exact published artifact bytes.

Do not claim the session fix was tested. Its new regression had not been written successfully before the executor disconnected.

## Recovery order

1. Reconnect this sandbox and inspect its existing /tmp and package caches. Avoid overwriting surviving exact packages with approximate reconstructions.
2. Restore Git-backed source into a stable directory in this same sandbox, outside the cleanup-prone scratch location if permitted. Reuse the shared pnpm/Maven caches and pinned tool/model downloads.
3. Recover /tmp/knoxx-publisher-next, /tmp/eta-identity-rescue, /tmp/knoxx-agent-admission-3446b7ca.tar.gz, and the recorded source-service/mailbox archives first. Their existence was confirmed before disconnection; it has not been rechecked since.
4. Integrate exact source checkpoints through their owning repositories. Compare bytes/hashes when available. Reconstructed changes require new validation and must not inherit prior green evidence.
5. Finish canonical Clio's uncertain-fsync retry fix, its fault tests, declared output contracts, and Axxium's pending OAuth SDK/proxy/retry fixes.
6. Rerun the guarded full backend and frontend suites, advertised builds, strict lint/boundary gates, and root manifest/receipt checks. Do not suppress warnings or broaden historical receipt exceptions.
7. Start services, clients and Chromium from one supervisor exec because loopback and process namespaces differ between separate calls. Native Mongo testing uses TCP with --nounixsocket and a 0.25 GB WiredTiger cache.
8. Rerun the complete browser tour and preserve masked screenshots/DOM on every failure. Verify served build hashes before and after the tour.
9. Add the final annotated browser walkthrough and inline author self-review to the PRs, request Codex/eta-mu/CodeRabbit on the exact successors, resolve actual findings, then merge only when current verification and review requirements are clear.

## Remaining service work

Canonical Clio remains eta-mu/packages/clio. The deprecated event-ledger package must not regain authority. Invitations still need a finite local command in the existing policy ledger, bound to the authenticated Axxium principal and verified email, with token hash, organization/role scope, expiry and one-use admission. Graph and ingestion/OpenPlanner proxy responsibilities are not yet fully locally implemented. The mailbox identifier "changes" collides with the static SSE route and needs explicit compatible admission or routing treatment.

External GitHub, Discord, Google and ATProto OAuth round trips still need configured provider clients and consenting accounts. The actual local password, PGP and passkey ceremonies are separate evidence; mocked OAuth checks must not be described as provider round trips.

## Saved evidence

The saved foresight-sandbox-report.md version 8 and foresight-browser-recovery-20260912.zip survive the workspace loss. The ZIP contains fresh runs 03–06, sanitized failure evidence, build records and annotated screenshots. Browser full07 results were observed before disconnection; its local screenshot/log artifacts have not been recovered. Earlier successful first-cycle screenshots are historical evidence of those recorded builds, not proof that the reconstructed current checkout has passed both cycles.

The final requested full-stack PR screenshot walkthrough is still pending.

## Durable remote checkpoints and honest rescue limits

| Repository / work | Durable checkpoint | Status |
| --- | --- | --- |
| Foresight PR91 | [30b1d321](https://github.com/open-hax/foresight/commit/30b1d321a0184862eaf5f1e41cb25aee8c7ba18a) | Open; current three-reviewer clearance is pending. Earlier reviews were on 70a, not this head. |
| Knoxx draft PR305 | [377892dc](https://github.com/open-hax/knoxx/commit/377892dcf4908c31b74ccc9349db04356a5d9181) | Published Mail/provenance/frontend work; current review round and full gates incomplete. |
| Foundation PR334 | [fd7cd256](https://github.com/open-hax/eta-mu/commit/fd7cd25645d6acf80c3e5909f3ceecb65b7b1148) | New durability/output-contract review findings remain open despite green hosted workflows. |
| Axxium PR333 | [dbbe6415](https://github.com/open-hax/eta-mu/commit/dbbe64154895e6e63af04054059c4bf34bc5a297) | Pending successor fixes and their current review/verification remain open. |
| Partial identity/protocol rescue | [d0bf4261](https://github.com/open-hax/eta-mu/commit/d0bf42611d99f55c3b155801a419e88c4515ca4e) | Isolated branch work/identity-protocol-offline-rescue-20260912. Two complete source files plus all 12 pending-path recovery instructions. Not a verified successor. |
| Eta build manifest rescue | [70563e1c](https://github.com/open-hax/eta-mu/commit/70563e1c50b5937af625a28d36967627356659f9) | Isolated branch work/sandbox-build-manifest-recovery. Four manifest edits; only the AI package build had run. |
| Admin lint rescue | [47aceb3e](https://github.com/open-hax/knoxx/commit/47aceb3ed569e9a8b43b7563654e44a2da325da4) | Isolated branch rescue/admin-ui-lint-unverified-20260912. Five source/test files plus precise reconstruction notes; explicitly incomplete, unbuildable and unverified. |
| Uxx PR13 | [c72fd2ea](https://github.com/open-hax/uxx/commit/c72fd2ea71829757bd96b2ab3c829199c1aecf90) | Merged after 466 tests, lint/types/build, all three current reviewers and no unresolved threads. Root pin promotion remains pending. |
| Calliope PR18 | [efe85a03](https://github.com/octave-commons/calliope/commit/efe85a036504adfb45d8cab26920a6d4482aa53d) | Merged after its recorded 112 tests / 699 assertions and three-reviewer clearance. Root pin promotion remains pending. |
| Muse PR16 | [4e2ab4f1](https://github.com/octave-commons/muse/commit/4e2ab4f1459df85afa3f589a6e6fa155f9caecc4) | Final generated-hook/gitignore/documentation fixes are published; fresh sandbox gates unrun, merge held. |
| Epiphany draft PR18 | [816531ec](https://github.com/octave-commons/epiphany/commit/816531ecadcaee2e3856f43a258ebc193ae301be) | 771 tests / 2134 assertions, static/AOT/CLI and actual Clio/Lucene/restart/concurrent proof retained. Five Codex findings and upstream fsync retry remain open. |
| Katamorph PR28 | [82a9952c](https://github.com/open-hax/katamorph/commit/82a9952c42ea69000a034fc862d4f942420e2494) | Recorded 218 CLJS tests / 647 assertions and 91 JVM tests / 348 assertions; final review/promotion remains pending. |

The 23 pending Knoxx runtime/admission payloads were captured only in /tmp/knoxx-publisher-next. The publisher explicitly confirmed that their complete contents were not retained in its tool state and were not remotely published. Recover those files before assuming the local 608b5cda, 3446b7ca and 8550c8db assembly survived. The separate /tmp/knoxx-agent-admission-3446b7ca.tar.gz archive contains nine CMS paths and frozen shared turn/bootstrap bytes. No claim is made that inaccessible /tmp has survived the disconnected environment.

Partial source that cannot be rescued exactly must be reconstructed and tested again. Known root changes to retain during that reconstruction:

- backend/src/cljs/knoxx/backend/extern/http_server.cljs: actual Fastify factory routerOptions.maxParamLength=1024; generated qualified Wiki ID regression with 139 characters accepted and 1025 refused.
- extern/fastify.cljs, law/error_body.cljs and infra/http.cljs: validated HTTP-failure data at the named native error boundary; statusCode/code properties; use the typed message property rather than array-inferred aget.
- infra/routes/translation_dispatch.cljs: split scoped receipt loading from gate evidence orchestration, preserving one scoped receipt read, authorization and source acceptance.
- extern/mcp_sdk.cljs: unique fallback call IDs generated with crypto/random-hex 16 instead of the constant mcp; explicit domain operation_id has priority for stable retries.
- backend tests: actual Fastify and registered SDK regressions; dedicated http-route-recovery build in backend/shadow-cljs.edn.
- scripts/verify-wiki-stack.mjs: actual Mail tool/UI tour, masked failure evidence, and services.verifyBuilds before and after the full tour. The separately published wiki-build-snapshot.mjs already hashes every served artifact recursively and refuses symlinks/special files.
- Shared turn admission: initial thread persistence must resolve before heap registration, run_started emission or model execution; rethrow the original failure. Earlier durable run facts may exist, so this is not an atomic transaction across ledgers.

Root receipt-history repair was not started. Its reviewed direction is to preserve the 137-record original bytes, explicitly pin and classify 11 malformed historical non-evidence rows as unverified archival input, and append a valid archive registration. It must not weaken current envelope/evidence/promotion predicates or admit future malformed rows. Required negative checks include tampered bytes, duplicate/forged registration, CRLF/truncation, malformed evidence-origin rows and no promotion of archival input.

The remaining local-service audit is separately durable at [c7a1c650](https://github.com/open-hax/knoxx/commit/c7a1c65078ce86fa04e995119ff5cbe36472daf6), branch rescue/local-service-audit-20260912. It cites immutable published source and distinguishes the focused 45/204 proof from the unstarted full suite.

### Exact missing Knoxx publication queue

The publisher recorded these 23 paths in runtime-admission-00.json through runtime-admission-10.json, runtime-admission-manifest.json and run-state-assembly.json:

```text
backend/shadow-cljs.edn
backend/src/cljs/knoxx/backend/extern/fastify.cljs
backend/src/cljs/knoxx/backend/extern/http_server.cljs
backend/src/cljs/knoxx/backend/extern/mcp_sdk.cljs
backend/src/cljs/knoxx/backend/infra/http.cljs
backend/src/cljs/knoxx/backend/infra/routes/translation_dispatch.cljs
backend/src/cljs/knoxx/backend/law/error_body.cljs
backend/test/cljs/knoxx/backend/extern/fastify_error_recovery_test.cljs
backend/test/cljs/knoxx/backend/extern/mcp_call_id_test.cljs
docs/verification/runtime-command-boundaries.md
scripts/verify-wiki-stack.mjs
backend/src/cljs/knoxx/backend/extern/actor_mailbox.cljs
backend/src/cljs/knoxx/backend/extern/actor_tools.cljs
backend/test/cljs/knoxx/backend/agent_hydration_test.cljs
backend/test/cljs/knoxx/backend/agent_turn_admission_test.cljs
backend/test/cljs/knoxx/backend/agent_turn_timeout_test.cljs
backend/test/cljs/knoxx/backend/extern/actor_mailbox_test.cljs
backend/test/cljs/knoxx/backend/extern/agent_turn_fixture.cljs
backend/test/cljs/knoxx/backend/infra/mailbox_bootstrap_test.cljs
docs/verification/agent-admission-recovery.md
backend/src/cljs/knoxx/backend/infra/agent/turn.cljs
backend/src/cljs/knoxx/backend/infra/persistence_bootstrap.cljs
backend/src/cljs/knoxx/backend/domain/action/run_state.cljs
```

The last captured run_state.cljs SHA256 was fede8c8ef4dc651fd9b8b4201ee7984d2c648c14cc6f78ef3249f6e7f192f9d2. This is a recovery comparison value, not a new verification result.

## Hosted checks inspected after executor disconnection

Hosted results are separate from sandbox execution and do not replace the requested browser proof.

- Foresight 30b1 has successful Muse compilation, deterministic evidence and MiMo review jobs, but the [OpenCode evidence job](https://github.com/open-hax/foresight/actions/runs/34687855041/job/103538471117) failed. The current head is not merge-ready.
- Knoxx 377 has failures in the [backend/frontend workflow](https://github.com/open-hax/knoxx/actions/runs/34688477820/job/103539519694), the [bundle job](https://github.com/open-hax/knoxx/actions/runs/34688477841/job/103539519837), and review resolution. Ingestion and deployment-boundary jobs passed. Draft evidence jobs were skipped; a green wrapper does not mean reviews ran.
- The final no-issues review and merge requirement is not satisfied for either PR. Do not treat old-head approvals, skipped checks, review quotas or unavailable execution as clearance.

## Final run-provider rescue and connection check

The complete 26-path run-provider reconstruction is now durable at [ae5799aa](https://github.com/open-hax/knoxx/commit/ae5799aaf39ff5453926f611977847fe936f6ac7), branch rescue/run-event-providers-unverified-20260912, tree bf9c02c9b5b1010210669a066326d9755d88228d. The additional docs/verification/run-event-recovery-manifest.json records every path and its reconstruction provenance. No path is omitted, but all 26 remain reconstructed and unverified because the original 8550 hash manifest was lost. Historical 19/131 and native Mongo 1/13 results do not verify these new bytes. Shared turn/run_state assembly remains separate and must be recovered/reconciled before integration.

A final connection check after remote rescues again ran only pwd from / with /bin/bash and login disabled. It returned 409 environment_offline before starting any process. The executor has not reconnected. No further runtime or browser success is claimed.

Author self-review / hold comments are posted on [Foresight PR91](https://github.com/open-hax/foresight/pull/91#issuecomment-5645415999) and [Knoxx PR305](https://github.com/open-hax/knoxx/pull/305#issuecomment-5645417807). This recovery documentation is isolated from those PR heads and does not make them merge-ready.
