# Shared Axxium login and Proxx federation: live inventory and PR dependencies

Observed 2026-10-22 America/Chicago. This is an operational inventory and a
proposed cross-product acceptance scope, not an implementation approval, board
transition, migration, or deployment receipt. Software/process documentation:
GPL-3.0-or-later.

## Operator intent

The DigitalOcean droplet replaces Big Ussy. Use the existing Services deployment
ownership (`services/digitalocean`), Axxium identity kernel, Knoxx product and
Proxx model gateway. The operator wants one identity across the droplet,
Stealth and Yoga; Knoxx and Proxx should reuse sign-in rather than require
independent accounts. Proxx nodes should federate under that identity. Provider
credentials remain at their owning node.

Do not create an owner by assigning an arbitrary DID string, merge accounts
by email/title, copy browser cookies between hosts, or copy provider refresh
tokens as a substitute for a federation relay.

## Observed runtime

| Host | Observed state | Implication |
| --- | --- | --- |
| DigitalOcean, `open-hax-services-production`, `157.245.125.134` | Proxx `88471efd7f8cd27f64fc733a1c31369f00a6d432`; Knoxx `841642adffa2656edd842db72843da0a0f4d8336`; Axxium `axxium-federation:local`; Caddy ingress | Service names share a host; deployment working directories are `/srv/open-hax/services/{proxx,axxium}`. Image health does not establish successful inference or shared login. |
| Stealth | `proxx-local-proxx-1` healthy, zero peers and zero connected bridge sessions; Axxium database running, no Axxium application in running-container inventory | Existing Proxx bridge configuration is not an active federation. Do not start a previously stopped identity service without establishing its intended role. |
| Yoga | Reachable through configured SSH alias; healthy `knoxx-yoga-backend-1` and `axxium-yoga-axxium-1`; Proxx database present, no running Proxx application | Yoga's existing identities/runtime must be preserved during integration. |

Yoga configures `KNOXX_AXXIUM_ORIGIN=https://yoga.axxium.promethean.rest`;
its Axxium issuer is that same origin. This is local authority delegation,
not evidence of a shared central browser session.

SSH to the droplet succeeds as `err` with `~/.ssh/id_ed25519_2026`.
The old `proxx.promethean.rest` known-host entry differs. Verification used
the already trusted `knoxx.promethean.rest` host key via `HostKeyAlias`, without
disabling host-key checking or replacing trusted keys. Its observed fingerprint
also matches Services `digitalocean/hosts/production.yaml`:
`SHA256:HKUMqjMeeJsIlZRqO++z5xx0pBqtLi8GOBFKZ6GomQY`.

### Inference failure, independently reproduced

The droplet's sole registered provider is `requesty`, account
`requesty-production`. A direct call using its existing stored credential
returns 200 for `/models` (177 models), but 402 for `/chat/completions`:
organization balance too low. Proxx reports that account in cooldown.
No billing operation, cooldown reset, credential rotation or provider-key copy
was performed.

Failed Proxx requests also attempt `http://ollama:11434` (ENOTFOUND) and
`http://192.168.12.68:11434` (connection timeout). Those are stale routes on
the droplet. The host does have an Ollama listener on a dedicated bridge;
reachability/admission from Proxx and its available models were not qualified
by this inventory. Do not bypass the bridge ownership/firewall contract.

### Axxium is present, but SSO is not qualified

`https://axxium.promethean.rest/api/auth/config` advertises Google and AT
Protocol login. Its database contains two actors and one provider binding;
only counts were read. The service DID document publishes
`did:web:axxium.promethean.rest` with a PDS service. That service identity is
not automatically the operator's human actor identity or a federation grant.

Neither deployed Proxx currently has registered federation peers. The local
default owner, bridge owner, advertised URL and bridge path are inconsistent:
default `did:web:proxx.promethean.rest:brethren`, bridge owner
`did:web:proxx.promethean.rest`, private advertised hostname
`open-hax-openai-proxy`, and `/api/ui/federation/bridge/ws`. The droplet's
federation self-description has all identity/topology fields null. Correcting
these strings alone would not establish Axxium authentication.

## Existing PRs: retain provenance and distinguish scope

