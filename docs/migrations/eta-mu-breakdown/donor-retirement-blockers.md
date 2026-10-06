# E1.11 — Observed donor-retirement blockers

Produced 2026-09-20 during PR #96 review. **Scope:** an exact-revision reference
scan of two eta-mu trees. It is not a build, a test run, or a closure proof.

## Why this file exists

PR #96 states that "E1.11 donor retirement and E1.12 final documentation transfer
remain incomplete", and the E1.11 card is `todo` behind E1.07, E1.08 and E1.09.
The pull request nevertheless proposed advancing the root `eta-mu` gitlink from
`0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90` to
`c2bbf7547592cb9e0c82eee01c2b01555c6cee68`. That candidate revision is two
commits of donor retirement:

| Commit | Subject |
|---|---|
| `af02f4e` | `chore(E1.11): retire extracted donor packages` |
| `c2bbf75` | `chore: extract receipt-river and axxium to independent repos` |

Pinning it would have performed E1.11 through a gitlink while E1.11's own
prerequisites remain blocked. The gitlink is therefore restored to the donor
baseline `0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90`, which is also `main`'s pin.

## Observed live inbound references at the candidate revision

E1.11's acceptance requires zero live inbound edges before a donor directory is
deleted. The candidate revision does not satisfy it. Comparing the donor baseline
with the candidate, retirement introduces **nine new unresolvable cross-package
references**. The baseline's two pre-existing ones are listed for contrast.

### Introduced by retirement

`packages/kondo-config` is deleted while eight remaining packages still declare
`:config-paths ["../../kondo-config/clj-kondo.exports/open-hax/kondo-config"]`:

- `packages/e2e/.clj-kondo/config.edn`
- `packages/eta-mu/.clj-kondo/config.edn`
- `packages/extensions/.clj-kondo/config.edn`
- `packages/fork-tax/.clj-kondo/config.edn`
- `packages/mcp-contracts/.clj-kondo/config.edn`
- `packages/protocols/.clj-kondo/config.edn`
- `packages/terminal-ui/.clj-kondo/config.edn`
- `packages/turn-processor/.clj-kondo/config.edn`

`packages/receipt-river` is deleted while the CLI still builds from its sources:

- `packages/eta-mu/shadow-cljs.edn` retains source path `../receipt-river/src/cljs`

`closure-manifest.md` already recorded the first of these as a standing
constraint: the shared kondo-config "must not be deleted when first client
moves". The observation confirms that constraint was not honoured.

### Pre-existing at the donor baseline (not caused by retirement)

- `packages/contracts/output/.clj-kondo/config.edn` — `../../kondo-config/...`
  resolves one level short of `packages/kondo-config`
- `packages/sol/shadow-cljs.edn` — `../katamorph/src/cljs`, satisfied by a Git
  dependency rather than a sibling directory

## Reproduction

```bash
git clone https://github.com/open-hax/eta-mu && cd eta-mu
for rev in 0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90 \
           c2bbf7547592cb9e0c82eee01c2b01555c6cee68; do
  echo "== $rev"
  git ls-tree --name-only "$rev:packages"
  git grep -n "kondo-config" "$rev" -- 'packages/*/.clj-kondo/config.edn'
  git show "$rev:packages/eta-mu/shadow-cljs.edn" | grep '\.\./'
done
```

`git ls-tree --name-only <rev>:packages` shows the baseline carrying all twenty
package directories and the candidate carrying eleven.

## Disposition

- The root `eta-mu` gitlink stays at the donor baseline until E1.11's
  prerequisites close. Restoring it is not a revert of the extraction; the nine
  child registrations are unaffected.
- The nine references above are E1.11 work items. Each needs a replacement
  coordinate and consumer evidence before its donor directory is removed.
- `packages/kondo-config` is shared tooling, not an extraction target. E1.07's
  "retained or lawfully replaced" clause governs it.
- This observation is an exact-revision reference scan only. It does not
  establish that the remaining references are the complete set; E1.01's
  exhaustive closure proof still owns that.
