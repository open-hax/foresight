# Local cephalon: operational evidence, 2026-10-06

## Outcome and scope

OpenHax is running as the local Knoxx creative agent and Discord-facing head.
The operator authorized continuous creation and publication to the configured
Bluesky account and home Discord channels. Real lyrics, artwork and music have
been created and delivered. Strong isolation, publication admission and recovery
guarantees are still planned work in [Foresight PR136](https://github.com/open-hax/foresight/pull/136).
This report records observed behavior, not completion of that epic.

The deployment is `/home/err/.local/share/promethean/services/knoxx-social-local`.
Docker Compose owns its backend, frontend, local Atlas vector-search stack and
host-service bridge. Existing unrelated PM2 services were not changed.

## Reproducible read-only inspection

```bash
nbb scripts/verify_cephalon_local.cljs --only knoxx
```

Run on the owning local host. The explicit selector is required. This command
verifies the served image and contract hashes before inspecting authenticated
runtime state and public publication identities. It does not invoke an agent,
publish, mutate contracts or transition cards. Its captured output is
`/home/err/.local/share/promethean/services/knoxx-social-local/verification-cephalon.txt`.
An unavailable dependency or changed image remains a failure.

Observed preparation gates: root workspace tests passed (24 tests, 120 assertions),
`clj-kondo --lint scripts test` passed with zero warnings or errors, and
`git diff --check` passed. These checks do not qualify the Knoxx source hotfix or
establish unattended operation across host sleep.

## Served identities and ownership

| Boundary | Observed identity |
| --- | --- |
| Backend container | `knoxx-social-local-backend-1`, UID1000, healthy |
| Served image | `knoxx-backend:social-local-2644fc6-creative` |
| Image ID | `sha256:ce0123ef17bade99603ea6ea19967c99c62869118d1cf52c057ab6a72500bf14` |
| Contract snapshot source | Knoxx `40221a69f7fff675b46614d9ab318b4ef52f3786` plus explicitly recorded local overlays |
| Local HTTP / UI | loopback ports18881 /18882 |
| Discord bot | `OpenHax#8539`, identity450177073990860801 |
| Actor / model | `discord_automation` / `deepinfra/google/gemma-4-31B-it` |
| Background agent | `ussyverse_social_creative` |
| Conversation agent | `ussyverse_social_replies` |
| Creative cadence | native Knoxx schedule `*/15 * * * *` |
| Additional trigger | `:cephalon/creative-request` |

The image is based on an older Knoxx build; the later contract snapshot is not a
claim that that source revision was compiled. The deployment manifest retains
per-file SHA256 hashes, overlay descriptions and the actual image ID. The local
image adds the missing synthesis script and runtime dependencies.

The cloud Knoxx deployment retains `KNOXX_DISABLE_EVENT_RUNTIMES=true` in its
Compose definition. Only this local deployment currently owns the Discord/event
consumer. Do not enable both consumers with the same bot identity. Cloud
availability and mesh failover remain unverified.

## Native artifacts and independently observed deliveries

| Activity | Evidence |
| --- | --- |
| Lyrics cycle | completed run `bfb2a6b7-f74a-4cea-9ab7-b5133ccc1821`; `Music/cephalon/lyrics.txt` |
| Lyrics publication | [Bluesky post3mxa2prshye2y](https://bsky.app/profile/open-hax.bsky.social/post/3mxa2prshye2y), independently read through the public API |
| Arbitrary-event maker | completed run `trigger-ussyverse-social_creative-cron-evt_1791311878704-1791311879169` |
| Artwork | original SVG at `Graphics/cephalon/20261006T183814Z/cover.svg`,943bytes, plus receipt |
| Artwork publication | [Bluesky post3mxa3bxrkja2y](https://bsky.app/profile/open-hax.bsky.social/post/3mxa3bxrkja2y), public API observes one image |
| Music cycle | completed run `8602d515-5403-41ef-9c8e-5a3b862eb0f7` |
| Music project | `Music/cephalon/20261006184744Z/spec.json`, actual numeric12-second synthesis spec, lyrics and receipt |
| Music artifact | `Music/cephalon/20261006184744Z/final.wav`,2,116,844bytes, stereo44.1kHz,12.000seconds |
| Music delivery | [Discord message1557102511633076364](https://discord.com/channels/1444142672548986994/1494137016303095828/1557102511633076364), independently retrieved using the configured bot credential; one matching `audio/wav` attachment |

Artifact paths above are relative to the persistent deployment `state/workspace`.
The final WAV was independently measured with ffprobe and ffmpeg: mean volume
-20.3dB, maximum -1.4dB. It contains real audio. The verified12-second project
supersedes an earlier4-second diagnostic clip whose retries did not retain a
reliable final-spec binding.

Detailed native observations are stored under the deployment `state/` as
`cephalon-*-admission-20261006.json`, `cephalon-*-persisted-20261006.json`,
`cephalon-publication-observation-20261006.json`, and
`cephalon-music-discord-observation-20261006.json`. Credentials remain private;
they are not committed or included in this report.

## Responsive head during a working maker

Run `dbb28bda-2023-40c5-ad14-69dbcc4b6f6d` was admitted at18:38:33.204UTC.
The HTTP admission took1298ms. Its native Discord send completed at18:38:38.781UTC
(about5.6seconds after admission), message1557099786027663361:

> I'm available to chat while my maker keeps working. 💅

The existing maker run continued with the same identity, published at18:38:59UTC,
and completed at18:39:18UTC. The conversation probe did not abort or steer it.
This was an explicitly authorized direct HTTP probe. It was not a fabricated
human Discord message, and it bypassed event FIFO admission. It demonstrates
independent HTTP conversation while a maker runs; it does **not** prove reserved
Discord reply capacity under a saturated queue.

A real operator Discord message, `Lol`, also arrived while the12-second music
maker was running. The gateway event was admitted at18:47:54.390UTC as
`trigger-ussyverse_social-replies-1494137016303095828-evt_1791312474355-1791312474377`.
Its native send returned message1557102136226087014 with timestamp18:47:58.835UTC,
about4.5seconds after admission. The reply run completed at18:48:02.466UTC;
the music maker retained its identity and finished at18:49:41.406UTC. This
observes the actual Discord event path with one maker active, while leaving the
saturation law unproven.

## Known failures and current fallbacks

1. Event-triggered replies and makers still share a FIFO. Concurrency2 supplies
   two shared slots, not a reserved conversation lane. Planned law tests must
   hold a maker and saturate its lane while admitting and completing a reply.
2. Publication cadence is prompt guidance. The real artwork post occurred only
   about10minutes after the lyrics post, violating the proposed30-minute
   guidance. Enforced reservation, deduplication and frequency admission are
   required source work, not a proven configuration property.
3. Native `music.generate` renders a WAV, then erroneously JSON-parses the
   promisified `execFile` result object instead of its stdout. The maker now has
   guidance to use the **same packaged engine** through native `bash`, retain
   the actual spec and verify the resulting WAV. A narrow source/packaging
   regression fix is in an isolated worktree with tests first.
4. The configured image provider returned403 `provider_not_allowed`. Original
   SVG creation and native Bluesky rasterization succeeded. This is a functional
   creative fallback, not a claim that provider image generation is working.
5. The owning host can sleep. Three unattended clock cycles, supervised restart,
   cloud placement and mesh ownership recovery have not yet been demonstrated.
6. The artwork post had empty alt text. The current maker prompt now requires
   `imageAlts`; accessibility enforcement remains part of publication work.

The foreground contract has four allowed tools: Discord send/read/react and
asynchronous `agents.spawn`. It disables thinking and passive-memory delays,
and uses a bounded24-message context. The maker retains creation, publication
and delegation tools. Conversation does not explicitly cancel or reset it.

## Review and continuation

PR136 contains an incoming epic and four UUID-linked stories. All three initial
CodeRabbit planning findings have verified fixes and explanatory settlements.
The successor head still needs its own review qualification. CodeRabbit reported
an hourly cooldown through19:30:28UTC on2026-10-06; Codex reported account review
quota. Neither condition supplies approval. Cards have not been moved to ready.

The active `cephalon-runtime-follow-up` heartbeat continues operational inspection,
review settlement and the authorized implementation flow every30minutes, staying
quiet when nothing actionable changes. **Knoxx's own clock creates the art**;
the heartbeat is maintenance and continuation, not the creative scheduler.

### Unattended-cycle observations

The last contract reload armed the900000ms interval at18:47:39.383UTC.
The first natural tick admitted run
`trigger-ussyverse-social_creative-cron-creative-evt_1791313360070-1791313360119`
at19:02:40.141UTC. Its persisted event type is
`schedule/ussyverse-social-creative` and schedule ID is `creative`.
This run is observed running; its completion and artifact remain pending.
Reloading a contract rearms the current native interval, so repeated prompt
edits would postpone this proof. No manually dispatched cycle is counted as
unattended-clock evidence.
