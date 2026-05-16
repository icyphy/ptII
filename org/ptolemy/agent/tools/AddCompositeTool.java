/* Tool that creates a TypedCompositeActor with named I/O ports so the
 agent can group unrelated logic into a single block.

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

import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// AddCompositeTool

/**
 Create a new {@code TypedCompositeActor} (a hierarchical "subsystem"
 block) with optional named input and output ports, optionally inside a
 parent composite. After creation, the agent populates the inside with
 {@code add_entity}/{@code connect}/etc. by passing
 {@code parent="<compositeName>"}.

 <p>Args:
 <ul>
 <li>{@code name} -- required, unique within the parent composite.</li>
 <li>{@code parent} -- optional, MoML name of an existing composite to
     hold this composite. Defaults to top-level.</li>
 <li>{@code inputs} -- optional array of port names (strings) to expose
     on the composite as input ports.</li>
 <li>{@code outputs} -- optional array of port names (strings) to expose
     on the composite as output ports.</li>
 <li>{@code x}, {@code y} -- optional canvas coordinates.</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class AddCompositeTool implements AgentTool {

    @Override
    public String name() {
        return "add_composite";
    }

    @Override
    public String description() {
        return "Create a hierarchical subsystem (TypedCompositeActor) with"
                + " named input/output ports. Use this to group unrelated"
                + " logic into a single block on the canvas. After"
                + " creation, drop entities inside it by passing"
                + " parent=\"<name>\" to add_entity / set_parameter /"
                + " delete and using \"<name>.<port>\" in connect.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("name", new JSONObject().put("type", "string")
                .put("description", "Composite name, e.g. \"FilterStage\"."));
        props.put("parent", new JSONObject().put("type", "string")
                .put("description",
                        "Optional name of an enclosing composite. Defaults"
                                + " to the top-level model."));
        props.put("inputs", new JSONObject().put("type", "array")
                .put("items", new JSONObject().put("type", "string"))
                .put("description",
                        "Optional list of input port names to expose."));
        props.put("outputs", new JSONObject().put("type", "array")
                .put("items", new JSONObject().put("type", "string"))
                .put("description",
                        "Optional list of output port names to expose."));
        props.put("x", new JSONObject().put("type", "integer")
                .put("description", "Optional X canvas coordinate.")
                .put("default", 100));
        props.put("y", new JSONObject().put("type", "integer")
                .put("description", "Optional Y canvas coordinate.")
                .put("default", 100));

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new JSONArray().put("name"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (!args.has("name")) {
            return AgentResult.fail("add_composite requires 'name'");
        }
        String entityName = AddEntityTool.sanitize(args.getString("name"));
        String parent = args.optString("parent", "").trim();
        int x = args.optInt("x", 120);
        int y = args.optInt("y", 120);
        if (session.toplevel() == null) {
            return AgentResult.fail("no model loaded");
        }
        try {
            CompositeEntity top = (CompositeEntity) session.toplevel();
            CompositeEntity scope = ToolScopeResolver.resolve(top, parent);
            if (scope.getEntity(entityName) != null) {
                return AgentResult.fail("name in use: '" + entityName
                        + "' already exists in scope");
            }
        } catch (IllegalArgumentException ex) {
            return AgentResult.fail(ex.getMessage());
        }

        StringBuilder body = new StringBuilder();
        body.append("<entity name=\"").append(AddEntityTool.escape(entityName))
                .append("\" class=\"ptolemy.actor.TypedCompositeActor\">")
                .append("<property name=\"_location\"")
                .append(" class=\"ptolemy.kernel.util.Location\"")
                .append(" value=\"[").append(x).append(".0, ").append(y)
                .append(".0]\"/>");

        JSONArray inputs = args.optJSONArray("inputs");
        if (inputs != null) {
            for (int i = 0; i < inputs.length(); i++) {
                String portName = AddEntityTool.sanitize(
                        String.valueOf(inputs.opt(i)));
                if (portName.length() == 0) {
                    continue;
                }
                body.append(_portMoml(portName, true, false));
            }
        }
        JSONArray outputs = args.optJSONArray("outputs");
        if (outputs != null) {
            for (int i = 0; i < outputs.length(); i++) {
                String portName = AddEntityTool.sanitize(
                        String.valueOf(outputs.opt(i)));
                if (portName.length() == 0) {
                    continue;
                }
                body.append(_portMoml(portName, false, true));
            }
        }
        body.append("</entity>");

        String moml = ToolScopeResolver.wrapInParent(parent, body.toString());

        AgentResult res = session.applyChange(moml);
        if (!res.ok()) {
            return res;
        }
        JSONObject data = new JSONObject();
        data.put("name", entityName);
        data.put("parent", parent);
        return AgentResult.ok("created composite " + entityName
                + (parent.length() == 0 ? "" : " inside " + parent), data);
    }

    private static String _portMoml(String portName, boolean isInput,
            boolean isOutput) {
        StringBuilder out = new StringBuilder();
        out.append("<port name=\"").append(AddEntityTool.escape(portName))
                .append("\" class=\"ptolemy.actor.TypedIOPort\">");
        if (isInput) {
            out.append("<property name=\"input\"/>");
        }
        if (isOutput) {
            out.append("<property name=\"output\"/>");
        }
        out.append("</port>");
        return out.toString();
    }
}
