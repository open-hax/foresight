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
harness family, installation id, repository identity or explicit operator scope,
session/task id, parent context, capability profile and effective-contract hash. Credentials stay in the runtime's secret
store; manifests contain references only. Each spawned context gets distinct
provenance and a bounded delegated grant. A verified resume may reuse a binding;
new tasks must not be silently aliased by model name or similar content.
On every use and resume, the authenticated context instance ID and actor ID
must each equal the stored binding instance ID and actor ID. Test each mismatch
independently; either denies admission before delivery, effects or protected
outcome return. Caller-supplied identities cannot replace the stored binding.
The canonical context identity is the immutable tuple of instance id, actor id,
authenticated principal reference, harness family, installation id, tagged
repository identity or explicit operator-scope identity, native session/task id,
and authenticated parent-context reference (explicitly nil for a root context).
The actor pair is an index into the stored binding, not a replacement for that
full tuple: lookup authenticates the pair and compares the same canonical tuple
for equality on first use and resume. A mismatched harness, session/task,
principal, installation, scope or parent reference denies before delivery,
effects or protected outcome return. Parent provenance supplies no grant.
Capability profile and effective-contract hash are separately validated binding
policy, not context-identity fields; changing policy requires explicit binding
authorization and invalidates stale policy evidence rather than silently
changing context identity.

Message envelopes carry producer instance/actor, destination instance/actor,
message id, correlation/causation, schema version, contract hash and an opaque
payload reference or permitted payload. Authentication binds the envelope to
its producer; user-supplied actor ids never establish authority. Before mailbox
admission, the authenticated receiving instance ID and actor ID must each equal
the envelope destination instance ID and actor ID, using the stored-binding
equality rule above and canonical HAC.02 admission. Test each destination
mismatch independently; either denies admission before delivery. Define delivery,
deduplication, ordering and cancellation through existing Knoxx/Clio contracts
before implementing adapters. Retries retain the original producer
(instance-id, actor-id), binding identity, message ID and destination and
deduplicate before side effects under the canonical HAC.02
rule; the owning implementation names its versioned rule and atomic
admission/effect boundary. The deduplication record and protected outcome must
be durably committed with the effect, or recovered through an equivalent
versioned protocol that prevents another effect and preserves the first outcome.
Test crash recovery after the effect commits and before its response arrives: an
authorized retry returns that durable outcome with exactly one effect.
A transport failure preserves pending work with an
explicit retry policy; it never silently switches actor or destination. Test a
non-idempotent effect committed before its response is lost: retrying the same
message with current delivery and protected-outcome authorization returns the
authorized recorded outcome with exactly one observable effect. Authorization
expired or revoked before retry denies delivery and protected outcome return
without repeating the effect. Cached outcomes never bypass current admission.
A producer or binding change denies retry; any explicitly authorized producer,
binding or destination change creates a new message, never a retargeted retry.

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

Run real MCP initialize and tools/list through the isolated fresh Codex session
required by HAC.01. For its harmless call, define a test-only, non-actor-scoped
read-only fixture tool `hac_fixture_read` in the selected Muse test profile,
compiled and registered through the same existing MCP backend. Call it with
empty arguments `{}` and assert exactly one text result with text
`HAC_READ_ONLY_FIXTURE_V1` and no error. This is a proposed fixture, not a claim
of an existing deployed tool. The runner must assert zero fixture mutation
calls and byte-identical before/after snapshots of the isolated configuration
and fixture data, plus unchanged actor/mailbox/memory state; no actor store
credentials or mutation capabilities are supplied to the fixture. A missing
call, different response or state change fails qualification.
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
or rebuilt through current source-read and destination-publish authorization.
Non-revoked projections need no new promotion or rebuild for an authorized read.
An authorized rebuild first passes current source-read and destination-publish
authorization and current source-policy approval for the destination before
accessing source bytes or transforming content. It then
reads the current source revision, revalidates policy and
revocation dependencies, and recomputes the versioned transformation and output
hash before publication admission. Its event binds those inputs and output,
including both grant identities/revisions and source-policy decision/revision.
Publication admission first compares the actual source identity and destination
with the event-bound source and destination; cross-source or cross-destination
publication denies even when bytes, revision numbers or grants otherwise match.
It then hashes actual replacement bytes and compares their hash
and transformation version to the event, then compares source
revision, source-policy decision/revision, revocation dependencies and both grants with
the event-bound values; any difference rejects or requires revalidation and a
newly bound event before visibility, as qualified separately in HAC.07.
An unauthorized rebuild reads no source bytes, performs no transformation, and
produces no promotion event or projection; a content-free denial audit is allowed. This conservative default is a proposal to review, not a claim
that current Knoxx implements it.

## Canonical contract and delegation rules

