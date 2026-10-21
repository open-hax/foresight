---
category: "kanban"
labels: "stabilization, coordination, e0"
dependency: []
parent: "760f7f1e-a086-4e0a-82a5-71d2a761073d"
type: "epic"
write-id: "1792545712937-0.t413knxuvnkqi73k08"
title: "Stabilization E0 — Reconstruct the authoritative backlog"
priority: "P1"
status: "breakdown"
uuid: "stabilization-e0"
assignee: "codex"
created_at: "2026-10-20T07:25:52.971Z"
---

# E0 — Reconstruct the authoritative backlog

## Context

We need to know exactly which work exists, how branches overlap, and which source is authoritative before merging anything.

Stabilization coordination only. Preserve child ownership and existing story identities.

## Scope

- E0.1: Freeze the PR census: Capture all upstream and personal PRs with immutable head SHAs, reviews, checks and base branches.
- E0.2: Identify unique implementations: Distinguish stacked branches, duplicate source, old snapshots and changes that haven't reached upstream.
- E0.3: Reconcile Rheos ownership: Connect existing PRs, UUIDs, cards, epics and dependencies without creating duplicate tasks.
- E0.4: Preserve historical work: Secure unique code, evidence, Cephalon designs and unpublished fixes.
- E0.5: Establish campaign rules: Allow only necessary repairs during stabilization, with explicit ownership and history preservation.

