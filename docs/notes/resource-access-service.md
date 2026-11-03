# Axxium resource responsibility

License: GPL-3.0-or-later.

Status: user-confirmed architectural direction, recovered prior design, and
explicit implementation gaps. No new service, repository, or deployment exists.

## Current user direction

The user confirms Axxium is broad enough to encompass the fundamental identity,
authentication, authorization, and resource concepts needed to act in the system.
Everything must be expressible as a resource from the relevant perspective.
The proper noun anchors a set of responsibilities whose conceptual borders stay
loose; related systems can use the same concepts from their own perspectives.

The user explicitly confirms the division: Katamorph defines the contract
shapes; Axxium is home to the most fundamental core resources; other systems
remain able to create and evolve versions of resources as needed. Shared shapes
do not require every domain resource to be authored or interpreted by Axxium.
The concrete rules for version identity, predecessor lineage, extension, and
authority to change a core resource need to be grounded in the existing contracts
when an implementation slice is planned.

Develop this as Axxium's foundational resource-and-authority responsibility before
inventing a separately named service. The concrete division of registry data,
shared resource grammar, secret custody, provider I/O, and deployment remains to
be worked through with existing owners. This direction does not accept every
detail of an older proposal or assert that it is already implemented.

The existing filename is a discovery alias from the initial exploratory summary.

Recovered documentary anchor: the closest later named record found so far is
[Axxium Event Fabric](design/axxium-event-fabric.md), a proposed September
cross-project architecture assigning Axxium account, installation, grant, object,
stream, and episode bindings while leaving provider operations in extern
adapters. Its presence in repository history does not make every proposal an
implemented or formally accepted contract.

## Naming discussion provenance

The user explicitly prefers a distinct proper noun, as with Axxium and Knoxx.
Generic terms such as resource, service, and manager recur with different meanings
across projects and do not give this concept a reliable search anchor. "Resource
access service" is only a temporary descriptor, not an accepted service name.

Earlier coined suggestions, now set aside while recovering existing names:

| Name | Pronunciation | Intended association |
| --- | --- | --- |
| Nexra | NEKS-ruh | Connections among actors, external accounts, grants, and resources |
| Pactra | PAK-truh | Consent, scoped delegation, and authorized use |
| Veyra | VAY-ruh | A neutral service identity whose precise meaning follows the recovered contract |

These were invented suggestions, not names recovered from a service, person,
mythology, or prior architecture. The user asked for their provenance and raised
the possibility that an existing named system already carries this responsibility.
No name was selected; the earlier coordinator preference is not a design decision.
A targeted
case-insensitive word search found no existing uses of these three names in the
inspected Axxium, Katamorph, eta-mu, Knoxx source/docs or Foresight docs.

Once a name is selected, make it the primary entry and preserve the descriptive
term and source links as discoverability aliases. Naming does not settle
deployment packaging or the outstanding Axxium ownership question.

The user identifies eta-mu as the place to recover the constellation's existing
names and responsibilities. Its README and instructions explicitly point to its
roadmap as the constellation hub. Consult that lineage and the later cross-project
records before proposing another proper noun.

## Why this concept has its own entry

The user reports that this idea repeatedly emerges in discussions about Axxium,
Katamorph, eta-mu, and Knoxx. Without a service name of its own, its design has
accumulated inside neighboring projects. This document provides one starting
point while retaining the provenance of those earlier discussions.

The recurring concept is authorized use of external resources through particular
account identities and credentials. Federated backup is one consumer. The scope
can also include other account-bound providers; it is not defined solely by
Google Drive or file transfer.

The user further clarified that a proper noun names a set of responsibilities.
Conceptual borders should remain loose: several systems use the same concepts
from different perspectives. Axxium can view an account through identity and
authority while this service views it through connections, credentials, grants,
and available resources. Shared concepts do not by themselves require centralizing
all their representations or implementations in one project. Specific contracts
can establish authority for particular decisions without assigning every use of
a word to one subsystem. Read the responsibility table below as perspectives
and current anchors, not a demand for impermeable conceptual partitions.

## Recovered resource-access responsibility

Represent and resolve the relationship between:

