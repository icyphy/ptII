import { useEffect, useMemo, useState } from "react";
import { LibraryEntry } from "../api/agentClient";
import { useSessionStore } from "../state/sessionStore";

function groupByCategory(entries: LibraryEntry[]): Record<string, LibraryEntry[]> {
  const out: Record<string, LibraryEntry[]> = {};
  for (const e of entries) {
    const key = e.category || "Other";
    (out[key] ??= []).push(e);
  }
  return out;
}

const CATEGORY_ORDER = [
  "Directors", "Sources", "Math", "Sinks", "FlowControl",
  "Logic", "String", "Array", "Matrix", "Conversions",
  "SignalProcessing", "HigherOrderActors", "Random", "IO",
  "Communication", "DataStores", "DomainSpecific", "RealTime",
  "Utilities", "Library", "Other",
];

const CATEGORY_LABEL: Record<string, string> = {
  FlowControl:       "Flow Control",
  SignalProcessing:  "Signal Processing",
  HigherOrderActors: "Higher-Order",
  DomainSpecific:    "Domain Specific",
  RealTime:          "Real Time",
  DataStores:        "Data Stores",
};

const CATEGORY_STYLE: Record<string, { dot: string; label: string }> = {
  Sources:           { dot: "bg-blue-500",    label: "text-blue-700"    },
  Math:              { dot: "bg-violet-500",  label: "text-violet-700"  },
  Sinks:             { dot: "bg-rose-500",    label: "text-rose-700"    },
  FlowControl:       { dot: "bg-cyan-500",    label: "text-cyan-700"    },
  Logic:             { dot: "bg-orange-500",  label: "text-orange-700"  },
  String:            { dot: "bg-lime-500",    label: "text-lime-700"    },
  Directors:         { dot: "bg-amber-500",   label: "text-amber-700"   },
  Conversions:       { dot: "bg-emerald-500", label: "text-emerald-700" },
  Communication:     { dot: "bg-sky-500",     label: "text-sky-700"     },
  SignalProcessing:  { dot: "bg-fuchsia-500", label: "text-fuchsia-700" },
  HigherOrderActors: { dot: "bg-indigo-500",  label: "text-indigo-700"  },
  Array:             { dot: "bg-teal-500",    label: "text-teal-700"    },
  Matrix:            { dot: "bg-teal-600",    label: "text-teal-700"    },
  Random:            { dot: "bg-purple-500",  label: "text-purple-700"  },
  IO:                { dot: "bg-yellow-500",  label: "text-yellow-700"  },
  DomainSpecific:    { dot: "bg-pink-500",    label: "text-pink-700"    },
  RealTime:          { dot: "bg-red-500",     label: "text-red-700"     },
  DataStores:        { dot: "bg-stone-600",   label: "text-stone-700"   },
  Utilities:         { dot: "bg-stone-500",   label: "text-stone-600"   },
  Library:           { dot: "bg-stone-500",   label: "text-stone-600"   },
  Other:             { dot: "bg-stone-400",   label: "text-stone-600"   },
};

// Categories that should start expanded; everything else collapses by
// default so the panel is not overwhelming with the full ~350 actor set.
const DEFAULT_OPEN: ReadonlySet<string> = new Set([
  "Directors", "Sources", "Math", "Sinks", "FlowControl", "Logic",
]);

