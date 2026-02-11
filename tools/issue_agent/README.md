# Issue Agent

Local replacement for the GitHub Actions self-hosted runner. Listens for GitHub
webhook events (issue opened/reopened) and runs Claude Code to fix them
automatically.

## Quick Start

```bash
# 1. Ensure gh CLI is authenticated
gh auth status

# 2. Install dependencies
cd tools/issue_agent
pip install -r requirements.txt

# 3. Set required env vars
export GITHUB_TOKEN=$(gh auth token)
export GITHUB_REPO=aider/dmv-android

# 4. Run
python -m issue_agent
# Starts on http://127.0.0.1:8347
```

## Endpoints

| Method | Path                      | Description                              |
|--------|---------------------------|------------------------------------------|
| POST   | `/webhook`                | GitHub webhook receiver                  |
| POST   | `/run/{issue_number}`     | Manual trigger (stream-json output)      |
| POST   | `/run_tui/{issue_number}` | Experimental TUI mode (colored terminal) |
| GET    | `/health`                 | Health check + currently running         |

## Manual trigger

```bash
curl -X POST http://127.0.0.1:8347/run/42
```

## GitHub Webhook Setup

1. Go to repo Settings > Webhooks > Add webhook
2. Payload URL: your server's public URL + `/webhook`
3. Content type: `application/json`
4. Secret: set `WEBHOOK_SECRET` env var to match
5. Events: select "Issues" only

## Environment Variables

| Variable         | Required | Default             | Description                    |
|------------------|----------|---------------------|--------------------------------|
| `GITHUB_TOKEN`   | Yes      | -                   | GitHub personal access token   |
| `GITHUB_REPO`    | No       | `aider/dmv-android`      | Owner/repo                     |
| `PROJECT_KEY`    | No       | `dmv`               | Branch name prefix key         |
| `WEBHOOK_SECRET` | No       | (empty)             | GitHub webhook HMAC secret     |
| `CLAUDE_CMD`     | No       | `claude`            | Path to Claude CLI             |
| `CLAUDE_ARGS`    | No       | (empty)             | Extra CLI args for Claude      |
| `MAX_TURNS`           | No       | `25`                | Max agentic turns per run                  |
| `HOST`                | No       | `127.0.0.1`         | Bind address                               |
| `PORT`                | No       | `8347`              | Bind port                                  |
| `TUI_TIMEOUT_SECONDS` | No       | `600`               | Timeout for TUI mode runs                  |
| `TUI_SEND_PROMPT`     | No       | `false`             | Auto-send the issue prompt in TUI mode     |

## TUI Mode (Experimental)

The `/run_tui/{issue_number}` endpoint launches Claude Code in interactive TUI mode
via a pseudo-TTY. Claude's native colored output renders directly in the server
terminal.

```bash
curl -X POST http://127.0.0.1:8347/run_tui/42
```

By default, Claude launches but the issue prompt is **not** sent automatically --
you'll see the interactive TUI waiting for input in the server terminal. Set
`TUI_SEND_PROMPT=true` to auto-send the issue prompt after a short init delay.

The existing `/run/{n}` endpoint is unchanged and continues to use `stream-json`.

## Branch Naming

- Issues labeled `type:bug` or `bug` -> `bug/dmv-{number}`
- Everything else -> `feature/dmv-{number}`

## State

Run history is persisted to `~/.issue_agent_state.json` (last 200 runs).
