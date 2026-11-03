# Local Axxium and Knoxx identity stack

This checkout has three PM2 apps in `ecosystem.identity.local.config.cjs`:

| Process | URL | Purpose |
| --- | --- | --- |
| `axxium-local` | `http://127.0.0.1:8788` | Identity API and account manager at `/portal/index.html` |
| `knoxx-axxium-local` | `http://127.0.0.1:8003` | Knoxx API delegating passwords to Axxium |
| `knoxx-axxium-ui` | `http://127.0.0.1:5176` | Knoxx UI and local API proxy |

The apps bind loopback. Start or restart them from this workspace with
`pm2 start ecosystem.identity.local.config.cjs` or `pm2 restart axxium-local knoxx-axxium-local knoxx-axxium-ui`.
Build Axxium with `npm --prefix axxium run build` and Knoxx with
`pnpm -C knoxx/backend typecheck` and `pnpm -C knoxx/frontend build` before
restarting after source changes.

Private operator configuration is in `~/.secrets/axxium/local.env`,
`~/.secrets/axxium/admin.env`, and `~/.secrets/knoxx/local.env` (mode 0600).
The Google web OAuth client JSON remains under `~/.secrets/google/`; empty
mode-0700 folders for Discord, GitHub, and Bluesky await their own provider
clients. Read the administrator email and password from `admin.env` on the
machine; do not copy them into source control. The local PostgreSQL database is
`axxium_local` on port 5433. Knoxx uses its own `knoxx_axxium_local` MongoDB
database.

The enabled Google callback on this machine is exactly
`http://127.0.0.1:8788/api/auth/google/callback`. The URI is present in both
Google Cloud Console and the private client JSON's `web.redirect_uris`.
Axxium verifies ID token issuer, audience, nonce,
and verified email, then binds the Google subject to a human actor. An email
already held by another actor is rejected instead of silently merged.

The AT lookup route verifies that a handle resolves to a DID and its DID
document names the same handle, or that a DID's handle resolves back to that
DID. Read-only discovery grants no account access. AT Protocol sign-in uses the
official OAuth client, a loopback `http://localhost` client ID, and a
browser-bound callback. A successful flow binds the verified DID to a human
actor; signed-in humans can use `link=1` to attach one to their existing actor.
The OAuth tokens are revoked before Axxium session creation. Live completion still
requires a human to approve the account authorization in a browser. The stack
does not run a PDS or register `did:plc` identities. Local actor IDs are Axxium
IDs, not DIDs.

The public origin is `https://axxium.promethean.rest` on the DigitalOcean host
`157.245.125.134`. The `services/digitalocean` topology runs Axxium,
PostgreSQL, the official PDS, and Caddy routes on that origin. Its service DID
is `did:web:axxium.promethean.rest`. The first hosted account,
`calliope.axxium.promethean.rest`, has PLC DID
`did:plc:322llrygnobkk4l7uxy7gxty`; its handle resolves over HTTPS and its
PLC document points back to this PDS. A browser completed the public PDS OAuth
flow and returned an Axxium human actor. Google sign-in is enabled for both
local and public callbacks. The local PM2 configuration remains loopback-only.

For a live Axxium proof, run:

```bash
cd axxium
node --env-file=$HOME/.secrets/axxium/local.env \
  --env-file=$HOME/.secrets/axxium/admin.env scripts/verify-local-identity.mjs
```

The verifier checks Google and AT OAuth starts, anonymous denial, admin login,
reciprocal AT resolution,
agent creation, credential use, administrator isolation, revocation, and
post-revocation denial. It removes its temporary actor and credentials. Knoxx
delegation has a separate live verifier in `knoxx/scripts/verify-axxium-local.mjs`.
