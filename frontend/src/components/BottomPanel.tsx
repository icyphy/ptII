import { useState } from "react";
import { ActivityPanel } from "./ActivityPanel";
import { MomlView } from "./MomlView";
import { PlotPanel } from "./PlotPanel";
import { useSessionStore } from "../state/sessionStore";

type Tab = "signals" | "moml" | "activity";

export function BottomPanel() {
  const [active,    setActive]    = useState<Tab>("signals");
  const [collapsed, setCollapsed] = useState(false);
  const signals       = useSessionStore((s) => s.signals);
  const activityCount = useSessionStore((s) => s.chat.filter((c) => c.source === "user").length);
  const momlLen       = useSessionStore((s) => s.moml.length);
  const probeCount    = signals?.probes?.length ?? 0;

  return (
    <div className={`border-t border-ink-700 bg-ink-850 flex flex-col
                     ${collapsed ? "h-9" : "h-[270px]"}`}>
      {/* Tab bar */}
      <div className="flex items-center h-9 px-2 border-b border-ink-700 shrink-0">
        <TabBtn label="Signals"      icon={<WaveIcon />}
                badge={probeCount > 0 ? String(probeCount) : undefined}
                active={active === "signals"}
                onClick={() => { setActive("signals"); setCollapsed(false); }} />
        <TabBtn label="MoML"         icon={<CodeIcon />}
                badge={momlLen > 0 ? `${(momlLen / 1024).toFixed(1)}k` : undefined}
                active={active === "moml"}
                onClick={() => { setActive("moml"); setCollapsed(false); }} />
        <TabBtn label="Activity"    icon={<ActivityIcon />}
                badge={activityCount > 0 ? String(activityCount) : undefined}
                active={active === "activity"}
                onClick={() => { setActive("activity"); setCollapsed(false); }} />
        <button className="btn-ghost ml-auto"
                onClick={() => setCollapsed((c) => !c)}
                title={collapsed ? "Expand panel" : "Collapse panel"}>
          {collapsed ? <ChevronUp className="w-3.5 h-3.5" />
                     : <ChevronDown className="w-3.5 h-3.5" />}
        </button>
      </div>

      {!collapsed && (
        <div className="flex-1 min-h-0 overflow-hidden">
          {active === "signals"  && <PlotPanel />}
          {active === "moml"     && <MomlView />}
          {active === "activity" && <ActivityPanel />}
        </div>
      )}
    </div>
  );
}

function TabBtn({
  label, icon, badge, active, onClick,
}: {
  label: string;
  icon: React.ReactNode;
  badge?: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button onClick={onClick}
            className={`tab ${active ? "tab-active" : ""}`}>
      <span className="w-3.5 h-3.5">{icon}</span>
      {label}
      {badge && (
        <span className={`ml-0.5 px-1.5 py-0.5 rounded-full text-[10px] font-mono
                          leading-none ${active
                            ? "bg-blue-100 text-blue-700"
                            : "bg-stone-100 text-stone-500"}`}>
          {badge}
        </span>
      )}
    </button>
  );
}

function WaveIcon() {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" className="w-full h-full">
    <path d="M3 12c2-6 4-6 6 0s4 6 6 0 4-6 6 0" />
  </svg>;
}
function CodeIcon() {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round" className="w-full h-full">
    <path d="m9 17-5-5 5-5M15 7l5 5-5 5" />
  </svg>;
}
function ActivityIcon() {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round" className="w-full h-full">
    <path d="M12 20V10M6 20v-4M18 20V4" />
  </svg>;
}
function ChevronUp({ className }: { className?: string }) {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round" className={className}>
    <path d="m6 15 6-6 6 6" />
  </svg>;
}
function ChevronDown({ className }: { className?: string }) {
  return <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
              strokeLinecap="round" strokeLinejoin="round" className={className}>
    <path d="m6 9 6 6 6-6" />
  </svg>;
}
