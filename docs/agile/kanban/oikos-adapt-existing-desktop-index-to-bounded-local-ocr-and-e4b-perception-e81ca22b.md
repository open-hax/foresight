---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["3b9279b5-aafd-431a-90a9-128ce4415de0", "0db50b53-6c7a-489a-bff9-048d57ce4cbb"]
parent: "92b529b9-256d-4654-8aa6-5f1884a8adaa"
type: "task"
write-id: "1793718990712-0.zb8p11vg5cdojbywbh"
points: "8"
title: "Oikos: Adapt existing desktop index to bounded local OCR and E4B perception"
priority: "P1"
status: "incoming"
uuid: "0e124c65-694e-44f1-9959-ee83e81ca22b"
created_at: "2026-11-03T15:11:10.348Z"
---

# Oikos: Adapt existing desktop index to bounded local OCR and E4B perception

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: continuous local perception and event-backed knowledge.

## Outcome

Adapt existing desktop index to bounded local OCR and E4B perception.

## Scope

Use existing NPU OCR text+boxes supplier and admitted local resident llama.cpp E4B; project into existing desktop index.

## Non-goals

No cloud egress, automatic model training, duplicate desktop index, local-trust-is-absolute claim or uncontrolled resident process launch.

## Acceptance criteria

- [ ] Frame/source hash, pixel dimensions/transforms and observation revision preserve box provenance; supplier text differs from model-derived facts.
- [ ] Idle/resource admission, debounce, bounded queue health, pause, sensitive-app exclusion and stop are explicit; Ollama is not runtime authority.

## Verification

Frozen synthetic frame tests and resource/overload/cancel harness prove bounded operation and no duplicate index or cloud egress. These are future acceptance checks, not executed implementation evidence.

## Risks

Local screenshots and derived labels remain sensitive; deletion from projections must not rewrite immutable history or retain forbidden raw payloads.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/docs/notes/design/epic-workspace-go.md
- /home/err/spaces/foresight/docs/agile/kanban/qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback.md

## Dependencies

- 3b9279b5-aafd-431a-90a9-128ce4415de0 (E2)
- 0db50b53-6c7a-489a-bff9-048d57ce4cbb (S7A)
- Baseline edge is S7A only, not ongoing knowledge or optional cloud/12B comparison; avoids a knowledge/qualification cycle.

## Initial runtime and NPU baseline update — 2026-11-03

Operator-reported follow-up evidence (not remeasured by this planning writer): the expected local llama.cpp E4B endpoint on port8082 refuses connections, and the previously observed transient host runner on port45265 has disappeared. This supersedes any earlier description of that transient runner as currently active. Stable endpoint/runtime qualification is first, before any actual model quality claim; no E4B semantic baseline is established.

The NPU live baseline used 3 synthetic 960x600 images containing 9 lines: 9 valid boxes, but exact normalized text only 3/9, with reported inference 130–286 ms. Box/schema validity is not text accuracy or model semantic correctness. Semantic performance remains unmeasured. No private capture, cloud calls or 12B load occurred in the reported baseline. Future measurements require pinned runtime/model/mmproj/hash/hardware/time/context and resource admission; do not infer LLM NPU support from OCR.

Additional acceptance check: preserve this reported baseline with synthetic source/coordinate provenance, distinguish supplier timings from end-to-end latency, and deny model-quality conclusions until stable runtime qualification and independent held-out semantic evaluation exist. No new inference, capture, cloud request or model load is authorized by this card.

---
Planning correction supersedes qualitative-only resource acceptance and verification. Before any admitted execution, require a versioned, frozen resource profile specifying memory and CPU limits, maximum queued frames and total queued bytes, overflow behavior, inference deadline, and maximum pause/stop latency. Measure candidate thresholds in an isolated resource-admitted fixture, then freeze the selected profile and its identity before the admitted run; do not invent fixed numeric limits now or tune them silently during qualification.

Corrected acceptance criteria: execution refuses a missing/unfrozen/incomplete profile; the profile covers every listed bound and the queue overflow policy. Corrected verification: overload and reconnect tests assert the configured memory/CPU/queue frame/byte bounds, declared overflow behavior, inference deadline and pause/stop latency; retain profile version/hash and measured results. Reconnect cannot discard resource admission or silently reset limits; violations are explicit failures. No resource measurement, inference or completed verification is claimed by this comment.

---