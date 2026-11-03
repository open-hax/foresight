---
uuid: "92b529b9-256d-4654-8aa6-5f1884a8adaa"
title: "Oikos: continuous local perception and event-backed knowledge"
status: "incoming"
type: "epic"
priority: "P1"
labels: "oikos, planning, local-first, epic"
dependency: ["3b9279b5-aafd-431a-90a9-128ce4415de0", "0db50b53-6c7a-489a-bff9-048d57ce4cbb"]
category: "kanban"
write-id: "1793718670025-0.vubq34b9tcje0jqlk2u"
created_at: "2026-11-03T15:11:10.025Z"
---

# Oikos: continuous local perception and event-backed knowledge

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Accumulate bounded, inspectable local knowledge while preserving the existing desktop index.

## Scope

Resident local llama.cpp E4B is runtime authority, not Ollama. Existing NPU OCR supplies text and boxes. Preserve existing desktop index through an adapter, not duplicate ingestion authority. Separate observed/derived/accepted facts and retrieval projection; retain image pixel coordinate provenance. Event-backed memory is not instant weight training. Provide forgetting/retention, pause, sensitive-app controls, idle/resource admission, debounce, bounded queue health and stop.

## Non-goals

No cloud egress, automatic model training, duplicate desktop index, local-trust-is-absolute claim or uncontrolled resident process launch.

## Acceptance criteria

- [ ] Supplier, frame/hash, dimensions, coordinate transform, revision and epistemic tier survive ingestion and retrieval.
- [ ] Pause/sensitive-app exclusion, retention/deletion propagation, queue overload and stop have measurable bounded behavior.
- [ ] Memory derives from events and projects into the existing index; no weight mutation is implied.

## Verification

Synthetic frames and accepted local baseline drive deterministic projection tests; resource/queue/retention/cancel fixtures prove bounds without production activation. These are future acceptance checks, not executed implementation evidence.

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
