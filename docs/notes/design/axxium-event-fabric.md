# Axxium Event Fabric

**Status:** proposed execution architecture  
**Date:** 2026-08-31  
**Scope:** GitHub, Google Drive, Discord, test evidence, Proxx policy, Knoxx graph projections, Axxium identity, Katamorph resources, Clio/event-ledger records, and an Electron operator client.

## Decision

Build one identity-bound event fabric rather than separate GitHub, Drive, Discord,
test, Knoxx, and Proxx databases.

The fabric has five distinct layers:

1. **External interaction adapters** observe and act on GitHub, Google Drive, and
   Discord through provider-specific APIs.
2. **Axxium** assigns durable identities and bindings to principals, provider
   accounts, installations, external objects, and streams.
3. **Clio plus event-ledger** normalize, admit, order, replay, checkpoint, and
   retain append-only records. A domain owns its vocabulary; the ledger owns
   append and replay law.
4. **Knoxx** builds disposable document, search, and graph projections from those
   records. A projection may be rebuilt and is never promoted into source truth.
5. **Katamorph** declares the portable source, action, store, workflow, identity,
   and policy resources that each host interprets. Proxx evaluates routing policy
   as one such portable interpreter; an external Proxx service remains an
   execution and secret-custody adapter.

Foresight owns conformance and cross-repository proof. It does not silently take
local semantic authority away from the child repositories.

## Why this shape

The desired system has several different kinds of identity that must not be
collapsed:

- a human or service principal;
- a GitHub App installation, Discord bot installation, or Google authorization;
- a provider object such as a repository, pull request, file, guild, channel, or
  message;
- an append-only stream containing observations about that object;
- an admitted ledger record;
- a content identity shared by byte-equivalent documents;
- a runtime episode or test execution.

Axxium supplies the durable bindings between these things. It does not become an
event store, policy engine, graph database, or provider-token vault.

Similarly, a webhook delivery is not the authoritative state of a provider
object. It is a signal that causes hydration or reconciliation. The durable fact
is the admitted observation produced after the provider object or change feed is
read and normalized.

## Authority table

| Concern | Authority | Must not become authority |
| --- | --- | --- |
| Portable resource shapes and compatibility | Katamorph | Provider SDK objects |
| Principal, entity, provider-account, installation, and stream bindings | Axxium | Proxx, Knoxx, JWT payloads, filenames |
| Record admission, ordering, idempotency, replay, checkpoints, retention | event-ledger | Drive folders, webhook queues, Knoxx indexes |
| Event/receipt normalization and portable record dialect | Clio-compatible shapes | Raw provider payloads |
| GitHub/Drive/Discord API behavior | provider interaction adapters | Katamorph schemas |
| Search, document index, tag index, and graph projections | Knoxx | Event ledgers |
| OpenAI-compatible model-routing decisions | Proxx policy kernel and EDN policy resources | TypeScript route handlers |
| Provider credentials, quotas, live account state, and request execution | Proxx service adapters | Portable policy resources |
| Cross-repository composition and revision-bound proof | Foresight | Child domain implementations |

## Identity law

### Axxium bindings

Axxium should expose additive, versioned bindings for at least:

```clojure
{:binding/id "axxium:binding:..."
 :binding/kind :principal/provider-account
 :principal/id "axxium:principal:..."
 :provider :github
 :provider/subject-id "123456"
 :valid/from "2026-08-31T00:00:00Z"
 :valid/to nil}

{:binding/id "axxium:binding:..."
 :binding/kind :provider-object
 :provider :github
 :provider/scope "installation:987/repository:open-hax/foresight"
 :provider/object-kind :pull-request
 :provider/object-id "MDExOlB1bGxSZXF1ZXN0..."
 :object/id "axxium:object:..."}

{:binding/id "axxium:binding:..."
 :binding/kind :event-stream
 :object/id "axxium:object:..."
 :stream/id "axxium:stream:..."
 :stream/domain :github}
```

Provider IDs remain visible. Axxium identity does not erase source identity; it
binds it to a durable local identity with a history.

### Record identity versus event identity

New records must distinguish these identities:

