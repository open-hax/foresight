<!-- SPDX-License-Identifier: GPL-3.0-or-later -->
# Colab Gemma: 24-case baseline and bounded pilot

Observed on 2026-10-25. The finite research cycle works; its candidate remains
isolated and is not eligible for production. Two actual Colab CPU runs evaluated
the same fixed grid and released their owned runtimes. Stealth and Yoga agreed
on every top-ten source list and every gold rank in the selected scorer replay.
Native Yoga embedding inference and full E4B behavior do not have a passing gate.

The operator explicitly authorized this pilot and a total 30-compute-unit ceiling
in chat `01a1722a-6907-7ff3-b756-eb75ea2d91aa`. This report does not authorize
production adoption or treat research implementation as retroactive planning review.

## Baseline and selected retrieval configuration

The frozen corpus has 5,917 vectors: 4,638 text/image and 1,279 calibrated audio.
Twenty-four analyst-labeled queries retain stable source IDs, content hashes,
paths and evidence anchors, frozen before retrieval scores were inspected.
The split is eight development, eight validation and eight locked-test cases.
Each category has six cases. Relevance labels remain provisional.

| Search configuration | Top-five source hits | Top-ten source hits | nDCG@10 |
| --- | ---: | ---: | ---: |
| Frozen exact cosine, raw query, 768 dimensions | 19/24 (79.2%) | 19/24 (79.2%) | 0.7471 |
| Selected isolated candidate: raw query, 512 dimensions, lexical fusion | 21/24 (87.5%) | 22/24 (91.7%) | 0.8489 |
| Live native ANN, 200 candidates and 200 returned chunks, then format filter | 13/24 (54.2%) | 13/24 (54.2%) | See per-case native artifact |
| Live native ANN, 2,000 candidates and 200 returned chunks, then format filter | 13/24 (54.2%) | 13/24 (54.2%) | See per-case native artifact |
| Live native exact search, 200 returned chunks, then format filter | 19/24 (79.2%) | 19/24 (79.2%) | See per-case native artifact |

Native results are controlled read-only Mongo queries over the creative-corpus
and workspace filters, not proof of an actor's full search behavior. The native
ANN probes returned zero chunks for eight queries. Raising `numCandidates` did
not recover them. Native exact search restored the overall 19/24 baseline.
The native index declares 768 dimensions and cosine similarity. Stored vector
norms were approximately one, so a simple cosine-versus-dot-product mismatch is
not established as the cause. These observations identify a search-path problem;
they do not establish its internal cause or prove the embedding model is at fault.

