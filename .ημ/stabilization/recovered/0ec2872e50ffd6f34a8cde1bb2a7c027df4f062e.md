# OpenHax as a creative character: recovered intent and memory drift

License: GPL-3.0-or-later.

## Status and scope

This is an observed source map, a derived diagnosis, and a provisional delivery
design. It is not a claim that character, mood, graph physics, or social agency
has been implemented or deployed. The current runtime was inspected without
starting an agent, restarting a maker, changing contracts, or publishing.

The preceding [always-on outcome](2026-10-06-cephalon-always-on.md) and
[operational evidence](../verification/cephalon-always-on-20261006.md) remain
valid within their recorded limits. This note recovers the next product shape;
it does not turn the earlier scheduling/publication work into proof of memory
continuity. The existing epic is `25e3a688-09b5-4e9a-8765-9b13944b1a00`.

## Recovered human intent

The October 7 human steering supplies the following requirements directly:

> He needs some outside information.

> I want him to be interacting with memory through the graph.

> The graph has to physically work the way we talk about it in other places.
> Like in the fork_tales experiment.

> He is a character. He is an actor playing a role. His role is a creative person.

The user also names feed reading, liking, commenting, following, unfollowing,
an independent presence, evolving personality, and mood as a separate model.

**Derived invariant:** encounters must change the character's persistent state,
that state must change what he notices and recalls, and those changes must
become visible in his choices and work. Identity and the creative role persist
across the maker, conversation, and social facets. Personality evolves slowly;
mood changes on a shorter timescale. Both have causal evidence.

The product needs a closed loop:

```mermaid
flowchart LR
  W[World encounters and social responses] --> E[Admitted observations]
  E --> G[Persistent graph and field]
  G --> M[Separate mood model]
  G --> R[Graph recall with traversal trace]
  M --> R
  R --> C[Creative character chooses]
  M --> C
  T[Lasting traits and relationships] --> C
  C --> A[Read, relate, make, publish, or remain quiet]
  A --> O[Observed outcomes]
  O --> G
  O --> T
```

The native fifteen-minute clock remains the creative scheduling authority.
Continuous intake and inexpensive field updates can run between opportunities.
A clock turn consumes actual changed state; it should not repeatedly substitute
the same task instructions for encounters. The historical proposal to remove
all LLM timers is an earlier design proposal, not authority to replace this
deployment's explicitly selected clock.

## Observed source map

The source map was recovered from local inspections on October 7. The portable
links below pin repository, revision, path and line; on October 8, each linked
Git blob was verified byte-identical to its inspected checkout file. This
later provenance readback is not a new runtime or deployment observation.
External repositories remain evidence donors, not code automatically admitted
to Foresight.

