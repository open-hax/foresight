#!/usr/bin/env python3
"""Archive inactive scratch children to Drive; delete only after streamed verification.

GPL-3.0-or-later. Dry run by default. Requires Python, GNU tar, zstd, rclone.
This deliberately does not manage Rheos state or remove Git worktrees.
"""
import argparse
import fcntl
import hashlib
import inspect
import json
import os
from pathlib import Path
import shutil
import stat
import subprocess
import sys
import tempfile
import time
import uuid


def snapshot(root):
    root_stat = root.lstat()
    result = {'.': (root_stat.st_mode, root_stat.st_size, root_stat.st_mtime_ns,
                    root_stat.st_ctime_ns, root_stat.st_ino)}
    for parent, dirs, files in os.walk(root, followlinks=False):
        for name in dirs + files:
            path = Path(parent) / name
            st = path.lstat()
            result[str(path.relative_to(root))] = (st.st_mode, st.st_size,
                st.st_mtime_ns, st.st_ctime_ns, st.st_ino)
    return result


def remove_verified_tree(root):
    """Allow removal of owner-controlled frozen dirs after backup and source checks."""
    for parent, dirs, files in os.walk(root, followlinks=False):
        descriptor = os.open(parent, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
        try:
            info = os.fstat(descriptor)
            if info.st_uid == os.getuid():
                os.fchmod(descriptor, stat.S_IMODE(info.st_mode) | stat.S_IWUSR | stat.S_IXUSR)
        finally:
            os.close(descriptor)
    shutil.rmtree(root)


def _process_paths():
    """Inspect process paths only, never file contents or process environments."""
    paths = []
    for proc in Path('/proc').iterdir():
        if not proc.name.isdigit() or int(proc.name) == os.getpid():
            continue
        try:
            links = [proc / 'cwd', proc / 'root'] + list((proc / 'fd').iterdir())
            for link in links:
                try:
                    paths.append(os.readlink(link).removesuffix(' (deleted)'))
                except FileNotFoundError:
                    pass
        except (FileNotFoundError, ProcessLookupError):
            continue
    return paths


def active_paths():
    """Require visibility across processes; optional sudo is read-only and noninteractive."""
    try:
        return _process_paths()
    except PermissionError:
        code = ('import os,json\nfrom pathlib import Path\n' +
                inspect.getsource(_process_paths) + '\nprint(json.dumps(_process_paths()))')
        result = subprocess.run(['sudo', '-n', sys.executable, '-c', code],
                                capture_output=True, text=True)
        if result.returncode:
            raise RuntimeError('Cannot inspect process usage; refusing cleanup')
        return json.loads(result.stdout)


def in_use(path, paths):
    prefix = str(path) + '/'
    return any(p == str(path) or p.startswith(prefix) for p in paths)


def run(args):
    subprocess.run(args, check=True, stdout=subprocess.DEVNULL)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', type=Path, help='Explicit scratch parent; never a product root')
    parser.add_argument('--remote', default='gdrive:AgentWorkspaceArchives')
    parser.add_argument('--min-age-days', type=float, default=7)
    parser.add_argument('--apply', action='store_true', help='Upload, verify, then remove eligible children')
    parser.add_argument('--state-dir', type=Path, required=True, help='Durable local manifest directory outside source')
    args = parser.parse_args()
    source = args.source.resolve(strict=True)
    state = args.state_dir.resolve()
    if source == Path('/') or source == Path.home() or source in state.parents or source == state:
        raise RuntimeError('Unsafe source or manifest location')
    if args.min_age_days < 1:
        raise RuntimeError('Minimum age must be at least one day')
    # Serialize this cleaner across invocations. Other agents still need to honor
    # the inactivity rule; an open-file scan cannot prevent future file opens.
    if args.apply:
        state.mkdir(parents=True, exist_ok=True)
        lock = (state / '.cleanup.lock').open('a')
        fcntl.flock(lock, fcntl.LOCK_EX | fcntl.LOCK_NB)
    cutoff = time.time_ns() - int(args.min_age_days * 86400 * 1e9)
    busy = active_paths()
    selected, before, skipped = [], {}, []
    for child in sorted(source.iterdir()):
        if not child.is_dir() or child.is_symlink():
            skipped.append([child.name, 'not a real directory'])
            continue
        tree = snapshot(child)
        st = child.stat()
        if child.name == '.git' or '.git' in tree or any(
            key.endswith('/.git') and not stat_is_dir(value[0]) for key, value in tree.items()):
            skipped.append([child.name, 'Git ownership boundary'])
        elif max([st.st_mtime_ns] + [v[2] for v in tree.values()]) > cutoff:
            skipped.append([child.name, 'recent modification'])
        elif str(source) in busy or in_use(child, busy):
            skipped.append([child.name, 'open file or process working directory'])
        else:
            selected.append(child.name)
            before[child.name] = tree
    print(json.dumps({'eligible_directories': len(selected), 'skipped': skipped, 'apply': args.apply}), flush=True)
    if not args.apply or not selected:
        return
    # Validate remote access before producing an archive. No local large temporary archive.
    run(['rclone', 'lsf', args.remote, '--max-depth', '1'])
    state.mkdir(parents=True, exist_ok=True)
    stamp = time.strftime('%Y%m%dT%H%M%SZ', time.gmtime()) + '-' + uuid.uuid4().hex[:8]
    destination = args.remote.rstrip('/') + '/' + source.parent.name + '-' + stamp + '.tar.zst'
    manifest = {'source': str(source), 'archive': destination, 'children': selected,
                'skipped': skipped, 'status': 'uploading', 'created_unix': time.time()}
    manifest_path = state / (stamp + '.json')
    def save():
        manifest_path.write_text(json.dumps(manifest, indent=2) + '\n')
    save()
    with tempfile.TemporaryFile() as names, tempfile.TemporaryFile() as errors:
        names.write(b''.join(os.fsencode(n) + b'\0' for n in selected)); names.seek(0)
        tar = subprocess.Popen(['tar', '--acls', '--xattrs', '--sparse',
                                '-C', str(source), '--null', '-T', '/dev/stdin',
                                '-I', 'zstd -T2 -3', '-cf', '-'], stdin=names,
                               stdout=subprocess.PIPE, stderr=errors)
        upload = subprocess.Popen(['rclone', 'rcat', destination], stdin=subprocess.PIPE)
        digest = hashlib.sha256()
        try:
            while chunk := tar.stdout.read(1024 * 1024):
                digest.update(chunk)
                upload.stdin.write(chunk)
            upload.stdin.close()
            tar_code, upload_code = tar.wait(), upload.wait()
            if tar_code or upload_code:
                raise RuntimeError('Archive or upload failed; source retained')
        finally:
            if tar.poll() is None: tar.terminate(); tar.wait()
            if upload.poll() is None: upload.terminate(); upload.wait()
        manifest['sha256'] = digest.hexdigest(); manifest['status'] = 'uploaded'; save()
    download = subprocess.Popen(['rclone', 'cat', destination], stdout=subprocess.PIPE)
    verified = hashlib.sha256()
    while chunk := download.stdout.read(1024 * 1024):
        verified.update(chunk)
    if download.wait() or verified.hexdigest() != manifest['sha256']:
        raise RuntimeError('Remote archive verification failed; source retained')
    manifest['status'] = 'verified'; save()
    run(['rclone', 'copyto', str(manifest_path), destination + '.manifest.json'])
    busy = active_paths()
    removed, retained = [], []
    for name in selected:
        child = source / name
        if str(source) in busy or in_use(child, busy) or snapshot(child) != before[name]:
            retained.append(name)
            continue
        try:
            remove_verified_tree(child)
        except OSError as error:
            manifest.update(status='cleanup_failed', failed_child=name,
                            error=type(error).__name__, removed=removed, retained=retained)
            save()
            raise RuntimeError('Verified backup retained; local removal failed; see manifest') from error
        removed.append(name)
        manifest.update(status='cleaning', removed=removed, retained=retained); save()
    manifest.update(status='complete', removed=removed, retained=retained); save()
    run(['rclone', 'copyto', str(manifest_path), destination + '.manifest.json'])
    print(json.dumps({'status': 'complete', 'removed_directories': len(removed),
                      'retained_changed_or_active': len(retained)}), flush=True)


def stat_is_dir(mode):
    return stat.S_ISDIR(mode)


if __name__ == '__main__':
    try:
        main()
    except (RuntimeError, OSError, subprocess.CalledProcessError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
