#!/usr/bin/env bash
# Quick launcher for η TUI agent
set -euo pipefail
cd "$(dirname "$0")"

# Ensure PROXX vars are set
: "${PROXX_URL:?Set PROXX_URL to your provider endpoint}"
: "${PROXX_AUTH_TOKEN:?Set PROXX_AUTH_TOKEN}"

exec clojure -M:run "$@"
