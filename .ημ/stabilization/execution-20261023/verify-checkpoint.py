import subprocess,pathlib,json,hashlib,gzip,csv,re
R=pathlib.Path('/home/err/spaces/foresight');D=R/'.ημ/stabilization/execution-20261023';base='d8ece877c9a9e07a87fec12f650bced173f6b164'
def git(*a):return subprocess.check_output(['git',*a],cwd=R)
assert git('rev-parse','HEAD').decode().strip()==base
pins=[line for line in git('ls-tree',base).decode().splitlines() if line.startswith('160000')];current=[line for line in git('ls-files','--stage').decode().splitlines() if line.startswith('160000')];assert len(pins)==len(current)==23
for line in pins:
 meta,path=line.split('\t');sha=meta.split()[2];assert any(x.split('\t')[1]==path and x.split()[1]==sha for x in current)
assert git('-C','eta-mu','rev-parse','HEAD').decode().strip()=='0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90'
assert git('-C','clio','rev-parse','HEAD').decode().strip()=='788cdd3434615a7932b924e68520dbf7f88408c2'
a=json.loads(git('show',base+':docs/stabilization/work-graph.json'));b=json.loads((R/'docs/stabilization/work-graph.json').read_text());assert all(b[k]==v for k,v in a.items())
over=json.loads((R/'docs/stabilization/e0-clio-intent-execution.json').read_text());sp=R/over['capture_snapshot'];assert hashlib.sha256(sp.read_bytes()).hexdigest()==over['capture_packed_sha256'];j=json.loads(gzip.decompress(sp.read_bytes()));assert len(j['files'])==153
for f in j['files']:
 raw=f['text'].encode();assert len(raw)==f['bytes'] and hashlib.sha256(raw).hexdigest()==f['sha256']
c=json.loads((R/'docs/stabilization/e0-clio-intent-census.json').read_text());rows=list(csv.DictReader((R/'docs/stabilization/e0-clio-intent-census.csv').open()));assert len(rows)==442;assert {p['identity'] for p in rows}=={p['identity'] for p in c['pull_requests']}
for x,y in zip(rows,c['pull_requests']):
 assert x['identity']==y['identity']
 for k in ['state','head_sha','reported_pr_base_sha','independent_live_base_tip_sha']:assert x[k]==y[k]
assert all(x['tested_merge_candidate'] is None for x in c['pull_requests'])
for p in D.rglob('*'):
 if p.is_file():assert not re.search(rb'(?:ghp_|gho_|github_pat_)[A-Za-z0-9_]{20,}',p.read_bytes()),str(p)
for p in ['.ημ/receipts.edn','.ημ/kanban-events/ledger.edn','.ημ/session-mycology/ledger.md']:assert (R/p).read_bytes().startswith(git('show',base+':'+p))
# All pre-existing tracked reports, snapshots and source files are byte-preserved outside named owners.
allowed={'docs/stabilization/tracker.md','docs/stabilization/work-graph.json','.ημ/receipts.edn','.ημ/kanban-events/ledger.edn','.ημ/session-mycology/ledger.md','docs/agile/kanban/stabilization-e0-reconstruct-the-authoritative-backlog-ation-e0.md','docs/agile/kanban/stabilization-e1-qualify-the-review-and-merge-pipeline-ation-e1.md'}
changed=set(git('diff','--name-only','-z',base).decode().strip('\0').split('\0'));assert changed<=allowed,changed-allowed
result={'base':base,'root_gitlinks':23,'primary_eta_unchanged':True,'primary_clio_unchanged':True,'prior_graph_keys_equal':True,'all_prior_tracked_bytes_outside_owned_paths_unchanged':True,'snapshot_files_verified':153,'census_rows':442,'append_only_prefixes':True,'secrets_scan':'no token-shaped credential in owned capture files','qualification':False}
(D/'checkpoint-verification.json').write_text(json.dumps(result,indent=2)+'\n')
print(json.dumps(result))
