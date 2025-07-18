#!/usr/bin/env python3

import requests
import sys
import argparse

def create_issue(token, title, body, labels=None):
    url = "https://api.github.com/repos/dorphalsig/Supernova/issues"
    headers = {
        "Authorization": f"token {token}",
        "Accept": "application/vnd.github.v3+json"
    }
    data = {"title": title, "body": body}
    if labels:
        data["labels"] = labels
    try:
        r = requests.post(url, headers=headers, json=data)
        if r.status_code == 201:
            print("GitHub Issue #:" + r.json()["number"])
            return True
        else:
            print(f"Error: {r.status_code}\n{r.text}", file=sys.stderr)
    except Exception as e:
        print(f"Request failed: {e}", file=sys.stderr)
    return False

def main():
    p = argparse.ArgumentParser(description="Create a GitHub issue")
    p.add_argument("--pat", required=True)
    p.add_argument("--title", required=True)
    p.add_argument("--body", required=True)
    p.add_argument("--labels", nargs="*")
    args = p.parse_args()
    if create_issue(args.pat, args.title, args.body, args.labels):
        sys.exit(0)
    sys.exit(1)

if __name__ == "__main__":
    main()
