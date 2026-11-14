# CLAUDE.md — Foresight

@AGENTS.md

<!--
AGENTS.md is the single source of truth for every agent harness; Codex and
OpenCode read it natively. Claude Code imports it here so it loads even when a
session cannot read AGENTS.md directly. Do not restate or fork its policy in
this file. Anything below this comment must be true only for Claude Code.
-->

## Claude Code

- The **Divine mandate: purify before you port** in AGENTS.md is binding.
- Subdirectory `CLAUDE.md` files (for example `eta/CLAUDE.md`) follow the same
  pattern: an `@AGENTS.md` import plus Claude-only notes. Put shared rules in
  the `AGENTS.md`. If `/init` appends a second copy of the policy, replace the
  copy with the import.
