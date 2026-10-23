# E0 — Ownership, existing-work mapping and dependency decisions

Decision date: October 20, 2026. Existing canonical card: [stabilization-e0](../agile/kanban/stabilization-e0-reconstruct-the-authoritative-backlog-ation-e0.md). State: **breakdown**. **M0 remains unqualified.**

This is an additive decision record over the [frozen census](e0-census.md). The original cohort, PR register, work graph, unresolved observations and native archive are unchanged. No source was fetched again and no work item was created.

## Ownership and scope

Rheos now assigns **Codex** to the existing E0 card. Codex owns reconciliation, identity adjudication, dependency evidence and follow-through for every census row and residual hold. This assigns coordination accountability; it does not assign GitHub assignees, grant merge authority, admit child cards or replace their operational histories.

The [442-row owner/work index](e0-owner-work-index.csv) preserves each frozen head and native assignee. Every row has an existing work anchor and an explicit scope decision. The 309 constellation candidate rows remain in reconciliation scope; the other 133 stay in the complete inventory without becoming new Foresight execution tasks. Actual stack execution/merge owners and terminal PR intent still need accepted evidence. No merge stack is activated.

## Existing work, without copies

| Decision | PR count | Meaning |
| --- | ---: | --- |
| Retain original explicit card associations | 192 | Source associations remain candidates; acceptance is not inferred. |
| Add explicit existing-work bindings for previously unmatched PRs | 34 | Exact authored cards or explicit native task-index/tracking declarations. |
| Retain native PR as existing work anchor | 216 | No verified card equivalence; 84 are constellation candidates. Keep an owned coverage question on E0 rather than inventing a story. |

The first survey missed some `docs/agile/tasks` inputs. Complete added-file patches in the frozen archive recover **23 existing authored card observations across 12 PRs**. Each reconstructed UTF-8 byte sequence reproduces the native Git blob SHA1. These are source inputs, not replayable admission events. Their 28 declared relationships all locate existing identities. Across recovered cards and explicit issue declarations, 41 PRs have a new source binding. Seven already had original card associations; the other 34 were previously unmatched.

Knoxx’s frozen issue #323 supplies exact task/PR pairs. Anchored `Tracks`, `Closes` and `Fixes` declarations provide additional issue bindings. Mere title resemblance or incidental issue mentions do not qualify a binding. Repeated evidence for the same anchor is retained as corroboration, not another work item.

### Newly bound, formerly unmatched PRs

