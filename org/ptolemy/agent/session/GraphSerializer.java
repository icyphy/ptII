/* Walk the Ptolemy entity tree of a session and emit a JSON graph
 suitable for React Flow consumption on the frontend.

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
package org.ptolemy.agent.session;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// GraphSerializer

/**
 Walks the immediate-child entity / relation graph of a Ptolemy
 composite and emits a JSON object of the form
 <pre>
 {
   "directors": [{"id": "...", "className": "..."}],
   "nodes": [
     {"id": "Ramp1",
      "className": "ptolemy.actor.lib.Ramp",
      "displayName": "Ramp1",
      "position": {"x": 100, "y": 200},
      "inputs":  [{"name": "input",  "id": "Ramp1.input"}],
      "outputs": [{"name": "output", "id": "Ramp1.output"}],
      "parameters": [{"name": "step", "value": "1.0"}, ...]}
   ],
   "edges": [
     {"id": "rel0",
      "source": "Ramp1", "sourceHandle": "Ramp1.output",
      "target": "Sink",  "targetHandle": "Sink.input"}
   ]
 }
 </pre>

 <p>This shape is intentionally a 1:1 fit for the React Flow API on
 the frontend, so the same JSON can be rendered without any reshaping
 on the browser side.

 <p>Hidden helper actors (anything whose name starts with
 {@code __recorder__}) are filtered out so the user never sees the
 auto-injected probes from {@link SignalCollector}.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class GraphSerializer {

    private static final String HIDDEN_PREFIX = "__recorder__";

    private GraphSerializer() {
    }

    /** Serialize the given composite as a graph JSON.
     *  @param toplevel The root entity (typically a session's toplevel).
     *  @return A JSON object as described in the class comment. Empty
     *      graph if {@code toplevel} is null.
     */
    public static JSONObject serialize(CompositeEntity toplevel) {
        return serialize(toplevel, java.util.Collections.<String>emptyList());
    }

    /** Serialize the composite reached by walking {@code path} from
     *  {@code toplevel}. An empty path serializes the top level. Each
     *  segment is the {@link ComponentEntity#getName() name} of an
     *  immediate child composite at that level.
     *
     *  <p>The result also exposes the chain of composites traversed in
     *  a {@code "path"} array so the frontend breadcrumb can render
     *  without round-tripping through the MoML.
     *
     *  @param toplevel The root entity.
     *  @param path Names of nested composites to descend into, e.g.
     *      {@code ["Plant", "Inner"]}.
     *  @return A JSON object describing the resolved level.
     */
    public static JSONObject serialize(CompositeEntity toplevel,
            List<String> path) {
        JSONObject root = new JSONObject();
        JSONArray nodes = new JSONArray();
        JSONArray edges = new JSONArray();
        JSONArray directors = new JSONArray();
        JSONArray pathOut = new JSONArray();
        root.put("nodes", nodes);
        root.put("edges", edges);
        root.put("directors", directors);
        root.put("path", pathOut);

        if (toplevel == null) {
            return root;
        }

        // Walk the path. If a segment is missing, fall back to the
        // deepest valid composite and report an error so the frontend
        // can recover gracefully (e.g. composite was deleted).
        CompositeEntity current = toplevel;
        if (path != null) {
            for (String segment : path) {
                if (segment == null || segment.isEmpty()) {
                    continue;
                }
                ComponentEntity child = current.getEntity(segment);
                if (!(child instanceof CompositeEntity)) {
                    root.put("pathError",
                            "no such composite at this level: " + segment);
                    break;
                }
                pathOut.put(segment);
                current = (CompositeEntity) child;
            }
        }

        root.put("topName", current.getName());
        root.put("topClass", current.getClassName());
        root.put("isComposite", current != toplevel);

        // Top-level director(s) appear as properties whose class
        // extends Director; expose them so the frontend canvas can
        // surface a "director" badge.
        @SuppressWarnings("unchecked")
        List<Attribute> attrs = current.attributeList();
        for (Attribute attr : attrs) {
            String klass = attr.getClassName();
            if (klass != null && klass.endsWith("Director")) {
                JSONObject d = new JSONObject();
                d.put("id", attr.getName());
                d.put("className", klass);
                directors.put(d);
            }
        }

        // When inside a composite, expose its boundary I/O ports as
        // pseudo "boundary" nodes so users can see and wire them.
        if (current != toplevel) {
            @SuppressWarnings("unchecked")
            List<?> boundary = current.portList();
            for (Object o : boundary) {
                if (o instanceof IOPort) {
                    JSONObject n = boundaryNodeJson(current, (IOPort) o);
                    if (n != null) {
                        nodes.put(n);
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<ComponentEntity> entities = current.entityList();
        for (ComponentEntity entity : entities) {
            if (isHidden(entity)) {
                continue;
            }
            nodes.put(nodeJson(entity));
        }

        @SuppressWarnings("unchecked")
        List<Relation> relations = current.relationList();
        for (Relation relation : relations) {
            JSONObject edge = edgeJson(relation, current);
            if (edge != null) {
                edges.put(edge);
            }
        }

        return root;
    }

    /** Boundary-port pseudo-node, rendered as a small marker on the
     *  edge of the canvas when viewing inside a composite. */
    private static JSONObject boundaryNodeJson(CompositeEntity owner,
            IOPort port) {
        JSONObject node = new JSONObject();
        String id = "__boundary__" + port.getName();
        node.put("id", id);
        node.put("displayName", port.getName());
        node.put("className", "ptolemy.actor.TypedIOPort");
        node.put("boundary", true);
        node.put("boundaryRole",
                port.isInput() && port.isOutput() ? "io"
                        : port.isInput() ? "input" : "output");

        // Inputs sit on the left side; outputs on the right.
        JSONObject pos = new JSONObject();
        pos.put("x", port.isInput() && !port.isOutput() ? -180 : 720);
        pos.put("y", 80);
        node.put("position", pos);

        JSONArray inputs = new JSONArray();
        JSONArray outputs = new JSONArray();
        // Boundary semantics flip: from inside the composite, an input
        // boundary acts as an OUTPUT (it produces values into the
        // composite), and vice versa.
        JSONObject portJson = new JSONObject();
        portJson.put("name", port.getName());
        portJson.put("id", id + "." + port.getName());
        portJson.put("multiport", port.isMultiport());
        if (port.isInput()) {
            outputs.put(portJson);
        }
        if (port.isOutput()) {
            inputs.put(portJson);
        }
        node.put("inputs", inputs);
        node.put("outputs", outputs);
        node.put("parameters", new JSONArray());
        return node;
    }

    private static JSONObject nodeJson(ComponentEntity entity) {
        JSONObject node = new JSONObject();
        node.put("id", entity.getName());
        node.put("displayName", entity.getName());
        node.put("className", entity.getClassName());

        node.put("position", positionJson(entity));

        JSONArray inputs = new JSONArray();
        JSONArray outputs = new JSONArray();
        if (entity instanceof TypedAtomicActor) {
            @SuppressWarnings("unchecked")
            List<?> inList = ((TypedAtomicActor) entity).inputPortList();
            for (Object o : inList) {
                if (o instanceof IOPort) {
                    inputs.put(portJson(entity, (IOPort) o));
                }
            }
            @SuppressWarnings("unchecked")
            List<?> outList = ((TypedAtomicActor) entity).outputPortList();
            for (Object o : outList) {
                if (o instanceof IOPort) {
                    outputs.put(portJson(entity, (IOPort) o));
                }
            }
        } else {
            // For other kinds of entities, fall back to enumerating
            // every port.
            @SuppressWarnings("unchecked")
            List<?> ports = entity.portList();
            for (Object o : ports) {
                if (o instanceof IOPort) {
                    IOPort port = (IOPort) o;
                    if (port.isInput()) {
                        inputs.put(portJson(entity, port));
                    } else if (port.isOutput()) {
                        outputs.put(portJson(entity, port));
                    }
                }
            }
        }
        node.put("inputs", inputs);
        node.put("outputs", outputs);

        JSONArray params = new JSONArray();
        @SuppressWarnings("unchecked")
        List<Attribute> attrs = entity.attributeList();
        for (Attribute attr : attrs) {
            if (attr instanceof ptolemy.data.expr.Parameter) {
                ptolemy.data.expr.Parameter p =
                        (ptolemy.data.expr.Parameter) attr;
                JSONObject pj = new JSONObject();
                pj.put("name", p.getName());
                pj.put("value", p.getExpression());
                params.put(pj);
            }
        }
        node.put("parameters", params);
        return node;
    }

    private static JSONObject portJson(NamedObj owner, IOPort port) {
        JSONObject pj = new JSONObject();
        pj.put("name", port.getName());
        pj.put("id", owner.getName() + "." + port.getName());
        pj.put("multiport", port.isMultiport());
        return pj;
    }

    private static JSONObject positionJson(ComponentEntity entity) {
        Attribute loc = entity.getAttribute("_location");
        if (loc instanceof Location) {
            double[] xy = ((Location) loc).getLocation();
            if (xy != null && xy.length >= 2) {
                JSONObject out = new JSONObject();
                out.put("x", xy[0]);
                out.put("y", xy[1]);
                return out;
            }
        }
        JSONObject fallback = new JSONObject();
        fallback.put("x", 100);
        fallback.put("y", 100);
        return fallback;
    }

    private static JSONObject edgeJson(Relation relation,
            CompositeEntity scope) {
        @SuppressWarnings("unchecked")
        List<?> linked = relation.linkedPortList();
        // For now we only render two-port relations as a single edge,
        // which covers all of the canonical demos. Multi-way
        // relations are flattened into a star later.
        IOPort src = null;
        IOPort dst = null;
        boolean srcIsBoundary = false;
        boolean dstIsBoundary = false;
        for (Object o : linked) {
            if (!(o instanceof IOPort)) {
                continue;
            }
            IOPort port = (IOPort) o;
            if (isHidden(port.getContainer())) {
                continue;
            }
            // A port on the scope itself is a boundary port. Inside the
            // composite, an input boundary acts as a SOURCE, and an
            // output boundary acts as a SINK.
            boolean onBoundary = (port.getContainer() == scope);
            if (onBoundary) {
                if (port.isInput()) {
                    if (src == null) {
                        src = port;
                        srcIsBoundary = true;
                    }
                } else if (port.isOutput()) {
                    if (dst == null) {
                        dst = port;
                        dstIsBoundary = true;
                    }
                }
                continue;
            }
            if (port.isOutput()) {
                if (src == null) {
                    src = port;
                }
            } else if (port.isInput()) {
                if (dst == null) {
                    dst = port;
                }
            }
        }
        if (src == null || dst == null) {
            return null;
        }
        JSONObject edge = new JSONObject();
        edge.put("id", relation.getName());
        if (srcIsBoundary) {
            String pseudo = "__boundary__" + src.getName();
            edge.put("source", pseudo);
            edge.put("sourceHandle", pseudo + "." + src.getName());
        } else {
            NamedObj owner = src.getContainer();
            edge.put("source", owner.getName());
            edge.put("sourceHandle", owner.getName() + "." + src.getName());
        }
        if (dstIsBoundary) {
            String pseudo = "__boundary__" + dst.getName();
            edge.put("target", pseudo);
            edge.put("targetHandle", pseudo + "." + dst.getName());
        } else {
            NamedObj owner = dst.getContainer();
            edge.put("target", owner.getName());
            edge.put("targetHandle", owner.getName() + "." + dst.getName());
        }
        return edge;
    }

    private static boolean isHidden(NamedObj obj) {
        return obj == null || obj.getName() == null
                || obj.getName().startsWith(HIDDEN_PREFIX);
    }

    /** Convenience: serialize the current model held by a session. */
    public static JSONObject serialize(PtolemySession session) {
        if (session == null || session.toplevel() == null) {
            return serialize((CompositeEntity) null);
        }
        return serialize((CompositeEntity) session.toplevel());
    }

    /** Convenience: serialize a session at the given composite path. */
    public static JSONObject serialize(PtolemySession session,
            List<String> path) {
        if (session == null || session.toplevel() == null) {
            return serialize((CompositeEntity) null,
                    java.util.Collections.<String>emptyList());
        }
        return serialize((CompositeEntity) session.toplevel(), path);
    }
}
