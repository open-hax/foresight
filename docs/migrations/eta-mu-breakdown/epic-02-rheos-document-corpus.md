# Epic E2 — Rheos Document Ownership and Foresight Corpus Migration

**Planning status:** proposed, informed by pinned source inspection on 2026-09-12.
**Outcome owner:** Foresight document/work system. **Implementation participants:** Rheos, extracted ingestion service (working name Osmos), Epiphany, and shared Foresight libraries where genuinely reusable.
**Prerequisite:** E1 supplies usable independent packages and stable interfaces. Read-only corpus inventory and schema work can run before donor retirement. IDs in this file are local planning identifiers, not existing cards.

## Outcome

A contributor can open a Markdown document in Rheos, inspect its relationships to other documents and the code it describes, see why labels and relationships were suggested, and safely accept edits, relocation, consolidation or retirement. Boards and agent chat remain useful views over the same file/work model rather than defining the whole product.

Foresight's active documentation becomes smaller, current, attributable, navigable, and independently useful at product boundaries. Outdated active guidance is retired before replacement writing. Its historical existence remains inspectable through Git and provenance records; the system does not confuse removal from active guidance with erasure of evidence.

This is not a promise that every Obsidian capability will be implemented in this epic. It establishes the document graph, command boundary, corpus workflows, and a usable document-centered view on which richer editing/navigation can grow.

## Existing implementation to reuse

The inspected Rheos README and domain tree describe shared CLI/HTTP/MCP mutation paths for Markdown cards, board projections and chat, not an already-general-purpose document graph. Extend that domain boundary rather than building a second independent write engine. [rheos.readme]

The ingestion service already has local-source scheduling/watch behavior, changed/deleted-path jobs, configurable sinks, and translation/audio-related integration. Its contract loader/configuration is coupled to its current deployment. Reuse acquisition and worker capabilities; make the new Markdown pipeline explicit, bounded, resumable and independently configured. [ingestion.server, ingestion.config, ingestion.contracts]

Epiphany already contains section/history/evidence/lineage infrastructure and a `redundancy` implementation. Its design explicitly distinguishes observed source facts from provisional and accepted interpretations. Its current classifier uses text overlap, negation and simple mutual-exclusion signals; its pair enumeration is quadratic before the result limit, and its “duplicate” check compares trimmed strings rather than raw file bytes. Those are concrete inputs to E2.06, not reasons to build a competing subsystem. [epiphany.readme, epiphany.redundancy]

Clio's documented event model separates immutable event facts and causal/order relations from disposable projections. This is a fit for durable document decisions and changes, but this pack does not assert the future Rheos/Osmos/Epiphany integration is already implemented or admitted through Clio. [clio.readme]

## Proposed responsibility boundaries

| Component | Owns | Must not silently own |
|---|---|---|
| **Git / source repository** | Original bytes and revision history for Git-backed files | Interpretive acceptance merely because a string was committed |
| **Rheos** | Document/work identity, revision-checked edit/move/retire commands, document relationships, accepted metadata and coordinated workflow | Model guesses treated as authoritative facts; bypass of product source ownership |
| **Osmos** (proposed name) | Acquisition, file watching, text extraction/normalization where needed, bounded jobs and delivery to analysis/index boundaries | A second document-authoring policy engine or duplicate historical authority |
| **Epiphany** | Revision-aware observations, sections/spans, retrieval, lineage/similarity/contradiction candidates and evidence packets | Unreviewed deletion or authoring decisions based solely on similarity |
| **Clio** | Admitted immutable decision/change records with real schema identities and causal references | Invented receipts for failed, unadmitted or unpersisted operations |
| **Foresight common/core** | Shared resource/relationship contracts, portable `.cljc` laws and test fixtures where multiple products truly share them | An unnecessary runtime dependency on the whole suite for a child to function |
| **Views and agents** | Boards, document navigation, backlinks, graphs, search, review and task execution through the same commands | A private alternative source of document state |

