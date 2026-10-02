---
uuid: "create-dev-forks"
title: "Create the riatzukiza dev forks"
status: "incoming"
type: "task"
priority: "P1"
points: "2"
labels: "workspace, ci"
parent: "fork-dev-origins"
category: "kanban"
write-id: "1790900466030-0.7dn0nocmvyh5vulugkt"
created_at: "2026-10-02T00:21:06.030Z"
---

# Create the riatzukiza dev forks

## Outcome

Every entry in `config/dev-origins.edn` exists on GitHub as a fork of its org upstream, with Actions enabled. A script run with `--dry-run` reports nothing left to create.

## Acceptance criteria

- [ ] GIVEN the map WHEN the script runs THEN each missing fork is created with `gh repo fork <org>/<name> --fork-name <dev name> --clone=false` and existing ones are left unchanged.
- [ ] GIVEN a created fork THEN its Actions are enabled and its `main` matches the org `main` SHA at creation time.
- [ ] GIVEN a pre-existing fork in the map (`riatzukiza/mojomast-opencode`, and any other) THEN its Actions are enabled too, checked through `gh api repos/<fork>/actions/permissions`.
- [ ] GIVEN the map's `:network/root` for an entry WHEN the preflight runs THEN it lists `riatzukiza`'s existing forks of that root (`gh api repos/<root>/forks`), and refuses to create one if a fork outside the map already occupies the network.
- [ ] VERIFY: `--dry-run` after the run reports zero actions.

## Verification

```bash
nbb scripts/dev_origins.clj check
```

## Anti-patterns

- Never delete or rename an existing `riatzukiza` repository; report collisions instead.
- No secrets are copied to the forks; deploy credentials stay in the orgs.
