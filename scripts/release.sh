#!/usr/bin/env bash
# Tag the current gradle version and push it. GitHub Actions attaches the APK
# to a Release; older tags stay in the archive.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
version="$(grep '^app.versionName=' gradle.properties | cut -d= -f2 | tr -d '[:space:]')"
code="$(grep '^app.versionCode=' gradle.properties | cut -d= -f2 | tr -d '[:space:]')"
tag="v${version}"
if git rev-parse "$tag" >/dev/null 2>&1; then
  echo "Tag $tag already exists." >&2
  exit 1
fi
git tag -a "$tag" -m "Learn Languages Bitch ${version} (versionCode ${code})"
git push origin "$tag"
echo "Pushed $tag. Latest: https://github.com/ATOMCK542/LearnLanguagesBitch/releases/latest"
echo "Archive: https://github.com/ATOMCK542/LearnLanguagesBitch/releases"
