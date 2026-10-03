---
category: "kanban"
labels: ["codex-cloud", "acceptance", "workspace", "3sp"]
type: "story"
story_id: "CC1.05"
points: "3"
title: "CC1.05 — Prove a fresh root-only Codex Cloud environment end to end"
blocked_by: ["codex-cloud-direct-child-hydration", "codex-cloud-toolchain-validation"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-root-only-acceptance"
---

# CC1.05 — Prove a fresh root-only Codex Cloud environment

## Outcome

The design question is settled with executable evidence: a newly created cloud
task can start from the Foresight root and reconstruct the read/build workspace
without hand-selecting every child solely for checkout.

## Acceptance criteria

- [ ] Create a new Codex Cloud environment/task with `open-hax/foresight` as
  the project root and without selecting all child repositories merely for
  source hydration.
- [ ] Run the Foresight bootstrap from an otherwise fresh task workspace.
- [ ] Verify every required direct child exists at the root-recorded gitlink
  revision.
- [ ] Verify the project validation, generated guide, and workspace inventory all
  succeed.
- [ ] Verify a second bootstrap run is idempotent.
- [ ] Verify no nested repository became an unintended workspace root.
- [ ] Verify an intentionally unavailable direct repository produces a clear
  failure in a controlled negative test rather than a false pass.

## Evidence

Record the root commit, environment/setup revision, direct source/revision table,
tool versions, gate results, and any repository whose provider authorization was
required.

## Follow-on boundary

This story proves source hydration and read/build readiness. It does **not**
prove that Codex can push to every child; that belongs to the sibling repository
access epic.
