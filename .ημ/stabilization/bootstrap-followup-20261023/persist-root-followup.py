import pathlib,json,subprocess,hashlib,gzip,datetime,socket,re
R=pathlib.Path('/home/err/spaces/foresight');D=R/'.ημ/stabilization/bootstrap-followup-20261023';ETA=pathlib.Path('/home/err/.local/share/foresight-worktrees/eta-publisher-intent-20261022');base='976fc89e2cd515d584d2cf73c129aceceb5ca5ab';head='cd9751d01a2b93123281d032f42a83fb4b9d42cb';source='b8af64f41de139d17741ee2696ab82bdb38a81d1'
def sha(b):return hashlib.sha256(b).hexdigest()
def save(p,j):p.write_text(json.dumps(j,indent=2)+'\n')
def git(*a):return subprocess.check_output(['git',*a],cwd=R)
def run(n,c):
 r=subprocess.run(c,cwd=R,capture_output=True,text=True);save(D/(n+'.json'),{'argv':c,'exit_code':r.returncode,'stdout':r.stdout,'stderr':r.stderr});assert r.returncode==0,(n,r.stderr);return r
assert git('rev-parse','HEAD').decode().strip()==base
prefixpaths=['.ημ/receipts.edn','.ημ/kanban-events/ledger.edn','.ημ/session-mycology/ledger.md'];prefix={p:git('show',base+':'+p)for p in prefixpaths}
for p,b in prefix.items():assert(R/p).read_bytes()==b
threads=json.loads((D/'eta-preflight-threads-after.json').read_text());assert threads['exit_code']==0 and 'OPEN'not in threads['stdout'];assert all(x in threads['stdout']for x in ['PRRT_kwDORu27H86vC7vO','PRRT_kwDORu27H86vdBQL','PRRT_kwDORu27H86vdBQR'])
proof=json.loads((D/'preflight-publication-readback.json').read_text());assert proof['published_head']==head and proof['all_source_and_archive_bytes_verified']
child=json.loads((D/'child-preflight-checkpoint.json').read_text());assert child['source']==source and all(x['exit_code']==0 for x in child['full_gates']['results'])
# Bind exact originals and every current capture before documentary root writes.
files=[]
for p in sorted(D.rglob('*')):
 if p.is_file():
  b=p.read_bytes();assert not re.search(rb'(?:ghp_|gho_|github_pat_)[A-Za-z0-9_]{20,}',b);files.append({'path':str(p.relative_to(D)),'bytes':len(b),'sha256':sha(b),'text':b.decode()})
