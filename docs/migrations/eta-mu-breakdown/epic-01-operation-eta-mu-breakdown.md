# Epic E1 — Operation Eta Mu Breakdown

**Planning status:** proposed, grounded in a pinned mainline inspection on 2026-09-12.  
**Outcome owner:** Foresight suite migration. **Execution host:** yoga. **Development branch:** `device/yoga`.  
**IDs:** E1 and E1.01–E1.12 are identifiers local to this planning pack, not already-created GitHub or Rheos records.

## Outcome

A contributor can clone, install, build or assemble, test, lint, and use each extracted product without an ambient eta-mu or Knoxx working tree. Existing consumers retain the same capabilities using declared, immutable Git dependencies. Foresight can discover and verify the products as direct submodules, and maintainers cannot merge unreviewed or unverified changes into their main branches.

The migration changes package ownership and dependency transport. It does **not** finish the pi rewrite, rename existing namespaces, replace Sol's ledger implementation, rewrite authentication, or implement the entire document knowledge system. Those changes must not be smuggled into an extraction diff.

## Scope and observed baseline

| Source | Destination intent | Initial scope |
|---|---|---|
| `eta-mu/packages/kanban-orchestrator` | `open-hax/kanban-orchestrator`, `foresight/kanban-orchestrator` | Contract-data product slice |
| `eta-mu/packages/clio` | `open-hax/clio`, `foresight/clio` | Event-sourcing source library and CLI |
| `eta-mu/packages/chat-ui` | `open-hax/chat-ui`, `foresight/chat-ui` | Chat components and session adapters |
| `eta-mu/packages/axxium` | Reconcile existing `open-hax/axxium`; add `foresight/axxium` | Identity/auth service |
| `eta-mu/packages/rheos` | `open-hax/rheos`, `foresight/rheos` | Current document/card mutations, board, CLI/API/UI |
| `eta-mu/packages/session-mycology` | `open-hax/session-mycology`, matching Foresight path | Reflection/lesson event library |
| `eta-mu/packages/sol` | `open-hax/sol`, `foresight/sol` | Runtime/provider/MCP surface |
| `knoxx/ingestion` | Working name **Osmos**, subject to repository-name preflight | Whole existing JVM service, including current translation/audio integrations |

The proposed ingestion name is not reserved. Preserve `kms-ingestion.*`, environment variables, routes, stored identifiers, and deployment compatibility during the mechanical transplant. A repository rename does not require a namespace rename.

Pinned main revisions are in `data/revisions.json`. Foresight's recorded eta-mu, Knoxx, and Epiphany gitlinks are older/different than their inspected mainline heads. An implementation run must choose and record its exact donor revisions again; it must not call the old gitlink “latest main.” No yoga worktree or open-branch survey was performed in this planning run.

## Non-negotiable migration laws

1. **Copy first. Do not move, remove, rewrite, or update donor package contents during receiver bootstrap.** Manifest, script, and packaging corrections happen in the copies. Read/fetch operations must not change donor working files or discard uncommitted work.
2. Donor deletion requires a per-package zero-live-consumer proof, including source paths, runtime launch paths, contracts, tests, workflows, and documentation used as executable instructions. Immutable historical commits remain available; history is not rewritten.
3. Every interrepository dependency records repository identity, full commit SHA, and package/subdirectory when needed. A branch name is not the installation identity.
4. An independently buildable child may fetch declared dependencies. It must not obtain undeclared inputs from neighboring Foresight checkouts, parent directories, a developer's global classpath, or stale output directories.
5. Preserve behavior, package identities, public exports, schema identities, executable names, assets, and applicable licensing/attribution. Record intentional differences from the copied tree; never apply the donor root's license indiscriminately to all children.
6. A source dependency and a built npm dependency are different products. Verify the form the consumer actually installs. Success inside the donor workspace is not standalone-install evidence.
7. A missing test, unavailable service, skipped reviewer, or zero selected test count is not a pass. Source-only and data-only products have honest assembly/conformance gates, not fake compilation.
8. Existing destination repositories and local directories are reconciled, never overwritten or force-pushed. Public publishing happens only after the staged content and ignore rules have been inspected for secrets and noise.
9. Record intended work, attempted commands, observed results, accepted review, Git persistence, and delivered artifacts separately. A GitHub UI state or agent declaration is not sufficient acceptance evidence.

## Specific findings that shape this epic

Source identifiers below resolve to revision-bound URLs in `data/sources.json`.

