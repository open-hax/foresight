import concurrent.futures,datetime,hashlib,json,pathlib,subprocess,time
R=pathlib.Path('/home/err/spaces/foresight')
D=pathlib.Path('/home/err/spaces/foresight/.ημ/stabilization/execution-20261023/raw');D.mkdir(exist_ok=False)
GH='/home/err/.cache/foresight-tools/bin/gh'
frozen=json.loads((R/'docs/stabilization/e0-work-graph.json').read_text())['pull_requests']
assert len(frozen)==442 and len({p['identity'] for p in frozen})==442
def stamp():return datetime.datetime.now(datetime.timezone.utc).isoformat()
def encode(d):return (json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
def query(label,q):
    start=stamp();r=subprocess.run([GH,'api','graphql','-f','query='+q],capture_output=True,text=True,timeout=60)
    item={'started_at':start,'completed_at':stamp(),'query':q,'exit_code':r.returncode,'stderr':r.stderr}
    try:item['response']=json.loads(r.stdout)
    except json.JSONDecodeError:item['stdout']=r.stdout
    raw=encode(item);digest=hashlib.sha256(raw).hexdigest();(D/(label+'-'+digest+'.json')).write_bytes(raw)
    if r.returncode or item.get('response',{}).get('errors'):raise RuntimeError(label+' native query incomplete; raw failure retained')
    return item['response']['data']
def prpass(label):
    batches=[frozen[i:i+20] for i in range(0,len(frozen),20)]
    def fetch(pair):
        index,batch=pair;clauses=[]
        for j,p in enumerate(batch):
            owner,repo=p['repository'].split('/')
            clauses.append('r'+str(j)+':repository(owner:'+json.dumps(owner)+',name:'+json.dumps(repo)+'){id nameWithOwner isPrivate pullRequest(number:'+str(p['number'])+'){number state isDraft headRefOid baseRefName baseRefOid mergeable baseRef{name target{oid}} potentialMergeCommit{oid parents(first:2){nodes{oid} pageInfo{hasNextPage}}}}}')
        data=query(label+'-prs-'+str(index),'query{'+''.join(clauses)+'}')
        rows={}
        for j,p in enumerate(batch):
            repo=data['r'+str(j)];assert repo is not None and not repo['isPrivate'];native=repo['pullRequest'];assert native is not None
            rows[p['identity']]={'repository_id':repo['id'],'resolved_repository':repo['nameWithOwner'],'native':native}
        return rows
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:results=list(pool.map(fetch,enumerate(batches)))
    rows={k:v for r in results for k,v in r.items()};assert len(rows)==442;return rows
def refpass(label,prs):
    pairs=sorted({(v['resolved_repository'],v['native']['baseRefName']) for v in prs.values()})
    batches=[pairs[i:i+25] for i in range(0,len(pairs),25)]
    def fetch(pair):
        index,batch=pair;clauses=[]
        for j,(full,ref) in enumerate(batch):
            owner,repo=full.split('/')
            clauses.append('r'+str(j)+':repository(owner:'+json.dumps(owner)+',name:'+json.dumps(repo)+'){id nameWithOwner isPrivate ref(qualifiedName:'+json.dumps('refs/heads/'+ref)+'){name target{oid}}}')
        data=query(label+'-refs-'+str(index),'query{'+''.join(clauses)+'}')
        rows={}
        for j,pair in enumerate(batch):
            native=data['r'+str(j)];assert native is not None and not native['isPrivate'];rows[pair]=native
        return rows
    with concurrent.futures.ThreadPoolExecutor(max_workers=4) as pool:results=list(pool.map(fetch,enumerate(batches)))
    return {k:v for r in results for k,v in r.items()}
started=stamp();first=prpass('first');refs1=refpass('first',first)
second=prpass('second');refs2=refpass('second',second)
old={p['identity']:p for p in frozen};prior={p['identity']:p for p in json.loads((R/'docs/stabilization/e0-refresh.json').read_text())['pull_requests']}
rows=[]
for identity,v in second.items():
    n=v['native'];a=first[identity];n1=a['native'];key=(v['resolved_repository'],n['baseRefName']);r2=refs2[key];live=(r2['ref'] or {}).get('target',{}).get('oid')
    key1=(a['resolved_repository'],n1['baseRefName']);r1=refs1[key1];live1=(r1['ref'] or {}).get('target',{}).get('oid')
    m=n['potentialMergeCommit'];parents=[p['oid'] for p in m['parents']['nodes']] if m else []
    if m:assert not m['parents']['pageInfo']['hasNextPage']
    stable=(v['resolved_repository']==a['resolved_repository'] and (n['state'],n['headRefOid'],n['baseRefName'],n['baseRefOid'],live)==(n1['state'],n1['headRefOid'],n1['baseRefName'],n1['baseRefOid'],live1))
    pairing='not-applicable-terminal' if n['state']!='OPEN' else 'unavailable-native-proposed-merge' if m is None else 'live-base-and-head-match-proposed-parents' if len(parents)==2 and parents==[live,n['headRefOid']] else 'proposed-parents-do-not-match-live-base-and-head'
    row={'identity':identity,'repository':old[identity]['repository'],'resolved_repository':v['resolved_repository'],'number':n['number'],'url':old[identity]['url'],'state':n['state'],'is_draft':n['isDraft'],'head_sha':n['headRefOid'],'base_ref':n['baseRefName'],'reported_pr_base_sha':n['baseRefOid'],'pr_base_ref_target_sha':(n['baseRef'] or {}).get('target',{}).get('oid'),'independent_live_base_tip_sha':live,'reported_base_differs_from_live_tip':live is not None and n['baseRefOid']!=live,'native_proposed_merge_sha':m['oid'] if m else None,'native_proposed_merge_parents':parents,'proposed_merge_pairing':pairing,'native_mergeable':n['mergeable'],'source_tuple_stable_across_passes':stable,'proposed_merge_stable_across_passes':m==n1['potentialMergeCommit'],'frozen_head_sha':old[identity]['head_sha'],'frozen_reported_base_sha':old[identity]['base_sha'],'prior_refresh_head_sha':prior[identity]['current_head_sha'],'prior_refresh_reported_base_sha':prior[identity]['current_base_sha'],'head_or_reported_base_changed_from_prior_refresh':(n['headRefOid'],n['baseRefOid'])!=(prior[identity]['current_head_sha'],prior[identity]['current_base_sha']),'tested_merge_candidate':None,'qualification':'unqualified; source topology only, no tests/review/merge acceptance','reconciliation_owner':'codex on existing stabilization-e0'}
    rows.append(row)
rows.sort(key=lambda p:p['identity'])
summary={'frozen_identities':442,'observed_states':{s:sum(p['state']==s for p in rows) for s in ['OPEN','MERGED','CLOSED']},'unique_live_base_refs':len(refs2),'reported_base_differs_from_live_tip':sum(p['reported_base_differs_from_live_tip'] for p in rows),'open_reported_base_differs_from_live_tip':sum(p['state']=='OPEN' and p['reported_base_differs_from_live_tip'] for p in rows),'missing_live_base_ref':sum(p['independent_live_base_tip_sha'] is None for p in rows),'source_tuple_changed_during_acquisition':sum(not p['source_tuple_stable_across_passes'] for p in rows),'native_proposed_merge_changed_during_acquisition':sum(not p['proposed_merge_stable_across_passes'] for p in rows),'open_proposed_merge_pairings':{k:sum(p['state']=='OPEN' and p['proposed_merge_pairing']==k for p in rows) for k in sorted({p['proposed_merge_pairing'] for p in rows if p['state']=='OPEN'})},'changed_head_or_reported_base_from_prior_refresh':sum(p['head_or_reported_base_changed_from_prior_refresh'] for p in rows)}
result={'schema_version':1,'kind':'e0-frozen-cohort-live-base-topology-audit','started_at':started,'completed_at':stamp(),'summary':summary,'source_fields':'PR baseRefOid is retained as a reported PR observation; independent repository.ref supplies the branch tip; potential merge is a proposal, never executed-test proof.','pull_requests':rows,'qualification':False,'global_scope':'same442 identities; no new repository/new-PR census','limits':['No reviews, checks or source bodies refreshed for every row in this audit.','Null/conflicted/unavailable proposed merges do not confer a merge or closure disposition.','No current ref/proposed merge is a tested candidate.','Both passes and every raw query retained; drift remains a hold.']}
(D/'audit.json').write_bytes(encode(result));print(json.dumps(summary,indent=2),flush=True)