raw=(json.dumps({'schema_version':1,'kind':'e0-bootstrap-preflight-and-native-owner-proof','files':files,'qualification':False},ensure_ascii=False,indent=2)+'\n').encode();packed=gzip.compress(raw,mtime=0);digest=sha(packed);archive=R/'.ημ/stabilization/snapshots'/(digest+'.json.gz');archive.write_bytes(packed)
summary={'schema_version':1,'root_source':base,'published_child_head':head,'child_source':source,'bootstrap_checkpoint':'ed4c7cb508feed77eff5d03f3fad7d686ac84657','bootstrap_red':'b0ac577cfd3c899d4baac553c870d47918933850','bootstrap_green':'14cb2742bfd1a15d5ab450b8f3ee6d3a64147790','classifier_red':'328a22c99caa2a1ed56fbbf5a7bef7c9567bc609','classifier_green':'1b76c9e1e6d6122a844ef418b0b4439ac8ee9495','preflight_red':'b599dc1cdbebc3bdd19279f19ef4100fbacd2196','preflight_green':source,'local_gates':{'intent':'29 tests /200 assertions','CLI':'194/587 plus workflows11/199','collection':'30/224 at unchanged collection source in ed4c','registered_workflow':'339 Node tests at unchanged bootstrap source in ed4c','lint_release':'zero errors/warnings; one preexisting architecture info','independent_public_probe':'actual private one-use authority; opaque uncertainty recovery/second-use refusal; fresh-clock refusal'},'native_settlement':{'classifier_P1':'resolved fixed','transition_P2':'resolved fixed','recovery_P2':'resolved fixed','threads':8,'unresolved':0,'current_approval':'unproven; latest pre-settlement status zero exact-head approvals','successor_hosted':'pending at captured observation'},'native_owners':{'source':'ed4c7cb508feed77eff5d03f3fad7d686ac84657','actual_merge':'abda0ec5cb195abe8ec497e29775ca69a8f5c5c0','base':'7112c36440d180ca183d6a3f89661cee161fdae5','tree':'2e118693f46d89a9ced651d6417e38f63af721b0','merge_tree_equals_head_tree':True,'Sol':{'run':39892253284,'job':120214491343,'workflow_tests':'11/199','product_tests':'142/607','server_build':'0 compiler warnings'},'Rheos_sync':{'run':39892253193,'job':120214490907,'workflow_tests':'11/199','product_tests':'212/1219','builds':'server/cli/github-sync/app:0 compiler warnings'},'whole_carrier_warning_parity':'not qualified; compiler zero is not carrier zero','artifact_restore':'not established','original_G_and_executed_caller':'not established'},'root_native':{'source':base,'run':39891485680,'deterministic_job':120212036821,'deterministic_job_conclusion':'success','receipt_history_exit':2,'diff_hygiene_exit':2,'model_job':120212314787,'model_failure':'missing App credentials','terminal_gate':'failure','raw_output_hygiene_correction':'lossless escaped JSON replaces redundant raw copy; immutable original archive and earlier commit preserved; no gate change','new_local_diff_hygiene':'0'},'root_receipt':{'accepted_base':'cffc34448aa6807fc51135d7f90f0f9fe6ba7efe','exit':2,'new_invalid_rows':2,'whole_history_invalid_rows':13,'original_bytes_unchanged':True},'latest_census':{'path':'docs/stabilization/e0-clio-intent-census.json','window':'2026-10-23T02:32:04Z/2026-10-23T02:32:26Z','frozen':442,'open':437,'merged':4,'closed':1,'changed_open_H_B_from_freeze':6,'source_drift':0,'proposal_drift':0,'new_global_acquisition_in_followup':False},'captures':{'path':str(archive.relative_to(R)),'sha256':digest,'raw_sha256':sha(raw),'files':len(files)},'preserved':{'root_gitlinks':23,'child_card_states_planning':309,'child_prefixes':4,'child_receipt_rows':358,'child_receipt_invalid':71,'child_receipt_errors':118,'CMS_Cephalon':'paused','E0':'breakdown','E1':'incoming','publisher':'in_progress/Codex/5points'},'qualification':False,'limits':['Original G/executed caller/tools/input custody remain incomplete','Full authenticated publisher and reconstruction/verifier composition remain incomplete','Native publication/readback/status and protected adoption remain unproven','No credential/grant/protection/workflow/caller pin changes, merge or deployment','Old-head hosted proof and approvals never transfer to successors']}
save(R/'docs/stabilization/e0-bootstrap-execution.json',summary)
report='''# E0 bootstrap restoration and structured worker failures

Published ETA checkpoint `cd9751d01a2b93123281d032f42a83fb4b9d42cb`
contains source `b8af64f41de139d17741ee2696ab82bdb38a81d1` in existing
[PR 362](https://github.com/open-hax/eta-mu/pull/362). It restores cold ordinary
workflow tests and returns structured worker failures while preserving recovery
authority. The existing publisher remains in progress; no milestone qualifies.

## Executed repairs

Bootstrap RED `b0ac577cfd3c899d4baac553c870d47918933850` precedes GREEN
`14cb2742bfd1a15d5ab450b8f3ee6d3a64147790`. Dedicated intent-runtime config
and fixed entrypoints isolate Malli bootstrap from ordinary package commands.
Actual cold workflow tests pass 11 tests / 199 assertions without bb under both
configured NBB 1.5.211 and fixed NBB 1.5.212. No unrelated workflow gains bb.

Classifier RED `328a22c99caa2a1ed56fbbf5a7bef7c9567bc609` reproduces 24 failures;
GREEN `1b76c9e1e6d6122a844ef418b0b4439ac8ee9495` guards namespace classification.
Native Error and malformed error tags retain unknown append outcomes and exact
recovery. The collection oracle now parses and compares the complete three-GET
trace: a fourth real GET passes the old substring check and fails the exact check.

Preflight RED `b599dc1cdbebc3bdd19279f19ef4100fbacd2196` reproduces 32 failures.
GREEN `b8af64f41de139d17741ee2696ab82bdb38a81d1` normalizes both public worker
calls. Known pre-launch installation/input-bound failures refuse transitions;
recovery remains unknown because original durability uncertainty persists.
Unclassified exceptions remain unknown. Actual host-port controls and a genuine
Clio opaque receipt prove recovery authorization survives launch failure, retries
the one exact event and refuses second use. Thrown data mints no authority.

Final intent **29 tests / 200 assertions**, full CLI **194 / 587** plus workflow
**11 / 199**, owning/test lint and release pass, with zero errors/warnings.
Collection30/224 and registered review-workflow339 pass at unchanged component
source in prior ed4c. Independent actual public-adapter probes pass one-use
capability, alternate-factory refusal, real visible append uncertainty with
opaque recovery, second-use refusal and fresh-clock failure. The ambient-provider
no-provider-fixture failure remains retained beside the provider-free CLI pass.

All three native findings now read resolved as fixed: classifier P1 and both
preflight P2s. Eight inline threads have zero unresolved at the retained readback.
Current-head approval/convergence, remaining summary dispositions and successor
hosted qualification stay open. Codex quota reset remains UNKNOWN; no retry or
paid review is used. Four final source/archive native blobs verify, following
fourteen earlier source/archive blobs and exact authenticated Git fetch at ed4c.

## Native bootstrap restoration, bound to its actual source

[Sol39892253284](https://github.com/open-hax/eta-mu/actions/runs/39892253284),
job120214491343, and
[Rheos sync39892253193](https://github.com/open-hax/eta-mu/actions/runs/39892253193),
job120214490907, succeed at **ed4c7cb508feed77eff5d03f3fad7d686ac84657**.
Both actually check out merge `abda0ec5cb195abe8ec497e29775ca69a8f5c5c0`.
Native Git objects prove parents base7112c364/headed4c and tree
`2e118693f46d89a9ced651d6417e38f63af721b0`, equal to that head tree.

Both run workflow11/199. Sol runs142/607 and builds its server; Rheos runs
212/1219 and builds server, CLI, GitHub sync and app. Compiler warnings are zero.
Whole-carrier warning parity, original event G/caller and artifact restore are
not established. This proof restores the observed bootstrap contract at ed4c;
it does not qualify successor b8/cd975 or a protected product baseline.

## Root failures, preservation and cohort

Root checkpoint `976fc89e2cd515d584d2cf73c129aceceb5ca5ab` run39891485680
still fails: its successful deterministic job120212036821 logs receipt_history
exit2 and diff_hygiene exit2. Model job120212314787 fails missing-App setup;
the required terminal gate fails. No credentials or gate waiver are restored.

The six trailing spaces in a newly captured CLI stdout caused the extra hygiene
failure. A lossless JSON representation now preserves that exact byte string;
the original immutable archive and commit remain intact. Existing diff hygiene
passes locally without changing its configuration. Receipt P1 stays open: two
new invalid ETA envelopes plus eleven pre-base invalid rows remain untouched.
Fresh accepted-base root receipt verification exits2 with empty stderr.

The latest full cohort remains the retained **October23 02:32 UTC** two-pass
window: **442 frozen /437open /four merged /one closed /six changed open H/B**,
zero source/proposal drift. This follow-up performs no new global acquisition.
Axxium#3/#4 movements and all earlier windows remain distinct. Tested merge
candidates stay null; terminal observations confer no reconciliation credit.

All23root pins, all309child states/planning and four child ledger prefixes verify.
Child receipt row358 validates prospectively;71invalid rows/118errors remain.
E0 breakdown, E1 incoming, publisher in_progress/Codex/5points; M0–M7 unqualified.
CMS/Cephalon remain paused. No new card, standalone plan, merge or deployment.

## Evidence custody and remaining execution

[Machine overlay](e0-bootstrap-execution.json) binds exact source, local and
native gates, settlements, public byte verification and a content-addressed
capture archive. Root raw data is under `.ημ/stabilization/bootstrap-followup-20261023/`;
child archives retain138bootstrap/classifier captures and41preflight captures.
Old capture manifests describe original archive bytes; lossless JSON copies are
explicitly marked where raw trailing spaces conflict with diff hygiene.

Full authenticated publication, original event G/executed caller/tools/input
custody, complete reconstruction/verifier composition, native publication and
readback/status, effective grants and protected adoption remain unfinished.
Existing required gates and root merge freeze remain in force.
'''
# Normalize current prose only; prior reports and snapshots are not rewritten.
for a,b in {'Collection30/224':'Collection 30/224','review-workflow339':'review-workflow 339','Sol39892253284':'Sol 39892253284','sync39892253193':'sync 39892253193','job120214491343':'job 120214491343','job120214490907':'job 120214490907','base7112c364/headed4c':'base 7112c364 / head ed4c','workflow11/199':'workflow 11/199','runs142/607':'runs 142/607','run39891485680':'run 39891485680','job120212036821':'job 120212036821','job120212314787':'job 120212314787','exit2':'exit 2','October23':'October 23','/437open':'/ 437 open','Axxium#3/#4':'Axxium #3/#4','All23root':'All 23 root','all309child':'all 309 child','row358':'row 358',';71invalid rows/118errors':'; 71 invalid rows / 118 errors','retain138bootstrap/classifier':'retain 138 bootstrap/classifier','and41preflight':'and 41 preflight'}.items():report=report.replace(a,b)
(R/'docs/stabilization/e0-bootstrap-execution.md').write_text(report)
p=R/'docs/stabilization/work-graph.json';graph=json.loads(p.read_text());assert 'e0_bootstrap_preflight_execution'not in graph;graph['e0_bootstrap_preflight_execution']={'report':'docs/stabilization/e0-bootstrap-execution.md','overlay':'docs/stabilization/e0-bootstrap-execution.json','child_head':head,'source':source,'native_owner_source':'ed4c7cb508feed77eff5d03f3fad7d686ac84657','native_executed_merge':'abda0ec5cb195abe8ec497e29775ca69a8f5c5c0','intent_tests':29,'intent_assertions':200,'inline_unresolved':0,'qualified':False,'publisher':'in_progress','CMS_Cephalon':'paused'};save(p,graph)
p=R/'docs/stabilization/tracker.md';s=p.read_text();at=s.index('Latest execution:');s=s[:at]+'''Latest execution: [cold bootstrap, native owner proof and structured worker failures](e0-bootstrap-execution.md).
Published ETA checkpoint `cd9751d01a2b93123281d032f42a83fb4b9d42cb` binds
source `b8af64f41de139d17741ee2696ab82bdb38a81d1`: intent29/200, full CLI194/587
plus workflows11/199, lint/release zero warnings and actual public adapter probes pass.
Classifier P1 and both launch-error P2 threads read resolved; eight inline threads,
zero unresolved. Current-head review/convergence and hosted qualification stay open.
Earlier ed4c native Sol142/607 and Rheos212/1219 succeed at actual merge abda0ec5,
verified parents and identical head tree. That proof never transfers to successors.
Root976 native receipt and diff hygiene fail; lossless capture JSON now restores
local diff hygiene, while original receipt bytes/P1 and missing-App/terminal holds remain.
Latest full census remains October23 02:32:442 frozen/437open/4merged/1closed/six
changed open H/B, zero acquisition drift. All pins/history/states preserved; CMS/Cephalon paused.

Earlier execution: '''+s[at+len('Latest execution: '):];s=s.replace('intent29/200','intent 29/200').replace('CLI194/587','CLI 194/587').replace('workflows11/199','workflows 11/199').replace('Sol142/607','Sol 142/607').replace('Rheos212/1219','Rheos 212/1219').replace('Root976','Root 976').replace('October23 02:32:442 frozen/437open/4merged/1closed/six','October 23 02:32: 442 frozen / 437 open / 4 merged / 1 closed / six');p.write_text(s)
note='E0 follow-up executed: published ETA '+head+', source '+source+'. Bootstrap cold workflow regression and classifier/launch-error findings have causal RED/GREEN. Intent29/200, full CLI194/587 plus workflows11/199, zero-warning lint/release and independent real public opaque recovery/one-use probes pass. Native classifier P1 and both P2 threads read resolved, eight inline threads/zero unresolved; current approval/convergence and successor hosted proof remain held. Earlier ed4c Sol142/607 and Rheos212/1219 succeed at actual merge abda0ec5cb195abe8ec497e29775ca69a8f5c5c0, verified base/head parents and same head tree; no successor credit. Root976 receipt_history and diff_hygiene exit2; lossless raw-output JSON now fixes local hygiene, original archive bytes and invalid receipt events preserved. Receipt P1/character oracle and missing-App/terminal gate stay open. Latest retained full census October23 02:32 remains442/437open/4merged/1closed/6changedOPEN H/B, no new global acquisition. All23pins,309child states/planning and ledger prefixes preserved; E0breakdown/E1incoming/publisherin_progress, CMS/Cephalon paused. Full publication/original G/caller/composition/adoption unfinished. Report docs/stabilization/e0-bootstrap-execution.md; no new plan/card/merge/deployment.'
for n in ['e0','e1']:
 c=['node',str(ETA/'packages/rheos/dist/cli.cjs')];before=json.loads(run(n+'-before',c+['read-task','stabilization-'+n]).stdout);run(n+'-comment',c+['comment','stabilization-'+n,'--text',note]);after=json.loads(run(n+'-after',c+['read-task','stabilization-'+n]).stdout);assert{k:v for k,v in before['frontmatter'].items()if k!='write-id'}=={k:v for k,v in after['frontmatter'].items()if k!='write-id'}
