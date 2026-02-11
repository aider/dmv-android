"""Run Claude Code CLI as a subprocess with direct terminal output."""

from __future__ import annotations

import logging
import shlex
import subprocess

from . import config

log = logging.getLogger(__name__)


def run_claude(prompt: str, cwd: str) -> subprocess.CompletedProcess[str]:
    """Invoke Claude Code with stdout/stderr going directly to the terminal.

    You see exactly the same output as running Claude interactively.
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

    log.info("Running Claude...")

    # No PIPE — stdout/stderr go straight to the terminal
    proc = subprocess.Popen(cmd, cwd=cwd)

    try:
        proc.wait(timeout=1800)  # 30 min hard limit
    except subprocess.TimeoutExpired:
        proc.kill()
        proc.wait()

    log.info("Claude exited with code %d", proc.returncode)

    return subprocess.CompletedProcess(
        args=cmd,
        returncode=proc.returncode,
        stdout="",
        stderr="",
    )
