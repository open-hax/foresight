# Character field kernel: owner and numeric contract proposal

Status: **proposed for planning review**. This completes a concrete decision
proposal for task `b5f2ba9e-8d67-4c73-9919-e275983aca87`; it does not mark that
task ready or done. The character epic remains
`63a0e4ff-353f-4c90-ab8a-7241d958d54c`. Native Rheos dependency retention and
admission, an acknowledged owning-repository plan, implementation RED/GREEN,
release qualification and deployed evidence remain required.

## Decision

Select the existing **`octave-commons/eros-eris-field`** repository as the
proposed owner of the character's physical computation library. Its existing
`@workspace/eros-eris-field` package supplies the force/layout seam. Extend that
library through a portable `.cljc` kernel and outward adapters after admission,
rather than adding a second force engine to Knoxx or the Foresight root.

The proposed semantic namespaces are `eros-eris-field.law`,
`eros-eris-field.shape` and `eros-eris-field.domain`. Their exact exported ABI
must be acknowledged in that owner's implementation plan. One compiled CLJS
artifact supplies the Node computation authority. The existing TypeScript
exports become compatibility adapters to the accepted kernel; they do not
retain independently evolving character physics.

This is a **repository-specific ownership proposal**, not a promotion of common
actor, identity, event or graph law. Foresight coordinates the decision and
conformance requirements. Existing Axxium/Katamorph/Clio authorities remain
separate. A later shared-law lift needs its own reviewed decision.

### Accountable boundaries

| Responsibility | Proposed owner and seam |
| --- | --- |
| Numeric evolution, sparse field, bonds, seeded movement and contacts | `eros-eris-field` kernel; no network, clock read, database or model call |
| Trusted visibility, graph/content projections, snapshots and query transport | OpenPlanner's current storage/query and SDK/REST adapters |
| Independent mood/appraisal, attention, traits, actor identity and action choice | Knoxx character domain and its explicitly versioned mood model |
| Intake and actual account effects | Existing Knoxx social/media adapters under the configured actor |
| Admission, replayable facts, grants and resource/lifecycle ownership | Existing canonical authorities consumed through their contracts |
| Kernel persistence/catch-up worker | One explicitly owned consumer adapter; it cannot become a second gateway or creative scheduler |
| Visualization | Read the committed kernel snapshot; screen coordinates cannot become cognition authority |

Development head and base must use the personal fork
`riatzukiza/eros-eris-field`. The initial authenticated native read on
2026-10-07 returned 404. The fork was subsequently created at
`2026-10-07T17:09:50Z`; native readback verified its `octave-commons/eros-eris-field`
parent and personal `main` at `3af779f3ec306834710719ccb46f49370343eed9`.
This establishes the personal repository and that source revision. The owning
plan, actual reviewer routes, source/distribution license and lawful readiness
still require independent qualification. The organizational repository is a
separately qualified release destination. No approval from this proposal
transfers to that source or release PR.

## Source basis and non-equivalence

The revision-bound selection manifest accompanying this proposal records actual
Git blob sizes and SHA256 values. These are source observations, not reviews,
runtime acceptance or verified licensing of a distribution.

| Source | Revision | Reuse and correction |
| --- | --- | --- |
| Standalone `eros-eris-field` | `3af779f3ec306834710719ccb46f49370343eed9` | Existing package, Barnes–Hut, local repulsion, springs, semantic forces and boundary seam. Its current solver is layout-only, mutates arrays, clamps elapsed time and damps per call. |
| OpenPlanner graph package and memory route | `07085d6557b75834ce6f50e6c54b8ca47e1c7c08`; inspected equivalent selected blobs at local `582efb7ebbdc542650fd239bfc5f2de076316c53` | Current storage/query integration and donor variant. The OpenPlanner force variant has distance-dependent semantic break behavior absent from the selected standalone source. Neither variant implements the complete character mechanics. |
| Fork Tales mechanics and later design correction | `f4c43d7b9c832a54bc5d224cb4d5d2497c04423b` | Eight sparse wind layers, velocity amplitude, intent bonds, unequal friction, lazy seeded simplex field, immutable emitted ownership and integer intent exchange. Later corrections control where older executable behavior differs. |

The standalone repository and package exist now; the package is private, and
its native source tree has no tracked license file or package license field.
The inspected OpenPlanner copy declares LGPL-3.0-or-later. Preserve these
distinct observations. The owner must establish the source/distribution license
and required notices before literal extraction or distribution; package-name
equality cannot transfer that metadata. This process document is GPL-3.0-or-later.

