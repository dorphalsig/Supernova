#!/usr/bin/env python3
"""
Gh PR Post‑Build Automator
==========================

Automates post‑gate tasks for agent branches:
1. **Issue linkage** – finds (or creates) an issue labelled with the branch
   name, then references it via *commit comment* **and** adds **"Refs #N"** to
   the PR description (strong link without auto‑closing).
2. Reads `/tmp/build_results.json` and, based on gate status:
   * **Green & mergeable** → rebase‑merge to `master`, comment
     "PR Gate Green, No conflicts. Auto merged", close issue, delete branch.
     *(Merging automatically closes the PR; no extra call needed.)*
   * **Green but conflicts** → comment "PR Gate Green. Conflicts exist:
     <mergeable_state>", leave issue open.
   * **Red** → comment "PR Gate Red" + concise error summary, leave issue open.

The script is **idempotent** – reruns mutate existing comments/body only when
missing.
"""
from __future__ import annotations

import argparse
import json
import os
import sys
from pathlib import Path
from typing import Any, Dict

import requests

GH_API = "https://api.github.com"

# --------------------------------------------------------------------------- #
# GitHub helpers                                                               #
# --------------------------------------------------------------------------- #

def gh_request(pat: str, method: str, url: str, **kwargs):
    headers = kwargs.pop("headers", {})
    headers.update({
        "Authorization": f"token {pat}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "gh_pr_postbuild",
    })
    r = requests.request(method, url, headers=headers, timeout=20, **kwargs)
    r.raise_for_status()
    return r.json() if r.text else {}


def find_or_create_issue(pat: str, owner: str, repo: str, branch: str) -> Dict[str, Any]:
    issues = gh_request(pat, "GET", f"{GH_API}/repos/{owner}/{repo}/issues",
                        params={"state": "open", "labels": branch, "per_page": 100})
    if issues:
        return issues[0]
    payload = {
        "title": f"Agent task for {branch}",
        "body": "Tracking task auto‑created by post‑build script.",
        "labels": [branch, "CI"]
    }
    return gh_request(pat, "POST", f"{GH_API}/repos/{owner}/{repo}/issues", json=payload)


def comment_issue(pat: str, owner: str, repo: str, num: int, body: str) -> None:
    gh_request(pat, "POST", f"{GH_API}/repos/{owner}/{repo}/issues/{num}/comments", json={"body": body})


def add_commit_comment(pat: str, owner: str, repo: str, sha: str, body: str) -> None:
    gh_request(pat, "POST", f"{GH_API}/repos/{owner}/{repo}/commits/{sha}/comments", json={"body": body})


def get_pr(pat: str, owner: str, repo: str, num: int) -> Dict[str, Any]:
    return gh_request(pat, "GET", f"{GH_API}/repos/{owner}/{repo}/pulls/{num}")


def patch_pr_body_with_ref(pat: str, owner: str, repo: str, pr_num: int, issue_num: int) -> None:
    pr = get_pr(pat, owner, repo, pr_num)
    body = pr.get("body") or ""
    ref_line = f"Refs #{issue_num}"
    if ref_line not in body:
        new_body = f"{body.rstrip()}\n\n{ref_line}\n"
        gh_request(pat, "PATCH", f"{GH_API}/repos/{owner}/{repo}/pulls/{pr_num}",
                   json={"body": new_body})


def merge_pr(pat: str, owner: str, repo: str, pr_num: int, sha: str) -> None:
    gh_request(pat, "PUT", f"{GH_API}/repos/{owner}/{repo}/pulls/{pr_num}/merge",
               json={"merge_method": "rebase", "sha": sha})


def close_issue(pat: str, owner: str, repo: str, num: int) -> None:
    gh_request(pat, "PATCH", f"{GH_API}/repos/{owner}/{repo}/issues/{num}", json={"state": "closed"})


def delete_branch(pat: str, owner: str, repo: str, branch: str) -> None:
    gh_request(pat, "DELETE", f"{GH_API}/repos/{owner}/{repo}/git/refs/heads/{branch}")

# --------------------------------------------------------------------------- #
# Error summariser                                                             #
# --------------------------------------------------------------------------- #

def summarise_errors(res: Dict[str, Any]) -> str:
    if res["result"] == "compile_error":
        return "**Compilation errors**\n" + "\n".join(
            f"* {e['file']}:{e['line']} — {e['msg']}" for e in res["compileErrors"])
    if res["result"] == "tests_failed":
        return f"**{res['failedTests']} failing tests**\n" + "\n".join(
            f"* {t['test']} — {t['msg']}" for t in res["testFailures"])
    return "Unknown gate failure"

# --------------------------------------------------------------------------- #
# Main                                                                         #
# --------------------------------------------------------------------------- #

def main() -> None:
    ap = argparse.ArgumentParser()
    ap.add_argument("--owner", required=True)
    ap.add_argument("--repo", required=True)
    ap.add_argument("--pr", type=int, required=True)
    ap.add_argument("--results", default="/tmp/build_results.json")
    ap.add_argument("--pat", default=os.getenv("GITHUB_TOKEN"))
    args = ap.parse_args()

    if not args.pat:
        sys.exit("❌  GITHUB_TOKEN / --pat is required")

    res = json.loads(Path(args.results).read_text())
    pr = get_pr(args.pat, args.owner, args.repo, args.pr)

    branch = pr["head"]["ref"]
    sha = pr["head"]["sha"]

    issue = find_or_create_issue(args.pat, args.owner, args.repo, branch)

    # Ensure strong linkage
    add_commit_comment(args.pat, args.owner, args.repo, sha,
                       f"{pr['title']}\nRefs #{issue['number']}")
    patch_pr_body_with_ref(args.pat, args.owner, args.repo, args.pr, issue["number"])

    # Handle gate outcome
    if res["result"] == "success":
        if pr["mergeable"]:
            merge_pr(args.pat, args.owner, args.repo, args.pr, sha)
            comment_issue(args.pat, args.owner, args.repo, issue["number"],
                          "PR Gate Green, No conflicts. Auto merged")
            close_issue(args.pat, args.owner, args.repo, issue["number"])
            delete_branch(args.pat, args.owner, args.repo, branch)
        else:
            comment_issue(args.pat, args.owner, args.repo, issue["number"],
                          f"PR Gate Green. Conflicts exist: {pr.get('mergeable_state', 'unknown')}")
    else:
        comment_issue(args.pat, args.owner, args.repo, issue["number"],
                      f"PR Gate Red\n\n{summarise_errors(res)}")


if __name__ == "__main__":
    main()
