/* Tool that runs auto-layout in an isolated subprocess.
 *
 * Copyright (c) 2024-2026 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2
 * COPYRIGHTENDKEY
 */
package org.ptolemy.agent.tools;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// AutoLayoutTool

/**
 Runs Ptolemy's built-in graph layout in a separate JVM process and applies
 resulting coordinates back to the current session model.

 <p>This isolates GUI-side effects from the long-lived backend process while
 still reusing Ptolemy II's native layout behavior.
 */
public class AutoLayoutTool implements AgentTool {

    private static final long DEFAULT_TIMEOUT_MS = 45000L;
    private static final long MAX_TIMEOUT_MS = 180000L;
    private static final long PER_ENTITY_TIMEOUT_MS = 90L;
    private static final int PATCH_CHUNK_SIZE = 120;

    @Override
    public String name() {
        return "auto_layout";
    }

    @Override
    public String description() {
        return "Apply Ptolemy II built-in auto-graph layout by running an"
                + " isolated layout worker and writing back _location values.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("parent", new JSONObject().put("type", "string")
                .put("description",
                        "Optional child composite name at top level."
                                + " Omit to layout top-level."));
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (session == null || session.toplevel() == null) {
            return AgentResult.fail("no model loaded");
        }
        String parent = args == null ? "" : args.optString("parent", "").trim();
        CompositeEntity top = (CompositeEntity) session.toplevel();
        CompositeEntity scope;
        try {
            scope = _resolveScope(top, parent);
        } catch (IllegalArgumentException e) {
            return AgentResult.fail(e.getMessage());
        }
        File input = null;
        File output = null;
        File log = null;
        long startedAtMs = System.currentTimeMillis();
        try {
            int entityCount = scope.entityList().size();
            input = File.createTempFile("agent-layout-input-", ".xml");
            output = File.createTempFile("agent-layout-output-", ".json");
            log = File.createTempFile("agent-layout-worker-", ".log");
            long exportStartMs = System.currentTimeMillis();
            try (Writer w = new OutputStreamWriter(
                    new java.io.FileOutputStream(input),
                    StandardCharsets.UTF_8)) {
                w.write(session.exportMoml());
            }
            long exportElapsedMs = System.currentTimeMillis() - exportStartMs;

            List<String> cmd = _workerCommand(input, output, parent);
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            pb.redirectOutput(log);
            Process p = pb.start();
            long timeoutMs = _timeoutMs(entityCount);
            long workerStartMs = System.currentTimeMillis();
            boolean finished = p.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
            if (!finished) {
                p.destroyForcibly();
                _log("timeout scope=" + (parent.isEmpty() ? "<top>" : parent)
                        + " entities=" + entityCount + " timeoutMs="
                        + timeoutMs + " exportMs=" + exportElapsedMs);
                return AgentResult.fail(
                        "auto_layout timed out after " + timeoutMs + " ms");
            }
            long workerElapsedMs = System.currentTimeMillis() - workerStartMs;

            JSONObject workerOut = _readJson(output);
            if (p.exitValue() != 0) {
                return AgentResult.fail("auto_layout worker failed (exit "
                        + p.exitValue() + "): " + _workerError(workerOut, log));
            }
            if (workerOut == null || !workerOut.optBoolean("ok", false)) {
                return AgentResult.fail("auto_layout worker reported failure: "
                        + _workerError(workerOut, log));
            }

            JSONArray positions = workerOut.optJSONArray("positions");
            if (positions == null || positions.length() == 0) {
                return AgentResult.fail(
                        "auto_layout returned no node positions");
            }
            List<String> patches = _locationPatches(positions, parent);
            long applyStartMs = System.currentTimeMillis();
            for (String patch : patches) {
                AgentResult applied = session.applyChange(patch);
                if (!applied.ok()) {
                    return applied;
                }
            }
            long applyElapsedMs = System.currentTimeMillis() - applyStartMs;

            JSONObject data = new JSONObject();
            data.put("scope", parent.isEmpty() ? scope.getName() : parent);
            data.put("moved", positions.length());
            String engine = workerOut.optString("layoutEngine", "ptolemy");
            data.put("algorithm", engine + "_layout_subprocess");
            data.put("exportMs", exportElapsedMs);
            data.put("workerMs", workerElapsedMs);
            data.put("applyMs", applyElapsedMs);
            data.put("entityCount", entityCount);
            _log("ok scope=" + (parent.isEmpty() ? "<top>" : parent)
                    + " entities=" + entityCount
                    + " moved=" + positions.length()
                    + " exportMs=" + exportElapsedMs
                    + " workerMs=" + workerElapsedMs
                    + " applyMs=" + applyElapsedMs
                    + " totalMs=" + (System.currentTimeMillis() - startedAtMs));
            return AgentResult.ok(
                    "auto-layout updated " + positions.length()
                            + " actor positions",
                    data);
        } catch (Throwable t) {
            return AgentResult.fail("auto_layout failed: " + t.getMessage());
        } finally {
            _deleteQuietly(input);
            _deleteQuietly(output);
            _deleteQuietly(log);
        }
    }

    private static List<String> _workerCommand(File input, File output,
            String parent) {
        List<String> cmd = new ArrayList<String>();
        cmd.add(_javaBin());
        cmd.add("-Djava.awt.headless=false");
        cmd.add("-cp");
        cmd.add(System.getProperty("java.class.path", ""));
        cmd.add("org.ptolemy.agent.tools.autolayout.AutoLayoutWorkerMain");
        cmd.add("--input");
        cmd.add(input.getAbsolutePath());
        cmd.add("--output");
        cmd.add(output.getAbsolutePath());
        if (parent != null && parent.trim().length() > 0) {
            cmd.add("--parent");
            cmd.add(parent.trim());
        }
        return cmd;
    }

    private static String _javaBin() {
        String home = System.getProperty("java.home", "");
        boolean windows = System.getProperty("os.name", "")
                .toLowerCase(Locale.ROOT).contains("win");
        File bin = new File(home, "bin"
                + File.separator + (windows ? "java.exe" : "java"));
        if (bin.exists()) {
            return bin.getAbsolutePath();
        }
        return windows ? "java.exe" : "java";
    }

    private static long _timeoutMs(int entityCount) {
        long scaled = DEFAULT_TIMEOUT_MS
                + Math.max(0, entityCount) * PER_ENTITY_TIMEOUT_MS;
        if (scaled > MAX_TIMEOUT_MS) {
            scaled = MAX_TIMEOUT_MS;
        }
        String env = System.getenv("AGENT_AUTOLAYOUT_TIMEOUT_MS");
        if (env == null || env.trim().isEmpty()) {
            return scaled;
        }
        try {
            long parsed = Long.parseLong(env.trim());
            return parsed <= 0 ? scaled : parsed;
        } catch (Throwable t) {
            return scaled;
        }
    }

    private static JSONObject _readJson(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            String text = new String(bytes, StandardCharsets.UTF_8).trim();
            if (text.isEmpty()) {
                return null;
            }
            return new JSONObject(text);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String _workerError(JSONObject workerOut, File log) {
        if (workerOut != null) {
            String err = workerOut.optString("error", "");
            if (!err.isEmpty()) {
                return err;
            }
        }
        if (log != null && log.exists()) {
            try {
                List<String> lines = java.nio.file.Files.readAllLines(
                        log.toPath(), StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    int start = Math.max(0, lines.size() - 8);
                    StringBuilder sb = new StringBuilder();
                    for (int i = start; i < lines.size(); i++) {
                        if (sb.length() > 0) {
                            sb.append(" | ");
                        }
                        sb.append(lines.get(i));
                    }
                    return sb.toString();
                }
            } catch (Throwable ignored) {
                // best-effort diagnostics
            }
        }
        return "unknown worker error";
    }

    private static List<String> _locationPatches(JSONArray positions,
            String parent) {
        List<String> out = new ArrayList<String>();
        if (positions == null || positions.length() == 0) {
            return out;
        }
        JSONArray chunk = new JSONArray();
        for (int i = 0; i < positions.length(); i++) {
            JSONObject row = positions.optJSONObject(i);
            if (row == null) {
                continue;
            }
            chunk.put(row);
            if (chunk.length() >= PATCH_CHUNK_SIZE) {
                out.add(_locationPatch(chunk, parent));
                chunk = new JSONArray();
            }
        }
        if (chunk.length() > 0) {
            out.add(_locationPatch(chunk, parent));
        }
        return out;
    }

    private static String _locationPatch(JSONArray positions, String parent) {
        StringBuilder sb = new StringBuilder();
        if (parent == null || parent.trim().isEmpty()) {
            sb.append("<group>");
        } else {
            sb.append("<entity name=\"")
                    .append(AddEntityTool.escape(parent.trim()))
                    .append("\">");
        }
        for (int i = 0; i < positions.length(); i++) {
            JSONObject row = positions.optJSONObject(i);
            if (row == null) {
                continue;
            }
            String entity = row.optString("entity", "").trim();
            if (entity.isEmpty()) {
                continue;
            }
            double x = row.optDouble("x", 0.0);
            double y = row.optDouble("y", 0.0);
            sb.append("<entity name=\"").append(AddEntityTool.escape(entity))
                    .append("\">")
                    .append("<property name=\"_location\"")
                    .append(" class=\"ptolemy.kernel.util.Location\"")
                    .append(" value=\"[")
                    .append(_num(x)).append(", ").append(_num(y))
                    .append("]\"/>")
                    .append("</entity>");
        }
        if (parent == null || parent.trim().isEmpty()) {
            sb.append("</group>");
        } else {
            sb.append("</entity>");
        }
        return sb.toString();
    }

    private static String _num(double value) {
        double nearest = Math.rint(value);
        if (Math.abs(value - nearest) < 1.0e-6) {
            return Long.toString(Math.round(nearest)) + ".0";
        }
        return String.format(Locale.US, "%.3f", value);
    }

    private static void _deleteQuietly(File file) {
        if (file != null && file.exists()) {
            try {
                file.delete();
            } catch (Throwable ignored) {
                // best-effort cleanup
            }
        }
    }

    private static void _log(String msg) {
        System.out.println("[auto_layout] " + msg);
    }

    private static CompositeEntity _resolveScope(CompositeEntity top,
            String parentPath) {
        if (parentPath == null || parentPath.trim().isEmpty()) {
            return top;
        }
        CompositeEntity current = top;
        String[] segments = parentPath.split("/");
        for (String segment : segments) {
            String name = segment == null ? "" : segment.trim();
            if (name.isEmpty()) {
                continue;
            }
            ComponentEntity child = current.getEntity(name);
            if (!(child instanceof CompositeEntity)) {
                throw new IllegalArgumentException(
                        "no composite named '" + parentPath + "' in scope");
            }
            current = (CompositeEntity) child;
        }
        return current;
    }
}
