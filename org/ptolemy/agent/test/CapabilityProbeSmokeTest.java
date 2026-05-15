/* Quick smoke test for CapabilityProbe and class-failure tracking.

 Copyright (c) 2024-2026 The Regents of the University of California.
 All rights reserved.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package org.ptolemy.agent.test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.agent.CapabilityProbe;
import org.ptolemy.agent.library.LibraryIndex;
import org.ptolemy.agent.session.ModelContext;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.tools.ToolRegistry;
import org.ptolemy.agent.util.AgentResult;

/** Tiny driver that prints the capability recipe for a few sample
 *  goals so we can eyeball the soft prior the LLM will see. */
public class CapabilityProbeSmokeTest {

    public static void main(String[] args) {
        String[] goals = new String[] {
                "Build an LSTM cell with 4 gates under SDF",
                "Simulate an RC low-pass filter with R=1, C=1",
                "Build a PID controller for a simple plant",
                "Generate a sine wave and record the output",
                "Compute y = 2*x + 1 over a Ramp source"
        };
        LibraryIndex lib = LibraryIndex.shared();
        System.out.println("LIBRARY INDEX SOURCE: " + lib.status());
        for (String term : new String[] {"sigmoid", "trigfunction",
                "tanh", "sampledelay", "multiplydivide"}) {
            JSONArray hits = lib.search(term, "", 0, 5, true);
            System.out.println("direct search('" + term + "') -> "
                    + (hits == null ? 0 : hits.length()) + " hits");
            if (hits != null) {
                for (int i = 0; i < hits.length(); i++) {
                    JSONObject row = hits.optJSONObject(i);
                    if (row == null) continue;
                    System.out.println("    " + row.optString("displayName")
                            + " (" + row.optString("className") + ")");
                }
            }
        }
        System.out.println();
        for (String goal : goals) {
            System.out.println("================================================");
            System.out.println("GOAL: " + goal);
            JSONObject recipe = CapabilityProbe.probe(goal, lib);
            System.out.println("matchedDomainKeywords: "
                    + recipe.optJSONArray("matchedDomainKeywords"));
            JSONArray actors = recipe.optJSONArray("recommendedActors");
            System.out.println("recommendedActors ("
                    + (actors == null ? 0 : actors.length()) + "):");
            if (actors != null) {
                for (int i = 0; i < actors.length(); i++) {
                    JSONObject a = actors.optJSONObject(i);
                    if (a == null) continue;
                    System.out.println("  - "
                            + a.optString("displayName") + " ("
                            + a.optString("className") + ")");
                }
            }
            System.out.println();
        }
        System.out.println("================================================");
        System.out.println("Class-failure tracker in-process test");
        try {
            PtolemySession sess = new PtolemySession();
            AgentResult load = sess.loadMoml(""
                    + "<entity name=\"T\" class=\"ptolemy.actor.TypedCompositeActor\">"
                    + "<property name=\"SDF Director\" class=\"ptolemy.domains.sdf.kernel.SDFDirector\"/>"
                    + "<entity name=\"Src\" class=\"ptolemy.actor.lib.Ramp\"/>"
                    + "<entity name=\"Exp1\" class=\"ptolemy.actor.lib.Expression\">"
                    + "<property name=\"expression\" class=\"ptolemy.data.expr.Parameter\" value=\"1.0\"/>"
                    + "</entity>"
                    + "</entity>");
            System.out.println("load.ok=" + load.ok() + " msg=" + load.message());
            ToolRegistry reg = ToolRegistry.defaultRegistry();
            String[][] bad = {
                    {"Src.output", "Exp1.input"},
                    {"Src.output", "Exp1.x"},
                    {"Src.output", "Exp1.in"}
            };
            for (String[] edge : bad) {
                JSONObject connectArgs = new JSONObject()
                        .put("from", edge[0]).put("to", edge[1]);
                AgentResult res = reg.dispatch("connect", sess,
                        connectArgs);
                System.out.println("  connect(" + edge[0] + ", "
                        + edge[1] + ") ok=" + res.ok()
                        + "; classFailures=" + sess.classFailures());
            }
            JSONObject ctx = ModelContext.forSession(sess);
            System.out.println();
            System.out.println("ModelContext.classFailures = "
                    + ctx.optJSONObject("classFailures"));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
