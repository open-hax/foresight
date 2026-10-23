# E0 Clio-backed intent and boundary execution

The published ETA checkpoint is `fa117fba0b761f8aa655e26c2ae8675b1af178cd`,
with functional boundary repair `bb82398568f7abf7f2c1103e0aa34806b7b87153`.
The private capability/recovery vault belongs to infra composition; extern owns
host objects, worker I/O, bounds, clock and descriptor observations. The forbidden
extern-to-domain dependency and raw worker host operations have causal RED/GREEN
history. Fresh retained intent tests pass **23 tests / 110 assertions**. All eight
files changed since published `a76225c0` reproduce their native GitHub blob bytes.
Broader successor tests and native qualification remain pending.

Canonical Clio candidate `5fad794de1a9e23d6e5aaad42eaa933be1c070be` remains
in [Clio #5](https://github.com/riatzukiza/clio/pull/5). It checks optional expected
descriptor identity under its owning lock before content read/append, preserving
old arities and keeping raw handles in externs. Earlier owning BB 29/182,
JVM 134/605, NBB 109/583 and compiled 109/583, independent JVM/NBB 8/49 and eleven
native byte comparisons are retained in ETA's immutable 809-capture archive.
Candidate use is not accepted pin advancement. The new native CodeRabbit approval
binds exact `5fad`; review convergence remains blocked with 63 threads / zero
unresolved. Codex quota has UNKNOWN reset and is not retried.

## Fresh frozen-cohort readback

The [census JSON](e0-clio-intent-census.json) and [CSV](e0-clio-intent-census.csv)
cover the same 442 frozen identities, **October 23 02:32:04–02:32:26 UTC**:
**437 OPEN / four MERGED / one CLOSED / six changed open head/base pairs**.
Both passes agree on source tuples and proposed merges. Axxium #3 and #4 changed
heads since the retained October 22 04:14 window. The earlier count of four stays
historical; the coordinator's transient four-count statement was corrected.

There are 144 independent live base refs; 181 open PRs report a base different
from its live branch tip. Proposed parent pairs match current B/H for 295 open
PRs, use older/different parents for five, and are unavailable for 137. All tested
candidates remain null. These observations confer no source acceptance, unique-work
reconciliation, merge qualification or new-PR census coverage.

## Actual hosted results and newly reproduced regression

Sandbox [39721175314](https://github.com/open-hax/eta-mu/actions/runs/39721175314)
succeeds at older ETA head `a76225c0bea040473949cb394d8d530fd23f1afc`.
Actual merge `30ccce5f6a94601f6f527d9609b45443152d047b` has expected base/head
parents and tree `866497bd19c676df15698e5d7bcb49ad798bf3b1`, equal to that head.
The intent 18/88, collection 30/224, publication 17/557 and CLI 194/587 plus
workflow 11/199 runs belong to that source. Twenty-three warning diagnostics keep
the whole-carrier warning hold open. The 327894869-byte artifact has metadata,
not download/restore proof. Original event G/executed caller custody remains absent.

Rheos 39721175244, sync 39721175305 and Sol 39721175246 fail in workflow-contract
bootstrap: package-wide NBB dependencies invoke absent `bb` before the tests.
They do not demonstrate product-suite failures; downstream suites skip.
Introducing source is `ed0fde2f5a1902f2258e16b0b9228b0913e1ea59`.
Tests-only RED `b0ac577cfd3c899d4baac553c870d47918933850` reproduces the failure.
Local GREEN `14cb2742bfd1a15d5ab450b8f3ee6d3a64147790` narrows the dependency
configuration to dedicated intent runtime entrypoints. Owning agent reports cold
workflow 11/199 with both configured NBB 1.5.211 and 1.5.212, focused 23/110, registered
review-workflow and zero-warning lint passes. Independent/full successor gates,
publication and hosted proof are still pending. The separate native append-error
classifier P1 and collection assertion finding are under scoped causal repair.

## Preserved failures and publication holds

Root `d8ece877c9a9e07a87fec12f650bced173f6b164` run 39708473352 fails.
Its deterministic job concludes success while receipt_history exits 2 and emits
deterministic_output=failure; missing-App setup and required terminal gate fail.
Fresh local receipt verification also exits 2: two new invalid ETA envelopes and
thirteen invalid history rows remain untouched. Root P1 and character-oracle
findings stay open. Root validation and 36 tests / 286 assertions pass; lint has zero
errors/warnings. No credential or gate waiver is introduced.

Native PR-flow root status remains blocked, two unresolved / zero approvals.
ETA remains blocked, one P1 / zero approvals, with evidence changing during the
capture. A successful old-head run never qualifies the successor. Full publisher,
original G/caller, complete reconstruction/verifier composition, native publication
readback/status and protected installation/adoption remain unfinished.

All 23 root pins, primary ETA `0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90` and
primary Clio `788cdd3434615a7932b924e68520dbf7f88408c2` remain fixed.
E0 is breakdown; E1 incoming; existing publisher in_progress; M0–M7 unqualified.
CMS/Cephalon remain paused. No new card, standalone plan, merge or deployment.

## Evidence custody

The [execution overlay](e0-clio-intent-execution.json) binds a content-addressed
capture archive under `.ημ/stabilization/snapshots/`; every capture records its
byte count and SHA256. Durable raw captures and reproducible acquisition scripts
are in `.ημ/stabilization/execution-20261023/`. Earlier uncommitted coordinator
projections are preserved there before refresh. October22 census archives and
all prior published reports remain intact.

The committed ETA 809-capture archive survives. Unarchived temporary layer captures
were lost during host resume; they are not represented as retained evidence.
Fresh focused 23/110 evidence is committed at fa117. Bootstrap evidence is being
written inside the owned persistent worktree, never relying on ephemeral `/tmp`.