- **Two dependency mechanisms:** Rheos uses `@open-hax/protocols = workspace:*` and also compiles `../protocols/src` plus `../chat-ui/src`. Sol uses `:local/root` for eta-mu source and Turn Processor. Changing package.json alone leaves the Clojure classpath unchanged. [rheos.package, rheos.deps, sol.deps]
- **Source packaging gap:** Protocols' npm `files` list contains built `dist` and types, not source; a `packages/protocols/deps.edn` read returned 404. Do not assume either a Git-installed npm package or a bare `:deps/root` selector provides the required source manifest. [protocols.package]
- **Mixed dependency cycle:** eta-mu's npm package depends on Sol, while Sol's Clojure dependency selects eta-mu source. This is an architectural cycle across dependency mechanisms, not proof of a cycle within a single resolver. Preserve it during extraction unless exact closure analysis proves an additional change necessary. [eta-cli.package, sol.deps]
- **Shared tooling:** Sol invokes a root contract guard and shared kondo-config; Session Mycology invokes a root extern-boundary guard also used by Receipt River. Do not delete shared tooling when the first client moves. [sol.package, sol.kondo, mycology.package, receipt.package]
- **Baseline coverage gap:** Session Mycology declares a test script but is not an explicit suite in the inspected root `scripts/test.bb`. Run every extracted package directly. [mycology.package, eta.test]
- **Axxium already exists:** inspect both histories before deciding which changes to import. The two inspected manifests are similar but not identical; this does not prove whole-tree equivalence. [axxium.package, axxium.existing]
- **Ingestion is not configuration-independent:** it discovers parent `contracts`, has configurable Knoxx/OpenPlanner/Proxx and infrastructure endpoints, and its integration alias requires live OpenPlanner and a database. Its isolated Docker context also needs investigation: Dockerfile copies `resources`, but no such directory appears in the pinned ingestion tree. [ingestion.contracts, ingestion.config, ingestion.deps, ingestion.docker, ingestion.tree]
- **Documentation is already out of sync:** Rheos's README says `build` omits the app; its manifest includes `app` and `github-sync`. Use executable descriptors for the baseline and repair the documentation. [rheos.readme, rheos.package]

## Story sequence

The stories define independently reviewable outcomes. The task paragraphs are an implementation plan, not immutable acceptance criteria. Each correction starts with its relevant regression fixture.

### E1.01 — Maintainer can inspect the exact migration surface before any write

**Acceptance.** A machine-readable manifest identifies each donor commit/tree/path, destination, current local checkout state, and existing remote history. It records both direct and transitive dependency mechanisms and classifies every edge as observed, reported, inferred, or unresolved. Current main, Foresight gitlinks, and local working state are distinct. All affected remaining packages, shared scripts, contracts, and workflows have an owner and disposition.

**Tasks.** Read tracked package manifests and workspace configuration; parse `deps.edn`, `shadow-cljs.edn`, `nbb.edn`, imports/requires, include paths, lint configs, scripts, Docker/Compose files, workflow matrices, exports and resource roots. Resolve source namespace ownership rather than using name frequency as dependency proof. Detect strongly connected components by edge type. Check all proposed remotes; compare existing Axxium and donor content without discarding newer work. Count tracked source bytes/lines only if a code-share claim is needed.

**Failing fixtures first.** A sibling-only CLJS import invisible to package.json must appear in the graph. A runtime URL must not become a compile edge. An existing repo or a dirty destination must prevent creation/overwrite. An unavailable read must not become “no dependencies.”

**Evidence.** Exact input revisions, scan coverage, source locations for edges, unresolved references, and destination preflight. The observation graph in this pack is the seed, not the exhaustive closure proof.

### E1.02 — Contributor receives faithful, clean copies without donor modification

**Acceptance.** All eight receiver trees are copied from the chosen source revisions; donor paths remain byte-for-byte unchanged. A manifest records every imported file and intentional exclusion/addition. No `.git` metadata, credentials, runtime data, installed modules, build outputs or nested worktrees are unintentionally staged. Existing receiver trees remain intact.

**Tasks.** Prefer a tracked-tree export from a fixed commit over copying an arbitrary dirty working directory. Export into a fresh destination. Apply receiver-root ignore rules based on the donor `.gitignore`; anchor output directories appropriately, preserving genuine source directories such as deliberately retained `lib` or `build` paths. Copy relevant licenses and portable lint hooks where required, retaining their origin. Inspect `git status`, staged diff, file modes, symlinks, secret scan, and `.env.example` treatment before the first public push. A tracked donor `target` directory is not automatically a valid source input for the new repository.

