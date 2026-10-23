import pathlib,json,subprocess,hashlib,gzip,datetime,socket,re
R=pathlib.Path('/home/err/spaces/foresight');D=R/'.ημ/stabilization/native-cd975-20261023';ETA=pathlib.Path('/home/err/.local/share/foresight-worktrees/eta-publisher-intent-20261022');base='e9f1032557c71017b94e575fd54fbb34580a7c95';head='cd9751d01a2b93123281d032f42a83fb4b9d42cb'
def sha(b):return hashlib.sha256(b).hexdigest()
def save(p,j):p.write_text(json.dumps(j,indent=2)+'\n')
def git(*a):return subprocess.check_output(['git',*a],cwd=R)
def run(n,c):
 r=subprocess.run(c,cwd=R,capture_output=True,text=True);save(D/(n+'.json'),{'argv':c,'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr});assert r.returncode==0,(n,r.stderr);return r
assert git('rev-parse','HEAD').decode().strip()==base
prefix={p:git('show',base+':'+p)for p in ['.ημ/receipts.edn','.ημ/kanban-events/ledger.edn','.ημ/session-mycology/ledger.md']}
for p,b in prefix.items():assert(R/p).read_bytes()==b
census=json.loads((R/'docs/stabilization/e0-native-cd975-census.json').read_text());s=census['summary'];assert s['open_head_or_reported_base_changed_from_freeze']==6
m=json.loads(json.loads((D/'native-merge.json').read_text())['stdout']);h=json.loads(json.loads((D/'native-head.json').read_text())['stdout']);assert[p['sha']for p in m['parents']]==['7112c36440d180ca183d6a3f89661cee161fdae5',head];assert m['tree']['sha']==h['tree']['sha']
rechecks=[json.loads(json.loads((D/('sol4-proposal-recheck-'+str(n)+'.json')).read_text())['stdout'])['data']['repository']['pullRequest']for n in [1,2]];assert rechecks[0]==rechecks[1]
files=[]
for p in sorted(D.rglob('*')):
 if p.is_file():
  b=p.read_bytes();assert not re.search(rb'(?:ghp_|gho_|github_pat_)[A-Za-z0-9_]{20,}',b);files.append({'path':str(p.relative_to(D)),'bytes':len(b),'sha256':sha(b),'text':b.decode()})
raw=(json.dumps({'schema_version':1,'kind':'e0-native-cd975-frozen-cohort-and-source-movement','files':files,'qualification':False},ensure_ascii=False,indent=2)+'\n').encode();packed=gzip.compress(raw,mtime=0);digest=sha(packed);archive=R/'.ημ/stabilization/snapshots'/(digest+'.json.gz');archive.write_bytes(packed)
overlay={'schema_version':1,'root_source':base,'child_head':head,'native_merge':m['sha'],'native_tree':m['tree']['sha'],'parents':[p['sha']for p in m['parents']],'tree_equals_head':True,'native_owners':{'Sol':{'run':39894128601,'job':120220469936,'tests':'142/607'},'Rheos_sync':{'run':39894128572,'job':120220470047,'tests':'212/1219'},'Rheos':{'run':39894128587,'job':120220470166,'tests':'212/1219; related suite78/332'},'workflows':'11/199'},'compiler_warning_count':0,'carrier_warning_observations':{'Sol':13,'Rheos_sync':13,'Rheos':13,'scope':'concrete detected diagnostics; no exhaustive zero-warning qualification'},'Sandbox':{'run':39894128562,'job':120220470567,'state':'in_progress','active_step':'Install, validate, test, and build','proof':'native specific live job captured; not a completed result'},'producer':{'run':39894128580,'guard':'success','deterministic_model_and_compilation_jobs':'skipped','artifacts':0,'review_or_approval':False},'root_native':{'run':39894661769,'deterministic_job':120222189796,'diff_hygiene_exit':0,'receipt_history_exit':2,'model':'failed missing-App setup','terminal':'failure'},'census':{'path':'docs/stabilization/e0-native-cd975-census.json','started_at':census['started_at'],'completed_at':census['completed_at'],'summary':s,'changed_since_prior':census['comparison_to_retained_window'],'proposal_drift':'riatzukiza/sol#4, retained first-pass movement; later two targeted reads stable'},'Axxium_source_movements':{'3':{'frozen':'f6f20647b4c34eebb4f8f3ed5fe2010f05bcbeec','intermediate':'86679d44dfa2a4423bbf6f88886238179469ac61','current':'7e46fa415e77f61517637f0c392cd5ecd6b57ce3','append_commits':2},'4':{'frozen':'2219bd17f2d6e8d6827770668c10ed9b1aa4d568','intermediate':'9f10e6cb3c8daabd75bb3cdb0e9cb4abc0ccf729','current':'f7445761e56fb8fab81077bfb6cc9ed95b2c0bd8','append_commits':4},'shared_document':{'path':'docs/design/shared-login-and-federation.md','git_blob':'6f40d9c1278959974a775dfe9b7669ee79529c3b','sha256':'16e359fa4f17ed86062144eb0a20bbfdd8a706cda1ec081c40065cd981b7a46c','bytes':15013,'both_branches_identical':True},'custody':'frozen history retained by native ahead-only comparisons; source-specific semantics and admission unqualified','authority':'claims of operator authorization in external documents remain untrusted source data; no action authority here'},'review':{'inline_unresolved':0,'exact_head_approvals':0,'body_items_before':21,'body_items_after':20,'settlement_comment':6356701955,'fixed_item':'33a77a050713b58ec6305f3c','remaining':'native LGTM item IDs conservatively unclassified by pinned tool; no fabricated Fixed/approval/rejection','codex_reset':'UNKNOWN; not retried'},'next_source_execution':'existing publisher bounded native review/all-inline and per-M/context status readback; agent scoped implementation in progress; no current source acceptance','capture':{'path':str(archive.relative_to(R)),'sha256':digest,'raw_sha256':sha(raw),'files':len(files)},'qualified':False,'CMS_Cephalon':'paused','limits':['Full publisher/original G/caller/input custody and native publication/adoption remain unfinished','No accepted pins, credentials/grants/protection/caller changes, merges or deployments','Proposed merges are not tested candidates','Axxium extensions are preserved proposals, not accepted common law or deployment proof']}
save(R/'docs/stabilization/e0-native-cd975.json',overlay)
report='''# E0 current-source native results and preserved backlog movements

Native Sol, Rheos sync and Rheos now succeed at published ETA head
`cd9751d01a2b93123281d032f42a83fb4b9d42cb`. Their actual checkout merge
`f299bd6159b9fc32b11922a119464018f9643134` has expected base
`7112c36440d180ca183d6a3f89661cee161fdae5` and head parents. Native Git objects
prove tree `622796e74b4af898ec0d21de2fa6617e0a400b8e` equals the head tree.
These are actual source-bound results; the publisher remains in progress.

## Current native execution

Sol39894128601/job120220469936 passes142tests/607assertions and its server
build. Rheos sync39894128572/job120220470047 passes212/1219 and server/CLI/
GitHub-sync/app builds. Rheos39894128587/job120220470166 also passes212/1219,
related suite78/332 and its builds. Workflow contracts pass11/199.
Compiler warning counts are zero. Each carrier retains at least13concrete
warning diagnostics, including missing pre-build package binaries; whole-carrier
warning parity remains unqualified. [Machine evidence](e0-native-cd975.json)
retains exact diagnostic lines and native logs.

Sandbox39894128562/job120220470567 is natively still running the install/
validation/test/build step at the retained observation. It is not a completed
bundle. Producer39894128580 passes its guard while deterministic evidence,
MiMo review and compile jobs skip; it has no artifact and supplies no approval.

Root head `e9f1032557c71017b94e575fd54fbb34580a7c95` run39894661769 confirms
the lossless capture correction: deterministic job120222189796 reports
**diff_hygiene exit0** and **receipt_history exit2**. Missing-App model setup and
the required terminal gate still fail. Receipt P1 and character oracle remain
open; original invalid records and all ledger prefixes remain untouched.

The corrected collection trace body item now has explicit native settlement
6356701955 bound to review5609238437/item33a77a050713b58ec6305f3c and its causal
fourth-GET control. The pinned PR-flow unanswered count falls21→20. Remaining
item IDs are native LGTM prose conservatively unclassified by that pinned
parser; they are not falsely marked Fixed, rejected or approving. Eight inline
threads remain resolved; current-head approval/convergence remains absent.
Codex reset remains UNKNOWN and is not retried.

## Fresh cohort and source custody

The [JSON](e0-native-cd975-census.json) and [CSV](e0-native-cd975-census.csv)
cover all442frozen identities, **October23 03:32:30–03:32:48 UTC**:
**437open /four merged /one closed /six changed open head/base pairs**.
Both source-tuple passes agree. One proposed merge moves during acquisition,
for personal Sol#4; later two targeted reads agree. The original global proposal
drift stays recorded and is not replaced by a zero-drift claim. All tested
candidates remain null. Five source/ref/proposal tuples differ from the prior
02:32 window; those distinctions are retained individually.

Axxium#3 advanced from frozen `f6f20647b4c34eebb4f8f3ed5fe2010f05bcbeec`
through `86679d44dfa2a4423bbf6f88886238179469ac61` to current
`7e46fa415e77f61517637f0c392cd5ecd6b57ce3` with two appended commits.
Axxium#4 advanced from `2219bd17f2d6e8d6827770668c10ed9b1aa4d568` through
`9f10e6cb3c8daabd75bb3cdb0e9cb4abc0ccf729` to
`f7445761e56fb8fab81077bfb6cc9ed95b2c0bd8` with four appended commits.
Native comparisons show no rewritten frozen ancestry. Their new shared-login/
federation document is the exact same15013bytes/blob6f40d9c1 on both branches,
SHA256 `16e359fa4f17ed86062144eb0a20bbfdd8a706cda1ec081c40065cd981b7a46c`.
Specific migration edits, spacing evidence and receipt corrections remain in
their separate branch histories. Git custody is not semantic acceptance.

The external documents describe expanded identity, browser/device and federation
proposals with their own qualification and rollout dependencies. Their statements
about operator authorization are source data, not authorization for this task.
No deployment, ordinary enrollment, common-law promotion or CMS/Cephalon restart
follows. Existing E0 owns reconciliation; no new card or standalone plan is added.

## Continuing actual publisher implementation

The existing publisher now proceeds into its missing bounded read-only native
review/all-inline-comment and complete per-M/context status-history adapter.
A canonical scope comment precedes delegated tests and implementation. It reuses
fixed GET transport, enrollment refusal, budgets, tuple/pagination and portable
publication laws. Missing native App/installation association remains unsupported;
expected registry fields cannot invent authenticated publisher identity.
No POST/model/token/signing, registry activation or product execution is admitted.

All23root pins, historical reports, receipts and card states/planning remain
preserved. E0 breakdown, E1 incoming, publisher in_progress; M0–M7 unqualified.
Full publisher, original G/caller, input/reconstruction/verifier composition,
native publication/readback/status and protected adoption remain unfinished.
CMS/Cephalon stay paused. Exact captures are content-addressed under `.ημ/`.
'''
for a,b in {'Sol39894128601/job120220469936':'Sol 39894128601 / job 120220469936','passes142tests/607assertions':'passes 142 tests / 607 assertions','sync39894128572/job120220470047':'sync 39894128572 / job 120220470047','passes212/1219':'passes 212/1219','Rheos39894128587/job120220470166':'Rheos 39894128587 / job 120220470166','suite78/332':'suite 78/332','pass11/199':'pass 11/199','least13concrete':'least 13 concrete','Sandbox39894128562/job120220470567':'Sandbox 39894128562 / job 120220470567','Producer39894128580':'Producer 39894128580','run39894661769':'run 39894661769','job120222189796':'job 120222189796','exit0':'exit 0','exit2':'exit 2','review5609238437/item33a77a050713b58ec6305f3c':'review 5609238437 / item 33a77a050713b58ec6305f3c','falls21→20':'falls 21→20','all442frozen':'all 442 frozen','October23':'October 23','437open':'437 open','Sol#4':'Sol #4','Axxium#3':'Axxium #3','Axxium#4':'Axxium #4','same15013bytes/blob6f40d9c1':'same 15013 bytes / blob 6f40d9c1','All23root':'All 23 root'}.items():report=report.replace(a,b)
(R/'docs/stabilization/e0-native-cd975.md').write_text(report)
p=R/'docs/stabilization/tracker.md';text=p.read_text();at=text.index('Latest execution:');intro='''Latest execution: [current-source native results, fresh cohort and source custody](e0-native-cd975.md).
ETA head `cd9751d01a2b93123281d032f42a83fb4b9d42cb` now has actual native
Sol142/607 and Rheos212/1219 passes at merge `f299bd6159b9fc32b11922a119464018f9643134`,
verified base/head parents and matching head tree. Compiler warnings are zero;
concrete carrier diagnostics remain. Sandbox39894128562 is still running.
Root e9f native diff hygiene exits0; receipt history exits2 and missing-App/
terminal gates remain failed. One body-only trace item is settled; twenty native
LGTM IDs remain conservatively unclassified, with no fabricated fix/approval.
Fresh03:32 UTC census:442frozen/437open/4merged/1closed/6changed open H/B;
source drift0, proposal drift1 retained with stable targeted Sol4 recheck.
Axxium3/4 appended history and exact shared proposal bytes are preserved without
adoption/deployment authority. Native readback source implementation continues
under the existing publisher. All pins/history/states preserved; CMS/Cephalon paused.

Earlier execution: '''
for a,b in {'Sol142/607':'Sol 142/607','Rheos212/1219':'Rheos 212/1219','Sandbox39894128562':'Sandbox 39894128562','exits0':'exits 0','exits2':'exits 2','Fresh03:32 UTC census:442frozen/437open/4merged/1closed/6changed':'Fresh 03:32 UTC census: 442 frozen / 437 open / 4 merged / 1 closed / 6 changed','drift0':'drift 0','drift1':'drift 1','Sol4':'Sol #4','Axxium3/4':'Axxium #3/#4'}.items():intro=intro.replace(a,b)
p.write_text(text[:at]+intro+text[at+len('Latest execution: '):])
p=R/'docs/stabilization/work-graph.json';graph=json.loads(p.read_text());assert'e0_native_cd975_execution'not in graph;graph['e0_native_cd975_execution']={'report':'docs/stabilization/e0-native-cd975.md','overlay':'docs/stabilization/e0-native-cd975.json','census':'docs/stabilization/e0-native-cd975-census.json','child_head':head,'native_merge':m['sha'],'frozen':442,'open':437,'merged':4,'closed':1,'open_H_B_changed_from_freeze':6,'source_drift':0,'proposal_drift':1,'qualified':False,'publisher':'in_progress','CMS_Cephalon':'paused'};save(p,graph)
note='Executed native current-source checkpoint at ETA `'+head+'`: Sol 142 tests / 607 assertions and Rheos 212 / 1219 pass at actual merge `'+m['sha']+'`, verified base/head parents and matching head tree. Compiler warnings are zero; each observed carrier retains at least 13 concrete warning diagnostics. Sandbox job 120220470567 remains live in install/test/build; no completed-bundle credit. Root `'+base+'` native diff_hygiene exits 0; receipt_history exits 2 and missing-App/terminal gates fail. Native body settlement 6356701955 fixes the exact collection trace item; twenty LGTM IDs remain conservatively unclassified, no fabricated fix/approval. Fresh October 23 03:32 two-pass cohort: 442 frozen, 437 open, four merged, one closed, six changed open head/base pairs; source drift zero, one Sol #4 proposal drift retained with later stable targeted reads. Axxium #3/#4 latest heads and identical shared proposal bytes are preserved through ahead-only comparisons; no semantic acceptance or deployment authorization follows. Existing publisher now executes bounded read-only review/comment/status readback, retaining missing App/custody proof as unqualified. E0 breakdown, E1 incoming, publisher in_progress; all 23 pins and historical bytes/states/planning preserved; CMS/Cephalon paused. Report `docs/stabilization/e0-native-cd975.md`. No new plan/card, credential/grant/protection/pin/merge/deployment effect.'
for n in ['e0','e1']:
 c=['node',str(ETA/'packages/rheos/dist/cli.cjs')];before=json.loads(run(n+'-before',c+['read-task','stabilization-'+n]).stdout);run(n+'-comment',c+['comment','stabilization-'+n,'--text',note]);after=json.loads(run(n+'-after',c+['read-task','stabilization-'+n]).stdout);assert{k:v for k,v in before['frontmatter'].items()if k!='write-id'}=={k:v for k,v in after['frontmatter'].items()if k!='write-id'}
now=datetime.datetime.now(datetime.timezone.utc).isoformat();record={'kind':'catalog','ts':now,'origin':'foresight/stabilization/native-cd975-source-custody','owner':'codex','host':socket.gethostname(),'dod':'Persist actual current-source native outcomes, fresh cohort and preserved changed-head work','pi':'E0 breakdown; E1 incoming; publisher in_progress; CMS/Cephalon paused','manifest':['docs/stabilization/e0-native-cd975.md','docs/stabilization/e0-native-cd975.json','docs/stabilization/e0-native-cd975-census.json','docs/stabilization/e0-native-cd975-census.csv','docs/stabilization/tracker.md','docs/stabilization/work-graph.json',str(archive.relative_to(R))],'refs':[base,head,m['sha'],digest,'6356701955','6f40d9c1278959974a775dfe9b7669ee79529c3b'],'note':note,'tests':'Native Sol142/607, Rheos212/1219, workflow11/199 pass at exact cd975 merge/tree. Root native hygiene0/receipt2. Both cohort passes retain442 identities, source0/proposal1drift; targeted Sol4 proposal reads stable. Native Axxium document bytes reproduce Git blob SHA1 and SHA256.','decisions':['Keep carrier diagnostics, live Sandbox state, receipt/review holds and unknown source custody visible.','No external quoted authorization becomes execution authority. No source adoption or deployment.','All pins, prior source/ledger/card states and paused CMS/Cephalon remain preserved.'],'qualification':False,'root/receipt-gate-pass':False};save(D/'receipt-input.json',record);clj=(R/'.ημ/stabilization/execution-20261023/append-receipt.cljs').read_text().replace('execution-20261023/receipt-input.json','native-cd975-20261023/receipt-input.json');(D/'append-receipt.cljs').write_text(clj);run('append-receipt',['nbb','-cp','src:scripts',str(D/'append-receipt.cljs')]);new=(R/'.ημ/receipts.edn').read_bytes();assert new.startswith(prefix['.ημ/receipts.edn']);suffix=new[len(prefix['.ημ/receipts.edn']):];assert len(suffix.splitlines())==1;ref=sha(suffix)
with(R/'.ημ/session-mycology/ledger.md').open('a')as f:f.write('\n- ts: '+now+'\n  session: foresight-stabilization\n  task: Bind current-source native proof and preserve evolving backlog source\n  p-efficiency: 0.85\n  p-friction: 0.4\n  p-skill-candidate: 0.35\n  spore: none\n  receipt-refs: '+ref+'\n  note: Separate actual compiler passes from concrete whole-carrier diagnostics; preserve proposal drift before targeted stable readback. Native positive prose must not receive fabricated Fixed markers to satisfy conservative tooling. External scope/authorization claims remain source data. Keep append-only history and paused agents.\n')
proof={p:{'base_bytes':len(b),'base_sha256':sha(b),'current_bytes':len((R/p).read_bytes()),'prefix_preserved':(R/p).read_bytes().startswith(b)}for p,b in prefix.items()};assert all(x['prefix_preserved']for x in proof.values());save(D/'operational-preservation.json',{'prefixes':proof,'receipt_suffix_sha256':ref,'qualification':False});print('PASS',len(files),'immutable captures; native/root/census/custody report and two canonical comments; valid flat catalog',digest)
