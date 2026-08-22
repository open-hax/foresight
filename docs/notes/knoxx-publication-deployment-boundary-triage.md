---
title: "Knoxx publication and deployment boundary triage"
summary: "Records the current split between Knoxx-owned publication semantics and services-owned production deployment, then records the operator adjudication that the DigitalOcean stack is the only forward production path and classifies the remaining legacy Promethean behavior for retirement."
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

2. `open-hax/services` owns the current DigitalOcean production stack contract.
   - `.github/workflows/deploy-stack-chain.yml` builds and deploys `proxx -> knoxx -> caddy` through `deploy-digitalocean.yml`.
   - Its Knoxx step explicitly treats hosted OpenPlanner REST as absent from the stack, retaining only the Gardens sentinel dependency.
   - `services/ROADMAP.md` describes the repository slice as host contract, image build, deploy order, and health gates rather than application behavior.

3. Knoxx still ships `.github/workflows/deploy-production.yml` on `main`.
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
- `open-hax/knoxx/.github/workflows/deploy-production.yml` and the `services/deploy-promethean.yml` Knoxx path are legacy operational surfaces unless retained only as bounded migration/compatibility machinery.
- Future synthesis should not describe the two paths as unresolved peers.
- Remaining work is migration/retirement verification: identify any still-required behavior on the legacy path, move it into the DigitalOcean path where necessary, then disable/remove the legacy trigger without losing required deploy evidence or recovery behavior.

The earlier observation and interpretation remain above as historical provenance showing what was known before operator adjudication.

## Legacy behavior trace — 2026-08-22

The Promethean Knoxx deploy path was compared directly with the current DigitalOcean service and stack contracts.

### Behavior already superseded by the DigitalOcean path

- **Source transfer and host-side builds.** The legacy script rsyncs the Knoxx source tree to the host, installs backend dependencies there, runs `shadow-cljs compile server`, and builds containers on the host. DigitalOcean instead builds revision-bound images in CI and deploys explicit GHCR image references. The legacy behavior is not a migration requirement.
- **Services-owned contract delivery.** Both paths ship the services-owned Knoxx contract tree read-only. The DigitalOcean deploy additionally tracks contract changes as service-definition changes so the container is recreated when a contract is added, changed, or withdrawn. No legacy-only behavior needs preservation here.
- **Sandbox Docker capability.** The legacy path mounts `/var/run/docker.sock` into Knoxx, granting host-equivalent Docker authority. DigitalOcean replaces it with a project-private rootless nested Docker daemon and deliberately withholds the host socket. The legacy mechanism is obsolete rather than a capability to migrate.
- **SSH trust.** The legacy workflow uses `StrictHostKeyChecking accept-new` plus `ssh-keyscan`; DigitalOcean requires a committed pinned host key and `StrictHostKeyChecking yes`. The legacy behavior is weaker and should not be preserved.
- **Runtime credentials.** The legacy script reads the running Proxx container to recover `PROXY_AUTH_TOKEN`, mutates an OpenPlanner-owned environment file, and creates a Knoxx API key if none exists. DigitalOcean renders a declared service environment from provisioned secrets/variables and refuses blank required credentials. Dynamic credential invention and cross-service env mutation are obsolete mechanisms, not migration requirements.
- **OpenPlanner host coupling.** The legacy Knoxx compose override is driven from the OpenPlanner service directory and its env file. DigitalOcean models Knoxx directly and explicitly treats OpenPlanner REST as absent except for the known Gardens compatibility seam. The legacy coupling is migration archaeology.
- **Production health.** The legacy script waits only for the backend container health check. DigitalOcean composes Proxx before Knoxx, deploys backend/frontend/devtools together, preserves the service contract and runtime/state roots, and runs a service-owned post-deploy verification gate. The forward path already has the stronger production verification surface.

### Legacy-only capability that is not a forward production requirement

The legacy reusable workflow also supports a distinct `staging` environment and host layout. That capability is real implementation evidence, but the operator decision here is specifically that only the DigitalOcean path moves forward. If a staging environment is still desired, it should be expressed as a DigitalOcean host/environment contract rather than keeping the Promethean deployment mechanism alive.

### Retirement classification

No unique **production** behavior was found that requires preserving the legacy Knoxx Promethean deploy path as an active executable route.

The remaining surfaces classify as:

- `knoxx/.github/workflows/deploy-production.yml` — **obsolete active trigger**; retirement candidate because every main push can still invoke the rejected production path.
- `services/.github/workflows/deploy-promethean.yml` Knoxx branch — **compatibility/legacy implementation**; retain only if another evidenced caller still requires it during migration, otherwise remove or narrow it separately.
- `services/promethean/scripts/deploy-knoxx.sh` — **legacy implementation evidence**; contains no forward production capability that should be copied as-is into DigitalOcean.
- Promethean staging support — **unmigrated environment capability**, not justification for retaining the legacy production path.

This classification is about the Knoxx deployment seam only; it does not establish that every other service branch in `deploy-promethean.yml` is unused.

## Foresight lift candidates

The following are candidates for further comparison, not accepted lifts:

- semantic target declarations should remain portable data while runtime adapter construction stays at the effect boundary;
- publication artifacts should be validated at the effect boundary before a target can materialize them;
- desired state and deployment/runtime ownership should remain separately modeled, so an application-owned semantic target does not imply ownership of the production host lifecycle;
- environment capability should be modeled independently from a particular deployment mechanism, so retiring a deployment implementation does not silently erase the concept of staging/review/production environments.

Independent evidence from other surviving repositories is still required before generalizing these as common Foresight law.

## Not promoted

This note does **not**:

- make Knoxx's static-site manifest format a common Foresight contract;
- make the current filesystem locking/idempotency mechanism portable law;
- infer broader service or deployment ownership from merge status alone;
- claim every non-Knoxx branch of `deploy-promethean.yml` is obsolete;
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
- https://github.com/open-hax/services/blob/main/promethean/scripts/deploy-knoxx.sh
- https://github.com/open-hax/services/blob/main/.github/workflows/deploy-stack-chain.yml
- https://github.com/open-hax/services/blob/main/.github/workflows/deploy-digitalocean.yml
- https://github.com/open-hax/services/blob/main/digitalocean/services/knoxx/compose.yaml
- https://github.com/open-hax/services/blob/main/ROADMAP.md

## Next evidence pass

Prepare the smallest reversible retirement change that prevents Knoxx `main` from invoking the legacy Promethean production deployment. Keep the broader `deploy-promethean.yml` cleanup separate until its other service callers are inventoried.
