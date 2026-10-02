---
uuid: "fork-org-drift-check"
title: "Detect fork/org drift"
status: "incoming"
type: "task"
priority: "P2"
points: "2"
labels: "workspace, law"
parent: "fork-dev-origins"
category: "kanban"
write-id: "1790900467148-0.mkdkg6rvecmh4g0sljt"
created_at: "2026-10-02T00:21:07.148Z"
---

# Detect fork/org drift

## Outcome

A check reports any repository where the org's `main` is not an ancestor of the fork's `main`, such as a hotfix landed directly on the org. It runs in `nbb scripts/project.clj validate --remote` and before every `promote`.

## Acceptance criteria

- [ ] GIVEN a promotion PR merged on the org with a merge commit M WHEN the post-promotion sync runs (`pr-flow-promotion`) THEN the fork's `main` merges `upstream/main` and M becomes an ancestor of the fork's `main`, so the next check passes without anyone landing an out-of-band hotfix.
- [ ] GIVEN the org `main` equal to or behind the fork `main` THEN the check passes.
- [ ] GIVEN a commit on the org `main` that is missing from the fork THEN the check fails, names the repository and the SHAs, and suggests merging `upstream/main` into the fork.
- [ ] VERIFY: the ancestry decision is a pure function over SHAs and parent lists, tested without network access.

## Verification

```bash
nbb -cp scripts:test test/project_test.cljs
```
