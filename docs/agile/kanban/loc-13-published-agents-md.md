---
category: "kanban"
labels: "contracts, muse, publication, adoption, 3sp"
type: "story"
story_id: "LOC.13"
points: "3"
title: "LOC.13 — Publish a standalone AGENTS.md projection that adopters can use without ~/.ημ"
dependency: ["loc-instruction-emitter"]
priority: "P2"
status: "incoming"
epic: "layered-operating-contracts"
parent: "layered-operating-contracts"
uuid: "loc-published-agents-md"
created_at: "2026-10-19T21:05:27.000Z"
---

# LOC.13 — Publish a standalone AGENTS.md projection that adopters can use without ~/.ημ

> Design: `docs/notes/design/layered-operating-contracts.md` §Layers (`:layer/root`), open question 2
> Research: `docs/research/layered-operating-contracts-inventory.md` §7
> Target repository: muse (projection), applied per published repository
> Blocked by: LOC.08 and open question 2

## Context

The user wants repositories to publish `AGENTS.md` so that others can adopt as
much of the system as they want. An outside adopter has no `~/.ημ`. On this
machine the same file would duplicate its ancestor layers.

## Outcome

A published-repository profile projects a standalone `AGENTS.md`.

- Tier 0 of the effective contract is inline.
- The tier-1 material is **committed with the repository** in a published
  directory such as `.ημ/contract/published/`.
- Pointers are repository-relative paths carrying a content hash, so a clone
  resolves them with no `~/.ημ` library and no external resolver.
- A layer marked `:layer/root true` produces a repository whose contract does
  not depend on private ancestors.

## Scope

- [ ] A published-repository profile, with its budget.
- [ ] Emit the tier-1 material into the repository's published directory.
- [ ] A pointer format: a repository-relative path plus a SHA-256 content
  hash.
- [ ] A small pointer-check script that adopters and CI can run.
- [ ] A local de-duplication rule, so that this machine's harnesses do not load
  the published file in addition to their own projection.

## Non-goals

- Rewriting every repository's `AGENTS.md` in this story. Pilot it on one
  repository.

## Acceptance criteria

- [ ] GIVEN a fresh clone on a machine with no `~/.ημ` WHEN `AGENTS.md` is read
  THEN it is self-contained for tier 0, and every pointer resolves to a file
  inside the clone whose SHA-256 matches the pointer.
- [ ] GIVEN this machine WHEN a harness starts in the pilot repository THEN the
  contract is not loaded twice.

## Verification

```bash
# on a machine (or container) with no ~/.ημ; every failure exits nonzero
set -euo pipefail
if [ -e "$HOME/.ημ" ]; then echo "FAIL: ~/.ημ present; run in a clean environment"; exit 1; fi
git clone --depth 1 <pilot-repo> /tmp/adopter && cd /tmp/adopter
root=$(pwd -P); fail=0; n=0
# every Markdown link target in AGENTS.md that is not a URL or an anchor must be a valid pointer
while read -r target; do
  case "$target" in http*://*|\#*) continue;; esac
  n=$((n+1))
  if [[ ! "$target" =~ ^\./[^#]+#sha256=[0-9a-f]{64}$ ]]; then echo "UNRECOGNIZED $target"; fail=1; continue; fi
  p=${target%%#*}; h=${target##*sha256=}
  real=$(realpath -e -- "$p" 2>/dev/null) || { echo "MISSING $p"; fail=1; continue; }
  case "$real" in "$root"/*) ;; *) echo "ESCAPES-CLONE $p"; fail=1; continue;; esac
  [ "$(sha256sum -- "$real" | cut -d' ' -f1)" = "$h" ] || { echo "HASH-MISMATCH $p"; fail=1; }
done < <(grep -oE '\]\([^)]+\)' AGENTS.md | sed -E 's/^\]\((.*)\)$/\1/')
[ "$n" -gt 0 ] || { echo "FAIL: no pointers found"; exit 1; }
exit "$fail"
```

## Risks

Pointers rot when tier-1 material moves. Mitigation: pointers carry content
hashes, and the alpha well-formedness check covers them.

## Verification state

This is a proposed Markdown input. Creating it claims no Rheos transition,
board validation or implementation.
