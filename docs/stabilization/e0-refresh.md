# E0 — Persisted live refresh and source-custody recovery

This execution continues existing `stabilization-e0`; it creates no work items.
E0 remains **breakdown**, E1–E7 remain **incoming**, and M0–M7 remain **unqualified**.
CMS and Cephalon goal agents remain paused; no work was dispatched to them.

[Immutable native evidence](../../.ημ/stabilization/snapshots/6e886224254bf9ca7d36c6f0ffc058428250e91fdc2acd0290a0cfecfe5dabe6.json.gz)
binds the raw API responses, queries, exact source bytes, trees and consumer pins.

## Frozen cohort and fresh source observations

The original 442-row census and all prior snapshots remain byte-for-byte intact.
Fresh authenticated reads retrieved all **442 frozen identities: 441 open, one
merged, zero closed-unmerged**. This is a frozen-cohort refresh, not a new
repository-membership or newly opened PR census. The recovered prior thread
reports 815 inspected repositories and no new cohort items; its raw native
payloads were not recovered and are not relabeled as this execution's proof.

**Five open PRs** changed head/base. The merged PR's head also changed: **six
changed identities in total**. This matches the prior report's five *other* PRs,
rather than establishing an additional change between sweeps.

| PR | Native state | Current head | Current base | Unresolved threads |
| --- | --- | --- | --- | ---: |
| [riatzukiza/.agents#34](https://github.com/riatzukiza/.agents/pull/34) | OPEN | `e6b0796270066f7e649d5763d0d414b34d75464a` | `8497afdd329c3dcaefb78eec36d8e629a59d9eb2` | 1 |
| [riatzukiza/axxium#10](https://github.com/riatzukiza/axxium/pull/10) | OPEN | `562eefe1c4d57fd18daa2602b1a7cc7ac867adce` | `6fdce062e54b93c92343ebfc8b3bc5d9fc7c3a4c` | 3 |
| [riatzukiza/clio#5](https://github.com/riatzukiza/clio/pull/5) | OPEN | `e04da66c3aefe9f3b124e275d92a1771feb04bd8` | `7262ddaaf39b32deb1794c5c6595724b6d69538e` | 3 |
| [riatzukiza/foresight#34](https://github.com/riatzukiza/foresight/pull/34) | OPEN | `1159a9dad7356115e01de67039fbe221b200223a` | `8e3ddd4f362d7f9612556b5185b6be9af646eac8` | 1 |
| [riatzukiza/session-mycology#1](https://github.com/riatzukiza/session-mycology/pull/1) | MERGED | `881edfe00cbba71bd50307ff0d52f28a152a7e70` | `5576255623fbaa84aa32ef494f13e347c781e5e4` | 0 |
| [riatzukiza/sol#6](https://github.com/riatzukiza/sol/pull/6) | OPEN | `0ddc5e6a1b218c1b4333c4c4eeb71d31c5af4b3d` | `a93ab2a4abc8b578fc2bfd20b1088e7e0e05d749` | 0 |

Full frozen/current identities and dispositions: [442-row refresh](e0-refresh.csv)
and [machine-readable overlay](e0-refresh.json). Fresh review submissions,
issue/inline comments, checks, statuses, files and thread metadata were collected
with terminal-page/cursor checks for these six PRs and Knoxx #331. All seven
head/base pairs were stable through collection. Other review records retain
their historical freeze scope. Collection is not a review or merge-gate verdict.

## Observed merge and preserved ancestry

[Session Mycology #1](https://github.com/riatzukiza/session-mycology/pull/1)
merged at `ddb63291cf856ab59ef14f02b8346e42f6ef651b` from head
`881edfe00cbba71bd50307ff0d52f28a152a7e70`. Native compare places its frozen head
`4cba181e9a51da3c2cfaf871b3681a74d709058a` on that merge's ancestry (ahead 2,
behind 0). This proves retained Git lineage; full semantic preservation,
review-policy qualification and downstream integration are still unassessed.
No root gitlink or consumer pin changed. The snapshot retains all 23 root index
pins and source hashes without entering child working trees.

## E0 custody hold reduced

Exact-ref base64 retrieval from both upstream and personal repositories now
reproduces all four previously quarantined observations, covering two unique blobs:

| Source path | Reproduced Git blob | Bytes |
| --- | --- | ---: |
| `kanban/tasks/terminal-ui-differential-render-host.md` | `db577393bda999e8309c1e6c51e9de2e6aa402f6` | 6,802 |
| `kanban/tasks/knoxx-publication-resource-contracts.md` | `20316282a233b1134a91bc326d2908fa37a57415` | 13,244 |

The exact bytes are retained under `.ημ/stabilization/recovered/`, with all four
repository/revision/path/hash bindings in the overlay and native snapshot.
Comparison locates an ESC versus literal `^[` difference in the terminal card,
and literal `u0000` versus `^@` segments in the publication card. That explains
the hash differences at the byte level; the original capture transformation's
cause remains unproven. Original mismatching observations stay preserved.
The exact-source-recovery hold is satisfied for these four observations only;
this does not adopt their historical statuses, prove behavior or admit child work.

## Knoxx semantic boundary verified against published source

Native PR metadata confirms #327, #329 and **#331 are merged**. #331 merged at
`0fdaae13f421ff9623b1bb6fe1afb7694ab68ca1` from
`d835a0224ece799462ec9b3f8f60baacd9abade2`. Issue #323 still describes #331 as
the sole ready target at an older head. Its operational sequencing and earlier
authorization prose remain historical source claims.

Complete, untruncated Git trees were captured for published heads #331, #333,
#335, #375 and #376. Every one of the **32 mailbox-delta paths** is identical
between #333 and #335, and differs from #331. #333 is also an ancestor of #335.
At those same paths #375 and #376 exactly retain #331. This verifies the concrete
published boundary problem behind the index's proposed identity-before-mailbox
order: retargeting #335 alone cannot perform the documented 32-path restoration.
The snapshot retains every path/blob and the complete trees. It does not inspect
or qualify the index's private integration candidates.

Existing [#324](https://github.com/open-hax/knoxx/issues/324) and
[#325](https://github.com/open-hax/knoxx/issues/325) remain open. Existing
[#372](https://github.com/open-hax/knoxx/issues/372) explicitly retains Wiki
HTTP/tools/SSE movements after #343 and before #349/#351, with startup and
tenant/stream proofs. Existing #377 retains pagination before HTTP consumer #339.
No replacement task, accepted merge order or live deployment is inferred.

## Checks and stop conditions

Current collection retains Axxium #10's failed `review-resolution-gate` and its
three unresolved conversations. The five changed open PRs retain 8 unresolved
threads in total. Knoxx #331 has zero unresolved threads, but its captured checks
include staging-build failure and skipped deployment; merged does not mean
deployment-qualified. Review records/checks and actor IDs are retained without
manufacturing approvals, requiredness, completed rounds or exceptions.

Canonical PR-flow status was attempted from recovered canonical revision
`580a361ee0110319cc4f3320d9982961073b66a2`; installed `gh` rejects its `--slurp`
flag. That failed attempt remains evidence. Fresh native collection used explicit
REST pages/GraphQL cursors instead; no PR-flow pass is claimed.

Root validation passes (25 sources / 23 submodules / 39 invariants); workspace
tests pass (24 tests / 120 assertions), project tests pass (12 / 166), lint has
zero errors/warnings and Rheos reports no drift. Negative workspace diagnostics
are fixtures. Original frozen graph/CSV/unresolved and compressed/uncompressed
native-corpus digests reproduce. Existing dirt and ledger prefixes are preserved.

M0 still needs accepted stack owners/terminal intent, semantic coverage, lawful
child custody/repair and qualified Knoxx recomposition. Codex owns reconciliation
on existing E0; no execution/merge authority is assigned by this checkpoint.

## Next evidence action

Verify the documented #335 mailbox-path restoration at its actual published
replacement head against the retained 32-path ledger before accepting the
identity-before-mailbox execution order.
