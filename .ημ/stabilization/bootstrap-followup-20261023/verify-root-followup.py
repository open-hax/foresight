import pathlib,json,subprocess,hashlib,gzip,re
R=pathlib.Path('/home/err/spaces/foresight');D=R/'.ημ/stabilization/bootstrap-followup-20261023';base='976fc89e2cd515d584d2cf73c129aceceb5ca5ab'
def git(*a):return subprocess.check_output(['git',*a],cwd=R)
def sha(b):return hashlib.sha256(b).hexdigest()
assert git('rev-parse','HEAD').decode().strip()==base
pins=[x for x in git('ls-tree',base).decode().splitlines()if x.startswith('160000')];now=[x for x in git('ls-files','--stage').decode().splitlines()if x.startswith('160000')];assert len(pins)==len(now)==23
for x in pins:
 m,p=x.split('\t');assert any(y.split('\t')[1]==p and y.split()[1]==m.split()[2]for y in now)
for p,rev in [('eta-mu','0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90'),('clio','788cdd3434615a7932b924e68520dbf7f88408c2')]:assert git('-C',p,'rev-parse','HEAD').decode().strip()==rev
old=json.loads(git('show',base+':docs/stabilization/work-graph.json'));new=json.loads((R/'docs/stabilization/work-graph.json').read_text());assert all(new[k]==v for k,v in old.items());assert set(new)-set(old)=={'e0_bootstrap_preflight_execution'}
for p in ['docs/stabilization/e0-clio-intent-execution.md','docs/stabilization/e0-clio-intent-execution.json','docs/stabilization/e0-clio-intent-census.json','docs/stabilization/e0-clio-intent-census.csv']:
 assert(R/p).read_bytes()==git('show',base+':'+p)
for p in ['.ημ/receipts.edn','.ημ/kanban-events/ledger.edn','.ημ/session-mycology/ledger.md']:assert(R/p).read_bytes().startswith(git('show',base+':'+p))
for p in ['docs/agile/kanban/stabilization-e0-reconstruct-the-authoritative-backlog-ation-e0.md','docs/agile/kanban/stabilization-e1-qualify-the-review-and-merge-pipeline-ation-e1.md']:
 before=git('show',base+':'+p);after=(R/p).read_bytes();bf,bb=before.split(b'---',2)[1:];af,ab=after.split(b'---',2)[1:];strip=lambda x:re.sub(rb'(?m)^write-id:.*\n',b'',x);assert strip(bf)==strip(af);assert ab.startswith(bb.removesuffix(b'---')) and ab.endswith(b'---')
rep=R/'.ημ/stabilization/execution-20261023/eta-threads.stdout.json';orig=json.loads(rep.read_text());oldraw=git('show',base+':.ημ/stabilization/execution-20261023/eta-threads.stdout');assert orig['text'].encode()==oldraw and orig['original_sha256']==sha(oldraw)
summary=json.loads((R/'docs/stabilization/e0-bootstrap-execution.json').read_text());capture=summary['captures'];packed=(R/capture['path']).read_bytes();assert sha(packed)==capture['sha256'];raw=gzip.decompress(packed);assert sha(raw)==capture['raw_sha256'];j=json.loads(raw);assert len(j['files'])==47
for x in j['files']:assert len(x['text'].encode())==x['bytes'] and sha(x['text'].encode())==x['sha256']
for p in D.rglob('*'):
 if p.is_file():assert not re.search(rb'(?:ghp_|gho_|github_pat_)[A-Za-z0-9_]{20,}',p.read_bytes())
allowed={'.ημ/receipts.edn','.ημ/kanban-events/ledger.edn','.ημ/session-mycology/ledger.md','.ημ/stabilization/execution-20261023/eta-threads.stdout','docs/stabilization/tracker.md','docs/stabilization/work-graph.json','docs/agile/kanban/stabilization-e0-reconstruct-the-authoritative-backlog-ation-e0.md','docs/agile/kanban/stabilization-e1-qualify-the-review-and-merge-pipeline-ation-e1.md'};changes=set(git('diff','--name-only','-z',base).decode().strip('\0').split('\0'));assert changes<=allowed,changes-allowed
r=subprocess.run(['git','diff','--check','cffc34448aa6807fc51135d7f90f0f9fe6ba7efe'],cwd=R,capture_output=True,text=True);assert r.returncode==0,r.stdout
result={'base':base,'root_gitlinks':23,'primary_pins_unchanged':True,'all_prior_graph_keys_unchanged':True,'prior_reports_census_and_original_raw_bytes_preserved':True,'root_ledger_prefixes':3,'E0_E1_states_planning_and_body_prefixes_preserved':True,'capture_archive_entries_verified':47,'diff_hygiene_at_accepted_base_exit':r.returncode,'no_credential_shaped_tokens_in_owned_captures':True,'CMS_Cephalon':'paused','qualification':False};(D/'root-followup-verification.json').write_text(json.dumps(result,indent=2)+'\n');print(json.dumps(result))
