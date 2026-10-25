---
uuid: "9f439b79-b810-4b59-97ed-8911ca8ab1b8"
title: "Compose immutable receipt corrections with the segmented reader"
status: "testing"
priority: "P1"
points: "5"
labels: ["receipts", "evidence", "compatibility", "antigravity"]
created_at: "2026-10-25"
write-id: "1792912370878-0.9qc533uwk1ezmm5qcnv"
---

## Context

The Antigravity rollout in [Foresight #145](https://github.com/open-hax/foresight/pull/145)
preserves two malformed ordinary receipt envelopes. The first, introduced at
`1f3f7317012e23d7ec0f35a7e22b2b01cdff65b3`, has a valid `:observation` kind
and scalar `:manifest` / `:refs`. The second, present at
`c61c563d25c15ba13751461b0235235cb6437f16`, has `:kind "test-run"`.
Neither is a revision-bound Foresight evidence receipt. Their original source
bytes, timestamps, observations and historical test statements must survive.

[Fork #35](https://github.com/riatzukiza/foresight/pull/35), adopted separately in
[Foresight #147](https://github.com/open-hax/foresight/pull/147), supplies the
segmented ledger, immutable-segment and record-conservation checks. It does not
admit corrections. [Fork #28](https://github.com/riatzukiza/foresight/pull/28)
already supplies strict, source-bound documentary correction views for legacy
journals. That reader permits only `:manifest`, `:refs`, `:dod` and `:pi`;
changing `:kind` is outside its accepted contract. Its physical current-line
coordinates also need explicit composition with records moved into segments.

This plan starts from #147 head
`02f330d45a7ccbf14349d1b1638927f473fb58ae`. It imports no malformed records into
its active ledger. The census contains historical **test fixtures**, not correction
entries admitted at this planning head. Source ancestry is evaluated against the
consumer being verified (`--at`, or the locked writer's held HEAD), never the
planning branch that supplies the reader. Both original revisions are ancestors
of published consumer head `d9fb8d4f87ca664e8b1bdb0a6d721a1343b59e2d` and its
segmented descendant `2b9df80d0135804be0998ee6be23cc0453767993`; neither is an
ancestor of planning head `2c451cc7548e425fd975e4049fdbc1da3fb828ba`.
Admission at that planning head must therefore reject those fixture sources.
Never substitute different receipts at the same physical ordinals to manufacture
ancestry. Each positive test constructs an ancestor containing the exact fixture
bytes; negative tests deliberately use a detached source. The census pins both
roles and verifies the source line, raw UTF-8 and LF hash together.

Initial Incoming metadata is a manually authored board input,
not a claimed Rheos admission or transition. Consumer card
`4634f396-e931-41d1-8883-0282b85e6022` names the rollout; this prerequisite does
not depend on completing that consumer.

## Outcome

The existing three receipt-validation consumers admit explicitly bound,
representation-only correction views in a segmented ledger while retaining all
original records and exposing every correction's provenance. The exact #145
receipt defect becomes admissible only after that reader and its narrowly scoped
kind extension have been reviewed, tested and adopted. Missing or unsupported
corrections remain actual failures.

## Scope

- Reuse the existing `foresight.evidence` correction decision and source-byte
  verification from #28. Keep ordinary `receipt-envelope?` strict and retain
  original maps separately from derived views. Compose the decision with #35's
  merged-ledger conservation checks rather than creating another ledger or CLI.
- Bind each correction to a full same-repository source revision, original
  source path, physical source line, origin and SHA256 including the final LF.
  Permit only the canonical legacy path or a regular immutable receipt segment,
  with the existing UTF-8, file-type and path-safety checks. Authenticate Git
  bytes and ancestry; locate exactly one byte-identical
  active original even after a permitted whole-line move into a segment. Current
  merged order or a reserialized map must never substitute for source identity.
- Retain the accepted four field interpretations exactly. Add only the explicit
  interpretation `"test-run"` to `:test-run` for an ordinary receipt: the target
  must not have the reserved evidence origin, any `:evidence/*` fields, an event
  envelope or correction entries. Reject
  every other kind conversion, semantic field change and partial correction.
  This is a proposed extension, not a claim that #28 already allows it.
- Route immutable verification, promotion readiness and held-ledger validation
  through the same admission decision. Reauthenticate correction documents even
  after they enter the comparison base; refuse duplicate, ambiguous, missing,
  non-ancestor or mismatched targets. Preserve original evidence validation.
- Apply the qualified reader to #145 using new correction segments and permitted
  verbatim whole-line relocation of its divergent legacy additions. Prove every
  original remains present exactly once. Record the actual outcome separately
  from historical observations; never rewrite them.

## Shared admission contract

`correction-document?` is an exclusion/scan discriminator: a map containing
`:correction/entries`, regardless of its value or any additional keys. It is not
proof of validity. A valid correction document has `:kind :correction`, the nine
required ordinary envelope keys, optional documentary metadata, and a nonempty
vector at `:correction/entries`. A plain historical `:kind :correction` record
without that marker remains documentary data and grants no correction authority.

Each entry is a closed map with these seven keys:

```clojure
{:receipt/source-revision "<full same-repository commit SHA>"
 :receipt/source-path ".ημ/receipts.edn or a regular .ημ/receipts/<name>.edn"
 :receipt/line 1
 :receipt/origin "<exact original origin>"
 :receipt/source-sha256 "<SHA256 of exact physical source bytes including LF>"
 :receipt/identity "<existing record-id of the original parsed map>"
 :envelope/corrected-fields {:manifest ["<exact original scalar>"]}}
```

Retain #28's closed five-key entry as a compatibility variant: it omits only
`:receipt/source-path` and `:receipt/identity`, defaults to the canonical legacy
path and derives identity from authenticated source bytes. That variant retains
only #28's four permitted field interpretations. The explicit kind extension
requires the fully qualified seven-key variant. Neither variant accepts unknown
entry keys, empty corrected fields, invalid locators or partially valid changes.
Target identity always identifies the original map, never the derived view.

The shared pure exclusion predicates have open key sets deliberately:
`event-envelope?` means a map containing `:event/type`; `correction-entry?` means
a map containing `:envelope/corrected-fields`. Nil/unknown marker values and
additional keys still match; these are conservative target exclusions, not
claims of event/entry validity. A plain map with `:event/id` alone is not an event
and remains ordinary data. Kind correction excludes either predicate, a
`correction-document?`, the reserved evidence origin, or any keyword key whose
namespace is exactly `evidence`. Valid-looking hybrid maps cannot evade these
exclusions by adding receipt envelope keys.

One adapter operation, `admit-merged-receipt-extension!`, serves immutable
verification, promotion readiness and held-ledger validation. It first runs
#35's raw record/segment conservation checks. It then scans **every head
record**, including frozen legacy history, every base segment and every new
segment, for `correction-document?`. It validates each document and entry,
reauthenticates each source against the current consumer anchor, and requires
exactly one active byte-identical original at any permitted current location.
Duplicate target claims across correction documents fail. The existing pure
correction law derives disclosed views without changing originals. Validate all
correction documents, targeted derived views and newly introduced records with
the strict envelope and existing evidence-semantic checks. Untargeted ordinary
base records retain the migration's grandfathering; a correction entering the
base never acquires grandfathered authority. Correction order is based on the
source ancestry and original identity, not timestamp/display/merged ordinal.

The base/source census at #147 head and composed consumer head found no parsed
correction-document markers; historical narrative corrections are not silently
reclassified. Tests must also introduce a correction, commit it into the base,
then corrupt/revoke its binding and prove all three consumers still reject it.

## Non-goals

Historical receipt edits; automatic coercion; kind changes on evidence receipts;
changes to result, approval, owner, origin, timestamp, revision or schema; merge
authorization; a second receipt authority; changes to child repository contracts;
full Clio cutover or fabricated Clio envelopes. The current segmented migration
is not evidence that the dedicated all-Clio cutover has occurred.

## Acceptance criteria

1. Frozen fixtures taken from the two exact source commits fail with current
   source for the expected envelope defects. Positive source revisions are ancestors
   of their actual consumer anchors; the isolated planning-head/non-ancestor
   pairing is an explicit failing control. Fixtures retain exact original bytes
   and LF hashes, including the first record's already-valid keyword kind.
2. The existing four permitted field interpretations remain identical. One
   explicit bound `"test-run"` to `:test-run` correction admits the ordinary
   second record; no unbound automatic normalization or already-valid-field
   substitution passes. Originals and provenance remain separately inspectable.
3. Wrong path/line/hash/origin/revision, absent LF, source non-ancestry, duplicate
   targets, ambiguous active copies, source-byte edits, deleted base segments,
   dropped records and partial unsupported corrections fail closed.
4. A target carrying the reserved evidence origin or any `:evidence/*` key
   refuses kind correction. Failed, unavailable, stale and invalid revision-bound
   results retain their original validation outcomes. A corrected ordinary
   test-run supplies no promotion result, provider approval or CI success.
   Positive/negative fixtures cover both shape predicates, extra keys, nil and
   unknown marker values, event/entry hybrids, correction documents, and the
   ordinary `:event/id`-only control.
5. The same positive and negative cases cover all three consumers, including
   reauthentication when the correction already belongs to the base, a moved
   legacy line, and an uncommitted source descending from the held HEAD.
6. Immutable #145 verification against its actual integration base passes only
   after adopted correction admission and explicit corrections. A conservation
   audit proves all original records and committed segment bytes preserved.

## Verification

Capture RED before implementation; extend pure `.cljc` laws first, then the NBB
adapter. Run evidence law and adapter suites, project validation and zero-warning
clj-kondo. Collect the native deterministic receipt-history result on the exact
consumer head. Planning review and lawful Rheos readiness precede implementation;
local tests or a source-only proposal supply no native review or merge credit.

## Risks

Physical source coordinates and merged display order are different identities.
Accidentally admitting metadata as evidence would weaken promotion gates; the
explicit evidence exclusion and semantic negative controls guard that boundary.
The two upstream features have diverged and must be composed with their existing
regressions intact. #147's legacy model-review caller currently withholds App
credentials by design, so its missing-credential result remains visible and
cannot be turned into an approving review.