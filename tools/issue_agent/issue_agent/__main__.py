"""Entry point: python -m issue_agent

Starts a FastAPI server that accepts GitHub webhook events (issue opened/reopened)
and can also be triggered manually via POST /run/{issue_number}.
"""

from __future__ import annotations

import hashlib
import hmac
import json
import logging

import uvicorn
from fastapi import FastAPI, Header, HTTPException, Request

from . import config
from .agent import process_issue

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(name)s: %(message)s",
)
log = logging.getLogger("issue_agent")

app = FastAPI(title="Issue Agent", version="0.1.0")

# Track running issues to prevent concurrent runs on the same issue
_running: set[int] = set()


def _verify_signature(payload: bytes, signature: str | None) -> bool:
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
    x_hub_signature_256: str | None = Header(None),
    x_github_event: str | None = Header(None),
):
    """Handle GitHub issue webhook events."""
    body = await request.body()

    if not _verify_signature(body, x_hub_signature_256):
        raise HTTPException(status_code=403, detail="Invalid signature")

    if x_github_event != "issues":
        return {"status": "ignored", "reason": f"event={x_github_event}"}

    payload = json.loads(body)
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


@app.get("/health")
async def health():
    return {"status": "ok", "running": list(_running)}


def main():
    log.info("Starting issue agent on %s:%d", config.HOST, config.PORT)
    uvicorn.run(app, host=config.HOST, port=config.PORT, log_level="info")


main()
