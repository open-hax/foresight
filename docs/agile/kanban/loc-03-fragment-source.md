---
category: "kanban"
labels: "contracts, constitution, source, 3sp"
type: "story"
story_id: "LOC.03"
points: "3"
title: "LOC.03 — Merge both contract lineages into identified, tiered fragments"
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-fragment-source"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.03 — Merge both contract lineages into identified, tiered fragments

> Design: `docs/notes/design/layered-operating-contracts.md` §Fragments, open question 7
> Research: `docs/research/layered-operating-contracts-inventory.md` §2
> Target: root layer `~/.ημ/contract/`, versioned according to open question 7
> Blocked by: open question 7, for the versioning location only

## Context

The user decided on 2026-10-19 to merge both lineages into a new `.ημ`
source. That decision also settled which lineage-A forms survive:

- carried forward as tier 1: `calibration`, `remember-protocol`, and
  `lisp-semantics` together with `delegation`;
- dropped: `model-architecture`;
- dropped by default: `skill-registry`, because it points at a dead `~/.pi`
  root and `~/.agents/skills` has replaced it.

## Outcome

`~/.ημ/contract/fragments/` holds one S-expression fragment per concern, each
with a stable id and a tier. Every fragment records its provenance: the source
file and the hash of the lineage copy it came from.

## Scope

- [ ] Tier 0:
  - `:opmf/core`: mission, directives (adding "Always ask questions"),
    operators, safety and license;
  - `:opmf/output`: output-shape, format-rule, context-symbols and
    uncertainty-operators, in compact form.
- [ ] Tier 1:
  - `:opmf/skill-repository`, including harness-boundary;
  - `:opmf/calibration`;
  - `:opmf/remember`;
  - `:opmf/semantics`, holding lisp-semantics and delegation;
  - `:opmf/workflow`, holding the Rheos board and ledger discipline.
- [ ] A provenance table that maps each fragment to its lineage source and
  hash, and lists every dropped form with its reason.
- [ ] Canonical uncertainty code points: `ლა` is U+10DA U+10D0 and `לா` is
  U+05DC U+0BBE. Reject the U+05DC U+05D2 look-alike seen in stale copies.

## Non-goals

- Compiling or projecting the fragments (LOC.08).
- Deleting lineage copies.

## Acceptance criteria

- [ ] GIVEN the fragment set WHEN each directive and rule from both lineages is
  traced THEN each one is either present in exactly one fragment or listed as
  dropped with a reason.
- [ ] GIVEN the fragments WHEN parsed with the EDN reader THEN every file
  reads, and the fragment ids are unique.
- [ ] GIVEN the tier-0 fragments WHEN their bytes are counted THEN the count is
  recorded as the baseline for LOC.07 budgets.

## Verification

```bash
set -euo pipefail
# CONTRACT_ROOT is the source location chosen in open question 7, e.g. ~/.ημ/contract or ~/.agents/.ημ/contract
# parse every fragment, require at least one and unique ids, and total the UTF-8 bytes of tier-0 fragments
CONTRACT_ROOT="${CONTRACT_ROOT:?set CONTRACT_ROOT to the open-question-7 source location}" bb -e '
(let [dir (str (System/getenv "CONTRACT_ROOT") "/fragments")
      _   (assert (.isDirectory (java.io.File. dir)) (str "no fragments directory: " dir))
      fs  (filter #(.isFile %) (file-seq (java.io.File. dir)))
      _   (assert (seq fs) (str "empty fragment scan: " dir))
      rd  (fn [f] (let [s (slurp f) form (read-string s)]
                    {:file (.getName f) :id (second form)
                     :tier (some #(when (and (seq? %) (= (first %) (quote tier))) (second %)) form)
                     :bytes (count (.getBytes s "UTF-8"))}))
      xs  (map rd fs)
      dup (->> xs (map :id) frequencies (filter #(> (val %) 1)))]
  (assert (every? :id xs) "fragment without id")
  (assert (empty? dup) (str "duplicate ids: " (pr-str dup)))
  (println "fragments" (count xs) "tier0-bytes" (reduce + (map :bytes (filter #(= 0 (:tier %)) xs)))))'
if grep -rnP '\x{05DC}\x{05D2}' "$CONTRACT_ROOT"; then echo "FAIL: look-alike operator"; exit 1; else echo ok; fi
```

## Risks

Losing a rule while merging. Mitigation: the traceability acceptance criterion
and review of the provenance table.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
