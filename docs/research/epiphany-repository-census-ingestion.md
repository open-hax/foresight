# Epiphany persistence boundary for the repository census

## Status

Working cross-repository design constraint for the repository-census research.
It records the intended persistence boundary but does not modify Epiphany's
accepted laws or MongoDB collections by itself.

## Decision boundary

The Foresight repository-census crawler is an **evidence acquisition provider**.
Its generated EDN files are a revision-pinned evidence packet that preserves
the observed declaration fields and their source Git object identities, plus
human-readable projections. They are not the durable semantic authority for
repository identity, continuity, or lineage.

Epiphany is the intended owner of:

- versioned observation contracts;
- append-only durable ingestion;
- repository-instance and repository-family identity;
- MongoDB observation collections;
- rebuildable repository-index projections;
- later provisional and accepted lineage decisions.

Git remains authoritative for commits, trees, blobs, exact paths, and Gitlinks.
MongoDB records what was observed, how it was derived, ingestion state, gaps,
and review decisions. A Mongo projection is disposable even when the underlying
observation is durable.

## Critical identity correction

The census currently uses values such as:

```clojure
{:repository/id "github:open-hax/foresight"
 :repository/full-name "open-hax/foresight"}
```

That is a stable **repository reference key inside this packet**. It is not an
Epiphany `:resource-id`, repository-instance identity, or repository-family
identity.

A normalized GitHub locator can group spelling variants of one remote location,
but it cannot establish shared history. Epiphany may bind a reference to a
registered repository instance only after observing stronger evidence such as a
shared commit OID, or after an explicit human decision. Matching remote URLs,
directory names, or similar contents never silently create identity.

## Translation from the current packet

### `summary.json`

Becomes one census-run observation containing:

- run and request IDs;
- pinned roots;
- producer and adapter version;
- configuration and schema versions;
- packet digest;
- counts and frontier state;
- start/completion timestamps;
- explicit failures.

### `occurrences.edn`

Each row is an effective decoded submodule-configuration namespace, not one
physical section header. Repeated headers with the same decoded name are folded
using Git's ordered property-assignment semantics: the row retains the first
header line and the final effective values. The exact source blob and digest
remain the authority for reconstructing every physical declaration. An importer
must not claim that one occurrence row proves one raw header.

Each effective row still contains several epistemically different facts and
must not be stored as one ambiguous repository edge.

It decomposes into:

1. **Observed effective submodule configuration**
   - parent repository reference;
   - parent commit OID;
   - exact `.gitmodules` path, source blob OID, and raw-byte SHA-256;
   - parent tree OID;
   - declaration line;
   - exact declared path;
   - exact raw URL;
   - optional declared branch.
2. **Observed Gitlink** when the declared path resolves to a Gitlink
   - parent commit and tree OIDs;
   - exact path;
   - target commit OID.
3. **Derived locator normalization**
   - raw URL;
   - normalized provider and owner/repository locator;
   - normalizer name, version, and configuration hash;
   - derived epistemic tier.
4. **Candidate reference relation**
   - declaration/Gitlink occurrence to a repository reference;
   - never directly to an Epiphany repository family.

The declared branch is context only. The pinned Gitlink commit is the observed
revision followed by recursive traversal.

### `repositories.edn`

This is a rebuildable aggregate projection over occurrence and traversal
observations. Its revision set, minimum depth, root flag, manifest statuses, and
submodule counts should not be imported as one authoritative mutable document.

An Epiphany `repository-index-v1` projection may reconstruct those fields and
add query indexes, but deleting it must not delete the observations needed to
rebuild it.

### `gaps.edn`

Each row becomes an append-only gap observation. Resolution gaps retain
`gap/occurrence` and their raw evidence. Pre-occurrence gaps such as
`manifest/unavailable`, `commit/unavailable`, or a queued `recursion/max-nodes`
have no source occurrence; they instead retain their repository, revision,
depth, gap type, limit, and HTTP/detail evidence whenever those fields apply.
The importer must accept that typed shape without dropping it or inventing an
occurrence. A gap is not a nullable field on a repository document.