- `:record/id` uniquely identifies one admitted physical record;
- `:stream/id` identifies the ordered Axxium-bound stream;
- `:stream/position` identifies the append position or backend sequence;
- `:event/id` identifies the normalized source occurrence for idempotency;
- `:source/delivery-id` identifies a webhook or Gateway delivery when supplied;
- `:source/object-id` and `:source/revision` identify hydrated object state;
- `:payload/hash` identifies normalized content.

No fold may discard records merely because a historical `:event/id` collides.
Historical event forms remain readable. New admissions require a unique
`:record/id`; idempotency is a declared key appropriate to the event profile,
not an assumption that every old `:event/id` is globally unique.

### Normalized record profile

```clojure
{:record/id "urn:uuid:..."
 :record/version 1
 :stream/id "axxium:stream:..."
 :stream/position 42

 :event/id "github:delivery:...:pull_request:synchronize"
 :event/kind :github/pull-request-observed
 :event/role :observation
 :event/occurred-at "2026-08-31T15:01:00Z"
 :event/observed-at "2026-08-31T15:01:02Z"

 :source/provider :github
 :source/delivery-id "..."
 :source/object-id "..."
 :source/revision "head-sha-or-provider-version"
 :source/locator {:installation/id "..."
                  :repository/id "..."
                  :repository/name "open-hax/foresight"}

 :identity/principal-binding "axxium:binding:..."
 :identity/object-binding "axxium:binding:..."
 :identity/installation-binding "axxium:binding:..."

 :causal/root "urn:uuid:..."
 :causal/parent "urn:uuid:..."
 :correlation/id "..."

 :contracts [{:resource/id :open-hax/github-interaction
              :resource/revision "git-sha"}]

 :payload/hash "sha256:..."
 :payload {...}
 :privacy/classification :workspace
 :retention/policy :source-history}
```

A record profile may omit fields that do not apply, but it may not silently
invent a principal, tenant, causal parent, revision, or successful outcome.

## Ledger families and file layout

Use newline-delimited EDN: one complete EDN map per nonblank line. Keep ledgers
small enough to inspect and mirror. Do not build one ever-growing shared Drive
file.

Each repository-local ledger remains under `.ημ/`; `.eta-mu` is a compatibility
locator, not a second authority.

```text
.ημ/
  ledgers/
    github/<installation-or-repository-stream>/segments/<first>-<last>-<sha256>.edn
    google-drive/<drive-or-root-stream>/segments/<first>-<last>-<sha256>.edn
    discord/<guild-or-dm-stream>/segments/<first>-<last>-<sha256>.edn
    tests/<repository-or-target-stream>/segments/<first>-<last>-<sha256>.edn
    tags/<workspace-stream>/segments/<first>-<last>-<sha256>.edn
  manifests/
    <stream-id>.edn
  checkpoints/
    github/<installation-id>.edn
    google-drive/<authorization-id>.edn
    discord/<bot-installation-id>.edn
  projections/
    document-index.edn
    object-index.edn
    tag-index.edn
    graph-index.edn
```

Segment files are immutable after publication. A manifest references segment
hashes and positions. Appending creates or seals a new segment and advances the
manifest through an expected-position comparison. Compaction appends a receipt;
it never rewrites history without evidence.

## Google Drive mirroring

Google Drive is the universal off-device mirror and discovery surface, not the
place where multiple writers append to the same mutable text file.

For every discovered `.ημ/` or `.eta-mu/` source:

1. identify the repository, Drive object, or Discord attachment/message that
   exposed it;
2. bind the source and stream through Axxium;
3. verify every immutable segment hash;
4. copy missing segments to the Drive mirror;
5. append a `:ledger/mirror-observed`, `:ledger/segment-mirrored`, or
   `:ledger/mirror-diverged` record;
6. project a catalog that maps every source locator to every Drive object ID;
7. never infer sameness from filename alone.

Suggested Drive layout:

```text
Axxium Event Fabric/
  ledgers/<stream-id>/<segment-hash>.edn
  manifests/<stream-id>.edn
  indexes/document-index.edn
  indexes/ledger-catalog.edn
  receipts/<yyyy>/<mm>/...
```

The Drive change feed wakes the reconciler. The reconciler then reads the change
feed and hydrates changed files. Notification headers are retained as raw signal
evidence, but they are not treated as the changed object itself.

## Document tagging and duplicate handling

“Tag every document” means event-source the classification. It does not mean
rename every Drive file or inject mutable frontmatter into provider-owned
objects.

