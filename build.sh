#!/usr/bin/env bash
set -euo pipefail

# 1. Run Gradle and capture EVERYTHING
echo "Running: ./gradlew $*"
./gradlew "$@" > /tmp/gradle_output.txt 2>&1 || true

# 2. Extract summary lines under "=== BUILD ERRORS ==="
awk '
  /^=== BUILD ERRORS ===/ { inErrors=1; next }
  /^===/ && inErrors       { inErrors=0 }
  inErrors && /^Line [0-9]+:/ { print }
' /tmp/gradle_output.txt > /tmp/build_result.txt

# 3. Prepare Markdown report
report=/tmp/build_error_context.md
{
  echo "# Build Error Context"
  echo
  if [[ ! -s /tmp/build_result.txt ]]; then
    echo "_No errors found in build_result.txt_"
    exit 0
  fi

  while IFS= read -r line; do
    # parse line number and message
    num=$(echo "$line" | sed -E 's/^Line ([0-9]+):.*$/\1/')
    msg=$(echo "$line" | sed -E 's/^Line [0-9]+: (.*)$/\1/')

    # compute context window
    start=$(( num - 5 ))
    (( start < 1 )) && start=1
    end=$(( num + 5 ))

    echo "## Error: $msg  (from build_result.txt at line $num)"
    echo '```text'
    sed -n "${start},${end}p" /tmp/gradle_output.txt
    echo '```'
    echo

  done < /tmp/build_result.txt

} > "$report"

echo "Summary of errors written to /tmp/build_result.txt"
echo "Detailed context report written to $report"
