/* In-process smoke test for the deterministic plan executor.

 Copyright (c) 2024-2026 The Regents of the University of California.
 All rights reserved.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package org.ptolemy.agent.test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.agent.PlanExecutor;
import org.ptolemy.agent.agent.PlanValidator;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.tools.ToolRegistry;

///////////////////////////////////////////////////////////////////
//// PlanExecutorSmokeTest

/** Drive a few hand-written plans through PlanValidator + PlanExecutor
 *  to verify the deterministic build path actually builds and runs
 *  models end-to-end without any LLM in the loop. */
public class PlanExecutorSmokeTest {

    public static void main(String[] args) throws Exception {
        _scenario("scenario A: minimal Ramp -> Scale -> Recorder",
                _basicLinearPlan());
        _scenario("scenario B: Ramp -> AddSubtract.plus +"
                        + " Const -> AddSubtract.plus -> Recorder",
                _addSubtractPlan());
        _scenario("scenario C: deliberately broken — bogus className",
                _brokenClassPlan());
        _scenario("scenario D: deliberately broken — wrong port",
                _brokenPortPlan());
    }

    private static void _scenario(String label, JSONObject plan)
            throws Exception {
        System.out.println("================================================");
        System.out.println(label);
        System.out.println();
        JSONArray issues = PlanValidator.validate(plan);
        System.out.println("PlanValidator issues: " + issues.length());
        for (int i = 0; i < issues.length(); i++) {
            JSONObject row = issues.optJSONObject(i);
            System.out.println("  " + row.optString("severity") + " "
                    + row.optString("code") + " - "
                    + row.optString("message"));
        }
        if (PlanValidator.hasErrors(issues)) {
            System.out.println("  -> skipping executor (errors present)");
            return;
        }
        PtolemySession session = new PtolemySession();
        ToolRegistry tools = ToolRegistry.defaultRegistry();
        PlanExecutor.Outcome outcome = PlanExecutor.execute(plan,
                session, tools, null);
        System.out.println();
        System.out.println("Executor: " + outcome.summary());
        System.out.println("  fullyAutonomous=" + outcome.fullyAutonomous());
        if (!outcome.failedSteps.isEmpty()) {
            System.out.println("  Failures:");
            for (PlanExecutor.StepResult s : outcome.failedSteps) {
                System.out.println("    [" + s.stage + "/" + s.toolName
                        + "] " + s.subject + " — " + s.message);
            }
        }
        System.out.println("  Report: " + outcome.toReport().toString(2));
        // Surface validate's own data block so we can see why
        // simulates=false even when no step failed.
        for (PlanExecutor.StepResult step : outcome.steps) {
            if ("validate".equals(step.stage)) {
                System.out.println("  validate.data = "
                        + step.data.toString(2));
            }
        }
        System.out.println();
    }

    private static JSONObject _basicLinearPlan() {
        JSONObject plan = new JSONObject();
        plan.put("domain", "SDF");
        plan.put("director", new JSONObject()
                .put("className", "ptolemy.domains.sdf.kernel.SDFDirector")
                .put("parameters", new JSONObject()
                        .put("iterations", "10")));
        JSONArray actors = new JSONArray();
        actors.put(new JSONObject().put("name", "Src")
                .put("className", "ptolemy.actor.lib.Ramp")
                .put("role", "source"));
        actors.put(new JSONObject().put("name", "Gain")
                .put("className", "ptolemy.actor.lib.Scale")
                .put("parameters", new JSONObject().put("factor", "2.0"))
                .put("role", "transform"));
        actors.put(new JSONObject().put("name", "Rec")
                .put("className", "ptolemy.actor.lib.Recorder")
                .put("role", "sink"));
        plan.put("actors", actors);
        JSONArray edges = new JSONArray();
        edges.put(new JSONObject().put("from", "Src.output")
                .put("to", "Gain.input"));
        edges.put(new JSONObject().put("from", "Gain.output")
                .put("to", "Rec.input"));
        plan.put("connections", edges);
        plan.put("validationGoal", "Ramp -> Scale(2) -> Recorder");
        return plan;
    }

    private static JSONObject _addSubtractPlan() {
        JSONObject plan = new JSONObject();
        plan.put("domain", "SDF");
        plan.put("director", new JSONObject()
                .put("className", "ptolemy.domains.sdf.kernel.SDFDirector")
                .put("parameters", new JSONObject()
                        .put("iterations", "5")));
        JSONArray actors = new JSONArray();
        actors.put(new JSONObject().put("name", "Src")
                .put("className", "ptolemy.actor.lib.Ramp"));
        actors.put(new JSONObject().put("name", "Bias")
                .put("className", "ptolemy.actor.lib.Const")
                .put("parameters", new JSONObject().put("value", "10.0")));
        actors.put(new JSONObject().put("name", "Sum")
                .put("className", "ptolemy.actor.lib.AddSubtract"));
        actors.put(new JSONObject().put("name", "Rec")
                .put("className", "ptolemy.actor.lib.Recorder"));
        plan.put("actors", actors);
        JSONArray edges = new JSONArray();
        edges.put(new JSONObject().put("from", "Src.output")
                .put("to", "Sum.plus"));
        edges.put(new JSONObject().put("from", "Bias.output")
                .put("to", "Sum.plus"));
        edges.put(new JSONObject().put("from", "Sum.output")
                .put("to", "Rec.input"));
        plan.put("connections", edges);
        plan.put("validationGoal", "summed Ramp + Const");
        return plan;
    }

    private static JSONObject _brokenClassPlan() {
        JSONObject plan = new JSONObject();
        plan.put("director", new JSONObject()
                .put("className", "ptolemy.domains.sdf.kernel.SDFDirector"));
        JSONArray actors = new JSONArray();
        actors.put(new JSONObject().put("name", "Sig")
                .put("className", "ptolemy.actor.lib.Sigmoid"));
        actors.put(new JSONObject().put("name", "Src")
                .put("className", "ptolemy.actor.lib.Ramp"));
        plan.put("actors", actors);
        plan.put("connections", new JSONArray()
                .put(new JSONObject().put("from", "Src.output")
                        .put("to", "Sig.input")));
        plan.put("validationGoal", "should fail at validator");
        return plan;
    }

    private static JSONObject _brokenPortPlan() {
        JSONObject plan = new JSONObject();
        plan.put("director", new JSONObject()
                .put("className", "ptolemy.domains.sdf.kernel.SDFDirector"));
        JSONArray actors = new JSONArray();
        actors.put(new JSONObject().put("name", "Src")
                .put("className", "ptolemy.actor.lib.Ramp"));
        actors.put(new JSONObject().put("name", "Sum")
                .put("className", "ptolemy.actor.lib.AddSubtract"));
        actors.put(new JSONObject().put("name", "Rec")
                .put("className", "ptolemy.actor.lib.Recorder"));
        plan.put("actors", actors);
        plan.put("connections", new JSONArray()
                .put(new JSONObject().put("from", "Src.output")
                        .put("to", "Sum.input"))
                .put(new JSONObject().put("from", "Sum.output")
                        .put("to", "Rec.input")));
        plan.put("validationGoal",
                "wire fails because AddSubtract has no .input");
        return plan;
    }
}
