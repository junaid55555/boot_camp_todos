#!/usr/bin/env bash
# Downloads the pinned ktlint CLI used by the Claude Code formatting hook (.claude/hooks/ktlint-check.sh).
# The binary is ~70 MB, so it's gitignored rather than committed.
set -euo pipefail

VERSION=1.8.0
SHA256=a3fd620207d5c40da6ca789b95e7f823c54e854b7fade7f613e91096a3706d75

dest="$(cd "$(dirname "$0")" && pwd)/ktlint"
curl -sSL --fail -o "$dest" "https://github.com/ktlint/ktlint/releases/download/$VERSION/ktlint"
echo "$SHA256  $dest" | sha256sum --check --quiet
chmod +x "$dest"
echo "ktlint $VERSION installed at $dest"
