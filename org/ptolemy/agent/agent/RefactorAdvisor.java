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

    private RefactorAdvisor() {
    }

    /** Suggest coarse groups from actor names. */
    public static JSONArray suggestions(PtolemySession session) {
        JSONArray out = new JSONArray();
        if (session == null || session.toplevel() == null) {
            return out;
        }
        CompositeEntity top = (CompositeEntity) session.toplevel();
        Map<String, JSONArray> buckets =
                new LinkedHashMap<String, JSONArray>();
        @SuppressWarnings("unchecked")
        java.util.List<ComponentEntity> entities = top.entityList();
        for (ComponentEntity entity : entities) {
            String name = entity.getName();
            String cls = entity.getClassName();
            if (name == null || cls == null || _skip(cls, name)) {
                continue;
            }
            String bucket = _bucket(name);
            if (bucket.length() == 0) {
                continue;
            }
            buckets.computeIfAbsent(bucket, k -> new JSONArray()).put(name);
        }
        for (Map.Entry<String, JSONArray> entry : buckets.entrySet()) {
            JSONArray members = entry.getValue();
            if (members.length() >= 3) {
                out.put(new JSONObject()
                        .put("name", _compositeName(entry.getKey()))
                        .put("members", members)
                        .put("reason",
                                "actors share a stable name prefix"));
            }
        }
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
                || cls.endsWith(".Sinewave");
    }

    private static String _bucket(String name) {
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
        return "";
    }

    private static String _compositeName(String bucket) {
        if (bucket.length() == 0) {
            return "Subsystem";
        }
        return Character.toUpperCase(bucket.charAt(0)) + bucket.substring(1);
    }
}