| Frozen PR | Existing work anchor(s) |
| --- | --- |
| [open-hax/knoxx#305](https://github.com/open-hax/knoxx/pull/305) | [open-hax/knoxx#issue:323](https://github.com/open-hax/knoxx/issues/323) |
| [open-hax/knoxx#333](https://github.com/open-hax/knoxx/pull/333) | [open-hax/knoxx#issue:332](https://github.com/open-hax/knoxx/issues/332) |
| [open-hax/knoxx#335](https://github.com/open-hax/knoxx/pull/335) | [open-hax/knoxx#issue:334](https://github.com/open-hax/knoxx/issues/334) |
| [open-hax/knoxx#337](https://github.com/open-hax/knoxx/pull/337) | [open-hax/knoxx#issue:336](https://github.com/open-hax/knoxx/issues/336) |
| [open-hax/knoxx#339](https://github.com/open-hax/knoxx/pull/339) | [open-hax/knoxx#issue:338](https://github.com/open-hax/knoxx/issues/338) |
| [open-hax/knoxx#341](https://github.com/open-hax/knoxx/pull/341) | [open-hax/knoxx#issue:340](https://github.com/open-hax/knoxx/issues/340) |
| [open-hax/knoxx#343](https://github.com/open-hax/knoxx/pull/343) | [open-hax/knoxx#issue:342](https://github.com/open-hax/knoxx/issues/342) |
| [open-hax/knoxx#345](https://github.com/open-hax/knoxx/pull/345) | [open-hax/knoxx#issue:344](https://github.com/open-hax/knoxx/issues/344) |
| [open-hax/knoxx#347](https://github.com/open-hax/knoxx/pull/347) | [open-hax/knoxx#issue:346](https://github.com/open-hax/knoxx/issues/346) |
| [open-hax/knoxx#349](https://github.com/open-hax/knoxx/pull/349) | [open-hax/knoxx#issue:348](https://github.com/open-hax/knoxx/issues/348) |
| [open-hax/knoxx#351](https://github.com/open-hax/knoxx/pull/351) | [open-hax/knoxx#issue:350](https://github.com/open-hax/knoxx/issues/350) |
| [open-hax/knoxx#355](https://github.com/open-hax/knoxx/pull/355) | [open-hax/knoxx#issue:354](https://github.com/open-hax/knoxx/issues/354) |
| [open-hax/knoxx#357](https://github.com/open-hax/knoxx/pull/357) | [open-hax/knoxx#issue:356](https://github.com/open-hax/knoxx/issues/356) |
| [open-hax/knoxx#359](https://github.com/open-hax/knoxx/pull/359) | [open-hax/knoxx#issue:358](https://github.com/open-hax/knoxx/issues/358) |
| [open-hax/knoxx#361](https://github.com/open-hax/knoxx/pull/361) | [open-hax/knoxx#issue:360](https://github.com/open-hax/knoxx/issues/360) |
| [open-hax/knoxx#363](https://github.com/open-hax/knoxx/pull/363) | [open-hax/knoxx#issue:362](https://github.com/open-hax/knoxx/issues/362) |
| [open-hax/knoxx#365](https://github.com/open-hax/knoxx/pull/365) | [open-hax/knoxx#issue:364](https://github.com/open-hax/knoxx/issues/364) |
| [open-hax/knoxx#367](https://github.com/open-hax/knoxx/pull/367) | [open-hax/knoxx#issue:366](https://github.com/open-hax/knoxx/issues/366) |
| [open-hax/knoxx#369](https://github.com/open-hax/knoxx/pull/369) | [open-hax/knoxx#issue:368](https://github.com/open-hax/knoxx/issues/368) |
| [open-hax/knoxx#371](https://github.com/open-hax/knoxx/pull/371) | [open-hax/knoxx#issue:370](https://github.com/open-hax/knoxx/issues/370) |
| [open-hax/knoxx#375](https://github.com/open-hax/knoxx/pull/375) | [open-hax/knoxx#issue:373](https://github.com/open-hax/knoxx/issues/373) |
| [open-hax/knoxx#376](https://github.com/open-hax/knoxx/pull/376) | [open-hax/knoxx#issue:374](https://github.com/open-hax/knoxx/issues/374) |
| [open-hax/proxx#330](https://github.com/open-hax/proxx/pull/330) | [open-hax/proxx#issue:14](https://github.com/open-hax/proxx/issues/14) |
| [open-hax/proxx#453](https://github.com/open-hax/proxx/pull/453) | [open-hax/proxx#issue:31](https://github.com/open-hax/proxx/issues/31) |
| [riatzukiza/bitch-tracker#1](https://github.com/riatzukiza/bitch-tracker/pull/1) | `acfa4cdf-a7b4-43d2-a165-eeffff40ce3d` |
| [riatzukiza/bitch-tracker#2](https://github.com/riatzukiza/bitch-tracker/pull/2) | `6fe3cf31-866d-4d6b-9c90-0645993cf27c`; `522a0b65-0fcb-43d7-a338-614af8cf3856`; `3ec6477d-331f-47bc-8c1e-e9fa4ed2396f`; `ef1097f4-cb92-4318-b7c9-a60d49624272` |
| [riatzukiza/bitch-tracker#3](https://github.com/riatzukiza/bitch-tracker/pull/3) | `e164efce-36e6-4cbe-9407-ddcb3d7d7509`; `36b20257-9d07-45a7-9246-867706f810cc`; `57b9ffc5-75e7-4098-9af8-efc001f60eb0` |
| [riatzukiza/chat-ui#1](https://github.com/riatzukiza/chat-ui/pull/1) | `da5cc8a0-39eb-4a2f-a6e8-75ff644a8b63` |
| [riatzukiza/clio#1](https://github.com/riatzukiza/clio/pull/1) | `07600a92-821a-4b35-89cd-d38393b3c393` |
| [riatzukiza/katamorph#1](https://github.com/riatzukiza/katamorph/pull/1) | `b2e7a944-8ab6-4e34-ada2-102763af58fc` |
| [riatzukiza/katamorph#2](https://github.com/riatzukiza/katamorph/pull/2) | `ab741ef4-8310-4853-a59e-79dbc042c038` |
| [riatzukiza/katamorph#3](https://github.com/riatzukiza/katamorph/pull/3) | `e464b145-6205-4d3b-b7a6-a87d55a21a38`; `f3f17bc9-4e87-4af1-a7b0-c28c3531a3c0`; `cedf658e-166e-4f6b-a2fe-0b0913ba6a9b`; `f73648ad-abe9-47cb-bd97-7365669132da` |
| [riatzukiza/rheos#3](https://github.com/riatzukiza/rheos/pull/3) | `4a68e0a9-072f-49f0-9f60-dbcdf70d3d5a` |
| [riatzukiza/rheos#6](https://github.com/riatzukiza/rheos/pull/6) | `c79a8f4e-6702-4b6a-9b79-7d3e2fa381cb`; `2d7c391d-b01b-4386-93ec-51a558bfa155`; `845caf18-17b5-4179-9a8e-156b0e68b67b`; `63796a65-d27d-430a-a7f6-8f1df8a48744` |

Exact path, revision, blob and declaration evidence for every binding is in [the decision graph](e0-resolution.json). The full CSV includes the 192 retained association rows and all PR-backed rows.

## Relationship adjudication

All **1,170 frozen card relationship observations** are classified: 721 parent/epic containment observations and 449 declared blocker observations. Containment does not imply merge order. The 183 frozen PR ancestry observations remain source facts, without being promoted to semantic prerequisites. Three unique cross-network UUID candidates remain explicit authority holds.

The former 20 unresolved source observations reduce to 11 distinct source/relationship/token references. Every reference now has a concrete disposition:

| References | Disposition | Remaining operational obligation |
| --- | --- | --- |
| 7 Epiphany references | Located six existing legacy `id` targets, using exact declared ID/path or a unique complete title slug within the same frozen source context. | Preserve legacy IDs. Prove the owning reader/custody and normalize through the child workflow before operational admission. |
| 1 Devel filename alias | Resolved to existing UUID `kanban-specs-drafts-contract-enforced-agent-output-pipeline-2026-03-23-md`. | Parent is containment; child may normalize the alias through its workflow. |
| 1 Knoxx epic reference | Located declared UUID `knoxx-events-agent-runtime-separation`; malformed `labels` YAML quarantines the target. | Repair its owning source before admission. A one-line, exact-blob proposal is preserved below. |
| 2 empty dependency entries | Rejected as no edge in the decision graph; no missing task is invented. | Owning child should remove/correct the malformed entries while preserving history. |

The decision graph lists all original source observations, located target revisions and remaining obligations. None of these identity decisions claims acceptance of a runnable prerequisite. The existing source bytes are untouched.

### Knoxx source conflicts

The frozen descriptions contain **72 literal namespace-supplier declarations**, all with located supplier aliases. Combined with branch ancestry, they form two conflicting evidence components:

- `#363`, `#365`, `#367`.
- `#333`, `#335`, `#337`, `#339`, `#341`, `#343`, `#345`, `#347`, `#349`, `#351`, `#353`.

For example, mailbox PR #333 declares identity-authority from #335, while #335 has historical ancestry through #333. This proves that the branch chain cannot act as the semantic integration sequence. The content/translation/shell group similarly closes a mixed evidence cycle. These are evidence conflicts, not proof that the runtime itself is cyclic.

The existing split-task issues remain canonical. Existing [main reconciliation #324](https://github.com/open-hax/knoxx/issues/324) and [qualification #325](https://github.com/open-hax/knoxx/issues/325) retain integration/recomposition obligations; no replacement tasks were made. The old index’s checkpoint/authorization prose is a historical source claim, not current authorization. An absent old PR is not assumed merged.

The identical OpenPlanner #92/#94 source head is retained with both targets and independent review obligations. All 93 differing card-revision groups remain revision-specific source/state observations; byte differences alone do not establish duplicates, conflicts or accepted implementations.

## Source custody and concrete repair

Four captured card-text observations (two unique blobs, mirrored between upstream and personal repositories) do not reproduce their captured Git blob OIDs:

| Card path | Expected Git blob | Captured text hashes as Git blob |
| --- | --- | --- |
| `kanban/tasks/terminal-ui-differential-render-host.md` | `db577393bda999e8309c1e6c51e9de2e6aa402f6` | `a859b94838cf9afd9a51d9aca3f1f115b27e23c0` |
| `kanban/tasks/knoxx-publication-resource-contracts.md` | `20316282a233b1134a91bc326d2908fa37a57415` | `d477cce987635b656cfefab7c6fef1e75942b19d` |

The mismatch is reproducible from the immutable native archive. Its cause is unproven. These texts are excluded from new authority-bearing identity decisions; their frozen observations remain intact.

The [Knoxx YAML repair proposal](../../.ημ/stabilization/resolution-proposals/f38d314aef13280a2b6eff3f8bf3e4232de5d4faedeba5dd613db33b3595f8ef.patch) changes only malformed `labels: "["epics"]"` to `labels: ["epics"]`, against blob `7cb5aeb608bb6845a2790ddd99b57e8aa88784b1`. The corrected result parses and retains the existing UUID. The patch is **not applied**, admits no task and makes no state transition.

The [recovered-card source proof](../../.ημ/stabilization/snapshots/4f080d1ab8959daa744d5c6d0d396c0522c00f81713e076f0c5e6574408e6f76.json) retains the 23 exact source texts, extraction method and successful archive revalidation. All 17 located former-gap target-source observations reproduce their Git blob hashes.

## M0 holds, owned by Codex on E0

1. Accepted per-stack execution/merge owners and intended terminal dispositions are still unproven.
2. The 216 PR-backed rows (84 constellation candidates) do not establish implementation-card coverage. Existing associations and differing revisions require semantic acceptance before supersession.
3. Legacy ID custody, the malformed Knoxx target, empty declarations and three cross-network authority claims require lawful child evidence/repair.
4. Knoxx semantic ordering/recomposition must satisfy existing issues #324/#325; mixed ancestry cannot authorize a schedule.
5. Resolve the four captured-text hash mismatches against exact source bytes.

These are explicit owned gaps on the existing E0, not new execution stories or milestone passes. Qualification requires new exact-source evidence; the frozen census stays the historical baseline.

## Evidence

- [Decision graph](e0-resolution.json): every PR binding, owner contract, all relationship classifications, exact target sources, holds and repair proposal.
- [Owner/work index](e0-owner-work-index.csv): all 442 frozen PRs.
- [Original frozen graph](e0-work-graph.json), [original unresolved observations](e0-unresolved.json) and [census report](e0-census.md).
- [Original freeze manifest](../../.ημ/stabilization/snapshots/62874eae7ee052cce39d658402326449e617a49b0965abe6795a93a2ad6fe341.json).

## Verification

Native-archive revalidation passes for the 23 recovered card blobs, new issue bindings, all 442 head identities and 17 located target-source observations. Static checks pass for census immutability, owner/work row uniqueness, original ledger prefixes, all 85 root identities, relationship classifications, source hashes and local links. An independent reachability check reproduces the two cyclic components. The proposed YAML repair applies and parses against its frozen source only in a temporary directory.

`nbb scripts/project.clj validate` passes with 25 sources, 23 submodules and 39 invariants. `clj-kondo --lint scripts test` reports zero errors/warnings; Rheos reports no drift; whitespace checks pass. These validate the reconciliation record and root structure, not product behavior or merge qualification.

## Next

Verify the Knoxx semantic integration order and recomposition evidence against existing issues #324/#325 before proposing an active merge stack.
