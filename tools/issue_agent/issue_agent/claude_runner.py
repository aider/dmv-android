"""Run Claude Code CLI as a subprocess."""

from __future__ import annotations

import logging
import shlex
import subprocess

from . import config

log = logging.getLogger(__name__)


def run_claude(prompt: str, cwd: str) -> subprocess.CompletedProcess[str]:
    """Invoke Claude Code with --dangerously-skip-permissions and return the result.

    The command is built as a list (no shell=True) for safety.
    """
    cmd: list[str] = [
        config.CLAUDE_CMD,
        "--dangerously-skip-permissions",
        "-p",
        prompt,
        "--max-turns",
        str(config.MAX_TURNS),
    ]

    extra = config.CLAUDE_ARGS.strip()
    if extra:
        cmd.extend(shlex.split(extra))

    log.info("Running: %s", " ".join(cmd))

    result = subprocess.run(
        cmd,
        cwd=cwd,
        capture_output=True,
        text=True,
        timeout=1800,  # 30 min hard limit
    )
    return result
