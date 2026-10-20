---
category: "kanban"
labels: "contracts, eta-mu, legacy, 1sp"
type: "story"
story_id: "LOC.15"
points: "1"
title: "LOC.15 — Stop the pi-era extension from bootstrapping .ημ/PRINCIPLE.edn copies"
priority: "P3"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-retire-legacy-principle-bootstrap"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.15 — Stop the pi-era extension from bootstrapping .ημ/PRINCIPLE.edn copies

> Design: `docs/notes/design/layered-operating-contracts.md`, open question 5
> Research: `docs/research/layered-operating-contracts-inventory.md` §2 (lineage A writer)
> Target repository: eta-mu (`packages/extensions/src/eta_mu/extensions/contract_runtime_v2.cljs`)
> Blocked by: nothing

## Context

`contract_runtime_v2` (lines 6, 81 and 118) bootstraps `PRINCIPLE.edn` into the
`.ημ/` of each working directory. That produced 57 copies across repositories.
The extension targets pi, which the user has called a poor first target.

## Outcome

- The extension no longer creates new `.ημ/PRINCIPLE.edn` files.
- Existing copies are left untouched, as the preservation rule in
  `spaces/AGENTS.md` requires.
- The extension's README and docstring say that it is legacy and pi-only.

## Scope

- [ ] Remove or disable the bootstrap write path, and keep the read path if
  any consumer remains.
- [ ] Confirm that no current harness (Claude Code, OpenCode or Codex) reads
  `.ημ/PRINCIPLE.edn`. Record the evidence.

## Non-goals

- Deleting existing copies.
- Removing the extension.

## Acceptance criteria

- [ ] GIVEN a fresh directory with no `.ημ/PRINCIPLE.edn` WHEN the extension's
  session hook runs THEN no `PRINCIPLE.edn` is created.
- [ ] GIVEN a code search across muse, eta-mu and the harness configs WHEN it
  runs THEN no live reader of `.ημ/PRINCIPLE.edn` remains outside this legacy
  extension.

## Verification

```bash
set -euo pipefail
pnpm -C packages/extensions test
# any live reader outside the legacy extension itself fails the check
if rg -n "PRINCIPLE\.edn" --hidden --glob '!**/node_modules/**' \
     --glob '!**/contract_runtime_v2*' \
     ~/spaces/foresight/muse/src ~/spaces/foresight/eta-mu/packages \
     ~/.claude/settings.json ~/.claude/hooks ~/.claude/CLAUDE.md \
     ~/.config/opencode ~/.ημ/config \
     ~/.codex/config.toml ~/.codex/AGENTS.md; then
  echo "FAIL: live PRINCIPLE.edn reader found"; exit 1
fi
echo "no live readers"
```

## Risks

A forgotten consumer of the bootstrapped file. Mitigation: the code search
acceptance criterion.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
