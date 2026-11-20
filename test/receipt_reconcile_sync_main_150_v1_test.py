import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

SCRIPT = Path(__file__).resolve().parents[1] / "scripts/reconcile_receipts_sync_main_150_v1.py"
spec = importlib.util.spec_from_file_location("receipt_sync_v1", SCRIPT)
mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)


class ReconciliationTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.fork, cls.upstream, cls.baseline = mod.load_inputs()
        cls.forward, cls.reverse = mod.snapshots(cls.fork, cls.upstream, cls.baseline)

    def test_pinned_exact_suffix_and_roundtrip(self):
        names = sorted(set(self.forward) - set(self.baseline) - {"receipts.edn"})
        self.assertEqual(len(names), 48)
        for name in names:
            self.assertRegex(name, r"^receipts/[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{6}[0-9]{0,9}Z-[0-9a-z][0-9a-z-]*\.edn$")
        self.assertEqual(b"".join(self.forward[n] for n in names), self.upstream[len(self.fork):])
        self.assertEqual(self.forward["receipts.edn"], self.fork)
        self.assertEqual(self.reverse["receipts.edn"], self.upstream)
        self.assertEqual({k: self.reverse[k] for k in self.baseline}, self.baseline)

    def test_drift_refused(self):
        with self.assertRaises(ValueError):
            mod.snapshots(self.fork, self.upstream + b"extra\n", self.baseline)
        with patch.object(mod, "FORK_SHA", "0" * 64):
            with self.assertRaises(ValueError):
                mod.load_inputs()

    def test_pinned_reads_disable_git_replacement(self):
        with patch.object(mod.subprocess, "check_output", return_value=b"blob") as run:
            self.assertEqual(mod.git("cat-file", "blob", "example"), b"blob")
            run.assert_called_once_with(
                ["git", "--no-replace-objects", "-C", str(mod.REPO), "cat-file", "blob", "example"]
            )

    def test_plan_retries_reverse_and_post_write_refusal(self):
        with tempfile.TemporaryDirectory() as tmp:
            forward = Path(tmp) / "forward"
            reverse = Path(tmp) / "reverse"
            def run(*args):
                return subprocess.run([str(SCRIPT), *map(str, args)], capture_output=True, text=True)
            self.assertEqual(run("forward", "--output", forward, "--plan").returncode, 0)
            self.assertFalse(forward.exists())
            self.assertEqual(run("forward", "--output", forward).returncode, 0)
            self.assertEqual(run("forward", "--output", forward).returncode, 0)
            self.assertEqual(run("reverse", "--input", forward, "--output", reverse).returncode, 0)
            self.assertEqual(run("reverse", "--input", forward, "--output", reverse).returncode, 0)
            mod.check_snapshot(reverse, self.reverse)
            (forward / "receipts/new.edn").write_text("{:new true}\n")
            result = run("reverse", "--input", forward, "--output", reverse)
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("post-migration writes", result.stderr)
            (forward / "receipts/new.edn").unlink()
            segment = next(forward / name for name in self.forward if name not in self.baseline and name != "receipts.edn")
            segment.write_bytes(b"changed\n")
            self.assertNotEqual(run("reverse", "--input", forward, "--output", reverse, "--plan").returncode, 0)

    def test_interrupted_output_refused_without_mutation(self):
        with tempfile.TemporaryDirectory() as tmp:
            out = Path(tmp) / "output"
            out.mkdir()
            (out / "receipts.edn").write_bytes(self.fork[:20])
            with self.assertRaises(ValueError):
                mod.publish(out, self.forward)
            self.assertEqual((out / "receipts.edn").read_bytes(), self.fork[:20])
            # An abandoned private stage does not prevent a clean retry.
            abandoned = Path(tmp) / ".receipt-sync-v1-interrupted"
            abandoned.mkdir()
            clean = Path(tmp) / "clean"
            self.assertEqual(mod.publish(clean, self.forward), "published")
            self.assertTrue(abandoned.exists())

    def test_ledger_output_forbidden(self):
        with tempfile.TemporaryDirectory() as tmp:
            ledger = Path(tmp) / ".ημ"
            ledger.mkdir()
            with self.assertRaises(ValueError):
                mod.publish(ledger / "output", self.forward)
            alias = Path(tmp) / "alias"
            alias.symlink_to(ledger, target_is_directory=True)
            with self.assertRaises(ValueError):
                mod.publish(alias / "output", self.forward)
            result = subprocess.run(
                [str(SCRIPT), "reverse", "--input", str(alias), "--output", str(Path(tmp) / "out")],
                capture_output=True,
                text=True,
            )
            self.assertNotEqual(result.returncode, 0)
            self.assertIn("snapshot must not be inside .ημ", result.stderr)


if __name__ == "__main__":
    unittest.main()
