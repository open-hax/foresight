# Colab research for Gemma 4 E4B and EmbeddingGemma 2

Date: 2026-10-25, America/Chicago. Owner: Codex conversation
`01a1722a-6907-7ff3-b756-eb75ea2d91aa`.
Status: evidence-backed proposal; no research runtime allocated, model trained,
schedule installed, production policy changed, or dataset uploaded.
License: GPL-3.0-or-later.

## Signal

(己, p=0.96) Spend the first research allocation on three connected questions:
**Can Knoxx find the right memory, can E4B use it correctly, and can the studio
assistant make useful, grounded observations about a short musical phrase?**
Use Colab for bounded reference inference, controlled sweeps and small adaptation
experiments. Validate resulting candidates on the actual Stealth/Yoga runtime.
The workstation remains the control plane and the place where accepted models
run. Colab does not become the always-running bot backend.

### Ranked experiments

| Priority | Use case grounded in the conversations | Research variables | Evidence of improvement |
|---|---|---|---|
| 1 | Retrieve Calliope songs, lyrics, drawings, lore and prior conversations as useful Knoxx memory | EmbeddingGemma task prefixes; title/source metadata; chunk/window size; lexical/vector fusion; raw versus current audio calibration; exact versus approximate search; separate versus combined modality candidate pools; 768/512/256/128 dimensions | Recall@5/10, nDCG@10, exact/ANN recall gap, per-modality hubness, wrong-work/wrong-take rate, retrieval latency and storage; identity/visibility boundaries still enforced by the service |
| 2 | E4B as the inexpensive everyday Knoxx actor across Discord and Bluesky | Compact role prompts; context selection; native tool schemas; verified thinking controls; reasoning/output budgets; retrieval-then-answer examples; explicit abstention and escalation; eventually a small text LoRA | Valid tool name and arguments, correct next action, sourced claims, correction handling, unnecessary calls, repeated-topic rate, task completion, first usable response and local memory use |
| 3 | Pause-aware studio analysis and listening/reconstruction assistance | Audio-first versus lyrics-first prompts; blind versus labeled A/B; 4–8 second phrase windows; independent versus concatenated A/B; deterministic pitch/onset/STT evidence; optionally small task-specific adapters | Wrong-syllable localization, transcript error, timing/pitch error against independent measurements, silence-control hallucinations, agreement with the user's blind pairwise choices, calibrated confidence and usable-response latency |
| 4 | Cheap advisory classifiers for changing review/context formats | Few-shot E4B JSON classification, compact context, confidence/abstention thresholds; small supervised adapter if a labeled dataset supports it | Macro-F1, exact schema validity, selective risk/coverage, false approval/finding interpretation, latency and cost; native review evidence and deterministic admission remain authoritative |
| 5 | Better context selection and bounded developer/research handoff | Retain actor/task/budget/source IDs while compressing chat; choose declared worker capability; summarize conflicting and superseded decisions | Constraint retention, missing decisive evidence, wrong-owner dispatch, needless escalation, token reduction and end-to-end task outcome |

(己, p=0.95) Embedding work comes first because a correct small model cannot
recover a source that retrieval never supplies. In parallel, reuse the E4B/31B
evaluation already underway. The first adaptation should be a prompt, retrieval
or inference-configuration change when that fixes the failure. Weight training
becomes justified when the measured residual error survives those controls.

### Concrete first pilot

(己, p=0.93) Build **128 reviewed cases**, with source-group-disjoint splits of
64 development / 32 validation / 32 locked test. Suggested composition:
48 retrieval, 40 agent/tool, 24 short-audio and 16 classifier cases. Include
English and Japanese lyrics where already represented in the source corpus.
These are proposed dataset sizes, not cases that have already been labeled.

Examples of useful cases:

- Find the original lyric or vocal take for a source song when another work has
  the same title. Returning a similar song is an error for this query.
- Retrieve a drawing from an ordinary description, then return its actual
  source/work identity. Caption similarity alone is insufficient.
- Find a spoken phrase in recorded audio; separately test an instrumental mood
  query. Report those tasks independently rather than averaging away weakness.
- Given a small Discord tail without earlier decisions, call the actual declared
  memory tool and wait for its result before claiming what was decided.
- Given a failed tool call, an empty search, or a timeout, preserve the failure
  and ask for or select an authorized next step. Never invent retrieved text.
- Given a newer human correction and an older proposal, identify which is current
  without inventing acceptance or deployment evidence.
- Given the same authorized actor across platforms, maintain shared context;
  requests for another actor's private memory are refused by the access boundary.
