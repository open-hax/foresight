# Epiphany persistence boundary for the repository census

## Status

Working cross-repository design constraint for the repository-census research.
It records the intended persistence boundary but does not modify Epiphany's
accepted laws or MongoDB collections by itself.

## Decision boundary

The Foresight repository-census crawler is an **evidence acquisition provider**.
Its generated EDN files are a lossless, revision-pinned evidence packet and
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

Each row contains several epistemically different facts and must not be stored
as one ambiguous repository edge.

It decomposes into:

1. **Observed submodule declaration**
   - parent repository reference;
   - parent commit OID;
   - exact `.gitmodules` path and blob OID;
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

Each row becomes an append-only gap observation with its source occurrence and
raw evidence. A gap is not a nullable field on a repository document.

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
:git/submodule-declaration-observed
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
 :observation/request-id ...}
```

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

- unique observation ID;
- unique request/idempotency ID where commands are replayable;
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
5. Foresight emits the additional source evidence required by the accepted
   contract, especially `.gitmodules` blob identity and packet provenance.
6. A Katamorph mapping or shared event envelope connects the provider packet to
   Epiphany without making either runtime own the other's infrastructure.
7. Repository projections are rebuilt and compared against the current census
   artifact as an equivalence test.

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
