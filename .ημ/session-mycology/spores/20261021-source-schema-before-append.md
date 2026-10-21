---
status: incubating
created: 2026-10-21T04:50:09.194875Z
source-session: codex/stabilization-portfolio
source-task: Repair invalid receipt authoring without rewriting ledger history
p-efficiency: 0.71
p-friction: 0.80
p-skill-candidate: 0.85
promoted-to: ""
rejected-reason: ""
---

## Problem

The generic Receipt River template omitted repository identity and allowed a
Python UTC offset representation. The child package law required `:repo` and
UTC-Z string timestamps. Eleven new records therefore failed the actual API,
even while the full registered workspace test gate passed. Its baseline already
contained historical invalid records.

## Pattern

A global authoring recipe is weaker evidence than the owning source schema.
A shape-valid event can still bind the wrong repository or source revision.
An append-only ledger cannot repair that by silently changing past bytes.

## Candidate skill outline

- Before authoring, inspect the actual owner API/schema/version and its writer.
- Construct prospective events through that API; validate exact serialized bytes.
- Assert repository identity, producer source and subject source independently.
- Run negative controls for missing identity, wrong timestamp grammar and event type.
- Compare new-suffix validity separately from the unchanged historical result.
- If past records are invalid, append source/hash-bound adjudications using an
  existing event kind, retain failures, and state that no automatic supersession,
  retrospective validity or approval of execution claims follows.
- Never widen a validator or rewrite history to manufacture a pass.

## Better path

Freeze original bytes and source IDs, inspect the local builder, validate and
semantically bind prospective lines, then append once. After append, verify the
entire preserved prefix and actual new suffix. Publish the full-history failure
alongside any valid new records. Do not confuse a broad green CI suite with
admission coverage of the provenance file.

## Receipt refs

- New root catalog SHA256 `75fb3b2c1d39ece9e4939bb2b96258bee97545921c78367c6d304d94d7309d39`.
- Child correction `fb8bf0e18b0aac9179e821952adfa7f257c5160f`.
- Child evidence `d3f755771ed95fe44671dfd8d39b687ac919c387ceb26ea11c7384f8bfb5a9bd`.
- `docs/stabilization/e0-receipt-adjudication.md`.

License: GPL-3.0-or-later. Incubating only; no same-session promotion.
