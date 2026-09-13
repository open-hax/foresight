# Self-documenting Foresight: new-actor archaeology

## Objective

A new actor with no prior chat context should be able to answer, from the
repository itself:

1. What is Foresight?
2. Which repository probably owns the requested change?
3. Which local documents and code are authoritative there?
4. What is the smallest lawful place to make the change?
5. Which checks actually prove the changed boundary?
6. How should a successful local practice become reusable constellation
   knowledge?
7. Which facts are generated projections and which are durable authority?

The root should make those answers easier over time by learning from
revision-bound Git history rather than accumulating another hand-maintained
wiki.

## Current onboarding assessment

### What already works

- `src/foresight/project.cljc` is a semantic source inventory with provenance,
  ownership/actionability metadata, and recovered invariants.
- `scripts/project.clj validate` compares that declaration with `.gitmodules`
  instead of trusting prose.
- `AGENTS.md` distinguishes routing hints from ownership authority and tells an
  actor to defer to repository-local evidence for local facts.
- The root evidence runner keeps unavailable, failed, and successful evidence
  distinct and binds results to exact revisions.
- The archaeology component already has the right storage law: normalized,
  append-only facts with derived projections.
- Child repositories often contain excellent local onboarding. Eta-mu has a
  discovery-first construction loop, Epiphany has an explicit epistemic
  promotion ladder, Knoxx requires human verification artifacts for
  user-reachable work, and Services states its deployment-vs-application
  boundary very clearly.

### Where a new actor still pays unnecessary tax

- The README reaches deep evidence-adapter mechanics before giving a cold actor
  a short work-routing loop.
- The repository map in `AGENTS.md` is a useful view, but it is still prose that
  can duplicate the semantic project model.
- Child documentation is locally optimized and intentionally heterogeneous.
  There is no single root projection that says “start here, route there, then
  trust that repository's local rules.”
- Some tests encode frozen source counts and source-id sets instead of deriving
  them from the project declaration and `.gitmodules`.

The last point is already a live regression. Commit
`27ed9b9a5c5ea896a4df7d1724c6e5092769a1f1` added the `shx` submodule, updated
`src/foresight/project.cljc`, and updated the human repository map together.
`test/project_test.cljs` retained the old 14-source / 13-submodule snapshot and
omitted `:shx`. The semantic declaration is current; the duplicate frozen
expectation is stale.

That is a useful failure: **facts that can be projected should not be copied
into additional authoritative-looking lists.**

## The promoted new-actor loop

This slice adds `foresight.onboarding`, a pure projection over
`foresight.project/project`, and exposes it as:

```sh
nbb scripts/project.clj guide
```

The generated route list therefore changes when the semantic project
declaration changes. The work loop is:

```text
orient
  ↓
route from Foresight project data
  ↓
ground in the owning repository
  ↓
work at the narrowest lawful boundary
  ↓
run the gate that exercises that boundary
  ↓
preserve exact-revision evidence when something works
  ↓
promote only the reusable semantics
```

The crucial authority split remains unchanged:

- Foresight owns constellation orientation, composition, cross-repository
  evidence, and promoted common semantics.
- A child repository owns its local behavior, package/runtime choices, local
  process, and local verification contract.
- Git identifies the immutable revision being discussed.
- Archaeology/evidence can recover and compare history without turning
  similarity, repetition, or merge status into authority.

## Working-history precedents

These are merged Foresight examples where the learning loop produced something
durable. The merge revision is recorded because a PR number or current branch
alone is not immutable evidence.

