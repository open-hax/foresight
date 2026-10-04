---
category: "kanban"
labels: "codex-cloud, workspace, law, 3sp"
story_id: "CC1.01"
parent: "codex-cloud-self-hydrating-foresight"
type: "story"
write-id: "1791131655007-0.tk7fq81cvidfpdvl1jk"
points: "3"
title: "CC1.01 — Implement and test the pinned direct-source bootstrap contract"
priority: "P1"
status: "ready"
epic: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-bootstrap-contract"
---

# CC1.01 — Implement and test the pinned direct-source bootstrap contract

## Context and starting point

**Start here after this planning PR passes review and merges. No predecessor
story is required.** Work locally in the root repository; child checkouts,
cloud environments, credentials, and a development-fork map are unnecessary.

These inputs already exist on `main`:

- `src/foresight/project.cljc`: `project`, `submodule-sources`,
  `gitmodule-declarations`, and `consolidation-inputs`.
- `src/foresight/law/project.cljc`: `validate-project`,
  `submodule-drift-errors`, and `confined-relative-path?`.
- `scripts/workspace.clj`: `parse-gitmodules` for names, paths, and URLs.
- `test/project_test.cljs`, `test/workspace_test.cljs`, and `nbb.edn`.

Existing project validation checks declarations, names, paths, and URLs. It
does not check committed gitlinks, initialized children, exact child HEADs, or
network access. This story adds the pure decision contract; later stories
collect real Git/host observations and execute its plan.

## Outcome

Given a project declaration, manifest records, and committed Git tree facts,
a pure function returns either a deterministic pinned direct-child plan or
named errors with no executable plan. A separate pure assessment accepts
supplied checkout/gate observations and decides whether readiness is proved.

## Scope

Proposed implementation paths (created by this story, not existing prerequisites):
`src/foresight/bootstrap.cljc`, `src/foresight/law/bootstrap.cljc`, and
`test/bootstrap_test.cljs`. Reuse the existing project law rather than
reimplementing source identity, path confinement, or manifest agreement.

The input contract names a full root commit, manifest records, and gitlink
records containing path, mode, and full object ID. The successful plan carries
root revision and ordered child identity/path/revision rows. All declared
`:git-submodule` sources are required, including inventory-only `.agents`;
its checkout confers no permission to inspect skills, manifests, or nested Git.
`eta` and `clobber` remain root-owned inventory inputs, not hydration targets.

## Tasks within this story

- [ ] Record the plan and assessment input/output shapes as Clojure data;
  preserve source identity, root revision, pinned child revision, and stage.
- [ ] Write failing fixtures for missing/extra gitlinks, non-`160000` modes,
  malformed object IDs, duplicate/escaping paths, and manifest/model drift.
- [ ] Implement pure planning using existing project declarations/laws; input
  order must not change the resulting plan. Enumerate only the declared direct
  sources, without traversing child contents.
- [ ] Write and satisfy assessment fixtures for absent/uninitialized children,
  wrong child HEAD, fetch failure, missing gates, failed gates, and success.
  These are supplied observations; do not perform Git or network I/O here.
- [ ] Keep tests green for the existing ownership/routing boundaries and add
  the new suite to the root hosted evidence gate using its existing NBB pattern.
- [ ] Document the resulting function signatures and fixture commands for
  CC1.02–CC1.05 in the implementation PR.

## Non-goals

Executing Git, installing tools, fetching children, changing remotes, creating
an environment, deciding provider permissions, or implementing a fork map.
This planning PR changes planning and provenance only; the proposed code belongs to CC1.01's
subsequent implementation PR.

## Acceptance criteria

- [ ] GIVEN matching project, manifest, and gitlink facts WHEN planned THEN
  each declared direct child appears once at its recorded full object ID;
  branch tips and nested-child discoveries cannot supply replacement revisions.
- [ ] GIVEN invalid or inconsistent facts WHEN planned THEN the result names
  the path and contract stage, fails closed, and supplies no executable plan.
- [ ] GIVEN `.agents`, `eta`, and `clobber` WHEN planned THEN `.agents` retains
  its pinned checkout and inventory-only authority; `eta`/`clobber` are not
  fetched and none of their nested packages/skills are traversed.
- [ ] GIVEN a failed/missing supplied checkout observation or gate WHEN
  assessed THEN readiness is false and the source/stage or gate is named.
- [ ] GIVEN exact child observations and every required gate passing for the
  same root revision WHEN assessed THEN readiness is true; declaration
  validation alone is insufficient.
- [ ] All new semantic functions are pure `.cljc`; the tests run without child
  initialization, network access, provider authentication, or future adapters.

## Verification

Baseline commands available now:

```bash
nbb scripts/project.clj validate
nbb test/project_test.cljs
nbb -cp scripts:test test/workspace_test.cljs
```

After this story creates its test file, run `nbb test/bootstrap_test.cljs` and
`clj-kondo --lint src scripts test`. Retain the initial failure and final pass
for the new laws. No cloud execution or hydrated readiness is claimed by these
local fixture results.

## Risks

Conflating declaration validity with checkout readiness would produce a false
pass. Keep those results separate. The current manifest parser omits `shallow`;
CC1.02 must read that committed Git setting at the adapter boundary, without
making it a new prerequisite for these pure fixtures.

## Anti-patterns

- Do not encode the full constellation a second time in a Codex-only manifest.
- Do not replace pinned gitlinks with `update --remote`.
- Do not infer success from an empty submodule directory.

---
Planning handoff: PR127 merged as fcfc2d17f28640203066ddfe0a22f87db7372a32 after current-head CodeRabbit/MiMo approval and all12 hosted gates at e16dc4e. CC1.01 is the entry story and has no predecessor. Begin its embedded tasks with failing pure fixtures using the existing root project model, project laws and manifest parser; proposed bootstrap files are deliverables. This readiness transition records planning admission, not story implementation or acceptance completion. The other reviewed stories retain their explicit dependency graph. Installed Rheos0.1.0 does not enforce dependency relationships on this readiness path; no substitute board engine or dependency-validation claim is made. Published handoff branch codex/pr127-ready-handoff carries these native events for the next implementation branch.
---