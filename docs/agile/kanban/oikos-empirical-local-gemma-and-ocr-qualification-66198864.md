---
category: "kanban"
labels: "oikos, planning, local-first, epic"
dependency: ["722906b7-77d7-4414-a976-23996f7533de"]
type: "epic"
write-id: "1793724132717-0.mj984w9vt25mw1l04k"
title: "Oikos: empirical local Gemma and OCR qualification"
priority: "P1"
status: "incoming"
uuid: "e842e6d9-dd41-4ca2-b229-dc2466198864"
created_at: "2026-11-03T15:11:09.409Z"
---

# Oikos: empirical local Gemma and OCR qualification

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Replace model-size assumptions with reproducible, resource-admitted local measurements and separately gated comparisons.

## Scope

Identify actual llama.cpp endpoint model/mmproj/hash/hardware/time/context. Freeze labelled synthetic text/image/bbox/action-planning cases and held-out cases independent of target words (no circular search). Compare existing NPU PP-OCR boxes and E4B, schema validity separately from semantic correctness. Measure word/block recall, IoU, hallucinations, cold/warm p50/p95 TTFT/token/s, RAM/VRAM and cancellation. Identify official 12B identity/quant/context and measure CPU/GPU/NPU OpenVINO support; OCR acceleration proves no LLM NPU support. No disruptive download/OOM test without admission. proxx gemma4:31b free-route claim needs verification and only governed sanitized fixtures after security admission.

## Non-goals

No claim 4B is inherently too dumb, port8082 deployment readiness, implicit cloud admission, model download or heavy live benchmark this turn.

## Acceptance criteria

- [ ] Frozen independent labels and runtime identities make every score and unsupported backend reproducible; structured validity and semantic quality are separate.
- [ ] Record the dated operator observation that port8082 was absent and port45265 was previously observed as a host ollama-owned transient llama-server, then reported disappeared. Neither endpoint is an active qualification target without fresh identity/resource probes; preserve the mismatch and do not silently route around it.
- [ ] Local synthetic baseline is independent of ongoing knowledge; any 12B/cloud comparison requires fresh resource/policy admission and reports route/pricing evidence.

## Verification

Offline metric unit tests, deterministic fixture hashes and held-out split audit first; admitted local trial later captures endpoint identity and raw bounded timings; security gate precedes cloud. These are future acceptance checks, not executed implementation evidence.

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

---
Native finding 4456856458: E7 root AC no longer describes port45265 as active. It was previously observed then reported disappeared; fresh runtime identity/resource proof is required. S7A consumes S1A only; optional S7B consumes S7A+S6A and stays blocked where policy withholds actual evidence. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

---