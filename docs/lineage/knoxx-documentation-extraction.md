---
title: "Knoxx documentation extraction"
summary: "Records which Knoxx markdown moved up into Foresight on 2026-09-30, which stayed, and the external relationships behind each decision; lists Knoxx cards that belong to other boards without moving them."
category: "lineage"
created: "2026-09-30"
---

# Knoxx documentation extraction

On 2026-09-30 the Knoxx submodule (`open-hax/knoxx`, audited at `origin/main`
`0fdaae13` on branch `docs/align-docs-with-code`) had its ~450 markdown files
audited against its code. Two things came out of that:

1. Docs that describe Knoxx were corrected in place so they match the code as it
   is (Knoxx side of the change).
2. Docs whose subject is **not Knoxx** were moved or split up into this
   repository. This page is the record of why.

Every extracted file carries a provenance banner naming its original Knoxx path,
the Knoxx commit, and its primary relationship. Knoxx keeps a pointer where
something still referenced the old path.

## The rule used

A document stays in Knoxx when its subject is Knoxx's own code, product, runtime,
contracts, or history — even if it mentions other systems as providers,
dependencies, or stores. It leaves when Knoxx is only the place it happened to be
written, a consumer of the thing described, or one example among several.

Knoxx's own position makes this sharper than usual. Its `ROADMAP.md` calls it
"a later application composition using stable upstream parts" that "does not
own the upstream seams while they are still moving". Material describing those
upstream seams therefore does not belong in Knoxx, even when Knoxx was their
first proving ground.

Historical documents about Knoxx's *own* retired designs (roughly 66 of them:
Postgres/Redis storage, the pre-`domain/infra/shape/law/extern` layout, the
OpenPlanner-hosted era) were **kept in Knoxx** with dated status banners. They are
Knoxx history, not Foresight lineage.

## External relationships

Each Foresight concept below is paired with what Knoxx does with it and with
what was extracted because of it.

| Concept / owner | Knoxx's relationship to it | What that meant for docs |
| --- | --- | --- |
| **Rheos** (board runtime, lawful card transitions) | Knoxx's board is *managed by* Rheos; Knoxx has no board UI or FSM of its own. | Board-UI feedback and workflow/FSM contract law moved out. |
| **Katamorph** (contracts), **Alpha** (structural integrity) | Knoxx *consumes* katamorph (pinned in `backend/deps.edn`, ~19 source files require `katamorph.*`). | Cross-DSL contract kernels, EDN repair/whitelist research and the driver-agnostic migration protocol moved out as lift candidates. They are *not* accepted Foresight law (see [project-law promotion-status triage](../notes/project-law-promotion-status-triage.md)). |
| **Sol / eta-mu** (agent runtime, tool catalog) | Knoxx *hosts* an agent runtime and exposes tools. | The general agent/action/tool ontology and the generic structural-editing tool surface moved out. Knoxx's nREPL wiring stayed. |
| **Axxium** (identity) | Knoxx *defers* identity to Axxium (`law/axxium_identity.cljs`). | Cross-host identity planning moved out with the services material. The credential-delegation spec stayed (see "Stayed on purpose"). |
| **services** / Promethean hosts (deployment topology) | Knoxx is *deployed by* services and does not own host placement. | VPS host notes, the environment-promotion plan and the mutation gate moved out. The compose file and verifier docs stayed. |
| **osmos** (extracted kms-ingestion JVM service) | `knoxx/ingestion/` is still the checked-out host of that code, but Foresight names osmos as its owner. | The ingestion hot-reload workflow moved out. Ingestion cards are listed below. |
| **proxx** (model proxying) | Knoxx *calls* proxx for models. | Generic Ollama streaming reference and a proxx image-handling debug session moved out. The Knoxx half of that debugging transcript stayed. |
| **calliope** / creative and audio lore (Fork Tales, OpenUtau, Ussyverse) | Knoxx *hosts* some studio/UTAU tools (`domain/openutau/`), but the creative work is not Knoxx behavior. | Audio research, spectrogram sessions, MIDI references, song prompts and the Shoedelussy UI map moved out. |
| **uxx** (UI kit) | Knoxx *uses* `@open-hax/uxx`. | The Solid/CLJS frontend landscape research moved out. uxx component cards are listed below. |
| **`.agents` skill catalog** | Knoxx carries mirrors of some skills. | The stale `sing-the-songs-of-your-people` copy and the spectrogram skill moved out. The `pr-review-to-merge` mirror stayed because `knoxx/AGENTS.md` links it and the skill sanctions project copies. |
| **Purify-before-port doctrine** | Knoxx follows it. | Repository-agnostic data-oriented patterns, CLJS inference-warning policy and Clojure currying reference moved out. |
| **OpenPlanner, Myrmex, Graph-Weaver, our-gpus, Shibboleth** (not Foresight submodules) | Knoxx was historically *inside* the OpenPlanner monorepo. It is now a client. `knoxx/AGENTS.md` forbids coordinated OpenPlanner changes. | The epistemic kernel (OpenPlanner is its store of record) moved out. Cards are listed below. |
| **Predecessor product line** (futuresight-kms, knowledge-lake TS package, Exposure Monitor, Ragussy) | None of this code exists in Knoxx, and it conflicts with the CLJS-first rule. | Cards are listed below as lineage. |

