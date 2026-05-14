/* List the entities currently present in the session model.

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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ListEntitiesTool

/**
 Enumerate the entities currently in the session model. This is the
 counterpart to {@code list_library}: where {@code list_library}
 returns the catalog of available actor classes, this tool returns the
 actors that have already been instantiated in THIS session's model.

 <p>Without this tool, agents typically loop calling {@code list_library}
 over and over hoping it will report the current model — a common
 stalled pattern.

 <p>Args:
 <ul>
 <li>{@code parent} -- optional name of a composite to inspect. When
     omitted, returns the top-level entity list.</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class ListEntitiesTool implements AgentTool {

    @Override
    public String name() {
        return "list_entities";
    }

    @Override
    public String description() {
        return "List the actors currently in the session model"
                + " (NOT the library catalog — for that use list_library)."
                + " Returns each entity's name, className, location and"
                + " parameter values. Optionally pass parent=<composite>"
                + " to enumerate inside a sub-composite.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("parent", new JSONObject().put("type", "string")
                .put("description",
                        "Optional composite name. When omitted, lists"
                                + " the top-level entities."));
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (session.toplevel() == null) {
            return AgentResult.fail("no model loaded");
        }
        CompositeEntity scope = (CompositeEntity) session.toplevel();
        String parent = args == null
                ? "" : args.optString("parent", "").trim();
        if (parent.length() > 0) {
            ComponentEntity child = scope.getEntity(parent);
            if (!(child instanceof CompositeEntity)) {
                return AgentResult.fail(
                        "no composite named '" + parent + "' in scope");
            }
            scope = (CompositeEntity) child;
        }

        JSONArray entities = new JSONArray();
        @SuppressWarnings("unchecked")
        List<ComponentEntity> kids = scope.entityList();
        for (ComponentEntity ent : kids) {
            // Filter out auto-injected probes — they are infrastructure,
            // not part of the user's model.
            String name = ent.getName();
            if (name != null && name.startsWith("__recorder__")) {
                continue;
            }
            JSONObject row = new JSONObject();
            row.put("name", name);
            row.put("className", ent.getClassName());
            row.put("isComposite", ent instanceof CompositeEntity);

            // Parameters (current expressions).
            JSONArray params = new JSONArray();
            @SuppressWarnings("unchecked")
            List<Attribute> attrs = ent.attributeList();
            for (Attribute a : attrs) {
                if (a instanceof ptolemy.data.expr.Parameter) {
                    JSONObject p = new JSONObject();
                    p.put("name", a.getName());
                    p.put("value",
                            ((ptolemy.data.expr.Parameter) a)
                                    .getExpression());
                    params.put(p);
                }
            }
            if (params.length() > 0) {
                row.put("parameters", params);
            }

            // Port summary — useful so the LLM can plan connect calls
            // without an extra describe_actor round-trip.
            if (ent instanceof TypedAtomicActor) {
                JSONArray inputs = new JSONArray();
                JSONArray outputs = new JSONArray();
                @SuppressWarnings("unchecked")
                List<?> inList = ((TypedAtomicActor) ent).inputPortList();
                for (Object o : inList) {
                    if (o instanceof IOPort) {
                        inputs.put(((IOPort) o).getName());
                    }
                }
                @SuppressWarnings("unchecked")
                List<?> outList = ((TypedAtomicActor) ent).outputPortList();
                for (Object o : outList) {
                    if (o instanceof IOPort) {
                        outputs.put(((IOPort) o).getName());
                    }
                }
                if (inputs.length() > 0)  row.put("inputs",  inputs);
                if (outputs.length() > 0) row.put("outputs", outputs);
            }

            entities.put(row);
        }

        // Director (if any) is a property at scope level; surface it.
        JSONArray directors = new JSONArray();
        @SuppressWarnings("unchecked")
        List<Attribute> propsList = scope.attributeList();
        for (Attribute a : propsList) {
            String cls = a.getClassName();
            if (cls != null && cls.endsWith("Director")) {
                JSONObject d = new JSONObject();
                d.put("name", a.getName());
                d.put("className", cls);
                JSONArray dParams = new JSONArray();
                @SuppressWarnings("unchecked")
                List<Attribute> sub = ((NamedObj) a).attributeList();
                for (Attribute s : sub) {
                    if (s instanceof ptolemy.data.expr.Parameter) {
                        JSONObject p = new JSONObject();
                        p.put("name", s.getName());
                        p.put("value", ((ptolemy.data.expr.Parameter) s)
                                .getExpression());
                        dParams.put(p);
                    }
                }
                if (dParams.length() > 0) d.put("parameters", dParams);
                directors.put(d);
            }
        }

        JSONObject data = new JSONObject();
        data.put("scope", parent.length() == 0
                ? scope.getName() : parent);
        data.put("entities", entities);
        data.put("directors", directors);
        data.put("count", entities.length());
        return AgentResult.ok(
                "model contains " + entities.length()
                        + " entit" + (entities.length() == 1 ? "y" : "ies")
                        + (directors.length() > 0
                                ? " and " + directors.length()
                                        + " director(s)"
                                : "")
                        + (parent.length() == 0 ? "" : " in " + parent),
                data);
    }
}
