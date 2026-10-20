---
category: "kanban"
labels: "contracts, muse, opencode, codex, cutover, 3sp"
type: "story"
story_id: "LOC.11"
points: "3"
title: "LOC.11 — Cut OpenCode and Codex over to compiled projections of the same contract"
dependency: ["loc-fragment-source", "loc-harness-profiles", "loc-instruction-emitter", "loc-per-directory-projections"]
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
> Blocked by: LOC.03, LOC.07 (budgets and profiles), LOC.08 and LOC.16 (per-directory projections)

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

- [ ] Replace the OpenCode `instructions` glob with the compiled **global-band**
  projection path (the root layer `~/.ημ` and the ancestor layers). The daemon
  renders `opencode.jsonc` from the root layer.
- [ ] Per-working-directory selection inside repositories comes from the LOC.16
  outputs, not from this static path: pure-union `AGENTS.md` deltas, or the
  per-session selection artifact where the OpenCode profile has verified it.
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
- [ ] GIVEN a new OpenCode session and a new Codex session started in
  `$WORKDIR`, the same directory whose chain step 4 checks, WHEN each is asked
  for the value after `contract-sentinel:` THEN each one's complete output,
  minus the trailing newline, is exactly its manifest's
  `:projection/sentinel` nonce. The nonce is random per build, so
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
WORKDIR="${WORKDIR:-$(git rev-parse --show-toplevel)}"   # the session directory whose Codex chain is checked
LOG="$M/sentinel-replies.log"
field() { bb -e "(println ((clojure.edn/read-string (slurp \"$1\")) $2))"; }

# 1. every manifest carries a valid hash, and all three agree
for h in claude opencode codex; do
  v=$(field "$M/$h.manifest.edn" :effective-contract/hash)
  [[ "$v" =~ ^[0-9a-f]{64}$ ]] || { echo "FAIL: $h manifest hash missing or invalid: $v"; exit 1; }
done
H=$(for h in claude opencode codex; do field "$M/$h.manifest.edn" :effective-contract/hash; done | sort -u)
[ "$(printf '%s\n' "$H" | wc -l)" -eq 1 ] || { echo "FAIL: manifests disagree: $H"; exit 1; }

# 2. OpenCode resolves no legacy instruction path, and the global projection exactly once
! grep -nE 'operation-mindfuck|ημΠ\.dev' ~/.config/opencode/opencode.jsonc || { echo "FAIL: legacy instructions remain"; exit 1; }
[ "$(grep -cF "$(field "$M/opencode.manifest.edn" :projection/path)" ~/.config/opencode/opencode.jsonc)" -eq 1 ]

# 3. each harness echoes its per-build nonce (whole file loaded); one retry; every reply recorded
ASK="Reply with only the value that follows 'contract-sentinel:' in your instructions."
echo_check() {  # harness expected command...
  local h=$1 want=$2 got attempt; shift 2
  for attempt in 1 2; do
    # complete stdout, compared exactly (command substitution strips only trailing newlines)
    got=$(cd "$WORKDIR" && "$@" 2>/dev/null) || got=""
    printf '%s %s attempt=%s want=%s got=%s\n' "$(date -u +%FT%TZ)" "$h" "$attempt" "$want" "$got" >> "$LOG"
    [ "$got" = "$want" ] && return 0
  done
  return 1
}
echo_check opencode "$(field "$M/opencode.manifest.edn" :projection/sentinel)" opencode run "$ASK" || { echo "FAIL: opencode sentinel"; exit 1; }
echo_check codex "$(field "$M/codex.manifest.edn" :projection/sentinel)" codex exec "$ASK" || { echo "FAIL: codex sentinel"; exit 1; }

# 4. the Codex project-doc chain Codex selects for WORKDIR fits project_doc_max_bytes
#    (root = nearest .git ancestor, the default project_root_markers; one file per directory:
#     AGENTS.override.md, then AGENTS.md, then configured fallbacks; empty files skipped)
FALLBACK=$(grep -E '^project_doc_fallback_filenames' ~/.codex/config.toml 2>/dev/null | grep -oE '"[^"]+"' | tr -d '"' || true)
codex_chain() {
  local cwd root rel d n p; local -a dirs parts
  cwd=$(cd "$1" && pwd -P); root=$(cd "$cwd" && git rev-parse --show-toplevel 2>/dev/null || echo "$cwd")
  dirs=("$root"); d="$root"; rel=${cwd#"$root"}
  IFS=/ read -ra parts <<< "${rel#/}"
  for p in "${parts[@]}"; do [ -n "$p" ] && { d="$d/$p"; dirs+=("$d"); }; done
  for d in "${dirs[@]}"; do
    for n in AGENTS.override.md AGENTS.md $FALLBACK; do
      if [ -e "$d/$n" ]; then
        [ -r "$d/$n" ] || { echo "UNREADABLE $d/$n" >&2; return 1; }
        [ -s "$d/$n" ] && { echo "$d/$n"; break; }
      fi
    done
  done
}
CHAIN=$(codex_chain "$WORKDIR")
LIMIT=$(grep -E '^project_doc_max_bytes' ~/.codex/config.toml 2>/dev/null | grep -oE '[0-9]+' || echo 32768)
TOTAL=0; while IFS= read -r f; do [ -n "$f" ] && TOTAL=$((TOTAL + $(wc -c < "$f"))); done <<< "$CHAIN"
[ "$TOTAL" -le "$LIMIT" ] || { echo "FAIL: codex chain $TOTAL > $LIMIT"; printf '%s\n' "$CHAIN"; exit 1; }

# 5. receipt with the recorded replies
~/.agents/skills/receipt-river/scripts/rr-append.bb --kind :test-run --origin "LOC.11 verification" \
  --note "hash $H; codex chain $TOTAL/$LIMIT bytes; replies: $(tail -n 4 "$LOG" | tr '\n' ';')"
echo "LOC.11 verified"
```

## Risks

OpenCode silently ignores missing instruction files. Mitigation: the sentinel
check proves presence in the session, not only in config. The sentinel check
goes through the model. The nonce cannot be guessed, so a correct reply is
evidence, but a wrong reply can also mean model noise. A harness that prints
progress to stdout fails this exact comparison by design; the LOC.07 fixture
records which output option yields only the final message. The verification retries
once, appends every reply to `sentinel-replies.log` and a receipt, and fails
only when both attempts miss.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
