# Foresight

Foresight is a source-level constellation of independently owned repositories
and root-declared consolidation inputs. The root coordinates visibility and
explicit cross-repository operations; it does not replace each submodule's
package manager or quality gates.

`.agents/` and `eta/` are intentional consolidation inputs. `.agents/` carries
the canonical skill catalog in its own nested Git repository; `eta/` is a
root-owned Clojure agent harness. Their working originals may remain elsewhere
while this repository converges on a higher-quality source. Compatibility
copies are not removed implicitly.

See [`AGENTS.md`](AGENTS.md#repository-map-where-to-look) for the repository
map: what each child submodule owns and where to look for a given topic
before searching root-owned code.

## Setup

```sh
git submodule update --init
nbb scripts/workspace.clj inventory
```

NBB has no task registry, so commands name the script directly:

```sh
nbb scripts/workspace.clj report
nbb scripts/workspace.clj install --only eta-mu,uxx
nbb scripts/workspace.clj build --only eta-mu
nbb scripts/workspace.clj test --only eta-mu
nbb scripts/workspace.clj lint --only eta-mu
nbb scripts/workspace.clj jscpd --only muse
nbb scripts/evidence.clj validate
nbb scripts/evidence.clj list --only katamorph --kind unit,static
nbb scripts/evidence.clj run --only katamorph --kind static
nbb scripts/evidence.clj verify-receipts \
  --base <full-trusted-base-commit> --at <full-reviewed-head-commit>
```

`inventory` is read-only; `report` writes only root-generated artifacts.
Inventory and reports include direct `.gitmodules` repositories plus the
explicit `.agents` and `eta` consolidation roots. Inspection stays at each
declared root; nested packages and Git repositories are not promoted into
workspace projects.

Executable actions require `--only` or `--all`, run sequentially, and use only
action-eligible direct submodule root metadata. `--all` excludes consolidation
inputs, and explicitly selecting `.agents` or `eta` fails before execution.
Missing scripts, ambiguous managers, and missing frozen-install locks are
reported as unavailable rather than guessed.

`report` writes `reports/workspace.json` and `reports/workspace.md`. `jscpd`
is baseline reporting rather than a duplication gate; broken tooling still
returns a failure.

## Evidence gates

`config/quality-gates.edn` is the explicit map for child repositories whose
owning gates are not represented by an exact root `package.json` script. Every
entry cites the owning manifest or workflow and classifies the gate as static,
unit, integration, E2E, coverage, security, build, live smoke, or independent
review. The catalog also distinguishes locally runnable commands from
workflow-only and externally hosted gates. Catalog repository keys must be a
subset of the project model's actionable direct submodules. Foresight never
enters a child's nested package root; when the child exposes no root command,
the owning workflow stays explicit and unavailable to local root execution.

`scripts/evidence.clj` validates and runs only mapped commands for explicitly
selected direct submodules. It repeats workspace path and checkout-identity
checks immediately before and after every spawned process, rejects dirty or
moving checkouts, and binds promotion to one explicit target revision across
every required gate. Every `START` and `RESULT` retains the exact argument
vector, a SHA-256 identity for the raw gate catalog, and the owning source path
plus repository revision. A checkout that moves or becomes dirty during a gate
produces unavailable evidence with the observed revision, never a
revision-bound pass. The pure law checks consistency only; it is not promotion
authority. The effectful adapter requires the named reviewed root commit to be
the clean current `HEAD`, loads both the catalog and Receipt River from that
same Git object, and requires every result revision to equal the corresponding
gitlink in its tree. It rechecks the root boundary after all reads.

The runner appends each complete result to `.ημ/receipts.edn`. Schema-v2
receipts retain the actual host and `nbb`/Node adapter identity; older receipts
remain parseable append-only history but cannot attest a promotion. The adapter
currently requires Linux `/proc/self/fd` semantics. It holds a no-follow parent
directory descriptor, rejects symbolic or multiply linked ledger files, and
serializes cooperating writers with an exclusive sibling lock. A complete line
is bounded to 1 MiB, written through the held descriptor, size-checked, and
followed by file and directory `fsync`. Pre-write rejection releases the lock;
uncertainty after writing starts retains the lock as a quarantine marker for
manual adjudication. This protects the adapter boundary from pathname swaps,
partial writes, and cooperating races; it does not exclude an unrelated writer
that ignores the lock.

For immutable review, the adapter disables Git replacement objects, requires
regular non-executable blobs, hashes
their raw bytes, decodes strict UTF-8, and proves the reviewed ledger preserves
the trusted base ledger byte-for-byte before accepting whole appended lines.
The portable consistency law then requires exact receipt/result equality.
Editing a failed result or prior receipt into a shape-valid pass therefore
cannot reuse the reviewed history. Git supplies content-addressed integrity and
a review-authorized immutable snapshot, not producer authentication. A trusted
GitHub Check or DSSE/signing identity is a future strengthening if producer
authentication becomes a requirement; its acceptance contract is tracked in
[Foresight #57](https://github.com/open-hax/foresight/issues/57).

The local adapter's before/after checkout snapshots detect ordinary drift; they
do not prove safety against an adversary that swaps a pathname out and restores
it between those observations. Adversarial execution requires a trusted CI
attestation or an isolated exact-commit checkout/immutable source mount, tracked
in [Foresight #58](https://github.com/open-hax/foresight/issues/58).
Failed, blocked, unavailable, and not-applicable are separate outcomes; a
missing checkout, tool, credential, or host never becomes a pass. See
[`docs/specs/inflight-completion-and-knoxx-lift.md`](docs/specs/inflight-completion-and-knoxx-lift.md)
for the revision-bound promotion and test-tier contract.

Coverage results are reportable but cannot yet authorize automatic promotion.
A zero exit does not retain the machine-readable report, its digest, measured
values, or the repository-owned threshold and baseline. The versioned artifact
attestation needed to remove this fail-closed guard is tracked in
[Foresight #59](https://github.com/open-hax/foresight/issues/59).

## Project law

`src/foresight/project.cljc` is the portable Lisp/EDN declaration of the
Foresight constellation. It lists every direct repository and root-owned
consolidation input, records ownership and actionability, and names invariants
with the repository evidence from which each law was recovered.

`src/foresight/law/project.cljc` validates the portable declaration. Runtime
checkout facts such as symlink confinement and device/inode identity remain in
the NBB workspace adapter; the pure project law validates identity, ownership,
actionability, invariant references, and agreement with `.gitmodules`.

```sh
nbb scripts/project.clj repos
nbb scripts/project.clj show
nbb scripts/project.clj validate
nbb test/project_test.cljs
```

`.gitmodules` remains Git's checkout manifest. The project declaration is the
semantic inventory and contract: changing one without the other is visible
drift, not an implicit change in ownership or authority.

## Board

The root board uses canonical `openhax.kanban.edn`; `openhax.kanban.json` is a
compatibility mirror for the published JSON-only CLI:

```sh
eta-mu kanban count
eta-mu kanban list
```

All provenance ledgers are physically stored beneath `.ημ/`.
