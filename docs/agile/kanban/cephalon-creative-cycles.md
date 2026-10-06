---
uuid: "25e3a688-09b5-4e9a-8765-9b13944b1a02"
title: "Each unattended creative cycle saves an original inspectable artifact"
status: incoming
priority: P1
points: 5
labels: "knoxx, cephalon, artifacts, contracts"
epic: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
parent: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
dependency: "25e3a688-09b5-4e9a-8765-9b13944b1a01"
---

## Context

The current social scan may do nothing. Native music, workspace, image and voice
tools exist; autonomous creation needs verifiable outputs and bounded lifetimes.

## Outcome

An enabled maker cycle produces an original artifact or records an explicit
failure, with job identity and a discoverable persisted result.

## Scope

Executable agent/trigger resources, an explicit cadence and deadline, one
outstanding cycle per maker, existing worker delegation, output validation and
addressable result/receipt data at the existing persistence boundaries.

## Non-goals

Invent a synthesis engine, require paid image generation, enforce a fixed artistic
style, or claim every generated piece must be published.

## Acceptance criteria

1. Three consecutive unattended cycles produce validated saved artifacts or
   explicit failed outcomes; successful cycles cover at least two media.
2. Music has a playable non-empty audio file and its composition specification;
   visual work has a valid image/SVG; lyrics/MIDI have parseable inspectable data.
3. The maker reservation lasts until its run and delegated work have terminal
   outcomes, not merely until dispatch returns an acceptance receipt. Hold a
   delegated job nonterminal after dispatch returns; later ticks must be
   coalesced or refused with an observable result until that job terminates.
   A provider failure yields the retry/backoff limits in the recovery story.
4. Delegated work has parent/job identities and a terminal outcome. The head
   does not await a maker before responding to conversation.
5. Completion is derived from verified outputs, not the model's prose. Stored
   artifacts and provenance survive backend recreation.

## Verification

Failing state/output laws, native-tool boundary tests and backend gates. Run
three bounded real cycles, inspect files and persisted receipts, document model
and served revision. Missing providers are failures, not synthetic successes.

## Risks

Expensive idle work, repeated motifs, tool hallucination, media decoding failures,
disk growth and a schedule dispatch lock that ends before job completion.
