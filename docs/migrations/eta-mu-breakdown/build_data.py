"""Rebuild static planning data from recorded observations; not a repository scanner."""
from pathlib import Path
import json, subprocess
P = Path(__file__).parent
for d in ['graphs','data','classification','checks']:
    (P/d).mkdir(exist_ok=True)
E='476b07bd66efb84566a4159556deacb1e9407e6f'
K='d49242b22cd8b303a2ac9807c70a3acb42a47a1a'
F='78befe3daed9a27d79aa511848de3985602decbc'
A='2439d4d6b8e546cda276f09f5c96db59226ecad6'
EP='643be698ea0d841dd19385506b272872306e456e'
refs={'open-hax/eta-mu':E,'open-hax/knoxx':K,'open-hax/foresight':F,'open-hax/axxium':A,'octave-commons/epiphany':EP}
sources={}
def source(id,repo,path,citation,kind='file',notes=None):
    url=f'https://github.com/{repo}/'+('tree/' if kind=='tree' else 'blob/')+refs[repo]+'/'+path
    sources[id]={'repository':repo,'revision':refs[repo],'path':path,'url':url,'kind':kind,'chat_citation':citation,'notes':notes}
eta_files=[
('eta.root','package.json','turn2file0'),('clio.package','packages/clio/package.json','turn3file0'),
('axxium.package','packages/axxium/package.json','turn4file0'),('rheos.package','packages/rheos/package.json','turn5file0'),
('sol.package','packages/sol/package.json','turn6file0'),('chat.package','packages/chat-ui/package.json','turn7file0'),
('mycology.package','packages/session-mycology/package.json','turn8file0'),('orchestrator.package','packages/kanban-orchestrator/package.json','turn9file0'),
('rheos.deps','packages/rheos/deps.edn','turn13file0'),('sol.deps','packages/sol/deps.edn','turn14file0'),
('mycology.shadow','packages/session-mycology/shadow-cljs.edn','turn15file0'),('chat.shadow','packages/chat-ui/shadow-cljs.edn','turn16file0'),
('axxium.shadow','packages/axxium/shadow-cljs.edn','turn17file0'),('clio.deps','packages/clio/deps.edn','turn18file0'),
('eta-cli.deps','packages/eta-mu/deps.edn','turn21file0'),('eta-cli.package','packages/eta-mu/package.json','turn22file0'),
('eta-cli.shadow','packages/eta-mu/shadow-cljs.edn','turn23file0'),('rheos.readme','packages/rheos/README.md','turn35file0'),
('sol.shadow','packages/sol/shadow-cljs.edn','turn43file0'),('receipt.package','packages/receipt-river/package.json','turn44file0'),
('turn.deps','packages/turn-processor/deps.edn','turn45file0'),('protocols.package','packages/protocols/package.json','turn46file0'),
('orchestrator.readme','packages/kanban-orchestrator/README.md','turn47file0'),('sol.kondo','packages/sol/.clj-kondo/config.edn','turn51file0'),
('clio.readme','packages/clio/README.md','turn54file0'),('mycology.readme','packages/session-mycology/README.md','turn55file0'),
('axxium.readme','packages/axxium/README.md','turn56file0'),('eta.ignore','.gitignore','turn58file0'),('eta.test','scripts/test.bb','turn59file0')]
for id,path,c in eta_files: source(id,'open-hax/eta-mu',path,c)
for id,path,c in [
('ingestion.deps','ingestion/deps.edn','turn24file0'),('ingestion.server','ingestion/src/kms_ingestion/server.clj','turn36file0'),
('ingestion.config','ingestion/src/kms_ingestion/config.clj','turn37file0'),('ingestion.docker','ingestion/Dockerfile','turn38file0'),
('ingestion.contracts','ingestion/src/kms_ingestion/contracts/loader.clj','turn53file0')]: source(id,'open-hax/knoxx',path,c)
source('contracts.locator','open-hax/eta-mu','packages/contracts/output/package.json','turn60file0',notes='Exact file path and revision located by GitHub code search; content not read in full.')
source('ingestion.tree','open-hax/knoxx','ingestion','turn27file0','tree')
source('eta.packages','open-hax/eta-mu','packages','turn29file0','tree')
source('axxium.tree','open-hax/eta-mu','packages/axxium','turn57file0','tree')
source('axxium.existing','open-hax/axxium','package.json','turn32file0')
source('foresight.modules','open-hax/foresight','.gitmodules','turn1file0',notes='Initial contents read; root tree at pinned revision reports the same gitmodules blob b7ef0772f5ca001f77c0bb854586c920e3cb03f4.')
source('foresight.tree','open-hax/foresight','','turn42file0','tree')
source('foresight.readme','open-hax/foresight','README.md','turn52file0',notes='Read first 140 source lines.')
source('epiphany.readme','octave-commons/epiphany','README.md','turn34file0')
source('epiphany.redundancy','octave-commons/epiphany','src/epiphany/domain/redundancy.clj','turn40file0')
source('epiphany.tree','octave-commons/epiphany','src','turn39file0','tree',notes='Large tree response was partially displayed; not an exhaustive source read.')
public_sources={
'pnpm.git':{'url':'https://pnpm.io/10.x/package-sources','checked_on':'2026-09-12','purpose':'Git SHA, path selector, combined Git parameters; canary on pnpm 10.14.0 still required.'},
'clojure.git':{'url':'https://clojure.org/reference/deps_edn','checked_on':'2026-09-12','purpose':'Git SHA, deps/root, manifest resolution; does not replace npm dependency installation.'},
'git.submodules':{'url':'https://git-scm.com/docs/gitmodules','checked_on':'2026-09-12','purpose':'Submodule branch configuration versus recorded gitlink.'},
'github.protection':{'url':'https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches','checked_on':'2026-09-12','purpose':'Required checks, PRs, conversation resolution, expected check source.'},
'coderabbit.config':{'url':'https://docs.coderabbit.ai/reference/configuration','checked_on':'2026-09-12','purpose':'review_progress, fail_commit_status, request_changes_workflow.'}}
def save(path,value): (P/path).write_text(json.dumps(value,indent=2,ensure_ascii=False)+'\n')
for sid,span in {'clio.readme':[1,140], 'axxium.readme':[1,100], 'ingestion.server':[1,180], 'ingestion.contracts':[1,140], 'foresight.readme':[1,140]}.items():
    sources[sid]['read_source_line_range']=span
