/* Semantic preflight checks for agent tool calls.

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

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ToolCallValidator

/**
 Best-effort semantic validation before a tool mutates the session.
 These checks produce clear, structured failures for common LLM errors
 (missing parent, wrong port, nonexistent entity) while leaving Ptolemy
 as the final authority for type and MoML semantics.
 */
final class ToolCallValidator {

    private ToolCallValidator() {
    }

    /** Validate a tool call before execution.
     *  @return null when valid, otherwise a failure AgentResult. */
    static AgentResult validate(String name, PtolemySession session,
            JSONObject args) {
        JSONArray issues = new JSONArray();
        JSONObject safeArgs = args == null ? new JSONObject() : args;
        if ("add_entity".equals(name)) {
            _validateAddEntity(session, safeArgs, issues);
        } else if ("connect".equals(name)) {
            _validateConnect(session, safeArgs, issues);
        } else if ("set_parameter".equals(name)) {
            _validateSetParameter(session, safeArgs, issues);
        } else if ("delete".equals(name)) {
            _validateDelete(session, safeArgs, issues);
        } else if ("group_into_composite".equals(name)) {
            _validateGroup(session, safeArgs, issues);
        }
        if (issues.length() == 0) {
            return null;
        }
        JSONObject data = new JSONObject();
        data.put("validator", "tool-call-preflight-v1");
        data.put("tool", name);
        data.put("issues", issues);
        return AgentResult.fail("tool preflight failed: "
                + issues.optJSONObject(0).optString("message"), data);
    }

    private static void _validateAddEntity(PtolemySession session,
            JSONObject args, JSONArray issues) {
        _require(args, "name", issues);
        _require(args, "className", issues);
        String className = args.optString("className", "").trim();
        if (className.length() > 0) {
            try {
                Class.forName(className);
            } catch (Throwable t) {
                _issue(issues, "CLASS_NOT_FOUND", "className",
                        "actor/director class is not on the classpath: "
                                + className);
            }
        }
        _scope(session, args.optString("parent", ""), issues);
    }

    private static void _validateConnect(PtolemySession session,
            JSONObject args, JSONArray issues) {
        _require(args, "from", issues);
        _require(args, "to", issues);
        CompositeEntity scope = _scope(session, args.optString("parent", ""),
                issues);
        if (scope == null) {
            return;
        }
        String from = args.optString("from", "").trim();
        String to = args.optString("to", "").trim();
        IOPort source = ConnectTool.resolvePort(scope, from);
        if (source == null) {
            _issue(issues, "SOURCE_PORT_NOT_FOUND", "from",
                    "source port not found in scope: " + from);
        } else if (!(source.isOutput()
                || (source.getContainer() == scope && source.isInput()))) {
            _issue(issues, "SOURCE_NOT_OUTPUT", "from",
                    "source is not an output/boundary input: " + from);
        }
        IOPort dest = ConnectTool.resolvePort(scope, to);
        if (dest == null) {
            _issue(issues, "DEST_PORT_NOT_FOUND", "to",
                    "destination port not found in scope: " + to);
        } else if (!(dest.isInput()
                || (dest.getContainer() == scope && dest.isOutput()))) {
            _issue(issues, "DEST_NOT_INPUT", "to",
                    "destination is not an input/boundary output: " + to);
        }
    }

    private static void _validateSetParameter(PtolemySession session,
            JSONObject args, JSONArray issues) {
        _require(args, "entity", issues);
        _require(args, "parameter", issues);
        _require(args, "value", issues);
        CompositeEntity scope = _scope(session, args.optString("parent", ""),
                issues);
        if (scope == null) {
            return;
        }
        String entity = args.optString("entity", "").trim();
        String param = args.optString("parameter", "").trim();
        NamedObj target = _target(scope, entity);
        if (target == null) {
            _issue(issues, "ENTITY_NOT_FOUND", "entity",
                    "entity/property not found in scope: " + entity);
        } else if (param.length() > 0 && target.getAttribute(param) == null) {
            _issue(issues, "PARAMETER_NOT_FOUND", "parameter",
                    "parameter not found on " + entity + ": " + param);
        }
    }

    private static void _validateDelete(PtolemySession session,
            JSONObject args, JSONArray issues) {
        _require(args, "name", issues);
        CompositeEntity scope = _scope(session, args.optString("parent", ""),
                issues);
        if (scope == null) {
            return;
        }
        String name = args.optString("name", "").trim();
        if (name.length() > 0 && scope.getEntity(name) == null) {
            _issue(issues, "ENTITY_NOT_FOUND", "name",
                    "entity not found in scope: " + name);
        }
    }

    private static void _validateGroup(PtolemySession session,
            JSONObject args, JSONArray issues) {
        _require(args, "name", issues);
        if (!args.has("members") || !(args.opt("members") instanceof
                JSONArray)) {
            _issue(issues, "MISSING_REQUIRED", "members",
                    "missing required array: members");
            return;
        }
        CompositeEntity scope = _scope(session, args.optString("parent", ""),
                issues);
        if (scope == null) {
            return;
        }
        JSONArray members = args.optJSONArray("members");
        if (members == null || members.length() == 0) {
            _issue(issues, "EMPTY_MEMBERS", "members",
                    "members must contain at least one entity");
            return;
        }
        for (int i = 0; i < members.length(); i++) {
            String member = members.optString(i, "").trim();
            if (member.length() == 0 || scope.getEntity(member) == null) {
                _issue(issues, "MEMBER_NOT_FOUND", "members[" + i + "]",
                        "member not found in scope: " + member);
            }
        }
    }

    private static CompositeEntity _scope(PtolemySession session,
            String parent, JSONArray issues) {
        if (session == null || session.toplevel() == null) {
            _issue(issues, "NO_MODEL", "session", "no model loaded");
            return null;
        }
        CompositeEntity scope = (CompositeEntity) session.toplevel();
        String p = parent == null ? "" : parent.trim();
        if (p.length() == 0) {
            return scope;
        }
        ComponentEntity child = scope.getEntity(p);
        if (!(child instanceof CompositeEntity)) {
            _issue(issues, "PARENT_NOT_FOUND", "parent",
                    "no composite named '" + p + "' at top level");
            return null;
        }
        return (CompositeEntity) child;
    }

    private static NamedObj _target(CompositeEntity scope, String name) {
        if (scope == null || name == null || name.length() == 0) {
            return null;
        }
        ComponentEntity entity = scope.getEntity(name);
        if (entity != null) {
            return entity;
        }
        return scope.getAttribute(name);
    }

    private static void _require(JSONObject args, String key,
            JSONArray issues) {
        if (!args.has(key) || args.optString(key, "").trim().length() == 0) {
            _issue(issues, "MISSING_REQUIRED", key,
                    "missing required argument: " + key);
        }
    }

    private static void _issue(JSONArray issues, String code, String field,
            String message) {
        issues.put(new JSONObject().put("code", code)
                .put("field", field).put("message", message));
    }
}
