---
category: "kanban"
labels: "colab, research, gemma, retrieval, budget"
dependency: []
parent: "da84a64d-f118-4722-8709-a1c556034f90"
type: "task"
write-id: "1793212238664-0.stda7pfwq7hmlv0gy1"
points: "5"
title: "Qualify the bounded Colab Gemma retrieval research pilot and retain production holdback"
priority: "P1"
status: "incoming"
uuid: "qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback"
created_at: "2026-10-25T14:15:02.221Z"
---

# Bounded Colab Gemma research pilot

## Outcome

Measure 24 frozen, source-linked creative retrieval cases, then run an isolated
candidate research cycle under the operator's total 30-compute-unit ceiling.
Candidates remain separate from production until the relevant Stealth and Yoga
runtime behavior is tested and adoption is explicitly authorized.

## Scope

- Freeze actual corpus vectors and analyst source labels before inspecting scores.
- Distinguish exact scoring, native ANN/ENN, image retrieval and audio metadata lookup.
- Use pure CLJC budget law with foreign CLI/SSH/scientific Python adapters.
- Run a finite 18-candidate matrix on owned Colab CPU sessions; download, hash-check,
  explicitly stop, and verify server-side endpoint absence.
- Replay candidate scoring on both machines and retain production holdback.
- Probe actual E4B RAG behavior separately from embedding/scorer quality.

## Non-goals

Production index/model/actor changes, gateway identity repair, uncontrolled GPU
spending, raw conversation cloud upload, automatic production adoption, or claims
of human-adjudicated relevance or native Yoga embedding inference.

## Acceptance criteria

- 24 cases retain source IDs, hashes, anchors, split and provenance.
- Actual baseline, candidate metrics and one selected locked-test evaluation persist.
- Budget state survives restart, serializes concurrent runners, fails closed on
  invalid telemetry or uncertain cleanup, and does not refund reservations.
- Real Colab output, downloaded artifacts and verified teardown are independent facts.
- Stealth and Yoga input/code fingerprints and source rankings are compared.
- Candidate promotion remains held when any relevant runtime requirement is unproven.

## Verification

Research artifacts: `docs/research/colab-gemma-pilot/README.md` and
`docs/research/colab-gemma-baseline-and-pilot-2026-10-25.md`.
Private evidence root:
`/home/err/.local/share/promethean/research/colab-gemma-20261025/`.

Implementation and real pilot execution occurred before this card was admitted;
this card records the resulting research/adoption work and makes no retroactive
planning-review or production-readiness claim. Board status is owned by Rheos.

---
Observed pilot: frozen 24-case baseline top-five19/24 nDCG0.7471; validation-selected isolated512d+lexical candidate21/24 nDCG0.8489. Real Colab CPU jobs research-b4cb05578e3a and research-f51b6a967496 completed with verified owned endpoint release. Campaign cap30, conservative charged reservations4, displayed account399.54 and no active assignments. Automatic scorer replay agrees on all24 source rankings/top-ten lists on Stealth and Yoga using frozen query vectors. Native ANN13/24 with eight empty queries; native exact19/24. Warm real E4B on Stealth correct candidate source2/2 but exact quote1/2; Yoga complete usable output0/4 by20seconds. Candidate and source remain unadopted; no production changes or readiness claim. Full report docs/research/colab-gemma-baseline-and-pilot-2026-10-25.md.

2026-10-26 followup: identical historical ANN requests now return candidates for24/24; original HNSW internal event remains unproven. Confirmed SDK defect: successful empty ANN treated as authoritative even for populated filtered scope. Isolated OpenPlanner candidate398d258 adds same-filter existence probe and bounded native ENN recovery, then explicit inconsistency error if still empty. Native tests passed StealthMongo8.2.7 andYoga8.3.4 using same compiled a8711517 artifact:24 healthy SDK result lists unchanged,8 historical-empty response replays recovered via native exact,real missing-index silent-empty rejected,empty project preserved,wrong-project canary excluded,owned DBs removed/readback verified. Six regression plus19 existing embedding/vector tests pass; SDK andCLJSdependencies build; independent local all-file review approved. No production activation. Broader gates remain blocked by existing SDK lint warnings218baseline/213candidate and identical no-new-TS inventory failure; no new TS file. Same campaign4/30 reserved,no additionalColab allocation. Source pushed isolated branch codex/ann-empty-recovery-20261026 in riatzukiza/openplanner; findings and human verifier in docs/research/native-ann-empty-recovery.md.

