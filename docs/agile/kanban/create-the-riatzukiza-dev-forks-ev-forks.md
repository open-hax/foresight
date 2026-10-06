---
category: "kanban"
labels: "workspace, ci"
parent: "fork-dev-origins"
type: "task"
write-id: "1790907950961-0.e1xmd7zhxlmdvgc59e1"
points: "2"
title: "Create the riatzukiza dev forks"
priority: "P1"
status: "incoming"
uuid: "create-dev-forks"
created_at: "2026-10-02T00:21:06.030Z"
---

# Create the riatzukiza dev forks

## Outcome

Every entry in `config/dev-origins.edn` exists on GitHub as a fork of its org upstream, with Actions enabled. A script run with `--dry-run` reports nothing left to create.

## Acceptance criteria

- [ ] GIVEN the map WHEN the script runs THEN it first checks `gh api user` and refuses unless the authenticated login is `riatzukiza`; then each missing fork is created with `gh repo fork <org>/<name> --fork-name <dev name> --clone=false`. Existing forks are preserved; their Actions setting may be enabled.
- [ ] GIVEN a created fork THEN its Actions are enabled and its `main` matches the org `main` SHA at creation time.
- [ ] GIVEN any mapped fork, including a pre-existing one such as `riatzukiza/mojomast-opencode`, THEN its Actions are enabled, checked through `gh api repos/<fork>/actions/permissions`, and its `main` contains the current org `main` history. A behind fork is synced non-destructively before setup passes; divergence or a failed ancestry check is reported as a blocking action.
- [ ] GIVEN the map's `:network/root` for an entry WHEN the preflight runs THEN it paginates the fork network (`gh api --paginate repos/<root>/forks`) and refuses to create one if a `riatzukiza` fork outside the map already occupies the network. An incomplete or failed listing blocks creation rather than being treated as empty.
- [ ] VERIFY: `--dry-run` after the run reports zero actions and zero org-main ancestry gaps for every mapped fork.

## Verification

```bash
nbb scripts/dev_origins.clj check
```

## Anti-patterns

- Never delete or rename an existing `riatzukiza` repository; report collisions instead.
- No secrets are copied to the forks; deploy credentials stay in the orgs.

---
Planning review on open-hax/foresight#122: acceptance criteria clarified for protected-main PRs, always-emitted required checks, exact-head review, account/network preflight, shallow ancestry and CLI verification; the card body is the current incoming contract.
---
