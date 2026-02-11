"""Run Claude Code CLI as a subprocess with live log streaming."""

from __future__ import annotations

import logging
import shlex
import subprocess
import threading

from . import config

log = logging.getLogger(__name__)


def _stream_pipe(pipe, level: int, label: str) -> list[str]:
    """Read lines from a pipe, log each one, and collect them."""
    lines: list[str] = []
    for line in pipe:
        line = line.rstrip("\n")
        lines.append(line)
        log.log(level, "[%s] %s", label, line)
    return lines


def run_claude(prompt: str, cwd: str) -> subprocess.CompletedProcess[str]:
    """Invoke Claude Code with --dangerously-skip-permissions and return the result.

    Streams stdout/stderr to the logger in real-time so you can watch progress.
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

    proc = subprocess.Popen(
        cmd,
        cwd=cwd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )

    # Stream both pipes in parallel threads
    stdout_lines: list[str] = []
    stderr_lines: list[str] = []

    def read_stdout():
        nonlocal stdout_lines
        stdout_lines = _stream_pipe(proc.stdout, logging.INFO, "claude")

    def read_stderr():
        nonlocal stderr_lines
        stderr_lines = _stream_pipe(proc.stderr, logging.WARNING, "claude:err")

    t_out = threading.Thread(target=read_stdout)
    t_err = threading.Thread(target=read_stderr)
    t_out.start()
    t_err.start()

    try:
        proc.wait(timeout=1800)  # 30 min hard limit
    except subprocess.TimeoutExpired:
        proc.kill()
        proc.wait()

    t_out.join()
    t_err.join()

    return subprocess.CompletedProcess(
        args=cmd,
        returncode=proc.returncode,
        stdout="\n".join(stdout_lines),
        stderr="\n".join(stderr_lines),
    )
