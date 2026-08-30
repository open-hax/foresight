# Composable agent prompts: recovered pattern and portable seam

## Existing evidence

Knoxx/Sol resolves role prompt contributions first, actor contributions second,
and agent-contract contributions last. It preserves prompt values as data and
uses a template form with blank-line separators. Roles also contribute
capabilities and tools. This is an operating implementation, although it has no
portable fragment identity, provenance, authority decision, conflict report or
render receipt.

Operation Mindfuck separates immutable doctrine from a priority-ordered skill
graph. Its contracts associate prompts with actors and roles, and its skill
registry already treats skills as subordinate modules that cannot override
immutable doctrine. This is strong design evidence, but not a general prompt
compiler. Muse currently contains only an idempotent marked-section helper;
that solves repeated injection growth, not composition authority.

## Portable v1 seam

A prompt fragment is versioned data with a stable authority-scoped contributor,
scope, slot, append/exclusive merge mode, precedence and tie-break order,
conditions, compatible targets, EvidenceRef provenance, and content. Compilation
receives separate Axxium authority decisions. Muse may compile/project granted
fragments but cannot grant them.

The pure compiler:

- evaluates only equality conditions over caller-supplied context;
- orders fragments deterministically by precedence, order and ID;
- rejects missing/ambiguous authority and exclusive-slot conflicts;
- reports over-budget output without silently dropping fragments;
- returns system/task projections plus a content-free receipt containing
  selected and excluded fragment IDs with reasons, versions, EvidenceRefs,
  order, sizes, token estimate, fingerprint and diagnostics. Rejected attempts
  also produce a content-free receipt.

Documentation is a workflow gate: a fragment vocabulary, graph projection, or
host adapter does not reach done until its identity, authority, ordering,
selection/exclusion, conflicts, diagnostics, migration and verification are
documented for the next actor.

Rheos cards/workflows and Epiphany/Clio history enter only through EvidenceRef.
Retrieved text remains untrusted content until Axxium grants the specific
fragment contributor authority. Target-specific rendering remains future Muse
adapter work.

The long-term index is a shared graph of self-describing fragments linking
actors and tenants to capabilities, policies, skills, skill-graph nodes,
workflows, EvidenceRefs and message contracts. It is not one universal prompt.
Compilation selects the lawful view needed by one actor, message episode and
host, then projects that view into language the target can understand.
