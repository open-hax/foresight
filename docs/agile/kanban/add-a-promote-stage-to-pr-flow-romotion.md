---
uuid: "pr-flow-promotion"
title: "Add a promote stage to pr-flow"
status: "incoming"
type: "task"
priority: "P1"
points: "3"
labels: "review, ci"
parent: "fork-dev-origins"
category: "kanban"
write-id: "1790900466688-0.00n8zss0nnvseww06myvj"
created_at: "2026-10-02T00:21:06.688Z"
---

# Add a promote stage to pr-flow

## Outcome

`pr-flow/flow.edn` gains a `:promote` state after `:merged`. `pr.cljs promote <dev-repo>` reads the map, then:
1. pushes the fork's `main` SHA to the org branch `promote/<short-sha>`;
2. opens an in-org PR into the org's `main`;
3. hands that PR to the existing merge gate with the org's reviewers.

## Acceptance criteria

- [ ] GIVEN fork `main` at SHA X WHEN `promote` runs THEN the org has branch `promote/<X7>` at exactly X, and an open PR from it into the org's `main`.
- [ ] GIVEN the org's `main` is not an ancestor of X WHEN `promote` runs THEN it refuses with a drift error (see `fork-org-drift-check`).
- [ ] GIVEN `flow.edn` WHEN `test_law.cljs` runs THEN the new state is reachable from `:merged`, and every named skill exists.

## Verification

```bash
nbb -cp ~/.agents/skills/pr-flow/scripts ~/.agents/skills/pr-flow/scripts/test_law.cljs
```

## Anti-patterns

- Not a cross-fork PR: those run without the org's secrets.
- Never force-push the org's `main`.
