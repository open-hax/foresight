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
- [ ] GIVEN a cross-fork PR whose head branch is named `promote/foo` THEN the check still fails, because it requires `pull_request.head.repo.full_name == github.repository`.
- [ ] GIVEN an in-org `promote/<full-sha>` branch WHEN the trusted required gate runs THEN its head is exactly receipt-bound X, reachable from the fork's `main`, with successful staging proof for X. Verify repository, artifact digest, receipt ID/hash and trusted issuer, plus admitted feature purpose; a manually opened branch with only reachability or a stale/foreign receipt fails.
- [ ] Read back classic branch protection and applicable active/inherited rulesets for org `main`: required PRs, trusted App-bound admission context, strict up-to-date checks or merge queue, conversations, and no admin/App/maintainer bypass, direct writes, force push or deletion. Prove a direct push is rejected.
- [ ] Candidate workflows/build hooks receive no signing/deployment secrets or writable token. The required gate and privileged publisher run from independently pinned trusted code, never candidate code; spoofing their context or changing the candidate gate fails admission.
- [ ] VERIFY: `open-hax/services` `README.md` names the fork/org split and links to `config/dev-origins.edn`.