An external editor is a valid source of changed bytes. The watcher records that observation and invalidates affected projections. It must not pretend Rheos authorized an external edit, silently discard it, or apply a stale queued write over it.

## Core contracts

### Identity and revision

A source observation carries repository/resource identity, exact commit, raw path, blob identity, content digest when computed, and extractor/policy version. A working-tree observation is separately labeled and content-addressed; it is not assigned a fake committed revision. Path spelling, case and Unicode form are preserved.

A stable logical document ID, a particular file revision, and a content identity are different things. Identical blobs at two paths do not automatically become one document. A move/copy proposal retains the original and destination observations. Section IDs/spans bind to the exact source revision; revisions invalidate old offsets.

### Typed relationships, not an undifferentiated link graph

| Relationship | How it is obtained | Initial status |
|---|---|---|
| Explicit Markdown link, reference-style link, image/embed, supported wiki-link | Parser observation with source span and raw target | Observed syntax; target resolution tracked separately |
| Non-linked path-like string | Lexical/AST observation in context | Observed string; unresolved or candidate until resolved |
| Resolved file/heading reference | Resolution against pinned repository/tree and anchor policy | Observed resolution result, including ambiguity/unavailability |
| Code import/namespace/manifest dependency | Language-aware source or manifest analysis | Observed declaration; dynamic/runtime resolution may remain unknown |
| `documents` / `implements` / `verified-by` | Explicit declarations or evidence-backed review | Observed declaration or provisional/accepted interpretation |
| Similarity / near-duplicate / complementary / contradiction | Deterministic signals, retrieval, or model analysis | Provisional candidate |
| Supersedes / semantic continuation / canonical owner | Directional, scope-aware evidence and a review decision | Provisional until accepted |
| Retired / relocated / consolidated | Authorized mutation with before/after checks | Recorded operation, distinct from the proposal and its execution attempt |

An observed mention of `../something` is not proof that the target exists. A declared dependency is not proof that a build resolved it. A board task saying “implemented” is not proof that source or runtime behavior satisfies it.

### Metadata and proposal authority

Keep observed frontmatter and inferred facets separate. Useful initial facets are document kind, topics, mentioned projects, audience, and review flags. Ownership and currentness require stronger evidence than word frequency or file age. Confidence values are self-assessed scores, not calibrated probabilities.

The model proposes JSON. The receiver validates it and attaches the trusted source identity, prompt/model/schema/taxonomy versions, timestamps, digests and job identity. The model is not asked to generate Git SHAs, declare itself accepted, or authorize a filesystem mutation. See the separate classification contract and prompt in this pack.

## Story sequence

### E2.01 — Maintainer can enumerate and reproduce the whole corpus before cleanup

**Acceptance.** A reproducible manifest covers each admitted Foresight repository at an exact revision and identifies included, excluded, unavailable and uninitialized roots. Every Markdown file has an observation record; generated/vendor/private/sensitive paths have explicit policies. All existing files remain untouched during inventory. The initial inventory is retained before active outdated documents are removed.

**Tasks.** Start from Foresight's project registry and gitlinks, not an uncontrolled recursive scan of arbitrary directories. Enumerate tracked `.md`/`.markdown` and explicitly admitted variants, with `.mdx` handled as its own parser policy. Record size, encoding, raw path, blob/digest, headings/frontmatter, owner candidate and historical/current scope. Avoid following symlinks beyond admitted roots. Recognize nested submodules and shared blobs without conflating repository identities. Identify licenses, AGENTS, fixtures and immutable audit records as protected document roles, not generic duplicates.

**Failing fixtures first.** Uninitialized submodule, case-distinct path, symlink escape, invalid UTF-8, unavailable revision, generated copy and a working-tree edit. None may be silently skipped or assigned false committed provenance.

**Evidence.** Revision manifest, coverage denominator and exclusions; repeat scan produces the same manifest under the same inputs/policy.

