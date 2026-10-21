---
uuid: "0d73c22a-b018-4b8c-92fe-560127f53294"
title: "Reconcile the opening pull-request portfolio"
status: "in_progress"
priority: "P1"
labels: ["portfolio", "github", "evidence"]
created_at: "2026-08-29T15:09:34.069Z"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
write-id: "1792483391560-0.sb0ilocyqzxaw7pg56"
---

# Reconcile the opening pull-request portfolio

Use the revision-scoped inventory in
[`docs/notes/open-pr-portfolio-2026-08-29.md`](../../notes/open-pr-portfolio-2026-08-29.md)
as the opening set. Refresh GitHub state before every mutation and assign each
item one terminal disposition from the program spec.

## Acceptance

- Merge-ready stacks land bottom-up with current checks and review threads.
- Substantive findings are repaired and retested at the new head.
- Exact duplicates, no-op branches, and superseded work close with replacement
  evidence.
- Work that should survive a large or unsafe branch is split into bounded,
  linked issues/cards before closure.
- External credentials, hosts, or product decisions remain explicit blockers
  with an owner and a concrete unblock proof.
- The final inventory contains no unexplained open item from the opening set.

---
Active parallel reconciliation tranches cover Foresight, eta-mu, Knoxx, services, and the residual child inventory.

Residual audit recorded from live GitHub evidence: 91 PRs classified, Proxx #315/#321/#325 closed as exact duplicates, Proxx #317 closed as zero-diff, and Foresight #48 plus Epiphany #7/#8 merged. The remaining 84 have explicit repair, split, supersession, or external-blocker dispositions in docs/notes/portfolio-terminal-state-audit-2026-08-29.md. Foresight #38 and #41-#47 have since merged; #37 is evidence-blocked and #40 remains under final verification.

Existing PR reconciliation work is referenced by stabilization-e0 and stabilization-e6 in docs/stabilization/work-graph.json. The August inventory remains historical input; E0 must freeze an exhaustive current upstream/personal-fork cohort with exact heads/bases, reviews/checks, owners and dependencies before M0 can qualify. This comment preserves this card identity and does not restart or complete its work.

E0 census scope fixed from complete API repository inventories: all riatzukiza-owned repositories plus open-hax and octave-commons repositories, and authored open PRs to external upstreams. Use repository-list and PR-list endpoints with explicit final-page termination; no search-result ceilings. Existing cards remain execution authorities. The resulting work graph will preserve unresolved ownership, intended dispositions and canonical dependency ambiguities rather than fabricate assignments or create duplicate cards.

E0 now provides a complete source-backed open-PR census at docs/stabilization/e0-census.md and 442-row e0-pr-census.csv. Reuse this existing reconciliation card: no duplicate execution tasks were created. The frozen graph includes reviews/checks/threads, canonical UUID candidates, stack ancestry, existing open issues and explicit unresolved owners/relationships. 309 PRs have candidate constellation routes, with 118 lacking explicit card matches; another 133 personal/external items remain visible scope decisions. Merge/close decisions require fresh exact-source evidence and an accountable stack owner; M0 remains unqualified.

---