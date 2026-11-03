---
category: "kanban"
labels: "oikos, planning, local-first, task"
dependency: ["59774be2-cc61-4708-9030-8b7b42935ec3", "0db50b53-6c7a-489a-bff9-048d57ce4cbb"]
parent: "92b529b9-256d-4654-8aa6-5f1884a8adaa"
type: "task"
write-id: "1793727207052-0.b2pn7g7lv2hk9pgs5f0"
points: "8"
title: "Oikos: Adapt existing desktop index to bounded local OCR and E4B perception"
priority: "P1"
status: "incoming"
uuid: "0e124c65-694e-44f1-9959-ee83e81ca22b"
created_at: "2026-11-03T15:11:10.348Z"
---

# Oikos: Adapt existing desktop index to bounded local OCR and E4B perception

## Context

Planning only for private open-hax/oikos at /home/err/spaces/foresight/oikos. Desktop is a local-first shared workbench. Initial incoming proposal; planning review is not complete. Creation authorizes no implementation, live configuration, deployment, inference, upload or spending. This focused slice belongs to Oikos: continuous local perception and event-backed knowledge.

## Outcome

Adapt existing desktop index to bounded local OCR and E4B perception.

## Scope

Define portable .cljc perception shapes and pure provenance, supplier attribution, resource admission and index-projection laws first. Then bind existing NPU OCR text+boxes and resource-admitted local llama.cpp E4B through outer adapters; runtime-specific fields cannot define domain authority. Project into the existing desktop index.

## Non-goals

No cloud egress, automatic model training, duplicate desktop index, local-trust-is-absolute claim or uncontrolled resident process launch.

## Acceptance criteria

- [ ] Frame/source hash, pixel dimensions/transforms and observation revision preserve box provenance; supplier text and model-derived facts remain separately attributed, even when their values match.
- [ ] A capture observation's immutable observation ID is its originating admitted Clio capture event ID, distinct from exact frame-content hash and window lifetime identity. A repeated capture with identical bytes is a new observation/event; exact redelivery of the same Clio event ID is idempotent, and conflicting reuse is rejected. The canonical projection key is observation ID plus supplier/model identity and version plus render/transform version; identical key redelivery does not append a duplicate, while a changed model/render version yields a separately attributed projection of the same observation. Derived result events retain this source observation ID even when they have their own event IDs.
- [ ] Existing desktop-index IDs `window:<class>:<wid>` are mutable retrieval compatibility aliases only, not immutable observation IDs. Clio runtime append generates a new UUID per call; exact-event ledger append retries preserve the admitted event ID. Content hashes never silently merge observations or lifetimes. Oikos owns observation/projection laws and reuses Clio's event kernel.
- [ ] Idle/resource admission, debounce, bounded queue health, pause, sensitive-app exclusion and stop are explicit; Ollama is not runtime authority.

### S5A stop barrier

- [ ] A permission generation gates capture, supplier dispatch and publication separately; sensitive-app exclusions are evaluated before capture. Stop/revocation invalidates outstanding work, rejects queued/new work and acknowledges within the frozen resource profile's stop bound only after the no-new-publication barrier holds. Late successful replies and reconnect cannot index content, log sensitive payloads or revive a revoked generation.

Future verification: deferred synthetic suppliers return success after stop/revocation injected before capture, before dispatch and before index/event publication. Assert excluded apps are never captured, forbidden dispatches/writes/diagnostic payloads remain zero, stop acknowledgement meets the frozen bound, and late replies/reconnect cannot publish or resume revoked work. Missing generation or missing stop bound denies execution; cancellation acknowledgement alone does not prove publication stopped.

### S5A index isolation

- [ ] Explicit agent-owned development bindings select cache roots, index project and label/read/write/delete namespaces. Missing, unresolved or colliding installed/development bindings deny before any I/O, including reads; installed cache/index defaults are forbidden. Reusing the index contract does not authorize reading or mutating the operator's installed projection.

Future verification: fake installed and development cache/index/label stores contain distinct sentinels. Capture, projection, label retention, retrieval, deletion and cleanup use only development bindings; installed read spies remain zero and installed bytes unchanged. Omitted project/namespace/cache bindings and normalized/resolved aliases or overlapping roots fail before any store I/O.

