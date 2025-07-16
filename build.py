#!/usr/bin/env python3

import subprocess
import time
import re
from pathlib import Path
import sys
import datetime

BUILD_CMD = ["./gradlew"] + sys.argv[1:]
LOGFILE = Path("/tmp/gradle_output.txt")
MD_REPORT = Path("/tmp/build_error_context.md")

print(f"[AGENT] Build script started at {datetime.datetime.now().isoformat(' ', 'seconds')}")
print(f"[AGENT] Running: {' '.join(BUILD_CMD)}")
print(f"[AGENT] Gradle step updates will print only if changed.")

with LOGFILE.open("w", encoding="utf-8", errors="replace") as log:
    proc = subprocess.Popen(
        BUILD_CMD,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        bufsize=1,
        universal_newlines=True,
    )
    last_task = ""
    lines = []
    try:
        while True:
            line = proc.stdout.readline()
            if not line and proc.poll() is not None:
                break
            if line:
                log.write(line)
                lines.append(line)
                # Detect and print current Gradle task
                m = re.match(r"^> Task .+", line)
                if m and line != last_task:
                    print(f"[AGENT] Gradle step: {line.strip()}")
                    last_task = line
    except Exception as e:
        print(f"[AGENT] BUILD_STATUS: FAILURE (exception: {e})")
        sys.exit(1)

exitcode = proc.wait()
print(f"[AGENT] Build finished at {datetime.datetime.now().isoformat(' ', 'seconds')}")

# Error summary
error_patterns = re.compile(
    r"(^[\w./\\-]+\.kt:\d+:|^[\w./\\-]+\.java:\d+:|^> Task .+ FAILED|^FAILURE:|^BUILD FAILED|^Lint found errors)"
)

matches = [(i, l.rstrip()) for i, l in enumerate(lines, 1) if error_patterns.search(l)]

with MD_REPORT.open("w", encoding="utf-8") as f:
    f.write("# Build Error Context\n\n")
    if not matches:
        f.write("_No errors found in build output_\n")
        f.write("BUILD_STATUS: SUCCESS\n")
    else:
        for lineno, msg in matches:
            f.write("## Error\n")
            f.write(f"**Line:** {lineno}\n")
            f.write(f"**Message:** `{msg}`\n\n")
            start = max(lineno-6, 0)
            end = min(lineno+5, len(lines))
            f.write("**Context:**\n```text\n")
            f.writelines(lines[start:end])
            f.write("```\n\n")
        f.write("BUILD_STATUS: FAILURE\n")

if exitcode == 0:
    print("[AGENT] BUILD_STATUS: SUCCESS")
    print(f"[AGENT] Full build log at {LOGFILE}")
    sys.exit(0)
else:
    print("[AGENT] BUILD_STATUS: FAILURE")
    print(f"[AGENT] See build errors in {MD_REPORT}")
    sys.exit(exitcode)
