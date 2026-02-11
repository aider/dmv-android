"""Orchestrator: fetch issue, build prompt, hand everything to Claude Code."""

from __future__ import annotations

import json
import logging
import re
from datetime import datetime, timezone
from pathlib import Path
import subprocess

from . import config
from .claude_runner import run_claude, run_claude_tui
from .github_client import (
    close_issue,
    get_issue,
    get_issue_labels,
    get_pr,
    get_pr_comments,
    get_pr_diff,
    get_pr_files,
    get_pr_labels,
    post_comment,
    post_pr_comment,
)

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
- NEVER include absolute local file paths (e.g. /Users/..., /home/...) in PR descriptions, issue comments, or any GitHub output. Always use relative paths from the repo root.
"""

ANDROID_PROMPT_TEMPLATE = """\
Implement GitHub issue #{number}.

Title: {title}

Body:
{body}

Use branch `{branch}`.
- If the branch already exists locally: git branch -D {branch}
- If the branch already exists on remote: git push origin --delete {branch}
- git checkout main && git pull --ff-only && git checkout -b {branch}

Follow your agent instructions for the full workflow: implement, build, emulator test, screenshots, cleanup, PR.
Commit message: "Fix #{number}: {title}"
After creating the PR, output the PR URL as the last line.
"""

FIX_REVIEW_PROMPT_TEMPLATE = """\
You are an autonomous agent fixing review findings on an existing PR.

PR: #{number} — {title}
Branch: {branch}
PR URL: {pr_url}

Review findings to address:
{review_body}

Instructions:
1. Checkout the existing branch — do NOT create a new branch or PR:
   git fetch origin && git checkout {branch} && git pull origin {branch}
2. Address each review finding (R-items) listed above.
   - Make minimal, targeted changes.
   - If a finding is not actionable or is a false positive, skip it and note why.
3. Validate your changes:
   - Run: ./gradlew assembleDebug
   - Run: ./gradlew test (if tests exist)
4. Commit with message: "Address review findings for PR #{number}"
5. Push: git push origin {branch}

Important:
- Do NOT create a new branch. Work on `{branch}`.
- Do NOT create a new PR. Push to the existing branch.
- NEVER include absolute local file paths in any output.

When finished, output a summary in this format:

## Fix Summary
- **Addressed**: list each R-item you fixed
- **Skipped**: list any R-items skipped with reason
- **Files changed**: list modified files
- **Validation**: assembleDebug result, test result
"""


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

_PROMPTS_DIR = Path(__file__).resolve().parent.parent / "prompts"

_LINKED_ISSUE_RE = re.compile(
    r"(?:closes|fixes|resolves)\s+#(\d+)", re.IGNORECASE,
)


def _load_prompt_template(name: str) -> str:
    """Load a prompt template from the prompts/ directory."""
    return (_PROMPTS_DIR / name).read_text()


def _extract_linked_issue(pr_body: str) -> int | None:
    """Return the first linked issue number from a PR body, or None."""
    m = _LINKED_ISSUE_RE.search(pr_body)
    return int(m.group(1)) if m else None


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

def _is_android_issue(labels: list[str]) -> bool:
    return "android-developer" in labels


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

    # 2. Build prompt — select Android agent + template if labeled
    is_android = _is_android_issue(labels)
    template = ANDROID_PROMPT_TEMPLATE if is_android else CLAUDE_PROMPT_TEMPLATE
    agent = "android-app-agent" if is_android else None
    log.info("Using %s template%s", "Android" if is_android else "default",
             f" with agent={agent}" if agent else "")
    prompt = template.format(
        number=issue_number, title=title, body=body, branch=branch,
    )
    result = run_claude(prompt, cwd=repo, agent=agent)

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


def process_issue_tui(issue_number: int) -> dict:
    """Same as process_issue but uses Claude's native TUI via pseudo-TTY."""
    repo = _repo_root()
    log.info("Repo root: %s", repo)

    issue = get_issue(issue_number)
    title = issue["title"]
    body = issue.get("body") or ""
    labels = get_issue_labels(issue)
    branch = _branch_name(issue_number, labels)

    log.info("Processing issue #%d (TUI): %s -> branch %s", issue_number, title, branch)

    # Select Android agent + template if labeled
    is_android = _is_android_issue(labels)
    template = ANDROID_PROMPT_TEMPLATE if is_android else CLAUDE_PROMPT_TEMPLATE
    agent = "android-app-agent" if is_android else None
    log.info("Using %s template (TUI)%s", "Android" if is_android else "default",
             f" with agent={agent}" if agent else "")
    prompt = template.format(
        number=issue_number, title=title, body=body, branch=branch,
    )
    result = run_claude_tui(prompt, cwd=repo, agent=agent)

    log.info("Claude TUI exit code: %d", result.returncode)

    status = "ok" if result.returncode == 0 else "claude_failed"
    _record_run(issue_number, branch, status, "(tui mode)")

    return {
        "status": status,
        "branch": branch,
        "claude_exit": result.returncode,
        "mode": "tui",
    }


