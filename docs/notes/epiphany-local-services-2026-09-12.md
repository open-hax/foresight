# Epiphany local service integration

The existing JVM integration alias now runs against disposable native MongoDB,
npm S3rver, and the pinned Transformers.js MiniLM embedding provider in this
sandbox. The original source-bound run passed **22 tests / 108 assertions**, with
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
`FORESIGHT_MONGOD`) and the root devtools dependencies, including its own
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
preserves the original integration result as historical evidence. Owned local server diagnostics are retained
locally; credentials or broader environment logs are not included in this report.


## Supervisor evidence revision

The review repair waits for the integration child's `close` event, including
closed stdout/stderr, before saving or checking its transcript. A real child
fixture reproduced a missing summary when its descendant wrote through an
inherited pipe after `exit`; another fixture confirms a trailing failure cannot
hide behind an earlier successful summary. Three actual failed supervisor runs
also reproduced stale `result.json` files before the repair.

Each invocation now removes the previous public success, allocates a unique
`run-*` directory and random `run_id`, and launches the service supervisor as a
child using the same script's `--worker` entry. The worker attempts all independent
service closers and records any failures. It writes its exclusive candidate only
after cleanup has succeeded. The parent waits for actual worker closure and
requires exit zero, the current nonce, successful cleanup and integration, and
unchanged supervisor bytes before atomically publishing `result.json`.
A missing candidate, old candidate, failed cleanup, nonzero exit, interruption or
signal therefore cannot publish success. Abandoned run diagnostics remain
available in their original directories; they are never substituted for the
current candidate.

Peer review also found that a direct child can exit while a descendant retains
its output pipe indefinitely. Shutdown now allows a bounded drain, then closes
only the supervisor's own pipe handles and records a failure. A timed-out run
cannot succeed merely because the direct child had exit zero. The regression
uses actual inherited pipes; the earlier run01 source was reconstructed and
SHA-256 checked against its recorded result before demonstrating the failure.
This bounds the observation failure; it does not claim to terminate arbitrary
untracked descendants outside the supervisor's owned direct children.

The generated schema version 2 records the integration command with the stable
`<verified-clio-source>/packages/clio` marker, the exact supervisor command,
`supervisor_sha256`, and the parent-observed `supervisor_exit_code` and signal.
`supervisor_observed: "child-close"` specifies the boundary: these fields describe
the service supervisor child, whose resources have closed, not a prediction of
the still-running evidence publisher's future exit. The historical artifact above
is unchanged. The current output is directly publishable without manually
inserting exit status or redacting the Clio path.

The existing root test gate includes all thirteen native process/source-admission
regressions through `devtools/epiphany-inputs.test.mjs`; the focused command is:

```sh
node --test devtools/epiphany-inputs.test.mjs
```

The final schema-v2 service rerun again passed **22 tests / 108 assertions**,
with zero skipped tests and successful cleanup on clean Epiphany
`c72b41eee96d22915a79487148df453c34627e48` and clean combined Clio
`2b7bfbefa580d512262ca18f9163ecba43e54cc5`. Its
[generated result](evidence/epiphany-supervisor-v2-2026-09-12.json) is copied
byte-for-byte from `result.json`; no publication-time evidence fields were added.
The thirteen focused process regressions and scoped strict JavaScript lint also
passed without skips or warnings. This proves the service supervisor revision;
newer Epiphany or Clio changes still require their own consumer verification.
