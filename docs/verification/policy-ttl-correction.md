# PR #105: preserve history while correcting TTL semantics

## Current proposal

Run `416ca87e-8071-4336-997d-9a4a391eb9e4` supersedes the interpretation in
`4da05854-5161-4351-a1d3-daf00fb4c3f1`. Its current counter name is
`TtlCleanedAtomicCounter<Key>`, not `ExpiringAtomicCounter<Key,Window>`.
The pinned Knoxx update matches only the key, increments the existing count,
and sets `expiresAt` only on insertion. An expired row still present before
asynchronous TTL deletion therefore increments without resetting its deadline.
This correction changes archaeology data, not Knoxx runtime behavior.

The first-writer-wins and replacement-set findings are carried forward unchanged.
The queue/mailbox investigation remains open. Supersession is recorded through
the existing `:run/consumes` relation; it does not silently hide old facts.

## Preservation and rejected input

All five original ledger files are byte-identical to parent
`f779efd0a25e3ef8f43a930c6527ee1a477932ec`. All six evidence hashes match the
GitHub Contents API at Knoxx `fb08a10a8aa32a594cc97ae11b820113de4cf386`;
CodeRabbit withdrew its hash-mismatch finding in review comment `4054490617`.
No evidence hash or source URL is changed.

Native EDN readers also reject the original numeric-leading relation keyword
names. That original file is retained, not represented as admitted history.
A separate `relations-reissued-416ca87e-8071-4336-997d-9a4a391eb9e4.edn`
stream reissues its seven semantic relations with valid, new identities.
The original run's mutable discovery manifest points to that stream. Its exact
previous bytes are archived under `.ημ/archaeology/rejected-inputs/`.
This follows `foresight.archaeology.infra/project-resources`: manifests are
indexes; ledger records are immutable. Recovery does not change prior facts.

## Executed checks

From the repository root, with Babashka and the pinned native dependencies:

```sh
bb archaeology/test/policy_ttl_correction_test.bb .
bb -cp archaeology/src:eta-mu/packages/clio/src:<malli-and-dynaload-source-path> \
  archaeology/test/policy_ttl_native_probe.bb .
```

The artifact regression passed **6 tests / 151 assertions**, zero failures or
errors. It checks original byte identities, the archived manifest, separate
successor identities and causes, unchanged evidence, supersession, retained
open work, rejection of the malformed original input, and a bounded counter
model. The initial, smaller four-test suite failed twice before the successor
existed; it is not described as the identical final suite.

The second probe executes actual Clio schema materialization, Clio's EDN reader,
Malli event/resource validation, and Foresight's production projection code.
It reproduced schema root
`8eb19ab0e4b70e205a84a627fd3e15c3ba79c0fd8c792d37e72f3c79511e0bf9`,
validated **36 events**, and composed both runs with three findings each.

Verification JSON and raw logs are in
`.ημ/verification/pr105-ttl-416ca87e-8071-4336-997d-9a4a391eb9e4/`.
The runtime was Babashka 1.13.219, Clio from eta-mu
`a2f428afd7623dcd188d535512525ee521a5ba61`, and the unchanged Foresight
source at the parent above. Malli dependencies were recovered from source-map
`sourcesContent` in that revision-bound Actions sandbox, not reimplemented.

## Limits and review boundary

These are not live MongoDB tests or a claim of complete historical Clio
canonicalization: ancestor closure outside the two resources was not loaded.
The malformed original relation file is preserved rejected input, not passed
through an invented compatibility decoder. No gate or schema is weakened.
Hosted checks and current-head independent review remain required.

The previous OpenCode review failure is separately explained by artifact
`10550014901` from run `35354081974`: the provider rejected the free-tier
invocation before a review submission existed. That is not a code verdict and
is not cleared by this data correction. No provider restriction is bypassed.
