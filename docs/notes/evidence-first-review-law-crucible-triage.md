---
slug: evidence-first-review-law-crucible-triage
uuid: 1d6ebf7e-4f0a-4a35-9d0e-9ef2d40f6c8d
title: "Evidence-first review law crucible triage"
kind: note
status: draft
description: "Revision-scoped triage of the Muse/eta-mu/Knoxx/Foresight evidence-first review stack as a Foresight Mu-law lift candidate."
created: "2026-08-15"
labels: [foresight, mu, review, muse, eta-mu, knoxx, crucible, promotion-candidate]
sources:
  - "https://github.com/octave-commons/muse/commit/f26538bd6c5ec3ef6084be9d2424d5b1daef648c"
  - "https://github.com/octave-commons/muse/commit/a9d31f3560c2b5a9c6157a6c981560450046e22e"
  - "https://github.com/octave-commons/muse/commit/1105e6194ca45f9394ee32c09d41bbda9f0fc092"
  - "https://github.com/octave-commons/muse/commit/3510440a01bac8c8ecbe8d5b8d08eb4a5cd46cac"
  - "https://github.com/open-hax/eta-mu/commit/2cde056e6d0b97d2a5cb37d41b7901ffeacad76a"
  - "https://github.com/open-hax/eta-mu/pull/292"
  - "https://github.com/open-hax/eta-mu/commit/2b918cdab2ebd30e745bb8fa86d077d7a3af0030"
  - "https://github.com/open-hax/knoxx/commit/af773f02bec120c28231d232b6a311762d0aec0d"
  - "https://github.com/open-hax/knoxx/pull/245"
  - "https://github.com/open-hax/foresight/commit/841d7e5ad36add0740ad1201921a2e516b80ddfb"
  - "https://github.com/open-hax/foresight/pull/31"
---

# Signal

A review architecture has survived implementation pressure in multiple repositories and is now a strong **Foresight Mu lift candidate**, but it is not yet an accepted Foresight law.

The stable-looking semantic core is narrower than the current host machinery:

```text
bounded review session
  -> deterministic evidence stages
  -> candidate finding
  -> location validation against observed diff
  -> adversarial classification
  -> derived review disposition
  -> machine-written submission artifact
```

The law-like part is that the reviewer does not publish arbitrary model prose as authority. The model drives typed transitions; the review event and publishable artifact are derived and validated by code.

A second law candidate emerged from rollout pressure on the host boundary: **a pinned top-level workflow is not revision-bound if it delegates transitively to mutable code references**. Revision-bound execution requires closure over executable dependencies, not merely an immutable caller reference.

# Observations

## Muse now owns the review host/profile machinery

Muse commit `f26538bd` introduced the evidence-first review state machine as tools and a pure review transition namespace. Commit `a9d31f35` moved the reviewer agent, prompt, and publisher into Muse so the review profile and its machinery travel together at one pinned Muse revision.

Subsequent fixes (`1105e619`, `3510440a`) exposed real state-machine laws rather than cosmetic defects: classification must remain legal until submission, and duplicate confirmed inline locations must be rejected before publication so the reviewer can recover.

## eta-mu now owns reusable orchestration, not the whole semantic implementation

Eta-mu's reusable workflow pins/stages the Muse review profile and publishes the resulting submission artifact. Commit `2cde056e` additionally moved review-profile installation into runner-global OpenCode configuration so reusable review cannot mutate a caller's tracked `.opencode/` tree.

The Foresight rollout then exposed a distinct reproducibility gap: callers pinned eta-mu's reusable workflow by full commit SHA, but that workflow still invoked third-party GitHub Actions through mutable tags. [eta-mu PR #292](https://github.com/open-hax/eta-mu/pull/292) corrected that gap without intentionally changing workflow behavior by replacing those nested tags with full commit SHAs. It merged as `2b918cda`.

This is consistent with the existing center split: Muse projects host configuration and review machinery; eta-mu coordinates reusable CLJS/runtime execution. The new evidence adds another boundary rule: orchestration provenance must include the executable dependency closure beneath the reusable entry point.

## Knoxx and Foresight are independent consumers

[Knoxx PR #244](https://github.com/open-hax/knoxx/pull/244) merged as `af773f02` with a caller pinned to eta-mu `2cde056e` and explicit least-privilege review secrets.

After eta-mu #292 landed, [Knoxx PR #245](https://github.com/open-hax/knoxx/pull/245) repinned that caller to the hardened provider merge `2b918cda` and merged without changing permissions, evidence gates, or secret forwarding.

[Foresight PR #28](https://github.com/open-hax/foresight/pull/28) remains open as the original caller adoption branch, but [Foresight PR #31](https://github.com/open-hax/foresight/pull/31) separately merged the provider repin to `2b918cda` for the active review workflow on `main`.

The consumer evidence is therefore stronger than the earlier snapshot: Foresight and Knoxx both moved to the transitively pinned provider after the crucible exposed the gap.

## Foresight main's submodule snapshot is older than some consumed review revisions

The crucible has already demonstrated that a submodule archaeology snapshot and an exact executable dependency pin can move independently. That distinction remains useful: **submodule snapshot provenance** and **runtime/law dependency provenance** are separate claims.

The supply-chain correction sharpens the latter. An exact top-level pin is still incomplete provenance if the pinned artifact resolves mutable executable dependencies underneath it.

# Interpretation

The strongest Foresight candidate remains a portable Mu evaluation law, not the GitHub Actions workflow, OpenCode plugin, filesystem staging, GitHub App token handling, or Discord notification path.

A portable extraction would likely own concepts such as:

- review stages and legal transitions;
- evidence records;
- candidate findings and classifications;
- reviewable-location constraints supplied as data;
- submission/disposition derivation;
- invariants preventing unclassified or conflicting findings from publication.

Host adapters would remain responsible for diff acquisition, filesystem staging, agent/tool projection, credentials, GitHub publication, notifications, and dependency resolution.

The rollout also supports a more general **revision-closure** process candidate for Foresight infrastructure: when execution is claimed to be reproducible or revision-bound, every executable dependency reachable from the pinned entry point must itself resolve immutably, or the unresolved edge must remain explicit evidence rather than being described as fully pinned.

The current namespace name `eta-mu.domain.review` inside the Muse-hosted implementation is historical evidence, not a sufficient ownership argument. Under Foresight's working vocabulary, the pure evaluation semantics fit Mu more directly than eta-mu runtime ownership.

# Counterevidence / limits

- Foresight has not accepted the review state machine or revision-closure candidate as workspace-wide law.
- Transitive SHA pinning materially improves reproducibility but is not a complete supply-chain proof: repository compromise, action source changes before pin selection, runner images, downloaded package registries, and external services remain separate trust surfaces.
- The present pure state machine is specialized to pull-request review and unified-diff locations. Generalizing prematurely could erase useful constraints.
- Muse and eta-mu have been actively fixing boundary failures, which means the shape is surviving fire but has not necessarily cooled into its final portable form.

# Disposition

Status: **Foresight lift candidate**.

Do not copy the current implementation wholesale into `alpha/` or another portable package. First separate evaluation semantics from GitHub/diff-specific shape and identify which laws remain true for other Mu workflows such as translation review, publication review, research finding adjudication, or artifact acceptance.

Treat revision closure as a separate process/law candidate: preserve the concrete #292/#245/#31 provenance and do not promote "all executable dependencies must be immutable" into policy until its intended trust boundary and exceptions are explicitly accepted.
