/* Tool that wraps an existing set of atomic actors into a new
 TypedCompositeActor in-place, automatically rewiring boundary ports.

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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Relation;

///////////////////////////////////////////////////////////////////
//// GroupIntoCompositeTool

/**
 Wrap an existing set of sibling entities into a new
 {@code TypedCompositeActor} in place. The relations between members
 stay where they are (logically pulled inside the new composite); any
 connection that crosses the new boundary is automatically split into
 two halves through a freshly-created boundary port on the composite.

 <p>This is an idempotent refactor: the simulation behaviour is
 unchanged, only the visual hierarchy and {@code .moml} footprint shrinks.

 <p>Args:
 <ul>
 <li>{@code name} -- required, name for the new composite (must be
     unique inside the parent scope).</li>
 <li>{@code members} -- required, JSON array of entity names that
     already exist in the scope and will be moved into the new
     composite.</li>
 <li>{@code parent} -- optional, name of the existing composite that
     currently holds the members. Defaults to top-level.</li>
 <li>{@code x}, {@code y} -- optional canvas coordinates for the new
     composite block. Defaults to the centroid of the members.</li>
 </ul>

 <p>Implementation notes:
 <ol>
 <li>Capture each member's full MoML via
     {@link ptolemy.kernel.util.NamedObj#exportMoML}.</li>
 <li>Walk every relation that touches a member port. Classify it as
     <em>internal</em> (every linked port belongs to a member) or
     <em>crossing</em> (at least one linked port is outside the
     member set).</li>
 <li>Build a single MoML {@code <group>} that:
     <ul>
     <li>deletes the members (which also removes their pure-internal
         relations),</li>
     <li>creates a new composite with one boundary port per member port
         that participates in a crossing relation,</li>
     <li>recreates the members inside the composite using their captured
         MoML,</li>
     <li>recreates internal relations inside the composite,</li>
     <li>links each member port that needed a boundary to the matching
         boundary port,</li>
     <li>and recreates external relations that connect the boundary
         ports to whatever sat at the other end of the original crossing
         relations.</li>
     </ul>
 </li>
 </ol>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class GroupIntoCompositeTool implements AgentTool {

    @Override
    public String name() {
        return "group_into_composite";
    }

    @Override
    public String description() {
        return "Wrap a set of existing sibling actors into a new"
                + " TypedCompositeActor in place. Boundary ports and"
                + " external wiring are generated automatically. Use this"
                + " to organise complex models AFTER the actors have been"
                + " created -- it is faster and safer than delete/recreate.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("name", new JSONObject().put("type", "string")
                .put("description",
                        "Name of the new composite, e.g. \"PID\" or"
                                + " \"FilterStage\"."));
        props.put("members", new JSONObject().put("type", "array")
                .put("items", new JSONObject().put("type", "string"))
                .put("description",
                        "Names of existing sibling actors to move inside"
                                + " the new composite."));
        props.put("parent", new JSONObject().put("type", "string")
                .put("description",
                        "Optional name of the composite that currently"
                                + " contains the members. Defaults to the"
                                + " top-level model."));
        props.put("x", new JSONObject().put("type", "integer")
                .put("description",
                        "Optional X canvas coordinate. Defaults to the"
                                + " members' centroid."));
        props.put("y", new JSONObject().put("type", "integer")
                .put("description",
                        "Optional Y canvas coordinate. Defaults to the"
                                + " members' centroid."));

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new JSONArray().put("name").put("members"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (!args.has("name") || !args.has("members")) {
            return AgentResult.fail(
                    "group_into_composite requires 'name' and 'members'");
        }
        String compositeName = AddEntityTool.sanitize(args.getString("name"));
        JSONArray membersArr = args.getJSONArray("members");
        String parentName = args.optString("parent", "").trim();

        if (session.toplevel() == null) {
            return AgentResult.fail("no model loaded");
        }
        CompositeEntity top = (CompositeEntity) session.toplevel();

        CompositeEntity scope = top;
        if (parentName.length() > 0) {
            ComponentEntity child = top.getEntity(parentName);
            if (!(child instanceof CompositeEntity)) {
                return AgentResult.fail(
                        "no composite named '" + parentName
                                + "' at top level");
            }
            scope = (CompositeEntity) child;
        }
        if (scope.getEntity(compositeName) != null
                || scope.getRelation(compositeName) != null) {
            return AgentResult.fail("name already in use in scope: "
                    + compositeName);
        }

        // 1. Resolve members.
        Set<ComponentEntity> members = new LinkedHashSet<ComponentEntity>();
        for (int i = 0; i < membersArr.length(); i++) {
            String mn = membersArr.optString(i, "").trim();
            if (mn.length() == 0) {
                continue;
            }
            ComponentEntity ent = scope.getEntity(mn);
            if (ent == null) {
                return AgentResult.fail(
                        "member not found in scope: " + mn);
            }
            members.add(ent);
        }
        if (members.isEmpty()) {
            return AgentResult.fail(
                    "members must contain at least one entity");
        }

        // 2. Walk all relations attached to member ports and classify.
        Set<Relation> internalRelations = new LinkedHashSet<Relation>();
        Set<Relation> crossingRelations = new LinkedHashSet<Relation>();
        // Map from a member port that needs to be exposed -> chosen
        // boundary-port name (insertion-ordered for stable MoML output).
        Map<IOPort, String> portToBoundary = new LinkedHashMap<IOPort, String>();
        // For each crossing relation we record the external (non-member)
        // ports it touches, so we can wire them to the new boundary.
        Map<Relation, List<IOPort>> crossingExternal =
                new LinkedHashMap<Relation, List<IOPort>>();

        Set<String> usedBoundaryNames = new HashSet<String>();
        long sumX = 0;
        long sumY = 0;
        int positionCount = 0;
        for (ComponentEntity m : members) {
            int[] xy = entityLocation(m);
            if (xy != null) {
                sumX += xy[0];
                sumY += xy[1];
                positionCount++;
            }
            List<?> ports = m.portList();
            for (Object portObj : ports) {
                if (!(portObj instanceof IOPort)) {
                    continue;
                }
                IOPort port = (IOPort) portObj;
                List<?> rels = port.linkedRelationList();
                for (Object relObj : rels) {
                    if (!(relObj instanceof Relation)) {
                        continue;
                    }
                    Relation rel = (Relation) relObj;
                    boolean hasExternal = false;
                    List<IOPort> externals = new ArrayList<IOPort>();
                    List<?> linked = rel.linkedPortList();
                    for (Object lp : linked) {
                        if (!(lp instanceof IOPort)) {
                            continue;
                        }
                        IOPort linkedPort = (IOPort) lp;
                        ComponentEntity owner = portOwner(linkedPort);
                        if (owner == null || !members.contains(owner)) {
                            hasExternal = true;
                            externals.add(linkedPort);
                        }
                    }
                    if (hasExternal) {
                        crossingRelations.add(rel);
                        crossingExternal
                                .computeIfAbsent(rel,
                                        k -> new ArrayList<IOPort>())
                                .addAll(externals);
                        // The current port (which belongs to a member)
                        // needs a matching boundary.
                        if (!portToBoundary.containsKey(port)) {
                            String base = AddEntityTool.sanitize(
                                    port.getName());
                            String chosen = base;
                            int n = 2;
                            while (usedBoundaryNames.contains(chosen)
                                    || scope.getPort(chosen) != null) {
                                chosen = m.getName() + "_" + base;
                                if (usedBoundaryNames.contains(chosen)) {
                                    chosen = m.getName() + "_" + base + n;
                                    n++;
                                }
                            }
                            usedBoundaryNames.add(chosen);
                            portToBoundary.put(port, chosen);
                        }
                    } else {
                        internalRelations.add(rel);
                    }
                }
            }
        }

        // 3. Capture MoML for every member.
        Map<String, String> memberMoml = new LinkedHashMap<String, String>();
        for (ComponentEntity m : members) {
            StringWriter sw = new StringWriter();
            try {
                m.exportMoML(sw, 1);
            } catch (java.io.IOException e) {
                return AgentResult.fail(
                        "failed to capture MoML for "
                                + m.getName() + ": " + e.getMessage());
            }
            memberMoml.put(m.getName(), sw.toString());
        }

        // 4. Pick coordinates for the new composite.
        int x;
        int y;
        if (args.has("x") && args.has("y")) {
            x = args.optInt("x", 200);
            y = args.optInt("y", 200);
        } else if (positionCount > 0) {
            x = (int) (sumX / positionCount);
            y = (int) (sumY / positionCount);
        } else {
            x = 200;
            y = 200;
        }

        // 5. Build the MoML group.
        StringBuilder body = new StringBuilder();
        body.append("<group>");

        // 5a. Unlink member ports from every crossing relation so the
        //     relation survives but only its external endpoints remain
        //     attached. We will re-attach the new boundary port to the
        //     same relation in step 5h, which keeps the external
        //     endpoints' single-port slots clean.
        for (Map.Entry<Relation, List<IOPort>> e
                : crossingExternal.entrySet()) {
            Relation rel = e.getKey();
            // Unlink every member port that participated in this relation.
            List<?> linked = rel.linkedPortList();
            for (Object lp : linked) {
                if (!(lp instanceof IOPort)) {
                    continue;
                }
                IOPort p = (IOPort) lp;
                ComponentEntity owner = portOwner(p);
                if (owner != null && members.contains(owner)) {
                    body.append("<unlink relation=\"")
                            .append(AddEntityTool.escape(rel.getName()))
                            .append("\" port=\"")
                            .append(AddEntityTool.escape(owner.getName()))
                            .append(".")
                            .append(AddEntityTool.escape(p.getName()))
                            .append("\"/>");
                }
            }
        }

        // 5b. Delete the member entities. Pure-internal relations between
        //     members go away with them; we will recreate those inside.
        for (ComponentEntity m : members) {
            body.append("<deleteEntity name=\"")
                    .append(AddEntityTool.escape(m.getName()))
                    .append("\"/>");
        }

        // 5c. Create the new composite with all boundary ports.
        body.append("<entity name=\"")
                .append(AddEntityTool.escape(compositeName))
                .append("\" class=\"ptolemy.actor.TypedCompositeActor\">");
        body.append("<property name=\"_location\""
                + " class=\"ptolemy.kernel.util.Location\"")
                .append(" value=\"[").append(x).append(".0, ").append(y)
                .append(".0]\"/>");
        for (Map.Entry<IOPort, String> e : portToBoundary.entrySet()) {
            IOPort memberPort = e.getKey();
            String boundaryName = e.getValue();
            body.append("<port name=\"")
                    .append(AddEntityTool.escape(boundaryName))
                    .append("\" class=\"ptolemy.actor.TypedIOPort\">");
            if (memberPort.isInput()) {
                body.append("<property name=\"input\"/>");
            }
            if (memberPort.isOutput()) {
                body.append("<property name=\"output\"/>");
            }
            // Critical: when a member port is a multiport (e.g.
            // AddSubtract.plus / MultiplyDivide.multiply), the boundary
            // port that represents it MUST also be a multiport. Otherwise
            // re-attaching the original two-or-more crossing relations
            // back into the composite would trigger
            // "Attempt to link more than one relation to a single port".
            if (memberPort.isMultiport()) {
                body.append("<property name=\"multiport\"/>");
            }
            body.append("</port>");
        }

        // 5d. Recreate members inside the new composite using their
        //     captured MoML. exportMoML(writer, 1) starts each member with
        //     `<entity ...>` and ends with `</entity>`, exactly what we
        //     want here.
        for (String mname : memberMoml.keySet()) {
            body.append(memberMoml.get(mname));
        }

        // 5e. Recreate internal relations + their links inside.
        int relIdx = 0;
        for (Relation r : internalRelations) {
            String relName = r.getName();
            body.append("<relation name=\"")
                    .append(AddEntityTool.escape(relName))
                    .append("\" class=\"")
                    .append(r.getClass().getName().replace("ComponentRelation",
                            "ComponentRelation"))
                    .append("\"/>");
            List<?> linked = r.linkedPortList();
            for (Object lp : linked) {
                if (!(lp instanceof IOPort)) {
                    continue;
                }
                IOPort p = (IOPort) lp;
                ComponentEntity owner = portOwner(p);
                if (owner == null || !members.contains(owner)) {
                    continue;
                }
                body.append("<link relation=\"")
                        .append(AddEntityTool.escape(relName))
                        .append("\" port=\"")
                        .append(AddEntityTool.escape(owner.getName()))
                        .append(".")
                        .append(AddEntityTool.escape(p.getName()))
                        .append("\"/>");
            }
        }

        // 5f. Connect each exposed member port to its boundary port via
        //     a fresh internal relation.
        for (Map.Entry<IOPort, String> e : portToBoundary.entrySet()) {
            IOPort memberPort = e.getKey();
            String boundaryName = e.getValue();
            ComponentEntity owner = portOwner(memberPort);
            String relName = "_b" + (relIdx++);
            body.append("<relation name=\"").append(relName)
                    .append("\" class=\"ptolemy.actor.TypedIORelation\"/>");
            body.append("<link relation=\"").append(relName)
                    .append("\" port=\"")
                    .append(AddEntityTool.escape(owner.getName()))
                    .append(".")
                    .append(AddEntityTool.escape(memberPort.getName()))
                    .append("\"/>");
            body.append("<link relation=\"").append(relName)
                    .append("\" port=\"")
                    .append(AddEntityTool.escape(boundaryName))
                    .append("\"/>");
        }
        body.append("</entity>"); // end composite

        // 5g. Re-attach each existing top-level relation to the new
        //     composite's matching boundary port. The relation already
        //     keeps its non-member endpoints, so external single-ports
        //     stay at exactly one relation: the same one as before, just
        //     pointing into the composite now.
        for (Map.Entry<Relation, List<IOPort>> e
                : crossingExternal.entrySet()) {
            Relation rel = e.getKey();
            // Determine the boundary port we should link to: pick the
            // first member port we exposed for this relation.
            String boundaryName = null;
            List<?> linkedPorts = rel.linkedPortList();
            for (Object lp : linkedPorts) {
                if (!(lp instanceof IOPort)) {
                    continue;
                }
                IOPort p = (IOPort) lp;
                if (portToBoundary.containsKey(p)) {
                    boundaryName = portToBoundary.get(p);
                    break;
                }
            }
            if (boundaryName == null) {
                continue;
            }
            body.append("<link relation=\"")
                    .append(AddEntityTool.escape(rel.getName()))
                    .append("\" port=\"")
                    .append(AddEntityTool.escape(compositeName))
                    .append(".")
                    .append(AddEntityTool.escape(boundaryName))
                    .append("\"/>");
        }

        body.append("</group>");

        String moml = parentName.length() == 0
                ? body.toString()
                : "<entity name=\"" + AddEntityTool.escape(parentName) + "\">"
                        + body.toString() + "</entity>";

        AgentResult res = session.applyChange(moml);
        if (!res.ok()) {
            // Ptolemy MoML `<group>` is NOT transactional: when the
            // ChangeRequest fails partway, anything already applied
            // sticks around. The most common stuck piece is the
            // composite shell itself (it was the first thing emitted),
            // which would then collide with the agent's next retry
            // under the same name. Best-effort cleanup: try to delete
            // the half-created composite so the model state is the
            // same as before this tool call.
            try {
                CompositeEntity scopeNow = (CompositeEntity)
                        session.toplevel();
                if (parentName.length() > 0) {
                    ComponentEntity parentEnt = scopeNow.getEntity(
                            parentName);
                    if (parentEnt instanceof CompositeEntity) {
                        scopeNow = (CompositeEntity) parentEnt;
                    }
                }
                if (scopeNow != null
                        && scopeNow.getEntity(compositeName) != null) {
                    String cleanup = parentName.length() == 0
                            ? "<deleteEntity name=\""
                                    + AddEntityTool.escape(compositeName)
                                    + "\"/>"
                            : "<entity name=\""
                                    + AddEntityTool.escape(parentName)
                                    + "\"><deleteEntity name=\""
                                    + AddEntityTool.escape(compositeName)
                                    + "\"/></entity>";
                    session.applyChange(cleanup);
                }
            } catch (Throwable ignored) {
                // Best-effort: if cleanup itself fails, surface the
                // original error to the caller anyway.
            }
            return res;
        }

        JSONObject data = new JSONObject();
        data.put("name", compositeName);
        data.put("parent", parentName);
        JSONArray inputsArr = new JSONArray();
        JSONArray outputsArr = new JSONArray();
        for (Map.Entry<IOPort, String> e : portToBoundary.entrySet()) {
            if (e.getKey().isInput()) {
                inputsArr.put(e.getValue());
            }
            if (e.getKey().isOutput()) {
                outputsArr.put(e.getValue());
            }
        }
        data.put("inputs", inputsArr);
        data.put("outputs", outputsArr);
        data.put("memberCount", members.size());
        return AgentResult.ok(
                "wrapped " + members.size() + " actors into composite "
                        + compositeName + " ("
                        + inputsArr.length() + " in, "
                        + outputsArr.length() + " out)",
                data);
    }

    private static ComponentEntity portOwner(IOPort port) {
        ptolemy.kernel.util.NamedObj parent = port.getContainer();
        if (parent instanceof ComponentEntity) {
            return (ComponentEntity) parent;
        }
        return null;
    }

    private static int[] entityLocation(ComponentEntity entity) {
        ptolemy.kernel.util.Attribute attr = entity.getAttribute("_location");
        if (attr instanceof ptolemy.kernel.util.Location) {
            try {
                double[] xy = ((ptolemy.kernel.util.Location) attr)
                        .getLocation();
                if (xy != null && xy.length >= 2) {
                    return new int[] { (int) xy[0], (int) xy[1] };
                }
            } catch (Exception ignored) {
                // fall through
            }
        }
        return null;
    }
}
