(pi-state
  (version 1)
  (timestamp "2026-08-13T13:15:00Z")
  (branch "main")
  (head "dab96f624c1e3625acd6d8effeffed96bc76da95")
  (next-head "pending-commit")
  (status "in-progress")
  (manifest
    (files [".gitmodules" "opencode" ".ημ/receipts.edn" ".ημ/kanban-events/ledger.edn"])
    (submodules ["opencode"]))
  (verification
    (tests-run true)
    (tests-passing "expected-failures-only")
    (lint-clean true))
  (concurrent-dirt
    (note "Workspace configuration files (.agents/, .clj-kondo/, .gitignore, etc.) are owned by root but currently untracked. These are consolidation inputs and will be staged in this commit.")
    (paths [".agents/" ".clj-kondo/" ".gitignore" ".jscpd.json" "AGENTS.md" "README.md" "docs/" "eta/" "nbb.edn" "openhax.kanban.edn" "openhax.kanban.json" "scripts/" "test/"]))
  (blockers [])
  (notes "Adding opencode submodule (shallow clone) and staging workspace configuration files for handoff."))
