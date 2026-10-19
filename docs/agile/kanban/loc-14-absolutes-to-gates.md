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
- An absolute whose intended surface has no implementation yet is listed as a
  gap.

Only `:pr-gate` absolutes, and `:runtime-hook` absolutes on harnesses that have
the hook, are ever compressed to one line.

## Scope

- [ ] An inventory of absolutes in the LOC.03 fragments: the immutable
  directives, safety, license and ledger discipline.
- [ ] For each absolute, its surface and its enforcement: the existing gate or
  hook, cited by file and line, or a prose-only classification, or a gap entry.
- [ ] An audit table at `~/.ημ/contract/absolutes.edn` with one row per
  absolute: fragment id, surface, then gate path and line, hook, or
  follow-up card uuid.
- [ ] A follow-up card for each gap. This story does not implement gates.

## Non-goals

- Writing new gates.

## Acceptance criteria

- [ ] GIVEN the fragment set WHEN filtered for absolutes THEN each absolute has
  exactly one audit row, and each row's evidence resolves:
  - a gate or hook path that exists, at the cited line;
  - or a prose-only classification for an `:interactive` absolute;
  - or a follow-up card uuid that exists on the board.
- [ ] GIVEN an `:interactive` absolute WHEN projected by LOC.08 THEN it appears
  in full.

## Verification

```bash
# per-absolute audit, not marker counts: every absolute has one row and its evidence resolves
# run from the foresight root so follow-up card uuids resolve against docs/agile/kanban
bb -e '
(let [rows  (clojure.edn/read-string (slurp (str (System/getenv "HOME") "/.ημ/contract/absolutes.edn")))
      cards (->> (file-seq (java.io.File. "docs/agile/kanban")) (filter #(.isFile %)) (map slurp))
      card? (fn [u] (some #(.contains % (str "uuid: \"" u "\"")) cards))]
  (doseq [{:keys [fragment surface gate-path card]} rows]
    (println fragment surface
             (cond gate-path (if (.exists (java.io.File. gate-path)) "gate-ok" "GATE-MISSING")
                   card      (if (card? card) "card-ok" "CARD-MISSING")
                   (= surface :interactive) "prose-only"
                   :else     "UNRESOLVED"))))'
```

## Risks

Some absolutes cannot be enforced as gates, for example "no faux feelings".
Mitigation: those stay as tier-0 prose and are listed as prose-only absolutes.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
