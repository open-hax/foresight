---
category: "kanban"
labels: "evidence, isolation, security"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
type: "task"
write-id: "1788047365904-0.n5p2yw44zo7zdd4mci"
title: "Isolate exact-commit evidence execution"
priority: "P1"
status: "todo"
uuid: "c9cfd94c-4c18-4469-acf4-d79fbe44f659"
created_at: "2026-08-29T23:48:38.409Z"
---

# Isolate exact-commit evidence execution

The stronger local-execution threat model and acceptance live in
[Foresight issue #58](https://github.com/open-hax/foresight/issues/58).
The current descriptor checks detect ordinary drift; this card owns isolated
materialization against adversarial pathname replacement.

## Exit

Merge bounded exact-commit worktree, sandbox, or immutable-mount execution with
fail-closed platform behavior, adversarial path/revision regressions, safe
cleanup, exact evidence retention, and independent review.

---
Projected from Foresight #58 as planned strengthening. Preserve the current documented threat boundary until isolated exact-commit execution lands.
---