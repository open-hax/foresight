# Interpreter / Corpus / Drive Boundary

- **Status:** working architecture note
- **Scope:** Calliope, Clio, Katamorph, NBB, Git corpus repositories, Google Drive media, Foresight, and Epiphany
- **Decision level:** design constraint, not yet an accepted ADR or migration receipt

## Signal

A repository is a storage and review boundary. It is not automatically an
application, runtime, or interpreter.

The intended stack is:

```text
Node.js
└── NBB                                  execution host
    ├── Katamorph                        portable resource language + interpreters
    ├── Clio                             immutable event language + canonicalization
    └── Calliope                         creative-domain laws + interpreter
          ▲
          │ reads validated programs, events, and documents
          │
    corpus repository                    small semantic source
          │ references immutable byte identities
          ▼
    Google Drive                         universal access plane + large-byte store
```

Foresight records the relationships among these locations. Epiphany observes and
indexes them. Neither should absorb the code, corpus, or blobs into a new
monolith.

## The critical distinction

“NBB hosts the interpreter” is more precise than “NBB is the application.”

- **Node.js** supplies the process and JavaScript host.
- **NBB** loads and runs reusable ClojureScript.
- **Katamorph** defines a portable resource/program grammar and small
  interpreters.
- **Clio** defines event admission, immutable history, schema identity,
  canonicalization, and projection laws.
- **Calliope** defines creative-domain types, laws, commands, folds, and
  provider-neutral interfaces.
- **Corpus repositories** contain particular programs and histories written in
  those languages.
- **Google Drive** stores large immutable byte replicas and universal
  human/agent-readable projections.

A corpus repository may select an interpreter version. It must not carry its own
copy of that interpreter.

## Not every EDN file is the same kind of “code”

The physical syntax is shared, but authority and evaluation differ.

| Class | Example | Meaning | May execute? | Authority |
| --- | --- | --- | --- | --- |
| Program | Katamorph resources, classifier DSL | Declarative behavior under a closed vocabulary | Only through its named interpreter | Accepted program file + interpreter version |
| Event | Clio ND-EDN event | Immutable fact or decision | Never as an arbitrary form; it is validated and folded | Canonical event set and causal graph |
| Schema snapshot | Clio catalog snapshot | Historical validation rules | Read by the kernel | Content-derived schema root |
| Authored corpus | Lyrics, lore, prompts, notes | Human or agent-authored source material | Input to bounded programs | Exact bytes + provenance |
| Projection | Search index, songbook, catalog, Sheet, Markdown index | Rebuildable convenience | No | Its source events/programs |
| Artifact manifest | Blob identity and provider locators | Cross-store reference | No | Verified digest and locator observations |
| Media blob | MP3, WAV, image, stems, video, archive | Immutable bytes | Consumed by adapters | Content digest, not filename or Drive path |

This classification prevents three recurring errors:

1. treating a projection as truth;
2. evaluating arbitrary EDN merely because other EDN is executable;
3. packaging the interpreter into every corpus that uses it.

## Repository roles

### `octave-commons/calliope` — interpreter/library

The active Calliope repository should contain:

```text
calliope/
├── src/cljc/calliope/law/
├── src/cljc/calliope/shape/
├── src/cljc/calliope/domain/
├── src/cljs/calliope/extern/
├── src/cljs/calliope/infra/
├── bin/*.nbb
├── test/
├── examples/                     small fictional fixtures only
├── deps.edn / nbb.edn / package.json
└── docs/                         architecture and API documentation
```

It should not contain:

- the living lyric/lore corpus;
- corpus-specific Clio ledgers;
- generated songbook or search projections;
- Suno exports or metadata dumps;
- MP3, WAV, JPEG, PNG, stems, video, or archive files;
- Git LFS pointers;
- machine-specific corpus roots such as `/home/err/Music`;
- a copied Clio, Katamorph, or NBB runtime.

Calliope should run its tests without the real corpus mounted.

### Corpus repositories — programs and small semantic history

One Calliope interpreter should be able to operate over many independent
corpora. A provisional corpus shape is:

```text
<corpus>/
├── corpus.edn                     Katamorph root resource
├── programs/                      corpus-specific declarative programs
├── ledgers/                       Clio ND-EDN partitions
├── schemas/                       historical Clio catalog snapshots
├── works/                         lyrics, lore, prompts, notes
├── artifacts/                     small manifests and locator events
├── projections/                   disposable indexes; optional in Git
├── README.md
└── LICENSE*
```

A corpus repository should normally have no `.clj`, `.cljs`, `.cljc`, or `.nbb`
implementation. Reusable semantics discovered in a corpus move upstream into
Calliope, Clio, or Katamorph. Corpus-specific behavior remains declarative EDN.

The current mixed corpus could become a transitional `calliope-corpus`, while
the continuing Fork Tales world may deserve a distinct `fork-tales-corpus`.
Those identities should be decided from lineage evidence rather than inferred
from the current directory name.

