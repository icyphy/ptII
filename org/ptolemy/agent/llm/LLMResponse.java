/* Uniform LLM response object.

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
package org.ptolemy.agent.llm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// LLMResponse

/**
 Uniform LLM response. Either the model returned a final reply for the
 user ({@link #content()}) or it asked the client to call one or more
 tools first ({@link #toolCalls()}). Both can be present in principle
 but for the M2 agent loop we treat them as mutually exclusive,
 preferring tool calls.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class LLMResponse {

    /** A single tool-call request from the LLM. */
    public static final class ToolCall {
        public final String id;
        public final String toolName;
        public final JSONObject arguments;

        public ToolCall(String id, String toolName, JSONObject arguments) {
            this.id = id;
            this.toolName = toolName;
            this.arguments = arguments == null
                    ? new JSONObject() : arguments;
        }
    }

    private final String _content;
    private final String _reasoningContent;
    private final List<ToolCall> _toolCalls;
    private final JSONObject _raw;

    public LLMResponse(String content, List<ToolCall> toolCalls,
            JSONObject raw) {
        this(content, "", toolCalls, raw);
    }

    public LLMResponse(String content, String reasoningContent,
            List<ToolCall> toolCalls, JSONObject raw) {
        _content = content == null ? "" : content;
        _reasoningContent = reasoningContent == null
                ? "" : reasoningContent;
        _toolCalls = toolCalls == null ? Collections.<ToolCall>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(toolCalls));
        _raw = raw == null ? new JSONObject() : raw;
    }

    /** @return The text content of the assistant message, if any. */
    public String content() {
        return _content;
    }

    /** @return Provider-specific reasoning trace content, if any. */
    public String reasoningContent() {
        return _reasoningContent;
    }

    /** @return The list of tool-call requests, possibly empty. */
    public List<ToolCall> toolCalls() {
        return _toolCalls;
    }

    /** @return True iff the response asks for at least one tool call. */
    public boolean hasToolCalls() {
        return !_toolCalls.isEmpty();
    }

    /** @return The raw provider JSON, for logging/debugging. */
    public JSONObject raw() {
        return _raw;
    }

    /** @return The OpenAI-shape assistant message we should append to
     *      chat history. */
    public JSONObject toAssistantMessage() {
        JSONObject msg = new JSONObject();
        msg.put("role", "assistant");
        msg.put("content", _content);
        if (!_reasoningContent.isEmpty()) {
            msg.put("reasoning_content", _reasoningContent);
        }
        if (hasToolCalls()) {
            JSONArray calls = new JSONArray();
            for (ToolCall call : _toolCalls) {
                JSONObject fn = new JSONObject();
                fn.put("name", call.toolName);
                fn.put("arguments", call.arguments.toString());
                JSONObject c = new JSONObject();
                c.put("id", call.id);
                c.put("type", "function");
                c.put("function", fn);
                calls.put(c);
            }
            msg.put("tool_calls", calls);
        }
        return msg;
    }
}
