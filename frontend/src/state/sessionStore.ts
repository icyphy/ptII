import { create } from "zustand";
import {
  agentApi,
  AgentResultJson,
  AgentStatus,
  AgentTraceStep,
  GraphPayload,
  LibraryEntry,
  SessionSummary,
  SignalsPayload,
} from "../api/agentClient";
import { DemoExample, runDemoExample } from "../examples/demoExamples";

// Single global session store. The right-hand panes (graph / signals /
// MoML / params) are all derived from a couple of pieces of state that
// this store keeps in sync with the backend.

export interface ChatEntry {
  id: number;
  kind: "user" | "agent" | "system" | "error" | "tool_call" | "tool_result";
  source: "user" | "agent";
  text: string;
  timestamp: number;
  meta?: Record<string, unknown>;
}

export interface AgentProgress {
  label: string;
  startedAt: number;
}

interface SessionState {
  sessionId: string | null;
  summary: SessionSummary | null;
  moml: string;
  signals: SignalsPayload | null;
  graph: GraphPayload | null;
  library: LibraryEntry[];
  selectedNodeId: string | null;
  isLoading: boolean;
  isAgentBusy: boolean;
  agentProgress: AgentProgress | null;
  /** Live partial reasoning from the currently-streaming LLM call.
   *  Updated in place (not appended to chat) so 30-60 s planning
   *  rounds show progress without flooding the chat list. Reset to
   *  null when no LLM stream is active. */
  agentLiveThought: string | null;
  isRunningDemo: boolean;
  activeDemoId: string | null;
  lastError: string | null;
  chat: ChatEntry[];
  agentStatus: AgentStatus | null;
  /** Composite path the canvas is currently viewing. Empty = top-level. */
  canvasPath: string[];

  ensureSession: () => Promise<void>;
  loadDemoModel: (path: string) => Promise<void>;
  refreshMoml: () => Promise<void>;
  refreshGraph: () => Promise<void>;
  refreshLibrary: () => Promise<void>;
  runSimulation: () => Promise<void>;
  sendToAgent: (message: string) => Promise<void>;
  stopAgent: () => void;
  refreshAgentStatus: () => Promise<void>;
  callTool: (
    name: string,
    args: Record<string, unknown>,
  ) => Promise<AgentResultJson | null>;
  selectNode: (id: string | null) => void;
  undo: () => Promise<void>;
  redo: () => Promise<void>;
  appendChat: (entry: Omit<ChatEntry, "id" | "timestamp">, source?: "user" | "agent") => void;
  clearChat: () => void;
  runExample: (demo: DemoExample) => Promise<void>;
  /** Drill into a child composite. */
  enterComposite: (name: string) => Promise<void>;
  /** Pop one level. */
  exitComposite: () => Promise<void>;
  /** Jump to an arbitrary depth (0 = root). */
  setCanvasPath: (path: string[]) => Promise<void>;
}

let _nextChatId = 1;
let _agentAbortController: AbortController | null = null;
const STRUCTURAL_TOOLS = new Set([
  "add_entity",
  "add_composite",
  "group_into_composite",
  "auto_layout",
  "connect",
  "disconnect",
  "set_parameter",
  "delete",
]);
const TOOL_REFRESH_DEBOUNCE_MS = 140;