Examples include:

- local-only reference;
- declaration path absent from the pinned tree;
- declared path present but not a Gitlink;
- unsupported locator syntax;
- source or target unavailable;
- recursion limit reached.

Later work may append a resolution or interpretation. It must not rewrite the
original gap observation.

## Proposed Epiphany observation vocabulary

Names are provisional until accepted in Epiphany law:

```clojure
:census/run-completed
:git/effective-submodule-config-observed
:git/gitlink-observed
:repository/reference-normalized
:census/gap-observed
:repository/reference-bound
```

The first, second, third, and fifth records are observed facts. Locator
normalization is derived. Binding a locator/reference to a registered repository
instance is provisional or accepted depending on its evidence and review path.

Every record should carry Epiphany's versioned observation envelope:

```clojure
{:observation/id ...
 :observation/type ...
 :observation/observed-at ...
 :observation/adapter-version ...
 :observation/schema-version ...
 :census/run-id ...
 :observation/source-provider-id ...
 :observation/request-id ...}
```

The Epiphany importer assigns one immutable `:census/run-id` when it accepts a
packet and persists that ID on the run observation and every child observation.
It assigns a distinct `:observation/id` to every stored record in that census
run. `:observation/request-id` remains the acquisition/idempotency request key;
it is not a substitute for run membership. Packet `occurrence/id` and `gap/id`
values remain stable evidence-subject keys and may participate in
request/idempotency checks; neither is reused as `:observation/id`. `gap/id`
excludes volatile human-readable `gap/detail` from its subject digest. A
repeated run may therefore observe the same packet occurrence or gap again
without collapsing two run-bound observations into one.

The request ID has packet-level scope. The acquisition provider supplies one
opaque request ID, and the importer binds it to the authenticated/configured
provider identity and the canonical packet digest after schema validation. The
packet cannot select its provider identity. In `census-run-v1`, MongoDB enforces
a unique compound index on
`[:observation/source-provider-id :observation/request-id]`. The run record also
persists the canonical packet digest, schema identity, expected canonical child
IDs, child-set digest and count, and an acceptance state. Reusing the pair with
different bytes, digest, schema, or expected child set is a conflict. Every
child copies the request ID for traceability, but child collections
intentionally do not make that shared value unique.

Accepting a packet is one atomic domain operation. Every adapter strategy must
enforce the run verification, insert-if-absent child commitment/equality,
complete commitment-set verification, and complete-only visibility laws below.
An adapter with one transaction spanning the run and child collections performs
all of those checks and writes inside that transaction, including readback and
validation of every preexisting same-ID child; it may not use an updating
upsert. An adapter without that transaction performs the same protocol with a
durable `pending` to `complete` state:

1. Atomically create or load the unique run record and verify all immutable
   packet and expected-child metadata.
2. For every expected child, derive an immutable canonical commitment after
   schema validation. The commitment covers its deterministic observation ID,
   versioned collection and observation type, accepted schema-contract identity
   and version, and complete immutable canonical payload; storage metadata is
   excluded. Atomically insert the validated record and commitment only if the
   ID is absent. If that ID already exists, including after losing an insert
   race, re-read it and require the same collection/type and schema identity plus
   byte-identical canonical payload. Never overwrite the winner; reject any
   mismatch as an import conflict and leave the run `pending`. A matching replay
   writes only missing children and never treats an existing request row or
   child ID alone as acceptance.
3. Re-read every expected child commitment and the complete run-bound child set.
   Reject any missing or extra child and any payload, schema, collection, or type
   mismatch. Compute the child-set digest over the canonical ID-sorted
   commitments and require the exact expected set, count, and digest.
4. Only after step 3 succeeds, make the run `complete`: commit that state with
   the children in the spanning transaction, or compare-and-swap the durable
   run from `pending` to `complete` in a non-transactional adapter.
