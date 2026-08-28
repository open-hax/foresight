# Foresight Archaeology Ledger

This directory is the durable history for recurring architecture archaeology across the Foresight superproject, with Knoxx as the primary pressure test.

The authority is a **causal event DAG**, not a mutable report. Each archaeology run appends one immutable event under `events/`. Events name the event(s) they build on, the prior actions they consume or acknowledge, the new findings and actions produced, and exact source evidence.

## Why a DAG

Recurring archaeology and dedicated conversations can proceed concurrently. A run can continue the main line, a focused conversation can branch from any prior event, and a later run can join multiple branches by naming multiple parents. No physical file order is authoritative.

PRs are physical ledger partitions. The logical archaeology history is the union of archaeology event files, reconstructed from `:event/parents`. A PR may therefore reference a parent event that is still present only in another open archaeology PR; that is a temporarily unmerged partition, not a new history.

## Event shape

```clojure
{:event/id "uuid"
 :event/type :archaeology/observation
 :event/at "RFC-3339 timestamp"
 :event/parents ["parent-event-uuid" ...]

 :archaeology/topic "short topic"
 :archaeology/summary "what this run established"

 :archaeology/consumes
 [{:event/id "parent-event-uuid"
   :action/id :parent/action-id
   :relation :continues}]

 :archaeology/findings
 [{:finding/id :stable-id
   :shape :registry
   :current/name "domain-specific-name"
   :candidate/name "common-name"
   :claim "grounded architectural claim"
   :evidence [0 1]}]

 :archaeology/actions
 [{:action/id :this-run/action-id
   :action/type :document
   :status :done
   :description "durable action produced by this event"}]

 :evidence/refs
 [{:repo "open-hax/knoxx"
   :path "path/in/repo.cljs"
   :commit "git-sha-or-blob-sha"
   :url "https://github.com/..."}]}
```

## Laws

1. **Append only.** Once an event is merged or cited by another event, do not rewrite it. Corrections are new events that cite the event being corrected.
2. **Explicit causality.** Every non-root event names one or more `:event/parents`.
3. **Action continuity.** Every non-root event must consume, continue, supersede, reject, or explicitly acknowledge at least one action from each parent. Parentage must mean more than chronology.
4. **Branching is normal.** Dedicated investigations may branch from any existing event. They do not need to wait for the recurring main line.
5. **Joining is explicit.** A synthesis event names every branch it joins as a parent and records which actions/findings it carries forward.
6. **Evidence before abstraction.** Findings cite exact repository paths and immutable revisions when available. A common shape is inferred only after inspecting concrete implementations.
7. **Generic shape != protocol.** Pure persistent data and algorithms remain data/functions. Protocols describe capability or implementation boundaries; multimethods describe intentional open-world semantic dispatch.
8. **PR per run.** Each recurring archaeology run creates a fresh branch and PR containing its new event plus any derived documentation/projection updates. It must not merge its own PR.
9. **Knoxx stays runnable.** Archaeology PRs are documentation/ontology-first unless a separately scoped implementation task explicitly authorizes code extraction.

## Recurring run algorithm

1. Inspect recent archaeology events and open archaeology PRs; reconstruct the current DAG heads.
2. Pick a head (normally the latest main-line event) or explicitly join heads.
3. Read every parent event and its `:archaeology/actions`.
4. Mine a fresh cluster of Foresight/Knoxx code for repeated or misleadingly domain-specific structures.
5. Classify each candidate as one of: `law`, `shape`, `domain-algorithm`, `protocol`, `multimethod/dispatch`, or `infra`.
6. Compare against already named common shapes before inventing another term.
7. Create exactly one new immutable event file with parent/action continuity and source citations.
8. Update derived archaeology documentation only when the new event changes the vocabulary or decomposition map.
9. Open a PR. The PR body must list the new event id, parent event ids, consumed parent actions, findings, and evidence links.

## Initial common vocabulary

The vocabulary is provisional and evolves through ledger events rather than edits to history.

- `Cell<T>` / `Slot<T>` — one replaceable runtime binding.
- `Registry<K,V>` — keyed registration and lookup.
- `BoundedCache<K,V>` — keyed values with capacity/expiration policy.
- `SeenSet<Id>` — idempotence/deduplication memory.
- `BoundedLog<T>` / `RingBuffer<T>` — recent finite history.
- `DAG<N>` — nodes plus directed acyclic parent/child relation.
- `CausalEventDAG<Event>` — events whose explicit causal references form a DAG.
- `LedgerPartition<Event>` — one physical subset of the logical event set.
- `Projection<S,E>` — reconstructable state derived by folding canonical history.
- `Registry<K,Descriptor>` + `Dispatch` — named executable behavior with metadata.
- `Reconciler<Desired,Observed,Plan>` — compare states and produce convergence work.
- `Driver` / `Adapter` — boundary implementation connecting an external system to a stable capability.
- `Resource` + `Facet` — one declarative entry interpreted through multiple orthogonal kind-specific views.

The purpose of this ledger is semantic compression: make Knoxx increasingly describable as composition of a small stable vocabulary instead of an accumulation of domain-named mini-frameworks.
