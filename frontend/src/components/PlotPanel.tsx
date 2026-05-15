import { useState } from "react";
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ReferenceLine,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { ProbeData } from "../api/agentClient";
import { useSessionStore } from "../state/sessionStore";

// Renders every numeric probe as a line in a shared chart.
// Uses sample-index axis when all times are 0 (common in SDF).

interface ChartPoint {
  idx: number;
  t: number;
  [probeId: string]: number | null;
}

const LINE_COLORS = [
  "#2563eb", "#7c3aed", "#0d9488", "#d97706",
  "#dc2626", "#0891b2", "#65a30d", "#9333ea",
];

function buildSeries(probes: ProbeData[]) {
  const numeric = probes
    .filter((p) => p.values.some((v) => typeof v === "number" && isFinite(v)))
    .map((p) => ({
      ...p,
      values: p.values.map((v) =>
        typeof v === "number" && isFinite(v) ? v : null
      ),
    }));

  const allZero = numeric.every((p) => p.time.every((t) => t === 0));
  const maxLen  = Math.max(0, ...numeric.map((p) => p.values.length));

  const data: ChartPoint[] = Array.from({ length: maxLen }, (_, i) => {
    const row: ChartPoint = { idx: i, t: numeric[0]?.time?.[i] ?? i };
    for (const p of numeric) row[p.id] = (p.values[i] as number | null) ?? null;
    return row;
  });

  return {
    data,
    keys: numeric.map((p, i) => ({
      id:     p.id,
      actor:  p.actor,
      port:   p.port,
      colour: LINE_COLORS[i % LINE_COLORS.length],
      min:    Math.min(...(p.values.filter((v) => v !== null) as number[])),
      max:    Math.max(...(p.values.filter((v) => v !== null) as number[])),
      last:   ([...p.values].reverse().find((v) => v !== null) ?? null) as number | null,
    })),
    useTimeAxis: !allZero,
  };
}

function fmt(v: number | null) {
  if (v === null || !isFinite(v)) return "—";
  if (Math.abs(v) >= 1e4 || (Math.abs(v) < 0.01 && v !== 0))
    return v.toExponential(2);
  return v.toFixed(3);
}

