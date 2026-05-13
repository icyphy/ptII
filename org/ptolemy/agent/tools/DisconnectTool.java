/* Tool that removes a relation, deleting the corresponding canvas edge.

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
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// DisconnectTool

/**
 Delete a relation. The two ports previously connected through it are
 left alone, just no longer linked. Mirrors what the React Flow canvas
 emits when the user presses Delete on an edge.

 <p>Args:
 <ul>
 <li>{@code relation} -- required, name of the relation to remove.</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class DisconnectTool implements AgentTool {

    @Override
    public String name() {
        return "disconnect";
    }

    @Override
    public String description() {
        return "Delete a relation, removing the corresponding edge from"
                + " the model.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("relation", new JSONObject().put("type", "string")
                .put("description",
                        "Name of the relation to delete, as shown in MoML."));
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new JSONArray().put("relation"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (!args.has("relation")) {
            return AgentResult.fail("disconnect requires 'relation'");
        }
        String rel = args.getString("relation").trim();
        String moml = "<deleteRelation name=\""
                + AddEntityTool.escape(rel) + "\"/>";
        AgentResult res = session.applyChange(moml);
        if (!res.ok()) {
            return res;
        }
        return AgentResult.ok("disconnected " + rel);
    }
}
