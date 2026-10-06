---
uuid: "b87eb466-81c4-41b3-831c-168edecac29f"
title: "Restore the standalone archaeology check with extracted Clio and lawful fixtures"
category: "kanban"
type: "story"
priority: "P1"
points: "3"
labels: "archaeology, clio, verification, dependency-routing"
status: "incoming"
epic: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
---

# Restore the standalone archaeology check

## Context

[Foresight issue132](https://github.com/open-hax/foresight/issues/132) records
two confirmed root defects discovered during [PR118](https://github.com/open-hax/foresight/pull/118)
review. At root `fcfc2d17f28640203066ddfe0a22f87db7372a32`,
`archaeology/deps.edn` routes Clio through `../eta-mu/packages/clio`, although
Foresight declares the extracted direct child `clio`.

With invocation-only overrides to private clones at the committed child pins,
`clojure -M:test` runs 6 tests / 21 assertions with 0 failures and 1 error:
`resource-events-preserves-independent-ledger-partitions` supplies event UUID
`00000000-0000-0000-0000-000000000001`, which fails canonical Clio version and
variant rules before the partition assertion. Other synthetic identities in the
same fixture family must be inspected, rather than fixing only the first error.

Actual PR118 records separately pass canonical Clio ledger/schema admission and
Foresight projection, yielding the expected parent/action continuity, 3 findings
and 5 evidence references. Evidence at PR118 commit
`23eff2e303af7beaeae5efa5bc95dd116e83909f` is retained under
`.ημ/verification/pr118-revision-cut-20261006/`. The fixture error does not prove
that these immutable records are invalid.

This is an initial manually authored incoming card, not a Rheos admission or
creation-event claim. Related migration cards E1.07/E1.09 are blocked and do
not supply ready-state authorization for this bounded implementation. Finish
planning review and use lawful Rheos readiness before implementation.

## Outcome

The documented standalone archaeology test command resolves the extracted
Clio owner from selected direct-child checkouts and exercises its intended
partition/projection assertions with lawful synthetic event identities.
Uninitialized or wrong selected child revisions remain visible failures.

## Scope

- Correct the root-owned Clio local-root in `archaeology/deps.edn` to the declared
  direct child, without changing Clio or Katamorph package policy or pins.
- Correct synthetic UUIDs consistently in
  `archaeology/test/foresight/archaeology/domain_test.cljc`, including causes and
  parent links, so fixture identity and the partition assertion are both tested.
- Use canonical `clio.law.event/event-identity-valid?` to verify intended valid
  fixtures and retain negative malformed-identity coverage. Add only the focused
  regression needed to expose accidental donor-path or invalid-fixture reuse.
- Reconcile `.ημ/archaeology/README.md` if the actual standalone prerequisites
  or command need clarification; retain precise verification counts/evidence.

## Non-goals

No historical run/resource/schema/event edits, event normalization, shape-law
weakening, child forks, dependency repins, donor restoration, alternate ledger
parser, Rheos implementation, model workflow edits, live service operation, or
promotion of archaeological candidates to accepted law. Do not use a sibling
agent checkout, shared mutable configuration or a stale donor classpath as proof.

## Acceptance criteria

- A fresh root with only the committed Clio and Katamorph children selected can
  run `cd archaeology && clojure -M:test` using the checked-in dependency map;
  no retired `eta-mu/packages/clio` path or invocation override is needed.
- All six existing tests execute their intended assertions, with positive
  assertion counts and zero failures/errors; no test is removed or skipped.
- Valid fixture UUID version/variant, causes, and parent relationships are
  checked through canonical Clio law. A malformed identity still fails admission.
- The actual selected archaeology resource union still passes canonical Clio
  historical-schema/causal admission and derives the PR118 projection when those
  records are available; record inputs instead of copying absent history.
- All pre-existing ledger/resource/schema bytes and child gitlinks are identical
  before and after. Root unit/contracts/lint/receipt-history/diff checks pass.
- Evidence binds the root commit and both actual child HEADs; unavailable
  hydration, commands or artifacts stay unavailable, never passing by omission.

## Verification

First retain the current failing fixture output and stale dependency route as
red evidence. Perform the bounded corrections in a separate isolated worktree
when this card is admitted. Hydrate selected children only at committed pins,
then run the documented focused command without dependency overrides and retain
test/ assertion counts. Confirm both lawful and rejected synthetic identities
using Clio's existing law; do not restate the UUID regex here.

Run the root project/catalog/unit and zero-warning lint checks appropriate to the
changed paths. Compare every prior archaeological artifact and selected gitlink
byte/ID, then validate the append-only receipt envelope/history. Native review
must distinguish the passing actual-record admission from the repaired fixture
suite and any remaining unavailable integration proof.

## Risks

A fixture-only patch can hide broken dependency routing, and a route-only patch
leaves the fixture suite failing. Correct both within this three-point story;
if another owned boundary is needed, surface a follow-up instead of expanding
scope. Repeated classpath caches must not silently select a donor source. A green
focused suite cannot grant accepted-law or deployment authority.
