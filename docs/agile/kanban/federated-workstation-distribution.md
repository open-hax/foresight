---
uuid: "federated-workstation-distribution"
title: "Treat Stealth and Yoga as one maintained workstation distribution"
status: "incoming"
priority: "P1"
labels: "epic,federation,workstation,distribution,updates"
created_at: "2026-10-22T23:00:00-05:00"
---

# Treat Stealth and Yoga as one maintained workstation distribution

## Why

The operator wants the workstation suite to behave like a distribution: both
machines converge automatically when qualified updates are available, preserve
local work, and surface clear human actions instead of depending on another
agent conversation to repair drift.

## Accepted operator choices

- Enable BetterDiscord update repair on Stealth and Yoga as part of the suite.
- Automatically apply eligible Ubuntu security updates on both machines.
- Do not force reboots or terminate Discord calls for routine repair.
- Make human-input requirements visible on the machine and across the federation.

## Observed first profile

BitchTracker's product repository implements the first scoped release adapter:
`/home/err/spaces/bitch-tracker/ops/README.md`. It includes content-addressed
suite generations, immutable payload manifests, per-host profiles, SHA-256
validation, backups, local-modification holds, installation rollback on write
failure, per-host installed state and attention notifications. Native systemd
owns scheduling; native APT/unattended-upgrades owns security package work.
Stealth distributes to Yoga over existing authenticated SSH and collects peer
status. BetterDiscord repair is enabled on both machines.

This is an observed deployment slice, not an accepted promotion of BitchTracker
into Foresight's generic distribution authority. It does not implement service
migration/failover, source-branch merging, OS images, secure boot, signed release
transparency, generic host enrollment, or a replacement board/event kernel.

## Outcomes for the larger distribution

1. Reconcile this profile with the existing Services deployment contracts and
   shx/Hexis supervisor work; reuse canonical ownership and render/check/apply
   seams instead of building a second generic supervisor.
2. Define release channels, provenance, qualification, signed metadata and host
   enrollment/revocation for the wider distribution.
3. Admit backend/service upgrades only with migration and backup evidence; retain
   product-owned runtime and provider policy boundaries.
4. Provide a user-facing fleet view of desired/installed generations, stale peers,
   failed updates, pending reboots and exact human actions.
5. Qualify recovery across peer loss, package failure, incompatible application
   updates and restored backups, without losing unique device changes.

## Acceptance

- Every installed component names its owning product and release source.
- Each host distinguishes desired, installed, deferred and human-held state.
- A disconnected machine preserves its working generation and converges later.
- Local modifications are never silently replaced.
- Credentials and private conversation data never enter release payloads.
- Human-required reboots, conflicts, root-policy updates and failures are visible.
- Promotion of generic laws remains reviewed and separate from this first adapter.

## Evidence and state

The first adapter has tests for invalid manifests, corrupt files, local-change
holds, exact backups, failed-write rollback, and notification cadence. Live
Stealth/Yoga verification is recorded in the BitchTracker Receipt River.
This manually authored card is an incoming planning artifact; it is not a claim
that Rheos admitted or completed the larger distribution program.
