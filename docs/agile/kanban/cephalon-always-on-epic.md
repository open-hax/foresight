---
uuid: "25e3a688-09b5-4e9a-8765-9b13944b1a00"
title: "EPIC: a responsive creative cephalon keeps making and sharing"
type: "epic"
status: incoming
priority: P1
points: 16
labels: "knoxx, cephalon, creativity, runtime"
---

## Context

The operator wants the fun OpenHax head to converse while independent workers
keep creating music, lyrics, MIDI and visual art. Bluesky is the preferred
publication outlet; Discord is a conversational and occasional sharing surface.
See `docs/notes/2026-10-06-cephalon-always-on.md` for evidence and ownership.

## Outcome

A supervised, configured Knoxx keeps producing addressable original artifacts.
Operator interaction is admitted independently and never cancels, replaces or
steers background work. Actual artifacts, tool results and publications can be
inspected through the same user/agent interfaces.

## Scope

- Head admission: `25e3a688-09b5-4e9a-8765-9b13944b1a01` (3 points).
- Creative cycles: `25e3a688-09b5-4e9a-8765-9b13944b1a02` (5 points).
- Triggers and publication: `25e3a688-09b5-4e9a-8765-9b13944b1a03` (3 points).
- Recovery and deployment: `25e3a688-09b5-4e9a-8765-9b13944b1a04` (5 points).

## Non-goals

Replace the agent runtime, duplicate Rheos or Clio, change unrelated identities,
introduce paid provider quotas, or equate successful prompting with durable laws.
Live Discord voice/STT and Codex chat dispatch are independent capabilities.

## Acceptance criteria

1. A blocked maker does not block head admission or abort on user conversation.
2. Three unattended clock cycles save inspectable artifacts; at least two
   creative media are represented. Every failed cycle reports its real reason.
3. A configured non-clock event produces work through the same bounded path.
4. A real Bluesky publication and Discord share have native IDs/receipts.
   Publication is deduplicated and governed by explicit frequency policy.
5. Restart recovery preserves work identity; there is one gateway owner and no
   duplicate publication. Placement availability is measured and documented.

## Verification

Child stories supply failing laws first, full relevant backend gates and a
runnable human verification artifact. Live evidence names the served revision,
jobs, paths, publication IDs, response timings and restart observations.

## Risks

Provider latency/quotas, local host sleep, sticky session conflicts, replay of
external effects, bot feedback loops, sensitive context and unbounded backlog.