A tag assertion is a record:

```clojure
{:event/kind :tag/asserted
 :event/role :attestation
 :subject/object-binding "axxium:binding:..."
 :tag/id :artifact/architecture
 :tag/value true
 :tag/source :classifier/rule
 :tag/confidence 1.0
 :tag/rule {:id :rule/path-and-content-v1
            :revision "sha256:..."}}
```

Retraction is another record. The current tag set is a projection.

The document index is keyed by provider object binding, not title or path. It
should retain:

```clojure
{:object/id "axxium:object:..."
 :provider :google-drive
 :provider/object-id "drive-file-id"
 :provider/parents ["drive-folder-id"]
 :title "README.md"
 :mime/type "text/markdown"
 :content/hash "sha256:..."
 :content/equivalence-class "sha256:..."
 :revision/id "provider-revision"
 :tags #{:system/eta-mu :artifact/document}
 :first-observed-at "..."
 :last-observed-at "..."
 :tombstoned? false}
```

Byte-equivalent documents may share a content-equivalence node while retaining
separate provider object identities, locations, permissions, revisions, and
histories. Similar-but-not-identical documents remain separate and may receive a
`:content/related-to` graph edge rather than a deduplication rewrite.

## Portable interaction layer

Do not start with one giant provider API. Define the smallest shared operations
that at least two adapters actually implement:

```clojure
(defprotocol InteractionAdapter
  (describe-capabilities [adapter])
  (discover! [adapter request])
  (hydrate! [adapter object-ref])
  (watch! [adapter subscription])
  (reconcile! [adapter checkpoint])
  (checkpoint [adapter])
  (apply-command! [adapter command]))
```

All calls receive and return Clojure-shaped maps. SDK objects are decoded in the
provider extern adapter. A result must say whether it is complete:

```clojure
{:interaction/status :ok        ; :ok :partial :blocked :unavailable
 :interaction/provider :github
 :interaction/capabilities #{:discover :hydrate :watch :reconcile}
 :interaction/objects [...]
 :interaction/signals [...]
 :interaction/next-cursor "..."
 :interaction/coverage {:scope "installation:987"
                        :complete? false
                        :reason :permission-limited}
 :interaction/evidence [...]}
```

Katamorph can express the first version by composing existing resource kinds:

- `:source` for watch, discover, hydrate, and emitted event profiles;
- `:action` for provider commands;
- `:store` for ledger/checkpoint capabilities;
- `:workflow` for backfill, reconcile, renew, and mirror jobs;
- `:actor` for the declared service actor;
- `:policy` for permission and retention decisions.

Do not add a universal `:interaction` kind until the GitHub and Drive adapters
show a stable shared shape. The current `:provider` contract is model-provider
specific and must not be overloaded to mean GitHub, Drive, or Discord.

## GitHub profile — first vertical slice

Use a GitHub App rather than repository-by-repository personal OAuth hooks.
Separate user login from installation authority:

- a user access token represents an authorized human acting through the app;
- an installation token represents the app acting within granted repositories;
- Axxium binds both to durable principals and installation identities;
- the app requests only the permissions needed by the selected repositories and
  webhook events.

Acquisition flow:

```text
verified webhook
  -> raw delivery signal record
  -> acknowledge quickly
  -> classify event
  -> hydrate repository/object state through REST or GraphQL
  -> normalized observation record
  -> tag/document/graph projections
  -> periodic delivery and repository reconciliation
```

Required laws:

- validate the webhook signature before admission;
- retain `X-GitHub-Delivery` and event/action identity;
- deduplicate redelivery without erasing separate normalized observations;
- enqueue hydration before slow processing;
- reconcile failed or missed deliveries on a schedule;
- bind every repository and object to its GitHub App installation;
- discover `.ημ` and `.eta-mu` paths, manifests, and segment files without
  treating a symlink or copied filename as identity;
- record permission-denied and rate-limited coverage as partial, not empty.

The first Katamorph/Knoxx resource pack should declare:

