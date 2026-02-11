"""Orchestrator: fetch issue, build prompt, hand everything to Claude Code."""

from __future__ import annotations

import json
import logging
from datetime import datetime, timezone
from pathlib import Path
import subprocess

from . import config
from .claude_runner import run_claude
from .github_client import get_issue, get_issue_labels

log = logging.getLogger(__name__)

CLAUDE_PROMPT_TEMPLATE = """\
You are an autonomous issue-fixing agent. Fix GitHub issue #{number}.

Title: {title}

Body:
{body}

Instructions:
1. Create and checkout a new branch named `{branch}` from main.
   - First run: git checkout main && git pull --ff-only
   - Then: git checkout -b {branch}
2. Read the codebase, understand the problem, and implement a fix.
   - Make minimal, targeted changes.
   - If you can add a small validation/test, do it.
3. Commit all changes with message: "Fix #{number}: {title}"
4. Push the branch: git push -u origin {branch}
5. Create a PR using: gh pr create --title "Fix #{number}: {title}" --body "<summary of what changed and why>" --base main --head {branch}

Important:
- Do ALL git operations yourself (branch, commit, push, PR).
- If the branch already exists locally, delete it first: git branch -D {branch}
- If the branch already exists on remote, delete it first: git push origin --delete {branch}
- After creating the PR, output the PR URL as the last line.
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
# Repo root
# ---------------------------------------------------------------------------

def _repo_root() -> str:
    """Return the git working tree that contains this tool."""
    candidate = Path(__file__).resolve().parent.parent.parent.parent
    if (candidate / ".git").exists():
        return str(candidate)
    r = subprocess.run(
        ["git", "rev-parse", "--show-toplevel"],
        capture_output=True, text=True, timeout=10,
    )
    return r.stdout.strip()


# ---------------------------------------------------------------------------
# Main orchestration
# ---------------------------------------------------------------------------

def process_issue(issue_number: int) -> dict:
    """Fetch issue, build prompt, run Claude Code, record result."""
    repo = _repo_root()
    log.info("Repo root: %s", repo)

    # 1. Fetch issue metadata
    issue = get_issue(issue_number)
    title = issue["title"]
    body = issue.get("body") or ""
    labels = get_issue_labels(issue)
    branch = _branch_name(issue_number, labels)

    log.info("Processing issue #%d: %s -> branch %s", issue_number, title, branch)

    # 2. Build prompt and run Claude — it handles all git operations
    prompt = CLAUDE_PROMPT_TEMPLATE.format(
        number=issue_number, title=title, body=body, branch=branch,
    )
    result = run_claude(prompt, cwd=repo)

    log.info("Claude exit code: %d", result.returncode)
    if result.stdout:
        log.info("Claude stdout (last 500 chars): %s", result.stdout[-500:])
    if result.stderr:
        log.warning("Claude stderr (last 500 chars): %s", result.stderr[-500:])

    # 3. Record run
    status = "ok" if result.returncode == 0 else "claude_failed"
    _record_run(issue_number, branch, status, result.stdout[-500:] if result.stdout else "")

    return {
        "status": status,
        "branch": branch,
        "claude_exit": result.returncode,
        "output_tail": result.stdout[-500:] if result.stdout else "",
    }
