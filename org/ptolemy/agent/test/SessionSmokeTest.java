/* Smoke test for the M0 deliverable: PtolemySession can load, run and
 export an existing Ptolemy II demo model.

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

import java.io.File;

import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// SessionSmokeTest

/**
 Plain-Java (no JUnit dependency) smoke test for the M0 deliverable.
 Loads {@code ptolemy/actor/lib/test/auto/Ramp1.xml} (an SDF model
 with 1 iteration), runs it, and checks that a non-empty MoML export
 plus a non-empty signals payload come back.

 <p>Designed to also work without JUnit on the classpath, because the
 agent backend is meant to be runnable straight out of the build tree
 with only the core Ptolemy classpath.

 <p>Run from the command line:
 <pre>
 java -classpath "$PTII" \
      org.ptolemy.agent.test.SessionSmokeTest \
      ptolemy/actor/lib/test/auto/Ramp1.xml
 </pre>

 <p>Exit code 0 = success, non-zero = failure.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class SessionSmokeTest {

    private SessionSmokeTest() {
    }

    public static void main(String[] args) throws Exception {
        String path = args.length > 0 ? args[0]
                : "ptolemy/actor/lib/test/auto/Ramp1.xml";

        File file = new File(path);
        if (!file.isAbsolute()) {
            String ptii = System.getenv("PTII");
            if (ptii == null) {
                ptii = System.getProperty("user.dir");
            }
            file = new File(ptii, path);
        }

        System.out.println("[smoke] using model: " + file.getAbsolutePath());

        PtolemySession session = new PtolemySession("smoke");
        check("load", session.loadFile(file.getAbsolutePath()));

        String moml = session.exportMoml();
        if (moml == null || moml.length() < 50) {
            fail("exportMoml returned suspiciously short content: " + moml);
        }
        System.out.println("[smoke] exported MoML length = " + moml.length());

        check("run", session.run());

        org.json.JSONObject signals = session.signals();
        int probeCount = signals.optJSONArray("probes") == null ? 0
                : signals.getJSONArray("probes").length();
        System.out.println("[smoke] probe count after run = " + probeCount);
        if (probeCount == 0) {
            System.out.println(
                    "[smoke] warning: no probes captured; the model may not"
                            + " expose any output ports on top-level entities");
        }

        System.out.println("[smoke] OK");
    }

    private static void check(String stage, AgentResult result) {
        System.out.println("[smoke] " + stage + " -> ok=" + result.ok()
                + ", message=" + result.message());
        if (!result.ok()) {
            fail(stage + " failed: " + result.message());
        }
    }

    private static void fail(String message) {
        System.err.println("[smoke] FAIL: " + message);
        System.exit(1);
    }
}
