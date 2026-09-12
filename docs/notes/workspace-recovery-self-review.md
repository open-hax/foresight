# Workspace recovery self-review

Scope: root source initialization, deterministic shared manifests, compiled Clio
consumption, and local development fixtures. The application/identity/wiki changes
have separate review and browser evidence.

## Decisions checked

- The root project model still declares 14 direct child repositories. Missing
  source was restored at the recorded pin; active eta-mu/Knoxx work was preserved.
  No child implementation was rewritten by the workspace generator.
- Only root tooling, `devtools`, and canonical Clio join the pnpm group. Tracked
  package inventory does not grant execution authority. Consolidation inputs
  remain excluded from manifest traversal and execution.
- Generation reads policy, source metadata, explicitly selected package manifests, and
  Clojure dependency inputs. Input digests and duplicate identities remain
  inspectable. Conflicting selected versions require an explicit explanation.
- A missing child Git boundary cannot silently inherit the parent's Git inventory.
  Existing workspace selection/ownership tests remain in the root test command.
- Selected child HEADs must match the root gitlinks, and selected source paths
  must be clean. Actual Git fixture tests rejected tracked source edits, untracked
  source files, and a clean unpromoted child commit. Promoting its gitlink restored
  generation. Uninitialized unrelated children and unselected example changes
  no longer affect composition; the earlier broad inventory was a review finding.
- Selected library aliases do not copy build/test aliases into a different working
  directory. The reviewed quality gate catalog is unchanged.
- Shared-cache installation is observable: the same Shadow package file has the
  same filesystem device/inode in root, Katamorph, and Bitch-tracker, with nine
  hardlinks. Child lockfiles were retained; the two children without lockfiles
  were installed without creating a fabricated lockfile claim.
- Root Clio tests exercise a real persisted event and refuse missing/corrupt
  ledger inputs. The compiled export reads `:canonical/events` from Clio's
  canonical history result, rather than mistaking the history map for events.
- Shadow compile/autorun alone was observed returning zero despite an assertion
  failure. Running the emitted Node suite separately returned one for that same
  failure. The generated command now preserves that failure boundary.
- Local S3 tests use a real server and AWS client. Embedding tests execute pinned
  offline CPU inference. Neither fixture is advertised as a production provider.

## Verification

The root command runs 114 Clojure tests / 511 assertions plus the Node repository
census assertion program, with no failures. The compiled consumer builds, root
lint is clean, S3 put/get works, real 384-dimensional embeddings pass the HTTP
smoke, and a frozen offline pnpm install succeeds. Model weights remain an
explicit cached capability and are not downloaded by normal root tests.

The separate warmed-cache HTTP suite passes 14 tests. Malformed tool fields
previously started real inference; a failure-first run reproduced five such
cases. They now receive a deterministic client refusal, while an empty tools
array remains accepted. Schema errors, inference deadlines and oversized upload
draining retain their real HTTP regressions. The selected eta-mu source is now
the published `a7b19825fb5d7c624c38f1d41043c42e92d7f0c3` identity checkpoint;
generation, frozen offline install and all root source/build/lint gates passed
again after that explicit gitlink promotion.

The recovery evidence also records unchanged child builds/tests and their
failures. A zero exit or a green subset is not a whole-stack acceptance signal.
The Katamorph namespace/var compiler warning and Calliope unused binding were
subsequently fixed in their child branches; their full declared gates now pass
with zero warnings. Exact child revisions still need deliberate root promotion.
The current-main Uxx branch passes all 466 tests. Review then exposed adapter
runtime peer and publication gaps: React/DOM are now exclusively runtime peers,
both adapters build before packing, compiler caches are excluded, and extracted
tarballs import together with the consumer's React. The unrelated npm package
named `reagent` was removed; the actual CLJS Maven Reagent dependency is retained.
This eliminated 106 unused Node packages. The committed tarball verifier checks
both entrypoints and dependency metadata, beyond workspace-only imports.

Proxx lint excludes generated compiler externs while retaining application rules
(0 errors, 238 existing calibrated warnings on current staging). Its full Node
suite was blocked by automatic approval review after HTTPS traffic targeted an
unverified private host. That execution was not retried. An early process-list
diagnostic failed, and a log tail was read before the parent clarified the
source-only boundary; no subsequent session access or stop was attempted.
There is no full-suite success claim. Fixture provider isolation is still needed.

Muse's current-main branch builds its four owned host targets with 0 compiler
warnings, executes 197 tests / 506 assertions successfully, and passes canonical
lint. Its latest hook/identity successor passes 198 tests / 517 assertions with
real Mongo. Actual emitted hooks execute in paths containing spaces, apostrophes,
command substitutions, and backticks; canonical Rheos records a UUIDv4 replacement
card while preserving the old archived projection and historical ledger bytes.
A fresh source checkout reproduced a missing generated namespace despite
warm builds passing. Moving the existing generation recipe into `prebuild` fixes
the cold path; all four targets then built in 51.960 seconds. The post-release
Claude emitter now refreshes active hooks for the actual checkout. Its stale Sol
application targets were retired against the accepted ownership boundary.
Review then found that direct host commands bypassed the root prebuild. The
actual configured OpenCode, MCP and Claude commands now run the shared generator
and pass in three isolated cold source checkouts (85.774 seconds, zero warnings).
The final full build, 198-test native-Mongo suite and compiled hook proof also pass.

Epiphany's
shallow-history test failures were resolved by fetching the history of the same
pinned checkout; its tests still emit the pre-existing SLF4J provider warning, and its lint alias
returns zero despite 66 warnings. Those warnings remain blocking findings.

## Remaining review boundaries

The final combined child revisions can change generated inputs. Regenerate the
manifests and refresh the root lockfile deliberately after those revisions are
promoted; then run `pnpm manifests:check` and a frozen install. The workspace card
must remain open until the whole-stack/browser/review obligations are supported
by their own evidence. Calliope's required append-only test and lint receipts are separate
child-owned changes for the parent integrator to publish.

Actual Codex, CodeRabbit, and eta-mu reviews are running on the Muse and Uxx
successors. The earlier Uxx eta-mu review approved its reviewed head but ran only
a `diff_stat` deterministic gate; that is distinct from the full local test and
artifact evidence above. The repository's native auto-merge action reports that
auto-merge is not enabled. Normal protected merge remains available after the
current head has clean checks and reviews; repository protection is not bypassed.
Later source reviews found no confirmed Uxx issues beyond the corrected parity
documentation, but the reviewer's dependency install was skipped with exit 125.
Root eta-mu source review also approves, while its CI deterministic commands
cannot find `bb` (exit 127). Those unavailable gates are not reported as passes.

See [workspace-obstacles.md](workspace-obstacles.md) for the recovery decisions,
remaining module limits, and what each recorded result actually proves.
