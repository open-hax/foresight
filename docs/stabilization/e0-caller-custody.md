# Root review caller credential custody

The existing Foresight PR142 caller forwarded App signing credentials and a
publication webhook into the legacy eta-mu review runner. That runner checks
out candidate source, executes review tools/model code and later mints an App
token in the same job. This conflicts with the supplied candidate isolation
policy, regardless of historical acceptance of that runner arrangement.

The bounded correction removes the caller's three secret mappings. Its pinned
workflow, dependency revisions, deterministic gates and read-only permissions
are unchanged. The called workflow declares those secret inputs optional and
selects no job environment. Its missing-App guard precedes model execution and
signing, so the legacy reviewer remains explicitly unavailable rather than
receiving signing authority. No missing review or failed gate becomes a pass.

[GitHub's reusable-workflow contract](https://docs.github.com/en/actions/how-tos/reuse-automations/reuse-workflows)
requires explicit secret forwarding or inheritance; the automatic GitHub token
remains subject to the caller's read-only permission ceiling. This correction
does not establish protection outside candidate control or implement anonymous
inference/trusted publication. The existing producer, publisher and hosted-proof
cards recovered in [review-route custody](e0-review-route.md) retain that work.
No child source, consumer pin, board state or paused agent changes.

Before/after YAML comparison verifies that only the secret map changes parsed
configuration. Root workspace laws pass (24 tests / 120 assertions), project
laws pass (12 / 166), project/catalog validation pass, lint reports zero errors
and warnings, and Rheos reports no drift. Workspace negative-case diagnostics
are fixtures. Hosted execution for the corrected caller is not yet observed in
this source checkpoint; later native readbacks must retain actual failures.

The additive content-addressed caller supplement records exact configuration,
verification, existing E1 comment/readback and source hashes. The earlier E0
topology/reviewer-route attempt remains immutable. An initial local verifier
used JSON to copy parsed YAML keys, changing YAML 1.1's boolean `on` key; the
corrected verifier preserves keys with a deep copy. This was a verifier failure,
not a product configuration defect.
