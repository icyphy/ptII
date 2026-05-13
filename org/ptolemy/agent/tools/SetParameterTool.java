/* Tool that sets a parameter value on an existing entity.

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
import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////////
//// SetParameterTool

/**
 Set the value of an existing parameter, e.g. tune {@code init}, {@code
 step}, {@code Kp}, {@code Ki}, ... on an actor. The change is
 expressed as a MoML {@code <property value="..."/>} fragment scoped to
 the target entity, so existing Ptolemy semantics (expression
 evaluation, type coercion, parameter dependencies) apply unchanged.

 <p>Args:
 <ul>
 <li>{@code entity} -- required, dotted MoML path of the owning entity,
     e.g. {@code "Ramp1"} or {@code "PID.Kp"}</li>
 <li>{@code parameter} -- required, parameter name on that entity</li>
 <li>{@code value} -- required, the new value as a Ptolemy
     expression string (numbers, expressions, references all OK)</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class SetParameterTool implements AgentTool {

    @Override
    public String name() {
        return "set_parameter";
    }

    @Override
    public String description() {
        return "Set a parameter value on an existing entity. The value is"
                + " a Ptolemy expression string (e.g. \"1.0\","
                + " \"2*pi\", or \"PI/4\").";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("entity", new JSONObject().put("type", "string")
                .put("description",
                        "Name of the entity holding the parameter."));
        props.put("parameter", new JSONObject().put("type", "string")
                .put("description",
                        "Parameter name, e.g. \"init\", \"step\", \"Kp\"."));
        props.put("value", new JSONObject().put("type", "string")
                .put("description",
                        "New value as a Ptolemy expression string."));
        props.put("parent", new JSONObject().put("type", "string")
                .put("description",
                        "Optional name of the enclosing composite when the"
                                + " entity lives inside one. Defaults to"
                                + " the top-level model."));

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new JSONArray().put("entity")
                .put("parameter").put("value"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (!args.has("entity") || !args.has("parameter")
                || !args.has("value")) {
            return AgentResult.fail(
                    "set_parameter requires 'entity', 'parameter', 'value'");
        }
        String entity = args.getString("entity").trim();
        String param = args.getString("parameter").trim();
        String value = args.getString("value");
        String parent = args.optString("parent", "").trim();

        // Idempotency: if the parameter is already at this exact value,
        // skip the MoML change request. Avoids retry storms when an
        // earlier round already set the same value.
        String currentValue = _currentParamValue(session, parent, entity,
                param);
        if (currentValue != null && currentValue.equals(value)) {
            JSONObject data = new JSONObject();
            data.put("entity", entity);
            data.put("parameter", param);
            data.put("value", value);
            data.put("noop", true);
            return AgentResult.ok("noop: " + entity + "." + param
                    + " is already " + value, data);
        }

        String inner;
        if (parent.length() == 0 && _isTopLevelDirector(session, entity)) {
            // Directors are top-level properties, not child entities.
            inner = "<property name=\"" + AddEntityTool.escape(entity) + "\">"
                    + "<property name=\"" + AddEntityTool.escape(param) + "\""
                    + " value=\"" + AddEntityTool.escape(value) + "\"/>"
                    + "</property>";
        } else {
            inner = "<entity name=\"" + AddEntityTool.escape(entity) + "\">"
                    + "<property name=\"" + AddEntityTool.escape(param) + "\""
                    + " value=\"" + AddEntityTool.escape(value) + "\"/>"
                    + "</entity>";
        }

        String moml = parent.length() == 0
                ? inner
                : "<entity name=\"" + AddEntityTool.escape(parent) + "\">"
                        + inner + "</entity>";

        AgentResult res = session.applyChange(moml);
        if (!res.ok()) {
            return res;
        }
        return AgentResult.ok(
                "set " + (parent.length() == 0 ? "" : parent + ".")
                        + entity + "." + param + " = " + value);
    }

    private static boolean _isTopLevelDirector(PtolemySession session,
            String name) {
        if (session == null || session.toplevel() == null || name == null) {
            return false;
        }
        CompositeEntity top = (CompositeEntity) session.toplevel();
        Attribute attr = top.getAttribute(name);
        if (attr == null) {
            return false;
        }
        String klass = attr.getClassName();
        return klass != null && klass.endsWith("Director");
    }

    /** Best-effort lookup of the current expression / value of the
     *  named parameter on the named entity. Returns null when the
     *  entity or parameter cannot be resolved, which conservatively
     *  causes the caller to fall back to the normal MoML change. */
    private static String _currentParamValue(PtolemySession session,
            String parent, String entity, String param) {
        try {
            if (session == null || session.toplevel() == null) {
                return null;
            }
            CompositeEntity scope = (CompositeEntity) session.toplevel();
            if (parent != null && parent.length() > 0) {
                ptolemy.kernel.ComponentEntity p = scope.getEntity(parent);
                if (p instanceof CompositeEntity) {
                    scope = (CompositeEntity) p;
                }
            }
            Attribute target;
            if (parent == null || parent.length() == 0) {
                // Could be a top-level director (property) OR a regular
                // entity reference.
                if (_isTopLevelDirector(session, entity)) {
                    Attribute dir = scope.getAttribute(entity);
                    target = dir == null ? null : dir.getAttribute(param);
                } else {
                    ptolemy.kernel.ComponentEntity e = scope.getEntity(
                            entity);
                    target = e == null ? null : e.getAttribute(param);
                }
            } else {
                ptolemy.kernel.ComponentEntity e = scope.getEntity(entity);
                target = e == null ? null : e.getAttribute(param);
            }
            if (target instanceof ptolemy.kernel.util.Settable) {
                return ((ptolemy.kernel.util.Settable) target)
                        .getExpression();
            }
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
