#!/usr/bin/env python3
"""Checks the generated planning pack, NOT the source repositories.

Requires Python 3.10+, jsonschema and networkx. It performs no network calls,
repository writes, dependency installs, model calls, or publication actions.
"""
from pathlib import Path
import copy
import hashlib
import json
import re
import unittest
import xml.etree.ElementTree as ET

import jsonschema
import networkx as nx

ROOT = Path(__file__).resolve().parents[1]

def read_json(path):
    return json.loads((ROOT / path).read_text())

def parse_model_output(raw, limit_bytes=16_384):
    """Reject non-JSON extensions before validation. No repair or code evaluation."""
    if not isinstance(raw, str) or len(raw.encode('utf-8')) > limit_bytes:
        raise ValueError('Model response must be bounded UTF-8 text')
    def reject_constant(value):
        raise ValueError(f'Non-finite JSON number: {value}')
    def unique_object(pairs):
        result={}
        for key,value in pairs:
            if key in result:
                raise ValueError(f'Duplicate JSON key: {key}')
            result[key]=value
        return result
    value=json.loads(raw, parse_constant=reject_constant, object_pairs_hook=unique_object)
    if not isinstance(value, dict):
        raise ValueError('Expected exactly one JSON object')
    return value


def validate_context(output, request):
    """Illustrative post-schema validation; it does not verify semantic truth."""
    schema = read_json('classification/document-labels.schema.json')
    jsonschema.Draft202012Validator(schema).validate(output)
    allowed_projects = {p['id'] for p in request['allowed_projects']}
    allowed_topics = set(request['taxonomy']['allowed_topics'])
    if any(p['project_id'] not in allowed_projects for p in output['projects']):
        raise ValueError('Unknown project identifier')
    if any(t['id'] not in allowed_topics for t in output['topics']):
        raise ValueError('Unknown topic identifier')
    for field,key in [('projects','project_id'),('topics','id')]:
        values=[v[key] for v in output[field]]
        if len(values) != len(set(values)):
            raise ValueError(f'Duplicate {field}')
    lines = request['document']['text'].splitlines()
    def visit(x):
        if isinstance(x, dict):
            if set(x) == {'start_line','end_line','quote'}:
                start,end=x['start_line'],x['end_line']
                if not (1 <= start <= end <= len(lines)):
                    raise ValueError('Invalid evidence span')
                span='\n'.join(lines[start-1:end])
                if x['quote'] not in span:
                    raise ValueError('Evidence quote does not match source span')
            for v in x.values(): visit(v)
        elif isinstance(x, list):
            for v in x: visit(v)
    visit(output)
    return True

