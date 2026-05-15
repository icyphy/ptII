/* Deterministic execution of a phase-0 JSON plan via ToolRegistry.

 Copyright (c) 2024-2026 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package org.ptolemy.agent.agent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.tools.ToolRegistry;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// PlanExecutor

/**
 Walk a phase-0 JSON plan and execute it deterministically by
 dispatching the same tools an LLM would, in a predictable order.  The
 executor is the heart of the industrial-grade pipeline:

 <ol>
 <li>The LLM is invited to make the high-level decisions (which
     actors, which director, what to wire).</li>
 <li>The Java executor performs the mechanical work (add_entity for
     every actor, set_parameter for every non-default parameter,
     connect_many for all edges, validate, run).</li>
 <li>The LLM only re-enters when the executor reports failures, and
     even then with a scoped "fix these N specific issues" task list.</li>
 </ol>

 <p>Every step runs through {@link ToolRegistry#dispatch} so that the
 Tier-1 / Tier-2 / Tier-5 guarantees (preflight didYouMean hints,
 connect_many transactional rollback, capability probe class-failure
 tracking) all apply uniformly.  Listener events are emitted for each
 step so the frontend shows the executor's work as if a chat turn had
 produced the same tool calls.

 <p>The executor does not throw on per-step failure: it records each
 failure into {@link Outcome#failedSteps} and continues with whatever
 remains useful.  A failed {@code add_entity} for actor X will skip
 X's parameters but still attempt to wire X's siblings; a failed
 {@code connect_many} batch will roll back but still let validate /
 run run so the LLM sees the full diagnostic.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class PlanExecutor {

    /** One executed step. */
    public static final class StepResult {
        public final int index;
        public final String stage;      // director, actor, parameter, wire, validate, run
        public final String toolName;
        public final JSONObject args;
        public final boolean ok;
        public final String message;
        public final JSONObject data;
        public final String subject;    // actor name / edge label / etc

        StepResult(int index, String stage, String toolName,
                JSONObject args, AgentResult result, String subject) {
            this.index = index;
            this.stage = stage;
            this.toolName = toolName;
            this.args = args == null ? new JSONObject() : args;
            this.ok = result.ok();
            this.message = result.message();
            this.data = result.data();
            this.subject = subject == null ? "" : subject;
        }

        public JSONObject toJson() {
            JSONObject out = new JSONObject();
            out.put("index", index);
            out.put("stage", stage);
            out.put("tool", toolName);
            out.put("ok", ok);
            out.put("message", message);
            out.put("subject", subject);
            out.put("args", args);
            return out;
        }
    }

    /** End-of-execution summary. */
    public static final class Outcome {
        public final List<StepResult> steps = new ArrayList<>();
        public final List<StepResult> failedSteps = new ArrayList<>();
        public boolean directorReady = false;
        public boolean actorsAllAdded = false;
        public boolean wireOk = false;
        public boolean validateOk = false;
        public boolean runOk = false;
        public boolean simulates = false;

        /** Compact report consumable by the phase-1 LLM. */
        public JSONObject toReport() {
            JSONObject report = new JSONObject();
            report.put("totalSteps", steps.size());
            report.put("successfulSteps",
                    steps.size() - failedSteps.size());
            report.put("failedSteps", failedSteps.size());
            report.put("directorReady", directorReady);
            report.put("actorsAllAdded", actorsAllAdded);
            report.put("wireOk", wireOk);
            report.put("validateOk", validateOk);
            report.put("runOk", runOk);
            report.put("simulates", simulates);
            JSONArray failures = new JSONArray();
            for (StepResult step : failedSteps) {
                failures.put(step.toJson());
            }
            report.put("failures", failures);
            return report;
        }

        /** True when nothing failed: the model was built, validated
         *  and simulated entirely from the plan, no LLM repair
         *  needed. */
        public boolean fullyAutonomous() {
            return failedSteps.isEmpty() && simulates;
        }

        /** Human-readable summary line for the chat. */
        public String summary() {
            return "executor: " + (steps.size() - failedSteps.size())
                    + "/" + steps.size() + " steps ok"
                    + (validateOk ? "; validate=ok" : "; validate=fail")
                    + (runOk ? "; run=ok" : "; run=fail");
        }
    }

    private PlanExecutor() {
    }

    /** Execute the plan.  Safe to call with a partially-valid plan;
     *  every step is independent and per-step failures are recorded
     *  but never thrown.
     *  @param plan Parsed phase-0 JSON plan.
     *  @param session Target session.
     *  @param tools Tool registry to dispatch through.
     *  @param listener Optional listener for live trace events.
     *  @return Aggregated outcome.
     */
    public static Outcome execute(JSONObject plan, PtolemySession session,
            ToolRegistry tools, AgentTraceListener listener) {
        Outcome outcome = new Outcome();
        if (plan == null || session == null || tools == null) {
            return outcome;
        }
        _ensureToplevel(session);

        // ---- Director ----
        JSONObject director = plan.optJSONObject("director");
        if (director != null) {
            String dirClass = director.optString("className", "").trim();
            String dirName = _directorNameFromClass(dirClass);
            if (!dirClass.isEmpty()) {
                JSONObject args = new JSONObject();
                args.put("name", dirName);
                args.put("className", dirClass);
                _record(outcome, "director", "add_entity",
                        tools.dispatch("add_entity", session, args),
                        args, dirName, listener);
                outcome.directorReady = _lastOk(outcome);
                JSONObject dirParams = director.optJSONObject(
                        "parameters");
                if (dirParams != null) {
                    _applyParameters(outcome, tools, session, dirName,
                            dirParams, listener);
                }
            }
        }

        // ---- Actors + their parameters ----
        Set<String> addedActors = new LinkedHashSet<>();
        JSONArray actors = plan.optJSONArray("actors");
        if (actors != null) {
            for (int i = 0; i < actors.length(); i++) {
                JSONObject a = actors.optJSONObject(i);
                if (a == null) {
                    continue;
                }
                String name = a.optString("name", "").trim();
                String cls = a.optString("className", "").trim();
                if (name.isEmpty() || cls.isEmpty()) {
                    continue;
                }
                JSONObject args = new JSONObject();
                args.put("name", name);
                args.put("className", cls);
                _record(outcome, "actor", "add_entity",
                        tools.dispatch("add_entity", session, args),
                        args, name, listener);
                if (_lastOk(outcome)) {
                    addedActors.add(name);
                    JSONObject params = a.optJSONObject("parameters");
                    if (params != null) {
                        _applyParameters(outcome, tools, session, name,
                                params, listener);
                    }
                }
            }
            outcome.actorsAllAdded = (addedActors.size()
                    == actors.length());
        }

        // ---- Connections (one batched connect_many) ----
        JSONArray connections = plan.optJSONArray("connections");
        if (connections != null && connections.length() > 0) {
            JSONArray edges = new JSONArray();
            for (int i = 0; i < connections.length(); i++) {
                JSONObject c = connections.optJSONObject(i);
                if (c == null) {
                    continue;
                }
                JSONObject e = new JSONObject();
                e.put("from", c.optString("from", ""));
                e.put("to", c.optString("to", ""));
                edges.put(e);
            }
            JSONObject args = new JSONObject();
            args.put("edges", edges);
            String label = edges.length() + " edges";
            _record(outcome, "wire", "connect_many",
                    tools.dispatch("connect_many", session, args),
                    args, label, listener);
            outcome.wireOk = _lastOk(outcome);
        } else {
            // No connections at all is itself a problem; let validate
            // diagnose it.
            outcome.wireOk = false;
        }

        // ---- Validate ----
        AgentResult validate = tools.dispatch("validate", session,
                new JSONObject());
        _record(outcome, "validate", "validate", validate,
                new JSONObject(), "", listener);
        outcome.validateOk = validate.ok()
                && _validateClean(validate);

        // ---- Run (only when validate is clean) ----
        if (outcome.validateOk) {
            AgentResult run = tools.dispatch("run", session,
                    new JSONObject());
            _record(outcome, "run", "run", run, new JSONObject(), "",
                    listener);
            outcome.runOk = run.ok();
        }
        outcome.simulates = outcome.validateOk && outcome.runOk;
        return outcome;
    }

    /** Ensure the session has a top-level composite to host actors. */
    private static void _ensureToplevel(PtolemySession session) {
        if (session.toplevel() != null) {
            return;
        }
        // Seed with a bare composite so add_entity has somewhere to
        // attach.  The director step will fill the director slot.
        session.loadMoml(""
                + "<entity name=\"AgentModel\""
                + " class=\"ptolemy.actor.TypedCompositeActor\"/>");
    }

    /** Apply a parameters object to an entity by issuing one
     *  set_parameter call per non-empty value. */
    private static void _applyParameters(Outcome outcome,
            ToolRegistry tools, PtolemySession session, String entity,
            JSONObject parameters, AgentTraceListener listener) {
        java.util.Iterator<String> it = parameters.keys();
        while (it.hasNext()) {
            String key = it.next();
            String value = parameters.optString(key, null);
            if (value == null || value.isEmpty()) {
                continue;
            }
            JSONObject args = new JSONObject();
            args.put("entity", entity);
            args.put("parameter", key);
            args.put("value", value);
            _record(outcome, "parameter", "set_parameter",
                    tools.dispatch("set_parameter", session, args),
                    args, entity + "." + key, listener);
        }
    }

    /** True when the validate result reports no ERROR-severity
     *  issues.  WARNs alone are fine. */
    private static boolean _validateClean(AgentResult validate) {
        if (validate == null || !validate.ok()) {
            return false;
        }
        JSONObject data = validate.data();
        if (data == null) {
            return true;
        }
        JSONArray diagnostics = data.optJSONArray("diagnostics");
        if (diagnostics != null) {
            for (int i = 0; i < diagnostics.length(); i++) {
                JSONObject d = diagnostics.optJSONObject(i);
                if (d != null && "ERROR".equals(
                        d.optString("severity"))) {
                    return false;
                }
            }
            return true;
        }
        // Fallback: legacy "issues" array (strings).  Treat presence
        // as a soft warning, not a blocker.
        return true;
    }

    /** Record one step into the outcome AND fire a listener event so
     *  the frontend shows the executor's tool calls in the chat
     *  panel. */
    private static void _record(Outcome outcome, String stage,
            String toolName, AgentResult result, JSONObject args,
            String subject, AgentTraceListener listener) {
        StepResult step = new StepResult(outcome.steps.size(), stage,
                toolName, args, result, subject);
        outcome.steps.add(step);
        if (!step.ok) {
            outcome.failedSteps.add(step);
        }
        if (listener != null) {
            // Emit a tool_call/tool_result pair so the executor's
            // actions look identical to LLM-driven ones in the trace.
            listener.onStep(new AgentTrace.Step(0, "tool_call",
                    toolName, args, "[exec] " + stage
                            + (subject.isEmpty() ? "" : ": " + subject)));
            JSONObject obs = new JSONObject(result.toJson().toString());
            obs.put("executorStage", stage);
            obs.put("executorSubject", subject);
            listener.onStep(new AgentTrace.Step(0, "tool_result",
                    toolName, obs, ""));
        }
    }

    private static boolean _lastOk(Outcome outcome) {
        if (outcome.steps.isEmpty()) {
            return false;
        }
        return outcome.steps.get(outcome.steps.size() - 1).ok;
    }

    /** Director name follows the Vergil convention of "SDF Director" /
     *  "DE Director" / "Continuous Director" so it matches the names
     *  the LLM sees in ModelContext and set_parameter calls. */
    private static String _directorNameFromClass(String className) {
        if (className == null) {
            return "Director";
        }
        if (className.contains("sdf.kernel.SDFDirector")) {
            return "SDF Director";
        }
        if (className.contains("de.kernel.DEDirector")) {
            return "DE Director";
        }
        if (className.contains("continuous.kernel.ContinuousDirector")) {
            return "Continuous Director";
        }
        if (className.contains("ddf.kernel.DDFDirector")) {
            return "DDF Director";
        }
        if (className.contains("hdf.kernel.HDFDirector")) {
            return "HDF Director";
        }
        int dot = className.lastIndexOf('.');
        return dot < 0 ? className : className.substring(dot + 1);
    }
}
