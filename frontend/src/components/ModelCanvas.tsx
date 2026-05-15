import { useCallback, useEffect, useMemo, useRef } from "react";
import ReactFlow, {
  Background,
  BackgroundVariant,
  Connection,
  Controls,
  Edge,
  Handle,
  MarkerType,
  MiniMap,
  Node,
  NodeProps,
  OnSelectionChangeParams,
  Position,
  ReactFlowInstance,
  useEdgesState,
  useNodesState,
} from "reactflow";
import "reactflow/dist/style.css";
import { NodeDescriptor } from "../api/agentClient";
import { useSessionStore } from "../state/sessionStore";
import { layoutNodes, isLayoutCluttered } from "../utils/autoLayout";

// ── Actor node ────────────────────────────────────────────────────────────────
//
// PORT GEOMETRY CONTRACT
// ─────────────────────
// Every handle's `top` is computed as:
//   HEADER_H + PORT_AREA_PADDING_TOP + portIndex * PORT_ROW_H + PORT_ROW_H / 2
//
// The DOM renders each port row as a <div> with `height: PORT_ROW_H px`.
// Both the DOM layout and the handle math share the same constants, so
// handles are always perfectly centred on their label row regardless of
// how many inputs vs outputs the node has.

const HEADER_H           = 58;   // px  — fixed header height
const PORT_ROW_H         = 22;   // px  — height of each port label row
const PORT_AREA_PAD_TOP  = 6;    // px  — gap above first port row
const PORT_AREA_PAD_BOT  = 6;    // px  — gap below last port row
const NODE_WIDTH         = 176;  // px  — default node width
const COMPOSITE_WIDTH    = 196;  // px  — composite nodes are slightly wider

/** Absolute `top` (px from node root) for the centre of port row `idx`.
 *  Handles are direct children of the root div so this value is correct. */
function portTop(idx: number): number {
  return HEADER_H + PORT_AREA_PAD_TOP + idx * PORT_ROW_H + PORT_ROW_H / 2;
}

interface ActorNodeData {
  label: string;
  className: string;
  inputs:  { id: string; name: string }[];
  outputs: { id: string; name: string }[];
  isComposite?: boolean;
  isBoundary?: boolean;
  boundaryRole?: "input" | "output" | "io";
}

// ─── Category palette ────────────────────────────────────────────────────────
// Each category gets a unique accent colour used for:
//   • the 3 px left stripe
//   • the tiny category badge text
//   • input handle dot colour (output always violet)

interface Accent {
  stripe: string;      // CSS hex for the left stripe
  badge: string;       // badge background (tailwind class)
  badgeText: string;   // badge text (tailwind class)
  label: string;       // category name shown in badge
  inputDot: string;    // handle dot colour (hex)
}

function classifyAccent(fqcn: string): Accent {
  const c = (fqcn ?? "").toLowerCase();
  if (c.includes("typedcompositeactor") || c.endsWith(".compositeactor"))
    return { stripe: "#6366f1", badge: "bg-indigo-100", badgeText: "text-indigo-700",
             label: "Subsystem", inputDot: "#6366f1" };
  if (c.includes("director"))
    return { stripe: "#f59e0b", badge: "bg-amber-100", badgeText: "text-amber-700",
             label: "Director",  inputDot: "#f59e0b" };
  if (c.endsWith(".ramp") || c.endsWith(".sinewave") || c.endsWith(".const") ||
      c.endsWith(".gaussian") || c.endsWith(".pulse") || c.endsWith(".counter") ||
      c.endsWith(".clock"))
    return { stripe: "#3b82f6", badge: "bg-blue-100", badgeText: "text-blue-700",
             label: "Source",   inputDot: "#3b82f6" };
  if (c.endsWith(".addsubtract") || c.endsWith(".scale") ||
      c.endsWith(".multiplydivide") || c.endsWith(".integrator") ||
      c.endsWith(".derivative") || c.endsWith(".expression") ||
      c.includes(".fft") || c.includes(".math") || c.includes("trigfunction") ||
      c.includes("unarymath") || c.endsWith(".sigmoid"))
    return { stripe: "#8b5cf6", badge: "bg-violet-100", badgeText: "text-violet-700",
             label: "Math",     inputDot: "#8b5cf6" };
  if (c.endsWith(".recorder") || c.endsWith(".discard") ||
      c.includes(".plotter") || c.includes(".display"))
    return { stripe: "#ef4444", badge: "bg-red-100", badgeText: "text-red-700",
             label: "Sink",     inputDot: "#ef4444" };
  if (c.includes(".filter") || c.includes(".iir") || c.includes(".fir") ||
      c.includes(".butterworth"))
    return { stripe: "#06b6d4", badge: "bg-cyan-100", badgeText: "text-cyan-700",
             label: "Filter",   inputDot: "#06b6d4" };
  return   { stripe: "#6b7280", badge: "bg-stone-100", badgeText: "text-stone-600",
             label: "Actor",    inputDot: "#6b7280" };
}

