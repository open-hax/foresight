# CLAUDE.md — Eta

@AGENTS.md

<!--
eta/AGENTS.md (imported above) and the root AGENTS.md hold Eta's rules for
every agent harness. Do not expand this file into a second architecture policy;
anything added here must be true only for Claude Code.
-->

## Claude Code

- A session started inside `eta/` reaches the root policy through the root
  `CLAUDE.md`'s `@AGENTS.md` import, which resolves outside `eta/`. Claude Code
  treats that as an external import and asks once to approve it. Approve it, or
  start the session from the repository root. Declining disables the import for
  this project and the dialog does not return.