```clojure
{:namespace :open-hax.github
 :resources
 [{:actor/id :event-indexer
   :actor/kind :agent
   :actor/contract :open-hax/github-event-indexer}

  {:source/id :app-events
   :source/type :event
   :source/driver :github/app
   :source/actor :open-hax.github/event-indexer
   :source/listens [{:event/type :github/webhook}]
   :source/emits [{:event/type :github/delivery-received}
                  {:event/type :github/object-observed}]
   :source/protocol {:delivery :at-least-once
                     :checkpoint :github/delivery-id
                     :reconcile :github/deliveries-and-api}}

  {:store/id :event-ledger
   :store/schema :clio/event-record-v1}

  {:action/id :hydrate-object
   :action/kind :github/hydrate-object}

  {:action/id :reconcile-installation
   :action/kind :github/reconcile-installation}

  {:workflow/id :github-index-and-mirror
   :workflow/triggers [{:on/event :github/webhook}
                       {:on/cron "17 */3 * * *"}]
   :workflow/jobs
   [{:job/id :observe
     :job/steps [{:step/id :hydrate
                  :step/action :open-hax.github/hydrate-object}]}
    {:job/id :reconcile
     :job/steps [{:step/action :open-hax.github/reconcile-installation}]}]}]}
```

Knoxx interprets the source as an ingestion/indexing profile and projects the
GitHub object graph. Eta-mu owns the GitHub event classifier and workflow
coordination. Katamorph owns only the portable declaration.

## Google Drive profile

Use Google OAuth linked through Axxium for user-owned Drive access. Service
accounts remain an explicit deployment profile, not the default identity model.

A Drive notification is a wake-up signal. The adapter retains the notification
channel identity and message number, then consumes `changes.list` from its saved
page token. Channel renewal is a workflow because Drive notification channels
expire.

The Drive index should preserve file ID, drive ID, parent IDs, MIME type,
revision/version, permissions coverage, exported content hash, and tombstone
state. Google-native Docs, Sheets, and Slides are exported through declared
formats for content hashing; the original provider object remains authoritative.

## Discord profile

Discord history is built from two complementary sources:

1. REST discovery/backfill for objects the bot is authorized to read;
2. Gateway dispatch events for changes observed after the bot is connected.

Discord outbound webhooks are useful for publishing notifications, but they are
not the source for a complete Discord history. The inbound observation path is a
bot using the Gateway and REST API.

Persist the Gateway sequence, session identity, intents, guild/install binding,
and resume state. Hydrate partial dispatch payloads through REST when allowed.
Run periodic reconciliation because disconnects, permission changes, deletions,
and intent limits can leave gaps.

The truthful scope is **every Discord object observable under the bot's granted
guilds, channel permissions, API endpoints, and Gateway intents**. It is not all
of Discord. Message bodies, embeds, attachments, components, and polls may be
unavailable without the Message Content privileged intent. Objects deleted
before first observation may be unrecoverable; the coverage record must say so.

## Test results as ledger facts

Lift the Foresight revision-bound evidence runner into a reusable event-producing
runner. Every invocation writes at least two records even when the command fails:

```text
:test/run-started
:test/run-finished
```

A finished record includes:

```clojure
{:event/kind :test/run-finished
 :event/role :attestation
 :test/run-id "axxium:episode:..."
 :test/target :open-hax/proxx
 :test/gate-kind :integration
 :test/command ["pnpm" "test"]
 :test/revision {:repository "open-hax/proxx"
                 :commit "..."
                 :tree "..."
                 :dirty? false
                 :inputs/hash "sha256:..."}
 :test/environment {:runner/id "axxium:principal:..."
                    :runtime {:node "..." :nbb "..."}
                    :platform "linux-x64"}
 :test/outcome :passed       ; passed failed blocked unavailable not-applicable
 :test/counts {:tests 52 :assertions 216 :failures 0 :errors 0}
 :test/artifacts [{:kind :junit :hash "sha256:..." :locator {...}}]
 :test/stdout {:hash "sha256:..." :locator {...}}
 :test/stderr {:hash "sha256:..." :locator {...}}
 :contracts [{:resource/id :foresight/revision-bound-gate
              :resource/revision "..."}]}
```

A process wrapper must append the started record before spawn and attempt to
append a terminal result from normal exit, spawn error, timeout, cancellation,
and signal handling. `passed`, `cached`, `failed`, `blocked`, `unavailable`, and
approved `not-applicable` remain distinguishable.

A previous pass may satisfy a later gate only when the trusted catalog,
contracts, target input hash, dependency closure, and required environment facts
match. A recorded failure never suppresses a rerun. Deleting test ledgers loses
speed and history, not correctness.

