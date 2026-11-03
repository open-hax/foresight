<!-- SPDX-License-Identifier: GPL-3.0-or-later -->
# Colab Gemma retrieval pilot

This is an isolated research harness, exercised against real Colab CPU runtimes
and replayed on Stealth and Yoga. It is not an adopted Knoxx runtime component.
The campaign is authorized by the operator's 2026-10-25 request for a 24-case
baseline, a 30-compute-unit ceiling, and local validation before production.

One invocation evaluates a finite grid of 18 candidates, selects by validation
nDCG@10, evaluates the selected candidate on the locked test, downloads results,
releases its own Colab runtime, replays on both machines, and writes a holdback
qualification. Its deterministic optimizer has no test-set score in its selection
key. Repeating this fixed experiment is a reproducibility check; it does not make
an eight-case test set into fresh evidence for new adaptive experiments.

The pure admission and promotion laws live in `budget_law.cljc`. Babashka hosts
the law; Python is the foreign CLI/file/SSH and scientific NumPy adapter. The
runner owns ephemeral sessions named `knoxx-colab-research-*`; it does not become
a second production Knoxx/Sol job service.

## Reproduce on Stealth

Requirements: existing isolated Colab ADC login, pinned official Colab CLI 0.7.5,
Babashka, Python with NumPy, and the existing `yoga` SSH alias. Google credentials
remain on Stealth. Runtime proxy state and logs stay in its private research folder.

```bash
bb /home/err/spaces/foresight/docs/research/colab-gemma-pilot/law_test.clj
python3 /home/err/spaces/foresight/docs/research/colab-gemma-pilot/adapter_test.py
python3 /home/err/spaces/foresight/docs/research/colab-gemma-pilot/evaluate_test.py
python3 /home/err/spaces/foresight/docs/research/colab-gemma-pilot/run_colab.py
```

Each invocation is one CPU research cycle against the same persistent campaign
budget. It is idle after the cycle completes. The operator has not requested a
particular recurring schedule. Read `campaign-state.json` before another cycle;
never delete it, rename the campaign, or create another initial balance to
replenish this authorization. Fixtures and the frozen corpus are at:

`/home/err/.local/share/promethean/research/colab-gemma-20261025/`

The source-linked labels are `cases-24.json`, fixed before inspecting scores.
Their immutable SHA-256 is
`26e865c3ad83d20e0d9d3c480bfbfe91448c6bdb1ed7b755f395d10c7bacf065`.
The original export is `corpus-snapshot.jsonl`, SHA-256
`a68eed7a9f139456c84976db1e1dc4e1001dbb66c87d29a9f911a4add14b1bf1`.
It includes 4,638 text/image vectors and 1,279 calibrated audio vectors. The
reported pilot routes six image cases to image vectors and the other 18 cases
to text vectors. Native audio vectors are preserved but not evaluated by these
metadata locator cases.

## Budget and recovery

- Fixed campaign ceiling: 30 units; every allocation permanently reserves and
  charges two units, with another two held back for billing delay and cleanup.
  Reservations are never refunded merely because displayed usage is rounded.
- No GPU/TPU admission exists in this pilot. CPU usage above one unit/hour,
  unavailable usage telemetry, balance loss reaching 28 units, or the ten-minute
  job deadline triggers cancellation and explicit teardown. The watcher checks
  at ten-second intervals; telemetry commands have a 20-second timeout.
- Account balance decreases are charged conservatively, including unrelated
  account consumption. The campaign state persists across restarts; an exclusive
  process lock prevents concurrent allocations.
- CLI stop success is insufficient. Cleanup requires the exact owned endpoint
  to be absent from a successful official SDK session-list response. Authentication
  errors cannot become empty-list proof. Uncertain cleanup blocks another job.
- On restart, reconcile an unfinished owned session before admission. Unknown
  allocation identity remains blocked for operator investigation; never stop
  another notebook/runtime to clear the block.
- This is an application spending guard, not a Google billing-side atomic quota.
  Allocation, polling, shutdown, and delayed vendor billing are not instantaneous.
  The conservative reservations and CPU-only/rate/deadline restrictions provide
  headroom; do not describe the displayed balance as precise per-job billing.

## Experimental boundaries

The 24 cases have four categories of six and an 8/8/8 development/validation/test
split. Source identities are distinct and source hashes/anchors are retained.
Lyrics and lore have analyst-verified source evidence; image labels use visible
SVG text; audio labels only name files. These are provisional relevance labels,
not blinded human judgements. Shared fictional settings remain a leakage risk.

The grid varies query prefix (raw/search/QA), output dimension (768/512/256), and
lexical fusion (off/0.5). Truncated vectors are renormalized. Fusion uses reciprocal
ranks with k=60 over vector cosine and token overlap divided by square-root document
vocabulary size. Lexical tokens uploaded to Colab are deterministic truncated
SHA-256 hashes, which reduce exposed plaintext but are not anonymization. Raw
conversation archives and full corpus plaintext are not uploaded. Vector
representations, the 24 case records, source IDs, hashed tokens and fixed evaluator
code are uploaded to the user's isolated runtime.

Work IDs deduplicate chunks before top-k metrics. Recall here is a hit against
one labeled source per case. `wrong_top1` means failure to rank that labeled
source first; it is not proof the other source is irrelevant or fabricated.
Source hashes and cases remain private; the runner does not write a Mongo
collection, production vector index, model weight, actor contract, or deployment.

`qualification.json` separately records the two-machine scoring pass and production
ineligibility. Yoga's first pilot uses frozen query vectors generated on Stealth;
it does not establish a native Yoga EmbeddingGemma inference path. Real local
embedding and actor integration, broader independent labels, review and activation
authority remain separate requirements.

`rag_probe.py` runs two development/validation questions through the installed
`gemma4:e4b` on each machine, comparing baseline versus candidate retrieved
excerpts. It requests visible JSON with thinking disabled equally in both
conditions, bounds complete usable output to 20 seconds, and verifies exact
quotes against supplied source text. This small probe is not a broad quality,
actual tool-execution, or production actor qualification.

The existing paired E4B/31B study belongs to the concurrent cost/performance task
and remains unchanged at
`/home/err/Documents/Codex/2026-10-25-proxx-cost-performance/evaluation-report.md`.

## Tests and teardown

The law suite covers cap, reservations, stale/invalid telemetry, rate, balance,
unknown cleanup and promotion prerequisites. Adapter tests cover strict telemetry,
concurrent-run refusal, atomic private state, budget exhaustion, watchdog telemetry
failure and stop/readback separation. The scorer fixture covers parent deduplication
and modality isolation. Tests use owned temporary directories and no cloud allocation.
Real job artifacts retain distinct IDs, hashes and cleanup evidence.

The research datasets and job evidence remain active, private artifacts. Preserve
original bytes until verified recovery exists elsewhere; neither a configured
remote nor an uncommitted checkout is recovery proof. No unrelated scratch or
nested repository data is eligible for cleanup by this harness.
