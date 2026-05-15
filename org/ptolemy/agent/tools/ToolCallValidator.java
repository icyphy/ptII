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

 <p>Every failure carries a structured payload with these keys when
 applicable, so the LLM can correct itself without another tool round:
 <ul>
 <li>{@code availablePorts} — actual {@code {inputs, outputs}} of the
     entity referenced by the bad argument.</li>
 <li>{@code availableEntities} — sibling entity names in the same
     scope.</li>
 <li>{@code availableComposites} — composite children of the top level
     (used when {@code parent} is wrong).</li>
 <li>{@code didYouMean} — up to three closest candidates ranked by a
     bucketed Levenshtein metric.</li>
 <li>{@code actualDirection} — for direction errors, the port's real
     direction.</li>
 </ul>

 <p>The same hints are mirrored as a short inline phrase in the
 top-level error message so LLMs that only read {@code message} still
 see the correction.
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
        JSONObject first = issues.optJSONObject(0);
        String headline = first == null ? "preflight failed"
                : first.optString("message", "preflight failed");
        JSONArray dym = first == null ? null
                : first.optJSONArray("didYouMean");
        if (dym != null && dym.length() > 0) {
            headline = headline + " — did you mean "
                    + _joinFirstTwo(dym) + "?";
        }
        return AgentResult.fail("tool preflight failed: " + headline, data);
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
            _portIssue(issues, "from", from, scope, "output",
                    "source port not found in scope");
        } else if (!(source.isOutput()
                || (source.getContainer() == scope && source.isInput()))) {
            _directionIssue(issues, "from", from, source, scope, "output",
                    "source is not an output/boundary input");
        }
        IOPort dest = ConnectTool.resolvePort(scope, to);
        if (dest == null) {
            _portIssue(issues, "to", to, scope, "input",
                    "destination port not found in scope");
        } else if (!(dest.isInput()
                || (dest.getContainer() == scope && dest.isOutput()))) {
            _directionIssue(issues, "to", to, dest, scope, "input",
                    "destination is not an input/boundary output");
        }
        // Cross-check: if both ports resolved but the directions are
        // exactly reversed, surface a single "swap from/to" hint that
        // is faster for the LLM to act on than two separate errors.
        if (source != null && dest != null
                && issues.length() == 0
                && (dest.isOutput() && source.isInput())) {
            JSONObject row = _issue(issues, "REVERSED_DIRECTION", "from",
                    "from/to look reversed: '" + from + "' is an input and"
                            + " '" + to + "' is an output");
            row.put("didYouMean", new JSONArray()
                    .put("connect(from=\"" + to + "\", to=\"" + from
                            + "\")"));
            row.put("suggestion", "swap from and to");
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
            JSONObject row = _issue(issues, "ENTITY_NOT_FOUND", "entity",
                    "entity/property not found in scope: " + entity);
            row.put("availableEntities", PortHints.entityNames(scope));
            row.put("didYouMean", PortHints.suggestEntities(scope, entity));
        } else if (param.length() > 0 && target.getAttribute(param) == null) {
            JSONObject row = _issue(issues, "PARAMETER_NOT_FOUND",
                    "parameter",
                    "parameter not found on " + entity + ": " + param);
            row.put("didYouMean",
                    PortHints.suggestAttributes(target, param));
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
            JSONObject row = _issue(issues, "ENTITY_NOT_FOUND", "name",
                    "entity not found in scope: " + name);
            row.put("availableEntities", PortHints.entityNames(scope));
            row.put("didYouMean", PortHints.suggestEntities(scope, name));
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
        JSONArray available = PortHints.entityNames(scope);
        for (int i = 0; i < members.length(); i++) {
            String member = members.optString(i, "").trim();
            if (member.length() == 0 || scope.getEntity(member) == null) {
                JSONObject row = _issue(issues, "MEMBER_NOT_FOUND",
                        "members[" + i + "]",
                        "member not found in scope: " + member);
                row.put("availableEntities", available);
                row.put("didYouMean",
                        PortHints.suggestEntities(scope, member));
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
            JSONObject row = _issue(issues, "PARENT_NOT_FOUND", "parent",
                    "no composite named '" + p + "' at top level");
            row.put("availableComposites",
                    PortHints.compositeNames(scope));
            row.put("didYouMean", PortHints.suggestComposites(scope, p));
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

    /** Emit a {@code XXX_PORT_NOT_FOUND} issue with full
     *  available-ports / did-you-mean context.  Distinguishes between
     *  "entity prefix unknown" and "entity known but port name wrong"
     *  because the right hint differs. */
    private static void _portIssue(JSONArray issues, String field,
            String reference, CompositeEntity scope, String wantDirection,
            String headline) {
        String code = "from".equals(field) ? "SOURCE_PORT_NOT_FOUND"
                : "DEST_PORT_NOT_FOUND";
        String[] parts = PortHints.splitRef(reference);
        String entityPrefix = parts[0];
        ComponentEntity entity = entityPrefix.length() > 0
                ? scope.getEntity(entityPrefix) : null;

        JSONObject row;
        if (entityPrefix.length() > 0 && entity == null) {
            // Entity prefix itself is wrong.  Demote the message and
            // surface entity-level suggestions, which is what the LLM
            // actually needs.
            String entCode = "from".equals(field) ? "SOURCE_ENTITY_NOT_FOUND"
                    : "DEST_ENTITY_NOT_FOUND";
            row = _issue(issues, entCode, field,
                    "entity '" + entityPrefix + "' from "
                            + reference + " does not exist in scope");
            row.put("availableEntities", PortHints.entityNames(scope));
            row.put("didYouMean",
                    PortHints.suggestEntities(scope, entityPrefix));
        } else {
            row = _issue(issues, code, field,
                    headline + ": " + reference);
            JSONObject portsDescription = PortHints.describePorts(scope,
                    entityPrefix);
            if (portsDescription.length() > 0) {
                row.put("availablePorts", portsDescription);
            }
            row.put("didYouMean", PortHints.suggestPorts(scope, reference,
                    wantDirection));
        }
    }

    /** Emit a direction issue.  Includes the port's actual direction
     *  and, when possible, the closest legal port reference matching
     *  the desired direction. */
    private static void _directionIssue(JSONArray issues, String field,
            String reference, IOPort port, CompositeEntity scope,
            String wantDirection, String headline) {
        String code = "from".equals(field) ? "SOURCE_NOT_OUTPUT"
                : "DEST_NOT_INPUT";
        JSONObject row = _issue(issues, code, field,
                headline + ": " + reference);
        String actual;
        if (port.isInput() && port.isOutput()) {
            actual = "both";
        } else if (port.isInput()) {
            actual = "input";
        } else if (port.isOutput()) {
            actual = "output";
        } else {
            actual = "neither";
        }
        row.put("actualDirection", actual);
        JSONObject portsDescription = PortHints.describePorts(scope,
                PortHints.splitRef(reference)[0]);
        if (portsDescription.length() > 0) {
            row.put("availablePorts", portsDescription);
        }
        row.put("didYouMean", PortHints.suggestPorts(scope, reference,
                wantDirection));
    }

    private static void _require(JSONObject args, String key,
            JSONArray issues) {
        if (!args.has(key) || args.optString(key, "").trim().length() == 0) {
            _issue(issues, "MISSING_REQUIRED", key,
                    "missing required argument: " + key);
        }
    }

    private static JSONObject _issue(JSONArray issues, String code,
            String field, String message) {
        JSONObject row = new JSONObject().put("code", code)
                .put("field", field).put("message", message);
        issues.put(row);
        return row;
    }

    /** Format the first one or two suggestion entries from a JSONArray
     *  as a short human-readable phrase: {@code "'plus'"}, or
     *  {@code "'plus' or 'minus'"}.  Empty input returns the empty
     *  string and the caller is expected to skip the "did you mean"
     *  suffix. */
    private static String _joinFirstTwo(JSONArray suggestions) {
        if (suggestions == null || suggestions.length() == 0) {
            return "";
        }
        String first = suggestions.optString(0, "");
        if (suggestions.length() == 1 || suggestions.optString(1, "")
                .length() == 0) {
            return "'" + first + "'";
        }
        return "'" + first + "' or '"
                + suggestions.optString(1, "") + "'";
    }
}
