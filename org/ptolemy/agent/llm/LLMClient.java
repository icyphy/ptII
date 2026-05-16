/* Provider-agnostic LLM client.

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

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// LLMClient

/**
 Provider-agnostic LLM client. One method,
 {@link #chat(JSONArray, JSONArray)}, takes a chat history and a list
 of tools (OpenAI function-calling shape) and returns the model's
 reply or a tool-call request as an {@link LLMResponse}.

 <p>Implementations should be thread-safe. The agent loop calls
 {@code chat} sequentially per session but multiple sessions may run
 in parallel.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public interface LLMClient {

    /** @return A short identifier for this provider, e.g. "openai",
     *      "ollama", or "null". Used in logs and in the UI. */
    String providerName();

    /** Send a chat request.
     *  @param messages Chat history in OpenAI shape:
     *      array of {role, content, ...} objects.
     *  @param tools OpenAI-style tools array
     *      (see ToolRegistry.toolsForOpenAI()). May be empty if no
     *      tool calling is desired.
     *  @return The model's reply, or a request to call one or more
     *      tools.
     *  @throws Exception If the network call or response parsing fails.
     */
    LLMResponse chat(JSONArray messages, JSONArray tools) throws Exception;

    /** Streaming variant of {@link #chat}.  Subclasses that support
     *  server-sent events override this to call {@code onDelta} as
     *  partial content arrives; the default implementation delegates
     *  to the blocking {@link #chat} and fires {@code onDelta} once
     *  at the end, so callers can use the same code path regardless
     *  of provider streaming support.
     *
     *  @param messages Chat history (OpenAI shape).
     *  @param tools Tools array (may be empty).
     *  @param onDelta Receives {@code (accumulatedContent,
     *      accumulatedReasoning)} as the response grows.  May be
     *      called many times; the strings are cumulative snapshots,
     *      not incremental diffs.  Implementations should throttle
     *      their own calls to a sane rate (≈10/s) so the listener
     *      does not have to.
     *  @return The same final {@link LLMResponse} the blocking
     *      {@link #chat} call would have produced.
     *  @throws Exception On network / parse errors. */
    default LLMResponse chatStreaming(JSONArray messages, JSONArray tools,
            java.util.function.BiConsumer<String, String> onDelta)
            throws Exception {
        LLMResponse reply = chat(messages, tools);
        if (onDelta != null) {
            String c = reply == null ? "" : reply.content();
            String r = reply == null ? "" : reply.reasoningContent();
            onDelta.accept(c == null ? "" : c, r == null ? "" : r);
        }
        return reply;
    }

    /** @return True iff this client can actually contact a model
     *      (e.g. {@link OpenAIClient} returns true once the API key
     *      is configured). The {@link NullLLMClient} returns false. */
    boolean isAvailable();

    /** @return A short status string describing the client, used by
     *      the {@code /agent/status} HTTP endpoint and the chat UI. */
    default JSONObject status() {
        JSONObject json = new JSONObject();
        json.put("provider", providerName());
        json.put("available", isAvailable());
        return json;
    }
}
