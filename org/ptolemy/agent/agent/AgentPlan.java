/* Structured plan utilities for the auto-modeling pipeline.

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

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// AgentPlan

/** Helpers for parsing and validating the planner's JSON contract. */
public final class AgentPlan {

    private AgentPlan() {
    }

    /** Parse the planner reply as a JSON object, tolerating accidental
     *  markdown fences.
     *  @param raw Raw LLM planner content.
     *  @return Parsed plan, or null. */
    public static JSONObject parse(String raw) {
        if (raw == null) {
            return null;
        }
        String text = raw.trim();
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            int lastFence = text.lastIndexOf("```");
            if (firstNewline >= 0 && lastFence > firstNewline) {
                text = text.substring(firstNewline + 1, lastFence).trim();
            }
        }
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        try {
            return new JSONObject(text);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** Validate the minimal shape needed by the Builder.
     *  @param plan Parsed plan.
     *  @return Array of human-readable validation issues. */
    public static JSONArray validationIssues(JSONObject plan) {
        JSONArray issues = new JSONArray();
        if (plan == null) {
            issues.put("planner did not return a JSON object");
            return issues;
        }
        _requireString(plan, "domain", issues);
        if (!plan.has("director") || !(plan.opt("director") instanceof
                JSONObject)) {
            issues.put("missing object: director");
        } else {
            _requireString(plan.optJSONObject("director"), "className",
                    issues, "director.");
        }
        JSONArray actors = plan.optJSONArray("actors");
        if (actors == null || actors.length() == 0) {
            issues.put("missing non-empty array: actors");
        } else {
            for (int i = 0; i < actors.length(); i++) {
                JSONObject a = actors.optJSONObject(i);
                if (a == null) {
                    issues.put("actors[" + i + "] is not an object");
                    continue;
                }
                _requireString(a, "name", issues, "actors[" + i + "].");
                _requireString(a, "className", issues,
                        "actors[" + i + "].");
            }
        }
        JSONArray connections = plan.optJSONArray("connections");
        if (connections == null) {
            issues.put("missing array: connections");
        } else {
            for (int i = 0; i < connections.length(); i++) {
                JSONObject c = connections.optJSONObject(i);
                if (c == null) {
                    issues.put("connections[" + i + "] is not an object");
                    continue;
                }
                _requireString(c, "from", issues,
                        "connections[" + i + "].");
                _requireString(c, "to", issues,
                        "connections[" + i + "].");
            }
        }
        _requireString(plan, "validationGoal", issues);
        return issues;
    }

    /** Format the plan for the Builder prompt. */
    public static String builderInstruction(JSONObject plan, String raw) {
        if (plan != null) {
            return "Pre-approved JSON plan from the planning phase."
                    + " Follow this exact actor/connection contract unless"
                    + " a tool reports it is impossible:\n\n```json\n"
                    + plan.toString(2) + "\n```";
        }
        return "Planner returned non-JSON text. Treat it only as a hint,"
                + " then build from the user goal:\n\n" + (raw == null
                        ? "" : raw);
    }

    /** Format future composite hints for the Refactor phase. */
    public static String refactorInstruction(JSONObject plan) {
        if (plan == null) {
            return "";
        }
        JSONArray composites = plan.optJSONArray("futureComposites");
        if (composites == null || composites.length() == 0) {
            return "";
        }
        return "\n\nSuggested composites from the JSON plan:\n```json\n"
                + composites.toString(2) + "\n```";
    }

    private static void _requireString(JSONObject obj, String key,
            JSONArray issues) {
        _requireString(obj, key, issues, "");
    }

    private static void _requireString(JSONObject obj, String key,
            JSONArray issues, String prefix) {
        if (obj == null || obj.optString(key, "").trim().length() == 0) {
            issues.put("missing string: " + prefix + key);
        }
    }
}