function isCompositeClass(fqcn: string): boolean {
  const c = (fqcn ?? "").toLowerCase();
  return c.includes("typedcompositeactor") || c.endsWith(".compositeactor");
}

function shortClass(fqcn: string): string {
  if (!fqcn) return "";
  return fqcn.split(".").pop() ?? fqcn;
}

// ─── Boundary pseudo-node ────────────────────────────────────────────────────
// Rendered when the user drills into a composite. Shows the composite's
// I/O ports as flat "terminal block" nodes on the canvas edges.

function BoundaryNode({ data }: { data: ActorNodeData }) {
  const isIn = data.boundaryRole === "input";
  return (
    <div
      style={{
        width: 110,
        background: isIn ? "#eef2ff" : "#f0fdf4",
        border: `1.5px dashed ${isIn ? "#818cf8" : "#4ade80"}`,
        borderRadius: 6,
        padding: "6px 8px",
        fontFamily: "ui-monospace, monospace",
      }}
    >
      {/* direction label */}
      <div style={{
        fontSize: 9, fontWeight: 700, letterSpacing: "0.08em",
        textTransform: "uppercase",
        color: isIn ? "#4f46e5" : "#16a34a",
        marginBottom: 2,
      }}>
        {isIn ? "▶ IN" : "OUT ▶"}
      </div>
      {/* port name */}
      <div style={{ fontSize: 11, fontWeight: 600, color: "#1e1b4b", lineHeight: 1.3 }}
           title={data.label}>
        {data.label}
      </div>

      {/* handles — use default top:50% from ReactFlow */}
      {data.outputs.map((p) => (
        <Handle key={`b-out-${p.id}`} type="source" position={Position.Right} id={p.id}
          style={{ right: -6, background: "#4f46e5", width: 10, height: 10, borderRadius: 2 }} />
      ))}
      {data.inputs.map((p) => (
        <Handle key={`b-in-${p.id}`} type="target" position={Position.Left} id={p.id}
          style={{ left: -6, background: "#16a34a", width: 10, height: 10, borderRadius: 2 }} />
      ))}
    </div>
  );
}

// ─── Main actor node ─────────────────────────────────────────────────────────

