# Agent scratch cleanup

License: GPL-3.0-or-later.

User preservation rule: remove a local artifact only after its exact contents
are verified recoverable remotely. For uncertain artifacts, use rclone Google
Drive backup. A clean Git status or a configured remote alone is insufficient.
Ignored files, untracked evidence, and unpushed commits require preservation.

## Current scope

`scripts/archive-scratch.py` accepts an explicit scratch parent. It defaults to
reporting eligible child directories without uploading or deleting anything.
Use it on scratch artifacts, not a product repository or the spaces root.
Python's standard library handles streaming hashes and filesystem inspection;
GNU tar, zstd and rclone provide archive and transport operations.

Example dry run from the Foresight checkout:

```sh
python3 scripts/archive-scratch.py /home/err/spaces/review-repair/.ημ \
  --state-dir /home/err/spaces/foresight/.ημ/scratch-cleanup
```

Add `--apply` to upload, verify, and remove eligible child directories. The
default remote folder is `gdrive:AgentWorkspaceArchives`. A unique archive name
prevents overwriting earlier archives. No large local staging archive is made.
Authentication failure leaves sources intact.

The cleaner skips recent directories (seven days by default), child repositories
with their own Git boundary, registered worktree Git files, symlinks at the
selection boundary, and directories used by processes. When process permissions
prevent inspection, it attempts a noninteractive sudo helper that reads only
process working-directory and file-descriptor link paths, not file contents,
credentials, or environments. It refuses cleanup if inspection still fails.
Individual files directly
under the scratch parent remain in place. Files inside selected directories,
including evidence and hidden files, are preserved in the archive.

Every archive is read back from Drive and checked against the SHA-256 computed
during upload. Its source selection and hash are recorded in a local manifest
and a remote manifest. Only then are unchanged, still-inactive sources removed.
Changed or newly active directories remain locally. Upload or verification
failure must never be treated as permission to delete. A local lock prevents
overlapping cleaner invocations using the same state directory.

After remote verification and unchanged-source checks, the reusable cleaner can
add owner-write/execute permission to user-owned directories to remove frozen
read-only scratch copies. Original modes, ACLs, and xattrs are carried in the
archive. Failed local removal is recorded in its manifest and does not erase the
verified backup. This path was exercised with restored read-only modes and
binary xattrs.

## Continuous-operation contract

Agents must stop writing a scratch directory before it becomes eligible for
cleanup. Old timestamps and process inspection are protective checks, not a
transactional lease: an unrelated agent can otherwise reopen a directory after
inspection. Do not run unattended deletion until the owning agent workflow
implements a completion/lease boundary or the scratch tree is known quiescent.

Check free disk space before starting a task and between phases. Proposed initial
thresholds are 50 GiB for a warning and cleanup request, and 20 GiB for pausing new
scratch-producing work. These are proposed operating defaults, not installed
limits. A failed backup should pause disk-growing work and request attention.
Age is an eligibility check, never evidence of a remote backup.

An unattended scheduler is intentionally not installed yet. First restore Drive
authorization, validate an actual backup/restore, and integrate the ownership
boundary. Then schedule bounded cleanup passes and log verified bytes reclaimed,
protected directories, backup failures, and remaining free space. Never silently
expand cleanup scope when a pass cannot recover enough room.

## Recovery

Use the manifest to locate the archive, download it with rclone, verify its
SHA-256, and extract into an empty recovery directory. Inspect restored content
before using it. Archives preserve symlinks as links; they do not copy external
targets. Registered Git worktrees are excluded because their external Git
metadata requires repository-aware handling.

The backup manifest does not prove that a Git branch was pushed. This tool uses
the authorized Drive fallback instead of guessing that Git state is sufficient.


## Verified Google Drive connector fallback

The 2026-10-17 recovery uses the connected Google Drive plugin because rclone's
separate Google authorization remains expired. The connector can upload an
absolute local path directly. Its file-size limit is 100 MiB, so transfer chunks
are 64 MiB. The archive producer is bounded by a 2 GiB staging queue and an 8 GiB
minimum free-space reserve; it pauses when uploads cannot keep up.

The destination is the private `AgentWorkspaceArchives` Drive folder. Every
chunk is uploaded, fetched with `download_raw_file=true` and
`include_base64=false`, then downloaded through the returned authenticated file
reference and hashed locally. The complete remote SHA-256 and size must match
the staged chunk. File metadata size alone is insufficient: the current
metadata adapter omits Drive's checksum field even when requested.

