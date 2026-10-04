---
category: "kanban"
labels: "codex-cloud, github, forks, authorization"
type: "epic"
points: "13"
title: "EPIC: Codex Cloud repository selection expresses access, not the Foresight source graph"
dependency: ["codex-cloud-self-hydrating-foresight"]
priority: "P1"
status: "incoming"
uuid: "codex-cloud-repository-access"
---

# EPIC: Codex Cloud repository selection expresses authenticated access

## Outcome

Codex Cloud repository selection is intentionally smaller than the Foresight
source graph. `open-hax/foresight` is always the root workspace repository;
children are normally materialized through Foresight's pinned direct-submodule
graph. Additional repositories are selected in the environment only when a task
needs first-class authenticated access to that repository, especially push/PR
work.

Where the Promethean development-fork process is activated, Codex uses the
validated development fork mapping instead of selecting both every fork and
every upstream by habit.

## Context

Current [Codex Cloud guidance](https://learn.chatgpt.com/docs/environments/cloud-environments)
separates repository selection and connected-account permission. The
environment must include the repository and the account must have the
permission needed for a provider operation. Public source fetch and provider
push are distinct capabilities. Guidance checked 2026-10-04.

Foresight separately owns the suite topology. The existing
`fork-dev-origins` epic owns the transition to mapped `riatzukiza/*`
development forks, org promotion, and operational activation. This epic must
reuse that mapping and must not create a conflicting fork policy.

At this planning baseline `config/dev-origins.edn` and its validator do not
exist, and the Promethean process contract records activation as
`:not-activated`. A planned mapping is not live write authority.

## Core identity

**Submodules answer “what source belongs to this root?”**

**Codex repository selection answers “which repositories need provider-level
access in this environment?”**

## Dependency hierarchy

The `dependency` on `codex-cloud-self-hydrating-foresight` names the epic's
intended delivery prerequisite: CC2.04 cannot complete before CC1.05 proves
source readiness. The story graph permits preparation after each immediate
prerequisite is met; actual admission and any inherited dependency behavior
remain subject to Rheos. See the sibling epic's full story graph.

- CC2.01 depends on CC1.01's contract and can proceed before full cloud setup.
- CC2.02 depends on CC2.01 and external `dev-origin-map`, owned by the existing
  `fork-dev-origins` epic. It consumes that output rather than implementing it.
- CC2.03 depends on CC2.02 and CC1.03's hydrated target.
- CC2.04 depends on CC2.03 and CC1.05's actual provider acceptance.

This epic does not require the entire fork-development epic to finish before
preactivation access can be proved. Any live activated-fork claim additionally
requires trusted per-repository evidence for the existing process's verified
map, protection, required gate, and staging controller. Missing evidence never
activates a fork by inference.

## Children

- `codex-cloud-access-tiers` — define root, hydrated-child, and first-class
  authenticated repository tiers, 3 points.
- `codex-cloud-fork-selection-projection` — derive writable repository choices
  from the validated development-origin mapping, 3 points.
- `codex-cloud-child-remote-configuration` — configure child remotes for the
  active fork/upstream policy without inventing a second source graph, 3 points.
- `codex-cloud-access-acceptance-matrix` — prove root-only work, one-child
  write work, and both independent access-denial conditions, 4 points.

Tasks are embedded in those four stories. The 13-point epic total is their
sum, not an additional task. Dependency UUIDs identify cards; no independent
board engine or validation is introduced here.

## Definition of done

Operators no longer need to select every Foresight member repository and every
personal fork “just in case.” Source inspection and named root validation use
the root bootstrap; individual child builds require their own selected tools.
A task that must push to one child adds the first-class
repository identity required for that write path. Fork/upstream choices follow
the currently activated Foresight fork policy, and missing authorization fails
clearly.

## Verification and risks

CC2.01 defines examples; CC2.02/03 prove pure selection and local remote
fixtures. CC2.04 records live root-read, one-child-write, environment-exclusion,
and account-permission-denial outcomes. Actual branch/ref/commit and sanitized
failure evidence are required for each case. Current authorized preactivation
behavior can be proved without pretending a planned fork is activated.

The current cloud environment surface and legacy Code Review environments are
separate: [OpenAI's legacy guide](https://learn.chatgpt.com/docs/environments/cloud-environment)
still supports Code Review/integrations, while [Code Review usage](https://learn.chatgpt.com/docs/pricing)
is accounted for on GitHub reviews. Publishing a current environment supplies
neither extra review quota nor a reviewed merge approval.

## Out of scope

- Granting credentials or access beyond the operator's existing GitHub
  permissions.
- Bypassing repository protection or the Promethean promotion gates.
- Treating environment inclusion as evidence that a fork process is activated.
