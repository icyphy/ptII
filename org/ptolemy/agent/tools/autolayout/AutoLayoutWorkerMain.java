/* Subprocess worker that runs Vergil auto-layout in isolation.
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
package org.ptolemy.agent.tools.autolayout;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Location;
import ptolemy.vergil.basic.PtolemyLayoutAction;
import ptolemy.vergil.basic.layout.kieler.KielerLayoutAction;

///////////////////////////////////////////////////////////////////
//// AutoLayoutWorkerMain

/**
 Worker entry point that runs Ptolemy's built-in layout in a dedicated JVM.

 <p>Arguments:
 <ul>
 <li>{@code --input <momlPath>} required</li>
 <li>{@code --output <jsonPath>} required</li>
 <li>{@code --parent <compositeName>} optional</li>
 </ul>
 */
public final class AutoLayoutWorkerMain {

    private AutoLayoutWorkerMain() {
    }

    /** CLI entry point for subprocess execution. */
    public static void main(String[] args) {
        JSONObject payload = new JSONObject();
        int exitCode = 0;
        String outputPath = null;
        try {
            Map<String, String> kv = _parseArgs(args);
            String input = kv.get("input");
            outputPath = kv.get("output");
            String parent = kv.get("parent");
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("missing --input");
            }
            if (outputPath == null || outputPath.trim().isEmpty()) {
                throw new IllegalArgumentException("missing --output");
            }
            payload = _run(new File(input), parent == null ? "" : parent);
        } catch (Throwable t) {
            payload.put("ok", false);
            payload.put("error", t.getMessage() == null
                    ? t.getClass().getName() : t.getMessage());
            payload.put("errorClass", t.getClass().getName());
            exitCode = 1;
        }
        if (outputPath != null && !outputPath.isEmpty()) {
            try (Writer w = new OutputStreamWriter(
                    new java.io.FileOutputStream(outputPath),
                    StandardCharsets.UTF_8)) {
                w.write(payload.toString());
            } catch (Throwable t) {
                System.err.println("failed to write worker output: "
                        + t.getMessage());
                exitCode = 1;
            }
        } else {
            System.out.println(payload.toString());
        }
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static JSONObject _run(File inputFile, String parent)
            throws Exception {
        final JSONObject[] out = new JSONObject[] { new JSONObject() };
        final Throwable[] failure = new Throwable[1];

        Runnable task = new Runnable() {
            @Override
            public void run() {
                CompositeEntity model = null;
                CompositeEntity closeTarget = null;
                try {
                    model = ConfigurationApplication.openModelOrEntity(
                            inputFile.getAbsolutePath());
                    closeTarget = _root(model);
                    CompositeEntity scope = closeTarget;
                    String p = parent == null ? "" : parent.trim();
                    if (!p.isEmpty()) {
                        scope = _resolveScope(scope, p);
                    }
                    long layoutStartMs = System.currentTimeMillis();
                    String engine = _applyLayout(scope);
                    long layoutMs = System.currentTimeMillis() - layoutStartMs;
                    long collectStartMs = System.currentTimeMillis();
                    Map<String, double[]> positions = _collect(scope);
                    long collectMs = System.currentTimeMillis()
                            - collectStartMs;
                    JSONObject payload = new JSONObject();
                    payload.put("ok", true);
                    payload.put("scope", p.isEmpty() ? scope.getName() : p);
                    payload.put("positions", _positionsJson(positions));
                    payload.put("moved", positions.size());
                    payload.put("layoutEngine", engine);
                    payload.put("entityCount", scope.entityList().size());
                    payload.put("layoutMs", layoutMs);
                    payload.put("collectMs", collectMs);
                    out[0] = payload;
                } catch (Throwable t) {
                    failure[0] = t;
                } finally {
                    if (closeTarget != null) {
                        try {
                            ConfigurationApplication
                                    .closeModelWithoutSavingOrExiting(
                                            closeTarget);
                        } catch (Throwable ignored) {
                            // best-effort cleanup
                        }
                    } else if (model != null) {
                        try {
                            ConfigurationApplication
                                    .closeModelWithoutSavingOrExiting(model);
                        } catch (Throwable ignored) {
                            // best-effort cleanup
                        }
                    }
                }
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            task.run();
        } else {
            SwingUtilities.invokeAndWait(task);
        }
        if (failure[0] != null) {
            throw new Exception(failure[0]);
        }
        return out[0];
    }

    /** Prefer KIELER (better crossing minimization); fall back to the
     *  classic Ptolemy layout action when KIELER is unavailable. */
    private static String _applyLayout(CompositeEntity scope) {
        try {
            new KielerLayoutAction().doAction(scope);
            return "kieler";
        } catch (Throwable ignored) {
            new PtolemyLayoutAction().doAction(scope);
            return "ptolemy";
        }
    }

    private static Map<String, String> _parseArgs(String[] args) {
        Map<String, String> out = new LinkedHashMap<String, String>();
        if (args == null) {
            return out;
        }
        for (int i = 0; i < args.length; i++) {
            String k = args[i];
            if (k == null || !k.startsWith("--")) {
                continue;
            }
            String key = k.substring(2);
            String val = "";
            if (i + 1 < args.length && (args[i + 1] == null
                    || !args[i + 1].startsWith("--"))) {
                val = args[++i];
            }
            out.put(key, val);
        }
        return out;
    }

    private static CompositeEntity _root(CompositeEntity entity) {
        CompositeEntity out = entity;
        while (out != null && out.getContainer() instanceof CompositeEntity) {
            out = (CompositeEntity) out.getContainer();
        }
        return out == null ? entity : out;
    }

    private static CompositeEntity _resolveScope(CompositeEntity root,
            String parentPath) {
        if (parentPath == null || parentPath.trim().isEmpty()) {
            return root;
        }
        CompositeEntity current = root;
        String[] segments = parentPath.split("/");
        for (String segment : segments) {
            String name = segment == null ? "" : segment.trim();
            if (name.isEmpty()) {
                continue;
            }
            ComponentEntity child = current.getEntity(name);
            if (!(child instanceof CompositeEntity)) {
                throw new IllegalArgumentException(
                        "no composite named '" + parentPath
                                + "' in worker model");
            }
            current = (CompositeEntity) child;
        }
        return current;
    }

    private static Map<String, double[]> _collect(CompositeEntity scope) {
        Map<String, double[]> out = new LinkedHashMap<String, double[]>();
        @SuppressWarnings("unchecked")
        List<ComponentEntity> entities = scope.entityList();
        for (ComponentEntity entity : entities) {
            String name = entity.getName();
            if (name == null || name.startsWith("__recorder__")) {
                continue;
            }
            Attribute loc = entity.getAttribute("_location");
            if (!(loc instanceof Location)) {
                continue;
            }
            double[] xy = ((Location) loc).getLocation();
            if (xy != null && xy.length >= 2) {
                out.put(name, new double[] { xy[0], xy[1] });
            }
        }
        return out;
    }

    private static JSONArray _positionsJson(Map<String, double[]> positions) {
        JSONArray out = new JSONArray();
        for (Map.Entry<String, double[]> e : positions.entrySet()) {
            double[] xy = e.getValue();
            JSONObject row = new JSONObject();
            row.put("entity", e.getKey());
            double x = xy.length > 0 ? xy[0] : 0.0;
            double y = xy.length > 1 ? xy[1] : 0.0;
            if (!Double.isFinite(x)) {
                x = 100.0;
            }
            if (!Double.isFinite(y)) {
                y = 100.0;
            }
            row.put("x", x);
            row.put("y", y);
            out.put(row);
        }
        return out;
    }
}
