/* Heuristic composite suggestions for refactor phase.

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

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// RefactorAdvisor

/**
 Produces conservative grouping suggestions that the refactor LLM can
 use instead of rediscovering structure from scratch. It deliberately
 does not mutate the model.
 */
public final class RefactorAdvisor {
    private static final int DEFAULT_MAX_ATOMS_PER_SCOPE = 12;
    private static final int MIN_GROUP_SIZE = 3;
    private static final int MAX_GROUP_SIZE = 8;

    private RefactorAdvisor() {
    }

    /** Suggest coarse groups recursively by scope and function. */
    public static JSONArray suggestions(PtolemySession session) {
        return suggestionsRecursive(session, DEFAULT_MAX_ATOMS_PER_SCOPE);
    }

    /** Suggest grouping recursively for every overcrowded scope. */
    public static JSONArray suggestionsRecursive(PtolemySession session,
            int maxAtomsPerScope) {
        JSONArray out = new JSONArray();
        if (session == null || session.toplevel() == null) {
            return out;
        }
        CompositeEntity top = (CompositeEntity) session.toplevel();
        int threshold = Math.max(4, maxAtomsPerScope);
        _collect(top, "", threshold, out);
        return out;
    }

    private static void _collect(CompositeEntity scope, String scopePath,
            int maxAtomsPerScope, JSONArray out) {
        @SuppressWarnings("unchecked")
        java.util.List<ComponentEntity> entities = scope.entityList();
        java.util.List<ComponentEntity> atomics =
                new java.util.ArrayList<ComponentEntity>();
        java.util.List<ComponentEntity> composites =
                new java.util.ArrayList<ComponentEntity>();
        for (ComponentEntity entity : entities) {
            String name = entity.getName();
            String cls = entity.getClassName();
            if (name == null || cls == null || _skip(cls, name)) {
                continue;
            }
            if (entity instanceof CompositeEntity) {
                composites.add(entity);
            } else {
                atomics.add(entity);
            }
        }

        if (atomics.size() > maxAtomsPerScope) {
            Map<String, JSONArray> buckets =
                    new LinkedHashMap<String, JSONArray>();
            for (ComponentEntity entity : atomics) {
                String name = entity.getName();
                String cls = entity.getClassName();
                String bucket = _functionalBucket(cls) + "_"
                        + _bucket(name);
                buckets.computeIfAbsent(bucket,
                        k -> new JSONArray()).put(name);
            }
            for (Map.Entry<String, JSONArray> entry : buckets.entrySet()) {
                JSONArray members = entry.getValue();
                if (members.length() < MIN_GROUP_SIZE) {
                    continue;
                }
                java.util.List<String> chunk =
                        new java.util.ArrayList<String>();
                for (int i = 0; i < members.length(); i++) {
                    chunk.add(members.optString(i, ""));
                    if (chunk.size() >= MAX_GROUP_SIZE) {
                        out.put(_suggestion(entry.getKey(), chunk, scopePath,
                                atomics.size(), maxAtomsPerScope));
                        chunk = new java.util.ArrayList<String>();
                    }
                }
                if (chunk.size() >= MIN_GROUP_SIZE) {
                    out.put(_suggestion(entry.getKey(), chunk, scopePath,
                            atomics.size(), maxAtomsPerScope));
                }
            }
        }

        for (ComponentEntity composite : composites) {
            if (!(composite instanceof CompositeEntity)) {
                continue;
            }
            String childPath = scopePath.length() == 0
                    ? composite.getName()
                    : scopePath + "/" + composite.getName();
            _collect((CompositeEntity) composite, childPath,
                    maxAtomsPerScope, out);
        }
    }

    private static JSONObject _suggestion(String bucket, java.util.List<String>
            members, String scopePath, int atomicCount, int threshold) {
        JSONArray arr = new JSONArray();
        for (String m : members) {
            if (m != null && !m.isEmpty()) {
                arr.put(m);
            }
        }
        String scope = scopePath.length() == 0 ? "<top>" : scopePath;
        String base = _compositeName(bucket);
        String hash = Integer.toString(Math.abs(arr.toString().hashCode()));
        if (hash.length() < 3) {
            hash = ("000" + hash);
            hash = hash.substring(hash.length() - 3);
        }
        String suffix = hash.substring(0, 3);
        String compositeName = base + "_" + suffix;
        JSONObject out = new JSONObject();
        out.put("name", compositeName);
        out.put("members", arr);
        out.put("parent", scopePath);
        out.put("scope", scope);
        out.put("recursive", true);
        out.put("reason", "scope '" + scope + "' has " + atomicCount
                + " atomic actors (> " + threshold
                + "), grouped by functional bucket");
        return out;
    }

    private static boolean _skip(String cls, String name) {
        return name.startsWith("__recorder__")
                || cls.endsWith(".Recorder")
                || cls.endsWith("TypedCompositeActor")
                || cls.endsWith("CompositeActor")
                || cls.endsWith("Director")
                || cls.endsWith(".Const")
                || cls.endsWith(".Ramp")
                || cls.endsWith(".Sinewave")
                || cls.endsWith(".Clock");
    }

    private static String _bucket(String name) {
        if (name == null || name.length() == 0) {
            return "cluster";
        }
        int underscore = name.indexOf('_');
        if (underscore > 1) {
            return name.substring(0, underscore).toLowerCase();
        }
        int digit = -1;
        for (int i = 0; i < name.length(); i++) {
            if (Character.isDigit(name.charAt(i))) {
                digit = i;
                break;
            }
        }
        if (digit > 1) {
            return name.substring(0, digit).toLowerCase();
        }
        return name.toLowerCase();
    }

    private static String _functionalBucket(String className) {
        if (className == null) {
            return "actor";
        }
        String c = className.toLowerCase();
        if (c.contains("integrator") || c.contains("derivative")
                || c.contains("state")) {
            return "state";
        }
        if (c.contains("filter") || c.contains("iir") || c.contains("fir")) {
            return "filter";
        }
        if (c.contains("addsubtract") || c.contains("multiplydivide")
                || c.contains("scale") || c.contains("math")
                || c.contains("trig")) {
            return "math";
        }
        if (c.endsWith(".recorder") || c.contains("plotter")
                || c.contains("display")) {
            return "sink";
        }
        if (c.endsWith(".ramp") || c.endsWith(".const")
                || c.endsWith(".sinewave") || c.endsWith(".pulse")
                || c.endsWith(".clock")) {
            return "source";
        }
        if (c.contains("delay") || c.contains("sample")) {
            return "memory";
        }
        return "actor";
    }

    private static String _compositeName(String bucket) {
        if (bucket.length() == 0) {
            return "Subsystem";
        }
        return Character.toUpperCase(bucket.charAt(0)) + bucket.substring(1);
    }
}
