#!/usr/bin/env python3
"""Restore a verified Music archive and its exact-content Downloads references.

GPL-3.0-or-later. Download and join archive chunks using the recovery manifests
first. Requires Python 3.12+, GNU tar, and zstd. Destination must be empty.
File ctime/device/inode values are provenance; restored files get new identities.
"""
import argparse
import base64
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import shutil
import stat
import subprocess
import sys
import tarfile
import tempfile


def digest(path):
    with path.open('rb') as handle:
        return hashlib.file_digest(handle, 'sha256').hexdigest()


def relative(name):
    path = PurePosixPath(name)
    if path.is_absolute() or '..' in path.parts or not path.parts:
        raise ValueError('Unsafe archive or manifest member path')
    return path.as_posix()


def validate_archive(archive, allowed):
    """Inspect headers before GNU tar receives any permission to extract."""
    seen = set()
    with tempfile.TemporaryFile() as errors:
        process = subprocess.Popen(['zstd', '-dc', str(archive)],
                                   stdout=subprocess.PIPE, stderr=errors)
        try:
            with tarfile.open(fileobj=process.stdout, mode='r|') as stream:
                for member in stream:
                    name = relative(member.name)
                    if member.isdir():
                        if not any(p.startswith(name.rstrip('/') + '/') for p in allowed):
                            raise ValueError('Unexpected directory in media archive')
                        continue
                    if name not in allowed or not (member.isfile() or member.islnk()):
                        raise ValueError('Unexpected file type or member in media archive')
                    if member.islnk() and relative(member.linkname) not in allowed:
                        raise ValueError('Unexpected hard-link target in media archive')
                    seen.add(name)
            while process.stdout.read(1024 * 1024):
                pass
            if process.wait() != 0:
                raise RuntimeError('Compressed archive integrity test failed')
        finally:
            if process.poll() is None:
                process.terminate()
                process.wait()
    if seen != allowed:
        raise ValueError('Archive member selection does not match its manifest')


def extract(archive, destination):
    subprocess.run(['tar', '--same-owner', '--same-permissions', '--acls',
                    '--xattrs', '--xattrs-include=*', '-I', 'zstd', '-xf',
                    str(archive), '-C', str(destination)], check=True)


def attributes(path):
    return {name: os.getxattr(path, name, follow_symlinks=False)
            for name in os.listxattr(path, follow_symlinks=False)}


