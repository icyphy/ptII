# Ptolemy II Auto-Modeling Agent

A backend service that wraps Ptolemy II so that:

- a Large Language Model (LLM) can drive model construction through a small set of tool functions, and
- a decoupled web frontend can render the model and plot simulation results over plain HTTP.

The Ptolemy II kernel is **not modified**. Everything in this package
plugs in through existing public APIs (`MoMLParser`,
`MoMLChangeRequest`, `Manager`, `ExecutionListener`, `Recorder`).

## Milestone status

- [x] **M0** Backend skeleton + embedded Ptolemy + REST endpoints
- [x] **M1** React frontend skeleton + chat/canvas/plot panels
- [x] **M2** Tool registry + OpenAI client + ReAct agent loop
- [x] **M3** Interactive canvas (React Flow + MoML <-> Graph JSON)
- [x] **M4** Bidirectional editing through the Tool API (params, connect, delete, undo)
- [x] **M5** Standard demos (RC low-pass, PID tuning, broken-model repair) + dev launcher

## One-command launch

```sh
# Unix-likes
./org/ptolemy/agent/bin/dev.sh

# Windows
org\ptolemy\agent\bin\dev.bat
```

Starts the Java backend on `:7777` and the React dev server on
`:5173`. Open <http://localhost:5173> in a browser.

## M0 — quick start

### Compile

The package follows the standard Ptolemy II build conventions:

```sh
cd $PTII/org/ptolemy/agent
make
```

### Run the smoke test

```sh
cd $PTII
java -classpath "$PTII" org.ptolemy.agent.test.SessionSmokeTest
```

You should see the model loaded, exported, and run, with output like:

```
[smoke] using model: .../ptolemy/actor/lib/test/auto/Ramp1.xml
[smoke] load -> ok=true, message=loaded ...
[smoke] exported MoML length = 1234
[smoke] run -> ok=true, message=run finished
[smoke] probe count after run = 1
[smoke] OK
```

### Run the HTTP server

```sh
cd $PTII
java -classpath "$PTII" org.ptolemy.agent.server.AgentServerMain 7777
```

Then in another terminal:

```sh
# liveness check
curl http://localhost:7777/api/v1/health

# create a session
SID=$(curl -s -X POST http://localhost:7777/api/v1/sessions \
        | python -c "import sys,json;print(json.load(sys.stdin)['id'])")

# load an existing demo model
curl -s -X POST http://localhost:7777/api/v1/sessions/$SID/load \
     -H 'Content-Type: application/json' \
     -d "{\"path\":\"$PTII/ptolemy/actor/lib/test/auto/Ramp1.xml\"}"

# inspect the model
curl -s http://localhost:7777/api/v1/sessions/$SID/moml

# run the simulation
curl -s -X POST http://localhost:7777/api/v1/sessions/$SID/run

# fetch signals
curl -s http://localhost:7777/api/v1/sessions/$SID/signals
```

## REST API (M0)

| Method | Path | Body | Notes |
|--------|------|------|-------|
| GET    | `/api/v1/health` | – | liveness check |
| POST   | `/api/v1/sessions` | – | create session, returns `{id, ...}` |
| GET    | `/api/v1/sessions` | – | list sessions |
| GET    | `/api/v1/sessions/{id}` | – | session summary |
| DELETE | `/api/v1/sessions/{id}` | – | dispose a session |
| POST   | `/api/v1/sessions/{id}/load` | `{"path":"..."}` or `{"moml":"..."}` | load model |
| GET    | `/api/v1/sessions/{id}/moml` | – | current model MoML (text/plain) |
| POST   | `/api/v1/sessions/{id}/change` | `{"moml":"<entity .../>"}` | apply a `MoMLChangeRequest` |
| POST   | `/api/v1/sessions/{id}/run` | – | run to completion, returns signals |
| GET    | `/api/v1/sessions/{id}/signals` | – | latest harvested signals |
| GET    | `/api/v1/agent/status` | – | LLM provider + available tools |
| GET    | `/api/v1/agent/library` | – | curated actor catalog |
| POST   | `/api/v1/sessions/{id}/agent/chat` | `{"message":"..."}` | run the agent on this goal |
| POST   | `/api/v1/sessions/{id}/tools/{name}` | tool args | call a tool directly (no LLM) |

CORS is enabled for every origin to make local React development frictionless.

## Enabling the LLM (M2)

The agent loop ships with a `NullLLMClient` fallback so the backend
boots even without an LLM. To enable real automatic modeling:

```sh
export OPENAI_API_KEY=sk-...           # required
export OPENAI_MODEL=gpt-4o-mini        # optional, default shown
export OPENAI_BASE_URL=https://api.openai.com  # optional; any OpenAI-compatible host works
```

Then verify:

```sh
curl http://localhost:7777/api/v1/agent/status
```

You can also drive tools directly without an LLM, useful for tests and
for the canvas:

```sh
curl -X POST http://localhost:7777/api/v1/sessions/$SID/tools/add_entity \
     -H 'Content-Type: application/json' \
     -d '{"name":"ramp","className":"ptolemy.actor.lib.Ramp","x":100,"y":100}'
```

## Bidirectional editing (M4)

Once a model is loaded, every UI interaction calls the same Tool API
the agent uses:

| User action | Tool invoked |
|-------------|--------------|
| Drag actor from Library onto canvas | `add_entity` |
| Drag from a source handle to a target handle | `connect` |
| Select node + press Delete | `delete` |
| Select edge + press Delete | `disconnect` |
| Edit parameter and blur the input | `set_parameter` |
| Ctrl-Z / Ctrl-Y | `POST /undo` and `/redo` (Ptolemy native UndoStack) |

Because every mutation is also a `MoMLChangeRequest`, the agent and
the user can interleave operations on the exact same model.

## Standard demos (M5)

Three canonical demos live in [demos/](demos/):

1. **BuildRCLowPass** — natural language to a running RC low-pass.
2. **TunePID** — multi-turn closed loop with deliberately bad PID gains
   in [demos/tune-pid-seed.xml](demos/tune-pid-seed.xml).
3. **RepairModel** — fix a broken SDF chain in
   [demos/repair-seed.xml](demos/repair-seed.xml).

See [demos/README.md](demos/README.md) for the suggested prompts.

## Architecture

```
+---------------+      +------------------------+      +--------------------+
| React UI      | <--> | SimpleHttpServer       | <--> | PtolemySession     |
| (M1+)         |      | + RestRoutes           |      | + SignalCollector  |
+---------------+      +------------------------+      +--------------------+
                                                                 |
                                                                 v
                                                  +-------------------------------+
                                                  | Ptolemy II kernel (embedded)  |
                                                  | MoMLParser / Manager / etc.   |
                                                  +-------------------------------+
```

The session is the unit of isolation: each one holds its own
`Workspace`, `MoMLParser`, `CompositeActor`, `Manager` and a private
collection of auto-injected `Recorder` actors.

## Dependencies

M0 deliberately introduces **no new third-party JARs**:

- `com.sun.net.httpserver.HttpServer` ships with the JDK.
- `org.json` is already vendored under `$PTII/org/json`.
- Ptolemy II classes are referenced directly.

Future milestones (M2 LLM client, M3 WebSocket push) may require an
HTTP client library; they will be added at that point.
