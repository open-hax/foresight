# Knoxx knowledge-worker event chain

Status: implementation draft  
Date: 2026-08-31  
Anchor card: `abdd5a2d-dbad-46a0-ba3f-2149dfeafbcf` — Prove translation generation and SME review independently

## Intent

Prove one end-to-end knowledge-work path before generalizing it:

```text
Git/file fact
  -> typed Katamorph event
  -> Knoxx trigger/action
  -> translation candidate
  -> human browser review
  -> immutable review facts
  -> approved publication
  -> reusable knowledge-worker evidence
```

Markdown is the human editing surface. Katamorph resource identities and Malli
schemas define the portable shapes. Rheos turns admitted file changes into
typed events. Knoxx executes the knowledge-work actions. Sol is a compatible
consumer, not a second source of contract truth. Services owns the production
deployment declarations and verification policy.

## Recovered facts

- Knoxx already has a deterministic, cleanup-safe browser tour for its
  translation split-review workflow. It exercises correction, immutable review
  history, rejection, approval, publication materialization, approval
  revocation, and anonymous-access refusal.
- The tour currently assumes a local password session and its generated
  screenshots are ignored. Knoxx pull-request CI does not run it.
- Services mounts `contracts/knoxx` read-only into the DigitalOcean Knoxx
  service. Its live verifier already performs a real MCP initialize, tool-list,
  schema, pagination, and tool-call probe, but skips the probe when the loopback
  token is absent.
- Rheos already preserves lossless Markdown/frontmatter bytes, exposes a
  deliberately partial flat-frontmatter projection, and emits file-change
  events. Kanban is one current projection over that document shape.
- Sol is intentionally wire-compatible with a subset of Knoxx, while
  Katamorph owns shared resource/contract identity.

## Ownership boundaries

| Concern | Owner | Required output |
| --- | --- | --- |
| Portable resource, schema, reference, and event envelope | Katamorph | Versioned data contract; no host effects |
| Markdown bytes, frontmatter decoding, sidecar loading, file observation | Rheos | Shaped document and typed file-change event |
| Knowledge-work trigger/action execution and review/publication | Knoxx | Runtime behavior plus HTTP/MCP surfaces |
| Compatible agent-runtime interpretation | Sol | Conformance tests against the shared subset |
| DigitalOcean resources, secrets wiring, deployment, live probes | Services | Production declarations and fail-closed verification |
| Product/browser representation | Knoxx/Uxx | Human-visible review states and storyboard evidence |

No child repository may silently redefine another row's contract. Runtime
adapters may add namespaced extensions, but portable identity and validation
remain upstream.

## Phase 1 — browser proof becomes a merge gate

### Requirements

1. The existing Knoxx browser tour accepts a short-lived API key without
   printing or persisting it; the existing local-password path remains usable.
2. The browser runs at 1600×1000 in dark mode.
3. One stable storyboard directory contains reviewed captures for every
   meaningful state transition. A Markdown storyboard explains input, action,
   expected state, and screenshot for each step.
4. Pull-request CI starts an isolated Mongo-backed Knoxx backend and frontend,
   creates an ephemeral API key, runs the same tour, and uploads all captures on
   failure or success.
5. The CI job fails on a missing screenshot, missing control, wrong state,
   cleanup failure, anonymous read success, or browser/runtime failure.
6. Provider dispatch remains a separate check. The review tour must continue to
   disclose that it seeds production-shaped candidate bytes without assessing
   model quality.

### Acceptance

- A local run completes with no failed steps and produces the complete
  storyboard.
- Knoxx CI declares the browser proof in the `pull_request` workflow.
- Repository branch protection can name the workflow job as a required check;
  configuring that remote rule is a separate, auditable repository mutation.

## Phase 2 — production Git events and Knoxx actions

### Event vocabulary

The first admitted vocabulary is intentionally small:

- `:git/push`
- `:git/ref-updated`
- `:git/pull-request-opened`
- `:git/pull-request-synchronized`
- `:git/pull-request-merged`

Each event must carry a Katamorph event envelope plus a namespaced Git payload:
repository identity, provider delivery id, actor identity when known, ref/base/
head identifiers as applicable, immutable commit SHA(s), occurrence time, and a
redacted source reference. Provider request bodies and credentials are not
semantic authority.

### Requirements

