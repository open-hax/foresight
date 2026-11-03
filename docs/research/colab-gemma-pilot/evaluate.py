# SPDX-License-Identifier: GPL-3.0-or-later
"""Scientific adapter: frozen vectors only; no network, DB writes or production paths.
Run: python3 evaluate.py PRIVATE_DATA_DIR OUTPUT_JSON [--baseline | --candidate JSON]
"""
import argparse, hashlib, json, math, pathlib, re, time
import numpy as np


def unit(a):
    return a / np.maximum(np.linalg.norm(a, axis=-1, keepdims=True), 1e-12)


def evaluate(root, config, splits):
    cases=json.loads((root/'cases-24.json').read_text())['cases']
    meta=json.loads((root/'retrieval-meta.json').read_text())
    docs=np.load(root/'corpus.npz')['vectors'][:, :config['dimensions']]
    queries=np.load(root/'queries.npz')[config['prefix']][:, :config['dimensions']]
    docs=unit(docs);queries=unit(queries)
    # Explicit target-format routing is part of this benchmark, not a production claim.
    tokens=[set(r['lexical_tokens']) for r in meta]
    rows=[]
    for ci,c in enumerate(cases):
        if c['split'] not in splits:continue
        start=time.perf_counter()
        eligible=[i for i,r in enumerate(meta) if r.get('normalized_format')==c['target_format'] and r['embedding_model']=='embeddinggemma-2']
        scores=docs[eligible]@queries[ci]
        vector_order=sorted(zip(eligible,scores),key=lambda x:(-float(x[1]),str(meta[x[0]]['_id'])))
        if config['fusion']:
            qt={hashlib.sha256(t.encode()).hexdigest()[:16] for t in re.findall(r'\w+',c['query'].casefold())}
            lex=sorted(eligible,key=lambda i:(-len(qt&tokens[i])/max(1,math.sqrt(len(tokens[i]))),str(meta[i]['_id'])))
            vr={i:rank for rank,(i,_) in enumerate(vector_order,1)}
            lr={i:rank for rank,i in enumerate(lex,1)}
            vector_order=sorted(((i,1/(60+vr[i])+config['fusion']/(60+lr[i])) for i in eligible),key=lambda x:(-x[1],str(meta[x[0]]['_id'])))
        hits=[];seen=set()
        for i,score in vector_order:
            p=meta[i]['parent_id']
            if p in seen:continue
            seen.add(p);hits.append({'parent_id':p,'score':float(score),'chunk_id':str(meta[i]['_id'])})
        ranks=[i+1 for i,h in enumerate(hits) if h['parent_id'] in c['gold_parent_ids']]
        rank=min(ranks) if ranks else None
        rows.append({'case_id':c['id'],'category':c['category'],'split':c['split'],'rank':rank,'recall_at_5':int(rank is not None and rank<=5),'recall_at_10':int(rank is not None and rank<=10),'ndcg_at_10':1/math.log2(rank+1) if rank and rank<=10 else 0,'wrong_top1':int(rank!=1),'score_ms':(time.perf_counter()-start)*1000,'top10':hits[:10]})
    def summary(rs):
        return {'n':len(rs),**{k:sum(r[k] for r in rs)/len(rs) for k in ['recall_at_5','recall_at_10','ndcg_at_10','wrong_top1','score_ms']}} if rs else {'n':0}
    return {'config':config,'overall':summary(rows),'categories':{g:summary([r for r in rows if r['category']==g]) for g in sorted({r['category'] for r in rows})},'splits':{s:summary([r for r in rows if r['split']==s]) for s in splits},'cases':rows}


def main():
    p=argparse.ArgumentParser();p.add_argument('data',type=pathlib.Path);p.add_argument('output',type=pathlib.Path);p.add_argument('--baseline',action='store_true');p.add_argument('--candidate');a=p.parse_args()
    if a.candidate:
        cfg=json.loads(pathlib.Path(a.candidate).read_text())['selected_config']
        result={'mode':'local-candidate-replay','result':evaluate(a.data,cfg,['development','validation','locked_test'])}
    elif a.baseline:
        result={'mode':'frozen-exact-baseline','result':evaluate(a.data,{'prefix':'raw','dimensions':768,'fusion':0},['development','validation','locked_test'])}
    else:
        matrix=[]
        for prefix in ['raw','search','qa']:
            for dims in [768,512,256]:
                for fusion in [0,0.5]:
                    matrix.append(evaluate(a.data,{'prefix':prefix,'dimensions':dims,'fusion':fusion},['development','validation']))
        # Validation alone selects; deterministic complexity preference resolves ties.
        selected=max(matrix,key=lambda r:(r['splits']['validation']['ndcg_at_10'],r['splits']['validation']['recall_at_5'],-bool(r['config']['fusion']),r['config']['dimensions'],r['config']['prefix']=='raw'))
        result={'mode':'candidate-matrix','selected_config':selected['config'],'matrix':matrix,'selected_test':evaluate(a.data,selected['config'],['locked_test'])}
    result['input_hashes']={name:hashlib.sha256((a.data/name).read_bytes()).hexdigest() for name in ['cases-24.json','corpus.npz','retrieval-meta.json','queries.npz']}
    result['evaluator_sha256']=hashlib.sha256(pathlib.Path(__file__).read_bytes()).hexdigest()
    result['promotion']='held; requires independent Stealth and Yoga evidence plus explicit production authority'
    a.output.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
    print(json.dumps({k:result[k] for k in ['mode','selected_config'] if k in result}))
    if 'result'in result:print(json.dumps(result['result']['overall']))

if __name__=='__main__':main()
