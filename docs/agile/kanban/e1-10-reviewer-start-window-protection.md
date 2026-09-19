---
category: "eta-mu-breakdown"
labels: ["eta-mu-breakdown", "migration", "branch-protection", "coderabbit", "5sp"]
story_id: "E1.10"
write-id: "1789857185730-0.z6l668h7suztn7m1na"
points: "5"
title: "E1.10 — Unreviewed changes cannot merge during the reviewer-start window"
blocked_by: ["e1-04-independent-git-history"]
priority: "P1"
status: "blocked"
epic: "operation-eta-mu-breakdown"
uuid: "e1-10-reviewer-start-window-protection"
created_at: "2026-09-13T00:00:00Z"
---

# E1.10 — Reviewer-start window protection

**Acceptance.** Main requires PRs, required build/test/lint/conformance results, resolved review conversations, and completed successful CodeRabbit review evidence for the exact head. Review absence, queueing, error, rate-limit, skipped review, stale-head evidence, or an unexpected producer cannot authorize merge.

## Tasks

- Discover actual emitted CodeRabbit check/status names and trusted app identity
- Require an always-present, trusted review-completion gate
- Prevent a PR from modifying the trusted gate to self-authorize
- Configure resolved conversations separately
- Scope agent permissions so bypass, force push, policy edits, and direct main writes are not their ordinary execution path
- Verify rules through API read-back and controlled PR fixtures

## Failing fixtures first

- Fresh PR before CodeRabbit starts
- Successful old SHA followed by a new push
- All checks green but one unresolved thread
- No review due to errors/rate limits
- Spoofed check producer
- A skipped test hidden by an aggregate
- Stale base requiring renewed verification

## Evidence

Policy JSON/read-back, app/check identity, negative and positive merge-eligibility tests, and exact-head reviewer state.

## Planning reference

See `docs/migrations/eta-mu-breakdown/epic-01-operation-eta-mu-breakdown.md` (E1.10).

---
PR #96 reconciliation: Unsupported done claim reopened through CLI. Trusted exact-head review-completion rollout remains tracked in https://github.com/open-hax/foresight/issues/111. Policy read 403 is unavailable evidence, not proof that policy is absent. No policy is weakened and no completion is manufactured.
---