# EvidenceRef v1

EvidenceRef is the source-neutral identity carried between context providers,
workflows, skills, and agents. It identifies evidence in an authority-owned
namespace without treating a checkout path, cache path, or machine location as
identity.

Supported v1 kinds are Git objects, Rheos cards/events/workflow definitions,
Clio events, skill definitions, and skill-graph nodes. Epiphany commonly emits
Git references. Rheos emits current-work references. Clio emits immutable event
references. Skills and workflows may cite these references or produce them as
outputs while preserving the named authority.

An EvidenceRef separates:

- `identity`: stable keys interpreted by the authority;
- `selector`: path, heading, section or projection details used to select a
  view, never establish identity;
- `freshness`: current, stale, unknown or unavailable;
- `epistemic-tier`: observed, derived, provisional or accepted when the source
  provides that distinction.

Resolver outcomes remain four distinct states: resolved, stale, unavailable,
and unsupported. Only `resolved` carries a required value. The pure contract
does not perform I/O and does not appoint Muse as evidence authority.

Migration begins with Muse's Epiphany context packet, whose previous ad-hoc
reference map now projects EvidenceRef v1. Freshness remains `unknown` until an
Epiphany-owned index revision token exists; Muse must not infer freshness from
HTTP availability.
