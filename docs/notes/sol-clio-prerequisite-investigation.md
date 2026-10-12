# Sol → Clio compatibility investigation

Observed 2026-10-03. This is prerequisite evidence, not an implemented migration,
Rheos admission, or service qualification. Parent owns the canonical card.

## Scope and proposed split

Sol #2 remains the admitted 3-point Node 22 baseline. Its four immutable Git
pins stay unchanged. The obsolete credential seam has been removed now that
event-ledger is public. Baseline head e4ae8d4f50625a254d1b219f9f4582156957b959
passed hosted runtime run 37116509976: 125 tests / 467 assertions, zero failures,
errors or compiler warnings, production-only frozen install, actual localhost
Sol health and SIGTERM exit 0. CodeRabbit completed native exact-head reviewed
coverage with an explicit no-material-risk verdict. Sol #2 was verified merged
as 72fdecca292859d92cba48b686ec6fe9c43a9133; its tree exactly matches that
reviewed head. Postmerge main run 37117009854 independently passed all actual
runtime steps as well. Three of five review rounds were used; two remain.

The user separately corrected record authority to Clio. A coherent complete
replacement is estimated at **5 points**, beyond the original card: roughly
18–22 paths across source, tests, dependency/build metadata and planning docs.
The parent should admit a separate Clio prerequisite after qualified Sol #2,
using a new UUID and acceptance criteria below; decompose it through Rheos if
the board requires cards at most 3 points. A partial pin/import rename would
leave both record shape and the session store wrong. No implementation edits
for Clio have been made in the candidate tree.

Proposed decomposition is an event catalog + real Clio adapter slice (3 points)
and session-ledger/compatibility slice (2 points). If stacked PRs are used, the
first must explicitly retain the predecessor only for the still-unmigrated
session adapter, and the final qualified layer must remove that dependency
and every production import. Do not describe the intermediate coexistence as
the completed correction. Alternatively, use one coherently reviewed 5-point
PR after the parent has qualified its breakdown.

## Exact sources

- Before: `io.github.open-hax/event-ledger` at
  `ada7374b7f4e1c3b0ab4e6bbe996f10f06e9b93a`.
- Proposed after: `io.github.open-hax/clio`, explicit Git URL
  `https://github.com/open-hax/clio.git`, exact main commit
  `788cdd3434615a7932b924e68520dbf7f88408c2`, tree
  `1c3ec6f29a6fee56c26cfe9fb69d696fa97fb1b8`.
- A public anonymous Git clone and fresh tools.deps Gitlibs checkout both
  resolved that exact Clio revision. No root AGENTS.md exists at this revision;
  README and source describe the local boundaries. Sources were read-only.
- Eta-mu `0ed56aa…` and Katamorph `305a5e4…` are outside this pin correction.
  Neither pinned eta-mu subdirectory declares an event-ledger transitive dep.

## API comparison

| Capability | Current predecessor API | Actual Clio API / consequence |
| --- | --- | --- |
| Validation | `open-hax.event-ledger/validate-envelope` returns `{:valid …}` | `clio.domain.schema/validate-event! [revisions event]` returns the event or throws. Historical root + schema id + leaf hash are required. |
| Event type/time | String `"sol.run.started"`, `:event/time` | Qualified keyword, e.g. proposed `:sol.run/started`, `:event/at`. These names must be approved as Sol-owned schema IDs. |
| Envelope | `:envelope/version`, actor descriptor, causal root/parent, run/session/turn/episode, `:payload`, contract refs | Closed Clio envelope: `:event/schema`, stream, seq, causes, actor, subject, at, data. Sol-owned correlation, principal references and payload belong in a reviewed data catalog, not new Clio core fields. |
| Identity | Any string accepted, tests use `event-1` | Valid UUID strings. Rewrite test fixtures; preserve existing historical IDs rather than relabeling them in place. |
| Ordering | Mongo assigns global `:ledger/seq`; Sol remembers root/parent in atoms | Explicit stream + contiguous positive seq; previous stream event must be a direct cause. Sol owns stream/episode policy, Clio owns admission and canonical graph checks. |
| Append | Async `append-event [db envelope]`, optional injected one-arg appender | Sync `clio.infra.ledger/append-event! [revisions path event]` returns `:appended` or `:already-present`. `clio.infra.runtime/append! [runtime path schema-id data]` returns event/runtime/result. Never pass a Mongo handle to this API. |
| Exact retry | Mongo duplicate ID returns existing doc, even if new content differs | Exact same record dedupes; same UUID with different data throws; same stream slot with another UUID throws. `runtime/append!` creates a new UUID/time on every invocation; retry the prepared event through ledger/append-event!, not by rebuilding it. |
| Storage | Mongo event collection plus optional TTL/change stream APIs | Existing EDN partition with OS advisory inode lock; explicit `create-ledger!`. No Mongo adapter, TTL or change-stream equivalent. Sol does not currently call watcher APIs, so no watcher migration is required in this minimal slice. |
| Read/replay | Sol session store splits lines and silently drops unreadable EDN | Clio `read-ledger`, strict existing-partition `read-ledgers`, `canonicalize-files`, pure canonicalize/projection. Explicit read-ledgers refuses missing partitions; read-ledger alone returns [] for a missing path. Recovery must not silently select the permissive path. |
| Persistence acknowledgment | Driver insert acknowledgment; Sol terminal failure reporter | Clio append completes a locked synchronous write; **no fsync/fdatasync** occurs. OS locking/idempotence is proven, power-loss durability is not. Hosted-worker durable admission/outbox needs separate upstream Clio qualification. |

