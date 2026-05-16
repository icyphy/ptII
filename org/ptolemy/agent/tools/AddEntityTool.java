/* Tool that instantiates a new actor inside the top-level composite of
 the target Ptolemy session.

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

import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// AddEntityTool

/**
 Instantiates a new entity (actor) inside the top-level composite of
 a session. Equivalent to dropping an actor on the Vergil canvas.

 <p>Args:
 <ul>
 <li>{@code name} -- required, unique within the parent composite</li>
 <li>{@code className} -- required, fully-qualified Java class name
     of the actor (e.g. {@code ptolemy.actor.lib.Ramp})</li>
 <li>{@code x}, {@code y} -- optional integer location for the
     graphical view; defaults to {@code 100,100}</li>
 </ul>

 <p>Internally emits a MoML {@code <entity .../>} fragment via
 {@link PtolemySession#applyChange(String)} so the change is undoable
 and observable by every {@code ChangeListener}.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class AddEntityTool implements AgentTool {

    @Override
    public String name() {
        return "add_entity";
    }

    @Override
    public String description() {
        return "Instantiate a Ptolemy II actor by class name and place it"
                + " on the canvas at the given (x,y) location.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("name", new JSONObject().put("type", "string")
                .put("description",
                        "Unique name within the parent composite,"
                                + " e.g. \"ramp1\" or \"gain\"."));
        props.put("className", new JSONObject().put("type", "string")
                .put("description",
                        "Fully qualified actor class name,"
                                + " e.g. \"ptolemy.actor.lib.Ramp\""
                                + " or \"ptolemy.actor.lib.Gaussian\"."));
        props.put("parent", new JSONObject().put("type", "string")
                .put("description",
                        "Optional name of an enclosing composite created"
                                + " via add_composite. Defaults to the"
                                + " top-level model."));
        props.put("x", new JSONObject().put("type", "integer")
                .put("description", "Optional X canvas coordinate.")
                .put("default", 100));
        props.put("y", new JSONObject().put("type", "integer")
                .put("description", "Optional Y canvas coordinate.")
                .put("default", 100));

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new org.json.JSONArray()
                .put("name").put("className"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (!args.has("name") || !args.has("className")) {
            return AgentResult.fail(
                    "add_entity requires both 'name' and 'className'");
        }
        String entityName = sanitize(args.getString("name"));
        String className = args.getString("className").trim();
        String parent = args.optString("parent", "").trim();
        int x = args.optInt("x", 100);
        int y = args.optInt("y", 100);

        // Directors live as <property> nodes on a CompositeActor, not
        // as child entities. Auto-detect by class-name suffix so callers
        // can use the same tool to add either actors or directors.
        boolean isDirector = className.endsWith("Director");
        String tag = isDirector ? "property" : "entity";

        // Idempotency check: if an entity with this name already exists
        // in the target scope AND its class matches, this is a no-op
        // success. This eliminates the most common thrash pattern: the
        // agent re-adds something after a panicked validate, then sees
        // it "fail" (name in use), then deletes and re-adds again.
        if (session.toplevel() instanceof
                ptolemy.kernel.CompositeEntity) {
            ptolemy.kernel.CompositeEntity scope =
                    (ptolemy.kernel.CompositeEntity) session.toplevel();
            if (parent.length() > 0) {
                try {
                    scope = ToolScopeResolver.resolve(scope, parent);
                } catch (IllegalArgumentException ex) {
                    return AgentResult.fail(ex.getMessage());
                }
            }
            if (isDirector) {
                ptolemy.kernel.util.Attribute prop = scope.getAttribute(
                        entityName);
                if (prop != null) {
                    String existingClass = prop.getClassName();
                    if (existingClass != null
                            && existingClass.equals(className)) {
                        JSONObject data = new JSONObject();
                        data.put("name", entityName);
                        data.put("className", className);
                        data.put("parent", parent);
                        data.put("kind", "director");
                        data.put("noop", true);
                        return AgentResult.ok("noop: director "
                                + entityName
                                + " already present with same class",
                                data);
                    }
                    return AgentResult.fail("name in use: property '"
                            + entityName + "' already exists with"
                            + " class " + existingClass
                            + ". Use set_parameter to change its"
                            + " parameters; only delete + re-add if"
                            + " you really need a different class.");
                }
            } else {
                ptolemy.kernel.ComponentEntity existing =
                        scope.getEntity(entityName);
                if (existing != null) {
                    String existingClass = existing.getClassName();
                    if (existingClass != null
                            && existingClass.equals(className)) {
                        JSONObject data = new JSONObject();
                        data.put("name", entityName);
                        data.put("className", className);
                        data.put("parent", parent);
                        data.put("kind", "actor");
                        data.put("noop", true);
                        return AgentResult.ok("noop: " + entityName
                                + " already exists with same class",
                                data);
                    }
                    return AgentResult.fail("name in use: '"
                            + entityName + "' already exists with"
                            + " class " + existingClass
                            + ". Use set_parameter to change its"
                            + " parameters; only delete + re-add if"
                            + " you really need a different class.");
                }
            }
        }
        String inner = "<" + tag + " name=\"" + escape(entityName) + "\""
                + " class=\"" + escape(className) + "\">"
                + "<property name=\"_location\""
                + " class=\"ptolemy.kernel.util.Location\""
                + " value=\"[" + x + ".0, " + y + ".0]\"/>"
                + "</" + tag + ">";

        String moml = ToolScopeResolver.wrapInParent(parent, inner);

        AgentResult res = session.applyChange(moml);
        if (!res.ok()) {
            return res;
        }
        JSONObject data = new JSONObject();
        data.put("name", entityName);
        data.put("className", className);
        data.put("parent", parent);
        data.put("kind", isDirector ? "director" : "actor");
        return AgentResult.ok("added "
                + (isDirector ? "director " : "")
                + entityName + " (" + className + ")"
                + (parent.length() == 0 ? "" : " inside " + parent),
                data);
    }

    static String sanitize(String name) {
        StringBuilder out = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            // MoML names may contain spaces (e.g. "SDF Director") and
            // are XML-escaped on emit, so we preserve them.  Dashes
            // are still converted to underscores because Ptolemy
            // expression parsers treat '-' as a subtraction operator
            // when actor names appear in formulas.
            if (Character.isLetterOrDigit(c) || c == '_' || c == ' ') {
                out.append(c);
            } else if (c == '-') {
                out.append('_');
            }
        }
        if (out.length() == 0) {
            out.append("entity");
        }
        return out.toString();
    }

    static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}
