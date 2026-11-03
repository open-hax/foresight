---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: []
parent: "722906b7-77d7-4414-a976-23996f7533de"
type: "task"
write-id: "1793730955949-0.frh4yldxg2d68mcfo78"
points: "5"
title: "Oikos: Define isolated generation manifests and target render contracts"
priority: "P1"
status: "testing"
uuid: "07452a65-4a6a-4704-bf81-50408927dcc8"
created_at: "2026-11-03T15:11:05.825Z"
---

# Oikos: Define isolated generation manifests and target render contracts

## Context

Private open-hax/oikos at /home/err/spaces/foresight/oikos is a local-first shared workbench. The original incoming proposal authorized no implementation. Following independent planning review and the user's explicit direction to proceed without waiting for hosted review queues, Rheos admitted this task to in_progress. The reviewed specification at 95dfbdb is the implementation input, not implementation proof. Work remains synthetic and sandbox/dev-only; installed configuration, live deployment, model/service changes, personal capture, uploads and spending remain held. This focused slice belongs to Oikos: isolated generations and shx/Muse configuration boundaries.

## Outcome

Define isolated generation manifests and target render contracts.

## Scope

Implement pure CLJC generation/environment shapes and shx/envm boundary with target-specific non-shell render contracts.

## Non-goals

No blanket config migration, generic supervisor replacement, secret material in manifests, or live deployment.

## Acceptance criteria

- [ ] Manifest separates dev/installed binary, artifact, state, socket and port namespaces and rejects collisions.
- [ ] Rendered fixtures use env secret references only; unsupported target formats fail explicitly; Muse is only tool compatibility.

### S1A config isolation

- [ ] Explicit manifest bindings include configuration roots and every rendered destination as well as binary/artifact/state/cache/socket/port resources. Development and installed resources must be disjoint, including normalized aliases, resolved symlink aliases and ancestor/descendant overlaps; missing or unresolved ownership/destination bindings deny before target I/O. No implicit HOME/XDG default may select installed configuration.

Future verification: shared BB/NBB law fixtures with synthetic HOME/XDG accept distinct owned roots/destinations and reject equal paths, dot/parent normalization aliases, resolved symlink aliases and ancestor overlaps across every resource kind. Positive fixtures retain disjoint resolved destinations; negative fixtures assert zero target reads/writes and preserve installed sentinels. These are future acceptance checks, not implementation evidence.

## Verification

Table/property tests exercise path/port collisions, missing env refs and unsupported targets; fixture diff proves installed configuration untouched. These are future acceptance checks, not executed implementation evidence.

## Risks

Shared paths or a non-shell renderer masquerading as shell IR could silently change the installed workstation.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/spaces/foresight/docs/agile/kanban/federated-workstation-distribution.md
- /home/err/docs/notes/design/epics-and-tasks.md

## Dependencies

- Related existing program (not replaced; not a blocking implementation edge): federated-workstation-distribution.

---
Planning correction supplements and supersedes any single-runtime-only interpretation of shared-law verification. Acceptance requires the same shared CLJC law fixtures to pass in both Babashka (BB) and NBB, with fixture identity and each runtime's actual result retained. Explicit BB subprocess protocol tests must cover EDN framing, success and nonzero exits, malformed/truncated output, timeout and cancellation, including bounded termination/cleanup and truthful error propagation. No malformed output, timeout or cancelled child may be accepted as a successful rendered result. Cross-runtime law parity and subprocess protocol behavior are separate required checks; neither substitutes for the other. These are future acceptance/verification requirements, not tests run or completed implementation in this planning turn.

S1A remains prerequisite-free; its portable generation/target-render shapes and isolated fixture identity precede adapters. Existing BB/NBB law parity and BB subprocess protocol checks remain required, not executed. Current native child prerequisites synchronized to the user-approved precise plan: none (S1A is prerequisite-free). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Consensus planning correction v1: root incoming acceptance/verification now explicitly specifies s1a/config-isolation. See the corresponding named root subsections for the contract and concrete future positive/negative fixtures. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

S1A scope correction from native Oikos PR1 findings4458124527/4458124541: initial acceptance is positive inert :oikos/profile-edn rendering plus explicit :adapter-unavailable rejection for the declared but unavailable :shell/bash-env capability. No owner-reviewed safe bash adapter exists; S1A must not depend on unassigned positive bash work or count a mock as that adapter. Positive bash qualification is an upstream shx-owned adapter/validator artifact consumed by S1B, not an extra hidden predecessor of S1A. S1A resolved-isolation/race tests cover evidence collection, normalized/resolved checks and inert render results under replacement; absence of writes is not proof of effect-time safety. Actual serialized write-boundary stale-identity rejection, anchored no-follow operations and observed write/rollback postconditions belong to S1B through the reviewed owner seam. This corrects future acceptance only; specification/implementation planning admission remains held.

User direction on 2026-11-03 authorizes implementation admission using the completed independent adversarial planning reviews without waiting for hosted review cohorts. The parent-supplied reviewed 95dfbdb specification is planning input, not implementation evidence or fabricated native approval. This supersedes the earlier planning-admission hold for S1A only. Use the actual Rheos FSM and deterministic gates; preserve synthetic sandbox/dev-only, isolated agent-owned resources, no personal capture, no model/service changes, zero-spend and remote-publication holdbacks. No implementation has been performed by this admission operation.

S1A GREEN candidate at exact tested commit 7896ee8709a9461ee54e2d39739c5ee8ce1a3157. Parent independently reports running bb test: BB 15 tests/245 assertions, NBB 25 tests/314 assertions, eval 9 tests/39 assertions, zero failures/errors; bb lint: 0 errors/0 warnings. Shared fixture SHA256 f7eb14070acf82fab6d5295620e4c7b90f0c11b6d569d8b9a9984edfacdcb209 is identical across BB and NBB. API documentation: docs/s1a-api.md at that candidate. These are parent-observed results recorded by this board operator, not independently rerun here. Distinct-agent independent CODE review and protocol probes are ACTIVE/PENDING; no acceptance or completion is claimed. User direction removes hosted queue waiting, not deterministic verification or safety holdbacks. Move only S1A from in_progress to testing for candidate verification; do not mark review/document/done or admit any successor.

---