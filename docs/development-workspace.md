# Reproducible local workspace

Foresight has a shared pnpm development group and generated Clojure/NBB/Shadow
manifests. The group currently contains the root tooling, `devtools`, and the
canonical `eta-mu/packages/clio` package. The independently owned children retain
their declared package managers, versions, lockfiles, working directories, and
quality gates.

## Generate and verify

Use the declared Node 24/JDK 21 toolchain and pnpm 10.14.0. The repository ships
its runtime bundle builder under `tools/chat-work-runtime/`. In the current
sandbox, source the recovered `../activate.sh` before each command so the
session-specific proxy, trusted CA store, shared caches, and native tool paths
are correct.

```sh
nbb scripts/manifests.cljs --write
pnpm install --frozen-lockfile --offline --ignore-scripts
pnpm manifests:check
pnpm build
pnpm test
pnpm lint
pnpm test:s3
```

The offline install requires an already populated shared pnpm store. Omit
`--offline` on a first hydration. `--ignore-scripts` deliberately avoids blanket
execution of dependency lifecycle hooks; the current root's prebuilt filesystem
lock binding and S3 implementation are exercised by real tests. A dependency
that requires a build remains an explicit setup failure until its needed build
is performed and verified.

`workspace.edn` is the manual composition policy. `scripts/manifests.cljs` reads
tracked child manifests and emits:

- `package.json` and `pnpm-workspace.yaml` for the selected development group;
- `deps.edn` with a canonical Clio alias and separate child library aliases;
- `nbb.edn` using Clio's declared NBB dependencies;
- `shadow-cljs.edn` for the actual compiled Clio consumer and its tests;
- `workspace-manifests.edn` with input digests, package identities, selected
  dependency declarations, overrides, and duplicate package names.

Do not hand-edit these six outputs. Regenerate after changing the policy,
generator, project model, or a child manifest. `--check` compares exact generated
content and fails on drift. A selected dependency version conflict fails unless
`workspace.edn` supplies an explicit version and explanation. The generator
rejects a child whose Git ownership is missing or inherited from its parent.

The current inventory includes 88 package manifests, including `devtools`.
Duplicate `opencode` and `@open-hax/uxx` names are reported; they are not silently
collapsed. Bun catalogs, React peer relationships, native build requirements,
and compiler version differences remain owned by the children. This is not an
87-package dependency-policy migration.

The pnpm store is shared and `.npmrc` requests hardlinks. There is no second
Maven repository or separate per-project copy of shared Node package contents.
A project still needs its own dependency links and build artifacts. The generated
Clojure aliases compose a child's library root; they do not reinterpret its test
or build aliases. Run those commands in the owning child.

## Child actions and evidence

```sh
pnpm inventory
pnpm report
pnpm modules:build --only uxx
pnpm modules:test --only uxx
pnpm gates:list --only katamorph
pnpm gates:run --only katamorph
```

Executable workspace actions require `--only` or `--all`. If a child has no
unambiguous package manager or exact root script, that action remains unavailable
(exit 3); do not turn it into a pass. `config/quality-gates.edn` is the reviewed
mapping for non-Node and multi-surface repositories. Its workflow-only and
external gates remain distinct from locally executable gates. `.agents` and
`eta` remain inventory-only consolidation inputs.

## Local storage tests

`pnpm build` produces `dist/local.cjs`. Its `ledgerSummary` export reads persisted
schema revisions and validates a full canonical Clio ledger union before returning
an event/stream summary. The test writes a real event, reads it back, and rejects
both a corrupt record and a missing ledger. It exercises the actual native lock
binding. The deprecated standalone event-ledger package is not a dependency.

`pnpm test:s3` starts S3rver on an ephemeral loopback TCP port, uses the real AWS
SDK to create a bucket and put/get an EDN object, then closes the server and
removes the test-owned temporary data. Server and client live in one command,
which works with the sandbox's per-command network namespace. This is a local
S3 protocol fixture, not a production object store or evidence that every
application service already selects it.

Shadow compilation alone can return exit 0 after autorun test failures. The root
therefore compiles without autorun and separately executes the emitted Node test
program. A deliberately failing fixture was observed returning exit 1 through
that path before the corrected fixture passed.

## Source recovery and disk limits

The September 12 sandbox recovery initialized all 14 direct child repositories.
Eleven missing children were fetched at their recorded gitlink revisions; the
active eta-mu and Knoxx work branches and the restored OpenCode pin were preserved.
OpenPlanner is an additional checkout supplying Knoxx's declared SDK link.

Calliope's Git objects were only about 14 MB, but checkout automatically fetched
Git LFS audio/artwork and materialized several gigabytes. The first checkout
exceeded 120 seconds. A second bounded attempt was interrupted after identifying
the media download. Its stale index lock was retained as evidence, then source
checkout completed with `GIT_LFS_SKIP_SMUDGE=1`. The source revision is complete;
LFS media hydration is partial and must be explicitly requested when a task needs
it. No already downloaded media/cache was force-deleted.

Recovering sources and passing these root gates does not establish a green result
for every child suite, an available remote identity provider, or an accepted
publishing flow. Each child and each live service needs its own current evidence.
The sandbox evidence files retain time bounds, command arguments, complete output,
and actual exits; historical runs are kept separate from current recovery runs.

## Small local embeddings

The shared tools include `@huggingface/transformers` 3.8.1 and an offline CPU
embedding server. It uses `Xenova/all-MiniLM-L6-v2` at revision
`751bff37182d3f1213fa05d7196b954e230abad9`, q8 weights, and two intra-operation
threads. It returns 384-dimensional normalized embeddings through the OpenAI
`/v1/embeddings` shape and an Ollama-compatible `/api/embed` endpoint.

```sh
# The sandbox recovery already populated this cache with the pinned model.
export FORESIGHT_MODEL_CACHE=/workspace/scratch/3655842e43cf/model-cache
pnpm test:embeddings
FORESIGHT_EMBEDDING_PORT=11434 pnpm model:embedding
```

`FORESIGHT_MODEL_CACHE` names the populated cache; the default is `.cache/models`
under the repository. The server does not
download weights or silently select a different model. A missing model is a
startup failure. The model test is explicit rather than part of the normal root
unit suite because its cached weights are a separate capability.

The smoke test executes real inference and checks finite normalized vectors,
a small semantic similarity ordering, base64 encoding, and rejection of invalid
requests. These observations establish protocol/runtime readiness, not quality
on the project's full research or writing workload. The browser supervisor may
import `startEmbeddingServer` from `devtools/embedding-server.mjs`; it returns a
local `baseUrl`, the HTTP server, model metadata, and an asynchronous `close`.
