import { DEMO_EXAMPLES, DemoExample } from "../examples/demoExamples";
import { useSessionStore } from "../state/sessionStore";

// ── Accent colour map ────────────────────────────────────────────────────────

type AccentKey = DemoExample["accentColor"];

const ACCENT: Record<
  AccentKey,
  { icon: string; badge: string; run: string; ring: string }
> = {
  blue:   { icon: "text-blue-600 bg-blue-50 border-blue-200",   badge: "text-blue-700 bg-blue-50 border-blue-200",   run: "text-blue-600",   ring: "ring-blue-300" },
  violet: { icon: "text-violet-600 bg-violet-50 border-violet-200", badge: "text-violet-700 bg-violet-50 border-violet-200", run: "text-violet-600", ring: "ring-violet-300" },
  teal:   { icon: "text-teal-600 bg-teal-50 border-teal-200",   badge: "text-teal-700 bg-teal-50 border-teal-200",   run: "text-teal-600",   ring: "ring-teal-300" },
  amber:  { icon: "text-amber-600 bg-amber-50 border-amber-200", badge: "text-amber-700 bg-amber-50 border-amber-200", run: "text-amber-600",  ring: "ring-amber-300" },
  rose:   { icon: "text-rose-600 bg-rose-50 border-rose-200",   badge: "text-rose-700 bg-rose-50 border-rose-200",   run: "text-rose-600",   ring: "ring-rose-300" },
};

// ── Panel ────────────────────────────────────────────────────────────────────

export function ExamplesPanel() {
  const isRunningDemo = useSessionStore((s) => s.isRunningDemo);
  const activeDemoId  = useSessionStore((s) => s.activeDemoId);
  const runExample    = useSessionStore((s) => s.runExample);

  return (
    <div className="flex flex-col h-full min-h-0">
      <div className="px-4 pt-4 pb-3 border-b border-ink-700">
        <div className="section-title">Examples</div>
        <p className="text-[11px] text-ink-400 mt-1 leading-snug">
          Click any card to build and run a fully-connected model end-to-end.
          Each demo wires actors explicitly so the canvas shows the dataflow graph.
        </p>
      </div>
      <div className="flex-1 min-h-0 overflow-y-auto p-3 space-y-2">
        {DEMO_EXAMPLES.map((demo) => (
          <DemoCard
            key={demo.id}
            demo={demo}
            isRunning={isRunningDemo && activeDemoId === demo.id}
            disabled={isRunningDemo}
            onRun={() => void runExample(demo)}
          />
        ))}
      </div>
    </div>
  );
}

// ── Card ─────────────────────────────────────────────────────────────────────

function DemoCard({
  demo,
  isRunning,
  disabled,
  onRun,
}: {
  demo: DemoExample;
  isRunning: boolean;
  disabled: boolean;
  onRun: () => void;
}) {
  const a = ACCENT[demo.accentColor];
  return (
    <button
      onClick={onRun}
      disabled={disabled}
      className={`w-full text-left rounded-lg border border-ink-700 bg-ink-950
                  px-3 py-2.5 transition-all group
                  hover:border-ink-600 hover:shadow-card
                  disabled:opacity-50 disabled:cursor-not-allowed
                  ${isRunning ? `ring-1 ${a.ring}` : ""}`}
    >
      <div className="flex items-start gap-2.5">
        {/* Icon chip */}
        <div className={`mt-0.5 w-7 h-7 rounded-md flex items-center justify-center
                         border ${a.icon} shrink-0`}>
          <DemoGlyph kind={demo.id} />
        </div>

        {/* Title block */}
        <div className="min-w-0 flex-1">
          <div className="flex items-baseline justify-between gap-2">
            <span className="text-[13px] font-semibold text-ink-100 truncate">
              {demo.title}
            </span>
            <span className={`text-[10px] font-medium px-1.5 py-0.5 rounded border ${a.badge} shrink-0`}>
              {demo.domain}
            </span>
          </div>
          <div className="text-[11px] text-ink-400 truncate">{demo.subtitle}</div>
        </div>
      </div>

      <p className="text-[11px] leading-snug text-ink-300 mt-2 line-clamp-2">
        {demo.description}
      </p>

      <div className="flex items-center justify-between mt-2">
        <span className="text-[10px] text-ink-500 font-mono">{demo.highlight}</span>
        <span className={`inline-flex items-center gap-1 text-[11px] font-medium
                          ${isRunning ? a.run : `text-ink-400 group-hover:${a.run}`}`}>
          {isRunning ? (
            <><Spinner className="w-3 h-3" /> Running…</>
          ) : (
            <><PlayIcon className="w-3 h-3" /> Run</>
          )}
        </span>
      </div>
    </button>
  );
}

// ── Icons ─────────────────────────────────────────────────────────────────────

function PlayIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="currentColor" className={className}>
      <path d="M8 5.5v13a1 1 0 0 0 1.55.83l10-6.5a1 1 0 0 0 0-1.66l-10-6.5A1 1 0 0 0 8 5.5Z" />
    </svg>
  );
}

function Spinner({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2"
         strokeLinecap="round" className={`${className} animate-spin`}>
      <path d="M21 12a9 9 0 1 1-3.2-6.9" />
    </svg>
  );
}

function DemoGlyph({ kind }: { kind: string }) {
  const cls = "w-4 h-4";
  switch (kind) {
    case "ramp":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
             strokeLinecap="round" className={cls}>
          <path d="M4 20 20 4" /><path d="M4 20h16" />
        </svg>
      );
    case "sine":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
             strokeLinecap="round" className={cls}>
          <path d="M3 12c2-6 4-6 6 0s4 6 6 0 4-6 6 0" />
        </svg>
      );
    case "pipeline":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
             strokeLinecap="round" strokeLinejoin="round" className={cls}>
          <path d="M3 12h4" />
          <path d="M7 6l10 6-10 6V6Z" />
          <path d="M17 12h4" />
        </svg>
      );
    case "adder":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
             strokeLinecap="round" className={cls}>
          <circle cx="12" cy="12" r="7" />
          <path d="M12 8.5v7M8.5 12h7" />
        </svg>
      );
    case "noise":
      return (
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
             strokeLinecap="round" className={cls}>
          <path d="M3 17c2 0 2-4 4-4s2 6 4 6 2-10 4-10 2 5 4 5" />
        </svg>
      );
    default:
      return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
                  className={cls}><circle cx="12" cy="12" r="6" /></svg>;
  }
}
