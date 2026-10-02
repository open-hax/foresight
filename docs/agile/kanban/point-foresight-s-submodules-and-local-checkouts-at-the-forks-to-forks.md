---
uuid: "foresight-submodules-to-forks"
title: "Point Foresight's submodules and local checkouts at the forks"
status: "incoming"
type: "task"
priority: "P1"
points: "3"
labels: "workspace"
parent: "fork-dev-origins"
category: "kanban"
write-id: "1790900466466-0.qvomj4unqee5iehd8q"
created_at: "2026-10-02T00:21:06.466Z"
---

# Point Foresight's submodules and local checkouts at the forks

## Outcome

`.gitmodules` and `src/foresight/project.cljc` (`:source/url`, plus a new `:source/upstream`) name the forks. Each local checkout has `origin` set to the fork and `upstream` set to the org. Project validation and the workspace tests pass.

## Acceptance criteria

- [ ] GIVEN a fresh `git clone --recurse-submodules` WHEN it completes THEN every pinned SHA is fetched from its fork.
- [ ] GIVEN each local submodule checkout WHEN `git remote -v` runs THEN `origin` is the fork and `upstream` is the org.
- [ ] GIVEN `.gitmodules` WHEN read THEN every entry's `branch` is `main`; all 23 currently say `device/yoga`, so `git submodule update --remote` would otherwise follow a branch that is never reviewed or promoted.
- [ ] VERIFY: `nbb scripts/project.clj validate` and both root test files pass with the new URLs.

## Verification

```bash
nbb scripts/project.clj validate
nbb -cp scripts:test test/project_test.cljs
nbb -cp scripts:test test/workspace_test.cljs
```

## Anti-patterns

- Do not rewrite a submodule's history or force-push; only remotes and URLs change.