### E2.02 — Operators can index and reindex through a bounded, resumable acquisition pipeline

**Acceptance.** The extracted service and Epiphany exchange versioned source observations/jobs through one documented boundary. Repeat delivery does not duplicate canonical observations or decisions. A restart resumes without losing accepted work. Changing the model/extractor creates new derived results, not overwritten source facts. Deleted active paths are reflected without erasing their history.

**Tasks.** Adapt existing watchers/jobs to revision- or digest-based inputs and explicit document extraction results. Coordinate with Epiphany's existing Git ingestion rather than having both services independently claim authority over the same historical scan. Define one observation identity and idempotency rule. Add bounded queue depth, cancellation, retry budget, durable checkpoints, exact outcomes and source-change checks. Keep lexical and semantic indexes disposable. Test model/database outages and consumer backpressure. Local-only execution is the initial privacy default; sending corpus text to a remote provider needs an explicit deployment policy.

**Failing fixtures first.** Duplicate event, process crash between delivery and acknowledgement, rename plus deletion, replay after extractor upgrade, and outage after half a batch. No loss, double acceptance, infinite retries, or false success.

**Evidence.** Restart/replay tests, queue/checkpoint inspection, per-document job states and reproducible reindex from source plus accepted decisions.

### E2.03 — Readers can inspect a source-grounded graph of documents and code

**Acceptance.** Rheos shows outgoing references and backlinks for a document, with relation type, source span, target revision and resolution status. Explicit links, bare path strings, source dependencies and semantic suggestions remain distinguishable. Ambiguous or broken references remain visible. A focused neighborhood and a full-suite export are available without presenting proposals as facts.

**Tasks.** Reuse or extract shared parsing/resolution contracts. Parse Markdown links, definitions, anchors, code spans/fences and wiki-links under explicit syntax policies. Preserve raw target text while resolving against the source document directory, repository root, current revision and admitted project aliases. Do not resolve an example path as live configuration merely because it exists. For Clojure/CLJS and JS, use namespace/import/manifest evidence; retain dynamic dependencies as unresolved where necessary. Export typed graph data and Mermaid views. Cross-reference story/requirement documents with actual code and tests.

**Failing fixtures first.** Link with URL escaping, renamed heading, same basename in two projects, Unicode paths, a path in an illustrative code block, stale source anchor, and dynamic import. Each gets the right typed result without false certainty.

**Evidence.** Parser/resolver fixtures, exact-span backlinks and deterministic graph exports; browser walkthrough of a document-to-code and code-to-document path.

### E2.04 — A small model can label one document without gaining write authority

**Acceptance.** Every model result is either a validated, source-bound proposal or an explicit failed/quarantined/abstained outcome. The job records complete-read or partial-read coverage. No classification result changes a document, deletes a file, accepts a relation, or imports a repository on its own. Initial labels are useful on a human-reviewed sample before a whole-corpus pass.

**Tasks.** Use the supplied closed JSON schema and bounded prompt as a starting contract. Supply allowed topic/project identifiers; keep new-taxonomy proposals separate from accepted labels. Validate JSON size, schema, unique labels, dictionary membership, evidence line bounds and verbatim quotes. Attach trusted source/model/prompt/schema/taxonomy provenance outside model output. Treat document text as untrusted data; run the classifier with no filesystem, network, shell or mutation tools. For oversized docs, preserve per-section coverage and deterministic aggregation; never quietly call a truncated prefix a full read. Cache by content and policy/model identities, not just path. Rebind cached suggestions to the requesting document identity without merging those documents.

**Failing fixtures first.** Markdown telling the model to ignore instructions and delete files; extra JSON keys; invented project ID; out-of-range span; non-matching quote; no valid JSON; oversized reply; hidden truncation; reused output after the source changes. Rejected results cannot enter accepted tags.

