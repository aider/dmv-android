"""Configuration loaded from .env file and environment variables."""

import os
from pathlib import Path

from dotenv import load_dotenv

load_dotenv(Path(__file__).resolve().parent.parent / ".env")

GITHUB_TOKEN = os.environ["GITHUB_TOKEN"]
GITHUB_REPO = os.environ.get("GITHUB_REPO", "aider/dmv-android")
PROJECT_KEY = os.environ.get("PROJECT_KEY", "dmv")
WEBHOOK_SECRET = os.environ.get("WEBHOOK_SECRET", "")

CLAUDE_CMD = os.environ.get("CLAUDE_CMD", "claude")
CLAUDE_ARGS = os.environ.get("CLAUDE_ARGS", "")
MAX_TURNS = int(os.environ.get("MAX_TURNS", "25"))

HOST = os.environ.get("HOST", "127.0.0.1")
PORT = int(os.environ.get("PORT", "8347"))

TUI_TIMEOUT_SECONDS = int(os.environ.get("TUI_TIMEOUT_SECONDS", "600"))
TUI_SEND_PROMPT = os.environ.get("TUI_SEND_PROMPT", "false").lower() in ("true", "1", "yes")

REVIEW_MAX_TURNS = int(os.environ.get("REVIEW_MAX_TURNS", "10"))
FIX_REVIEW_MAX_TURNS = int(os.environ.get("FIX_REVIEW_MAX_TURNS", "25"))
REVIEW_MAX_DIFF_CHARS = int(os.environ.get("REVIEW_MAX_DIFF_CHARS", "50000"))

STATE_FILE = os.path.expanduser("~/.issue_agent_state.json")