## Review corrections after extraction

PR #119 corrects inherited example defects, formatting, and unsupported current-
authority claims while retaining the original extraction provenance. These are
curated successor documents, not byte-identical archival copies or evidence of
new runtime deployment. Historical implementation sketches remain proposals.

Targeted regression evidence: run `nbb docs/verification/knoxx_extraction_snippets.cljs`
from the Foresight root. The verifier evaluates the corrected document snippets;
PostgreSQL query effects are stubbed, and no live service or child is invoked.
It checks example mechanics, not production migration or authority admission.

## Moved whole

Paths are relative to this repository. The source is the Knoxx path.

| Foresight path | From Knoxx | Relationship and reason |
| --- | --- | --- |
| [`docs/notes/workflow-contract-graph-v1.md`](../notes/workflow-contract-graph-v1.md) | `disabled-contracts/workflow-contract-graph-v1.md` | Rheos, katamorph, proxx. Agent-work workflow law across repositories; Knoxx contracts are one grounding input. Filed under notes as a lift candidate, not under specs, so it is not read as current authority. |
| [`docs/notes/workflow-contract-kernel-three-graphs.md`](../notes/workflow-contract-kernel-three-graphs.md) | `docs/notes/contracts/workflow-contract-kernel-intent.md` | Rheos, proxx, eta-mu, katamorph. Knoxx is one of five dialects; the "advisory edges never authorize transitions" rule is Rheos law. It is the intent note for the spec above. |
| [`docs/architecture/agent-workflows-kanban-github-review.md`](../architecture/agent-workflows-kanban-github-review.md) | `docs/agent-workflows.md` (pointer left) | Rheos, eta-mu, `.agents`. The shared Kanban → GitHub → review-gate stack that Knoxx "participates in". Its `eta-mu kanban sync github` references should be reconciled with Rheos. |
| [`docs/architecture/epistemic-kernel.md`](../architecture/epistemic-kernel.md) + [`epistemic-examples.edn`](../architecture/epistemic-examples.edn) | `docs/epistemic-kernel.md` (pointer left), `docs/epistemic-examples.edn` | epiphany (observed→accepted), clio (immutable ledger), OpenPlanner. Historical model / lift candidate, not accepted Foresight law. The proposal places truth in OpenPlanner and treats Knoxx as one controller; extraction does not promote it. Knoxx's implementation `ingestion/src/kms_ingestion/epistemic.cljc` stays. |
| [`docs/architecture/data-oriented-patterns.md`](../architecture/data-oriented-patterns.md) | `docs/style/data-oriented-patterns.md` | alpha, Truth ECS, purify-before-port. Repository-agnostic doctrine; "Knoxx" appears twice. |
| [`docs/notes/driver-agnostic-migration-protocol.md`](../notes/driver-agnostic-migration-protocol.md) | `docs/notes/architecture/driver-agnostic-migration-protocol.md` | alpha, katamorph, clio. Never implemented in Knoxx (no `MigrationDriver`, runner or migration log), and its Postgres target is retired. The surviving idea is a storage-agnostic, data-as-migration law, so it is a recovered claim and not an accepted lift. |
| [`docs/notes/agent-action-tool-ontology.md`](../notes/agent-action-tool-ontology.md) | `docs/notes/thoughts/agent-emerges-from-context.md` | sol, katamorph. It defines agent, action and tool in general. Knoxx's `domain/action/` versus `domain/tools.cljs` split is one instance. It overlaps [operation-contracts-beneath-workflowstep](../notes/operation-contracts-beneath-workflowstep.md). |
| [`docs/notes/cljs-target-type-inference-warnings.md`](../notes/cljs-target-type-inference-warnings.md) | `docs/notes/reference/cljs-target-type-inference-warnings.md` | Every CLJS child. The enforced rule stays in `knoxx/AGENTS.md` (warning ratchet, extern boundary); the explanation is shared. |
| [`docs/notes/rheos-board-ui-frontmatter-comments-feedback.md`](../notes/rheos-board-ui-frontmatter-comments-feedback.md) | `docs/notes/2026.05.28.17.30.58.md` | Rheos. UX feedback on the board UI; the Knoxx card was only a fixture. The Knoxx notes `INDEX.md` already marked it as an extraction candidate. |
| [`docs/notes/ops/osmos-ingestion-hot-reload.md`](../notes/ops/osmos-ingestion-hot-reload.md) | `docs/notes/ops/clojure-ingestion-hot-reload.md` | osmos. JVM Ring/Jetty ingestion dev loop, unrelated to the CLJS backend. It was still accurate against `knoxx/ingestion` at extraction time. |
| [`docs/notes/ops/development-vps.md`](../notes/ops/development-vps.md) | `docs/development-vps.md` | services / Promethean hosts. Describes a host (IP, Caddy, UFW, PM2 unit), not Knoxx code. Knoxx's README says services owns host placement. |
| [`docs/notes/ussyverse-repo-links.md`](../notes/ussyverse-repo-links.md) | `docs/notes/reference/ussyverse-repo-links.md` | Repository census. External community repos already tracked in [repository-census-current-pinned-closure](../research/repository-census-current-pinned-closure.md). |
| [`docs/research/solid-cljs-landscape.md`](../research/solid-cljs-landscape.md) | `docs/notes/research/solid-cljs-landscape.md` | uxx. A framework survey with no Knoxx decision; rendering substrate is decided at uxx/Foresight level (see [hiccup-vs-helix](../notes/hiccup-vs-helix-rendering-representation.md)). |
| [`docs/research/open-source-audio-separation-tools.md`](../research/open-source-audio-separation-tools.md) | `docs/notes/research/open-source-audio-separation-tools.md` | calliope / music-studio lineage. No Knoxx code references it. |
| [`docs/research/suno-to-openutau-midi-reference.md`](../research/suno-to-openutau-midi-reference.md) | `docs/suno-to-openutau-midi-reference.md` | calliope (Fork Tales). Sources are host music corpora; Knoxx agents are only named as consumers. |
| [`docs/notes/creative/spectrogram-audio-lyrics-session.md`](../notes/creative/spectrogram-audio-lyrics-session.md) | `docs/notes/tools/spectrogram-audio-lyrics-session.md` | calliope. An art-session prompt; relates to Knoxx only because the UTAU tool is Knoxx-hosted. |
| [`docs/notes/creative/music-spectrogram-analysis-skill.md`](../notes/creative/music-spectrogram-analysis-skill.md) | `docs/skills/music-spectrogram-analysis.md` | `.agents`, calliope. A tool-agnostic skill that Knoxx never loads. It should be promoted into `.agents` properly. |
| [`docs/notes/creative/shoedelussy-frontend-surface-map.md`](../notes/creative/shoedelussy-frontend-surface-map.md) | `docs/shoedelussy-frontend-surface-map.md` | Shoedelussy (external). Every path points into another codebase; Knoxx only bridges to it (`SHOEDELUSSY_MCP_*`). |
| [`docs/lineage/skills/sing-the-songs-of-your-people-knoxx-copy.md`](skills/sing-the-songs-of-your-people-knoxx-copy.md) | `docs/skills/sing-the-songs-of-your-people.md` | `.agents`. A stale predecessor of the canonical core skill in `~/.agents`, kept as lineage and not as a second authority. |

