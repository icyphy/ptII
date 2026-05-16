/* Tool that removes an entity from the top-level composite.

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
//// DeleteTool

/**
 Remove an entity from the top-level composite. Internally emits a
 MoML {@code <deleteEntity name="..."/>} which Ptolemy expands into
 the corresponding removal of links and relations.

 <p>Args:
 <ul>
 <li>{@code name} -- required, the entity name as it appears in MoML.</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class DeleteTool implements AgentTool {

    @Override
    public String name() {
        return "delete";
    }

    @Override
    public String description() {
        return "Remove an entity (and the relations linked through it)"
                + " from the model.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("name", new JSONObject().put("type", "string")
                .put("description", "Name of the entity to delete."));
        props.put("parent", new JSONObject().put("type", "string")
                .put("description",
                        "Optional name of the enclosing composite when the"
                                + " entity lives inside one. Defaults to"
                                + " the top-level model."));
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new JSONArray().put("name"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (!args.has("name")) {
            return AgentResult.fail("delete requires 'name'");
        }
        if (session.toplevel() == null) {
            return AgentResult.fail("no model loaded");
        }
        String entity = args.getString("name").trim();
        String parent = args.optString("parent", "").trim();
        try {
            ToolScopeResolver.resolve((ptolemy.kernel.CompositeEntity)
                    session.toplevel(), parent);
        } catch (IllegalArgumentException ex) {
            return AgentResult.fail(ex.getMessage());
        }
        String inner = "<deleteEntity name=\""
                + AddEntityTool.escape(entity) + "\"/>";
        String moml = ToolScopeResolver.wrapInParent(parent, inner);
        AgentResult res = session.applyChange(moml);
        if (!res.ok()) {
            return res;
        }
        return AgentResult.ok("deleted "
                + (parent.length() == 0 ? "" : parent + ".") + entity);
    }
}
