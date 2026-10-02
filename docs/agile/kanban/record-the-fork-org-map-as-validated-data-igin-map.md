---
uuid: "dev-origin-map"
title: "Record the fork-org map as validated data"
status: "incoming"
type: "task"
priority: "P1"
points: "2"
labels: "workspace, law"
parent: "fork-dev-origins"
category: "kanban"
write-id: "1790900465819-0.qjktgtt0chhkx20d6"
created_at: "2026-10-02T00:21:05.819Z"
---

# Record the fork↔org map as validated data

## Outcome

`config/dev-origins.edn` lists, for each org submodule, its dev origin (`riatzukiza/<name>`) and its org upstream. A pure `.cljc` law validates the file, and `nbb scripts/project.clj validate` fails when a submodule has no entry, an entry names no submodule, two entries share a fork, or a fork name breaks GitHub's one-fork-per-network rule as recorded.

## Context

Every other story reads this map. Known exceptions (observed 2026-10-01):
- `open-hax/opencode` uses the existing `riatzukiza/mojomast-opencode`.
- `open-hax/uxx` needs a non-`uxx` name, because `riatzukiza/uxx` forks `shuv1337/uxx`.

## Acceptance criteria

- [ ] GIVEN `.gitmodules` and `config/dev-origins.edn` WHEN validated THEN every `open-hax`/`octave-commons` submodule has exactly one entry with `:dev/origin` and `:org/upstream`.
- [ ] GIVEN an entry whose `:org/upstream` is not a declared submodule WHEN validated THEN it fails, naming the entry.
- [ ] GIVEN two entries with the same `:dev/origin` WHEN validated THEN it fails.
- [ ] VERIFY: the law lives in `src/foresight/*.cljc`, with no I/O.

## Verification

```bash
nbb scripts/project.clj validate
nbb -cp scripts:test test/project_test.cljs
clj-kondo --lint scripts test src
```

## Anti-patterns

- Do not derive fork names at runtime from repository names; exceptions must be explicit data.