1. A Knoxx Git adapter verifies the provider signature before shaping and
   dispatching an event. Replay identity is `(provider, delivery-id)`.
2. Services production resources declare the generator/source, listener
   trigger, and an already-registered Knoxx action. EDN may not pretend a new
   driver or handler exists.
3. The first action starts a scoped knowledge-worker session with repository,
   commit, and pull-request context. Any mutation back to Git requires a
   separate capability and explicit policy.
4. Actions that target the deployed Knoxx HTTP/MCP surface use HTTPS, scoped
   credentials, idempotency keys, closed request schemas, and tenant identity
   derived by the server rather than supplied as an authority claim.

### Acceptance

- Contract validation rejects missing event identity, mutable branch-only
  references, unknown event types, and caller-supplied tenant authority.
- A signed fixture dispatches exactly one typed event; a replay is recognized
  without executing the action twice.
- The production Services verifier observes the deployed event/action surface.

## Phase 3 — deployed MCP is required evidence

### Requirements

1. Knoxx deployment fails before rollout when the production loopback MCP token
   is absent or shorter than the backend's accepted minimum.
2. Deployment sets the MCP verification expectation to true from that admitted
   secret; callers cannot weaken it through `extra_env`.
3. The live verifier performs MCP initialization, lists the expected read-only
   tools, validates their schemas and pagination, invokes representative calls,
   and rejects duplicates, degraded results, or unauthenticated access.
4. Secret values are never printed, committed, included in screenshots, or
   placed in contract EDN.

### Acceptance

- Render/preflight tests prove missing token = deployment failure.
- A production deployment receipt contains the verifier outcome, not the token.

## Phase 4 — Rheos typed Markdown documents

### Document declaration

Frontmatter remains human-oriented YAML and may declare only compact references:

```yaml
---
contract-ref: katamorph://document/translation-review@1
schema-ref: katamorph://schema/translation-review@1
process-sidecar: ./translation-review.edn
---
```

The referenced EDN sidecar carries complex process data. It is parsed as EDN,
validated against a closed Malli schema, and merged by an explicit merge
policy. It is not evaluated as code.

### Merge law

1. Source path establishes the base directory; a sidecar may not escape it.
2. Frontmatter supplies document identity and compact overrides explicitly
   allowed by the referenced schema.
3. The EDN sidecar supplies structured process configuration.
4. Derived/runtime fields are computed after merge and cannot be supplied by
   either file.
5. Unknown keys, conflicting immutable identity, invalid Malli values, missing
   resources, and ambiguous schema versions produce a rejected document event;
   they do not silently fall back to Kanban parsing.
6. Raw Markdown/frontmatter/sidecar bytes remain available as evidence while
   shaped data becomes the runtime input.

### File-change event

A Rheos watcher emits an admitted event containing event id/type/version,
occurrence time, source path, content digest, change kind, actor/correlation
when known, contract/schema references, validation verdict, and the shaped
document only when valid. Kanban becomes one projection selected by its
contract reference.

### Acceptance

- Pure `.cljc` law and shape tests cover valid merge, invalid schema, traversal,
  identity conflict, unknown key, missing sidecar, and deterministic digest.
- Watcher integration tests show one file change produces one shaped event and
  write-id correlation remains intact.
- Existing Kanban cards parse and project without semantic drift.

## Phase 5 — Sol conformance

Sol consumes the shared event/resource subset through an adapter. It must not
copy Knoxx's full product runtime or introduce new `KNOXX_*` configuration.

Acceptance requires fixture-level conformance for resource identity, event
envelope validation, action request/response shapes, and explicit rejection of
Knoxx-only capabilities such as MCP HTTP, RBAC, memory, and publication when no
Sol implementation exists.

## Evidence policy

Every merge request changing a translation surface, event contract, document
shape, action, MCP tool, or deployment contract must attach the relevant
machine result. User-visible changes additionally require the deterministic
browser storyboard. Production verification is a separate post-deploy receipt;
a local or CI capture must never be represented as production evidence.

## Open blockers

- Confirm the exact supported Knoxx driver/route for signed Git webhooks before
  committing production source resources. If absent, implement and test the
  runtime adapter first.
- Provision the DigitalOcean production MCP loopback secret before changing the
  deploy render path from optional to required.
- Configure repository rules only after the new CI check name exists remotely.