save('data/sources.json',{'inspection_date':'2026-09-12','sources':sources,'official_documentation':public_sources})
revisions={'inspection_date':'2026-09-12','mainline_snapshots':refs,'foresight_gitlinks':{'eta-mu':'0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90','knoxx':'fb08a10a8aa32a594cc97ae11b820113de4cf386','epiphany':'ca3fd843b30ef8fd9ca2881aeb9758e58dac6b66'},'scope':'Mainline file inspection. These are not a survey of open branches, the yoga host, uncommitted work, or all constellation repositories.','donor_changes_performed':False,'repository_builds_executed':False,'repository_tests_executed':False,'repository_creation_performed':False,'remote_persistence_performed':False,'source_content_hashes':'Git revisions and source paths identify observations. No uncomputed input SHA-256 values are claimed.'}
save('data/revisions.json',revisions)
ns=[
('orchestrator','Kanban Orchestrator','extraction','@open-hax/kanban-orchestrator','open-hax/eta-mu','packages/kanban-orchestrator'),
('clio','Clio','extraction','@eta-mu/clio','open-hax/eta-mu','packages/clio'),
('chat','Chat UI','extraction','@open-hax/chat-ui','open-hax/eta-mu','packages/chat-ui'),
('axxium','Axxium','extraction','@open-hax/axxium','open-hax/eta-mu','packages/axxium'),
('rheos','Rheos','extraction','@eta-mu/rheos','open-hax/eta-mu','packages/rheos'),
('mycology','Session Mycology','extraction','@eta-mu/session-mycology','open-hax/eta-mu','packages/session-mycology'),
('sol','Sol','extraction','@eta-mu/sol','open-hax/eta-mu','packages/sol'),
('ingestion','Knoxx ingestion','extraction',None,'open-hax/knoxx','ingestion'),
('eta_cli','eta-mu CLI/source','remaining','eta-mu','open-hax/eta-mu','packages/eta-mu'),
('protocols','Protocols','remaining','@open-hax/protocols','open-hax/eta-mu','packages/protocols'),
('turn','Turn Processor','remaining',None,'open-hax/eta-mu','packages/turn-processor'),
('receipt','Receipt River','remaining',None,'open-hax/eta-mu','packages/receipt-river'),
('fork','Fork Tax','remaining',None,'open-hax/eta-mu','packages/fork-tax'),
('terminal','Terminal UI','remaining',None,'open-hax/eta-mu','packages/terminal-ui'),
('contracts','Contracts Output','remaining',None,'open-hax/eta-mu','packages/contracts/output'),
('kondo','Shared kondo-config','tooling',None,'open-hax/eta-mu','packages/kondo-config'),
('mcp','MCP Contracts','remaining',None,'open-hax/eta-mu','packages/mcp-contracts'),
('eta_root','eta-mu root scripts','tooling','@open-hax/eta-mu-monorepo','open-hax/eta-mu',''),
('guard','contract-guard.mjs','tooling',None,'open-hax/eta-mu','scripts/contract-guard.mjs'),
('ledger_guard','ledger extern guard','tooling',None,'open-hax/eta-mu','scripts/check-ledger-extern-boundaries.mjs'),
('katamorph','Katamorph','external-project',None,'open-hax/katamorph',''),
('event_ledger','event-ledger','external-project',None,'open-hax/event-ledger',''),
('uxx','uxx-helix','external-project','@open-hax/uxx-helix','open-hax/uxx',''),
('knoxx','Knoxx backend','external-project',None,'open-hax/knoxx',''),
('opencode','OpenCode','external-project',None,'open-hax/opencode',''),
('openplanner','OpenPlanner','external-project',None,'open-hax/openplanner',''),
('proxx','Proxx','external-project',None,'open-hax/proxx',''),
('knoxx_contracts','Knoxx contracts root','configuration',None,'open-hax/knoxx','contracts'),
('postgres','PostgreSQL','infrastructure',None,None,None),('redis','Redis','infrastructure',None,None,None),
('qdrant','Qdrant','infrastructure',None,None,None),('ragussy','Ragussy','runtime-service',None,None,None)]
nodes=[dict(zip(['id','label','category','npm_name','repository','path'],n)) for n in ns]
for n in nodes:
    if n['id']=='uxx':
        n['repository_mapping_status']='provisional; npm manifest observed, uxx package export ownership not inspected'