function ActorNode({ data }: NodeProps<ActorNodeData>) {
  if (data.isBoundary) return <BoundaryNode data={data} />;

  const accent    = classifyAccent(data.className);
  const composite = data.isComposite ?? isCompositeClass(data.className);
  const numRows   = Math.max(data.inputs.length, data.outputs.length, 1);
  const nodeH     = HEADER_H + PORT_AREA_PAD_TOP + numRows * PORT_ROW_H + PORT_AREA_PAD_BOT;
  const nodeW     = composite ? COMPOSITE_WIDTH : NODE_WIDTH;

  // KEY INSIGHT: the outer root div must NOT have overflow:hidden because
  // handles must visually protrude outside the node boundary. Instead we
  // use an inner "card" div that carries overflow:hidden + border-radius
  // for visual clipping, while handles are direct children of the root
  // so their top/left/right are resolved against the root (position:relative).

  const inputHandleStyle = (idx: number): React.CSSProperties => ({
    position: "absolute",
    top: portTop(idx),
    left: -6,
    width: 10, height: 10,
    borderRadius: "50%",
    background: accent.inputDot,
    border: "2px solid #fff",
    boxShadow: `0 0 0 1.5px ${accent.inputDot}44`,
    transform: "none",     // defeat ReactFlow's built-in translate
  });

  const outputHandleStyle = (idx: number): React.CSSProperties => ({
    position: "absolute",
    top: portTop(idx),
    right: -6,
    width: 10, height: 10,
    borderRadius: "50%",
    background: "#8b5cf6",
    border: "2px solid #fff",
    boxShadow: "0 0 0 1.5px #8b5cf644",
    transform: "none",
  });

  return (
    // Outer root: overflow VISIBLE so handles poke out of the card edges
    <div
      style={{
        width: nodeW,
        height: nodeH,
        position: "relative",
        overflow: "visible",    // ← must NOT be hidden
        fontFamily: "system-ui, -apple-system, sans-serif",
        cursor: composite ? "pointer" : "default",
      }}
      title={composite ? "Double-click to enter subsystem" : undefined}
    >
      {/* ── Visual card (carries border/shadow/radius/overflow-clipping) ── */}
      <div style={{
        position: "absolute",
        inset: 0,
        borderRadius: 8,
        overflow: "hidden",     // clips visuals only (no handles inside)
        background: composite ? "#f5f3ff" : "#ffffff",
        border: composite ? "1.5px dashed #a5b4fc" : "1px solid #e5e7eb",
        boxShadow: "0 1px 4px rgba(0,0,0,0.08), 0 0 0 0.5px rgba(0,0,0,0.04)",
      }}>
        {/* Left accent stripe */}
        <div style={{
          position: "absolute", left: 0, top: 0, bottom: 0, width: 3,
          background: accent.stripe,
        }} />

        {/* Header */}
        <div style={{
          position: "absolute", left: 3, right: 0, top: 0, height: HEADER_H,
          background: composite ? "rgba(238,242,255,0.92)" : "rgba(249,250,251,0.92)",
          borderBottom: "1px solid rgba(0,0,0,0.07)",
          padding: "7px 10px 6px 10px",
          display: "flex", flexDirection: "column", gap: 1,
        }}>
          {/* category + drill-in badge */}
          <div style={{ display: "flex", alignItems: "center", gap: 5 }}>
            <span style={{
              fontSize: 9, fontWeight: 700, letterSpacing: "0.07em",
              textTransform: "uppercase",
              padding: "1px 5px", borderRadius: 3,
              background: composite ? "#e0e7ff" : "transparent",
              color: composite ? "#4338ca" : accent.stripe,
            }}>
              {accent.label}
            </span>
            {composite && (
              <span style={{ fontSize: 9, color: "#818cf8", marginLeft: "auto" }}>
                ⤵ open
              </span>
            )}
          </div>
          {/* name */}
          <div style={{
            fontSize: 13, fontWeight: 700, lineHeight: 1.25,
            color: composite ? "#312e81" : "#111827",
            overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap",
          }} title={data.label}>
            {data.label}
          </div>
          {/* class */}
          <div style={{
            fontSize: 9, color: "#9ca3af",
            fontFamily: "ui-monospace, monospace",
            overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap",
          }} title={data.className}>
            {composite ? "TypedCompositeActor" : shortClass(data.className)}
          </div>
        </div>

        {/* Port label area — visual only, no handles here */}
        <div style={{
          position: "absolute",
          left: 3, right: 0, top: HEADER_H,
          paddingTop: PORT_AREA_PAD_TOP, paddingBottom: PORT_AREA_PAD_BOT,
          background: composite ? "rgba(245,243,255,0.5)" : "transparent",
        }}>
          {Array.from({ length: numRows }).map((_, idx) => {
            const inp = data.inputs[idx];
            const out = data.outputs[idx];
            return (
              <div key={idx} style={{
                display: "flex", height: PORT_ROW_H,
                alignItems: "center",
              }}>
                {/* Input label (left half) */}
                <div style={{
                  flex: 1, minWidth: 0,
                  paddingLeft: 10,
                  display: "flex", alignItems: "center",
                }}>
                  {inp && (
                    <span style={{
                      fontSize: 10, color: "#6b7280",
                      fontFamily: "ui-monospace, monospace",
                      overflow: "hidden", textOverflow: "ellipsis",
                      whiteSpace: "nowrap",
                    }}>
                      {inp.name}
                    </span>
                  )}
                </div>
                {/* Output label (right half) */}
                <div style={{
                  flex: 1, minWidth: 0,
                  paddingRight: 10,
                  display: "flex", alignItems: "center",
                  justifyContent: "flex-end",
                }}>
                  {out && (
                    <span style={{
                      fontSize: 10, color: "#6b7280",
                      fontFamily: "ui-monospace, monospace",
                      overflow: "hidden", textOverflow: "ellipsis",
                      whiteSpace: "nowrap",
                    }}>
                      {out.name}
                    </span>
                  )}
                </div>
              </div>
            );
          })}

          {/* Column divider */}
          {data.inputs.length > 0 && data.outputs.length > 0 && (
            <div style={{
              position: "absolute",
              left: "50%", top: 2, bottom: 2, width: 1,
              background: composite ? "#c7d2fe" : "#f0f0f0",
              pointerEvents: "none",
            }} />
          )}
        </div>
      </div>{/* end visual card */}

      {/* ── Handles — direct children of root (overflow:visible) ──
           top: portTop(idx) is measured from the root, which is correct
           because Handles are not inside any sub-positioned div. */}
      {data.inputs.map((p, idx) => (
        <Handle
          key={`in-${p.id}`}
          type="target"
          position={Position.Left}
          id={p.id}
          style={inputHandleStyle(idx)}
        />
      ))}
      {data.outputs.map((p, idx) => (
        <Handle
          key={`out-${p.id}`}
          type="source"
          position={Position.Right}
          id={p.id}
          style={outputHandleStyle(idx)}
        />
      ))}
    </div>
  );
}

