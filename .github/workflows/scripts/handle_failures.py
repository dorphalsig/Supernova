import os
import re
import json
import requests
from datetime import datetime

def post_comment(repo, issue_number, token, message):
    url = f"https://api.github.com/repos/{repo}/issues/{issue_number}/comments"
    headers = {"Authorization": f"token {token}"}
    response = requests.post(url, headers=headers, json={"body": message})
    response.raise_for_status()

def get_gist(gist_id, token):
    url = f"https://api.github.com/gists/{gist_id}"
    headers = {"Authorization": f"token {token}"}
    r = requests.get(url, headers=headers)
    r.raise_for_status()
    return r.json()

def update_gist(gist_id, file_name, new_content, token):
    url = f"https://api.github.com/gists/{gist_id}"
    headers = {"Authorization": f"token {token}"}
    data = {"files": {file_name: {"content": new_content}}}
    r = requests.patch(url, headers=headers, json=data)
    r.raise_for_status()

def extract_issue_number(commit_msg):
    import re
    match = re.search(r"#(\d+)", commit_msg)
    if match:
        return match.group(1)
    return None

def parse_failures(test_output):
    failures = []
    # Regex matching lines like SomeTest.kt:123: error message
    pattern = re.compile(r"^([\w./-]+\.kt):(\d+):\s*(.*)$", re.MULTILINE)
    for match in pattern.finditer(test_output):
        failures.append({
            "file": match.group(1),
            "line": int(match.group(2)),
            "error": match.group(3).strip(),
        })
    return failures

def main():
    repo = os.environ["GITHUB_REPOSITORY"]
    gist_token = os.environ["GIST_TOKEN"]
    gist_id = os.environ["FAILURE_GIST_ID"]
    branch = os.environ["GITHUB_REF_NAME"]
    commit = os.environ["GITHUB_SHA"]
    run_id = os.environ["GITHUB_RUN_ID"]
    github_token = os.environ["GITHUB_TOKEN"]

    # Read the latest commit message
    commit_msg = os.popen("git log -1 --pretty=%B").read()
    issue_number = extract_issue_number(commit_msg)
    if not issue_number:
        print("No issue number found in commit message, exiting.")
        return

    # Read test output file (assumes test output redirected to test_output.log)
    if not os.path.exists("test_output.log"):
        print("test_output.log not found, exiting.")
        return

    with open("test_output.log") as f:
        test_output = f.read()

    failures = parse_failures(test_output)
    if not failures:
        print("No test failures detected, exiting.")
        return

    json_line = {
        "timestamp": datetime.utcnow().strftime("%Y-%m-%dT%H:%M:%SZ"),
        "repository": repo,
        "branch": branch,
        "commit": commit,
        "workflow": "Run Tests & Auto Rebase",
        "run": int(run_id),
        "test_failures": failures,
    }

    # Comment on the GitHub issue
    comment_body = f"""❌ **Tests failed for branch `{branch}`**
Commit: `{commit}`
[Workflow run](https://github.com/{repo}/actions/runs/{run_id})

### Failures:
```json
{json.dumps(json_line, indent=2)}
```"""
    post_comment(repo, issue_number, github_token, comment_body)
    print("Posted comment on issue #", issue_number)

    # Update the gist with the new JSONL line
    gist = get_gist(gist_id, gist_token)
    file_name = next(iter(gist["files"].keys()))
    current_content = gist["files"][file_name]["content"]
    updated_content = current_content + json.dumps(json_line) + "\n"
    update_gist(gist_id, file_name, updated_content, gist_token)
    print("Gist updated with new failure entry.")

if __name__ == "__main__":
    main()
