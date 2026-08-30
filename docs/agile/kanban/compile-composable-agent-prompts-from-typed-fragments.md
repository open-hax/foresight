---
uuid: "809785a6-213f-492a-a840-7174ffcdd865"
title: "Compile composable agent prompts from typed fragments"
status: "done"
priority: "P0"
labels: [""]
created_at: "2026-08-29T01:47:42.577Z"
parent: "225c8b6b-3ad1-4c29-b5bf-70104630d950"
write-id: "1787968267262-0.z9hubiawtvis1gmjd6"
---

---
Archaeology anchors: Knoxx/Sol currently composes role prompts, then actor prompts, then agent prompts with blank-line templates; roles also contribute capabilities/tools. Mindfuck separates immutable doctrine and a priority-ordered skill graph, while Muse only has idempotent string-section replacement. Implement the smallest pure fragment law/compiler: explicit scope, contributor kind/id, precedence/order, conditions, authority claim, EvidenceRef provenance, target set, version, deterministic conflict/budget diagnostics, and a content-free rendered receipt.

Acceptance clarified: receipt must record compiler/target versions, selected order, excluded fragments with reasons, EvidenceRefs, conflicts, diagnostics, size/token estimate and a content fingerprint without storing prompt content. Documentation is required before document→done.

Implemented pure prompt-fragment law/compiler and documented archaeology. Deterministic conditions/order; separate Axxium authority decisions; exclusive-slot conflicts; target and token-budget diagnostics; selected/excluded receipt with EvidenceRefs and no rendered content. Verification: prompt compiler 5 tests/14 assertions, EvidenceRef 4/14, root lint zero warnings, diff check clean.
---