---
uuid: "foresight-root-gate-in-ci"
title: "Run the Foresight root gate on every pull request"
status: "incoming"
type: "task"
priority: "P1"
points: "2"
labels: "quality, ci, workspace"
category: "kanban"
write-id: "1790898480547-0.tktmvxlgfahh8ugksez"
created_at: "2026-10-01T23:48:00.547Z"
---

# Run the Foresight root gate on every pull request

## Outcome

Every pull request to `open-hax/foresight` runs the root gate in GitHub Actions: `nbb scripts/project.clj validate`, `nbb -cp scripts:test test/project_test.cljs`, `nbb -cp scripts:test test/workspace_test.cljs`, and `clj-kondo --lint scripts test src`. The check is required on `main`.

## Context

- Observed 2026-10-01 on open-hax/foresight#120: the only checks reported on a root-only change were the eta-mu evidence-review jobs and CodeRabbit. `alpha-jvm-test`, `chat-work-runtime` and `repository-census` are path-filtered and did not run. The root gate in `AGENTS.md` "Commands" ran only on the author's machine.
- Branch protection on `main` was enabled on 2026-10-01 with required checks `Evidence-first review (eta-mu) / OpenCode evidence review gate` and `CodeRabbit`, plus required conversation resolution. The new check should be added to that list once it reports reliably.

## Acceptance criteria

- [ ] GIVEN a PR that touches only `src/foresight/project.cljc` WHEN CI runs THEN a `root-gate` check runs all four commands and fails if any of them fails.
- [ ] GIVEN a PR that touches only `docs/` WHEN CI runs THEN `root-gate` still reports (no path filter), so it can be required.
- [ ] VERIFY: `root-gate` is listed in `main`'s required status checks.

## Verification

```bash
gh api repos/open-hax/foresight/branches/main/protection --jq '.required_status_checks.contexts'
```

## Anti-patterns

- No path filters on a required check: a required check that never runs blocks every merge.
- Do not initialise consolidation inputs (`.agents`, `eta`, `clobber`) in CI just to inventory them.
