#!/usr/bin/env python3
"""
GitHub Issue Creator Script for dorphalsig/Supernova
Creates a GitHub issue and returns the issue number.
"""

import requests
import json
import sys
import argparse

def create_github_issue(pat, title, body, labels=None):
    """
    Create a GitHub issue in dorphalsig/Supernova.

    Args:
        pat (str): Personal Access Token
        title (str): Issue title
        body (str): Issue description
        labels (list): Optional list of labels

    Returns:
        int: Issue number if successful, None if failed
    """

    repo = "dorphalsig/Supernova"
    url = f"https://api.github.com/repos/{repo}/issues"

    headers = {
        'Authorization': f'token {pat}',
        'Accept': 'application/vnd.github.v3+json',
        'Content-Type': 'application/json'
    }

    data = {
        'title': title,
        'body': body
    }

    if labels:
        data['labels'] = labels

    try:
        response = requests.post(url, headers=headers, json=data)

        if response.status_code == 201:
            issue_data = response.json()
            return issue_data['number']
        else:
            print(f"Error creating issue: {response.status_code}", file=sys.stderr)
            print(f"Response: {response.text}", file=sys.stderr)
            return None

    except requests.exceptions.RequestException as e:
        print(f"Request failed: {e}", file=sys.stderr)
        return None

def main():
    parser = argparse.ArgumentParser(description='Create a GitHub issue in dorphalsig/Supernova')
    parser.add_argument('--pat', required=True, help='GitHub Personal Access Token')
    parser.add_argument('--title', required=True, help='Issue title')
    parser.add_argument('--body', required=True, help='Issue description')
    parser.add_argument('--labels', nargs='*', help='Optional labels for the issue')

    args = parser.parse_args()

    issue_number = create_github_issue(
        pat=args.pat,
        title=args.title,
        body=args.body,
        labels=args.labels
    )

    if issue_number:
        print(issue_number)
        sys.exit(0)
    else:
        sys.exit(1)

if __name__ == "__main__":
    main()