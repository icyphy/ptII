import { useCallback, useEffect, useState } from "react";
import { agentApi } from "../api/agentClient";
import { useSessionStore } from "../state/sessionStore";

// Two-row application header.
// Row 1: wordmark + session/LLM status + Undo / Redo / Run
// Row 2: model file path input (like the first version) + Load + model name

const DEFAULT_PATH = "ptolemy/domains/sdf/demo/FourierSeries/FourierSeries.xml";

export function AppHeader() {
  const {
    sessionId,
    summary,
    isLoading,
    isRunningDemo,
    lastError,
    ensureSession,
    loadDemoModel,
    runSimulation,
    undo,
    redo,
    agentStatus,
    refreshAgentStatus,
  } = useSessionStore();

  const [modelPath, setModelPath] = useState(DEFAULT_PATH);

  useEffect(() => { void ensureSession(); },       [ensureSession]);
  useEffect(() => { void refreshAgentStatus(); }, [refreshAgentStatus]);

  const busy     = isLoading || isRunningDemo;
  const hasModel = !!summary?.hasModel;
  const state    = summary?.state ?? "—";

  const handleSave = useCallback(async () => {
    if (!sessionId) return;
    try {
      const moml = await agentApi.getMoml(sessionId);
      const blob = new Blob([moml], { type: "application/xml;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${summary?.modelName ?? "model"}.xml`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch {
      /* best-effort */
    }
  }, [sessionId, summary?.modelName]);

  const handleOpenVergil = useCallback(async () => {
    if (!sessionId) return;
    try {
      const result = await agentApi.saveModel(sessionId, { openVergil: true });
      if (!result.ok) {
        useSessionStore.getState().appendChat({
          kind: "error",
          text: `Vergil: ${result.message}`,
          source: "user",
        });
      } else {
        useSessionStore.getState().appendChat({
          kind: "system",
          text: `Model saved to ${result.path ?? "disk"}${
            result.vergilLaunched ? " — Vergil launched" : ""
          }${result.vergilError ? ` (Vergil error: ${result.vergilError})` : ""}`,
          source: "user",
        });
      }
    } catch (e) {
      useSessionStore.getState().appendChat({
        kind: "error",
        text: `Vergil: ${(e as Error).message}`,
        source: "user",
      });
    }
  }, [sessionId]);

  return (
    <header className="shrink-0 border-b border-ink-700 bg-ink-950">
      {/* ── Row 1 ─ brand + status + controls ── */}
      <div className="flex items-center justify-between gap-4 px-5 h-13 py-2.5">
        <div className="flex items-center gap-3">
          <Logo />
          <div className="leading-tight">
            <div className="text-[15px] font-semibold text-ink-100 tracking-tight">
              Ptolemy II
              <span className="text-ink-400 font-normal"> · Auto-Modeling Agent</span>
            </div>
            <div className="text-[11px] text-ink-400">
              Heterogeneous concurrent model-based design
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2 flex-wrap">
          <SessionPill sessionId={sessionId} state={state} />
          <LLMPill status={agentStatus} />

          {lastError && (
            <span className="pill-bad max-w-[240px] truncate" title={lastError}>
              <WarnIcon className="w-3 h-3 shrink-0" />
              {lastError}
            </span>
          )}

          <div className="w-px h-5 bg-ink-700 mx-1" />

          <button className="btn" disabled={busy || !hasModel} onClick={() => void undo()}
                  title="Undo (Ctrl+Z)">
            <UndoIcon className="w-3.5 h-3.5" /> Undo
          </button>
          <button className="btn" disabled={busy || !hasModel} onClick={() => void redo()}
                  title="Redo (Ctrl+Y)">
            <RedoIcon className="w-3.5 h-3.5" /> Redo
          </button>
          <button className="btn-primary" disabled={busy || !hasModel}
                  onClick={() => void runSimulation()}>
            <PlayIcon className="w-3.5 h-3.5" />
            Run
          </button>
          <button className="btn" disabled={busy || !hasModel}
                  onClick={() => void handleSave()}
                  title="Save model as MoML XML (Vergil-compatible)">
            <SaveIcon className="w-3.5 h-3.5" /> Save
          </button>
          <button className="btn" disabled={busy || !hasModel}
                  onClick={() => void handleOpenVergil()}
                  title="Save to disk and open in Ptolemy II Vergil GUI">
            <VergilIcon className="w-3.5 h-3.5" /> Vergil
          </button>
        </div>
      </div>

      {/* ── Row 2 ─ model file loading ── */}
      <div className="flex items-center gap-2 px-5 py-1.5 bg-ink-850 border-t border-ink-700">
        <span className="text-[11px] text-ink-400 shrink-0">Model file:</span>
        <input
          className="flex-1 bg-ink-950 border border-ink-700 rounded px-2 py-1
                     font-mono text-[11px] text-ink-200 placeholder:text-ink-500
                     focus:outline-none focus:border-accent/50 focus:ring-1 focus:ring-accent/20
                     min-w-0"
          value={modelPath}
          onChange={(e) => setModelPath(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") void loadDemoModel(modelPath);
          }}
          placeholder="path/to/model.xml  (relative to $PTII)"
          title="Path relative to the Ptolemy II root directory"
        />
        <button
          className="btn shrink-0"
          disabled={busy || !sessionId}
          onClick={() => void loadDemoModel(modelPath)}
        >
          <FolderIcon className="w-3.5 h-3.5" /> Load
        </button>
        {hasModel && summary?.modelName && (
          <span className="text-[11px] text-ink-400 shrink-0 font-mono truncate max-w-[180px]"
                title={summary.modelName}>
            ✓ {summary.modelName}
          </span>
        )}
        <span className="text-[10px] text-ink-500 shrink-0">
          session: <span className="font-mono">{sessionId ?? "—"}</span>
        </span>
      </div>
    </header>
  );
}

// ── Status pills ──────────────────────────────────────────────────────────────

function SessionPill({ sessionId, state }: { sessionId: string | null; state: string }) {
  const cls =
    state === "ERROR"    ? "pill-bad"  :
    state === "RUNNING"  ? "pill-warn" :
    (state === "FINISHED" || state === "READY") ? "pill-ok" : "pill-muted";
  return (
    <span className={cls}>
      <Dot pulsing={state === "RUNNING"} />
      {sessionId ? "session" : "no session"}
      <span className="font-mono text-[10px] opacity-70">· {state}</span>
    </span>
  );
}

function LLMPill({ status }: { status: ReturnType<typeof useSessionStore.getState>["agentStatus"] }) {
  if (!status) return <span className="pill-muted"><Dot />LLM <span className="text-[10px] opacity-60">…</span></span>;
  const live = status.llm.available;
  return (
    <span className={live ? "pill-accent" : "pill-muted"}
          title={live
            ? `${status.llm.provider}${status.llm.model ? " / " + status.llm.model : ""}`
            : "No LLM configured — examples and manual editing still work."}>
      <Dot />
      LLM
      <span className="text-[10px] font-mono opacity-70">
        · {live ? status.llm.provider : "offline"}
      </span>
    </span>
  );
}

function Dot({ pulsing }: { pulsing?: boolean }) {
  return (
    <span className={`inline-block w-1.5 h-1.5 rounded-full bg-current
                      ${pulsing ? "animate-pulseDot" : ""}`} />
  );
}

// ── Icons ─────────────────────────────────────────────────────────────────────

function Logo() {
  return (
    <svg viewBox="0 0 32 32" className="w-7 h-7 shrink-0" aria-hidden="true">
      <rect x="2" y="2" width="28" height="28" rx="8"
            fill="#2563eb" opacity="0.12" />
      <path d="M8 22 Q12 8 16 22 T24 22" fill="none"
            stroke="#2563eb" strokeWidth="2.2" strokeLinecap="round" />
      <circle cx="8"  cy="22" r="2.2" fill="#2563eb" />
      <circle cx="16" cy="22" r="2.2" fill="#2563eb" />
      <circle cx="24" cy="22" r="2.2" fill="#2563eb" />
    </svg>
  );
}
function PlayIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
      <path d="M8 5.5v13a1 1 0 0 0 1.55.83l10-6.5a1 1 0 0 0 0-1.66l-10-6.5A1 1 0 0 0 8 5.5Z" />
    </svg>
  );
}
function UndoIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M9 14 4 9l5-5" /><path d="M4 9h9a7 7 0 1 1 0 14h-4" />
    </svg>
  );
}
function RedoIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="m15 14 5-5-5-5" /><path d="M20 9h-9a7 7 0 1 0 0 14h4" />
    </svg>
  );
}
function WarnIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" className={className}>
      <circle cx="12" cy="12" r="9" /><path d="M12 8v4M12 16h.01" />
    </svg>
  );
}
function VergilIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" className={className}>
      <rect x="2" y="3" width="20" height="14" rx="2" />
      <path d="M8 21h8M12 17v4" />
    </svg>
  );
}
function SaveIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
      <polyline points="17 21 17 13 7 13 7 21" />
      <polyline points="7 3 7 8 15 8" />
    </svg>
  );
}
function FolderIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
    </svg>
  );
}
