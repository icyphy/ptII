/* Fluent JSON response helpers used by the agent server.

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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// JsonResponse

/**
 Small helper that writes a JSON body to a {@link HttpExchange} with
 the right Content-Type, length and CORS headers for browser access
 from the React dev server. Used by every REST handler in the agent
 backend.

 <p>Usage:
 <pre>
 JsonResponse.ok(exchange, new JSONObject().put("hello", "world"));
 JsonResponse.error(exchange, 404, "session not found");
 </pre>

 <p>CORS headers are always emitted so that the React frontend running
 on a different origin (typically http://localhost:5173) can call this
 backend during development.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class JsonResponse {

    private JsonResponse() {
    }

    /** Write a 200 response carrying the given JSON object.
     *  @param exchange The exchange to write to.
     *  @param body The JSON body.
     *  @throws IOException If writing fails.
     */
    public static void ok(HttpExchange exchange, JSONObject body)
            throws IOException {
        write(exchange, 200, body.toString());
    }

    /** Write a 200 response carrying the given JSON array. */
    public static void ok(HttpExchange exchange, JSONArray body)
            throws IOException {
        write(exchange, 200, body.toString());
    }

    /** Write a 201 Created response carrying the given JSON object. */
    public static void created(HttpExchange exchange, JSONObject body)
            throws IOException {
        write(exchange, 201, body.toString());
    }

    /** Write an error response with the given status code and message
     *  packaged as {@code {"ok":false,"message":"..."}}.
     */
    public static void error(HttpExchange exchange, int status, String message)
            throws IOException {
        JSONObject body = new JSONObject();
        body.put("ok", false);
        body.put("message", message);
        write(exchange, status, body.toString());
    }

    /** Write a 204 No Content response (used for OPTIONS preflight). */
    public static void noContent(HttpExchange exchange) throws IOException {
        applyCors(exchange);
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    /** Write a response body as text/plain, used for the MoML source view. */
    public static void textPlain(HttpExchange exchange, String text)
            throws IOException {
        applyCors(exchange);
        byte[] payload = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type",
                "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(200, payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    /** Begin a chunked NDJSON response (one JSON object per line). */
    public static OutputStream startNdjson(HttpExchange exchange)
            throws IOException {
        applyCors(exchange);
        exchange.getResponseHeaders().add("Content-Type",
                "application/x-ndjson; charset=utf-8");
        exchange.getResponseHeaders().add("Cache-Control", "no-cache");
        exchange.sendResponseHeaders(200, 0);
        return exchange.getResponseBody();
    }

    /** Write one NDJSON event line and flush it immediately. */
    public static void writeNdjsonLine(OutputStream out, JSONObject line)
            throws IOException {
        out.write(line.toString().getBytes(StandardCharsets.UTF_8));
        out.write('\n');
        out.flush();
    }

    private static void write(HttpExchange exchange, int status, String body)
            throws IOException {
        applyCors(exchange);
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type",
                "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, payload.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(payload);
        }
    }

    private static void applyCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers",
                "Content-Type, Authorization");
    }
}
