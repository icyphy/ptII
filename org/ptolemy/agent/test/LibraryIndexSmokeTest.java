/* Smoke test for the automatically scanned Ptolemy actor library.

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

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.library.LibraryIndex;
import org.ptolemy.agent.tools.ToolRegistry;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// LibraryIndexSmokeTest

/**
 Verifies that the agent library index is populated from the Ptolemy
 MoML library and that the LLM-facing tools can page and describe
 entries.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class LibraryIndexSmokeTest {

    private LibraryIndexSmokeTest() {
    }

    public static void main(String[] args) {
        LibraryIndex index = LibraryIndex.shared();
        JSONObject status = index.status();
        System.out.println("[library-smoke] status=" + status);
        if (!"scanned".equals(status.optString("source"))) {
            fail("expected scanned library, got " + status);
        }
        if (status.optInt("count") < 30) {
            fail("expected a substantial scanned library, got " + status);
        }
        if (index.find("ptolemy.actor.lib.Ramp") == null) {
            fail("Ramp not found by class name");
        }
        if (index.find("Scale") == null) {
            fail("Scale not found by display name");
        }

        JSONArray math = index.search("Scale", "Actors", 0, 5, false);
        if (math.length() == 0) {
            fail("search did not return Scale");
        }

        ToolRegistry registry = ToolRegistry.defaultRegistry();
        AgentResult listed = registry.dispatch("list_library", null,
                new JSONObject().put("query", "Ramp").put("limit", 5)
                        .put("summary", true));
        check("list_library", listed);

        AgentResult described = registry.dispatch("describe_actor", null,
                new JSONObject().put("className", "ptolemy.actor.lib.Ramp"));
        check("describe_actor", described);
        JSONObject data = described.data();
        if (!data.has("inputs") || !data.has("outputs")
                || !data.has("parameters")) {
            fail("describe_actor returned incomplete metadata: " + data);
        }

        System.out.println("[library-smoke] OK");
    }

    private static void check(String stage, AgentResult result) {
        System.out.println("[library-smoke] " + stage + " -> ok="
                + result.ok() + ", message=" + result.message());
        if (!result.ok()) {
            fail(stage + " failed: " + result.message());
        }
    }

    private static void fail(String message) {
        System.err.println("[library-smoke] FAIL: " + message);
        System.exit(1);
    }
}
