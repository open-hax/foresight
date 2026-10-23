import json,pathlib,hashlib,gzip,csv,subprocess,datetime
R=pathlib.Path('/home/err/spaces/foresight');D=R/'.ημ/stabilization/execution-20261023'
def enc(x):return (json.dumps(x,ensure_ascii=False,indent=2)+'\n').encode()
def save(p,x):p.write_bytes(enc(x))
def sha(b):return hashlib.sha256(b).hexdigest()
# Preserve every pre-existing coordinator edit before refreshing its projection.
for n in ['docs/stabilization/tracker.md','docs/stabilization/e0-clio-intent-execution.md','docs/stabilization/e0-clio-intent-census.json','docs/stabilization/e0-clio-intent-census.csv']:
 p=D/('recovered-'+pathlib.Path(n).name)
 if not p.exists():p.write_bytes((R/n).read_bytes())
a=json.loads((D/'raw/audit.json').read_bytes());old=json.loads((D/'recovered-e0-clio-intent-census.json').read_bytes());prior={p['identity']:p for p in old['pull_requests']}
rows=a['pull_requests'];assert len(rows)==442 and len({p['identity'] for p in rows})==442
s=a['summary'];s['open_head_or_reported_base_changed_from_freeze']=sum(p['state']=='OPEN' and (p['head_sha'],p['reported_pr_base_sha'])!=(p['frozen_head_sha'],p['frozen_reported_base_sha']) for p in rows)
assert s['open_head_or_reported_base_changed_from_freeze']==6
keys=['state','head_sha','reported_pr_base_sha','independent_live_base_tip_sha','native_proposed_merge_sha']
a['comparison_to_retained_window']={'completed_at':old['completed_at'],'changed_tuples':[p['identity'] for p in rows if any(p[k]!=prior[p['identity']][k] for k in keys)],'fields':keys}
save(R/'docs/stabilization/e0-clio-intent-census.json',a)
with (R/'docs/stabilization/e0-clio-intent-census.csv').open('w') as f:
 w=csv.DictWriter(f,fieldnames=list(rows[0]));w.writeheader()
 for p in rows:w.writerow({k:json.dumps(v,separators=(',',':')) if isinstance(v,list) else v for k,v in p.items()})
# Public evidence archive. Every original capture and recovered coordinator edit remains reproducible.
files=[]
for p in sorted(D.rglob('*')):
 if p.is_file():
  b=p.read_bytes();files.append({'path':str(p.relative_to(D)),'bytes':len(b),'sha256':sha(b),'text':b.decode()})
