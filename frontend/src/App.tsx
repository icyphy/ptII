import { useEffect, useState } from "react";
import { AppHeader } from "./components/AppHeader";
import { BottomPanel } from "./components/BottomPanel";
import { ChatPanel } from "./components/ChatPanel";
import { ExamplesPanel } from "./components/ExamplesPanel";
import { Inspector } from "./components/Inspector";
import { LibraryPanel } from "./components/LibraryPanel";
import { ModelCanvas } from "./components/ModelCanvas";
import { useSessionStore } from "./state/sessionStore";

function useUndoRedoShortcuts() {
  const undo = useSessionStore((s) => s.undo);
  const redo = useSessionStore((s) => s.redo);
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const meta = e.ctrlKey || e.metaKey;
      if (!meta) return;
      if (e.key === "z" || e.key === "Z") {
        if (e.shiftKey) {
          e.preventDefault();
          void redo();
        } else {
          e.preventDefault();
          void undo();
        }
      } else if (e.key === "y" || e.key === "Y") {
        e.preventDefault();
        void redo();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [undo, redo]);
}

type LeftTab = "examples" | "components";
type RightTab = "chat" | "inspector";

export default function App() {
  useUndoRedoShortcuts();
  const [leftTab, setLeftTab] = useState<LeftTab>("examples");
  const [rightTab, setRightTab] = useState<RightTab>("chat");

  return (
    <div className="flex flex-col h-screen bg-ink-900 text-ink-100">
      <AppHeader />

      <main className="flex flex-1 min-h-0">
        {/* Left sidebar: Examples & Components */}
        <aside className="w-[280px] shrink-0 border-r border-ink-700/70 bg-ink-850 flex flex-col">
          <nav className="flex border-b border-ink-700/70 px-2">
            <button
              className={`tab ${leftTab === "examples" ? "tab-active" : ""}`}
              onClick={() => setLeftTab("examples")}
            >
              <BeakerIcon className="w-3.5 h-3.5" />
              Examples
            </button>
            <button
              className={`tab ${leftTab === "components" ? "tab-active" : ""}`}
              onClick={() => setLeftTab("components")}
            >
              <CubeIcon className="w-3.5 h-3.5" />
              Components
            </button>
          </nav>
          <div className="flex-1 min-h-0 overflow-hidden">
            {leftTab === "examples" ? <ExamplesPanel /> : <LibraryPanel />}
          </div>
        </aside>

        {/* Center: canvas + bottom panel */}
        <section className="flex-1 min-w-0 flex flex-col">
          <div className="flex-1 min-h-0">
            <ModelCanvas />
          </div>
          <BottomPanel />
        </section>

        {/* Right sidebar: Agent Chat / Inspector */}
        <aside className="w-[380px] shrink-0 border-l border-ink-700/70 bg-ink-850 flex flex-col">
          <nav className="flex border-b border-ink-700/70 px-2 shrink-0">
            <button
              className={`tab ${rightTab === "chat" ? "tab-active" : ""}`}
              onClick={() => setRightTab("chat")}
            >
              <ChatBubbleIcon className="w-3.5 h-3.5" />
              Agent Chat
            </button>
            <button
              className={`tab ${rightTab === "inspector" ? "tab-active" : ""}`}
              onClick={() => setRightTab("inspector")}
            >
              <InspectorIcon className="w-3.5 h-3.5" />
              Inspector
            </button>
          </nav>
          <div className="flex-1 min-h-0 overflow-hidden">
            {rightTab === "chat" ? <ChatPanel /> : <Inspector />}
          </div>
        </aside>
      </main>
    </div>
  );
}

function BeakerIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      <path d="M9 3v6.2L4.6 17a2 2 0 0 0 1.7 3h11.4a2 2 0 0 0 1.7-3L15 9.2V3" />
      <path d="M9 3h6" />
      <path d="M7.5 14h9" />
    </svg>
  );
}

function CubeIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      <path d="M12 3 3 7.5v9L12 21l9-4.5v-9L12 3Z" />
      <path d="M3 7.5 12 12l9-4.5" />
      <path d="M12 12v9" />
    </svg>
  );
}

function ChatBubbleIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      <path d="M21 12a8 8 0 0 1-8 8c-1.6 0-3.2-.5-4.5-1.4L3 21l1.4-5.5A8 8 0 1 1 21 12Z" />
    </svg>
  );
}

function InspectorIcon({ className }: { className?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      className={className}
    >
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68 1.65 1.65 0 0 0 10 3.17V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9c.38.22.83.35 1.27.35H21a2 2 0 0 1 0 4h-.09c-.44 0-.89.13-1.27.35Z" />
    </svg>
  );
}
