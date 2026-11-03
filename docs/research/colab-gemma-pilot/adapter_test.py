# SPDX-License-Identifier: GPL-3.0-or-later
import importlib.util,json,pathlib,tempfile,unittest,fcntl
from unittest.mock import patch
p=pathlib.Path(__file__).with_name('run_colab.py');spec=importlib.util.spec_from_file_location('adapter',p);m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
class AdapterTests(unittest.TestCase):
    def test_usage_strict(self):
        self.assertEqual(m.parse_usage('Current balance: 399.54 compute units\nUsage rate: 0.08/hr\nActive assignments: 1')['rate'],.08)
        for s in ['', 'Current balance: NaN compute units\nUsage rate: 0/hr\nActive assignments: 1','Current balance: -1 compute units\nUsage rate: 0/hr\nActive assignments: 1']:
            with self.assertRaises(RuntimeError):m.parse_usage(s)
    def test_unknown_endpoint_blocks(self):
        with tempfile.TemporaryDirectory() as td,patch.object(m,'DATA',pathlib.Path(td)):
            c=m.Campaign();c.state={'cleanup_verified':False,'active_job':{'session':'owned'}}
            self.assertFalse(c.cleanup());self.assertFalse(json.loads(c.statefile.read_text())['cleanup_verified'])
    def test_stop_is_not_release_proof(self):
        with tempfile.TemporaryDirectory() as td,patch.object(m,'DATA',pathlib.Path(td)):
            c=m.Campaign();c.state={'cleanup_verified':False,'active_job':{'session':'owned','endpoint':'a'}}
            c.command=lambda *a,**kw:None;c.endpoints=lambda:{'a','unrelated'}
            self.assertFalse(c.cleanup())
            c.endpoints=lambda:{'unrelated'}
            self.assertTrue(c.cleanup())
    def test_telemetry_failure_cancels(self):
        c=m.Campaign();c.done.wait=lambda n:False
        c.usage=lambda: (_ for _ in ()).throw(TimeoutError('telemetry unavailable'))
        c.watchdog();self.assertTrue(c.abort.is_set());self.assertIn('TimeoutError',c.guard_error)
    def test_concurrent_runner_denied_before_telemetry(self):
        with tempfile.TemporaryDirectory() as td,patch.object(m,'DATA',pathlib.Path(td)):
            with (pathlib.Path(td)/'campaign.lock').open('a') as lock:
                fcntl.flock(lock,fcntl.LOCK_EX|fcntl.LOCK_NB)
                c=m.Campaign();c.usage=lambda: self.fail('A second runner reached telemetry')
                with self.assertRaises(BlockingIOError):c.run()
    def test_atomic_state_survives_reload(self):
        with tempfile.TemporaryDirectory() as td:
            p=pathlib.Path(td)/'state.json'
            m.atomic_json(p,{'committed_units':28,'cap_units':30})
            self.assertEqual(json.loads(p.read_text())['committed_units'],28)
            self.assertEqual(p.stat().st_mode & 0o777,0o600)
    def test_mutable_cap_refused_by_law(self):
        c=m.Campaign();c.state={'committed_units':28,'initial_balance':399,'cleanup_verified':True}
        self.assertFalse(c.decision({'balance':399,'rate':0,'observed_at':m.now()})['admit'])
if __name__=='__main__':unittest.main()
