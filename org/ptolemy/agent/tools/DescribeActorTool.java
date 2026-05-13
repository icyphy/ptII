/* Tool that returns the full catalog entry for one Ptolemy component.

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
package org.ptolemy.agent.tools;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.library.LibraryIndex;
import org.ptolemy.agent.library.LibraryIndex.Entry;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// DescribeActorTool

/**
 Return the full scanned catalog entry for one actor or director.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class DescribeActorTool implements AgentTool {

    @Override
    public String name() {
        return "describe_actor";
    }

    @Override
    public String description() {
        return "Return full port and parameter metadata for one actor or"
                + " director by className or displayName.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("className", new JSONObject().put("type", "string")
                .put("description",
                        "Fully qualified className returned by list_library,"
                                + " or a displayName such as \"Ramp\"."));
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new JSONArray().put("className"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (!args.has("className")) {
            return AgentResult.fail("describe_actor requires 'className'");
        }
        String className = args.getString("className").trim();
        Entry entry = LibraryIndex.shared().find(className);
        if (entry == null) {
            return AgentResult.fail("actor not found in library: "
                    + className);
        }
        return AgentResult.ok("described " + entry.className,
                entry.toJson());
    }
}
