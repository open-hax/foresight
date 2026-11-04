---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["93a76dad-678a-4980-86fb-ba0fbb5e8203", "0e124c65-694e-44f1-9959-ee83e81ca22b", "c703893f-aea4-41c2-9eab-c3bd37b28c9b", "0db50b53-6c7a-489a-bff9-048d57ce4cbb"]
parent: "3de19b00-adae-43e6-a6c4-da339faa6482"
type: "task"
write-id: "1793726620832-0.pjmrwzynnydig9g5115"
points: "5"
title: "Oikos: Record consented corrected labels and leakage-safe corpus partitions"
priority: "P1"
status: "incoming"
uuid: "2a08e0cb-e029-46ae-b217-399d0d3ebf42"
created_at: "2026-11-03T15:11:12.528Z"
---

# Oikos: Record consented corrected labels and leakage-safe corpus partitions

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: consented labels and bounded Colab training corpus.

## Outcome

Record consented corrected labels and leakage-safe corpus partitions.

## Scope

Append corrected labels, negative feedback, accepted/rejected actions and OCR boxes with source hashes, consent and privacy lineage.

## Non-goals

No new compute-unit spending, automatic training, automatic production adoption, private screenshot export or assumption old budget transfers.

## Acceptance criteria

- [ ] Actual identity controls dedupe; corrections append without similarity merges or rewriting provenance.
- [ ] Oikos portable .cljc corpus laws define a versioned source-family key from source/session identity, app identity and a frozen temporal grouping policy. Every source record, correction (transitively), rejected action and OCR box retains its source observation ID and family key; all rows sharing actual identity or correction lineage and all members of that defined family stay in one train/dev/held-out split. Similarity can flag leakage but never establish identity or silently merge families. Consent revocation controls payload availability.

### S8A consent admission

- [ ] Valid explicit consent for the exact source/item and intended purpose precedes label/correction retention and use. Missing, wrong-scoped, expired or revoked consent denies admission/use. Model predictions and action acceptance are not human gold labels and grant no retention, training or cloud-export rights; retain their distinct attribution. Local consent cannot authorize a cloud recipient.

Future verification: synthetic labels/corrections exercise absent consent, wrong source/item/purpose, expiry and revocation and assert no unauthorized retained/used payload. A valid scoped consent permits only its stated local purpose. Revoke after preparation and verify corrections, projections and prepared payload availability are withdrawn, leaving only safe provenance; a prediction or accepted action alone never passes human-gold/retention/training/export admission.

## Verification

Synthetic replay/correction/identity fixtures and split leakage audit place a source, correction-of-correction, rejected action and OCR box in the same family/split; missing or conflicting family/lineage is rejected, and cross-split near-duplicates fail the audit without similarity merges. Verify source observation identity and coordinate lineage survive export preparation. Clio supplies append/replay, not product-specific corpus-family semantics. These are future acceptance checks, not executed implementation evidence.

## Risks

Cloud training exposes private data and can consume nonrefundable CU; historical pilot authority does not transfer to this corpus.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/spaces/foresight/docs/agile/kanban/qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback.md
- /home/err/spaces/foresight/docs/agile/kanban/colab-browser-mcp-knoxx.md

## Dependencies

- 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A): causal observation identity and replay contracts.
- 0e124c65-694e-44f1-9959-ee83e81ca22b (S5A): attributed perception observations and coordinate provenance, not the full knowledge loop.
- c703893f-aea4-41c2-9eab-c3bd37b28c9b (S6A): policy-session/consent boundary, not the entire Lens epic.
- 0db50b53-6c7a-489a-bff9-048d57ce4cbb (S7A): frozen synthetic baseline and independent split identity; optional S7B comparisons are not prerequisites.
- Related research (not inherited budget or completion authority): qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback; parent da84a64d-f118-4722-8709-a1c556034f90.

---
Native findings 4456846195 and 4456856532: consume S2A causal identity, S5A attributed perception, S6A policy boundary and S7A local frozen baseline; optional S7B/whole E7 is not a prerequisite. Versioned source-family key binds source/session identity, app identity and frozen temporal grouping policy. Source, transitive corrections, rejected actions and OCR boxes retain source observation ID/family and stay in one split. Missing/conflicting lineage denies admission; similarity flags leakage but never establishes identity. Current native child prerequisites synchronized to the user-approved precise plan: 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A), 0e124c65-694e-44f1-9959-ee83e81ca22b (S5A), c703893f-aea4-41c2-9eab-c3bd37b28c9b (S6A), 0db50b53-6c7a-489a-bff9-048d57ce4cbb (S7A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Consensus planning correction v1: root incoming acceptance/verification now explicitly specifies s8a/consent-admission. See the corresponding named root subsections for the contract and concrete future positive/negative fixtures. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

---