---
category: "kanban"
labels: ["shape", "representation", "katamorph", "clio", "eta-mu", "rheos"]
type: "epic"
title: "EPIC: One semantic shape, many lawful representations"
priority: "P1"
status: "incoming"
uuid: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
created_at: "2026-10-07T21:16:00Z"
---

# EPIC: One semantic shape, many lawful representations

## Outcome

Foresight defines semantic/domain shapes once and can lawfully represent the
same value as Clojure/EDN, YAML, Markdown frontmatter, JSON, BSON/MongoDB
documents, SQL rows, and future storage or transport forms without creating a
parallel domain model for each representation.

EDN remains the preferred durable human/agent-authored form for these contracts
and records, and durable EDN belongs under `.ημ/`. That location is a repository
convention, not semantic identity.

The canonical thing is the semantic shape and its laws:

```text
DomainShape + Laws
```

Representation-specific morphisms follow the same general model as
`clj->js` / `js->clj`:

```clojure
(clj->yaml value WorkItem)
(yaml->clj value WorkItem)

(clj->json value WorkItem)
(json->clj value WorkItem)

(clj->bson value WorkItem)
(bson->clj value WorkItem)

(clj->rows value WorkItem WorkItemRows)
(rows->clj rows WorkItem WorkItemRows)
```

## Core laws

For representation `R` and semantic shape `S`:

```text
decode(R,S, encode(R,S,x)) ≡S x
```

Semantic equality is not byte equality. Set ordering, map ordering, YAML
formatting, and unspecified SQL row ordering do not become domain semantics
merely because a representation exposes them.

A representation must preserve declared meaning or explicitly report an
incompatibility. Silent semantic loss is a defect.

## .ημ identity and causality

The filesystem is a useful projection for people and agents. It is not the
semantic graph.

Existing `.ημ/` layouts in eta-mu, Muse, Foresight, and other independently
evolved packages do not need to be normalized into one directory tree. The hard
boundary is identity and causality:

```text
identity(record) = UUID
edge(a,b) exists because a declared causal relation references UUID(b)
path(record) is organization/provenance, not identity
```

Clio remains authoritative for event admission, immutable ledger identity,
causal `:event/causes`, missing-cause/cycle checks, canonicalization, and
physical-partition invariance. This epic must not create another ledger engine.

## Children

1. Define representation laws and semantic equivalence.
2. Make `.ημ` a relocatable UUID and causal-graph substrate.
3. Define a self-describing portable value shape.
4. Define schema-directed YAML shapes.
5. Define JSON, BSON, and document-store shapes.
6. Define relational row shapes.
7. Make Rheos frontmatter a full YAML representation.
8. Define WorkItem, Persona, Participant, and Readiness shapes once.
9. Build the cross-representation conformance suite.
10. Migrate duplicate parsers and serializers to shared shapes.

The durable planning graph for these cards is recorded as EDN under
`.ημ/design/lawful-universal-shapes.edn`. Its paths are organizational; its
UUIDs and explicit causal/dependency references carry the relationships.

## Definition of done

At least one nontrivial domain object can be represented lawfully as EDN, YAML,
Markdown YAML frontmatter, JSON, BSON/MongoDB, and SQL rows; each representation
reconstructs the same semantic value under the same domain shape.

A recursive index can discover applicable `.ημ` records, build a UUID index,
and reconstruct their explicit causal graph without deriving identity or order
from filenames or directories.

## Non-goals

- Making EDN the only persistence format.
- Making YAML the canonical domain model.
- Inventing SQL schemas automatically for arbitrary values.
- Standardizing one mandatory `.ημ/` directory hierarchy.
- Treating path adjacency, lexical order, or file order as causal order.
- Creating a second event ledger authority beside Clio.
- Byte-for-byte preservation of representation formatting.
