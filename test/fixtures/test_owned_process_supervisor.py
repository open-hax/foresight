import base64
import hashlib
import importlib.util
from pathlib import Path
import signal
import sys
import time
import unittest
from unittest import mock


spec = importlib.util.spec_from_file_location(
    "supervisor", Path(__file__).with_name("owned-process-supervisor.py"))
supervisor = importlib.util.module_from_spec(spec)
spec.loader.exec_module(supervisor)


class SupervisorTests(unittest.TestCase):
    def test_identity_ignores_non_utf8_name(self):
        fields = [b"S", b"10", b"11"] + [b"0"] * 16 + [b"42"]
        with mock.patch.object(Path, "read_bytes", return_value=b"123 (\xff) " + b" ".join(fields)):
            self.assertEqual(supervisor.process_identity(123),
                             {"state": "S", "parent": 10, "group": 11, "start": 42})

    def test_late_exit_is_timeout(self):
        original = supervisor.Capture.drain
        first = True

        def delayed(capture, wait):
            nonlocal first
            if first:
                first = False
                time.sleep(0.08)
            return original(capture, wait)

        with mock.patch.object(supervisor.Capture, "drain", delayed):
            result = supervisor.supervise([sys.executable, "-c", "pass"], 30)
        self.assertEqual(result["termination"], "timeout")
        self.assertTrue(result["timed-out?"])
        self.assertTrue(result["cleanup-complete?"])

    def test_unrepresentable_cleanup_duration_does_not_launch(self):
        with mock.patch.object(supervisor.subprocess, "Popen") as launch:
            result = supervisor.supervise(["unused"], 100, cleanup_ms=10 ** 400)
        launch.assert_not_called()
        self.assertEqual(result["termination"], "setup-error")

    def test_cancellation_during_teardown(self):
        original = supervisor.reap_adopted_children
        sent = False

        def interrupt(leader, terminate):
            nonlocal sent
            if not sent:
                sent = True
                signal.raise_signal(signal.SIGTERM)
            original(leader, terminate)

        with mock.patch.object(supervisor, "reap_adopted_children", interrupt):
            result = supervisor.supervise([sys.executable, "-c", "pass"], 1000)
        self.assertEqual(result["termination"], "cancelled")
        self.assertTrue(result["cleanup-complete?"])

    def test_truncated_utf8_preserves_raw_prefix(self):
        result = supervisor.supervise(
            [sys.executable, "-c", "import os; os.write(1, '€'.encode())"], 1000, 2)
        prefix = '€'.encode()[:2]
        self.assertEqual(result["termination"], "output-limit")
        self.assertEqual(base64.b64decode(result["output-base64"]), prefix)
        self.assertEqual(result["output-sha256"], hashlib.sha256(prefix).hexdigest())
        self.assertTrue(result["cleanup-complete?"])

    def test_complete_invalid_utf8_remains_error(self):
        result = supervisor.supervise(
            [sys.executable, "-c", "import os; os.write(1, bytes([255]))"], 1000)
        self.assertEqual(result["termination"], "cleanup-error")
        self.assertFalse(result["capture-truncated?"])


if __name__ == "__main__":
    unittest.main()
