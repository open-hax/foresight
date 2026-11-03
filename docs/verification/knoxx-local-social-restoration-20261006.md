# Local Knoxx social restoration

Operational working directory: `/home/err/.local/share/promethean/services/knoxx-social-local`. Relative paths and commands below are relative to that directory.

Activated 2026-10-06 after the operator said “Just make it happen.” Status: local OpenHax consumer active; remote consumer persistently disabled; native agent-generated spoken delivery confirmed.

## Entry points

- UI: http://127.0.0.1:18882
- API: http://127.0.0.1:18881
- Compose project: `knoxx-social-local`
- State: `./state`; contracts: `./contracts` (mounted read-only).
- Mongo policy/session data: isolated Atlas Local `atlas-data` volume, with separate `atlas-config` and `atlas-search` volumes. The former `mongo-data` volume and stopped Mongo7 service are retained for rollback.
- Verification: `./verify.sh`; latest output: `./verification.txt`.
- Local operator email: `system-admin@knoxx-social.local`. The generated password is `KNOXX_BOOTSTRAP_SYSTEM_ADMIN_PASSWORD` in the private, mode600 `.env` file. Do not publish that file.

The backend is the existing local `clio-2644fc6` build plus FFmpeg, Python, curl and Git; the frontend is the matching local build. Application containers run as UID1000. The production filesystem snapshot is preserved separately as `knoxx-backend:production-snapshot-841642ad`, with source-image provenance. Its server loader and package hashes matched production; it is not the currently selected local backend.

## Restored identity and workflow

The historical `discord_automation` credential and the production `open_hax` credential belong to the same OpenHax bot, ID450177073990860801. The bot can read The Ussyverse guild1444142672548986994, including Frankie infinite yap1494137016303095828 and errorcoded slop1444189585373663417. The other running PM2 bot, OpenSkull284539421414391809, is a separate identity.

Historical Discord and Bluesky credentials were read in memory and written through Knoxx's actor credential API into the local policy database. They are not in contracts, the report, or this README. During preparation, the local backend kept `KNOXX_DISABLE_EVENT_RUNTIMES=true`. Activation now sets it to `false`; logs confirm local OpenHax READY, while the recreated remote backend persists `true` and refuses event startup.

The prepared workflow has:

1. A30-minute schedule emitting the social creative event.
2. A trigger selecting `ussyverse_social_creative` as actor `discord_automation`.
3. Mention/keyword replies through `ussyverse_social_replies`, explicitly scoped to the two home channels.
4. Both home channels restored as publication sinks.
5. An explicit `deepinfra/google/gemma-4-31B-it` model resource plus deployment overrides for social creative/replies and their audio/notifier subagents, through the existing Proxx endpoint. The generic Google and Novita routes failed during activation; the DeepInfra route returned READY and completed the native voice/send turn.
6. Voxx reachable on the existing local voice network. A2.35-second speech artifact is saved at `state/workspace/Voice/local-restoration-probe.wav`. It was not sent to Discord or Bluesky.

`contracts-manifest.json` records source revision, every file hash and local overlay rationale. Canonical repository contracts were not changed.

## Verification and limitations

`verify.sh` exercises the running Knoxx validators, actor credential metadata, scoped trigger discovery, data-plane/provider health, declared tools and required creative executables. It creates no agent turn or social message.

- Discord catalog `enabled` flags still consult a retired global token configuration. Actor-owned native tools are registered separately by agent hydration. This UI/catalog defect is reported as WARN; it has not been repaired or waved away as successful Discord execution.
- A native social agent turn and one-consumer cutover are now verified. Two voice attachment messages were delivered to Frankie: one operator bootstrap greeting and one native agent-generated reply. Bluesky posting and live Discord voice-channel participation remain unverified.
- Local Atlas Search/vector search is now verified with a real synthetic nearest-vector query, including after container recreation. This establishes the database capability; migration of the historical memory corpus and complete agent retrieval parity are not claimed.
- Voxx reports STT disabled. Speech synthesis was verified; speech recognition was not.
- GitHub OAuth is not configured for localhost. Local password sign-in was verified by the application API, and the browser displays its sign-in form.
- Some archived legacy agents in the copied corpus still produce event-control audit findings. Their unrelated schedules/triggers were not included in this local deployment.
- The raw agent EDN HTTP route returns500 (`reply.end is not a function`). The authenticated admin contract JSON route succeeds and performs validation.

