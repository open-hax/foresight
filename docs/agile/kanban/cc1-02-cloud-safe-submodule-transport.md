---
category: "kanban"
labels: "codex-cloud, git, submodule, 3sp"
story_id: "CC1.02"
dependency: ["codex-cloud-bootstrap-contract"]
parent: "codex-cloud-self-hydrating-foresight"
type: "story"
write-id: "1791131647175-0.9zds3n0qkwuunfzfh5u"
points: "3"
title: "CC1.02 — Make direct submodule transport cloud-safe without rewriting identity"
priority: "P1"
status: "ready"
epic: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-submodule-transport"
---

# CC1.02 — Make direct submodule transport cloud-safe

## Context and dependencies

Requires the pure plan/result shapes from CC1.01. Git already provides URL
rewriting and committed manifest/tree inspection; no cloud service or fork-map
implementation is needed for local transport fixtures.

## Outcome

A clean managed VM can fetch the GitHub-hosted direct Foresight submodules even
when no developer SSH agent or private SSH key is present, while the committed
repository declarations remain unchanged.

## Scope

An outer Git transport adapter consumes the CC1.01 plan. It reads committed
manifest settings, including `shallow`, using Git's configuration surface;
the existing `workspace/parse-gitmodules` supplies only names, paths, and URLs.
Preserve declarations and constrain transport changes to this invocation.

## Tasks within this story

- [ ] Add local Git fixture tests for SSH-form GitHub URLs, exact pinned
  objects, declared shallow policy, and an inaccessible source.
- [ ] Implement command/task-scoped HTTPS rewriting with no local SSH-agent
  dependency; scope it only to the supported GitHub transport forms.
- [ ] Define the ownership/lifetime of temporary configuration and restore it
  on success, failure, and interruption. Do not overwrite user Git settings.
- [ ] Verify the transport operation preserves existing authorized child write
  remotes; keep checkout transport distinct from persistent development remotes.
- [ ] Return sanitized source/stage diagnostics and hand the adapter contract
  to CC1.03.

## Non-goals

Hydrating the whole suite, granting repository access, persisting credentials,
modifying committed `.gitmodules`, or installing every child's dependencies.

## Acceptance criteria

- [ ] GIVEN a fresh host with HTTPS GitHub access but no SSH agent WHEN bootstrap
  initializes public direct children THEN GitHub SSH-style submodule URLs are
  translated at the transport/configuration boundary and checkout succeeds.
- [ ] GIVEN bootstrap completes WHEN `git diff -- .gitmodules` runs THEN the
  committed manifest is unchanged solely because the host uses HTTPS.
- [ ] GIVEN a private or otherwise unauthorized child WHEN transport cannot
  authenticate THEN bootstrap reports that repository as inaccessible and does
  not silently continue with an empty source.
- [ ] GIVEN setup logs and Git configuration WHEN inspected THEN no credential,
  token, private key, or credential-bearing URL is written into the repository.
- [ ] GIVEN pre-existing host/child Git settings WHEN transport succeeds,
  fails, or is interrupted THEN unrelated settings and write remotes retain
  their prior values and temporary settings do not escape their owned scope.

## Candidate mechanism

Prefer invocation-scoped `url.<https-github>.insteadOf` configuration passed to
Git. A broad persistent rewrite or unconditional `git submodule sync` could
overwrite the write remotes CC2.03 later configures; neither is the default.
The implementation may choose another mechanism that preserves the same laws.

## Verification

Exercise the transport adapter with SSH-form GitHub URLs and no available SSH
identity. Verify exact gitlink checkout and an unchanged committed manifest.
Local fixtures may use controlled Git repositories/transport substitutes;
actual GitHub HTTPS access is recorded separately during CC1.05 acceptance.

## Risks

A working developer SSH agent can mask an adapter failure. Disable it in the
fixture environment. Error messages and effective configuration must be
sanitized before retaining evidence.

## Anti-patterns

- Do not commit host-specific HTTPS rewrites into every submodule stanza merely
  to make one executor work.
- Do not inject reusable secrets into `.gitmodules`, shell history, or logs.