- [ ] Safe-reference propagation from S5B: immutable perception events contain only the reviewed closed safe-metadata allowlist and deletable content references admitted through Clio. Raw image/OCR and secret-bearing titles/paths/labels or echoed denial payloads remain outside immutable events/diagnostics. Classify hashes and IDs rather than assuming anonymity; no separate privacy engine.

Future verification: synthetic markers in supplier output and all rejected metadata stay out of event/diagnostic retention; content erasure leaves valid causal references and replay cannot reconstruct erased perception payloads.

### S5A supplier resource ownership

- [ ] The frozen resource profile accounts for admitted supplier-side queued/in-flight work and retained/decoded buffers until completion or termination is established. Client timeout or disconnect alone cannot release reservations or admit replacement work as though the original job ended. If the selected supplier cannot establish bounded ownership/accounting, deny its admission; do not implicitly reconfigure an operator-owned OCR/model service. Revoking publication and proving supplier termination are distinct outcomes.

Future verification: a deferred synthetic supplier continues work after caller timeout/disconnect. Reservations remain charged; repeated timeout/reconnect cannot exceed the admitted concurrency/byte bounds or grow unaccounted work. Capacity is released only on established completion/termination. Unknown supplier ownership denies dispatch; stop acknowledgement reports unresolved resource ownership rather than claiming quiescence, and no installed supplier configuration changes.

## Verification

Frozen synthetic frame tests cover equal supplier/model text with distinct provenance, identical-byte recapture with new observation IDs, exact event redelivery, conflicting event-ID reuse, model/render-version changes and compatibility-alias updates without history overwrite. Resource/overload/cancel harness proves bounded operation, one projection per canonical key and no unauthorized external egress. These are future acceptance checks, not executed implementation evidence.

## Risks

Local screenshots and derived labels remain sensitive; deletion from projections must not rewrite immutable history or retain forbidden raw payloads.

## Source anchors

Dated planning evidence: 2026-11-03. Operator requirements are reported intent, not live runtime proof. Cross-repository absolute paths identify source ownership, not a promotion of domain authority.
- /home/err/docs/notes/design/epic-workspace-go.md
- /home/err/spaces/foresight/docs/agile/kanban/qualify-the-bounded-colab-gemma-retrieval-research-pilot-and-retain-production-holdback.md

## Dependencies

- 59774be2-cc61-4708-9030-8b7b42935ec3 (S2B): reconciled observation revisions, lifetime identity and reconnect invalidation.
- 0db50b53-6c7a-489a-bff9-048d57ce4cbb (S7A)
- Baseline edge is S7A only, not ongoing knowledge or optional cloud/12B comparison; avoids a knowledge/qualification cycle.

## Initial runtime and NPU baseline update — 2026-11-03

Operator-reported follow-up evidence (not remeasured by this planning writer): the expected local llama.cpp E4B endpoint on port8082 refuses connections, and the previously observed transient host runner on port45265 has disappeared. This supersedes any earlier description of that transient runner as currently active. Stable endpoint/runtime qualification is first, before any actual model quality claim; no E4B semantic baseline is established.

The NPU live baseline used 3 synthetic 960x600 images containing 9 lines: 9 valid boxes, but exact normalized text only 3/9, with reported inference 130–286 ms. Box/schema validity is not text accuracy or model semantic correctness. Semantic performance remains unmeasured. No private capture, cloud calls or 12B load occurred in the reported baseline. Future measurements require pinned runtime/model/mmproj/hash/hardware/time/context and resource admission; do not infer LLM NPU support from OCR.

Additional acceptance check: preserve this reported baseline with synthetic source/coordinate provenance, distinguish supplier timings from end-to-end latency, and deny model-quality conclusions until stable runtime qualification and independent held-out semantic evaluation exist. No new inference, capture, cloud request or model load is authorized by this card.