OpenPlanner's local `582efb7` roadmap describes package-by-package home selection
and a teardown. That file is absent from native `07085d6`. It is historical
context, not evidence that a current teardown or ownership transfer completed.
The actual standalone source and current consumer seams are the basis here.

### Mechanics that must survive

1. There is one nexus-bond topology. Authorship, reply, pinning, inspiration and
   contradiction retain their evidence types and intent. They project into one
   bond record, with contributions and provenance, instead of two independent
   semantic/structural edges counting the same relationship twice.
2. Nexus, presences and daimoi occupy the same physical coordinate system. Nexus
   and presences respond to wind and forces with greater friction; neither gains
   daimoi self-propulsion. A dissimilar but deliberately pinned pair can have a
   flexible intent bond. A cosine threshold cannot silently discard its purpose.
3. The environmental field stores eight vector layers sparsely. Moving daimoi
   deposit direction **and amplitude**. Deposit, decay and pressure are numeric
   state, never keyword counts or adjectives in a prompt.
4. Daimoi have immutable `emitted-owner`, a stable seed embedding, attributed
   mantle/intent changes and integer wallets. Deflection changes trajectory and
   contact target, not ownership or authorization. Absorption terminates that
   particle and attributes the transferred units to both emitter and recipient.
5. Integer exchange has a separate explicit collision operation. It transfers
   nonnegative counts without fractional wallets, silent minting, owner handoff
   or probability-based authority. Full absorption, deflection and a bounded
   exchange remain distinct operations. The owning plan must resolve the later
   design's uncertain partial-absorption wording against these conserved units;
   unresolved behavior blocks that fixture instead of disappearing from scope.
6. A scoped recall intent can emit a seeded owned particle. Its actual motion,
   contacts and bond path affect which memories are encountered. A static force
   picture, renamed Dijkstra cost or accumulated trail counter cannot satisfy
   this contract by itself.

## Numeric contract selected for review

### Representation and actual targets

- Physical scalars use finite IEEE-754 **binary64**. The authoritative target
  is the compiled CLJS kernel on Node. `.cljc` keeps the domain portable, but
  this proposal does not claim JVM, Python, GPU or browser numerical parity.
- IDs, sequence numbers, event times and wallet counts are exact integers or
  canonical strings. Integer values accepted by the JS adapter must be within
  `[0, 2^53 - 1]`; overflow refuses the whole transition. No f32 state packing.
- The world is two-dimensional and measured in **world units**. Position is wu;
  velocity is wu/s; acceleration is wu/s²; mass is positive dimensionless
  inertia; spring rest length is wu; friction/decay rates are s⁻¹. Screen pixels
  and embedding coordinates are different spaces and cannot be substituted.
- Cell size is a positive wu configuration value. Sparse keys are
  `[scope-revision, layer-index, floor(x/cell-size), floor(y/cell-size)]`.
  Layer indices are exactly `0..7`. Their semantic bindings are a versioned
  character configuration, not labels inferred from eight English keywords.
- Intent units are nonnegative exact integer counts. Embedding vectors are
  immutable, dimension/model-revision-bound input values; mismatched dimensions,
  missing revisions or nonfinite components are refused before any force pass.

All physical coefficients, capacity limits, bounding geometry and layer bindings
are mandatory revision-bound configuration. The existing donor's constants are
not production defaults. The owner publishes an explicit calibration artifact
and configuration hash before a candidate can be admitted. Missing coefficients
cannot fall back to a plausible-looking motion.

### Time and quiet periods

1. The adapter admits UTC observation identity separately from a monotonic
   **integer-microsecond elapsed interval**. The pure kernel never calls a host
   clock. Negative/noninteger/nonfinite elapsed time refuses unchanged state;
   zero elapsed time is an exact no-op with no movement, damping or RNG advance.
2. Proposed integration quantum: **10,000µs / 0.01s**, with residual time carried
   exactly. One advance executes at most **100 substeps** and returns the
   remaining backlog and state time. The cap is a work bound, not an elapsed-time
   clamp. A long interval cannot become a fictitious 0.5-second successful step.
3. Boundary events are applied in an explicitly recorded schedule. Equal total
   elapsed time with different event placement is not assumed equivalent. With
   identical placements, splitting a host call preserves the same quantum
   sequence, residual, seed counter and elapsed-time damping.
