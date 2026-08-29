# Workstream: Knoxx MCP authentication, dev ingress, and deployment lifecycle

Inventory date: 2026-08-29.

This note tracks one operational trust-boundary thread spanning Knoxx, Services, Epiphany, and Foresight. It does not promote deployment details into portable Foresight law.

## Signal

Knoxx now has an explicit authentication contract and a real MCP end-to-end test surface, while Services is opening authenticated access to long-lived dev instances and proposing a staging gate. These changes are related because they decide which identities, environments, and network paths are trusted before code reaches production.

## Primary PRs

- [open-hax/knoxx#224](https://github.com/open-hax/knoxx/pull/224) — contract-declared MCP authentication plus a CLJS end-to-end suite over the real Fastify serving path.
- [open-hax/knoxx#231](https://github.com/open-hax/knoxx/pull/231) — bootstrap credential rotation and separation of Knoxx API-key authentication from model-provider bearer credentials.
- [open-hax/services#58](https://github.com/open-hax/services/pull/58) — authenticated Caddy ingress for Knoxx dev, OpenCode dev, and Shadow dev; nREPL remains SSH-only.
- [open-hax/services#44](https://github.com/open-hax/services/pull/44) — proposed lifecycle environments and a staging-before-production gate.
- [octave-commons/epiphany#9](https://github.com/octave-commons/epiphany/pull/9) — revision-scoped authentication/MCP readiness evidence.
- [octave-commons/epiphany#14](https://github.com/octave-commons/epiphany/pull/14) — follow-up evidence after Knoxx moved MCP authentication methods into contract-declared policy.
- [open-hax/foresight#37](https://github.com/open-hax/foresight/pull/37) — records DigitalOcean as the sole forward production deployment authority.

## Trust-boundary shape

```text
operator / client identity
      -> declared authentication method
      -> runtime guards + credentials
      -> membership / authorization
      -> MCP tool exposure
      -> environment ingress
      -> staged deployment evidence
      -> production
```

Each transition should fail closed. An enabled method is permission to attempt the method, not proof that runtime guards are satisfied.

## Current blockers and decisions

1. **Services #58 has explicit pre-deploy blockers.** Production environment secrets `DEV_BASIC_AUTH_USER` and `DEV_BASIC_AUTH_HASH` must exist, and DNS A records are still needed for `opencode-dev.promethean.rest` and `shadow-dev.promethean.rest`. The PR intentionally says not to add its deploy label before those exist.
2. **nREPL stays outside public ingress.** It has no native authentication or HTTP framing suitable for the Caddy auth layer, so SSH tunneling remains the trust boundary.
3. **Environment lifecycle is still a proposal.** Services #44 leaves database-per-phase, whether shared-host `dev` should exist, and review-stack teardown policy unresolved.
4. **MCP catalog correctness is now observable.** Knoxx #224 surfaced missing factories, absent tool annotations, and authorization coupling that were previously hidden by lack of a runnable end-to-end surface. Those findings should remain ratchets rather than being normalized as expected behavior.
5. **Credential classes stay separate.** Knoxx #231 explicitly separates bootstrap/local credentials, Knoxx API-key auth, and model-provider bearer credentials. Do not collapse them into one generic secret path.

## Exit criteria

This workstream is coherent when:

- MCP authentication methods are contract-declared, runtime-guarded, and exercised in CI through the production serving path;
- bootstrap identity rotation cannot leave stale active local credentials behind;
- dev HTTP surfaces are authenticated and network-restricted, while raw eval surfaces remain tunnel-only;
- a staging deployment relation is enforced before production rather than merely documented;
- the DigitalOcean chain is the only forward production deployment authority;
- environment-specific secrets and DNS are managed as explicit deployment prerequisites rather than implicit host state.

## Cross-links

Publication/evaluation/reconciliation is tracked separately. It consumes this operational substrate but has different semantic ownership and exit criteria.
