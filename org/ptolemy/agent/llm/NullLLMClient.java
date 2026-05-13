/* No-op LLM client used when no provider is configured.

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

import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// NullLLMClient

/**
 Fallback {@link LLMClient} implementation used when no provider is
 configured (no {@code OPENAI_API_KEY} environment variable, no
 {@code agent.api.key} system property, etc.). Every call returns a
 helpful error message so the rest of the agent loop can run end-to-end
 in tests and demos even without network access.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class NullLLMClient implements LLMClient {

    private final String _reason;

    public NullLLMClient() {
        this("no LLM provider configured;"
                + " set OPENAI_API_KEY (or DEEPSEEK_API_KEY) to enable an"
                + " OpenAI-compatible client");
    }

    public NullLLMClient(String reason) {
        _reason = reason;
    }

    @Override
    public String providerName() {
        return "null";
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public LLMResponse chat(JSONArray messages, JSONArray tools) {
        String reply = "LLM is offline: " + _reason
                + ". The agent backend still works -- the tool API"
                + " can be exercised manually from the canvas, the"
                + " chat will resume once a provider is configured.";
        return new LLMResponse(reply, Collections.emptyList(),
                new JSONObject().put("provider", "null"));
    }
}
