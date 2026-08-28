#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
repo_dir="$(cd -- "$script_dir/../.." && pwd)"
# shellcheck source=versions.env
source "$script_dir/versions.env"

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

"$bundle_dir/lib/node/bin/npm" install --global --prefix "$bundle_dir/lib/node" \
  --omit=dev --ignore-scripts --no-audit --no-fund \
  "nbb@$NBB_VERSION" "jscpd@$JSCPD_VERSION"

for command in node npm npx nbb jscpd; do
  ln -s "../lib/node/bin/$command" "$bundle_dir/bin/$command"
done

cp "$repo_dir/LICENSE" "$bundle_dir/share/licenses/foresight-GPL-3.0-or-later.txt"
cp "$script_dir/README.bundle.md" "$bundle_dir/README.md"
cp "$script_dir/versions.env" "$bundle_dir/versions.env"
(
  cd "$bundle_dir"
  find . -type f ! -name SHA256SUMS -print0 | sort -z | xargs -0 sha256sum > SHA256SUMS
)

archive_path="$output_dir/$bundle_name.tar.gz"
tar --owner=0 --group=0 -C "$work_dir" -czf "$archive_path" "$bundle_name"
sha256sum "$archive_path" > "$archive_path.sha256"
printf '%s\n' "$archive_path"
