# Epiphany local service integration

The existing JVM integration alias now runs against disposable native MongoDB,
npm S3rver, and the pinned Transformers.js MiniLM embedding provider in this
sandbox. The final source-bound run passed **22 tests / 108 assertions**, with
no skipped tests. It also performed an actual Mongo command ping and an S3
create/put/get round trip preserving EDN bytes. This exercises the retained service
adapters; the default Clio development path does not acquire a Mongo dependency.

The supervisor is `devtools/epiphany-integration.mjs`, exposed as:

```sh
EPIPHANY_CLIO_SOURCE=/absolute/path/to/verified/eta-mu \
FORESIGHT_MODEL_CACHE=/absolute/path/to/warmed/models \
pnpm --filter @foresight/devtools test:epiphany-integration
```

Use the installed Node/Clojure/JVM toolchain, native `mongod` on PATH (or
`FORESIGHT_MONGOD`), root devtools dependencies and the installed Knoxx backend
Mongo client. No container or alternate language/runtime is involved. The
supervisor allocates its own loopback ports and data directory, runs every service
and caller under the same supervisor, and stops its children and removes its data
on completion. `FORESIGHT_VERIFICATION_OUTPUT` selects the local diagnostic output
directory; otherwise it uses `.cache/verification/epiphany-integration`.

Epiphany reads explicit test settings for Mongo URI, S3 endpoint, embedding URL,
model, dimensions and immutable artifact digest. Its original Ollama/nomic/768
embedding defaults remain available when no override is selected. This run used
real 384-dimensional `Xenova/all-MiniLM-L6-v2` embeddings at revision
`751bff37182d3f1213fa05d7196b954e230abad9`; no vectors were padded or fabricated.
The quantized artifact SHA-256 is
`afdb6f1a0e45b715d0bb9b11772f032c399babd23bfc31fed1c170afc848bdb1`.

The run records Epiphany's Git head/tree, a digest of all nonignored source files
and symlink targets, and its clean/dirty state before and after testing. The exact
tested application source is `d06a08b88e940204ce4575fee8a34837eaf0a2a0`, tree
`0eac2c8d734913ecb973b996478e006638da706b`, with independently fetched Clio
`690aad83ff54ef5225a1f1533b4a7bd0eaef3561`. This is evidence for that immutable
kernel; a later Clio source revision requires its own consumer verification.

Obstacles and their resolutions:

- One Mongo law fixture contained a hardcoded connection while another silently
  skipped absent configuration. Both now require the dedicated test URI and use
  isolated fixture collections. The readiness regression first failed one of two
  assertions and now passes both; missing required services refuse execution.
- Embedding fixtures hardcoded both the model and its 768-dimensional result.
  Explicit configuration now selects the real provider contract, including its
  expected dimensions and artifact digest. The adapter still executes real HTTP.
- Startup attempts 01 and 02 reused a Mongo client whose initial connection raced
  the server. Its topology stayed closed even after the server became ready.
  Closing and recreating the client for each bounded readiness attempt fixed the
  problem. Attempt 02 retained the owned Mongo log, confirming normal startup and
  shutdown; attempt 01 retained its assertion output but had already removed its
  ephemeral server log during cleanup.
- Attempt 03 passed the actual service suite while fixture changes were still
  uncommitted. It is an intermediate result, not the final Git-bound proof.
- Attempt 04 exposed a source-digest bug: a directory symlink was read as a file.
  Hashing the symlink's target text fixed it. Attempt 05 passed on clean source,
  with matching before/after source digests and successful cleanup.
- Separately, actual Codex review found that the old observation reference could
  silently keep earlier content on direct retries. Prospective Clio writes now
  validate and compare every observation type before acknowledgment, while
  historical replay retains its original interpretation. The failure-first
  regression reported ten failures / 35 assertions; the corrected focused suite
  passed 17 tests / 103 assertions. This does not claim that legacy Mongo and
  in-memory first-write-wins semantics have also been migrated.

[Source and execution metadata](evidence/epiphany-local-services-2026-09-12.json)
records the final integration result. Owned local server diagnostics are retained
locally; credentials or broader environment logs are not included in this report.
