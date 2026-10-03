---
category: "kanban"
labels: "review, ci"
parent: "fork-dev-origins"
type: "task"
write-id: "1790907950733-0.evy1qrmd2xh0pl7abp"
points: "3"
title: "Add a promote stage to pr-flow"
priority: "P1"
status: "incoming"
uuid: "pr-flow-promotion"
created_at: "2026-10-02T00:21:06.688Z"
---

# Add a promote stage to pr-flow

## Outcome

`pr-flow/flow.edn` gains a `:promote` state after `:merged`. `pr.cljs promote <dev-repo>` reads the map, then, after the exact fork merge SHA has a successful trusted Services staging receipt:
1. pins the qualified fork merge SHA and pushes that SHA to the org branch `promote/<short-sha>`;
2. opens an in-org PR into the org's `main`;
3. hands that PR to the existing merge gate with the org's reviewers;
4. after the org merge, creates a fork branch `sync/<org-main-sha7>` from fork `main`, merges the org's `main` into that branch, and opens a PR into the protected fork `main`. It waits for the fork's normal review and required-check gate, merges that PR, and verifies the org tip is an ancestor of fork `main` before allowing the next promotion. No force push or direct write to either protected `main`.

It depends on `fork-org-drift-check`, which supplies the ancestry decision `promote` refuses on.

## Acceptance criteria

- [ ] GIVEN fork merge SHA X with successful staging evidence WHEN `promote` runs THEN the org has branch `promote/<X7>` at exactly X, and an open PR from it into the org's `main`.
- [ ] GIVEN the org's `main` is not an ancestor of X WHEN `promote` runs THEN it refuses with a drift error (see `fork-org-drift-check`).
- [ ] GIVEN a merged promotion WHEN the sync step runs THEN a `sync/<org-main-sha7>` branch and fork PR exist; the fork's required checks and exact-head reviewers pass before its merge. The org `main` is then an ancestor of fork `main`, and a second `promote` does not report drift.
- [ ] GIVEN `flow.edn` WHEN `test_law.cljs` runs THEN the new state is reachable from `:merged`, and every named skill exists.

## Verification

```bash
nbb -cp ~/.agents/skills/pr-flow/scripts ~/.agents/skills/pr-flow/scripts/test_law.cljs
```

## Anti-patterns

- Not a cross-fork PR: those run without the org's secrets.
- Never force-push the org's `main`.

---
Planning review on open-hax/foresight#122: acceptance criteria clarified for protected-main PRs, always-emitted required checks, exact-head review, account/network preflight, shallow ancestry and CLI verification; the card body is the current incoming contract.
---

- [ ] GIVEN a successful exact-SHA staging receipt WHEN the promotion event is
  reconciled THEN exactly one in-org promotion PR and one affected Foresight
  integration PR are opened or reused. Missing/failed staging creates neither.
- [ ] GIVEN a delayed event for X after fork main advances THEN promotion uses
  the receipt-bound X, never silently substitutes the moving branch tip.