# ---------------------------------------------------------------------------
# PR review
# ---------------------------------------------------------------------------

def review_pr(pr_number: int) -> dict:
    """Run the pr-requirements-ux-reviewer agent on a PR.

    If the agent's verdict is APPROVE and a linked issue exists, the issue is
    closed automatically.
    """
    repo = _repo_root()
    log.info("Repo root: %s", repo)

    # 1. Fetch PR metadata and verify label
    pr = get_pr(pr_number)
    labels = get_pr_labels(pr)
    if "agent:review" not in labels:
        log.info("PR #%d missing agent:review label, skipping", pr_number)
        return {"status": "skipped", "reason": "no agent:review label"}

    title = pr["title"]
    body = pr.get("body") or ""
    pr_url = pr["html_url"]

    # 2. Detect linked issue from PR body
    linked_issue_number = _extract_linked_issue(body)
    issue_title = ""
    issue_body = ""
    if linked_issue_number:
        log.info("PR #%d links to issue #%d", pr_number, linked_issue_number)
        issue = get_issue(linked_issue_number)
        issue_title = issue["title"]
        issue_body = issue.get("body") or ""
    else:
        log.info("PR #%d has no linked issue (no Closes/Fixes/Resolves #N)", pr_number)

    # 3. Fetch diff and files list
    diff = get_pr_diff(pr_number)
    files = get_pr_files(pr_number)
    file_names = "\n".join(f"- {f['filename']}" for f in files)

    max_diff = config.REVIEW_MAX_DIFF_CHARS
    if len(diff) > max_diff:
        diff = diff[:max_diff] + f"\n\n... (diff truncated at {max_diff} chars)"

    log.info("Reviewing PR #%d: %s (%d files, %d chars diff)",
             pr_number, title, len(files), len(diff))

    # 4. Build prompt from template and run with the review agent
    template = _load_prompt_template("pr_requirements_ux_reviewer.txt")
    prompt = template.format(
        pr_number=pr_number,
        pr_title=title,
        pr_body=body,
        pr_url=pr_url,
        issue_number=linked_issue_number or "N/A",
        issue_title=issue_title or "N/A",
        issue_body=issue_body or "(no linked issue found)",
        files=file_names,
        diff=diff,
    )
    result = run_claude(
        prompt, cwd=repo,
        agent="pr-requirements-ux-reviewer",
        max_turns=config.REVIEW_MAX_TURNS,
    )

    log.info("Claude review exit code: %d", result.returncode)

    # 5. Post review as PR comment
    output = result.stdout.strip()
    if result.returncode == 0 and output:
        # The agent output should already start with "## Agent Review"
        if not output.startswith("## Agent Review"):
            output = f"## Agent Review\n\n{output}"
        post_pr_comment(pr_number, output)
        log.info("Posted review comment on PR #%d", pr_number)
    elif not output:
        log.warning("Claude produced no output for PR #%d", pr_number)

    # 6. Handle APPROVE verdict — close linked issue
    is_approved = "Ready to merge" in output if output else False
    if is_approved and linked_issue_number:
        log.info("Verdict APPROVE — closing issue #%d", linked_issue_number)
        close_issue(linked_issue_number)
        post_comment(
            linked_issue_number,
            f"Approved. Ready to merge. Tracking via PR #{pr_number}.",
        )
    elif is_approved and not linked_issue_number:
        post_pr_comment(
            pr_number,
            "**Note**: No linked issue found (missing `Closes #N` in PR body).",
        )

    # 7. Record run
    status = "ok" if result.returncode == 0 else "claude_failed"
    _record_run(pr_number, f"pr-{pr_number}", status,
                output[-500:] if output else "")

    return {
        "status": status,
        "pr": pr_number,
        "claude_exit": result.returncode,
        "verdict": "APPROVE" if is_approved else "REQUEST_CHANGES",
        "linked_issue": linked_issue_number,
        "output_tail": output[-500:] if output else "",
    }


