---
category: "kanban"
labels: ["work-item", "persona", "competency", "readiness", "shape"]
type: "story"
title: "Define WorkItem Persona Participant and Readiness shapes once"
priority: "P1"
status: "incoming"
uuid: "e6e34d1a-49c7-5aba-9a3b-25af22af6834"
created_at: "2026-10-07T21:16:00Z"
parent: "7b0ad3c9-f579-56de-8052-8e79586bd5e2"
---

# Define WorkItem, Persona, Participant, and Readiness shapes once

## Story

**As a** Producer or Tech Lead  
**I want** work readiness to be expressed in one representation-independent
WorkItem shape  
**so that** planning and review do not depend on where a work item is stored.

## Acceptance

- Define portable shapes for WorkItem, Persona, competency profiles, participant
  slots, support, requirements, authority requirements, and readiness
  diagnostics.
- Human competency does not reuse existing `:cap/*` runtime/tool capability
  semantics.
- Personas provide defaults/expectations and never grant permissions.
- A WorkItem can independently identify beneficiary, consumer, implementer,
  acceptor, technical reviewer, domain reviewer, investigator, decision owner,
  and operator.
- Support may satisfy explicitly substitutable competency/context requirements.
- Support, competency, persona, or model intelligence cannot manufacture
  authority.
- Independence constraints are explicit.
- Readiness operates on decoded semantic values, not YAML maps, BSON documents,
  or database rows.
- Diagnostics distinguish missing competency, context, support, authority,
  participant, reviewer, independence conflict, and invalid work shape.
- Legacy unprofiled cards remain valid and are not silently classified.
- Initial Rheos readiness remains advisory.

## Evidence

At least one outcome story and one derived implementation task produce the same
readiness result after EDN and YAML round trips.
