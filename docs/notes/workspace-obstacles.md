# Sandbox recovery obstacles and evidence

This note records the workspace/bootstrap slice of the Foresight effort. It is
not a claim that every application provider, browser flow, or review is accepted.
The implementation stays in the declared Clojure, ClojureScript, JVM and Node
stack in the same sandbox. Child repositories retain their package managers and
locks; the root commands expose their ownership boundaries.

## Recovery and shared resources

| Obstacle | Resolution or current boundary |
|---|---|
| Scratch pruning removed binaries, caches and an external worktree | Recovered source from surviving Git objects and published exact-tree checkpoints. Restored the declared toolchain inside the current workspace. Active work no longer depends on the lost external directory. |
| Node native builds could not find headers/addons | Restored Node 24.20.0 headers and verified the declared native modules with their real packages. Shared pnpm hardlinks avoid repeated binary copies. Build-policy exceptions were not silently enabled. |
| JVM dependency downloads required the sandbox proxy and certificate chain | Installed the provided trusted CA chain into the JVM truststore, preserved HTTPS verification, and configured both JVM/Node proxy paths. Loopback exclusions include `0.0.0.0`. |
| Fourteen child sources were incomplete | Restored every direct child's source. Eleven initially returned to their declared gitlinks; active integration branches were preserved. Later Muse/Uxx/Proxx fixes were reconciled with their current upstream branch rather than discarding upstream tests. |
| Calliope Git LFS checkout consumed about 7.6 GB including cache and hydrated tracks | Completed the source checkout with LFS smudging disabled after the large transfer. Media hydration is partial. Normal source tests run; complete media availability is not claimed. |
| A single flat dependency tree would collide with child identities and versions | The generated pnpm group includes root tooling, devtools and canonical Clio. The inventory records conflicting and duplicate package identities; independent child commands still run in their own roots. |
| Uxx's directory named `react` collided with pnpm's hoisted workspace identity | Isolated linking plus public dependency visibility produced one actual React identity. Packed adapter consumers now verify that identity outside the workspace package layout. |
| Child manifests and root manifests could drift | The generator reads only explicit composition inputs, hashes their manifests, and verifies selected child HEADs against root gitlinks. It rejects uncommitted selected source and permits unrelated uninitialized children. Root dependency aliases compose libraries without relocating child build/test commands. |
| Unix-domain sockets were denied | Native Mongo uses a TCP-only transitional fixture. Chromium works through Playwright's pipe transport. The agent-browser daemon could not start in this sandbox; no browser redownload or environment replacement was needed. |

## Providers and models

Canonical event sourcing belongs to `eta-mu/packages/clio`. The deprecated
standalone event-ledger package is retired from the active dependency paths in
the foundation work. A working native Mongo 8.0.13 replica-set fixture remains a
transitional way to exercise existing integrations, not the final Mongo-free
provider. Protocol and application cutovers have their own durable-file tests.

The shared devtools S3 fixture uses a real S3rver process and AWS client put/get
operations. The embedding fixture runs pinned MiniLM q8 CPU inference locally
and returns real 384-dimensional vectors. Generation supports pinned SmolLM2
135M and Qwen2.5 0.5B ONNX models with explicit cache warm-up; inference itself is
offline. Shared model cache location is controlled by `FORESIGHT_MODEL_CACHE`.

SmolLM2 repeated itself on the reviewed-memory workload and correctly reached
the token-limit failure boundary. Qwen completed the actual writing/translation
provider probe, but candidate quality still needs human revision. Model output
is never fabricated or substituted for a successful production receipt. The
generation cache occupies about 1.1 GB for Qwen; normal root tests do not fetch
models. The model investigation records the separate OpenCode Zen HTTP attempt
and its access limitations; free hosted inference is not assumed to work.

## Child verification boundaries

These are concrete executed results, not an assertion that all possible suites
or production integrations have passed. Raw evidence retains failed attempts as
well as subsequent successful repairs.

