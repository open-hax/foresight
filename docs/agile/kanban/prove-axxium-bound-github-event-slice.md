---
uuid: "e07b6592-c09f-4efe-acba-27865b66064d"
title: "Prove an Axxium-bound GitHub event slice"
status: "todo"
priority: "P1"
labels: ["identity", "event-sourcing", "github", "katamorph", "knoxx"]
created_at: "2026-08-31T21:15:26.931Z"
write-id: "1788210962822-0.gqtpmbeg998s643z74"
---

# Prove an Axxium-bound GitHub event slice

Turn the Axxium Event Fabric design into one complete, revision-bound path before
adding Google Drive or Discord breadth. GitHub supplies the first provider signal
because its App installation, signed delivery, hydration, delivery-recovery, and
repository-object boundaries exercise the shared identity and evidence laws.

## Vertical slice

```text
signed GitHub App fixture
  -> verified delivery signal
  -> eta-mu classification
  -> Axxium principal + installation + object + stream bindings
  -> hydrated GitHub object observation
  -> Clio-compatible ND-EDN event-ledger segment
  -> immutable Google Drive mirror receipt
  -> Knoxx object/ledger graph projection
  -> revision-bound test-result record
  -> replay-equivalent projection
```

## Acceptance

- Katamorph validates a data-only GitHub actor/source/store/action resource pack;
  no GitHub SDK object crosses the portable-resource boundary.
- Eta-mu verifies the fixture signature, retains provider delivery identity, and
  emits distinct raw-delivery, hydrated-object, and coverage records.
- Axxium fixtures bind the user or service principal, GitHub App installation,
  repository/object identity, and event stream without deriving identity from a
  username, email, path, filename, or content hash.
- Re-admitting the same provider delivery is idempotent under the declared
  profile while separate physical records retain unique `:record/id` values.
- The event ledger writes one complete EDN map per nonblank line, seals an
  immutable segment, advances a manifest by expected position, and detects a
  segment/hash divergence rather than overwriting it.
- The Drive mirror copies the sealed segment and records source locator, Drive
  object ID, content hash, stream position range, and coverage. Drive is not used
  as a concurrent mutable append file.
- Knoxx projects the repository, object, ledger, segment, and causal relations;
  deleting and rebuilding the projection from the same admitted history yields
  the same Axxium identities and graph edges.
- The test runner appends started and terminal records for success, failure,
  timeout, cancellation, and spawn error. The terminal record identifies the
  exact repository commit/tree, dirty state, target input hash, dependency
  closure, environment, outcome, and artifact hashes.
- Unit/law tests cover identity, admission, duplicate delivery, malformed
  signature, missing permission, partial coverage, segment divergence, and
  replay. One integration test crosses all repositories through an exact pinned
  revision set.
- No unavailable, skipped, rate-limited, or not-applicable check is represented
  as a pass.

## Current bounded artifacts

- `open-hax/foresight#69` freezes authority, identity, ledger, mirroring, tagging,
  provider-interaction, test-evidence, Proxx, OAuth, and projection laws.
- `open-hax/katamorph#27` declares the first portable GitHub interaction
  resources using existing resource kinds.
- `open-hax/knoxx#294` declares the disabled Axxium-bound GitHub source, ND-EDN
  ledger, immutable Drive mirror, and rebuildable graph projection.
- `open-hax/eta-mu#206` is the existing GitHub webhook/event classifier lane.
- `open-hax/eta-mu#248` carries the historical event-identity collision law that
  this slice must preserve.

## Explicit boundary

This card does not claim that the connected Drive corpus is recursively tagged,
that live provider credentials are configured, or that automatic mirroring is
running. Those claims require admitted coverage records from the completed
vertical slice. Discord history, general Drive indexing, Proxx policy-kernel
extraction, and the Electron client follow only after this path is proven.

---
This card is the first executable child of the Axxium Event Fabric design. It
remains `todo` until the three bounded contract/design PRs are review-clean and
the exact provider fixture revisions are pinned.
