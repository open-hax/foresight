# SPDX-License-Identifier: GPL-3.0-or-later
"""Qualify isolated retrieval scoring; never activates anything."""
import hashlib,json,pathlib,sys
job=pathlib.Path(sys.argv[1]);s=json.loads((job/'stealth-replay.json').read_text());y=json.loads((job/'yoga-replay.json').read_text());c=json.loads((job/'candidate.json').read_text())
assert s['input_hashes']==y['input_hashes']==c['input_hashes'],'Input drift'
assert s['evaluator_sha256']==y['evaluator_sha256']==c['evaluator_sha256'],'Code drift'
assert s['result']['config']==y['result']['config']==c['selected_config'],'Candidate drift'
for a,b in zip(s['result']['cases'],y['result']['cases'],strict=True):
    assert a['case_id']==b['case_id'] and a['rank']==b['rank'],'Gold ranking drift'
    assert [x['parent_id'] for x in a['top10']]==[x['parent_id'] for x in b['top10']],'Top-ten identity drift'
for metric in ['recall_at_5','recall_at_10','ndcg_at_10','wrong_top1']:
    assert s['result']['overall'][metric]==y['result']['overall'][metric],metric
report={'candidate_sha256':hashlib.sha256((job/'candidate.json').read_bytes()).hexdigest(),'selected_config':c['selected_config'],'input_hashes':s['input_hashes'],'evaluator_sha256':s['evaluator_sha256'],'isolated_scoring_stealth':'pass','isolated_scoring_yoga':'pass','same_top10_identities':24,'same_gold_ranks':24,'production_eligible':False,'production_activated':False,'limits':['Source-grounded analyst labels, not human-adjudicated relevance','Only eight validation and eight locked-test cases','Yoga replay uses frozen Stealth-generated query vectors; no native Yoga embedding service was available','Live ANN path differs from frozen exact search','No Gemma4 weight or actor behavior change tested'],'replays':{h:{'overall':v['result']['overall'],'sha256':hashlib.sha256((job/(h+'-replay.json')).read_bytes()).hexdigest()}for h,v in [('stealth',s),('yoga',y)]}}
(job/'qualification.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps({k:report[k]for k in ['isolated_scoring_stealth','isolated_scoring_yoga','production_eligible']}))