**Failing fixtures first.** Plant a local `.env`, `node_modules`, `target`, and nested checkout; none may enter the published tree. A tracked source directory named `build` must not be lost merely because its name resembles output. A donor hash change aborts the copy acceptance.

**Evidence.** Before/after donor tree comparison, receiver file map, exclusion reasons, staged inspection and secret-scan result.

### E1.03 — Each receiver can resolve its bootstrap dependencies from immutable donor revisions

**Acceptance.** Every necessary donor dependency is explicit, pinned, and available outside the donor workspace. JS dependencies, CLJ/CLJS source, Maven libraries, build tooling, contract data, and runtime configuration are all accounted for. A clean install/build fails rather than silently falling back to neighboring copies. Every receiver command is exercised individually; failures are recorded as baseline defects or extraction regressions.

**Tasks.** Replace receiver `workspace:*`, parent paths, and local roots with appropriate immutable selectors. Use subdirectory-aware Git coordinates, not eta-mu's root package. Run the small Git-subdirectory canary on the pinned package manager first. Add portable dependency metadata only in receiver trees. Where an unchanged donor lacks consumable metadata (notably Protocols), use an explicit, receiver-owned bootstrap adapter that fetches and verifies the donor SHA, stages only declared source/tooling inputs in an ignored dependency area, and supplies their classpath/JS requirements. Its lock, selected paths and verification must be recorded. This is a temporary declared Git dependency, not an undeclared ambient sibling checkout; do not invent resolver support or modify the donor to hide the problem.

Validate actual packed/installed files and launchers. A `prepublishOnly` or `build` script is not evidence that the chosen Git installation ran the needed compilation. Retain required source files for source consumers. Test Clio as an installed CLI and library, Chat UI as consumed by Rheos, and compiler compatibility without opportunistic React/Malli upgrades.

**Failing fixtures first.** Run a consumer with no donor directory, no global tool/classpath, and no old dist. A dist-only Protocols package must not count as satisfying its source consumer. A required Git input that is unavailable must fail with its exact coordinate.

**Evidence.** Per-package cold install, resolved dependency basis/lock, packed file inventory, build/assembly/test/lint logs, test counts, and reproducible failure notes. “Should work first try” is a hypothesis to test, not a release condition.

### E1.04 — Each product has an independently addressable Git history and development branch

**Acceptance.** Every receiver has a verified open-hax remote, appropriate public visibility, a valid main branch, and a pushed `device/yoga` branch. Axxium retains its existing history; other existing remotes follow the same reconciliation rule. Public package names and CLI names remain stable. `private: true` in an npm manifest is not changed merely to make its GitHub repository public.

**Tasks.** For genuinely new repositories, initialize a clean receiver with main, commit the inspected project state with donor provenance, create the remote, and push; then create/push `device/yoga`. The requested `gh repo create open-hax/$TRANSPLANTED_PACKAGE --public --source=. --remote=origin --push` applies only after existence and staged-content checks. For existing repos, use a normal branch/import PR based on their history. Record the imported commit, remote repository ID, origin URL, and donor relationship. Apply initial repository policy before autonomous merging is possible.

**Failing fixtures first.** Re-running initialization must not create duplicates, change origin silently, lose history, or push directly to an existing protected main. A local directory name alone cannot establish destination identity.

**Evidence.** Remote read-back of repository identity, visibility, branch tips, published commit, and reconciled history.

### E1.05 — Foresight discovers each new product as a pinned, actionable child

**Acceptance.** `.gitmodules` and the index contain matching path/URL and gitlink entries. Each gitlink is remotely reachable and points to a tested commit. `branch = device/yoga` is recorded as requested development tracking metadata, not treated as a reproducible dependency pin. Fresh Foresight checkout obtains exactly the recorded commits without `update --remote`.

**Tasks.** Register existing local receiver repositories through Git's submodule operation, not by editing `.gitmodules` alone. Reconcile paths before adding; absorb Git directories as appropriate. Update `src/foresight/project.cljc`, its law/tests, the workspace inventory, and `config/quality-gates.edn`. Inspect `scripts/workspace.clj`, `scripts/evidence.clj`, and the public README command surface. Supply package-owned root commands or explicit workflow-only gates for products without root npm scripts. Keep child-local development usable without Foresight.

