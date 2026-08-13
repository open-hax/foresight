# Workflow diagrams as code

The `.mmd` files in this directory are canonical Mermaid source artifacts.
Generated SVG/PNG images are projections and must not become the authority.

The diagrams are architectural rather than runtime-specific. They should evolve
with the `.cljc` laws and may eventually be parsed into Alpha `DiagramSource`
artifacts and related to workflows through typed relations.

Current diagrams:

- `alpha-eta-mu-pi.mmd` — the loose α → η → μ → Π responsibility spine.
- `artifact-reactive-flow.mmd` — generalized document/event/reaction workflow.
- `epiphany-document-flow.mmd` — Epiphany methodology as a document graph rather
  than a mandatory linear pipeline.
