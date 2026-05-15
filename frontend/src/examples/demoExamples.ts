// Click-to-run example workflows.
//
// Each example is self-contained: it loads a fresh model skeleton, then
// applies a deterministic sequence of tool calls (add_entity /
// set_parameter / connect) to build a properly-wired SDF dataflow
// graph, and finally runs the simulation.
//
// Important: every example explicitly adds a Recorder sink and connects
// it. This means (a) the canvas shows proper wire connections, (b) the
// SDF scheduler can validate graph connectivity, and (c) results appear
// in the Signals panel.  The backend SignalCollector additionally
// auto-probes any remaining unrecorded ports, but the primary output
// comes from the explicit connections.
//
// The MoML skeletons do NOT set allowDisconnectedGraphs — connectivity
// is enforced at the director level.

import { agentApi } from "../api/agentClient";

export interface DemoOp {
  tool: "add_entity" | "set_parameter" | "connect" | "disconnect" | "delete";
  args: Record<string, unknown>;
}

export interface DemoExample {
  id: string;
  title: string;
  subtitle: string;
  domain: "SDF" | "DE" | "Continuous";
  highlight: string;
  description: string;
  accentColor: "blue" | "violet" | "rose" | "amber" | "teal";
  skeleton: string;
  ops: DemoOp[];
}

// ── MoML skeleton ────────────────────────────────────────────────────────────

function sdfSkeleton(name: string, iterations: number): string {
  return `<?xml version="1.0" standalone="no"?>
<!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"
    "http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd">
<entity name="${name}" class="ptolemy.actor.TypedCompositeActor">
  <property name="SDFDirector" class="ptolemy.domains.sdf.kernel.SDFDirector">
    <property name="iterations" class="ptolemy.data.expr.Parameter" value="${iterations}"/>
  </property>
</entity>`;
}

// ── The five demos ────────────────────────────────────────────────────────────

