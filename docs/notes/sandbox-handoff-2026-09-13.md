# Foresight sandbox handoff and lessons — 2026-09-13

## Decision and status

The user asked to stop pursuing browser acceptance for now, publish the work available, and record the experience in Google Drive under Agents/ChatGPT/Foresight. This report closes the current implementation attempt as a handoff. It does not claim the entire stack passed, all reviewers approved, or two publication cycles completed.

The main work is already in [Foresight PR 91](https://github.com/open-hax/foresight/pull/91) and [Knoxx PR 305](https://github.com/open-hax/knoxx/pull/305), with related child PRs listed in the accompanying audit. Foresight PR 91 was converted to draft for this handoff; Knoxx PR 305 was already draft. The new handoff documentation is an ordinary successor to the existing branch. No merge, force push, protection bypass, dependency promotion, or new browser run is part of this handoff.

A fresh inspection found zero files in the previous Foresight source checkouts, browser22 evidence directory, principal evidence directory, and several active successor checkouts. Directory names survived workspace maintenance, but that did not mean their files or Git history survived. Some older sibling files remained; those are not substitutes for the missing current work.

The previously saved report and screenshot archives have recorded persistent identities, but their authenticated retrieval actions are unavailable in this turn. Exact-name Google Drive searches also found no copies. The files uploaded with this handoff therefore contain newly assembled documentation, freshly read GitHub metadata, and recovered published documentation. They do not contain the missing screenshot archive or unpublished code. This distinction is central to the handoff.

## How to read the evidence

Three evidence classes must stay separate:

1. **Freshly verified published state:** source files and PR/review metadata read back from GitHub for known commits during this handoff. The recovered-source index records the Git blob identity, SHA-256 and byte count of each recovered file. The PR audit records current heads and review limitations.
2. **Historical execution recorded in the prior working checkpoint:** commands, counts, failures and measurements observed during the preceding sandbox work. They are useful evidence of what was tried, but are not reruns, and their raw local output is no longer present unless independently published.
3. **Unfinished or unavailable:** a started test is not a completed test; a local commit hash is not a remote commit; a previously saved archive is not an archive successfully uploaded to Drive today.

Historical notes sometimes describe an earlier state. Later dated findings in this report and the fresh PR audit supersede those earlier status claims. In particular, older notes saying free Zen access was unavailable do not negate the later successful native CLI smoke, and older Epiphany suite counts are not the latest child counts.

## What the user wanted

The original goal was to initialize Foresight and all its children in the same sandbox, with real build, run, test and lint capabilities. Constraints and requested product work expanded to include:

- Canonical event sourcing in eta-mu/packages/clio, retiring the deprecated standalone event-ledger dependency.
- Replaceable providers for service protocols, with inspectable EDN-backed development operation and no mandatory external Mongo or object-storage service for the local product.
- Shared dependency storage and generated root pnpm, Clojure, NBB and Shadow manifests without duplicating every child installation.
- Real small local embeddings/generation, and a real OpenCode Zen Big Pickle request using the source's actual request path.
- Axxium ownership of identity, including password, GitHub, Discord, Google, ATProto/Bluesky, PGP and passkey entry points.
- A Wiki-like CMS: remember, brainstorm, draft, review, revise, accept English, translate, review, revise, accept translations, publish, then repeat after new content.
- Human UI and agent-tool parity for the same roles, with immediate UI updates after authorized agent mutations.
- Browser proof, annotated screenshots, author walkthrough, actual Codex/eta-mu/CodeRabbit review, and eventual gated merge.

The work produced significant implementations and tests, but this attempt did not reach the full acceptance condition.

## Published entry points at the handoff boundary

| Repository / PR | Published application head observed before handoff documentation | Meaning |
|---|---|---|
| open-hax/foresight #91 | d21686e8f349de0c3000d42b857aeae156bf1616 | Shared workspace, local fixtures, model transport and evidence infrastructure; new handoff doc is added after this head. |
| open-hax/knoxx #305 | 530b1db992e3c1373d3427263c1d708582319911 | Published foundation/application work; many later local changes were not published before cleanup. |
| open-hax/eta-mu #334 | bd617b9301e2774c721ddeff434b707b788be95f | Canonical Clio foundation. |
| open-hax/eta-mu #333 | 33852976fd7d30afc2fc28cee62d2bacca866a81 | Axxium identity successor with startup concurrency repairs; fresh review found further issues. |
| open-hax/eta-mu #335 | 969095596da6f39bf1238b2f74702e222d20a832 | Rheos rendering repair; integration conflict still present. |
| octave-commons/epiphany #18 | 21ddc7153213a3e26092f9f4eef6440e7e849c2c | Clio/EDN recovery and request-identity repairs; native-warning and fresh review issues remain. |
| open-hax/proxx #444 | c9357168f26a208f48d781369c046467aeda188f | Separately published fixture/workflow repair; historical application evidence is explicitly dated. |
| open-hax/services #84 | e29d2ce3adca261a8fbaab1173287303941d03cf | Deployment-boundary evidence; not a production deployment. |

The accompanying PR audit has fresh URLs, review observations and CI statuses. Root gitlinks were not promoted merely because related child branches existed. At the previous implementation checkpoint the declared root eta-mu gitlink was a7b19825fb5d7c624c38f1d41043c42e92d7f0c3. The locally composed Clio/Axxium candidate was separately identified, not silently presented as the declared root dependency.

## What actually ran

These are historical executed results tied to their recorded source scopes, not a fresh full-stack certification:

| Scope | Recorded result | Practical limit |
|---|---|---|
| Root Clojure/CLJS | 125 tests, 607 assertions; root compilation and configured linters reported zero warnings | Does not test every child application. |
| Root native Node | 28 tests passed | Separate from model suites. |
| Published model HTTP/codec/close/observer suite | 94 tests, zero failures/skips with pinned local models | A later real terminal-write issue was found; this old pass is not final correctness. |
| Real Epiphany service integration | 22 tests / 108 assertions, real Mongo ping, S3 EDN roundtrip and MiniLM embeddings | Specific integration scope, not complete Mongo independence. |
| Clio | BB 29/182; JVM 80/388; NBB and Shadow each 78/395; configured gates clean | Exact Clio candidate only. |
| Axxium startup successor | 151 tests / 1,290 assertions; test/server/library compilation without warnings; native ESM/TCP probes | Later credential-revocation findings remain. |
| Rheos rendering successor | 216 tests / 1,278 assertions twice; JVM 13/118; separate CLI/workflow gates | Current branch cannot yet merge cleanly with its updated dependency base. |
| Epiphany later unit suite | 811 tests / 2,552 assertions passed; static/build/CLI scopes recorded separately | Native Lucene warning causes the strict integration gate to fail. |
| Knoxx frontend | Typecheck, production build and configured lint passed; Vitest 47 files, 276 passes and 41 todo cases | Todo cases are not passes. |
| Knoxx frontend CLJS | 526 tests / 2,282 assertions: one failure, zero errors | Generated inventory was stale. Repair was committed locally; the rerun's completion is unavailable. |
| Knoxx broad publication slice | 499 tests / 3,093 assertions twice, plus a separate 1/10 regression | Not the final whole combined backend suite. |
| OpenCode isolated expansion | 90 approved files, 706 tests, 1,444 expectations, zero failures | Local repair was not published; full LLM replay and whole application acceptance are incomplete. |
| Proxx historical application repair | 672 passes out of 673; one existing absent-bootstrap skip; lint 238 warnings | Not warning-free, not a rerun at the later workflow head. |

Other earlier child results were preserved in the published obstacle history: Truth 822 tests / 12,019 assertions; shx 19/46; Calliope 61/219 with partial media hydration; Katamorph CLJS 155/371 and JVM 27/70; Bitch-tracker 2/7; Muse 198/517 with real Mongo and cold-host builds; Uxx 466 tests and packed adapter consumer checks. Read their source-bound notes and child PRs before reusing these numbers as acceptance evidence.

All 14 direct children had been initialized before the later cleanup. That is a historical initialization result, not a description of today's empty source directories.

## Browser results and the timeouts

### The browser itself was usable

The ordinary agent-browser daemon could not bind its Unix socket in this sandbox. Chromium could run through Playwright pipe transport. The existing Chromium binary was reused rather than repeatedly downloaded.

Each shell execution had its own process/network namespace while the filesystem was shared. Starting an HTTP service in one execution and opening the browser in another did not reliably put them on the same loopback network. The working arrangement was one supervisor owning the backend, frontend, model/embedding services and Chromium in the same execution.

This means “browser is a no-go” is too broad a technical diagnosis. The decision to stop browser work is reasonable for the handoff, but the observed final blocker was the translation path and its dependencies, not an inability to launch or interact with Chromium.

### Browser22 was a failed run, with useful completed steps

The final observed browser run began at 20:04:25.252 UTC on September 12 and ended at 20:12:07.012, about 7 minutes 42 seconds, with exit 1.

The checkpoint recorded real identity ceremonies, admin views, Mail, Contracts editing, AI-assisted writing, source review/acceptance, delegated MCP mutations, live SSE updates, a concurrent draft, and refusal to publish a stale source revision. It captured 33 annotated screenshots, including initial/failure views.

It then waited for a current translation candidate/dispatch action and timed out. The selected row reported accepted dispatch, in-flight work and no candidate. It did not complete translation acceptance, publication, remembered lessons and the second publication cycle.

The model observer recorded a real Qwen 1.5B translation request with 1,927 prompt tokens and 339 generated tokens before a 180.766-second generation timeout. A later request with the same prompt length was interrupted during cleanup after roughly 71 seconds and 57 tokens. A shorter writing request completed in about 5.135 seconds with 100 input and 18 output tokens.

An older source revision was still running while the newer revision was queued. Source-hash reconstruction matched the two revisions exactly:

- Earlier source: sha256-f16aadc557b82a7faed491b7b81da9aba18768a55b960c333e78ec6878a2d128.
- Revised source: sha256-1eaca57aa82558c77a9f97258c3b557c61b9639f132670c16a9f1549df81abe2.

The captured active-agent endpoint showed the earlier run running and the later run queued. There was no direct request-ID-to-run-ID field; associating individual model request IDs with those runs by timing is an inference, not an explicit trace join.

A separate embedding transport failure was logged early, while the embedding log was empty. The old adapter discarded the native cause, so the exact network failure could not be identified retrospectively. It was not justified to call that a 120-second timeout. A later local diagnostic repair retained a finite, privacy-conscious set of native error fields, but it was not published before cleanup.

### Earlier proof limitations

Browser21 also failed on translation after roughly 8 minutes 35 seconds. A concurrent backend build had overlapped its ESM output directory, so its continuously running compiled-source identity could not be claimed.

Browser22 used before/after source/configuration hashes and a separate source snapshot; those were stable. However, that snapshot included local changes relative to the recorded Git HEAD. It was valid diagnostic evidence for that snapshot, not a clean committed-head acceptance run.

The last checkpoint explicitly had no active browser. Browser23, intended to use real OpenCode Zen plus local embeddings, was never started.

### What should change in any future browser attempt

Keep the same sandbox and implementation languages. Run the browser only after the exact provider path passes a smaller native integration check that covers translation output, durable candidate settlement and process cleanup. Use one supervisor, unique compiled output directories, fixed source identities, and visible stage progress. Preserve a failed stage's state before cleanup and do not require another seven-minute tour merely to rediscover a provider error.

A full end-to-end run must eventually cover the real two-cycle publication story, but it should not be the first or only way to observe model progress, FIFO blocking, transport errors or process leaks.


### Additional measured failures recovered from published notes

Browser20's delegated MCP operation took 63.8 seconds against a 60-second SDK deadline. A generated tool-call probe took about 15 seconds alone, but a later comparable probe under other verification load took roughly 49 seconds plus 22 seconds for its streamed confirmation. This supports separating CPU contention and inner request deadlines from the outer browser timeout; it does not prove that removing load alone would make the whole tour pass.

Native Mongo readiness initially reused a client whose topology had become permanently closed after an early failed connection. The bounded retry needed to close and recreate that client even after the Mongo process became healthy. A healthy process is not necessarily a usable existing client instance.

Several fixture assumptions were also stale: a hardcoded Mongo URI, silent fixture skipping, 768-dimensional embedding assumptions, and implicit model selection. Dedicated test configuration, isolated collections, explicit model/dimension/artifact identity, and required-fixture failure made those assumptions visible.

Uxx packed-consumer verification found problems hidden by workspace imports. It proved one actual React identity and excluded compiler caches/runtime build dependencies from published tarballs. Removing an unrelated npm package called reagent eliminated 106 packages without removing the actual Maven Reagent dependency.

The recovered model cache was about 3.6 GiB across the four pinned models; the Qwen 1.5B ONNX file alone was 1,787,566,590 bytes. The earlier 1.1 GB number refers to the earlier Qwen cache measurement, not the final combined cache.

A model's text-completion configuration did not automatically register its agent-streaming provider with eta-mu. Actual generated tool use required the separate provider registration, the tokenizer's tool template, exact offered tool names, validated arguments, real tool execution and the streamed follow-up.

The generation observer's five-second progress threshold was checked when native generation steps yielded. It was not an independent heartbeat capable of reporting while native computation prevented JavaScript execution. A future supervisor needs its own visible stage/progress reporting in addition to native token events.

## Models, tokens and provider lessons

### Models actually used

Ollama was not the generation server used in the final browser attempt. The local implementation used Transformers.js 3.8.1, pinned Hugging Face ONNX models and CPU inference:

| Model | Role / configuration | Observed limit |
|---|---|---|
| Xenova/all-MiniLM-L6-v2 | q8, two intra-operation threads, normalized 384-dimensional embeddings; pinned revision 751bff37182d3f1213fa05d7196b954e230abad9 | Real inference and a small similarity-ordering check passed; this is not a broad domain benchmark. |
| HuggingFaceTB/SmolLM2-135M-Instruct | q4; pinned revision 12fd25f77366fa6b3b4b768ec3050bf629380bac | Fast enough for protocol probes, but repeated itself on the reviewed-memory workload and reached the token limit. |
| onnx-community/Qwen2.5-0.5B-Instruct | q4; pinned revision cc5cc01a65cc3ff17bdb73a7de33d879f62599b0 | Completed an actual translation-provider probe; poor source repetition/quality remained. Cached footprint was about 1.1 GB in the earlier measurements. |
| onnx-community/Qwen2.5-1.5B-Instruct | q4; pinned revision 6287331f475a3e20e8c879be8fd4bf3551ad9d34 | Actual tool-use and shorter writing probes worked; browser translation exceeded its 180-second deadline. |
| opencode/big-pickle | Real native OpenCode CLI against the inspected Zen catalog | Later smoke and translation work progressed, but full lifecycle acceptance failed; no full browser proof. |

The user's snowflake-arctic-embed:22m, ArxivSciGenFusionTest and pubmedbert-base-embeddings-100K suggestions remain candidate alternatives within the same service boundaries. This handoff does not claim they were downloaded, benchmarked or selected. Tiny-model protocol readiness must not be equated with adequate editorial or translation quality.

Model warm-up and inference should be separate operations. The local server refused missing weights rather than downloading or selecting a different model during a request. Warm-up retrieves pinned public artifacts; offline inference uses the warmed cache.

### The OpenCode Zen path

The earlier direct Zen HTTP attempt and its access limits are retained in the published model notes. The later successful smoke used the actual Foresight OpenCode child binary and production writing provider, not a fabricated response or a direct replacement HTTP client.

The checkpoint recorded binary source cc4b456, binary SHA-256 be619cb5e4b178100ba707e604e70ea7f8e02d531cab725c9a87d61e95e67be3 and catalog SHA-256 876afae217cdff37c7267ba757d4d375a64eb52cdb2caa24c7f027b5142e3265. The inspected catalog declared the Zen URL, OpenAI-compatible SDK, Big Pickle and zero cost. This is a recorded catalog/response observation, not a promise about future availability or rate limits.

The real smoke returned “sandbox ready” in about 16.442 seconds, with 220 input and 38 output tokens, 258 total, and reported cost zero.

The first structured translation failed after about 28.1 seconds because the response had extra keys. Investigation found that the OpenCode payload asked for schema-shaped output without actually supplying the schema, whereas the other providers included it. The local repair passed the exact translated-text schema into the prompt payload and retained strict one-key validation.

Another local repair explicitly refused unsupported native CLI agent drivers before entering a generic SDK model lookup. That lookup could silently fall back to another provider. Provider identity and cost boundary are correctness requirements: an unavailable requested driver must not turn into an unrelated or paid fallback.

The later full dispatch probe exercised actual dispatcher/FIFO behavior, the native-driver refusal, a Big Pickle completion, canonical Clio EDN persistence, organization exclusion and same-attempt replay/deduplication. Its final cleanup assertion failed because the private CLI session directory reappeared. Candidate progress inside that run did not make the entire run pass.

The adapter launched a detached Linux CLI group. Leader exit alone did not prove descendants were gone, and successful cleanup could race surviving descendants recreating files. There was also no complete adapter-owned shutdown registry. Fixes were in progress, with real process fixtures planned; their completion is unavailable.

The existing model provider can therefore be a viable path for continued work in this same sandbox, but “Zen smoke works” must remain distinct from “all agent tool turns and lifecycle behavior work.”

No ChatGPT session tokens were borrowed for workspace inference, and no OpenAI API key was created or configured for this task. The existence of an OpenAI developer plugin was not treated as an entitlement to API credentials or token usage.

## Mongo, EDN, Clio and service boundaries

A native Mongo 8.0.13 replica-set fixture did run in the same sandbox using TCP, with Unix sockets disabled where necessary. It was useful for existing integrations and for distinguishing “Mongo unavailable” from other failures. It did not satisfy the desired architectural end state.

Canonical accepted-operation replay and event persistence belong to eta-mu/packages/clio. The deprecated standalone event-ledger package was removed from active foundation dependency paths. The root local compiled consumer exercised real Clio append/read validation and rejected corrupt or missing ledgers.

The important design lesson is to replace service ownership dependencies, not just install a Mongo-shaped emulator. Domain decisions should depend on pure contracts and accepted events; filesystem, Mongo, S3 and other runtime providers should be outer adapters. EDN is useful because it makes the accepted history inspectable, but a readable file does not automatically supply concurrency, crash safety, authorization, ordering or replay correctness.

A development event ledger needs explicit answers for append serialization, duplicate operation IDs, replay validation, truncated/corrupt records, acknowledgements, durable completion and tenant boundaries. Mutable projections and caches must remain rebuildable derivatives, not alternate truth sources.

A real S3rver process and real AWS SDK bucket/put/get operations succeeded with an EDN object. Server and client ran in the same supervisor and cleaned up only owned data. This established a container-free local S3 protocol fixture in the existing Node ecosystem. It did not prove every application service already selected that provider.

Lucene is a search/index component, not a replacement for Mongo's entire storage/protocol contract. Separating durable Clio events from disposable Lucene projections is consistent with the desired architecture. No “fully compatible Mongo drop-in” was validated during this attempt. Future drivers should be judged by the protocol methods the product actually uses, with shared contract tests; no database or license claim should substitute for those tests.

Epiphany's final native integration remained red because Lucene/JDK emitted warning output under a zero-warning gate. The investigation tried native-access/module settings in the same JDK: allowing native access removed one linker warning, while enabling the vector module introduced an incubator startup warning. Actual memory-mapped/vector operations worked, but that did not satisfy the configured output policy. Output was not filtered, warnings were not suppressed by a fake test, and the JDK/language/environment was not replaced. The future choice is a deliberate same-stack dependency/invocation correction or an explicitly reviewed gate policy decision; this attempt did not silently make that decision.

## Disk and root manifest lessons

The successful sharing model used one pnpm content store, shared Maven/model caches and hardlinks where supported, while retaining each child's package manager, lockfile and command ownership.

A single indiscriminate flat workspace was not workable: duplicate package identities, incompatible versions, Bun catalogs, React peer identity, generated artifacts and native modules had distinct requirements. The implemented root composition selected explicit inputs. Inventory could report the entire constellation without pretending every nested package was one installable unit.

The generator emitted root package.json, pnpm-workspace.yaml, deps.edn, nbb.edn, shadow-cljs.edn and workspace-manifests.edn from reviewed composition policy and selected child manifests. It rejected unapproved version conflicts and consumed-source drift. It verified selected child HEADs against root gitlinks and refused tracked/untracked changes in consumed source paths. Unrelated uninitialized children and unselected examples did not invalidate the selected root group.

Clojure local overrides must name the actual dependency symbols and be passed in the command's own working-directory context. A root alias does not automatically affect a child launched from its own root.

Git LFS was a major avoidable disk multiplier: Calliope's Git objects were only about 14 MB, but checkout/cache/media hydration consumed roughly 7.6 GB. Source checkout was completed with LFS smudging disabled; media remained explicitly partial. Do not hydrate a corpus simply to run source-level tests, and do not claim complete media availability after a source-only checkout.

At the last full runtime checkpoint, the filesystem was approximately 32 GB total, 22 GB used and 8.6 GB free. Shared Shadow cache was about 3.1 GB and backend output about 146 MB. These are historical measurements, not today's disk usage.

Warm-cache success can conceal a cold-build problem. Muse's generated namespaces required shared prebuild generation, and actual cold host builds were necessary to prove the fix. Conversely, indiscriminately rebuilding every package after an unrelated documentation change wastes disk and time.

## Test and process lessons

| Failure mode encountered | What was learned / repaired |
|---|---|
| Shadow compile/autorun returned zero despite test failures | Compile without autorun, then execute emitted tests and propagate their real exit. |
| Async tests threw before their first assertion | A successful-looking empty/partial counter is insufficient; native guarded failure-first probes are needed. |
| A wrapper recorded native failure but itself returned zero | Propagate the actual code/signal. This happened in the frontend proof helper and was corrected locally. |
| Exit occurred while inherited pipes remained open | Wait for child close, not merely exit, before treating the transcript as complete. |
| A stale successful artifact survived a failed run | Remove prior success at admission; bind a new candidate to a nonce/source snapshot; publish only after observed close and cleanup. |
| Evidence claimed its own future exit | A separate supervisor must record the child's observed final status. |
| response.end queued output but native finish had not happened | writableEnded does not establish network completion. Observe finish/error/close. This remains a current root review issue. |
| Awaiting response finish could itself hang | Preserve a bounded deadline through response flushing and shutdown. Pipelined responses can be queued behind another response. |
| Native model interruption and disposal raced | Join actual inference/network lifetimes before disposing owned resources; reject late admission. |
| Detached CLI leader exited before descendants | Track exact owned process groups; prove close and cleanup with native fixtures. Do not infer closure from an empty directory or a sleep. |
| Shared output directories changed under a running server | Build unique output directories and hash source/configuration before and after proof. ESM server builds use output-dir, not output-to. |
| Network failures lost useful causes | Preserve finite native error name/code/cause classifications, not unbounded messages or secrets. |
| Fixed tiny sleeps made event tests flaky | Wait for observable state with a bounded deadline. |
| A queued older revision blocked the current revision | Carry revision identity through dispatch and observability; cancellation needs durable, correctly scoped semantics. |
| Skipped/todo tests were mixed with passes | State pass/fail/skip/todo counts separately. |
| A lint command returned zero while printing warnings | Read both native exit and configured output policy. Zero exit is not zero warnings. |
| A recorded replay fixture was absent | Fail the selected replay explicitly rather than accidentally reporting an empty/filtered pass or allowing live fallback. |

The frontend CLJS failure was concrete fixture drift: its generated migration inventory did not match the checked-in ledger. Running the repository's own writer added 43 records (41 extracted source paths and two test-suite entries) without changing or deleting existing records. That generated file was committed locally as 1787a63b so the test comparing Git HEAD could see it. A rerun had started when the prior turn ended, but no closed result survived. Therefore the gate remains unverified after repair.

The root model terminal-write follow-up had an intermediate 98-test pass, but independent reasoning found unbounded flush/shutdown and JSON-failure classification problems afterward. That intermediate pass must not be promoted to final acceptance. New native regressions and production changes were in progress; they were not published.

## OpenCode tests and stale cassettes

OpenCode was useful as an actual baseline and provider path, but its whole test surface was not validated.

A bounded fresh expansion initially produced 494 passing tests and 38 module-initialization errors. A circular initialization path involving internal provider plugins was repaired locally by moving the ProviderPlugins import into the Effect generator before service acquisition/registration. The later approved 90-file scope passed 706 tests and 1,444 expectations. Package typechecks, scoped lint and formatting passed. Two Windows early-return test bodies were disclosed; this was not a platform-complete run.

The isolated repair commit was 03d603b7708ed13d60569501c38f8d92de27bf49, with tree 94cd22a5999b6d58a7ada579cee64e1bd0ca106e. It did not replace the Foresight child binary used in the Zen smoke and was not published before cleanup.

The LLM replay audit found 30 missing cassette names. Five appeared to match existing cassette requests exactly after source-level comparison: Haiku, Gemini, and Together/Groq/OpenRouter compatible-chat cases. Eighteen had actual prompt/limit/default drift; seven had no corresponding recording. The matcher and recorded bytes should not be edited to make a different request appear identical.

The next authorized local task was to add only the five verified aliases, make missing selected replay a named failure before transport construction, and run strict RECORD=false replay with no credentials or live fallback. It was expected to expose at least 25 remaining failures. No final result of that task is available.

An earlier automatic approval rejection involving unverified private-host traffic was respected. The rejected Proxx/OpenCode executions were not resumed, polled, stopped or relaunched indirectly. Separately owned fixtures and fresh bounded executions were used where authorized. A blocked old run must remain a distinct historical fact.

## Identity and CMS lessons

Axxium was being separated from Knoxx's direct identity responsibilities. Tests and browser work covered native identity paths and selected ceremonies, but a local ceremony or provider selector is not the same as completing external OAuth consent with configured clients and real accounts.

GitHub, Discord, Google and ATProto need provider-specific registration/redirect/account setup. ATProto requires its own OAuth pattern rather than pretending it is interchangeable with a generic GitHub flow. Password, PGP and passkey paths have their own enrollment, challenge, revocation and session behavior. Shared roles should be enforced at the domain boundary used by both UI and tools.

Startup concurrency repairs were necessary: health must not falsely claim completed readiness, concurrent initial bootstrap must not create incompatible authority, and ATProto key material must have one durable identity. Failure-first tests caught races that sequential startup tests missed.

Fresh Axxium review now reports incomplete credential revocation: an ATProto SDK session can remain available, and local private credential blobs can remain unreclaimed. These are current findings, not solved by the earlier startup fixes.

The CMS's useful invariants survived the debugging effort: acceptance belongs to a specific source revision; translation candidates belong to their manifest/splits/locale; stale acceptance must not authorize publication; real provider output must pass the declared shape; human and agent mutations use the same authorization and update the open view through observable events.

Cancelling “older work” is not automatically correct. A historical pinned attempt can still be lawful, and a naive abort can enter settlement logic that starts structured generation again before releasing FIFO. A safe cancellation design needs exact tenant/document/garden/locale/attempt/run/event/source identity and a durable disposition. This was investigated but deliberately not completed as a shortcut.

The final end-to-end acceptance still needs two complete cycles: English review and acceptance, translation review/revision/acceptance, publish, record/reuse lessons, edit source, refuse old acceptance, translate/review again and publish the new accepted version. Browser22 did not prove that story.

## Review outcomes and self-review

Actual Codex, eta-mu and CodeRabbit reviews were used; their findings were not replaced by local agent impersonation. Reviews repeatedly found problems after locally green runs: source ownership in generated manifests, peer React identity, cold generation, optional JS arguments, authentication races, watch handles, status/encoding mismatches, late process output and stale receipts.

The fresh audit has new blocking findings. At the root application head, Codex identified asynchronous terminal response failure being reported as completed. Eta-mu also reported repeated Ajv construction overhead and trailing whitespace in captured test evidence causing diff-check failure. The latter should be corrected without rewriting the historical raw evidence as though it had always been clean; a clearly identified normalized presentation or new evidence artifact is preferable to pretending the original capture differed.

Axxium has credential-revocation findings. Epiphany has non-atomic registration-request admission and overly narrow candidate request-UUID lookup findings. Rheos is conflict-blocked even though the current automated review comments were clear. A green wrapper or a draft-skipped job is not proof that its actual review ran.

The agents therefore did not “run out of issues.” Auto-merge would misrepresent the state, and the user has now requested a handoff instead.

My execution also had process failures:

- I left communication gaps long enough that the user repeatedly thought the session had frozen. A long-running tool does not excuse missing status. Updates should identify the currently running stage, last observed progress, deadline, and whether the process actually closed.
- I let the task expand across too many interdependent repair lanes before consolidating a reviewable checkpoint.
- I delayed publication of substantial local work. Local commits and transient reports were insufficient protection against workspace pruning.
- I sometimes treated the next broad run as the way to learn a narrow fact. Provider, cleanup and queue proofs should have been isolated before another full browser tour.
- I initially archived a lint transcript before its process had closed, producing an empty receipt. A later local correction preserved the actual completed log and explicitly documented the mistake. That correction itself was not published before cleanup.
- I needed to distinguish “all children initialized,” “a service starts,” “a test subset passes,” and “the complete stack is accepted” more consistently.
- Review convergence needed explicit per-head state and a finite handoff policy. Repeatedly asking for another broad review without first collecting exact current evidence created churn.

These are lessons about execution and evidence ownership, not reasons to change the requested language or sandbox.

## Local-only work that must not be mistaken for published work

The following identifiers come from the prior checkpoint. They are recovery leads, not available GitHub commits or claims that their source is included in this handoff. A GitHub lookup of the frontend local commit 1787a63b returned “No commit found.”

| Local identifier | Recorded change / proof |
|---|---|
| 634c4043 | Discord work, 14 paths; 21 tests / 88 assertions. |
| 889f8e39 | MCP/contracts, 16 paths; 93/374. |
| e2fa7cbb and db6e6416 | Memory/filesystem, 28 paths; 45/189, plus explicit catch/boundary-debt correction. |
| c3c0c5b9 | Media work, 13 paths; 13/47. |
| 7a1e7808 and a64fe210 | Source queue; 37/252 and failure-first proof, isolated ESM output repair. |
| 62c83d47 | Exact legacy defroute fixture and deliberately isolated warning-negative proof. |
| 55ddb471 | Unused requires / Mongo helper; 12/50. |
| 39bada6c | Process-warning regressions; 16/78. |
| 4fb59160 and 14802be5 | Runtime infrastructure slices, 53/212 and 64/220. |
| 9abd144d | Publication slice; 499/3,093 twice plus separate regression. |
| 1308fa38 and dd371170 | Configured static evidence and introduced optional-linter repairs. Broad optional debt remained. |
| 009a2394 and 14496816 | Browser/MCP diagnostics and correction of prematurely captured empty lint evidence. |
| 427d1fd543c70690778b2c99933a6576cd0cde47 | Embedding native-cause diagnostics; native 7/36, clean compiled/static scopes. |
| 1787a63b | Generated frontend migration manifest, 43 added records; rerun completion unknown. |
| 03d603b7708ed13d60569501c38f8d92de27bf49 | Isolated OpenCode provider-cycle repair and bounded 706-test evidence. |

Uncommitted work included optional real-Zen browser composition, visible translation waits, provider schema/driver refusal changes, detached CLI lifecycle changes, and root terminal-response deadline/shutdown fixes. The Rheos ordinary merge with the Axxium successor had metadata conflicts being resolved; no final published integration result is available.

The exact local Clio/Axxium combined candidate 82da7b2a500ed69eb1c241b3d93a1a12a0e6c365 had the same tree as published Axxium 33852976fd7d30afc2fc28cee62d2bacca866a81. That published source is recoverable. It should not be confused with unpublished Knoxx changes that consumed it.

## Artifact recovery inventory

The prior saved report was version 20, 154,153 bytes. Browser22's archive was 6,422,664 bytes with 51 files, an index, an HTML gallery, 33 annotated screenshots, and raw browser/backend/model logs and snapshots. Earlier archives covered browser21 plus child evidence, and browser19–20.

Their exact previously recorded file identities and recovery limitations are in recovered/recovery-status.md. This handoff does not say those archives were downloaded or uploaded today. No replacement screenshots were generated, reconstructed or presented as actual browser evidence.

Published root documentation was recovered through authenticated GitHub reads and verified against its Git blob identities. It is included as source material in the uploaded evidence package. The fresh PR audit and published root/Knoxx metadata are also included.

The practical recovery order is: published source and PR attachments first; authenticated saved-artifact retrieval when available; only then attempt to recover local-only changes from an actual surviving bundle/object store. Do not regenerate a missing patch from memory and claim it is the original tested code.

## Continuing later in this same sandbox

The current handoff runs no new browser/build/test suite. To resume later:

1. Restore the published repository heads and declared submodules; materialize saved evidence through an authenticated supported path. Record which local-only changes cannot be recovered.
2. Restore the declared runtime bundle, trusted CA/proxy setup, shared caches and pinned models. The old activation script path is now absent; do not assume it exists because documentation names it.
3. Use the root's declared manifest generator and checks. Populate dependencies once, retain child ownership, and avoid incidental LFS hydration.
4. Repair current review findings in bounded slices, with native failure-first tests for response completion/shutdown, credential revocation and request-identity races.
5. Publish each slice before starting another long run. Record exact source/configuration/build output identities and complete raw exit evidence.
6. Recover or redo the frontend generated manifest repair, then obtain the actual completed CLJS gate. Run the whole combined backend only after its dependency roots and provider source are coherent.
7. Prove one real translation through the production provider, canonical Clio settlement, replay and exact owned-process cleanup. Keep real embeddings separate from hosted generation.
8. Address the explicit Lucene warning gate and remaining OpenCode replay fixture gaps without filtering failures or changing languages/environments.
9. Only then resume the full browser publication story if the user wants it. Keep visible progress, one supervisor, isolated output and a short failure-state capture.
10. Re-run actual reviews for the published successor commits. Merge only after the required checks and findings are truly clear.

Published root commands are documented in the recovered workspace guide: manifest generation/check, frozen shared install, build, test, lint, S3 and explicit model smoke commands. Child commands remain owned by the child projects. Those documented commands were not executed during this handoff.

## What this handoff delivers

A reviewable draft PR entry point, a precise publication/recovery boundary, an experience report with both successful and unsuccessful attempts, fresh child review status, and recoverable published source notes in the requested Drive folder. The full browser publication goal remains unfinished, and the missing local-only code and screenshot archives are explicitly disclosed.
