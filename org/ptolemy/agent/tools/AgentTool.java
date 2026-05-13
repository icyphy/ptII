/* The interface every agent tool implements.

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

import org.json.JSONObject;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// AgentTool

/**
 The contract every agent-callable tool implements. A tool is a small
 deterministic operation on a {@link PtolemySession} which:

 <ul>
 <li>advertises itself with a stable, snake_case name,</li>
 <li>publishes a JSON Schema describing its arguments (so OpenAI-style
     function calling can validate calls before they hit Java), and</li>
 <li>executes the operation, returning a uniform
     {@link AgentResult}.</li>
 </ul>

 <p>Implementations are expected to be stateless and thread-safe; one
 instance is shared across every session.

 <p>Mutating tools should always go through
 {@link PtolemySession#applyChange(String)} so that:
 <ul>
 <li>the change is undoable (Ptolemy native undo stack),</li>
 <li>every {@code ChangeListener} attached to the model is notified
     (this is what the frontend will hook into in M3),</li>
 <li>type and topology checks are performed by Ptolemy itself, not
     by hand-written validation here.</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public interface AgentTool {

    /** @return The unique snake_case name, e.g. {@code "add_entity"}. */
    String name();

    /** @return One-line description suitable for the LLM prompt. */
    String description();

    /** @return JSON Schema for the tool arguments. The shape is the
     *      OpenAI function-calling subset: a JSON object with
     *      "type":"object", "properties":{...}, and "required":[...]. */
    JSONObject parametersSchema();

    /** Execute the tool against the given session.
     *  @param session The session to mutate or inspect.
     *  @param args Arguments parsed from a tool call. May be empty
     *      but never null.
     *  @return Uniform result describing success or failure.
     */
    AgentResult execute(PtolemySession session, JSONObject args);
}