## Split: non-Knoxx sections moved, Knoxx sections stayed

| Foresight path | From Knoxx (sections) | What stayed in Knoxx |
| --- | --- | --- |
| [`docs/notes/ops/environment-promotion-and-cross-host-identity-plan.md`](../notes/ops/environment-promotion-and-cross-host-identity-plan.md) | `docs/verification/environment-and-cross-host-acceptance.md`: requested contract, execution phases, initial observations, Services promotion gate | Host runtime (`compose.environment.yaml`, `prepare-environment.mjs`), cross-host verifier, local validation. The moved material is a multi-repo operator plan (services, axxium, Promethean hosts). |
| [`docs/research/agent-authored-edn-repair-and-expression-whitelist.md`](../research/agent-authored-edn-repair-and-expression-whitelist.md) | `docs/notes/contracts/contract-runtime-validation-layers.md` §B–C | §A, §D and §E (Knoxx schema, agent tools, event adapter). B and C answer Alpha's "is it well-formed?" for any agent-authored EDN. Neither was implemented in Knoxx. |
| [`docs/research/structural-editing-agent-tool-surface.md`](../research/structural-editing-agent-tool-surface.md) | `docs/notes/architecture/nrepl-structural-editing.md`: "Lisp as data", tool surface, stack choices | Two-problems framing and the nREPL wiring (`domain/nrepl.cljs`, `cap_nrepl.edn`). The tool surface is a generic muse/eta-mu capability. |
| [`docs/notes/creative/ussyverse-song-prompts.md`](../notes/creative/ussyverse-song-prompts.md) | `docs/notes/frontend/chat-page-ux-issues.md` "Prompts" | ChatPage UX defects. |
| [`docs/research/ollama-multimodal-streaming-reference.md`](../research/ollama-multimodal-streaming-reference.md) | `docs/notes/reference/ollama-multimodal-stream-reference.md` (Ollama REST reference) | The long Knoxx image-part debugging transcript, which is Knoxx history. |
| [`docs/notes/proxx-ollama-image-handling-debug.md`](../notes/proxx-ollama-image-handling-debug.md) | same file, proxx `ollama-compat`/provider-strategy section | same as above |
| [`docs/research/clojure-currying-and-partial-application.md`](../research/clojure-currying-and-partial-application.md) | `docs/notes/reference/clojure-currying-partial.md` (generic currying Q&A) | The `defroute` route-DSL design session, which is Knoxx history. |