## Vector search correction and stability evidence (2026-10-06)

The historical local MongoDB deployment **already supports vector search**. The earlier capability gap came from the plain `mongo:7` image selected for this isolated restoration, not from an inherent limitation of local MongoDB.

Read-only inspection of `openplanner-mongodb-1` found MongoDB Atlas Local, Mongo 8.2.7 with mongot 1.65.0. Two 768-dimensional indexes were READY/queryable: `event_chunks__nomic-embed-text__d768__6f5738e62b.vs_embedding` and `graph_node_embeddings.embedding_vector`. Actual `$vectorSearch` calls against stored vectors each returned a result with score 1; no existing document contents or credentials were emitted.

The historical deployment is defined by `/home/err/devel/services/openplanner/docker-compose.yml`, which includes `compose/storage.yml`. That live configuration differs from the separate Community Mongo/mongot arrangement in the recovered OpenPlanner checkout. Its override health check prints an invalid-URI error then succeeds through a fallback primary check. It therefore does not establish search readiness. Its mutable `latest` image also makes deployment revisions harder to reproduce. The vendor runner health check succeeds now; current-container restart count is 0 and OOMKilled is false. These observations do not identify the complete cause of historical instability.

The isolated restoration now uses:

- `mongodb/mongodb-atlas-local@sha256:b3b11da285f28e6a37543d9b42b988c676c7c3b8515c1f7d93f1d0dba566dd66`, pinned to the inspected local image.
- Separate project-owned data, configuration, and search volumes; no host database port.
- The image's runner health check, which checks both Mongo and the search process.
- Search and runner logs sent to Docker, with log rotation at 10 MiB and 3 files.
- An actual query verifier (`verify-vector.sh` / `verify-vector.cjs`) against the isolated synthetic `knoxx_runtime_probe.vector_readiness_v1` fixture. Normal verification is read-only; `VECTOR_PROBE_INIT=true ./verify-vector.sh` initializes that fixture if restoring into fresh volumes. It is not an agent memory record.

The regression verifier failed on plain Mongo7 with `Location6047401`, then passed against Atlas Local. After preparing search, the local backend was paused while the `knoxx` database was dumped/restored. All 55 collections and 1,177 documents matched by canonical BSON content hashes before switching the backend; `migration-verification.json` records counts/hashes only. The private archive `backups/knoxx-before-vector-20261006.archive.gz` is mode 600 and contains sensitive database material; never publish it.

Final preparation verification: **32 PASS, 1 WARN, 0 FAIL**. The remaining warning is the pre-existing Discord catalog flag limitation described above.

The Atlas container was recreated with the same volumes. The vector query still returned the expected nearest vector with score 1, backend and database became healthy, the existing actor membership IDs survived, and local password sign-in returned 200. The old Mongo7 service is stopped and excluded from normal Compose startup through the `rollback` profile. Its volume was not changed to a different database image. `compose.mongo7-rollback.yaml` preserves the previous wiring; rolling back after new writes requires a new consistent data migration rather than assuming the old volume is current.

The historical shared Atlas deployment and its consumers were not restarted. Local event consumers remain disabled, and no Discord or Bluesky message was sent. One recreation demonstrates recovery for this prepared instance; it does not establish long-term stability.

## Activation and rollback procedure

The operator authorized activation. The steps below were applied: the remote consumer is disabled and the local consumer is active. Preserve this one-consumer invariant during future placement changes:

