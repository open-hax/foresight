# Federated Knoxx backup direction

License: GPL-3.0-or-later.

The broader identity-to-resource concept is recorded in
[Axxium resource responsibility](resource-access-service.md). The user now
confirms Axxium as the foundational resource-and-authority direction. Backup is
one consumer. Concrete contracts, custody, and deployment remain to be refined.
The user further confirms Katamorph owns shared contract shapes, Axxium houses
the most fundamental core resources, and other systems can evolve resource
versions for their own needs.

## User requirements recorded during cleanup

- The laptop runs agents for weeks and must not fill its disk with abandoned
  scratch artifacts. Agents need a cleanup lifecycle, not periodic user rescue.
- Local removal requires an exact recoverable pushed Git version or a verified
  remote backup. Use Google Drive for uncertain artifacts.
- Downloads and Music are included in the current authorized backup and cleanup.
- Deduplicate on matching content hashes, preserving each path and its metadata.
  The user explicitly agreed there is no need to transfer duplicate bytes.
- Longer-term, use the user's own Google application credentials in Knoxx.
- Run cooperating Knoxx instances on the laptop and a server, with a federated
  view of content and backups. Avoid dependence on Rclone as the product backend.
- Repeated Rclone credential expiry is a user-reported problem. Its root cause
  has not been established. The current cleanup uses the working Drive plugin.

These are requirements and direction, not a claim that a federated backup
service has been deployed. The server identity, existing Google client type,
Google publishing state, and live Knoxx deployment ownership still need grounding.

## Proposed design, subject to repository and deployment evidence

Use an inventory keyed by content hash, with separate original path, metadata,
version, owner, and location records. A duplicate content object must not erase
the provenance or metadata of another file that references it.

Each node should own its workspace activity and completion records. A backup
job resumes from durable transfer receipts, verifies recoverability, and only
then permits eviction of unchanged inactive local content. Policy can prioritize
data and storage destinations; no model-confidence threshold authorizes deletion.

Start with one Google credential owner and a stable web authorization callback
usable from a phone. Authenticate federation peers separately. Do not spread
long-lived Google refresh tokens to every peer by default. Determine the actual
outbound integration and secret-storage boundary from Knoxx and its extracted
component owners before introducing another implementation.

Health should distinguish refreshable access-token expiry, revoked or expired
refresh credentials, quota failures, interrupted transfers, missing replicas,
and insufficient local space. A credential problem must not authorize eviction
or silently discard queued work.

## Verified external constraints

Google documents seven-day refresh-token expiry for external OAuth apps left in
Testing when the requested scopes extend beyond basic identity. This is a
configuration to inspect, not a diagnosis of the user's current app. Other
revocation/expiry conditions remain possible. See [Google OAuth token
expiration](https://developers.google.com/identity/protocols/oauth2/#expiration).

Google Drive supports resumable uploads directly. The current plugin's observed
100 MiB per-file limit and our 64 MiB archive pieces are transport-adapter
constraints of this cleanup; they must not become universal backup-domain law.
See [Google Drive upload guidance](https://developers.google.com/workspace/drive/api/guides/manage-uploads).

## Immediate boundary

Continue the already authorized, verified Drive backup and cleanup. Record the
actual results and recovery manifests. Do not replace the working transfer
mid-flight or claim unattended cleanup exists. Review the workspace cleanup
procedure alongside this direction before planning a concrete Knoxx change.

## Axxium relationship under discussion

The user initially considered whether Axxium should also be central to resource
provision because resource access is bound to particular credential identities.
After prior-design recovery, the user confirmed Axxium's broader foundational
resource-and-authority responsibility. Specific operational and data boundaries
remain open; this is direction, not a deployment or blanket acceptance of a prior
architecture record.

Observed in the current Axxium checkout: provider bindings associate provider
and verified subject with an internal actor. Google sign-in currently requests
`openid email profile`; no Drive-access grant follows from that identity binding.
Do not silently treat signing in with Google as consent to access Google Drive.

Proposed separation: Axxium owns actor/external-identity bindings and contracts
for authorization relationships. A resource connection separately records the
provider account, resource scope, consent/grant, credential reference, and
delegated operations. Provider adapters own operational refresh and I/O; backup
placement policy consumes verified authority and content inventory. The registry
of resource connections could be exposed through Axxium or remain separately
owned; neither choice has been accepted yet. Do not implement the earlier
single-credential-owner proposal as an agreed architecture.

## Recovered prior design and implementation anchors

The user recalled this might already be documented in Axxium, Katamorph,
eta-mu, or Knoxx. Targeted internal archaeology found:

1. [Knoxx Actor Credential Delegation Spec](../../knoxx/docs/notes/contracts/actor-credential-delegation-spec.md),
   originally May 5: distinguishes subject actor, executor actor, and connection
   owner actor; proposes non-secret connection/default/delegation contracts,
   encrypted secret references, and contextual credential resolution. This is a
   design proposal, not evidence that its entire proposed schema is deployed.
2. [Axxium Proxx identity migration plan](../../axxium/kanban/axxium-proxx-identity-migration.md):
   explicitly plans identity/capabilities in Axxium while provider-specific OAuth
   credentials remain with Proxx. This is documentary evidence of intended
   ownership, not board validation or proof that the migration completed.
3. [Katamorph roadmap](../../katamorph/ROADMAP.md): resource identity, shared
   schemas, validation, and generic interpretation are its domain; consumers
   provide storage connections and effects. Its consumer-adoption claims are
   dated and must be checked against current code rather than treated as live
   implementation facts.
4. [Eta-mu Contract Dialects and Data Authority](../../eta-mu/docs/architecture/contract-dialect-and-data-authority.md),
   a working cross-repository record verified in July: distinguishes Axxium
   identity/authorization, Katamorph resource language, and deployable Knoxx
   composition. This is an architecture record with explicit epistemic limits.
5. [Event-envelope correction](../../eta-mu/docs/architecture/event-envelope-v1-correction-2026-07-29.md):
   Axxium principal bindings and optional Katamorph resource references are
   related but distinct contracts. Resource lineage does not manufacture a
   runtime principal or authorize an event producer.
6. [Current Knoxx actor credential resolver](../../knoxx/backend/src/cljs/knoxx/backend/infra/actor/credentials.cljs):
   implements actor-scoped provider credential lookup with membership/org
   context. A scoped absence of actor identity prevents fallback to an unrelated
   process-global actor context. The full three-identity connection/delegation
   proposal was not found in this targeted current-source search.

Interpretation: the prior design already supplies the conceptual identity to
connection link the user is recalling. Build on it and reconcile current owners;
do not invent a competing flat credential model or claim central resource
provider ownership has already been settled.