const NODE_TYPES = { actor: ActorNode };

// ── Graph converter ───────────────────────────────────────────────────────────

interface DescriptorWithExtras extends NodeDescriptor {
  boundary?: boolean;
  boundaryRole?: "input" | "output" | "io";
}

function toReactFlow(graph: ReturnType<typeof useSessionStore.getState>["graph"]): {
  nodes: Node<ActorNodeData>[];
  edges: Edge[];
} {
  if (!graph) return { nodes: [], edges: [] };
  const nodes: Node<ActorNodeData>[] = (graph.nodes ?? []).map(
    (raw: NodeDescriptor) => {
      const n = raw as DescriptorWithExtras;
      return {
        id:   n.id,
        type: "actor",
        position: {
          x: typeof n.position?.x === "number" ? n.position.x : 100,
          y: typeof n.position?.y === "number" ? n.position.y : 100,
        },
        data: {
          label:   n.displayName ?? n.id,
          className: n.className,
          inputs:  (n.inputs  ?? []).map((p) => ({ id: p.id, name: p.name })),
          outputs: (n.outputs ?? []).map((p) => ({ id: p.id, name: p.name })),
          isComposite: isCompositeClass(n.className),
          isBoundary: n.boundary === true,
          boundaryRole: n.boundaryRole,
        },
      };
    }
  );
  const edges: Edge[] = (graph.edges ?? []).map((e) => ({
    id:           e.id,
    source:       e.source,
    sourceHandle: e.sourceHandle,
    target:       e.target,
    targetHandle: e.targetHandle,
    type:         "smoothstep",
    animated:     false,
    style:        {
      stroke: "#8b5cf6",
      strokeWidth: 1.75,
      strokeLinecap: "round" as const,
    },
    markerEnd: {
      type: MarkerType.ArrowClosed,
      width: 12,
      height: 12,
      color: "#8b5cf6",
    },
  }));
  return { nodes, edges };
}

// ── Canvas ────────────────────────────────────────────────────────────────────

// React-Flow handles for boundary pseudo-nodes look like
// "__boundary__<port>.<port>"; the connect tool wants just "<port>"
// because the relation lives in the composite scope.
function handleToConnectRef(handle: string): string {
  if (handle.startsWith("__boundary__")) {
    const dot = handle.lastIndexOf(".");
    return dot >= 0 ? handle.slice(dot + 1) : handle.replace("__boundary__", "");
  }
  return handle;
}