# ---------------------------------------------------------------------------
# Fix PR review findings
# ---------------------------------------------------------------------------

def fix_pr_review(pr_number: int) -> dict:
    """Find the latest Agent Review comment on a PR and run the android agent to fix it."""
    repo = _repo_root()
    log.info("Repo root: %s", repo)

    # 1. Fetch PR metadata
    pr = get_pr(pr_number)
    branch = pr["head"]["ref"]
    title = pr["title"]
    pr_url = pr["html_url"]

    log.info("Fixing review findings for PR #%d: %s (branch %s)", pr_number, title, branch)

    # 2. Find the most recent Agent Review comment
    comments = get_pr_comments(pr_number)
    review_body = None
    for comment in reversed(comments):
        body = comment.get("body", "")
        if body.startswith("## Agent Review"):
            review_body = body
            break

    if not review_body:
        msg = "No Agent Review findings found on this PR. Run a review first."
        log.info("No review comment found on PR #%d", pr_number)
        post_pr_comment(pr_number, f"**Fix Review**: {msg}")
        return {"status": "no_review_found", "pr": pr_number}

    # 3. Build prompt and run Claude with android agent
    prompt = FIX_REVIEW_PROMPT_TEMPLATE.format(
        number=pr_number,
        title=title,
        branch=branch,
        pr_url=pr_url,
        review_body=review_body,
    )
    result = run_claude(
        prompt, cwd=repo, agent="android-app-agent",
        max_turns=config.FIX_REVIEW_MAX_TURNS,
    )

    log.info("Claude fix-review exit code: %d", result.returncode)
    output = result.stdout.strip()

    # 4. Post summary comment
    if result.returncode == 0 and output:
        # Try to extract the Fix Summary section
        summary = output
        marker = "## Fix Summary"
        idx = output.find(marker)
        if idx >= 0:
            summary = output[idx:]
        comment_body = f"**Fix Review for PR #{pr_number}**\n\n{summary}"
        post_pr_comment(pr_number, comment_body)
        log.info("Posted fix summary comment on PR #%d", pr_number)
    else:
        tail = output[-500:] if output else "(no output)"
        comment_body = (
            f"**Fix Review for PR #{pr_number}** failed "
            f"(exit code {result.returncode}).\n\n```\n{tail}\n```"
        )
        post_pr_comment(pr_number, comment_body)
        log.warning("Fix review failed for PR #%d (exit %d)", pr_number, result.returncode)

    # 5. Record run
    status = "ok" if result.returncode == 0 else "claude_failed"
    _record_run(pr_number, f"fix-pr-{pr_number}", status,
                output[-500:] if output else "")

    return {
        "status": status,
        "pr": pr_number,
        "branch": branch,
        "claude_exit": result.returncode,
        "output_tail": output[-500:] if output else "",
    }