now=datetime.datetime.now(datetime.timezone.utc).isoformat();record={'kind':'catalog','ts':now,'origin':'foresight/stabilization/bootstrap-preflight-execution','owner':'codex','host':socket.gethostname(),'dod':'Persist executed cold-bootstrap and launch-error repairs, native owner proof and frozen-cohort continuation','pi':'E0 breakdown; E1 incoming; publisher in_progress; CMS/Cephalon paused','manifest':['docs/stabilization/e0-bootstrap-execution.md','docs/stabilization/e0-bootstrap-execution.json','docs/stabilization/tracker.md','docs/stabilization/work-graph.json',str(archive.relative_to(R))],'refs':[base,head,source,digest,'abda0ec5cb195abe8ec497e29775ca69a8f5c5c0'],'note':note,'tests':'Final intent29/200, CLI194/587 plus workflows11/199, lint/release zero warnings, independent public probe pass. Native Sol142/607 and Rheos212/1219 pass only at ed4c executed merge. Root receipt gate exits2; local diff hygiene restored losslessly.','decisions':['Keep all source-specific failures/holds and immutable original bytes.','No credentials/gates/pins/merge/deployment or paused CMS/Cephalon advance.','Current native approval/convergence and full publisher/source custody/adoption remain unqualified.'],'qualification':False,'root/receipt-gate-pass':False};save(D/'receipt-input.json',record)
clj=(R/'.ημ/stabilization/execution-20261023/append-receipt.cljs').read_text().replace('execution-20261023/receipt-input.json','bootstrap-followup-20261023/receipt-input.json');(D/'append-receipt.cljs').write_text(clj);run('append-receipt',['nbb','-cp','src:scripts',str(D/'append-receipt.cljs')])
new=(R/'.ημ/receipts.edn').read_bytes();assert new.startswith(prefix['.ημ/receipts.edn']);suffix=new[len(prefix['.ημ/receipts.edn']):];assert len(suffix.splitlines())==1;ref=sha(suffix)
with(R/'.ημ/session-mycology/ledger.md').open('a')as f:f.write('\n- ts: '+now+'\n  session: foresight-stabilization\n  task: Bind bootstrap restoration, native owner execution and structured worker failures\n  p-efficiency: 0.8\n  p-friction: 0.65\n  p-skill-candidate: 0.5\n  spore: none\n  receipt-refs: '+ref+'\n  note: Preserve exact raw capture bytes in escaped JSON or immutable archives when output whitespace conflicts with diff hygiene. Distinguish pre-launch refusal from persistent append uncertainty, and source-bound native product proof from successor qualification. Recovered capacity interruption continued saved tests without losing work. Existing spore stays unpromoted.\n')
proof={p:{'base_bytes':len(b),'base_sha256':sha(b),'current_bytes':len((R/p).read_bytes()),'prefix_preserved':(R/p).read_bytes().startswith(b)}for p,b in prefix.items()};assert all(x['prefix_preserved']for x in proof.values());save(D/'root-operational-preservation.json',{'prefixes':proof,'receipt_suffix_sha256':ref,'qualification':False});print('PASS root reports, two canonical comments, valid flat catalog, original ledger prefixes; capture archive',digest,len(files))
