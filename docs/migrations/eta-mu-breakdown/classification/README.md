# First-pass document labeling contract

**Status:** proposed schema and prompt, with synthetic fixtures. No model was called and no real repository corpus was classified in this run.

## Boundary

The small model answers “what kind of document is this, what topics/projects does it discuss, and what needs checking?” It does **not** answer “may I delete it?” or “is this now accepted knowledge?”

`document-labels.schema.json` is a closed Draft 2020-12 JSON schema. Every object rejects additional keys; arrays and strings are bounded. Topic IDs are a seed taxonomy proposal, not a recovered existing Rheos taxonomy. The request provides the allowed project dictionary. Production may generate the topic/project enums from an admitted taxonomy resource and record its revision.

`example-document.md`, `example-input.json` and `example-output.json` are a **synthetic design-proposal fixture**, not model output and not a report of currently implemented integration. Its source digest is computed from the fixture bytes. Confidence values in that example illustrate format only.

## Trusted request and execution policy

Supply the schema and `system-prompt.txt` as trusted instructions, then a structured request containing document text, a raw source identity, full/partial coverage, the allowed project dictionary, taxonomy, and optional already-resolved reference IDs. Do not splice document content into instructions. Run with no shell, filesystem, network or mutation tools; prompt isolation alone is not a security boundary.

The production worker owns a maximum input budget, an output limit (start with 16 KiB), timeout, bounded concurrency, retry count and failure/quarantine queue. Choose those budgets using measurements on the intended local model. A whole-document label must not be produced from an unmarked truncated prefix. Large-document jobs need explicit section coverage and aggregation.

Do not choose a model solely by nominal size. Build a small representative evaluation set spanning guides, plans, ADRs, work items, READMEs, multilingual text, code-heavy docs, stale guidance, and malicious embedded instructions. Measure label quality, abstention, runtime/memory cost and false confidence before bulk inference. No score alone can authorize a destructive action.

## Admission pipeline

1. **Parse:** require one JSON object; reject duplicate keys, non-finite numbers, extra trailing content, Markdown fences and oversized replies.
2. **Validate structure:** validate the closed schema and arrays/strings/enums.
3. **Validate context:** project IDs belong to the request's registry; topic IDs belong to its taxonomy; labels are not duplicated; evidence spans exist; verbatim quotes match the exact input span. A matching quote establishes provenance, not correctness of the inference.
4. **Bind source:** the worker attaches original repository/resource ID, exact revision/path/blob/digest, input coverage, request/job ID, model identity or available weight digest, prompt/schema/taxonomy digests, output digest, attempt count and actual outcome. Unknown hashes or usage remain unknown.
5. **Record proposal:** valid output becomes a provisional label proposal. Semantic review and authorized application are separate operations; invalid output remains a failure/quarantine record.

A production adapter must recheck document revision before applying accepted metadata. The illustrative Python checker validates structure/context for this pack; it is not an ingestion service, model runner, permission system or Clio admission adapter.

## Cache identity

Cache analysis by content digest, extraction policy, complete/partial coverage, schema, taxonomy, prompt and model identity. Reattach a cached analysis to the requesting source identity. Equal bytes do not merge two document identities. Retain observed frontmatter separately from model proposals; do not overwrite user labels or canonical ownership metadata merely because the model disagrees.

## Later phases are different jobs

Currentness checking requires evidence against current source/decisions. Redundancy comparison requires at least two exact sources and section-level context. Consolidation requires a preservation map and reviewed diff. Placement requires owner/scope assessment and link rewrites. Retirement requires an authorized revision-checked mutation. Keeping these separate makes first-pass JSON classification cheap without granting it unjustified authority.

## Local checks

Run from the pack root:

```bash
python checks/check_pack.py
```

This requires Python, jsonschema and networkx already installed. It makes no network calls and runs no repository build, install, model, migration or publication operation. Read `checks/validation-report.json` for the actual scope and result.
