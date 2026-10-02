---
category: "kanban"
labels: "ci, review"
parent: "fork-dev-origins"
type: "task"
write-id: "1790907950501-0.r02swfqjd4fzpljxgle"
points: "3"
title: "Configure review and merge protection on every fork"
priority: "P1"
status: "incoming"
uuid: "fork-review-setup"
created_at: "2026-10-02T00:21:06.248Z"
---

# Configure review and merge protection on every fork

## Outcome

Every fork has the CodeRabbit and Codex apps installed. Its `main` requires a pull request, successful required checks, resolved conversations and an exact-head review gate. Auto-merge is enabled at the repository level. Path-specific tests remain blocking for PRs that touch their paths.

## Acceptance criteria

- [ ] GIVEN a fork WHEN a probe PR is opened THEN CodeRabbit reviews it, and its footer reports the personal plan (Essentials, 5 per hour).
- [ ] GIVEN the same probe PR WHEN Codex is requested THEN a review or check from the expected Codex GitHub App appears on its exact head; its installation is verified on every fork.
- [ ] GIVEN each fork THEN `gh api repos/riatzukiza/<name>/branches/main/protection` shows required checks, `required_conversation_resolution: true`, and a required-PR rule (`required_pull_request_reviews` or an equivalent ruleset). A direct push to `main`, even of a SHA checked on another branch, is rejected. Separately, `gh api repos/riatzukiza/<name>` reports `allow_auto_merge: true`.
- [ ] GIVEN a PR whose CI finishes before CodeRabbit reviews, or whose CodeRabbit review is skipped, rate-limited, failed or on an older SHA, THEN an always-emitted required review-gate job waits within a documented bound and fails unless CodeRabbit completed successfully for `pull_request.head.sha`. A probe proves auto-merge cannot land before that evidence exists. This implements `e1-10-reviewer-start-window-protection` on each fork.
- [ ] VERIFY: every required context is emitted for every PR. Preserve path-specific tests as blocking gates by using an always-emitted summary job that waits for the relevant filtered workflow when its paths match and reports success as a no-op otherwise. Foresight's `alpha-jvm-test`, `chat-work-runtime` and `repository-census` are path-filtered; requiring their raw contexts would deadlock unrelated PRs, while dropping them would let matching PRs merge before testing.
- [ ] VERIFY: no required check reads `secrets.*`. GitHub withholds a target repository's secrets from PRs opened from other forks, even when the secret exists on the `riatzukiza` fork, so a secret-dependent required check would block outside contributors. Checked statically over each fork's workflows.

## Verification

```bash
nbb scripts/dev_origins.clj check --protection
```

---
Planning review on open-hax/foresight#122: acceptance criteria clarified for protected-main PRs, always-emitted required checks, exact-head review, account/network preflight, shallow ancestry and CLI verification; the card body is the current incoming contract.
---