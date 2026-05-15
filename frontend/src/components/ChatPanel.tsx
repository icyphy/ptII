import { useEffect, useRef, useState } from "react";
import { ChatEntry, useSessionStore } from "../state/sessionStore";

export function ChatPanel() {
  const chat          = useSessionStore((s) => s.chat.filter((c) => c.source === "agent"));
  const isAgentBusy   = useSessionStore((s) => s.isAgentBusy);
  const agentProgress = useSessionStore((s) => s.agentProgress);
  const agentStatus   = useSessionStore((s) => s.agentStatus);
  const sendToAgent   = useSessionStore((s) => s.sendToAgent);
  const stopAgent     = useSessionStore((s) => s.stopAgent);
  const clearChat     = useSessionStore((s) => s.clearChat);
  const [draft, setDraft] = useState("");
  const [elapsedSec, setElapsedSec] = useState(0);
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: 1e9, behavior: "smooth" });
  }, [chat.length, isAgentBusy, agentProgress?.label]);

  useEffect(() => {
    if (!isAgentBusy || !agentProgress) {
      setElapsedSec(0);
      return;
    }
    const tick = () => {
      setElapsedSec(
        Math.max(0, Math.floor((Date.now() - agentProgress.startedAt) / 1000)),
      );
    };
    tick();
    const timer = window.setInterval(tick, 1000);
    return () => window.clearInterval(timer);
  }, [isAgentBusy, agentProgress]);

  const submit = () => {
    const text = draft.trim();
    if (!text || isAgentBusy) return;
    setDraft("");
    void sendToAgent(text);
  };

  const llmAvailable = agentStatus?.llm?.available ?? false;

  // Group consecutive tool_call + tool_result pairs into collapsible blocks.
  const grouped = groupEntries(chat);

  return (
    <div className="h-full flex flex-col bg-ink-950">
      {/* Toolbar */}
      <div className="px-3 pt-2 pb-1 flex items-center justify-between
                      border-b border-ink-700 shrink-0">
        <span className="section-title">Agent conversation</span>
        <div className="flex items-center gap-1.5">
          {!llmAvailable && (
            <span className="pill-warn text-[10px]">LLM offline</span>
          )}
          {isAgentBusy && (
            <button
              className="btn-ghost border-red-300/70 text-red-300 hover:bg-red-950/30"
              onClick={stopAgent}
              title="Stop the current AI generation"
            >
              Stop
            </button>
          )}
          {chat.length > 0 && (
            <button className="btn-ghost" onClick={clearChat}>Clear</button>
          )}
        </div>
      </div>

      {/* Messages */}
      <div ref={scrollRef}
           className="flex-1 min-h-0 overflow-y-auto px-3 py-2 space-y-1.5">
        {chat.length === 0 ? (
          <EmptyChat llmAvailable={llmAvailable} />
        ) : (
          grouped.map((item) => {
            if (item.type === "single") {
              return <MessageBubble key={item.entry.id} entry={item.entry} />;
            }
            return (
              <ToolGroup key={item.entries[0].id} entries={item.entries} />
            );
          })
        )}
        {isAgentBusy && (
          <div className="flex items-center gap-1.5 px-1 text-[11px] text-ink-400">
            <span className="inline-flex gap-1">
              {[0, 0.15, 0.3].map((delay, i) => (
                <span key={i} className="w-1 h-1 rounded-full bg-blue-400 animate-pulseDot"
                      style={{ animationDelay: `${delay}s` }} />
              ))}
            </span>
            <span>{agentProgress?.label ?? "Agent thinking…"}</span>
            <span className="font-mono text-ink-500">· {elapsedSec}s</span>
          </div>
        )}
      </div>

      {/* Composer */}
      <div className="p-2 border-t border-ink-700 shrink-0 flex gap-2">
        <textarea
          className="input resize-none flex-1 text-xs"
          rows={2}
          placeholder={
            llmAvailable
              ? 'e.g. "build a 1 Hz sine wave through Scale(3.0) and run"'
              : "LLM is offline — configure an API key to enable agent."
          }
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              submit();
            }
          }}
          disabled={isAgentBusy}
        />
        {isAgentBusy ? (
          <button
            className="btn-primary self-stretch px-3 bg-red-600 hover:bg-red-700"
            onClick={stopAgent}
          >
            Stop
          </button>
        ) : (
          <button
            className="btn-primary self-stretch px-3"
            onClick={submit}
            disabled={!draft.trim()}
          >
            Send
          </button>
        )}
      </div>
    </div>
  );
}

