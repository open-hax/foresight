---
category: "content"
labels: "epiphany, content, providers, review, design"
parent: "e7cdf5cf-423e-49fc-aca8-6ed6055f1cd5"
type: "story"
write-id: "1791011471191-0.kwevbs2ln8s3u690jo8"
points: "3"
title: "Design Epiphany source-provider and review boundaries"
priority: "P1"
status: "ready"
epic: "e7cdf5cf-423e-49fc-aca8-6ed6055f1cd5"
uuid: "76983190-be58-47ac-849c-c8a9f37fe573"
---

# Design Epiphany source-provider and review boundaries

## Context

(己, p=0.99) Inspected Epiphany at
`ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66` provides Git revision observations,
Markdown maps/spans, evidence retrieval and review records for lineage
candidates. Its document-governance/research/review policies describe broader
artifact forms, while non-Git authority remains deferred. Relevant documents
and Markdown law/shape files are identical at inspected remote
`643be698ea0d841dd19385506b272872306e456e`. This does not establish a general
provider/review runtime.

## Outcome

A reviewer can evaluate one bounded Epiphany design for source-bearing document
inputs and review provenance without mistaking Git evidence, retained decisions
or rendered Markdown for universal source authority or persistence.

## Scope

- Implementation/design owner: `octave-commons/epiphany`, following its ADR and
  document-governance instructions; Foresight holds the coordination card.
- One design document grounding provider observations and review targets in
  current Epiphany contracts and the proposed shared admission boundary.
- Worked Markdown/Git, volatile API-query and formatted-raw-input examples.
- Explicit decisions/gaps for identity, version, assembly context, availability,
  coverage, freshness, content retention, review retention and write authority.
- Review the design for its stated scope and record the disposition; identify
  follow-on implementation decisions without silently approving them.

## Non-goals

- No production provider, API query adapter, acquisition job or new source store.
- No generalization of the existing Git lineage review runtime into an edit
  proposal/acceptance engine.
- No universal Git/path/frontmatter requirement or compulsory payload archive.
- No copying Alpha/Katamorph laws or Rheos task/write semantics into Epiphany.
- No automatic acceptance of existing draft policies or common-law promotion.

## Acceptance criteria

- The design cites exact existing schemas/functions and governing document
  status, separating implemented behavior from proposals and unresolved gaps.
- Each worked example identifies its source entity or query/input identity,
  separate observation identity, actual source version if available, coverage,
  observation time and versioned transformation/schema context.
- The volatile example works without file/Git fields or a fabricated immutable
  revision. It states whether payloads expire, refresh or become unavailable
  and what retained review records can still explain without exact replay.
- Content retention and review-record retention are separate. A digest or
  locator alone is not presented as reconstructable evidence.
- Review identifies target/context, criteria, inspected basis, reviewer/authority,
  disposition and limitations. Review acceptance is distinct from authorizing a
  source mutation, attempting that write and observing its result.
- The design identifies reusable shared contracts and their owners without
  assuming the Alpha candidate is accepted or requiring its implementation as
  an input dependency.
- An independent design review records a bounded disposition; unresolved
  architectural decisions remain visible and constrain follow-on work.

## Verification

Read applicable current Epiphany ADRs, source contracts and process documents.
Trace the worked examples through the proposed boundaries and compare them
against the actual Git/Markdown implementation. Record source heads, design
diff, reviewer findings/dispositions and remaining decisions in the owning PR.
Use existing document checks where available; missing automation remains an
explicit limit. Design review is the evidence for this card, not a fictional
provider test run or deployed capability.

## Risks

Git-backed identity can be generalized too broadly, and retaining a judgment
can be mistaken for retaining its source payload. Existing lineage acceptance
may be confused with reviewed edit application. Architectural authority must be
resolved in Epiphany rather than inferred from a Foresight inventory row or a
shared vocabulary.