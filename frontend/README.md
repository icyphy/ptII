# Ptolemy II Agent Frontend

React + Vite + Tailwind UI that talks to the agent backend in
`org/ptolemy/agent`.

## Quick start

```sh
cd frontend
npm install
npm run dev
```

Then open <http://localhost:5173>. Vite proxies `/api/*` to
`http://localhost:7777`, so start the backend first:

```sh
java -classpath "$PTII" org.ptolemy.agent.server.AgentServerMain 7777
```

## What you can do in M1

1. A session is created automatically on first load (header shows its id).
2. Type a path relative to `$PTII` in the toolbar (e.g.
   `ptolemy/actor/lib/test/auto/Ramp1.xml`) and click **Load**.
3. Click **Run** to execute the simulation; the right-hand chart
   refreshes with whatever probes the backend auto-attached, and the
   bottom panel shows the live MoML returned by the kernel.
4. The chat panel echoes a stub reply. The real agent loop lands in M2.

## Stack

- React 18 + TypeScript
- Vite dev server with `/api` proxy to the Java backend
- Tailwind CSS for styling
- Recharts for time-series plots
- Zustand for state management

## Roadmap

- **M2** — LLM tool calling, real agent replies, library panel.
- **M3** — React Flow canvas, MoML <-> graph diffing over WebSocket.
- **M4** — Parameter editor, bidirectional editing.
