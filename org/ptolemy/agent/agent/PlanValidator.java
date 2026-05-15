/* Static validation of phase-0 JSON plans before deterministic
 execution.

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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// PlanValidator

/**
 Strict static checks on a phase-0 JSON plan before it gets handed to
 the deterministic executor.  Catches LLM hallucinations that would
 otherwise burn through the executor and force the phase-1 LLM into
 expensive recovery work.

 <p>The checks are intentionally cheap and complementary to
 {@link AgentPlan#validationIssues}: AgentPlan checks the SHAPE of the
 plan (required string fields exist), this validator checks the
 SEMANTIC viability of what the LLM wrote — does the className resolve
 to a real class? does every connection reference an actor that the
 plan actually declared? is the director paired with actors that are
 compatible with it?

 <p>Output is a JSON array of {@code {severity, code, subject,
 message}} issues with optional {@code suggestion} text.  Errors must
 be fixed before the executor can be trusted; warnings are advisory
 (e.g. "Integrator usually wants ContinuousDirector").

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class PlanValidator {

    private PlanValidator() {
    }

    /** Validate the plan and return the issue list.  Empty list means
     *  the plan is ready for the executor.
     *  @param plan Parsed plan JSON, or null.
     *  @return Structured issue array; never null. */
    public static JSONArray validate(JSONObject plan) {
        JSONArray issues = new JSONArray();
        if (plan == null) {
            _issue(issues, "ERROR", "NO_PLAN", "plan",
                    "plan is null or unparseable");
            return issues;
        }

        // ---- Director ----
        String directorClass = "";
        JSONObject director = plan.optJSONObject("director");
        if (director == null) {
            _issue(issues, "ERROR", "MISSING_DIRECTOR", "director",
                    "plan.director is missing; the executor needs a"
                            + " director to add at the top level");
        } else {
            directorClass = director.optString("className", "").trim();
            if (directorClass.isEmpty()) {
                _issue(issues, "ERROR", "MISSING_DIRECTOR_CLASS",
                        "director.className",
                        "plan.director.className is missing");
            } else if (!_classOnPath(directorClass)) {
                JSONObject issue = _issue(issues, "ERROR",
                        "DIRECTOR_CLASS_NOT_FOUND",
                        "director.className",
                        "director className is not on the classpath: "
                                + directorClass);
                issue.put("suggestion",
                        "Use one of: ptolemy.domains.sdf.kernel.SDFDirector,"
                                + " ptolemy.domains.de.kernel.DEDirector,"
                                + " ptolemy.domains.continuous.kernel"
                                + ".ContinuousDirector");
            }
        }

        // ---- Actors ----
        Set<String> actorNames = new LinkedHashSet<>();
        Set<String> duplicateNames = new HashSet<>();
        JSONArray actors = plan.optJSONArray("actors");
        if (actors == null || actors.length() == 0) {
            _issue(issues, "ERROR", "EMPTY_ACTORS", "actors",
                    "plan.actors must contain at least one actor");
        } else {
            for (int i = 0; i < actors.length(); i++) {
                JSONObject a = actors.optJSONObject(i);
                if (a == null) {
                    _issue(issues, "ERROR", "MALFORMED_ACTOR",
                            "actors[" + i + "]",
                            "entry is not a JSON object");
                    continue;
                }
                String name = a.optString("name", "").trim();
                String cls = a.optString("className", "").trim();
                if (name.isEmpty()) {
                    _issue(issues, "ERROR", "MISSING_ACTOR_NAME",
                            "actors[" + i + "].name",
                            "actor entry has no name");
                } else if (!actorNames.add(name)) {
                    duplicateNames.add(name);
                    _issue(issues, "ERROR", "DUPLICATE_ACTOR_NAME",
                            "actors[" + i + "].name",
                            "duplicate actor name: " + name);
                }
                if (cls.isEmpty()) {
                    _issue(issues, "ERROR", "MISSING_ACTOR_CLASS",
                            "actors[" + i + "].className",
                            "actor " + name + " has no className");
                } else if (!_classOnPath(cls)) {
                    JSONObject issue = _issue(issues, "ERROR",
                            "ACTOR_CLASS_NOT_FOUND",
                            "actors[" + i + "].className",
                            "className is not on the classpath: " + cls
                                    + " (actor " + name + ")");
                    issue.put("actor", name);
                    issue.put("className", cls);
                    _addClassAlternativeHints(cls, issue);
                }
                // Sanity-check director / continuous-actor pairing.
                if (!directorClass.isEmpty()
                        && _requiresContinuous(cls)
                        && directorClass.indexOf(
                                "ContinuousDirector") < 0) {
                    _issue(issues, "WARN",
                            "CONTINUOUS_ACTOR_WITHOUT_DIRECTOR",
                            "actors[" + i + "]",
                            "actor " + name + " (" + cls + ") usually"
                                    + " requires a ContinuousDirector"
                                    + " but plan.director is "
                                    + directorClass);
                }
            }
        }

        // ---- Connections ----
        JSONArray connections = plan.optJSONArray("connections");
        if (connections == null) {
            _issue(issues, "ERROR", "MISSING_CONNECTIONS",
                    "connections",
                    "plan.connections array is missing");
        } else {
            Set<String> seenEdges = new HashSet<>();
            for (int i = 0; i < connections.length(); i++) {
                JSONObject c = connections.optJSONObject(i);
                if (c == null) {
                    _issue(issues, "ERROR", "MALFORMED_CONNECTION",
                            "connections[" + i + "]",
                            "entry is not a JSON object");
                    continue;
                }
                String from = c.optString("from", "").trim();
                String to = c.optString("to", "").trim();
                if (from.isEmpty() || to.isEmpty()) {
                    _issue(issues, "ERROR", "MALFORMED_CONNECTION",
                            "connections[" + i + "]",
                            "connection requires non-empty 'from' and 'to'");
                    continue;
                }
                String fromActor = _actorPart(from);
                String toActor = _actorPart(to);
                if (!fromActor.isEmpty()
                        && !actorNames.contains(fromActor)) {
                    JSONObject issue = _issue(issues, "ERROR",
                            "CONNECTION_UNKNOWN_FROM_ACTOR",
                            "connections[" + i + "].from",
                            "from references unknown actor '"
                                    + fromActor + "' (not in plan.actors)");
                    issue.put("availableActors",
                            new JSONArray(new java.util.ArrayList<>(
                                    actorNames)));
                }
                if (!toActor.isEmpty()
                        && !actorNames.contains(toActor)) {
                    JSONObject issue = _issue(issues, "ERROR",
                            "CONNECTION_UNKNOWN_TO_ACTOR",
                            "connections[" + i + "].to",
                            "to references unknown actor '"
                                    + toActor + "' (not in plan.actors)");
                    issue.put("availableActors",
                            new JSONArray(new java.util.ArrayList<>(
                                    actorNames)));
                }
                String edgeKey = from + "\u0001" + to;
                if (!seenEdges.add(edgeKey)) {
                    _issue(issues, "WARN", "DUPLICATE_CONNECTION",
                            "connections[" + i + "]",
                            "duplicate edge: " + from + " -> " + to
                                    + " (fan-out is fine, but this is"
                                    + " an exact duplicate)");
                }
            }
        }

        return issues;
    }

    /** True when {@code className} resolves on the classpath. */
    private static boolean _classOnPath(String className) {
        if (className == null || className.isEmpty()) {
            return false;
        }
        try {
            Class.forName(className);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /** Continuous-time-only actors that complain under SDF. */
    private static boolean _requiresContinuous(String className) {
        if (className == null) {
            return false;
        }
        String c = className.toLowerCase();
        return c.endsWith(".integrator") || c.endsWith(".derivative");
    }

    /** Targeted hints for classNames the LLM frequently gets wrong. */
    private static void _addClassAlternativeHints(String cls,
            JSONObject issue) {
        if (cls == null) {
            return;
        }
        String lower = cls.toLowerCase();
        JSONArray hints = new JSONArray();
        if (lower.endsWith(".sigmoid")) {
            hints.put("ptolemy.actor.lib.Sigmoid does NOT exist in"
                    + " this tree.  Compose sigmoid as 1/(1+exp(-x))"
                    + " from ptolemy.actor.lib.UnaryMathFunction"
                    + " (function=\"exp\") + Const(1) + AddSubtract"
                    + " + MultiplyDivide.");
        }
        if (lower.contains("relu") || lower.contains("softmax")) {
            hints.put("No ReLU / Softmax actor exists; compose from"
                    + " primitives (Comparator+Multiplexor for ReLU,"
                    + " UnaryMathFunction(exp)+normalization for"
                    + " Softmax) or accept a simpler activation"
                    + " (TrigFunction(tanh), Sigmoid via exp).");
        }
        if (lower.endsWith(".scaleneg")
                || lower.endsWith(".addone")
                || lower.endsWith(".invert")) {
            hints.put("That is not a real Ptolemy actor class.  Use"
                    + " ptolemy.actor.lib.Scale (factor=-1.0),"
                    + " ptolemy.actor.lib.AddSubtract (Const(1) into"
                    + " plus), or ptolemy.actor.lib.MultiplyDivide"
                    + " (Const(1) into multiply + your signal into"
                    + " divide) respectively.");
        }
        if (hints.length() > 0) {
            issue.put("suggestion", hints);
        }
    }

    /** Extract the "actor" part of an "actor.port" reference. */
    private static String _actorPart(String reference) {
        if (reference == null) {
            return "";
        }
        int dot = reference.lastIndexOf('.');
        return dot < 0 ? reference : reference.substring(0, dot);
    }

    private static JSONObject _issue(JSONArray issues, String severity,
            String code, String subject, String message) {
        JSONObject row = new JSONObject()
                .put("severity", severity)
                .put("code", code)
                .put("subject", subject)
                .put("message", message);
        issues.put(row);
        return row;
    }

    /** True when {@code issues} contains at least one ERROR-severity
     *  entry; warnings alone do not block execution. */
    public static boolean hasErrors(JSONArray issues) {
        if (issues == null) {
            return false;
        }
        for (int i = 0; i < issues.length(); i++) {
            JSONObject issue = issues.optJSONObject(i);
            if (issue != null
                    && "ERROR".equals(issue.optString("severity"))) {
                return true;
            }
        }
        return false;
    }
}
