# E0 execution — Retain directory ownership through segment operations

Existing [Clio #5](https://github.com/riatzukiza/clio/pull/5) contains a verified
parent-directory binding repair and a follow-up descriptor-retention correction.
These are forward source commits on the existing PR, coordinated through E0/E2.
No duplicate work item, merge or consumer-pin update was performed.

## Source and falsification

The prior head `38c0b8c9e45160ae4fecd7d87bf6c700f62cc631` remains an ancestor.
RED `6c4d26c2af7a83beb75f030f4abbc798509a4f88` reproduces six failures on both
JVM and NBB: same-name event substitution, foreign directory enumeration,
wrong-inode durability acknowledgement and temporary publication into another
store. GREEN `9a10f1d49d49fda55811b09138e961c781ca574b` retains one verified
parent descriptor across enumeration, segment opens, existing-entry durability
and temporary publication. Persistent pathname replacement still refuses.
Descriptor acquisition and callback success/failure lifetime tests are retained.

The full suite exposed a recovery regression and FIFO/force observer assumptions.
The repair preserves original in-flight recovery state; fixtures now recognize
descriptor aliases while still checking actual opened inode identity. The owned
FIFO hang was diagnosed from a thread dump before stopping that process.
Failed attempts are retained in native evidence.

Complete review pagination also recovered two body-only findings. The existing
real-process publication race test satisfies the earlier concurrency finding;
its native settlement names actual workers, outcomes, link count and cleanup.
The second finding required a source correction: active entry locks now retain
the already-validated descriptor. Link-count checks avoid another descriptor
scan; ordinary lock tokens retain discovery fallback. Corrected RED
`2959cec4ae7adc0ca602802699a70272484b4720` fails six assertions before GREEN
`7c85a26e3663f3c8b9f2fe038feb9ec5a9bb99cc`. Classpath and primitive-signature probe failures remain visible.

A further native finding identified stat-only descriptor proxies: a backing
directory stat does not prove child lookup works. A real-descriptor observer
reproduces three failures/four assertions. The Node capability probe now requires
`candidate + "/."` traversal, rejects both unusable namespaces with the documented
error, and closes the descriptor before any callback. Current head is
`5a7a04c6ced7b15be940e16dc772c95b0525d55f`; native macOS/Windows remain unqualified.

## Verification and review

| Gate | Latest result |
| --- | --- |
| JVM | 126 tests / 556 assertions, zero failures/errors |
| NBB | 101 / 534, zero failures/errors |
| Compiled Shadow | 101 / 534, zero failures/errors/compiler warnings |
| BB | 29 / 182, zero failures/errors |
| Lint and extern boundary | zero warnings/errors |
| [Hosted CI](https://github.com/riatzukiza/clio/actions/runs/39526548866) | success at the latest head |

The actual job log and native Git objects bind executed merge candidate
`efe27b0fb69b0439ed572f6b6a522f4e39db2a12` to base `7262ddaaf39b32deb1794c5c6595724b6d69538e`
and the published head. Its tree exactly equals the head tree. This is an
executed-source proof, not an inference from the workflow's head metadata.

The [directory-binding finding](https://github.com/riatzukiza/clio/pull/5#discussion_r4331978405)
has a Fixed reply and resolved readback. The complete native readback contains 63 threads,
with 0 unresolved at the named observation window.
The [race-test](https://github.com/riatzukiza/clio/pull/5#issuecomment-6305430429)
and [descriptor-retention](https://github.com/riatzukiza/clio/pull/5#issuecomment-6305495511)
body findings have itemized source-backed settlements. The traversal finding
also has a Fixed reply and resolved native readback. Fresh current-head review
is requested; review convergence remains unqualified. Codex's account-quota
boundary is unknown, and the MiMo route candidate in existing Clio #3 remains
unqualified. Passing checks and resolved threads do not supply an approval.

The implementation requires a validated descriptor bridge and refuses explicitly
where unavailable. Native macOS/Windows are not qualified. Read-only unsupported
directory-sync preflight and real force failures remain distinct contracts.

## Campaign custody

[E5 source recovery](e5-replay-custody.md) records the exact proposed numeric
contract and explicit adoption hold without altering the recovered milestone,
adopting a tolerance, reporting B1–B3 results or resuming Cephalon execution.
Root native run 39522223787 is queued; predecessor 39522109889 is now cancelled
after its earlier observed active MiMo step. Cancellation supplies no approval.
No timeout, cause or privileged rerun is inferred.

The [execution overlay](e0-directory-binding.json) and its immutable native
snapshot retain all revisions, gates, failures, settlements and source custody.
The previous 442 frozen / 441 open / one merge and five changed open head/base
observations remain historical at their named window. This is a targeted
execution refresh. Foresight #142 is separate remediation work, outside that
frozen denominator. All 23 consumer pins and unrelated work remain unchanged.
E0 stays breakdown, M0–M7 unqualified, CMS/Cephalon agents paused.