## Stayed on purpose, despite external relationships

- `docs/notes/contracts/actor-credential-delegation-spec.md`: its three-identity
  delegation model (subject / executor / connection owner) is an **Axxium** lift
  candidate. The text is unstructured and truncated, and interleaves the generic
  model with Knoxx provider schemas, so splitting it would have been a rewrite.
  It stays in Knoxx with a status banner; lift the model when Axxium takes up
  delegation.
- `docs/notes/ops/shoedelussy-redirect-loop.md`: half of it is Promethean ingress
  (services), but it is an unresolved incident prompt too thin to split.
- `docs/notes/2026.06.03.09.09.14.md`: a single `gh` command for services. The
  Knoxx notes `INDEX.md` records it as `closed-no-extraction`, and that
  decision was respected.
- `.agents/skills/pr-review-to-merge/` and `.opencode/skill/pr-review-to-merge/`:
  sanctioned mirrors of the canonical skill (byte-identical) that
  `knoxx/AGENTS.md` links.
- `docs/notes/product/knoxx-commercial-model.md`: Knoxx-specific, but it contains
  commercial pricing figures. It was deliberately not copied to another public
  repository, and whether it should remain public is an owner decision.

## Kanban cards that belong elsewhere (not moved)

Board state is owned by Rheos, so **no card was moved, copied or edited**. The
cards below are Knoxx cards whose subject belongs to another board. Carry out the
recommended action through Rheos, not by hand-editing frontmatter. Paths are
under `knoxx/kanban/`.

**Already covered on another board: close as superseded**
- `epics/contract-runtime-merge-proposal.md` → eta-mu `katamorph-canonical-cutover`.
- `epics/knowledge-ops-chat-ui-library.md` → eta-mu `chat-ui-extraction` (done).
- `tasks/knoxx-contract-runtime-extraction.md` (status `pending`, not an FSM
  state) → superseded by katamorph, which is pinned in `backend/deps.edn`.

**chat-ui** (sibling repo `open-hax/chat-ui`; `packages/chat-ui` was never created in Knoxx)
- `tasks/knoxx-chat-ui-core-components.md`, `-hooks-and-utils.md`,
  `-package-scaffold-and-types.md`, `-test-and-typecheck-gate.md`.

**osmos** (the extracted kms-ingestion service)
- `tasks/ingestion/ingestion-{bulk-import-api,file-upload-api,github-driver,google-drive-driver,progress-streaming}.md`
- `tasks/knowledge-ops-kms-openplanner-ingest-arity-fix.md`
- Split: `tasks/knowledge-ops-translation-mt-pipeline.md` (the MT worker half).

**services** (no board of its own; park on the Foresight board if tracked)
- `tasks/services-caddy-hostname-scale-decision.md`,
  `services-staging-slot-pattern.md`, `services-knoxx-staging-migration.md`,
  `services-proxx-staging-migration.md`. They relate to Foresight
  [`record-digitalocean-deployment-acceptance`](../agile/kanban/record-digitalocean-deployment-acceptance-86af1ffc.md). The staging hazard they describe
  is live: Knoxx `main` calls a services workflow pinned to an unmerged commit
  (`knoxx/.github/workflows/environment-promotion.yml:14`).
