---
uuid: "org-ops-only"
title: "Make org repositories promotion-only and document the split"
status: "incoming"
type: "task"
priority: "P2"
points: "2"
labels: "ci, ops"
parent: "fork-dev-origins"
category: "kanban"
write-id: "1790900466909-0.dolmmjy9vsvnq0uq3e4"
created_at: "2026-10-02T00:21:06.909Z"
---

# Make org repositories promotion-only and document the split

## Outcome

Each org repository's `main` accepts changes only through `promote/*` PRs. `open-hax/services` documents that ops-as-code deploys from org repositories, and that development happens on the forks.

## Acceptance criteria

- [ ] GIVEN an org repository WHEN a PR from a non-`promote/*` branch targets `main` THEN a required check fails with a message naming the fork.
- [ ] VERIFY: `open-hax/services` `README.md` names the fork/org split and links to `config/dev-origins.edn`.