## Exact Sol integration points

Three production files import predecessor namespaces:

1. `src/cljs/open_hax/sol/shape/episode_event.cljs`: principal/resource projection
   and `envelope`; replace envelope construction/validation using the supplied
   Clio revision and Sol event-data catalog. Preserve Axxium/Katamorph references;
   do not recreate their authoritative laws in Sol or invent authenticated identity.
2. `src/cljs/open_hax/sol/infra/agent/episode_ledger.cljs`:
   `configured-appender`, `create-episode`, `emit!`; replace Mongo DB dispatch
   with explicit Clio runtime + ledger capability. Build/stabilize candidate
   identity before append, advance stream/parent only after accepted append,
   propagate typed append failures. Existing injected callbacks are transport
   seams, not proof that Clio has been configured. Decide/review legacy config
   aliases; an old `:event-ledger-db` must never silently become a no-appender
   success after migration.
3. `src/cljs/open_hax/sol/infra/agent/session_store.cljs`:
   `append-event!`, `append-ledger!`, `read-ledger`; current implementation
   validates a predecessor envelope then rewrites existing text + new line.
   Replace canonical record I/O with Clio-owned append/read validation. Mutable
   state/runs remain projections; do not turn this prerequisite into a repair
   of the stubbed run-store protocol methods or a new recovery runtime.

Adjacent contracts and tests:

- `infra/agent/episode_turn.cljs` wraps the lifecycle; preserve blocked starts,
  executor failure, and separately reported terminal persistence failure.
  Rename vocab/config only as required; execution remains the existing loop.
- Existing tests `shape/episode_event_test.cljs`,
  `infra/agent/episode_ledger_test.cljs`, `episode_turn_test.cljs`,
  `episode_terminal_test.cljs`, and the service episode fixtures need Clio IDs,
  shape and callback expectations. The configured-DB test currently mocks
  append-event, so replacing that mock is insufficient operation proof.
- Add real Sol-adapter operation tests and session-store tests; the current
  source has no session_store_test.cljs. Bootstrap creates the session store
  at bootstrap.cljs:94, but the public session event append/read convenience
  APIs have no in-repo production callers. That bounds the migration, not a
  license to leave the advertised persistence API wrong.
- `deps.edn`, `package.json`, `package-lock.json`, `shadow-cljs.edn`: Clio Git
  source declares Maven malli 0.16.4, edamame 1.6.42, promesa 11.0.678.
  Its host adapter additionally needs runtime npm `fs-ext-extra-prebuilt`
  **2.2.9**. tools.deps does not install that native package. Add a frozen
  runtime dependency and check compiled ESM plus production-only installation.
  Clio uses `node:crypto` and the native package at the extern boundary;
  ensure Shadow retains imports as appropriate.
- `AGENTS.md`, `README.md`, `docs/design/persistent-review-workers.{md,edn}`,
  `docs/design/sol-build-evidence.md`, and `receipts.edn`: change current
  authority/pin claims together. Existing plan checker derives full pin set
  from deps; preserve all 16 integration anchors, proposal disabled, and test
  the updated pin contract. Append ADA→Clio provenance correction; never
  rewrite historical receipt bytes or earlier build observations.

## Actual runnable compatibility proof

Isolated, ignored probe: `.ημ/node22-prerequisite/clio-probe/`.
`deps.edn` consumes the immutable Git pin, not a sibling source tree.
Exact manifest dependencies were resolved into a recorded npm v3 lock, then
installed with normal **npm ci**, Node 22.20.0/npm 10.9.3, 15 packages.
Lock SHA256: `51ac6cab08de73cb0f4d80027aa159dce1051b38bdca2a9616e5b8a2f68b024f`.

Commands executed with an explicit credential-free environment:

```sh
node node_modules/shadow-cljs/cli/runner.js compile test esm
node dist/probe.js
```

Both actual compiled CommonJS autorun and compiled ESM execution completed
**1 test / 13 assertions, 0 failures / 0 errors**. Both compilation targets
reported **0 warnings**. Assertions performed real file creation, schema
snapshot persistence, two causally linked appends, readback, exact duplicate
retry, schema/runtime reopen, partition/deduped canonical replay, ID collision
and stream-slot conflict rejection, missing partition rejection, old Sol
envelope rejection, and stream-gap rejection. No Mongo, credentials, server,
provider generation, service activation or board mutation was involved.

