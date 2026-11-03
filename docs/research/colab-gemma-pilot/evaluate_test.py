# SPDX-License-Identifier: GPL-3.0-or-later
import importlib.util,json,pathlib,tempfile,unittest,numpy as np
p=pathlib.Path(__file__).with_name('evaluate.py');s=importlib.util.spec_from_file_location('scorer',p);m=importlib.util.module_from_spec(s);s.loader.exec_module(m)
class ScoreTests(unittest.TestCase):
    def test_parent_dedup_and_modality_boundary(self):
        with tempfile.TemporaryDirectory() as td:
            r=pathlib.Path(td)
            meta=[{'_id':str(i),'parent_id':parent,'normalized_format':fmt,'embedding_model':'embeddinggemma-2','lexical_tokens':[]}for i,(parent,fmt)in enumerate([('other','image'),('duplicate','text'),('duplicate','text'),('gold','text')])]
            (r/'retrieval-meta.json').write_text(json.dumps(meta));(r/'cases-24.json').write_text(json.dumps({'cases':[{'id':'x','category':'test','split':'validation','target_format':'text','query':'x','gold_parent_ids':['gold']}]}))
            np.savez(r/'corpus.npz',vectors=np.array([[1.,0.],[1.,0.],[.99,.01],[.9,.1]]));np.savez(r/'queries.npz',raw=np.array([[1.,0.]]))
            result=m.evaluate(r,{'prefix':'raw','dimensions':2,'fusion':0},['validation'])
            self.assertEqual(result['cases'][0]['rank'],2)
            self.assertEqual([x['parent_id']for x in result['cases'][0]['top10']],['duplicate','gold'])
            self.assertEqual(result['overall']['recall_at_5'],1)
if __name__=='__main__':unittest.main()
