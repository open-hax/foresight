# New-actor documentation census — 2026-09-06

This is a revision-pinned archaeology snapshot, not a live source inventory.
The live constellation routes are projected by `nbb scripts/project.clj guide`
from `src/foresight/project.cljc`.

Reviewed Foresight root: `78befe3daed9a27d79aa511848de3985602decbc`.

The question used for every entry was intentionally simple:

> A capable actor arrives with the repository checkout but no prior chat memory.
> Can it determine what this repository owns, what it must not own, where to
> start, and what evidence counts as completion?

## Direct-source census

| Path | Reviewed revision | Cold-start signal | Foresight consequence |
| --- | --- | --- | --- |
| `.agents` | `00a05c8f6ba68ff30a98266879310464e966748b` | Strong. README names the governing files, harness instruction order, append-only receipts, and the evidence/recurrence-based mycology promotion loop. | Keep it inventory-only as declared. Reuse its learning principle, not its whole skill corpus as automatic project authority. |
| `Truth` | `8ade66a3553bdd89696aebf4fc4628a6fe66e5ae` | Strong. README starts with one architectural rule, explains the historical failure that created it, and names the architecture test that prevents recurrence. AGENTS adds performance and single-writer laws. | Treat “historical failure -> named law -> executable guard” as a high-signal pattern for future promotion. Truth's ECS-specific law stays local. |
| `bitch-tracker` | `2751fa62b164c739cdb1ef86adc6aa1a9ff1fb90` | Strong and compact. README has a Rule zero, exact artifact shape, source map, and the smoke test that verifies the distributable boundary. | Good example of minimum viable onboarding: one critical contract plus the gate that proves it. The CommonJS shape remains local. |
| `calliope` | `2655ae6eddbd20ac400a8e1ff99914c56d81b835` | Strong for agents but nonstandard for humans: there is no root README at this pin; AGENTS explicitly says read `PROCESS.md` first and carefully separates source truth, projections, epistemic tiers, and harness capabilities. | Foresight already recovered append-only/projection and similarity-not-identity laws. Root routing must not assume every child exposes the same entry filename. |
| `epiphany` | `ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66` | Strong epistemic contract: observed -> derived -> provisional -> accepted, with explicit durable promotion and Git authority. | Already recovered into Foresight project invariants; this is the model for separating archaeology evidence from accepted common law. |
| `eta-mu` | `0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90` | Strong but dense. AGENTS defines discovery-first construction, an anomaly rule, namespace layers, board authority, and lawful transitions. | Promote the general discovery/anomaly loop; keep Rheos board commands and eta-mu runtime policy local. |
| `katamorph` | `3bd4cf26e68dc88fbe67f831baa4bc389e3363e7` | Strong README, no AGENTS at this pin. It says what Katamorph is, what it explicitly does not do, gives executable examples, and distinguishes contracts from application effects. | Existing Foresight invariant correctly retains semantic-preserving host translation. Root onboarding must fall back from AGENTS to README/ROADMAP rather than treating AGENTS as mandatory. |
| `knoxx` | `fb08a10a8aa32a594cc97ae11b820113de4cf386` | Strong but large. AGENTS carries precise JS/CLJS boundaries, human-verification artifacts, and author-led review walkthrough expectations. | Human verification for user-reachable work is a cross-repository candidate, not yet a universal law. Existing CLJS/extern ownership laws remain local. |
| `muse` | `b4bdb0a7d019bb33c71aba1bd8daec5933e7ebde` | Strong local instructions, but substantial construction-order and board-process text duplicates eta-mu nearly verbatim. | This is evidence for lifting the reusable discovery/anomaly kernel upward and reducing future copied process law. Muse remains a compatibility/compiler workspace, not semantic authority. |
| `opencode` | `cc4b45612974f735ddec46009ede07729511fba4` | Upstream/product-oriented README. Excellent for using OpenCode, weak for answering why this shallow Foresight fork is present or which constellation semantics it owns. | Foresight's project model must supply the missing constellation role (`coding-agent-host`). Do not copy upstream product documentation into Foresight. |
| `proxx` | `abbbc8b1ad80738233593e17e751203db785c9e2` | Feature-heavy README with a `DEVEL.md` pointer. Important secret/local-state and Services deployment boundaries exist, but appear after substantial product detail. | Foresight already recovers the secret locality law. Root routing should get an actor to Proxx only after the model-proxy boundary is known. |
| `services` | `d361c7837ba369f5c22c9a3f9d827e74605b6c7d` | Clear ownership boundary in README: deployment topology here; application code/tests/builds in application repositories. No AGENTS was present at the reviewed pin. | Already promoted as a Foresight routing invariant. This is one of the best examples of documenting what a repository does *not* own. |
| `uxx` | `97e67a7a758c080450a200e8e6e1ada614eabc6d` | Clear canonical-React/wrapper architecture and package matrix. Some quick-start commands assume the larger `orgs/open-hax/uxx` workspace path rather than the Foresight submodule path. | Existing React-canonical binding invariant is useful. Root onboarding should route ownership without assuming child command paths are relative to the Foresight root. |
| `shx` | `6cd0ce366c66ac50dfa3ec55bdd8e55eb021bcaa` | Strong and compact: IR purpose, namespace law, strict gate, and explicit “could not run also fails” semantics. | Newly added source currently has no source-specific invariant in the Foresight project model. Its strict unavailable-is-failure behavior already agrees with `:foresight/unavailable-never-pass`; IR/layer details remain recovery candidates. |
| `eta` | root-owned | Dual role: workspace source/consolidation input and root-native transduction harness. | A generated route must show the native role without granting the source execution authority. PR #79 now folds both meanings into one path. |

