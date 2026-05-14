/* Tool that returns the full catalog entry for one Ptolemy component.

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

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.library.LibraryIndex;
import org.ptolemy.agent.library.LibraryIndex.Entry;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// DescribeActorTool

/**
 Return the full scanned catalog entry for one actor or director.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class DescribeActorTool implements AgentTool {

    @Override
    public String name() {
        return "describe_actor";
    }

    @Override
    public String description() {
        return "Return full port and parameter metadata for one actor or"
                + " director by className or displayName.";
    }

    @Override
    public JSONObject parametersSchema() {
        JSONObject props = new JSONObject();
        props.put("className", new JSONObject().put("type", "string")
                .put("description",
                        "Fully qualified className returned by list_library,"
                                + " or a displayName such as \"Ramp\"."));
        JSONObject schema = new JSONObject();
        schema.put("type", "object");
        schema.put("properties", props);
        schema.put("required", new JSONArray().put("className"));
        return schema;
    }

    @Override
    public AgentResult execute(PtolemySession session, JSONObject args) {
        if (!args.has("className")) {
            return AgentResult.fail("describe_actor requires 'className'");
        }
        String className = args.getString("className").trim();
        Entry entry = LibraryIndex.shared().find(className);
        if (entry == null) {
            return AgentResult.fail("actor not found in library: "
                    + className);
        }
        JSONObject data = entry.toJson();
        JSONObject guide = _modelingGuide(entry);
        if (guide.length() > 0) {
            data.put("modelingGuide", guide);
        }
        return AgentResult.ok("described " + entry.className,
                data);
    }

    /** Return practical modeling guidance that is not present in the
     *  scanned library metadata. The LLM needs more than a port list:
     *  it needs to know which actor is canonical for a pattern, what
     *  each port means, and which director domain is expected. */
    private JSONObject _modelingGuide(Entry entry) {
        String c = entry.className.toLowerCase(Locale.US);
        String d = entry.displayName.toLowerCase(Locale.US);
        JSONObject g = new JSONObject();

        if (c.endsWith(".addsubtract") || d.equals("addsubtract")) {
            _put(g, "bestUse", "Sum and subtract several signals.");
            _put(g, "portSemantics",
                    "plus is a multiport input whose tokens are added; "
                            + "minus is a multiport input whose tokens are "
                            + "subtracted; output is plusSum - minusSum.");
            _putArray(g, "patterns",
                    "error = reference - measurement: connect reference to plus and measurement to minus",
                    "fan-in sum: connect every summand to plus",
                    "feedback loop: connect state/output feedback to minus");
            _putArray(g, "avoid",
                    "Do not use separate Scale(-1)+AddSubtract when the minus port expresses the intent directly",
                    "Do not connect an output to AddSubtract.input; the real input ports are plus and minus");
        } else if (c.endsWith(".multiplydivide")
                || d.equals("multiplydivide")) {
            _put(g, "bestUse", "Multiply and divide several signals.");
            _put(g, "portSemantics",
                    "multiply is a multiport input whose tokens are "
                            + "multiplied; divide is a multiport input whose "
                            + "tokens divide the product; output is product "
                            + "of multiply inputs divided by product of "
                            + "divide inputs.");
            _putArray(g, "patterns",
                    "gain scheduling or gating: signal -> multiply and gate -> multiply",
                    "normalization: numerator -> multiply and denominator -> divide");
        } else if (c.endsWith(".scale") || d.equals("scale")) {
            _put(g, "bestUse",
                    "Apply one fixed scalar gain to one incoming signal.");
            _put(g, "portSemantics",
                    "input is the signal; output is factor * input.");
            _put(g, "keyParameters", "factor is the constant gain.");
            _putArray(g, "chooseInstead",
                    "Use Expression when the operation is a richer formula such as a*x+b or x*x",
                    "Use AddSubtract for summing several signals");
        } else if (c.endsWith(".expression") || d.equals("expression")) {
            _put(g, "bestUse",
                    "Compact scalar formulas into one actor when a simple "
                            + "dedicated actor does not exist.");
            _put(g, "portSemantics",
                    "The actor may expose input ports referenced by name in "
                            + "the expression; output emits the expression "
                            + "value.");
            _putArray(g, "patterns",
                    "Use for affine or nonlinear formulas that would otherwise require many tiny actors",
                    "For typed numeric formulas, cast ambiguous inputs, e.g. input::double");
            _putArray(g, "avoid",
                    "Do not use Expression for a plain constant; use Const",
                    "Do not use Expression when a clearer domain actor exists, e.g. Integrator for continuous integration");
        } else if (c.endsWith(".integrator") || d.equals("integrator")) {
            _put(g, "bestUse",
                    "Continuous-time state integration for ODE-style models.");
            _put(g, "director",
                    "Requires ContinuousDirector for physical continuous-time models.");
            _put(g, "portSemantics",
                    "input is dx/dt; output is the integrated state x.");
            _put(g, "keyParameters",
                    "initialState sets the initial output/state.");
            _putArray(g, "patterns",
                    "First-order RC: error -> Scale(1/RC) -> Integrator; feed Integrator.output back to AddSubtract.minus",
                    "Plant/state model: derivative signal -> Integrator -> state output");
            _putArray(g, "avoid",
                    "Do not emulate continuous integration with Scale chains in SDF",
                    "Do not use for sample accumulation unless the model is truly continuous-time");
        } else if (c.endsWith(".derivative") || d.equals("derivative")) {
            _put(g, "bestUse",
                    "Continuous-time derivative of a signal, commonly in controllers.");
            _put(g, "director",
                    "Use with ContinuousDirector.");
            _put(g, "portSemantics",
                    "input is x; output approximates dx/dt.");
            _putArray(g, "patterns",
                    "PID D branch: error -> Derivative -> Scale(Kd) -> sum.plus");
        } else if (c.endsWith(".const") || d.equals("const")) {
            _put(g, "bestUse",
                    "Provide a fixed scalar, vector, or expression-valued source.");
            _put(g, "portSemantics", "output emits the value parameter.");
            _put(g, "keyParameters", "value is the token emitted.");
            _putArray(g, "patterns",
                    "Use for setpoints, gains-as-signals, initial test inputs");
        } else if (c.endsWith(".ramp") || d.equals("ramp")) {
            _put(g, "bestUse",
                    "Generate a deterministic arithmetic sequence.");
            _put(g, "portSemantics", "output emits init + step*k.");
            _put(g, "keyParameters",
                    "init is the first value; step is the increment; firingCountLimit can bound emissions.");
            _putArray(g, "patterns",
                    "Use for time/sample-index-like test signals in SDF");
        } else if (c.endsWith(".sinewave") || d.equals("sinewave")) {
            _put(g, "bestUse",
                    "Generate a sinusoidal test signal without writing an expression.");
            _put(g, "portSemantics", "output emits the sine waveform.");
            _putArray(g, "patterns",
                    "Use as an input source for filters and signal-processing demos");
        } else if (c.endsWith(".recorder") || d.equals("recorder")) {
            _put(g, "bestUse",
                    "Capture simulation output for the frontend Signals panel.");
            _put(g, "portSemantics",
                    "input records every arriving token; no output.");
            _putArray(g, "patterns",
                    "Keep Recorders at top level so results are easy to inspect",
                    "Connect one Recorder to each observable final output");
            _putArray(g, "avoid",
                    "Do not hide the only Recorder inside a composite",
                    "Do not use GUI Display/Plotter actors in headless agent models");
        } else if (c.contains("continuousdirector")) {
            _put(g, "bestUse",
                    "Director for continuous-time ODE and physical dynamics.");
            _put(g, "keyParameters",
                    "stopTime controls simulation duration; maxStepSize controls integration granularity.");
            _putArray(g, "patterns",
                    "Use for RC filters, PID with continuous plant, mass-spring, state-space ODEs");
        } else if (c.contains("sdfdirector")) {
            _put(g, "bestUse",
                    "Director for synchronous token-flow pipelines.");
            _put(g, "keyParameters",
                    "iterations controls how many firings/samples to execute.");
            _putArray(g, "patterns",
                    "Use for sample-by-sample arithmetic pipelines, simple LSTM/RNN cell demos, discrete signal processing");
        } else if (c.contains("dedirector")) {
            _put(g, "bestUse",
                    "Director for timestamped discrete events.");
            _put(g, "keyParameters", "stopTime controls the event horizon.");
            _putArray(g, "patterns",
                    "Use for queues, event scheduling, clocks, and asynchronous event models");
        } else if (c.endsWith(".sigmoid") || d.equals("sigmoid")) {
            _put(g, "bestUse",
                    "Neural-network gate activation: output = 1/(1+exp(-input)).");
            _put(g, "portSemantics", "input is preactivation; output is gate value.");
            _putArray(g, "patterns",
                    "LSTM forget/input/output gates after weighted sum");
        } else if (c.endsWith(".trigfunction") || d.equals("trigfunction")) {
            _put(g, "bestUse",
                    "Apply a named trigonometric function such as tanh, sin, cos.");
            _put(g, "keyParameters",
                    "function selects the operation; use tanh for LSTM candidate/state squashing.");
            _put(g, "portSemantics", "input is the argument; output is function(input).");
        }

        if (g.length() > 0) {
            _put(g, "selectionRule",
                    "Choose this actor only when the requested behavior "
                            + "matches bestUse and the planned connections "
                            + "match portSemantics.");
        }
        return g;
    }

    private void _put(JSONObject json, String key, String value) {
        json.put(key, value);
    }

    private void _putArray(JSONObject json, String key, String... values) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < values.length; i++) {
            array.put(values[i]);
        }
        json.put(key, array);
    }
}