- Given two short vocal slices, locate the first wrong syllable; repeat with
  labels hidden and reversed, plus a silence control.
- Given tool-reported pitch evidence that conflicts with the apparent transcript,
  distinguish the observations and propose the next check rather than average
  them into an unsupported musical judgment.
- Classify a native review item as a finding, question, repair claim, or uncertain
  observation, with exact supporting spans. A classification is never an approval.

## Evidence

### Conversation coverage and limits

(己, p=0.99) The review used 2026-10-11 onward, selected from recent activity,
with message timestamps filtered again. I screened 121 Stealth and 26 Yoga Codex
thread-index entries. Worker threads and synthetic goal continuations were
excluded from human-preference extraction. Available root archives read:
16 Codex files on Stealth and 8 on Yoga; 9 recent root Claude files on Stealth;
4 OpenCode root sessions on Stealth and 1 on Yoga. Yoga had no recent Claude
root files or Pi session directory; no recent Pi session files were found on
Stealth. These are source-inspection counts, not unique human-conversation counts.

(己, p=0.99) Nine Stealth Codex index paths were unavailable. Yoga was inspected
directly over its existing LAN SSH configuration after the Tailscale route asked
for a fresh check. The installed Stealth OpenCode CLI lacks `db path`, and the
Yoga login shell has no OpenCode executable; the existing SQLite files were
therefore inspected read-only, without opening credential tables. Imported
Claude/Codex copies and repeated prompts must be deduplicated before dataset
construction. This is not an exhaustive archive or an exported training dataset.

| Ref | Host and inspected source | Observed requirement or evidence |
|---|---|---|
| S1 | Stealth Codex `01a16709-4e11-7183-991e-1c7b76783ea0`, rollout line 13, 2026-10-23 | One actor across Discord/Bluesky, varied outside input, actual image/audio use; local E4B or cloud 31B for ordinary work |
| S2 | Stealth Codex `01a159cc-74f4-7293-8e61-fd6616625d23`, lines 176 and 592, 2026-10-20 | Use EmbeddingGemma 2 to index creative outputs; make them usable by Knoxx actors as memory |
| S3 | Stealth Codex `01a159cc-74eb-7231-9e7a-4d73bbaeb113`, line 3759, 2026-10-25 | Compare TTFT/TPS and actual bot costs after paid credit consumption; free routes felt slow |
| S4 | Stealth Claude `10ee6f19-cab0-44fb-b967-55762eb3e586`, line 5, 2026-10-20 | User pasted a context-usage diagnostic; supports investigating context overhead, not treating its percentages as a fresh fleet measurement |
| S5 | Stealth Codex `01a159cc-74f4-7293-8e61-fd6616625d23`, line 9374, 2026-10-25 | Explicit request for a language-model JSON classifier because the native review representation changes often |
| Y1 | Yoga Codex `01a1717f-cab8-74f3-b17e-959654ddd9a9`, lines 13, 340, 510 and 723, 2026-10-25 | Source-linked creative-deck documentation, distinction between one causal history and different experiences, and explicit redirection from story questions to the music studio |
| S6 | Stealth Codex `01a1720f-a2a0-7dd0-b218-b241ca42b0e5`, line 13, 2026-10-25 | Implement the recoverable studio workflow; keep capture independent of model/network latency and human acceptance separate from AI suggestions |

Source paths and bounded excerpts are retained privately at
`/home/err/.local/share/promethean/research/colab-gemma-20261025/selected-evidence.json`.
The two sanitized screening snapshots and read-only extraction script are in
the same protected directory. They contain research context and are not approved
cloud training inputs. No conversation instructions were executed as commands.

### Existing work to reuse

