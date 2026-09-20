# Operation Eta Mu Breakdown — planning and evidence pack

**Planning snapshot:** 2026-09-12. **Current status:** nine extracted/existing
children registered by PR #96; migration acceptance remains incomplete.
**Project destination:** `foresight/docs/migrations/eta-mu-breakdown/` (this directory).

Start with the [module acceptance register](acceptance.md) for current pins,
remaining proof, lifecycle corrections and the documentation update procedure
that applies when each module is officially accepted. The original planning
snapshot and reported execution claims are retained as historical evidence.

## Start here

| Artifact | Purpose |
|---|---|
| [Dependency analysis](dependency-analysis.md) | Pinned revisions, package relationships, exact edit inventory, bootstrap coordinate examples and complete 46-edge register |
| [Epic E1 — package extraction](epic-01-operation-eta-mu-breakdown.md) | 12 outcome stories, acceptance criteria, implementation tasks, failing fixtures, evidence and rollback; covers all 14 requested steps |
| [Epic E2 — document corpus](epic-02-rheos-document-corpus.md) | 12 outcome stories for Rheos document ownership, ingestion/Epiphany analysis, labels, currentness, deduplication, placement and safe mutation |
| [Classification contract](classification/README.md) | Closed JSON schema, trusted/untrusted boundary, prompt, synthetic input/output and admission requirements |
| [Revision manifest](data/revisions.json) | Donor main revisions, differing Foresight gitlinks, and actions explicitly not performed |
| [Sources](data/sources.json) | 45 revision-bound source locators plus official package-manager/Git/review-policy documentation references |
| [Relationships](data/relationships.json) | 32 graph nodes and 46 typed edges with source IDs and observed/reported status |
| [ND-EDN relationships](data/relationships.nd.edn) | One static relation record per line; not a Clio event ledger |
| [Unresolved scope](data/unresolved.json) | Remaining verification and decision boundaries |
| [Donor-retirement blockers](donor-retirement-blockers.md) | Exact-revision scan of the withdrawn eta-mu candidate pin: the live inbound references E1.11 must close before any donor directory is deleted |
| [Child gate evidence](child-gate-evidence.md) | One clean-clone execution of every child's declared build, test and lint gates, with the failures, the bootstrap gap and the tool-unavailable rows recorded as observed |
| [Validation report](checks/validation-report.json) | Results and exact scope of the local pack checks |

## Graphs

The two observed graphs have editable Mermaid and Graphviz sources plus rendered SVG/PNG previews:

- [Package/source dependencies](graphs/01-observed-package-dependencies.svg) — [Mermaid](graphs/01-observed-package-dependencies.mmd)
- [Runtime/configuration/contract relations](graphs/02-runtime-and-contract-relationships.svg) — [Mermaid](graphs/02-runtime-and-contract-relationships.mmd)

The three additional Mermaid diagrams describe **proposed** workflows and ownership:

- [Migration sequence](graphs/03-proposed-migration-stages.mmd)
- [Document architecture](graphs/04-proposed-document-architecture.mmd)
- [Document lifecycle](graphs/05-proposed-document-lifecycle.mmd)

The arrow direction in the observed graphs is consumer to dependency/related service. A source-path edge, an npm dependency, and a runtime adapter are deliberately different types. Isolated nodes in a selected view do not prove global independence. Graphviz rendered the previews; Mermaid source was generated but not rendered by a Mermaid engine here.

## Source and certainty boundaries

Inspection used pinned mainline manifests, build descriptors, selected source/configuration files, and README-reported relationships. Source IDs in the prose and relation register resolve in `data/sources.json`; they are not newly created GitHub issues. This is not an exhaustive source-namespace/import/CI/deployment closure or a claim that any target builds independently today. E1.01 requires the full closure proof before migration decisions that depend on it, especially donor deletion.

During the original September 12 planning run, no repository package install, build, test suite, coverage run, live integration, browser run, repository creation, push, submodule change, branch-policy change, document classification or corpus deletion was executed. The source graph includes declarations and configuration, not dynamic runtime proof. No source-volume percentage was computed. Local generated artifacts have SHA-256 checksums; there was no Clio schema admission or remote persistence.

## Local verification

`python checks/check_pack.py` runs the pack's own checks. It requires jsonschema and networkx, does not install anything, and performs no network or repository operations. The current report records the check count and outcomes, covering graph/source references, the expected mixed dependency cycle, scope separation, JSON structure/context and negative model-output fixtures, source fixture digest, epic story IDs, request-step coverage, SVG structure and ND-EDN line framing. Passing these checks does not prove the correctness of the architectural inferences or the future migration.

`build_data.py` rebuilds **static recorded observations** and Graphviz previews; it is not a repository scanner. `classification/build_contract.py` rebuilds the proposed schema and synthetic fixtures; it does not invoke a model. `checks/initial-check.log` records the initial missing-fixture check, not a failing product test.

## Proposed naming and decisions

**Osmos** is registered as `open-hax/osmos` for the extracted Knoxx ingestion service. Standalone acceptance remains pending. Preserve its current `kms-ingestion` namespace/API/configuration identity in the initial transplant. Existing `open-hax/axxium` requires reconciliation, not recreation or overwrite. All nine candidate pins were cloned during PR reconciliation; their remaining acceptance evidence is listed in the register.

The epics intentionally leave product feature refactors, Sol's event-ledger-to-Clio cutover, broad authentication changes, and full Obsidian-style feature parity outside this mechanical migration. They preserve room for Rheos's document-centered evolution without making it a prerequisite for separating the current products.
