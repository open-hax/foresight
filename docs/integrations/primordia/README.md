# Primordia × Truth integration

Status: planning and standalone acquisition; native integration and remote fork registration are not implemented.

User outcome (2026-10-24): run Primordia on its own, begin answering the research questions, start Truth integration, and add our fork to Foresight.

## Source and ownership

Upstream: https://github.com/Chokyotager/Primordia at `1c93703ae33b0016d48ca59d22a5703835bfe1e6` (master), MIT, Copyright (c) 2026 ChocoParrot. Preserve its Rust/wgpu implementation and complete history in the eventual downstream fork. Truth stays GPL under its own existing ownership. Native ABI implementation belongs downstream in Primordia; JVM effects belong in Truth infra; suite wiring belongs here.

Expected downstream destination: `riatzukiza/Primordia`, pending actual fork creation and readback. On this run the connected GitHub principal was riatzukiza. The connector exposes no fork operation; browser authentication was declined. No remote fork, gitlink or registry entry is claimed. Do not commit an unreachable submodule. The adjacent candidate registration patch is deliberately inactive.

## Experiments first

Truth PR [#64](https://github.com/octave-commons/Truth/pull/64) contains the bounded pilot harness at `08c34bf662108ba8220b43a3dd3c32ff9b64a3b6`. Reuse it against clean upstream rather than changing physics to get a positive result. The previous attempt completed zero runs; new evidence must remain a separately dated successor.

Five research questions: replication/failure threshold; bonded division of labor; homochirality; dimensional/resource/thermal geometry; open-ended evolution versus stagnation. The initial pilot addresses resource sensitivity only. Four seeds and 1,000 epochs cannot establish a universal threshold or sustained heredity. The default forcing period is 2,000 epochs.

The built-in copying assay uses a resource-free paper sandbox; it does not establish resource-constrained replication. Births, population, lineage depth and sampled copying counts do not individually establish heredity. No-energy occupancy may persist through dormancy. Require explicit continuing reproduction and parent/offspring identity under costs before biological promotion.

## Proposed native contract v0 (not implemented)

Truth ECS owns habitat IDs, world time, environment and lifecycle. A native habitat is an owned subsystem, not a second Truth world. One Rust worker exclusively owns each Sim/backend, including GPU queue access. Truth systems submit bounded batches asynchronously; only the ECS barrier admits immutable observations.

| Operation | Input | Result / rejection |
| --- | --- | --- |
| create | ABI version, exact resolved TOML, explicit backend | opaque generation-qualified handle; invalid config/backend fails explicitly |
| submit | handle, request UUID, expected start epoch, bounded epoch count, epoch-bound interventions | request ticket; exact duplicate returns same ticket; changed retry rejected; full queue reports busy |
| poll | handle, ticket | queued/running/completed/failed/cancelled; terminal observation immutable |
| observe | any terminal ticket (completed/failed/cancelled), caller capacity | immutable success observation or structured terminal diagnostic; reports required capacity without truncation |
| close | handle | cancel/drain policy then join owner; stale/closed handle rejected; no use-after-free |

Submission is atomic: the worker admits a FIFO batch only when expected start equals the accepted queue tail. An intervention carries event UUID, target epoch, within-epoch ordinal, kind and validated payload; its target must lie in the batch's half-open epoch window [start,end). Apply events at the start of their target epoch, before stepping; ordinals must be unique at that epoch. Reject stale/out-of-window events, repeated event IDs with changed bytes and overlap with accepted work. Preserve admitted order and payload digests in the replay log. Request retry identity covers the complete batch including interventions: exact retry returns the existing ticket; any changed field rejects. Queue-full rejection admits neither batch nor events. On worker failure, accepted successors become cancelled with a causal diagnostic, rather than silently skipping their expected start epochs.

Poll returns status and a stable terminal-result reference. Observe accepts all terminal statuses and copies a bounded result tagged success/error/cancelled; the error form includes failed operation, stable error code, last confirmed epoch, sanitized diagnostic, and request identity. Failed requests remain retrievable until close. Close joins the owner only after a final poll/observe or an explicit discard acknowledgement; it never leaves a dangling result pointer. A panic test must retrieve the structured error through the same API, not merely observe a failed status.

Observation fields: schema/ABI version, habitat/run/request identities, upstream and adapter revision, resolved-config digest, backend, start/end epoch, observation digest, summary and coverage, ledger-violation count, units profile, omitted/unknown fields and terminal error. All integer widths, allocation ownership, maximum lengths, endianness and status codes must be fixed in the reviewed C header before implementation. No Rust structs, panics, borrowed GPU buffers or mutable pointers cross the ABI. Never call Clojure from a worker. No implicit GPU-to-CPU fallback.

JVM FFM requires Java >=22; JNI is an alternative to select after checking Truth's supported runtime. No production dependency or Java version change is made by this plan. No per-epoch full GPU readback: compact observations at batch boundaries, full snapshots only on explicit cadence. Native buffers remain native-owned and are copied/released through documented functions before ECS admission.

## Required red tests before native code

Lifecycle: create/submit/poll/observe/close; invalid TOML; unknown ABI; stale/closed handle; close during pending work; panic becomes terminal error. Ordering: duplicate request, changed retry payload, wrong expected epoch, out-of-order completion, stale observation and bounded queue. Interventions: same-epoch ordinal ordering, stale/out-of-window rejection, changed-payload retries, full-queue atomic rejection and replay of queued batches with forcing events. Failures: retrieve diagnostics after panic and retain cancelled-successor causal references. Science: same seed/config/events and CPU backend reproduce nonempty state hashes; explicit unknown units and missing lineage remain unknown. ECS: only the owning system writes its declared component at the barrier; no native effect imports into domain.

One-way environment changes need a named abstract translation profile and logged interventions. Two-way exchange requires calibrated units and conservation audits. Adaptive fidelity remains a later shadow-rollout experiment; no automatic life or fidelity transition is admitted here.

## Activation and review

Fork availability is an external prerequisite for publishing downstream native work; completion of final Foresight registration is not a prerequisite for native development. Baseline acquisition proceeds independently on pinned upstream. Native work depends on the baseline and verified fork availability; environmental coupling depends on native admission plus matched baseline controls.

The [epic](../../agile/kanban/primordia-epic.md) and four child stories remain incoming planning inputs. Use canonical pr-flow for planning review, Rheos admission, red/green and exact-head review. Missing review/board/runtime capabilities remain missing. Standalone execution of unchanged upstream is evidence acquisition, not native implementation admission.

After fork creation, verify its parent and reachable commit; apply the registration candidate, add the gitlink, and add Primordia's gate catalog and routing tests in the same implementation. Root validation and project/workspace suites must pass. Do not claim merge, deployment or constellation registration from this document.
