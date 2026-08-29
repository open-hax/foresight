---
title: "Workstream: Alpha + Katamorph portable workflow law stack"
summary: "Tracks the stacked dependency graph connecting Foresight Alpha laws, Katamorph portable workflow contracts, and eta-mu Rheos consumers."
category: "tracking"
created: "2026-08-29"
---

# Workstream: Alpha + Katamorph portable workflow law stack

Inventory date: 2026-08-29.

This is the largest active cross-repository stack in Foresight. It should be treated as a dependency graph, not as twenty-some independent Alpha pull requests.

## Signal

Alpha is consuming Katamorph for portable condition/action/invocation/workflow contracts while tightening its own Artifact/Event/Reaction/Markdown laws. The current stack has a concrete upstream blocker and a concrete repair path:

- [open-hax/katamorph#22](https://github.com/open-hax/katamorph/pull/22) explicitly identifies itself as the blocker for Foresight #12 and #15 because those PRs pin commits from its unmerged integration branch.
- [open-hax/foresight#41](https://github.com/open-hax/foresight/pull/41) repairs the broken Katamorph pin on Foresight `main` and lands the JVM law gate that should have caught it.
- eta-mu's Rheos condition stack consumes the same Katamorph condition kernel, so the bad pin also appears downstream there.

## Foresight Alpha stack

### Event/materialization lane

- [#6](https://github.com/open-hax/foresight/pull/6) event draft laws
- [#17](https://github.com/open-hax/foresight/pull/17) runtime identity/time at materialization
- [#20](https://github.com/open-hax/foresight/pull/20) portable EventDraft payloads
- [#26](https://github.com/open-hax/foresight/pull/26) reader-valid EventDraft fixture IDs

### Markdown / Artifact lane

- [#9](https://github.com/open-hax/foresight/pull/9) declarative Markdown profile selection
- [#10](https://github.com/open-hax/foresight/pull/10) fail-closed profile-registry composition
- [#14](https://github.com/open-hax/foresight/pull/14) Markdown -> validated Artifact resolution
- [#16](https://github.com/open-hax/foresight/pull/16) frontmatter decode provenance through resolution

### Diagram lane

- [#7](https://github.com/open-hax/foresight/pull/7) Mermaid workflows as lawful graphs
- [#8](https://github.com/open-hax/foresight/pull/8) Mermaid graph projection into Alpha artifacts

### Reaction / action / invocation lane

- [#11](https://github.com/open-hax/foresight/pull/11) reaction-registry composition and binding
- [#12](https://github.com/open-hax/foresight/pull/12) reactions through Katamorph action registry
- [#15](https://github.com/open-hax/foresight/pull/15) pure invocation planning from reactions
- [#13](https://github.com/open-hax/foresight/pull/13) shared Katamorph InvocationRequest
- [#18](https://github.com/open-hax/foresight/pull/18) typed Katamorph input references

### Portability envelope lane

- [#19](https://github.com/open-hax/foresight/pull/19) portable semantic payload maps
- [#21](https://github.com/open-hax/foresight/pull/21) portable integer identities
- [#22](https://github.com/open-hax/foresight/pull/22) portable extensible semantic records

### Gate / recovery lane

- [#24](https://github.com/open-hax/foresight/pull/24) Alpha JVM law workflow
- [#41](https://github.com/open-hax/foresight/pull/41) repair Katamorph pin + install the law gate on current main

Foresight #41 states that it supersedes #24 by carrying the workflow unchanged on top of current `main`; if #41 lands as designed, #24 should not remain a parallel authority.

## Katamorph stack

### Workflow composition

- [#8](https://github.com/open-hax/katamorph/pull/8) typed step composition
- [#10](https://github.com/open-hax/katamorph/pull/10) structural port compatibility
- [#17](https://github.com/open-hax/katamorph/pull/17) shared portable workflow/input values
- [#22](https://github.com/open-hax/katamorph/pull/22) integration branch for condition/action registry, workflow graph, and relation schemas

### Action / invocation contracts

- [#11](https://github.com/open-hax/katamorph/pull/11) portable semantic action registry
- [#13](https://github.com/open-hax/katamorph/pull/13) provider bindings separated from selection
- [#14](https://github.com/open-hax/katamorph/pull/14) portable InvocationRequest ownership
- [#15](https://github.com/open-hax/katamorph/pull/15) invocation application against action ports
- [#16](https://github.com/open-hax/katamorph/pull/16) portable input-reference language

### Portability / CI support

- [#18](https://github.com/open-hax/katamorph/pull/18) portable safe-integer path indices
- [#19](https://github.com/open-hax/katamorph/pull/19) runnable sandbox-bundle validation
- [#4](https://github.com/open-hax/katamorph/pull/4) lint/editor configuration cleanup

## Eta-mu consumers

- [open-hax/eta-mu#284](https://github.com/open-hax/eta-mu/pull/284) — Rheos consumes Katamorph condition laws.
- [open-hax/eta-mu#285](https://github.com/open-hax/eta-mu/pull/285) — Rheos compose filters route through shared conditions; this is one downstream location affected by the bad Katamorph condition revision called out in Foresight #41.
- [open-hax/eta-mu#287](https://github.com/open-hax/eta-mu/pull/287) — partial frontmatter decode provenance; aligns with Foresight #16 without being a hard dependency.
- [open-hax/eta-mu#281](https://github.com/open-hax/eta-mu/pull/281) — namespace architecture documentation adjacent to the same pure-law/runtime-edge separation.

## Dependency order worth preserving

```text
Katamorph portable primitives
  -> action / invocation / input-reference laws
  -> workflow compatibility + graph laws
  -> Katamorph integration branch (#22)
  -> Foresight Alpha consumers (#12/#15 and related lanes)
  -> eta-mu Rheos adapters
```

Alpha owns structural integrity of Foresight artifacts/reactions. Katamorph owns reusable portable contract language. Rheos remains an adapter/coordination consumer. Do not solve merge pressure by copying Katamorph grammar back into Alpha or Rheos.

## Immediate blockers

1. **Katamorph #22 must be rebased and its accidental CI/test deletions resolved before it becomes mergeable.** Its own PR body identifies the removals and recommends restoring them unless explicitly justified.
2. **Foresight main must compile Alpha under a real gate.** #41 is the current repair path and carries the #24 workflow.
3. **Stacked branches are hiding signal.** Foresight #41 notes that many Alpha PRs currently receive effectively no meaningful status context because review automation is skipped on stacked bases. The gate needs to run on the branches that actually determine merge safety.
4. **Superseded PR cleanup matters.** Once a repair PR absorbs an older branch exactly, leaving both open makes the dependency graph harder to reason about.

## Exit criteria

This workstream is ready to flatten when:

- Katamorph's action/invocation/workflow integration is merged from a reviewable branch with current tests and CI preserved;
- Foresight pins merged Katamorph revisions rather than invisible integration commits;
- Alpha's JVM law gate executes on main and on every relevant PR path;
- Event, Artifact, Reaction, Markdown, and invocation payloads use one portable-value kernel rather than parallel host-value rules;
- eta-mu consumes the merged Katamorph condition surface without carrying a divergent predicate language.