| PR | Merge revision | What worked and was retained |
| --- | --- | --- |
| [#29](https://github.com/open-hax/foresight/pull/29) | `bc45117abbc5208ceee25f73f03eadfc6eae58c8` | A working Muse → eta-mu → Knoxx/Foresight review stack was mined as revision-scoped evidence without copying provider machinery wholesale. |
| [#30](https://github.com/open-hax/foresight/pull/30) | `c19b4dca35ade1c57a6e2827fd82b6041e6a1c66` | Real JVM execution showed that `.cljc` and conceptual ownership are portability intent, not proof; the reusable lesson became executable-runtime verification. |
| [#33](https://github.com/open-hax/foresight/pull/33) | `bb6b478bc33b52952698b6abcbf76c3f68ccc9ae` | Repository archaeology became a portable semantic project declaration with provenance and validation. |
| [#35](https://github.com/open-hax/foresight/pull/35) | `df95723be66f228c69e4c276dbdc0cc183ba7a08` | The project declaration was projected into a repository-routing map for actors. |
| [#36](https://github.com/open-hax/foresight/pull/36) | `873b49ba4e9d5ee9e0f267c2af2c7301441f73bc` | The routing map was corrected so routing metadata could not silently become ownership or promotion authority. |
| [#38](https://github.com/open-hax/foresight/pull/38) | `909ec43caa592a310d738e2b5fe562990d2551c5` | Merged Knoxx review/reconciliation behavior became candidate Foresight law while remaining revision-qualified. |
| [#39](https://github.com/open-hax/foresight/pull/39) | `85f04b3fcc2c608320d8e76d81dfffa2bd709e09` | Recurring architecture archaeology became normalized append-only Clio ledgers with disposable projections. |
| [#41](https://github.com/open-hax/foresight/pull/41) | `2013351996f1766246f201640795f342b3e12461` | A broken Katamorph pin demonstrated that several green checks were irrelevant because they never compiled Alpha; the relevant executable gate landed with the repair. |
| [#49](https://github.com/open-hax/foresight/pull/49) | `366d1ff427c690602ad67b0ac3df2a806af939f5` | Promotion became revision-bound and fail-closed, with explicit unavailable/incomplete outcomes instead of optimistic green. |
| [#50](https://github.com/open-hax/foresight/pull/50) | `fcb30c0bbbf1b7558d465e479c0b1b34f3d275a5` | Repeated actor-tool setup became a reproducible, integrity-checked Linux work runtime. |
| [#60](https://github.com/open-hax/foresight/pull/60) | `c3e2da70a444eacd943f156aef076b3247766065` | Repository archaeology scaled into a pinned recursive census that preserves unresolved gaps rather than fabricating closure. |

This is not intended to be the last historical list. It is a seed set of
high-signal precedents already merged into Foresight. Future archaeology should
add evidence when it discovers another materially distinct successful pattern.

## Child practices observed at the current Foresight pins

These are useful inputs, but scope matters.

### Eta-mu — discovery is part of construction

At `open-hax/eta-mu@0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90`,
`AGENTS.md` defines:

```text
Discovery → Describe → specify → define → shape → extern → domain → infra
```

and an anomaly rule: discoveries that do not invalidate the target are recorded
and work continues; discoveries that invalidate the target force
re-description.

**Promoted here:** orient and discover before editing; record anomalies rather
than silently absorbing them.

**Not globally promoted here:** eta-mu-specific board commands, “commit every
turn,” or its particular package/runtime rules. Those remain local until
separate cross-repository evidence justifies promotion.

### Epiphany — knowledge has promotion states

At
`octave-commons/epiphany@ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66`,
`AGENTS.md` distinguishes:

```text
observed → derived → provisional → accepted
```

with explicit durable promotion and preserved evidence.

**Already aligned with Foresight:** recovered evidence, lift candidates, and
accepted law are different states. The onboarding guide now makes that
distinction part of the default work loop.

### Knoxx — tests are not the whole human proof

At `open-hax/knoxx@fb08a10a8aa32a594cc97ae11b820113de4cf386`,
`AGENTS.md` requires runnable human-verification artifacts for user-reachable
surfaces and an author-led PR walkthrough that points reviewers at acceptance
criteria, decisions, risks, and stale premises.

**Candidate for broader promotion:** human-visible work should often ship a
repeatable human verification artifact in addition to automated gates.

**Why it is not made universal in this slice:** the practice is strongly
documented in Knoxx, but not every Foresight child exposes a user-reachable
runtime. The reusable kernel should be demonstrated in more than one relevant
repository before becoming a universal requirement.

### Services — say what the repository does not own

At
`open-hax/services@d361c7837ba369f5c22c9a3f9d827e74605b6c7d`,
the README says deployment topology belongs to Services while application
behavior, code, tests, and package builds belong to application repositories.

**Promoted here:** route to the narrowest owner and read that owner's local
boundary before making coordinated edits.

## Promotion law for future self-documentation

When archaeology finds a practice that appears to work:

1. **Observe** the exact repository and immutable revision.
2. **Preserve evidence** for the behavior and the gate that actually exercised
   it.
3. **Classify scope**: local implementation detail, reusable adapter pattern, or
   cross-repository semantic law.
4. **Seek another occurrence** when universality is not already inherent in the
   operator's decision.
5. **Record a lift candidate** without mutating current authority.
6. **Promote explicitly** when operator/review authority accepts the reusable
   semantics.
7. **Encode the accepted part** in Foresight as data, `.cljc` law, a reusable
   component, or a generated projection.
8. **Generate human views** from that semantic source wherever the facts can be
   derived.
9. **Gate drift** so a stale copied list cannot remain green.

A child merge is therefore not enough. “It worked” is evidence. “This is common
Foresight law” is a separate decision.

## Design consequence

The long-term documentation architecture should look like this:

```text
Git history + child-local authority
              │
              ▼
        archaeology evidence
              │
              ▼
      explicit promotion state
              │
              ▼
    Foresight semantic project data
        /        |         \
       /         |          \
new-actor     validation   other generated
 guide          gates        projections
```

The repository should progressively need less tribal/chat memory, not more.
When a new actor asks “where do I work, what do I trust, and how do I prove it,”
Foresight should be able to answer from its own checked-in state.
