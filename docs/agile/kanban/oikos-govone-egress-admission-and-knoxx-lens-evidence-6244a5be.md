---
category: "kanban"
labels: "oikos, planning, local-first, epic"
dependency: ["3b9279b5-aafd-431a-90a9-128ce4415de0", "29caa8e3-289a-416c-9850-8ef9cc769e88"]
type: "epic"
write-id: "1793724132338-0.pml810swyog6y4sikli"
title: "Oikos: GovOne egress admission and Knoxx Lens evidence"
priority: "P1"
status: "incoming"
uuid: "e616187d-ba13-42b1-addd-aa4d6244a5be"
created_at: "2026-11-03T15:11:10.941Z"
---

# Oikos: GovOne egress admission and Knoxx Lens evidence

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Make the existing GovOne policy session the mandatory gate for every local-to-cloud path.

## Scope

Integrate mojomast/govone, not the ussyverse homonym; reuse gateway APIs/contracts, never a second policy engine. Only a GovOne policy session may egress local text, images, derived labels and tool payloads, not just prompts. Modes local_only/hybrid_redacted/external_approved. Fail closed with no direct retry; redact images or deny unsupported image cases. Enforce auth scopes/deletion; Lens policy evidence is distinct from raw data. Complete history uses nonstreaming gateway; cloud response remains untrusted. Free proxx routes do not waive privacy.

## Non-goals

No alternate policy evaluator, direct provider retry, implicit approval from a free route, raw data in Lens evidence, or live cloud calls.

## Acceptance criteria

- [ ] Every egress route requires an authorized GovOne session/mode and rejects missing/expired scope, unsupported redaction or gateway outage.
- [ ] Nonstreaming complete-history request/response handling validates untrusted output and keeps Lens audit evidence separate from raw payload retention.
- [ ] Deletion and scoped access are tested end to end; local_only produces zero unauthorized external/cloud/third-party egress. Explicitly authenticated, allowlisted loopback/local-service transport is permitted; private IP alone grants no trust. Unauthenticated/unallowlisted local traffic is denied.

## Verification

Gateway contract fixtures and deny-network harness cover text/image/derived/tool paths, outage/retry attempts, auth revocation, deletion and adversarial cloud response. Separately assert allowed authenticated/allowlisted local transport, denied external egress in local_only, and denied unauthenticated/unallowlisted local/private-IP destinations, without direct retry. These are future acceptance checks, not executed implementation evidence.

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
- 29caa8e3-289a-416c-9850-8ef9cc769e88 (S4A): shared local capability, not completion of the whole E4 client matrix.

---
Planning correction supersedes the original dependency on the whole E4 epic. The security foundation depends on E2 (3b9279b5-aafd-431a-90a9-128ce4415de0) and the shared local capability S4A (29caa8e3-289a-416c-9850-8ef9cc769e88), not completion of all client integrations. S6A admits the GovOne session gate before S4B real remote tool-result qualification. Synthetic no-private-data tests may precede admission; real local text/images/derived labels/tool payloads may not bypass it. Parent hierarchy is organization, not an implicit requirement to finish E4 before its local capability child can execute. No status advancement, implementation completion or live admission is claimed.

Native findings 4456856407/4456856471: E6 root local_only criterion now allows only explicitly authenticated allowlisted local-service/loopback transports while denying unauthorized external/cloud/third-party egress and untrusted private-IP routes. Root dependency prose matches current E2+S4A summary; child S6A concretely consumes S2A+S4A. Historical creation/comments are preserved, not amended. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

---