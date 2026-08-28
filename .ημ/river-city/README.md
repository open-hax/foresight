# Foresight River City

River City is the external-signal sibling of Foresight archaeology.

Both use the same substrate:

```text
Katamorph resource -> referenced Clio partitions -> canonical history -> pure domain projection
```

They differ only in domain semantics and source adapters:

- `.ημ/archaeology` records runs, code findings, actions, evidence, and relations mined from repository history.
- `.ημ/river-city` records source-backed geopolitical, maritime, energy, defense, infrastructure, and AI-economic observations.

## Ownership

- **Clio** owns event envelopes, content-derived schema revisions, append admission, OS-backed ledger locking, physical partition invariance, causal/stream validation, and canonical replay order.
- **Katamorph** supplies the namespace/resource composition shape used to discover the ledgers.
- **River City** owns observation normalization and domain projections such as PortWatch current-record selection and later pressure/latent models.
- **Foresight** owns this host composition and the background jobs because its workspace pins all three repositories together.

No Foresight or River City code may grow a second filesystem/event-sourcing implementation beside Clio.

## Layout

```text
.ημ/river-city/
├── catalog.edn
├── schemas/
├── resources/
│   └── imf-portwatch.edn
├── ledgers/
│   └── imf-portwatch.edn
└── projections/
    └── maritime.edn
```

The ledger is newline-delimited EDN: one complete Clio event per nonblank line. Resource manifests are reference/index documents, not accumulating stores. Projections are disposable derived state.

## PortWatch correction model

Each upstream PortWatch `ObjectId` is one Clio stream. The first observed value is sequence 1. If the same upstream record later changes, Foresight appends sequence N+1 with the previous stream event in `:event/causes`. If the payload is unchanged, collection is a no-op.

River City's projection retains every historical event id as evidence while emitting only the highest stream revision as the current row. Clio remains responsible for rejecting gaps, competing stream slots, missing causes, cycles, invalid historical schemas, and duplicate-id corruption.

## Mutation policy

Scheduled jobs may append valid observations, materialize schema-history snapshots, and rebuild projections. They may commit only generated `.ημ/river-city` state.

Schemas, resource laws, source adapters, normalization, scoring, and projections are executable policy. Changes to those belong in reviewed PRs.
