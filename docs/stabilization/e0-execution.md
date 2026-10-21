# E0 execution — Correct shared-ledger prerequisites

Existing [Clio #5](https://github.com/riatzukiza/clio/pull/5) now contains two
verified repairs, published as ordinary forward commits. Codex owns this bounded
repair/review loop under the active stabilization goal, recorded on existing E0.
No duplicate story or PR was created. E0 stays **breakdown**; E1–E7 stay **incoming**;
M0–M7 remain **unqualified**. CMS/Cephalon goal agents remain paused.

## Source and behavior

| Revision | Role |
| --- | --- |
| `e04da66c3aefe9f3b124e275d92a1771feb04bd8` | Original current PR head, retained as ancestor |
| `d83296ff2250a8d2faa7f9441681bdd6befeb993` | Red regression commit: NBB: 2 assertion failures; JVM: 119 tests / 530 assertions, 3 failures / 0 errors |
| `38c0b8c9e45160ae4fecd7d87bf6c700f62cc631` | Published green head |

Read-only directory replay now skips only unsupported directory-sync preflight.
The real force remains outside that exception handler: actual force failures
still propagate, including failures without a nested cause. This repairs a
verified portability defect without weakening write durability.

A callback that deliberately released every pathname now keeps its `claim-lost`
outcome when the locked temporary's link count reaches zero. Results retaining
an owned pathname still undergo link-count and inode/path ownership checks.
The regression uses a real locked inode, unlinks its temp, and verifies release.

## Verification and independent review

| Gate | Result |
| --- | --- |
| Babashka 1.13.219 | 29 tests / 182 assertions, zero failures/errors |
| JVM 21 | 119 / 530, zero failures/errors |
| NBB | 94 / 510, zero failures/errors |
| Compiled Shadow CLJS | 94 / 510, zero failures/errors/compiler warnings |
| Lint and extern boundary | zero warnings/errors |
| [Hosted Clio CI](https://github.com/riatzukiza/clio/actions/runs/39520773474) | success at exact published green head |

Local verification required matching the existing CI-pinned Babashka version:
installed 1.12.207 lacked `java.util.HexFormat`. The failed attempt is retained.
The isolated downloaded runtime was digest-verified; no package policy changed.
The earlier PR-flow `--slurp` blocker is also removed through isolated,
digest-verified GitHub CLI 2.102.1. Native macOS/Windows execution is not claimed.

Both corrected native findings have writer replies and resolved readbacks:
[lost claim](https://github.com/riatzukiza/clio/pull/5#discussion_r4331734104),
[read-only replay](https://github.com/riatzukiza/clio/pull/5#discussion_r4331733656).
[Fresh CodeRabbit review](https://github.com/riatzukiza/clio/pull/5#issuecomment-6304845341)
was requested once. Its actual quota response sets retry at **00:36:46 UTC on
October 21**; Codex's account-quota boundary is unknown. Neither supplies approval.

The [directory-ABA finding](https://github.com/riatzukiza/clio/pull/5#discussion_r4331187538)
remains open. Current canonical PR-flow reports one unresolved thread and no
eligible current-head approval. The fixes, source push and passing CI do not
qualify merge or adoption.

## Review-route and integration evidence

The MiMo workflow is absent from Clio #5's green head and fork main. Existing
[Clio #3](https://github.com/riatzukiza/clio/pull/3) proposes it, but its gate is
blocked. Its October 9 native failure was missing App credentials; a fresh
metadata-only read finds both named repository secrets created October 19.
Those names prove neither App installation nor a successful admitted review.
No secret values were read and no privileged workflow was rerun.

Fresh native compare results show old published Knoxx #331/#333/#335/#375/#376 heads
all diverge from current main `3409977bca4ba35e09967f9a99d50867a679a73c`.
The earlier path ledger remains valid at its named heads. No accepted current-main
composition or blanket merge permission follows from the old sequence.

The [execution overlay](e0-execution.json) binds exact commits, test commands,
native thread/readback, CI, gate/quota and route evidence. Original census,
snapshots, independent child checkouts, unrelated dirt and all 23 root gitlinks
are preserved. This batch performs no merge, closure, deployment or agent restart.

## Next source action

Reproduce and repair the remaining directory-ABA window on existing Clio #5 with
parent-descriptor binding and adversarial native-host tests, retaining read-only
portability and current failure contracts.