4. Each field cell decays by `exp(-lambda * elapsed-seconds)`, where each positive
   layer half-life gives `lambda = ln(2)/half-life`. Quiet-time reads evaluate
   decay at the snapshot's declared kernel time. A bounded worker advances time
   through admitted clock observations independently of whether an LLM is called.
5. Incomplete catch-up is explicit. Decisions cannot label a behind-time physical
   state current. Resume, checkpoint restoration and a chosen reset/discontinuity
   have separate attributed events. Host sleep does not prove worker termination,
   effect fencing or permission to retry a creative publication.

The 100-substep cap and quantum are reviewable choices. Required bounded-load
measurements must qualify them at the owner's selected graph/particle/cell caps.
No performance or uninterrupted availability has been measured by this proposal.

### Integration, deposit and deterministic order

Use one revisioned semi-implicit Euler step: gather all forces from the prior
snapshot, update velocity, apply elapsed-time friction, update position, resolve
ordered contacts, then publish the next snapshot. Deposit and contact outputs
enter the following field buffer; array iteration order cannot grant a particle
extra influence in the current force pass.

Velocity damping is `exp(-gamma-kind * h)`. Configurations require
`gamma-presence > gamma-nexus > gamma-daimoi >= 0`; static-friction thresholds
have the corresponding order. Below a configured static threshold, a passive
nexus remains still. The owner must test both the ordering and zero-threshold
boundary. A common per-invocation multiplication such as `0.8` is not time law.

For layer `l`, deposit is `gain-l * weight-l * velocity * h`, with gain in s⁻¹,
weight dimensionless and field vector in wu/s. A higher speed therefore produces
greater magnitude with otherwise equal inputs. Saturation is a declared radial
magnitude bound, with an attributed saturation observation; it never silently
turns the field into unbounded noise. Sparse pruning uses a declared vector
magnitude epsilon and keeps original events available for reconstruction.

Entities, bonds, cells and contact pairs use canonical ID/tuple ordering.
Barnes–Hut construction and accumulation use that same order; pair collision
resolution records its order. Duplicate entity IDs, missing endpoints or
ambiguous typed-bond projection refuse admission rather than winning by insertion
order. Recentring applies to all spatial state together or to the display only;
moving nexus coordinates while leaving cells and daimoi behind is forbidden.

### Seeds, noise and replay

Use a recorded nonzero unsigned 32-bit seed and an explicit draw counter.
The proposed discrete sampler is xorshift32 with shifts `13,17,5`, modulo 2³²,
and `u = unsigned-state / 2^32`. Canonical ordered candidate weights determine
selection. No `Math.random`, process hash, wall-clock noise or provider sampling
inside the physical transition.

For continuous exploration retain the recovered **lazy 2D simplex** seam, sampled
at the particle's position and recorded kernel time. Pin its permutation/gradient
table, octave/scaling configuration and particle seed. The Fork Tales donor uses
an identity permutation and small gradient offsets, so its `seed` argument alone
does not prove independent seeded realizations. The owner must qualify the
selected seed-to-permutation mapping and prove same-seed replay/different-seed
influence. This is a planned correction, not an assertion that an existing
noise implementation passes those laws.

The initial same-target replay tolerance is:

`abs(a-b) <= 1e-9 + 1e-9 * max(abs(a), abs(b))`

for positions, velocities, field vectors and continuous force/scalar summaries
in their declared units. IDs, event order, residual microseconds, seed counters,
wallet counts, contact classifications, permissions, included memories and
selected actions must match **exactly**. Tolerance cannot excuse a changed path,
ownership result or choice. Validate numeric bounds and threshold-near cases;
a candidate that amplifies tolerated scalar error into different discrete
outcomes fails replay. State/config/artifact hashes remain actual exact bytes,
never replaced with a tolerance-derived synthetic hash.

## Physical recall and independent mood

The trusted OpenPlanner adapter first resolves the stored Knoxx actor and its
actual grants, then seals a visible graph/content snapshot. Visibility controls
semantic seeds, expansion, field contributors, contacts, compaction and every
feedback write. A private node cannot exert hidden mass or wind on an otherwise
public walk. Revocation requires a new permitted projection/state revision,
including contributions retained in cells and trails; filtering only returned
text is insufficient.

