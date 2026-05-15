import { useEffect, useState } from "react";
import { NodeDescriptor } from "../api/agentClient";
import { useSessionStore } from "../state/sessionStore";

// Right sidebar: parameter editor when a node is selected, model summary otherwise.

function findSelected(nodes: NodeDescriptor[] | undefined, id: string | null) {
  if (!id || !nodes) return null;
  return nodes.find((n) => n.id === id) ?? null;
}

export function Inspector() {
  const graph        = useSessionStore((s) => s.graph);
  const selectedId   = useSessionStore((s) => s.selectedNodeId);
  const callTool     = useSessionStore((s) => s.callTool);
  const selectNode   = useSessionStore((s) => s.selectNode);
  const node = findSelected(graph?.nodes, selectedId);

  if (!node) return <ModelSummary />;
  return (
    <ParamEditor
      node={node}
      onCommit={(param, value) =>
        void callTool("set_parameter", { entity: node.id, parameter: param, value })
      }
      onDelete={() => {
        void callTool("delete", { name: node.id });
        selectNode(null);
      }}
      onDeselect={() => selectNode(null)}
    />
  );
}

// ── Parameter editor ─────────────────────────────────────────────────────────

function ParamEditor({
  node,
  onCommit,
  onDelete,
  onDeselect,
}: {
  node: NodeDescriptor;
  onCommit: (param: string, value: string) => void;
  onDelete: () => void;
  onDeselect: () => void;
}) {
  const [drafts, setDrafts] = useState<Record<string, string>>({});

  useEffect(() => {
    const init: Record<string, string> = {};
    node.parameters.forEach((p) => { init[p.name] = p.value; });
    setDrafts(init);
  }, [node.id, node.parameters]);

  const commit = (name: string) => {
    const val = drafts[name] ?? "";
    const orig = node.parameters.find((p) => p.name === name)?.value;
    if (val !== orig) onCommit(name, val);
  };

  return (
    <div className="flex flex-col h-full min-h-0">
      {/* Heading */}
      <div className="px-4 pt-4 pb-3 border-b border-ink-700">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <div className="section-title">Inspector</div>
            <div className="mt-1 text-[16px] font-semibold text-ink-100 truncate">
              {node.displayName}
            </div>
            <div className="text-[10px] text-ink-400 font-mono truncate">
              {node.className}
            </div>
          </div>
          <button className="btn-ghost text-red-600 hover:text-red-700 hover:bg-red-50"
                  onClick={onDelete} title="Delete entity">
            <TrashIcon className="w-3.5 h-3.5" />
            Delete
          </button>
        </div>
        <div className="mt-2.5 flex flex-wrap gap-1.5">
          <span className="pill-muted">{node.inputs.length} inputs</span>
          <span className="pill-muted">{node.outputs.length} outputs</span>
          <span className="pill-muted">{node.parameters.length} params</span>
        </div>
      </div>

      {/* Parameters */}
      <div className="flex-1 min-h-0 overflow-y-auto px-4 py-3 space-y-3">
        {node.parameters.length === 0 && (
          <p className="text-xs text-ink-400">No editable parameters.</p>
        )}
        {node.parameters.map((p) => (
          <label key={p.name} className="block">
            <span className="text-[11px] font-medium text-ink-300">{p.name}</span>
            <input
              className="input-mono mt-0.5"
              value={drafts[p.name] ?? ""}
              onChange={(e) => setDrafts((d) => ({ ...d, [p.name]: e.target.value }))}
              onBlur={() => commit(p.name)}
              onKeyDown={(e) => { if (e.key === "Enter") (e.target as HTMLInputElement).blur(); }}
            />
          </label>
        ))}

        {/* Port listing */}
        {(node.inputs.length > 0 || node.outputs.length > 0) && (
          <div className="pt-3 border-t border-ink-700">
            <div className="section-title mb-2">Ports</div>
            <div className="grid grid-cols-2 gap-3 text-[11px]">
              <PortList label="Inputs"  ports={node.inputs}  color="text-blue-600" />
              <PortList label="Outputs" ports={node.outputs} color="text-violet-600" />
            </div>
          </div>
        )}
      </div>

      {/* Footer */}
      <div className="px-4 py-2 border-t border-ink-700 flex items-center justify-between
                      text-[10px] text-ink-400">
        <span>Commits on blur or Enter</span>
        <button className="btn-ghost" onClick={onDeselect}>Deselect</button>
      </div>
    </div>
  );
}

