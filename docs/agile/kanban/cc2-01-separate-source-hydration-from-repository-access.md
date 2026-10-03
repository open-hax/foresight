---
category: "kanban"
labels: ["codex-cloud", "github", "authorization", "3sp"]
type: "story"
story_id: "CC2.01"
points: "3"
title: "CC2.01 — Separate source hydration from first-class repository access"
priority: "P1"
status: "incoming"
epic: "codex-cloud-repository-access"
uuid: "codex-cloud-access-tiers"
---

# CC2.01 — Separate source hydration from first-class repository access

## Outcome

Foresight documents a small access model that prevents the Codex environment
repository list from becoming a duplicate project manifest.

## Access tiers

1. **Root repository** — `open-hax/foresight`; selected for every Foresight
   cloud environment.
2. **Hydrated direct child** — materialized at a pinned gitlink by Foresight;
   sufficient for source inspection/build/test when provider-level write access
   is not needed.
3. **First-class authenticated repository** — additionally selected in Codex
   Cloud because the task needs provider-level access such as fetch of protected
   content, push, or PR work for that repository.

## Acceptance criteria

- [ ] Root bootstrap remains independent of a manually duplicated complete child
  repository list.
- [ ] A child is promoted to first-class environment access only for a named task
  capability or provider restriction, not merely because it is a submodule.
- [ ] Environment inclusion is never described as granting GitHub permission;
  account/repository authorization remains a separate prerequisite.
- [ ] A missing required first-class repository fails at the provider operation
  with a useful repository identity rather than silently switching remotes.

## Verification

Document at least one root-only task and one child-write task and show which tier
each repository occupies.

## Anti-patterns

- “Select every upstream and every fork so it probably works.”
- Treating the Codex environment selector as Foresight's canonical source graph.
