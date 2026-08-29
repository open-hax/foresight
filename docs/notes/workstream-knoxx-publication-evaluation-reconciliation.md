# Workstream: Knoxx publication, evaluation, and reconciliation

Inventory date: 2026-08-29.

This note is a coordination surface, not a promotion of child-repository implementation into Foresight law. Child repositories retain local authority; the point here is to keep one cross-repository thread navigable while its pieces land independently.

## Signal

The active thread is converging on four separable capabilities already named in Knoxx: repository/resource semantics, transduction, evaluation/review, and representation/publication. Publication is now materially coupled to revision-bound review evidence and desired-vs-observed reconciliation, so these PRs should be reviewed as one workstream even when they merge independently.

## Primary PRs

- [open-hax/knoxx#238](https://github.com/open-hax/knoxx/pull/238) — board split into repository, transduction, evaluation, and representation capability owners. This is the architectural routing map.
- [open-hax/knoxx#265](https://github.com/open-hax/knoxx/pull/265) — publication convergence now treats title as materialized state, distinguishes converged no-op from refused publication, and adds sequential garden-wide publication.
- [open-hax/foresight#38](https://github.com/open-hax/foresight/pull/38) — records revision-bound translation/review/reconciliation evidence and identifies durable receipt persistence as an unresolved runtime follow-up.
- [open-hax/foresight#37](https://github.com/open-hax/foresight/pull/37) — records DigitalOcean as the forward deployment authority and the old Promethean deploy path as migration/retirement surface.
- [open-hax/services#44](https://github.com/open-hax/services/pull/44) — proposes lifecycle environments and a staging-before-production gate; this is the deployment-policy edge of the same publication path.

## Dependency shape

```text
resource repository
      -> transduction candidate + provenance
      -> evaluation judgment bound to exact revision
      -> publication intent
      -> desired materialization
      -> observe current target
      -> pure convergence plan
      -> effect
      -> durable execution receipt
```

The arrows are composition seams, not a claim that every workflow must be linear.

## Current pressure points

1. **Receipt durability.** Foresight #38 records the current Knoxx publication receipt journal as process-local and bounded. Once reconciliation is a production caller, successful effects need a durable fact source if they are to remain audit evidence rather than transient telemetry.
2. **Convergence vocabulary.** Knoxx #265 makes title part of materialized state and exposes the difference between a true converged `:noop` and a planner refusal. Future materialized fields must be added through one shared desired/observed shape or drift detection will split again.
3. **Evaluation ownership.** Knoxx #238 moves SME translation review under generic evaluation semantics. The useful lift candidate is revision-bound judgment/currentness, not translation-specific UI or provider behavior.
4. **Deployment authority.** Foresight #37 says only the DigitalOcean path moves forward. Services #44 should therefore be interpreted as strengthening that path, not creating a peer deployment authority.

## Exit criteria

This workstream can collapse back into normal repository-local maintenance when:

- Knoxx has one explicit durable publication/reconciliation receipt path;
- review judgments bind to immutable work products and currentness is derived rather than overwritten;
- repository/transduction/evaluation/representation ownership is reflected in active board/runtime boundaries;
- production publication uses the DigitalOcean deployment chain without an unverified legacy peer path;
- desired and observed materialization share one extensible contract so new fields cannot become unreachable drift.

## Cross-links

The authentication/MCP/dev-environment work is tracked separately because it is an operational trust-boundary thread, even though it gates the same deployed Knoxx runtime.
