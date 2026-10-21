---
uuid: "stabilization-e3"
title: "Stabilization E3 — Settle identity, deployment and OpenPlanner integration"
status: "incoming"
type: "epic"
priority: "P1"
labels: "stabilization, coordination, e3"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
dependency: ["stabilization-e1", "stabilization-e2"]
category: "kanban"
write-id: "1792481172429-0.gfeac78mmk61wb901xr"
created_at: "2026-10-20T07:26:12.429Z"
---

# E3 — Settle identity, deployment and OpenPlanner integration

## Context

The main CMS merge conflict isn't merely textual. It reflects incompatible identity ownership between current Knoxx and the older Wiki stack.

Stabilization coordination only. Preserve child ownership and existing story identities.

## Scope

- E3.1: Decide Axxium ownership: Resolve the remote-versus-embedded provider boundary documented in [Knoxx #324](https://github.com/open-hax/knoxx/issues/324?utm_source=chatgpt.com).
- E3.2: Qualify Knoxx authorization: Test login, logout, sessions, MCP authority, revocation and cross-tenant refusal.
- E3.3: Resolve Axxium's upstream PRs: Settle concurrency and OAuth-state findings.
- E3.4: Qualify Services: Verify protected deployment authority, required secrets, staging and rollback.
- E3.5: Promote OpenPlanner: Resolve [Foresight #139](https://github.com/open-hax/foresight/pull/139?utm_source=chatgpt.com) and establish accepted runtime/source pins.

The slot-by-slot exit proofs and existing-task candidates are in [the portfolio](../../stabilization/portfolio.md#e3--settle-identity-deployment-and-openplanner-integration).

## Acceptance

Identity and deployment interfaces are compatible across the accepted repositories, with executed integration tests.

- Resolve every slot to existing canonical work or an explicit owned gap before creating execution stories.
- Bind qualification evidence to exact source revisions and preserve prior attempts.
- Registration and board status do not qualify a milestone.

## Dependencies

`stabilization-e1`, `stabilization-e2`

## Evidence

[Portfolio tracker](../../stabilization/tracker.md); [source-backed work graph](../../stabilization/work-graph.json).

## Definition of done

Identity and deployment interfaces are compatible across the accepted repositories, with executed integration tests.
