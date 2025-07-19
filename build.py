#!/usr/bin/env python3
"""
gh_pr_build_runner.py — build / test reporter
================================================

Runs a Gradle build+test task and emits a **compact, LLM‑friendly JSON**
report. Includes an optional heartbeat so autonomous agents (which may kill a
process after >60 s of silence) keep receiving console output.

Key JSON schema
---------------
```
{
  "timestamp": "…Z",
  "pr": 42,                 # omitted if not provided
  "branch": "feature/foo",# omitted if not provided
  "task": "testDebugUnitTest",
  "result": "compile_error" | "tests_failed" | "success",
  "exitCode": 1,
  "durationSec": 17.4,
  "totalTests": 128,
  "failedTests": 3,
  "skippedTests": 2,
  "compileErrors": [ {"file": "…", "line": 88, "msg": "…"} ],
  "testFailures":  [ {"test": "…", "msg": "…"} ]
}
```

Operating modes
---------------
* **Agent mode** (`--agent`) — writes the blob to `/tmp/build_results.json`
  and prints a heartbeat every *N* seconds (default 30) so orchestration
  layers don’t abort long builds.
* **Gist mode** (default if `--gist` and `--pat` provided) — also appends the
  JSON blob to `test_failures.txt` in the designated error‑gist.

Exit‑code mirrors Gradle’s exit‑code so CI checks stay accurate.
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import threading
import time
import xml.etree.ElementTree as ET
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Dict, List

import requests

# --------------------------------------------------------------------------- #
# Regex helpers                                                               #
# --------------------------------------------------------------------------- #
KOTLIN_ERR = re.compile(r"e: (/.+?): \((\d+), \d+\): (.+)")
JAVA_ERR = re.compile(r"(/.+?\.java):(\d+): error: (.+)")

# --------------------------------------------------------------------------- #
# Compile‑error parsing                                                        #
# --------------------------------------------------------------------------- #

def parse_compile_errors(stdout: str, limit: int = 10) -> List[Dict[str, Any]]:
    """Return up to *limit* compile‑errors as list‑of‑dicts."""
    out: List[Dict[str, Any]] = []
    for line in stdout.splitlines():
        if (m := KOTLIN_ERR.search(line)):
            out.append({"file": Path(m.group(1)).name,
                        "line": int(m.group(2)),
                        "msg": m.group(3).strip()})
        elif (m := JAVA_ERR.search(line)):
            out.append({"file": Path(m.group(1)).name,
                        "line": int(m.group(2)),
                        "msg": m.group(3).strip()})
        if len(out) >= limit:
            break
    return out

# --------------------------------------------------------------------------- #
# JUnit result parsing                                                         #
# --------------------------------------------------------------------------- #

def _xml_files(task: str) -> List[Path]:
    return list(Path.cwd().glob(f"**/build/test-results/{task}/*.xml"))


def parse_test_results(task: str, limit: int = 50) -> Dict[str, Any]:
    total = failed = skipped = 0
    dur = 0.0
    fails: List[Dict[str, str]] = []

    for xml in _xml_files(task):
        root = ET.parse(xml).getroot()
        for tc in root.iter("testcase"):
            total += 1
            dur += float(tc.attrib.get("time", 0.0))
            err = tc.find("failure") or tc.find("error")
            if err is not None:
                failed += 1
                if len(fails) < limit:
                    fails.append({
                        "test": f"{tc.attrib.get('classname')}.{tc.attrib.get('name')}",
                        "msg": err.attrib.get("message", "")[:160]})
            if tc.find("skipped") is not None:
                skipped += 1

    return {"total": total, "failed": failed, "skipped": skipped,
            "duration": round(dur, 2), "failures": fails}

# --------------------------------------------------------------------------- #
# GitHub Gist integration                                                      #
# --------------------------------------------------------------------------- #

def append_to_gist(pat: str, gist_id: str, blob: str) -> None:
    sess = requests.Session()
    sess.headers.update({
        "Authorization": f"token {pat}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "gh_pr_build_runner",
    })
    g = sess.get(f"https://api.github.com/gists/{gist_id}", timeout=20)
    g.raise_for_status()
    old = g.json()["files"].get("test_failures.txt", {}).get("content", "")
    upd = {"files": {"test_failures.txt": {"content": f"{old.rstrip()}\n{blob}\n"}}}
    sess.patch(f"https://api.github.com/gists/{gist_id}", json=upd, timeout=20).raise_for_status()

# --------------------------------------------------------------------------- #
# Heartbeat helper                                                             #
# --------------------------------------------------------------------------- #

def start_heartbeat(enabled: bool, interval: int) -> None:
    if not enabled:
        return

    def _beat():
        while True:
            time.sleep(interval)
            stamp = datetime.now(timezone.utc).isoformat(timespec="seconds")
            print(f"[heartbeat] build running — {stamp}", flush=True)

    threading.Thread(target=_beat, daemon=True).start()

# --------------------------------------------------------------------------- #
# Main                                                                         #
# --------------------------------------------------------------------------- #

def main() -> None:
    ap = argparse.ArgumentParser(description="Run Gradle build/tests and emit compact JSON results.")
    ap.add_argument("--task", default=os.getenv("GRADLE_TASK", "testDebugUnitTest"))
    ap.add_argument("--agent", action="store_true",
                    help="Agent mode (heartbeat + /tmp output)")
    ap.add_argument("--beat-interval", type=int,
                    default=int(os.getenv("BEAT_INTERVAL", 30)),
                    help="Seconds between heartbeat prints (agent mode)")
    ap.add_argument("--gist", default=os.getenv("GIST_ID"),
                    help="Gist ID for uploading results (gist mode)")
    ap.add_argument("--pat", default=os.getenv("GIST_PAT"),
                    help="Personal access token with gist scope")
    ap.add_argument("--pr", type=int, default=os.getenv("PR_NUMBER"),
                    help="Pull request number (optional)")
    ap.add_argument("--branch", default=os.getenv("BRANCH_NAME"),
                    help="Branch name (optional)")
    args = ap.parse_args()

    # Validate gist mode requirements
    if not args.agent and (not args.gist or not args.pat):
        sys.exit("❌  Missing --gist or --pat (gist mode)")

    start_heartbeat(args.agent, args.beat_interval)

    t0 = time.time()
    proc = subprocess.run([
        "./gradlew", args.task, "--no-daemon", "--console=plain", "--stacktrace"
    ], capture_output=True, text=True)
    dur = round(time.time() - t0, 2)

    combined = proc.stdout + proc.stderr
    rc = proc.returncode

    # Build payload
    payload: Dict[str, Any] = {
        "timestamp": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "task": args.task,
        "exitCode": rc,
        "durationSec": dur,
        "compileErrors": [],
        "testFailures": []
    }
    # Optional fields
    if args.pr:
        payload["pr"] = int(args.pr)
    if args.branch:
        payload["branch"] = args.branch

    # Parse errors or tests
    if rc != 0:
        payload["result"] = "compile_error"
        payload["compileErrors"] = parse_compile_errors(combined)
    else:
        tests = parse_test_results(args.task)
        payload.update({
            "totalTests": tests["total"],
            "failedTests": tests["failed"],
            "skippedTests": tests["skipped"],
        })
        if tests["failed"]:
            payload["result"] = "tests_failed"
            payload["testFailures"] = tests["failures"]
        else:
            payload["result"] = "success"

    blob = json.dumps(payload, separators=(',', ':'))

    # Always write the local JSON summary
    Path("/tmp/build_results.json").write_text(blob + "\n", encoding="utf-8")

    if not args.agent:
        # In gist mode, also append to your Gist
        append_to_gist(args.pat, args.gist, blob)
        print(f"✅  Report appended to gist {args.gist}")
    else:
        print("✅  JSON results written to /tmp/build_results.json")

    sys.exit(rc)

# --------------------------------------------------------------------------- #
# Self‑test                                                                    #
# --------------------------------------------------------------------------- #

def _self_test() -> None:
    line = "/foo/Bar.kt: (12, 5): Bad"
    assert parse_compile_errors(f"e: {line}")[0]["line"] == 12
    print("self‑test OK")

if __name__ == "__main__":
    if "--self-test" in sys.argv:
        _self_test()
    else:
        main()
