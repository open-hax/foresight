---
title: "Workstream: portable sandbox bundles and evidence-first review infrastructure"
summary: "Tracks the cross-repository rollout of revision-bound sandbox bundles and immutable evidence-first automated-review workflows."
category: "tracking"
created: "2026-08-29"
---

# Workstream: portable sandbox bundles and evidence-first review infrastructure

Inventory date: 2026-08-29.

This thread is cross-cutting infrastructure: make repositories reconstructable in constrained chat/sandbox environments, then make automated review consume explicit evidence rather than ambient repository assumptions.

## Signal

Two rollout patterns are happening in parallel across Foresight submodules:

1. revision-bound sandbox bundles that package source, pinned toolchains, validation logs, checksums, and optional dependency caches;
2. eta-mu's evidence-first reusable PR review workflow, pinned immutably into consumer repositories.

Foresight #40 generalizes the first pattern further into a small relocatable Linux chat-work runtime containing Node, NBB, Babashka, clj-kondo, and jscpd.

## Portable bundle/runtime PRs

- [open-hax/foresight#40](https://github.com/open-hax/foresight/pull/40) — self-contained Linux chat-work runtime.
- [open-hax/knoxx#208](https://github.com/open-hax/knoxx/pull/208) — Knoxx backend/frontend/JVM sandbox bundle.
- [open-hax/katamorph#19](https://github.com/open-hax/katamorph/pull/19) — repairs Katamorph sandbox validation so it actually runs.
- [open-hax/proxx#306](https://github.com/open-hax/proxx/pull/306) — Proxx API/CLJS/web-console sandbox bundle.
- [open-hax/uxx#6](https://github.com/open-hax/uxx/pull/6) — Uxx workspace sandbox bundle.
- [octave-commons/Truth#4](https://github.com/octave-commons/Truth/pull/4) — Truth revision-bound sandbox bundle.
- [octave-commons/epiphany#6](https://github.com/octave-commons/epiphany/pull/6) — Epiphany sandbox bundle.
- [octave-commons/muse#5](https://github.com/octave-commons/muse/pull/5) — Muse host-compatibility sandbox bundle.

## Evidence-first review rollout PRs

- [open-hax/proxx#307](https://github.com/open-hax/proxx/pull/307)
- [octave-commons/Truth#5](https://github.com/octave-commons/Truth/pull/5)
- [octave-commons/epiphany#13](https://github.com/octave-commons/epiphany/pull/13)
- [octave-commons/muse#7](https://github.com/octave-commons/muse/pull/7)
- [octave-commons/bitch-tracker#3](https://github.com/octave-commons/bitch-tracker/pull/3)

Each pins the reviewed eta-mu reusable workflow by immutable commit and constrains permissions to read-only contents/pull-request access plus the review app credentials.

## Adjacent gate work

- [open-hax/foresight#41](https://github.com/open-hax/foresight/pull/41) is not a sandbox-bundle PR, but it demonstrates why this infrastructure exists: green status is meaningless if the semantic package is never compiled. Its Alpha JVM gate belongs to the same evidence discipline.

## Common contract

A useful bundle should answer, without external repository archaeology:

- exactly which revision is being inspected;
- which source/submodule revisions are present;
- which toolchain versions are expected;
- which validation commands ran and whether they passed;
- which generated outputs/logs survive a failed validation;
- how integrity is checked after extraction or transfer.

A useful automated review should then consume explicit evidence from that bounded revision rather than treating prose, stale review threads, or unavailable toolchains as proof.

## Risks to avoid

1. **False portability.** A bundle that uploads successfully but never executes its own validation path is packaging, not reconstruction evidence. Katamorph #19 is the concrete example.
2. **Mutable workflow supply chain.** Consumer repositories should pin reusable review workflows and their transitive third-party actions by immutable revision.
3. **Hidden runtime inflation.** Foresight #40 intentionally keeps JVM Clojure out of the small default chat-work runtime. Repository-specific bundles may still carry JVM tooling when their actual test/build path requires it.
4. **Failure erasure.** Partial artifacts and logs may be preserved on failure, but the workflow itself must remain failed. An artifact is evidence of execution, not a pass token.

## Exit criteria

This rollout is coherent when:

- every actively maintained Foresight submodule can emit a revision-bound bundle or explicitly documents why it does not need one;
- the shared small Linux runtime can execute the lightweight Foresight/NBB workflows after extraction without host installation assumptions;
- evidence-first review callers pin one reviewed provider revision and expose no broader permissions than required;
- semantic packages have repository-local executable gates, so review automation cannot report green while skipping the code that matters.
