#!/usr/bin/env bash
# PostToolUse hook (Edit|Write): ktlint check of the Kotlin file that was just changed.
# Check only, never rewrites the file. On violations it exits 2 so Claude sees them and fixes the code.
# Rules come from .editorconfig. One-time setup: tools/get-ktlint.sh

input=$(cat)

# Pull tool_input.file_path out of the hook JSON without jq. Quotes inside string values are escaped (\"),
# so this can't match text inside the edited content.
file=$(printf '%s' "$input" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\(\([^"\\]\|\\.\)*\)".*/\1/p' | head -n 1)
file=${file//\\\\/\\}

case "$file" in
  *.kt | *.kts) ;;
  # No Java formatter is approved yet, and the app has no .java files.
  *) exit 0 ;;
esac
case "$file" in
  */build/* | *\\build\\*) exit 0 ;;
esac

project_dir=${CLAUDE_PROJECT_DIR:-$(pwd)}
ktlint="$project_dir/tools/ktlint"
if [ ! -f "$ktlint" ]; then
  echo '{"systemMessage": "ktlint check skipped: run tools/get-ktlint.sh once to download it."}'
  exit 0
fi

java_bin=java
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then java_bin="$JAVA_HOME/bin/java"; fi

cd "$project_dir" || exit 0
output=$("$java_bin" --enable-native-access=ALL-UNNAMED -jar "$ktlint" --relative "$file" 2>/dev/null)
status=$?

if [ $status -ne 0 ] && [ -n "$output" ]; then
  {
    echo "ktlint found style violations in $file (rules: .editorconfig, docs/CODE_STYLE.md). Fix them:"
    echo "$output"
  } >&2
  exit 2
fi
exit 0
