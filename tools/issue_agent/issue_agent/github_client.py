"""Thin wrapper around `gh` CLI and GitHub REST API via httpx."""

from __future__ import annotations

import httpx

from . import config


def _headers() -> dict[str, str]:
    return {
        "Authorization": f"token {config.GITHUB_TOKEN}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
    }


def _api(path: str) -> str:
    return f"https://api.github.com/repos/{config.GITHUB_REPO}{path}"


def get_issue(number: int) -> dict:
    """Fetch a single issue by number."""
    r = httpx.get(_api(f"/issues/{number}"), headers=_headers(), timeout=15)
    r.raise_for_status()
    return r.json()


def post_comment(number: int, body: str) -> dict:
    """Post a comment on an issue."""
    r = httpx.post(
        _api(f"/issues/{number}/comments"),
        headers=_headers(),
        json={"body": body},
        timeout=15,
    )
    r.raise_for_status()
    return r.json()


def close_issue(number: int) -> dict:
    """Close a GitHub issue."""
    r = httpx.patch(
        _api(f"/issues/{number}"),
        headers=_headers(),
        json={"state": "closed"},
        timeout=15,
    )
    r.raise_for_status()
    return r.json()


def get_issue_labels(issue: dict) -> list[str]:
    """Return label names from an issue dict."""
    return [lbl["name"] if isinstance(lbl, dict) else lbl for lbl in issue.get("labels", [])]


# ---------------------------------------------------------------------------
# Pull Request helpers
# ---------------------------------------------------------------------------

def get_pr(number: int) -> dict:
    """Fetch a single pull request by number."""
    r = httpx.get(_api(f"/pulls/{number}"), headers=_headers(), timeout=15)
    r.raise_for_status()
    return r.json()


def get_pr_diff(number: int) -> str:
    """Fetch the unified diff for a pull request."""
    headers = _headers()
    headers["Accept"] = "application/vnd.github.diff"
    r = httpx.get(_api(f"/pulls/{number}"), headers=headers, timeout=30)
    r.raise_for_status()
    return r.text


def get_pr_files(number: int) -> list[dict]:
    """Fetch the list of changed files for a pull request."""
    r = httpx.get(_api(f"/pulls/{number}/files"), headers=_headers(), timeout=15)
    r.raise_for_status()
    return r.json()


def get_pr_labels(pr: dict) -> list[str]:
    """Return label names from a PR dict."""
    return [lbl["name"] if isinstance(lbl, dict) else lbl for lbl in pr.get("labels", [])]


def get_pr_comments(number: int) -> list[dict]:
    """Fetch all comments on a pull request."""
    r = httpx.get(_api(f"/issues/{number}/comments"), headers=_headers(), timeout=15)
    r.raise_for_status()
    return r.json()


def post_pr_comment(number: int, body: str) -> dict:
    """Post a comment on a pull request (uses the issues comments endpoint)."""
    return post_comment(number, body)
