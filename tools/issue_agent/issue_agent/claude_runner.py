"""Run Claude Code CLI with stream-json + partial messages, printing to terminal."""

from __future__ import annotations

import json
import logging
import shlex
import subprocess
import sys
import threading

from . import config

log = logging.getLogger(__name__)


def _process_stdout(pipe) -> list[str]:
    """Read stream-json lines, print human-readable summary to terminal, collect raw."""
    lines: list[str] = []
    for raw in pipe:
        raw = raw.rstrip("\n")
        lines.append(raw)

        try:
            event = json.loads(raw)
        except (json.JSONDecodeError, TypeError):
            print(raw, file=sys.stderr, flush=True)
            continue

        etype = event.get("type", "")

        # Assistant text (partial chunks arrive here too)
        if etype == "assistant":
            content = event.get("message", "")
            if isinstance(content, str) and content.strip():
                print(content, file=sys.stderr, flush=True)
            elif isinstance(content, list):
                for block in content:
                    if isinstance(block, dict) and block.get("type") == "text":
                        text = block.get("text", "")
                        if text.strip():
                            print(text, end="", file=sys.stderr, flush=True)

        # Tool use — show what Claude is doing
        elif etype == "tool_use":
            name = event.get("name", "?")
            inp = event.get("input", {})
            if name == "Bash":
                cmd = inp.get("command", "")
                print(f"\n> {name}: {cmd[:200]}", file=sys.stderr, flush=True)
            elif name in ("Write", "Edit"):
                path = inp.get("file_path", "?")
                print(f"\n> {name}: {path}", file=sys.stderr, flush=True)
            elif name in ("Read", "Glob", "Grep"):
                print(f"\n> {name}: {str(inp)[:200]}", file=sys.stderr, flush=True)
            else:
                print(f"\n> {name}", file=sys.stderr, flush=True)

        # Result (final output)
        elif etype == "result":
            text = event.get("result", "")
            if text:
                print(f"\n=== RESULT ===\n{text[:1000]}", file=sys.stderr, flush=True)

        # System
        elif etype == "system":
            subtype = event.get("subtype", "")
            if subtype == "init":
                session = event.get("session_id", "?")
                print(f"[session: {session}]", file=sys.stderr, flush=True)

    return lines


def _stream_stderr(pipe) -> list[str]:
    """Pass stderr through to terminal."""
    lines: list[str] = []
    for line in pipe:
        line = line.rstrip("\n")
        lines.append(line)
        if line.strip():
            print(line, file=sys.stderr, flush=True)
    return lines


def run_claude(prompt: str, cwd: str) -> subprocess.CompletedProcess[str]:
    """Invoke Claude Code with stream-json + partial messages.

    Output is printed to terminal in real-time AND captured for the API response.
    """
    cmd: list[str] = [
        config.CLAUDE_CMD,
        "--dangerously-skip-permissions",
        "-p",
        prompt,
        "--max-turns",
        str(config.MAX_TURNS),
        "--output-format",
        "stream-json",
        "--verbose",
        "--include-partial-messages",
    ]

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
        stdout_lines = _process_stdout(proc.stdout)

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
