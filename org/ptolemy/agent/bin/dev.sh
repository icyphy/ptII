#!/bin/sh
# One-shot launcher: starts the agent backend on 7777 and the React
# dev server on 5173. Press Ctrl-C to stop both.
#
# Requires PTII to point at the Ptolemy II repo root, plus a working
# javac (already used by `make` in the rest of the build) and Node.js
# for the frontend.

set -eu

if [ -z "${PTII:-}" ]; then
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    PTII="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
    export PTII
fi
echo "Using PTII=$PTII"

cleanup() {
    if [ -n "${BACKEND_PID:-}" ]; then
        kill "$BACKEND_PID" 2>/dev/null || true
    fi
    if [ -n "${FRONTEND_PID:-}" ]; then
        kill "$FRONTEND_PID" 2>/dev/null || true
    fi
}
trap cleanup INT TERM EXIT

# Backend.
echo "[dev] starting agent backend on port 7777"
java -classpath "$PTII" org.ptolemy.agent.server.AgentServerMain 7777 &
BACKEND_PID=$!

# Frontend.
if [ -d "$PTII/frontend" ]; then
    echo "[dev] starting React frontend on port 5173"
    cd "$PTII/frontend"
    if [ ! -d node_modules ]; then
        npm install --no-audit --no-fund
    fi
    npm run dev -- --host &
    FRONTEND_PID=$!
fi

wait
