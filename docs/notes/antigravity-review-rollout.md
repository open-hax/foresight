# Antigravity review rollout

## Plan and boundary

Add one thin Antigravity reviewer caller to the Foresight root and each independently
owned, actionable direct member declared by `foresight.project/sources` (23 targets
today). Consolidation inputs `.agents`, `eta`, and `clobber` are excluded. Existing
reviewers, deterministic checks, required gates, package managers, nested packages,
and branch protection retain their current policy.

The reusable producer is being implemented in
[eta-mu PR 364](https://github.com/open-hax/eta-mu/pull/364). Callers must pin both
that workflow and its review runtime to reviewed, immutable 40-character commits.
The persistent worker executes a preinstalled runtime whose HEAD must match the
runtime pin; it never checks out an input-selected revision. The runtime commit
can precede the workflow commit. They request `gemini-3.8-flash-high` explicitly; unavailable models or
workers must remain unavailable rather than selecting another model.

## Organization-local workflow routing

The hosting choice uses this machine's existing Antigravity login. Without shared
GitHub Enterprise membership, organization-owned restricted runner groups require
an organization-local reusable workflow. Open-hax members and Foresight use
`open-hax/eta-mu/.github/workflows/antigravity-code-review.yml` at `--revision`.
Octave Commons members use the byte-identical compatibility copy at
`octave-commons/muse/.github/workflows/antigravity-code-review.yml`, pinned by
`--octave-revision`. The declared repository identity determines the route, rather
than its checkout path. The current source inventory routes 17 targets (including
Foresight) to eta-mu and six to Muse: Truth, bitch-tracker, calliope, epiphany,
muse, and shx.

Eta-mu owns the reviewer and runtime semantics. Muse supplies compatibility bytes,
with no separate implementation or policy authority. Root integration must verify
the copied YAML's byte hash against its canonical eta-mu source and record both
commit identities before rollout. This tool validates pins and routes; it does not
claim remote mirror-byte equivalence. The runtime pin always identifies the
preinstalled eta-mu runtime, including when the reusable workflow resides in Muse.

Candidate callers grant only `contents: read` and `pull-requests: read`, forward no
secrets, bind the PR base and head, and leave review publication with the existing
trusted publisher. Generated files are configuration proposals. Hosting is operational at the observed revisions documented below. Default-branch
adoption remains incomplete until the producer and member rollouts are reviewed
and merged. A generated file or passing local test alone supplies no hosted proof.

Callers retain the opened, synchronize, reopened, and ready-for-review events and
also review `edited` events when `changes.base.ref.from` is nonempty. Title/body
edits skip review. Their concurrency groups include the run ID, so those edits
cannot cancel an active review or displace a pending review. Eligible events
share the stable PR review group. Exact callers from either earlier generated
format remain recognized for safe updates.

## Verification before implementation

Tests first establish the 23-target boundary, exclusion of consolidation inputs,
immutable revision validation, explicit mutation selection, read-only rendered
permissions, idempotency, owned-file revision updates, and refusal of unknown
pre-existing content. The runtime adapter must preflight all selected checkouts
before writing any workflow. It must expose absent checkouts, wrong repository
identities, workflow revision mismatches, and unsafe paths.

## Tool interface

Run from the Foresight repository with NBB:

```sh
nbb -cp src:scripts scripts/antigravity_rollout.clj plan --revision "$REVIEW_SHA" --octave-revision "$MUSE_SHA" --root /absolute/foresight
nbb -cp src:scripts scripts/antigravity_rollout.clj check --revision "$REVIEW_SHA" --root /absolute/foresight --only .,clio
nbb -cp src:scripts scripts/antigravity_rollout.clj apply --revision "$REVIEW_SHA" --octave-revision "$MUSE_SHA" --root /absolute/foresight --all
```

`REVIEW_SHA` must be an exact lowercase 40-character Git SHA, never a branch or
tag. `plan` and `check` default to the entire declared scope; `apply` requires
exactly one explicit `--all` or `--only <comma-separated-paths>`. `check` exits 1
when selected workflows are absent or differ, or any checkout is unavailable.
`plan` reports those states without modifying files. Usage errors exit 2.
The optional `--runtime-revision <exact-sha>` defaults to `--revision`; pass a
separate immutable runtime commit when the workflow commit follows runtime installation.
`--octave-revision <exact-sha>` is required whenever the selected scope includes
Octave Commons members; it pins the reviewed Muse compatibility copy. Plan entries
include each target's `:workflow-repo` and `:workflow-revision` so route and pin
differences remain visible. The renderer accepts only the two reviewed repository
routes. Exact generated callers for either route can be updated to the selected
route and revision; arbitrary repository substitutions remain unmanaged content.

Use repeated `--checkout <member>=<absolute-worktree>` arguments to direct selected
members to dedicated worktrees outside `--root`, for example:

```sh
nbb -cp src:scripts scripts/antigravity_rollout.clj apply --revision "$REVIEW_SHA" --root /absolute/foresight-worktree --only .,clio --checkout clio=/absolute/clio-worktree
```

Checkout overrides are allowed only for declared, selected targets and must be
absolute paths. Each location must itself be a Git checkout of the declared
repository, with a matching GitHub remote. The tool never initializes submodules,
changes branches, installs dependencies, or commits/pushes. Symlinked workflow
paths are refused. It writes only `.github/workflows/antigravity-review.yml`.
An exact generated file is unchanged on repeat application. The generator marker
and exact previous rendering identify an owned file eligible for a pinned-revision
update; arbitrary pre-existing content is refused, even if it contains the marker.

## Checks

```sh
nbb -cp src:scripts:test test/antigravity_review_test.cljs
clj-kondo --lint scripts/antigravity_rollout.clj src/foresight/antigravity_review.cljc test/antigravity_review_test.cljs
```

Current local verification: 12 rollout tests with 166 assertions, zero failures
or errors; all 23 generated callers pass actionlint and the exact-pin check;
clj-kondo reports zero warnings. The generated concurrency expressions are
executed in tests across eligible and ineligible edits with distinct run IDs.

## Pinned rollout plan, October 23

The derived operational plan lives at
[`.ημ/antigravity-review/rollout-plan-20261023.edn`](../../.ημ/antigravity-review/rollout-plan-20261023.edn).
It maps all 23 targets to explicit dedicated checkouts and records these source
identities:

| Purpose | Repository | Immutable revision |
| --- | --- | --- |
| Canonical workflow | open-hax/eta-mu | `2f49ccc2378ef2bbe873e79fb3cd52508ebe32f2` |
| Pinned runtime | open-hax/eta-mu | `5152dcce6e279a49b5a706ab3d3eaa4224df6cd3` |
| Octave Commons compatibility workflow | octave-commons/muse | `93ea554332bcdafffc26688836b2e6a655575c94` |

The canonical and compatibility Git blobs at
`.github/workflows/antigravity-code-review.yml` were independently read from their
respective workflow commits: 12,643 bytes each, SHA-256
`9eab25fa2a8e2508d9eb14b268a41639afa0b562b8267f11c603643a75651c16`.
That verifies byte equivalence for these revisions. After application through the
generator, the exact mapped `check` invocation verified all 23 checkouts available
and all 23 local caller files matching their expected route and pins, with exit 0.
The snapshot records that local-file result and the separately observed hosted
runs below; native review qualification and merge/adoption remain pending. It is a disposable
projection, not an event ledger or acceptance record.

R6 retains early cumulative base/head blob admission and adds one narrowly
recognized native finish-progress shape. An optional ACTIVE `tool` record must
name `finish`, have the exact allowed keys and preview containers, and immediately
precede the final DONE finish with the same conversation and index. Its clipped
preview is never review authority. Complete terminal validation, permissions and
all input limits remain unchanged. Raw replay and twenty adversarial mutations
verify that boundary.

Fresh R6 systemd service cases used this machine's existing Antigravity login:
clean 24.088677621 seconds and the known line 4 defect in 46.065075283 seconds.
Both produced five events and one turn with strict initialization, empty current
law errors and stderr, and complete cleanup. These cases did not emit the optional
wrapper; the hosted SHX case below did. The published
[R6 service descriptor](https://github.com/open-hax/eta-mu/blob/4072220789297ca94eec97506c13cd80772c7d55/.%CE%B7%CE%BC/antigravity-review/service-qualification-R6.json)
is 7,728 bytes, SHA-256
`a711b263e93a28d0401353fa20820cbce34979efb8b6c1cabfce5a4bbfdf708f`.
All 49 raw files and the lossless archive remain verified under
`/home/err/.local/share/antigravity-review/qualification/20261023/R6-service`.

Both workers were stopped and all 23 readiness flags disabled during rotation.
After qualification, both services were active/enabled, both runners online,
and all 23 readiness/group values read back. CLI, root-owned bwrap/AppArmor,
global namespace restrictions and A4/B4 group restrictions are unchanged. The
current source is `4072220789297ca94eec97506c13cd80772c7d55`; runtime and all
21 other member callers are public at R6. The last source commit appends a readable
ledger clarification and its receipt; runtime code and pins are unchanged.

Two initial R6 hosted runs passed independent validation:

| Repository | Head | Run | Result |
| --- | --- | --- | --- |
| eta-mu | `38c3f66d4a25a83877f2133e2d2e337f823cf59f` | [39937922355](https://github.com/open-hax/eta-mu/actions/runs/39937922355) | 57 paths; one retained false coercion finding; 197.8665 seconds |
| shx | `564574e6a8dd57f22f3ccb25177f26d4e9015728` | [39938323452](https://github.com/octave-commons/shx/actions/runs/39938323452) | Four paths, clean; 220.2731 seconds; real six-event finish wrapper |

Both had exact pins, complete canonical input and blob checks, current R6 law
errors empty, strict native output, empty stderr and cleanup. The
[initial R6 descriptor](../../.ημ/antigravity-review/hosted-qualification-R6-initial-20261023.json)
binds 99 raw files in the 102-file durable cache
`/home/err/.local/share/antigravity-review/qualification/20261023/R6-final-hosted`.
The eta-mu head above is superseded by the ledger clarification; it supplies no
current-head credit. Its fresh [40722207 run](https://github.com/open-hax/eta-mu/actions/runs/39939126005)
also passed: 57 paths, six events, one turn and 278.3494 native seconds, with
strict initialization, empty current-law errors/stderr and complete cleanup.
It retains the same one false coercion finding and official-document rebuttal.
The [current combined descriptor](../../.ημ/antigravity-review/hosted-qualification-R6-20261023.json)
is 13,043 bytes, SHA-256
`bef089fce35c952cc4dc327bd3a1cffbe882a41be33f1db44ca5523f78d7b131`.
Its fresh ETA artifact is `12495272961`, ZIP SHA-256
`8e6d7e8961aedab8b7bb431f028c31209f4cae739cd3d8a8a0363e82f992b52b`.
All 56 fresh raw files are retained under
`/home/err/.local/share/antigravity-review/qualification/20261023/R6-final-hosted-eta407`;
the initial 99 raw files remain unchanged. SHX's proof remains current, with
artifact `12494238848`, SHA-256
`76755066a1d056340ce784019b8c1751f3bc4a3d7ba73df63a7bd870b0f06d47`.

The original R5 hosted refusal remains a failure in the
[refusal descriptor](../../.ημ/antigravity-review/hosted-refusal-R5-20261023.json).
Its native stream completed, but R5 rejected the newly observed finish wrapper.
Historical R5 service evidence remains separately bound to its revision. The
[old MiMo failure](../../.ημ/antigravity-review/native-mimo-refusal-R4-20261023.json)
contains no complete canonical submission: its first attempt assessed 120 of
122 chunks and recovery timed out. No approval or completed cohort is claimed.

To reduce duplicate archive payloads in the source review, two owned containers
were reduced from 520,338 to 37,004 bytes of explicit metadata and custody links.
Their original bytes remain in public commit
`904ad0acd5d0c2ca634c5af68a8ee98619f2a3c7` and verified durable storage. All 61
diagnostic entries (57 tracked and four local), 15 stream files, three derived
files, hashes, modes, provenance and original outcomes are retained. No review
scope or input bound was weakened. A fresh complete review is still required.

Historical R4 service proof remains unchanged at
[its published source head](https://github.com/open-hax/eta-mu/blob/5e6af5489b42e494f3f83642a37ea4e1f98ad945/.%CE%B7%CE%BC/antigravity-review/service-qualification-R4.json):
clean 28.3808 seconds and defect 31.5417 seconds. It supplies no R6 qualification.
The following hosted runs likewise remain historical R4 evidence.

Both final R4 hosted CLI runs passed at their exact published heads:

| Repository | Head | Run | Result |
| --- | --- | --- | --- |
| eta-mu | `5e6af5489b42e494f3f83642a37ea4e1f98ad945` | [39925909795](https://github.com/open-hax/eta-mu/actions/runs/39925909795) | Clean, 54 paths, 321.9088 native seconds |
| shx | `1717d9a7173f676d7df959c08c33eabd71ce0b7b` | [39926493415](https://github.com/octave-commons/shx/actions/runs/39926493415) | Clean, four paths, 181.3723 native seconds |

Independent validation checked forced-text full-index patches and every declared
old/new blob, the current R4 law with no errors, empty native stderr, and cleanup.
The runtime's canonical `base` is the unique merge base of the requested target
tip and PR head; `base_tip` separately preserves that requested target tip. The
eta-mu artifact is `12489034409`, SHA-256
`b453b2fff43c9e7ae6bf4e1438ebfb508a2247b6323fb5080964c6e06b6a171f`;
the shx artifact is `12489847091`, SHA-256
`c8fcb9084000291d3f0f2c1a89e43ab4e2aadfe5049c710b998739a5ee6726e6`.
The 94,438-byte
[compact R4 hosted proof](../../.ημ/antigravity-review/hosted-qualification-R4-20261023.json)
records both runs. Its 97 raw retained files plus manifest were verified by byte
count and SHA-256 under
`/home/err/.local/share/antigravity-review/qualification/20261023/R4`.

The historical
[A3 proof descriptor](../../.ημ/antigravity-review/hosted-qualification-A3-20261023.json)
retains canonical metadata, validation and triage. Its full 793,819-byte archive
and raw ZIPs are retained losslessly, with descriptor hashes, under
`/home/err/.local/share/antigravity-review/qualification/20261023/A3` and on GitHub.
Large raw ZIP/base64 payloads are excluded from the root diff so bounded review
admission can inspect its source. The operator cache is storage, with no ledger
or approval authority. Historical A3 results supply no later-head R4 credit.

Negative runner canaries remain **UNKNOWN**: unapproved callers stayed queued
with runner ID zero for 180 seconds and were cancelled, with no explicit denial
observed. Configuration readback supplies no additional enforcement credit.
Earlier hosted prompt-size and AppArmor refusals retain their observed outcomes.

All 21 member caller updates are published at verified R6 heads. Twenty remain
draft; SHX is ready, with auto-merge off for all 21. Source runtime validation
passed 42 integration tests, seven pure tests with 99 assertions and 12 workflow
tests. Root rollout laws passed 12 tests with 166 assertions; project validation
and clj-kondo passed with zero warnings. All 23 local callers match R6 and the
unchanged A4/B4 workflow routes. Child product checks and native review obligations
remain separate.

Hosting is operational at R6; default-branch adoption remains **unmerged**.
The source allocation finding and ledger-readability clarification are fixed;
SHX's strict verification command finding is settled. The old positive
archive-link observation was acknowledged truthfully without inventing a repair;
the conservative canonical body-item obligation still blocks. Historical native
approvals supply no later-head credit, and fresh source deterministic/native
reviews remain pending. Imported CLI results supply no native GitHub approval.

The in-place receipt correction proposal was withdrawn on October 24 after
refreshing Receipt River from `riatzukiza/.agents` branch
`docs/segmented-receipt-ledger` at `7f4dfb8229e16996cfdb807694a630b60a130d71`.
The updated guidance makes corrections new records. Every historical receipt
remains unchanged. At withdrawal this checkout still had a legacy ledger, without the
segmented directory or Clio cutover declaration; refreshing the skill did not
migrate storage or waive its owning validator. The original invalid appended
envelopes therefore remain an unresolved validation failure. The base merge is committed locally as `0a344db6023139bbacf8edd77db326f3001804f2`,
with the preserved tree `54f98c791bb378af1668a07754118e380293c176`.
The existing segmented migration is prepared separately in
[Foresight #147](https://github.com/open-hax/foresight/pull/147); its immutable
merged-ledger gate passes locally, while the original malformed rollout receipts
remain an explicit failing control. The root R6 update is prepared for publication
in this review branch while that gate stays unqualified. Automatic merge remains off; current
review convergence and default adoption are not qualified.

This invocation checks the exact full mapping from the saved plan, including the
separately prepared eta-mu worktree. Run it from the rollout checkout. Using
`"apply"` in place of `"check"` performs the same explicitly selected rollout:

```sh
nbb -cp src:scripts -e '
(require (quote [antigravity-rollout :as rollout])
         (quote [clojure.edn :as edn])
         (quote ["fs" :as fs]))
(let [manifest (edn/read-string (fs/readFileSync ".ημ/antigravity-review/rollout-plan-20261023.edn" "utf8"))
      workflow (get-in manifest [:rollout/workflow :revision])
      runtime (get-in manifest [:rollout/runtime :revision])
      octave (get-in manifest [:rollout/compatibility :revision])
      args (into ["check" "--all" "--revision" workflow
                  "--runtime-revision" runtime "--octave-revision" octave]
                 (mapcat (fn [{:keys [path checkout]}]
                           ["--checkout" (str path "=" checkout)])
                         (:rollout/targets manifest)))]
  (set! (.-exitCode js/process) (apply rollout/-main args)))'
```

## Repository accountability during application

The global user contract requires append-only receipts for substantive work and
session-mycology reflection after substantive turns. A root aggregate report does
not replace a child's locally required record. Preserve existing ledger history
and use each child's established locations. Foresight's provenance records belong
under `.ημ/`.

| Repository | Additional inspected requirement |
| --- | --- |
| calliope | `PROCESS.md` explicitly counts workflow changes and relied-on tests as substantive. Recall at least the last three receipts before consequential work; append a receipt in every substantively touched repository. Its standard record additionally requires `:repo`, alongside `:ts :kind :origin :owner :dod :pi :host :manifest :refs`. |
| eta-mu | Read `receipts.edn`, `.ημ/session-mycology/ledger.md`, and spores when opening the session. Run session-mycology at the end of every turn. Keep implementation in its dedicated worktree and own any Rheos ledger churn caused. |
| Truth | Read recent receipts and recent session-mycology lessons before work. Preserve the blocking static-analysis gate. |
| epiphany, muse, shx | Preserve their existing Rheos/board authority and lawful transitions. Epiphany also resolves the common Git directory for worktree metadata. No workflow rollout authorizes a second board engine or hand-edited operational status. |
| Other direct members | The root survey found no additional receipt/mycology-specific rule in their root `AGENTS.md`; the global contract still applies. Later descendant instructions must be inspected before touching their paths. |

The observed hosting and final CLI runs establish execution at their recorded
revisions. Existing required checks, native review obligations and branch policies
remain authoritative; pending producer/member PRs do not establish default-branch
adoption or native approval.


## October 25 publication and receipt reconciliation

The R6 root caller and prepared operational evidence are published for review
without claiming receipt admission or merge qualification. A fresh explicit
23-target checkout check matches every caller; the 22 member PR heads match
their local revisions. Root laws pass 12 tests / 166 assertions and targeted
clj-kondo reports zero errors or warnings.

The fresh PR census found OpenCode #2 automatically closed by its imported
contribution checks. Its title and body now follow the actual repository
requirements, and it was reopened as a draft with auto-merge off. OpenCode has
issues disabled; Foresight issue149 supplies its verified closing-issue link.
The native compliance result remains separately observable.

Foresight #147 adopts the existing segmented migration and passes native
deterministic checks. Its existing model-review caller withholds signing
credentials and consequently fails the credential prerequisite; no native
approval is inferred. Foresight #148 began as a planning bridge and now carries the composed
correction reader after CodeRabbit verified all planning fixes and the user
authorized implementation. Its code review remains pending. The first rollout receipt already has a
valid keyword kind and needs scalar manifest/refs interpretations; only the
second needs a proposed ordinary-receipt kind interpretation. Neither original
record is rewritten, and full Clio cutover remains unimplemented.


The subsequent local composition adopts #147 without changing its six evidence
runtime/test blobs. Ten divergent rollout records move verbatim into separate
segments; the legacy file equals the existing migration's frozen legacy bytes.
The working conservation check retains all 293 records exactly once and every
inherited segment byte. Admission still refuses exactly the two malformed
ordinary envelopes. This is segmented Receipt River adoption, not a full Clio
cutover, correction waiver or default-branch merge. The current published R6 root
run40167985191 completed successfully at d9fb8d4f; its independent artifact
validation is separate. The local migration composition remains unpublished.


## October 25 explicit correction admission

Reader implementation `541eb5a26e6eae6dbf4ab44b7900f167b0a83d0b` preserves
the accepted four field interpretations and adds only a qualified, bound ordinary
`"test-run"` to `:test-run` representation. The same source authentication serves
immutable verification, promotion readiness and held-ledger writes. Corrections
in the comparison base are reauthenticated rather than grandfathered. Local laws
58/448, adapter 115/721 and workspace 24/120 pass; lint has zero warnings/errors.
[Native run40172110081](https://github.com/open-hax/foresight/actions/runs/40172110081)
passes all deterministic checks including receipt history on the exact #148 head.
Its MiMo job fails at the withheld-credentials boundary and publishes no review.

Local consumer `34158900e139a994ace97ba59f874316a56d3e68` composes this reader
and one new correction segment with the rollout. The actual immutable gate
against integration base `2b26173c2cbcf58f14abda2b4e710bd9a912e9bd` passes:
301 records, 36 segments and two explicit corrections. The frozen legacy hash
remains `628c21721edd557f8856dcbdc1e0ca57c16541faf1b5117e634f1620c57212ef`;
every prior segment and all 23 gitlinks are unchanged, and both original source
rows remain byte-identical exactly once. The original keyword kind on the first
record is retained; the second ordinary test-run grants no promotion evidence.
[Consumer custody](../../.ημ/antigravity-review/receipt-correction-consumer-qualification-20261025.json)
discloses the exact bindings, provenance and local result.

At that local qualification, CodeRabbit's included code review was
rate-limited until 07:38:30 UTC, and the published #145 head was `d9fb8d4f`.
The subsequent published consumer `24642b15` passed all 13 native deterministic
gates in run40172943872, including actual-base receipt history with 303 records
and 38 segments. Its Antigravity input preparation refused the complete diff
at 1,054,047 bytes, above the existing 1,048,576-byte cap. Generated JSON metadata
is subsequently compacted without changing any parsed values or raw ledger bytes;
limits and complete-file review remain unchanged.
No native approval, consumer hosted pass, default adoption, merge authorization
or full Clio cutover is inferred. Separately, eta-mu364 MiMo attempt2 timed out
after 45 minutes despite reading/assessing all 77 diff chunks; its missing final
submission and host trace leave the underlying stall cause unknown. The actual
raw artifacts and [diagnostic](../../.ημ/antigravity-review/eta-mimo-attempt2-diagnostic-20261025.json)
are retained, and no native review was published.
