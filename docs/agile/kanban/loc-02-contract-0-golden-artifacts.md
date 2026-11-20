---
category: "kanban"
labels: "contracts, muse, preservation, testing, 3sp"
type: "story"
story_id: "LOC.02"
points: "3"
title: "LOC.02 — Prove contract-0 still builds byte-identical artifacts"
dependency: ["loc-contract-0-freeze"]
priority: "P1"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-contract-0-golden-artifacts"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.02 — Prove contract-0 still builds byte-identical artifacts

> Design: `docs/notes/design/layered-operating-contracts.md` §Contract-0 preservation, item 3
> Target repository: muse (the tree chosen in LOC.01)
> Blocked by: LOC.01

## Context

Contract-0 has to survive every later change. Only a golden comparison against
today's outputs can show that it did.

## Outcome

A test rebuilds contract-0 and compares its artifacts byte for byte against
committed golden copies. This test must exist before any layered-compiler code
lands.

## Scope

- [ ] Capture the golden artifacts:
  - the OpenCode plugin bundle digest;
  - the shim;
  - `.opencode/opencode.json` permissions;
  - the agents Markdown;
  - `.mcp.json`;
  - `.claude/settings.json`, including its hook entries;
  - the generated `src/gen/**` entrypoint;
  - `.ημ/contract-0.edn` from LOC.01, with its per-file SHA-256 values, so
    that a source edit is detected even when the projections are unchanged.
- [ ] Normalise only documented sources of nondeterminism, such as absolute
  home paths and build timestamps, and list each normalisation in the test.
- [ ] Wire the comparison into the existing test build.

## Non-goals

- Layered compilation (LOC.09 proves contract-0 through the layered path).
- Fixing existing artifact drift; record it instead.

## Acceptance criteria

- [ ] GIVEN an unchanged contract-0 WHEN the golden test runs THEN it passes.
- [ ] GIVEN a one-byte change to any plugin or config fragment WHEN the golden
  test runs THEN it fails and names the differing artifact.
- [ ] GIVEN the list of normalisations WHEN reviewed THEN each one cites the
  nondeterminism it removes.

## Verification

```bash
npx shadow-cljs compile test   # muse's existing test build, which runs the golden comparison
```

## Risks

Artifacts may not be deterministic today, for example because of bundle
hashing. Mitigation: normalise only documented nondeterministic fields, then
compare all remaining artifact bytes. Record unresolved exceptions as findings.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
