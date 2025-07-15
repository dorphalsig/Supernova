#!/usr/bin/env bash
set -euo pipefail

BUILD_CMD="./gradlew $*"
LOGFILE="/tmp/gradle_output.txt"
REPORT="/tmp/build_error_context.md"

echo "Running: $BUILD_CMD"
$BUILD_CMD > $LOGFILE 2>&1 || true

# Patterns for common Android errors
ERROR_PATTERNS='(^[[:alnum:]_./\\-]+\.kt:[0-9]+:|^[[:alnum:]_./\\-]+\.java:[0-9]+:|^> Task .+ FAILED|^FAILURE:|^BUILD FAILED|^Lint found errors)'

# Find all matching lines and their line numbers
awk -v pat="$ERROR_PATTERNS" '
  {
    if ($0 ~ pat) {
      print NR":"$0
    }
  }
' $LOGFILE > /tmp/error_matches.txt

# Prepare the markdown report
{
  echo "# Build Error Context"
  echo

  if [[ ! -s /tmp/error_matches.txt ]]; then
    echo "_No errors found in build output_"
    echo "BUILD_STATUS: SUCCESS"
  else
    while IFS= read -r match; do
      # Extract error line number and message
      error_line=$(echo "$match" | cut -d: -f1)
      error_msg=$(echo "$match" | cut -d: -f2-)

      # Try to parse file path and line number
      if [[ "$error_msg" =~ ^([[:alnum:]_./\\-]+\.kt|java):([0-9]+): ]]; then
        file_path="${BASH_REMATCH[1]}"
        file_line="${BASH_REMATCH[2]}"
      else
        file_path="N/A"
        file_line="N/A"
      fi

      # Compute context window
      start=$(( error_line - 5 ))
      (( start < 1 )) && start=1
      end=$(( error_line + 5 ))

      echo "## Error"
      echo "**File:** $file_path"
      echo "**Line:** $file_line"
      echo "**Message:** \`$error_msg\`"
      echo
      echo "**Context:**"
      echo '```'
      sed -n "${start},${end}p" "$LOGFILE"
      echo '```'
      echo
    done < /tmp/error_matches.txt
    echo "BUILD_STATUS: FAILURE"
  fi
} > "$REPORT"

# Feedback log (append result, if file is writable)
if [[ -w codex_build_log.md ]]; then
  status="FAILURE"
  [[ ! -s /tmp/error_matches.txt ]] && status="SUCCESS"
  echo "$(date -Iseconds) | $BUILD_CMD | $status" >> codex_build_log.md
fi

# Print and exit for agent loop
if [[ ! -s /tmp/error_matches.txt ]]; then
  echo "BUILD_STATUS: SUCCESS"
  exit 0
else
  echo "BUILD_STATUS: FAILURE"
  exit 1
fi
