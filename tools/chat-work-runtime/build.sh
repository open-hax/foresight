#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-or-later
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_dir="$(cd -- "$script_dir/../.." && pwd)"
# shellcheck source=versions.env
source "$script_dir/versions.env"

package_version() {
  local package_name="$1"
  awk -v key="\"$package_name\":" '
    $1 == key {
      version = $2
      gsub(/[\",]/, "", version)
      print version
      exit
    }
  ' "$script_dir/package.json"
}

locked_package_version() {
  local package_name="$1"
  awk -v key="\"node_modules/$package_name\":" '
    $1 == key { in_package = 1; next }
    in_package && $1 == "\"version\":" {
      version = $2
      gsub(/[\",]/, "", version)
      print version
      exit
    }
  ' "$script_dir/package-lock.json"
}

nbb_version="$(package_version nbb)"
jscpd_version="$(package_version jscpd)"
import_meta_resolve_version="$(locked_package_version import-meta-resolve)"
if [[ -z "$nbb_version" || -z "$jscpd_version" ||
      -z "$import_meta_resolve_version" ]]; then
  echo "cannot derive third-party versions from the committed npm manifests" >&2
  exit 2
fi

require_complete_license() {
  local license_file="$1"
  local license_name="$2"
  shift 2
  local marker
  for marker in "$@"; do
    if ! grep -Fq -- "$marker" "$license_file"; then
      echo "incomplete $license_name text: $license_file" >&2
      exit 2
    fi
  done
}

require_complete_license "$repo_dir/LICENSE" "LGPL-3.0" \
  "GNU LESSER GENERAL PUBLIC LICENSE" \
  "6. Revised Versions of the GNU Lesser General Public License."
require_complete_license "$script_dir/LICENSE" "GPL-3.0" \
  "GNU GENERAL PUBLIC LICENSE" \
  "END OF TERMS AND CONDITIONS"
require_complete_license "$script_dir/licenses/EPL-1.0.txt" "EPL-1.0" \
  "Eclipse Public License - v 1.0" \
  "any resulting litigation."
require_complete_license "$script_dir/licenses/jscpd-MIT.txt" "jscpd MIT" \
  "Copyright (c) 2013-2024 Andrey Kucherenko" \
  "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE" \
  "SOFTWARE."
require_complete_license "$script_dir/licenses/rewrite-clj-MIT.txt" "rewrite-clj MIT" \
  "Copyright (c) 2013-2018 Yannick Scherer" \
  "OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE" \
  "SOFTWARE."
for notice_marker in \
  "Node.js $NODE_VERSION" \
  "NBB $nbb_version" \
  "Babashka $BABASHKA_VERSION" \
  "clj-kondo $CLJ_KONDO_VERSION" \
  "jscpd $jscpd_version" \
  "import-meta-resolve $import_meta_resolve_version"; do
  if ! grep -Fq -- "$notice_marker" "$script_dir/THIRD_PARTY_NOTICES.md"; then
    echo "incomplete third-party notices: missing $notice_marker" >&2
    exit 2
  fi
done

arch="${ARCH:-x64}"
output_dir="${OUTPUT_DIR:-$repo_dir/dist}"
case "$arch" in
  x64)
    node_arch="x64"
    native_arch="amd64"
    ;;
  arm64)
    node_arch="arm64"
    native_arch="aarch64"
    ;;
  *)
    echo "unsupported ARCH: $arch (expected x64 or arm64)" >&2
    exit 2
    ;;
esac

bundle_name="foresight-chat-work-linux-$arch"
work_dir="$(mktemp -d)"
download_dir="$work_dir/downloads"
bundle_dir="$work_dir/$bundle_name"
trap 'rm -rf -- "$work_dir"' EXIT
mkdir -p "$download_dir" "$bundle_dir/bin" "$bundle_dir/lib" "$bundle_dir/share/licenses" "$output_dir"

download() {
  local url="$1"
  local destination="$2"
  curl --fail --location --retry 3 --silent --show-error "$url" --output "$destination"
}

verify_remote_sha256() {
  local archive="$1"
  local checksum_url="$2"
  local checksum_file="$archive.sha256"
  download "$checksum_url" "$checksum_file"
  local expected
  expected="$(awk '{print $1; exit}' "$checksum_file")"
  printf '%s  %s\n' "$expected" "$(basename -- "$archive")" | (cd "$(dirname -- "$archive")" && sha256sum --check --status -)
}

node_archive="$download_dir/node-v$NODE_VERSION-linux-$node_arch.tar.xz"
node_url="https://nodejs.org/dist/v$NODE_VERSION/$(basename -- "$node_archive")"
download "$node_url" "$node_archive"
download "https://nodejs.org/dist/v$NODE_VERSION/SHASUMS256.txt" "$download_dir/node-shasums.txt"
grep "  $(basename -- "$node_archive")$" "$download_dir/node-shasums.txt" |
  (cd "$download_dir" && sha256sum --check --status -)
mkdir -p "$bundle_dir/lib/node"
tar --no-same-owner -xJf "$node_archive" --strip-components=1 -C "$bundle_dir/lib/node"
npm_version="$(awk '
  $1 == "\"version\":" {
    version = $2
    gsub(/[\",]/, "", version)
    print version
    exit
  }
' "$bundle_dir/lib/node/lib/node_modules/npm/package.json")"
if [[ -z "$npm_version" ]] ||
   ! grep -Fq -- "npm $npm_version" "$script_dir/THIRD_PARTY_NOTICES.md"; then
  echo "incomplete third-party notices: missing npm $npm_version" >&2
  exit 2
fi

# npm's CLI is JavaScript, but it still needs a Node executable matching the
# build host. During a cross-build the staged target Node cannot execute here,
# so download the same publisher-verified Node release for the host and use it
# only to materialize the locked target dependency graph.
case "$(uname -m)" in
  x86_64) host_node_arch="x64" ;;
  aarch64 | arm64) host_node_arch="arm64" ;;
  *) echo "unsupported build-host architecture: $(uname -m)" >&2; exit 2 ;;
