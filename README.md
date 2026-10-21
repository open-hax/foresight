# Foresight

Foresight is a software suite of independently useful products and specialized
views over shared data. The root owns common/core abstractions, portable `.cljc`,
suite documentation, and cross-package integration. Child repositories retain
local ownership, independent builds, package-manager policy, and quality gates.

The extraction registrations and their exact pinned revisions are documented in
[Operation Eta Mu Breakdown](docs/migrations/eta-mu-breakdown/README.md).
Registration is distinct from standalone acceptance: the
[acceptance register](docs/migrations/eta-mu-breakdown/acceptance.md) records
outstanding build, consumer, review-policy, and documentation evidence. Update
that register and the routing map when a module's acceptance lands.

## New actor: start here

A capable actor should be able to route work from the checkout without prior
chat or operator memory. Start by asking Foresight for its current semantic
view rather than searching every child repository by hand:

```sh
git submodule update --init
nbb scripts/project.clj validate
nbb scripts/project.clj guide
nbb scripts/workspace.clj inventory
```

`guide` is generated from `src/foresight/project.cljc`. It covers direct
repositories and root-native components, shows the default work/verification
loop, and points at revision-bound examples of practices Foresight has learned
from Git history. After routing, enter the likely owning repository and read its
local `AGENTS.md` when present, then `README.md`, `ROADMAP.md`, process/ADR
records, and the code/tests that own the behavior. Local evidence controls local
facts; a Foresight routing role is not a grant of cross-repository authority.

See [`CONTRIBUTING.md`](CONTRIBUTING.md) for the short contribution contract and
[`docs/architecture/self-documentation.md`](docs/architecture/self-documentation.md)
for how successful local practice is recovered, classified, and explicitly
promoted instead of copied into root law by accident.

`.agents/`, `eta/` and `clobber/` are intentional consolidation inputs.
`.agents/` carries the canonical skill catalog in its own nested Git repository;
`eta/` is a root-owned Clojure agent harness; `clobber/` is the recovered
pm2-clj/clobber PM2 DSL (see `clobber/PROVENANCE.md`). Their working originals may remain elsewhere
while this repository converges on a higher-quality source. Compatibility
copies are not removed implicitly.

See [`AGENTS.md`](AGENTS.md#repository-map-where-to-look) for the repository
map: what each child submodule owns and where to look for a given topic
before searching root-owned code.

## Workspace setup

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
explicit `.agents`, `eta` and `clobber` consolidation roots. Inspection stays at each
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

The Receipt River ledger is segmented. `.ημ/receipts.edn` is the legacy,
frozen first segment; every new receipt is its own immutable file under
`.ημ/receipts/` (`<UTC timestamp>-<random id>.edn`, exactly one
newline-terminated record). The ledger is the merge of all segments. New
records require an RFC 3339 `:ts` naming a real instant; records with such an
instant order by UTC time, then canonical record identity (SHA-256). Retained
historical records keep their original `:ts`: values naming no real instant
sort first by their canonical printed timestamp, then record identity.
Parallel branches add disjoint files instead of editing the same lines, and a
diverged fork and org merge without conflicts.
The runner writes each complete result as a new segment. Schema-v2 receipts
retain the actual host and `nbb`/Node adapter identity. Older receipts remain
immutable historical records and cannot attest a current promotion;
`verify-receipts` applies the current envelope law only to records that are new
relative to the reviewed base.

The adapter currently requires Linux `/proc/self/fd` semantics. Gate execution also requires
Python 3.9+ with Linux subreaper, pidfd and waitid support. A fresh gate adapter
retains every child identity, including descendants that create another session,
and binds its lifetime to the calling receipt process. Gate stdout/stderr remain
inherited; incomplete or interrupted custody quarantines the receipt reservation.
The existing five-second child-cleanup bound does not impose a gate runtime cap.
The append adapter holds a
no-follow parent directory descriptor, rejects symbolic, multiply linked or
executable ledger files (Git would record an executable file as a `100755`
blob, which immutable review rejects), and serializes semantic writers in one checkout on
`.ημ/receipts/.receipt-river.writer.lock` for the whole reserve, gate and
write sequence. A concurrent writer fails closed instead of waiting. A writer
lock left by a crashed process may be a quarantine marker for ledger
uncertainty, so an operator must verify and adjudicate the ledger before
removing it by hand; only a lock whose setup fails before any gate runs is
removed automatically. Each writer reserves
its own new segment with `O_EXCL` and a sibling lock for that segment; a
reservation that ends before writing removes its empty segment. A retained
lock (see below) quarantines the whole ledger. The rest of the working ledger
is revalidated against `HEAD` immediately before the receipt is written, and
compared with the pre-gate snapshot again after it is written or when a
gate aborts before writing. The writer lock is verified around the write. The
reserved segment's path and bytes are revalidated after its descriptor is
closed and again before each lock is released. The whole-ledger comparison
finishes while the writer lock is still held; after release only the run's
own reserved segment is rechecked, so a later writer's new segment is not
mistaken for tampering. A mismatch there writes the durable
`.ημ/.receipt-river.quarantine` marker, which a later writer that already owns
the writer-lock path cannot remove on its release. A
complete line is bounded to 1 MiB, written through the held descriptor,
size-checked, and followed by file and directory `fsync`. Pre-write rejection
releases the lock, unless the working ledger changed while the gate ran; that
change, a change after the write, and any uncertainty after writing starts
retain the lock as a quarantine marker for manual adjudication. A retained lock is verified by
path identity first; if gate code unlinked or replaced it, a fresh marker is
created exclusively at the same path. If the segment directory itself was
renamed or replaced while the gate ran, every lock is reachable only through
the renamed directory, so the adapter also creates
`.ημ/.receipt-river.quarantine` through a held `.ημ` descriptor; its presence
closes the working ledger until it is adjudicated. This protects the adapter boundary
from pathname swaps, partial writes, and cooperating races; it does not exclude
an unrelated writer that ignores the lock.

For immutable review, the adapter disables Git replacement objects, requires
regular non-executable blobs, hashes
their raw bytes, decodes strict UTF-8, and proves the reviewed ledger extends
the trusted base ledger as an event ledger: every base segment file is still
present byte-for-byte, every base record is still present somewhere in the
merged ledger, the legacy file only ever loses whole lines (no new record, no
rewrite or reordering), and no record appears twice. Records may move out of the legacy file into segments; that is how a
fork and its org reconcile histories that split inside the legacy file. The
current receipt-envelope and evidence-result laws apply only to the new
records. Historical records remain evidence even when their envelopes predate
the current schema. Secure append applies the same boundary against the
committed `HEAD` ledger: it validates the whole working ledger before running a
gate, then writes the receipt into a newly created (`O_EXCL`) segment. Rewrites,
deletions, legacy appends, and
malformed new records still fail closed. Summary totals describe the entire
ledger; appended counts describe the records subject to current validation.
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
nbb scripts/project.clj guide
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
