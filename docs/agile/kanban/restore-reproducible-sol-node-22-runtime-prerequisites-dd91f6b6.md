---
category: "kanban"
labels: "sol, review, runtime, ci"
parent: "330aa63f-f697-5bd6-9fc0-3f19fbea2be4"
type: "task"
write-id: "1791028157197-0.6nmifc8azgn9fjl9fao"
points: "3"
title: "Restore reproducible Sol Node 22 runtime prerequisites"
priority: "P1"
status: "in_progress"
uuid: "146f1b47-c6a7-5a37-996b-a381dd91f6b6"
created_at: "2026-10-03T09:08:18.822Z"
---

# Restore reproducible Sol Node 22 runtime prerequisites

## Outcome

A standalone Sol checkout installs from a frozen lock on Node 22, completes a nonempty CLJS test suite with truthful exit status, compiles the server without warnings, and starts/stops through its existing localhost health boundary.

## Context and reviewed scope

Parent: 330aa63f-f697-5bd6-9fc0-3f19fbea2be4. This is the runtime prerequisite explicitly recorded in merged riatzukiza/sol#1, reviewed head 17c568f005792f601cd949a2f9c79f774effc62b, merge d9de724a01115c894da11219b4df345c84fb9e01. Foresight#122's process planning is merged at4aa6baf780c5d85caa5c73fe908d19832f9e9f3a. It is a baseline repair, preceding persistent review-job implementation.

Observed on Node22.20.0/npm10.9.3: frozen install fails on manifest/lock drift; npm test exits0 after a missing MCP SDK import while the guarded runner and direct tests/startup fail; runner fixtures reproduce zero-test and conflicting-summary false passes. The Sol worker's .ημ/node22-prerequisite/handoff.md records commands and logs. These are failures, not unavailable-tool passes.

## Scope

- Repair Sol's own package manifest/lock dependency closure, including imported runtime modules; follow its existing package-manager policy and record the frozen graph.
- Route npm test through the existing result guard and correct its verified false-success cases. Add red/green fixtures for import/crash failure, zero tests, conflicting summaries and completed positive counters.
- Keep immutable Clojure Git pins unchanged; use no local-sibling substitution.
- Publish actual Node22 install, tests, server/test compile, health and SIGTERM evidence. If restored dependencies expose application failures, report them and keep qualification red until verified corrections exist.

## Acceptance

- [ ] Fresh standalone frozen install succeeds repeatably from the committed lock, with production imports resolvable.
- [ ] npm test exits nonzero on crashes/missing counters/zero tests/conflicting results; a completed nonempty passing CLJS suite is required for success.
- [ ] Server and test targets compile with zero warnings; report actual tests/assertions and any diagnostics.
- [ ] Existing server starts on an isolated localhost test port with production dependencies, returns its documented health response and exits cleanly on SIGTERM.
- [ ] Exact-head eligible review and repository-required checks pass before merge; historical receipts remain byte-preserved.

## Verification

Use Node22.20.0/npm10.9.3, frozen npm ci, the existing npm test/guard fixture commands, pnpm lint, both shadow compile targets, and the isolated localhost smoke fixture. Record exact commands, exit codes and counts in Sol receipts and PR evidence.

## Non-goals

No hosted review-worker implementation, Git-pin migration, service activation, deployment or production restart. New worker semantics retain their separately reviewed canonical owners and prerequisites.

---

Scope admitted as 3 points from merged Sol#1 reviewed 17c568f005792f601cd949a2f9c79f774effc62b and Foresight#122 merged 4aa6baf780c5d85caa5c73fe908d19832f9e9f3a. Node22 red evidence is recorded in the Sol worker handoff. Readiness covers standalone dependency closure and truthful test-result gating; worker hosting, activation, deployment and immutable Git-pin changes remain outside this card. If restored tests expose further application defects, report and split scope instead of counting unavailable or partial output as a pass.

Implementation is under review in riatzukiza/sol#2 at11f19fa1d4519c770d05f833f7b1a1994fd37992. Worker reports repeated frozen install165packages,125CLJS tests467assertions, zero-warning server compile, production-only152package install, real localhost health200 and SIGTERMexit0, thirteen guard fixtures and unchanged Git pins. These are local observations; hosted runtime CI currently fails and exact-head review is pending, so qualification and completion remain blocked.

Sol #2 is now merged at 72fdecca292859d92cba48b686ec6fe9c43a9133, reviewed head e4ae8d4f50625a254d1b219f9f4582156957b959. Hosted Node22 runtime CI 37116509976 and the post-merge main run 37117009854 passed: 125 tests / 467 assertions, zero-warning builds, frozen dependency installation, production health and clean SIGTERM. This supersedes the earlier pending/failing observation; it does not admit Clio migration or hosted workers. Comment rendering was repaired through the actual Rheos CLI built from unmerged PR #2 head 66e8b67951971af541503c4a556c5645eaa727c0 (complete CodeRabbit review and 153 tests / 790 assertions). Historical event bytes and prior section content are preserved. The upstream formatter still awaits review convergence; no formatter merge or card completion is claimed.

Dependency clarification: Rheos formatter PR #2 affects only the sequencing of this documentation/rendering PR stack. It is not a prerequisite for the Sol Node22 runtime acceptance criteria: Sol #2 already passed its actual runtime gates and merged at 72fdecca292859d92cba48b686ec6fe9c43a9133. The runtime scope and accepted criteria remain unchanged. This card retains its existing in_progress state until the owning board workflow records completion after the admission PR is qualified; no additional Sol runtime defect or formatter-dependent runtime requirement is claimed. Accordingly, no blocked_by or dependency edge to the formatter is asserted. The earlier receipt phrase upstream merge remains a prerequisite refers solely to the PR stack, not this runtime task.

---