### Google Drive — large bytes and universal access

Use one canonical Drive folder **by folder ID**, not by display name.

Suggested shape:

```text
calliope-media/
├── _catalog/
│   ├── catalog.edn                machine-readable projection
│   ├── catalog.json               broad runtime compatibility
│   ├── CATALOG.md                 human/agent browsing
│   └── catalog-receipt.edn
├── blobs/
│   └── sha256/
│       └── <first-two>/<digest>/<original-name>
├── inbox/
│   └── <agent-or-session>/        proposals from runtimes without Git writes
├── audio/
├── images/
├── stems/
├── video/
└── archives/
```

Folder names are navigation aids. Drive file IDs locate provider objects.
Content digests identify immutable bytes.

Drive should also receive a published read replica of the corpus catalog and
selected text projections so mobile agents that cannot inspect Git can still
understand the current world.

## Identity and locator model

Do not use any of these as universal identity:

- repository path;
- Drive path;
- Drive display name;
- Drive URL;
- GitHub URL;
- Git LFS pointer path;
- Suno URL.

Separate domain identity, byte identity, and location.

```clojure
{:render/id "render:1c542acf-a06f-4466-91ae-3a6e16860480"
 :render/work "work:aquila-regina"
 :render/blob "sha256:211cce48219584744f7f9b23768e55f7ca7dace4d7d52fd28b477049b86eac0e"}
```

```clojure
{:blob/id "sha256:211cce48219584744f7f9b23768e55f7ca7dace4d7d52fd28b477049b86eac0e"
 :blob/bytes 4389702
 :blob/media-type "audio/mpeg"}
```

```clojure
{:locator/id "locator:..."
 :locator/blob "sha256:211cce48219584744f7f9b23768e55f7ca7dace4d7d52fd28b477049b86eac0e"
 :locator/provider :google-drive
 :google-drive/file-id "..."
 :google-drive/parent-folder-id "1VmGSkS4EBl2IA8FmlDsUt504Up6s6rd0"
 :locator/web-url "https://drive.google.com/file/d/..."
 :locator/observed-at "..."
 :locator/verified-at "..."
 :locator/verified-digest
 {:algorithm :sha-256
  :value "211cce48219584744f7f9b23768e55f7ca7dace4d7d52fd28b477049b86eac0e"}}
```

The legacy LFS occurrence remains evidence:

```clojure
{:locator/provider :git-lfs
 :git/repository "octave-commons/calliope"
 :git/commit "06322f54a059e6076d9062d7dfedd6c633d34517"
 :git/path "tracks/aquila-regina/211cce48.mp3"
 :git-lfs/oid "sha256:211cce48219584744f7f9b23768e55f7ca7dace4d7d52fd28b477049b86eac0e"
 :git-lfs/size 4389702
 :locator/status :legacy}
```

The current set of locators is a projection over append-only observations, not a
mutable vector treated as history.

## Clio event vocabulary

Provisional events:

```clojure
:corpus/declared
:corpus/interpreter-selected
:blob/declared
:blob/replica-observed
:blob/replica-verified
:blob/replica-unavailable
:blob/replica-retired
:artifact/attached
:projection/published
:proposal/submitted
:proposal/accepted
:proposal/rejected
```

The exact names belong in accepted domain law. The important invariant is that a
new Drive location appends a replica observation; it does not rewrite the
original Git/LFS occurrence.

## Katamorph responsibility

Katamorph should own portable shapes and resolution laws for:

- corpus resources;
- interpreter requirements;
- program resources;
- blob identities;
- provider locators;
- source/store declarations;
- projection publication targets.

Katamorph does not call Google Drive or GitHub. It validates the resource and
dispatches through injected resolver/store protocols.

Calliope owns creative meanings such as work, render, clip, arrangement, export,
rating, provenance, and release. Clio owns event/history semantics. Provider
adapters own API effects.

## Universal mobile-agent workflow

Google Drive is the common access plane, but not a second competing semantic
authority.

```text
Git-capable agent
  -> reads corpus Git + Drive catalog
  -> proposes/commits validated EDN and text
  -> bridge republishes Drive projections

Drive-only agent
  -> reads Drive catalog + text projections + media
  -> writes a proposal packet to Drive inbox
  -> bridge validates it with the pinned interpreter
  -> accepted proposal becomes Clio events / Git commits
  -> receipt and refreshed catalog return to Drive
```

This makes Drive universally useful without requiring concurrent unsupervised
dual writes to Git and Drive.

A proposal packet should carry:

```clojure
{:proposal/id "..."
 :proposal/corpus :fork-tales
 :proposal/interpreter
 {:id :calliope
  :version "git-sha-or-release"}
 :proposal/base
 {:corpus/commit "..."
  :catalog/digest "sha256:..."}
 :proposal/operations [...]
 :proposal/attachments ["sha256:..."]
 :proposal/actor "..."
 :proposal/created-at "..."}
```

