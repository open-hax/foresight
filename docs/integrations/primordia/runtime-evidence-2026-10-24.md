# Primordia runtime evidence — 2026-10-24

Observed at upstream `1c93703ae33b0016d48ca59d22a5703835bfe1e6`. Engine unchanged. Locked release build completed with Rust 1.99.1 on Ubuntu 24.04, x86_64. CPU requested; GPU unavailable (`/dev/dri` absent). All 18 CPU core/golden tests passed. This does not reproduce cross-vendor GPU results.

The predeclared Truth PR64 pilot completed all 17 CLI jobs, including 13 simulations: 20-epoch smoke plus 12 matched unseeded 1,000-epoch runs (seeds 2–5). All 13 simulation artifacts validated; positive copying assay passed, null failed as expected. Smoke replay matched 2 nonempty checkpoints.

| Arm | Seeds completed | Final occupied range | Mean final occupied | Final sampled copier types | Reported ledger violations |
| --- | ---: | ---: | ---: | ---: | ---: |
| baseline | 4 | 1344–1429 | 1396.8 | 0 | 0 |
| high-upkeep | 4 | 1361–1443 | 1389.2 | 0 | 0 |
| no-energy | 4 | 1271–1301 | 1290.8 | 0 | 0 |

No sampled sandbox-positive copiers were detected at the recorded assay times in these short unseeded runs. This is not a test of all possible genomes or all times. It does not establish that constrained replication is impossible, or a cost threshold. Occupancy in the no-energy arm persists without proving reproduction. These arms differ in upkeep or energy supply, not in a calibrated SI energy scale. The 1,000-epoch horizon is shorter than one default 2,000-epoch forcing cycle.

A separate explicitly seeded live-M4 derivative ran 4,000 epochs on 51×51 cells, with zero reported ledger violations and final population 947. Its final sampled sandbox assay counted 8 copier types. A replay reproduced all 40 recorded state hashes. 41 real browser-viewer payload frames were captured from epochs 1–3992 and rendered into an animation. Frame and metadata GETs are non-atomic and cannot be used as paired scientific snapshots. The final frame's HUD population (951) is not the final epoch-4000 population (947).

This is a bounded smoke/research pilot, not verified sustained heredity, multicellular individuality, homochirality or open-ended evolution. The first questions are now empirical: which occupied sites actually reproduce, and what tracing is missing to distinguish copying over occupied tapes from parent/offspring descent? Extend horizon and add lineage instrumentation only under the reviewed next experiment.

Retained package includes resolved configs, interventions, Parquet tables, full snapshots, hashes, summaries, raw web frames, logs, unchanged pilot harness, full upstream Git history, and a Linux CPU executable/launch script. The executable's measured runtime dependencies are glibc, libgcc and libm; portability to other platforms is unverified. Native JVM integration and remote fork/constellation registration remain pending.
