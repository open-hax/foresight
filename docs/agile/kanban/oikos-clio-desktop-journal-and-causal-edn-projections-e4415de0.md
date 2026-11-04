---
category: "kanban"
labels: "oikos, planning, local-first, epic"
dependency: ["722906b7-77d7-4414-a976-23996f7533de"]
type: "epic"
write-id: "1793728991623-0.zz9ysskz5qnmlw2k4mo"
title: "Oikos: Clio desktop journal and causal EDN projections"
priority: "P1"
status: "incoming"
uuid: "3b9279b5-aafd-431a-90a9-128ce4415de0"
created_at: "2026-11-03T15:11:06.490Z"
---

# Oikos: Clio desktop journal and causal EDN projections

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Replay a causally honest desktop projection from admitted immutable observations, intentions and outcomes.

## Scope

Reuse Clio schema, admission, locking and canonical causal streams. i3 subscriptions are notifications, not complete tree deltas: subscribe before snapshot, reconcile races and resync on disconnect with explicit observed revisions. Window lifetime identity is server epoch plus XID plus open/incarnation identity (creation revision), never epoch plus XID alone. Reconnect uncertainty invalidates lifetime continuity and action targeting until freshly established. Oikos owns these portable product laws; Clio remains the shared event kernel. Pure CLJC replay separates observation from intent/outcome. Undo is compensating action only.

## Non-goals

No parallel journal kernel, invented complete deltas, guaranteed reopening of closed applications, or live collector activation.

## Acceptance criteria

- [ ] Clio admits distinct observation/intention/outcome schemas with causal references and immutable revision evidence.
- [ ] S2A defines, independently reviews and hands off the initial versioned closed safe-metadata allowlist/deletable content-reference contract within its 5-point scope, before its own immutable event/diagnostic admission and S5A consumption. Bind exact artifact version/content digest, source revision and review evidence; no future S5B artifact gates either consumer. Never admit raw images/OCR, secret-bearing titles/paths/labels or echoed rejections; hashes/IDs need sensitivity classification. Synthetic marker-denial and post-content-erase causal replay tests are required through the same Clio boundary, not a second privacy engine.
- [ ] Subscribe-before-snapshot, concurrent events, disconnect/resync and epoch rollover cannot silently produce current-state claims.
- [ ] Deterministic replay rejects stale identities; compensation records attempted and observed outcomes without claiming resurrection.

## Verification

CLJC replay/property tests and a fake i3 IPC race harness cover same-epoch close/reuse with different incarnations: replay and memberships retain the old lifetime without attaching it to the new window, and even a fresh revision cannot authorize an old-lifetime target. Interrupted snapshots, duplicate delivery, uncertain reconnect and failed compensation must fail closed without invented continuity. These are future acceptance checks, not executed implementation evidence.

## Risks

Snapshot/event races and reused identities can turn a historical observation into a destructive command target.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-workspace-go.md
- /home/err/docs/notes/design/epic-i3-mcp.md

## Dependencies

- 722906b7-77d7-4414-a976-23996f7533de (E1)

---
Native findings 4456846184/4456856398: E2 root lifetime contract now requires epoch, XID and open/incarnation identity, with same-epoch close/reuse replay/membership/targeting fixtures and fail-closed uncertain reconnect. Historical creation/event bytes are intentionally retained; this comment corrects rather than rewrites them. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Consensus planning correction v1, s5b/safe-ledger parent consistency: E2 admits only reviewed closed safe immutable metadata plus deletable content references through Clio. Marker denial and causal replay after erasure are future checks; raw observations/secret metadata may not be put in immutable history. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

Safe-reference order correction for native F153 findings Codex4458493223 and CR4458522612: current root acceptance assigns initial closed/versioned portable .cljc metadata/content-reference definition, independent review and exact artifact handoff to S2A within its existing 5 points. S2A gates its own Clio admission; S5A explicitly depends on S2A plus existing S2B/S7A and consumes the exact reviewed version/digest/source/review evidence without S5B output. S5B follows S5A, consumes/reviews the same baseline and may only compatibly extend it prospectively under separately reviewed versions, never rewrite immutable history or retroactively qualify admission. E2/E5 acceptance reflects this order. Privacy exclusions, raw 24h/7d retention, deletion propagation, accepted-memory consent and Clio authority remain unchanged. Historical comments assigning initial ownership to S5B are superseded, not edited. This is future planning/verification only: no implementation, review completion, native settlement, status transition or execution permission is claimed.

---