2026-10-26 gate-repair checkpoint: isolated codex/ann-empty-recovery-20261026 adapter strict lint0warnings/errors; inventory558active787entries0unlisted/stale/errors;34tests andfullrootbuild pass. Same5-filecompiledartifact2e8cc585b0fcc5a52242d64d804a23bd463ab9b0381bede28a9367146ed48b44 passed nativeStealth8.2.7/Yoga8.3.4:24healthyIDs unchanged,8capturedemptyreplays recovered,realmissingindex rejected,tenantcanaryexcluded,ownedDBcleanupverified. Fullrepository strictlint stillred0errors7814warnings. No done/adoption/merge/deploy. Same4/30CU reservation,0newallocations;2hourappheartbeat continues boundedwork/reports. Report: isolatedOpenPlanner docs/research/ann-gate-repair-2026-10-26.md.

Checkpoint published as 11a22b07a96683ccf7672cb7ba137fd73ff519e0. Adapter lint, SDK inventory, 34 tests, full build and native checks on both hosts pass; independent review approved all twelve changed files. Full repository strict lint still fails on 7,814 warnings. No adoption or production action. A two-hour app heartbeat continues bounded repair and research with dated reports, under the original 30-unit cap (4 reserved; no allocation this stage). See OpenPlanner docs/research/ann-gate-repair-2026-10-26.md.

Published isolated checkpoint 3021d41115342ae092509bab768e9db174a03a8c. Tiered-ranking cleanup removes 31 warnings (7,783 remain; strict global gate still red). All 192 frozen synthetic compatibility cases and 40 focused tests pass; independent review approved all five changed files. Exact compiled utility checks pass on Stealth and Yoga with cleanup verified. SDK build, root typecheck, inventory and changed-file strict lint pass. No new Colab allocation; original cap remains 30 units with 4 conservatively reserved. Production remains held. Report: OpenPlanner docs/research/retrieval-lint-tranche-2026-10-26.md. Next: version the E4B quote-fidelity/latency experiment and continue bounded gate repair.

Published isolated E4B quote-preparation checkpoint cf4b67d19ac3c12ff6f4f9f0d4c234a6c614ccae. Private eight-case fixture is frozen (4 development, 4 validation, no locked cases), with fixed prompt comparison and balanced order. Both hosts passed the exact source/fixture manifests and eight law tests with 69 assertions; clj-kondo is clean. No inference or cloud allocation occurred. Current CPU resource admission is denied on both hosts; the worker/stream adapter and context-retention proof are still incomplete. Same original 30-unit cap, four reserved. Broader lint remains held at 7,783 warnings. No adoption or production action. Report: OpenPlanner docs/research/e4b-quote-preparation-2026-10-26.md.

Published isolated bounded-stream checkpoint f0d5899cd6a69e31e3c5701936394fcd070baabb. Both hosts passed exact executable manifests, 18 synthetic HTTP tests and nine CLJC tests with 74 assertions; independent review approved all eight changed files. Absolute deadline/cancellation, cleanup and body/output/framing bounds are verified against owned fakes only. No model inference or cloud allocation. The worker/controller and token-retention proof still block a smoke. Same 30-unit cap with four reserved; production and global lint holdback remain (7,783 warnings). Report: OpenPlanner docs/research/e4b-stream-adapter-2026-10-26.md.

Synthetic lifecycle checkpoint 61f193750976cc3b4bd66fb007e49cad7c4e8288 is isolated and pushed. Independent nine-file source approval plus final exact manifests: Stealth and Yoga each passed 14 worker methods, 18 fake HTTP methods, 10 CLJC tests/99 assertions; no fixture units remain. Four review findings fixed: durable attempted nonce, interrupted lease writes, child cgroup lifetime and stale readiness. SSH requester loginuid1002 differs from observed manager-child1000. No model inference or Colab allocation; same four conservative units reserved of30. Exact tokenizer/native-template retention, admitted model/cache/endpoint design and resource admission remain pending. Full strict lint still0errors7783warnings; inventory558active787entries passes. No Done, adoption, merge or production action. Report docs/research/e4b-worker-lifecycle-2026-10-27.md; private qualification ann-followup/e4b-worker-v1/qualification.json.

