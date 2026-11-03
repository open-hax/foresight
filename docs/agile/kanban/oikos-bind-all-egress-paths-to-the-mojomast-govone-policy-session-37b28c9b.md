---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["3b9279b5-aafd-431a-90a9-128ce4415de0", "29caa8e3-289a-416c-9850-8ef9cc769e88"]
parent: "e616187d-ba13-42b1-addd-aa4d6244a5be"
type: "task"
write-id: "1793718989582-0.g4sp4rgvg3vo7dps1z"
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

Deny-network fixtures instrument every payload path and provider bypass; local_only produces zero network calls; image/text/tool policy failures reject before transport. These are future acceptance checks, not executed implementation evidence.

## Risks

A background exporter or derived label can bypass prompt-only policy; complete histories increase privacy impact.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- https://github.com/mojomast/govone
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-emacs-mcp.md
- https://github.com/mojomast/govone is operator-specified integration identity; API compatibility requires inspection during implementation.

## Dependencies

- 3b9279b5-aafd-431a-90a9-128ce4415de0 (E2)
- 456b2c69-cb5d-417d-9aeb-ac05e0a3251a (E4)

---
Planning correction supersedes the original whole-E4 prerequisite and the original local_only verification wording “zero network calls.”

Dependencies: E2 (3b9279b5-aafd-431a-90a9-128ce4415de0) plus shared local capability S4A (29caa8e3-289a-416c-9850-8ef9cc769e88); the entire E4/client-matrix epic is not an execution prerequisite. GovOne admission must precede S4B qualification with real remote tool results. Only synthetic tests with no private data may precede that admission.

Corrected acceptance criteria: local_only permits zero unauthorized cloud/third-party egress. Explicitly authenticated, allowlisted loopback/local-service transports may be used; a private IP address alone is not trusted and grants no authorization. Existing GovOne policy-session/scopes remain authoritative. Denied or unavailable routes fail closed, without direct-provider retry or fallback.

Corrected verification: separately test an explicitly authenticated allowlisted local request that is permitted, an external destination denied in local_only, and unauthenticated/unallowlisted local or private-IP destinations denied. Cover expired scope, gateway outage and attempted fallback; no denied route is retried directly. This is an incoming planning correction, not completed verification or authorization for a live request.

---