The Knoxx repository is the canonical semantic owner of binding and message
contracts. HAC.02 introduces versioned registry entries there, using Katamorph
schema machinery and Clio envelope conventions rather than parallel actor models.
Serialization is explicit tagged EDN with namespaced keys and an integer major
schema version. Transport JSON is an adapter codec whose roundtrip must preserve
the canonical values; raw host objects are never canonical contract data.
Unknown major versions, incompatible required fields and missing required fields fail admission. An older schema is
accepted only through a named, versioned migration with tests. Muse, Axxium and
Clio consume that registry contract; changes require an owning Knoxx PR before
adapter adoption. LOC.04 defines the effective-contract hash field; LOC.05 defines the folded contract and canonical fragment meaning; LOC.06 validates the layer chain; LOC.07 supplies a separate profile input; LOC.08 carries the unchanged hash in each projection. This plan defines no competing hashing algorithm. HAC.01
remains independent of LOC implementation: standalone qualification pins the
compiler revision, exact single-tree configuration/profile paths and Git blob
IDs, and loaded target artifact hashes. These byte-provenance identities do not
claim a canonical layered operating-contract hash; HAC.06 qualifies that later.
HAC.02 can specify portable admission laws against explicitly synthetic contract
inputs before that producer exists, preserving every required admission check.
Actual active-contract qualification of those laws through HAC.03 use/resume
requires the implemented LOC producer and canonical contract/projection evidence
qualified in HAC.06; fixture or copied hashes never qualify that integration.

Delegation requires an issuer authorized by the parent grant's explicit
delegation permission, bound to the authenticated principal and parent grant.
A parent grant without delegation permission is non-delegable. A delegated child
grant must be a subset of its parent's capabilities and allowed tenants, audiences
and resource scopes, with expiry no later than the parent's. Repository identity and explicit operator scope are inherited unchanged from the parent grant; delegation cannot change either field or switch between repository and operator scope. Independently deny a changed repository identity or changed/widened operator scope, including when all other delegated fields are valid. Any widening fails. Every use and resume validates the full delegation chain;
revocation or expiry of any ancestor invalidates descendants before admission,
delivery, effects or protected outcome return. Missing chain authority fails closed.
Binding validation checks authenticated principal, installation identity,
repository identity or explicit operator scope, context identity, capability
profile, expiry and revocation on every use and resume. For mutating jobs, the
requested repository must equal the bound repository or fall within the
explicitly authorized operator scope; scope is never inferred from cwd. Test a
repository-A binding against repository B, and a request outside operator scope:
each denies before delivery or effects on both use and resume.
The binding profile must match the active contract's profile, and the binding
and message envelope effective-contract hashes must equal the active contract
hash; mismatches deny use and resume. Revoked bindings cannot resume; parentage
grants no implicit privileges.

Committed cancellation is terminal for queued or retrying descendant work and
wins over later dispatch. Work admitted earlier is stopped best-effort; already
observed effects stay recorded. This rule is tested with transport failure,
parent cancellation and retry. Retry never revives a cancelled message.

Initial lift admission requires current source-read and destination-publish
grants and source-policy approval for the destination before accessing source
bytes or running the transformation. Failure produces no output, promotion event
or projection. An admitted lift binds the authorized decision actor and exact
source revision. The record binds source identity/revision, destination,
decision actor, transformation/redaction version, output hash, reason, schema
version and revocation dependencies. Stale input or unauthorized lift fails
before shared visibility. Revocation denies later reads through the online
authority check defined above, including projections materialized earlier.

## Payload reference admission

A message payload reference conveys no read authority by itself. Resolution
requires the authenticated destination actor's current source-read grant to
authorize the referenced source identity and resource scope, alongside tenant
and audience match and exact authorized source revision. A grant for a different
source identity or resource scope independently denies content return, even
when destination, tenant, audience and revision otherwise match. Wrong destination,
revoked/expired grant or inaccessible source denies resolution without returning
content. HAC.02 specifies these checks in the canonical Knoxx contract; HAC.05
qualifies them through the adapter. No existing resolver behavior is presumed.


## Inline payload admission

Permitted inline payloads retain the same content authority as references.
Before a sender reads protected source bytes or serializes/discloses them to a
transport, it verifies current source-read authority and the authenticated
intended destination's current content-access grant, tenant, audience and exact
source revision. The inline envelope binds that source identity/revision and
authorization provenance. At destination admission, those same current grants,
source revision, tenant, audience and destination binding are revalidated before
payload delivery, effects or protected outcome return. Missing provenance,
unauthorized destination, stale revision, or expired/revoked grants denies
without content disclosure. Public inline data needs an explicit source-policy
classification; absence of private metadata never implies public authority.
Retries and stored outcomes retain current authorization checks; copying bytes
into an envelope cannot bypass source revocation. HAC.02 specifies these laws;
HAC.05 qualifies the sender and receiver boundaries separately, including
unauthorized-destination negatives with no payload-bearing transport emission.
