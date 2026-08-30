- ts: 2026-08-13T13:47:48.455456762Z
  session: ses_00564f74fffej4ysPhbpJt10NX
  task: Scaffold foresight orchestration and opencode reference
  p-efficiency: 0.67
  p-friction: 0.72
  p-skill-candidate: 0.86
  spore: none
  receipt-refs: none
  note: Published-vs-source Rheos config/FSM drift and hardcoded board ledger paths required explicit compatibility mirrors and .ημ symlink redirection.
- ts: 2026-08-13T16:00:00.000Z
  session: ses_00564f74fffej4ysPhbpJt10NX
  task: Model root consolidation inputs safely
  p-efficiency: 0.76
  p-friction: 0.58
  p-skill-candidate: 0.61
  spore: none
  receipt-refs: 2026-08-13T16:00:00.000Z
  note: Declarative inventory must remain separate from executable authority; filesystem identity and current ownership metadata are revalidated at process boundaries.
- ts: 2026-08-25T23:34:12Z
  session: current-codex-task
  task: Audit Knoxx translation-to-publication production wiring
  p-efficiency: 0.78
  p-friction: 0.47
  p-skill-candidate: 0.38
  spore: none
  receipt-refs: 2026-08-25T23:23:12Z, 2026-08-25T23:34:12Z
  note: Cross-repository readiness required distinguishing merged laws from invoked runtime paths, deployed revisions from main, and OpenPlanner HTTP retirement from source/package/schema independence; existing repository and host-inventory skills were adequate.

## 2026-08-26 — a broken build hid a service that had never started

Friction: `deploy-knoxx` had been failing so long that production served images
from three weeks earlier, and the two causes were both in `services`, not in
`knoxx` — a missing clojure CLI for a `:deps`-mode shadow-cljs build, and a base
image tag the vendor retired. Fixing them surfaced a third problem underneath:
`knoxx-sandboxd` has never once started on this host, and because the backend
declares `depends_on: {condition: service_healthy}` on it, the first successful
build in weeks took production down instead of updating it.

Lesson worth keeping: when a build has been red long enough that nobody reads it,
the first green build is a deployment of everything that accumulated behind it.
Treat it as a first deploy, not an increment — and check what the stack will do
if an optional dependency is unhealthy *before* running it.
- ts: 2026-08-29T01:46:56.317119946Z
  session: /home/err/spaces/foresight
  task: Reconcile distributed context program and implement EvidenceRef v1
  p-efficiency: 0.79
  p-friction: 0.42
  p-skill-candidate: 0.58
  spore: none
  receipt-refs: none
  note: Typed source-neutral references let Rheos, Epiphany, Clio and skills cross-link without sharing authority; the only notable tooling gap is no top-level board-create command.
- ts: 2026-08-29T02:02:00Z
  session: /home/err/spaces/foresight
  task: Correct Witness Thread and River City conceptual boundaries
  p-efficiency: 0.91
  p-friction: 0.18
  p-skill-candidate: 0.22
  spore: none
  receipt-refs: witness-thread-concept-archaeology
  note: Lore supplies conceptual provenance, not implementation authority; separate recovered narrative, observed reuse, and recommended contracts.
