---
title: "Knoxx publication and deployment boundary triage"
summary: "Records the current split between Knoxx-owned publication semantics and services-owned production deployment, then records the operator adjudication that the DigitalOcean stack is the only forward production path."
category: "architecture"
created: "2026-08-22"
status: "triage"
---

# Knoxx publication and deployment boundary triage

## Scope

This note is revision-scoped synthesis. It records current implementation and deployment evidence without promoting any repository-local mechanism into accepted Foresight law.

## Observations

1. Knoxx has continued moving publication semantics into explicit portable law and adapter boundaries on `main`.
   - `open-hax/knoxx#247` merged the legacy garden/publication-state migration into resources.
   - `open-hax/knoxx#248` merged `PublicationArtifact` validation at the publication effect boundary.
   - `open-hax/knoxx#250` merged garden locale catalogs plus artifact/intent locale consistency checks.
   - `open-hax/knoxx#251` merged publication target declarations as portable law data, with runtime adapter factories remaining in infra.
   - `open-hax/knoxx#252` merged a static-site publication target whose manifest contract is `.cljc` while filesystem locking, rename, and idempotency-store mechanics remain runtime-specific.

2. `open-hax/services` still owns the current DigitalOcean production stack contract.
   - `.github/workflows/deploy-stack-chain.yml` builds and deploys `proxx -> knoxx -> caddy` through `deploy-digitalocean.yml`.
   - Its Knoxx step explicitly treats hosted OpenPlanner REST as absent from the stack, retaining only the Gardens sentinel dependency.
   - `services/ROADMAP.md` describes the repository slice as host contract, image build, deploy order, and health gates rather than application behavior.

3. Knoxx also still ships `.github/workflows/deploy-production.yml` on `main`.
   - A push to Knoxx `main` runs application preflight and then calls `open-hax/services/.github/workflows/deploy-promethean.yml@main` with `service: knoxx` and `environment: production`.
   - In the current services workflow, the Knoxx deployment step resolves `PROMETHEAN_SSH_HOST` from the repository/environment variable or falls back to `proxx.promethean.rest`.
   - This is a different production mechanism from the DigitalOcean image/stack chain.

4. The deployment split was explicitly noticed in merged `open-hax/knoxx#247`: its PR evidence states that the Knoxx push-triggered production workflow targets the legacy Promethean path while the public Knoxx host is served from the DigitalOcean stack.

## Interpretation at observation time

The publication domain boundary was becoming clearer while the deployment authority boundary remained duplicated.

The evidence supported:

- Knoxx owns publication intent, artifact, locale, reconciliation, receipt, and target-selection semantics that live inside the application boundary.
- Services owns production host composition, image deployment, ordering, ingress, and post-deploy verification for the DigitalOcean stack.
- Knoxx's direct production workflow was therefore not evidence of a second accepted deployment owner; it was an unresolved operational path that could independently act on merges.

## Contradiction observed before adjudication

Two executable production paths existed for Knoxx:

```text
Knoxx main push
  -> knoxx/.github/workflows/deploy-production.yml
  -> services/deploy-promethean.yml
  -> legacy SSH host resolution (fallback proxx.promethean.rest)

Services production stack
  -> services/deploy-stack-chain.yml
  -> build-images.yml
  -> deploy-digitalocean.yml
  -> DigitalOcean Knoxx service + Caddy
```

This was actionable drift because application merges could trigger a deployment path that was not the stack contract currently used to serve Knoxx.

## Operator adjudication — 2026-08-22

The operator explicitly resolved the deployment-authority ambiguity:

> Only the DigitalOcean path is moving forward.

This is an accepted ownership/direction decision for the deployment seam, not an inference from merge status or implementation shape.

Consequences for current triage:

- `open-hax/services` DigitalOcean stack deployment is the accepted forward production path for Knoxx.
- `open-hax/knoxx/.github/workflows/deploy-production.yml` and the `services/deploy-promethean.yml` Knoxx path are legacy operational surfaces unless they are retained only as bounded migration/compatibility machinery.
- Future synthesis should not describe the two paths as unresolved peers.
- Remaining work is migration/retirement verification: identify any still-required behavior on the legacy path, move it into the DigitalOcean path where necessary, then disable/remove the legacy trigger without losing required deploy evidence or recovery behavior.

The earlier observation and interpretation remain above as historical provenance showing what was known before operator adjudication.

## Foresight lift candidates

The following are candidates for further comparison, not accepted lifts:

- semantic target declarations should remain portable data while runtime adapter construction stays at the effect boundary;
- publication artifacts should be validated at the effect boundary before a target can materialize them;
- desired state and deployment/runtime ownership should remain separately modeled, so an application-owned semantic target does not imply ownership of the production host lifecycle.

Independent evidence from other surviving repositories is still required before generalizing these as common Foresight law.

## Not promoted

This note does **not**:

- make Knoxx's static-site manifest format a common Foresight contract;
- make the current filesystem locking/idempotency mechanism portable law;
- infer broader service or deployment ownership from merge status alone;
- change the accepted Clio/event-ledger ownership decision.

The DigitalOcean-only forward deployment direction is recorded because it was explicitly accepted by the operator.

## Sources

- https://github.com/open-hax/knoxx/pull/247
- https://github.com/open-hax/knoxx/pull/248
- https://github.com/open-hax/knoxx/pull/250
- https://github.com/open-hax/knoxx/pull/251
- https://github.com/open-hax/knoxx/pull/252
- https://github.com/open-hax/knoxx/blob/main/.github/workflows/deploy-production.yml
- https://github.com/open-hax/services/blob/main/.github/workflows/deploy-promethean.yml
- https://github.com/open-hax/services/blob/main/.github/workflows/deploy-stack-chain.yml
- https://github.com/open-hax/services/blob/main/ROADMAP.md

## Next evidence pass

Trace behavior unique to the legacy Promethean deployment path, if any. Classify each dependency as required migration behavior, compatibility-only behavior, or obsolete machinery; then prepare the smallest reversible change that leaves the DigitalOcean stack as the sole active production deployment path.
