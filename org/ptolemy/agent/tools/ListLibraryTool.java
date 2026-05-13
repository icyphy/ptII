/* Tool that exposes a curated subset of the actor library to the LLM.

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
import org.ptolemy.agent.library.LibraryIndex;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// ListLibraryTool

/**
 Return a curated, paged list of actors that the agent may instantiate.
 The catalog comes from {@link LibraryIndex}, which today reads the
 same XML library files Vergil uses, so the LLM never invents class
 names that don't exist.

 <p>Args:
 <ul>
 <li>{@code query} -- optional substring filter against actor class
     name or short description; case-insensitive.</li>
 <li>{@code limit} -- optional integer (default 20).</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class ListLibraryTool implements AgentTool {

    @Override
    public String name() {
        return "list_library";
    }

    @Override
    public String description() {
        return "List Ptolemy II actors available for instantiation."
                + " Use this BEFORE add_entity to discover the exact"
                + " className you should pass. For complex actors, call"
                + " describe_actor with the className returned here.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("query", new JSONObject().put("type", "string")
                .put("description",
                        "Optional case-insensitive substring filter."));
        props.put("limit", new JSONObject().put("type", "integer")
                .put("description", "Maximum entries to return (default 20).")
                .put("default", 20));
        props.put("offset", new JSONObject().put("type", "integer")
                .put("description", "Number of matching entries to skip.")
                .put("default", 0));
        props.put("category", new JSONObject().put("type", "string")
                .put("description",
                        "Optional category substring, e.g. \"Math\"."));
        props.put("summary", new JSONObject().put("type", "boolean")
                .put("description",
                        "When true, omit parameter details for brevity.")
                .put("default", true));

        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        String query = args.optString("query", "").trim().toLowerCase();
        int limit = args.optInt("limit", 20);
        int offset = args.optInt("offset", 0);
        String category = args.optString("category", "").trim();
        boolean summary = args.optBoolean("summary", true);
        JSONArray entries = LibraryIndex.shared().search(query, category,
                offset, limit, summary);
        JSONObject data = new JSONObject();
        data.put("entries", entries);
        data.put("totalReturned", entries.length());
        data.put("offset", offset);
        data.put("limit", limit);
        data.put("library", LibraryIndex.shared().status());
        return AgentResult.ok(
                "found " + entries.length() + " matching actor(s)", data);
    }
}
