---
category: "kanban"
labels: "oikos, planning, local-first, epic"
dependency: ["3b9279b5-aafd-431a-90a9-128ce4415de0", "29caa8e3-289a-416c-9850-8ef9cc769e88"]
type: "epic"
write-id: "1793718989871-0.bzfdp9vkgdf68mx335h"
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
- [ ] Deletion and scoped access are tested end to end; local_only produces no network egress.

## Verification

Gateway contract fixtures and deny-network harness cover text/image/derived/tool paths, outage/retry attempts, auth revocation, deletion and adversarial cloud response. These are future acceptance checks, not executed implementation evidence.

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
Planning correction supersedes the original dependency on the whole E4 epic. The security foundation depends on E2 (3b9279b5-aafd-431a-90a9-128ce4415de0) and the shared local capability S4A (29caa8e3-289a-416c-9850-8ef9cc769e88), not completion of all client integrations. S6A admits the GovOne session gate before S4B real remote tool-result qualification. Synthetic no-private-data tests may precede admission; real local text/images/derived labels/tool payloads may not bypass it. Parent hierarchy is organization, not an implicit requirement to finish E4 before its local capability child can execute. No status advancement, implementation completion or live admission is claimed.

---