**Failing fixtures first.** A `.gitmodules` stanza without a gitlink, an unreachable commit, a renamed mismatched remote, or a project registry omission prevents admission. A floating branch advancement cannot silently change the recorded workspace.

**Evidence.** Clean submodule clone, identity checks, project-law validation, direct-child inventory and exact-revision evidence catalog.

### E1.06 — Receivers switch transplant-to-transplant edges one at a time

**Acceptance.** Each changed dependency points to its new repository's tested full SHA, and the consumer still passes clean install/build/test/packaging checks. All intended public behavior is preserved. Records identify the old coordinate, new coordinate, affected consumer, and evidence.

**Tasks.** Start with low-coupling packages: Clio, Chat UI, Session Mycology, and reconciled Axxium can proceed independently after their own tooling/packaging checks. Assemble and validate the orchestrator separately as contract data; its host integration follows when Sol/Rheos are ready. Switch Rheos's Chat UI source dependency after Chat UI exposes a consumable source contract. Stage Sol with its remaining eta-mu/Turn Processor dependencies and their actual source closure. Do not perform a speculative architecture refactor just to draw a DAG. Separate package-manager DAGs from runtime and source-consumption relationships.

**Failing fixtures first.** Removing the old donor's selected source path from an isolated consumer must not change a successful result. A dependency update that resolves a newer/different commit or drops source/assets fails even if a local checkout happens to work.

**Evidence.** Before/after typed graph, locked resolver output, changed source paths, fresh consumer logs, and per-edge rollback coordinate.

### E1.07 — Remaining eta-mu consumers use the new products without donor copies

**Acceptance.** Eta-mu's remaining packages install/use the new product repositories and do not import the soon-to-be-deleted donor targets. Source consumption and CLI launch paths are checked in addition to npm entries. The existing `eta-mu` command still reaches Rheos, Sol, and Session Mycology behavior. Shared tooling needed by remaining packages is retained or lawfully replaced.

**Tasks.** Change the eta-mu CLI's Rheos/Sol/Mycology npm references; replace its Mycology shadow source path. Audit actual command launchers and source requires. Repeat for every additional consumer found by E1.01. Rewrite root test/lint/build selectors and integration fixtures. Enforce remote dependency selection in the verification environment so workspace links cannot mask incomplete migration. Sol may keep a declared Git dependency on remaining eta-mu source; historical donor commits being present in a Git cache is not itself a forbidden live dependency on deleted targets.

**Failing fixtures first.** A clean consumer test with all extraction-target donor paths unavailable must still run. A stale `../session-mycology/src/cljs`, root dispatch path, or packed launcher reference must be detected. A zero-match pnpm filter must not become a green suite.

**Evidence.** Exhaustive inbound edge disposition, command smoke tests, standalone eta-mu checks, and no-selected-input-from-retired-targets report.

### E1.08 — Osmos preserves ingestion behavior as an independent JVM service

**Acceptance.** The extracted JVM service runs from its own root and image, with explicit configuration/contracts/resources. Unit tests need no live services; integration tests demonstrate their documented live dependencies. Existing Knoxx ingestion/translation/audio clients continue to work. The service has a clear root README and executable operator commands.

**Tasks.** Preserve current namespaces and APIs. Declare how contract bundles and migrations are obtained; replace implicit parent-directory contract discovery in the extracted deployment with a tested explicit binding. Inventory database/schema ownership, mounted paths, image build context, health checks, upload limits, translation callbacks/config reads, audio agent interactions, OpenPlanner sink, Proxx adapter and optional backends. Resolve the missing-resources Docker input based on actual required files or a justified recipe correction. Update Knoxx and Services deployment references in coordinated PRs. Do not turn this extraction into a database ownership migration.

**Failing fixtures first.** A fresh isolated Docker context with no parent resources must reveal missing inputs. An incorrect contract root, missing sink, or Knoxx configuration outage must not be mistaken for successful ingestion. Repeated delivery must preserve the existing dedup/retry contract; add characterization tests before altering it.

**Evidence.** Unit/integration logs, image digest, health/API smoke, explicit service contract configuration, consumer compatibility and documented rollback image/config.

### E1.09 — Every child has honest quality gates and self-contained agent guidance

