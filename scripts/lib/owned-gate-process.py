#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-or-later
"""Own one Linux gate and all descendants while the receipt caller waits.

The adapter reserves the private process group until teardown and reaps
descendants adopted by its Linux subreaper. Gate stdout
and stderr stay inherited; only the control result uses FD3. The child does
not inherit FD3. A parent-death SIGTERM and checked parent PID keep a blocked
synchronous NBB caller from orphaning the custody adapter on interruption.
"""
import ctypes
import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import time

POLL_SECONDS = 0.01
CLEANUP_SECONDS = 5.0

def process_identity(pid):
    """Read kernel identity and parentage without inspecting command/env data."""
    try:
        fields = Path(f"/proc/{pid}/stat").read_bytes().rsplit(b")", 1)[1].split()
        return {
            "start": int(fields[19]), "parent": int(fields[1]),
            "group": int(fields[2]), "state": fields[0].decode("ascii"),
        }
    except (FileNotFoundError, ProcessLookupError):
        return None

def direct_children(pid):
    """Include children forked by any thread of an owned Linux process."""
    result = set()
    try:
        tasks = list(Path(f"/proc/{pid}/task").iterdir())
    except (FileNotFoundError, ProcessLookupError):
        return result
    for task in tasks:
        try:
            result.update(int(value) for value in (task / "children").read_text().split())
        except (FileNotFoundError, ProcessLookupError):
            pass
    return result

def enable_subreaper():
    """Refuse to spawn unless Linux can retain/reap orphaned owned children."""
    required = (hasattr(os, "pidfd_open"), hasattr(signal, "pidfd_send_signal"),
                hasattr(os, "waitid"), hasattr(os, "WNOWAIT"))
    if sys.platform != "linux" or not all(required):
        raise RuntimeError("Linux subreaper, pidfd, and waitid support required")
    if not process_identity(os.getpid()) or not Path("/proc/self/task").is_dir():
        raise RuntimeError("Readable Linux /proc child custody required")
    try:
        Path(f"/proc/self/task/{os.getpid()}/children").read_text()
    except OSError as error:
        raise RuntimeError(
            "Linux /proc task children (CONFIG_PROC_CHILDREN) required") from error
    if direct_children(os.getpid()):
        raise RuntimeError("Supervisor must start without pre-existing children")
    probe = os.pidfd_open(os.getpid())
    os.close(probe)
    libc = ctypes.CDLL(None, use_errno=True)
    if libc.prctl(36, ctypes.c_ulong(1), 0, 0, 0) != 0:
        raise OSError(ctypes.get_errno(), "PR_SET_CHILD_SUBREAPER failed")
    enabled = ctypes.c_int()
    if libc.prctl(37, ctypes.byref(enabled), 0, 0, 0) != 0 or enabled.value != 1:
        raise RuntimeError("PR_GET_CHILD_SUBREAPER did not confirm custody")

def leader_exit(pid):
    """Observe exit without reaping, preserving the private process-group id."""
    return os.waitid(os.P_PID, pid, os.WEXITED | os.WNOHANG | os.WNOWAIT)

def reap_adopted_children(leader, terminate):
    """Reap this fresh adapter's adopted children, even after setup failed.

    These direct child PIDs remain reserved until this single-threaded adapter
    reaps them (SIGCHLD is explicitly default). Their kernel parentage grants
    custody without relying on capture/selector or pidfd allocation succeeding.
    """
    for pid in direct_children(os.getpid()) - {leader}:
        current = process_identity(pid)
        if current and current["parent"] == os.getpid():
            try:
                if terminate:
                    os.kill(pid, signal.SIGKILL)
                os.waitpid(pid, os.WNOHANG)
            except (ChildProcessError, ProcessLookupError):
                pass

def arm_parent_death(expected_parent):
    """Bind the original caller before any gate can start; close the prctl race."""
    if expected_parent <= 1 or os.getppid() != expected_parent:
        raise RuntimeError("Gate caller identity changed before custody")
    libc = ctypes.CDLL(None, use_errno=True)
    if libc.prctl(1, signal.SIGTERM, 0, 0, 0) != 0:
        raise OSError(ctypes.get_errno(), "PR_SET_PDEATHSIG failed")
    if os.getppid() != expected_parent:
        raise RuntimeError("Gate caller exited while parent-death custody armed")


