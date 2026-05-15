import { useEffect, useRef } from "react";
import { useSessionStore } from "../state/sessionStore";

export function ActivityPanel() {
  const entries = useSessionStore((s) =>
    s.chat.filter((c) => c.source === "user"),
  );
  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: 1e9, behavior: "smooth" });
  }, [entries.length]);

  return (
    <div className="h-full flex flex-col bg-ink-950">
      <div className="px-3 pt-2 pb-1 flex items-center border-b border-ink-700">
        <span className="section-title">User activity log</span>
      </div>

      <div
        ref={scrollRef}
        className="flex-1 min-h-0 overflow-y-auto px-3 py-2 space-y-1"
      >
        {entries.length === 0 ? (
          <p className="text-[11px] text-ink-400 px-1 pt-1">
            No user operations yet. Load a model, run a simulation, or drag
            components onto the canvas.
          </p>
        ) : (
          entries.map((e) => (
            <div
              key={e.id}
              className="flex items-start gap-2 text-[11px] leading-relaxed"
            >
              <span className="text-ink-500 font-mono shrink-0 w-[60px] text-right">
                {new Date(e.timestamp).toLocaleTimeString()}
              </span>
              <KindDot kind={e.kind} />
              <span
                className={`text-ink-200 break-words ${
                  e.kind === "tool_call" ? "font-mono" : ""
                }`}
              >
                {e.text}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

function KindDot({ kind }: { kind: string }) {
  const cls =
    kind === "error"
      ? "bg-red-400"
      : kind === "tool_call"
        ? "bg-violet-400"
        : "bg-stone-400";
  return <span className={`mt-1.5 w-1.5 h-1.5 rounded-full shrink-0 ${cls}`} />;
}