export function ModelCanvas() {
  const graph         = useSessionStore((s) => s.graph);
  const callTool      = useSessionStore((s) => s.callTool);
  const selectNode    = useSessionStore((s) => s.selectNode);
  const selectedNodeId = useSessionStore((s) => s.selectedNodeId);
  const canvasPath    = useSessionStore((s) => s.canvasPath);
  const enterComposite = useSessionStore((s) => s.enterComposite);
  const exitComposite  = useSessionStore((s) => s.exitComposite);
  const setCanvasPath  = useSessionStore((s) => s.setCanvasPath);

  // When inside a composite, mutating tool calls need parent set so
  // they target the correct level. The parent is the deepest segment.
  const currentParent = canvasPath[canvasPath.length - 1] ?? "";

  const initial = useMemo(() => toReactFlow(graph), [graph]);
  const [nodes, setNodes, onNodesChange] = useNodesState<ActorNodeData>(initial.nodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initial.edges);
  const flowRef = useRef<ReactFlowInstance<ActorNodeData, Edge> | null>(null);

  // Track the last graph signature we've auto-laid-out so we don't
  // keep snapping back if the user manually drags a node afterwards.
  const lastAutoSig = useRef<string>("");

  useEffect(() => {
    const { nodes: n, edges: e } = toReactFlow(graph);
    // Auto-apply layout when the freshly-loaded graph looks cluttered
    // (default positions, heavy collisions, etc.). For non-cluttered
    // graphs we keep the agent's / MoML's coordinates.
    // Signature includes node + edge counts so the layout re-runs
    // whenever the topology changes (e.g. the agent adds an actor at
    // the default (100,100) spawn point) — but NOT when the user just
    // drags a node, since that keeps node count fixed.
    const sig = `${graph?.topName ?? ""}#${canvasPath.join("/")}#${n.length}#${e.length}`;
    let nextNodes = n;
    if (sig !== lastAutoSig.current && isLayoutCluttered(n)) {
      nextNodes = layoutNodes(n, e);
      lastAutoSig.current = sig;
      window.requestAnimationFrame(() => {
        flowRef.current?.fitView({ padding: 0.22, duration: 350 });
      });
    }
    setNodes(nextNodes);
    setEdges(e);
  }, [graph, canvasPath, setNodes, setEdges]);

  const applyAutoLayout = useCallback(() => {
    const laid = layoutNodes(nodes, edges);
    setNodes(laid);
    window.requestAnimationFrame(() => {
      flowRef.current?.fitView({ padding: 0.22, duration: 450 });
    });
    // Persist new (x,y) back to the MoML so a save/reload survives.
    laid.forEach((n) => {
      if (n.id.startsWith("__boundary__")) return;
      const x = Math.round(n.position.x);
      const y = Math.round(n.position.y);
      const args: Record<string, unknown> = {
        entity:    n.id,
        parameter: "_location",
        value:     `[${x}.0, ${y}.0]`,
      };
      if (currentParent) args.parent = currentParent;
      void callTool("set_parameter", args);
    });
    lastAutoSig.current = `${graph?.topName ?? ""}#${canvasPath.join("/")}#${nodes.length}#manual`;
  }, [nodes, edges, callTool, currentParent, graph, canvasPath, setNodes]);

  const onSelectionChange = useCallback(
    (p: OnSelectionChangeParams) => { selectNode(p.nodes[0]?.id ?? null); },
    [selectNode]
  );
  const onNodeClick = useCallback(
    (_: React.MouseEvent, node: Node) => { selectNode(node.id); },
    [selectNode]
  );
  const onNodeDoubleClick = useCallback(
    (_: React.MouseEvent, node: Node<ActorNodeData>) => {
      if (node.data?.isComposite && !node.data?.isBoundary) {
        void enterComposite(node.id);
      }
    },
    [enterComposite]
  );
  const onPaneClick = useCallback(() => { selectNode(null); }, [selectNode]);

  const onConnect = useCallback(
    (c: Connection) => {
      const from = handleToConnectRef(c.sourceHandle ?? c.source ?? "");
      const to   = handleToConnectRef(c.targetHandle ?? c.target ?? "");
      if (!from || !to) return;
      const args: Record<string, unknown> = { from, to };
      if (currentParent) args.parent = currentParent;
      void callTool("connect", args);
    },
    [callTool, currentParent]
  );
  const onNodesDelete = useCallback(
    (deleted: Node[]) => {
      deleted.forEach((n) => {
        if (n.id.startsWith("__boundary__")) return; // can't delete ports here
        const args: Record<string, unknown> = { name: n.id };
        if (currentParent) args.parent = currentParent;
        void callTool("delete", args);
      });
      selectNode(null);
    },
    [callTool, currentParent, selectNode]
  );
  const onEdgesDelete = useCallback(
    (deleted: Edge[]) => {
      deleted.forEach((e) => {
        const args: Record<string, unknown> = { relation: e.id };
        if (currentParent) args.parent = currentParent;
        void callTool("disconnect", args);
      });
    },
    [callTool, currentParent]
  );

  const onDragOver = (ev: React.DragEvent) => {
    ev.preventDefault();
    ev.dataTransfer.dropEffect = "move";
  };
  const onDrop = (ev: React.DragEvent) => {
    ev.preventDefault();
    const className   = ev.dataTransfer.getData("application/x-ptolemy-class");
    const displayName = ev.dataTransfer.getData("application/x-ptolemy-displayname");
    if (!className) return;
    const rect = (ev.currentTarget as HTMLElement).getBoundingClientRect();
    const x = Math.max(20, Math.round(ev.clientX - rect.left));
    const y = Math.max(20, Math.round(ev.clientY - rect.top));
    const base = (displayName || shortClass(className) || "actor")
      .toLowerCase().replace(/[^a-z0-9_]/g, "_");
    const taken = new Set(nodes.map((n) => n.id));
    let name = base;
    for (let i = 2; taken.has(name); i++) name = `${base}${i}`;
    const args: Record<string, unknown> = { name, className, x, y };
    if (currentParent) args.parent = currentParent;
    void callTool("add_entity", args);
  };

  const empty = !graph || (graph.nodes?.length ?? 0) === 0;

  return (
    <div className="relative h-full w-full" onDragOver={onDragOver} onDrop={onDrop}>
      <CanvasOverlay
        graph={graph}
        canvasPath={canvasPath}
        onJump={(depth) =>
          depth === 0 ? void setCanvasPath([]) :
          depth >= canvasPath.length ? undefined :
          void setCanvasPath(canvasPath.slice(0, depth))
        }
        onExit={() => void exitComposite()}
        onAutoLayout={applyAutoLayout}
        canAutoLayout={!empty}
      />

      {empty ? (
        <EmptyCanvas />
      ) : (
        <ReactFlow
          nodes={nodes.map((n) => ({ ...n, selected: n.id === selectedNodeId }))}
          edges={edges}
          nodeTypes={NODE_TYPES}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          onSelectionChange={onSelectionChange}
          onNodeClick={onNodeClick}
          onNodeDoubleClick={onNodeDoubleClick}
          onPaneClick={onPaneClick}
          onNodesDelete={onNodesDelete}
          onEdgesDelete={onEdgesDelete}
          onInit={(instance) => { flowRef.current = instance; }}
          deleteKeyCode={["Delete", "Backspace"]}
          fitView
          fitViewOptions={{ padding: 0.2 }}
          proOptions={{ hideAttribution: true }}
          minZoom={0.25}
          maxZoom={2.5}
        >
          <Background
            gap={24}
            size={1}
            color="#e5e7eb"
            variant={BackgroundVariant.Dots}
          />
          <Controls position="bottom-right" />
          <MiniMap
            position="bottom-left"
            pannable
            zoomable
            nodeColor={(n) => {
              const cls = (n.data as ActorNodeData)?.className ?? "";
              return classifyAccent(cls).stripe;
            }}
            nodeStrokeColor="transparent"
            maskColor="rgba(248,250,252,0.75)"
            style={{ borderRadius: 8, border: "1px solid #e5e7eb" }}
          />
        </ReactFlow>
      )}
    </div>
  );
}

