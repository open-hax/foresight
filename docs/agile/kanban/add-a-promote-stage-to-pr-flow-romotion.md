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
1. pins the qualified fork merge SHA and pushes that SHA to the org branch `promote/<full-sha>`, checking any existing branch against the full binding;
2. opens an in-org PR into the org's `main`;
3. hands that PR to the existing merge gate with the org's reviewers;
4. after the org merge, creates a fork branch `sync/<full-org-main-sha>` from fork `main`, merges the org's `main` into that branch, and opens a PR into the protected fork `main`. It waits for the fork's normal review and required-check gate, merges that PR, and verifies the org tip is an ancestor of fork `main` before allowing the next feature promotion. The admitted `upstream-sync` purpose makes this merge terminal: no staging, promotion or integration is triggered. No force push or direct write to either protected `main`.

It depends on `fork-org-drift-check`, which supplies the ancestry decision `promote` refuses on.

## Acceptance criteria

- [ ] GIVEN qualified feature merge X with exact-X trusted staging evidence WHEN `promote` runs THEN the org has branch `promote/<full-X>` at exactly X and an open PR into org `main`. Reconciliation keys the full digest; two commits sharing seven prefix characters cannot collide or overwrite branches.
- [ ] GIVEN the org's `main` is not an ancestor of X WHEN `promote` runs THEN it refuses with a drift error (see `fork-org-drift-check`).
- [ ] GIVEN a merged promotion WHEN sync runs THEN a `sync/<full-org-main-sha>` branch and fork PR exist; the fork's required checks and exact-head quorum pass before merge. Org main becomes an ancestor of fork main; a subsequent qualified feature can promote without drift. Replaying the sync merge produces no deployment or PR. Purpose comes from the trusted App's admitted repository/PR/SHA record, never a mutable label/prefix; unknown purposes reject.
- [ ] GIVEN `flow.edn` WHEN `test_law.cljs` runs THEN the new state is reachable from `:merged`, and every named skill exists.

## Verification

```bash
nbb -cp ~/.agents/skills/pr-flow/scripts ~/.agents/skills/pr-flow/scripts/test_law.cljs
```

## Anti-patterns

- Candidate code must run without secrets on fork and in-org PRs alike; trusted admission/publication never executes it.
- Never force-push the org's `main`.

---
Planning review on open-hax/foresight#122: acceptance criteria clarified for protected-main PRs, always-emitted required checks, exact-head review, account/network preflight, shallow ancestry and CLI verification; the card body is the current incoming contract.
---

- [ ] GIVEN a successful exact-SHA staging receipt WHEN the promotion event is
  reconciled THEN exactly one in-org promotion PR and one affected Foresight
  integration PR are opened or reused. Its affected gitlink is exactly staged X
  and is written only after X is reachable from the approved child destination.
  Bind the receipt ID/hash, issuer and upstream PR. Missing/failed staging
  creates neither; an org merge Y cannot silently replace staged X.
- [ ] GIVEN a delayed event for X after fork main advances THEN promotion uses
  the receipt-bound X, never silently substitutes the moving branch tip.
