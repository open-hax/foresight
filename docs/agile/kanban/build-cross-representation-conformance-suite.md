---
category: "kanban"
labels: ["test", "law", "conformance", "representation"]
type: "story"
title: "Build the cross-representation conformance suite"
priority: "P1"
status: "incoming"
uuid: "2aae6edd-cb49-516f-96b6-61592342ad49"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Build the cross-representation conformance suite

## Story

**As a** maintainer  
**I want** every registered representation shape to prove the same laws against
shared fixtures  
**so that** a new format cannot silently weaken a domain contract.

## Acceptance

- Maintain a registry of supported representation morphisms.
- Shared fixtures exercise every applicable round trip.
- Cross-format paths compare normalized semantic values, not serialized bytes.
- Failures distinguish invalid semantic input, invalid representation,
  unsupported target, unexpected semantic loss, declared loss, and adapter
  unavailability.
- Include `.ημ` relocation/partition fixtures proving UUID and causal-graph
  invariance across different physical layouts.
- Historical evidence remains immutable; strengthened laws have explicit version
  boundaries.
- Consumer repositories can run conformance against the exact dependency
  revision they use.

## Dependencies

Consumes the implemented representation shapes and `.ημ` identity/causality
laws.
