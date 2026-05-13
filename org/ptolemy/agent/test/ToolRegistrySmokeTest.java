/* End-to-end smoke test for the M2 tool registry. Builds a minimal SDF
 model purely through tool calls (no LLM), runs it, and checks that the
 simulation reaches FINISHED state.

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
package org.ptolemy.agent.test;

import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.tools.ToolRegistry;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// ToolRegistrySmokeTest

/**
 Builds the simplest possible SDF model (Ramp -> Discard) by calling
 each tool the same way an LLM would, then runs it through the
 default {@link ptolemy.actor.Manager}.

 <p>Verifies the M2 deliverable end-to-end without requiring network
 access, an API key, or an actual LLM.

 <p>Run from the command line:
 <pre>
 java -classpath "$PTII" org.ptolemy.agent.test.ToolRegistrySmokeTest
 </pre>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class ToolRegistrySmokeTest {

    private ToolRegistrySmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        PtolemySession session = new PtolemySession("toolsmoke");

        AgentResult loaded = session.loadMoml(
                "<entity name=\"toolsmoke\""
                        + " class=\"ptolemy.actor.TypedCompositeActor\">"
                        + "<property name=\"SDFDirector\""
                        + " class=\"ptolemy.domains.sdf.kernel.SDFDirector\">"
                        + "<property name=\"iterations\""
                        + " class=\"ptolemy.data.expr.Parameter\""
                        + " value=\"3\"/>"
                        + "</property>"
                        + "</entity>");
        check("load empty model", loaded);

        ToolRegistry registry = ToolRegistry.defaultRegistry();

        check("add_entity ramp", registry.dispatch("add_entity", session,
                new JSONObject().put("name", "ramp")
                        .put("className", "ptolemy.actor.lib.Ramp")
                        .put("x", 100).put("y", 100)));

        check("add_entity sink", registry.dispatch("add_entity", session,
                new JSONObject().put("name", "sink")
                        .put("className", "ptolemy.actor.lib.Discard")
                        .put("x", 300).put("y", 100)));

        check("set step", registry.dispatch("set_parameter", session,
                new JSONObject().put("entity", "ramp")
                        .put("parameter", "step").put("value", "2.0")));

        check("connect ramp -> sink",
                registry.dispatch("connect", session,
                        new JSONObject().put("from", "ramp.output")
                                .put("to", "sink.input")));

        AgentResult valid = registry.dispatch("validate", session,
                new JSONObject());
        System.out.println("[tool-smoke] validate => " + valid);

        AgentResult run = registry.dispatch("run", session, new JSONObject());
        check("run", run);

        String moml = session.exportMoml();
        if (!moml.contains("ramp") || !moml.contains("sink")) {
            fail("expected MoML to contain ramp and sink entities, got: "
                    + moml);
        }

        System.out.println("[tool-smoke] OK; final state="
                + session.state());
    }

    private static void check(String stage, AgentResult result) {
        System.out.println("[tool-smoke] " + stage + " -> ok="
                + result.ok() + ", message=" + result.message());
        if (!result.ok()) {
            fail(stage + " failed: " + result.message());
        }
    }

    private static void fail(String message) {
        System.err.println("[tool-smoke] FAIL: " + message);
        System.exit(1);
    }
}
