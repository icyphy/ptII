/* Per-turn trace recorded by the AgentLoop.

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
package org.ptolemy.agent.agent;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// AgentTrace

/**
 Records the step-by-step execution of one {@code AgentLoop.run()}
 invocation. The trace is what the frontend shows in the chat panel
 (one card per step) and what tests inspect to confirm the LLM made
 sensible tool choices.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class AgentTrace {

    /** One step in the trace. */
    public static final class Step {
        public final int index;
        public final String kind; // "thought" | "tool_call" | "tool_result" | "final" | "error"
        public final String name; // tool name or empty
        public final JSONObject arguments;
        public final String text;
        public final long timestamp;

        public Step(int index, String kind, String name,
                JSONObject arguments, String text) {
            this.index = index;
            this.kind = kind;
            this.name = name == null ? "" : name;
            this.arguments = arguments == null ? new JSONObject()
                    : arguments;
            this.text = text == null ? "" : text;
            this.timestamp = System.currentTimeMillis();
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("index", index);
            json.put("kind", kind);
            json.put("name", name);
            json.put("arguments", arguments);
            json.put("text", text);
            json.put("timestamp", timestamp);
            return json;
        }
    }

    private final List<Step> _steps = new ArrayList<>();
    private final JSONObject _diagnostics = new JSONObject();
    private boolean _completed = false;
    private boolean _success = false;
    private String _finalReply = "";
    private AgentTraceListener _listener;

    /** Attach a listener that receives each step as it is recorded. */
    public synchronized void setListener(AgentTraceListener listener) {
        _listener = listener;
    }

    private synchronized void _append(Step step) {
        _steps.add(step);
        AgentTraceListener listener = _listener;
        if (listener != null) {
            listener.onStep(step);
        }
    }

    public synchronized void addThought(String text) {
        _append(new Step(_steps.size(), "thought", "", null, text));
    }

    public synchronized void addToolCall(String name, JSONObject args) {
        _append(new Step(_steps.size(), "tool_call", name, args, ""));
    }

    public synchronized void addToolResult(String name, JSONObject result) {
        _append(new Step(_steps.size(), "tool_result", name, result, ""));
    }

    public synchronized void addError(String message) {
        _append(new Step(_steps.size(), "error", "", null, message));
    }

    /** Attach diagnostic metadata to the trace. This is additive and
     *  does not affect the stable step list consumed by existing
     *  clients.
     *  @param key Diagnostic key.
     *  @param value JSON-compatible value. */
    public synchronized void putDiagnostic(String key, Object value) {
        if (key != null && key.length() > 0) {
            _diagnostics.put(key, value == null ? JSONObject.NULL : value);
        }
    }

    public synchronized void finish(boolean success, String finalReply) {
        _completed = true;
        _success = success;
        _finalReply = finalReply == null ? "" : finalReply;
        _append(new Step(_steps.size(), "final", "", null, _finalReply));
    }

    /** True after the loop ended successfully (LLM produced a final
     *  reply without remaining tool calls). */
    public synchronized boolean isSuccess() {
        return _success;
    }

    /** Last reply text from the agent. Empty until {@link #finish}
     *  has been called. */
    public synchronized String finalReply() {
        return _finalReply;
    }

    /** Drop the oldest steps until at most {@code n} remain.  Used
     *  by long-running pipelines (especially the iterative
     *  mega-build) to keep the {@code trace.steps} array bounded
     *  before the trace is serialised back to the client.  Indices
     *  on retained steps are re-numbered so the array still reads
     *  0..N-1 from the frontend's perspective.
     *  @param n Cap on retained steps; values &lt;= 0 are ignored. */
    public synchronized void truncateToLastN(int n) {
        if (n <= 0 || _steps.size() <= n) {
            return;
        }
        int drop = _steps.size() - n;
        List<Step> kept = new ArrayList<Step>(n);
        for (int i = drop; i < _steps.size(); i++) {
            Step src = _steps.get(i);
            kept.add(new Step(kept.size(), src.kind, src.name,
                    src.arguments, src.text));
        }
        _steps.clear();
        _steps.addAll(kept);
        _diagnostics.put("truncatedSteps", drop);
    }

    /** Append a clone of another trace's step into this trace WITHOUT
     *  firing the listener. The pipeline uses this when it has already
     *  forwarded the event live to the external client, but still
     *  wants the step to land in the combined transcript that gets
     *  returned to the caller. */
    public synchronized void appendStepSilent(Step src) {
        _steps.add(new Step(_steps.size(), src.kind, src.name,
                src.arguments, src.text));
    }

    public synchronized JSONObject toJson() {
        JSONObject root = new JSONObject();
        root.put("completed", _completed);
        root.put("success", _success);
        root.put("finalReply", _finalReply);
        root.put("diagnostics", _diagnostics);
        JSONArray steps = new JSONArray();
        for (Step s : _steps) {
            steps.put(s.toJson());
        }
        root.put("steps", steps);
        return root;
    }
}