**Evidence.** Small labeled evaluation set, per-facet error/abstention rates, examples of harmful label mistakes, latency/resource notes, and negative fixtures. Model selection follows measured quality on this corpus rather than a claimed universal “best small model.”

### E2.05 — Maintainers can remove outdated active guidance before writing replacements

**Acceptance.** Retirement candidates distinguish outdated guidance, historical records, unresolved currentness, and partially stale documents. A retirement decision cites the exact claim/scope and superseding implementation, decision or replacement evidence. Approved outdated guidance leaves the active corpus first; replacement content is optional later work. Original bytes remain available through recorded Git references and history. No artificial replacement prose is generated merely to keep file counts stable.

**Tasks.** Compare explicit commands, paths, exports, dependencies and documented decisions with current source observations. Use date/age only as a triage signal. Start with known discrepancies such as Rheos's old build description. Record a tombstone/retirement event and repair active navigation/backlinks with an appropriate current destination or retired state. Do not keep redundant archive folders in the active tree merely to avoid using Git history. Keep immutable receipts and historical ADRs inspectable as historical evidence, not mislabeled as current instructions. Documents with unique still-valid material require an explicit preservation/split decision rather than a casual whole-file deletion.

**Failing fixtures first.** A recent but false guide and an old but correct guide; a historical decision valid at its own revision; a mixed-currentness document; a queued retirement after source changes. Age-only deletion and stale-head deletion are refused.

**Evidence.** Before-state snapshot, source/counterevidence for each retired claim, revision-checked change, tombstone, active-link report and demonstrated historical retrieval.

### E2.06 — Reviewers receive bounded redundancy candidates, not an all-pairs deletion machine

**Acceptance.** Exact byte duplicates, normalized-text matches, near duplicates, complementary sections, contradictions and directional supersession are distinct outputs. Candidate generation is bounded before expensive comparisons. Each candidate retains both exact source revisions/spans and reasons. No score establishes document identity or removal authority. Existing Epiphany behavior is reused behind tests, then corrected where warranted.

**Tasks.** First group identical raw content by digest with collision-safe verification; preserve separate path/document identities. Treat whitespace/normalization matches as a different relation. Retrieve plausible section/document pairs using lexical fingerprints and/or search/embedding neighbors rather than blindly comparing the entire corpus. Check contradictions and scope differences before recommending consolidation. Extend Epiphany's current classifier: regression-test near-duplicate precedence over mutual-exclusion signals, trimmed-text identity, order-sensitive supersession, hash-based pair selection and non-stable candidate IDs. Replace hash-order pair uniqueness with collision-safe canonical pair identity. Include source/policy/model revisions in stable candidate keys. A cap applied after an all-pairs sort is not a work budget.

**Failing fixtures first.** Nearly identical text where “all” becomes “any”; a security negation; an indented code block changed by trimming; same content in different roles (LICENSE/AGENTS); A≈B and B≈C without A≈C; duplicate IDs/hash collisions; supersession in only one direction; large corpus with a strict comparison budget.

**Evidence.** Candidate recall/error sample, bounded pair-count and memory/latency measurements, exact pair identity, contradictions that veto automatic consolidation and repeated-run reproducibility.

### E2.07 — Maintainers can consolidate documents without losing unique claims or provenance

**Acceptance.** A consolidation proposal states the canonical scope/owner, source documents and sections, overlapping claims, unique retained material, contradictory material, and planned link changes. Accepted consolidation produces a reviewed diff and source map. It does not flatten contradictions into an apparently certain summary. Obsolete inputs are retired only after the new state satisfies the preservation contract.

**Tasks.** Produce a section/claim coverage map before writing. Select a surviving document or a new combined document based on responsibility, audience, currentness and evidence, not just length or embedding centrality. Keep unresolved disagreement visible or defer the merge. Validate examples/commands against source where practicable. Rewrite inbound links and fragments from a path/anchor map. Separate summarization quality review from source-fidelity review. Apply the same revision checks and rollback rules as other edits.

