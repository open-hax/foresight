---
status: incubating
created: 2026-10-20T08:04:58.093684Z
source-session: codex/stabilization-portfolio
source-task: Freeze complete owner-bounded PR census and reconcile existing work
p-efficiency: 0.72
p-friction: 0.65
p-skill-candidate: 0.85
promoted-to: ""
rejected-reason: ""
---

## Problem
A public owner repository list, successfully paginated PR file list, and matching UUID each looked more complete than the evidence justified.

## Pattern
Enumeration completeness has separate boundaries: authenticated private visibility; repository and PR final pages; per-item hard API caps; current source consistency; and fork-qualified card identity. GitHub capped two native file lists at 3000 although changedFiles was over 15000. Fork and PR-head card copies can share UUIDs without sharing accepted state.

## Candidate skill outline
- Discover authenticated ownership and actual fork network metadata, including renamed forks.
- Record explicit terminal pages and recheck complete membership plus exact source pointers.
- Compare per-item declared totals with captured records; repair hard file caps with immutable merge-base tree deltas, retaining different rename semantics.
- Index repository/revision/path/UUID observations; distinguish authored inputs and admitted board state.
- Keep missing owners, unmatched work and malformed relationships explicit. Do not manufacture tasks or acceptance.
- Preserve private metadata in excluded local evidence; public manifests carry only safe summaries and digests.

## Better path
Use authenticated repository inventory first, validate advertised item totals immediately, and normalize source-qualified card references with token indexing before graph construction. Batch immutable reads, retain native custody, and keep semantic acceptance separate from census completion.

## Receipt refs
- 873e646ca7813784157edfe87c3b0fd9b958f7a63395673ed6f9998c4e9d931d
- 62874eae7ee052cce39d658402326449e617a49b0965abe6795a93a2ad6fe341
