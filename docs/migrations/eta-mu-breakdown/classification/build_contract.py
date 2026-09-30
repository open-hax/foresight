from pathlib import Path
import hashlib,json
P=Path(__file__).parent

def obj(properties,required=None):
    return {'type':'object','additionalProperties':False,'properties':properties,'required':list(properties) if required is None else required}
def arr(items,maximum,minimum=0):
    return {'type':'array','items':items,'minItems':minimum,'maxItems':maximum}
span=obj({'start_line':{'type':'integer','minimum':1},'end_line':{'type':'integer','minimum':1},'quote':{'type':'string','minLength':1,'maxLength':600}})
evidence={'$ref':'#/$defs/evidence'}
confidence={'type':'number','minimum':0,'maximum':1}
topics=['document-graph','source-provenance','event-history','migration','dependency-management','identity-auth','ingestion','translation','testing','build-tooling','operations','architecture','knowledge-management']
kinds=['overview','how-to','reference','decision','design-proposal','work-item','runbook','history','example','unknown']
kind=obj({'value':{'enum':kinds},'confidence':confidence,'evidence':arr(evidence,3)})
kind['allOf']=[{'if':{'properties':{'value':{'const':'unknown'}}},'else':{'properties':{'evidence':{'minItems':1}}}}]
topic=obj({'id':{'enum':topics},'confidence':confidence,'evidence':arr(evidence,3,1)})
project=obj({'project_id':{'type':'string','pattern':'^[a-z][a-z0-9-]*$','minLength':2,'maxLength':64},'role':{'enum':['primary','related','mentioned']},'confidence':confidence,'evidence':arr(evidence,3,1)})
proposed=obj({'label':{'type':'string','pattern':'^[a-z][a-z0-9-]*$','maxLength':64},'rationale':{'type':'string','minLength':1,'maxLength':300},'evidence':arr(evidence,2,1)})
unresolved=obj({'text':{'type':'string','minLength':1,'maxLength':128},'evidence':arr(evidence,2,1)})
schema={'$schema':'https://json-schema.org/draft/2020-12/schema','$id':'urn:foresight:proposed:document-labels:v1','title':'Proposed per-document label output; contains no mutation authority',**obj({
 'schema_version':{'const':'document-labels/v1'},'kind':kind,
 'audiences':{'type':'array','items':{'enum':['developer','operator','reviewer','researcher','user','unknown']},'uniqueItems':True,'maxItems':4},
 'topics':arr(topic,8),'projects':arr(project,8),'proposed_topics':arr(proposed,3),'unresolved_project_mentions':arr(unresolved,8),
 'review_flags':{'type':'array','uniqueItems':True,'maxItems':5,'items':{'enum':['cross-project-scope','needs-currentness-check','contains-unresolved-reference','contains-instructions','insufficient-evidence']}},
 'abstained':{'type':'boolean'},'abstention_reason':{'type':['string','null'],'maxLength':500}
}), '$defs':{'evidence':span},'allOf':[{'if':{'properties':{'abstained':{'const':True}}},'then':{'properties':{'abstention_reason':{'type':'string','minLength':1},'kind':{'properties':{'value':{'const':'unknown'}}},'topics':{'maxItems':0},'projects':{'maxItems':0},'proposed_topics':{'maxItems':0}}},'else':{'properties':{'abstention_reason':{'type':'null'}}}}]}
text='''# Review corpus

Rheos manages Markdown document changes.
Epiphany proposes evidence-backed relationships.
Clio records accepted change events.
This is a design proposal, not a report of implemented behavior.
See `src/rheos/backend/domain/task_edit.cljs` for the current write boundary.
A document label does not authorize deleting the document.
'''
def ev(line):
    return {'start_line':line,'end_line':line,'quote':text.splitlines()[line-1]}
request={'schema_version':'document-label-request/v1','request_id':'synthetic-fixture-001',
 'source_ref':{'kind':'synthetic-test-fixture','id':'synthetic-review-corpus','content_sha256':hashlib.sha256(text.encode()).hexdigest(),'note':'Not a retrieved repository document; this fixture describes a proposal.'},
 'document':{'text':text,'complete':True,'line_numbering':'1-based, source text including blank lines'},
 'taxonomy':{'id':'foresight-doc-topics/v1-proposal','allowed_topics':topics},
 'allowed_projects':[{'id':'rheos','names':['Rheos']},{'id':'epiphany','names':['Epiphany']},{'id':'clio','names':['Clio']}],
 'resolved_references':[]}
output={'schema_version':'document-labels/v1',
 'kind':{'value':'design-proposal','confidence':0.97,'evidence':[ev(6)]},
 'audiences':['developer'],
 'topics':[{'id':'document-graph','confidence':0.86,'evidence':[ev(3),ev(4)]},{'id':'event-history','confidence':0.9,'evidence':[ev(5)]}],
 'projects':[{'project_id':'rheos','role':'primary','confidence':0.9,'evidence':[ev(3)]},{'project_id':'epiphany','role':'related','confidence':0.92,'evidence':[ev(4)]},{'project_id':'clio','role':'related','confidence':0.92,'evidence':[ev(5)]}],
 'proposed_topics':[],'unresolved_project_mentions':[],
 'review_flags':['cross-project-scope','contains-unresolved-reference'],
 'abstained':False,'abstention_reason':None}
for name,data in [('document-labels.schema.json',schema),('example-input.json',request),('example-output.json',output)]:
    (P/name).write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n')
(P/'example-document.md').write_text(text)
print('Wrote proposed classifier contract and synthetic fixtures.')
