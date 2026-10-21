# E0 execution: root receipt contract correction and hosted publisher proof

The root receipt gate **fails** against accepted base `cffc3444`. I wrote two
nested eta-mu events into Foresight's flat receipt stream. Their ETA validation
did not establish Foresight validity. This report withdraws that implication
from the prior receipt-adjudication and publisher checkpoints. Every original
byte, source-window report, failed check and immutable attempt remains intact.

The retained 05:01:54–05:02:15 UTC census remains **442 frozen PRs, 441 open,
one observed merge and five changed open reported head/base pairs**. This
correction does not claim another census sweep or close a frozen source.

## Actual root contract and immutable failure

At `ef538b657568b4339f738665a86f3f8f34fa6280`, the root
`foresight.evidence/receipt-envelope?` requires top-level keyword `kind`,
nonblank string `ts`, `origin`, `owner`, `dod`, `pi`, `host`, and string vectors
`manifest` and `refs`. Root `evidence/require-valid-receipt-records!` additionally
checks evidence receipts. Neither API delegates to the child ETA event schema.

The actual accepted-base check has 206 base records and 40 appended source
records, of which **two fail** the root envelope. A whole-history diagnostic
finds 13 invalid records, including 11 already inside the accepted base. Those
historical rows are not the two new failures and are not newly adjudicated.

| Root line | Introducing source | ETA event ID | SHA-256 of original physical line, including LF |
| --- | --- | --- | --- |
| 245 | `3b272e9ecd472e3af02ff420c6e1297a6cf7c258` | `fde0bbb9-adb0-4698-9d15-42fa3a695dfc` | `75fb3b2c1d39ece9e4939bb2b96258bee97545921c78367c6d304d94d7309d39` |
| 246 | `ef538b657568b4339f738665a86f3f8f34fa6280` | `2779af77-d433-4ff2-b958-d0b4e1d1130e` | `923bc9087d54f01dbfb80ce1195b410697e16dfc6bbbbb1a5b05e6c595fa02c8` |

One prospective **flat root catalog** now passes the actual owning validator,
one-line encoder and EDN roundtrip. Six negative controls refuse both nested
events, missing required keys and string-valued manifest/refs. The existing
descriptor-bound root `append-edn-line!` writes the validated catalog. All
386,287 prior bytes remain identical. The new 41-record suffix still has exactly
two invalid records; the whole stream now has 247 records and 13 invalid rows.
The catalog retracts a validity claim; it does not repair, supersede, admit or
retrovalidate the two records. The trusted base and validator are unchanged.
The actual current-base validator still refuses. Native P1 `4333214808`
remains open and root merge qualification remains held.

## Existing correction work recovered, without widening its authority

Frozen personal Foresight PR 3, exact source
`cf57e8988f32ea3a8adc2dfaab5993dd5875c622`, contains existing card
`707901cb-e498-4ce3-aa1c-245202ced648`: “Support bound envelope corrections
in immutable receipt verification.” Native tree and three Git blobs were
captured and verified read-only. A local missing-object lookup was corrected
through the native API; missing local history did not establish absent work.

Its pure law permits corrections only to **manifest, refs, dod and pi**, bound
to immutable source/line/origin/LF hash and ancestry. It forbids changing kind,
timestamp, owner, origin, host or execution semantics. Therefore it cannot
repair these wholly nested ETA events. Its historical completed state and
consumer proof are distinct from adoption at the current accepted root. No
source was imported, executed, promoted or used as a gate waiver.

## Newly completed hosted publisher bundle

Existing [eta-mu #362](https://github.com/open-hax/eta-mu/pull/362), at
`5610a2cc42126114cd599cef33cbee1af97000e3`, completed sandbox run
[`39550202681`](https://github.com/open-hax/eta-mu/actions/runs/39550202681)
/ job `119057415817`, attempt 1, at 05:55:34 UTC. Actual checkout logs and the
native Git commit bind base `7112c36440d180ca183d6a3f89661cee161fdae5`, tested
merge `c63d7c5648dae6afb81e92103da2714f77023f18`, both parents and tree
`11de5ce3eb07b4d67dffa800349ced4a6211bba5`. The registered publisher owner
actually ran **14 tests / 216 assertions**. The bundle was allowed to finish;
no restart or replacement run supplies this result.

Artifact `12323938548` is 315,410,622 bytes, unexpired at readback, with native
digest `468ff4b9328070deafbc906bd9cb3518101453bb8dcc5b7fcbf1f8192fe09cd7`.
Only metadata was read; no download, restore or same-pin identity claim is made.
The log retains 11 deprecation/GitHub warning markers and the intentional
SIGTERM-to-SIGKILL fixture diagnostic. Compiler zero-warning messages do not
erase those carrier warnings; strict whole-carrier qualification stays held.

Canonical current-head status has 16 passing checks, four skipped checks, no
native exact-head approval and no inline threads at this capture. CodeRabbit
is rate-limited until 06:02:20 UTC; Codex's account reset is UNKNOWN. Missing or
skipped routes supply no approval. The portable core remains an in-progress
part of the existing publisher, without actual native collection, physical
archive bounds, wire publication/readback, protected setup or default adoption.
The full publisher, E0 and the stabilization goal remain unfinished.

After the retained status window and cooldown expiry, canonical pr-flow sent
[one current-head CodeRabbit request](https://github.com/open-hax/eta-mu/pull/362#issuecomment-6309254437)
at 2026-10-21T06:03:07Z. Native readback binds `5610a2cc`; the request
supplies no review or approval credit. Codex was not retried.

## Targeted verification and preserved scope

Root evidence laws pass **17 tests / 89 assertions**, zero failures/errors.
The prospective root append and all six controls pass; the actual root ledger
qualification still fails. Current root run `39551062080`, bound to `ef538b65`,
has successful context/deterministic jobs but fails the model job
`119060436411` at missing App credentials and terminal job `119060545680`.
No credential restoration, gate relaxation, rollout, merge or deployment occurred.

The CI-isolation paragraph now separates words, counts and run/job IDs, fixing
native spacing P2 `4333214813`. Its old source-window facts and JSON are
unchanged. The encounter/recall/action-oracle P2 remains open. Root runtime,
tests, workflows, manifests and all 23 gitlinks are unchanged. Unrelated local
work is preserved. E0 stays breakdown, E1–E7 incoming, M0–M7 unqualified and
B1–B3 unassessed. **CMS/Cephalon agents remain paused.**

[Machine overlay](e0-root-receipt-contract.json) ·
[Immutable native captures](../../.ημ/stabilization/snapshots/9fec8c09d58756a92492e603608d0b40d6114d99918c9e71b14ec8be94f9e356.json.gz).

[Final verification and native review request](../../.ημ/stabilization/snapshots/8a8cbbe84c4f1500a7e38a3a5f168e01d0ced2ab9c827052290cf9a33f16fd7a.json.gz).
