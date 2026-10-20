---
category: "kanban"
labels: "contracts, pr-flow, gates, 3sp"
type: "story"
story_id: "LOC.14"
points: "3"
title: "LOC.14 — Classify every absolute by enforcement surface and map it to its gate, hook, prose-only status or gap"
dependency: ["loc-fragment-source"]
priority: "P2"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-absolutes-to-gates"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.14 — Classify every absolute by enforcement surface and map it to its gate, hook, prose-only status or gap

> Design: `docs/notes/design/layered-operating-contracts.md` §The fold (absolutes), §Projection
> Target: `~/.ημ/contract` fragments plus the existing pr-flow and eta-mu review gates
> Blocked by: LOC.03

## Context

The user's rule is that anything absolute is enforced at the workflow layer, in
pull requests. Restating absolutes in full prose costs context in every
session.

## Outcome

Every fragment marked `(absolute)` is classified by its enforcement surface.

- `:pr-gate` absolutes name an existing gate, for example a pr-flow law, a
  required check or an eta-mu review gate.
- `:runtime-hook` absolutes name an existing host hook.
- `:interactive` absolutes are recorded as prose-only. They stay full tier-0
  prose until a specified runtime check exists.
- `:gap` absolutes are those whose intended enforcement does not exist yet.
  Each one carries a follow-up card uuid and keeps full tier-0 prose. A card
  is never accepted as evidence for `:pr-gate` or `:runtime-hook`.

Only `:pr-gate` absolutes with real gate evidence, and `:runtime-hook`
absolutes with real hook evidence on harnesses that have the hook, are ever
compressed to one line.

## Scope

- [ ] An inventory of absolutes in the LOC.03 fragments: the immutable
  directives, safety, license and ledger discipline.
- [ ] For each absolute, its surface and its enforcement: the existing gate or
  hook, cited by file and line, or a prose-only classification, or a gap entry.
- [ ] An audit table at `$CONTRACT_ROOT/absolutes.edn` (the open-question-7
  location), with exactly one row per absolute. Each row holds `:fragment` and
  `:surface`, then the evidence for that surface:
  - `:pr-gate` needs `:gate-path` and `:gate-line`;
  - `:runtime-hook` needs `:hook-path` and `:hook-event`;
  - `:gap` needs a follow-up `:card` uuid.

  Interactive rows need no evidence. A row's `:surface` must equal the surface
  its fragment declares in `(absolute :surface …)`.
- [ ] A follow-up card for each gap. This story does not implement gates.

## Non-goals

- Writing new gates.

## Acceptance criteria

- [ ] GIVEN the fragment set WHEN filtered for absolutes THEN each absolute has
  exactly one audit row, and the row's `:surface` equals the surface the
  fragment declares.
- [ ] GIVEN each row WHEN its evidence is checked THEN the evidence matches its
  surface:
  - `:pr-gate`: a gate file with the cited line;
  - `:runtime-hook`: an existing hook file and a hook event;
  - `:gap`: a follow-up card uuid that exists on the board;
  - `:interactive`: no evidence.

  A card is never accepted as evidence for `:pr-gate` or `:runtime-hook`.
- [ ] GIVEN an `:interactive` absolute WHEN projected by LOC.08 THEN it appears
  in full.

## Verification

```bash
# run from the foresight root with CONTRACT_ROOT set (open question 7); exits nonzero on any problem
set -euo pipefail
CONTRACT_ROOT="${CONTRACT_ROOT:?set CONTRACT_ROOT}" bb -e '
(require (quote [clojure.edn :as edn]) (quote [clojure.string :as str]))
(let [root     (System/getenv "CONTRACT_ROOT")
      one      (fn [f] (let [forms (edn/read-string (str "[" (slurp f) "\n]"))]
                         (assert (and (= 1 (count forms)) (seq? (first forms)) (= (quote fragment) (ffirst forms)))
                                 (str "expected exactly one (fragment ...) form: " f))
                         (first forms)))
      frags    (->> (file-seq (java.io.File. (str root "/fragments"))) (filter #(.isFile %)) (map one))
      absolute (fn [form] (some #(when (and (seq? %) (= (first %) (quote absolute))) %) form))
      declared (into {} (keep (fn [f] (when-let [a (absolute f)] [(second f) (:surface (apply hash-map (rest a)))])) frags))
      rows     (edn/read-string (slurp (str root "/absolutes.edn")))
      ids      (map :fragment rows)
      cards    (->> (file-seq (java.io.File. "docs/agile/kanban")) (filter #(.isFile %)) (map slurp))
      card?    (fn [u] (and u (some #(str/includes? % (str "uuid: \"" u "\"")) cards)))
      line-ok? (fn [p l] (let [f (java.io.File. (str p))] (and (.isFile f) (pos-int? l) (<= l (count (str/split-lines (slurp f)))))))
      problems (concat
                 (for [m (sort (remove (set ids) (keys declared)))]  [:missing-row m])
                 (for [x (sort (remove (set (keys declared)) ids))]  [:row-without-absolute x])
                 (for [[k n] (frequencies ids) :when (> n 1)]        [:duplicate-row k])
                 (for [{:keys [fragment surface]} rows
                       :let [d (get declared fragment)]
                       :when (and d (not= d surface))]               [:surface-mismatch fragment {:declared d :row surface}])
                 (for [{:keys [fragment surface gate-path gate-line hook-path hook-event card]} rows
                       :let [why (case surface
                                   :pr-gate      (when-not (line-ok? gate-path gate-line) :gate-evidence)
                                   :runtime-hook (when-not (and (.isFile (java.io.File. (str hook-path))) (some? hook-event)) :hook-evidence)
                                   :gap          (when-not (card? card) :gap-card)
                                   :interactive  nil
                                   :unknown-surface)]
                       :when why] [why fragment]))]
  (doseq [p problems] (println "FAIL" p))
  (println "absolutes" (count declared) "rows" (count rows) "problems" (count problems))
  (System/exit (if (empty? problems) 0 1)))'
```

## Risks

Some absolutes cannot be enforced as gates, for example "no faux feelings".
Mitigation: those stay as tier-0 prose and are listed as prose-only absolutes.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
