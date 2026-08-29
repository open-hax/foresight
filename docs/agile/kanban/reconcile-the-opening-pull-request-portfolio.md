---
uuid: "0d73c22a-b018-4b8c-92fe-560127f53294"
title: "Reconcile the opening pull-request portfolio"
status: "in_progress"
priority: "P1"
labels: ["portfolio", "github", "evidence"]
created_at: "2026-08-29T15:09:34.069Z"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
write-id: "1788016338074-0.0uxawdbisxbe0vk8nfq"
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

---
