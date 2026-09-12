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
- Generation reads policy, source metadata, tracked child package manifests, and
  Clojure dependency inputs. Input digests and duplicate identities remain
  inspectable. Conflicting selected versions require an explicit explanation.
- A missing child Git boundary cannot silently inherit the parent's Git inventory.
  Existing workspace selection/ownership tests remain in the root test command.
- Child library aliases do not copy build/test aliases into a different working
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

The root command runs 111 Clojure tests / 501 assertions plus the Node repository
census assertion program, with no failures. The compiled consumer builds, root
lint is clean, S3 put/get works, real 384-dimensional embeddings pass the HTTP
smoke, and a frozen offline pnpm install succeeds. Model weights remain an
explicit cached capability and are not downloaded by normal root tests.

The recovery evidence also records unchanged child builds/tests and their
failures. A zero exit or a green subset is not a whole-stack acceptance signal.
Known current findings include a Katamorph namespace/var compiler warning,
Uxx React resolution/identity failures, Proxx lint walking generated Shadow
externs, and Muse exporting an unavailable application namespace. Epiphany's
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