// ── Grouping logic ────────────────────────────────────────────────────────────

type GroupedItem =
  | { type: "single"; entry: ChatEntry }
  | { type: "tool_group"; entries: ChatEntry[] };

function groupEntries(entries: ChatEntry[]): GroupedItem[] {
  const result: GroupedItem[] = [];
  let i = 0;
  while (i < entries.length) {
    const e = entries[i];
    if (e.kind === "tool_call" || e.kind === "tool_result") {
      const group: ChatEntry[] = [];
      while (
        i < entries.length &&
        (entries[i].kind === "tool_call" || entries[i].kind === "tool_result")
      ) {
        group.push(entries[i]);
        i++;
      }
      result.push({ type: "tool_group", entries: group });
    } else {
      result.push({ type: "single", entry: e });
      i++;
    }
  }
  return result;
}

// ── Collapsible tool group ────────────────────────────────────────────────────

function ToolGroup({ entries }: { entries: ChatEntry[] }) {
  const [expanded, setExpanded] = useState(false);

  const calls = entries.filter((e) => e.kind === "tool_call");
  const results = entries.filter((e) => e.kind === "tool_result");
  const successCount = results.filter((e) =>
    e.text.includes("→ OK"),
  ).length;
  const failCount = results.filter((e) =>
    e.text.includes("→ FAIL"),
  ).length;

  return (
    <div className="rounded-md border border-ink-700 bg-ink-900/60 overflow-hidden">
      {/* Summary header — always visible, clickable */}
      <button
        onClick={() => setExpanded((v) => !v)}
        className="w-full flex items-center gap-2 px-2.5 py-1.5
                   text-left hover:bg-ink-800/50 transition-colors"
      >
        <ChevronIcon expanded={expanded} />
        <ToolIcon className="w-3 h-3 text-violet-400 shrink-0" />
        <span className="text-[11px] text-ink-200 truncate flex-1">
          {calls.length} tool call{calls.length > 1 ? "s" : ""}
        </span>
        <span className="flex items-center gap-1.5 text-[10px] font-mono shrink-0">
          {successCount > 0 && (
            <span className="text-green-400">{successCount} OK</span>
          )}
          {failCount > 0 && (
            <span className="text-red-400">{failCount} FAIL</span>
          )}
        </span>
      </button>

      {/* Expanded details */}
      {expanded && (
        <div className="border-t border-ink-700 px-2.5 py-1.5 space-y-1">
          {entries.map((e) => (
            <div key={e.id} className="flex items-start gap-1.5">
              <span
                className={`mt-0.5 w-1.5 h-1.5 rounded-full shrink-0 ${
                  e.kind === "tool_call"
                    ? "bg-violet-400"
                    : e.text.includes("→ FAIL")
                      ? "bg-red-400"
                      : "bg-green-400"
                }`}
              />
              <span className="text-[11px] font-mono text-ink-300 break-all leading-relaxed">
                {e.text}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ── Single message bubble ─────────────────────────────────────────────────────

function MessageBubble({ entry }: { entry: ChatEntry }) {
  const s = KIND_STYLES[entry.kind] ?? KIND_STYLES.system;
  return (
    <div className={`rounded-md border px-2.5 py-1.5 ${s.body}`}>
      <div className="flex items-center gap-1.5 mb-0.5">
        <span className={`inline-flex items-center gap-1 px-1.5 py-0.5
                         rounded text-[10px] font-medium ${s.badge}`}>
          {s.icon} {s.label}
        </span>
        <span className="text-[10px] text-ink-500 font-mono">
          {new Date(entry.timestamp).toLocaleTimeString()}
        </span>
      </div>
      <div className="text-[12px] break-words whitespace-pre-wrap text-ink-200">
        {entry.text}
      </div>
    </div>
  );
}

// ── Styles ────────────────────────────────────────────────────────────────────

interface KindStyle {
  badge: string;
  body: string;
  label: string;
  icon: React.ReactNode;
}

const KIND_STYLES: Record<string, KindStyle> = {
  user: {
    badge: "bg-stone-100 text-stone-700 border border-stone-200",
    body:  "bg-stone-50 border-stone-200",
    label: "You",
    icon:  <UserIcon className="w-3 h-3" />,
  },
  agent: {
    badge: "bg-blue-50 text-blue-700 border border-blue-200",
    body:  "bg-blue-50/60 border-blue-200",
    label: "Agent",
    icon:  <AgentIcon className="w-3 h-3" />,
  },
  system: {
    badge: "bg-stone-100 text-stone-500 border border-stone-200",
    body:  "bg-stone-50/60 border-stone-200",
    label: "System",
    icon:  <DotIcon className="w-3 h-3" />,
  },
  error: {
    badge: "bg-red-50 text-red-700 border border-red-200",
    body:  "bg-red-50/60 border-red-200",
    label: "Error",
    icon:  <ErrIcon className="w-3 h-3" />,
  },
};

// ── Icons ─────────────────────────────────────────────────────────────────────

function ChevronIcon({ expanded }: { expanded: boolean }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={`w-3 h-3 text-ink-400 shrink-0 transition-transform ${
        expanded ? "rotate-90" : ""
      }`}
    >
      <path d="m9 18 6-6-6-6" />
    </svg>
  );
}
function UserIcon({ className }: { className?: string }) {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" className={className}>
    <circle cx="12" cy="8" r="4" /><path d="M4 21c1-4 5-6 8-6s7 2 8 6" />
  </svg>;
}
function AgentIcon({ className }: { className?: string }) {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round" className={className}>
    <rect x="4" y="6" width="16" height="12" rx="3" />
    <path d="M9 12h.01M15 12h.01M12 3v3" />
  </svg>;
}
function ToolIcon({ className }: { className?: string }) {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round" className={className}>
    <path d="M14.7 6.3a4 4 0 0 0-5.4 5.4l-6 6 2.6 2.6 6-6a4 4 0 0 0 5.4-5.4l-2.3 2.3-1.6-1.6 2.3-2.3Z" />
  </svg>;
}
function ErrIcon({ className }: { className?: string }) {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round" className={className}>
    <circle cx="12" cy="12" r="9" /><path d="M12 8v4M12 16h.01" />
  </svg>;
}
function DotIcon({ className }: { className?: string }) {
  return <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
    <circle cx="12" cy="12" r="4" />
  </svg>;
}

function EmptyChat({ llmAvailable }: { llmAvailable: boolean }) {
  return (
    <div className="text-sm text-ink-300 space-y-2 px-1 pt-1">
      <p>
        Describe the model you want and the agent will build it step by step —
        or click an <strong>Example</strong> on the left for a one-click demo.
      </p>
      {!llmAvailable && (
        <p className="text-[11px] text-ink-400">
          (LLM is offline — configure <code>DEEPSEEK_API_KEY</code> or a
          compatible provider in the backend to enable natural-language control.)
        </p>
      )}
      <ul className="list-disc list-inside text-[11px] text-ink-300 space-y-0.5 pt-1">
        <li>"Add a Sinewave at (200, 200) and run 64 iterations."</li>
        <li>"Connect Sine.output to Gain.input, set Gain.factor to 3.5."</li>
        <li>"Build a simple RC low-pass filter."</li>
      </ul>
    </div>
  );
}
