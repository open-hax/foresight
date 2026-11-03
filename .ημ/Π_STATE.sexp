;; Π_STATE.sexp - deterministic Foresight handoff
;; Generated: 2026-11-03T11:56:01Z (pre-commit pass)

(pi-state
  (version 1)
  (timestamp "2026-11-03T11:56:01Z")
  (repo "open-hax/foresight")
  (branch "plan/fork-dev-origins")
  (snapshot-parent-head "de9ca9dac95ed8e73f7a4782578effd3e54150cf")
  (snapshot-commit "35319da5a3e4159d7647a132729ac7917b36977b")
  (tag "Π/foresight/20261103T115601Z")
  (ahead-of-remote-before-snapshot 169)
  (push
    (branch "origin/plan/fork-dev-origins b0ea7900..35319da5")
    (tag "Π/foresight/20261103T115601Z new tag pushed")
    (receipt "a74f3b73-c340-43ae-b176-6637b0e4f25c"))
  (status "pushed")
  (verification
    (pass "nbb scripts/project.clj validate: 24 workspace sources, 23 submodules, 39 invariants")
    (pass "nbb -cp scripts:test test/workspace_test.cljs: 24 tests, 118 assertions, 0 failures")
    (pass "clj-kondo --lint scripts test: 0 errors, 0 warnings")
    (pass "python3 -m py_compile scripts/archive-scratch.py scripts/restore-drive-media.py docs/research/colab-gemma-pilot/*.py")
    (pass "node --check scripts/start-knoxx-identity-local.mjs ecosystem.identity.local.config.cjs")
    (pass "git diff --check")
    (visible "workspace tests report opencode lint/jscpd spawn ENOENT as ERROR lines inside a passing run; pre-existing host tooling gap, not a snapshot failure"))
  (submodules
    (note "only gitlinks whose new commit is already contained in a pushed remote branch were absorbed")
    (absorbed
      (agents "2a62d58ae1945c99d3a7faf757d5114d71567cf2" (branch "device/stealth") (proof "ancestor of origin/device/stealth, local behind 15"))
      (axxium "603ea4cd88f589c621234f73d3afd6e4e08788b8" (branch "feat/did-actor-identity-local") (proof "ancestor of origin/feat/did-actor-identity-local, local behind 2"))
      (bitch-tracker "693f1388e61bd268814f85d1a40ef15a4485e6a3" (branch "device/stealth") (proof "ancestor of origin/device/stealth, local behind 15"))
      (calliope "b473c21251d3281eae942c5bddfef829b37d1182" (branch "device/stealth") (proof "equals origin/device/stealth"))
      (katamorph "0271e84ab7b7dab179d5b5360bd836604dae9dda" (branch "device/stealth") (proof "equals origin/device/stealth"))
      (knoxx "40221a69f7fff675b46614d9ab318b4ef52f3786" (branch "feat/axxium-identity-local") (proof "equals origin/feat/axxium-identity-local"))
      (shx "5dbac4fde8b3626316c71e15b48b9fe62a5802c2" (branch "feat/supervisor-ir-card") (proof "ancestor of origin/feat/supervisor-ir-card, local behind 26"))
      (Truth "c7cc34a8248a615f39eeefd5ba8ac841b65e49fa" (branch "codex/truth-native-demo-tour") (proof "equals origin/codex/truth-native-demo-tour")))
    (left-unstaged
      (calliope "4 modified tracked files in the child worktree; belongs to that repository's owner")
      (uxx "4 modified tracked files (package.json, pnpm-lock.yaml, pnpm-workspace.yaml, receipts.edn); concurrent work")
      (opencode "gitlink unchanged; HEAD cc4b4561 has no remote branch, but no pointer update was proposed either")))
  (manifest
    (hash-receipts "see .ημ/Π_MANIFEST.sha256")
    (hash-kanban-ledger "see .ημ/Π_MANIFEST.sha256")
    (hash-gitignore "see .ημ/Π_MANIFEST.sha256")
    (detail ".ημ/Π_MANIFEST.sha256"))
  (concurrent-dirt
    (excluded (path ".env")
      (reason "holds a live GH_TOKEN; never commit")
      (action "added /.env, /.env.*, !/.env.example to .gitignore"))
    (excluded (path ".ημ/diagnostics")
      (reason "bulk CI/session evidence, ~733 MiB and ~18100 files, never tracked in this repository")
      (action "added /.ημ/diagnostics/ to .gitignore; per-directory inventory recorded below"))
    (excluded (path ".github/workflows/agy-review.yml scripts/agy-review.sh")
      (reason "antigravity agent's review-workflow work; byte-identical to origin/feat/agy-gemini-3-8-flash-review-workflow (foresight#152)")
      (action "left untracked; already preserved on the pushed PR branch"))
    (excluded (path "openplanner/")
      (reason "184 MiB nested clone not declared in .gitmodules or src/foresight/project.cljc; absorbing it silently would grant ownership without the required manifest+project+table update")
      (action "left untracked; HEAD 582efb7 is on origin/device/stealth and personal/device/stealth, one modified pnpm-lock.yaml remains local"))
    (excluded (path ".agents/bitch-tracker/shx/axxium checkouts")
      (reason "local checkouts sit behind their already-pushed remote branches; nothing to publish")
      (action "untouched")))
  (diagnostics-inventory
    (truth-vertical-slice 16771 "651 MiB")
    (truth-physical-flight 1407 "73 MiB")
    (rheos-review-handoff-e634b40-4a38902-20261003 32 "5 MiB")
    (foresight-native-review-0fdeb01-20261003 19 "1 MiB")
    (truth-review-budget 15 "4 MiB")
    (muse-full-input-existing-pr-audit-20261003 14 "2 MiB")
    (rheos-create-core-repair-e634b40-20261003 10 "1 MiB")
    (rheos-complete-manifest-77510d8-20261003 6 "1 MiB")
    (rheos-ci-native-review-4a38902-20261003 2 "1 MiB")
    (remaining-20-directories "23 files total, <=1 MiB each"))
  (blockers
    (colab-gemma-pilot
      "docs/research/colab-gemma-pilot/budget_law.cljc declares research.colab-budget-law but the filename implies research.colab-gemma-pilot.budget-law, so clj-kondo reports 1 error and 1 warning (law_cli.clj unresolved require). Pre-existing, untracked research work; recorded rather than edited outside this snapshot's scope.")
    (opencode-submodule
      "opencode HEAD cc4b4561 has no remote branch; not part of this pointer set, so no action was possible or needed here.")
    (root-lint-scope
      "root gate lints scripts and test only. docs/research and docs/**/*.clj* are outside the gate and are not claimed clean.")))