def restore_metadata(path, metadata):
    current = path.stat()
    if (current.st_uid, current.st_gid) != (metadata['uid'], metadata['gid']):
        os.chown(path, metadata['uid'], metadata['gid'], follow_symlinks=False)
    os.chmod(path, metadata['mode'] | stat.S_IRUSR | stat.S_IWUSR, follow_symlinks=False)
    expected = {name: base64.b64decode(value, validate=True)
                for name, value in metadata['xattrs'].items()}
    for name in attributes(path).keys() - expected.keys():
        os.removexattr(path, name, follow_symlinks=False)
    for name, value in sorted(expected.items(), key=lambda pair: pair[0] == 'system.posix_acl_access'):
        os.setxattr(path, name, value, follow_symlinks=False)
    os.chmod(path, metadata['mode'], follow_symlinks=False)
    os.utime(path, ns=(metadata['atime_ns'], metadata['mtime_ns']),
             follow_symlinks=False)
    current = path.stat()
    if ((stat.S_IMODE(current.st_mode), current.st_uid, current.st_gid,
         current.st_mtime_ns, attributes(path)) !=
        (metadata['mode'], metadata['uid'], metadata['gid'],
         metadata['mtime_ns'], expected)):
        raise RuntimeError('Restored Music file metadata did not match')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--music-manifest', type=Path, required=True)
    parser.add_argument('--music-archive', type=Path, required=True)
    parser.add_argument('--downloads-manifest', type=Path, required=True)
    parser.add_argument('--downloads-archive', type=Path, required=True)
    parser.add_argument('--destination', type=Path, required=True)
    parser.add_argument('--expected-music-manifest-sha256')
    parser.add_argument('--alias-sidecar', type=Path)
    parser.add_argument('--restore-recorded-aliases', action='store_true')
    parser.add_argument('--directory-sidecar', type=Path)
    parser.add_argument('--restore-recorded-directories', action='store_true')
    args = parser.parse_args()
    if (args.expected_music_manifest_sha256 and
        digest(args.music_manifest) != args.expected_music_manifest_sha256):
        raise ValueError('Music manifest SHA256 did not match its recovery receipt')
    music = json.loads(args.music_manifest.read_text())
    dependency = music['referenced_downloads_archive']
    if music['status'] != 'verified' or not dependency['manifest']['verified']:
        raise ValueError('Verified archive manifests are required')
    if digest(args.downloads_manifest) != dependency['manifest']['sha256']:
        raise ValueError('Referenced Downloads manifest SHA256 did not match')
    downloads = json.loads(args.downloads_manifest.read_text())
    if downloads['status'] != 'verified':
        raise ValueError('Downloads backup is incomplete')
    if digest(args.music_archive) != music['sha256']:
        raise ValueError('Music compressed archive SHA256 did not match')
    if (digest(args.downloads_archive) != downloads['sha256'] or
        downloads['sha256'] != dependency['compressed_sha256']):
        raise ValueError('Downloads compressed archive SHA256 did not match')
    independent = {relative(name) for name in music['archive_selected']}
    duplicates = {relative(name): value
                  for name, value in music['duplicate_members'].items()}
    selected = {relative(name) for name in music['selected']}
    if independent & duplicates.keys() or independent | duplicates.keys() != selected:
        raise ValueError('Music source selection does not match recovery mapping')
    for name, record in duplicates.items():
        internal = 'music_member' in record
        member = relative(record['music_member'] if internal else record['downloads_member'])
        hashes = music.get('source_content_sha256', {}) if internal else downloads['source_content_sha256']
        if internal and member not in independent:
            raise ValueError('Music reference must target an archived canonical member')
        if hashes.get(member) != {
                'sha256': record['sha256'], 'size': record['size']}:
            raise ValueError('Music reference does not match verified Downloads member')
    validate_archive(args.music_archive, independent)
    validate_archive(args.downloads_archive,
                     {relative(name) for name in downloads['selected']})
    if args.destination.is_symlink():
        raise ValueError('Recovery destination must be a real directory')
    destination = args.destination.resolve()
    if destination.is_symlink() or (destination.exists() and any(destination.iterdir())):
        raise ValueError('Recovery destination must be an empty real directory')
    destination.mkdir(parents=True, exist_ok=True)
    extract(args.music_archive, destination)
    with tempfile.TemporaryDirectory(prefix='downloads-restore-',
                                     dir=destination.parent) as temporary:
        staged = Path(temporary)
        extract(args.downloads_archive, staged)
        groups = {}
        for name, record in duplicates.items():
            original = (destination / relative(record['music_member']) if 'music_member' in record
                        else staged / relative(record['downloads_member']))
            if original.stat().st_size != record['size'] or digest(original) != record['sha256']:
                raise ValueError('Restored Downloads member content did not match')
            target = destination / name
            target.parent.mkdir(parents=True, exist_ok=True)
            if target.exists() or target.is_symlink():
                raise ValueError('Duplicate recovery would overwrite an existing Music file')
            metadata = record['metadata']
            group = (metadata['device'], metadata['inode'])
            if group in groups:
                os.link(groups[group], target)
            else:
                shutil.copyfile(original, target, follow_symlinks=False)
                groups[group] = target
            if target.stat().st_size != record['size'] or digest(target) != record['sha256']:
                raise ValueError('Reconstructed Music file content did not match')
            restore_metadata(target, metadata)
    if args.restore_recorded_aliases:
        proof = music.get('alias_metadata_companion', {})
        if not args.alias_sidecar or not proof.get('verified') or digest(args.alias_sidecar) != proof['sha256']:
            raise ValueError('Pinned alias companion is required for optional alias restoration')
        records = json.loads(args.alias_sidecar.read_text())['source_aliases'][music['source']]
        for name, metadata in records.items():
            target = destination / relative(name)
            parent = target.parent
            while parent != destination:
                if parent.is_symlink():
                    raise ValueError('Alias parent must not be a symlink')
                parent = parent.parent
            target.parent.mkdir(parents=True, exist_ok=True)
            if target.exists() or target.is_symlink():
                raise ValueError('Alias restoration would overwrite a path')
            if metadata.get('metadata_unreadable'):
                raise ValueError('Alias metadata was not fully readable')
            os.symlink(metadata['target'], target)
            os.chown(target, metadata['uid'], metadata['gid'], follow_symlinks=False)
            for key, value in metadata['xattrs'].items():
                os.setxattr(target, key, base64.b64decode(value, validate=True), follow_symlinks=False)
            os.utime(target, ns=(metadata['atime_ns'], metadata['mtime_ns']), follow_symlinks=False)
    for name in selected:
        if not (destination / name).is_file():
            raise ValueError('A selected Music file was not reconstructed')
    if args.restore_recorded_directories:
        proof = music.get('directory_metadata_companion', {})
        if not args.directory_sidecar or not proof.get('verified') or digest(args.directory_sidecar) != proof['sha256']:
            raise ValueError('Pinned directory companion is required for optional scaffold restoration')
        records = json.loads(args.directory_sidecar.read_text())['source_directories'][music['source']]
        for name in sorted(records, key=lambda x: len(Path(x).parts)):
            target = destination if name == '.' else destination / relative(name)
            parent = target
            while parent != destination:
                if parent.is_symlink():
                    raise ValueError('Scaffold path must not contain a symlink')
                parent = parent.parent
            target.mkdir(parents=True, exist_ok=True)
        for name in sorted(records, key=lambda x: len(Path(x).parts), reverse=True):
            metadata = records[name]
            if metadata.get('metadata_unreadable'):
                raise ValueError('Directory metadata was not fully readable')
            restore_metadata(destination if name == '.' else destination / relative(name), metadata)
    print(json.dumps({'restored_music_files': len(selected),
                      'reconstructed_exact_duplicates': len(duplicates),
                      'status': 'complete'}))


if __name__ == '__main__':
    try:
        main()
    except (OSError, ValueError, RuntimeError, subprocess.CalledProcessError,
            tarfile.TarError) as error:
        print(str(error), file=sys.stderr)
        sys.exit(1)