Machine proof: `clio-compatibility-proof.json`. Source fixture and logs are in
the probe directory. This establishes Node22 Clio API/native/ESM feasibility,
**not** Sol-adapter correctness, crash recovery, cancellation, durable outbox,
fsync, Windows/NFS behavior, or hosted MiMo/Kimi execution.

## Proposed acceptance for separate prerequisite

1. Clio exact Git coordinate replaces ADA after the final coherent layer;
   no production event-ledger imports remain; other pins/history stay intact.
2. Sol-owned event-data catalog uses Clio's complete envelope law; validates
   supplied identity references through their proper owners, never local law
   copies. Tests reject invalid schema, UUID/stream/causal facts and old envelope
   input at the new canonical boundary.
3. Actual Sol episode appends/readback/retry/reopen run on disk with Clio,
   including collision and failed-append causality behavior. No callback-only
   mock counts as this proof. Preserve existing start/terminal failure policy.
4. Session canonical events use Clio I/O; legacy files are preserved bytewise
   and are never falsely claimed as Clio records. A reviewed new partition or
   explicit compatibility-reader/migration plan governs old data. Do not
   silently mix old envelopes and new Clio events in one canonical file.
5. Fresh frozen Node22 install and production-only native import are tested;
   actual full CLJS tests, zero-warning server build, health + SIGTERM pass
   on hosted CI. Add these real-operation tests to candidate CI.
6. Correct proposal compatibility/pins and append-only evidence, one trusted
   exact-head pass and settled findings within its separate review budget.
   Auto-merge off until qualified. Parent owns card transitions/admission.
7. Explicitly retain hosted-worker non-readiness: Clio fsync/outbox/recovery,
   authenticated Knoxx controls, eta-mu admission/publication, Katamorph shapes,
   and Services deployment remain owner-qualified prerequisites.

## Subsequent admission/policy clarification (append-only addendum)

The full absolute artifact paths and smallest staged five-point proposal are
now recorded in `clio-admission-handoff.md` beside this report. The user directs
that no Clio implementation begins before parent-owned Rheos admission.

The earlier five-round cap and Sol #2 3/5 result above belong to the prior
policy and remain historical evidence. For future work, direct human steering
sets five **completed** review rounds as a **soft minimum, not a cap**, or
unanimous early approval; first five rounds prefer fixes for all verified
findings, and outright rejection requires independent non-CodeRabbit agreement.
The global policy worker owns its isolated checkout; do not execute the mixed
uncommitted shared pr-flow CLI. No old results have been rewritten.

The recorded 13-assertion Clio fixture exercises sequential native locked
operations; it does not establish cross-process exclusion, crash recovery or
power-loss durability. The source's lock protocol is inspectable, and fsync is
absent. Treat hosted durability as unqualified pending its owner's work.

## Parent qualification of this captured report

The report above preserves the worker's original investigation, including its then-current hard five-review-budget wording. The user's newer global PR-policy correction makes five a soft minimum of completed rounds, with unanimous early approval; the canonical skill pack controls the current workflow. Historical review counts are observations, not current request caps.

The parent created an incoming five-point epic and three-/two-point child cards through Rheos. The installed dependency frontmatter operation refused `dependency`, so intended child prerequisites remain proposed in their Markdown bodies, not machine-admitted edges. This gap is tracked at https://github.com/open-hax/rheos/issues/3. No ready transition or Clio implementation is claimed. The default Rheos-created epic identity is its recorded slug; explicit child UUIDs and existing parent identity are retained without a local identity rewrite.

The standalone Node22 baseline merge is independently verified through the live API. The eventual episode/session adapter tests and final dependency removal remain acceptance work, and the separate Clio fsync/outbox prerequisite remains unresolved for hosted review jobs.

## Independent planning-review clarifications

The report's sibling evidence paths refer to the original Sol worktree at `/home/err/spaces/review-repair/sol-server-workers/.ημ/node22-prerequisite/`, not files beside this captured note. A portable byte-preserved copy of its exact-pin fixture, frozen manifests, original machine proof and original logs is now published under [`../verification/sol-clio-probe/README.md`](../verification/sol-clio-probe/README.md). The handoff and full artifact inventory remain local inspection artifacts in that original directory; their existence is not claimed in this root PR. Original-proof.json retains its historical local source path, and the README identifies the copied source explicitly. Publication alone is not a new run or Sol integration evidence.

Clio append admission and complete-history canonicalization are separate boundaries. Append validates event/schema and detects identity/stream-slot collisions; canonicalization checks missing causal parents, stream gaps and predecessor causality across the selected partitions. The probe's stream-gap check uses canonicalization, not append. The episode card now makes this distinction explicit and proposes Node22 prerequisite UUID `146f1b47-c6a7-5a37-996b-a381dd91f6b6` pending actual relationship admission through Rheos #3. All three cards remain incoming; no machine dependency, readiness, implementation or hosted durability is claimed.
