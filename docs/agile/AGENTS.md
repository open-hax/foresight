# Foresight Kanban

The board source lives in `docs/agile/kanban`. Run `eta-mu kanban` from
the repository root. `openhax.kanban.edn` is canonical and selects the
Promethean FSM; `openhax.kanban.json` supports the published JSON-only CLI.

Use CLI status transitions and comments rather than editing card
frontmatter directly. Walk lawful transitions through `todo`,
`in_progress`, `testing`, `review`, `document`, and `done`.

Rheos writes through `docs/agile/kanban/.events`, which is a symlink into
`.ημ/kanban-events`. Receipts belong in `.ημ/receipts.edn`. Do not create
provenance ledgers elsewhere.
