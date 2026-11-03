---
uuid: "da84a64d-f118-4722-8709-a1c556034f90"
title: "Connect Colab notebooks and CLI compute to ChatGPT and local Knoxx through an authenticated droplet gateway"
priority: "P1"
points: 5
labels: "colab, mcp, knoxx, deployment, plugin"
---

# Colab notebooks and CLI compute for Knoxx

## Context

The operator requested a Google Colab MCP server for the DigitalOcean droplet
at `knoxx.promethean.rest`, a ChatGPT plugin, and a capability with skills for
the local Knoxx actor. The operator confirmed browser-based Colab rather than
Colab Enterprise in chat `01a17197-ef98-7240-9470-4a5329add2af` on 2026-10-25.
Google's official [colab-mcp](https://github.com/googlecolab/colab-mcp) runs beside
the browser and dynamically discovers the selected notebook's tools.
The operator subsequently authorized Google's official
[google-colab-cli](https://github.com/googlecolab/google-colab-cli) alongside
the notebook tools, then completed its isolated ADC login. This adds bounded
ephemeral compute jobs without requiring an open notebook tab.

This is a manually authored first-class incoming card. It records no admitted
board status, transition, validation or creation-event claim. Implementation
preparation began before this card; no retroactive planning review is claimed.

## Outcome

ChatGPT and the local Knoxx actor can inspect and work in the same explicitly
connected Colab notebook through authenticated MCP boundaries, with truthful
connectivity, execution and artifact evidence. They can also read compute-unit
usage and run bounded ephemeral CPU or explicitly selected accelerator jobs,
with teardown verified against Google's session list.

## Scope

- Knoxx owns the thin foreign Python transport wrapper around pinned official
  Google code, four stable wrapper tools, and its EDN capability/role/agent.
- Knoxx's ClojureScript startup resolves validated MCP-server resources and
  named environment credentials, failing closed when credentials are absent.
- Services owns the droplet placement, separate gateway process, private SSH
  relay, public Caddy candidate, secrets paths and rollback instructions.
- A separate Stealth compute service uses pinned official CLI 0.7.5, isolated
  Google credentials, six typed compute tools and verified session teardown.
- A private Agent Plugins 1.0 package contains the connection and both skills.
- A single-operator OAuth/DCR adapter supports ChatGPT's connection flow;
  private API-key declarations alone do not provide a portal connection.
- Install the skill into the actual local Knoxx SDK search path and verify it.

## Non-goals

Colab Enterprise provisioning, persistent unattended runtime pools, Google
cookie extraction, a multi-user notebook pool, production Knoxx migration,
changes to OpenPlanner, arbitrary remote file export, and generic remote shell
access to Colab.

## Acceptance criteria

- Anonymous and wrong-key MCP requests return 401 on both HTTP boundaries.
- Public OAuth discovery, consent, PKCE, scoped token issuance, refresh and
  revocation work, while the existing Knoxx issuer keeps its own routes.
- Official browser origin and connection-token restrictions remain in place.
- Dynamic notebook tools are discovered through a fixed outer catalog.
- Tool errors, disconnection and ambiguous timeouts cannot count as success.
- The droplet gateway runs as a non-root user and reaches the bridge privately.
- Public DNS/TLS/route/auth and unrelated production routes are checked separately.
- Local Knoxx resolves the capability, hydrates its skill and actually calls a tool.
- ChatGPT installs/connects the private plugin and can call real status/inspection.
- A disposable Google notebook runs a probe cell and returns its actual output.
- A CLI CPU job returns its actual output and its endpoint is absent after stop;
  the pre-existing browser session remains available.
- Uncertain teardown prevents another allocation until cleanup is verified.
- Runtime and public activation have the applicable explicit authority and checks.

## Verification

See `knoxx/docs/verification/colab-mcp.md`,
`knoxx/scripts/verify-colab-mcp.sh`, and
`services/digitalocean/services/colab-mcp/README.md`. Python transport fixtures
are separate from actual Google/browser and local actor acceptance. Full backend
suite/build failures remain blocking; narrow adapter tests cannot replace them.

## Risks

An open Colab tab is required for notebook tools; the CLI path is independent.
Stealth must remain online for both paths. The single operator shares the selected
notebook between clients. Upstream reconnects may require restarting the dedicated bridge.
Clients that only offer OAuth need an OAuth adapter rather than anonymous access.
Public activation follows the Services promotion contract or an explicit operator
exception for the tested isolated candidate. Local Knoxx restart is approval-gated.
