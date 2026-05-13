#!/bin/sh
# Launch the Ptolemy II Auto-Modeling Agent backend on Unix-likes.
# Requires the PTII environment variable to point at the Ptolemy II
# repo root.

set -eu

if [ -z "${PTII:-}" ]; then
    # Assume this script lives at $PTII/org/ptolemy/agent/bin
    SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
    PTII="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
    export PTII
fi

PORT="${1:-7777}"

echo "Using PTII=$PTII"
echo "Starting backend on port $PORT ..."

exec java -classpath "$PTII" org.ptolemy.agent.server.AgentServerMain "$PORT"