## Proxx as service plus embeddable eta-mu plugin

Proxx already has the correct semantic center: ordered EDN policy resources and
a CLJS interpreter. Finish the migration by separating the policy kernel from
the live proxy service.

```text
proxx.policy.kernel       pure CLJS/CLJC policy loading, validation, compilation,
                          preview, provider/model/account selection
proxx.policy.resources    versioned EDN policy programs using Katamorph shapes
eta-mu.proxx.plugin       in-process adapter that loads and invokes the kernel
proxx.runtime.nbb         NBB HTTP/CLI/worker host for Node-adjacent effects
proxx.service             secret custody, provider OAuth, account state, quotas,
                          streaming request execution and compatibility endpoints
```

Rules:

- eta-mu may evaluate a Proxx policy without a network call;
- eta-mu may not gain access to provider secrets merely by loading the plugin;
- Proxx receives an Axxium principal/actor binding and returns a policy decision
  that cites policy resource revisions;
- Knoxx passes the same Axxium identities, so authorization and routing policy
  can be shared without sharing application-local sessions;
- provider credentials stay in the Proxx execution adapter;
- TypeScript remains only as shrinking compatibility edges during migration;
- new backend policy, routing, queue, model-family, or provider-selection logic
  stays in EDN and CLJS;
- NBB is the first backend host where its SCI/Node surface is sufficient;
- compiled shadow-cljs remains legitimate for browser artifacts and any backend
  slice that cannot yet run lawfully under NBB. Runtime migration must not fork
  the policy semantics.

Cleanup order:

1. freeze module ownership and delete no compatibility edge yet;
2. extract and publish the pure policy kernel with fixtures;
3. add the eta-mu plugin and conformance tests;
4. add an NBB host around existing CLJS boundaries;
5. replace TypeScript HTTP/database/auth edges one bounded slice at a time;
6. remove an old edge only after its live and test behavior is reproduced;
7. keep the external service for execution, secrets, quotas, and streaming even
   when policy evaluation can happen in-process.

The existing AT Protocol federation draft remains useful lineage: owner-scoped
append-only diffs, resumable cursors, DID references, and lazy projections fit
this event fabric. Axxium should absorb the durable identity and binding law;
Proxx should not invent a second principal system for federation.

## OAuth and Electron boundary

The Electron client is an operator surface over Axxium, not a new identity
provider.

- Login choices: GitHub, Google, Discord.
- Authorization callback: system browser plus Authorization Code with PKCE.
- Axxium links the provider subject to an existing or new principal through an
  explicit identity-link event.
- GitHub App installation, Google Drive consent, and Discord bot/guild install
  are separate grants from human login.
- Long-lived refresh tokens and provider secrets live in the main process or OS
  credential store, never renderer storage.
- The renderer has no Node integration for remote content, uses context
  isolation and sandboxing, and receives a narrow validated IPC API.
- OAuth success does not imply permission to every repository, Drive object,
  guild, channel, or message. Interaction coverage is recorded per grant.

## Projection graph

Knoxx should project at least these node kinds:

```text
AxxiumPrincipal
ProviderAccount
ProviderInstallation
Repository
GitObject
Issue
PullRequest
Review
Drive
DriveFile
DriveRevision
DiscordGuild
DiscordChannel
DiscordThread
DiscordMessage
DiscordAttachment
LedgerStream
LedgerSegment
TestRun
PolicyResource
Tag
ContentIdentity
```

And these edge kinds:

```text
BOUND_TO
INSTALLED_IN
OBSERVED_AS
REVISION_OF
PARENT_OF
REFERENCES
MENTIONS
ATTACHED_TO
MIRRORED_AS
CONTAINS_LEDGER
CAUSED_BY
TESTED
PROVED
EVALUATED_BY
TAGGED_WITH
CONTENT_EQUIVALENT_TO
```

Every projected node and edge carries the record IDs and stream positions from
which it was derived. Deleting and rebuilding the graph must reproduce the same
identity and relations for the same admitted history.

## Execution sequence

### Slice 1 — GitHub contract and fixture

- Land Katamorph GitHub source/action/store/workflow resources.
- Add an eta-mu classifier fixture for one signed webhook.
- Bind app installation, repository, sender, and object through Axxium fixtures.
- Append raw delivery plus hydrated object observation to an in-memory and EDN
  reference ledger.