raw=enc({'schema_version':1,'kind':'e0-clio-intent-boundary-and-live-census-captures','files':files,'qualification':False});packed=gzip.compress(raw,mtime=0);digest=sha(packed);sp=R/'.ημ/stabilization/snapshots'/(digest+'.json.gz');sp.write_bytes(packed)
proof=json.loads((D/'publication-verification.json').read_bytes())
overlay={'schema_version':1,'observed_at':a['completed_at'],'root_source':'d8ece877c9a9e07a87fec12f650bced173f6b164','published_child_head':proof['published_head'],'child_source':proof['source'],'canonical_clio_candidate':'5fad794de1a9e23d6e5aaad42eaa933be1c070be','capture_snapshot':str(sp.relative_to(R)),'capture_count':len(files),'capture_raw_sha256':sha(raw),'capture_packed_sha256':digest,'census_summary':s,'census_comparison':a['comparison_to_retained_window'],'published_bytes':proof,'current_focused_gate':{'tests':23,'assertions':110,'failures':0,'errors':0,'retained_in_child':'fa117fba0b761f8aa655e26c2ae8675b1af178cd'},'bootstrap_regression':{'introduced_by':'ed0fde2f5a1902f2258e16b0b9228b0913e1ea59','red':'b0ac577cfd3c899d4baac553c870d47918933850','local_green':'14cb2742bfd1a15d5ab450b8f3ee6d3a64147790','published':False,'full_successor_gates':'pending','native_runs':[39721175244,39721175305,39721175246],'cause':'Package-wide NBB dependency configuration makes unrelated workflow tests invoke absent bb before tests.'},'native_review':{'root':'blocked; 2 unresolved, zero exact-head approvals','eta':'blocked; 1 unresolved P1, zero exact-head approvals; changing evidence during collection','clio':'CodeRabbit exact-head approval at 5fad; convergence still blocked; 63 threads zero unresolved','codex':'native account quota; reset UNKNOWN; not retried'},'root_receipt_gate':{'exit_code':2,'accepted_base':'cffc34448aa6807fc51135d7f90f0f9fe6ba7efe','new_invalid':2,'whole_history_invalid':13,'repair_or_admission':False},'local_root_verification':{'project':'25 sources / 23 submodules / 39 invariants','workspace':'24 tests /120 assertions','project_tests':'12 tests /166 assertions','lint':'0 errors /0 warnings'},'preserved':{'root_gitlinks':23,'primary_eta':'0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90','primary_clio':'788cdd3434615a7932b924e68520dbf7f88408c2','historical_bytes':True,'states':'E0 breakdown; E1 incoming; publisher in_progress','CMS_Cephalon':'paused'},'limits':['Complete authenticated publication and reconstruction/verifier composition remain unfinished.','Original G and exact executed caller are not established by candidate merge/tree equality.','Sandbox success at a762 does not qualify successor source. Carrier retains 23 warning diagnostics; artifact metadata is not restore proof.','Bootstrap GREEN is local and still awaits independent/full gates and publication.','Unarchived earlier temporary captures were lost on host resume; fresh 23/110 capture is retained in fa117 and original 809 captures remain committed at a762.','No protected adoption, credential/grant/protection/caller-pin changes, merges, deployments or milestone qualification.'],'qualification':False}
save(R/'docs/stabilization/e0-clio-intent-execution.json',overlay)
report='''# E0 Clio-backed intent and boundary execution

The published ETA checkpoint is `fa117fba0b761f8aa655e26c2ae8675b1af178cd`,
with functional boundary repair `bb82398568f7abf7f2c1103e0aa34806b7b87153`.
The private capability/recovery vault belongs to infra composition; extern owns
host objects, worker I/O, bounds, clock and descriptor observations. The forbidden
extern-to-domain dependency and raw worker host operations have causal RED/GREEN
history. Fresh retained intent tests pass **23 tests / 110 assertions**. All eight
files changed since published `a76225c0` reproduce their native GitHub blob bytes.
Broader successor tests and native qualification remain pending.

Canonical Clio candidate `5fad794de1a9e23d6e5aaad42eaa933be1c070be` remains
in [Clio #5](https://github.com/riatzukiza/clio/pull/5). It checks optional expected
descriptor identity under its owning lock before content read/append, preserving
old arities and keeping raw handles in externs. Earlier owning BB29/182,
JVM134/605, NBB109/583 and compiled109/583, independent JVM/NBB8/49 and eleven
native byte comparisons are retained in ETA's immutable 809-capture archive.
Candidate use is not accepted pin advancement. The new native CodeRabbit approval
binds exact `5fad`; review convergence remains blocked with 63 threads / zero
unresolved. Codex quota has UNKNOWN reset and is not retried.

## Fresh frozen-cohort readback

The [census JSON](e0-clio-intent-census.json) and [CSV](e0-clio-intent-census.csv)
cover the same 442 frozen identities, **October 23 02:32:04–02:32:26 UTC**:
**437 OPEN / four MERGED / one CLOSED / six changed open head/base pairs**.
Both passes agree on source tuples and proposed merges. Axxium #3 and #4 changed
heads since the retained October 22 04:14 window. The earlier count of four stays
historical; the coordinator's transient four-count statement was corrected.

There are 144 independent live base refs; 181 open PRs report a base different
from its live branch tip. Proposed parent pairs match current B/H for 295 open
PRs, use older/different parents for five, and are unavailable for 137. All tested
candidates remain null. These observations confer no source acceptance, unique-work
reconciliation, merge qualification or new-PR census coverage.

## Actual hosted results and newly reproduced regression

Sandbox [39721175314](https://github.com/open-hax/eta-mu/actions/runs/39721175314)
succeeds at older ETA head `a76225c0bea040473949cb394d8d530fd23f1afc`.
Actual merge `30ccce5f6a94601f6f527d9609b45443152d047b` has expected base/head
parents and tree `866497bd19c676df15698e5d7bcb49ad798bf3b1`, equal to that head.
The intent18/88, collection30/224, publication17/557 and CLI194/587 plus
workflow11/199 runs belong to that source. Twenty-three warning diagnostics keep
the whole-carrier warning hold open. The 327894869-byte artifact has metadata,
not download/restore proof. Original event G/executed caller custody remains absent.

Rheos39721175244, sync39721175305 and Sol39721175246 fail in workflow-contract
bootstrap: package-wide NBB dependencies invoke absent `bb` before the tests.
They do not demonstrate product-suite failures; downstream suites skip.
Introducing source is `ed0fde2f5a1902f2258e16b0b9228b0913e1ea59`.
Tests-only RED `b0ac577cfd3c899d4baac553c870d47918933850` reproduces the failure.
Local GREEN `14cb2742bfd1a15d5ab450b8f3ee6d3a64147790` narrows the dependency
configuration to dedicated intent runtime entrypoints. Owning agent reports cold
workflow11/199 with both configured NBB1.5.211 and1.5.212, focused23/110, registered
review-workflow and zero-warning lint passes. Independent/full successor gates,
publication and hosted proof are still pending. The separate native append-error
classifier P1 and collection assertion finding are under scoped causal repair.

## Preserved failures and publication holds

Root `d8ece877c9a9e07a87fec12f650bced173f6b164` run39708473352 fails.
Its deterministic job concludes success while receipt_history exits2 and emits
deterministic_output=failure; missing-App setup and required terminal gate fail.
Fresh local receipt verification also exits2: two new invalid ETA envelopes and
thirteen invalid history rows remain untouched. Root P1 and character-oracle
findings stay open. Root validation and36tests/286assertions pass; lint has zero
errors/warnings. No credential or gate waiver is introduced.

Native PR-flow root status remains blocked, two unresolved / zero approvals.
ETA remains blocked, one P1 / zero approvals, with evidence changing during the
capture. A successful old-head run never qualifies the successor. Full publisher,
original G/caller, complete reconstruction/verifier composition, native publication
readback/status and protected installation/adoption remain unfinished.

All23root pins, primary ETA `0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90` and
primary Clio `788cdd3434615a7932b924e68520dbf7f88408c2` remain fixed.
E0 is breakdown; E1 incoming; existing publisher in_progress; M0–M7 unqualified.
CMS/Cephalon remain paused. No new card, standalone plan, merge or deployment.

## Evidence custody

The [execution overlay](e0-clio-intent-execution.json) binds a content-addressed
capture archive under `.ημ/stabilization/snapshots/`; every capture records its
byte count and SHA256. Durable raw captures and reproducible acquisition scripts
are in `.ημ/stabilization/execution-20261023/`. Earlier uncommitted coordinator
projections are preserved there before refresh. October22 census archives and
all prior published reports remain intact.

The committed ETA809capture archive survives. Unarchived temporary layer captures
were lost during host resume; they are not represented as retained evidence.
Fresh focused23/110 evidence is committed at fa117. Bootstrap evidence is being
written inside the owned persistent worktree, never relying on ephemeral `/tmp`.
'''
(R/'docs/stabilization/e0-clio-intent-execution.md').write_text(report)
p=R/'docs/stabilization/tracker.md';text=p.read_text();start=text.index('Latest execution in progress:');end=text.index('Earlier execution: [native file inventory',start)
text=text[:start]+'''Latest execution: [Clio-backed intent, repaired layer boundary and bootstrap regression](e0-clio-intent-execution.md).
Published ETA checkpoint `fa117fba0b761f8aa655e26c2ae8675b1af178cd` contains
boundary GREEN `bb82398568f7abf7f2c1103e0aa34806b7b87153`: fresh23/110 passes;
eight changed published bytes verify. Earlier a762 Sandbox succeeds at its exact
merge/tree, with23warning diagnostics and artifact-metadata-only limits.
Native Rheos/sync/Sol bootstrap fails before tests on absent bb. Causal RED
`b0ac577c` precedes local GREEN `14cb2742`; full successor qualification is pending.
Clio `5fad794d` has exact-head CodeRabbit approval but convergence remains blocked.
Root receipt P1, missing-App/terminal gate, character oracle and separate ETA
classifier P1 remain open. Original G/caller and full publication/adoption stay held.

Fresh October23 02:32:04–02:32:26 UTC two-pass census: **442 frozen /437open /
four merged /one closed /six changed open head/base pairs**. Axxium#3/#4 changed
heads since the retained October22 window; zero acquisition source/proposal drift.
All23root pins and historical bytes survive. E0 breakdown, E1 incoming, publisher
in_progress, milestones unqualified; CMS/Cephalon paused. No standalone planning.

'''+text[end:];text=text.replace('Updated October 22 UTC, 2026.','Updated October 23 UTC, 2026.',1);p.write_text(text)
p=R/'docs/stabilization/work-graph.json';graph=json.loads(p.read_bytes());assert 'e0_clio_intent_execution' not in graph;graph['e0_clio_intent_execution']={'report':'docs/stabilization/e0-clio-intent-execution.md','overlay':'docs/stabilization/e0-clio-intent-execution.json','census':'docs/stabilization/e0-clio-intent-census.json','child_head':proof['published_head'],'source':proof['source'],'clio_candidate':overlay['canonical_clio_candidate'],'frozen':442,'open':437,'merged':4,'closed':1,'open_H_B_changed_from_freeze':6,'source_drift':0,'proposal_drift':0,'qualified':False,'publisher':'in_progress','CMS_Cephalon':'paused'};save(p,graph)
print(json.dumps({'snapshot':str(sp.relative_to(R)),'captures':len(files),'summary':s,'changed_since_prior':a['comparison_to_retained_window']}))
