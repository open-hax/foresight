---
category: "kanban"
labels: "muse, knoxx, harness, 3sp"
story_id: "HAC.01"
parent: "harness-actor-continuity"
type: "story"
write-id: "1793058382819-0.swc8ibhnv8otvq0ictp"
points: "3"
title: "HAC.01 — Compile and qualify the shared Muse tools in Codex"
priority: "P1"
status: "in_progress"
epic: "harness-actor-continuity"
uuid: "hac-01-codex-tools"
---

# HAC.01 — Compile and qualify the shared Muse tools in Codex

> Design: [harness actor continuity](../../notes/design/harness-actor-continuity.md)
> Research: [grounding](../../research/harness-actor-continuity.md)
> Target: muse

## Context

PR 141 provides the layered-contract plan. This slice connects its selected operating contract to host tools and Knoxx identity.

## Outcome

Compile and qualify the shared Muse tools in Codex.

## Scope

Qualify the existing single-tree MCP compiler and Codex registration independently of LOC implementation. Compare exposed tool names and argument schemas under the same existing profile in Claude/OpenCode. Layered integration is HAC.06 and remains blocked on LOC implementation.

## Non-goals

No production cutover, broad credential copying, new board implementation or automatic memory lift.

## Acceptance criteria

- [ ] GIVEN a selected profile and its registration applied to an isolated Codex configuration WHEN an isolated fresh Codex session starts THEN it discovers the registered target through that configuration and completes initialize, tools/list and the design’s read-only, non-actor-scoped `hac_fixture_read({})` fixture call (exact `HAC_READ_ONLY_FIXTURE_V1` response and unchanged state) with recorded compiler/tool-source revision, exact selected single-tree configuration/profile input paths and Git blob IDs, artifact hash and profile hash; source revision and input blob IDs must match the active single-tree inputs, and the artifact hash verifies the loaded artifact; a direct or pre-existing connection does not qualify the applied registration. The same session's tools/list must equal the expected profile-allowed Muse tool manifest reconstructed from the recorded source/config inputs, with explicit host dispositions; comparing two incomplete discovered registries is insufficient. It must also invoke at least one existing non-fixture, profile-allowed shared Muse tool with its recorded argument schema and require a successful response conforming to the recorded argument/result contract and retain its sanitized result; invocation or dispatch evidence alone cannot qualify. Select a designated read-only, non-actor-scoped shared Muse tool for this standalone route and verify no protected state changes; missing such a tool blocks that positive qualification rather than granting an actor-admission exception. Preserve the fixture's exact response and unchanged-state checks separately.
- [ ] GIVEN an actor-scoped tool WHEN an unbound or wrong-context Codex session calls it THEN HAC.02 authenticated context admission denies the call before any protected content or effect. Until HAC.02 actor binding is qualified, these tools are reported unsupported in the capability manifest and cannot execute; process cwd never authenticates a context. This restriction does not block non-actor-scoped MCP qualification.
- [ ] GIVEN existing unrelated Codex servers WHEN the registration is applied and rolled back THEN those definitions remain unchanged, the new registration is absent after rollback, and a fresh Codex session cannot discover or call it.
- [ ] GIVEN a duplicate or conflicting target registration WHEN registration is applied THEN it is rejected without changing the existing target definition or unrelated server definitions.
- [ ] GIVEN hooks or native task messages WHEN only MCP is available THEN the capability manifest reports them unsupported.
- [ ] GIVEN the same selected profile WHEN tool registries are normalized THEN allowed tool names and argument schemas agree across the supported targets, with explicit dispositions for every host-specific gap. Each Codex, Claude and OpenCode parity artifact records the compiler/tool-source revision, selected profile revision and exact configuration/profile input paths and Git blob IDs, plus its own target-specific artifact hash. Source/profile revision and input blob evidence must match across compared results; each artifact hash must be independently verified against its own actual target artifact; equal hashes are valid when target artifacts are byte-identical. Mismatched source/input evidence invalidates the comparison rather than qualifying as a host-specific gap. HAC.01 qualifies the existing single-tree compiler with source/input/artifact provenance, without requiring the not-yet-implemented LOC effective-contract hash. Git blob IDs identify exact input bytes; they are not a competing operating-contract hash. Layered reconstruction and canonical effective-contract hash qualification remain HAC.06.
- [ ] GIVEN a profile-excluded tool WHEN Codex lists or calls it THEN it is absent from tools/list and a direct tools/call is rejected before backend dispatch, invokes no tool, returns no protected result or effect and leaves relevant state unchanged.

- [ ] Before HAC.02 actor binding is qualified, omit actor-scoped tools from runtime `tools/list` and retain their explicit unsupported disposition in the separate capability manifest. Parity compares the actual registry with the selected profile’s allowed runtime registry after this exclusion and verifies the unsupported dispositions separately; actor-scoped calls cannot execute or disclose protected results.

Implementation sequencing within the 3-point standalone qualification slice: first registration/rollback, then fresh-session MCP fixture and successful read-only shared-tool evidence, then profile exclusion and source-bound cross-target parity. These use the existing single-tree compiler; missing infrastructure blocks rather than expands this slice. HAC.02 separately owns actor admission implementation; HAC.01 retains only its unsupported/non-execution boundary until that dependency is qualified. HAC.06 owns layered-contract parity. During Rheos breakdown, assign each retained criterion to these ordered verification steps and record blockers before readiness; this prose performs no status transition or runtime qualification.

## Verification

Implementation supplies named upstream test runners and sanitized end-to-end
evidence for each criterion, including negative cases. Run the affected owner's
existing test suite, then the isolated adapter scenario; missing runtime access
remains an explicit blocker. This planning card claims no executed integration
test or Rheos status transition.

## Risks

Native capability availability and deployed source can differ from checked-in
code; preserve source revision and actual observed capability separately.

---
Owner accepted and merged planning PR144 as cb73d35b0945e9961745e119dd0c1d9c8dc7dee2 and directed implementation. Breakdown: registration/rollback; fresh isolated Codex MCP fixture and real read-only tool evidence; profile denial and source-bound parity. Actor admission remains HAC02 and layered integration HAC06.

Implementation started in Muse branch codex/hac01-codex-mcp: native isolated Codex MCP registration, duplicate denial, unrelated-server preservation and guarded rollback; pure law6assertions and real CLI smoke pass. Remaining: actual Muse build/profile filtering, fixture+real-tool invocation, fresh native-session evidence and cross-target parity.

---