The bridge must derive the submitting principal from an authenticated channel,
session, or credential and authorize that principal for the requested corpus,
base, operations, and attachments before interpreter validation or promotion.
`:proposal/actor` is claimed packet metadata, not proof of identity: it must
match the authenticated principal (or an explicitly authorized delegation), or
the bridge rejects the proposal without writing Clio events or Git commits.

## LFS-to-Drive migration

### Phase 1 — inventory and freeze

1. Pin the active Calliope revision and create a migration tag.
2. Enumerate every tracked LFS pointer with Git path, Git blob OID, LFS SHA-256,
   declared size, first-seen commit, and last-seen commit.
3. Record missing LFS objects as gaps. Do not silently omit them.

### Phase 2 — materialize and verify

1. Materialize each object from LFS or another evidenced local replica.
2. Compute SHA-256 and byte size.
3. Require equality with the LFS pointer OID and size.
4. Classify mismatches and unavailable bytes; do not upload them as verified
   replicas.

### Phase 3 — upload and round-trip

1. Upload the exact bytes to the canonical Drive folder.
2. Record Drive file ID, parent folder ID, display name, MIME type, size, and
   observed URL.
3. Download or stream the stored object and recompute SHA-256.
4. Append `:blob/replica-verified` only after round-trip equality.

### Phase 4 — create the corpus boundary

1. Create the corpus repository from small semantic files and append-only
   ledgers.
2. Add provenance that pins the source Calliope commit and path mapping.
3. Replace repo-relative `tracks/` references with blob identities.
4. Publish catalog projections into Drive.
5. Change Calliope tests to use fictional fixture corpora.

### Phase 5 — remove active LFS coupling

1. Delete media and LFS pointer paths from the active Calliope tree.
2. Remove LFS filter entries from `.gitattributes`.
3. Add CI gates rejecting:
   - the Git LFS pointer signature;
   - media extensions outside explicit tiny fixtures;
   - tracked blobs above a configured limit;
   - absolute machine paths;
   - unverified artifact references.
4. Require code-repo verification with no corpus checkout.

### Phase 6 — historical policy

“Remove LFS references” has two different meanings:

- **Active-tree removal:** no LFS pointers or LFS dependency in current/future
  Calliope. Old commits remain truthful historical evidence. This is the
  recommended first migration.
- **Full-history purge:** rewrite every affected commit and force-push. This
  breaks commit identities, review links, provenance references, and archaeology.
  Perform it only through a separate explicit decision.

If a completely LFS-free history is required, preserving the existing mixed repo
as a reference artifact and creating a clean code-only successor may be safer
than rewriting the archaeological source.

## Required proofs

The split is real only when all of these pass:

1. One installed Calliope interpreter runs two independent fixture corpora.
2. A corpus repository contains no interpreter implementation.
3. Calliope tests pass with no real corpus or Drive connection.
4. Corpus validation pins Calliope, Clio, Katamorph, and schema identities.
5. Arbitrary Clio ledger partitioning produces the same canonical history and
   projections.
6. A Drive blob resolves by verified content digest after rename or folder move.
7. Duplicate Drive display names cannot confuse resolution.
8. Missing Drive access becomes explicit unavailability, never an empty corpus.
9. A Drive-only proposal cannot enter canonical history without validation.
10. No active Calliope path contains an LFS pointer or oversized media blob.
11. Catalog projections rebuild byte-identically from the same inputs.
12. Foresight can cross-reference code repo, corpus repo, Drive folder, and
    Epiphany observation records without claiming ownership of any of them.

## Ownership matrix

| Concern | Owner |
| --- | --- |
| ClojureScript execution host | NBB |
| Portable resource grammar and resolver contracts | Katamorph |
| Event identity, causal graph, schema history, canonical replay | Clio |
| Creative-domain law and interpretation | Calliope |
| Particular lyrics, lore, prompts, programs, and accepted decisions | Corpus repository |
| Large byte replicas and universal mobile access | Google Drive |
| Host-specific agent artifact generation | Muse |
| Cross-repository constellation and references | Foresight |
| Observation persistence and research projections | Epiphany |
| Product-specific publication/translation effects | Their domain services/adapters |

## Non-decisions

This note does not yet decide:

- whether the first extracted repo is named `calliope-corpus` or
  `fork-tales-corpus`;
- whether authored lyrics remain Git-authoritative or use a Drive proposal lane
  before acceptance;
- whether any existing Drive folder becomes canonical;
- whether old Calliope history is rewritten;
- whether the artifact/locator resource vocabulary lands first in Katamorph,
  Clio, or a narrower shared package.

Those decisions should be made after inventorying the current corpus and Drive
duplicates, but they do not block accepting the interpreter/corpus/blob boundary.
