/* Registry that owns every AgentTool the backend supports and exposes
 them either by name (for HTTP and the agent loop) or as an
 OpenAI-compatible tools array (for LLM function calling).

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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// ToolRegistry

/**
 Process-wide registry of {@link AgentTool} instances. Look-up is by
 the tool's {@link AgentTool#name() name}; ordering is insertion order
 so the LLM sees a stable, predictable tool list. Thread-safe for the
 typical use of register-once-at-startup-then-read.

 <p>The {@link #defaultRegistry()} static builder constructs the M2
 default set:
 {@code add_entity}, {@code connect}, {@code set_parameter},
 {@code delete}, {@code run}, {@code list_library},
 {@code describe_actor}, {@code validate}.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class ToolRegistry {

    private final Map<String, AgentTool> _byName = new LinkedHashMap<>();

    /** Build an empty registry. Use {@link #defaultRegistry()} for the
     *  M2 default toolset. */
    public ToolRegistry() {
    }

    /** Register a tool. Replaces any previous registration with the
     *  same {@link AgentTool#name()}. */
    public ToolRegistry register(AgentTool tool) {
        _byName.put(tool.name(), tool);
        return this;
    }

    /** @return The tool with the given name, or null if no such tool. */
    public AgentTool find(String name) {
        return _byName.get(name);
    }

    /** @return Every registered tool, in registration order. */
    public Collection<AgentTool> all() {
        return _byName.values();
    }

    /** Execute a tool by name.
     *  @param name Tool name.
     *  @param session Target session.
     *  @param args Arguments, may be null (treated as empty).
     *  @return The tool's result, or a failure if the tool is unknown.
     */
    public AgentResult dispatch(String name, PtolemySession session,
            JSONObject args) {
        AgentTool tool = _byName.get(name);
        if (tool == null) {
            return AgentResult.fail("unknown tool: " + name);
        }
        AgentResult result;
        try {
            JSONObject safeArgs = args == null ? new JSONObject() : args;
            if (!"validate".equals(name)
                    && (safeArgs.optBoolean("_dryRun", false)
                            || safeArgs.optBoolean("dryRun", false))) {
                JSONObject dryArgs = new JSONObject(safeArgs.toString());
                dryArgs.remove("_dryRun");
                dryArgs.remove("dryRun");
                boolean lightweightAutoLayout = "auto_layout".equals(name);
                return session.dryRunTool(this, name, dryArgs,
                        lightweightAutoLayout);
            }
            AgentResult preflight = ToolCallValidator.validate(name, session,
                    safeArgs);
            if (preflight != null) {
                result = preflight;
            } else {
                result = tool.execute(session, safeArgs);
            }
        } catch (Throwable t) {
            result = AgentResult.fail(name + " crashed: " + t.getMessage());
        }
        if (result != null && !result.ok() && session != null) {
            _accrueClassFailures(name, session, args, result);
        }
        return result;
    }

    /** Best-effort attribution of a tool failure to specific actor
     *  classes in the current model.  Records ONCE per (className,
     *  dispatch) so a single bad call cannot inflate a class's
     *  counter by referencing it multiple times.  The signal is
     *  consumed by {@link org.ptolemy.agent.session.ModelContext} so
     *  the LLM sees its own failure history each round and can pick
     *  a different actor without being hard-vetoed. */
    private static void _accrueClassFailures(String tool,
            PtolemySession session, JSONObject args, AgentResult result) {
        if (session.toplevel() == null) {
            return;
        }
        if (args == null) {
            args = new JSONObject();
        }
        java.util.Set<String> recorded =
                new java.util.HashSet<String>();
        ptolemy.kernel.CompositeEntity scope = _scope(session, args);
        if (scope == null) {
            return;
        }
        // add_entity / add_composite carry an explicit className.
        if ("add_entity".equals(tool) || "add_composite".equals(tool)) {
            String cls = args.optString("className", "").trim();
            _recordClass(session, cls, recorded);
            return;
        }
        // connect / connect_many / set_parameter / delete fail in a
        // way that points at one or more existing entities; attribute
        // to those entities' classes — but only the side(s) the
        // failure payload flags via `field`, so a valid endpoint
        // paired with a bad one does not get blamed.
        if ("connect".equals(tool)) {
            java.util.Set<String> blameFields = _badFields(result);
            if (blameFields.isEmpty() || blameFields.contains("from")) {
                _recordEntityClassFromRef(scope, session,
                        args.optString("from", ""), recorded);
            }
            if (blameFields.isEmpty() || blameFields.contains("to")) {
                _recordEntityClassFromRef(scope, session,
                        args.optString("to", ""), recorded);
            }
        } else if ("connect_many".equals(tool)) {
            org.json.JSONArray edges = args.optJSONArray("edges");
            org.json.JSONArray rejected = result.data()
                    .optJSONArray("rejected");
            if (rejected != null && rejected.length() > 0) {
                // connect_many rolled back on rejected[].  Attribute
                // only to the failing endpoint(s) of each rejected
                // edge so unrelated edges don't get blamed.
                for (int i = 0; i < rejected.length(); i++) {
                    JSONObject row = rejected.optJSONObject(i);
                    if (row == null) {
                        continue;
                    }
                    JSONObject edge = row.optJSONObject("edge");
                    if (edge == null) {
                        continue;
                    }
                    java.util.Set<String> blameFields =
                            _badFieldsFromIssueArray(
                                    row.optJSONArray("issues"));
                    if (blameFields.isEmpty()
                            || blameFields.contains("from")) {
                        _recordEntityClassFromRef(scope, session,
                                edge.optString("from", ""), recorded);
                    }
                    if (blameFields.isEmpty()
                            || blameFields.contains("to")) {
                        _recordEntityClassFromRef(scope, session,
                                edge.optString("to", ""), recorded);
                    }
                }
            } else if (edges != null) {
                // Failure outside the preflight stage (e.g. Ptolemy
                // refused the MoML); without per-edge attribution,
                // blame everything in the batch.
                for (int i = 0; i < edges.length(); i++) {
                    JSONObject e = edges.optJSONObject(i);
                    if (e == null) {
                        continue;
                    }
                    _recordEntityClassFromRef(scope, session,
                            e.optString("from", ""), recorded);
                    _recordEntityClassFromRef(scope, session,
                            e.optString("to", ""), recorded);
                }
            }
        } else if ("set_parameter".equals(tool)
                || "delete".equals(tool)) {
            String entity = args.optString("entity",
                    args.optString("name", "")).trim();
            if (!entity.isEmpty()) {
                ptolemy.kernel.ComponentEntity ent =
                        scope.getEntity(entity);
                if (ent != null) {
                    _recordClass(session, ent.getClassName(), recorded);
                }
            }
        }
    }

    /** Resolve the scope (top level or named child composite) from a
     *  tool's argument bag.  Returns null when no model is loaded. */
    private static ptolemy.kernel.CompositeEntity _scope(
            PtolemySession session, JSONObject args) {
        ptolemy.kernel.CompositeEntity top =
                (ptolemy.kernel.CompositeEntity) session.toplevel();
        if (top == null) {
            return null;
        }
        String parent = args.optString("parent", "").trim();
        if (parent.isEmpty()) {
            return top;
        }
        ptolemy.kernel.CompositeEntity current = top;
        String[] segments = parent.split("/");
        for (String segment : segments) {
            String name = segment == null ? "" : segment.trim();
            if (name.isEmpty()) {
                continue;
            }
            ptolemy.kernel.ComponentEntity child = current.getEntity(name);
            if (!(child instanceof ptolemy.kernel.CompositeEntity)) {
                return top;
            }
            current = (ptolemy.kernel.CompositeEntity) child;
        }
        return current;
    }

    private static void _recordEntityClassFromRef(
            ptolemy.kernel.CompositeEntity scope, PtolemySession session,
            String reference, java.util.Set<String> recorded) {
        if (reference == null || reference.isEmpty()) {
            return;
        }
        int dot = reference.lastIndexOf('.');
        if (dot <= 0) {
            return;
        }
        String entityName = reference.substring(0, dot);
        ptolemy.kernel.ComponentEntity ent = scope.getEntity(entityName);
        if (ent == null) {
            // The entity does not exist; the failure is more likely a
            // typo than a class-level problem.  Skip.
            return;
        }
        _recordClass(session, ent.getClassName(), recorded);
    }

    private static void _recordClass(PtolemySession session,
            String className, java.util.Set<String> recorded) {
        if (className == null || className.isEmpty()) {
            return;
        }
        if (recorded.add(className)) {
            session.recordClassFailure(className);
        }
    }

    /** Extract the set of {@code field} values from a top-level
     *  failure payload's {@code data.issues[*]} so we know which
     *  side(s) of a connect call were the actual cause.  Returns an
     *  empty set when the payload has no structured issues — in
     *  which case the caller blames both endpoints conservatively. */
    private static java.util.Set<String> _badFields(AgentResult result) {
        if (result == null) {
            return java.util.Collections.emptySet();
        }
        org.json.JSONObject data = result.data();
        if (data == null) {
            return java.util.Collections.emptySet();
        }
        org.json.JSONArray issues = data.optJSONArray("issues");
        return _badFieldsFromIssueArray(issues);
    }

    private static java.util.Set<String> _badFieldsFromIssueArray(
            org.json.JSONArray issues) {
        java.util.Set<String> out = new java.util.HashSet<String>();
        if (issues == null) {
            return out;
        }
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.optJSONObject(i);
            if (issue == null) {
                continue;
            }
            String field = issue.optString("field", "");
            if ("from".equals(field) || "to".equals(field)) {
                out.add(field);
            }
        }
        return out;
    }

    /** Build an OpenAI-style tools array suitable for the
     *  {@code tools} field of a Chat Completions request. The shape:
     *  <pre>
     *  [{"type":"function","function":{"name":"...","description":"...",
     *                                  "parameters": &lt;JSON Schema&gt;}}]
     *  </pre>
     */
    public JSONArray toolsForOpenAI() {
        JSONArray out = new JSONArray();
        for (AgentTool tool : _byName.values()) {
            try {
                JSONObject fn = new JSONObject();
                fn.put("name", tool.name());
                fn.put("description", tool.description());
                fn.put("parameters", tool.parametersSchema());
                JSONObject wrap = new JSONObject();
                wrap.put("type", "function");
                wrap.put("function", fn);
                out.put(wrap);
            } catch (org.json.JSONException e) {
                // ignore malformed tool entries
            }
        }
        return out;
    }

    /** Build a JSON descriptor consumed by the frontend's LibraryPanel
     *  to show the user (and tests) what is available. */
    public JSONArray toolsForUi() {
        JSONArray out = new JSONArray();
        for (AgentTool tool : _byName.values()) {
            try {
                JSONObject row = new JSONObject();
                row.put("name", tool.name());
                row.put("description", tool.description());
                row.put("parameters", tool.parametersSchema());
                out.put(row);
            } catch (org.json.JSONException e) {
                // ignore malformed tool entries
            }
        }
        return out;
    }

    /** Build a registry that includes every tool from this one whose
     *  name does NOT appear in {@code excluded}. Used to give the
     *  Builder phase a tool list that hides composite-creation tools,
     *  and the Refactor phase one that hides mutating atomic tools.
     *  @param excluded Names of tools to leave out (e.g.
     *      "add_composite", "group_into_composite"). */
    public ToolRegistry except(String... excluded) {
        java.util.HashSet<String> deny = new java.util.HashSet<>();
        for (String n : excluded) {
            if (n != null) {
                deny.add(n);
            }
        }
        ToolRegistry out = new ToolRegistry();
        for (Map.Entry<String, AgentTool> e : _byName.entrySet()) {
            if (!deny.contains(e.getKey())) {
                out.register(e.getValue());
            }
        }
        return out;
    }

    /** Build a registry that includes ONLY the tools whose names
     *  appear in {@code allowed}. Order follows {@code allowed}. */
    public ToolRegistry only(String... allowed) {
        ToolRegistry out = new ToolRegistry();
        for (String n : allowed) {
            AgentTool t = _byName.get(n);
            if (t != null) {
                out.register(t);
            }
        }
        return out;
    }

    /** @return A registry pre-populated with the default toolset. */
    public static ToolRegistry defaultRegistry() {
        ToolRegistry r = new ToolRegistry();
        r.register(new AddEntityTool())
                .register(new AddCompositeTool())
                .register(new GroupIntoCompositeTool())
                .register(new AutoLayoutTool())
                .register(new ConnectTool())
                .register(new ConnectManyTool())
                .register(new DisconnectTool())
                .register(new SetParameterTool())
                .register(new DeleteTool())
                .register(new RunSimulationTool())
                .register(new ListLibraryTool())
                .register(new ListEntitiesTool())
                .register(new DescribeActorTool())
                .register(new ValidateTool());
        return r;
    }
}