**Failing fixtures first.** A short document contains one unique constraint; two documents disagree about an authentication requirement; an inbound fragment points into a merged section; a source changes mid-review. The operation must preserve the constraint, expose the contradiction, maintain navigation, or refuse the stale write.

**Evidence.** Before/after diff, claim/section preservation map, unresolved issues, link graph diff, review decisions, and historical retrieval of every source.

### E2.08 — Documentation lands with the correct product or suite owner

**Acceptance.** High-frequency cross-project references nominate documents for hoisting to `foresight/docs`, with the matched spans and counting policy inspectable. Accepted placement is based on actual responsibility/scope, not frequency alone. Product-local README, AGENTS, API and development docs remain sufficient for an independent clone. Every moved document has a relocation map and repaired links.

**Tasks.** Maintain a versioned project alias registry. Count references by distinct meaningful sections and resolved targets; discount repeated templates/navigation. Make nomination thresholds explicit and tune them against reviewed examples. Route shared architecture, multi-product workflows and integration contracts to Foresight; retain product operation details with the owner or leave a concise pointer to suite material. Use a path map for re-rooting relative links and anchors, not global string replacement. E1's mechanical transfer of relevant eta-mu/docs is input to this placement pass, not proof that all such documents should stay centralized forever.

**Failing fixtures first.** Boilerplate repeats another project name fifty times; a one-product API guide legitimately integrates a second product; README must work outside Foresight; a moved page has relative source and image links. Wrong-owner hoists and broken independent docs are rejected.

**Evidence.** Per-document routing rationale, observed mention counts, reviewer corrections, path map, active backlinks and independent-clone documentation check.

### E2.09 — References outside the constellation become explicit scope decisions

**Acceptance.** Mentions of non-admitted internal projects produce a dossier identifying the repo, purpose, actual code/runtime dependencies, affected documents and options. Importing a module, retiring an irrelevant document, or retaining a documented external boundary is an explicit reviewed decision. No model or high mention count automatically clones, executes, imports, or deletes anything. Normal third-party tools are not automatically treated as Foresight product candidates.

**Tasks.** Compare resolved project references with the Foresight registry. Separate internal products, historical names, third-party dependencies, examples and unknown strings. In the inspected baseline, Sol's Git dependency on event-ledger and ingestion's OpenPlanner integration deserve specific reconciliation against the constellation; deleting their docs does not remove those dependencies. Assess module ingestion/digestion with exact revision, tests, ownership, maintenance and interface scope. Capture a proposed follow-on import or a justified document retirement rather than silently expanding this epic.

**Failing fixtures first.** Mentioning PostgreSQL does not create a Foresight PostgreSQL submodule; an unknown “Sol” word is not accepted as a repo match; a real runtime dependency cannot be declared gone by deleting its guide.

**Evidence.** Scope-decision dossiers, source relationships and accepted project/placement changes. Unknown identities remain unknown.

### E2.10 — Rheos offers a document-centered workspace alongside boards and chat

**Acceptance.** A user can browse/read/search documents, inspect typed backlinks and code references, see source/review state, compare a proposal with the current revision, and accept or reject a permitted change. Existing board operations still work. Graph, board, list and chat show consistent state because they share the same command/query boundary.

**Tasks.** Start with a thin vertical slice: select one Markdown file → view its references → inspect a label/relation proposal with evidence → accept metadata → observe the same result in CLI and UI. Add an explicit diff/review surface for edit, move, consolidation and retirement proposals. Show observed/provisional/accepted/stale/unavailable separately from workflow columns such as ready/in-progress/review. Authorize commands through the chosen actor boundary; agents do not inherit arbitrary write authority from reading a document. Use the extracted Chat UI rather than duplicating its session logic.

**Failing fixtures first.** One view shows an accepted label while another shows it pending; stale document content during edit; actor lacks authority; a prompt embedded in source text asks for a command; a board action accidentally rewrites unrelated frontmatter or prose.