edges=[]
def edge(a,b,kind,evidence,detail,status='observed',view='package'):
    edges.append({'id':f'R{len(edges)+1:03d}','from':a,'to':b,'kind':kind,'evidence_status':status,'source_ids':evidence.split(','),'detail':detail,'view':view})
edge('rheos','protocols','npm-dev','rheos.package','devDependencies @open-hax/protocols = workspace:*')
edge('rheos','protocols','cljs-source-path','rheos.deps','../protocols/src')
edge('rheos','chat','cljs-source-path','rheos.deps','../chat-ui/src')
edge('rheos','katamorph','clojure-git','rheos.deps','Git SHA be7cc332d865cfedc57b55b10cab3c9f2bd41fc4')
edge('rheos','uxx','npm-dev','rheos.package','@open-hax/uxx-helix ^0.1.0; package/repository relationship needs a package export audit')
edge('sol','eta_cli','clojure-local','sol.deps','open-hax/eta-mu :local/root ../eta-mu; source library, not necessarily execution of the full CLI')
edge('sol','turn','clojure-local','sol.deps','open-hax/turn-processor :local/root ../turn-processor')
edge('sol','katamorph','clojure-git','sol.deps','v0.2.0; Git SHA 305a5e49d834aca27566f739e8510f6b409fda78')
edge('sol','event_ledger','clojure-git','sol.deps','Git SHA ada7374b7f4e1c3b0ab4e6bbe996f10f06e9b93a; no observed Sol -> Clio replacement in this mainline manifest')
for t in ['rheos','sol','mycology','fork','receipt']:
    edge('eta_cli',t,'npm-runtime','eta-cli.package','workspace:* dependency')
