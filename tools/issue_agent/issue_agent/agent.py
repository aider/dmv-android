"""Orchestrator: receive an issue, branch, run Claude, commit, push, open PR."""

from __future__ import annotations

import json
import logging
import os
import subprocess
from datetime import datetime, timezone
from pathlib import Path

from . import config
from .claude_runner import run_claude
from .github_client import get_issue, get_issue_labels, post_comment

log = logging.getLogger(__name__)

CLAUDE_PROMPT_TEMPLATE = """\
Fix GitHub issue #{number}.

Title: {title}

Body:
{body}

Requirements:
- Make minimal, targeted changes.
- If you can add a small validation/test, do it.
- Commit all changes with message: "Fix #{number}: {title}"
- Summarize what changed and why (for PR description).
"""


# ---------------------------------------------------------------------------
# State persistence
# ---------------------------------------------------------------------------

def _load_state() -> dict:
    p = Path(config.STATE_FILE)
    if p.exists():
        return json.loads(p.read_text())
    return {"runs": []}


def _save_state(state: dict) -> None:
    Path(config.STATE_FILE).write_text(json.dumps(state, indent=2) + "\n")


def _record_run(issue_number: int, branch: str, status: str, detail: str = "") -> None:
    state = _load_state()
    state["runs"].append({
        "issue": issue_number,
        "branch": branch,
        "status": status,
        "detail": detail[:500],
        "ts": datetime.now(timezone.utc).isoformat(),
    })
    # Keep last 200 runs
    state["runs"] = state["runs"][-200:]
    _save_state(state)


# ---------------------------------------------------------------------------
# Branch naming
# ---------------------------------------------------------------------------

def _branch_prefix(labels: list[str]) -> str:
    for lbl in labels:
        if lbl.startswith("type:bug") or lbl == "bug":
            return "bug/"
    return "feature/"


def _branch_name(issue_number: int, labels: list[str]) -> str:
    prefix = _branch_prefix(labels)
    return f"{prefix}{config.PROJECT_KEY}-{issue_number}"


# ---------------------------------------------------------------------------
# Git helpers (list-based, no shell=True)
# ---------------------------------------------------------------------------

def _git(args: list[str], cwd: str) -> subprocess.CompletedProcess[str]:
    return subprocess.run(
        ["git"] + args, cwd=cwd, capture_output=True, text=True, timeout=30,
    )


def _repo_root() -> str:
    """Return the git working tree that contains this tool."""
    # Walk up from tools/issue_agent/ to find .git
    candidate = Path(__file__).resolve().parent.parent.parent.parent
    if (candidate / ".git").exists():
        return str(candidate)
    # Fallback: ask git
    r = subprocess.run(
        ["git", "rev-parse", "--show-toplevel"],
        capture_output=True, text=True, timeout=10,
    )
    return r.stdout.strip()


# ---------------------------------------------------------------------------
# Main orchestration
# ---------------------------------------------------------------------------

def process_issue(issue_number: int) -> dict:
    """End-to-end: fetch issue -> branch -> Claude -> commit -> push -> PR.

    Returns a dict with status and message.
    """
    repo = _repo_root()
    log.info("Repo root: %s", repo)

    # 1. Fetch issue
    issue = get_issue(issue_number)
    title = issue["title"]
    body = issue.get("body") or ""
    labels = get_issue_labels(issue)
    branch = _branch_name(issue_number, labels)

    log.info("Processing issue #%d: %s -> branch %s", issue_number, title, branch)

    # 2. Ensure we're on main and up to date
    _git(["checkout", "main"], cwd=repo)
    _git(["pull", "--ff-only"], cwd=repo)

    # 3. Create and checkout branch
    _git(["checkout", "-b", branch], cwd=repo)

    # 4. Post start comment
    try:
        post_comment(issue_number, f"Issue agent started on branch `{branch}`...")
    except Exception as e:
        log.warning("Failed to post start comment: %s", e)

    # 5. Run Claude
    prompt = CLAUDE_PROMPT_TEMPLATE.format(
        number=issue_number, title=title, body=body,
    )
    result = run_claude(prompt, cwd=repo)

    log.info("Claude exit code: %d", result.returncode)
    if result.stdout:
        log.info("Claude stdout (last 500 chars): %s", result.stdout[-500:])
    if result.stderr:
        log.warning("Claude stderr (last 500 chars): %s", result.stderr[-500:])

    # 6. Stage and commit (Claude may have already committed)
    _git(["add", "-A"], cwd=repo)
    commit_r = _git(
        ["commit", "-m", f"Fix #{issue_number}: {title}",
         "--allow-empty-message", "--no-edit"],
        cwd=repo,
    )
    committed = commit_r.returncode == 0

    # 7. Push
    push_r = _git(["push", "-u", "origin", branch], cwd=repo)
    if push_r.returncode != 0:
        log.error("Push failed: %s", push_r.stderr)
        _record_run(issue_number, branch, "push_failed", push_r.stderr)
        _git(["checkout", "main"], cwd=repo)
        return {"status": "error", "message": f"Push failed: {push_r.stderr.strip()}"}

    # 8. Open PR via gh CLI
    pr_r = subprocess.run(
        [
            "gh", "pr", "create",
            "--title", f"Fix #{issue_number}: {title}",
            "--body", f"Auto-fix for issue #{issue_number}\n\nClaude output:\n```\n{result.stdout[-1000:] if result.stdout else '(no output)'}\n```",
            "--base", "main",
            "--head", branch,
        ],
        cwd=repo, capture_output=True, text=True, timeout=30,
    )

    pr_url = pr_r.stdout.strip() if pr_r.returncode == 0 else None

    # 9. Post done comment
    done_body = f"Issue agent finished on branch `{branch}`."
    if pr_url:
        done_body += f"\nPR: {pr_url}"
    try:
        post_comment(issue_number, done_body)
    except Exception as e:
        log.warning("Failed to post done comment: %s", e)

    # 10. Return to main
    _git(["checkout", "main"], cwd=repo)

    status = "ok" if pr_url else "pr_failed"
    _record_run(issue_number, branch, status, pr_url or pr_r.stderr)

    return {
        "status": status,
        "branch": branch,
        "pr_url": pr_url,
        "claude_exit": result.returncode,
        "committed": committed,
    }