- `tasks/knoxx-knowledge-ops-docker-compose-self-hosted.md`.
- Split: `tasks/knoxx-deploy-actor-owning-local-credentials.md` (provisioning
  half), `epics/translated-publication-to-website.md` (services slices).

**uxx** (no board of its own)
- `tasks/knoxx-uxx-button-chord-prop.md`, `-chord-overlay-composite.md`,
  `-export-editor-statusbar-toolbar.md`, `-mode-indicator-primitive.md`
- `epics/knowledge-ops-ui-design-system.md`

**Foresight capability lanes** (split: generic core → Foresight, Knoxx adapter stays)
- Evaluation (Mu): `epics/knoxx-evaluation-review-system.md`,
  `tasks/knoxx-evaluation-case-contracts.md`, under Foresight `abdd5a2d` /
  `760f7f1e`.
- Resource repository / representation: `epics/knoxx-resource-repository-cms.md`,
  `epics/knoxx-representation-output-boundary.md`,
  `tasks/knoxx-cms-contract-validation.md`,
  `tasks/knoxx-file-resource-repository-provider.md`, under Foresight `3fd1fdf7`.
- Transduction: `epics/knoxx-transduction-provider-pipeline.md`, under Foresight
  `abdd5a2d`.
- Doctrine: `epics/knowledge-ops-knoxx-opinionated-distribution.md`, which matches
  purify-before-port.

**OpenPlanner / Myrmex / Graph-Weaver / our-gpus / Shibboleth** (not Foresight submodules)
- `epics/knowledge-ops-adaptive-web-frontier-and-multiscale-backbone.md`. It is
  also malformed: two frontmatter blocks, so Rheos parses no uuid.
- `epics/knowledge-ops-mongodb-vector-unification.md`,
  `epics/knowledge-ops-exposure-monitor.md`
- `tasks/knowledge-ops-adaptive-web-frontier-umbrella-spec-authoring.md`,
  `knowledge-ops-graph-weaver-live-sync-truth.md`,
  `knowledge-ops-myrmex-openplanner-write-recovery.md`,
  `knowledge-ops-openplanner-{derived-edge-projections-slice,gardens-backend-fixes,graph-population-smoke,salience-backbone-materialization}.md`
- `tasks/knoxx-mongodb-docker-compose-decommission.md`,
  `knoxx-mongodb-migration-script.md`,
  `knoxx-multi-tenant-migrations-prod-runbook.md`
- Split (TypeScript `TenantPolicy` half is OpenPlanner):
  `epics/knowledge-ops-multi-tenant-control-plane.md`,
  `tasks/knoxx-multi-tenant-rate-limit-policy-hook.md`,
  `knoxx-multi-tenant-review-workflow-queue.md`,
  `knowledge-ops-graph-memory-runtime-smoke-e2e.md`,
  `knowledge-ops-docs-source-of-truth-normalization.md`,
  `epics/knowledge-ops-shibboleth-lite-labeling.md`,
  `epics/knowledge-ops-source-lakes-cross-lake-graph.md` (phases 3–5).

**Predecessor product line** (lineage; most should be rejected on the Knoxx board)
- `epics/knowledge-ops-architecture-migration.md`,
  `epics/knowledge-ops-product-line.md`
- `tasks/knowledge-ops-product-line-{cross-link-roadmap,exposure-monitor-specs,kanban-grooming,shared-infra-scaffold}.md`
- `tasks/knowledge-lake-{azure-aws-provider-stubs,domain-logic-layer,local-dev-provider-and-factory-wiring,package-scaffold-and-core-interfaces,self-hosted-provider-implementations}.md`
- `tasks/knoxx-lake-{local-embedding-search,local-storage-blob-queue,package-scaffold,provider-factory}.md`
- `tasks/knoxx-futuresight-kms-{chat-widget,cms-python-backend,cms-react-ui,sync-public-collection}.md`
- `tasks/knowledge-ops-demo-seed.md`, `tasks/knoxx-arch-migration-ragussy-ui-sunset.md`

The per-card evidence (uuid, status, `path:line` drift) was produced during the
audit. The Knoxx-side status drift (cards whose code has shipped, or cards marked
done whose code is absent) is summarized in the Knoxx PR description rather than
here, because it is Knoxx board state.
