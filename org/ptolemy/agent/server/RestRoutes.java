/* REST route table for the Ptolemy II auto-modeling agent backend.

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
package org.ptolemy.agent.server;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.json.JSONObject;
import org.ptolemy.agent.agent.AgentBackend;
import org.ptolemy.agent.agent.AgentTrace;
import org.ptolemy.agent.library.LibraryIndex;
import org.ptolemy.agent.session.GraphSerializer;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.session.SessionManager;
import org.ptolemy.agent.util.AgentResult;
import org.ptolemy.agent.util.JsonResponse;

///////////////////////////////////////////////////////////////////
//// RestRoutes

/**
 Registers the REST routes for the M0 milestone of the auto-modeling
 agent backend. Routes are intentionally narrow: enough to load,
 inspect, run and clean up a Ptolemy session via HTTP, which is the
 contract the frontend will eventually consume.

 <p>Route table (prefix {@code /api/v1}):
 <table>
 <tr><td>{@code GET}</td><td>{@code /health}</td>
   <td>liveness check</td></tr>
 <tr><td>{@code POST}</td><td>{@code /sessions}</td>
   <td>create a new session, returns its id</td></tr>
 <tr><td>{@code GET}</td><td>{@code /sessions}</td>
   <td>list sessions</td></tr>
 <tr><td>{@code DELETE}</td><td>{@code /sessions/&#123;id&#125;}</td>
   <td>dispose a session</td></tr>
 <tr><td>{@code GET}</td><td>{@code /sessions/&#123;id&#125;}</td>
   <td>session summary</td></tr>
 <tr><td>{@code POST}</td><td>{@code /sessions/&#123;id&#125;/load}</td>
   <td>load a model from {@code {"path":"..."}} or {@code {"moml":"..."}}</td></tr>
 <tr><td>{@code GET}</td><td>{@code /sessions/&#123;id&#125;/moml}</td>
   <td>current model as MoML</td></tr>
 <tr><td>{@code POST}</td><td>{@code /sessions/&#123;id&#125;/change}</td>
   <td>apply a MoML fragment</td></tr>
 <tr><td>{@code POST}</td><td>{@code /sessions/&#123;id&#125;/run}</td>
   <td>run the simulation to completion</td></tr>
 <tr><td>{@code GET}</td><td>{@code /sessions/&#123;id&#125;/signals}</td>
   <td>harvest recorder data after a run</td></tr>
 </table>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class RestRoutes {

    private RestRoutes() {
    }

    /** Register every route on the given server. */
    public static void register(SimpleHttpServer server) {
        server.route("GET", "/api/v1/health",
                (ex, p) -> JsonResponse.ok(ex,
                        new JSONObject().put("ok", true)
                                .put("service", "ptolemy-agent")
                                .put("version", "0.1.0")));

        server.route("POST", "/api/v1/sessions", (ex, p) -> {
            try {
                PtolemySession session = SessionManager.get().create();
                JsonResponse.created(ex, session.summary());
            } catch (Exception e) {
                JsonResponse.error(ex, 500,
                        "could not create session: " + e.getMessage());
            }
        });

        server.route("GET", "/api/v1/sessions",
                (ex, p) -> JsonResponse.ok(ex,
                        new JSONObject().put("sessions",
                                SessionManager.get().toJsonArray())));

        server.route("GET", "/api/v1/sessions/{id}", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JsonResponse.ok(ex, s.summary());
        });

        server.route("DELETE", "/api/v1/sessions/{id}", (ex, p) -> {
            boolean removed = SessionManager.get().remove(p.get("id"));
            if (!removed) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JsonResponse.ok(ex,
                    new JSONObject().put("ok", true).put("removed", true));
        });

        server.route("POST", "/api/v1/sessions/{id}/load", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            String body = SimpleHttpServer.readBody(ex);
            JSONObject in = parseJsonOrEmpty(body);
            AgentResult result;
            if (in.has("path")) {
                result = s.loadFile(in.getString("path"));
            } else if (in.has("moml")) {
                result = s.loadMoml(in.getString("moml"));
            } else {
                JsonResponse.error(ex, 400,
                        "expected JSON body with \"path\" or \"moml\"");
                return;
            }
            JsonResponse.ok(ex, result.toJson());
        });

        server.route("GET", "/api/v1/sessions/{id}/moml", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JsonResponse.textPlain(ex, s.exportMoml());
        });

        server.route("GET", "/api/v1/sessions/{id}/graph", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            // Accept an optional ?path=A/B/C query string for drilling
            // into nested composites. URL-decode just to be safe.
            java.util.List<String> path = new java.util.ArrayList<>();
            String raw = ex.getRequestURI().getRawQuery();
            if (raw != null) {
                for (String pair : raw.split("&")) {
                    int eq = pair.indexOf('=');
                    if (eq < 0) continue;
                    String key = pair.substring(0, eq);
                    String val = pair.substring(eq + 1);
                    if ("path".equals(key) && !val.isEmpty()) {
                        String decoded = java.net.URLDecoder.decode(val,
                                java.nio.charset.StandardCharsets.UTF_8);
                        for (String segment : decoded.split("/")) {
                            if (!segment.isEmpty()) {
                                path.add(segment);
                            }
                        }
                    }
                }
            }
            JsonResponse.ok(ex, GraphSerializer.serialize(s, path));
        });

        server.route("POST", "/api/v1/sessions/{id}/change", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JSONObject in = parseJsonOrEmpty(
                    SimpleHttpServer.readBody(ex));
            if (!in.has("moml")) {
                JsonResponse.error(ex, 400,
                        "expected JSON body with \"moml\"");
                return;
            }
            AgentResult result = s.applyChange(in.getString("moml"));
            JsonResponse.ok(ex, result.toJson());
        });

        server.route("POST", "/api/v1/sessions/{id}/run", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            AgentResult result = s.run();
            JsonResponse.ok(ex, result.toJson());
        });

        server.route("GET", "/api/v1/sessions/{id}/signals", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            if (s.toplevel() == null) {
                JsonResponse.error(ex, 409, "no model loaded");
                return;
            }
            JSONObject payload = s.summary();
            payload.put("signals", s.signals());
            JsonResponse.ok(ex, payload);
        });

        // ----- Agent endpoints -----

        server.route("GET", "/api/v1/agent/status",
                (ex, p) -> JsonResponse.ok(ex, AgentBackend.get().status()));

        server.route("GET", "/api/v1/agent/library",
                (ex, p) -> JsonResponse.ok(ex,
                        new JSONObject().put("entries",
                                LibraryIndex.shared().all())));

        server.route("POST", "/api/v1/sessions/{id}/agent/chat", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JSONObject in = parseJsonOrEmpty(
                    SimpleHttpServer.readBody(ex));
            if (!in.has("message")) {
                JsonResponse.error(ex, 400,
                        "expected JSON body with \"message\"");
                return;
            }
            String message = in.getString("message");
            // Routing precedence:
            //   1) explicit {"mode":"pipeline"|"single"|"chat"} in body
            //   2) legacy {"singlePass":true} → single
            //   3) AgentRouter heuristic
            org.ptolemy.agent.agent.AgentRouter.Mode mode =
                    _resolveMode(in, s, message);
            AgentTrace trace = _runMode(mode, s, message, null);
            JSONObject payload = new JSONObject();
            payload.put("mode", mode.name().toLowerCase());
            payload.put("trace", trace.toJson());
            payload.put("session", s.summary());
            payload.put("signals", s.signals());
            JsonResponse.ok(ex, payload);
        });

        server.route("POST", "/api/v1/sessions/{id}/agent/chat/stream",
                (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JSONObject in = parseJsonOrEmpty(
                    SimpleHttpServer.readBody(ex));
            if (!in.has("message")) {
                JsonResponse.error(ex, 400,
                        "expected JSON body with \"message\"");
                return;
            }
            String message = in.getString("message");
            org.ptolemy.agent.agent.AgentRouter.Mode mode =
                    _resolveMode(in, s, message);
            OutputStream out = JsonResponse.startNdjson(ex);
            try {
                JSONObject started = new JSONObject();
                started.put("event", "started");
                started.put("mode", mode.name().toLowerCase());
                JsonResponse.writeNdjsonLine(out, started);

                org.ptolemy.agent.agent.AgentTraceListener listener =
                        step -> {
                            JSONObject evt = new JSONObject();
                            evt.put("event", "step");
                            evt.put("step", step.toJson());
                            try {
                                JsonResponse.writeNdjsonLine(out, evt);
                            } catch (IOException io) {
                                throw new RuntimeException(io);
                            }
                        };
                AgentTrace trace = _runMode(mode, s, message, listener);

                JSONObject done = new JSONObject();
                done.put("event", "done");
                done.put("trace", trace.toJson());
                done.put("session", s.summary());
                done.put("signals", s.signals());
                JsonResponse.writeNdjsonLine(out, done);
            } catch (RuntimeException e) {
                JSONObject err = new JSONObject();
                err.put("event", "error");
                err.put("message", e.getMessage());
                JsonResponse.writeNdjsonLine(out, err);
            } finally {
                out.close();
                ex.close();
            }
        });

        server.route("POST", "/api/v1/sessions/{id}/undo", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JsonResponse.ok(ex, s.undo().toJson());
        });

        server.route("POST", "/api/v1/sessions/{id}/redo", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JsonResponse.ok(ex, s.redo().toJson());
        });

        server.route("POST", "/api/v1/sessions/{id}/tools/{name}",
                (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            String toolName = p.get("name");
            JSONObject args = parseJsonOrEmpty(
                    SimpleHttpServer.readBody(ex));
            AgentResult result = AgentBackend.get().tools()
                    .dispatch(toolName, s, args);
            JsonResponse.ok(ex, result.toJson());
        });

        // Save model to disk and optionally open in Vergil.
        server.route("POST", "/api/v1/sessions/{id}/save", (ex, p) -> {
            PtolemySession s = SessionManager.get().find(p.get("id"));
            if (s == null) {
                JsonResponse.error(ex, 404, "no such session");
                return;
            }
            JSONObject in = parseJsonOrEmpty(
                    SimpleHttpServer.readBody(ex));

            // Determine save path.
            String ptii = System.getenv("PTII");
            if (ptii == null) ptii = System.getProperty("ptolemy.ptII.dir", ".");
            String modelName = "AgentModel";
            JSONObject summary = s.summary();
            if (summary.has("modelName")) {
                modelName = summary.getString("modelName");
            }
            String path = in.optString("path", "");
            if (path.isEmpty()) {
                path = ptii + File.separator + "agent-output"
                        + File.separator + modelName + ".xml";
            }

            AgentResult result = s.saveToFile(path);
            JSONObject payload = result.toJson();
            payload.put("path", new File(path).getAbsolutePath());

            // Optionally open in Vergil.
            boolean openVergil = in.optBoolean("openVergil", false);
            if (openVergil && result.ok()) {
                try {
                    String absPath = new File(path).getAbsolutePath();
                    String[] cmd;
                    if (System.getProperty("os.name", "")
                            .toLowerCase().contains("win")) {
                        String vergilCmd = ptii + File.separator
                                + "bin" + File.separator + "vergil.bat";
                        if (!new File(vergilCmd).exists()) {
                            vergilCmd = ptii + File.separator
                                    + "bin" + File.separator + "vergil";
                        }
                        cmd = new String[] { "cmd", "/c", vergilCmd, absPath };
                    } else {
                        cmd = new String[] { ptii + "/bin/vergil", absPath };
                    }
                    new ProcessBuilder(cmd)
                            .directory(new File(ptii))
                            .inheritIO()
                            .start();
                    payload.put("vergilLaunched", true);
                } catch (Exception e) {
                    payload.put("vergilLaunched", false);
                    payload.put("vergilError", e.getMessage());
                }
            }
            JsonResponse.ok(ex, payload);
        });
    }

    private static JSONObject parseJsonOrEmpty(String body) {
        if (body == null || body.isEmpty()) {
            return new JSONObject();
        }
        try {
            return new JSONObject(body);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    /** Resolve the execution mode for a chat request. Precedence:
     *  explicit {"mode":"..."} field, legacy {"singlePass":true}, then
     *  the {@link org.ptolemy.agent.agent.AgentRouter} heuristic. */
    private static org.ptolemy.agent.agent.AgentRouter.Mode _resolveMode(
            JSONObject in, PtolemySession s, String message) {
        String explicit = in.optString("mode", "").trim().toLowerCase();
        if (explicit.equals("pipeline")) {
            return org.ptolemy.agent.agent.AgentRouter.Mode.PIPELINE;
        }
        if (explicit.equals("single")) {
            return org.ptolemy.agent.agent.AgentRouter.Mode.SINGLE;
        }
        if (explicit.equals("chat")) {
            return org.ptolemy.agent.agent.AgentRouter.Mode.CHAT;
        }
        if (in.optBoolean("singlePass", false)) {
            return org.ptolemy.agent.agent.AgentRouter.Mode.SINGLE;
        }
        return org.ptolemy.agent.agent.AgentRouter.classify(s, message);
    }

    /** Dispatch a chat request to the right driver based on the
     *  routed mode. {@code listener} may be null for non-streaming. */
    private static AgentTrace _runMode(
            org.ptolemy.agent.agent.AgentRouter.Mode mode,
            PtolemySession s, String message,
            org.ptolemy.agent.agent.AgentTraceListener listener) {
        switch (mode) {
        case PIPELINE:
            return AgentBackend.get().pipeline().run(s, message, listener);
        case CHAT:
            return AgentBackend.get().chat(s, message, listener);
        case SINGLE:
        default:
            return AgentBackend.get().loop().run(s, message, listener);
        }
    }
}
