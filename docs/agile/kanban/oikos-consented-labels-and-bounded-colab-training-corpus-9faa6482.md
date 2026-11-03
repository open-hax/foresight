---
category: "kanban"
labels: "oikos, planning, local-first, epic"
dependency: ["3b9279b5-aafd-431a-90a9-128ce4415de0", "92b529b9-256d-4654-8aa6-5f1884a8adaa", "e616187d-ba13-42b1-addd-aa4d6244a5be", "0db50b53-6c7a-489a-bff9-048d57ce4cbb"]
type: "epic"
write-id: "1793724128468-0.ry67s2flkhplaiq3i1z"
title: "Oikos: consented labels and bounded Colab training corpus"
priority: "P1"
status: "incoming"
uuid: "3de19b00-adae-43e6-a6c4-da339faa6482"
created_at: "2026-11-03T15:11:12.216Z"
---

# Oikos: consented labels and bounded Colab training corpus

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending.

## Outcome

Produce a consented reproducible corpus while retaining cloud spending, training and production holdbacks.

## Scope

Append-only corrected labels, negative feedback, accepted/rejected actions and OCR boxes with consent/source hashes/privacy lineage. Dedupe by actual identity, not similarity. Train/dev/held-out splits grouped by temporal/app/source identity to prevent near-duplicate leakage. Reproducible JSONL/VLM coordinates, model/template/license. Export allowlist requires GovOne inspection and explicit approval before Google/Colab, which is cloud. Fresh budgets and teardown/resource proofs; link bounded pilot and its parent without inheriting its budget.

## Non-goals

No new compute-unit spending, automatic training, automatic production adoption, private screenshot export or assumption old budget transfers.

## Acceptance criteria

- [ ] Corrections retain provenance and rejected-action/negative feedback history without silent similarity merges; consent and deletion lineage control payload availability.
- [ ] Group split audit rejects near-duplicate leakage across source/app/time groups; exports pin coordinate, template, model and license versions.
- [ ] GovOne inspection plus explicit approval and a fresh campaign/resource/teardown plan are mandatory before export or allocation; existing pilot and parent remain separate.

## Verification

Synthetic correction/dedupe/split/export tests; deny export without consent/policy/approval, with a reused historical pilot budget, missing fresh campaign budget, missing resource-bound proof or uncertain teardown. Dry-run budget/resource/teardown proofs only and zero new allocation; historical pilot authority never transfers. These are future acceptance checks, not executed implementation evidence.

## Risks

Cloud training exposes private data and can consume nonrefundable CU; historical pilot authority does not transfer to this corpus.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/docs/agile/kanban/qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback.md
- /home/err/spaces/foresight/docs/agile/kanban/colab-browser-mcp-knoxx.md

## Dependencies

- 3b9279b5-aafd-431a-90a9-128ce4415de0 (E2)
- 92b529b9-256d-4654-8aa6-5f1884a8adaa (E5)
- e616187d-ba13-42b1-addd-aa4d6244a5be (E6)
- 0db50b53-6c7a-489a-bff9-048d57ce4cbb (S7A): local baseline only, not optional S7B/cloud/12B comparison completion. These parent entries summarize intent; child execution gates are the concrete artifact edges on S8A/S8B, never implicit whole-parent prerequisites.
- Related research (not inherited budget or completion authority): qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback; parent da84a64d-f118-4722-8709-a1c556034f90.

---
Native findings 4456846195/4456856448: E8 replaces whole E7 with S7A baseline in native dependencies/root prose, so optional S7B cannot block local corpus work. Children use precise producer artifacts. Verification explicitly denies reused historical pilot budgets and missing resource-bound proof. S8A family/correction lineage stays in one split; S8B export is separately governed and zero-spend. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

---