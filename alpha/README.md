# Alpha (α)

Alpha is Foresight's portable structural-integrity layer.

It answers deterministic questions such as:

- Is this artifact shaped lawfully?
- Does this reference have stable identity?
- Is this relation explicit and attributable?
- Is this event structurally usable?
- Is this reaction declarative rather than hidden executable code?

Alpha does **not** decide whether a finding is true, a translation is good, a
review is persuasive, or an action is authorized. Those belong to other laws,
Mu evaluation, and runtime policy.

## Current kernel

`alpha.law.artifact` defines portable `.cljc` shapes for:

- `Ref` / `ArtifactRef`
- `Relation`
- parsed `MarkdownDocument`
- `DiagramSource`
- `Artifact`
- declarative `Condition`
- `Event`
- `OperationRef`
- `Reaction`

Maps are open where extension data is expected. A Calliope review, Epiphany
finding, and Rheos story can therefore share the base Artifact law while layering
stricter kind-specific contracts elsewhere.

## Document observations and admission

`alpha.law.document` adds version-one contracts for source-bearing document
maps. A document need not be a file or carry Git fields, frontmatter, a task
UUID, or workflow status.

- An observation names its own identity, a source `Ref`, observation time,
  full/partial coverage, portable input, retention and source capabilities.
  A source revision and content digest are optional and separate from the
  observation identity. A query result is not an identity for its returned
  entities. Query/selection context can be retained as portable data.
- A document names the supplied observation and source, carries a portable map
  representation, and records an assembly ID/version plus input/output schema
  kinds from the caller's registry. The caller owns versioning and provenance
  of that registry; the signature contains no executable assembly code.
- `admit-document` checks both shapes, matching observation/source context, and
  both declared schema boundaries through Katamorph. It returns the supplied
  maps unchanged; it does not parse, transform or store them.

The tests qualify a source-owned Markdown map and an ephemeral, partial GitHub
query map without file fields or an immutable revision. Capability declarations
use `:read`, `:refresh` and `:write`; retention declarations use `:ephemeral`,
`:source-owned` and `:retained`. These are explicit adapter declarations, not
proof that persistence, replay, refresh or writing works. A capability is not
authorization. Retaining a decision does not require retaining its payload.

Observation time is a supplied nonempty string, not a clock or freshness check.
Admission refuses a document bound to a different supplied observation or source
revision. It cannot establish that the supplied observation is current, that a
digest matches source bytes, or that the representation follows from its input.
Acquisition, assembly implementations, source availability, retention, guarded
writes and review decisions require separately qualified adapters and laws.

## Validation

```bash
cd alpha
clojure -M:test
```

The first semantic law beyond Malli shape validation is intentionally small: an
embedded relation must name its containing artifact as the relation source.

## Runtime boundary

This package contains semantic data, schemas, laws, and pure validation only.
Markdown parsing, filesystem access, GitHub events, workflow execution, and
rendering belong in adapters/runtimes outside this kernel.

Katamorph remains the reusable contract machinery between systems. Alpha's
schemas are ordinary Malli data and are intended to be registered/consumed
through Katamorph as that integration is generalized.