esac
npm_node="$bundle_dir/lib/node/bin/node"
if [[ "$host_node_arch" != "$node_arch" ]]; then
  host_node_archive="$download_dir/node-v$NODE_VERSION-linux-$host_node_arch.tar.xz"
  download "https://nodejs.org/dist/v$NODE_VERSION/$(basename -- "$host_node_archive")" \
    "$host_node_archive"
  grep "  $(basename -- "$host_node_archive")$" "$download_dir/node-shasums.txt" |
    (cd "$download_dir" && sha256sum --check --status -)
  host_node_dir="$work_dir/host-node"
  mkdir -p "$host_node_dir"
  tar --no-same-owner -xJf "$host_node_archive" --strip-components=1 -C "$host_node_dir"
  npm_node="$host_node_dir/bin/node"
fi

bb_archive="$download_dir/babashka-$BABASHKA_VERSION-linux-$native_arch-static.tar.gz"
bb_url="https://github.com/babashka/babashka/releases/download/v$BABASHKA_VERSION/$(basename -- "$bb_archive")"
download "$bb_url" "$bb_archive"
verify_remote_sha256 "$bb_archive" "$bb_url.sha256"
tar --no-same-owner -xzf "$bb_archive" -C "$bundle_dir/bin" bb

kondo_archive="$download_dir/clj-kondo-$CLJ_KONDO_VERSION-linux-static-$native_arch.zip"
if [[ "$native_arch" == "aarch64" ]]; then
  kondo_archive="$download_dir/clj-kondo-$CLJ_KONDO_VERSION-linux-$native_arch.zip"
fi
kondo_url="https://github.com/clj-kondo/clj-kondo/releases/download/v$CLJ_KONDO_VERSION/$(basename -- "$kondo_archive")"
download "$kondo_url" "$kondo_archive"
verify_remote_sha256 "$kondo_archive" "$kondo_url.sha256"
unzip -q "$kondo_archive" -d "$bundle_dir/bin"

tools_prefix="$bundle_dir/lib/chat-work-tools"
mkdir -p "$tools_prefix"
cp "$script_dir/package.json" "$script_dir/package-lock.json" "$tools_prefix/"
"$npm_node" \
  "$bundle_dir/lib/node/lib/node_modules/npm/bin/npm-cli.js" \
  ci --prefix "$tools_prefix" --omit=dev --ignore-scripts --no-audit --no-fund \
  --cpu="$node_arch" --os=linux --libc=glibc