**Acceptance.** Build/assembly, tests, lint, coverage, and applicable integration/E2E gates have stable names, real selection counts, retained reports and exact-revision identity. Every child has local `AGENTS.md` and accurate README commands; neither depends on an inaccessible former parent document. CI does not require donor checkout layout accidentally.

**Tasks.** Migrate dedicated jobs and factor mixed workflows carefully. Keep shared test/review infrastructure required by eta-mu. For contract-only orchestrator, implement EDN parsing, schema/reference conformance, and host-load/MCP integration instead of a fake compile. For source-only Clio, make assembly/packaging plus BB/NBB/Shadow consumer tests the relevant build contract. Create a measured coverage baseline and repository-owned policy before ratcheting; data-only coverage can be explicitly not-applicable while contract conformance is still required. Preserve safe credential boundaries for untrusted PRs and mutation harnesses. Build the Rheos browser app and smoke actual navigation, not only its server. Carry lint hooks and ignore rules into the new root.

**Failing fixtures first.** A skipped/missing suite, omitted browser bundle, malformed contract, zero test count, changed locked dependency, or coverage-report loss must stop the relevant gate. AGENTS/README command examples are checked against real scripts or tested equivalents.

**Evidence.** Workflows, local commands, reports with digests, test counts, coverage policy, lint output, browser screenshots where applicable, and agent-guidance review.

### E1.10 — Unreviewed changes cannot merge during the reviewer-start window

**Acceptance.** Main requires PRs, required build/test/lint/conformance results, resolved review conversations, and completed successful CodeRabbit review evidence for the exact head. Review absence, queueing, error, rate-limit, skipped review, stale-head evidence, or an unexpected producer cannot authorize merge. The policy does not depend on obtaining a second human approval from a solo maintainer.

**Tasks.** Discover actual emitted CodeRabbit check/status names and trusted app identity; do not guess a check name. Require an always-present, trusted review-completion gate if the provider's outward status cannot itself establish exact-head completion. Prevent a PR from modifying the trusted gate to self-authorize. Validate required child jobs rather than allowing an aggregate `always()` job to return success on skipped dependencies. Configure resolved conversations separately. Scope agent permissions so bypass, force push, policy edits, and direct main writes are not their ordinary execution path. Verify rules through API read-back and controlled PR fixtures.

CodeRabbit's inspected configuration exposes `reviews.review_progress` (canonical outward review status), `reviews.fail_commit_status` (fail outward status on review errors), and an optional `reviews.request_changes_workflow` approval feature. The first two can support this policy; the third is not a prerequisite for the requested zero-extra-human-approval workflow. Inspect behavior, not just YAML presence. See official references in `data/sources.json`.

**Failing fixtures first.** Fresh PR before CodeRabbit starts; successful old SHA followed by a new push; all checks green but one unresolved thread; no review due to errors/rate limits; spoofed check producer; a skipped test hidden by an aggregate; stale base requiring renewed verification. Every case remains non-mergeable.

**Evidence.** Policy JSON/read-back, app/check identity, negative and positive merge-eligibility tests, and exact-head reviewer state. No auto-merge enablement before these facts are established.

### E1.11 — Donor packages and obsolete workflow paths are retired only after the cutover proof

**Acceptance.** Each donor directory is deleted in its own reviewable step only after every live consumer has a verified replacement. Remaining eta-mu and Knoxx test/build behavior is clean after a new install with caches and old outputs excluded. Package-specific obsolete workflows disappear; shared workflows remain valid. Historical Git objects and provenance are preserved.

**Tasks.** Re-run the exhaustive reference scan, classify remaining hits (live, historical, examples, migration records), and test actual resolver/launcher closure. Obtain replacement CI evidence before deleting old CI. Remove donor package directories and their now-obsolete package-local metadata only after acceptance. Update workspace lockfiles without unrelated mass upgrades. Produce final graph and dependency disposition report. Promote child PRs, then update Foresight gitlinks to their final tested commits. Rollback is a normal revert/re-pin, not force-pushing or editing history.

**Failing fixtures first.** Inject one still-live import, runtime path, workflow selector or package dependency into a temporary fixture; retirement must be rejected. Historical quoted references must be labeled rather than blindly rewritten or treated as live imports.

**Evidence.** Zero-live-inbound proof per target, final clean builds/tests, replacement workflow runs, final commit/gitlink identities, and a tested re-pin/revert procedure.

### E1.12 — Contributors can find the products and their migration documentation