1. Persist `KNOXX_DISABLE_EVENT_RUNTIMES=true` on the remote `knoxx-backend` service through the production Compose deployment, then recreate only that backend. This also pauses the remote event-triggered jobs; HTTP/MCP and credential records remain available. Inspect the applied runtime and logs to establish the remote gateway is absent. A transient event-runtime stop API call alone is insufficient: current source shutdown does not prove it disconnects all actor gateway managers, and restart would reconnect.
2. Change only the local `.env` setting to `KNOXX_DISABLE_EVENT_RUNTIMES=false`.
3. Recreate only local backend using `docker compose -p knoxx-social-local -f compose.yaml up -d --no-deps --force-recreate backend`.
4. Confirm exactly one OpenHax gateway READY, intended bot/guild, scoped triggers, actor-owned credential binding, and provider resolution. Observe natural channel activity; do not send a separate test message without authorization.
5. If activation fails, disable/recreate the local backend before restoring the remote event runtime. Never enable both.

Run `./verify.sh --active` for the current instance. Running `./verify.sh` without the flag retains the disabled-events preparation gate and intentionally fails against the active consumer.

## Spoken activation evidence (2026-10-06)

- Remote `/srv/open-hax/services/knoxx/compose.yaml` now persists `KNOXX_DISABLE_EVENT_RUNTIMES=true`; only its backend was recreated. A private rollback copy remains beside it. The new remote process logs the disabled-events banner and refuses event startup.
- Local `.env` persists `KNOXX_DISABLE_EVENT_RUNTIMES=false`. The gateway reports OpenHax#8539 in3guilds, bound to `discord_automation`. The scoped reply trigger and30-minute creative schedule are active.
- Bootstrap clip:11.88seconds,47,756bytes, written through the local Knoxx TTS HTTP bridge. Its text was authored by Codex for this authorized greeting. It was delivered using the local actor-owned bot credential after the connected MCP app reported a different actor with no credential. Message1557091278272069803.
- Native follow-up: run `b107ea78-3efb-45b5-b454-513637642832` completed with DeepInfra Gemma4 31B. Persisted receipts show native `voice_tts` and `discord_send` execution. TTS ran twice with identical text/path, and exactly one send occurred. Discord GET independently confirmed the message author450177073990860801 and its32,300byte MP3 attachment. Message1557093356427808780, duration8.016seconds.
- Native audio: `state/workspace/Voice/openhax-own-voice-20261006.mp3`; transcript has the same basename with`.txt`. Run and tool evidence are in `state/own-voice-run-persisted-20261006.json`. Delivery links: https://discord.com/channels/1444142672548986994/1494137016303095828/1557093356427808780 and https://discord.com/channels/1444142672548986994/1494137016303095828/1557091278272069803.
- Embeddings now use the existing local Ollama `qwen3-embedding:0.6b`, with verified1024-dimensional output. A non-root Caddy adapter binds only the Docker bridge IP172.17.0.1:11435 to the existing loopback Ollama. Port11436 similarly exposes the existing OpenPlanner record-label authority to the local containers. Its credential remains in private `.env`. Application/label code and the shared services were not changed or restarted.
- During the native turn, absent OpenPlanner REST configuration caused label lookup to fail closed and hide channel messages. The now-configured record-label endpoint returned200/valid labels; its policy was preserved. The existing SDK Mongo data plane remains the isolated local database.
- Final current verification: **36 PASS, 1 WARN, 0 FAIL** via `./verify.sh --active`. It includes live event state, credential metadata, local embeddings, label lookup, creative tools, Voxx and an actual vector query. The warning still identifies the retired global-token field used by the Discord tool catalog; actual actor-owned send is independently verified.

This activation did not post to Bluesky, join a voice channel, or change the separate OpenSkull PM2 bot. It establishes spoken attachments and an active event runtime, not two-way speech recognition; Voxx STT remains disabled.

License: GPL-3.0-or-later.
