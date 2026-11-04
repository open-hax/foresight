---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["93a76dad-678a-4980-86fb-ba0fbb5e8203", "29caa8e3-289a-416c-9850-8ef9cc769e88"]
parent: "e616187d-ba13-42b1-addd-aa4d6244a5be"
type: "task"
write-id: "1793748632867-0.dwle7xjsroqpu5qmrl9"
points: "8"
title: "Oikos: Bind all egress paths to the mojomast GovOne policy session"
priority: "P1"
status: "in_progress"
uuid: "c703893f-aea4-41c2-9eab-c3bd37b28c9b"
created_at: "2026-11-03T15:11:11.241Z"
---

# Oikos: Bind all egress paths to the mojomast GovOne policy session

## Context

Private open-hax/oikos at /home/err/spaces/foresight/oikos is a local-first shared workbench. Parent now admits only the corrected S6A P1 source phase under .ημ/runs/oikos-program-20261103/permits/s6a-p1-source-v2.edn, after independent planning review and same-reviewer confirmation settling all four findings. Exact reviewed S2A c399b60e1676c47778445c1098eb541ab38743cb and S4A 8ae64df4c7ad170c5de9206f4d3f712d44c60b43 provide pure contract components, not whole-producer completion or an operational read issuer. Source composition must start at accepted 008b0611198d13eb503bb57d5ef69d8dec1c268c plus only the six permitted S4A additions, with no existing-file overwrite. Independent composition review and explicit parent green light precede any new P1 RED/GREEN. This is not full GovOne/all-path/all-mode admission; P2/P3/P4 and whole-task completion remain held.

## Outcome

Bind all egress paths to the mojomast GovOne policy session.

## Scope

Reuse mojomast/govone gateway APIs/contracts for local_only/hybrid_redacted/external_approved across text, images, derived labels and tools.

## Non-goals

No alternate policy evaluator, direct provider retry, implicit approval from a free route, raw data in Lens evidence, or live cloud calls.

## Acceptance criteria

- [ ] No egress occurs without the scoped GovOne policy session; expiry, revocation, gateway outage and attempted direct retry fail closed.
- [ ] Images are redacted under supported contract or denied; unsupported redaction never becomes text-only privacy approval; no second policy engine.

## Verification

Deny-network fixtures instrument every payload path and provider bypass; local_only produces zero unauthorized external/cloud/third-party egress, permits only explicitly authenticated allowlisted local-service/loopback requests and denies untrusted local/private-IP destinations. Image/text/tool policy failures reject before transport without direct retry. These are future acceptance checks, not executed implementation evidence.

### Current P1 source-phase verification

- [x] Corrected planning inputs independently approved: reviews/s6a-prerequisite-and-api-plan-v1.edn, s6a-prerequisite-and-api-plan-review-v1.edn, s6a-p1-corrections-v2.edn and s6a-p1-fix-review-v2.edn in .ημ/runs/oikos-program-20261103. Permit s6a-p1-source-v2.edn binds exact prerequisite freezes and settles planning findings only; it supplies no native artifact attestation or implementation evidence.
- [ ] Compose accepted 008b0611198d13eb503bb57d5ef69d8dec1c268c with exactly the six named additions from 8ae64df4c7ad170c5de9206f4d3f712d44c60b43; preserve existing files and nine frozen S2A files. Obtain independent composition approval and explicit parent green light before P1 code.
- [ ] P1 handles exactly one synthetic USER text message, closed source/ref/revision/optional named derivation, inert preparation/serialization and fixed input-free denial/unknown classifications. Prepared authority is none. Extra messages and unsupported structured modalities deny; no universal encoded-string detector or authenticated provenance is claimed. Unsafe root/nested host values must be refused before traversal, iteration or coercion.
- [ ] Meaningful RED precedes GREEN; run explicit bb scripts/test_egress.clj, bb scripts/test_capability.clj, bb scripts/test_desktop.clj, bb test and bb lint using existing pinned Clio/cache only. Retain actual BB/NBB law parity, NBB offline-adapter adversaries, baseline counts, zero-accessor/zero-effect evidence and zero new warnings; obtain independent exact-head code review.

The adapter and exercised import/call paths must never resolve content, read credentials, create sessions, open sockets, call providers or spawn processes; explicit test-runner processes are verification only. P2 transport/second-hop, P3 authentic local_only trial, P4 real hybrid/external transformations, private-read authority, multi-message history and all-path runtime integration remain held. No operational issuer is provided by S2A's independently qualified fadf3478 composition pending PR. The inherited 470 BB transport stays operationally held absent explicit accepted-source import; diagnostic suites confer no transport qualification. Full-task acceptance checkboxes above remain unchecked.

## Risks

A background exporter or derived label can bypass prompt-only policy; complete histories increase privacy impact.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- https://github.com/mojomast/govone
- /home/err/spaces/foresight/AGENTS.md
- /home/err/docs/notes/design/epic-emacs-mcp.md
- https://github.com/mojomast/govone is operator-specified integration identity; API compatibility requires inspection during implementation.

## Dependencies