| PR | Observed status | Scope and remaining limit |
| --- | --- | --- |
| [open-hax/axxium#30](https://github.com/open-hax/axxium/pull/30) | Merged 2026-10-21, merge `99e6c725c64baed7d3ee93930b206724874e1486` | Canonical DID actor identity, Google/AT sign-in, agent credentials and hardening. Does not prove a consumer-facing authorization server/SSO implementation or that the droplet runs this exact source. |
| [riatzukiza/axxium#11](https://github.com/riatzukiza/axxium/pull/11) | Open | Syncs that organization merge back into the personal fork. |
| [riatzukiza/axxium#3](https://github.com/riatzukiza/axxium/pull/3) | Draft, head `f6f20647b4c34eebb4f8f3ed5fe2010f05bcbeec` | Complete Knoxx identity migration plan. Explicitly proposes future service-authenticated introspection/status, capability freshness, migration and rollback. Documentation, not runtime implementation. |
| [riatzukiza/axxium#4](https://github.com/riatzukiza/axxium/pull/4) | Draft, head `2219bd17f2d6e8d6827770668c10ed9b1aa4d568` | Complete Proxx identity migration plan, including HTTP/WS/SSE/bridge and browser/API-key continuity. Current native review gate fails and one thread is unresolved. |
| [open-hax/knoxx#381](https://github.com/open-hax/knoxx/pull/381) | Open, head `40221a69f7fff675b46614d9ab318b4ef52f3786` | Loopback/local Axxium password delegation and signed-out UI handling. Consumer revokes the transient Axxium session and creates its own session; this is not redirect SSO. Review/evidence jobs are failing despite passing build jobs. |
| [riatzukiza/knoxx#2](https://github.com/riatzukiza/knoxx/pull/2) | Open draft | Preserves #381 atop fork synchronization, with explicit readiness holds; not another completed identity integration. |
| [open-hax/axxium#13](https://github.com/open-hax/axxium/pull/13) | Open, head `0e0a67299b61ae8ac309385a6400ca603ac3855f` | Signed recipient-bound identity transfer, preserving actor/entity IDs while keeping passwords, permissions and sessions local. Independent/offline identity continuity; not SSO. Review gates fail. |
| [open-hax/services#92](https://github.com/open-hax/services/pull/92) | Open, head `3016fdaee920607efc4e72b56e12e112599e8918` | Existing Axxium/PDS deployment line, already reflected in live topology but not merged/qualified as a fresh release. Planning must reconcile its old Axxium dependency with merged #30. |

The current PR-flow CLI was used read-only to inspect #30, #13, personal #3/#4,
Knoxx #381 and Services #92. Its gate calculation is not a retroactive claim
that an already merged PR was unmerged or that historic approvals were absent.
No review comments, approvals, board transitions, merges or deployments were
performed. Searched both `open-hax` and `riatzukiza` Proxx/Knoxx/Axxium/Services
forks; the full Proxx identity integration found is still a planning artifact.

## Proposed complete acceptance scope

Keep three concerns separate and bind their interfaces explicitly:

1. **Identity/session authority (Axxium):** stable issuer/actor/entity binding,
   authentication, session lifecycle and revocation. Inventory and preserve
   existing actor/provider bindings first. The public Axxium can be the primary
   online authority; local independent identity/import remains a separate,
   reviewed offline capability rather than implicit privilege replication.
2. **Product admission (Knoxx and Proxx):** registered application clients,
   exact redirect allowlists, one-time authorization codes with PKCE/state,
   audience-bound grants and authenticated status verification. Each product
   keeps its own HttpOnly session and resource/tenant authorization while
   referring to the same verified actor. Existing provider credentials and
   local resource ownership stay with their product owners.
3. **Machine federation (Proxx):** separately identified nodes, explicit
   Axxium-authorized owner/service grants, relay-only policy and loop prevention.
   Workstation-initiated outbound relay connections can avoid requiring inbound
   access to laptops. Credentials stay at their source; expose only admitted
   capabilities and provenance. Retire Big Ussy from the active topology without
   rewriting historical evidence.

Two browser experiences must be tested separately:

- **Same browser, multiple applications:** sign in to Axxium once, visit Knoxx
  and Proxx, return through their bound callbacks with the same actor and no
  second password prompt. An Axxium session is not a shared parent-domain cookie
  and is never readable by the applications.
- **New laptop/browser:** approve an expiring, one-use device request from an
  authenticated device, with the receiving device/application clearly shown.
  A new device does not inherit cookies from another device. Device approval
  is proposed scope, not an existing capability found in the reviewed PRs.

Verification must include logout/revocation, disabled actors, denied and expired
device approvals, wrong audiences/redirects, replay and wrong-node grants,
cross-tenant refusal, dependency outages and attribution preservation. Offline
behavior must not silently resurrect local password or capability authority.

Federation acceptance additionally requires public inference and a Knoxx actor
answer using locally indexed memory; prove remote-to-local routing with served-
by provenance and credential non-export, recovery after disconnect/reconnect,
and independent machine admission/revocation for Yoga. A healthy process or a
connected WebSocket is insufficient.

## Work order and unresolved decisions

Extend the two existing migration plans with this explicit redirect-SSO and
cross-device acceptance scope, reconciling the merged Axxium actor foundation
before implementing consumer cutover. Follow owner-repo planning qualification,
then Axxium laws/adapters, Knoxx/Proxx consumers, and Services rollout. Node
federation can proceed as a separate slice once its owner grant is actually
bound; do not claim service bearer tokens alone establish human identity.

Remaining live operations are deliberately unperformed: Requesty billing,
identity setup/linking, deployments/restarts, legacy account retirement and
federation registration. No new runtime was created during this inventory.
