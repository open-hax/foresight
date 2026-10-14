---
category: "kanban"
type: "story"
story_id: "EC1.01"
uuid: "d3277a3c-6e30-42b1-8151-16c041612a6a"
epic: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
parent: "82c55f16-a688-4ba5-983d-c44c618cdd5d"
title: "EC1.01: Validate one Knoxx embedding runtime across ingest, search and repair"
priority: "P1"
points: "5"
labels: "knoxx, embeddings, runtime, law, 5sp"
---

# EC1.01: Validate one Knoxx embedding runtime across ingest, search and repair

## Context

Start after planning review and lawful Rheos readiness. There is no predecessor
story. The dated production observations and complete deployment outcome are
recorded in [the epic](epic-source-preserving-knoxx-embedding-cutover-82c55f16.md).
This story is owned by `open-hax/knoxx`; Foresight coordinates its plan only.

The current [Knoxx SDK adapter](https://github.com/open-hax/knoxx/blob/e524c7e7519bcb867514dbc164b0868bc6ee6c8a/backend/src/cljs/knoxx/backend/extern/openplanner_sdk.cljs)
uses convenience methods and dimension-only event-vector checks. The pinned
SDK default embedding provider sends `{model,input}`, and its cache identity
omits dimensions, route and most source text. A migration-only provider canary
cannot qualify future ingest, search and repair paths.

## Outcome

One Knoxx-owned, uncached, validated generation runtime enforces direct host
Ollama, `qwen3-embedding:8b`, and actual finite 1024-dimensional output in every
owned SDK ingest, search and repair path. Model identity becomes part of
coverage validity as well as dimension identity.

## Scope

Separate portable request/result/identity laws in Knoxx `.cljc` from CLJS
HTTP/provider and SDK interop in named extern adapters. Integrate the existing
`extern/openplanner_sdk.cljs` with the public SDK cores:

- `ingestEvents({mongo, embeddingRuntime, log}, events)`;
- `vectorSearchWithQuality({mongo, embeddingRuntime}, body)`;
- `indexTextInMongoVectors` with the explicit owning `embeddingFunction`.

These exports are from SDK
`07085d6557b75834ce6f50e6c54b8ca47e1c7c08`. Keep its chunking, partitions,
model resolution, graph projection and persistence semantics in that owner.
Do not invent a runtime option for `createOpenPlannerSdk`: its factory accepts
config/log and closes over its own runtime. Any unsupported parallel-pool path
must refuse rather than fall back to an unvalidated provider.

The provider request is exactly bound to model, route, full inputs and
`dimensions:1024`. Require a complete response with unambiguous per-input
identity, no contradictory model identity, and exactly 1024 finite numbers per
vector. Omitted/ignored dimensions yielding another size refuse. Initial
qualification is uncached and cannot borrow the SDK's model/prefix cache.

[Ollama's official OpenAI compatibility reference](https://docs.ollama.com/api/openai-compatibility)
lists `dimensions` for `/v1/embeddings` (checked 2026-10-14). This is API
documentation, not evidence that production Ollama 0.33.2 and the target model
produce the requested 1024 output. Bootstrap and an actual whole-batch candidate
canary remain required; no installation or provider invocation is claimed here.

## Non-goals

Migration execution, canonical event replay, SDK changes, custom chunking or
model-selection semantics, cached success evidence, a second provider for a
fallback path, or Services admission. No source/controller revision is selected
by this card.

## Acceptance criteria

- [ ] Actual SDK ingest, hot/compact query and repair contexts receive the same
  checked runtime; source guards prove no legacy/default provider remains in
  the declared owned callsites. Wrong model or route selection refuses before
  dispatch, including query-visible legacy partitions.
- [ ] Missing/duplicate/unbound result indices, wrong cardinality, short/long,
  nonfinite or contradictory-model vectors refuse the generated batch before
  vector persistence attributable to that batch. Never truncate, pad, filter
  or accept only its successful subset. Retain and account for genuine SDK
  source/structural writes and successful independent batches as partial
  progress; any generation failure refuses overall completion and qualification.
- [ ] Coverage checks bind recorded model, dimensions, source and chunk identity;
  old gte/1024 rows cannot satisfy the qwen/1024 predicate.
- [ ] Direct host requests carry no Proxx credential. Actual installed SDK and
  compiled adapter hashes, candidate image/source and request/response custody
  bind every runtime qualification result.
- [ ] Owned integration controls exercise actual SDK ingest/search/repair with
  the injected runtime, including partial generation failure and a wrong-model
  legacy partition. SDK success counters alone cannot establish coverage.
- [ ] After host model bootstrap, actual target runtime requests produce finite
  1024 vectors through each declared path; unknown endpoint support or an
  unavailable model stays a failure, not an exemption.

## Verification

Retain causal red controls against the current default provider and dimension-only
validity predicate, then green controls through the real exported SDK contexts.
Use owned persistence fixtures and assert that a refused generated batch writes
no attributable vectors. Real ingest fixtures retain and account for the SDK's
source, structural and independently successful batch writes; do not counterfeit
event admission or claim a global no-write transaction. Preserve EC1.02's
separate prohibition on production reinsertion of historical sources. Run the
genuine Knoxx owner tests, server/typecheck/lint gates and
source guards with zero errors/warnings. The later migration and production
qualification reuse this exact runtime; a fixture or unrelated Services direct
embedding request is insufficient live proof. No provider call is performed by
this planning card.

## Risks

The host API may ignore or reject the requested dimension. The SDK may catch
batch errors and report partial progress; the adapter and later coverage proof
must preserve that failure. Model selection and hot/compact contexts must stay
aligned without copying SDK owner semantics. Five points assumes the bounded
current export/callsite integration; split and review any new independent scope.
