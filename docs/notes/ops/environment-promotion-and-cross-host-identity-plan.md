# Environment promotion and cross-host identity: plan, observations and promotion gate

> Split out of `open-hax/knoxx` `docs/verification/environment-and-cross-host-acceptance.md` (lines 3–33 and 75–84) at knoxx `0fdaae13` on 2026-09-30. Primary relationship: services, axxium, Promethean hosts. Why it left Knoxx: [knoxx-documentation-extraction.md](../../lineage/knoxx-documentation-extraction.md).

## Requested contract

- Stealth is this machine (`192.168.12.128` observed); Yoga is `192.168.12.68`.
- Service URLs follow `<env>.<service-name>.promethean.rest`.
- An authorized code owner can label a PR targeting `main` with `testing` to claim its service's testing slot.
- A competing live testing claim blocks takeover for two hours. The working interpretation is a two-hour incumbent lease; confirmation was requested.
- A successfully merged main revision deploys to staging, not directly to production.
- Production promotion requires integration, end-to-end, and several batches of hundreds of actual source mutations, all tied to the same immutable revision/artifact.
- Deploy first on Stealth, then Yoga. Register/copy a user identity across the two Axxium instances in a browser.
- Stop only the original identity-provider service after registration; verify fresh authentication and Knoxx content-management, review, and translation workflows on the second machine without dependence on that issuer.
- Capture browser screenshots of actual actions and resulting state. Do not substitute API-only or simulated screenshots.

## Execution phases

1. Inventory current source, runtime paths, deployment triggers, identity capabilities, and existing test harnesses.
2. Target behavior, not verified active enforcement: implement code-owner testing-slot admission, main-to-staging delivery, immutable production qualification, and shared environment instructions.
3. Implement/repair Axxium portable identity and the Knoxx authentication boundary; validate real integration and negative cases.
4. Deploy isolated service stacks on Stealth then Yoga, with persistent data and HTTPS ingress.
5. Run the browser acceptance flow, stop the original Axxium service, authenticate anew and complete content/review/translation workflows; capture evidence.
6. Run production qualification, save honest outcomes and source changes, and update reusable skills.

## Initial observations

- Both machines are reachable over SSH/LAN; Yoga's existing Axxium submodule git metadata is broken. Use an isolated deployed checkout rather than repairing unrelated workspace state.
- Neither machine currently has a running Axxium container.
- Current upstream Axxium has signup/login APIs and a static landing page; it has no implemented identity export/import flow or registration UI.
- Knoxx has existing CMS, publication, review and translation browser verifiers, and a source-mutation harness to extend/reuse.
- Stealth's Tailscale client is logged out; public ingress needs an existing authenticated path or a service-specific SSH tunnel without requiring a new login.

This file records acceptance criteria and progress, not a completion claim.


<!-- lines 75–84 of the Knoxx page -->

## Promotion and validation

The target Services workflow is intended to own code-owner testing admission
and exact-main-merge staging. This extracted plan does not prove those gates are
active; activation requires the current repository policy and revision-bound
workflow/deployment evidence. In the proposed behavior, a newer main commit
supersedes an older queued staging candidate.
CODEOWNERS initially names the three existing repository administrators. App
callers activate when reviewed changes reach main. Production qualification
requires a merged immutable commit, integration and real e2e checks, then four
non-overlapping batches of at least 250 source mutations. Only completed tests
with assertions can kill a mutant. Invalid compilation, timeouts, survivors,
duplicates and stale evidence fail the gate.
