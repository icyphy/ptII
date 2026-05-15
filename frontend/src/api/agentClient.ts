// Thin REST client for the Ptolemy II agent backend.
//
// All endpoints share a small shape:
//   { ok: boolean, message: string, data: { ... } }
// except GET /moml which returns text/plain and the list/summary
// endpoints which embed their payload directly.

const API_BASE = "/api/v1";

export interface SessionSummary {
  id: string;
  createdAt: string;
  state: string;
  hasModel: boolean;
  modelName?: string;
  modelClass?: string;
}

export interface AgentResultJson {
  ok: boolean;
  message: string;
  data?: Record<string, unknown>;
}

export interface ProbeData {
  id: string;
  actor: string;
  port: string;
  time: number[];
  values: Array<number | string | null>;
}

export interface SignalsPayload {
  probes: ProbeData[];
}

export interface AgentTraceStep {
  index: number;
  kind: "thought" | "tool_call" | "tool_result" | "final" | "error";
  name: string;
  arguments: Record<string, unknown>;
  text: string;
  timestamp: number;
}

export interface AgentTrace {
  completed: boolean;
  success: boolean;
  finalReply: string;
  steps: AgentTraceStep[];
}

export interface AgentChatResponse {
  trace: AgentTrace;
  session: SessionSummary;
  signals: SignalsPayload | null;
}

export type AgentStreamEvent =
  | { event: "started" }
  | { event: "step"; step: AgentTraceStep }
  | {
      event: "done";
      trace: AgentTrace;
      session: SessionSummary;
      signals: SignalsPayload | null;
    }
  | { event: "error"; message: string };

export interface ToolDescriptor {
  name: string;
  description: string;
  parameters: Record<string, unknown>;
}

export interface AgentStatus {
  llm: {
    provider: string;
    available: boolean;
    baseUrl?: string;
    model?: string;
  };
  tools: ToolDescriptor[];
}

export interface PortDescriptor {
  name: string;
  id: string;
  multiport: boolean;
}

export interface ParameterDescriptor {
  name: string;
  value: string;
}

export interface NodeDescriptor {
  id: string;
  className: string;
  displayName: string;
  position: { x: number; y: number };
  inputs: PortDescriptor[];
  outputs: PortDescriptor[];
  parameters: ParameterDescriptor[];
}

export interface EdgeDescriptor {
  id: string;
  source: string;
  sourceHandle: string;
  target: string;
  targetHandle: string;
}

export interface DirectorDescriptor {
  id: string;
  className: string;
}

export interface GraphPayload {
  topName?: string;
  topClass?: string;
  nodes: NodeDescriptor[];
  edges: EdgeDescriptor[];
  directors: DirectorDescriptor[];
  /** Composite path actually resolved (may differ from request on error). */
  path?: string[];
  /** True when the resolved level is a child composite, not the root. */
  isComposite?: boolean;
  pathError?: string;
}

export interface LibraryEntry {
  className: string;
  displayName: string;
  category: string;
  description: string;
  inputs: string[];
  outputs: string[];
  parameters: Array<{ name: string; default: string; description: string }>;
}

async function request<T>(
  path: string,
  init?: RequestInit,
  expectText = false
): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers:
      init?.body && !(init.body instanceof FormData)
        ? { "Content-Type": "application/json" }
        : undefined,
    ...init,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => "");
    throw new Error(`HTTP ${res.status}: ${text || res.statusText}`);
  }
  if (expectText) {
    return (await res.text()) as unknown as T;
  }
  return (await res.json()) as T;
}

export const agentApi = {
  health: () => request<{ ok: boolean; service: string }>("/health"),

  listSessions: () =>
    request<{ sessions: SessionSummary[] }>("/sessions"),

  createSession: () =>
    request<SessionSummary>("/sessions", { method: "POST" }),

  deleteSession: (id: string) =>
    request<AgentResultJson>(`/sessions/${id}`, { method: "DELETE" }),

  getSummary: (id: string) =>
    request<SessionSummary>(`/sessions/${id}`),

  loadFile: (id: string, path: string) =>
    request<AgentResultJson>(`/sessions/${id}/load`, {
      method: "POST",
      body: JSON.stringify({ path }),
    }),

  loadMoml: (id: string, moml: string) =>
    request<AgentResultJson>(`/sessions/${id}/load`, {
      method: "POST",
      body: JSON.stringify({ moml }),
    }),

  getMoml: (id: string) =>
    request<string>(`/sessions/${id}/moml`, undefined, true),

  applyChange: (id: string, moml: string) =>
    request<AgentResultJson>(`/sessions/${id}/change`, {
      method: "POST",
      body: JSON.stringify({ moml }),
    }),

  run: (id: string) =>
    request<AgentResultJson>(`/sessions/${id}/run`, { method: "POST" }),

  getSignals: (id: string) =>
    request<SessionSummary & { signals?: SignalsPayload }>(
      `/sessions/${id}/signals`
    ),

  agentStatus: () => request<AgentStatus>("/agent/status"),

  agentChat: (id: string, message: string, signal?: AbortSignal) =>
    request<AgentChatResponse>(`/sessions/${id}/agent/chat`, {
      method: "POST",
      body: JSON.stringify({ message }),
      signal,
    }),

  async *agentChatStream(
    id: string,
    message: string,
    signal?: AbortSignal,
  ): AsyncGenerator<AgentStreamEvent> {
    const res = await fetch(`${API_BASE}/sessions/${id}/agent/chat/stream`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ message }),
      signal,
    });
    if (!res.ok) {
      const text = await res.text().catch(() => "");
      throw new Error(`HTTP ${res.status}: ${text || res.statusText}`);
    }
    if (!res.body) {
      throw new Error("Agent stream returned no response body");
    }

    const reader = res.body.getReader();
    const decoder = new TextDecoder();
    let buffer = "";

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      let newline = buffer.indexOf("\n");
      while (newline >= 0) {
        const line = buffer.slice(0, newline).trim();
        buffer = buffer.slice(newline + 1);
        if (line) {
          yield JSON.parse(line) as AgentStreamEvent;
        }
        newline = buffer.indexOf("\n");
      }
    }

    const tail = buffer.trim();
    if (tail) {
      yield JSON.parse(tail) as AgentStreamEvent;
    }
  },

  callTool: (id: string, name: string, args: Record<string, unknown>) =>
    request<AgentResultJson>(`/sessions/${id}/tools/${name}`, {
      method: "POST",
      body: JSON.stringify(args),
    }),

  getGraph: (id: string, path: string[] = []) => {
    const qs = path.length
      ? `?path=${encodeURIComponent(path.join("/"))}`
      : "";
    return request<GraphPayload>(`/sessions/${id}/graph${qs}`);
  },

  getLibrary: () =>
    request<{ entries: LibraryEntry[] }>("/agent/library"),

  undo: (id: string) =>
    request<AgentResultJson>(`/sessions/${id}/undo`, { method: "POST" }),

  redo: (id: string) =>
    request<AgentResultJson>(`/sessions/${id}/redo`, { method: "POST" }),

  saveModel: (
    id: string,
    options?: { path?: string; openVergil?: boolean },
  ) =>
    request<
      AgentResultJson & {
        path?: string;
        vergilLaunched?: boolean;
        vergilError?: string;
      }
    >(`/sessions/${id}/save`, {
      method: "POST",
      body: JSON.stringify(options ?? {}),
    }),
};