- 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A): immutable event/payload provenance.
- 29caa8e3-289a-416c-9850-8ef9cc769e88 (S4A): shared scoped local capability contract, not the whole E4 client matrix.

---
Planning correction supersedes the original whole-E4 prerequisite and the original local_only verification wording “zero network calls.”

Dependencies: E2 (3b9279b5-aafd-431a-90a9-128ce4415de0) plus shared local capability S4A (29caa8e3-289a-416c-9850-8ef9cc769e88); the entire E4/client-matrix epic is not an execution prerequisite. GovOne admission must precede S4B qualification with real remote tool results. Only synthetic tests with no private data may precede that admission.

Corrected acceptance criteria: local_only permits zero unauthorized cloud/third-party egress. Explicitly authenticated, allowlisted loopback/local-service transports may be used; a private IP address alone is not trusted and grants no authorization. Existing GovOne policy-session/scopes remain authoritative. Denied or unavailable routes fail closed, without direct-provider retry or fallback.

Corrected verification: separately test an explicitly authenticated allowlisted local request that is permitted, an external destination denied in local_only, and unauthenticated/unallowlisted local or private-IP destinations denied. Cover expired scope, gateway outage and attempted fallback; no denied route is retried directly. This is an incoming planning correction, not completed verification or authorization for a live request.

Native findings 4456856407/4456856471/4456856521: root local_only wording now matches authenticated allowlisted local transport with zero unauthorized external/cloud/third-party egress. Private IP alone grants no trust; unauthenticated/unallowlisted local routes deny. S2A supplies immutable payload provenance and S4A named scoped capabilities; no whole client-matrix prerequisite. Existing historical E2/E4 comments are superseded only for current child execution dependencies by the precise S2A+S4A edge. Current native child prerequisites synchronized to the user-approved precise plan: 93a76dad-678a-4980-86fb-ba0fbb5e8203 (S2A), 29caa8e3-289a-416c-9850-8ef9cc769e88 (S4A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Parent authorizes ONLY corrected S6A P1 source admission under .ημ/runs/oikos-program-20261103/permits/s6a-p1-source-v2.edn. Inputs: reviews/s6a-prerequisite-and-api-plan-v1.edn (SHA256 df20d2c4fa9f32f8d3eb3a0b8982cf0fe614e2f3bd05f862277e6a8ba8d93e02), s6a-prerequisite-and-api-plan-review-v1.edn (d93b0b9e5135820d3faa0d2fc376797297bc212d1239087eed29c00713311908), corrections s6a-p1-corrections-v2.edn (2cd190543b6ec4d8e9ce5e9b5ad104b1768472f27470589de9ec0b972de29e54), same-independent-reviewer s6a-p1-fix-review-v2.edn (8e50024ee8518646d6431aa7a111858c393731221ca437197a9eacf8537ec749) approved corrected P1 source phase, all four planning findings settled. Prerequisite permits s2a-contract-c399b60e.edn SHA256 4783ce94b130ca169356359395386aedbe652f73e84be51f9f55862f2d2c2d39 and s4a-contract-8ae64df4.edn SHA256 c6dc4e99932b492f518ae7e2853ed84b081f64d5f3f44d1162027215f4b77d53 bind pure sources c399b60e1676c47778445c1098eb541ab38743cb and 8ae64df4c7ad170c5de9206f4d3f712d44c60b43. S2A contract oikos.desktop.safe-reference/v1 digest 3cf52cca7310d1bf628f555fc6ffc86d416f55ebe527a7007f0d25eaa28eaa49; S4A oikos.capability/v1 digest 2a82066221bfe57cb326f0fecce12082505ddfdbcdd53ab986451f69d67e884b; Clio 788cdd3434615a7932b924e68520dbf7f88408c2. These are exact pure component prerequisites, NOT whole-producer completion or operational read authority. S2A actual qualified composition fadf3478 remains pending PR and supplies no operational issuer. First source phase: accepted base 008b0611198d13eb503bb57d5ef69d8dec1c268c plus exactly six S4A additions named in the permit; no existing-file overwrite or whole-S4A merge, preserve nine S2A files. Independently review composition and obtain explicit parent green light BEFORE new P1 RED/GREEN. Only ONE synthetic USER text message, closed source/ref/revision/derivation, inert portable preparation/serialization and fixed safe denial/unknown classification; prepared authority none. No transport, credentials, resolver, sessions, sockets, providers or adapter subprocess effects. Eight-file new-P1 ceiling; additional extern requires named grant. All P1 code/verification checks remain unexecuted here; full task AC remains unchecked. P2/P3/P4, real GovOne runtime, authenticated local_only policy proof, private-read authority, multi-message/all-path modes and whole-task completion are held. Inherited 470 BB transport remains operationally held until explicit accepted-source import; diagnostic tests grant no protocol qualification. Ordinary Rheos FSM/WIP admission is not native artifact attestation. Retain S2A/S4A UUID dependencies, E6 parent, labels and historical comments. No other task/epic admission or effect permission; zero-spend/synthetic sandbox restrictions remain.

---