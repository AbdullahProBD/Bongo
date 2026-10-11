#!/usr/bin/env bash
set -euo pipefail

WIKI_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WIKI_REMOTE="https://github.com/mehedishakeel/Bongo.wiki.git"

echo "============================================="
echo "  Syncing Bongo Wiki to GitHub Wiki Remote   "
echo "============================================="

TMP_DIR=$(mktemp -d /tmp/bongo-wiki-sync.XXXXXX)
trap 'rm -rf "$TMP_DIR"' EXIT

echo ">>> Initializing wiki git workspace..."
cd "$TMP_DIR"
git init
git config user.name "Mehedi Shakeel"
git config user.email "mehedishakeel@users.noreply.github.com"

# Copy markdown pages and images
cp "$WIKI_DIR"/*.md "$TMP_DIR/"
if [ -d "$WIKI_DIR/images" ]; then
    mkdir -p "$TMP_DIR/images"
    cp "$WIKI_DIR/images/"* "$TMP_DIR/images/"
fi

git add -A
git commit -m "docs(wiki): update Bongo project documentation and guides"
git branch -M master
git remote add origin "$WIKI_REMOTE"

echo ">>> Pushing wiki pages to $WIKI_REMOTE..."
if git push -u origin master --force; then
    echo ">>> Success: Bongo wiki published successfully!"
else
    echo ""
    echo ">>> Note: If GitHub returned 'repository not found', visit:"
    echo "    https://github.com/mehedishakeel/Bongo/wiki"
    echo "    and click 'Create the first page' once, then re-run this script."
fi
