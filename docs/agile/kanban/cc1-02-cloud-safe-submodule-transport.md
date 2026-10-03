---
category: "kanban"
labels: ["codex-cloud", "git", "submodule", "3sp"]
type: "story"
story_id: "CC1.02"
points: "3"
title: "CC1.02 — Make direct submodule transport cloud-safe without rewriting identity"
blocked_by: ["codex-cloud-bootstrap-contract"]
priority: "P1"
status: "incoming"
epic: "codex-cloud-self-hydrating-foresight"
uuid: "codex-cloud-submodule-transport"
---

# CC1.02 — Make direct submodule transport cloud-safe

## Outcome

A clean managed VM can fetch the GitHub-hosted direct Foresight submodules even
when no developer SSH agent or private SSH key is present, while the committed
repository declarations remain unchanged.

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

## Candidate mechanism

Prefer a reversible Git URL rewrite such as a task-local or user-level
`url.<https-github>.insteadOf` mapping followed by `git submodule sync` and
an exact pinned `git submodule update --init`. The implementation may choose a
different mechanism if it preserves the same laws.

## Verification

Exercise the transport adapter with SSH-form GitHub URLs and no available SSH
identity. Verify exact gitlink checkout and an unchanged committed manifest.

## Anti-patterns

- Do not commit host-specific HTTPS rewrites into every submodule stanza merely
  to make one executor work.
- Do not inject reusable secrets into `.gitmodules`, shell history, or logs.