def supervise_gate(command, expected_parent):
    """Retain/reap the gate identity and every adopted detached descendant."""
    process = None
    cancelled = []
    handlers = {}
    outcome = {"status": None, "signal": None, "pid": None,
               "error": None, "cleanupComplete": True, "cancelled": False}
    try:
        if not command:
            raise ValueError("Gate command is required")
        enable_subreaper()
        for sig in (signal.SIGINT, signal.SIGTERM):
            handlers[sig] = signal.signal(
                sig, lambda received, _frame: cancelled.append(received))
        handlers[signal.SIGCHLD] = signal.signal(signal.SIGCHLD, signal.SIG_DFL)
        arm_parent_death(expected_parent)
        # No pipe/capture policy is added: the gate retains its normal streams.
        # close_fds=True prevents it from writing the FD3 control result.
        if cancelled:
            raise InterruptedError("Gate custody interrupted before spawn")
        process = subprocess.Popen(command, stdin=None, stdout=None, stderr=None,
                                   start_new_session=True, close_fds=True)
        outcome["pid"] = process.pid
        while True:
            reap_adopted_children(process.pid, False)
            if cancelled or os.getppid() != expected_parent:
                outcome["cancelled"] = True
                outcome["signal"] = signal.Signals(
                    cancelled[0] if cancelled else signal.SIGTERM).name
                break
            observed = leader_exit(process.pid)
            if observed:
                if observed.si_code == os.CLD_EXITED:
                    outcome["status"] = observed.si_status
                else:
                    outcome["signal"] = signal.Signals(observed.si_status).name
                break
            time.sleep(POLL_SECONDS)
    except Exception as error:
        outcome["error"] = f"{type(error).__name__}: {error}"
    finally:
        if process:
            outcome["cleanupComplete"] = False
            deadline = time.monotonic() + CLEANUP_SECONDS
            leader_reaped = False
            try:
                while time.monotonic() < deadline:
                    if not leader_reaped:
                        # Popen started a private session. Keep its leader
                        # unreaped until the last group signal reserves the ID.
                        try:
                            os.killpg(process.pid, signal.SIGKILL)
                        except ProcessLookupError:
                            pass
                    reap_adopted_children(process.pid, True)
                    if not leader_reaped and leader_exit(process.pid):
                        process.wait(timeout=0)
                        leader_reaped = True
                    if leader_reaped:
                        # Only ECHILD proves the subreaper has no children;
                        # an empty /proc listing is not a sufficient proof.
                        try:
                            while os.waitpid(-1, os.WNOHANG)[0]:
                                pass
                        except ChildProcessError:
                            outcome["cleanupComplete"] = True
                            break
                    time.sleep(POLL_SECONDS)
                if not outcome["cleanupComplete"]:
                    outcome["error"] = outcome["error"] or "Gate custody cleanup deadline exceeded"
            except Exception as error:
                outcome["error"] = outcome["error"] or f"Gate cleanup {type(error).__name__}: {error}"
        for sig, handler in handlers.items():
            signal.signal(sig, handler)
        if cancelled or (handlers and os.getppid() != expected_parent):
            outcome["cancelled"] = True
            outcome["signal"] = signal.Signals(
                cancelled[0] if cancelled else signal.SIGTERM).name
    return outcome


def main():
    # The CLI owns this fixed protocol. Invalid setup never starts a gate.
    try:
        if len(sys.argv) < 4 or sys.argv[2] != "--":
            raise ValueError("Expected caller PID, --, and gate command")
        result = supervise_gate(sys.argv[3:], int(sys.argv[1]))
    except Exception as error:
        result = {"status": None, "signal": None, "pid": None,
                  "error": f"{type(error).__name__}: {error}",
                  "cleanupComplete": True, "cancelled": False}
    payload = (json.dumps(result, ensure_ascii=True, separators=(",", ":")) + "\n").encode()
    try:
        with os.fdopen(3, "wb", closefd=True) as control:
            control.write(payload)
            control.flush()
    except OSError:
        # The interrupted caller may already be gone. Cleanup happened first.
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