- Project the object and its `.ημ` discovery into Knoxx.
- Emit revision-bound test results for the slice.

### Slice 2 — immutable Drive ledger mirror and document tags

- Create the Drive mirror root and stream catalog.
- Scan selected Drive roots and GitHub repositories for `.ημ` / `.eta-mu`.
- Hash and mirror immutable segments.
- Build the Axxium-keyed document and tag projections.
- Renew Drive watch channels and reconcile through change-feed page tokens.

### Slice 3 — Discord observable-history index

- Add bot installation and guild bindings.
- Backfill guild/channel/thread/message objects within granted permissions.
- Append Gateway dispatches with sequence/resume evidence.
- Reconcile partial payloads and gaps.
- Project Discord objects into the same Knoxx graph/query contract.

### Slice 4 — test-evidence runner

- Generalize Foresight's exact-revision evidence runner.
- Append started/terminal records for local, CI, and agent-triggered tests.
- Project current proof state without treating projection as authority.
- Mirror immutable test segments to Drive.

### Slice 5 — Proxx kernel, eta-mu plugin, and NBB host

- Extract policy evaluation behind a pure API.
- Validate Proxx EDN through Katamorph.
- Pass Axxium actor/capability facts into policy context.
- Add the in-process eta-mu adapter.
- Move one backend route through NBB with parity and live evidence.

### Slice 6 — Electron operator client

- Add Axxium OAuth login and provider grant management.
- Show coverage, sync checkpoints, gaps, ledger mirror state, and test evidence.
- Reuse Knoxx graph/query surfaces rather than creating a second graph UI.

## Non-goals

- One universal payload vocabulary for every domain.
- Treating webhook delivery as provider truth.
- Treating Drive as an atomic multi-writer append database.
- Rewriting historical ledgers to fit a new envelope.
- Deducing identity from email, username, filename, title, or content hash alone.
- Claiming access to Discord objects outside granted scopes or retained history.
- Moving provider secret custody into Katamorph resources, Axxium event records,
  eta-mu plugins, or Drive.
- Replacing all Proxx edges, all Knoxx graph code, or all test runners in one PR.

## Existing evidence and work to reconcile

- Foresight `AGENTS.md`: child authority, pure laws first, NBB runtime ladder, and
  `.ημ` provenance location.
- Foresight revision-bound evidence gate and exact-head runner.
- eta-mu #147: actor-runtime seam across Katamorph, Axxium, Sol, and event-ledger.
- eta-mu #159: portable event-ledger taxonomy and backend law.
- eta-mu #206: GitHub webhook/event classifier CLJS rewrite.
- eta-mu #233: ledger-recorded content-hash test results.
- eta-mu #248: historical event-ID collision hazard.
- Axxium Knoxx and Proxx identity-migration cards.
- Axxium Discord OAuth card.
- Knoxx Google Drive ingestion issue #73.
- Knoxx graph query contract and source-lake/graph work.
- Proxx AT-DID federation draft and current CLJS/EDN policy boundary.
- Katamorph source, action, store, workflow, actor, policy, model, and provider
  resource schemas.

## Provider protocol anchors

- GitHub Apps and permissions:
  https://docs.github.com/en/apps/creating-github-apps/about-creating-github-apps/about-creating-github-apps
- GitHub webhook delivery recovery:
  https://docs.github.com/en/webhooks/using-webhooks/handling-failed-webhook-deliveries
- Google Drive change feed:
  https://developers.google.com/workspace/drive/api/guides/manage-changes
- Google Drive push channels:
  https://developers.google.com/workspace/drive/api/guides/push
- Discord Gateway and intents:
  https://docs.discord.com/developers/events/gateway
- Discord Gateway events:
  https://docs.discord.com/developers/events/gateway-events
- Electron security checklist:
  https://www.electronjs.org/docs/latest/tutorial/security

## First completion gate

The first implementation is complete only when a signed GitHub fixture can be
admitted twice without duplicate semantic effects, hydrated into an Axxium-bound
object observation, written to an EDN ledger, mirrored as an immutable Drive
segment, projected into the Knoxx graph, and accompanied by a revision-bound test
result whose own record survives replay.
