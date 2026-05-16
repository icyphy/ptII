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
import org.ptolemy.agent.agent.RefactorAdvisor;
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
    private static final int MAX_ATOMS_PER_SCOPE = 12;

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
        int entities = 0;
        int relations = 0;
        int links = 0;
        int directors = 0;
        boolean hasRecorder = false;
        boolean hasContinuousActor = false;
        String directorClass = "";

        JSONArray issues = new JSONArray();
        JSONArray diagnostics = new JSONArray();
        JSONArray scopes = new JSONArray();
        JSONArray disconnectedEntities = new JSONArray();
        JSONArray groupingSuggestions = RefactorAdvisor.suggestionsRecursive(
                session, MAX_ATOMS_PER_SCOPE);
        @SuppressWarnings("unchecked")
        java.util.List<ComponentEntity> topEntities = top.entityList();
        @SuppressWarnings("unchecked")
        java.util.List<Attribute> attributeList = top.attributeList();

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
        if (_visibleEntityCount(topEntities) == 0) {
            _issue(issues, diagnostics, "ERROR", "EMPTY_MODEL", "model",
                    "model is empty: no entities found");
        }
        if (directors == 0) {
            _issue(issues, diagnostics, "ERROR", "NO_DIRECTOR", "director",
                    "no director declared at top level: simulation will fail");
        }
        for (ComponentEntity entity : topEntities) {
            if (_isHidden(entity.getName())) continue;
            String cls = entity.getClassName();
            if (cls != null && cls.endsWith(".Recorder")) hasRecorder = true;
            if (_requiresContinuousDirector(cls)) hasContinuousActor = true;
        }
        int[] counts = _scanScope(top, "", issues, diagnostics,
                disconnectedEntities, scopes);
        entities = counts[0];
        relations = counts[1];
        links = counts[2];

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

        JSONObject data = new JSONObject();
        data.put("entities", entities);
        data.put("relations", relations);
        data.put("links", links);
        data.put("directors", directors);
        data.put("issues", issues);
        data.put("diagnostics", diagnostics);
        data.put("scopes", scopes);
        data.put("disconnectedEntities", disconnectedEntities);
        data.put("groupingSuggestions", groupingSuggestions);
        data.put("healthy", issues.length() == 0);

        boolean ok = issues.length() == 0;
        return ok
                ? AgentResult.ok("model looks healthy", data)
                : AgentResult.ok(
                        "model has " + issues.length() + " issue(s)", data);
    }

    /** Recursively scan one scope and return [entities, relations, links]. */
    private static int[] _scanScope(CompositeEntity scope, String scopePath,
            JSONArray legacy, JSONArray diagnostics,
            JSONArray disconnectedEntities, JSONArray scopes) {
        int entities = 0;
        int relations = 0;
        int links = 0;
        int atomics = 0;
        int composites = 0;

        @SuppressWarnings("unchecked")
        java.util.List<ComponentEntity> entityList = scope.entityList();
        for (ComponentEntity entity : entityList) {
            if (_isHidden(entity.getName())) {
                continue;
            }
            entities++;
            if (entity instanceof CompositeEntity) {
                composites++;
            } else {
                atomics++;
            }
            int totalEntityLinks = 0;
            int inputs = 0;
            int outputs = 0;
            @SuppressWarnings("unchecked")
            java.util.List<Port> ports = entity.portList();
            for (Port port : ports) {
                if (!(port instanceof IOPort)) {
                    continue;
                }
                IOPort io = (IOPort) port;
                int n = io.numLinks();
                links += n;
                totalEntityLinks += n;
                if (io.isInput()) {
                    inputs++;
                }
                if (io.isOutput()) {
                    outputs++;
                }
                String ref = _entityPath(scopePath, entity.getName())
                        + "." + io.getName();
                if (io.isInput() && !io.isMultiport()
                        && io.numLinks() == 0
                        && !_isOptionalInput(io)) {
                    _issue(legacy, diagnostics, "ERROR",
                            "UNCONNECTED_INPUT", ref,
                            "input port is unconnected: " + ref);
                }
                if (io.isOutput() && io.numLinks() == 0) {
                    _issue(legacy, diagnostics, "WARN",
                            "UNCONNECTED_OUTPUT", ref,
                            "output port is unconnected: " + ref);
                }
            }

            if ((inputs + outputs) == 0 || totalEntityLinks == 0) {
                JSONObject rec = _recommendDisconnectedAction(scope, scopePath,
                        entity, inputs, outputs);
                disconnectedEntities.put(rec);
                diagnostics.put(new JSONObject()
                        .put("severity", "WARN")
                        .put("code", "DISCONNECTED_ENTITY")
                        .put("subject", rec.optString("subject"))
                        .put("message", rec.optString("message"))
                        .put("recommendedAction",
                                rec.optString("recommendedAction"))
                        .put("recommendedTool",
                                rec.optString("recommendedTool"))
                        .put("recommendedArgs", rec.optJSONObject(
                                "recommendedArgs")));
                legacy.put(rec.optString("message"));
            }

            if (entity instanceof CompositeEntity) {
                String childScope = _entityPath(scopePath, entity.getName());
                int[] child = _scanScope((CompositeEntity) entity, childScope,
                        legacy, diagnostics, disconnectedEntities, scopes);
                entities += child[0];
                relations += child[1];
                links += child[2];
            }
        }

        @SuppressWarnings("unchecked")
        java.util.List<Relation> relationList = scope.relationList();
        relations += relationList.size();
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
                String relSubject = _scopePath(scopePath)
                        + ".relation:" + relation.getName();
                _issue(legacy, diagnostics, "ERROR", "INVALID_RELATION",
                        relSubject,
                        "relation does not connect output to input: "
                                + relSubject);
            }
        }

        scopes.put(new JSONObject()
                .put("scope", _scopePath(scopePath))
                .put("atomicEntities", atomics)
                .put("compositeEntities", composites)
                .put("relations", relationList.size())
                .put("overcrowded", atomics > MAX_ATOMS_PER_SCOPE)
                .put("maxRecommendedAtoms", MAX_ATOMS_PER_SCOPE));
        if (atomics > MAX_ATOMS_PER_SCOPE) {
            String scopeName = _scopePath(scopePath);
            diagnostics.put(new JSONObject()
                    .put("severity", "WARN")
                    .put("code", "OVERCROWDED_SCOPE")
                    .put("subject", scopeName)
                    .put("message",
                            "scope has too many atomic entities (" + atomics
                                    + "): consider grouping recursively"));
            legacy.put("scope has too many atomic entities: " + scopeName
                    + " (" + atomics + ")");
        }
        return new int[] { entities, relations, links };
    }

    private static boolean _isHidden(String name) {
        return name != null && name.startsWith("__recorder__");
    }

    private static int _visibleEntityCount(
            java.util.List<ComponentEntity> entities) {
        int count = 0;
        for (ComponentEntity entity : entities) {
            if (!_isHidden(entity.getName())) {
                count++;
            }
        }
        return count;
    }

    /** PortParameter-backed input ports (Ramp.init / Ramp.step /
     *  Pulse.trigger etc.) are optional: when no upstream is
     *  connected they fall back to the colocated parameter value.
     *  Flagging them as unconnected is noise.  Detect via the
     *  ParameterPort class on the classpath; tolerate the class
     *  being absent in non-actor-parameters builds. */
    private static boolean _isOptionalInput(IOPort port) {
        if (port == null) {
            return false;
        }
        try {
            Class<?> paramPortClass = Class.forName(
                    "ptolemy.actor.parameters.ParameterPort");
            if (paramPortClass.isInstance(port)) {
                return true;
            }
        } catch (Throwable ignored) {
            // ParameterPort not on classpath in this Ptolemy build.
        }
        // Fallback: a sibling Parameter with the same name as the
        // port is the historical hallmark of a PortParameter setup.
        if (port.getContainer() != null) {
            Attribute sibling = port.getContainer()
                    .getAttribute(port.getName());
            if (sibling instanceof ptolemy.data.expr.Parameter) {
                return true;
            }
        }
        return false;
    }

    private static boolean _requiresContinuousDirector(String className) {
        if (className == null) {
            return false;
        }
        String c = className.toLowerCase();
        return c.endsWith(".integrator") || c.endsWith(".derivative");
    }

    private static JSONObject _recommendDisconnectedAction(
            CompositeEntity scope, String scopePath, ComponentEntity entity,
            int inputs, int outputs) {
        String subject = _entityPath(scopePath, entity.getName());
        JSONObject out = new JSONObject();
        out.put("subject", subject);
        out.put("scope", _scopePath(scopePath));
        out.put("entity", entity.getName());
        out.put("className", entity.getClassName());

        String parentArg = scopePath == null ? "" : scopePath;
        String firstInput = _firstConnectableInput(entity);
        String firstOutput = _firstOutput(entity);
        String candidateIn = _findUnconnectedInput(scope, entity);
        String candidateOut = _findAnyOutput(scope, entity);

        if (outputs > 0 && inputs == 0 && firstOutput.length() > 0
                && candidateIn.length() > 0) {
            JSONObject args = new JSONObject()
                    .put("from", entity.getName() + "." + firstOutput)
                    .put("to", candidateIn);
            if (!parentArg.isEmpty()) {
                args.put("parent", parentArg);
            }
            out.put("recommendedAction", "CONNECT_TO_DOWNSTREAM");
            out.put("recommendedTool", "connect");
            out.put("recommendedArgs", args);
            out.put("message",
                    "disconnected source-like entity: suggest connecting "
                            + subject + " to downstream input");
            return out;
        }
        if (inputs > 0 && outputs == 0 && firstInput.length() > 0
                && candidateOut.length() > 0) {
            JSONObject args = new JSONObject()
                    .put("from", candidateOut)
                    .put("to", entity.getName() + "." + firstInput);
            if (!parentArg.isEmpty()) {
                args.put("parent", parentArg);
            }
            out.put("recommendedAction", "CONNECT_FROM_UPSTREAM");
            out.put("recommendedTool", "connect");
            out.put("recommendedArgs", args);
            out.put("message",
                    "disconnected sink-like entity: suggest connecting "
                            + subject + " from upstream output");
            return out;
        }
        JSONObject args = new JSONObject().put("name", entity.getName());
        if (!parentArg.isEmpty()) {
            args.put("parent", parentArg);
        }
        out.put("recommendedAction", "DELETE_ORPHAN");
        out.put("recommendedTool", "delete");
        out.put("recommendedArgs", args);
        out.put("message",
                "isolated entity with no clear functional integration: "
                        + "suggest delete " + subject);
        return out;
    }

    private static String _firstOutput(ComponentEntity entity) {
        @SuppressWarnings("unchecked")
        java.util.List<Port> ports = entity.portList();
        for (Port port : ports) {
            if (port instanceof IOPort && ((IOPort) port).isOutput()) {
                return ((IOPort) port).getName();
            }
        }
        return "";
    }

    private static String _firstConnectableInput(ComponentEntity entity) {
        @SuppressWarnings("unchecked")
        java.util.List<Port> ports = entity.portList();
        for (Port port : ports) {
            if (!(port instanceof IOPort)) {
                continue;
            }
            IOPort io = (IOPort) port;
            if (io.isInput() && !_isOptionalInput(io)) {
                return io.getName();
            }
        }
        return "";
    }

    private static String _findUnconnectedInput(CompositeEntity scope,
            ComponentEntity exclude) {
        @SuppressWarnings("unchecked")
        java.util.List<ComponentEntity> entities = scope.entityList();
        for (ComponentEntity entity : entities) {
            if (entity == exclude || _isHidden(entity.getName())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            java.util.List<Port> ports = entity.portList();
            for (Port port : ports) {
                if (!(port instanceof IOPort)) {
                    continue;
                }
                IOPort io = (IOPort) port;
                if (io.isInput() && io.numLinks() == 0
                        && !_isOptionalInput(io)) {
                    return entity.getName() + "." + io.getName();
                }
            }
        }
        return "";
    }

    private static String _findAnyOutput(CompositeEntity scope,
            ComponentEntity exclude) {
        @SuppressWarnings("unchecked")
        java.util.List<ComponentEntity> entities = scope.entityList();
        for (ComponentEntity entity : entities) {
            if (entity == exclude || _isHidden(entity.getName())) {
                continue;
            }
            @SuppressWarnings("unchecked")
            java.util.List<Port> ports = entity.portList();
            for (Port port : ports) {
                if (port instanceof IOPort && ((IOPort) port).isOutput()) {
                    return entity.getName() + "." + ((IOPort) port).getName();
                }
            }
        }
        return "";
    }

    private static String _scopePath(String path) {
        return (path == null || path.isEmpty()) ? "<top>" : path;
    }

    private static String _entityPath(String scopePath, String entityName) {
        if (scopePath == null || scopePath.isEmpty()) {
            return entityName;
        }
        return scopePath + "/" + entityName;
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
