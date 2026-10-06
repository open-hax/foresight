---
category: "kanban"
labels: "cognitive-substrate, user-stories, roles, capabilities, authority, 3sp"
type: "story"
story_id: "CS1.06"
points: "3"
title: "CS1.06 — Represent purpose, stories, roles, and bounded authority as first-class state"
dependency: ["preserve-raw-causal-event-graph"]
status: "incoming"
epic: "cognitive-substrate-research-program"
parent: "cognitive-substrate-research-program"
uuid: "purpose-roles-bounded-authority"
---

# CS1.06 — Represent purpose, stories, roles, and bounded authority as first-class state

## User story

**As a** human delegating software work to persistent agents,\
**I want** goals/user stories, composable roles, capabilities, and authority
represented explicitly in the shared substrate,\
**so that** agents can act like team members while their work remains anchored
to a human-readable reason and bounded permission.

## Acceptance criteria

- [ ] A story can represent `as a / I want / so that`, acceptance criteria, evidence, and causal links to derived execution work.
- [ ] Agent roles can be composed dynamically without role identity itself granting a capability or authorization.
- [ ] Presence, attention/recruitment, role, capability, authority, proposed action, and executed effect are separately representable states/events.
- [ ] A small/cheap observer can escalate/recruit more expensive deliberation from an event or disequilibrium without granting that deliberator ambient authority.
- [ ] "Blocked", "uncertain", and "needs human decision" are legitimate successful outcomes of a turn and do not require policy bypass to satisfy the goal.
- [ ] Actions can be traced to both the motivating story/goal and the authority/capability decision that admitted them.

## Tasks within this story

- [ ] Reuse existing Knoxx/Katamorph/Axxium role-capability-policy contracts where they already own semantics.
- [ ] Define the minimum graph representation linking story -> role -> proposal -> admission -> effect.
- [ ] Add failure fixtures for role-without-authority and goal-without-an-accepted-action path.

## Non-goals

Making an LLM "want" policy compliance by prompt alone, treating persistent
presence as continuous permission, or inventing an independent identity for a
tool acting through the user's connector.
