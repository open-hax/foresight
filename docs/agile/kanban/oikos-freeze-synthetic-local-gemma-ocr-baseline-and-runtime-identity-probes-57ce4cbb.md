---
uuid: "0db50b53-6c7a-489a-bff9-048d57ce4cbb"
title: "Oikos: Freeze synthetic local Gemma OCR baseline and runtime identity probes"
status: "incoming"
type: "task"
priority: "P1"
points: "5"
labels: "oikos, planning, local-first, task"
parent: "e842e6d9-dd41-4ca2-b229-dc2466198864"
dependency: ["722906b7-77d7-4414-a976-23996f7533de"]
category: "kanban"
write-id: "1793718669717-0.fh5m7lmbtscs0zwro9"
created_at: "2026-11-03T15:11:09.717Z"
---

# Oikos: Freeze synthetic local Gemma OCR baseline and runtime identity probes

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: empirical local Gemma and OCR qualification.

## Outcome

Freeze synthetic local Gemma OCR baseline and runtime identity probes.

## Scope

Create independent labelled synthetic text/image/bbox/action-planning cases, frozen held-out split and bounded endpoint identity preflight.

## Non-goals

No claim 4B is inherently too dumb, port8082 deployment readiness, implicit cloud admission, model download or heavy live benchmark this turn.

## Acceptance criteria

- [ ] Record actual llama.cpp model/mmproj hashes, hardware, timestamp and context before admitted measurement; preserve reported absent8082 versus transient45265 host ollama-owned llama-server mismatch pending fresh observation.
- [ ] Compute schema validity separately from semantics; word/block recall, IoU, hallucination, cold/warm p50/p95 TTFT/token/s, RAM/VRAM and cancellation have tested definitions.
- [ ] Baseline has no knowledge-loop dependency, target-word circular search, cloud call, disruptive download or inference without resource admission.

## Verification

Offline metric oracle fixtures and split/hash audit first; negative preflight rejects unknown endpoint identity or denied resources; later admitted E4B/PP-OCR run retains raw timings and supplier boxes. These are future acceptance checks, not executed implementation evidence.

## Risks

Runtime identity drifts, tiny biased samples and resource contention can invalidate comparisons; endpoint observation is reported, not verified this session.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/docs/agile/kanban/qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback.md
- /home/err/docs/notes/design/epics-and-tasks.md

## Dependencies

- 722906b7-77d7-4414-a976-23996f7533de (E1)
- Local baseline depends only on E1; cloud comparison is isolated in S7B and depends on E6 security admission.

## Initial runtime and NPU baseline update — 2026-11-03

Operator-reported follow-up evidence (not remeasured by this planning writer): the expected local llama.cpp E4B endpoint on port8082 refuses connections, and the previously observed transient host runner on port45265 has disappeared. This supersedes any earlier description of that transient runner as currently active. Stable endpoint/runtime qualification is first, before any actual model quality claim; no E4B semantic baseline is established.

The NPU live baseline used 3 synthetic 960x600 images containing 9 lines: 9 valid boxes, but exact normalized text only 3/9, with reported inference 130–286 ms. Box/schema validity is not text accuracy or model semantic correctness. Semantic performance remains unmeasured. No private capture, cloud calls or 12B load occurred in the reported baseline. Future measurements require pinned runtime/model/mmproj/hash/hardware/time/context and resource admission; do not infer LLM NPU support from OCR.

Additional acceptance check: preserve this reported baseline with synthetic source/coordinate provenance, distinguish supplier timings from end-to-end latency, and deny model-quality conclusions until stable runtime qualification and independent held-out semantic evaluation exist. No new inference, capture, cloud request or model load is authorized by this card.
