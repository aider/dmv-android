"""Entry point: python -m issue_agent

Starts a FastAPI server that accepts GitHub webhook events (issue opened/reopened)
and can also be triggered manually via POST /run/{issue_number}.
"""

from __future__ import annotations

import hashlib
import hmac
import json
import logging
from typing import Optional, Set

import uvicorn
from fastapi import FastAPI, Header, HTTPException, Request

from . import config
from .agent import fix_pr_review, process_issue, process_issue_tui, review_pr

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s: %(message)s",
)
log = logging.getLogger("issue_agent")

app = FastAPI(title="Issue Agent", version="0.1.0")

# Track running issues/PRs to prevent concurrent runs on the same item.
# Issues use int keys; PR reviews use "pr-{number}" string keys.
_running: Set[int | str] = set()


def _verify_signature(payload: bytes, signature: Optional[str]) -> bool:
    """Verify GitHub webhook HMAC-SHA256 signature if a secret is configured."""
    if not config.WEBHOOK_SECRET:
        return True  # no secret configured, skip verification
    if not signature or not signature.startswith("sha256="):
        return False
    expected = hmac.new(
        config.WEBHOOK_SECRET.encode(), payload, hashlib.sha256,
    ).hexdigest()
    return hmac.compare_digest(f"sha256={expected}", signature)


@app.post("/webhook")
async def webhook(
    request: Request,
    x_hub_signature_256: Optional[str] = Header(None),
    x_github_event: Optional[str] = Header(None),
):
    """Handle GitHub issue webhook events."""
    body = await request.body()

    if not _verify_signature(body, x_hub_signature_256):
        raise HTTPException(status_code=403, detail="Invalid signature")

    payload = json.loads(body)

    if x_github_event == "pull_request":
        return await _handle_pr_webhook(payload)

    if x_github_event != "issues":
        return {"status": "ignored", "reason": f"event={x_github_event}"}

    action = payload.get("action")
    if action not in ("opened", "reopened"):
        return {"status": "ignored", "reason": f"action={action}"}

    issue = payload["issue"]
    number = issue["number"]

    # Only process issues from the repo owner
    sender = payload.get("sender", {}).get("login", "")
    owner = config.GITHUB_REPO.split("/")[0]
    if sender != owner:
        return {"status": "ignored", "reason": f"sender={sender} != owner={owner}"}

    if number in _running:
        return {"status": "ignored", "reason": "already running"}

    _running.add(number)
    try:
        result = process_issue(number)
    finally:
        _running.discard(number)

    return result


@app.post("/run/{issue_number}")
async def manual_run(issue_number: int):
    """Manually trigger a run for a specific issue number."""
    if issue_number in _running:
        raise HTTPException(status_code=409, detail="Already running for this issue")

    _running.add(issue_number)
    try:
        result = process_issue(issue_number)
    finally:
        _running.discard(issue_number)

    return result


@app.post("/run_tui/{issue_number}")
async def manual_run_tui(issue_number: int):
    """Trigger a TUI-mode run -- Claude's colored output renders in the server terminal."""
    if issue_number in _running:
        raise HTTPException(status_code=409, detail="Already running for this issue")

    _running.add(issue_number)
    try:
        result = process_issue_tui(issue_number)
    finally:
        _running.discard(issue_number)

    return result


async def _handle_pr_webhook(payload: dict) -> dict:
    """Handle pull_request webhook events for agent:review labeled PRs."""
    action = payload.get("action")
    if action not in ("opened", "synchronize", "labeled"):
        return {"status": "ignored", "reason": f"pr action={action}"}

    pr = payload["pull_request"]
    labels = [lbl["name"] for lbl in pr.get("labels", [])]
    if "agent:review" not in labels:
        return {"status": "skipped", "reason": "no agent:review label"}

    pr_number = pr["number"]
    key = f"pr-{pr_number}"

    if key in _running:
        return {"status": "ignored", "reason": "already running"}

    _running.add(key)
    try:
        result = review_pr(pr_number)
    finally:
        _running.discard(key)

    return result


@app.post("/run_pr/{pr_number}")
async def manual_run_pr(pr_number: int):
    """Manually trigger a PR review. The PR must have the agent:review label."""
    key = f"pr-{pr_number}"
    if key in _running:
        raise HTTPException(status_code=409, detail="Already running for this PR")

    _running.add(key)
    try:
        result = review_pr(pr_number)
    finally:
        _running.discard(key)

    return result


@app.post("/run_pr_fix_review/{pr_number}")
async def manual_run_pr_fix_review(pr_number: int):
    """Run the android agent to fix review findings on an existing PR."""
    key = f"fix-{pr_number}"
    if key in _running:
        raise HTTPException(status_code=409, detail="Already running fix for this PR")

    _running.add(key)
    try:
        result = fix_pr_review(pr_number)
    finally:
        _running.discard(key)

    return result


@app.get("/health")
async def health():
    return {"status": "ok", "running": list(_running)}


def main():
    log.info("Starting issue agent on %s:%d", config.HOST, config.PORT)
    uvicorn.run(app, host=config.HOST, port=config.PORT, log_level="info")


main()