Offline token-retention checkpoint 7a22f5a7af2b2aa4dd014ee1086e4d64385de224 is isolated and pushed. All eight public files and two private harnesses independently approved; final exact manifests passed on Stealth and Yoga: 16 frozen frames, 10 negative probes, 11 CLJC tests/130 assertions and 18 fake HTTP methods. Full prompts use 1,481–3,047 tokens; complete byte roundtrips and native BOS token-ID paths agree. Maximum plus 256 output and one native slot leaves 792 slots in 4,096. This qualifies only the copied offline research artifact profile; no live server retention, generation quality or production readiness. CLI version calls were metadata API observations, not verified installed ELF versions; retained correction and final zero-server-call harness. Global lint remains zero errors and 7,783 warnings; inventory passes 558 active files/787 entries. No inference/cloud allocation or production action; original four conservative reservations of 30. Next bounded SDK lint tranche with parent behavior and public mutability compatibility, alongside owned model/cache/endpoint design, live accounting and fresh admission. Report docs/research/e4b-token-retention-2026-10-27.md; private qualification ann-followup/e4b-token-retention-v1/qualification.json.

Isolated candidate eb3b5f4f47e5dafb076d9799e11a042e381e6975 removes 239 public type-contract warnings without changing runtime ASTs; full strict lint remains zero errors and 7,544 warnings. One existing mapped-type exception moved to leaf DTO module, old subpath preserved; no added suppressions or TS inventory allowances. Independent seven-file source review approved; root build/typecheck, 33 focused tests and inventory 558/787 pass. Frozen parent checks preserve 27 legacy exports/309 mutation operations; 12 contract methods also detect literal/template oracle regressions. Final Stealth exact manifest passes 12 tests with cleanup. Yoga remains unqualified: No route to host on direct harness alias; exact stage-final prepared and protected for later transfer/replay. Private recovery pending Yoga, no scratch removed. Same four conservative reservations of original 30 units; 08:16 usage zero rate/assignments, no inference/cloud/production action. Report docs/research/sdk-public-contract-lint-2026-10-28.md; private qualification sdk-public-contracts-v1/qualification.json. Task stays in progress; no adoption waiver.

Isolated CLJC embedding-model law tranche ac450fe4e16c03f3e414e078cd78decd6101a33f: 12 warnings removed, strict lint now0errors/7532warnings (gate held). Exact parent fixture208parse+128scope cases,10 boundary methods,2portable tests/13assertions per BB/NBB,33 prior focused methods, full root build/typecheck and inventory558/787 pass. Independent12-source review+report addendum approved; three review findings fixed (JSON assignment fallback, optimized-Python stage preservation, pinned test module with poisoned inherited override). Exact Stealth stage-v3 passes10 and cleanup. Yoga directNo route; both SDK stage-final and new policy stage-v3 remain unqualified there. Five ANN compiled hashes unchanged; no native database rerun. Original30unit campaign4conservative reservations;10:19usage399.54/rate0/assignments0/directempty, state unchanged. No inference/allocation/production action. Evidence/backup pendingYoga, no scratch reclaimed. Report docs/research/embedding-model-law-2026-10-28.md; private qualification ann-followup/embedding-model-law-v1/qualification.json. Global lint/Yoga/model/actor/adoption gates open.

Isolated endpoint preflight b5b37604f4f8967f60c2cda041ecbdabcec948a8 independently approved/pushed.12observer methods,4CLJC laws/58assertions,11prior laws/130 and18HTTP tests pass on exact Stealthstage-v1, UID1000/loginuid1002. Reads bounded targetPID/startticks/UID/cgroup/netns/FDs/IPv4listener metadata, joins two already-pinned controller snapshots, never starts/stops/connects a model. Realowned3second listening child observed and reaped; controller join uses mocked manager facts. Not a real managedlistener/model readiness/atomicconnect/exclusivesocket/sameUID boundary. InstalledStealthELF bytefingerprint+Go metadata captured without executing target; release/sourcecommit remain unknown. Globalstrictlint0errors7532warnings/inventory558/787 unchanged; largestSDKtype/interface risks documented, no waiver/massfix. YogaNo route and all three exact phases unqualified there; preserve evidence, no scratchdeleted. Same30totalunitcampaign4conservative reservations;12:07balance399.54/rate0/assignments0/directempty, stateunchanged; no inference/allocation/production action. Report docs/research/e4b-endpoint-preflight-2026-10-28.md. Next bounded SDK purepolicy and real managed syntheticlistener/requesttime join; cache/backend/live-token/resource/model/actor/adoption gates remain.

