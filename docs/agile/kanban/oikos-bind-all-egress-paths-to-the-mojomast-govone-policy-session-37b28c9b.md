---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["93a76dad-678a-4980-86fb-ba0fbb5e8203", "29caa8e3-289a-416c-9850-8ef9cc769e88"]
parent: "e616187d-ba13-42b1-addd-aa4d6244a5be"
type: "task"
write-id: "1793724131966-0.oooux55wk8bot7e1s6q"
points: "8"
title: "Oikos: Bind all egress paths to the mojomast GovOne policy session"
priority: "P1"
status: "incoming"
uuid: "c703893f-aea4-41c2-9eab-c3bd37b28c9b"
created_at: "2026-11-03T15:11:11.241Z"
---

# Oikos: Bind all egress paths to the mojomast GovOne policy session

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: GovOne egress admission and Knoxx Lens evidence.

## Outcome

Bind all egress paths to the mojomast GovOne policy session.

## Scope

Reuse mojomast/govone gateway APIs/contracts for local_only/hybrid_redacted/external_approved across text, images, derived labels and tools.

## Non-goals

No alternate policy evaluator, direct provider retry, implicit approval from a free route, raw data in Lens evidence, or live cloud calls.

## Acceptance criteria

- [ ] No egress occurs without the scoped GovOne policy session; expiry, revocation, gateway outage and attempted direct retry fail closed.
- [ ] Images are redacted under supported contract or denied; unsupported redaction never becomes text-only privacy approval; no second policy engine.

## Verification

Deny-network fixtures instrument every payload path and provider bypass; local_only produces zero unauthorized external/cloud/third-party egress, permits only explicitly authenticated allowlisted local-service/loopback requests and denies untrusted local/private-IP destinations. Image/text/tool policy failures reject before transport without direct retry. These are future acceptance checks, not executed implementation evidence.

## Risks

A background exporter or derived label can bypass prompt-only policy; complete histories increase privacy impact.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- https://github.com/mojomast/govone
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-emacs-mcp.md
- https://github.com/mojomast/govone is operator-specified integration identity; API compatibility requires inspection during implementation.

## Dependencies

- 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A): immutable event/payload provenance.
- 29caa8e3-289a-416c-9850-8ef9cc769e88 (S4A): shared scoped local capability contract, not the whole E4 client matrix.

---
Planning correction supersedes the original whole-E4 prerequisite and the original local_only verification wording “zero network calls.”

Dependencies: E2 (3b9279b5-aafd-431a-90a9-128ce4415de0) plus shared local capability S4A (29caa8e3-289a-416c-9850-8ef9cc769e88); the entire E4/client-matrix epic is not an execution prerequisite. GovOne admission must precede S4B qualification with real remote tool results. Only synthetic tests with no private data may precede that admission.

Corrected acceptance criteria: local_only permits zero unauthorized cloud/third-party egress. Explicitly authenticated, allowlisted loopback/local-service transports may be used; a private IP address alone is not trusted and grants no authorization. Existing GovOne policy-session/scopes remain authoritative. Denied or unavailable routes fail closed, without direct-provider retry or fallback.

Corrected verification: separately test an explicitly authenticated allowlisted local request that is permitted, an external destination denied in local_only, and unauthenticated/unallowlisted local or private-IP destinations denied. Cover expired scope, gateway outage and attempted fallback; no denied route is retried directly. This is an incoming planning correction, not completed verification or authorization for a live request.

Native findings 4456856407/4456856471/4456856521: root local_only wording now matches authenticated allowlisted local transport with zero unauthorized external/cloud/third-party egress. Private IP alone grants no trust; unauthenticated/unallowlisted local routes deny. S2A supplies immutable payload provenance and S4A named scoped capabilities; no whole client-matrix prerequisite. Existing historical E2/E4 comments are superseded only for current child execution dependencies by the precise S2A+S4A edge. Current native child prerequisites synchronized to the user-approved precise plan: 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A), 29caa8e3-289a-416c-9850-8ef9cc769e88 (S4A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

---