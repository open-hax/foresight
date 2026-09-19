# E1.10 review-completion rollout

This is the initial discovery and conformance-design slice of
[Foresight #111](https://github.com/open-hax/foresight/issues/111), following
[PR #96](https://github.com/open-hax/foresight/pull/96). No target is accepted
by this inventory. E1.10 remains blocked on E1.04 and missing enforcement proof.

## Observed policy and execution surface

The [September 19 snapshot](observations-2026-09-19.json) records the exact
observed `main` commits, source API URLs, check IDs, producer App IDs, ruleset
details and unavailable reads. It is a dated observation, not live policy.
The seven extracted repositories' observed main commits equal their PR #96
pins. Axxium main is `2439d4d6b8e546cda276f09f5c96db59226ecad6`; PR #96
initially recorded reconciliation candidate `ee5284a00eb5c034cd6ef0db281726f00ce77cca`
at the snapshot's Foresight head. The later PR #96 correction selects the merged
Axxium baseline. Those revisions must not be treated as equivalent.

| Repository | Ruleset collection | Full classic protection | Checks/statuses at observed main | Rollout state |
|---|---|---|---|---|
| kanban-orchestrator | Empty | HTTP 403 | None observed | Blocked |
| clio | Empty | HTTP 403 | None observed | Blocked |
| chat-ui | Empty | HTTP 403 | None observed | Blocked |
| rheos | Empty | HTTP 403 | None observed | Blocked |
| session-mycology | Empty | HTTP 403 | None observed | Blocked |
| sol | Empty | HTTP 403 | None observed | Blocked |
| osmos | Empty | HTTP 403 | None observed | Blocked |
| axxium | Active ruleset 17172221 | HTTP 403 | `build`, `deployment-boundary`: success; no statuses | Blocked |

All eight branch summaries report `protected: true`. The seven empty ruleset
collections therefore do **not** establish absent protection. The connector
cannot read the full classic policy. No administrative write authority was
established or exercised.

No `.github/workflows/` files are present at the seven observed extraction
commits. Axxium main contains `auto-merge.yml`, `ci.yml`, `kanban-sync.yml` and
`review-resolution-gate.yml`. Its two observed check runs use GitHub Actions
App ID `15368`. This records the emitter, not authorization for any similarly
named check or a guarantee that a review-completion check always runs.

Axxium's readable ruleset requires conversation resolution, stale-review
dismissal, CodeQL/code-quality restrictions, deletion/force-push protection,
and merge commits. It has zero required approving reviews and a user bypass
actor. Preserve stronger existing requirements; zero human approvals alone is
not the identified defect. The snapshot contains the exact rule and bypass
details so rollout can be reviewed by an authorized policy owner.

Receipt River is deliberately listed as a scope exclusion: it was registered
later, while #111 specifies the original eight targets. Decide and record its
inclusion before claiming review enforcement across all nine registrations.

## Reuse and ownership

[Foresight #71](https://github.com/open-hax/foresight/issues/71) assigns reusable
review execution, validation, aggregation and publication to eta-mu; Foresight
owns caller integration and cross-repository conformance. Producer-authentication
work remains linked to [#57](https://github.com/open-hax/foresight/issues/57).
The exact-head synthesis law already has an owning issue:
[eta-mu #330](https://github.com/open-hax/eta-mu/issues/330). Adapt the examples
to that issue's input/output vocabulary, preserving pending, stale, blocked,
unavailable and partial outcomes separately from merge eligibility.

The diagnostic Foresight head calls eta-mu's
[`opencode-code-review.yml` at d3937a2](https://github.com/open-hax/eta-mu/blob/d3937a2f2fa6ecf74cd525c6a0daceb5380a0e1d/.github/workflows/opencode-code-review.yml).
It verifies the exact clean PR checkout, stages deterministic evidence and
trusted review machinery, publishes through a deterministic publisher, and
aggregates job outcomes. The
[`review-resolution-gate.yml` at that revision](https://github.com/open-hax/eta-mu/blob/d3937a2f2fa6ecf74cd525c6a0daceb5380a0e1d/.github/workflows/review-resolution-gate.yml)
separately invokes pinned legacy GitHub logic to find unresolved threads.
The legacy `findAllUnresolvedThreads`/`findTrackedUnresolvedThreads` functions
only filter conversations. Their result does not prove a reviewer started,
completed on the current head, or came from an authorized producer.

The [conformance examples](conformance-cases.edn) translate #111's requirements
into a positive input and independent negative mutations. They are proposed
fixture data, **not an implemented evaluator or executed acceptance suite**.
Bind them to the owning eta-mu contract, add failing tests there first, and put
portable decision logic in `.cljc` with GitHub effects in adapters. Do not copy
the decision law into eight workflows or promote these illustrative keys into
a competing public contract.

## Concrete blocker discovered while closing PR #96

[Run 35474049937](https://github.com/open-hax/foresight/actions/runs/35474049937)
on `c95c72170675b5a873af4410d3bca436a6478bd9` passed deterministic evidence
and Muse context preparation. The initial reviewer invocation failed with
`OpenCode's free tier can only be used from within OpenCode` and produced an
empty response. The review-attempt artifact is `10593452060`; its ZIP SHA-256
is `0bb5093035e5fa138cd772b33c051951619379552cd7c927f0ebfde415216b84`.
No completed automated review was published by that job.

Retrying failed jobs exposed a second failure: attempt 2 requested
`review-evidence-96-35474049937-2`, which did not exist when downloaded.
The retry repair already merged in
[eta-mu #304](https://github.com/open-hax/eta-mu/pull/304), closing
[#296](https://github.com/open-hax/eta-mu/issues/296). PR #96's correction adopts
accepted provider revision `e8eea02d31030215984375980b765803fc72d80d`, whose
downloads consume prerequisite job artifact-name outputs. This is a caller
repin, not a new implementation of that repair. Neither failure authorizes
bypassing the aggregate gate. A supported review provider/configuration and a
successful exact-head run are required.

The caller also pins official OpenCode `1.18.31`. Its direct free-model probe
returned `OK`; that probe only establishes model connectivity. The owning
workflow's 36 executable tests pass at the accepted provider revision. Hosted
review publication and policy acceptance remain separate requirements.

## Next implementation slice

1. Reverify E1.04's independent-history prerequisite and the declared target set.
2. Obtain full policy read-back through an authorized actor. Preserve HTTP 403
   as unavailable until actual evidence replaces it. Identify trusted review
   publishers and workflows; a display name and App ID alone are insufficient.
3. Implement the missing completion/producer/base/required-gate conformance in
   eta-mu, reusing the existing contracts, and execute every negative example
   plus the positive control in disposable branches.
4. Restore provider execution and validate the accepted artifact retry repair;
   retain run IDs, exact head/base, publication identity and artifact digests.
5. Propose one caller rollout at a time, preserving branch restrictions. Require
   an always-present trusted gate, policy read-back and failure-fixture evidence
   before marking that target complete. Policy edits require actual authority.
6. On official module acceptance, follow the
   [documentation procedure](../migrations/eta-mu-breakdown/acceptance.md#accepted-module-documentation-procedure):
   update exact pins, evidence, routing, commands and dependency dispositions
   together. Keep standalone acceptance pending until its own evidence exists.
