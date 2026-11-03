---
uuid: "3de19b00-adae-43e6-a6c4-da339faa6482"
title: "Oikos: consented labels and bounded Colab training corpus"
status: "incoming"
type: "epic"
priority: "P1"
labels: "oikos, planning, local-first, epic"
dependency: ["3b9279b5-aafd-431a-90a9-128ce4415de0", "92b529b9-256d-4654-8aa6-5f1884a8adaa", "e616187d-ba13-42b1-addd-aa4d6244a5be", "e842e6d9-dd41-4ca2-b229-dc2466198864"]
category: "kanban"
write-id: "1793718672216-0.46te7656w8aij491bd2"
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

Synthetic correction/dedupe/split/export tests; deny export without consent/policy/approval; dry-run budget/teardown proofs only and zero new allocation. These are future acceptance checks, not executed implementation evidence.

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
- e842e6d9-dd41-4ca2-b229-dc2466198864 (E7)
- Related research (not inherited budget or completion authority): qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback; parent da84a64d-f118-4722-8709-a1c556034f90.