| Child | Executed result and remaining boundary |
|---|---|
| Truth | 822 tests / 12,019 assertions passed; owning strict analysis passed with its pinned kondo. |
| shx | 19 tests / 46 assertions passed; owning strict analysis passed with its pinned kondo. |
| Calliope | 61 tests / 219 assertions passed. One existing unused-binding lint warning remains; media hydration is partial. |
| Epiphany | A shallow clone caused 21 failures. Fetching history for the same pinned source fixed them: 763 tests / 2,102 assertions passed. Its lint alias returns zero despite 66 warnings, and tests report an SLF4J-provider notice. These are still open findings. |
| Katamorph | CLJS 155 tests / 371 assertions and JVM 27 / 70 passed; examples/release/static checks passed. One namespace/var compiler warning remains. |
| Bitch-tracker | 2 tests / 7 assertions passed, lint 0/0, plugin release without compiler warnings. The repository has no committed package lock; no fabricated frozen-install claim is made. |
| Muse | Current-main 197 tests / 506 assertions, lint 0/0, all four host builds without compiler warnings. Warm build success originally concealed a cold generated-namespace failure; a fresh source proof now passes with the prebuild generator. Claude post-release hooks are verified against the actual checkout. |
| Uxx | Current-main 466 tests, root/token and adapter production builds, TypeScript, lint and frozen install passed. Actual packed Helix/Reagent archives expose 63 runtime exports each and share a consumer React. Tarballs exclude compiler caches and runtime build dependencies. |
| Proxx | Current-staging production build and 124 CLJS tests / 324 assertions passed before follow-up extern hints. Lint has 0 errors and 238 existing calibrated warnings. Full Node suite is blocked by automatic approval review; no later success or termination is asserted. |
| services | Deployment-boundary self-test, immutable-pin checks and 370 EDN/documentation inputs passed. This is not a live-host deployment or complete production CI claim. |
| eta-mu | Clio host gates and service-protocol tests execute real file persistence and restart. The protocol review fixes add typed ESM consumers, same-history authentication admission, JavaScript defaults and explicit subscription failure handling. Current child PR checks remain authoritative for their exact heads. |
| Knoxx | Application agents own the backend/frontend/wiki/identity/browser proofs and their exact commits. Root workspace tests do not substitute for those checks. |
| OpenCode | Restored source and the declared baseline build/runtime investigation. The separately blocked execution is not retried through another launcher; its full test status remains in the dedicated evidence. |
| .agents | Initialized source/configuration repository; it does not declare an application runtime build. Its manifest/input boundary is inventoried. |

The additional OpenPlanner checkout builds the declared SDK and stores for Knoxx
using shared installed dependencies. It is an explicit sibling integration input,
not a fabricated fifteenth direct gitlink.

## Tests, review and remaining work

Shadow compile/autorun was observed returning success even when a test assertion
failed. Root, Muse and the protocol package now run emitted Node tests with their
actual process exit status. Time-bounded runners preserve stdout/stderr and exit
codes; a timeout is recorded as incomplete. Subscription tests wait for observable
events with a bounded deadline rather than assuming a fixed short delay.

Actual external reviewers found issues after the first local green pass. Those
findings produced fixes for graph TypeScript declarations, authentication races,
optional JavaScript arguments, watch handles, packed React peer identity, cold
generated namespaces and Claude hook emission. The workflow projector also
dropped declared expected-output and no-warning checks; executable generated-shell
regressions now prove those checks fail when required. Reviews must rerun against
each published successor, and historical green reviews are not carried forward
as proof about a new head.

Automatic approval review rejected the Proxx full Node test action because it
caused HTTPS traffic to an unverified private host whose ownership and payload
were unknown. The rejected execution was not retried. A subsequent process-list
diagnostic itself failed before any stop, and an early log inspection identified
the nonlocal fixture routes. Once the restriction on indirect access was made
explicit, investigation continued through source inspection only; no later
process state or successful termination is claimed. Source inspection points to
test provider registries retaining nonlocal fallback URLs. A bounded next fix is
to make those integration fixtures select only explicitly owned local endpoints
and prove the transport boundary before requesting a new execution. All work
stays in the current sandbox and existing implementation stack.

Outstanding lint/compiler warnings above, hosted exact-head checks, provider
cutover completeness, browser acceptance and unresolved reviewer findings remain
acceptance work. Neither a zero exit with warning output nor a passing subset
is counted as a clean entire-stack result.