- the subject on whose behalf work is requested;
- the executing actor, agent, or device;
- the owner of the external connection;
- the external provider and account identity;
- the resource and the operations actually authorized;
- the consent or delegation that permits this execution;
- the credential reference and its lifecycle state.

Connection identity, authorization, and secret material have related but distinct
lifetimes. A credential refresh or revocation must not erase an actor identity,
resource inventory, or historical receipt. Separate accounts belonging to one
person must remain explicit rather than collapsing into a single credential.

Contracts and inventories carry non-secret references and policy. Secrets belong
in protected runtime storage. Connection resolution must account for the subject,
executor, connection owner, and applicable delegation before invoking a provider.

## Relationship to the existing systems

This is a proposed boundary, not a settled extraction plan:

| System | Existing anchor | Proposed relationship to this concept |
| --- | --- | --- |
| Axxium | Runtime identities, external identity bindings, credentials and authorization kernel | User places foundational resource and authority responsibility here; concrete registry and connection contracts need refinement |
| Katamorph | Shared resource identity, schemas, validation and contract language | Define shared portable resource/connection/delegation vocabulary where the owning contracts establish that responsibility |
| Provider adapters | Existing provider-specific credential and I/O implementations | Execute authorized operations and manage provider-specific lifecycle and transport |
| Knoxx | Deployable product composition and actor-scoped credential lookup | Compose the service for people and agents and expose its account/resource view |
| Backup capability | Current verified Drive cleanup and proposed federated backup | Consume scoped resource access, maintain content/replica receipts, and enforce verified recovery before eviction |

The current direction is to develop Axxium's responsibility. Its implementation
and provider composition can be worked through without another brand. Do not
silently equate shared resource identity with one universal payload store or
provider implementation; preserve the user's broader conceptual scope while
grounding concrete contracts and owners before implementation.

## Recovered evidence

- [Axxium Event Fabric](design/axxium-event-fabric.md), September: explicitly
  includes Google Drive, provider accounts/installations/grants, external object
  identity, protected mirrors, and provider-specific operation/credential
  boundaries. It is marked proposed. This later record materially expands the
  identity-binding perspective beyond the earlier sign-in-only implementation.
- [Eta-mu constellation roadmap](../../eta-mu/ROADMAP.md) is the named hub for
  responsibilities and satellite roadmaps. It points to Katamorph actor account,
  capability and policy contracts and describes consumer-adoption gaps. Its
  survey date and warnings about stale implementation claims must be retained.

- [Knoxx Actor Credential Delegation Spec](../../knoxx/docs/notes/contracts/actor-credential-delegation-spec.md)
  contains the subject/executor/connection-owner model, non-secret contracts,
  secret references, and delegation proposal. It remains a design source.
- [Axxium Proxx identity migration plan](../../axxium/kanban/axxium-proxx-identity-migration.md)
  explicitly separates canonical identity from provider-specific credentials.
  A plan is not evidence that every migration step is complete.
- [Katamorph roadmap](../../katamorph/ROADMAP.md) separates portable resource
  vocabulary from storage connections and runtime effects; adoption claims are
  dated and require current-source verification.
- [Eta-mu Contract Dialects and Data Authority](../../eta-mu/docs/architecture/contract-dialect-and-data-authority.md)
  records cross-repository ownership and warns against competing authorities.
- [Current Knoxx credential resolver](../../knoxx/backend/src/cljs/knoxx/backend/infra/actor/credentials.cljs)
  proves actor-scoped credential lookup exists. It does not prove the full
  proposed connection/delegation model is implemented.
- [Federated backup direction](federated-backup-direction.md) preserves the
  current user's backup requirements, credential-expiry concern, and proposed
  laptop/server federation use case.

## Open decisions

1. Concrete resource contracts and the scope of Axxium's foundational authority.
2. Registry data, shared resource grammar, secret custody, provider operations,
   and deployment relationships within that direction.
3. Credential custody and delegation across cooperating nodes. A single
   credential owner was discussed as a proposal, not accepted as a requirement.
4. Reconciliation of the older three-identity design with current actor-scoped
   implementations and shared contract owners.
5. Concrete first provider and consumer, including the user's Google application
   configuration and a stable phone-usable authorization route.

Next step: use this entry as the starting point for resolving the ownership
boundary before planning a concrete implementation; keep original sources intact.