**Acceptance.** Foresight scripts, root architecture/project records, quality catalog, and documentation describe the new ownership. Relevant `eta-mu/docs` material is transferred through an explicit path map into `foresight/docs`; product-local operational documentation is available in each child. No unresolved internal links are introduced, and outdated documents are not silently promoted into current authority.

**Tasks.** Inventory docs referring to extracted products and assign each a destination/disposition. Transfer still-relevant suite/cross-project docs to Foresight, rewriting relative links and recording source revision/path. Give children their own README/AGENTS/API/development references so independent users need not reverse-engineer the old monorepo. Mark contradictory/stale material for E2's retirement workflow. Preserve immutable receipts as history, not rewritten guidance. Link migration records to the final repositories and verification evidence.

**Failing fixtures first.** A moved document's relative source link, inbound Markdown link, or fragment anchor must resolve at the destination. A package README must not retain `pnpm -C packages/<old-name>` as its standalone quickstart. A known stale build claim cannot be reissued as verified documentation.

**Evidence.** Source-to-destination path map, link-check report, independent README walkthrough and final migration dossier.

## Verification matrix

| Product | Existing command surface to characterize | Additional extraction proof |
|---|---|---|
| Clio | `pnpm test` (BB, NBB, Shadow); `pnpm lint` | Installed CLI from unrelated cwd; source/library consumer; native dependency portability; package contents |
| Chat UI | `pnpm build`, `pnpm build:app`, `pnpm test`, `pnpm lint:kondo` | Standalone demo plus actual Rheos source-consumer/browser check |
| Axxium | `pnpm build`, `pnpm test`, `pnpm lint:kondo`, `pnpm boundary:check` | Existing repo reconciliation; real test discovery; PostgreSQL health/auth smoke |
| Rheos | `pnpm build` (server, CLI, GitHub sync, app), `pnpm test`, `pnpm lint` | Installed CLI, served web assets, persisted card operation and chat adapter compatibility |
| Session Mycology | `pnpm build`, `pnpm test`, `pnpm lint:kondo` | Guard relocated or pinned; exact event API; eta-mu session command source cutover |
| Sol | `pnpm build`, `pnpm test` (Shadow autorun), lint scripts; mutation aliases in deps.edn | Provider/MCP contracts; declared donor source closure; safe standalone premerge harness |
| Kanban Orchestrator | No package scripts in inspected manifest | Contract assembly/schema/reference validation; external contract-root loading and MCP behavior |
| Ingestion / proposed Osmos | `clojure -M:test`, `clojure -M:integration`, `clojure -M:uberjar` | Explicit contracts/resources, Docker build, health, Knoxx/OpenPlanner integration; add owned lint/coverage commands |

These commands were **read**, not executed here. Missing commands are implementation work, not invented baseline passes. Use fresh disposable clones, isolated HOME/tool caches and package-manager stores for cold gates; retain installed dependency versions and allowed build-script decisions. Run faster cached lanes separately and label them accurately.

## Mapping to the requested operation

| User step | Covered by |
|---|---|
| 1: identify affected packages | E1.01 |
| 2: copy without donor edits | E1.02 |
| 3: donor-SHA bootstrap references | E1.03 |
| 4: each receiver's tests/build | E1.03, E1.08, E1.09 |
| 5: ignore/stage/commit/new remotes/device branches/submodules | E1.02, E1.04, E1.05 |
| 6: transplant edges one by one | E1.06 |
| 7: remaining eta-mu consumers | E1.07 |
| 8: clean installs/builds/tests | E1.03, E1.07, E1.11 |
| 9: obsolete donor workflows | E1.09, E1.11 |
| 10: build/test/coverage/lint/integration/E2E CI | E1.09 |
| 11: AGENTS.md | E1.09 |
| 12: Foresight scripts/docs | E1.05, E1.12 |
| 13: lift relevant donor docs | E1.12 and E2 |
| 14: repository protections/review completion | E1.10 |

## Completion and rollback

Completion is an evidence-backed state, not the number of repositories created. All eight products have independent roots and tested remote revisions; each live relationship is preserved through an explicit coordinate/configuration contract; donor retirement is proven; Foresight has verified gitlinks and gates; package documentation and reviewer policies are usable.

A failed edge switch is rolled back to the previous tested coordinate. A failed donor removal is reverted while the new repositories remain intact. A failed ingestion deployment is rolled back using the prior image/configuration pair. No rollback rewrites event history, discards uncommitted work, erases an existing remote, or mutates an already-published source snapshot.
