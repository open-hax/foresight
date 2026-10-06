---
category: "kanban"
labels: "codex-cloud, github, authorization, 3sp"
type: "story"
story_id: "CC2.01"
points: "3"
title: "CC2.01 — Separate source hydration from first-class repository access"
dependency: ["codex-cloud-bootstrap-contract"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-repository-access"
parent: "codex-cloud-repository-access"
uuid: "codex-cloud-access-tiers"
---

# CC2.01 — Separate source hydration from first-class repository access

## Context and dependencies

Requires CC1.01's source-plan contract. This story can start before the
self-hydration epic finishes because it defines access independently of Git
execution; live end-to-end access acceptance waits for CC1.05 in CC2.04.

## Outcome

Foresight documents a small access model that prevents the Codex environment
repository list from becoming a duplicate project manifest.

## Scope

Describe the task capability, selected provider repository, connected-account
permission, and hydrated-source identity separately. Derive source identities
from the root contract; select additional access only for a named capability.

## Tasks within this story

- [ ] Define the three tiers below and the inputs needed to distinguish source
  readability from provider connection and write authorization.
- [ ] Write root-only inspection/validation and one-child feature-PR examples,
  naming the minimum repositories and required permissions in each.
- [ ] Define fail-closed access outcomes for missing environment inclusion and
  missing account permission as distinct cases for CC2.04.
- [ ] Record the provider/environment surface and official documentation used
  to substantiate each capability claim; leave untested behavior provisional.
- [ ] Hand the access model to CC2.02 without duplicating the source manifest.

## Non-goals

Provisioning repositories/credentials, implementing a fork map, asserting live
provider access, or treating public Git source fetch as provider write access.

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
Review the examples against CC1.01's exact source-plan output and current
provider guidance; no live push or environment publication is needed here.

## Risks

Environment inclusion cannot grant account permission. Public HTTPS fetch may
work even when a provider operation is unavailable; name the capability being
tested instead of equating all repository access with one permission bit.

## Anti-patterns

- “Select every upstream and every fork so it probably works.”
- Treating the Codex environment selector as Foresight's canonical source graph.
