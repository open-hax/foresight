---
uuid: "abdd5a2d-dbad-46a0-ba3f-2149dfeafbcf"
title: "Prove translation generation and SME review independently"
status: "todo"
priority: "P1"
labels: ["translation", "transduction", "evaluation"]
created_at: "2026-08-29T15:09:34.361Z"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
write-id: "1788020874514-0.e9n7ziinhpa6bgm3kfg"
---

# Prove translation generation and SME review independently

Recover Knoxx's durable translation semantics without importing CMS, layout,
publishing, or frontend coupling. Translation produces a candidate artifact and
a translation-specific review case; SME judgments produce revision-bound
receipts.

## Vertical slice

```text
source segment + locale + terminology + policy
  -> provider-neutral translation operation
  -> candidate + provenance
  -> agent presents review evidence to one SME
  -> accept, correct, reject, or defer
  -> durable judgment and decision receipt
```

## Acceptance

- Katamorph shapes describe operation input/output and provider envelopes.
- Pure `.cljc` laws validate identity, revision, terminology evidence, legal
  review transitions, and receipt derivation on JVM and CLJS.
- Knoxx keeps model, storage, MCP/HTTP, and UI behavior in replaceable adapters.
- Unit/law tests cover valid and invalid decisions; integration tests cross the
  provider and receipt boundaries; one agent-driven SME flow is exercised E2E.
- Coverage is emitted for the affected packages and no unavailable gate is
  reported as passed.
- The resulting API contains no layout or publication requirement.

---
This is the deliberate post-inflight successor lane for translation generation plus SME judgment. It remains todo until the opening PR portfolio is terminal. Entry evidence must identify the Knoxx behavior and revision being characterized; layout, publishing, and product/content review remain outside this card.

---
