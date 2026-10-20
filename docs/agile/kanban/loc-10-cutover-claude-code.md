---
category: "kanban"
labels: "contracts, muse, claude-code, cutover, context-budget, 3sp"
type: "story"
story_id: "LOC.10"
points: "3"
title: "LOC.10 — Cut Claude Code over to the compiled projection and remove the double load"
dependency: ["loc-fragment-source", "loc-harness-profiles", "loc-instruction-emitter"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-cutover-claude-code"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.10 — Cut Claude Code over to the compiled projection and remove the double load

> Design: `docs/notes/design/layered-operating-contracts.md` §Projection
> Research: `docs/research/layered-operating-contracts-inventory.md` §3 and §5 (Claude Code)
> Target: the `~/.claude` device repository plus muse publication
> Blocked by: LOC.03, LOC.07 (budgets and profiles) and LOC.08

## Context

- `~/.claude/CLAUDE.md` is a 12.3 KB hand-made render of lineage B.
- A session started in `~` also loads `~/AGENTS.md`, the Codex render.
- Claude Code walks to the filesystem root. `claudeMdExcludes` and the
  AGENTS.md mode can suppress duplicates.

## Outcome

- `~/.claude/CLAUDE.md` is a muse projection that carries its manifest
  reference in an HTML comment.
- No Claude Code session loads the contract twice.
- Always-on contract bytes are within the LOC.07 budget.

## Scope

- [ ] Measure the always-on instruction bytes before and after. Use `/context`
  or an equivalent, in sessions started in `~` and in `~/spaces/foresight`.
- [ ] Publish the projection to `~/.claude/CLAUDE.md` and keep the existing
  harness-only sections (scratchpad hygiene, Rheos pointer) as their own
  fragments.
- [ ] Prevent the double load with the projection's settings fragment
  (`claudeMdExcludes` or the AGENTS.md mode), owned by muse.
- [ ] Commit to the `~/.claude` repository on `device/stealth`, staging explicit
  paths only, as its `AGENTS.md` requires.

## Non-goals

- Changing the content of project `AGENTS.md` files (LOC.13).

## Acceptance criteria

- [ ] GIVEN a session started in `~` WHEN context is inspected THEN exactly one
  copy of the contract is loaded.
- [ ] GIVEN a session started in `~/spaces/foresight` WHEN context is
  inspected THEN exactly one copy of the contract is loaded there too. Claude
  Code's discovery depends on the ancestry of the working directory, so the
  `~` result does not establish this case.
- [ ] GIVEN the before and after measurements for both working directories
  WHEN compared THEN every limit in the Claude projection manifest holds in
  its own unit, and both measurements are recorded in receipts.
- [ ] GIVEN the new `CLAUDE.md` WHEN Claude Code starts THEN it starts cleanly,
  which is the `~/.claude` definition of done.

## Verification

```bash
# PHASE=before (run on the current hand copies) or PHASE=after (run on the published projection)
set -euo pipefail
PHASE="${PHASE:?before|after}"; M="${MANIFEST_DIR:?set MANIFEST_DIR}"; OUT="$M/claude-cutover.log"
LOG=$(mktemp); SETTINGS=$(mktemp --suffix=.json)
# record every instruction file Claude Code loads; the payload field name is confirmed by the LOC.07 fixture
cat > "$SETTINGS" <<JSON
{"hooks":{"InstructionsLoaded":[{"hooks":[{"type":"command","command":"jq -r '.file_path // .path // empty' >> $LOG"}]}]}}
JSON
for dir in "$HOME" "$HOME/spaces/foresight"; do
  : > "$LOG"
  (cd "$dir" && claude -p "ok" --settings "$SETTINGS" >/dev/null)
  [ -s "$LOG" ] || { echo "FAIL: no instruction-load events recorded in $dir"; exit 1; }
  copies=$(sort -u "$LOG" | tr '\n' '\0' | xargs -0 -r grep -l -e 'contract-sentinel:' -e '(mission' | wc -l)
  bytes=$(sort -u "$LOG" | tr '\n' '\0' | xargs -0 -r cat | wc -c)
  echo "$PHASE dir=$dir contract-copies=$copies always-on-bytes=$bytes" | tee -a "$OUT"
  if [ "$PHASE" = after ]; then
    [ "$copies" -eq 1 ] || { echo "FAIL: $dir loads $copies contract copies"; exit 1; }
  fi
done
if [ "$PHASE" = after ]; then
  # every limit in the Claude projection manifest holds in its own unit (LOC.08 :limits)
  bb -e "(let [m (clojure.edn/read-string (slurp \"$M/claude.manifest.edn\"))
              bad (remove #(<= (:actual %) (:max %)) (:limits m))]
          (when (or (empty? (:limits m)) (seq bad)) (println \"FAIL limits\" (pr-str (or (seq bad) :missing))) (System/exit 1)))"
  claude -p "ok" >/dev/null && echo "claude starts"
fi
~/.agents/skills/receipt-river/scripts/rr-append.bb --kind :test-run --origin "LOC.10 $PHASE" \
  --note "$(grep "^$PHASE " "$OUT" | tail -n 2 | tr '\n' ';')"
```

## Risks

Tier-0 trimming can change agent behaviour. Mitigation: compare one recorded
session before and after on the same task, and roll back by publishing the
previous projection.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
