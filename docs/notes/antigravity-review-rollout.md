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
trusted publisher. Generated files are configuration proposals. Hosted installation
is incomplete until the producer and member rollouts are reviewed and merged,
the isolated worker/model is verified available, and actual hosted execution proves
the integration. A generated file or passing local test is not that proof.

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

## Pinned rollout plan, October 23

The derived operational plan lives at
[`.ημ/antigravity-review/rollout-plan-20261023.edn`](../../.ημ/antigravity-review/rollout-plan-20261023.edn).
It maps all 23 targets to explicit dedicated checkouts and records these source
identities:

| Purpose | Repository | Immutable revision |
| --- | --- | --- |
| Canonical workflow | open-hax/eta-mu | `6461caabbe4eefbd1615036936863d06ee69d68d` |
| Installed runtime | open-hax/eta-mu | `3bcf27bef296f9349fdf4997df256a617da5f120` |
| Octave Commons compatibility workflow | octave-commons/muse | `5bc9a8543ceb8afb04d3fd7e92ba0e1d9c2a03dd` |

The canonical and compatibility Git blobs at
`.github/workflows/antigravity-code-review.yml` were independently read from their
respective workflow commits: 12,458 bytes each, SHA-256
`f6c5f07015846b14a27f03bd8a59cd5445cd06ec10f5613413c92d12ca35d82b`.
That verifies byte equivalence for these revisions. After application by the root
operator, the exact mapped `check` invocation verified all 23 checkouts available
and all 23 local caller files matching their expected route and pins, with exit 0.
The snapshot records that local-file result; hosted-run, review-qualification,
and merge evidence remain pending until separately observed. It is a disposable
projection, not an event ledger or acceptance record.

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

Workflow generation alone leaves the isolated runner registration, selected model,
trusted publication path, and hosted end-to-end execution to be verified. Existing
required checks and branch policies remain authoritative; a pending producer or
member PR is not an activated review workflow.
