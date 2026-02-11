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
    """Read stream-json lines, print human-readable output to terminal, collect raw."""
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

        # --- stream_event: partial deltas (text, tool_use) ---
        if etype == "stream_event":
            inner = event.get("event", {})
            inner_type = inner.get("type", "")

            # Text delta — partial assistant text
            if inner_type == "content_block_delta":
                delta = inner.get("delta", {})
                if delta.get("type") == "text_delta":
                    print(delta.get("text", ""), end="", file=sys.stderr, flush=True)
                elif delta.get("type") == "input_json_delta":
                    pass  # tool input streaming, skip

            # Content block start — tool_use begins
            elif inner_type == "content_block_start":
                block = inner.get("content_block", {})
                if block.get("type") == "tool_use":
                    name = block.get("name", "?")
                    print(f"\n> [{name}] ", end="", file=sys.stderr, flush=True)

        # --- assistant: complete message ---
        elif etype == "assistant":
            msg = event.get("message", {})
            content = msg.get("content", []) if isinstance(msg, dict) else []
            for block in content:
                if not isinstance(block, dict):
                    continue
                if block.get("type") == "text":
                    text = block.get("text", "")
                    if text.strip():
                        print(f"\n{text}", file=sys.stderr, flush=True)
                elif block.get("type") == "tool_use":
                    name = block.get("name", "?")
                    inp = block.get("input", {})
                    if name == "Bash":
                        print(f"\n> Bash: {inp.get('command', '')[:300]}", file=sys.stderr, flush=True)
                    elif name in ("Write", "Edit"):
                        print(f"\n> {name}: {inp.get('file_path', '?')}", file=sys.stderr, flush=True)
                    elif name in ("Read", "Glob", "Grep"):
                        detail = inp.get("file_path", "") or inp.get("pattern", "") or inp.get("path", "")
                        print(f"\n> {name}: {detail[:200]}", file=sys.stderr, flush=True)
                    else:
                        print(f"\n> {name}", file=sys.stderr, flush=True)

        # --- tool_use (top-level, non-stream) ---
        elif etype == "tool_use":
            name = event.get("name", "?")
            inp = event.get("input", {})
            if name == "Bash":
                print(f"\n> Bash: {inp.get('command', '')[:300]}", file=sys.stderr, flush=True)
            elif name in ("Write", "Edit"):
                print(f"\n> {name}: {inp.get('file_path', '?')}", file=sys.stderr, flush=True)
            else:
                print(f"\n> {name}: {str(inp)[:200]}", file=sys.stderr, flush=True)

        # --- result ---
        elif etype == "result":
            text = event.get("result", "")
            cost = event.get("total_cost_usd", 0)
            turns = event.get("num_turns", 0)
            print(f"\n{'='*50}", file=sys.stderr, flush=True)
            print(f"Done. Turns: {turns}, Cost: ${cost:.4f}", file=sys.stderr, flush=True)
            if text:
                # Show first 500 chars of result
                print(text[:500], file=sys.stderr, flush=True)

        # --- system init ---
        elif etype == "system":
            subtype = event.get("subtype", "")
            if subtype == "init":
                model = event.get("model", "?")
                sid = event.get("session_id", "?")
                print(f"[Claude {model} | session: {sid}]", file=sys.stderr, flush=True)

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
