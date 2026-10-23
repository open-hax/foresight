# Muse tools and harness actors across Knoxx instances

- Status: proposed; 2026-10-22.
- Research: [inventory and sources](../../research/harness-actor-continuity.md).
- Extends [layered contracts](layered-operating-contracts.md); preserves contract-0.
- Epic: harness-actor-continuity.

## Outcome

Select one operating contract through .ημ layers, project its supported tools
into Codex, OpenCode, Claude and qualified Hermes contexts, and bind each active
context to its own Knoxx actor. Local and remote Knoxx instances retain scoped
memory and explicit authority over what crosses their boundary.

## Ownership and interfaces

Muse compiles declared tools, schemas, exposures and supported host adapters.
Reuse its existing MCP backend for Codex tools. Host lifecycle, task messages
and agent spawning require separate host adapters; MCP tool parity never implies
hook parity. Unsupported capabilities must be reported in the manifest.
Knoxx owns actors, mailboxes and admission. Axxium owns principal/authorization
contracts; Clio owns event identity/replay. Katamorph supplies portable shapes.
Epiphany supplies archaeological evidence, Rheos supplies work state.

A binding identifies instance id, actor id, authenticated principal reference,
harness family, installation id, session/task id, parent context, capability
profile and effective-contract hash. Credentials stay in the runtime's secret
store; manifests contain references only. Each spawned context gets distinct
provenance and a bounded delegated grant. A verified resume may reuse a binding;
new tasks must not be silently aliased by model name or similar content.

Message envelopes carry producer instance/actor, destination instance/actor,
message id, correlation/causation, schema version, contract hash and an opaque
payload reference or permitted payload. Authentication binds the envelope to
its producer; user-supplied actor ids never establish authority. Define delivery,
deduplication, ordering and cancellation through existing Knoxx/Clio contracts
before implementing adapters. A transport failure preserves pending work with
an explicit retry policy; it never silently switches actor or destination.

## Local and remote federation

Start with an explicit mapping between local and remote actor references,
verified by their respective identity providers. Equal display names or one
human account do not grant cross-instance authority. Each destination checks
audience, tenant, role, capability and current grant independently.
Private records remain private. A lift into shared memory is a separate event
recording source, destination scope, decision actor, transformation/redaction,
reason, revision and revocation dependencies. A schedule or repeated references
may propose a lift; neither grants authority to perform one. The trigger remains
an open design choice and does not block the tool-adapter slice.

## Scope and rollout

1. Qualify Codex MCP registration from the shared Muse adapter without editing
   existing unrelated server definitions. Inventory tool names and schemas;
   compare allowed exposure with Claude/OpenCode under the same selected profile.
2. Define portable binding/message shapes and admission laws upstream.
3. Bind Codex context lifecycle and native messages to Knoxx through a verified
   host capability adapter; record unsupported desktop/CLI differences.
4. Probe Orbit/Hermes runtime and qualify its own adapter separately.
5. Exercise local/remote identity and scoped memory in isolated test instances.

Each step produces evidence and a rollback path. Publishing runtime artifacts
is a separate implementation PR after Rheos admission; this PR is planning.

## Verification and negative cases

Run real MCP initialize/tools-list and one harmless tool call through Codex.
Test duplicate/conflicting registrations and preservation of other servers.
Test two simultaneous contexts, resume, actor spoofing, expired delegation,
parent cancellation and unknown capabilities. Federation probes include cross-tenant
denial, wrong audience, replay, duplicate delivery, remote outage and revocation.
Record actual revisions, model/adapter capability evidence and receipts. Tests
may validate existing schema contracts; no second Rheos engine is introduced.

## Decisions and alternatives

The common representation is the Knoxx actor/message contract. Reusing native
host messages reduces duplicated scheduling, but cannot import privileges from
a desktop task API into MCP. A generic MCP-only deployment supports tools while
explicitly reporting lifecycle/message integration as unavailable.
Hermes is a candidate consumer through Orbit's existing plugin, pending probes.
Contract-0 source selection and language ownership remain the earlier LOC open
questions; this extension does not resolve them by assumption.

## Review refinements

HAC.01 independently qualifies the existing single-tree MCP compiler. HAC.06
depends on LOC.05/06/07/08 to prove tools and instructions share the same
effective-contract hash. PR141 supplies specifications, not those implementations.
New contexts have distinct actor references; authenticated resume is the only
reuse exception. Parentage grants no private-memory access.
HAC.05 qualifies transport and identity; HAC.07 separately qualifies memory.
The initial protected-read consistency model checks source authority online on
every read and promotion. No cached grant permits offline disclosure. Revocations
committed before the read authorization check must deny that read. The check
is the linearization point; already delivered bytes cannot be recalled. Revoked
materialized projections become unavailable for later reads until reauthorized
or rebuilt. This conservative default is a proposal to review, not a claim
that current Knoxx implements it.

## Canonical contract and delegation rules

The Knoxx repository is the canonical semantic owner of binding and message
contracts. HAC.02 introduces versioned registry entries there, using Katamorph
schema machinery and Clio envelope conventions rather than parallel actor models.
Serialization is explicit tagged EDN with namespaced keys and an integer major
schema version. Transport JSON is an adapter codec whose roundtrip must preserve
the canonical values; raw host objects are never canonical contract data.
Unknown major versions or required fields fail admission. An older schema is
accepted only through a named, versioned migration with tests. Muse, Axxium and
Clio consume that registry contract; changes require an owning Knoxx PR before
adapter adoption. The effective operating-contract hash is produced by LOC.05/08
and passed unchanged; this plan defines no competing hashing algorithm.

A delegated child grant must be a subset of its parent's capabilities and
allowed tenants, audiences and resource scopes, with expiry no later than the
parent's. Any widening fails. Binding validation checks authenticated principal,
installation identity, context identity, expiry and revocation on every use and
resume. Revoked bindings cannot resume; parentage grants no implicit privileges.

Committed cancellation is terminal for queued or retrying descendant work and
wins over later dispatch. Work admitted earlier is stopped best-effort; already
observed effects stay recorded. This rule is tested with transport failure,
parent cancellation and retry. Retry never revives a cancelled message.

Lift admission requires a currently authorized decision actor with source-read
and destination-publish grants, source policy permitting the destination, and an
exact source revision. The record binds source identity/revision, destination,
decision actor, transformation/redaction version, output hash, reason, schema
version and revocation dependencies. Stale input or unauthorized lift fails
before shared visibility. Revocation denies later reads through the online
authority check defined above, including projections materialized earlier.
