"""Run Claude Code CLI, printing output to terminal."""

from __future__ import annotations

import logging
import os
import pty
import shlex
import signal
import subprocess
import sys
import threading
import time

from . import config

log = logging.getLogger(__name__)


def _stream_stderr(pipe) -> list[str]:
    """Pass stderr through to terminal."""
    lines: list[str] = []
    for line in pipe:
        line = line.rstrip("\n")
        lines.append(line)
        if line.strip():
            print(line, file=sys.stderr, flush=True)
    return lines


def run_claude(
    prompt: str,
    cwd: str,
    agent: str | None = None,
    max_turns: int | None = None,
) -> subprocess.CompletedProcess[str]:
    """Invoke Claude Code in print mode and capture the result.

    Output is printed to terminal in real-time AND captured for the API response.
    If agent is provided, runs with --agent <name> (e.g. "android-app-agent").
    """
    cmd: list[str] = [
        config.CLAUDE_CMD,
        "--dangerously-skip-permissions",
        "-p",
        prompt,
        "--max-turns",
        str(max_turns if max_turns is not None else config.MAX_TURNS),
    ]

    if agent:
        cmd.extend(["--agent", agent])

    extra = config.CLAUDE_ARGS.strip()
    if extra:
        cmd.extend(shlex.split(extra))

    log.info("Running Claude...")

    proc = subprocess.Popen(
        cmd,
        cwd=cwd,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
    )

    stdout_lines: list[str] = []
    stderr_lines: list[str] = []

    def read_stdout():
        nonlocal stdout_lines
        for line in proc.stdout:
            line = line.rstrip("\n")
            stdout_lines.append(line)
            if line.strip():
                print(line, file=sys.stderr, flush=True)

    def read_stderr():
        nonlocal stderr_lines
        stderr_lines = _stream_stderr(proc.stderr)

    t_out = threading.Thread(target=read_stdout)
    t_err = threading.Thread(target=read_stderr)
    t_out.start()
    t_err.start()

    try:
        proc.wait(timeout=1800)
    except subprocess.TimeoutExpired:
        proc.kill()
        proc.wait()

    t_out.join()
    t_err.join()

    log.info("Claude exited with code %d", proc.returncode)

    return subprocess.CompletedProcess(
        args=cmd,
        returncode=proc.returncode,
        stdout="\n".join(stdout_lines),
        stderr="\n".join(stderr_lines),
    )


def run_claude_tui(prompt: str, cwd: str, agent: str | None = None) -> subprocess.CompletedProcess[str]:
    """Invoke Claude Code in TUI mode via a pseudo-TTY.

    Claude sees a real terminal and renders its native colored output.
    All output goes directly to the server's terminal (stdout).
    Returns a CompletedProcess with empty stdout/stderr since output is not captured.
    If agent is provided, runs with --agent <name>.
    """
    cmd: list[str] = [
        config.CLAUDE_CMD,
        "--dangerously-skip-permissions",
    ]

    if agent:
        cmd.extend(["--agent", agent])

    log.info("Running Claude TUI: %s", " ".join(cmd))

    master_fd, slave_fd = pty.openpty()

    proc = subprocess.Popen(
        cmd,
        cwd=cwd,
        stdin=slave_fd,
        stdout=slave_fd,
        stderr=slave_fd,
        close_fds=True,
    )
    os.close(slave_fd)  # parent doesn't need the slave side

    # Reader thread: relay raw PTY output to our terminal
    stop_event = threading.Event()

    def _relay():
        while not stop_event.is_set():
            try:
                data = os.read(master_fd, 4096)
                if not data:
                    break
                sys.stdout.buffer.write(data)
                sys.stdout.buffer.flush()
            except OSError:
                break

    reader = threading.Thread(target=_relay, daemon=True)
    reader.start()

    # Optionally send the prompt after a short init delay
    if config.TUI_SEND_PROMPT:
        time.sleep(0.5)
        os.write(master_fd, (prompt + "\n").encode())

    # Wait with timeout
    timeout = config.TUI_TIMEOUT_SECONDS
    try:
        proc.wait(timeout=timeout)
    except subprocess.TimeoutExpired:
        log.warning("Claude TUI timed out after %ds, sending SIGTERM", timeout)
        proc.send_signal(signal.SIGTERM)
        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            log.warning("SIGTERM ignored, sending SIGKILL")
            proc.kill()
            proc.wait()

    stop_event.set()
    try:
        os.close(master_fd)
    except OSError:
        pass
    reader.join(timeout=2)

    log.info("Claude TUI exited with code %d", proc.returncode)

    return subprocess.CompletedProcess(
        args=cmd,
        returncode=proc.returncode,
        stdout="",
        stderr="",
    )
