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
