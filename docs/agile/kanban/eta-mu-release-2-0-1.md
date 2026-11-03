---
category: "kanban"
labels: "release, review"
dependency: "eta-mu-review-workflow-on-forks"
parent: "eta-mu-baseline-review"
type: "task"
write-id: "1792443123331-0.3sol2w5tyoaa9fmvg7g"
points: "3"
title: "Merge open-hax/eta-mu#360 and publish eta-mu 2.0.1"
priority: "P1"
status: "incoming"
uuid: "eta-mu-release-2-0-1"
created_at: "2026-10-19T20:30:00.000Z"
---

# Merge open-hax/eta-mu#360 and publish eta-mu 2.0.1

## Outcome

`eta-mu` reports its true version in receipts, `eta-mu receipt` honors `.ημ/receipts.edn`, and `@eta-mu/receipt-river@0.1.1` and `eta-mu@2.0.1` are published from merged `main`.

## Acceptance criteria

- [ ] GIVEN `open-hax/eta-mu#360` WHEN the review loop completes THEN five rounds, or unanimous current-head approval, have run, every finding is settled, required checks pass and the merge uses a merge commit at the gated head.
- [ ] GIVEN the merge WHEN `@eta-mu/receipt-river@0.1.1` then `eta-mu@2.0.1` are published THEN `npm view` shows both versions, and `eta-mu version` prints `2.0.1`.
- [ ] GIVEN a repository with only `.ημ/receipts.edn` THEN `eta-mu receipt append` and `validate` use it and create no root `receipts.edn`.
- [ ] GIVEN the release THEN a receipt records the published versions and the earlier correction that 2.0.0 was additive and arguably a minor bump.

## Verification

```bash
npm view eta-mu@2.0.1 version
npm view @eta-mu/receipt-river@0.1.1 version
eta-mu version
```

---
open-hax/eta-mu#360 opened; CodeRabbit round 1 finding (schema introduced-by, P2) fixed in e4e12a09 and settled. Codex quota-unavailable. Required checks verify, Rheos tests and lint, rheos-github-sync fail only because ETA_MU_APP_ID/ETA_MU_APP_PRIVATE_KEY are unset on open-hax/eta-mu (secrets live on riatzukiza forks). Decision needed: set org secrets or run the PR on the fork.

Correction: the earlier note that ETA_MU_APP_ID/ETA_MU_APP_PRIVATE_KEY are unset on open-hax/eta-mu was unverified (org secrets are not readable with the current token). #360's head is the riatzukiza fork, and GitHub withholds secrets from fork-originated pull_request runs even when org secrets exist, which fits the 'unavailable' failures. Decision: run the review on riatzukiza/eta-mu as a same-repo PR.

riatzukiza/eta-mu#36 round 1: CodeRabbit P2 (root ledger shadowing; knoxx has both files) fixed and settled; round 2 requested. Codex quota-unavailable.

---