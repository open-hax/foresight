#!/usr/bin/env python3
"""Offline, pinned receipt snapshot reconciliation; never writes an active ledger.

forward --output DIR [--plan]
reverse --input DIR --output DIR [--plan]

DIR is a complete snapshot containing receipts.edn and receipts/. Publish only
while all writers are stopped, after the existing Receipt River verifier passes.
Reverse accepts only the exact forward snapshot, including fork baseline segments;
any later write requires separate reconciliation and is refused. This tool does
not activate either snapshot or certify that an offline snapshot is current.
"""
import argparse
import ctypes
import errno
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile

FORK = "a8dff342cd69d27d621fe8d2c0b02ba3da624927"
UPSTREAM = "d5921c300fab28a08cdec6eb9f48ea03994c0e44"
FORK_SHA = "8b9e61649758441a76a396bd6d117aaab6ab1fae3b2577e754049d9c6d0fc0a2"
UPSTREAM_SHA = "7313ba946f8b1c02e2097134e930a47a4de75f09a12045d301b60a06d7df6e09"
REPO = Path(__file__).resolve().parent.parent


def digest(data):
    return hashlib.sha256(data).hexdigest()


def git(*args):
    return subprocess.check_output(["git", "--no-replace-objects", "-C", str(REPO), *args])


def load_inputs():
    fork = git("show", FORK + ":.ημ/receipts.edn")
    upstream = git("show", UPSTREAM + ":.ημ/receipts.edn")
    if digest(fork) != FORK_SHA or digest(upstream) != UPSTREAM_SHA:
        raise ValueError("pinned legacy blob hash mismatch")
    tree = git("ls-tree", "-rz", "--full-tree", FORK, "--", ".ημ/receipts/")
    baseline = {}
    for entry in tree.split(b"\0"):
        if not entry:
            continue
        metadata, name = entry.split(b"\t", 1)
        mode, kind, oid = metadata.split(b" ")
        path = name.decode("utf-8")
        prefix = ".ημ/receipts/"
        if mode != b"100644" or kind != b"blob" or not path.startswith(prefix) or "/" in path[len(prefix):]:
            raise ValueError("unexpected pinned receipt tree entry")
        baseline["receipts/" + path[len(prefix):]] = git("cat-file", "blob", oid.decode())
    if not baseline:
        raise ValueError("pinned baseline segments missing")
    return fork, upstream, baseline


def snapshots(fork, upstream, baseline):
    if not fork.endswith(b"\n") or not upstream.startswith(fork) or not upstream.endswith(b"\n"):
        raise ValueError("legacy prefix or newline mismatch")
    suffix = upstream[len(fork):]
    rows = suffix.splitlines(keepends=True)
    if len(rows) != 48 or any(not row.endswith(b"\n") or b"\r" in row or row == b"\n" for row in rows):
        raise ValueError("expected exactly 48 nonempty LF-terminated physical rows")
    # Timestamp is the UTC second of the pinned upstream commit, not the row time.
    forward = dict(baseline)
    forward["receipts.edn"] = fork
    for index, row in enumerate(rows, 1):
        name = f"receipts/2026-11-20T130102Z-sync-main-150-{index:03d}-{digest(row)[:16]}.edn"
        if name in forward:
            raise ValueError("segment collision")
        forward[name] = row
    reverse = dict(baseline)
    reverse["receipts.edn"] = fork + b"".join(rows)
    if reverse["receipts.edn"] != upstream:
        raise ValueError("round-trip mismatch")
    return forward, reverse


def snapshot_contents(root):
    if root.is_symlink() or not root.is_dir():
        raise ValueError("snapshot must be a real directory")
    result = {}
    for path in root.rglob("*"):
        relative = path.relative_to(root).as_posix()
        if path.is_symlink():
            raise ValueError("symlink in snapshot: " + relative)
        if path.is_dir():
            if relative != "receipts":
                raise ValueError("unexpected snapshot directory: " + relative)
        elif path.is_file():
            result[relative] = path.read_bytes()
        else:
            raise ValueError("unexpected snapshot entry: " + relative)
    return result


def check_snapshot(root, expected):
    actual = snapshot_contents(root)
    if actual != expected:
        missing = sorted(expected.keys() - actual.keys())
        extra = sorted(actual.keys() - expected.keys())
        changed = sorted(k for k in expected.keys() & actual.keys() if expected[k] != actual[k])
        raise ValueError(f"snapshot drift (including possible post-migration writes): missing={missing}, extra={extra}, changed={changed}")


def require_offline_path(path):
    # Reject both lexical and resolved paths so symlink aliases cannot target ledgers.
    if ".ημ" in path.absolute().parts or ".ημ" in path.resolve().parts:
        raise ValueError("snapshot must not be inside .ημ")


def safe_output(output):
    require_offline_path(output)
    if not output.parent.is_dir():
        raise ValueError("output parent must already exist")


def sync_directory(path):
    fd = os.open(path, os.O_RDONLY | os.O_DIRECTORY)
    try:
        os.fsync(fd)
    finally:
        os.close(fd)


def publish(output, expected):
    safe_output(output)
    if output.exists() or output.is_symlink():
        check_snapshot(output, expected)
        sync_directory(output.parent)
        return "already complete"
    stage = Path(tempfile.mkdtemp(prefix=".receipt-sync-v1-", dir=output.parent))
    try:
        for name, data in sorted(expected.items()):
            path = stage / name
            path.parent.mkdir(parents=True, exist_ok=True)
            with path.open("xb") as stream:
                stream.write(data)
                stream.flush()
                os.fsync(stream.fileno())
        check_snapshot(stage, expected)
        sync_directory(stage / "receipts")
        sync_directory(stage)
        # Atomic no-replace publication; a concurrent creator is never overwritten.
        libc = ctypes.CDLL(None, use_errno=True)
        result = libc.renameat2(-100, os.fsencode(stage), -100, os.fsencode(output), 1)
        if result:
            error = ctypes.get_errno()
            if error == errno.EEXIST:
                check_snapshot(output, expected)
                sync_directory(output.parent)
                return "already complete"
            raise OSError(error, os.strerror(error))
        sync_directory(output.parent)
        return "published"
    finally:
        if stage.exists():
            shutil.rmtree(stage)


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("direction", choices=("forward", "reverse"))
    parser.add_argument("--input", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--plan", action="store_true")
    args = parser.parse_args()
    forward, reverse = snapshots(*load_inputs())
    safe_output(args.output)
    if args.direction == "reverse":
        if args.input is None:
            parser.error("reverse requires --input")
        require_offline_path(args.input)
        check_snapshot(args.input, forward)
        expected = reverse
    else:
        if args.input is not None:
            parser.error("forward does not accept --input")
        expected = forward
    if args.output.exists() or args.output.is_symlink():
        check_snapshot(args.output, expected)
    print(f"{args.direction} v1: {len(expected)} files, fork={FORK}, upstream={UPSTREAM}, output={args.output}")
    if not args.plan:
        print(publish(args.output, expected))


if __name__ == "__main__":
    try:
        main()
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        print(f"receipt reconciliation refused: {error}", file=sys.stderr)
        sys.exit(1)
