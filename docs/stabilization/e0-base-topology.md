# E0 execution — Live bases and native merge proposals

Two independent PR/ref acquisition passes cover the same **442 frozen identities**
through 144 live base refs. The observation window is
`2026-10-21T01:39:02.638630+00:00` to `2026-10-21T01:39:23.510947+00:00`.
All source tuples and proposed merges remain unchanged across those passes.
The current states remain **441 open, one merged, zero closed-unmerged**.
This is a frozen-cohort source refresh, not a new repository/new-PR census.

## Field distinctions

| Observation | Meaning in this audit |
| --- | --- |
| PR `baseRefOid` | Reported PR base observation, retained exactly |
| Repository `ref(refs/heads/<baseRefName>).target.oid` | Independently read live base tip B |
| PR `baseRef.target.oid` | Cross-check against that independent live ref |
| `potentialMergeCommit` and parents | Native proposed candidate, not an executed test |
| `tested_merge_candidate` | Null in every row; this audit executes no candidate |

**179 open PRs** have a reported base different from their live base tip;
180 including the terminal merged source. These are different fields observed
in one window, not 179 newly changed branches. Every PR base-ref target matches
the independently queried ref, and all 144 refs exist. The earlier five changed
open head/reported-base pairs remain five against the original freeze.
Since the prior global refresh, only Clio #5's head changed, through the
already-published verified repairs. Prior captures retain their original bytes.

For example, Clio #3 reports `e6f5cf400909ef3d75ad79496645d1d1a8074e88`, while
its live main is `7262ddaaf39b32deb1794c5c6595724b6d69538e`. Native candidate
`06009d5ee6158c1c3e4fac54e33bd00d733ff5b8` actually names the latter as parent.
A base-field equality check alone therefore cannot establish current pairing.

## Proposed merge consistency

| Open PR class | Count | Disposition |
| --- | --- | --- |
| Proposed parents match live B and H | 301 | Pairing observed; no executed-test or merge qualification |
| Proposed parents name another base | 5 | Hold on current candidate/test proof |
| No proposed merge; native CONFLICTING | 135 | Preserve open; reconcile unique source before disposition |

The five mismatches are reproduced by independent native Git commit reads:

- [octave-commons/Truth#8](https://github.com/octave-commons/Truth/pull/8): proposed `18754851d7522e8050e53157dd2f229ce82fa895` has base parent `0112e228e93abf346da31f15456290c1b4c9b7ae`; live base is `c7cc34a8248a615f39eeefd5ba8ac841b65e49fa`.
- [open-hax/knoxx#381](https://github.com/open-hax/knoxx/pull/381): proposed `fb4da19fd454cd4c5fba3d04d66e127b85b35f12` has base parent `4754c7c3cd3d98179c302278b5bacdb573a4ec60`; live base is `3409977bca4ba35e09967f9a99d50867a679a73c`.
- [riatzukiza/TANF-app#26](https://github.com/riatzukiza/TANF-app/pull/26): proposed `43ad796c54fc486a2c936c9bdd08c66ca7fc2c80` has base parent `4d408e22dc9a535eb7aeebd4cedba17d7778f625`; live base is `800375a66a8a151bc22678bf27529392a77f19b0`.
- [riatzukiza/clio#1](https://github.com/riatzukiza/clio/pull/1): proposed `5582439a52d8980dae9ca86562533ecf2afcd1e4` has base parent `e6f5cf400909ef3d75ad79496645d1d1a8074e88`; live base is `7262ddaaf39b32deb1794c5c6595724b6d69538e`.
- [riatzukiza/rheos#2](https://github.com/riatzukiza/rheos/pull/2): proposed `64a276ebe5944223c7a12c6099d123fad92eec38` has base parent `bc3ed4978e9b1c1e2c2a88c65de9c631b51ca309`; live base is `b85854514c613b13a3a9930795f5ae595ab30ac7`.

Those objects retain the current head as their other parent. Their different
base parents are real native object facts, not a missing-input fallback.
No matching proposal, mergeable flag or stable source tuple supplies an approval,
actual test result, accepted stack order or permission to discard unique work.

The [442-row register](e0-base-topology.csv) and [additive overlay](e0-base-topology.json)
retain all identities, original/prior/current fields, real refs, proposed parents,
stability and explicit qualification holds. Existing E0 owns reconciliation;
each PR/work anchor remains intact. No task, merge, closure, rewrite, pin change
or runtime restart was performed. All 23 consumer pins remain unchanged.
M0–M7 stay unqualified; CMS/Cephalon agents stay paused.

## Shared reviewer dependency

[Review-route source reconciliation](e0-review-route.md) locates existing
implementations, accepted newer verifier work and the already-reviewed producer /
publisher / hosted-proof cards. It records actual native gate results and their
limits. Enabling the old credential-bearing Clio caller would not establish the
current supplied isolation contract, so no privileged rerun was attempted.