A deliberate recall intent has a stable request ID, scope revision, query/seed
identity, kernel/config revision, current mood/lens revision and physical budget.
It admits a particle emission once. Polling that request's committed contacts is
a projection read, and repeating the request cannot emit/reinforce twice. Its
trace binds starting nodes, motion samples, encountered bonds, contacts, actual
memory IDs, inclusion reasons, elapsed horizon and budget exhaustion. No-contact,
denied, stale, incomplete and failed results remain distinguishable.

The independent Knoxx mood model supplies validated appraisal/state and attention
inputs. Those can change emitted intent weights and the lens used to interpret
contacts, but cannot change grants or ownership. OpenPlanner's stable content
embeddings remain intact. Semantic attraction, physical movement, contacts and
inclusion are separately observable links; a geometry-only comparison is weak
evidence for the full character loop.

Motion can deposit an attributed transient field signal because the motion
actually occurred. Positive semantic reinforcement requires a later admitted
choice/outcome with actual success/failure evidence. Retrieval alone cannot
claim a useful association. Persistence failures return explicit failure and
commit no success receipt; outcome feedback is idempotent by immutable outcome
identity. A disabled-trail option must not conceal a separate feedback write.

## Owning implementation acknowledgement and adoption

The kernel owner's plan must explicitly acknowledge this decision's revision,
source map, numeric rules and unresolved calibration/license/collision cases.
Its reviewed work must separate the pure kernel from semantic-provider and host
effects; the current optional Vexx network scoring stays outside the transition.
Record source and compiled artifact hashes and one Node runtime/config identity.
Consumers take the qualified artifact, rather than copying its force functions.

OpenPlanner's graph/storage changes and Knoxx's character composition each need
their own personal head/base, current full-input review, native readiness and
implementation proof. The existing OpenPlanner fork is far behind its donor;
ordinary history-preserving source integration needs independent qualification.
No default-branch reset, force push, identity alias or approval transfer is part
of this decision.

All character cards remain incoming. Upstream Rheos must actually retain the
relationships and enforce missing/unfinished/cyclic refusal plus completed
predecessor admission. A Markdown link or this source-owner choice cannot stand
in for those native cases. The pending coordination question concerns the
existing upstream Rheos lane; this proposal changes no board writer or runtime.

## Required falsification matrix

These are **future RED/GREEN obligations**, not executed tests in this proposal.

| Requirement | Failure that must be exposed |
| --- | --- |
| Finite/time shape | Zero dt moves or draws RNG; negative/NaN/Infinity changes state; overflow is coerced |
| Elapsed damping and catch-up | Splitting the same event schedule changes damping; excess time silently vanishes |
| Sparse velocity field | Dense global allocation is required; speed amplitude is discarded; quiet time never decays |
| One intent-bond topology | Two representations double the force; a dissimilar explicit pin disappears |
| Different friction | Nexus self-propels or all three kinds share a tick-based multiplier |
| Emitted ownership and intent conservation | Deflection changes owner/grants; exchange creates fractional or unaccounted units |
| Seed/noise/replay | Same seed diverges or clock/random calls enter the kernel; near-threshold contacts/paths differ |
| Physical causal recall | Only the picture moves; traced contacts and included authorized memories cannot change |
| Scope before influence | Hidden content supplies seed, mass, pressure, path, compacted evidence or feedback |
| Independent mood | One prompt blob substitutes for state; appraisal can invent an encounter or permission |
| Truthful idempotent feedback | Query repeats reinforce twice; a failed writer returns successful reinforcement |
| End-to-end character choice | Two admitted encounters under fixed seed cannot change state/recall and meaningful choice |

The full implementation test binds **encounter → field → independent mood and
attention → physical associative recall → choice → observed outcome → memory**.
It includes social relationship choices and persistent character continuity.
Transport success, a green layout build or a qualified planning proposal cannot
complete that test.

## Estimates and remaining decisions

The existing kernel-selection story remains a **provisional 3-point planning
decision**. Its frontmatter and the solver's 8-point estimate are unchanged.
Owner acknowledgement and independent review must assess whether portable
extraction, sparse dynamics, conserved collisions, physical recall coupling and
bounded replay fit that solver story or require separate implementation slices.
An estimate concern does not remove any required mechanic.

Still blocking acceptance: owning-plan acknowledgement; a complete reviewed
physical coefficient/capacity calibration; explicit partial-exchange policy;
source/distribution license and ABI; native Rheos readiness; actual source and
distribution qualification. These are concrete remaining requirements, not
accepted deferrals or evidence that this document implements the character.