// ── Canvas overlay (always on top) ────────────────────────────────────────────

function CanvasOverlay({
  graph,
  canvasPath,
  onJump,
  onExit,
  onAutoLayout,
  canAutoLayout,
}: {
  graph: ReturnType<typeof useSessionStore.getState>["graph"];
  canvasPath: string[];
  onJump: (depth: number) => void;
  onExit: () => void;
  onAutoLayout: () => void;
  canAutoLayout: boolean;
}) {
  const director = graph?.directors?.[0]?.className;
  if (!graph) return null;
  const inside = canvasPath.length > 0;

  return (
    <div className="absolute top-0 left-0 right-0 z-20 px-4 pt-2 pointer-events-none">
      <div className="flex items-center gap-2 pointer-events-auto">
        {inside && (
          <button
            type="button"
            onClick={onExit}
            className="rounded-md border border-stone-200 bg-white/90
                       hover:bg-stone-50 shadow-card px-1.5 py-0.5
                       flex items-center gap-1 text-[11px] text-stone-700"
            title="Back to parent (Esc)"
          >
            <svg viewBox="0 0 24 24" width="12" height="12" fill="none"
                 stroke="currentColor" strokeWidth="2.2"
                 strokeLinecap="round" strokeLinejoin="round">
              <path d="M15 6l-6 6 6 6" />
            </svg>
            Back
          </button>
        )}

        {/* Breadcrumb */}
        <div className="flex items-center flex-wrap gap-1">
          <button
            type="button"
            onClick={() => onJump(0)}
            disabled={!inside}
            className={`text-[11px] font-medium ${
              inside
                ? "text-blue-600 hover:underline"
                : "text-stone-700 cursor-default"
            }`}
          >
            {graph.topName && !inside ? graph.topName : "model"}
          </button>
          {canvasPath.map((seg, i) => (
            <span key={i} className="flex items-center gap-1">
              <span className="text-stone-400 text-[10px]">/</span>
              <button
                type="button"
                onClick={() => onJump(i + 1)}
                disabled={i === canvasPath.length - 1}
                className={`text-[11px] font-mono ${
                  i === canvasPath.length - 1
                    ? "text-indigo-700 font-semibold cursor-default"
                    : "text-blue-600 hover:underline"
                }`}
              >
                {seg}
              </button>
            </span>
          ))}
        </div>

        {director && !inside && (
          <span className="pill-accent text-[10px]">{shortClass(director)}</span>
        )}
        {inside && (
          <span className="text-[10px] text-indigo-700/80 font-medium">
            inside subsystem
          </span>
        )}

        <div className="ml-auto flex items-center gap-2">
          {canAutoLayout && (
            <button
              type="button"
              onClick={onAutoLayout}
              className="rounded-md border border-stone-200 bg-white/90
                         hover:bg-stone-50 shadow-card px-1.5 py-0.5
                         flex items-center gap-1 text-[11px] text-stone-700"
              title="Re-arrange nodes left-to-right"
            >
              <svg viewBox="0 0 24 24" width="12" height="12" fill="none"
                   stroke="currentColor" strokeWidth="2.2"
                   strokeLinecap="round" strokeLinejoin="round">
                <rect x="3"  y="5"  width="6" height="6" rx="1" />
                <rect x="15" y="5"  width="6" height="6" rx="1" />
                <rect x="3"  y="13" width="6" height="6" rx="1" />
                <rect x="15" y="13" width="6" height="6" rx="1" />
                <path d="M9 8h6M9 16h6M6 11v2M18 11v2" />
              </svg>
              Layout
            </button>
          )}
          <span className="text-[10px] text-ink-400">
            {(graph.nodes?.length ?? 0) - (graph.nodes?.filter((n) =>
              (n as DescriptorWithExtras).boundary).length ?? 0)} actors ·
            {" "}{graph.edges?.length ?? 0} wires
            {graph.pathError && (
              <span className="ml-2 text-rose-600">{graph.pathError}</span>
            )}
          </span>
        </div>
      </div>
    </div>
  );
}

