# Foresight Kanban

Inherit the architecture rules in [`../../AGENTS.md`](../../AGENTS.md). In
particular, consolidation cards should extract portable `.cljc` shapes, laws,
contracts, ledger semantics, and pure functions before runtime-specific adapters
whenever that separation is practical.

The board source lives in `docs/agile/kanban`. Run `eta-mu kanban` from
the repository root. `openhax.kanban.edn` is canonical and selects the
Promethean FSM; `openhax.kanban.json` supports the published JSON-only CLI.

Cards may be authored manually as Markdown with their UUID, relationships and
initial incoming metadata. These are first-class inputs, not claims of admitted
board state or replayable creation events. Do not invent event/write IDs or
claim validation. Rheos owns operational admission, status changes and comments;
use its CLI/API/MCP/UI for those operations rather than editing established
state frontmatter directly. Walk lawful transitions through `todo`,
`in_progress`, `testing`, `review`, `document`, and `done`.

Rheos writes through `docs/agile/kanban/.events`, which is a symlink into
`.ημ/kanban-events`. Receipts belong in the
segmented Receipt River: add each new receipt as its own file
`.ημ/receipts/<UTC timestamp>-<id>.edn` (exactly one EDN record, ending in a
newline). `.ημ/receipts.edn` is frozen legacy history: never append to it
or add new records there. Existing segment files are immutable. The one
permitted legacy edit is a terminator-preserving whole-line migration into
`.ημ/receipts/<UTC timestamp>-<id>.edn` (head legacy bytes stay an ordered,
byte-identical subsequence of the base file) for fork/org reconciliation.
Do not create provenance ledgers elsewhere.