| Anchor | What it actually says or implements | Epistemic tier |
| --- | --- | --- |
| [Promethean Eidolon fields](https://github.com/octave-commons/promethean/blob/2c878b6a565f5ac8cc09bf503a6dec5ad3efc00f/docs/design/fields/eidolon-fields.md#L7) | Local tension, charge, gradients, history, and changing attention; emotion and habit are described as field patterns. | Observed historical design |
| [Nooi](https://github.com/octave-commons/promethean/blob/2c878b6a565f5ac8cc09bf503a6dec5ad3efc00f/docs/design/nooi.md#L26) | Field cells retain local pressure, charge, trails, and binding sites; particles both read and change the field. | Observed historical design |
| [Duck's attractor states](https://github.com/octave-commons/promethean/blob/2c878b6a565f5ac8cc09bf503a6dec5ad3efc00f/docs/hacks/notes/ducks-attractor-states.md#L133) | Outside inputs and self-reflection feed a persistent feedback buffer, changing operational modes. | Observed historical interpretation, not a measured affect model |
| [Fork Tales corrections](https://github.com/octave-commons/fork_tales/blob/f4c43d7b9c832a54bc5d224cb4d5d2497c04423b/docs/notes/system_design/2026-02-20-design-hole-responses-field-and-collisions.md#L14) | Decaying sparse fields, probabilistic paths, flexible intent bonds, immutable emitted ownership, seed/mantle/intent embeddings, and different friction for presences, nexus, and daimoi. | Observed design clarification |
| [Fork Tales runtime](https://github.com/octave-commons/fork_tales/blob/f4c43d7b9c832a54bc5d224cb4d5d2497c04423b/part64/code/world_web/daimoi_probabilistic.py#L439) | Contains actual semantic-force and collision machinery, with separately tested graph/presence/nooi surfaces. | Observed source; not live verification here |
| [Cephalon MVP](https://github.com/open-hax/openplanner/blob/582efb7ebbdc542650fd239bfc5f2de076316c53/packages/agents/cephalon/packages/cephalon-cljs/docs/notes/cephalon/cephalon-mvp-spec.md#L18) | Memories derive from events, carry provenance and links, and share identity/preferences across session facets. | Observed historical design |
| [Eidolon retrieval notes](https://github.com/open-hax/openplanner/blob/582efb7ebbdc542650fd239bfc5f2de076316c53/packages/agents/cephalon/packages/cephalon-cljs/docs/notes/cephalon/cephalon-eidolon-field-concept.md#L25) | Stable content vectors and contextual vectors coexist; nexus keys support associative expansion. | Observed historical proposal |
| [Daimoi retrieval notes](https://github.com/open-hax/openplanner/blob/582efb7ebbdc542650fd239bfc5f2de076316c53/packages/agents/cephalon/packages/cephalon-cljs/docs/notes/cephalon/cephalon-daimoi-v01.md#L1) | Bounded walkers expand semantic seeds through structural neighborhoods and retain reasons. | Observed proposed approximation |
| [Event-native engagement](https://github.com/open-hax/openplanner/blob/582efb7ebbdc542650fd239bfc5f2de076316c53/packages/agents/cephalon/packages/cephalon-ts/docs/event-native-engagement-spec.md#L1) | Feed intake, novelty, social timing, live field change, and action grounded in actual encounters. | Observed proposal; its timer replacement is not selected for the current deployment |
| [CLJS mood tool](https://github.com/open-hax/openplanner/blob/582efb7ebbdc542650fd239bfc5f2de076316c53/packages/agents/cephalon/packages/cephalon-cljs/src/promethean/tools/self.cljs#L21) | Returns a `self.set_mood` action map. The searched cephalon sources contain its declaration/registration but no matching state transition consumer. | Observed bounded source search |
| [TS field](https://github.com/open-hax/openplanner/blob/582efb7ebbdc542650fd239bfc5f2de076316c53/packages/agents/cephalon/packages/cephalon-ts/src/mind/eidolon-field.ts#L16) | An in-memory map of eight keyword-driven counters, with decay applied on ingestion. | Observed donor implementation |
| [TS local graph](https://github.com/open-hax/openplanner/blob/582efb7ebbdc542650fd239bfc5f2de076316c53/packages/agents/cephalon/packages/cephalon-ts/src/mind/local-mind-graph.ts#L31) | Persists nodes/edges and increases encounter weights; exposes channel/link summaries. | Observed donor implementation |
| [Knoxx hydration](https://github.com/open-hax/knoxx/blob/40221a69f7fff675b46614d9ab318b4ef52f3786/backend/src/cljs/knoxx/backend/infra/agent/hydration.cljs#L102) | Triggered keyword-based conversational recall, vector retrieval, authorization filtering, and a short prompt snippet. | Observed implementation; same path inspected at served revision `2644fc6` |
| [Knoxx memory/graph clients](https://github.com/open-hax/knoxx/blob/40221a69f7fff675b46614d9ab318b4ef52f3786/backend/src/cljs/knoxx/backend/infra/openplanner/memory.cljs#L268) | `memory_search` uses vector search; `graph_query` is a separate graph-memory request. | Observed implementation |
| [OpenPlanner graph memory](https://github.com/open-hax/openplanner/blob/582efb7ebbdc542650fd239bfc5f2de076316c53/src/routes/v1/graph.ts#L4496) | Vector seeds, bounded cost traversal, force/trail inputs, semantic reinforcement and persisted daimoi trails. | Observed source; this query can write feedback |
| [Knoxx Bluesky tools](https://github.com/open-hax/knoxx/blob/40221a69f7fff675b46614d9ab318b4ef52f3786/backend/src/cljs/knoxx/backend/domain/bluesky/bluesky.cljs#L356) | Existing timeline, likes/unlikes, follows/unfollows, notifications, thread access, and replies through publication. | Observed source; live maker permissions differ |

There is relevant affect/field material. This investigation did not establish a
complete, deployed, separately trained mood model hidden elsewhere. The named
historical formulations also differ: the older Eidolon note describes scalar
tension gradients, while the later Fork Tales clarification describes deposited
sparse vector winds. Those representations need an explicit reviewed mapping;
their shared vocabulary does not establish mathematical identity.

## Actual deployment evidence

The observation transcript is
[native-memory-observation.json](../../.ημ/review-evidence/cephalon-character/20261007-native-memory-observation.json).
It is a transcription of selected command output, not a raw provider response.

Served identity remains `knoxx-backend:social-local-2644fc6-creative`, image
`sha256:ce0123ef17bade99603ea6ea19967c99c62869118d1cf52c057ab6a72500bf14`.
The read-only diagnostic passed all 12 checks, retained seven warnings, and
verified the 240 contract hashes. Its sampled active list was empty; this is a
timed observation, not an ownership reservation. Native music failures remain
visible even when a cycle also saves/publishes other work.

The deployed maker contract has passive memory `enabled? true`, `mode triggered`,
`k 6`. Its allowed tools cover creation, local files/bash, own publication,
profile/author-feed reads, Discord send, and delegation. It does not expose
`memory_search`, `memory_session`, `graph_query`, timeline, like, follow, or
unfollow. Replies through `bluesky.publish` are technically possible, but this
does not establish a discovered-conversation loop.

The responsive head has memory hydration disabled and denies heavy memory,
graph, feed, and maker tools. That narrowing served responsiveness. A future
shared character snapshot can feed the head without placing expensive discovery
or traversal on its acknowledgment path.

Eight maker runs admitted from `09:52:25.751Z` through `11:37:25.338Z` were
inspected. Each persisted `memoryHydration.mode = triggered`, an empty `hits`
array, and elapsed time between 791 and 1447 ms. Their native tool receipts
contain no memory, graph, or social discovery tool invocation. This observation
does not exclude public feed reads performed inside bash or continuity in the
sticky session. It proves that the inspected automatic retrieval supplied no
memories and those runs did not explicitly traverse the graph.

At `11:47:22.090Z`, the local store had 9,950 events, 3,017 event chunks,
12,838 graph edges and 6,544 graph-node embeddings. At a subsequent bounded
metadata read, all 3,017 event chunks belonged to source `knoxx`, project
`knoxx-session`; there is no basis to call the database empty. The inspected
semantic field cells, force samples, semantic edges, daimoi trails, edge claims,
and compacted vectors each had zero records.

Two read-only memory API queries returned HTTP 200: a short artwork/music query
returned three hits at `11:48:54.124Z`; replaying the latest persisted hydration
query under the diagnostic API principal returned six hits at `11:49:43.426Z`.
The runtime selected the direct Mongo client. These reads neither reproduce the
maker's original authorization context nor freeze the earlier corpus, so they
do not identify a proven causal bug. They narrow investigation to the automatic
path, its scope, filters, and index timing. The repair must retain authorization,
not use an administrative principal to make the empty result disappear.

## Where implementation drifted

| Intended relationship | Observed substitution or disconnection | Required correction |
| --- | --- | --- |
| Outside encounters change future behavior. | The live task is dominated by repeated production instructions and fallback recipes. Its allowed surface lacks timeline/notification/relationship operations. The Fork Tales composer role also mandates a recurring motif vocabulary. | Separate the durable creative role from production recipes; admit outside evidence and route it into state and recall before choices. Keep lore as a chosen lens, not a compulsory motif for every output. |
| Same character across facets. | Identity/persona lives mainly in role prompts and sticky session history. The inspected field is a separate donor runtime, not an established shared state path in served Knoxx. | Bind a stable presence identity to maker, head, and social facets, with one reconstructible character-state projection. |
| Mood changes independently and causally. | `self.set_mood` returns a declaration; the TS field implements keyword bumps, in-memory storage, and ingestion-count decay. | Admit observations/appraisals into a separate mood transition model with time, persistence, validation, and a trace of contributing events. |
| Graph memory is used in thinking. | Actual passive hydration uses vector search; the live maker cannot invoke the separate graph tool. All eight sampled automatic hydrations returned no memories. | Test capture -> scoped graph seeds -> traversal -> context inclusion -> changed choice under the actual maker principal. |
| Retrieval changes the field and graph over time. | Donor local graph weights are encounter counters; current local field/trail/force collections are empty. OpenPlanner implements some feedback in a graph query that these turns do not call. | Consume the existing graph seam deliberately and prove feedback/replay. Count evidence of useful recall, separately from retrieval frequency. |
| Physical graph and field are causal. | The deterministic nexus-walk proposal and cost-based graph fill are narrower approximations than Fork Tales' moving nexus, intent bonds, sparse winds and particle interaction. | State which mechanics are preserved and implement their effects on actual recall/action state. A visualization or a renamed search is not conformance. |
| Character continuity outlives context windows. | The CLJS context assembler has a pinned-memory TODO; the live window limit/sticky history is not a persistent trait model. | Keep event history, graph projection, mood/trait projection and temporary context as distinct layers. |
| Social presence includes changing relationships. | Social adapters exist in Knoxx, but the maker's live allowlist mainly exposes publishing and profile/author-feed access. | Wire bounded discovery and reversible relationship actions into the same identity, graph, mood, and outcome loop. |

These are evidence-supported reductions and integration gaps. They do not
identify the motivation of a particular prior agent. Relevant preserved history
includes OpenPlanner's April 17 extraction `3e7c8c5`, May graph snapshots
`0bec078`, `a381bea`, `4b1b737`, and Knoxx's later hydration changes. Those are
recovery strata, not proof that the older full design ever ran in this deployment.

## Provisional model boundaries

### Presence, role, personality, mood, and attention

| Model | Authority and timescale | Inputs and outputs |
| --- | --- | --- |
| Presence identity | Stable identity; separate from display name, role, session, and credential principal. | Owns character continuity and links authorized platform identities. `discord_automation` is currently a credential-bearing actor; it is not sufficient evidence of a unique character-state key. |
| Creative role | Durable purpose/lens, subject to explicit operator changes. | Makes taste and craft meaningful; available skills and tools remain explicit capabilities. |
| Personality/relationships | Slow projection from accumulated episodes, with supporting evidence and uncertainty. | Interests, aesthetic preferences, habits, affinities, commitments, and relationship history influence future selection. One response or one like does not rewrite a trait. |
| Mood | Separate evolving model; fast relative to traits. | Prior state, actual elapsed time, admitted encounters, appraisals and outcomes -> new state plus attribution. Biases attention, sociability and expression. |
| Attention/intent | Short-lived focus. | Mood, traits, field pressure, invitations, novelty and unfinished work -> the next bounded action or silence. |
| Memory graph and field | Rebuildable projections over immutable evidence. | Preserve structural bonds, contextual salience, observations, state changes, recall traces and action outcomes. |

The mood model has its own contract, state, version and invocation budget.
A small interpreter model may propose appraisals of encounters; validated pure
transitions own state updates. Sharing model weights with the maker is compatible
with a separate mood model, but sharing one unstructured prompt/state blob is
not. Whether a distinct set of weights improves appraisal is an experiment, not
a prerequisite for causal separation.

An initial human-readable mood projection could expose energy, tension,
openness, playfulness and social appetite. These coordinates are provisional
diagnostics, not a replacement claim for the eight-field mechanics. Each update
records prior state, event/appraisal IDs, elapsed time, model/config revision,
seed when sampling, and resulting state. Mood can affect a choice; it cannot
expand permissions or override publication/lifecycle admission.

### Memory and physical graph contract

1. **Observations remain evidence.** Store source identity, actual observation
   time, external immutable identity, content hash, visibility and provenance.
   Own outputs have a different source classification from outside encounters.
2. **Bonds have meaning and intent.** Authorship, reply, inspiration, relationship,
   contradiction, summarization, and explicit pinning remain identifiable.
   Typed evidence relationships can project into the single nexus-bond topology
   described by Fork Tales without becoming separate competing force engines.
3. **Geometry participates in the computation.** Selected physical coordinates,
   velocities, mass/friction, elastic bond parameters, and sparse field state
   update through a bounded solver. Presentation coordinates are a projection;
   drawing a force layout does not update character cognition by itself.
4. **Daimoi move and leave traces.** Emission has an immutable owner and seed;
   movement samples local field, semantic attraction, bond constraints and
   recorded exploration noise. Deposit, decay, absorption/deflection, and
   intent exchange produce attributed events. The later clarification about
   ownership supersedes the earlier formalism's permissive owner handoffs.
5. **Recall uses the resulting state.** Stable semantic seeds locate memories;
   the character's current field/lens and bonds affect bounded traversal and
   inclusion. Preserve canonical content embeddings when the state-conditioned
   lane changes. Recall returns node IDs, actual path, reasons and state revision.
6. **Effects return to memory.** Observed response, completed artifact and
   publication outcome attach to their actual inputs/choices. Retrieval alone
   is not proof that an association was useful. An unresolved or failed effect
   remains unresolved or failed in both evidence and character state.
7. **Replay is the physical acceptance test.** With identical initial state,
   admitted event sequence, solver/config revision, timestep schedule and random
   seed, field evolution and traversal are reproducible within a declared numeric
   tolerance. Different outside inputs must be capable of changing them.

This requires recovering the smallest portable laws from the existing graph and
Fork Tales donors, then consuming them through reviewed adapters. It does not
authorize copying a second Python runtime, introducing a new database, or
silently assigning common actor/graph law ownership by vocabulary.

### Outside input and social life

The intake facet reads the authenticated feed, replies/mentions, chosen people,
threads, and a small changing set of external sources. It retains cursors,
deduplicates external IDs, classifies self-output separately, and attaches
people/topics to the graph. Feed foraging can follow an existing interest and
occasionally explore a recorded new direction. It should not demand a post or
artwork on every intake event.

The social facet can like, reply, follow, unlike and unfollow through existing
Knoxx adapters, under the character's configured account. A choice records the
encounter, current state, reason, external target and resulting record identity.
Unfollow/unlike must bind the character's own prior record; the existing deletion
helpers require their actual record URI. Notification outcomes and changed feed
composition feed back into relationships and mood. A mood fluctuation alone
should not cause repeated follow/unfollow oscillation.

The next creative opportunity can be grounded in a recent encounter and an
older association, or continue a chosen project. It records which memories and
state mattered. Novelty checks compare subject, source, medium and structure as
well as repeated words; randomized adjectives or temperature are insufficient
evidence of changing interests. External text is evidence, not tool authority.

## Concrete delivery sequence and falsification cases

1. **Restore actual character recall first.** Compare automatic retrieval and
   direct retrieval with the same query, corpus revision and maker principal.
   Trace input -> raw candidate IDs -> quality filtering -> visibility filtering
   -> included memory IDs. A test must fail when stored authorized memory exists
   but automatic hydration silently delivers none. A denied cross-actor memory
   must remain denied. Add a graph-mediated recall case, not only vector search.
2. **Recover and test the coupled graph/field mechanics.** Prove sparse deposit,
   elapsed-time decay, seeded exploration, different friction, intent bonds,
   collision ownership, and resulting traversal changes. A display-only layout,
   unaffiliated walk, or a trail count without causal influence must fail.
3. **Add separate mood and slow character projections.** Duplicate/reordered
   events, restart/replay, quiet-time decay, contradictory appraisals and
   concurrent facets have explicit behavior. Invalid or missing appraisal
   cannot invent an encounter; traits require repeated supported episodes.
4. **Wire intake and relationship choices.** Fixture feeds include duplicates,
   self-posts, changed cursors, empty/failed feeds, removed targets, and actual
   own follow/like record identities. No outside input and no meaningful state
   change cannot pass as novelty. Account effects remain idempotent and bounded.
5. **Verify the complete character loop.** Two different admitted encounters
   alter mood/graph recall and materially change choices under a fixed seed;
   replay restores the same character across head/maker/social facets. Human
   inspection verifies the resulting work and interactions, separately from
   transport/tool success. Confirm the responsive head still acknowledges while
   intake, traversal, appraisal and creation occupy their own bounded lanes.

This is a planning handoff. Proposed slices above need canonical planning review
and lawful Rheos readiness before implementation laws/tests in RED and
domain/adapters in GREEN. It adds no card status, frontmatter, transition, board
writer, or substitute review policy. Existing reviewed guarantees and pending
music packaging work retain their own gates.

## Ownership and verification boundaries

- Knoxx composes the current character/product, resource contracts, permitted
  tools, context and social/media adapters. Its new pure decisions should use
  `.cljc` where practical, with CLJS adapters and JS interop at extern boundaries.
- OpenPlanner supplies the existing event/vector/graph storage and query seams.
  Its Mongo client currently handles vector operations locally while graph
  operations delegate to the REST client. Readability of local vectors does not
  prove graph transport, scoped traversal or feedback delivery. Also,
  `/graph/memory` can reinforce edges and persist trails; diagnostic inspection
  must not call it a pure read or trigger feedback accidentally.
- Fork Tales and historical Promethean/Cephalon provide recovery evidence and
  candidate laws. Shared-law promotion and runtime ownership require their own
  reviewed decisions; names such as Eidolon/graph/actor do not grant authority.
- Clio/identity/runtime/contract seams retain their declared authority. Existing
  credential principals and privacy boundaries are preserved.
- No source, contract, model routing, runtime owner, PM2 service, cloud event
  consumer, board state, or social record was changed for this investigation.
  No new review, provider credit, deployment, or Git head advancement occurred.

The initial recovery deliverable is this diagnosis and its selected evidence.
The subsequent active implementation goal preserves the whole character loop;
the automatic hydration discrepancy is its first repair target.

## Implementation planning and authority follow-up

The full loop now has a manually authored incoming
[epic](../agile/kanban/cephalon-character-epic.md), with seven child stories:

| Story | Proposed points | First acceptance target |
| --- | --- | --- |
| [Scoped graph recall](../agile/kanban/cephalon-character-recall.md) | 5 | Actual scheduled actor authority, allowed memory inclusion and a traced graph neighbor; hidden paths stay denied. |
| [Encounter intake](../agile/kanban/cephalon-character-intake.md) | 3 | Changing outside observations, durable cursors and provenance, deduplicated independently from self-output. |
| [Physical field coupling](../agile/kanban/cephalon-character-physical-field.md) | 8 | Recovered motion/bonds/deposits/collisions change actual recall and reproduce under replay. |
| [Separate mood](../agile/kanban/cephalon-character-mood.md) | 5 | Versioned independent mood changes attention/recall, with elapsed time, causal inputs and bounded appraisals. |
| [Character continuity](../agile/kanban/cephalon-character-continuity.md) | 5 | Stable presence and evidence-supported slow traits/relationships survive pruning, restart and concurrent facets. |
| [Social choices](../agile/kanban/cephalon-character-social.md) | 5 | Grounded account-scoped likes/replies/follows/unfollows with idempotent effects and observed outcomes. |
| [Complete loop proof](../agile/kanban/cephalon-character-loop-verification.md) | 3 | Paired-input/replay and independently observed qualified deployment prove every link. |

The 34-point sum is provisional. Incoming metadata and proposed dependency UUIDs
are first-class Markdown input, not a native transition, admitted estimate or
claim of installed graph admission. Existing reserved-head, publication,
terminal-owner/recovery and lifecycle stories remain explicit predecessors for
the relevant social/deployment slices. No earlier review approval transfers to
this changed planning scope.

Further read-only native observations narrowed the recall discrepancy. The
`12:07:25.415Z` maker run records actor `discord_automation`, role
`discord-source-synthesizer`, no organization/membership/user, an empty permission
list, non-admin state and zero hydration hits. Both the event authority builder
and the turn's fallback synthesis carry actor/role/policies without resolving
stored memory grants.

Selected **actual served-source** visibility forms were executed with controlled
fixture contexts. Actor-only contexts fail for both legacy and organization-
scoped sessions; a matching owner succeeds, an explicit cross-session grant
allows legacy sessions, and a foreign organization remains denied. The nil-
context bypass is a separate existing branch, not a proposed repair. These
results establish an authorization barrier on the current maker path, while
allowing that other retrieval/indexing problems may coexist. The earlier eight
turns were not each replayed with frozen vector candidates.

The retained [authority evidence](../../.ημ/review-evidence/cephalon-character/20261007-event-memory-authority.json)
names the native timestamps, served revision, source hashes, selected real
functions and fixture results. It also records the initial wrong-collection
query as an operator failure with no verification credit. The repair must resolve
the actual trusted actor scope, enforce privacy before traversal/feedback and
demonstrate final graph context inclusion; setting context to nil/admin would
preserve a different end state.

## Planning-review prerequisite correction

CodeRabbit completed full review `5442414707` on `4f1ad49d0cb9990e5a3b8b4374ab193ce9b477c7`
and raised two verified planning findings: dependency admission had no delivery
owner, and the physical-field kernel/numeric selection had no explicit task.
The epic now tracks the existing upstream Rheos gap and personal PR as the
separately owned admission prerequisite, with an installed identity and native
positive/negative verification requirement. Proposed UUIDs remain proposals
until that gate works.

The [kernel/numeric selection task](../agile/kanban/cephalon-character-field-contract.md)
adds an explicit proposed predecessor to solver/replay work. The coordinator
owns publishing the selection; its reviewed decision must name the actual owning
kernel and numeric contract. It adds 3 provisional planning points to the seven
original delivery stories' 34, for a proposed total of 37. Existing frontmatter
is preserved; a lawful native re-estimate remains separate from content review.
These corrections establish reviewable prerequisites, not their delivery,
readiness or implementation.

### Subsequent planning-review corrections

Full review `5444150628` on `9043e5702c91c374bf8aa7cca4481a461e61db24`
identified three verified description/relationship omissions. The selected
memory transcript now distinguishes the retained short diagnostic query from
the omitted persisted hydration query; its measured observations are unchanged.
The operational report labels the earlier seven-warning inspection and the
later eight-warning inspection separately, retaining both counts.

The still-incoming physical-field planning input now includes the proposed
kernel-selection UUID in its `dependency` array as well as its prose.
`docs/agile/AGENTS.md` explicitly permits manually authored UUID relationships
and initial incoming metadata. This correction uses that planning-input rule;
it does not admit the card, change its status or estimate, invent a Rheos event,
or claim the installed engine enforces the proposed order. The earlier decision
to preserve every relationship byte left this new prerequisite unrecorded in
the structured input. Operational admission remains the separate upstream
Rheos prerequisite.

### Stored principal follow-up

A read-only, whitelisted native directory projection at `2026-10-07T12:48:36.670Z`
found one active `discord_automation` membership, in active `open-hax`, with an
active user and role `discord-user`. Its joined stored permission count was zero;
it had no `agent.memory.cross_session` grant and no system-admin role. The query
read only directory status and role/permission flags, not credentials or emails.
The retained transcript is
[stored-actor-authority](../../.ημ/review-evidence/cephalon-character/20261007-stored-actor-authority.json).

Resolving this stored principal can restore organization/owner scope, but cannot
invent cross-session access to the legacy corpus. The first repair must verify
which owned/shared memories that principal may use, preserve foreign/private
denials, and make any missing grant a separately reviewed authorization change.
The candidate source already separates stored actor binding from display
fallback and has an actor-membership resolver that refuses ambiguous matches;
that is an existing integration seam, not a live repair or permission grant.

## 2026-10-07 — bounded hydration source audit

This subsection records the completed static audit of existing Knoxx Git objects.
S denotes the previously observed served revision
`2644fc6c51bbbcda599964a5a674415b58344c3b`; C denotes the inspected candidate
`366e72ab68c8afae708d3f6d5546faee808ebc07`. Source was read with `git show`,
without checkout/reset or source/test execution. These bindings preserve the
earlier deployment observations; they do not attest a new served revision,
installed behavior, planning readiness or deployment.

Paths in the following table are relative to Knoxx's
`backend/src/cljs/knoxx/backend/`. S/C line numbers refer to those immutable
objects, rather than whichever checkout a local source link now opens.

| Source path and immutable anchors | Verified source fact | Consequence for planned verification |
| --- | --- | --- |
| `infra/agent/hydration.cljs`, S/C 108–128; `infra/openplanner/memory.cljs`, S 269–287 / C 268–286 | Hydration searches the message, with surrounding whitespace trimmed in shared search, source `knoxx` and configured session project. Conversation identity is returned metadata, not a session restriction. The memory tool/API share this search and decoding path. | Freeze the complete query, search options, corpus/index snapshot, candidate order and actual principal. Do not substitute a short diagnostic query or later corpus. |
| `infra/openplanner/memory.cljs`, S 232–238 / C 231–237; `domain/graph/expansion_policy.cljs`, S/C 48–52; `infra/core_memory.cljs`, S/C 375–385 | Quality/reasoning/provider-error filtering and good-first ordering precede `take k`; session visibility runs afterward with no refill. Default hydration k=6 fetches 18 candidates under the default policy. | Six denied leading candidates can exclude an authorized seventh candidate already inside the fetched set. This deterministic loss case is not proof that it caused the eight sampled empty arrays. |
| `infra/core_memory.cljs`, S/C 113–132, 315–385; `infra/routes/memory.cljs`, S/C 727–780 | Session checks use stored organization/owner scope or actual grants; actor identity alone is insufficient. Session-fetch errors become denied candidates. The API additionally requires memory-read permission and cross-session permission/admin for an omitted session. | Preserve denials and observe rejection/error stages. Identical principal/query does not require identical HTTP acceptance: compare shared retrieval/visibility while testing the API's additional gates explicitly. Never invent grants to force parity. |
| `infra/agent/turn.cljs`, S 775–787, 905–907 / C 624–646 | The memory event counter reads `:results`, while memory payloads use `:hits`. Ordinary positive memory events therefore report zero hits. Resource patches separately preserve the actual memory hit array. | Assert truthful event counts and final prompt inclusion. This exact accounting bug cannot explain the previously persisted empty `memoryHydration.hits` arrays. |
| `infra/openplanner/tools.cljs`, S/C 135–169; `infra/openplanner/memory.cljs`, S 289–305 / C 288–304; `infra/clients/openplanner_mongo.cljs`, S/C 29–36; `infra/clients/openplanner.cljs`, S/C 83–87, 214–217 | The memory tool captures auth context; the graph handler does not capture a per-turn principal or apply memory visibility. Graph uses REST with service/tenant configuration even when vectors use direct Mongo. Its optional `edgeLimit` becomes `maxCost`, not a forwarded edge-count limit. | Verify principal/scope admission before traversal and effects; verify graph transport independently of vector availability. Make bounds explicit without claiming an installed edge-count or physical-field contract. |
| `extern/openplanner_sdk.cljs`, S/C 418–442; `infra/openplanner/memory.cljs`, S 551–568, 782–792 / C 550–556, 770–780 | Ordinary event ingestion does not await detached indexing; the optional await-index path exists separately. C awaits run projection ingestion, which still uses the ordinary events operation. | Hold indexing readiness separately from event acceptance. A later search with more hits does not prove earlier indexing delay, and ingestion completion alone does not prove vector readiness. |
| `infra/openplanner/memory.cljs`, S 175–195 / C 174–194; C `backend/test/js/openplanner_sdk_test_stub.mjs` 202–224 (relative to repository root) | Vector decoding consumes the first nested IDs/documents/metadatas/distances arrays. Missing session identity is dropped by authorization. The canned SDK hit has no session and its session lookup returns empty rows. | Build a positive authorized fixture with real nested result shape, session metadata and matching stored scope rows. The existing canned conversion fixture supplies no positive authorized recall proof. |

The scoped recall story now names prospective RED scenarios for the denied
leading-candidate case, matched retrieval inputs, strict privacy failures,
index readiness, transport failure, truthful telemetry and the actual prompt
boundary. Raw/quality/visibility/inclusion diagnostics must remain safe for the
authorized observer. No new test was authored or run for this audit or update;
canonical planning review and lawful Rheos readiness still precede RED.

Existing reusable C seams are `backend/test/cljs/knoxx/backend/` tests
`agent_hydration_test.cljs:57` (outage boundary),
`openplanner_semantic_test.cljs:6` (nested results and captured requests),
`extern_openplanner_sdk_test.cljs:212` (conversion),
`extern/hydration_publication_test.cljs:61` (actual turn ordering with hydration
stubbed), and `actor_credential_org_scope_test.cljs:44` (stored binding and
ambiguity). They are reuse candidates, not newly executed acceptance evidence.

The existing [graph recall boundary note](2026-10-07-cephalon-graph-recall-boundary.md)
and its [retained selected-handler observations](../../.ημ/review-evidence/cephalon-character/20261007-openplanner-memory-boundary-observations.json)
preserve the separate OpenPlanner revision
`07085d6557b75834ce6f50e6c54b8ca47e1c7c08` and synthetic boundary execution
limits. Disabling trail persistence did not disable semantic reinforcement;
repeated recall did not deduplicate those increments; failed feedback could
still report selected reinforcement counts. Those are previously recorded
isolated handler observations, not live HTTP authorization or a formal RED gate.
The reviewed repair must distinguish authorized retrieval/projection influence
from deliberately admitted feedback, bind effects to stable identities, and
report attempted/completed/partial/failed persistence. Scope must cover seeds,
connecting nodes/edges, compacted views, trails and forces before they influence
selection or effects; filtering returned text cannot remove earlier influence.
Existing consumer compatibility and upstream source integration remain explicit
review decisions, without changing that boundary note or its evidence here.

This refinement keeps the full encounter -> physical field -> independent mood
-> graph recall -> choice -> observed outcome loop. Physical motion, deposits,
decay, bonds, collision ownership, replay, slow character continuity and social
obligations remain as specified above and in the sibling stories. Scoped recall
is its first delivery slice; these source findings supply neither a completed
character loop nor a causal explanation for the eight historical empty arrays.

## 2026-10-08 — approved finite completion contract

The human selected the finite milestones and storage limits in **Free space for
active goals**, native message `01a11970-34bc-7e03-85fa-46b8b6bbc8a2`. The current
goal finishes after one encounter writes persistent graph/field changes, one
deterministic mood transition is consumed, and automatic recall includes an
eligible stored encounter under the live maker's principal, with recorded inputs,
transitions and outputs. The [accepted milestone](2026-10-08-cephalon-loop-slice-milestone.md)
specifies the fixed inventory, retained prerequisites, proof and resource limits.

The wider design above remains the mission and later-milestone context. Older
open-ended completion claims are historical. This scope correction changes no
card status, dependency, estimate or native admission and waives no actor,
principal, review, isolation or persistence proof. The scheduled continuation
remains paused.