## Root-native components

The original `guide` implementation projected only `:project/sources`. That was
insufficient: `alpha` and `archaeology` are root-owned native components, not
sources, and therefore disappeared from the first generated routing view.

The review caught this before merge. The corrected projection includes:

- `alpha` — structural integrity;
- `archaeology` — causal architecture archaeology;
- `eta` — transduction harness, folded into the existing `eta` source row.

This is itself a documentation lesson: **the generated human view must cover the
semantic model, not merely the easiest collection inside it.**

## Patterns already earned by Foresight

These are not new proposals; the current root project model or merged history
already carries them:

1. **Independent repositories keep local authority.** Root routing does not
   rewrite a child's package/runtime/quality policy.
2. **Git revisions bind historical claims.** Current-looking prose is not enough
   to identify the behavior that was actually reviewed.
3. **Projections are not truth.** Calliope and Epiphany independently support
   rebuildable views plus explicit promotion rather than silent identity.
4. **Unavailable is not pass.** Foresight, Epiphany, Calliope, shx, and the
   evidence runner all converge on this behavior.
5. **Relevant execution beats nominal green.** Foresight PR #41 demonstrated
   that several successful checks can coexist with an uncompiled broken
   component.
6. **Routing is not authority.** Foresight PR #36 already corrected this exact
   category error.

## Repeated patterns ready for a lift candidate

### Discovery before construction

Eta-mu and Muse independently carry the same discovery-first construction order
and anomaly rule. The portable kernel is smaller than either local document:

> Inspect the existing shapes before editing. Record a discovered anomaly when
> it does not invalidate the target; if it does invalidate the target, revise
> the description before continuing.

PR #79 promotes that kernel into the default new-actor work loop without making
the Clojure-specific layer order universal.

### Critical rule plus executable guard

Truth's single-substrate regression and BitchTracker's distributable export
shape both follow this pattern, while shx's strict analyzer applies the same
spirit to quality gates:

```text
important failure or boundary
        -> short named rule
        -> narrow executable guard
        -> gate that actually runs it
```

This is a strong candidate for common Foresight process law. It is not promoted
as a mandatory invariant in this slice because not every semantic rule has a
cheap deterministic executable check.

### Human verification beside automated verification

Knoxx has the clearest version: user-reachable changes get a repeatable human
verification artifact and the PR author leads a walkthrough. This is a useful
candidate for UI/product repositories, but the direct census does not yet show
independent adoption across enough comparable repositories to call it universal.

## Anti-patterns recovered by the census

1. **Frozen duplicate inventories.** The `shx` addition updated `.gitmodules`,
   the semantic project model, and the human repository map, but an old hardcoded
   source/count test stayed stale. Derive these facts instead.
2. **One filename as universal entry point.** Some repositories lead with
   AGENTS, some README, some PROCESS/ROADMAP, and upstream forks have their own
   contribution contract. Foresight should tell the actor the search order, not
   demand one copied template.
3. **Copied process doctrine.** Muse and eta-mu demonstrate how common rules can
   drift when duplicated. Lift the portable kernel; keep local mechanics local.
4. **Feature catalog before ownership.** Product-heavy READMEs are useful to
   users but can bury the repository boundary for a worker. Foresight's route
   projection should answer ownership before the actor dives into the child.
5. **Network-dependent proof in pure law tests.** Historical GitHub metadata is
   useful archaeology evidence, but a pure project-law test should not require
   live GitHub availability. Validate remote claims during archaeology/review,
   preserve immutable revisions, and keep deterministic project tests offline.

## What self-documenting means here

It does **not** mean one giant canonical handbook.

It means the root can increasingly derive the answer to:

```text
what exists?
  -> what owns this kind of work?
  -> what local evidence governs it?
  -> what gate proves the change?
  -> what did Git history teach us?
  -> has that lesson merely been observed, or explicitly promoted?
```

The live answer should come from semantic project data and ledgers. Documents
like this one are revision-bound archaeology evidence explaining why those
projections and laws exist.