// ── Empty state ───────────────────────────────────────────────────────────────

function EmptyCanvas() {
  return (
    <div className="absolute inset-0 flex items-center justify-center bg-ink-900">
      {/* Subtle dot grid */}
      <div className="absolute inset-0"
           style={{
             backgroundImage: "radial-gradient(circle, #d6d3d1 1px, transparent 1px)",
             backgroundSize: "20px 20px",
           }}
      />
      <div className="relative z-10 text-center px-6 max-w-sm">
        <div className="mx-auto mb-4 w-11 h-11 rounded-xl bg-white border border-stone-200
                        shadow-sm flex items-center justify-center">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6"
               className="w-6 h-6 text-stone-400">
            <rect x="3" y="3" width="7" height="7" rx="1.5" />
            <rect x="14" y="3" width="7" height="7" rx="1.5" />
            <rect x="3"  y="14" width="7" height="7" rx="1.5" />
            <rect x="14" y="14" width="7" height="7" rx="1.5" />
            <path d="M10 6.5h4M10 17.5h4M6.5 10v4M17.5 10v4" />
          </svg>
        </div>
        <h2 className="font-semibold text-[17px] text-ink-100 mb-1">
          Canvas is empty
        </h2>
        <p className="text-sm text-ink-400 leading-relaxed">
          Run an example from the left panel, drag a component from the
          Components tab, type a model path in the toolbar, or use the agent
          chat below.
        </p>
        <div className="mt-4 flex items-center justify-center gap-2 text-[11px] text-ink-400">
          <span className="kbd">Del</span> delete ·
          <span className="kbd">Ctrl Z</span> undo
        </div>
      </div>
    </div>
  );
}
