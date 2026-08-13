# η — Clojure TUI Agent Harness

A minimal Clojure terminal agent that talks to an OpenAI-compatible API via `PROXX_*` environment variables.

## Quick Start

```bash
# Set your provider env vars
export PROXX_URL=http://your-provider:8789
export PROXX_AUTH_TOKEN=your-token

# Run it
clojure -M:run
```

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PROXX_URL` | `http://localhost:8789` | Provider base URL |
| `PROXX_AUTH_TOKEN` | (empty) | Bearer auth token |
| `PROXX_MODEL` | `mimo-v2.5-pro` | Default model ID |

## Tools

| Tool | Description |
|------|-------------|
| `read` | Read file contents (path, offset, limit) |
| `write` | Write to file (creates dirs) |
| `bash` | Execute shell command (with timeout) |
| `nrepl` | Evaluate Clojure in an nREPL server |

## Architecture

```
eta.core        → entry point, bootstraps agent + TUI
eta.tui         → JLine3 terminal UI, readline, prompts
eta.agent       → agent loop: LLM → tool calls → repeat
eta.tools       → tool definitions + handlers
eta.provider    → PROXX_* API client
```

## REPL Session

```
η> What files are in the current directory?
  ⚡ bash: ls -la
  → total 48
  drwxr-xr-x  6 user user 4096 ...
  ...

There are 6 entries in the current directory...
```

## License

LGPL v3 or later.
