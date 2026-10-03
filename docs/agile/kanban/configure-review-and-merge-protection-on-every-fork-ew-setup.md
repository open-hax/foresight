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

Every fork has the available CodeRabbit and Codex apps installed and declares MiMo and Kimi review workers. Its `main` requires a pull request, successful required checks, resolved conversations and an exact-head review gate. Auto-merge is enabled at the repository level. Path-specific tests remain blocking for PRs that touch their paths.

## Acceptance criteria

- [ ] GIVEN a fork WHEN a probe PR is opened and its exact-head manual CodeRabbit review is requested when required THEN CodeRabbit reviews it, and its footer reports the personal plan (Essentials, 5 per hour).
- [ ] GIVEN the same probe PR WHEN Codex is requested THEN a review or check from the expected Codex GitHub App appears on its exact head; its installation is verified on every fork.
- [ ] GIVEN each fork THEN query `gh api repos/riatzukiza/<name>/branches/main/protection` and separately `gh api repos/riatzukiza/<name>/rulesets?includes_parents=true` plus the applicable rule details. Read-back of active protection/rulesets proves required PRs, conversation resolution, required checks and strict up-to-date checks or a merge queue on `main`, with no maintainer/App/admin bypass, force push or deletion. A direct push to `main`, even of a SHA checked on another branch, is rejected. Separately, `gh api repos/riatzukiza/<name>` reports `allow_auto_merge: true`. Inaccessible policy remains unavailable, not absent.
- [ ] GIVEN a PR whose checks finish before any reviewer approves THEN an always-emitted required review gate rejects admission until at least one eligible CodeRabbit, Codex, MiMo or Kimi approval names `pull_request.head.sha`. A skipped, rate-limited, failed, partial or stale review contributes no approval. A current alternative approval can satisfy the quorum while unavailable optional reviewers remain visible. All observed findings still need settlement, and all repository-required deterministic checks must pass. Probes cover no approval, stale approval, one approval with another worker unavailable, superseding changes-requested, and a head change during publication.
- [ ] Quorum accepts formal `APPROVED` or an explicit completed passing/no-issues native provider verdict with trusted identity and verified current-commit coverage. Imported CLI output cannot supply native approval. The required context is restricted to its trusted App identity and external/base-pinned gate code/configuration; candidate code cannot modify it to self-authorize. Fixtures reject a changed candidate gate/workflow and a same-name status from another producer.
- [ ] Qualification records base SHA and tested merge candidate. Advancing base after a green head forces a new tested combination through strict checks/merge queue before merge; a head change also invalidates approval. Exercise that base-change race before activation.
- [ ] VERIFY: every required context is emitted for every PR. Preserve path-specific tests as blocking gates by using an always-emitted summary job that waits for the relevant filtered workflow when its paths match and reports success as a no-op otherwise. Foresight's `alpha-jvm-test`, `chat-work-runtime` and `repository-census` are path-filtered; requiring their raw contexts would deadlock unrelated PRs, while dropping them would let matching PRs merge before testing.
- [ ] VERIFY: candidate workflows/builds/hooks receive no signing/deployment secrets and only read-only tokens, including same-repository promotion PRs. Privileged admission/publication is separate trusted code that never executes the candidate. Required deterministic checks do not require target secrets, so outside contributors can run them. A secret-exfiltration candidate fixture cannot access publisher credentials.

## Verification

```bash
nbb scripts/dev_origins.clj check --protection
```

---
Planning review on open-hax/foresight#122: acceptance criteria clarified for protected-main PRs, always-emitted required checks, exact-head review, account/network preflight, shallow ancestry and CLI verification; the card body is the current incoming contract.
---