if [[ ! -d "$tools_prefix/node_modules/jscpd-linux-$node_arch-gnu" ]]; then
  echo "locked jscpd native package does not match target: linux-$node_arch-gnu" >&2
  exit 1
fi

for command in node npm npx nbb jscpd; do
  case "$command" in
    node) command_script="" ;;
    npm) command_script="../lib/node/lib/node_modules/npm/bin/npm-cli.js" ;;
    npx) command_script="../lib/node/lib/node_modules/npm/bin/npx-cli.js" ;;
    nbb) command_script="../lib/chat-work-tools/node_modules/nbb/cli.js" ;;
    jscpd) command_script="../lib/chat-work-tools/node_modules/jscpd/run-jscpd.js" ;;
  esac
  cat > "$bundle_dir/bin/$command" <<EOF
#!/bin/sh
# Generated by tools/chat-work-runtime/build.sh.
script_path=\$0
case "\$script_path" in
  */*) ;;
  *) echo "cannot resolve bundle path from: \$script_path" >&2; exit 127 ;;
esac
runtime_bin=\$(CDPATH= cd -- "\${script_path%/*}" && pwd -P)
EOF
  if [[ -n "$command_script" ]]; then
    cat >> "$bundle_dir/bin/$command" <<EOF
exec "\$runtime_bin/../lib/node/bin/node" "\$runtime_bin/$command_script" "\$@"
EOF
  else
    cat >> "$bundle_dir/bin/$command" <<'EOF'
exec "$runtime_bin/../lib/node/bin/node" "$@"
EOF
  fi
  chmod 0755 "$bundle_dir/bin/$command"
done

cp "$repo_dir/LICENSE" "$bundle_dir/share/licenses/foresight-LGPL-3.0-or-later.txt"
cp "$script_dir/LICENSE" "$bundle_dir/share/licenses/foresight-chat-work-runtime-GPL-3.0-or-later.txt"
cp "$script_dir/THIRD_PARTY_NOTICES.md" "$bundle_dir/share/licenses/THIRD_PARTY_NOTICES.md"
cp "$script_dir/licenses/EPL-1.0.txt" "$bundle_dir/share/licenses/EPL-1.0.txt"
cp "$script_dir/licenses/jscpd-MIT.txt" "$bundle_dir/share/licenses/jscpd-MIT.txt"
cp "$script_dir/licenses/rewrite-clj-MIT.txt" "$bundle_dir/share/licenses/rewrite-clj-MIT.txt"
cp "$script_dir/README.bundle.md" "$bundle_dir/README.md"
cp "$script_dir/versions.env" "$bundle_dir/versions.env"

# Regular-file hashes cannot represent a symbolic link's target. Record every
# link explicitly, then make the verifier check both representations. The
# staged dependency paths are controlled by pinned archives/packages and do
# not contain tabs or newlines, so a tab-separated manifest is unambiguous.
(
  cd "$bundle_dir"
  while IFS= read -r -d '' link; do
    target="$(readlink -- "$link")"
    if [[ "$link" == *$'\t'* || "$link" == *$'\n'* ||
          "$target" == *$'\t'* || "$target" == *$'\n'* ]]; then
      echo "unsupported tab or newline in symbolic-link manifest: $link" >&2
      exit 1
    fi
    printf '%s\t%s\n' "$link" "$target"
  done < <(find . -type l -print0 | sort -z)
) > "$bundle_dir/SYMLINKS.tsv"

cat > "$bundle_dir/bin/verify-integrity" <<'EOF'
#!/bin/sh
# SPDX-License-Identifier: GPL-3.0-or-later
set -eu

script_path=$0
case "$script_path" in
  */*) ;;
  *) echo "cannot resolve bundle path from: $script_path" >&2; exit 127 ;;
esac
bundle_root=$(CDPATH= cd -- "${script_path%/*}/.." && pwd -P)
cd "$bundle_root"

tree_snapshot=$(mktemp "/tmp/foresight-runtime-tree.XXXXXX")
trap 'rm -f -- "$tree_snapshot"' EXIT
trap 'exit 130' HUP INT TERM
for bootstrap_manifest in TREE.types0 SHA256SUMS; do
  if [ ! -f "$bootstrap_manifest" ] || [ -L "$bootstrap_manifest" ]; then
    echo "bootstrap manifest must be a regular file: $bootstrap_manifest" >&2
    exit 1
  fi