export const useSessionStore = create<SessionState>((set, get) => {
  let refreshInFlight = false;
  let refreshPending = false;
  let refreshWantsSignals = false;
  let refreshTimer: ReturnType<typeof setTimeout> | null = null;

  const flushRefresh = () => {
    const sid = get().sessionId;
    if (!sid) return;
    if (refreshInFlight) {
      refreshPending = true;
      return;
    }
    refreshInFlight = true;
    void (async () => {
      try {
        await get().refreshMoml();
        if (refreshWantsSignals) {
          try {
            const latest = await agentApi.getSignals(sid);
            set({ signals: latest.signals ?? null });
          } catch {
            // Best effort only; next refresh will recover.
          } finally {
            refreshWantsSignals = false;
          }
        }
      } finally {
        refreshInFlight = false;
        if (refreshPending) {
          refreshPending = false;
          flushRefresh();
        }
      }
    })();
  };

  const scheduleToolRefresh = (alsoSignals: boolean) => {
    refreshWantsSignals = refreshWantsSignals || alsoSignals;
    if (refreshTimer) {
      clearTimeout(refreshTimer);
    }
    refreshTimer = setTimeout(() => {
      refreshTimer = null;
      flushRefresh();
    }, TOOL_REFRESH_DEBOUNCE_MS);
  };

  return ({
  sessionId: null,
  summary: null,
  moml: "",
  signals: null,
  graph: null,
  library: [],
  selectedNodeId: null,
  isLoading: false,
  isAgentBusy: false,
  agentProgress: null,
  agentLiveThought: null,
  isRunningDemo: false,
  activeDemoId: null,
  lastError: null,
  chat: [],
  agentStatus: null,
  canvasPath: [],

  ensureSession: async () => {
    if (get().sessionId) return;
    set({ isLoading: true, lastError: null });
    try {
      const summary = await agentApi.createSession();
      set({ sessionId: summary.id, summary });
      get().appendChat({
        kind: "system",
        text: `Session ${summary.id} ready.`,
        source: "user",
      });
    } catch (e) {
      set({ lastError: (e as Error).message });
    } finally {
      set({ isLoading: false });
    }
  },

  loadDemoModel: async (path: string) => {
    await get().ensureSession();
    const id = get().sessionId;
    if (!id) return;
    set({ isLoading: true, lastError: null });
    try {
      const result = await agentApi.loadFile(id, path);
      if (!result.ok) {
        set({ lastError: result.message });
        get().appendChat({ kind: "error", text: result.message, source: "user" });
        return;
      }
      get().appendChat({ kind: "system", text: `Loaded ${path}`, source: "user" });
      await get().refreshMoml();
    } catch (e) {
      set({ lastError: (e as Error).message });
    } finally {
      set({ isLoading: false });
    }
  },

  refreshMoml: async () => {
    const id = get().sessionId;
    if (!id) return;
    try {
      const moml = await agentApi.getMoml(id);
      const summary = await agentApi.getSummary(id);
      set({ moml, summary });
      await get().refreshGraph();
    } catch (e) {
      set({ lastError: (e as Error).message });
    }
  },

  refreshGraph: async () => {
    const id = get().sessionId;
    if (!id) return;
    try {
      const path = get().canvasPath;
      const graph = await agentApi.getGraph(id, path);
      // If the resolved path doesn't match what we requested, the
      // composite was deleted (or renamed); auto-walk back.
      const resolved = graph.path ?? [];
      if (resolved.length !== path.length) {
        set({ canvasPath: resolved });
      }
      set({ graph });
    } catch (e) {
      set({ lastError: (e as Error).message });
    }
  },

  refreshLibrary: async () => {
    try {
      const { entries } = await agentApi.getLibrary();
      set({ library: entries });
    } catch (e) {
      set({ lastError: (e as Error).message });
    }
  },

  callTool: async (name, args) => {
    await get().ensureSession();
    const id = get().sessionId;
    if (!id) return null;
    set({ isLoading: true, lastError: null });
    try {
      const result = await agentApi.callTool(id, name, args);
      if (!result.ok) {
        set({ lastError: result.message });
        get().appendChat({ kind: "error", text: `${name}: ${result.message}`, source: "user" });
        return result;
      }
      get().appendChat({
        kind: "tool_call",
        text: `${name}(${prettyArgs(args)})`,
        source: "user",
      });
      if (STRUCTURAL_TOOLS.has(name)) {
        scheduleToolRefresh(false);
      } else {
        await get().refreshMoml();
      }
      return result;
    } catch (e) {
      set({ lastError: (e as Error).message });
      return null;
    } finally {
      set({ isLoading: false });
    }
  },

  runSimulation: async () => {
    const id = get().sessionId;
    if (!id) return;
    set({ isLoading: true, lastError: null });
    try {
      const result = await agentApi.run(id);
      if (!result.ok) {
        set({ lastError: result.message });
        get().appendChat({ kind: "error", text: result.message, source: "user" });
        return;
      }
      get().appendChat({ kind: "system", text: "simulation finished.", source: "user" });
      const summary = await agentApi.getSummary(id);
      const sig = await agentApi.getSignals(id);
      set({ summary, signals: sig.signals ?? null });
    } catch (e) {
      set({ lastError: (e as Error).message });
    } finally {
      set({ isLoading: false });
    }
  },

  runExample: async (demo) => {
    await get().ensureSession();
    const id = get().sessionId;
    if (!id) return;
    set({
      isRunningDemo: true,
      activeDemoId: demo.id,
      lastError: null,
      signals: null,
    });
    get().appendChat({
      kind: "system",
      text: `▶ Running example: ${demo.title}`,
      source: "user",
    });
    try {
      const result = await runDemoExample(id, demo);
      if (!result.ok) {
        set({ lastError: result.message });
        get().appendChat({ kind: "error", text: result.message, source: "user" });
        return;
      }
      get().appendChat({
        kind: "tool_call",
        text: result.message,
        source: "user",
      });
      // Refresh graph + MoML before running so the canvas updates first.
      await get().refreshMoml();
      // Now run the simulation and pick up signals.
      await get().runSimulation();
    } catch (e) {
      set({ lastError: (e as Error).message });
      get().appendChat({ kind: "error", text: (e as Error).message, source: "user" });
    } finally {
      set({ isRunningDemo: false });
    }
  },

  sendToAgent: async (message: string) => {
    await get().ensureSession();
    const id = get().sessionId;
    if (!id) return;
    if (_agentAbortController) {
      _agentAbortController.abort();
    }
    const controller = new AbortController();
    _agentAbortController = controller;
    get().appendChat({ kind: "user", text: message, source: "agent" });
    const startedAt = Date.now();
    set({
      isAgentBusy: true,
      lastError: null,
      agentProgress: { label: "Connecting to agent…", startedAt },
    });

    // Coalesce bursty refreshes: while one is in flight, only schedule
    // ONE follow-up so we never queue up dozens of redundant fetches.
    let refreshing = false;
    let pendingRefresh = false;
    const scheduleAgentRefresh = (alsoSignals: boolean) => {
      const sid = get().sessionId;
      if (!sid) return;
      if (refreshing) {
        pendingRefresh = true;
        return;
      }
      refreshing = true;
      void (async () => {
        try {
          // refreshMoml internally calls refreshGraph and updates summary.
          await get().refreshMoml();
          if (alsoSignals) {
            try {
              const latest = await agentApi.getSignals(sid);
              set({ signals: latest.signals ?? null });
            } catch {
              // ignore — final refresh in finishTurn will recover.
            }
          }
        } finally {
          refreshing = false;
          if (pendingRefresh) {
            pendingRefresh = false;
            scheduleAgentRefresh(alsoSignals);
          }
        }
      })();
    };

    const applyStep = (step: AgentTraceStep) => {
      if (step.kind === "thought" && step.text) {
        // Streaming partial-thought updates from the planner (or any
        // future phase that opts into `plan_stream`) replace a single
        // live slot instead of piling up in the chat list.  When the
        // step name is anything else (or empty), the thought is final
        // and gets committed to the chat.
        if (step.name === "plan_stream") {
          set({
            agentLiveThought: step.text,
            agentProgress: { label: "Agent reasoning…", startedAt },
          });
          return;
        }
        get().appendChat({
          kind: "agent",
          text: step.text,
          meta: { stepKind: "thought" },
          source: "agent",
        });
        set({
          agentLiveThought: null,
          agentProgress: { label: "Agent reasoning…", startedAt },
        });
      } else if (step.kind === "tool_call") {
        get().appendChat({
          kind: "tool_call",
          text: `${step.name}(${prettyArgs(step.arguments)})`,
          meta: step.arguments,
          source: "agent",
        });
        set({
          agentLiveThought: null,
          agentProgress: { label: `Running ${step.name}…`, startedAt },
        });
      } else if (step.kind === "tool_result") {
        const data = step.arguments as { ok?: boolean; message?: string };
        const ok = data.ok !== false;
        get().appendChat({
          kind: "tool_result",
          text: `${step.name} → ${ok ? "OK" : "FAIL"} ${data.message ?? ""}`,
          source: "agent",
        });
        set({
          agentProgress: {
            label: `${step.name} ${ok ? "completed" : "failed"}`,
            startedAt,
          },
        });
        // Live canvas update: refresh after any successful mutating tool
        // so the user can watch the model build itself.
        if (ok) {
          if (STRUCTURAL_TOOLS.has(step.name)) {
            scheduleAgentRefresh(false);
          } else if (step.name === "run") {
            scheduleAgentRefresh(true);
          }
        }
      } else if (step.kind === "error") {
        get().appendChat({ kind: "error", text: step.text, source: "agent" });
        set({ agentProgress: { label: "Agent error", startedAt } });
      } else if (step.kind === "final" && step.text) {
        get().appendChat({
          kind: "agent",
          text: step.text,
          meta: { stepKind: "final" },
          source: "agent",
        });
        set({ agentProgress: { label: "Finalizing response…", startedAt } });
      }
    };

    const finishTurn = async (
      session: SessionSummary,
      signals: SignalsPayload | null,
    ) => {
      set({ summary: session, signals: signals ?? null });
      const sid = session?.id ?? id;
      if (!sid) return;
      await get().refreshMoml();
      try {
        const latest = await agentApi.getSignals(sid);
        set({ signals: latest.signals ?? null });
      } catch (e) {
        set({ lastError: (e as Error).message });
      }
    };

    try {
      let streamed = false;
      try {
        for await (const evt of agentApi.agentChatStream(
          id,
          message,
          controller.signal,
        )) {
          streamed = true;
          if (evt.event === "started") {
            set({
              agentProgress: { label: "Planning model with LLM…", startedAt },
            });
          } else if (evt.event === "step") {
            applyStep(evt.step);
          } else if (evt.event === "done") {
            await finishTurn(evt.session, evt.signals ?? null);
          } else if (evt.event === "error") {
            throw new Error(evt.message);
          }
        }
      } catch (streamError) {
        if (streamed) {
          throw streamError;
        }
        set({
          agentProgress: { label: "Running agent turn…", startedAt },
        });
        const response = await agentApi.agentChat(id, message, controller.signal);
        for (const step of response.trace?.steps ?? []) {
          applyStep(step);
        }
        await finishTurn(response.session, response.signals ?? null);
      }
    } catch (e) {
      if (controller.signal.aborted) {
        get().appendChat({
          kind: "system",
          text: "Generation stopped by user.",
          source: "agent",
        });
        try {
          await get().refreshMoml();
        } catch {
          // Best effort: the model may already include successful earlier steps.
        }
        return;
      }
      const msg = (e as Error).message;
      set({ lastError: msg });
      get().appendChat({ kind: "error", text: msg, source: "agent" });
    } finally {
      if (_agentAbortController === controller) {
        _agentAbortController = null;
      }
      set({ isAgentBusy: false, agentProgress: null, agentLiveThought: null });
    }
  },

  stopAgent: () => {
    if (!_agentAbortController) return;
    _agentAbortController.abort();
    set({
      agentProgress: {
        label: "Stopping agent generation…",
        startedAt: get().agentProgress?.startedAt ?? Date.now(),
      },
    });
  },

  refreshAgentStatus: async () => {
    try {
      const status = await agentApi.agentStatus();
      set({ agentStatus: status });
    } catch (e) {
      set({ lastError: (e as Error).message });
    }
  },

  selectNode: (id) => set({ selectedNodeId: id }),

  undo: async () => {
    const id = get().sessionId;
    if (!id) return;
    set({ isLoading: true, lastError: null });
    try {
      const result = await agentApi.undo(id);
      get().appendChat({
        kind: result.ok ? "system" : "error",
        text: `undo: ${result.message}`,
        source: "user",
      });
      await get().refreshMoml();
    } catch (e) {
      set({ lastError: (e as Error).message });
    } finally {
      set({ isLoading: false });
    }
  },

  redo: async () => {
    const id = get().sessionId;
    if (!id) return;
    set({ isLoading: true, lastError: null });
    try {
      const result = await agentApi.redo(id);
      get().appendChat({
        kind: result.ok ? "system" : "error",
        text: `redo: ${result.message}`,
        source: "user",
      });
      await get().refreshMoml();
    } catch (e) {
      set({ lastError: (e as Error).message });
    } finally {
      set({ isLoading: false });
    }
  },

  appendChat: (entry, source) =>
    set((s) => {
      // Safety cap: very long iterative builds can otherwise grow
      // the chat array into the thousands of entries, which causes
      // O(n^2) array-copy churn here and freezes the React panel.
      // 800 lines is generous for any reasonable conversation and
      // still keeps the recent context visible.
      const MAX_CHAT_ENTRIES = 800;
      const next = [
        ...s.chat,
        {
          ...entry,
          source: entry.source ?? source ?? "user",
          id: _nextChatId++,
          timestamp: Date.now(),
        },
      ];
      if (next.length > MAX_CHAT_ENTRIES) {
        return { chat: next.slice(next.length - MAX_CHAT_ENTRIES) };
      }
      return { chat: next };
    }),

  clearChat: () => set({ chat: [] }),

  enterComposite: async (name: string) => {
    set((s) => ({
      canvasPath: [...s.canvasPath, name],
      selectedNodeId: null,
    }));
    await get().refreshGraph();
  },

  exitComposite: async () => {
    const path = get().canvasPath;
    if (path.length === 0) return;
    set({ canvasPath: path.slice(0, -1), selectedNodeId: null });
    await get().refreshGraph();
  },

  setCanvasPath: async (path: string[]) => {
    set({ canvasPath: [...path], selectedNodeId: null });
    await get().refreshGraph();
  },
  });
});

function prettyArgs(args: Record<string, unknown>): string {
  const keys = Object.keys(args);
  if (keys.length === 0) return "";
  return keys
    .map((k) => {
      const v = args[k];
      const s = typeof v === "string" ? `"${v}"` : JSON.stringify(v);
      return `${k}=${s}`;
    })
    .join(", ");
}