for t in ['turn','terminal','contracts']:
    edge('eta_cli',t,'npm-dev','eta-cli.package,contracts.locator' if t=='contracts' else 'eta-cli.package','workspace:* devDependency')
for t in ['mycology','fork','receipt','turn','terminal']:
    edge('eta_cli',t,'cljs-source-path','eta-cli.shadow','Sibling src/cljs included in shadow source-paths')
edge('sol','guard','tooling-path','sol.package','../../scripts/contract-guard.mjs',view='tooling')
edge('sol','kondo','tooling-path','sol.kondo','../../kondo-config/clj-kondo.exports/open-hax/kondo-config relative to .clj-kondo',view='tooling')
edge('mycology','ledger_guard','tooling-path','mycology.package','../../scripts/check-ledger-extern-boundaries.mjs session-mycology',view='tooling')
edge('receipt','ledger_guard','tooling-path','receipt.package','Shared guard remains required by Receipt River after Mycology is extracted',view='tooling')
for t in ['clio','rheos','sol','chat','axxium']:
    edge('eta_root',t,'test-dispatch','eta.test','Explicit pnpm filter in scripts/test.bb; rewrite only after new repo gate is installed',view='tooling')
edge('eta_root','rheos','script-dispatch','eta.root','Root dev and start dispatch to Rheos',view='tooling')
for t in ['sol','knoxx','opencode']:
    edge('chat',t,'adapter-surface','chat.shadow','Exported create session adapter; not an npm or Clojure dependency declaration',view='runtime')
edge('orchestrator','rheos','mcp-service','orchestrator.readme','Documented rheos-kanban MCP endpoint; inspect contract bytes in implementation preflight','reported','runtime')
edge('orchestrator','mcp','contract-loader','orchestrator.readme','Documented generic :mcp-server loader','reported','runtime')
edge('orchestrator','sol','runtime-host','orchestrator.readme','Documented optional Sol-style host with additional contract roots','reported','runtime')
edge('ingestion','knoxx_contracts','configuration-path','ingestion.contracts','CONTRACTS_DIR, cwd/contracts, then cwd/../contracts discovery','observed','runtime')
edge('ingestion','knoxx','runtime-configuration','ingestion.config','Knoxx backend adapter; translation configuration remains Knoxx-owned','observed','runtime')
edge('ingestion','openplanner','runtime-configuration','ingestion.config,ingestion.deps','Configurable sink; integration suite explicitly requires OpenPlanner and DB','observed','runtime')
for t in ['proxx','postgres','redis','qdrant','ragussy']:
    edge('ingestion',t,'runtime-configuration','ingestion.config',f'Configured {t} endpoint; configuration presence does not establish mandatory use in every deployment','observed','runtime')
data={'schema_version':'dependency-observation-snapshot/v1','as_of':'2026-09-12','edge_direction':'consumer -> dependency or related service','coverage':'Selected pinned manifests, build descriptors, scripts, README-reported contract relationships, and selected ingestion implementation. Not an exhaustive namespace/import/CI/deployment closure.','observed_vs_reported':'Configuration and declaration edges are observed; orchestrator integration details currently rely on README and are marked reported. Runtime edges are not package-manager edges.','nodes':nodes,'edges':edges}
save('data/relationships.json',data)
def edn(x):
    if x is None: return 'nil'
    if x is True: return 'true'
    if x is False: return 'false'
    if isinstance(x,str): return json.dumps(x,ensure_ascii=False)
    if isinstance(x,(int,float)): return str(x)
    if isinstance(x,list): return '['+' '.join(map(edn,x))+']'
    if isinstance(x,dict): return '{'+' '.join(':'+k.replace('_','-')+' '+edn(v) for k,v in x.items())+'}'
    raise TypeError(type(x))
