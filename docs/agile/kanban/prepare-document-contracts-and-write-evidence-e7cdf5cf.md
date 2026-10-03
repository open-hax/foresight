---
uuid: "e7cdf5cf-423e-49fc-aca8-6ed6055f1cd5"
title: "Prepare document contracts, source-preserving writes, and provider review design"
type: "epic"
category: "content"
status: "incoming"
priority: "P1"
points: 11
labels: "content, alpha, rheos, epiphany, preparation"
---

# Prepare document contracts, source-preserving writes, and provider review design

## Context

(汝, p=1.00) The selected product loop is to open a document, discuss it, and
review an edit. Most sources will be Markdown, while compatible API results,
volatile data, and formatted raw inputs may produce document maps without
becoming files. The proposed [content-loop brief](../../notes/rheos-markdown-content-loop.md)
records that direction and the inspected implementation limits.

(己, p=0.99) Alpha code was prepared before the PR-flow skill and these cards
were supplied. Commit `0602064f9dafdd5708b790cd986de32b5e3e50fb` is an unaccepted
candidate, not evidence that planning review, card readiness, a prior red
commit, or independent implementation acceptance occurred.

## Outcome

Prepare three separately reviewable PRs that establish a portable admission
boundary, repair existing Rheos source preservation, and define Epiphany's
provider/review design before runtime document-provider integration begins.

## Scope

- Foresight/Alpha owns portable document observation/admission laws and their
  CLJ/CLJS evidence.
- `open-hax/rheos` owns metadata-preserving existing writes and real filesystem
  write-boundary evidence; Foresight consumes the reviewed upstream result.
- `octave-commons/epiphany` owns its provider/review design and decision gaps.
- Foresight coordinates the three PRs, linked evidence, planning review and
  later authoritative Rheos transitions. Story estimates total 11 points.

## Non-goals

- No new production document provider, API connector, provider registry or
  assembly execution engine.
- No complete browser read/discuss/propose/accept loop, proposal store, public
  hosting, general authorization subsystem or external-provider mutation.
- No root copy of Rheos parsing, board transitions, comments or write commands.
- No retroactive planning, fabricated failing-test history or automatic
  promotion of historical designs into accepted common law.

## Acceptance criteria

- The epic has the three UUID-linked stories listed below, each at most five
  points with testable scope, ownership and verification limits.
- Planning findings receive explicit dispositions before the stories are
  advanced through Rheos; the prepared Alpha candidate remains distinguishable
  from accepted work throughout review.
- Each preparatory PR records its exact source revision, observable evidence,
  remaining limits and independent review disposition.
- Alpha admission accepts the bounded Markdown and non-file fixtures and
  refuses malformed or mismatched contexts without asserting live freshness.
- Rheos proves an existing supported write preserves unrelated source content
  through a real file write and reread, with failure outcomes visible.
- Epiphany's reviewed design separates identity, source availability, content
  retention, review retention and mutation authority without universalizing Git.
- Any later runtime integration is a separately planned item; completion of
  this epic does not claim the user-facing document loop is delivered.

## Verification

Inspect the planning and implementation/design PRs against each child's
acceptance criteria. Attach revision-bound commands/results or design review
records to their owning PRs. Use the authoritative Rheos engine for status and
comment events; initial Markdown authoring is not a transition history.

- Alpha: `e6aa52eb-681d-4d79-8605-87aa1fc8b4ca` — 3 points.
- Rheos: `819717c4-735b-4dd5-b5b7-ba860d3942f9` — 5 points.
- Epiphany: `76983190-be58-47ac-849c-c8a9f37fe573` — 3 points.

These preparations have no implementation dependency on one another. Their
combined outcomes inform the later content-loop slice.

## Risks

Contract acceptance can be mistaken for runtime availability; a green mock can
be mistaken for a durable write; a reviewed design can be mistaken for an
implemented provider. Keep those evidence boundaries explicit. Child repository
ownership and reviewer/tool availability may require a recorded blocker rather
than a completion claim.
