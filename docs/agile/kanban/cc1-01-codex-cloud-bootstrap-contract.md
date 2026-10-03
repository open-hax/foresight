---
category: "kanban"
labels: ["codex-cloud", "workspace", "law", "3sp"]
type: "story"
story_id: "CC1.01"
points: "3"
title: "CC1.01 — Define the Codex Cloud Foresight bootstrap contract"
priority: "P1"
status: "incoming"
epic: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-bootstrap-contract"
---

# CC1.01 — Define the Codex Cloud Foresight bootstrap contract

## Outcome

Foresight has one explicit, testable contract for what a clean Codex Cloud
workspace must reconstruct before work starts.

## Acceptance criteria

- [ ] GIVEN `open-hax/foresight` at a pinned root commit WHEN bootstrap starts
  THEN the authoritative direct checkout set is derived from committed Foresight
  declarations, not from a separately hand-maintained Codex repository list.
- [ ] GIVEN a direct `.gitmodules` entry WHEN its gitlink is inspected THEN the
  exact recorded object is the requested revision; branch tips do not replace it.
- [ ] GIVEN a repository nested inside a direct child WHEN bootstrap runs THEN it
  is not initialized or promoted unless Foresight separately declares it as a
  direct source.
- [ ] GIVEN a required direct source that cannot be fetched WHEN bootstrap runs
  THEN the result is unavailable/failing with the repository and stage named.
- [ ] GIVEN disagreement among `.gitmodules`,
  `src/foresight/project.cljc`, and root routing metadata WHEN validation runs
  THEN bootstrap fails before ordinary task execution.

## Failing fixtures first

- Manifest entry without a matching gitlink.
- Gitlink without a declared project source.
- Duplicate or escaping child path.
- Missing/unreachable required child.
- Nested child Git repository incorrectly treated as another root source.

## Verification

The implementation story must add deterministic tests for the contract before
the cloud-specific adapter is considered complete.

## Anti-patterns

- Do not encode the full constellation a second time in a Codex-only manifest.
- Do not replace pinned gitlinks with `update --remote`.
- Do not infer success from an empty submodule directory.
