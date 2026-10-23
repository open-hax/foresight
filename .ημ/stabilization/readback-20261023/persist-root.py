import pathlib,json,subprocess,hashlib,gzip,datetime,socket,re
R=pathlib.Path.cwd();D=R/'.ημ/stabilization/readback-20261023';ETA=pathlib.Path('/home/err/.local/share/foresight-worktrees/eta-publisher-intent-20261022')
base='23e07bb97508dd1e0f081e0d2732806e733b2121';head='db99b1682c94ecdf9f8b239709dd44cb6101c7da';source='6e1f088fcd81dba5e9edaf526e453962875215c6'
def sha(b):return hashlib.sha256(b).hexdigest()
def save(p,j):p.write_text(json.dumps(j,indent=2,ensure_ascii=False)+'\n')
def git(*a):return subprocess.check_output(['git',*a],cwd=R)
def run(n,c):
 r=subprocess.run(c,cwd=R,capture_output=True,text=True);save(D/(n+'.json'),{'argv':c,'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr});assert r.returncode==0,(n,r.stderr);return r
assert git('rev-parse','HEAD').decode().strip()==base
assert not(D/'receipt-input.json').exists(),'one-shot closure must not repeat append'
prefix={p:git('show',base+':'+p)for p in ['.ημ/receipts.edn','.ημ/kanban-events/ledger.edn','.ημ/session-mycology/ledger.md']}
for p,b in prefix.items():assert(R/p).read_bytes()==b
child=json.loads((ETA/'.ημ/review-evidence/trusted-publication-readback/checkpoint.json').read_text());assert child['source']==source
assert json.loads((D/'child-push.json').read_text())['head']==head and json.loads((D/'child-fetch.json').read_text())['whole_git_object_verified']
for n in range(4):assert json.loads((D/('child-blob-'+str(n)+'.json')).read_text())['matches_local']
for name in ['sandbox-run','sandbox-job']:
 x=json.loads(json.loads((D/(name+'.json')).read_text())['stdout']);assert x['conclusion']=='success' and x['head_sha']=='cd9751d01a2b93123281d032f42a83fb4b9d42cb'
assert json.loads((D/'sandbox-warning-observations.json').read_text())['count']==22
assert 'receipt_history: exit 2'in json.loads((D/'root-deterministic-log.json').read_text())['stdout'];assert 'diff_hygiene: exit 0'in json.loads((D/'root-deterministic-log.json').read_text())['stdout']
request=json.loads((D/'eta-review-request.json').read_text());assert request['exit_code']==0
note=('Executed bounded native read-only readback at published ETA '+head+' / source '+source+'. Readback 10 tests / 130 assertions, collection 30/224, publication 17/557 plus 138 NBB/Babashka parity values, eight new portable values, full CLI 194/587 plus workflows 11/199 and zero-warning owning lint/release pass. Independent review reproduced inline native App conflict; causal RED141c11e3 has two failures before GREEN6e1f088f; eight independent successor controls pass. Raw App42 is retained/refused, matching App3152464 stays unsupported with no effects. Production fixed absent registry refuses caller override before GET; actual native review/comments/status page1 observations carry no full traversal/authentication credit. All 309 child states/planning and four prefixes survive; typed receipt359 valid,71 historical invalid/118 errors unchanged;160 captures archived. Earlier CD975 Sandbox39894128562 now SUCCESS at exact Mf299bd61, verified parents/tree, including intent29/200, collection30/224, CLI194/587, Sol142/607, Rheos212/1219;22 detected carrier diagnostics and metadata-only artifact328412696B retained. Native root23e gate has hygiene0/receipt2 and missing-App/terminal failure. CodeRabbit current-head request6357019277 is posted, not approval; Codex reset UNKNOWN not retried. Latest complete frozen census remains October23 03:32 UTC:442 frozen/437 open/4 merged/1 closed/6 changed open H/B,source0/proposal1 drift retained. E0 breakdown/E1 incoming, all milestones unqualified, publisher in_progress; all23 pins/history preserved, CMS/Cephalon paused. No new plan/card or publication/adoption/credential/grant/protection/pin/merge/deploy. Original G/caller/input custody and full authenticated publisher remain unfinished. Report docs/stabilization/e0-readback-execution.md.')
for n in ['e0','e1']:
 cli=['node',str(ETA/'packages/rheos/dist/cli.cjs')];before=json.loads(run(n+'-before',cli+['read-task','stabilization-'+n]).stdout);run(n+'-comment',cli+['comment','stabilization-'+n,'--text',note]);after=json.loads(run(n+'-after',cli+['read-task','stabilization-'+n]).stdout);assert{k:v for k,v in before['frontmatter'].items()if k!='write-id'}=={k:v for k,v in after['frontmatter'].items()if k!='write-id'}
files=[]
for p in sorted(D.rglob('*')):
 if p.is_file():
  b=p.read_bytes();assert not re.search(rb'(?:ghp_|gho_|github_pat_)[A-Za-z0-9_]{20,}',b);files.append({'path':str(p.relative_to(D)),'bytes':len(b),'sha256':sha(b),'text':b.decode()})
raw=(json.dumps({'schema_version':1,'kind':'e0-bounded-native-readback-execution','files':files,'qualified':False},ensure_ascii=False,indent=2)+'\n').encode();packed=gzip.compress(raw,mtime=0);digest=sha(packed);archive=R/'.ημ/stabilization/snapshots'/(digest+'.json.gz');archive.write_bytes(packed)
cohort={'path':'docs/stabilization/e0-native-cd975-census.json','window':'2026-10-23T03:32:30.551815+00:00/2026-10-23T03:32:48.621899+00:00','frozen':442,'open':437,'merged':4,'closed':1,'changed_open_H_B':6,'source_drift':0,'proposal_drift':1,'retained_not_resampled_here':True}
overlay={'schema_version':1,'root_source':base,'child_head':head,'readback_source':source,'child_checkpoint':child,'native_published_bytes':{'whole_git_commit_fetch':True,'four_source_archive_checkpoint_blobs':True},'earlier_native':{'head':'cd9751d01a2b93123281d032f42a83fb4b9d42cb','merge':'f299bd6159b9fc32b11922a119464018f9643134','parent_and_tree_proof':'e0-native-cd975.json','Sandbox':{'run':39894128562,'job':120220470567,'conclusion':'success','intent':'29/200','collection':'30/224','CLI':'194/587','workflows':'11/199','Sol':'142/607','Rheos':'212/1219','detected_warning_diagnostics':22,'artifact':{'id':12476707740,'bytes':328412696,'digest':'sha256:c2859ccdb605a3fd4bcca03bbf4c9fbc8456d104efeba67645063fed00a48055','scope':'native upload metadata only; bytes/download/restoration not verified'}}},'root_native':{'head':base,'run':39896317035,'deterministic_job':120227496208,'receipt_history_exit':2,'diff_hygiene_exit':0,'model':'failed missing-App setup','terminal':'failure'},'review':{'old_head_observation':'18pass/4skip,8threads/0unresolved,0exactapprovals,20unansweredpositivebodyIDs','current_head_status_capture':'eta-status-published.json','request':request,'request_id':6357019277,'codex_reset':'UNKNOWN; not retried','current_head_qualification':False},'frozen_cohort':cohort,'capture':{'path':str(archive.relative_to(R)),'sha256':digest,'raw_sha256':sha(raw),'files':len(files)},'qualified':False,'CMS_Cephalon':'paused','limits':child['limits']}
save(R/'docs/stabilization/e0-readback-execution.json',overlay)
report='''# E0 bounded native readback execution

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

Tests-only RED28de0da1 has 73 failures in 9 tests / 114 assertions before
GREEN6fa625cd. Independent review then reproduced an inline App conflict omitted
from aggregation. RED141c11e3 has two failures before GREEN6e1f088f; conflicting
App42 remains raw/shaped42 and now refuses. Matching App3152464 remains
observed/authentication-unsupported. Original review captures survive; eight
independent successor controls pass with no new narrow finding.

Final readback **10 tests / 130 assertions**, collection30/224,
publication17/557 plus138 actual NBB/Babashka parity values and eight new portable
values pass. Full CLI194/587 plus workflows11/199, owning lint and release pass
with zero compiler/linter warnings. A test-process cleanup escalation diagnostic
and preexisting architecture info remain recorded. Actual fixed public GET reads
review5609238437, its inline-comment page1 and f299bd61 status page1, alongside
actual production-registry refusal. These samples supply no full native traversal
or installation authentication. An early async summary line is excluded as a
completion witness; per-route assertions/raw responses and process exit remain.

Child checkpoint archives160 exact captures. One valid typed receipt gives359
rows; history remains71 invalid/118 errors. All309 states/planning and four
ledger prefixes verify. Checkpoint closure corrected an assertion's card-closing
newline assumption without repeating any append. Authenticated Git fetch verifies
the published commit; four native blob reads reproduce source/archive/checkpoint.

## Completed older native execution and retained holds

Sandbox39894128562/job120220470567 now succeeds at earlier CD975 source and
actual merge f299bd6159b9fc32b11922a119464018f9643134. [Parent/tree proof](e0-native-cd975.json)
binds expected B/H and matching head tree. Logs show intent29/200,
collection30/224, CLI194/587, workflows11/199, Sol142/607 and Rheos212/1219
passing, with zero compiler/linter warnings and22 detected carrier diagnostics.
Artifact12476707740 records328412696 bytes and native digestc2859ccd…; upload
metadata proves no archive contents, restoration or successor qualification.

Root23e07bb9 run39896317035/job120227496208 confirms **diff_hygiene exit0** and
**receipt_history exit2**. Missing-App model setup and required terminal gate fail.
Original records, receipt P1 and character oracle remain untouched. ETA before
push showed18pass/4skip, eight inline threads resolved, zero exact approvals and
twenty conservatively unclassified native positive body IDs. Current-head checks
remain pending at the retained readback. CodeRabbit request6357019277 was posted;
request/acknowledgement is not approval. Codex reset UNKNOWN is not retried;
auto-merge stays off. [Machine evidence](e0-readback-execution.json).

## Cohort and unfinished execution

The latest complete [census](e0-native-cd975-census.json) remains October23
03:32:30–03:32:48 UTC: **442 frozen /437 open /four merged /one closed /six changed
open head/base pairs**, source drift0/proposal drift1. Original proposal movement
and later targeted stable Sol#4 reads remain separate facts. This checkpoint
claims no new full sweep or tested candidate. Axxium custody/proposal limits stay.

Known reviewID is required; unknown POST discovery remains unfinished.
Native original_commit_id stays raw under the existing current-commit law.
Original event G/executed caller/tools/input custody, full reconstruction/verifier
and authenticated publication/status composition, effective grants and protected
adoption remain unqualified. Publisher in_progress, E0 breakdown, E1 incoming,
M0–M7 unqualified. All23 accepted pins and prior history survive. CMS/Cephalon
remain paused. No standalone planning/card or privileged execution follows.
'''
for a,b in {'RED28de0da1':'RED 28de0da1','GREEN6fa625cd':'GREEN 6fa625cd','RED141c11e3':'RED 141c11e3','GREEN6e1f088f':'GREEN 6e1f088f','App42':'App 42','raw/shaped42':'raw/shaped 42','App3152464':'App 3152464','collection30/224':'collection 30/224','publication17/557 plus138':'publication 17/557 plus 138','CLI194/587':'CLI 194/587','workflows11/199':'workflows 11/199','review5609238437':'review 5609238437','archives160':'archives 160','gives359':'gives 359','remains71':'remains 71','All309':'All 309','Sandbox39894128562/job120220470567':'Sandbox 39894128562 / job 120220470567','intent29/200':'intent 29/200','Sol142/607':'Sol 142/607','Rheos212/1219':'Rheos 212/1219','and22':'and 22','Artifact12476707740 records328412696':'Artifact 12476707740 records 328412696','digestc2859ccd':'digest c2859ccd','Root23e07bb9 run39896317035/job120227496208':'Root 23e07bb9 run 39896317035 / job 120227496208','exit0':'exit 0','exit2':'exit 2','showed18pass/4skip':'showed 18 pass / 4 skip','request6357019277':'request 6357019277','October23':'October 23','drift0/proposal drift1':'drift 0 / proposal drift 1','Sol#4':'Sol #4','All23':'All 23'}.items():report=report.replace(a,b)
(R/'docs/stabilization/e0-readback-execution.md').write_text(report)
p=R/'docs/stabilization/tracker.md';text=p.read_text();at=text.index('Latest execution:');intro='''Latest execution: [bounded readback and completed older Sandbox](e0-readback-execution.md).
Published ETA db99b168 binds source6e1f088f: readback10/130, collection30/224,
publication17/557, parity and full CLI/lint/release pass. Independent inline App
conflict is causally repaired; eight successor controls pass. Actual public GET
samples and absent-registry refusal remain unqualified for authentication/full
traversal. All309 child states/planning/four prefixes survive; receipt359 valid,
historical71 invalid/118 errors unchanged. Earlier CD975 Sandbox now succeeds
at exact f299bd61;22 carrier diagnostics and328412696-byte artifact metadata
limits remain. Root23e hygiene0/receipt2 and missing-App/terminal fail. Current-head
review/full publisher/original G/caller/input custody remain open. Retained full
03:32 UTC census:442 frozen/437 open/4 merged/1 closed/6 changed open H/B,
drift0/1. All accepted pins/history preserved; E0 breakdown,M0–M7 unqualified,
CMS/Cephalon paused. No standalone planning.

Earlier execution: '''
for a,b in {'source6e1f088f':'source 6e1f088f','readback10/130':'readback 10/130','collection30/224':'collection 30/224','publication17/557':'publication 17/557','All309':'All 309','receipt359':'receipt 359','historical71':'historical 71',';22':'; 22','and328412696':'and 328412696','Root23e hygiene0/receipt2':'Root 23e hygiene 0 / receipt 2','census:442':'census: 442','frozen/437 open/4 merged/1 closed/6':'frozen / 437 open / 4 merged / 1 closed / 6','drift0/1':'drift 0/1','breakdown,M0':'breakdown, M0'}.items():intro=intro.replace(a,b)
p.write_text(text[:at]+intro+text[at+len('Latest execution: '):])
p=R/'docs/stabilization/work-graph.json';graph=json.loads(p.read_text());old=json.loads(git('show',base+':docs/stabilization/work-graph.json'));assert graph==old;graph['e0_readback_execution']={'report':'docs/stabilization/e0-readback-execution.md','overlay':'docs/stabilization/e0-readback-execution.json','child_head':head,'source':source,'readback_tests':10,'readback_assertions':130,'independent_controls':8,'older_Sandbox':'success/source-bound CD975 at M f299bd61','census_retained':'e0-native-cd975-census.json','qualified':False,'publisher':'in_progress','CMS_Cephalon':'paused'};save(p,graph);assert all(graph[k]==v for k,v in old.items())
now=datetime.datetime.now(datetime.timezone.utc).isoformat();record={'kind':'catalog','ts':now,'origin':'foresight/stabilization/bounded-native-readback','owner':'codex','host':socket.gethostname(),'dod':'Execute bounded native readback and independent causal repair, bind published source and completed older native proof','pi':'E0 breakdown; E1 incoming; publisher in_progress; CMS/Cephalon paused','manifest':['docs/stabilization/e0-readback-execution.md','docs/stabilization/e0-readback-execution.json','docs/stabilization/tracker.md','docs/stabilization/work-graph.json',str(archive.relative_to(R))],'refs':[base,head,source,child['archive_sha256'],digest,'39894128562','39896317035'],'note':note,'qualification':False,'root/receipt-gate-pass':False};save(D/'receipt-input.json',record);s=(R/'.ημ/stabilization/native-cd975-20261023/append-receipt.cljs').read_text().replace('native-cd975-20261023/receipt-input.json','readback-20261023/receipt-input.json');(D/'append-receipt.cljs').write_text(s);run('append-receipt',['nbb','-cp','src:scripts',str(D/'append-receipt.cljs')]);suffix=(R/'.ημ/receipts.edn').read_bytes()[len(prefix['.ημ/receipts.edn']):];assert len(suffix.splitlines())==1;ref=sha(suffix)
with(R/'.ημ/session-mycology/ledger.md').open('a')as f:f.write('\n- ts: '+now+'\n  session: foresight-stabilization\n  task: Bind bounded readback and independent repair to native evidence\n  p-efficiency: 0.85\n  p-friction: 0.45\n  p-skill-candidate: 0.45\n  spore: none\n  receipt-refs: '+ref+'\n  note: Matching content and native App fields do not authenticate installation. Independent adversarial readback found inline conflict omission after registered tests passed; preserve causal repair and original review. Earlier native passes, page1 samples, carrier diagnostics and unsupported custody remain distinct. Recover post-append assertion without repeating side effects. No new spore/promotion.\n')
proof={p:{'base_bytes':len(b),'base_sha256':sha(b),'current_bytes':len((R/p).read_bytes()),'prefix_preserved':(R/p).read_bytes().startswith(b)}for p,b in prefix.items()};assert all(x['prefix_preserved']for x in proof.values());pins=[l for l in git('ls-tree','-r',base).decode().splitlines()if l.startswith('160000 ')];assert len(pins)==23 and pins==[l for l in git('ls-tree','-r','HEAD').decode().splitlines()if l.startswith('160000 ')];save(D/'operational-preservation.json',{'prefixes':proof,'receipt_suffix_sha256':ref,'gitlinks':pins,'CMS_Cephalon':'paused','qualified':False})
attempt=(json.dumps({'kind':'e0-bounded-native-readback-checkpoint','root_source':base,'child_head':head,'readback_source':source,'report_sha256':sha((R/'docs/stabilization/e0-readback-execution.md').read_bytes()),'overlay_sha256':sha((R/'docs/stabilization/e0-readback-execution.json').read_bytes()),'archive_sha256':digest,'prefixes':proof,'qualified':False},indent=2)+'\n').encode();(R/'.ημ/stabilization/snapshots'/(sha(attempt)+'.json')).write_bytes(attempt)
print(json.dumps({'captures':len(files),'archive':digest,'attempt':sha(attempt),'root_flat_receipt':True,'prefixes_preserved':True,'pins':23},indent=2))