**Evidence.** End-to-end browser screenshots for reading, evidence inspection, diff/review, acceptance/refusal and resulting navigation; CLI/API/UI parity tests. No source-rewriting “cleanup” to make the screenshots appear consistent.

### E2.11 — Documents and code invalidate each other's derived claims where relationships warrant it

**Acceptance.** Changing code, a documented command, a source path or a document revision invalidates only the relevant derived checks/proposals. The system reports stale or unavailable evidence, not a false contradiction or an automatic semantic rewrite. Current code and documentation can be reverified through owned checks.

**Tasks.** Index acceptance criteria, documented commands, source/heading references and explicit implements/documents relations. Bind evidence to subject revision plus test/extractor/policy revision. Implement deterministic checks first: broken links, missing files/namespaces, documented script missing or changed, copied old root paths, invalid frontmatter and duplicate IDs. Use NLP/LLM reviewers for candidate semantic issues; recurring safe wording fixes can be policy-driven only after scoped regression tests. Package shared portable laws in Foresight common/core when genuinely reused, while keeping adapters/effects in the owning product.

**Failing fixtures first.** Code rename with old doc link; manifest build target change; updated model/prompt but unchanged file; review evidence for the wrong SHA; a removed file still searchable as current. Only affected projections invalidate, and historical evidence remains retrievable.

**Evidence.** Incremental invalidation tests, exact-target check results, dependency graph diff and controlled correction examples.

### E2.12 — Maintainers can demonstrate the migration and reproduce its decisions

**Acceptance.** A final corpus report accounts for every initially observed active document: retained, relocated, consolidated, retired, protected-history, or unresolved. Every mutation has provenance and review; every unresolved item has an owner/reason. No percentage improvement is claimed without its denominator and evaluation method. Rebuilding disposable indexes does not erase accepted decisions.

**Tasks.** Pilot on the newly extracted projects plus their hoisted eta-mu docs. Resolve the measured error cases before a whole-constellation run. Export initial/final manifests, graph snapshots, taxonomy versions, model/prompt/schema identities, decision records and link reports. Record exact duplicates separately from semantically merged documents; track unique-information loss checks, candidate errors, currentness coverage and outstanding external-project decisions. Demonstrate rollback of one move/consolidation/retirement and recovery after reindex.

**Failing fixtures first.** An unaccounted document disappears, accepted tags vanish after reindex, a failed classification is counted as coverage, or a consolidation has no source map. Completion is refused.

**Evidence.** Accounted corpus delta, reproducible indexes, decision audit, browser walkthrough, rollback demonstration and explicit unresolved queue.

## Suggested implementation order

**Inventory → reliable delivery → deterministic references → small-model labeling pilot → currentness retirement → bounded redundancy → reviewed consolidation → placement/scope decisions → document workspace → incremental quality checks → corpus completion.**

The workspace UI can start earlier against a small fixture corpus. Full-scale classification and rewriting must not precede identity/provenance, retry and review safety. Outdated active docs are removed before writing replacements, but only after their original source is inventoried and preserved.

## Review of proposed ownership

Rheos becoming document-centered should not absorb every ingestion, retrieval, indexing and inference implementation. Its distinctive responsibility is governing the file/work lifecycle and presenting actionable relationships. Epiphany keeps the historical/evidence analysis, and the extracted service keeps acquisition/processing jobs. Shared `.cljc` contracts belong in Foresight when the common abstraction is real. This preserves independently useful products instead of replacing one monorepo entanglement with a single oversized service.

## Completion

The epic is complete when contributors can use the system to inspect and improve real constellation documentation with reproducible source evidence and safe mutations, and when the corpus disposition report accounts for the entire admitted scope. A pretty graph, a pile of inferred tags, an unreviewed summarization pass, or a reduced file count alone does not satisfy the outcome.