function PortList({
  label, ports, color,
}: {
  label: string;
  ports: Array<{ id: string; name: string }>;
  color: string;
}) {
  return (
    <div>
      <div className={`text-[10px] uppercase tracking-wider font-semibold mb-1 ${color}`}>
        {label}
      </div>
      {ports.length === 0
        ? <div className="text-ink-400 italic">none</div>
        : <ul className="space-y-0.5">
            {ports.map((p) => (
              <li key={p.id} className="font-mono text-ink-200 truncate">{p.name}</li>
            ))}
          </ul>
      }
    </div>
  );
}

// ── Model summary (no selection) ─────────────────────────────────────────────

function ModelSummary() {
  const graph   = useSessionStore((s) => s.graph);
  const signals = useSessionStore((s) => s.signals);
  const nodes   = graph?.nodes ?? [];
  const edges   = graph?.edges ?? [];
  const probes  = signals?.probes ?? [];

  return (
    <div className="flex flex-col h-full min-h-0">
      <div className="px-4 pt-4 pb-3 border-b border-ink-700">
        <div className="section-title">Inspector</div>
        <div className="mt-1 text-[16px] font-semibold text-ink-100">Model summary</div>
        <div className="text-[11px] text-ink-400 mt-0.5">
          Click an actor on the canvas to inspect and edit its parameters.
        </div>
      </div>
      <div className="flex-1 min-h-0 overflow-y-auto p-4 space-y-3">
        <Stat label="Actors"          value={nodes.length.toString()} />
        <Stat label="Wires"           value={edges.length.toString()} />
        <Stat label="Recorded probes" value={probes.length.toString()} />
        <Stat label="Director"
              value={graph?.directors?.[0]?.className
                ? shortClass(graph.directors[0].className) : "—"}
              mono />

        {probes.length > 0 && (
          <div className="pt-3 border-t border-ink-700">
            <div className="section-title mb-2">Results</div>
            <ul className="space-y-2">
              {probes.map((p) => {
                const nums = p.values.filter(
                  (v): v is number => typeof v === "number" && isFinite(v),
                );
                if (nums.length === 0) return null;
                const min  = Math.min(...nums);
                const max  = Math.max(...nums);
                const mean = nums.reduce((a, b) => a + b, 0) / nums.length;
                const last = nums[nums.length - 1];
                return (
                  <li
                    key={p.id}
                    className="rounded-md border border-ink-700 bg-ink-800 px-2.5 py-2"
                  >
                    <div className="text-[11px] font-mono text-ink-200 mb-1.5">
                      {p.actor}.{p.port}
                    </div>
                    <div className="grid grid-cols-2 gap-x-4 gap-y-0.5 text-[10px]">
                      <ResultRow label="Samples" value={String(nums.length)} />
                      <ResultRow label="Min" value={fmtNum(min)} />
                      <ResultRow label="Max" value={fmtNum(max)} />
                      <ResultRow label="Mean" value={fmtNum(mean)} />
                      <ResultRow label="Last" value={fmtNum(last)} />
                    </div>
                  </li>
                );
              })}
            </ul>
          </div>
        )}

        {nodes.length > 0 && (
          <div className="pt-3 border-t border-ink-700">
            <div className="section-title mb-2">Actors</div>
            <ul className="space-y-1 text-[11px]">
              {nodes.map((n) => (
                <li key={n.id}
                    className="flex items-center justify-between gap-2
                               px-2 py-1 rounded-md bg-ink-800 border border-ink-700">
                  <span className="font-mono text-ink-200 truncate">{n.displayName}</span>
                  <span className="text-ink-500 truncate shrink-0">{shortClass(n.className)}</span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}

function ResultRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between">
      <span className="text-ink-400">{label}</span>
      <span className="text-ink-200 font-mono">{value}</span>
    </div>
  );
}

function fmtNum(v: number): string {
  if (!isFinite(v)) return "—";
  if (Math.abs(v) >= 1e4 || (Math.abs(v) < 0.01 && v !== 0))
    return v.toExponential(3);
  return v.toFixed(4);
}

function Stat({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex items-center justify-between text-sm">
      <span className="text-ink-300">{label}</span>
      <span className={`text-ink-100 ${mono ? "font-mono text-xs" : "font-semibold"}`}>
        {value}
      </span>
    </div>
  );
}

function shortClass(fqcn: string): string {
  if (!fqcn) return "";
  return fqcn.split(".").pop() ?? fqcn;
}

function TrashIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" strokeLinejoin="round" className={className}>
      <path d="M3 6h18M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2M5 6v14a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V6" />
    </svg>
  );
}
