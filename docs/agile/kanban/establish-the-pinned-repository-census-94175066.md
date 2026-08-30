---
category: "kanban"
labels: "research, evidence, inventory"
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
type: "task"
write-id: "1788050190884-0.og0f5f03ly8oz5hwnm4"
title: "Establish the pinned repository census"
priority: "P1"
status: "in_progress"
uuid: "a1d0645d-49a6-4e8d-b744-4e1a94175066"
created_at: "2026-08-30T00:24:05.963Z"
---

# Establish the pinned repository census

Foresight PR #60 introduces an orientation-grade census over exact repository
revisions. The census must preserve repository identity separately from path
occurrences and must retain every inaccessible, malformed, unsupported, or
bounded observation as explicit evidence rather than empty success.

## Exit

- Accept only full immutable root revisions and safe GitHub repository names.
- Verify commits, trees, regular `.gitmodules` blobs, and exact Gitlink paths.
- Resolve Git-relative remotes against the parent repository and retain local or
  unsupported remotes as explicit gaps.
- Preserve per-revision manifest digests and unique declaration identities.
- Fail closed when traversal bounds leave an unprocessed frontier.
- Produce a deterministic artifact from an exact-head hosted run.
- Resolve independent review findings before merging PR #60.

## Boundary

This evidence projection does not infer ownership, lineage, lifecycle, product
identity, continuation, consolidation, or retirement.

---
Canonical GitHub projection: issue #61 (https://github.com/open-hax/foresight/issues/61). Active implementation and evidence lane: PR #60 (https://github.com/open-hax/foresight/pull/60).
---