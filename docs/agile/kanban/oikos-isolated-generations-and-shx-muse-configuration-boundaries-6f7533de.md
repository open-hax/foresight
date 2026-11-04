---
category: "kanban"
labels: "oikos, planning, local-first, epic"
dependency: []
type: "epic"
write-id: "1793726848123-0.wz2klybbihphgmjn3mx"
title: "Oikos: isolated generations and shx/Muse configuration boundaries"
priority: "P1"
status: "incoming"
uuid: "722906b7-77d7-4414-a976-23996f7533de"
created_at: "2026-11-03T15:08:06.164Z"
---

# Oikos: isolated generations and shx/Muse configuration boundaries

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Develop and install independently identifiable Oikos generations without disturbing the working desktop.

## Scope

Services owns deployment. Separate development/installed binaries, build artifacts, state, sockets and ports. Reuse shx/envm shell/environment IR; non-shell targets use explicit target-specific render adapters. Muse owns tool compatibility only. Version preview/check/apply/rollback with secret environment references and local-change holds. Link, do not replace, the federated workstation program.

## Non-goals

No blanket config migration, generic supervisor replacement, secret material in manifests, or live deployment.

## Acceptance criteria

- [ ] A generation manifest identifies each owned binary/artifact/state/cache/configuration root/rendered destination/socket/port and rejects normalized/resolved aliases and ancestor overlaps before target I/O; S1A's synthetic positive/negative isolation fixtures cover every resource kind.
- [ ] Target adapters declare preview/check/apply/rollback and compatibility; rendered payloads contain only secret env references.
- [ ] Failure recovery and local-change holds are proven in an isolated fixture; federated-workstation-distribution remains separately owned.
- [ ] S1B records the absent generic Services local-generation API as a blocking owning handoff before apply/rollback implementation qualification. Services' documented DO production lane is not that API; shx/envm merge/render is not a generation promoter. Bind an actual reviewed Services contract/version/entry point and forbid a second Oikos supervisor. BitchTracker operations is a product precedent, not shared-kernel authority.

## Verification

Portable CLJC manifest laws plus adapter fixtures cover collision, missing secret reference, dirty target, partial apply and exact rollback; Services reviews deployment seam. These are future acceptance checks, not executed implementation evidence.

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
Native finding 4456856511: E1 acceptance binds S1B to an actual reviewed Services-owned local-generation handoff; the generic API is absent/unestablished and remains blocked. The DO lane and shx env merge/render do not prove generation promotion. No second Oikos supervisor. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Consensus planning correction v1, s1a/config-isolation parent consistency: E1 now includes configuration roots/render destinations/cache and normalized/resolved alias/ancestor rejection in the manifest isolation acceptance, with S1A synthetic fixtures. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

E1 scope clarification: S1A root is independently bounded to portable isolation/identity/contracts and positive inert profile EDN; absent safe bash adapter is explicitly unavailable, not a hidden predecessor or a mocked pass. Actual positive bash and effect-time write/race/rollback qualification remains S1B work gated by the reviewed shx adapter/validator and Services handoff artifacts. Existing installed/dev, no-supervisor, synthetic-only and no-live-promotion gates are unchanged. Native PR1 specification corrections do not admit or complete either child.

---