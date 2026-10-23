# E0 execution — Separate anonymous evidence from publication custody

Existing eta-mu producer `cf32641a-b336-407e-b68b-c75ec7d20dbb` now has a concrete
repair in [upstream fork PR361](https://github.com/open-hax/eta-mu/pull/361), head
`9c05ba9d7de528529d08b901a1f3a6635fb34f7d`, base
`7112c36440d180ca183d6a3f89661cee161fdae5`. It is open, auto-merge is absent,
and review/integration qualification remains blocked. This remediation PR is
tracked separately from the frozen 442 identities; planning PR22 is unchanged.

The ordinary producer removes App/Discord secret inputs, private-dependency
credential/mirror setup, the pre-inference credential guard, token minting and
review/Discord publication. Exact clean checkout/completion, full input and
invocation/discovery verification, finite recovery and artifacts remain. The
terminal result is “OpenCode producer evidence” and asserts no publication or
trusted App status. Existing pure validators are reused without executing their
publication adapter. Missing private dependencies remain actual failures.

Composition `96862eef543ace2bbd788221ae05ee19121312db` retains accepted main's
69 commits beyond the old planning baseline, reviewed PR22 card bytes, both Git
parents and each unique ledger suffix exactly once. Rebuilt current-source
Rheos records the existing producer's lawful breakdown→ready→todo→in_progress→
testing hops. No new task or board/publisher engine is introduced. Publisher
`ed4e2a2a-6055-4d53-9cea-f3971c707551` and hosted proof
`164c2de7-7495-4738-94e1-a81167edefb0` retain their separate obligations.

RED `83dd52478fd6928027aa66bfa665f52b3953e671` executes the actual old App guard
without credentials and fails at that guard. GREEN passes all 338 registered
review-workflow tests, with zero failures, skips or emitted warnings. Five
deleted Discord adapter cases plus three real custody cases account for
340→338; preserved/renamed controls are mapped. Actual extracted deterministic,
review, final-validation, completion and terminal steps use inert external
boundaries. Synthetic unauthorized-write and credential-output variants fail at
their intended fake-transport/stdout boundaries. The existing
`TRANSPORT_MOCK_ONLY` verifier supplies no independent canonical Muse-law or
real provider qualification. Runtime supervisor, embedded runner and separate
recovery/invocation test owners remain exact accepted-source bytes.

Unfiltered owning actionlint with ShellCheck, Node syntax and whitespace checks
pass. A private v1.7.12 checker contains only upstream deadlock repair
`fd33e9f582a01a02885c93ae3775e1190cf63653`, with exact official postimages and
actual process tests. No checker suppression or global tool replacement occurs.
An initial missing unbuilt Rheos-bin install warning and a fixture assertion
error are retained; the built source CLI and subsequent relink/checks are clean.

Native PR361 observations distinguish scope: the ordinary producer jobs are
skipped for fork enrollment, while its terminal job succeeds on the unsupported
event path. That is **not** a hosted anonymous invocation. Several independent
CI channels pass and others remain pending. The successful `main-tests` job
actually ran two contract-guard cases, not the 338-test owner; its raw log has
Actions/input/runtime warnings. It executed merge candidate
`8f4256e8f48c3a5a951f73441de60ce45754b6f0` with native current B/H parents.
No whole-run warning-free or full hosted-owner qualification is inferred.

The [immutable native/local attempt](../../.ημ/stabilization/snapshots/92897a503c8378a5ce0a98e56c38628309df68ef113f1feee832956dd62ac185.json.gz)
retains source correspondence, original failures, actual card operations,
local proof, native root CI/review, current producer CI and selection custody.
The owned eta worktree was clean and pushed before removal; primary child
checkout and all 23 root pins remain unchanged.

Root067's live deterministic/context jobs passed, but the model job failed at
the missing-App guard and the required terminal review gate failed. Native main
protection still requires that gate and CodeRabbit. **Root merges remain frozen
until the isolated route and required gate qualify.** No protection is relaxed
and no signing credential is restored. Superseded credential-bearing run
`39522223787` is confirmed completed/cancelled.

[Review dispositions](e0-review-findings.md) and
[selection source amendments](e5-source-amendments.md) preserve original sources
while correcting custody. The concrete character oracle stays open. E0 remains
breakdown; root E1–E7 incoming; M0–M7 unqualified; B1–B3 unassessed;
CMS/Cephalon agents paused.
