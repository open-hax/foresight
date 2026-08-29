---
title: "Knoxx deployment authority decision — DigitalOcean only"
summary: "Preserves the operator adjudication that the Services-owned DigitalOcean path is the only forward production deployment authority for Knoxx."
category: "decision"
created: "2026-08-22"
status: "accepted"
decision_id: "foresight/knoxx-deployment-authority/2026-08-22"
---

# Knoxx deployment authority decision — DigitalOcean only

## Decision

> Only the DigitalOcean path is moving forward.

The Services-owned DigitalOcean stack is the sole forward production deployment authority for Knoxx.

## Authority and provenance

This record preserves an explicit operator adjudication given on 2026-08-22. It is not inferred from repository topology, merge status, or the relative maturity of the competing implementations.

## Scope

- The Knoxx push-triggered Promethean production workflow is a retirement surface.
- The Knoxx branch inside `open-hax/services/.github/workflows/deploy-promethean.yml` is compatibility or migration machinery unless another evidenced caller still requires it.
- Any staging capability worth retaining must be expressed through the DigitalOcean environment and host contracts.
- This decision does not classify unrelated service branches in the reusable Promethean workflow.

## Follow-through

Retirement remains evidence-gated: preserve any required recovery or deployment receipts before disabling the legacy trigger, and verify the DigitalOcean path owns the replacement behavior.
