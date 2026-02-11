"""Run Claude Code CLI as a subprocess with live streaming via --output-format stream-json."""

from __future__ import annotations

import json
import logging
import shlex
import subprocess
import threading

from . import config

log = logging.getLogger(__name__)


def _stream_json_stdout(pipe) -> list[str]:
    """Parse stream-json lines from Claude and log meaningful events."""
    lines: list[str] = []
    for raw in pipe:
        raw = raw.rstrip("\n")
        lines.append(raw)
        try:
            event = json.loads(raw)
        except (json.JSONDecodeError, TypeError):
            log.info("[claude] %s", raw)
            continue

        etype = event.get("type", "")

        # Assistant text output
        if etype == "assistant" and "message" in event:
            content = event["message"]
            # content can be a string or list of blocks
            if isinstance(content, str):
                for line in content.splitlines():
                    if line.strip():
                        log.info("[claude] %s", line.strip())
            elif isinstance(content, list):
                for block in content:
                    if isinstance(block, dict) and block.get("type") == "text":
                        for line in block.get("text", "").splitlines():
                            if line.strip():
                                log.info("[claude] %s", line.strip())

        # Tool use
        elif etype == "tool_use":
            name = event.get("name", "?")
            tool_input = event.get("input", {})
            if name == "Bash" and "command" in tool_input:
                log.info("[claude:tool] %s: %s", name, tool_input["command"][:200])
            elif name in ("Read", "Glob", "Grep"):
                log.info("[claude:tool] %s: %s", name, str(tool_input)[:200])
            elif name in ("Write", "Edit"):
                path = tool_input.get("file_path", "?")
                log.info("[claude:tool] %s: %s", name, path)
            else:
                log.info("[claude:tool] %s", name)

        # Tool result
        elif etype == "tool_result":
            pass  # skip verbose tool results

        # System / error
        elif etype == "system" or etype == "error":
            msg = event.get("message", event.get("error", raw))
            log.info("[claude:%s] %s", etype, str(msg)[:300])

        # Result (final)
        elif etype == "result":
            text = event.get("result", "")
            if text:
                for line in text.splitlines()[:10]:
                    log.info("[claude:result] %s", line.strip())

    return lines


def _stream_stderr(pipe) -> list[str]:
    """Read stderr lines and log them."""
    lines: list[str] = []
    for line in pipe:
        line = line.rstrip("\n")
        lines.append(line)
        if line.strip():
            log.warning("[claude:err] %s", line)
    return lines


def run_claude(prompt: str, cwd: str) -> subprocess.CompletedProcess[str]:
    """Invoke Claude Code with --output-format stream-json for real-time visibility.

    The command is built as a list (no shell=True) for safety.
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
    ]

    extra = config.CLAUDE_ARGS.strip()
    if extra:
        cmd.extend(shlex.split(extra))

    log.info("Running Claude (stream-json mode)...")

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
        stdout_lines = _stream_json_stdout(proc.stdout)

    def read_stderr():
        nonlocal stderr_lines
        stderr_lines = _stream_stderr(proc.stderr)

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