class PackChecks(unittest.TestCase):
    def test_revision_shapes(self):
        revisions=read_json('data/revisions.json')
        for sha in list(revisions['mainline_snapshots'].values())+list(revisions['foresight_gitlinks'].values()):
            self.assertRegex(sha,r'^[0-9a-f]{40}$')
        self.assertFalse(revisions['repository_builds_executed'])
        self.assertFalse(revisions['remote_persistence_performed'])

    def test_all_eight_targets_present(self):
        data=read_json('data/relationships.json')
        self.assertEqual({n['id'] for n in data['nodes'] if n['category']=='extraction'},
                         {'orchestrator','clio','chat','axxium','rheos','mycology','sol','ingestion'})

    def test_edges_resolve(self):
        data=read_json('data/relationships.json')
        nodes={n['id'] for n in data['nodes']}
        sources=read_json('data/sources.json')['sources']
        self.assertEqual(len(nodes),len(data['nodes']))
        self.assertEqual(len({e['id'] for e in data['edges']}),len(data['edges']))
        for edge in data['edges']:
            self.assertIn(edge['from'],nodes);self.assertIn(edge['to'],nodes)
            self.assertTrue(edge['source_ids'])
            for source_id in edge['source_ids']: self.assertIn(source_id,sources)
            self.assertIn(edge['evidence_status'],{'observed','reported','inferred','unresolved'})

    def test_mixed_package_cycle_recorded(self):
        data=read_json('data/relationships.json')
        g=nx.DiGraph((e['from'],e['to']) for e in data['edges'] if e['view']=='package')
        cycles=[set(c) for c in nx.strongly_connected_components(g) if len(c)>1]
        self.assertEqual(cycles,[{'sol','eta_cli'}])

    def test_runtime_edges_not_package_edges(self):
        edges=read_json('data/relationships.json')['edges']
        for e in edges:
            if e['kind'] in {'mcp-service','runtime-host','runtime-configuration','adapter-surface'}:
                self.assertEqual(e['view'],'runtime')

    def test_strict_json_rejects_duplicate_keys(self):
        with self.assertRaises(ValueError): parse_model_output('{"kind": 1, "kind": 2}')

    def test_strict_json_rejects_extensions(self):
        for raw in ['{"confidence": NaN}', '{"confidence": Infinity}', '{} trailing', '```json\n{}\n```', '[]']:
            with self.assertRaises(ValueError): parse_model_output(raw)

    def test_strict_json_rejects_oversize(self):
        with self.assertRaises(ValueError): parse_model_output('{"x":"'+'x'*20000+'"}')

    def test_synthetic_source_digest(self):
        request=read_json('classification/example-input.json')
        self.assertEqual(hashlib.sha256(request['document']['text'].encode()).hexdigest(), request['source_ref']['content_sha256'])
        self.assertEqual(request['source_ref']['kind'],'synthetic-test-fixture')

    def test_schema_and_positive_fixture(self):
        schema=read_json('classification/document-labels.schema.json')
        jsonschema.Draft202012Validator.check_schema(schema)
        self.assertTrue(validate_context(read_json('classification/example-output.json'),read_json('classification/example-input.json')))

    def test_reject_extra_action(self):
        out=read_json('classification/example-output.json');out['delete_files']=['README.md']
        with self.assertRaises(jsonschema.ValidationError):
            validate_context(out,read_json('classification/example-input.json'))

    def test_reject_claimed_authority(self):
        out=read_json('classification/example-output.json');out['status']='accepted'
        with self.assertRaises(jsonschema.ValidationError):
            validate_context(out,read_json('classification/example-input.json'))

    def test_reject_unknown_project(self):
        out=read_json('classification/example-output.json');out['projects'][0]['project_id']='invented-project'
        with self.assertRaises(ValueError):
            validate_context(out,read_json('classification/example-input.json'))

    def test_reject_unknown_topic(self):
        out=read_json('classification/example-output.json');out['topics'][0]['id']='invented-topic'
        with self.assertRaises(jsonschema.ValidationError):
            validate_context(out,read_json('classification/example-input.json'))

    def test_reject_schema_valid_topic_excluded_by_request(self):
        request=read_json('classification/example-input.json')
        out=read_json('classification/example-output.json')
        request['taxonomy']['allowed_topics'].remove(out['topics'][0]['id'])
        with self.assertRaisesRegex(ValueError, 'Unknown topic identifier'):
            validate_context(out,request)

    def test_accept_narrow_request_containing_output_topics(self):
        request=read_json('classification/example-input.json')
        out=read_json('classification/example-output.json')
        request['taxonomy']['allowed_topics']=[topic['id'] for topic in out['topics']]
        self.assertTrue(validate_context(out,request))

    def test_stage_graph_preserves_card_prerequisites(self):
        graph=(ROOT/'graphs/03-proposed-migration-stages.mmd').read_text()
        stages={}
        for node,label in re.findall(r'^\s*(\w+)\["([^"]+)"\]',graph,re.M):
            for story in re.findall(r'E1\.\d{2}',label):
                stages[story]=node
        edges=set()
        for line in graph.splitlines():
            if '-->' in line:
                chain=[x.strip() for x in line.split('-->')]
                edges.update(zip(chain,chain[1:]))
        cards=ROOT.parents[1]/'agile/kanban'
        for card in cards.glob('e1-*.md'):
            text=card.read_text()
            story=re.search(r'story_id: "(E1\.\d{2})"',text)[1]
            blockers=re.search(r'^blocked_by: (.+)$',text,re.M)
            for number in re.findall(r'e1-(\d{2})-',blockers[1] if blockers else ''):
                source=stages[f'E1.{number}'];target=stages[story]
                if source != target:
                    self.assertIn((source,target),edges,card.name)

    def test_edge_coordinates_are_full_and_revision_scoped(self):
        text=(ROOT/'edge-disposition.md').read_text()
        commits=re.findall(r'(?:@|#)([0-9a-f]{7,40})\b',text)
        self.assertGreater(len(commits),5)
        self.assertTrue(all(len(sha)==40 for sha in commits))
        sources=read_json('data/sources.json')['sources']
        self.assertEqual(sources['rheos.deps']['revision'],
                         '476b07bd66efb84566a4159556deacb1e9407e6f')
        self.assertIn('0ed56aa74a53a1d1e9c2e55ce95451817a7f3a90',
                      sources['rheos.deps']['notes'])

    def test_registration_is_explicitly_pending_acceptance(self):
        modules=read_json('data/registration.json')['modules']
        self.assertEqual({m['path'] for m in modules},
                         {'kanban-orchestrator','clio','chat-ui','rheos',
                          'session-mycology','sol','osmos','receipt-river','axxium'})
        for module in modules:
            self.assertRegex(module['revision'],r'^[0-9a-f]{40}$')
            self.assertEqual(module['acceptance'],'pending')

    def test_historical_receipt_bytes_are_preserved(self):
        workspace=ROOT.parents[2]
        archive=workspace/'.ημ/archive/pr96-c551e986'
        self.assertEqual(hashlib.sha256((archive/'root-receipts.edn').read_bytes()).hexdigest(),
                         'a9a0f48df3aaf53cec1a0d34f7551e77850577cb9d6a8c38543363643b3fffed')
        self.assertEqual(hashlib.sha256((archive/'planning-archive-receipt.edn').read_bytes()).hexdigest(),
                         '40a811fcb1415bc28dd94391690fd270ce80b47eb2f125bdec74aae91c3ad033')
        self.assertFalse((workspace/'receipts.edn').exists())

    def test_reject_invalid_span(self):
        out=read_json('classification/example-output.json');out['kind']['evidence'][0]['end_line']=999
        with self.assertRaises(ValueError):
            validate_context(out,read_json('classification/example-input.json'))

    def test_reject_fabricated_quote(self):
        out=read_json('classification/example-output.json');out['kind']['evidence'][0]['quote']='This source is fully implemented.'
        with self.assertRaises(ValueError):
            validate_context(out,read_json('classification/example-input.json'))

    def test_reject_duplicate_labels(self):
        out=read_json('classification/example-output.json');out['projects'].append(copy.deepcopy(out['projects'][0]))
        with self.assertRaises(ValueError):
            validate_context(out,read_json('classification/example-input.json'))

    def test_reject_empty_abstention(self):
        out=read_json('classification/example-output.json');out['abstained']=True
        with self.assertRaises(jsonschema.ValidationError):
            validate_context(out,read_json('classification/example-input.json'))

    def test_rendered_graphs_are_svg(self):
        for name in ['01-observed-package-dependencies','02-runtime-and-contract-relationships']:
            root=ET.parse(ROOT/'graphs'/f'{name}.svg').getroot()
            self.assertTrue(root.tag.endswith('svg'))
            self.assertGreater((ROOT/'graphs'/f'{name}.png').stat().st_size,1000)

    def test_epic_stories_and_requested_steps(self):
        texts=[(ROOT/'epic-01-operation-eta-mu-breakdown.md').read_text(),(ROOT/'epic-02-rheos-document-corpus.md').read_text()]
        for i,text in enumerate(texts,1):
            ids=re.findall(r'^### (E\d\.\d{2}) ',text,re.M)
            self.assertEqual(ids,[f'E{i}.{n:02d}' for n in range(1,13)])
        for step in range(1,15): self.assertRegex(texts[0],rf'\| {step}:')

    def test_nd_edn_has_one_record_per_edge(self):
        lines=(ROOT/'data/relationships.nd.edn').read_text().splitlines()
        self.assertEqual(len(lines),len(read_json('data/relationships.json')['edges']))
        # This checks framing only, not EDN parsing or Clio admission.
        self.assertTrue(all(line.startswith('{') and line.endswith('}') for line in lines))

if __name__=='__main__':
    suite=unittest.defaultTestLoader.loadTestsFromTestCase(PackChecks)
    result=unittest.TextTestRunner(verbosity=2).run(suite)
    report={'scope':'Planning-pack checks only; not source repository tests or model quality evaluation',
            'tests_run':result.testsRun,'failures':len(result.failures),'errors':len(result.errors),
            'success':result.wasSuccessful(),
            'edn_validation':'Record framing checked; no EDN parser or Clio admission executed.',
            'mermaid_validation':'Generated diagram text retained; rendered previews use Graphviz, not a Mermaid renderer.'}
    (ROOT/'checks'/'validation-report.json').write_text(json.dumps(report,indent=2)+'\n')
    raise SystemExit(0 if result.wasSuccessful() else 1)