export function LibraryPanel() {
  const library        = useSessionStore((s) => s.library);
  const refreshLibrary = useSessionStore((s) => s.refreshLibrary);
  const [filter, setFilter] = useState("");
  const [collapsed, setCollapsed] = useState<Record<string, boolean>>({});

  useEffect(() => {
    if (library.length === 0) void refreshLibrary();
  }, [library.length, refreshLibrary]);

  const grouped = useMemo(() => {
    const q = filter.trim().toLowerCase();
    const list = q
      ? library.filter((e) =>
          e.className.toLowerCase().includes(q) ||
          e.displayName.toLowerCase().includes(q) ||
          e.description.toLowerCase().includes(q)
        )
      : library;
    return groupByCategory(list);
  }, [library, filter]);

  const orderedCategories = useMemo(
    () =>
      Object.keys(grouped).sort((a, b) => {
        const ai = CATEGORY_ORDER.indexOf(a);
        const bi = CATEGORY_ORDER.indexOf(b);
        return (ai < 0 ? 99 : ai) - (bi < 0 ? 99 : bi);
      }),
    [grouped]
  );

  const visibleTotal = useMemo(
    () => orderedCategories.reduce((n, c) => n + grouped[c].length, 0),
    [grouped, orderedCategories]
  );

  const filtering = filter.trim().length > 0;

  const isOpen = (cat: string) => {
    if (filtering) return true;
    if (cat in collapsed) return !collapsed[cat];
    return DEFAULT_OPEN.has(cat);
  };
  const toggle = (cat: string) =>
    setCollapsed((p) => ({ ...p, [cat]: isOpen(cat) }));

  const onDragStart = (ev: React.DragEvent, entry: LibraryEntry) => {
    ev.dataTransfer.setData("application/x-ptolemy-class",       entry.className);
    ev.dataTransfer.setData("application/x-ptolemy-displayname", entry.displayName);
    ev.dataTransfer.effectAllowed = "move";
  };

  return (
    <div className="flex flex-col h-full min-h-0">
      {/* Search */}
      <div className="px-3 pt-3 pb-2.5 border-b border-ink-700">
        <div className="flex items-baseline gap-2 mb-2">
          <div className="section-title">Components</div>
          <span className="text-[10px] text-ink-500 font-mono">
            {filtering
              ? `${visibleTotal} / ${library.length}`
              : `${library.length} total`}
          </span>
        </div>
        <div className="relative">
          <SearchIcon className="absolute left-2 top-1/2 -translate-y-1/2 w-3.5 h-3.5 text-ink-500" />
          <input
            className="input pl-7 text-xs"
            placeholder="Filter by name or class…"
            value={filter}
            onChange={(e) => setFilter(e.target.value)}
          />
        </div>
        <p className="text-[10px] text-ink-500 mt-1.5">
          Drag onto canvas to instantiate
        </p>
      </div>

      {/* List */}
      <div className="flex-1 min-h-0 overflow-y-auto p-3 space-y-3 text-xs">
        {orderedCategories.map((cat) => {
          const style = CATEGORY_STYLE[cat] ?? CATEGORY_STYLE.Other;
          const open = isOpen(cat);
          return (
            <div key={cat}>
              <button
                type="button"
                onClick={() => toggle(cat)}
                className={`w-full flex items-center gap-1.5 text-[10px] uppercase
                            tracking-[0.15em] font-semibold mb-1.5 ${style.label}
                            hover:opacity-80`}
              >
                <span className={`w-1.5 h-1.5 rounded-full ${style.dot}`} />
                <span>{CATEGORY_LABEL[cat] ?? cat}</span>
                <Caret open={open} />
                <span className="ml-auto text-stone-400 font-mono normal-case">
                  {grouped[cat].length}
                </span>
              </button>
              {open && (
                <div className="space-y-1">
                  {grouped[cat].map((entry) => (
                    <div
                      key={entry.className}
                      draggable
                      onDragStart={(e) => onDragStart(e, entry)}
                      title={entry.description || entry.className}
                      className="rounded-md border border-stone-200 px-2 py-1.5 bg-white
                                 cursor-grab hover:border-blue-300 hover:bg-blue-50/40
                                 shadow-card transition-colors"
                    >
                      <div className="text-stone-700 font-medium leading-tight">
                        {entry.displayName}
                      </div>
                      <div className="text-[9px] text-stone-400 font-mono truncate">
                        {shortClass(entry.className)}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          );
        })}
        {library.length === 0 && (
          <div className="text-stone-400 text-xs">No components loaded.</div>
        )}
        {filtering && visibleTotal === 0 && (
          <div className="text-stone-400 text-xs">
            No components match "{filter}".
          </div>
        )}
      </div>
    </div>
  );
}

function Caret({ open }: { open: boolean }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={`w-3 h-3 transition-transform ${open ? "" : "-rotate-90"}`}
    >
      <path d="M6 9l6 6 6-6" />
    </svg>
  );
}

function shortClass(fqcn: string): string {
  if (!fqcn) return "";
  const parts = fqcn.split(".");
  if (parts.length < 3) return fqcn;
  return `${parts[0]}…${parts.slice(-2).join(".")}`;
}

function SearchIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" className={className}>
      <circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" />
    </svg>
  );
}