export const DEMO_EXAMPLES: DemoExample[] = [
  // 1. Ramp Counter —————————————————————————————————————————————————————————
  {
    id: "ramp",
    title: "Ramp Counter",
    subtitle: "0, 1, 2, … — arithmetic sequence source",
    domain: "SDF",
    highlight: "2 actors · 1 wire · 30 samples",
    accentColor: "blue",
    description:
      "A Ramp source emits 0, 1, 2, … connected to an explicit Recorder sink. " +
      "Demonstrates the most basic SDF dataflow graph with a single wire.",
    skeleton: sdfSkeleton("RampCounter", 30),
    ops: [
      { tool: "add_entity",    args: { name: "Ramp",     className: "ptolemy.actor.lib.Ramp",     x: 180, y: 210 } },
      { tool: "set_parameter", args: { entity: "Ramp",   parameter: "init", value: "0" } },
      { tool: "set_parameter", args: { entity: "Ramp",   parameter: "step", value: "1" } },
      { tool: "add_entity",    args: { name: "Recorder", className: "ptolemy.actor.lib.Recorder", x: 440, y: 210 } },
      { tool: "connect",       args: { from: "Ramp.output", to: "Recorder.input" } },
    ],
  },

  // 2. Sine Wave ——————————————————————————————————————————————————————————————
  {
    id: "sine",
    title: "Sine Wave",
    subtitle: "1 Hz sampled at 32 Hz · 3 full cycles",
    domain: "SDF",
    highlight: "2 actors · 1 wire · 96 samples",
    accentColor: "violet",
    description:
      "Sinewave actor at 1 Hz (32 samples/s) connected to a Recorder. " +
      "The Signals tab renders the full waveform with min / max / last stats.",
    skeleton: sdfSkeleton("SineWave", 96),
    ops: [
      { tool: "add_entity",    args: { name: "Sine",     className: "ptolemy.actor.lib.Sinewave",  x: 180, y: 210 } },
      { tool: "set_parameter", args: { entity: "Sine",   parameter: "frequency",        value: "1.0"  } },
      { tool: "set_parameter", args: { entity: "Sine",   parameter: "samplingFrequency",value: "32.0" } },
      { tool: "set_parameter", args: { entity: "Sine",   parameter: "amplitude",        value: "1.0"  } },
      { tool: "add_entity",    args: { name: "Recorder", className: "ptolemy.actor.lib.Recorder",  x: 440, y: 210 } },
      { tool: "connect",       args: { from: "Sine.output", to: "Recorder.input" } },
    ],
  },

  // 3. Sine → Scale pipeline ————————————————————————————————————————————————
  {
    id: "pipeline",
    title: "Gain Pipeline",
    subtitle: "Sinewave → Scale(2.5) — actor composition",
    domain: "SDF",
    highlight: "3 actors · 2 wires · 64 samples",
    accentColor: "teal",
    description:
      "Chain: Sinewave → Scale (gain 2.5) → Recorder. " +
      "Demonstrates two-stage actor composition with both intermediate " +
      "and final values captured in the Signals panel.",
    skeleton: sdfSkeleton("SineGain", 64),
    ops: [
      { tool: "add_entity",    args: { name: "Sine",     className: "ptolemy.actor.lib.Sinewave",  x: 140, y: 210 } },
      { tool: "set_parameter", args: { entity: "Sine",   parameter: "frequency",        value: "1.0"  } },
      { tool: "set_parameter", args: { entity: "Sine",   parameter: "samplingFrequency",value: "32.0" } },
      { tool: "add_entity",    args: { name: "Gain",     className: "ptolemy.actor.lib.Scale",      x: 360, y: 210 } },
      { tool: "set_parameter", args: { entity: "Gain",   parameter: "factor",           value: "2.5" } },
      { tool: "add_entity",    args: { name: "Recorder", className: "ptolemy.actor.lib.Recorder",  x: 580, y: 210 } },
      { tool: "connect",       args: { from: "Sine.output",  to: "Gain.input"      } },
      { tool: "connect",       args: { from: "Gain.output",  to: "Recorder.input"  } },
    ],
  },

  // 4. Adder ——————————————————————————————————————————————————————————————————
  {
    id: "adder",
    title: "Signal Adder",
    subtitle: "Ramp A + Ramp B → AddSubtract multiport",
    domain: "SDF",
    highlight: "4 actors · 3 wires · 20 samples",
    accentColor: "amber",
    description:
      "Two independent ramps feed an AddSubtract through its multi-input 'plus' " +
      "port. Output flows to a Recorder. Shows how a single port accepts " +
      "multiple connections in SDF.",
    skeleton: sdfSkeleton("Adder", 20),
    ops: [
      { tool: "add_entity",    args: { name: "RampA",    className: "ptolemy.actor.lib.Ramp",          x: 140, y: 150 } },
      { tool: "set_parameter", args: { entity: "RampA",  parameter: "init", value: "0" } },
      { tool: "set_parameter", args: { entity: "RampA",  parameter: "step", value: "1" } },
      { tool: "add_entity",    args: { name: "RampB",    className: "ptolemy.actor.lib.Ramp",          x: 140, y: 300 } },
      { tool: "set_parameter", args: { entity: "RampB",  parameter: "init", value: "10" } },
      { tool: "set_parameter", args: { entity: "RampB",  parameter: "step", value: "2" } },
      { tool: "add_entity",    args: { name: "Sum",      className: "ptolemy.actor.lib.AddSubtract",   x: 370, y: 220 } },
      { tool: "add_entity",    args: { name: "Recorder", className: "ptolemy.actor.lib.Recorder",     x: 590, y: 220 } },
      { tool: "connect",       args: { from: "RampA.output", to: "Sum.plus"       } },
      { tool: "connect",       args: { from: "RampB.output", to: "Sum.plus"       } },
      { tool: "connect",       args: { from: "Sum.output",   to: "Recorder.input" } },
    ],
  },

  // 5. Gaussian Noise ————————————————————————————————————————————————————————
  {
    id: "noise",
    title: "Gaussian Noise",
    subtitle: "N(0, 1) random samples — stochastic source",
    domain: "SDF",
    highlight: "2 actors · 1 wire · 64 samples",
    accentColor: "rose",
    description:
      "Gaussian source (μ=0, σ=1) wired to a Recorder over 64 iterations. " +
      "Useful baseline for testing downstream filters and statistical operators.",
    skeleton: sdfSkeleton("Noise", 64),
    ops: [
      { tool: "add_entity",    args: { name: "RNG",      className: "ptolemy.actor.lib.Gaussian",  x: 180, y: 210 } },
      { tool: "set_parameter", args: { entity: "RNG",    parameter: "mean",              value: "0.0" } },
      { tool: "set_parameter", args: { entity: "RNG",    parameter: "standardDeviation", value: "1.0" } },
      { tool: "add_entity",    args: { name: "Recorder", className: "ptolemy.actor.lib.Recorder", x: 440, y: 210 } },
      { tool: "connect",       args: { from: "RNG.output", to: "Recorder.input" } },
    ],
  },
];

// ── Runner ───────────────────────────────────────────────────────────────────

export async function runDemoExample(
  sessionId: string,
  demo: DemoExample
): Promise<{ ok: boolean; message: string }> {
  // 1. Load the fresh skeleton (replaces any existing model).
  const loadResult = await agentApi.loadMoml(sessionId, demo.skeleton);
  if (!loadResult.ok) {
    return { ok: false, message: `load skeleton: ${loadResult.message}` };
  }

  // 2. Apply each op in sequence.
  for (const op of demo.ops) {
    const res = await agentApi.callTool(sessionId, op.tool, op.args);
    if (!res.ok) {
      return {
        ok: false,
        message: `${op.tool}(${JSON.stringify(op.args)}) → ${res.message}`,
      };
    }
  }

  return { ok: true, message: `built '${demo.title}' (${demo.ops.length} ops)` };
}
