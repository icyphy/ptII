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
        try {
            return tool.execute(session, args == null ? new JSONObject()
                    : args);
        } catch (Throwable t) {
            return AgentResult.fail(name + " crashed: " + t.getMessage());
        }
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
                .register(new ConnectTool())
                .register(new DisconnectTool())
                .register(new SetParameterTool())
                .register(new DeleteTool())
                .register(new RunSimulationTool())
                .register(new ListLibraryTool())
                .register(new DescribeActorTool())
                .register(new ValidateTool());
        return r;
    }
}