export function PlotPanel() {
  const signals = useSessionStore((s) => s.signals);
  const probes  = signals?.probes ?? [];
  const [view, setView] = useState<"chart" | "table">("chart");

  if (probes.length === 0) {
    return (
      <div className="h-full flex items-center justify-center text-stone-400 text-sm bg-ink-900">
        <div className="text-center">
          <WaveIcon className="w-7 h-7 mx-auto mb-2 text-stone-300" />
          <div className="font-medium">No simulation results yet</div>
          <div className="text-[11px] mt-1 text-stone-400">
            Run a model — connected Recorder actors will appear here.
          </div>
        </div>
      </div>
    );
  }

  const { data, keys, useTimeAxis } = buildSeries(probes);

  if (keys.length === 0) {
    return (
      <div className="h-full flex items-center justify-center text-stone-400 text-sm">
        Probes returned no numeric data.
      </div>
    );
  }

  return (
    <div className="h-full flex bg-ink-950">
      {/* Chart / Table */}
      <div className="flex-1 min-w-0 flex flex-col">
        <div className="px-3 pt-2 pb-1 flex items-center justify-between border-b border-ink-700">
          <span className="section-title">Simulation results</span>
          <div className="flex items-center gap-2">
            <span className="text-[10px] text-ink-400">
              {keys.length} series · {data.length} samples ·{" "}
              {useTimeAxis ? "time axis" : "sample index"}
            </span>
            <div className="flex rounded overflow-hidden border border-ink-600 ml-2">
              <button
                className={`px-2 py-0.5 text-[10px] font-medium ${
                  view === "chart"
                    ? "bg-blue-600 text-white"
                    : "bg-ink-800 text-ink-300 hover:bg-ink-700"
                }`}
                onClick={() => setView("chart")}
              >
                Chart
              </button>
              <button
                className={`px-2 py-0.5 text-[10px] font-medium ${
                  view === "table"
                    ? "bg-blue-600 text-white"
                    : "bg-ink-800 text-ink-300 hover:bg-ink-700"
                }`}
                onClick={() => setView("table")}
              >
                Table
              </button>
            </div>
          </div>
        </div>

        {view === "chart" ? (
          <div className="flex-1 px-1 py-1">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={data} margin={{ top: 6, right: 16, bottom: 16, left: 2 }}>
                <CartesianGrid stroke="#f5f5f4" strokeDasharray="4 4" />
                <ReferenceLine y={0} stroke="#e7e5e4" strokeWidth={1} />
                <XAxis
                  dataKey={useTimeAxis ? "t" : "idx"}
                  stroke="#d6d3d1"
                  tick={{ fill: "#a8a29e", fontSize: 10 }}
                  tickLine={false}
                  axisLine={{ stroke: "#e7e5e4" }}
                  tickFormatter={(v: number) =>
                    useTimeAxis ? v.toFixed(2) : v.toString()
                  }
                  label={{
                    value: useTimeAxis ? "time (s)" : "sample",
                    position: "insideBottom",
                    offset: -8,
                    fill: "#a8a29e",
                    fontSize: 10,
                  }}
                />
                <YAxis
                  stroke="#d6d3d1"
                  tick={{ fill: "#a8a29e", fontSize: 10 }}
                  tickLine={false}
                  axisLine={{ stroke: "#e7e5e4" }}
                  width={46}
                />
                <Tooltip
                  contentStyle={{
                    background: "#ffffff",
                    border: "1px solid #e7e5e4",
                    borderRadius: 6,
                    fontSize: 12,
                    boxShadow: "0 2px 8px rgba(0,0,0,0.08)",
                  }}
                  labelStyle={{ color: "#78716c" }}
                  itemStyle={{ color: "#292524" }}
                  labelFormatter={(v) =>
                    useTimeAxis ? `t = ${Number(v).toFixed(4)} s` : `sample ${v}`
                  }
                />
                <Legend
                  wrapperStyle={{ fontSize: 11, paddingTop: 4 }}
                  formatter={(value: string) => {
                    const k = keys.find((k) => k.id === value);
                    return k ? `${k.actor}.${k.port}` : value;
                  }}
                />
                {keys.map((k) => (
                  <Line
                    key={k.id}
                    type="monotone"
                    dataKey={k.id}
                    name={k.id}
                    stroke={k.colour}
                    strokeWidth={1.8}
                    dot={false}
                    isAnimationActive={false}
                    connectNulls
                  />
                ))}
              </LineChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <DataTable data={data} keys={keys} useTimeAxis={useTimeAxis} />
        )}
      </div>

      {/* Probes sidecar */}
      <div className="w-52 shrink-0 border-l border-ink-700 overflow-y-auto bg-ink-850">
        <div className="px-3 pt-2 pb-1 section-title border-b border-ink-700">
          Probes
        </div>
        <ul className="space-y-1 p-2">
          {keys.map((k) => (
            <li key={k.id}
                className="rounded-md border border-ink-700 bg-ink-950 px-2 py-1.5">
              <div className="flex items-center gap-2">
                <span className="w-2 h-2 rounded-full shrink-0"
                      style={{ background: k.colour }} />
                <span className="text-[11px] font-mono text-ink-200 truncate">
                  {k.actor}.{k.port}
                </span>
              </div>
              <div className="mt-1 grid grid-cols-3 gap-1 text-[10px] text-ink-400 font-mono">
                <span title="min">↓ {fmt(k.min)}</span>
                <span title="max">↑ {fmt(k.max)}</span>
                <span title="last">∎ {fmt(k.last)}</span>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}

interface ProbeKey {
  id: string;
  actor: string;
  port: string;
  colour: string;
}

function DataTable({
  data,
  keys,
  useTimeAxis,
}: {
  data: ChartPoint[];
  keys: ProbeKey[];
  useTimeAxis: boolean;
}) {
  return (
    <div className="flex-1 overflow-auto">
      <table className="w-full text-[11px] font-mono border-collapse">
        <thead className="sticky top-0 bg-ink-850 z-10">
          <tr className="border-b border-ink-700">
            <th className="px-2 py-1 text-left text-ink-400 font-medium">#</th>
            {useTimeAxis && (
              <th className="px-2 py-1 text-right text-ink-400 font-medium">
                time (s)
              </th>
            )}
            {keys.map((k) => (
              <th
                key={k.id}
                className="px-2 py-1 text-right font-medium"
                style={{ color: k.colour }}
              >
                {k.actor}.{k.port}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row) => (
            <tr
              key={row.idx}
              className="border-b border-ink-800 hover:bg-ink-800/50"
            >
              <td className="px-2 py-0.5 text-ink-500">{row.idx}</td>
              {useTimeAxis && (
                <td className="px-2 py-0.5 text-right text-ink-300">
                  {fmt(row.t)}
                </td>
              )}
              {keys.map((k) => (
                <td key={k.id} className="px-2 py-0.5 text-right text-ink-200">
                  {fmt(row[k.id] as number | null)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function WaveIcon({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"
         strokeLinecap="round" className={className}>
      <path d="M3 12c2-6 4-6 6 0s4 6 6 0 4-6 6 0" />
    </svg>
  );
}
