/* Uniform success/failure result returned by tools and session operations.

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
package org.ptolemy.agent.util;

import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// AgentResult

/**
 A uniform value object returned from every tool and session operation
 in the agent backend. Carries a success flag, a human readable message
 (suitable for surfacing in chat logs or HTTP responses), and an
 optional structured payload as a {@link JSONObject}.

 <p>This type is intentionally simple. It is the only return type used
 across the boundary between Java tool implementations and the LLM
 driver, so it has to be cheap to construct and trivial to translate
 into JSON for either the LLM tool-call response or the HTTP REST
 response.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class AgentResult {

    private final boolean _ok;
    private final String _message;
    private final JSONObject _data;

    private AgentResult(boolean ok, String message, JSONObject data) {
        _ok = ok;
        _message = message == null ? "" : message;
        _data = data == null ? new JSONObject() : data;
    }

    /** Build a successful result with a message and no payload.
     *  @param message Human readable message.
     *  @return A successful AgentResult.
     */
    public static AgentResult ok(String message) {
        return new AgentResult(true, message, null);
    }

    /** Build a successful result with a message and JSON payload.
     *  @param message Human readable message.
     *  @param data Structured payload (may be null).
     *  @return A successful AgentResult.
     */
    public static AgentResult ok(String message, JSONObject data) {
        return new AgentResult(true, message, data);
    }

    /** Build a failure result.
     *  @param message Human readable error message.
     *  @return A failure AgentResult.
     */
    public static AgentResult fail(String message) {
        return new AgentResult(false, message, null);
    }

    /** Build a failure result with an attached JSON payload (for
     *  example a stack trace string).
     *  @param message Human readable error message.
     *  @param data Structured payload.
     *  @return A failure AgentResult.
     */
    public static AgentResult fail(String message, JSONObject data) {
        return new AgentResult(false, message, data);
    }

    /** @return True iff this represents a successful operation. */
    public boolean ok() {
        return _ok;
    }

    /** @return Human-readable message. Never null. */
    public String message() {
        return _message;
    }

    /** @return JSON payload. Never null; empty if no payload was set. */
    public JSONObject data() {
        return _data;
    }

    /** Serialize this result as a JSON object with stable shape:
     *  <pre>
     *  { "ok": true|false, "message": "...", "data": { ... } }
     *  </pre>
     *  @return JSON representation suitable for an HTTP response body.
     */
    public JSONObject toJson() {
        JSONObject out = new JSONObject();
        out.put("ok", _ok);
        out.put("message", _message);
        out.put("data", _data);
        return out;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
