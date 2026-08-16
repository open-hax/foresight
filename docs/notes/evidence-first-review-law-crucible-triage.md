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
  - "https://github.com/open-hax/knoxx/commit/af773f02bec120c28231d232b6a311762d0aec0d"
  - "https://github.com/open-hax/foresight/commit/841d7e5ad36add0740ad1201921a2e516b80ddfb"
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

# Observations

## Muse now owns the review host/profile machinery

Muse commit `f26538bd` introduced the evidence-first review state machine as tools and a pure review transition namespace. Commit `a9d31f35` moved the reviewer agent, prompt, and publisher into Muse so the review profile and its machinery travel together at one pinned Muse revision.

Subsequent fixes (`1105e619`, `3510440a`) exposed real state-machine laws rather than cosmetic defects: classification must remain legal until submission, and duplicate confirmed inline locations must be rejected before publication so the reviewer can recover.

## eta-mu now owns reusable orchestration, not the whole semantic implementation

Eta-mu's reusable workflow pins/stages the Muse review profile and publishes the resulting submission artifact. Commit `2cde056e` additionally moved review-profile installation into runner-global OpenCode configuration so reusable review cannot mutate a caller's tracked `.opencode/` tree.

This is consistent with the existing center split: Muse projects host configuration and review machinery; eta-mu coordinates reusable CLJS/runtime execution.

## Knoxx and Foresight are independent consumers

[Knoxx PR #244](https://github.com/open-hax/knoxx/pull/244) merged as `af773f02` with a caller pinned to eta-mu `2cde056e` and explicit least-privilege review secrets.

[Foresight PR #28](https://github.com/open-hax/foresight/pull/28) remains open at evidence revision `841d7e5a` and proposes the same pinned reusable workflow. This is independent adoption evidence, but not yet evidence that Foresight has accepted or successfully exercised the protocol on `main`.

## Foresight main's submodule snapshot is older than the consumed review revisions

At this revision, Foresight `main` pins eta-mu at `0ed56aa` and Muse at `b4bdb0a`, while PR #28 intentionally consumes eta-mu `2cde056e`, whose review stack in turn consumes newer Muse review machinery.

That is not necessarily drift. It demonstrates that a crucible dependency can be revision-bound explicitly without advancing the whole archaeology workspace snapshot. The distinction should stay visible: **submodule snapshot provenance** and **runtime/law dependency provenance** are separate claims.

# Interpretation

The strongest Foresight candidate is a portable Mu evaluation law, not the GitHub Actions workflow, OpenCode plugin, filesystem staging, GitHub App token handling, or Discord notification path.

A portable extraction would likely own concepts such as:

- review stages and legal transitions;
- evidence records;
- candidate findings and classifications;
- reviewable-location constraints supplied as data;
- submission/disposition derivation;
- invariants preventing unclassified or conflicting findings from publication.

Host adapters would remain responsible for diff acquisition, filesystem staging, agent/tool projection, credentials, GitHub publication, and notifications.

The current namespace name `eta-mu.domain.review` inside the Muse-hosted implementation is historical evidence, not a sufficient ownership argument. Under Foresight's working vocabulary, the pure evaluation semantics fit Mu more directly than eta-mu runtime ownership.

# Counterevidence / limits

- The cited consumer evidence covers two repositories: Knoxx PR #244 is merged, while Foresight PR #28 remains open at `841d7e5a`.
- Foresight has not accepted this review protocol as a workspace-wide law.
- The present pure state machine is specialized to pull-request review and unified-diff locations. Generalizing prematurely could erase useful constraints.
- Muse and eta-mu are still actively fixing boundary failures, which means the shape is surviving fire but has not necessarily cooled into its final portable form.

# Disposition

Status: **Foresight lift candidate**.

Do not copy the current implementation wholesale into `alpha/` or another portable package. First separate evaluation semantics from GitHub/diff-specific shape and identify which laws remain true for other Mu workflows such as translation review, publication review, research finding adjudication, or artifact acceptance.
