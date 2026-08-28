# Foresight Archaeology

Architecture archaeology is persisted as **Clio newline-delimited EDN ledgers** and composed through Katamorph resource references.

The convenient aggregate run shape is a projection. It is never a ledger file.

## Physical layout

Each archaeology run owns independent physical ledgers:

```text
.ημ/archaeology/
├── catalog.edn
├── schemas/
│   └── <clio-schema-root>.edn
├── resources/
│   └── <run-uuid>.edn
└── ledgers/
    └── <run-uuid>/
        ├── run.edn
        ├── findings.edn
        ├── actions.edn
        ├── evidence.edn
        └── relations.edn
```

Every `*.edn` under `ledgers/` is a Clio ledger: zero or more EDN event forms, **one complete event per line**. No ledger is represented by an EDN vector and no file persists several logical vectors inside one aggregate event.

A run resource is a Katamorph-style reference object. It contains no findings, actions, evidence collections, or materialized projection:

```clojure
{:resource/kind :archaeology/run
 :resource/id :archaeology.run/<uuid>
 :archaeology/run-id "<uuid>"
 :archaeology/schema
 {:catalog/path ".ημ/archaeology/catalog.edn"
  :history/path ".ημ/archaeology/schemas"}
 :archaeology/ledgers
 {:run       {:ledger/path ".../run.edn"
              :ledger/event-type :archaeology/run-recorded}
  :findings  {:ledger/path ".../findings.edn"
              :ledger/event-type :archaeology/finding-recorded}
  :actions   {:ledger/path ".../actions.edn"
              :ledger/event-type :archaeology/action-recorded}
  :evidence  {:ledger/path ".../evidence.edn"
              :ledger/event-type :archaeology/evidence-recorded}
  :relations {:ledger/path ".../relations.edn"
              :ledger/event-type :archaeology/relation-recorded}}
 :archaeology/projection :foresight.archaeology/run}
```

The five values under `:archaeology/ledgers` are references, not embedded ledgers.

## Why normalized ledgers

The old seed shape persisted several independently growing collections inside one event:

```clojure
{:archaeology/findings [...]
 :archaeology/actions [...]
 :evidence/refs [...]
 :archaeology/consumes [...]}
```

That makes unrelated append domains contend on one document and eventually turns an append-only history into repeated whole-file rewrites.

The normalized model records one fact per Clio event:

- `:archaeology/run-recorded` — run identity, topic, summary, and causal parent run events through Clio `:event/causes`.
- `:archaeology/finding-recorded` — one finding.
- `:archaeology/action-recorded` — one durable action.
- `:archaeology/evidence-recorded` — one immutable source reference.
- `:archaeology/relation-recorded` — one semantic edge, currently finding→evidence or run→parent-action continuity.

Finding evidence vectors, action vectors, evidence vectors, parent vectors, and continuity vectors exist only in the derived projection.

## Clio authority

Clio owns the event-sourcing mechanics:

- newline-delimited EDN physical ledgers;
- immutable event identity;
- stream revision conflicts;
- causal `:event/causes`;
- missing-cause and cycle checks;
- arbitrary physical partition invariance;
- content-derived Malli schema revisions;
- deterministic canonicalization;
- pure projection folds.

The archaeology package does **not** implement another filesystem ledger. NBB/Node callers use `clio.infra.ledger/read-ledger` and `clio.infra.ledger/append-event!`; schema history uses Clio's schema store.

## Portable package

`archaeology/src/foresight/archaeology/` follows the same construction discipline as Clio:

```text
law.cljc -> shape.cljc -> domain.cljc -> infra.cljc
```

- `law.cljc` — archaeology invariants layered on Clio event laws.
- `shape.cljc` — Malli data schemas, Clio event catalog, resource shape, derived projection shape.
- `domain.cljc` — Clio canonicalization plus the pure run projection.
- `infra.cljc` — host-neutral orchestration with file operations injected; callers supply Clio's runtime adapters.

The infra namespace remains CLJC by taking `read-ledger` and revision data as dependencies rather than importing Node or JVM APIs.

## Causal archaeology

The **logical history** is the union of ledger files referenced by archaeology run resources. Files and PRs have no semantic ordering authority.

A run event's `:event/causes` names its parent run event(s). A focused investigation may therefore fork from any prior run, and a later run may join multiple parent runs.

A non-root run must also record one `:run/consumes` relation for each parent. That relation names the parent run and the parent action being continued, consumed, superseded, rejected, or acknowledged. Parentage therefore means causal work continuity, not merely "this happened later."

## Projection

The familiar aggregate is reconstructed from canonical history:

```clojure
{:event/id "<run uuid>"
 :event/parents ["<parent run event>" ...]
 :archaeology/topic "..."
 :archaeology/summary "..."
 :archaeology/consumes [...]
 :archaeology/findings [...]
 :archaeology/actions [...]
 :evidence/refs [...]}
```

Those vectors are disposable derived state. Delete the projection and reconstruct it from the ledgers.

## Recurring run

Each archaeology execution:

1. discovers `:archaeology/run` resource files;
2. loads their referenced ledgers with Clio;
3. loads historical schema revisions;
4. canonicalizes the union and reconstructs the causal DAG;
5. chooses or joins current run heads;
6. reads parent action projections;
7. mines a fresh concrete code cluster;
8. appends new run/finding/action/evidence/relation events to fresh per-run ledgers;
9. writes one new Katamorph resource referencing those ledgers;
10. opens a Foresight PR and does not merge it.

Corrections are new events. Existing ledger lines are never rewritten after they become causal history.

## Initial vocabulary

The seed run establishes provisional names only; future runs evolve them through events.

- `Cell<T>` / `Slot<T>`
- `Registry<K,V>`
- `BoundedCache<K,V>`
- `SeenSet<Id>`
- `BoundedLog<T>` / `RingBuffer<T>`
- `DAG<N>`
- `CausalEventDAG<Event>`
- `LedgerPartition<Event>`
- `Projection<S,E>`
- `Dispatch`
- `Reconciler<Desired,Observed,Plan>`
- `Driver` / `Adapter`
- `Resource` + `Facet`

The objective is semantic compression of Knoxx without making the production runtime carry archaeology machinery.
