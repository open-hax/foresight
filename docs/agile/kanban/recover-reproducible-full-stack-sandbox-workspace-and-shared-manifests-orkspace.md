---
category: "kanban"
dependency: []
type: "task"
write-id: "1789232991926-0.tg6x7gyn8ux3f70rhw"
title: "Recover reproducible full-stack sandbox workspace and shared manifests"
priority: "P0"
status: "in_progress"
uuid: "foresight-wiki-sandbox-workspace"
created_at: "2026-09-12T05:33:06.401Z"
---

## Intent
Recover and initialize every Foresight child in the existing sandbox, with a reproducible shared workspace and an honest obstacle/evidence ledger.

## Acceptance criteria
- Restore all declared child checkouts without overwriting active work.
- Generate portable top-level pnpm/Clojure/NBB/shadow manifests from canonical child declarations; surface incompatible dependency versions explicitly.
- Preserve child package-manager ownership and reuse shared caches and hardlinked dependencies.
- Route root build, run, test and lint commands through declared quality gates; distinguish missing, failed, timed out and passed checks.
- Run the Wiki CMS with canonical Clio and Axxium identity, actual local/Zen models, annotated browser evidence and linked reviewed PRs.
- Publish recoverable source checkpoints and record every obstacle, fix, remaining constraint and concrete workaround in this same language/environment.

---
Source recovery: all 14 direct Git submodules are initialized; 11 newly restored children match declared pins and eta-mu/Knoxx active branches are preserved. Calliope Git LFS media triggered a 120-second checkout timeout and gigabytes of media materialization; source-only recovery with GIT_LFS_SKIP_SMUDGE=1 passed after preserving the interrupted index lock. Six deterministic root manifests, selected dependency conflict checks, canonical Clio compiled boundary, separate Node test exit enforcement, and local S3 round-trip are implemented. Evidence: recovery-child-sources.json, recovery-calliope-source-no-lfs-retry.json, recovery-root-test-final.json, recovery-root-build.json, recovery-root-lint.json, recovery-root-s3.json, recovery-root-frozen-install.json. Child suites remain individually unverified in this recovery stage.

Root gate coverage now also includes the pre-existing evidence CLI and repository census suites. The generator refuses a missing child .git boundary or Git ownership inherited from the parent, fingerprints all child deps.edn inputs, and emits readable sorted EDN. Root manifests do not alter or broaden config/quality-gates.edn; declared workflow-only/external gates remain unavailable for automatic local promotion.

Additional bounded recovery evidence: Calliope clojure -M:test passed 61 tests / 219 assertions; shx clojure -M:test passed 19 / 46; Truth ./bin/test unit passed 822 / 12019. Bitch-tracker passed lint, 2 / 7, build and export verification. Katamorph passed 155 / 371 CLJS assertions and JVM tests, but its test compile emitted a namespace/var clash warning, so it is not a clean warning-free gate. Calliope PROCESS.md requires a test receipt; one append-only receipt was written without changing prior bytes. Whole-repository static/integration gates remain separately unverified.

Shared model integration: @huggingface/transformers 3.8.1 added to devtools and generated root dependencies, with zero downloads during pnpm installation. Pinned q8 MiniLM CPU/offline provider is in devtools/embedding-server.mjs; real HTTP vector/base64/input-refusal smoke passes via pnpm test:embeddings using FORESIGHT_MODEL_CACHE. Final root lint and frozen offline install pass. Scope remains model runtime readiness, not a claim that the small model meets research or writing quality requirements.

Current bounded recovery closeout: Truth and shx full bin/analyze --strict gates pass with their owning pinned kondo versions. Epiphany lint passes and its unit suite passes after same-checkout history recovery; existing SLF4J warnings remain. Proxx builds, passes 114 CLJS tests / 304 assertions, and answers local /health, but its lint is red from generated externs plus existing source warnings. Uxx builds and typechecks but React test resolution remains red. Muse has a working opencode-plugin release and 184 / 470 tests; its stale app target remains broken. Calliope tests pass but generic kondo finds one unused runtime binding. Root final lint includes pinned Oxlint 1.60.0: 93 rules, zero findings; final frozen offline install passes. All evidence remains in recovery-*.json/logs and recovery-workspace-checks.json; none of these subsets authorizes whole-stack promotion.

