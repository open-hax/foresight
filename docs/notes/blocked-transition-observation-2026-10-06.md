# Direct blocker reporting in the current Promethean FSM

Canonical upstream issue: [open-hax/rheos#4](https://github.com/open-hax/rheos/issues/4).

Foresight's authorized backlog sweep cannot record an observed dependency blocker through the canonical Promethean FSM without first pretending that the card has entered another lifecycle stage.

Observed on October 6, 2026 in an isolated checkout of `open-hax/foresight` at `fcfc2d1` with `fsm: promethean`:

```text
eta-mu kanban move e1-11-donor-retirement-after-cutover --to blocked
REJECTED ... No transition from 'todo' to 'blocked'

eta-mu kanban status-update e1-11-donor-retirement-after-cutover --to blocked
Error: transition rejected: No transition from 'todo' to 'blocked'

eta-mu kanban move codex-cloud-fork-selection-projection --to blocked
REJECTED ... No transition from 'incoming' to 'blocked'
```

These commands produced no tracked changes. The first card already names three blocked prerequisites; the second explicitly requires the absent `config/dev-origins.edn` and unfinished CC2.01. The user asked the sweep to mark blocked work and move on. Advancing incoming work to accepted/breakdown or todo work into execution merely to reach blocked would fabricate progress.

Current Rheos `main` at `ef3c4ab` confirms in `src/rheos/backend/law/fsm.cljs` that only `breakdown` can enter `blocked`. `blocked` can return to `breakdown` or `ready`; this issue does not request new readiness or completion bypasses.

Please review and add direct obstruction-reporting transitions from unfinished Promethean stages where useful, including `incoming` and `todo`. Keep terminal/unknown statuses excluded, preserve existing forward gates and blocked exit rules, and record moves through the existing transition/event authority. Specify whether the blocked column's existing WIP limit should govern obstruction reporting; reaching capacity must not silently lose the blocker evidence.

Verification should include rejected terminal/unknown sources, no new readiness/completion paths, and real CLI/status-update fixture writes that preserve historical ledger bytes and append the canonical transition event. Foresight will consume a qualified immutable Rheos revision rather than add its own FSM override or parser. The Foresight cards remain in their actual source states pending this upstream capability; no successful blocked transition or board validation is claimed.
