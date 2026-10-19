---
category: "kanban"
labels: "contracts, muse, opencode, codex, cutover, 3sp"
type: "story"
story_id: "LOC.11"
points: "3"
title: "LOC.11 — Cut OpenCode and Codex over to compiled projections of the same contract"
dependency: ["loc-fragment-source", "loc-harness-profiles", "loc-instruction-emitter"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-cutover-opencode-codex"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.11 — Cut OpenCode and Codex over to compiled projections of the same contract

> Design: `docs/notes/design/layered-operating-contracts.md` §Projection
> Research: `docs/research/layered-operating-contracts-inventory.md` §3 and §5 (OpenCode, Codex)
> Target: the `~/.ημ` root layer (`config/opencode/settings/core.edn`) and the muse projection
> Blocked by: LOC.03, LOC.07 (budgets and profiles) and LOC.08

## Context

- OpenCode loads lineage A through the `instructions` glob at
  `~/.ημ/config/opencode/settings/core.edn:37-38`, plus a separate 11.6 KB
  `~/.config/opencode/AGENTS.md`.
- Codex loads `~/.codex/AGENTS.md`, a lineage-B render. Codex truncates project
  documents at 32 KiB and never reads above the git root.

## Outcome

OpenCode and Codex load projections of the same effective contract as Claude
Code, within their budgets. Each projection has a manifest and a receipt.

## Scope

- [ ] Replace the OpenCode `instructions` glob with the compiled projection
  path. The daemon renders `opencode.jsonc` from the root layer.
- [ ] Reconcile `~/.config/opencode/AGENTS.md` ("general agent behaviors"):
  either fold it into fragments or keep it as an explicit OpenCode-only layer
  fragment.
- [ ] Publish `~/.codex/AGENTS.md` as a projection.
- [ ] Measure before and after for both harnesses.

## Non-goals

- In-repository delta files, which follow the open-question-1 decision in
  LOC.07.

## Acceptance criteria

- [ ] GIVEN the rendered OpenCode config WHEN its `instructions` are resolved
  THEN no `operation-mindfuck` or `ημΠ.dev` path remains, and the projection
  path appears exactly once.
- [ ] GIVEN a new OpenCode session and a new Codex session WHEN each is asked
  for the value after `contract-sentinel:` THEN each replies with exactly its
  manifest's `:projection/sentinel` nonce. The nonce is random per build, so
  the reply proves the harness loaded that projection through its last line,
  without truncation.
- [ ] GIVEN the Codex project-document chain WHEN its bytes are summed THEN the
  total is at or below the configured `project_doc_max_bytes` (default 32768).
- [ ] GIVEN the three harness projections WHEN their manifests are compared
  THEN all three carry the same `:effective-contract/hash`.

## Verification

```bash
set -euo pipefail
M="${MANIFEST_DIR:?set MANIFEST_DIR to where LOC.08 writes projection manifests}"
field() { bb -e "(println ((clojure.edn/read-string (slurp \"$1\")) $2))"; }

# 1. one effective contract across the three harnesses
H=$(for h in claude opencode codex; do field "$M/$h.manifest.edn" :effective-contract/hash; done | sort -u)
[ "$(printf '%s\n' "$H" | wc -l)" -eq 1 ] || { echo "FAIL: manifests disagree: $H"; exit 1; }

# 2. OpenCode resolves no legacy instruction path, and the projection exactly once
! grep -nE 'operation-mindfuck|ημΠ\.dev' ~/.config/opencode/opencode.jsonc || { echo "FAIL: legacy instructions remain"; exit 1; }
[ "$(grep -c "$(field "$M/opencode.manifest.edn" :projection/path)" ~/.config/opencode/opencode.jsonc)" -eq 1 ]

# 3. each harness echoes its per-build nonce, so it loaded the whole projection
ASK="Reply with only the value that follows 'contract-sentinel:' in your instructions."
[ "$(opencode run "$ASK" | tr -d '[:space:]')" = "$(field "$M/opencode.manifest.edn" :projection/sentinel)" ] || { echo "FAIL: opencode sentinel"; exit 1; }
[ "$(codex exec "$ASK" | tail -n 1 | tr -d '[:space:]')" = "$(field "$M/codex.manifest.edn" :projection/sentinel)" ] || { echo "FAIL: codex sentinel"; exit 1; }

# 4. the Codex chain fits its cumulative budget
LIMIT=$(grep -E '^project_doc_max_bytes' ~/.codex/config.toml | grep -oE '[0-9]+' || echo 32768)
TOTAL=$(cat ~/.codex/AGENTS.md "$(git rev-parse --show-toplevel)"/AGENTS.md 2>/dev/null | wc -c)
[ "$TOTAL" -le "$LIMIT" ] || { echo "FAIL: codex chain $TOTAL > $LIMIT"; exit 1; }
echo "LOC.11 verified"
```

## Risks

OpenCode silently ignores missing instruction files. Mitigation: the sentinel
check proves presence in the session, not only in config. The sentinel check
goes through the model. The nonce cannot be guessed, so a correct reply is
evidence, but a wrong reply can also mean model noise. Retry once before
treating it as a failure, and record both replies.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