5. Expose only `complete` runs and their children to readers and projections.

Concurrent importers may race on deterministic inserts, but only one can create
the run or finalize its state. An insert loser validates the winner's immutable
record and never updates it; a mismatch blocks finalization. A crash before or
after any child collection, or immediately around finalization, is recovered by
replaying the same operation. No pending or extra child can become accepted
evidence.

Child-record identity is separate. The importer derives each
`:observation/id` from the run ID, versioned observation type, and canonical
packet-record identity, and every versioned collection enforces a unique index
on that observation ID. This prevents duplicate children within one accepted
packet without collapsing the same stable occurrence or gap observed by a
later run.

Repository-bound records additionally carry `:resource-id`. Pre-registration
external-reference observations require a distinct typed subject/reference;
they must not mint a repository `:resource-id` from a remote URL.

## MongoDB shape

The smallest direct implementation is one append-only collection per versioned
observation contract:

```text
census-run-v1
submodule-declaration-v1
gitlink-observation-v1
repository-reference-normalization-v1
census-gap-v1
repository-reference-binding-v1
```

A disposable projection can then materialize:

```text
repository-index-v1
repository-occurrence-index-v1
census-coverage-v1
```

Useful observation indexes include:

- unique observation ID in every versioned collection;
- unique source-provider plus request/idempotency ID on `census-run-v1` only;
- non-unique request ID on child collections for packet tracing;
- census run ID;
- parent reference plus parent commit OID;
- target reference plus target commit OID;
- exact declared path;
- raw URL and normalized locator;
- source `.gitmodules` blob OID;
- occurrence status and gap type.

## Import flow

```text
Foresight crawler
  -> revision-pinned evidence packet
  -> packet digest and schema validation
  -> Epiphany census importer
  -> append versioned observations to MongoDB
  -> record projection checkpoint
  -> rebuild repository indexes
  -> later reconcile references with registered repository instances
```

The importer is a domain operation, not a raw call to Epiphany's backup
`:import-all`. Backup restore expects already-valid Epiphany records in known
collections. Census import must translate and validate foreign evidence before
calling registered durable write operations.

## Ownership and implementation sequence

1. Epiphany law defines the observation schemas and epistemic tiers.
2. Epiphany ports and operation registry expose the durable write/read boundary.
3. The in-memory adapter establishes reference semantics and replay tests.
4. The MongoDB adapter implements the same contract and indexes.
5. Foresight maintains the emitted source evidence required by the accepted
   contract, including `.gitmodules` blob identity, parent tree identity, and
   packet provenance.
6. A Katamorph mapping or shared event envelope connects the provider packet to
   Epiphany without making either runtime own the other's infrastructure.
7. Repository projections are rebuilt and compared against the current census
   artifact as an equivalence test.
8. Failure injection before and after every child-collection write and on both
   sides of finalization proves that same-digest replay converges to one exact
   complete child set, while pending evidence remains invisible.
9. Every adapter strategy's conformance tests pre-seed an in-flight import with
   a deterministic child observation ID, then replay the packet with a different
   canonical payload, schema identity, or versioned collection/type. Each
   mismatch must report a conflict without overwriting the child or publishing
   the run, and it must remain invisible to readers and projections. A
   byte-identical record replay must be an insert-if-absent no-op and still
   converge to one complete child set. The suite runs these cases against both a
   spanning-transaction implementation and a durable `pending` implementation
   when both strategies exist.

## Non-decisions

This boundary does not:

- classify any referenced repository as the user's work;
- infer ownership from mount path or organization;
- equate a GitHub locator with repository-family identity;
- assert that two forks or mirrors are the same family without commit evidence;
- turn current submodule topology into conceptual lineage;
- require all referenced repositories to become Foresight submodules;
- make MongoDB canonical for Git facts;
- discard the file artifact after import.