---
Planning correction supersedes qualitative-only resource acceptance and verification. Before any admitted execution, require a versioned, frozen resource profile specifying memory and CPU limits, maximum queued frames and total queued bytes, overflow behavior, inference deadline, and maximum pause/stop latency. Measure candidate thresholds in an isolated resource-admitted fixture, then freeze the selected profile and its identity before the admitted run; do not invent fixed numeric limits now or tune them silently during qualification.

Corrected acceptance criteria: execution refuses a missing/unfrozen/incomplete profile; the profile covers every listed bound and the queue overflow policy. Corrected verification: overload and reconnect tests assert the configured memory/CPU/queue frame/byte bounds, declared overflow behavior, inference deadline and pause/stop latency; retain profile version/hash and measured results. Reconnect cannot discard resource admission or silently reset limits; violations are explicit failures. No resource measurement, inference or completed verification is claimed by this comment.

Native findings 4456856420/4456856431 and review 5755016882 item cr-comment:v1:efd09d07c97f26c80451d912: define portable perception/provenance/resource/index laws before adapters; equal supplier/model text remains distinctly attributed. Observation ID is not frame-content hash or window alias. Identical-byte recapture creates a new observation; exact Clio event-ID redelivery is idempotent. Projection key is observation ID plus supplier/model version plus render/transform version; changed versions remain separately attributed. window:<class>:<wid> is retrieval compatibility only. S2B supplies reconciled currentness and S7A frozen baseline; missing actual model/resource proof stays blocked. Current native child prerequisites synchronized to the user-approved precise plan: 59774be2-cc61-4708-9030-8b7b42935ec3 (S2B), 0db50b53-6c7a-489a-bff9-048d57ce4cbb (S7A). Parent relationship is preserved and is not an implicit gating edge. All checks stated here are future acceptance requirements, not executed implementation results. Earlier dependency prose in historical comments is superseded by this correction for current execution edges; historical event/comment bytes remain unchanged. Policy correction — sandbox/dev and synthetic data only; no personal capture. Default raw retention is 24h images and 7d text; propagate deletion to derived/retrieval payloads, with explicit consent for accepted knowledge. Automatic actions are agent-owned-resources-only; operator resources need explicit approval. Existing models only; 12B download/load/restart is held. Any cloud experiment is synthetic, GovOne-authorized, verified free and 0 spend only; no allocation or inherited pilot budget. Remote publication remains held. Qualified program merges are authorized only after their required gates; this is not a readiness override, implementation permission or native review approval.

Observation-key clarification for native finding 4456856431: the canonical observation ID is the originating admitted Clio capture event ID, not a content hash, retrieval alias or newly generated derived-result event ID. Derived events keep that source observation ID. The projection key also includes supplier/model identity as well as version and render/transform version, so equal version strings from distinct suppliers cannot collide. This is an Oikos portable-law planning contract, not an assertion that the existing adapter already implements it. Prior history is retained and no execution/status advancement is authorized.

Consensus planning correction v1: root incoming acceptance/verification now explicitly specifies s5a/stop-barrier, s5a/index-isolation. See the corresponding named root subsections for the contract and concrete future positive/negative fixtures. Also propagates s5b/safe-ledger to perception admission and diagnostic retention. Only the 12 consensus-remaining gaps in skeptics/reconciliation.edn are in scope; the 23 disputed findings receive no settlement/rejection here. All added scenarios are future acceptance requirements, not implemented or executed behavior. Existing dependency vectors, parent relationships, incoming status, historical events and prior correction text remain preserved. Synthetic sandbox/dev-only, zero-spend, existing-model and remote-publication holdbacks remain unchanged; no status advancement or execution permission.

Source-grounded adjudication of :s5a/supplier-accounting: root acceptance now keeps admitted supplier queued/in-flight work and retained/decoded buffers charged until completion or termination is established. Client timeout/disconnect alone cannot release reservations or admit replacements. Deferred-supplier timeout/reconnect fixtures must prove bounded concurrency/bytes; unknown bounded supplier ownership denies dispatch. Stop/publication revocation is separate from termination/quiescence; no implicit operator-supplier reconfiguration is authorized. This is a future acceptance clarification based on inspected source behavior, not a measured running-service result.

---