(P/'data/relationships.nd.edn').write_text(''.join(edn(e)+'\n' for e in edges))
# These are snapshot records, NOT Clio-admitted events.
def mermaid(name,sel_nodes,sel_edges,title):
    lines=['flowchart LR',f'  %% {title}', '  %% Consumer points to dependency; edge IDs resolve in data/relationships.json.']
    for n in sel_nodes:
        label=n['label'].replace('"',"'")
        lines.append(f'  {n["id"]}["{label}"]')
    for e in sel_edges:
        op='-.->' if e['view'] in ['runtime','tooling'] else '-->'
        lines.append(f'  {e["from"]} {op}|"{e["id"]} {e["kind"]}"| {e["to"]}')
    (P/'graphs'/f'{name}.mmd').write_text('\n'.join(lines)+'\n')
def dot(name,sel_nodes,sel_edges,title):
    lines=['digraph dependencies {','rankdir=LR;','graph [pad=0.25, nodesep=0.3, ranksep=0.55, labelloc=t, fontsize=18, fontname="DejaVu Sans", label='+json.dumps(title)+'];',
           'node [shape=box, style=rounded, fontname="DejaVu Sans", fontsize=11];','edge [fontname="DejaVu Sans", fontsize=8];']
    for n in sel_nodes:
        # No specific colors; categories expressed in labels and outlines.
        label=n['label']+ ('\n(extraction target)' if n['category']=='extraction' else '')
        lines.append(n['id']+' [label='+json.dumps(label)+'];')
    for e in sel_edges:
        style='dashed' if e['view'] in ['runtime','tooling'] else 'solid'
        lines.append(f'{e["from"]} -> {e["to"]} [style={style},label='+json.dumps(e['id']+' '+e['kind'])+'];')
    lines.append('}')
    file=P/'graphs'/f'{name}.dot';file.write_text('\n'.join(lines)+'\n')
    for fmt in ['svg','png']:
        subprocess.run(['dot',f'-T{fmt}',str(file),'-o',str(P/'graphs'/f'{name}.{fmt}')],check=True)
pkg_edges=[e for e in edges if e['view']=='package']
pkg_ids={e[k] for e in pkg_edges for k in ['from','to']} | {n['id'] for n in nodes if n['category']=='extraction' and n['id']!='ingestion'}
pkg_nodes=[n for n in nodes if n['id'] in pkg_ids]
mermaid('01-observed-package-dependencies',pkg_nodes,pkg_edges,'Observed mainline package/source declarations; not a build result')
dot('01-observed-package-dependencies',pkg_nodes,pkg_edges,'Observed package/source declarations | 2026-09-12')
rt_edges=[e for e in edges if e['view']=='runtime']
rt_ids={e[k] for e in rt_edges for k in ['from','to']}
rt_nodes=[n for n in nodes if n['id'] in rt_ids]
mermaid('02-runtime-and-contract-relationships',rt_nodes,rt_edges,'Runtime/configuration surfaces; not install dependencies')
dot('02-runtime-and-contract-relationships',rt_nodes,rt_edges,'Runtime / contract relationships | observations and reported host wiring')
# No premature closure assertion from search results.
save('data/unresolved.json',{'items':[
 'Exhaustive source import, namespace, relative script, resource, CI, and deployment scan is still required at implementation head.',
 'Destination existence/history checks remain unperformed except open-hax/axxium. Osmos is a proposed ingestion name, not reserved.',
 'Protocol source distribution cannot be assumed from its dist-only npm files list; packages/protocols/deps.edn fetch returned 404.',
 'Other target shared .clj-kondo and root scripts may add tooling edges beyond Sol/Mycology observations.',
 'Exact CI runner, secrets, environment, Docker and resource contracts must be read before moving workflows.',
 'Open branches, recent review threads, and yoga worktrees were not inspected.',
 'No package installs, builds, test suites, coverage runs, browser runs, repo creation, or branch settings changes were executed.',
 'The full Markdown constellation was not indexed, classified, counted or deduplicated in this planning run.',
 'No code-volume percentage has been computed.',
 'No Clio schema admission or external persistence was performed; ND-EDN here is a static observation dataset.'
]})
print(f'Wrote {len(sources)} source references, {len(nodes)} nodes, {len(edges)} typed relationships.')
