---
uuid: "fork-dev-origins"
title: "EPIC: develop on riatzukiza forks; orgs receive promotions and run ops"
status: "incoming"
type: "epic"
priority: "P1"
points: "13"
labels: "workspace, ci, review"
category: "kanban"
write-id: "1790900465598-0.2czmco9mqytq0wisg31"
created_at: "2026-10-02T00:21:05.598Z"
---

# EPIC: Development happens on riatzukiza forks; the orgs only receive promotions and run ops

## Outcome

Every Foresight submodule from `open-hax` or `octave-commons` has a `riatzukiza` fork that is its development origin. Feature PRs are opened, reviewed and merged on the fork. Code reaches the org repository only through a promotion: the fork's `main` is pushed to an org branch, and an in-org PR runs the org's secrets, required checks and deploy gates. Ops-as-code in `open-hax/services` deploys from the org repositories.

## Context

- Observed 2026-10-01 from each repository's CodeRabbit review footer: the personal account `riatzukiza/*` is on Essentials at 5 reviews per hour. `open-hax` and `octave-commons` are on the free OSS program at 1 review per hour.
- CodeRabbit's knowledge base: a personal subscription "does not extend to organization repositories". Each org needs its own subscription.
- In this session, the hourly org limit was the main wall-clock cost of the review loop (open-hax/foresight#120, octave-commons/shx#2).
- User decisions (2026-10-01):
  - promotion is a push to an org branch plus an in-org PR, not a cross-fork PR, because fork PRs run without the org's secrets and the eta-mu review gate needs them;
  - Foresight's `.gitmodules` points at the forks;
  - every org submodule moves.
- GitHub constraints (observed 2026-10-01):
  - An account can hold one fork per network. `riatzukiza/mojomast-opencode` already forks `anomalyco/opencode`, the network of `open-hax/opencode`.
  - `riatzukiza/uxx` forks `shuv1337/uxx`, so the dev fork of `open-hax/uxx` needs another name.
  - `riatzukiza/axxium` is only a transfer redirect to `open-hax/axxium`.

## The core identity

The fork is where work is reviewed; the org is where reviewed work becomes operational.

## Children

- `dev-origin-map` — the fork↔org map as validated data in `.cljc` — **hard blocker for the rest**
- `create-dev-forks` — create the forks and enable Actions — needs `dev-origin-map`
- `fork-review-setup` — CodeRabbit, Codex, branch protection and auto-merge on every fork — needs `create-dev-forks`
- `foresight-submodules-to-forks` — `.gitmodules`, project model and local remotes point at the forks — needs `create-dev-forks`
- `pr-flow-promotion` — a `promote` stage and command in pr-flow — needs `dev-origin-map`
- `org-ops-only` — org `main` accepts only promotion branches; documented in services — needs `pr-flow-promotion`
- `fork-org-drift-check` — org `main` must always be an ancestor of fork `main` — needs `dev-origin-map`

## Definition of done

A feature PR on a `riatzukiza` fork is reviewed by CodeRabbit under the personal plan and merged there. It then reaches the org repository through `pr.cljs promote`, and the in-org PR passes the org's required checks. This is shown end to end on at least one repository, and every other repository is configured the same way.

## Verification

```bash
nbb scripts/project.clj validate
nbb -cp ~/.agents/skills/pr-flow/scripts ~/.agents/skills/pr-flow/scripts/pr.cljs flow promote
```

## Out of scope

- Moving or archiving the org repositories, which stay canonical for ops.
- Paid CodeRabbit subscriptions for the orgs.
