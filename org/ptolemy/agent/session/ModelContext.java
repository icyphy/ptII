/* Structured model context for LLM prompts and diagnostics.

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
package org.ptolemy.agent.session;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// ModelContext

/**
 Produce a compact, structured description of the current model for
 prompts and API diagnostics. This is intentionally smaller and more
 semantic than raw MoML, so agents can reason over actors, ports,
 connections, directors and counts without depending on truncated XML.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class ModelContext {

    private ModelContext() {
    }

    /** Build the model context JSON for a session.
     *  @param session The session to inspect.
     *  @return Structured summary. */
    public static JSONObject forSession(PtolemySession session) {
        JSONObject out = new JSONObject();
        out.put("format", "ptolemy-agent-model-context-v1");
        if (session == null || session.toplevel() == null) {
            out.put("hasModel", false);
            out.put("nodes", new JSONArray());
            out.put("edges", new JSONArray());
            out.put("directors", new JSONArray());
            out.put("summary", new JSONObject()
                    .put("nodes", 0).put("edges", 0)
                    .put("directors", 0));
            return out;
        }

        JSONObject graph = GraphSerializer.serialize(session);
        JSONArray nodes = graph.optJSONArray("nodes");
        JSONArray edges = graph.optJSONArray("edges");
        JSONArray directors = graph.optJSONArray("directors");
        if (nodes == null) nodes = new JSONArray();
        if (edges == null) edges = new JSONArray();
        if (directors == null) directors = new JSONArray();

        out.put("hasModel", true);
        out.put("topName", graph.optString("topName", ""));
        out.put("topClass", graph.optString("topClass", ""));
        out.put("directors", directors);
        out.put("nodes", _nodesForPrompt(nodes));
        out.put("edges", _edgesForPrompt(edges));
        out.put("summary", new JSONObject()
                .put("nodes", nodes.length())
                .put("edges", edges.length())
                .put("directors", directors.length()));
        return out;
    }

    /** Render the context as a prompt block. */
    public static String promptBlock(PtolemySession session) {
        return "Current model context (structured JSON; prefer this over"
                + " raw MoML when planning tool calls):\n```json\n"
                + forSession(session).toString(2) + "\n```";
    }

    private static JSONArray _nodesForPrompt(JSONArray nodes) {
        JSONArray out = new JSONArray();
        for (int i = 0; i < nodes.length(); i++) {
            JSONObject n = nodes.optJSONObject(i);
            if (n == null) {
                continue;
            }
            JSONObject row = new JSONObject();
            row.put("name", n.optString("id", ""));
            row.put("className", n.optString("className", ""));
            row.put("isComposite", n.optBoolean("isComposite", false));
            row.put("boundary", n.optBoolean("boundary", false));
            row.put("inputs", _portNames(n.optJSONArray("inputs")));
            row.put("outputs", _portNames(n.optJSONArray("outputs")));
            row.put("parameters", n.optJSONArray("parameters") == null
                    ? new JSONArray() : n.optJSONArray("parameters"));
            out.put(row);
        }
        return out;
    }

    private static JSONArray _edgesForPrompt(JSONArray edges) {
        JSONArray out = new JSONArray();
        for (int i = 0; i < edges.length(); i++) {
            JSONObject e = edges.optJSONObject(i);
            if (e == null) {
                continue;
            }
            out.put(new JSONObject()
                    .put("from", e.optString("sourceHandle", ""))
                    .put("to", e.optString("targetHandle", "")));
        }
        return out;
    }

    private static JSONArray _portNames(JSONArray ports) {
        JSONArray out = new JSONArray();
        if (ports == null) {
            return out;
        }
        for (int i = 0; i < ports.length(); i++) {
            JSONObject p = ports.optJSONObject(i);
            if (p != null) {
                out.put(p.optString("name", ""));
            }
        }
        return out;
    }
}