Verified staging copies may be discarded to bound disk usage. Original source
artifacts remain until the complete ordered chunk manifest has itself been
uploaded and fully read back with a matching SHA-256. The manifest preserves
source selection, skipped/protected paths, ordered chunk IDs and hashes, full
compressed-stream SHA-256, and recovery format. A remote sample extraction was
also compared against its source hash before audit cleanup.

To recover, download the chunks named by the manifest, verify each SHA-256,
concatenate them in manifest order, verify the whole compressed-stream SHA-256,
then test with `zstd -t` and extract the tar into an empty directory. Keep
recovery instructions and the final manifest with the chunks in Drive.

Credential, key and configuration files in Downloads and Music remain local
even after backup. Likely Google client/service-account/authorized-user JSON is
recognized without printing credential values. Known local runtime-config and
installed-command references also protect their dependencies. Open-file scans
alone cannot protect a credential file that an application reads only on startup.
The private operational inventory is excluded locally from Git; public receipts
report counts and purpose instead of credential values or identifiers.

The user subsequently authorized the same verified-backup-before-removal rule
for Downloads and Music. Their folder roots, Git boundaries, symlinks,
unfinished downloads, and files that are active or change during backup remain
protected. A manifest records file selection and remote provenance before any
original is removed.

This connector fallback is an agent-operated recovery procedure, not an
installed unattended transport. No recurring cleanup or deletion timer is
enabled. Rclone authentication and an ownership/completion lease for long-running
agents still need separate integration before unattended cleanup is safe.


## Exact-content Music references

Music may reuse bytes from an already fully verified Downloads archive only
when file SHA-256 and byte size match. Each original Music path keeps a separate
manifest entry and its own mode, uid/gid, nanosecond timestamps and binary-encoded
xattrs, including readable POSIX ACL attributes. Unreadable metadata is recorded explicitly and the original remains local,
even if an independent tar member was captured. No filenames, titles, embeddings or similarity
scores establish identity.

The Downloads manifest records content hashes only for sources whose metadata
still matches their pre-archive snapshot after tar completes. Music references
pin that verified manifest's Drive identity/hash, the exact archive member and
ordered chunk dependencies. Before removing a referenced Music original, the
cleaner rechecks its own metadata, xattrs, content SHA-256 and inactivity, and
validates the referenced verified archive/manifest again. Music's complete
manifest must itself pass remote byte readback before any Music removal.

Keep the referenced Downloads archive while Music depends on it. The uploaded
MEDIA-RECOVERY guide and [`restore-drive-media.py`](../../scripts/restore-drive-media.py)
rebuild referenced files at their original Music paths, restore their distinct
permissions/ownership/times/xattrs/ACLs and verify the result. The helper refuses
archive/manifest hash mismatches, unsafe archive paths and a nonempty recovery
destination. It does not remove downloaded archives or manifests.

The recovery fixture verifies separate Music paths and metadata, binary xattrs,
POSIX ACL, read-only mode, hardlink groups, ordinary tar xattrs, refusal of a wrong
manifest hash and refusal to overwrite a nonempty destination. Source ctime,
inode and device values remain provenance; restored files receive filesystem
identities assigned by the recovery host.

Music content is coalesced by exact SHA256 plus size both across Downloads and within Music. Each internal reference points directly to a canonical archived Music member; separate path metadata and provenance remain in the remotely verified manifest. Restoration copies content into each original Music path and restores its own permissions, uid/gid, nanosecond timestamps and xattrs/ACLs.

## Directory and alias recovery companions

Separately verified `media-aliases.json` and `media-directories.json` companions
preserve both media roots, empty directory names, symlink target strings and
readable ownership/time/xattr metadata. Targets are never followed; aliases and
directories stay local. The already published Downloads manifest is immutable;
companions have independent SHA256 receipts and are pinned by the Music manifest.
The recovery helper optionally restores pinned aliases and empty scaffolds into
a fresh destination, applying directory metadata last. Absolute link targets
may point outside that destination, so alias reconstruction is explicitly optional.
Directory timestamps captured after Downloads cleanup describe that capture point;
no earlier directory timestamps are claimed. The fixture covers an empty readonly
album directory and relative alias in addition to data and metadata restoration.