Correction to the preceding closeout comment after inspecting the full Epiphany lint summary: clojure -M:lint returned exit 0 but printed 66 warnings. It is warning-blocked, not a passing lint gate. This is another observed exit-status mismatch and must be fixed in the owning lint launcher as well as the findings. Root lint remains zero-warning; no child warnings were suppressed.

2026-09-12 08:25 UTC checkpoint: root generator regression reproduced three failures plus missing-child failure before fix; final root114tests511assertions, source-bound build31.84s, lint0/0 and frozen install pass. Model HTTP successor671d95c and mapped root test:model-http pass7 actual offline HTTP regressions; original server copies reproduce6failures; null schemas400, timeouts504, oversized request draining/socket reuse, Qwen and MiniLM smokes, JSDoc5/5 verified. Root published PR91 currently018d04e4. Primary eta-mu promoted to exact published Axxium a7b19825fb5d7c624c38f1d41043c42e92d7f0c3; package66tests632assertions, both118-file releases/lint/realTCPpass, strict inherited boundary56violations remains red. Complete Knoxx backend1835tests8278assertions plus26JS pass; actual EDN no-Mongo browserfirstwholeEnglish/Spanish publication passes but secondcyclefailed rawcandidate repair selecting reviewedreceipts. Regression failure-first4fail then9tests78assertionsgreen; awaitingfullrerun. Knoxx golden959f179 decomposed sixisolated cumulativePRstages, noKnoxxPRpublishedyet. Actual reviewers continue actionablefindings; no merges. Report and annotated evidence saved with historical failures intact.

---

Current bounded successor: full backend09 passes1857/8422 with guarded exit0;
frontend production06 passes532/2306 and advanced188 inputs/zero warnings.
Fresh pinned Proxx passes650/651 with one pre-existing absent external-script
skip, no refused transports, build green and ESLint0errors/236warnings; current
remote-main integration is separate. Root1d83089 closes the reviewed prepended
exit-hook gap with authenticRED2 then nine native tests green. Root46c2e8b owns
Mongo6.21.0 directly in devtools and validates real embedding tensor row count,
384dimensions and finite Float32 components; ten actual bad-output regressions
fail before the repair, all71 model HTTP tests pass after it. The isolated
manifest checkout matches declared eta a7b19825 exactly; generated manifests and
frozen installation pass without bypass. Real Mongo/S3rver/MiniLM JVM integration
passes22/108 with Knoxx absent. Details and exact receipts are in
`docs/notes/model-provider-boundaries-2026-09-12.md` and its evidence JSON.

Browser13–15 failures and annotated screenshots are retained. Browser16 now
passes actual Contracts validation/save/clone, refusal of invalid saving with
unchanged persisted EDN and preserved draft, and responsive controls; Wiki
translation/publication is still in progress. Automatic translator agent setup
also exposed a missing eta-mu provider mapping, separately from the working Wiki
text-provider mapping. This is an unresolved integration gate, not model success.
Final reviewed dependency pins, regenerated manifests, whole-stack browser
completion and actual reviewer convergence remain required before merge.

---
Recovery checkpoint: workspace maintenance removed the prior active checkout/tools/builds/models. All14 declared child repositories restored, root published07440a348964736d149b639092052ba8c68999a8 exactly matches tested tree968c61893a28b4c6b18c17ff9522132d4ee4c7b9. Actual root full test125 Clojure/CLJS tests607assertions plus17Node tests; build/test compiler0warnings and lint0/0. Codex transport/source-input findings reproduced before fixes;13native transport and3actual Git/supervisor admission cases now included in generated pnpm test. Same pinned runtime, real Mongo TCP insert/read and Chromium interaction restored. Later unpublished Knoxx checkpoints remain lost and require new proof. Reportv16 and runtime recovery archive are saved; complete browser publishing/reviewed child promotion remains pending. AJV8.17.1 added to devtools and generated root manifests with frozen lock because real generated tool arguments must be checked against the offered full JSON Schema without coercion/defaulting; model streaming implementation is separate ongoing work. No old rejected process resumed; no broad merge release.

---