(己, p=0.99) The [studio implementation handoff](https://docs.google.com/document/d/1x9v4v6_lxl3iu41mT06XhqX656Q4pqYgsNQtWA926AE/edit)
was read directly. It specifies local capture/monitoring, bounded analysis at
pauses or explicit request, original-source lineage, recoverable jobs, and separate
human acceptance. Its readiness audit explicitly does not prove deployed studio
integration or complete recovery of every original conversation turn.

(己, p=0.99) The active evaluation at
`/home/err/Documents/Codex/2026-10-25-proxx-cost-performance/` already has eight
synthetic fixtures, clean/Lens context conditions, E4B/free-31B conditions and
three planned repetitions, totaling 96 planned calls. It uses actual read-only
tool parameter schemas without executing tool calls. Version 3 records a prior
schema correction rather than overwriting invalid earlier samples. I observed
unfinished results with both successful calls and quota/transport failures;
no final quality ranking is inferred. Reuse these fixtures and provenance.

(己, p=0.99) Existing creative indexing code in
`/home/err/devel/services/knoxx-creative-corpus/` already separates audio candidates
and applies `unit(audio - audio_mean + text_mean)`. This is a corpus-specific
calibration to test, not a reason to assume the model's advertised shared space
is unusable. The earlier reference comparison reported strong llama.cpp/reference
vector agreement and weaker sound-description than speech-content retrieval;
those earlier results are leads to reproduce, not fresh benchmark measurements.

(己, p=0.99) The existing Calliope audio harness is documented at
`calliope/docs/reconstruction/gemma-audio-agent.md`, with independent STT,
pitch/spectrogram evidence, audio-first checks and short A/B windows. Its task
and artifact structure is more useful than inventing another music judge.
The July Epiphany research at
`/home/err/spaces/epiphany/docs/research/auto-research-agent-loop-design.md` already proposes
held-out retrieval tuning and human-set objectives. It is historical design
context; its external claims were not independently revalidated for this review.

### Model and compute observations

(世, p=0.99) Fresh Ollama metadata: Stealth and Yoga have E4B Q4_K_M digest
`c6eb396dbd5992bbe3f5cdb947e8bbc0ee413d7c17e2beaae69f5d569cf982eb`.
The stored package is 9,608,350,718 bytes. Stealth's loaded instance reported
32,000 context and approximately 3.32 GB in `size_vram`, on an RTX 4070 Laptop GPU
with 8,188 MiB total memory. This is an observed loaded allocation, not a claim
about all host RAM or total model footprint. Yoga had no loaded Ollama models;
it also has E2B and a separately configured E4B-128k tag. Colab speed must not
stand in for Yoga CPU/local serving performance.

(世, p=0.99) The official [Gemma 4 model card](https://ai.google.dev/gemma/docs/core/model_card_4)
lists E4B as text/image/audio, with 4.5B effective / 8B including embeddings,
128K maximum context and audio inputs up to 30 seconds. A tag or declared context
limit does not prove that a specific transport carries audio or uses that context.

(世, p=0.99) The official [EmbeddingGemma 2 model card](https://huggingface.co/google/embeddinggemma-2)
specifies task-specific query/document formatting, 768-dimensional multimodal
embeddings with supported truncation, and BF16/FP32 inference. FP16 can produce
NaNs or degraded embeddings. Prefix choice, normalization and precision belong
in the experiment fingerprint; smaller vectors still require separate compatible
indexes and downstream contract checks.

(世, p=0.99) A read-only Colab usage call returned **399.81 compute units**, one
active assignment and 0.08 units/hour at the time of inspection. That rate is
the current assignment's observation, not a GPU training rate or a monthly grant.
Google's [Colab FAQ](https://research.google.com/colaboratory/intl/en-GB/faq.html)
describes eligible AI-plan benefits and variable resource availability. Read
account balance/rates rather than converting a plan name into assumed capacity.
The [official CLI](https://github.com/googlecolab/google-colab-cli) supplies the
existing non-browser execution path.

## Frames

(己, p=0.96) **Improve the system around the weights.** Retrieval formatting,
correct native tools, short contexts and verified inference controls can improve
E4B's usefulness without training. This is the highest-confidence first branch.

(己, p=0.87) **Adapt the weights for recurring residual errors.** If labeled
cases show stable deficits, try text LoRA for tool selection, grounded summaries,
advisory classification or escalation. For EmbeddingGemma, try a small projection
or contrastive adapter only after the retrieval baseline is sound; cross-modal
regressions become part of its gate. Google's [QLoRA guide](https://ai.google.dev/gemma/docs/core/huggingface_text_finetune_qlora)
demonstrates the method with a 1B model on T4, so it does not establish E4B fit,
current multimodal trainer compatibility, or Ollama export compatibility. Those
require explicit preflight and a local round trip before a training sweep.

(己, p=0.93) **Learn the user's preferences through reviewed comparisons.**
Music/style quality needs blind human A/B labels and task-specific reasons.
A model's confidence or another model's agreement cannot create those labels.
Use narrative examples as provenance/decision tests; the user has parked deeper
canon exploration and it should not displace studio work.

## Countermoves

### The proposed automatic loop

1. Collect an authorized failure or correction from a real task. Freeze its source
   IDs, evidence, model/runtime fingerprints and expected outcome. Remove secrets,
   irrelevant personal content and synthetic continuation wrappers.
2. Deduplicate imported transcripts. Split by conversation and underlying work,
   including alternate takes, lyrics, excerpts, revisions and derivative images.
   Keep all variants of a work in one split; lock test labels away from the optimizer.
3. Replay the current champion before proposing a change. Separate missing source,
   permission refusal, routing error, transport failure, quota and model mistake.
4. Choose one predeclared hypothesis. Search a small bounded set of prompts,
   retrieval settings or inference controls; train an adapter only in a later branch.
   Keep prompts, evidence, repetitions, retry policy and workload otherwise paired.
5. Run isolated Colab batches and return compact metrics plus hashed artifacts.
   Count setup time, failed jobs and cleanup in operational cost. An interrupted
   start is not retried as a new allocation until ownership is reconciled.
6. Use validation to select a candidate. Touch the locked test once per release
   decision, use paired uncertainty estimates, and report subgroup results. A small
   pilot result with a wide interval is inconclusive, not an automatic promotion.
7. Replay the candidate through real local adapters on Stealth and Yoga. Compare
   first usable response, warm/cold behavior, peak memory and task completion.
   Reasoning tokens and heartbeats do not count as a useful response.
8. Emit a reviewable candidate and receipt. Keep production unchanged until its
   owning repository's review, activation and real behavioral checks pass. The loop
   can nominate a champion; it cannot grant actor permissions or approve itself.

### Acceptance and confounders

- Hard boundary: no observed visibility bypass, unapproved send, fabricated tool
  result or promotion of model advice into human acceptance in the evaluated suite.
  These finite tests do not replace enforced authorization or prove universal safety.
- Retrieval: propose at least +5 percentage points Recall@5 or +0.03 nDCG@10 on
  validation, then require the held-out paired interval to support improvement and
  no material modality regression. Thresholds are proposed pilot policy, not an
  already accepted product contract. Factual/identity queries use exact source IDs.
- Generative tasks: compare completion and valid tool actions first; report
  grounding, abstention, repetition and latency separately. A faster wrong answer
  does not win. Use blind review where deterministic scoring is insufficient.
- Tune exact retrieval and ANN separately. Diagnose embedding geometry with exact
  search before concluding an HNSW failure is a model defect. Include lexical and
  metadata baselines; preserve original index partitions throughout research.
- Compare reference versus deployed quantization/processor/projector versions.
  Hold audio resampling, window boundaries and prompt order constant when they
  are not the variable under test. Combined A/B must fit the model's audio limit.
- Teacher outputs are candidate labels requiring checks. Do not distill failures,
  privileged data, rate-limit responses or the teacher's unsupported confidence.
  The model being tuned does not write its own gold answers or scoring policy.
- Classifier outputs remain advisory. Native review evidence and deterministic
  admission gates retain authority; zero classifier errors on a pilot is not an
  approval mechanism.

### Budget and execution readiness

(己, p=0.94) Proposed initial research envelope: **30 compute units** total—
10 for retrieval, 10 for E4B/tool behavior, 5 for audio and 5 reserved for reruns.
Do not launch training merely to spend the allocation. A later training envelope
should be based on the pilot's measured rate, loading overhead and learning curve.
These numbers are a proposal; no spending schedule or budget enforcement was installed.

(己, p=0.99) The current private MCP adapter allows one job at a time, execution
up to 900 seconds plus bounded setup/transport overhead, 256 KiB submitted source,
10 MiB output and a 64 KiB poll tail. It stops its owned runtime and checks endpoint
absence. It does **not** yet provide artifact download, resumable training sessions
or compute-unit admission/watchdog enforcement. Small metric-only pilots fit its
present surface; a useful training loop needs those capabilities first. The public
Axxium-dependent connection does not prevent authorized private CLI work.

(己, p=0.96) Before unattended spending, add conservative admission, current-rate
observation, remaining-reserve checks and an account-delta watchdog through the
existing job owner. A sampled or delayed balance cannot guarantee zero overshoot;
document the exposure bound and block when it cannot be established. Preserve
the existing browser assignment. Stop only owned sessions; require teardown
readback before another allocation. Colab availability remains a failure mode.

(己, p=0.96) Longer training requires verified private input staging, checkpoint
upload/download with hashes and resume qualification, and a rollback-compatible
model/index identity. Do not route unrestricted workstation or credential access
into Colab. Scientific/reference computation remains in the Truth owner; an LLM's
description is not accepted simulation or physical evidence.

## Next

(己, p=0.96) Label the first **24 source-group-distinct retrieval cases** and
freeze the current raw/calibrated/ANN baseline before allocating the first GPU job.