Isolated indexing-batch law 3062e47aa7869ee25ad43b23998aea10345a7a26 independently approved and pushed.28warnings removed: strictlint exit1 with0errors7504warnings (held/unwaived); editedindexing61->33existingwarnings. CLJC owns defaults, heuristic and immutable batch transition; thinboundary retains lazy native limitcoercion/IteratorClose/objectidentity. Parent128batch+10heuristic+6wholeDocument cases,23unaffectedruntimeAST statements and5publiccontracts/21mutableclientoperations pass.9newcompiledmethods on exactStealthstage-v3,1186manifestentries,Node22.22.2/TS5.9.3,nonroot/no-network/readonly, cleanupverified. BB/NBB2tests9assertions each,kondo/newJSstrict0/0;43priorfocusedmethods,rootbuild/typecheck/inventory558/787pass. Initialiterator.call mismatch andoracle errors corrected; preliminary64KiBmetadata truncation retained, finalcompactmanifestdigest/count validates. All5ANNcompiledbytes unchanged; no nativeDBrerun. YogaNo route, all4phaseartifacts remainunqualifiedthere/protected; privatebackup pending, no scratchreclaimed. Sameoriginal30unitcap4conservative reservations;14:19usage399.54/rate0/assignments0/directempty/stateunchanged. No model/cloud/productionaction. Report docs/research/index-batch-law-2026-10-28.md; nextremainingindexinglaw andrealmanaged syntheticlistener/controller/preflightjoin, retainallmodel/cache/backend/token/resource/actor/adoptiongates.

Isolated managed-listener integration 16c57038e18954980e5c1db87ae0fde153fdb1d9 reviewed/pushed. Real transient fixture underexistinguser-manager nowjoins controller invocation/MainPID,procUID/cgroup/netns/listenerFD andfixed boundedHTTPtransport onStealth.6newlivemethods+14legacyworkers+12observers+18HTTP,5laws70assertions+11priorlaws130 pass. Actualstopafterfirstproof preventsHTTP; foreignport/staleinvocation denied; arbitrarybody400; managerexpireslistener2s. Same64MiB/zeroSwap/8tasks/25%CPUprofile, oldplansunchanged. All19ownedleaseidentities absent (includesnever-submitted fixtures, not19starts). Childmarkerloginuid1000/requester1002 distinct; no central-auditclaim. No atomic/exclusivesocket/sameUIDsecurity ormodelauthorization claim; onlyonefixedfixturebody, zero model/cloud/productionactions. Strictlint0errors7504warnings held,inventory558/787 andfiveANNcompiledidentitiesunchanged; no nativeDBrerun. Original30unitcap4conservative reservations;16:11usage399.54/rate0/assignments0/directempty,stateunchanged. YogaNo route; fiveexactphaseartifacts andprivatebackup remainpending/protected, no scratchreclaimed. Report docs/research/e4b-managed-listener-2026-10-28.md. NextboundedSDKpurepolicy andseparatelyreviewedmodelcache/backend/launch/live-tokenprofile withfreshadmission; noDone/adoption/merge/deploy/restart/routingchange.

Packing/offset tranche 8111d06d78319a422c89c5a478597d856450cecd is independently reviewed and pushed. Twenty warnings removed: root strict lint exits1 with0 errors/7,484 warnings; indexing retains13 existing warnings. CLJC owns packing, overlap and cursor transitions; native boundaries preserve object literals, trim/coercion order and UTF16 search. Parent evidence:192 packing rows/144 distinct inputs,3 offset sequences,7 full documents; all3 earlier fixtures unchanged and21 unmigrated runtime statements still guarded. Five public contracts and30 mutable operations compile against both frozen parent and candidate with separate negative checks. Exact Stealth stage-v2 (1,189 entries) passes19 methods under nonroot/no-network/readonly bounds; cleanup verified. BB/NBB each4 tests/20 assertions,43 prior regressions,root build/typecheck,inventory558/787 pass. Two review findings were repaired: inherited offset setters and emission-before-overlap effects. Parent imports are pinned to the earlier artifact, not the live candidate. ANN5 compiled bytes unchanged; no native DB rerun. Yoga remains unreachable and six exact phases remain pending; artifacts/dependencies protected, no scratch reclaimed. Original30-unit campaign retains4 conservative reservations;18:24 balance399.54,rate0,assignments0,directempty,state unchanged. No model/cloud/production action. Report: docs/research/index-pack-law-2026-10-28.md. Next: owned model cache/backend/launch/live-token profile and remaining SDK law, with resource and human-adoption gates held.

---