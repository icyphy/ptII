/* Batch port wiring: preflight every edge, apply transactionally.

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;

///////////////////////////////////////////////////////////////////
//// ConnectManyTool

/**
 Wire many ports in one transaction.  The tool takes a list of
 {@code {from, to}} edges and:

 <ol>
 <li>preflight-validates EVERY edge against the same rules
     {@link ToolCallValidator} applies to a single {@code connect}
     (including {@link PortHints} did-you-mean candidates),</li>
 <li>if ANY edge fails, the whole call is rejected and the response
     payload reports the issues for every bad edge at once,</li>
 <li>otherwise the batch is applied as a single MoML {@code <group>}
     so the change appears atomic on the undo stack and in the
     change-listener stream.</li>
 </ol>

 <p>Fan-out is simulated in-order across the batch: when an earlier
 edge has already created a relation for a single-port source, a
 later edge from the same source reuses that relation instead of
 emitting a new one — exactly mirroring what {@link ConnectTool}
 does for one-at-a-time calls.

 <p>Args:
 <ul>
 <li>{@code edges} — required, an array of {@code {from, to}}
     objects.  Port references use the same {@code Entity.port}
     syntax as {@code connect}.</li>
 <li>{@code parent} — optional, name of the enclosing composite for
     all edges in the batch.</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class ConnectManyTool implements AgentTool {

    @Override
    public String name() {
        return "connect_many";
    }

    @Override
    public String description() {
        return "Wire many ports in one transaction.  Validates EVERY edge"
                + " first (with the same didYouMean hints as connect); if"
                + " any edge is invalid the whole batch is rejected and"
                + " all errors are returned together.  Prefer this over"
                + " calling connect repeatedly when you have 2 or more"
                + " edges — it saves tool rounds and gives a complete"
                + " diagnostic in one shot.  Fan-out from a single-port"
                + " source is handled automatically across the batch.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject edge = new JSONObject();
        edge.put("type", "object");
        JSONObject edgeProps = new JSONObject();
        edgeProps.put("from", new JSONObject().put("type", "string")
                .put("description",
                        "Source port reference, e.g. \"Ramp1.output\"."));
        edgeProps.put("to", new JSONObject().put("type", "string")
                .put("description",
                        "Destination port reference, e.g. \"Gain.input\"."));
        edge.put("properties", edgeProps);
        edge.put("required", new JSONArray().put("from").put("to"));

        JSONObject edges = new JSONObject();
        edges.put("type", "array");
        edges.put("items", edge);
        edges.put("description",
                "Edges to wire, applied as one atomic MoML group.");

        JSONObject props = new JSONObject();
        props.put("edges", edges);
        props.put("parent", new JSONObject().put("type", "string")
                .put("description",
                        "Optional enclosing composite for ALL edges in"
                                + " the batch."));

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new JSONArray().put("edges"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (session.toplevel() == null) {
            return AgentResult.fail("no model loaded");
        }
        if (!args.has("edges")
                || !(args.opt("edges") instanceof JSONArray)) {
            return AgentResult.fail(
                    "connect_many requires 'edges' as an array of"
                            + " {from, to} objects");
        }
        JSONArray edges = args.getJSONArray("edges");
        if (edges.length() == 0) {
            return AgentResult.fail(
                    "connect_many requires at least one edge");
        }
        String parent = args.optString("parent", "").trim();

        CompositeEntity top = (CompositeEntity) session.toplevel();
        CompositeEntity scope = top;
        if (parent.length() > 0) {
            ComponentEntity child = top.getEntity(parent);
            if (!(child instanceof CompositeEntity)) {
                JSONObject row = new JSONObject()
                        .put("code", "PARENT_NOT_FOUND")
                        .put("field", "parent")
                        .put("message", "no composite named '" + parent
                                + "' at top level")
                        .put("availableComposites",
                                PortHints.compositeNames(top))
                        .put("didYouMean",
                                PortHints.suggestComposites(top, parent));
                JSONObject data = new JSONObject();
                data.put("validator", "connect-many-preflight-v1");
                data.put("tool", "connect_many");
                data.put("issues", new JSONArray().put(row));
                return AgentResult.fail("connect_many: parent '" + parent
                        + "' is not a composite at top level", data);
            }
            scope = (CompositeEntity) child;
        }

        // Plan: walk every edge, decide fan-out reuse vs new relation,
        // collect per-edge issues.  Two state maps drive the planner:
        //   reusedRelationByPort:  Entity.port -> relation name to reuse
        //   newRelationByPort:     Entity.port -> relation name we will
        //                          assign for this single-port source
        // The reused map starts with the live-model state and grows as
        // we plan each edge.  When a later edge has the SAME single-port
        // source, we reuse instead of creating a new relation.
        Map<String, String> sourceRelation = new HashMap<String, String>();
        List<JSONObject> accepted = new ArrayList<JSONObject>();
        JSONArray rejected = new JSONArray();
        int nextRelIndex = _firstFreeRelIndex(scope);

        for (int i = 0; i < edges.length(); i++) {
            JSONObject e = edges.optJSONObject(i);
            JSONArray issues = new JSONArray();
            if (e == null) {
                issues.put(new JSONObject().put("code", "MALFORMED_EDGE")
                        .put("field", "edges[" + i + "]")
                        .put("message",
                                "edge entry is not an object"));
                rejected.put(_rejectionRow(i, null, null, issues));
                continue;
            }
            String from = e.optString("from", "").trim();
            String to = e.optString("to", "").trim();

            _checkEnd(scope, "from", from, "output", issues);
            _checkEnd(scope, "to", to, "input", issues);

            // Direction-reversed shortcut: both ports resolved but
            // exactly backwards.
            if (issues.length() == 0) {
                IOPort src = ConnectTool.resolvePort(scope, from);
                IOPort dst = ConnectTool.resolvePort(scope, to);
                if (src != null && dst != null
                        && dst.isOutput() && src.isInput()) {
                    JSONObject row = new JSONObject()
                            .put("code", "REVERSED_DIRECTION")
                            .put("field", "from")
                            .put("message", "from/to look reversed: '"
                                    + from + "' is an input and '" + to
                                    + "' is an output")
                            .put("suggestion", "swap from and to")
                            .put("didYouMean", new JSONArray()
                                    .put("connect_many edge { from=\""
                                            + to + "\", to=\"" + from
                                            + "\" }"));
                    issues.put(row);
                }
            }

            if (issues.length() > 0) {
                rejected.put(_rejectionRow(i, from, to, issues));
                continue;
            }

            // Plan: fan-out reuse vs new relation.
            IOPort src = ConnectTool.resolvePort(scope, from);
            String existingRel = _planSourceRelation(src, from,
                    sourceRelation);
            JSONObject plan = new JSONObject();
            plan.put("index", i);
            plan.put("from", from);
            plan.put("to", to);
            if (existingRel != null) {
                plan.put("relation", existingRel);
                plan.put("fannedOut", true);
            } else {
                String relName = "rel" + nextRelIndex++;
                while (scope.getRelation(relName) != null) {
                    relName = "rel" + nextRelIndex++;
                }
                plan.put("relation", relName);
                plan.put("fannedOut", false);
                // Remember for batch-level fan-out: only single-port
                // sources should reuse; multiport sources legitimately
                // get one new relation per call.
                if (src != null && !src.isMultiport()) {
                    sourceRelation.put(from, relName);
                }
            }
            accepted.add(plan);
        }

        if (rejected.length() > 0) {
            JSONObject data = new JSONObject();
            data.put("validator", "connect-many-preflight-v1");
            data.put("tool", "connect_many");
            data.put("rejected", rejected);
            JSONArray acceptedSummary = new JSONArray();
            for (JSONObject row : accepted) {
                acceptedSummary.put(new JSONObject(row.toString())
                        .put("status", "would-apply"));
            }
            data.put("accepted", acceptedSummary);
            String headline = rejected.length() + "/" + edges.length()
                    + " edge(s) rejected; whole batch rolled back."
                    + " Fix the rejected edges and retry.";
            return AgentResult.fail("connect_many preflight failed: "
                    + headline, data);
        }

        // All edges valid — emit one MoML group containing every new
        // relation plus every link.
        StringBuilder inner = new StringBuilder("<group>");
        java.util.Set<String> emittedRelations =
                new java.util.HashSet<String>();
        for (JSONObject plan : accepted) {
            String relName = plan.getString("relation");
            boolean fanout = plan.getBoolean("fannedOut");
            String from = plan.getString("from");
            String to = plan.getString("to");
            if (!fanout && !emittedRelations.contains(relName)) {
                inner.append("<relation name=\"")
                        .append(AddEntityTool.escape(relName))
                        .append("\" class=\"ptolemy.actor.TypedIORelation\"/>");
                inner.append("<link port=\"")
                        .append(AddEntityTool.escape(from))
                        .append("\" relation=\"")
                        .append(AddEntityTool.escape(relName))
                        .append("\"/>");
                emittedRelations.add(relName);
            }
            inner.append("<link port=\"")
                    .append(AddEntityTool.escape(to))
                    .append("\" relation=\"")
                    .append(AddEntityTool.escape(relName))
                    .append("\"/>");
        }
        inner.append("</group>");

        String moml = parent.length() == 0
                ? inner.toString()
                : "<entity name=\"" + AddEntityTool.escape(parent)
                        + "\">" + inner.toString() + "</entity>";

        AgentResult res = session.applyChange(moml);
        if (!res.ok()) {
            // applyChange shouldn't normally fail after a clean
            // preflight, but if it does (e.g. type clash detected by
            // Ptolemy at link time) report which edges were in flight.
            JSONObject data = new JSONObject();
            data.put("phase", "applyChange");
            JSONArray planned = new JSONArray();
            for (JSONObject p : accepted) {
                planned.put(p);
            }
            data.put("planned", planned);
            return AgentResult.fail(
                    "connect_many: MoML apply rejected by Ptolemy: "
                            + res.message(), data);
        }

        JSONObject data = new JSONObject();
        data.put("applied", accepted.size());
        JSONArray plan = new JSONArray();
        for (JSONObject row : accepted) {
            plan.put(row);
        }
        data.put("edges", plan);
        data.put("parent", parent);
        return AgentResult.ok("connect_many: wired " + accepted.size()
                + " edge(s)", data);
    }

    /** Validate one endpoint and append the appropriate structured
     *  issue rows to {@code issues}.  Mirrors {@link ToolCallValidator}
     *  port-issue logic so batch validation looks identical to single
     *  validation. */
    private static void _checkEnd(CompositeEntity scope, String field,
            String reference, String wantDirection, JSONArray issues) {
        if (reference == null || reference.length() == 0) {
            issues.put(new JSONObject().put("code", "MISSING_REQUIRED")
                    .put("field", field)
                    .put("message", "missing required argument: " + field));
            return;
        }
        IOPort port = ConnectTool.resolvePort(scope, reference);
        if (port == null) {
            String[] parts = PortHints.splitRef(reference);
            String entityPrefix = parts[0];
            ComponentEntity entity = entityPrefix.length() > 0
                    ? scope.getEntity(entityPrefix) : null;
            if (entityPrefix.length() > 0 && entity == null) {
                String code = "from".equals(field)
                        ? "SOURCE_ENTITY_NOT_FOUND"
                        : "DEST_ENTITY_NOT_FOUND";
                issues.put(new JSONObject().put("code", code)
                        .put("field", field)
                        .put("message", "entity '" + entityPrefix
                                + "' from " + reference
                                + " does not exist in scope")
                        .put("availableEntities",
                                PortHints.entityNames(scope))
                        .put("didYouMean",
                                PortHints.suggestEntities(scope,
                                        entityPrefix)));
            } else {
                String code = "from".equals(field)
                        ? "SOURCE_PORT_NOT_FOUND" : "DEST_PORT_NOT_FOUND";
                JSONObject row = new JSONObject().put("code", code)
                        .put("field", field)
                        .put("message", ("from".equals(field)
                                ? "source" : "destination")
                                + " port not found in scope: "
                                + reference);
                JSONObject described = PortHints.describePorts(scope,
                        entityPrefix);
                if (described.length() > 0) {
                    row.put("availablePorts", described);
                }
                row.put("didYouMean", PortHints.suggestPorts(scope,
                        reference, wantDirection));
                issues.put(row);
            }
            return;
        }
        // Direction check.
        boolean wantOut = "output".equalsIgnoreCase(wantDirection);
        boolean wantIn = "input".equalsIgnoreCase(wantDirection);
        boolean okOut = port.isOutput()
                || (port.getContainer() == scope && port.isInput());
        boolean okIn = port.isInput()
                || (port.getContainer() == scope && port.isOutput());
        if (wantOut && !okOut) {
            issues.put(_directionRow(scope, field, reference, port,
                    "output",
                    "source is not an output/boundary input"));
        } else if (wantIn && !okIn) {
            issues.put(_directionRow(scope, field, reference, port,
                    "input",
                    "destination is not an input/boundary output"));
        }
    }

    private static JSONObject _directionRow(CompositeEntity scope,
            String field, String reference, IOPort port,
            String wantDirection, String headline) {
        String code = "from".equals(field) ? "SOURCE_NOT_OUTPUT"
                : "DEST_NOT_INPUT";
        String actual;
        if (port.isInput() && port.isOutput()) {
            actual = "both";
        } else if (port.isInput()) {
            actual = "input";
        } else if (port.isOutput()) {
            actual = "output";
        } else {
            actual = "neither";
        }
        JSONObject row = new JSONObject().put("code", code)
                .put("field", field)
                .put("message", headline + ": " + reference)
                .put("actualDirection", actual);
        JSONObject portsDescription = PortHints.describePorts(scope,
                PortHints.splitRef(reference)[0]);
        if (portsDescription.length() > 0) {
            row.put("availablePorts", portsDescription);
        }
        row.put("didYouMean", PortHints.suggestPorts(scope, reference,
                wantDirection));
        return row;
    }

    /** Decide whether this edge should reuse an existing relation on
     *  its source port.  Honors BOTH the live-model state (a relation
     *  pre-existed) and earlier accepted edges in this same batch
     *  ({@code sourceRelation} cache).
     *  @return The relation name to reuse, or null when a new
     *      relation must be created. */
    private static String _planSourceRelation(IOPort source, String from,
            Map<String, String> sourceRelation) {
        if (source == null || source.isMultiport()) {
            // Multiport sources legitimately get one fresh relation
            // per call — no fan-out reuse needed.
            return null;
        }
        String cached = sourceRelation.get(from);
        if (cached != null) {
            return cached;
        }
        List<?> existing = source.linkedRelationList();
        for (Object obj : existing) {
            if (obj instanceof Relation) {
                Relation rel = (Relation) obj;
                sourceRelation.put(from, rel.getName());
                return rel.getName();
            }
        }
        return null;
    }

    /** Find the first {@code relN} index not currently used in
     *  {@code scope}; gives the batch a stable starting point. */
    private static int _firstFreeRelIndex(CompositeEntity scope) {
        for (int i = 0; i < 1_000_000; i++) {
            if (scope.getRelation("rel" + i) == null) {
                return i;
            }
        }
        return (int) (System.nanoTime() & 0x7fffffff);
    }

    private static JSONObject _rejectionRow(int index, String from,
            String to, JSONArray issues) {
        JSONObject row = new JSONObject();
        row.put("index", index);
        JSONObject edge = new JSONObject();
        if (from != null) edge.put("from", from);
        if (to   != null) edge.put("to",   to);
        row.put("edge", edge);
        row.put("issues", issues);
        return row;
    }
}