The slot-by-slot exit proofs and existing-task candidates are in [the portfolio](../../stabilization/portfolio.md#e0--reconstruct-the-authoritative-backlog).

## Acceptance

Every PR has an owner, intended disposition and verified dependency relationship. The census is complete, not capped by a GitHub search-result limit.

- Resolve every slot to existing canonical work or an explicit owned gap before creating execution stories.
- Bind qualification evidence to exact source revisions and preserve prior attempts.
- Registration and board status do not qualify a milestone.

## Dependencies

None; establishes the frozen cohort.

## Evidence

[Portfolio tracker](../../stabilization/tracker.md); [source-backed work graph](../../stabilization/work-graph.json).

## Definition of done

Every PR has an owner, intended disposition and verified dependency relationship. The census is complete, not capped by a GitHub search-result limit.

---
E0 execution begins: enumerate direct source upstreams, both organization repository inventories and all riatzukiza repositories through final API pages; verify actual fork networks including renamed forks; capture exact PR head/base and complete review/check/thread pages. Reconcile against existing current-source Rheos UUIDs and dependency declarations without creating execution-story copies. Separate observed associations from accepted prerequisites and keep missing ownership/disposition evidence explicit.

Census frozen: 442 open PRs across 815 repositories (663 authenticated personal + 48 open-hax + 104 octave-commons; eight external authored PRs added). Complete list pagination and final source/membership checks pass with no drift. Native reviews/comments/checks/threads retained; two 3000-file caps repaired by complete Git-tree deltas. docs/stabilization/e0-census.md, e0-pr-census.csv, e0-work-graph.json and e0-unresolved.json bind existing work. 3821 card observations, 192 PR/card associations, 183 PR relationship observations, zero new execution tasks. 250 PRs lack explicit card matches; 441 lack native assignees; 11 distinct malformed/missing relationship tokens remain. E0 stays breakdown and M0 unqualified. Preserve original card UUIDs and histories, one owner per dependent stack, repair-only changes, exact-source evidence reuse, and unique source before any closure.

Taking explicit E0 reconciliation ownership under the current user instruction. Assignee codex owns census identity matching, dependency adjudication and blocker follow-through for this existing card. Native PR assignees, declared child owners and author identities remain historical evidence; this is not an assignment of merge authority or a claim of accepted implementation coverage. Use the frozen 442-PR census, create no tasks, and keep M0 unqualified while source custody or semantic prerequisites remain unresolved.

E0 gap decisions recorded in docs/stabilization/e0-resolution.md and e0-resolution.json; all 442 frozen PR/head rows are indexed in e0-owner-work-index.csv. Codex owns reconciliation and every residual hold on this existing E0; execution/merge authority remains unassigned by this action. Retain 192 original PR/card association rows; 34 formerly unmatched PRs now bind explicit existing work. Frozen added-file patches recover 23 exact authored card blobs across 12 PRs, with 28 existing relationships. The other 216 rows retain native PR anchors (84 constellation candidates); this does not prove implementation coverage or require duplicate cards. All 133 supplemental scope rows stay visible without importing execution work.

All 11 former missing relationship references are adjudicated: 7 legacy-ID references located, 1 existing UUID filename alias resolved, 1 malformed Knoxx target quarantined with an exact-blob YAML repair proposal, and 2 empty references rejected as no edge. Preserve child identities and admission histories. Classified all 1170 frozen card relationships and 183 PR ancestry observations; parent/epic containment and branch ancestry do not confer execution order. Retain 72 literal Knoxx supplier declarations and 2 mixed evidence cycle components against existing task/index issues, including reconciliation #324 and qualification #325. No new tasks, child mutations, merges or closures.

M0 remains UNQUALIFIED and E0 stays breakdown. Remaining holds owned here: accepted stack execution/merge owners and terminal intent; semantic coverage and source preservation; legacy/child custody and malformed-source repair; Knoxx integration/recomposition; and 4 captured-text hash mismatches (2 unique blobs) excluded from new identity authority decisions. Frozen source/census artifacts remain unchanged. The repair proposal is reviewable and tested only in scratch; it is not applied to a child.

E0 execution checkpoint: fresh exact frozen-cohort reads confirm 442 identities: 441 open, 1 native merge, 0 closed-unmerged. Five open PRs changed head/base; merged Session Mycology #1 also changed head (six total, same counting as the prior report). Merge ddb63291cf856ab59ef14f02b8346e42f6ef651b retains frozen source 4cba181e9a51da3c2cfaf871b3681a74d709058a on its ancestry; this is not semantic/merge-gate qualification. Current paginated reviews/comments/checks/statuses/files/thread metadata collected for six changed sources and Knoxx331; Axxium10 failed gate and 8 unresolved changed-open threads stay explicit.

Exact-ref base64 recovery now reproduces all 4 quarantined source observations / 2 unique Git blobs; exact bytes and bindings retained. That exact-source-recovery hold is satisfied only for those observations. Original mismatching captures stay unchanged; child status/behavior/admission remains unqualified.

Native source confirms Knoxx331 already merged at 0fdaae13f421ff9623b1bb6fe1afb7694ab68ca1 despite stale active-target prose in issue323. Complete published trees verify all32 mailbox-delta paths identical in333/335 and different from331, while375/376 retain331 bytes at those paths. Retargeting335 alone cannot implement the documented identity-before-mailbox split. Existing324/325/372/377 keep their work; no new task, child edit, closure or accepted merge schedule.

Evidence: docs/stabilization/e0-refresh.md, e0-refresh.json, e0-refresh.csv; native snapshot .ημ/stabilization/snapshots/6e886224254bf9ca7d36c6f0ffc058428250e91fdc2acd0290a0cfecfe5dabe6.json.gz (sha256 6e886224254bf9ca7d36c6f0ffc058428250e91fdc2acd0290a0cfecfe5dabe6). Original census/ledgers and all23 consumer pins retained. Canonical pr-flow status attempt failed on installed gh --slurp support; no gate pass claimed. Root validate,36tests/286assertions, lint0/0 and Rheos drift pass. Codex retains reconciliation ownership on existing E0; E0 breakdown, E1-E7 incoming, M0-M7 UNQUALIFIED. CMS/Cephalon paused; no agent work dispatched. Next source proof: published335 32-path restoration and qualified intermediate source before any active order.

Execution ownership under the active user stabilization goal: Codex owns the bounded current-head repair/review loop for existing native Clio PR riatzukiza/clio#5 (e04da66c3aefe9f3b124e275d92a1771feb04bd8) as a shared-ledger prerequisite linked through E0/E2. This reuses its existing PR work record and creates no duplicate story. Native gate now runs with isolated checksum-verified gh2.102.1; it remains blocked on three findings and current-head review convergence. Isolated checkout /tmp/foresight-clio-segments-e04da66 preserves the pinned child checkout. Reproduced unsupported-platform read-only force and unowned-temp lost-claim defects before repair; descriptor-bound directory ABA finding remains an independent open obligation. Source changes will use red/green commits, exact-head source checks and ordinary forward-only publication. CMS/Cephalon remain paused; no child pin/deployment or milestone advance.

Shared-ledger execution checkpoint: corrected two verified findings on existing riatzukiza/clio#5 using red commit d83296ff2250a8d2faa7f9441681bdd6befeb993 and published green head38c0b8c9e45160ae4fecd7d87bf6c700f62cc631. Unsupported-platform read-only replay skips preflight only; actual force errors remain visible. An unowned/unlinked temporary keeps claim-lost; acknowledged results retain link/path guards. Native fixed replies and resolved readbacks: discussion_r4331734104 and4331733656. Ordinary fast-forward preserves original source; no new work item or PR.

BB29tests/182assertions, JVM119/530, NBB94/510 and compiled Shadow94/510 pass, lint/compiler warnings0. Hosted Clio CI39520773474 succeeds at exact green head. Isolated checksum-verified gh2.102.1 and CI-pinned BB1.13.219 remove local compatibility blockers; failed initial attempts retained. Native macOS/Windows execution is not claimed.

Clio5 remains OPEN and merge-unqualified: directory ABA thread PRRT_kwDOU4VfQc6ujoR4 is still open; no current-head approval/convergence. One CodeRabbit request posted; actual cooldown ends2026-10-21T00:36:46Z. Codex account quota resetUNKNOWN. Existing Clio3 proposes MiMo route absent from current5/main; historic October9 missing-secret failure predates now-visible secret-name setup, not proof of fresh operation. No secret values read or privileged rerun.

Evidence docs/stabilization/e0-execution.md/json and native snapshot .ημ/stabilization/snapshots/91d111935d0839945eb5da25305dcb5f7f72fd05227d780cb7be1f7a9a3455d2.json.gz. Existing E0 retains coordination and Codex owns bounded Clio5 execution. E0breakdown, E1-E7incoming, M0-M7unqualified. All23 root gitlinks/child checkouts retained; no merge, closure, deployment or CMS/Cephalon restart. Next source action: parent-descriptor ABA regression and native-adapter repair on existing Clio5; retain complete-input review and current-head CI obligations.

Published versioned stabilization checkpoint in existing program scope: new open-hax/foresight#142 https://github.com/open-hax/foresight/pull/142, initial commit6b630662f279149e61e9411a0021915663b19c73 against cffc34448aa6807fc51135d7f90f0f9fe6ba7efe. Track this necessary E0 publication PR separately from frozen442; no denominator rewrite. Native readback: OPEN, ready, MERGEABLE, auto-merge absent. Independent review/current-head checks remain required. The 50 explicitly staged owned files preserve registration, append-only receipts, immutable census/resolution/refresh/source-custody proof and actual Clio5 corrective execution. Exact original CSV CRLF bytes retained with scoped attributes. Private local-census evidence and unrelated .claude work excluded. No child pointer, merge, closure, deployment, CMS/Cephalon restart or milestone pass. Source/publication attempt .ημ/stabilization/snapshots/04c53eac43df9947d1d8c7d200b1d3e9a013e7f39bd2ef16c3eca99093e0da2b.json.

E0 execution checkpoint: existing Clio #5 is now at 5a7a04c6ced7b15be940e16dc772c95b0525d55f through ordinary forward RED/GREEN commits. Parent-directory ABA is bound to a retained descriptor across enumeration, entry durability and publication. Follow-up fixes retain acquired entry descriptors and require child traversal during the Node binding capability probe. RED probes reproduced 6 JVM/NBB ABA failures, 6 descriptor-retention failures and 3 stat-only-proxy failures. Full hosted CI 39526548866 passes BB 29/182, JVM 126/556, NBB 101/534 and compiled Shadow 101/534 with zero failures/errors/compiler warnings; lint is clean. Actual executed merge candidate efe27b0fb69b0439ed572f6b6a522f4e39db2a12 has the exact head tree. Native readback: 63 threads, zero unresolved; two body findings have itemized settlements. Fresh exact-head review remains pending; Codex quota reset is unknown and MiMo route candidate Clio #3 remains unqualified. Reports: docs/stabilization/e0-directory-binding.md/json; native snapshot 5c0a7de45683e73e14052ba1f9ad8079b7ef25f7908339b66b829040dbbde938. E5 custody correction retains the existing proposed numeric contract and an explicit adoption hold, with real existing-E5 comment/readback. No tolerance/design adoption, new execution task, merge, pin update or agent restart. Previous 442 frozen / 441 open / 1 merge and 5 changed open head/base observations retain their original window; this is a targeted execution overlay. E0 stays breakdown; M0-M7 unqualified; CMS/Cephalon paused.

---