done
find . -mindepth 1 \
  ! -path './TREE.types0' \
  ! -path './SHA256SUMS' \
  -printf '%y\t%p\0' | LC_ALL=C sort -z > "$tree_snapshot"
if ! cmp -s TREE.types0 "$tree_snapshot"; then
  echo "payload entry set or type mismatch" >&2
  exit 1
fi

tab=$(printf '\t')
while IFS="$tab" read -r link expected_target; do
  [ -n "$link" ] || continue
  case "$link" in
    ./*) ;;
    *) echo "invalid symbolic-link path in SYMLINKS.tsv: $link" >&2; exit 1 ;;
  esac
  if [ ! -L "$link" ]; then
    echo "missing symbolic link: $link" >&2
    exit 1
  fi
  actual_target=$(readlink -- "$link")
  if [ "$actual_target" != "$expected_target" ]; then
    echo "symbolic-link target mismatch: $link" >&2
    echo "expected: $expected_target" >&2
    echo "actual:   $actual_target" >&2
    exit 1
  fi
done < SYMLINKS.tsv
while IFS="$tab" read -r expected_mode executable; do
  [ -n "$expected_mode" ] || continue
  case "$expected_mode" in
    [0-7][0-7][0-7] | [0-7][0-7][0-7][0-7]) ;;
    *) echo "invalid mode in EXECUTABLES.tsv: $expected_mode" >&2; exit 1 ;;
  esac
  case "$executable" in
    ./*) ;;
    *) echo "invalid executable path in EXECUTABLES.tsv: $executable" >&2; exit 1 ;;
  esac
  if [ ! -f "$executable" ] || [ -L "$executable" ]; then
    echo "missing regular executable: $executable" >&2
    exit 1
  fi
  actual_mode=$(stat -c '%a' -- "$executable")
  if [ "$actual_mode" != "$expected_mode" ]; then
    echo "executable mode mismatch: $executable" >&2
    echo "expected: $expected_mode" >&2
    echo "actual:   $actual_mode" >&2
    exit 1
  fi
done < EXECUTABLES.tsv
sha256sum --check --quiet SHA256SUMS
echo "integrity verification passed: regular files, symbolic links, and executable modes"
EOF
chmod 0755 "$bundle_dir/bin/verify-integrity"

# Record the exact mode of every executable regular file. The staged paths are
# controlled by pinned archives/packages and do not contain tabs or newlines.
(
  cd "$bundle_dir"
  while IFS= read -r -d '' executable; do
    if [[ "$executable" == *$'\t'* || "$executable" == *$'\n'* ]]; then
      echo "unsupported tab or newline in executable manifest: $executable" >&2
      exit 1
    fi
    printf '%s\t%s\n' "$(stat -c '%a' -- "$executable")" "$executable"
  done < <(find . -type f -perm /111 -print0 | sort -z)
) > "$bundle_dir/EXECUTABLES.tsv"

# Record every payload entry and its filesystem type with NUL delimiters. This
# rejects added entries and regular-file/symbolic-link substitution. TREE.types0
# and SHA256SUMS are excluded to avoid self-reference; both manifests are still
# covered by the adjacent archive digest, and TREE.types0 is covered by SHA256SUMS.
(
  cd "$bundle_dir"
  find . -mindepth 1 \
    ! -path './TREE.types0' \
    ! -path './SHA256SUMS' \
    -printf '%y\t%p\0' | LC_ALL=C sort -z
) > "$bundle_dir/TREE.types0"

(
  cd "$bundle_dir"
  find . -type f ! -name SHA256SUMS -print0 | sort -z | xargs -0 sha256sum > SHA256SUMS
)

archive_path="$output_dir/$bundle_name.tar.gz"
LC_ALL=C tar --sort=name --mtime='@0' --owner=0 --group=0 --numeric-owner \
  -C "$work_dir" -cf - "$bundle_name" | gzip -n > "$archive_path"
(cd "$output_dir" && sha256sum "$bundle_name.tar.gz" > "$bundle_name.tar.gz.sha256")
printf '%s\n' "$archive_path"