The initial attempt to prefilter by `normalized_format` was rejected because that
field is not indexed as a filter. That failed probe is retained. Later native
queries only prefilter supported source/project fields and postfilter by format;
no search index was edited. MongoDB documents the separate ANN and exact search
controls in its [vector search stage documentation](https://www.mongodb.com/docs/vector-search/query/aggregation-stages/vector-search-stage/).

| Case category | Baseline top-five hits | Candidate top-five hits | What the labels establish |
| --- | ---: | ---: | --- |
| Lyrics | 3/6 | 4/6 | Source-text lookup |
| Lore | 4/6 | 5/6 | Source-text lookup |
| Image | 6/6 | 6/6 | Retrieval of image vectors using visible SVG text |
| Audio locator | 6/6 | 6/6 | Named recording lookup through text metadata |

The audio cases do not establish speech, music, or acoustic understanding. The
image cases are text-rich SVGs, not a general visual-retrieval benchmark.

Eighteen candidates combined three query prefixes, three output dimensions and
two lexical-fusion settings. Validation alone chose raw-query 512-dimensional
normalized vectors with reciprocal-rank lexical fusion weight 0.5. Validation
nDCG@10 improved from 0.6250 to 0.7945 and top-five recall from 5/8 to 6/8.
The selected locked-test nDCG@10 was 0.9539 versus baseline 0.8663; both had 8/8
top-five recall. The locked-test result did not select the candidate. The second
identical Colab cycle was a reproducibility/integration check, not a new adaptive
candidate selection or eight additional independent test cases.

One development lyrics source regressed from rank one to rank five. Two lyrics
sources remain well outside the top ten. Source-level improvement does not mean
there are no regressions. The small validation set, provisional labels and shared
fictional setting leave considerable uncertainty about generalization.

## Real E4B RAG probes

Both machines have `gemma4:e4b`, Q4_K_M digest
`c6eb396dbd5992bbe3f5cdb947e8bbc0ee413d7c17e2beaae69f5d569cf982eb`.
Two development/validation questions used actual top-five retrieved excerpts in
baseline and candidate conditions. Both conditions requested the same JSON shape,
zero temperature, seed 123 and thinking disabled. No locked-test question was used.
No tool or posting action was available to the model.

Initial combined cold-load/request attempts all reached the 20-second deadline
without visible text. Separate empty-prompt loads then completed in 22.73 seconds
on Stealth and 16.62 seconds on Yoga. The following rows describe warm attempts.

| Host and condition | Complete cited JSON within 20 seconds | Correct labeled source | Exact source quote |
| --- | ---: | ---: | ---: |
| Stealth, baseline retrieval | 2/2 | 0/2 | 2/2 |
| Stealth, candidate retrieval | 2/2 | 2/2 | 1/2 |
| Yoga, baseline retrieval | 0/2 | Unscored timeout | Unscored timeout |
| Yoga, candidate retrieval | 0/2 | Unscored timeout | Unscored timeout |

Stealth completed warm probes in 4.25–6.15 seconds. Its baseline answers quoted
real sources that did not satisfy the specific requested source; source quotation
alone is not sufficient relevance evidence. Candidate retrieval corrected both
source identities. One answer shortened/reformatted a quotation with an ellipsis,
so the strict exact-substring verifier correctly rejected it. These two cases
are too few for a broad answer-quality claim.

Yoga produced first visible text after 11.94–19.71 seconds, but no complete cited
JSON within the deadline. Partial output is retained, and quality is unscored for
those failures. Cold loading, prompt ingestion and complete-output latency must
remain separate. These tests leave the E4B workflow held; they do not establish
that training, a particular quantization, or another model is the remedy.

The separate existing 96-sample paired E4B/31B study remains unchanged in
`/home/err/Documents/Codex/2026-10-25-proxx-cost-performance/evaluation-report.md`.
Its read-only tool calls were not executed; it does not replace these runtime
checks or establish production actor behavior.

## Spending guard and actual cloud evidence

The campaign starts at a displayed balance of 399.54 Colab compute units. After
both owned CPU runs the displayed balance was still 399.54, the rate was zero,
and there were no active assignments. The CLI displays two decimal places, so
this is not precise zero-cost proof. Four units have been conservatively and
permanently charged as reservations against the 30-unit authorization. Twenty-six
remain in the campaign accounting, including the two-unit cleanup/billing headroom.

The pure CLJC law fixes the cap at 30, reserves two units before every allocation,
keeps two additional units unavailable for new jobs, and denies stale/invalid
telemetry, high rate, low balance, or unverified cleanup. A persistent private
state file and exclusive lock prevent normal restarts/concurrent invocations
from resetting or double-spending this authorization. The runner is CPU-only,
with a one-unit/hour rate ceiling, a ten-minute job deadline, a ten-second watcher
and bounded provider calls. Uncertain cleanup blocks another allocation.
This application guard is not an atomic Google billing-side quota; provider
billing and shutdown are asynchronous. The conservative reservations provide
headroom rather than a claim of exact instantaneous vendor enforcement.

Completed owned jobs:

- `research-b4cb05578e3a`: real candidate output downloaded, hash checked, owned
  endpoint absent after explicit stop; local replay independently exercised.
- `research-f51b6a967496`: full automatic cycle including download, cleanup,
  Stealth replay, Yoga transfer/replay, identity comparison and holdback passed.

Only creative vectors, selected case records, hashed lexical tokens and fixed
scientific code were uploaded to the user's Colab runtime. Hashing tokens is not
anonymization. Raw conversation archives, full corpus plaintext and Google
credentials were not uploaded. Google authentication remains isolated on Stealth.

## Artifacts, validation and remaining gates

Source and commands: `docs/research/colab-gemma-pilot/README.md`.
Current pilot projection: `docs/research/colab-gemma-pilot-proposal.edn`.
The source harness is a prepared research artifact, not a merged/review-approved
production component or a recurring scheduler. It is idle after a finite cycle.

Private evidence root:
`/home/err/.local/share/promethean/research/colab-gemma-20261025/`.

- `cases-24.json`: fixed labels, SHA-256
  `26e865c3ad83d20e0d9d3c480bfbfe91448c6bdb1ed7b755f395d10c7bacf065`.
- `corpus-snapshot.jsonl`: original export, SHA-256
  `a68eed7a9f139456c84976db1e1dc4e1001dbb66c87d29a9f911a4add14b1bf1`.
- `baseline-v1.json`, `native-ann.json`, `native-ann-postfilter.json`,
  `native-ann-2000.json`, `native-enn.json`: distinct baseline and native probes.
- `campaign-state.json`: durable reservations, job IDs, usage and cleanup facts.
- `jobs/research-f51b6a967496/`: candidate matrix, both local replays,
  `qualification.json` and `qualification-with-rag.json`.
- `rag-*.jsonl` and warm-up JSON: cold and warm attempts, raw/partial output,
  source validation and timing retained separately.

The latest law suite passes 3 tests and 19 assertions; the adapter suite passes
7 tests; the scorer's parent-deduplication/modality fixture passes. Actual provider
and SSH evidence is separate from these fixtures. Admission boolean checks were
further tightened after the real cycles and verified locally without another
allocation. No model weights, production index, actor contract, backend service
or deployment were changed.

Scorer replay passes on both machines using identical inputs and evaluator code.
Yoga used frozen Stealth-generated query vectors; no native Yoga EmbeddingGemma
service was available. The real E4B Yoga latency check and Stealth quote check
remain unsuccessful. A wider independently adjudicated dataset, native embedding
path evidence, actual actor/tool behavior, review and authorized adoption are
still required for production. No candidate was promoted.

The next research experiment should isolate the eight ANN-empty queries against
native exact search and retain the source/filter/index evidence. Repairing that
path can be evaluated independently from any embedding-model training proposal.
