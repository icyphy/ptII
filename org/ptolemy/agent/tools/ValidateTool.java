/* Tool that parse-validates a MoML fragment without applying it.

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
package org.ptolemy.agent.tools;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;

///////////////////////////////////////////////////////////////////
//// ValidateTool

/**
 Lightweight static check on the currently loaded model. Walks the
 entity tree and returns counts plus a list of obvious problems
 (entities without a director, dangling ports, etc.).

 <p>This is intentionally cheap so the agent can call it on every
 step of a multi-tool plan as a guardrail; the real check that the
 model still runs is performed by {@link RunSimulationTool}.

 <p>No arguments.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class ValidateTool implements AgentTool {

    @Override
    public String name() {
        return "validate";
    }

    @Override
    public String description() {
        return "Run a cheap static check on the current model: returns"
                + " entity/relation counts and a list of obvious"
                + " problems. Call this before \"run\" to catch errors"
                + " early.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", new JSONObject());
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (session.toplevel() == null) {
            return AgentResult.fail("no model loaded");
        }
        CompositeEntity top = (CompositeEntity) session.toplevel();
        @SuppressWarnings("unchecked")
        java.util.List<ComponentEntity> entityList = top.entityList();
        @SuppressWarnings("unchecked")
        java.util.List<Relation> relationList = top.relationList();
        @SuppressWarnings("unchecked")
        java.util.List<Attribute> attributeList = top.attributeList();
        int entities = entityList.size();
        int relations = relationList.size();
        int links = 0;
        int directors = 0;
        boolean hasRecorder = false;
        boolean hasContinuousActor = false;
        String directorClass = "";

        JSONArray issues = new JSONArray();
        JSONArray diagnostics = new JSONArray();
        for (Attribute attr : attributeList) {
            String klass = attr.getClassName();
            if (attr instanceof Director || (klass != null
                    && klass.endsWith("Director"))) {
                directors++;
                if (directorClass.length() == 0 && klass != null) {
                    directorClass = klass;
                }
            }
        }
        if (entities == 0) {
            _issue(issues, diagnostics, "ERROR", "EMPTY_MODEL", "model",
                    "model is empty: no entities found");
        }
        if (directors == 0) {
            _issue(issues, diagnostics, "ERROR", "NO_DIRECTOR", "director",
                    "no director declared at top level: simulation will fail");
        }
        for (ComponentEntity entity : entityList) {
            if (_isHidden(entity.getName())) {
                continue;
            }
            String entityClass = entity.getClassName();
            if (entityClass != null && entityClass.endsWith(".Recorder")) {
                hasRecorder = true;
            }
            if (_requiresContinuousDirector(entityClass)) {
                hasContinuousActor = true;
            }
            @SuppressWarnings("unchecked")
            java.util.List<Port> ports = entity.portList();
            for (Port port : ports) {
                if (!(port instanceof IOPort)) {
                    continue;
                }
                IOPort io = (IOPort) port;
                links += io.numLinks();
                String ref = entity.getName() + "." + io.getName();
                if (io.isInput() && !io.isMultiport() && io.numLinks() == 0) {
                    _issue(issues, diagnostics, "ERROR",
                            "UNCONNECTED_INPUT", ref,
                            "input port is unconnected: " + ref);
                }
                if (io.isOutput() && io.numLinks() == 0) {
                    _issue(issues, diagnostics, "WARN",
                            "UNCONNECTED_OUTPUT", ref,
                            "output port is unconnected: " + ref);
                }
            }
        }
        if (!hasRecorder && entities > 0) {
            _issue(issues, diagnostics, "WARN", "NO_RECORDER", "Recorder",
                    "no top-level Recorder found; frontend Signals panel may"
                            + " have no data");
        }
        if (hasContinuousActor && directorClass.indexOf(
                "ContinuousDirector") < 0) {
            _issue(issues, diagnostics, "ERROR",
                    "CONTINUOUS_ACTOR_WITHOUT_CONTINUOUS_DIRECTOR",
                    "director",
                    "continuous-time actor present but top-level director is "
                            + (directorClass.length() == 0 ? "missing"
                                    : directorClass));
        }

        for (Relation relation : relationList) {
            @SuppressWarnings("unchecked")
            java.util.List<Port> linkedPorts = relation.linkedPortList();
            int sources = 0;
            int destinations = 0;
            for (Port port : linkedPorts) {
                if (!(port instanceof IOPort)) {
                    continue;
                }
                IOPort io = (IOPort) port;
                if (io.isOutput()) {
                    sources++;
                }
                if (io.isInput()) {
                    destinations++;
                }
            }
            if (sources == 0 || destinations == 0) {
                _issue(issues, diagnostics, "ERROR", "INVALID_RELATION",
                        relation.getName(),
                        "relation does not connect output to input: "
                                + relation.getName());
            }
        }

        JSONObject data = new JSONObject();
        data.put("entities", entities);
        data.put("relations", relations);
        data.put("links", links);
        data.put("directors", directors);
        data.put("issues", issues);
        data.put("diagnostics", diagnostics);
        data.put("healthy", issues.length() == 0);

        boolean ok = issues.length() == 0;
        return ok
                ? AgentResult.ok("model looks healthy", data)
                : AgentResult.ok(
                        "model has " + issues.length() + " issue(s)", data);
    }

    private static boolean _isHidden(String name) {
        return name != null && name.startsWith("__recorder__");
    }

    private static boolean _requiresContinuousDirector(String className) {
        if (className == null) {
            return false;
        }
        String c = className.toLowerCase();
        return c.endsWith(".integrator") || c.endsWith(".derivative");
    }

    private static void _issue(JSONArray legacy, JSONArray diagnostics,
            String severity, String code, String subject, String message) {
        legacy.put(message);
        diagnostics.put(new JSONObject()
                .put("severity", severity)
                .put("code", code)
                .put("subject", subject)
                .put("message", message));
    }
}
