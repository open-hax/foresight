# E0 bounded native readback execution

ETA #362 is published at db99b1682c94ecdf9f8b239709dd44cb6101c7da, binding
readback source 6e1f088fcd81dba5e9edaf526e453962875215c6. The existing publisher
now has a bounded read-only adapter for one known native review, every inline
comment and complete per-M status history. Publisher qualification remains open.

## Executed behavior and source proof

The adapter reuses fixed GET transport, fixed registry reading, enrollment
refusal, live tuples and finite request/page/byte/time budgets. It traverses all
statuses before exact-context filtering, retains unexpected writers and raw
identities/positions, and requires two stable passes bracketed by live tuples.
Full terminal array pages require another locally built page. Response URLs are
validated as data and never fetched. The absent production registry refuses
before GET; caller claims cannot enable it. Matching content or App fields
remain unsupported for authentication, with empty effects.

Tests-only RED 28de0da1 has 73 failures in 9 tests / 114 assertions before
GREEN 6fa625cd. Independent review then reproduced an inline App conflict omitted
from aggregation. RED 141c11e3 has two failures before GREEN 6e1f088f; conflicting
App 42 remains raw/shaped 42 and now refuses. Matching App 3152464 remains
observed/authentication-unsupported. Original review captures survive; eight
independent successor controls pass with no new narrow finding.

Final readback **10 tests / 130 assertions**, collection 30/224,
publication 17/557 plus 138 actual NBB/Babashka parity values and eight new portable
values pass. Full CLI 194/587 plus workflows 11/199, owning lint and release pass
with zero compiler/linter warnings. A test-process cleanup escalation diagnostic
and preexisting architecture info remain recorded. Actual fixed public GET reads
review 5609238437, its inline-comment page1 and f299bd61 status page1, alongside
actual production-registry refusal. These samples supply no full native traversal
or installation authentication. An early async summary line is excluded as a
completion witness; per-route assertions/raw responses and process exit remain.

Child checkpoint archives 160 exact captures. One valid typed receipt gives 359
rows; history remains 71 invalid/118 errors. All 309 states/planning and four
ledger prefixes verify. Checkpoint closure corrected an assertion's card-closing
newline assumption without repeating any append. Authenticated Git fetch verifies
the published commit; four native blob reads reproduce source/archive/checkpoint.

## Completed older native execution and retained holds

Sandbox 39894128562 / job 120220470567 now succeeds at earlier CD975 source and
actual merge f299bd6159b9fc32b11922a119464018f9643134. [Parent/tree proof](e0-native-cd975.json)
binds expected B/H and matching head tree. Logs show intent 29/200,
collection 30/224, CLI 194/587, workflows 11/199, Sol 142/607 and Rheos 212/1219
passing, with zero compiler/linter warnings and 22 detected carrier diagnostics.
Artifact 12476707740 records 328412696 bytes and native digest c2859ccd…; upload
metadata proves no archive contents, restoration or successor qualification.

Root 23e07bb9 run 39896317035 / job 120227496208 confirms **diff_hygiene exit 0** and
**receipt_history exit 2**. Missing-App model setup and required terminal gate fail.
Original records, receipt P1 and character oracle remain untouched. ETA before
push showed 18 pass / 4 skip, eight inline threads resolved, zero exact approvals and
twenty conservatively unclassified native positive body IDs. Current-head checks
remain pending at the retained readback. CodeRabbit request 6357019277 was posted;
request/acknowledgement is not approval. Codex reset UNKNOWN is not retried;
auto-merge stays off. [Machine evidence](e0-readback-execution.json).

## Cohort and unfinished execution

The latest complete [census](e0-native-cd975-census.json) remains October 23
03:32:30–03:32:48 UTC: **442 frozen /437 open /four merged /one closed /six changed
open head/base pairs**, source drift 0 / proposal drift 1. Original proposal movement
and later targeted stable Sol #4 reads remain separate facts. This checkpoint
claims no new full sweep or tested candidate. Axxium custody/proposal limits stay.

Known reviewID is required; unknown POST discovery remains unfinished.
Native original_commit_id stays raw under the existing current-commit law.
Original event G/executed caller/tools/input custody, full reconstruction/verifier
and authenticated publication/status composition, effective grants and protected
adoption remain unqualified. Publisher in_progress, E0 breakdown, E1 incoming,
M0–M7 unqualified. All 23 accepted pins and prior history survive. CMS/Cephalon
remain paused. No standalone planning/card or privileged execution follows.
