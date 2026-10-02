---
uuid: "fork-review-setup"
title: "Configure review and merge protection on every fork"
status: "incoming"
type: "task"
priority: "P1"
points: "3"
labels: "ci, review"
parent: "fork-dev-origins"
category: "kanban"
write-id: "1790900466248-0.lfhi5umkpejwvj34fo9"
created_at: "2026-10-02T00:21:06.248Z"
---

# Configure review and merge protection on every fork

## Outcome

Every fork has the CodeRabbit and Codex apps installed. Its `main` is protected with required checks, required conversation resolution and auto-merge, matching its org upstream's checks where those checks don't need org secrets.

## Acceptance criteria

- [ ] GIVEN a fork WHEN a probe PR is opened THEN CodeRabbit reviews it, and its footer reports the personal plan (Essentials, 5 per hour).
- [ ] GIVEN each fork THEN `gh api repos/riatzukiza/<name>/branches/main/protection` shows required checks plus `required_conversation_resolution: true`, and `allow_auto_merge` is true.
- [ ] VERIFY: every required check comes from a workflow with no `paths`/`paths-ignore` filter on `pull_request`, so it reports on every PR. In Foresight, `alpha-jvm-test`, `chat-work-runtime` and `repository-census` are path-filtered and must not be required; a skipped required check stays pending forever.
- [ ] VERIFY: no required check reads `secrets.*`. GitHub withholds a target repository's secrets from PRs opened from other forks, even when the secret exists on the `riatzukiza` fork, so a secret-dependent required check would block outside contributors. Checked statically over each fork's workflows.

## Verification

```bash
nbb scripts/dev_origins.clj check --protection
```
