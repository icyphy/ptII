/* An attribute that manages generation of Giotto code.

Copyright (c) 1998-2005 The Regents of the University of California.
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

@ProposedRating Red (hyzheng)
@AcceptedRating Red (hyzheng)
*/
package ptolemy.apps.charon;


// Ptolemy imports.
import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.lib.Assertion;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.Scheduler;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringUtilities;

// Java imports.
import java.awt.Frame;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;


//////////////////////////////////////////////////////////////////////////
//// CharonCodeGenerator

/**
   This attribute is a visible attribute that when configured (by double
   clicking on it or by invoking Configure in the context menu) it generates
   Charon code and displays it a text editor.  It is up to the user to save
   the Charon code in an appropriate file, if necessary.

   @author Haiyang Zheng
   @version $Id:
*/
public class CharonCodeGenerator extends Attribute {
    /** Construct a factory with the specified _container and name.
     *  @param _container The _container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the _container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the _container.
     */
    public CharonCodeGenerator(NamedObj _container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(_container, name);

        _attachText("_iconDescription",
            "<svg>\n"
            + "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
            + "style=\"fill:blue\"/>" + "<text x=\"-40\" y=\"-5\" "
            + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
            + "Double click to\ngenerate code.</text></svg>");
        new CharonEditorFactory(this, "_editorFactory");

        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate Charon code for the _container model.
     *  @return The Charon code.
     */
    public String generateCode() throws IllegalActionException {
        try {
            // initialization
            generatedCode = "";
            _modeCode = "";

            if (!_initialize()) {
                return "Can not generate code for this model!";
            }

            String containerName = _container.getName();

            // the top level agent is a composite agent too.
            generatedCode += (_compositeAgentCode(_container) + _modeCode);
        } catch (IllegalActionException ex) {
            System.out.println(ex.getMessage());
            throw new IllegalActionException(ex.getMessage());
        }

        //return generatedCode;
        return generatedCode;
    }

    ///////////////////////////////////////////////////////////////////
    ////        public variables and parameters                    ////

    /** The String of the generated code
     */
    public String generatedCode;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Topology analysis and initialization.
     *
     * @ return Ture if container is not null.
     */
    private boolean _initialize() throws IllegalActionException {
        _container = (TypedCompositeActor) getContainer();
        return (_container != null);
    }

    /** Topology analysis to get a list of agents.
     *  The agents should be TypedCompositeActor with parameter charonMode
     *  as null (by default) or true.
     *
     * @param actor Container to be analyzed to return agent list.
     * @return List of agents defined in model.
     */
    private LinkedList _agents(CompositeActor actor)
        throws IllegalActionException {
        LinkedList agentList = new LinkedList();
        ListIterator agentsIterator = actor.entityList(TypedCompositeActor.class)
                                                   .listIterator();

        while (agentsIterator.hasNext()) {
            TypedCompositeActor agent = (TypedCompositeActor) agentsIterator
                        .next();
            Parameter charonAgent = (Parameter) agent.getAttribute(
                    "charonAgent");

            if ((charonAgent == null)
                        || ((BooleanToken) charonAgent.getToken()).booleanValue()) {
                agentList.add(agent);
            }
        }

        return agentList;
    }

    // Topology analysis to get the list of assertions.
    private LinkedList _assertions(CompositeActor actor)
        throws IllegalActionException {
        LinkedList assertionList = new LinkedList();
        assertionList = (LinkedList) actor.entityList(Assertion.class);
        return assertionList;
    }

    /** Return a list of source ports connected to this port on the
     *  same layer that can send data to this port.  This includes
     *  output ports that are connected on the outside to this port,
     *  and input ports that are connected on the inside to this port.
     *
     *  @param input TypedIOPort
     *  @return A list of IOPort objects.
     */
    private LinkedList _shallowSourcePortList(TypedIOPort input) {
        try {
            _workspace.getReadAccess();

            Actor container = (Actor) input.getContainer();
            Director excDirector = ((Actor) container).getExecutiveDirector();
            int depthOfContainer = ((NamedObj) container).depthInHierarchy();
            LinkedList result = new LinkedList();
            Iterator ports = input.connectedPortList().iterator();

            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                int depth = port.depthInHierarchy();

                if (port.isInput() && (depth <= depthOfContainer)) {
                    result.addLast(port);
                } else if (port.isOutput() && (depth == (depthOfContainer + 1))) {
                    result.addLast(port);
                }
            }

            return result;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Generate code for the composite agent.
     *
     *  @param CompositeActor The composite agent generated code from.
     *  @return The composite agent code.
     */

    // we assume the composite agent has no mode
    // only atomic agent has mode
    private String _compositeAgentCode(CompositeActor actor)
        throws IllegalActionException {
        if (FSMDirector.class.isInstance(actor.getDirector())) {
            //            System.out.println("in FSM");
            return _agentCode(actor);
        }

        LinkedList subAgents = _agents(actor);

        if (subAgents.size() == 0) {
            return _agentCode(actor);
        }

        String compositeCodeString = "";
        String subAgentCode = "";
        String privateVariables = "";

        ListIterator subAgentsIterator = subAgents.listIterator();

        // the output ports of composite agent
        // In fact, there is always at most one output
        List outputPorts = actor.outputPortList();
        ListIterator outputPortsIterator = actor.outputPortList().listIterator();

        if (outputPorts.size() > 1) {
            throw new IllegalActionException(
                " The agent has more than one output!");
        }

        // get the source subAgent name
        String outputAgentName = "";
        String outputPortName = "";
        String sourceForOutputName = "";

        while (outputPortsIterator.hasNext()) {
            TypedIOPort output = (TypedIOPort) outputPortsIterator.next();
            outputPortName = output.getName();

            ListIterator sourcePorts = output.insidePortList().listIterator();

            TypedIOPort sourcePort = new TypedIOPort();

            while (sourcePorts.hasNext()) {
                TypedIOPort port = (TypedIOPort) sourcePorts.next();

                if (port.isOutput()) {
                    if (sourcePort == null) {
                        throw new IllegalActionException(
                            " The output has more than one source!");
                    } else {
                        sourcePort = port;
                        sourceForOutputName = sourcePort.getName();

                        Nameable sourceContainer = sourcePort.getContainer();
                        outputAgentName = sourceContainer.getName();
                    }
                }
            }
        }

        while (subAgentsIterator.hasNext()) {
            String subAgentConnectionInputs = "";
            String subAgentConnectionOutputs = "";

            CompositeActor subAgent = (CompositeActor) subAgentsIterator.next();

            if (outputAgentName.equals(subAgent.getName())) {
                // the inside output actually is input to outside environment
                subAgentConnectionOutputs += sourceForOutputName;
                subAgentConnectionInputs += outputPortName;
            }

            subAgentCode += ("  agent " + subAgent.getName().toLowerCase()
                    + " = " + subAgent.getName() + " ( ");

            if (actor.depthInHierarchy() == 0) {
                subAgentCode += _agentParameterTokens((NamedObj) subAgent);
            } else {
                subAgentCode += _agentParameters((NamedObj) subAgent, false);
            }

            subAgentCode += (" );" + _endLine);

            ListIterator subAgentInputs = subAgent.inputPortList().listIterator();

            while (subAgentInputs.hasNext()) {
                TypedIOPort input = (TypedIOPort) subAgentInputs.next();
                LinkedList sourceList = _shallowSourcePortList(input);
                ListIterator sources = sourceList.listIterator();
                boolean privateVariable = true;

                while (sources.hasNext()) {
                    TypedIOPort source = (TypedIOPort) sources.next();

                    if (source.depthInHierarchy() != input.depthInHierarchy()) {
                        privateVariable = false;
                    }

                    if (!(source.getName().equals(input.getName()))) {
                        if (subAgentConnectionOutputs == "") {
                            subAgentConnectionOutputs += source.getName();
                            subAgentConnectionInputs += input.getName();
                        } else {
                            subAgentConnectionOutputs += (", "
                                    + source.getName());
                            subAgentConnectionInputs += (", " + input.getName());
                        }
                    }
                }

                if (privateVariable) {
                    if (privateVariables == "") {
                        privateVariables += ("private analog real "
                                + input.getName());
                    } else {
                        privateVariables += (", " + input.getName());
                    }
                }
            }

            if (subAgentConnectionInputs.length() != 0) {
                subAgentCode += ("       [ " + subAgentConnectionInputs
                        + " := " + subAgentConnectionOutputs + " ] ;"
                        + _endLine);
            }

            compositeCodeString += _compositeAgentCode(subAgent);
        }

        compositeCodeString += "agent";

        String parameterString = "";
        String inputString = "";
        String outputString = "";
        String initString = "";
        String modeString = "";
        String modeParameterString = "";

        LinkedList parameterList = (LinkedList) actor.attributeList(Parameter.class);
        int parameterNumber = parameterList.size();
        ListIterator parameters = parameterList.listIterator();

        _inPorts = actor.inputPortList().iterator();

        while (_inPorts.hasNext()) {
            if (inputString == "") {
                inputString += ("read analog real "
                        + ((NamedObj) _inPorts.next()).getName());
            } else {
                inputString += (", " + ((NamedObj) _inPorts.next()).getName());
            }
        }

        if (inputString != "") {
            inputString += ";";
        }

        _outPorts = actor.outputPortList().iterator();

        if (_outPorts.hasNext()) {
            String outportName = ((NamedObj) _outPorts.next()).getName();

            if (outputString == "") {
                outputString += ("write analog real " + outportName);
            } else {
                outputString += (", " + outportName);
            }
        }

        if (outputString != "") {
            outputString += ";";
        }

        if (privateVariables.length() != 0) {
            privateVariables += ";";
        }

        compositeCodeString += (" " + actor.getName() + " ( ");

        if (actor.depthInHierarchy() != 0) {
            compositeCodeString += _agentParameters((NamedObj) actor, true);
        }

        compositeCodeString += (" )" + _endLine + "{" + _endLine + "  "
                + outputString + _endLine + "  " + inputString + _endLine
                + "  " + privateVariables + _endLine + subAgentCode + _endLine
                + "}" + _endLine);

        return compositeCodeString;
    }

    /** Generate string of parameters of the agent.
     *
     *  @param agent whether the parameters belong to.
     *  @param typed indicates whether the parameters have type.
     *  @return string of parameters.
     */
    private String _agentParameters(NamedObj agent, boolean typed) {
        LinkedList parameterList = (LinkedList) agent.attributeList(Parameter.class);
        ListIterator parameters = parameterList.listIterator();

        String prefix = "";

        if (typed) {
            prefix = "real ";
        }

        String parameterString = "";

        while (parameters.hasNext()) {
            String parameterName = ((NamedObj) parameters.next()).getName();

            if (parameterName.startsWith("_")) {
                continue;
            }

            if (parameterString == "") {
                parameterString += (prefix + parameterName);
            } else {
                parameterString += (", " + prefix + parameterName);
            }
        }

        return parameterString;
    }

    /** Generate string of evaluated tokens of parameters of the agent.
     *
     *  @param agent whether the parameters belong to.
     *  @return string of evaluated parameters.
     */
    private String _agentParameterTokens(NamedObj agent)
        throws IllegalActionException {
        LinkedList parameterList = (LinkedList) agent.attributeList(Parameter.class);
        ListIterator parameters = parameterList.listIterator();

        String tokenString = "";

        while (parameters.hasNext()) {
            Parameter parameter = (Parameter) parameters.next();
            String parameterName = parameter.getName();

            if (parameterName.startsWith("_")) {
                continue;
            }

            String tokenValue = parameter.getToken().toString();

            if (tokenString == "") {
                tokenString += tokenValue;
            } else {
                tokenString += (", " + tokenValue);
            }
        }

        return tokenString;
    }

    /** Generate code for the agent.
     *
     *  @param CompositeActor The agent generated code from.
     *  @return The agent code.
     */
    private String _agentCode(CompositeActor actor)
        throws IllegalActionException {
        //    System.out.println("dealing with " + actor.getFullName());
        String codeString = "agent";
        String parameterString = "";
        String inputString = "";
        String outputString = "";
        String initString = "";
        String modeString = "";
        String modeParameterString = "";
        String typedModeParameterString = "";
        String flowString = "";
        String invariantString = "";

        LinkedList parameterList = (LinkedList) actor.attributeList(Parameter.class);

        /*        Parameter invariantPara = (Parameter) actor.getAttribute("_invariant");
                  if (invariantPara  != null) {
                  invariantString = "inv { "
                  + ((StringToken)invariantPara.getToken()).stringValue()
                  + " } ";
                  //get rid of _invariant parameter
                  parameterList.remove(invariantPara);;
                  //FIXME: it seems that after getAttribute,
                  //the attribute does not exist?
                  }
        */
        ListIterator assertions = _assertions(actor).listIterator();

        while (assertions.hasNext()) {
            Assertion assertion = (Assertion) assertions.next();

            if (invariantString.length() == 0) {
                invariantString = "inv { "
                    + assertion.assertion.getExpression();
            } else {
                invariantString += (" ; " + assertion.assertion.getExpression());
            }

            invariantString += " } ";
        }

        int parameterNumber = parameterList.size();
        ListIterator parameters = parameterList.listIterator();

        _inPorts = actor.inputPortList().iterator();

        while (_inPorts.hasNext()) {
            if (inputString == "") {
                inputString += ("read analog real "
                        + ((NamedObj) _inPorts.next()).getName());
            } else {
                inputString += (", " + ((NamedObj) _inPorts.next()).getName());
            }
        }

        if (inputString != "") {
            inputString += ";";
        }

        _outPorts = actor.outputPortList().iterator();

        int outportNumber = actor.outputPortList().size();

        // since the parameters are either for mode or for outport,
        // we assume the number of the parameters for outport is the same
        // with the number of outport(s)
        boolean parameterForOutport = false;

        while (parameters.hasNext()) {
            String parameterName = ((NamedObj) parameters.next()).getName();

            if (parameterName.startsWith("_")) {
                continue;
            }

            if (parameters.nextIndex() > (parameterNumber - outportNumber)) {
                parameterForOutport = true;
            }

            if (parameterString == "") {
                parameterString += ("real " + parameterName);
            } else {
                parameterString += (", real " + parameterName);
            }

            if (parameterForOutport) {
                if (_outPorts.hasNext()) {
                    String outportName = ((NamedObj) _outPorts.next()).getName();

                    if (outputString == "") {
                        outputString += ("write analog real " + outportName);
                    } else {
                        outputString += (", " + outportName);
                    }

                    initString += (outportName + " = " + parameterName + " ;");
                }
            } else {
                if (modeParameterString == "") {
                    modeParameterString += parameterName;
                    typedModeParameterString += ("real " + parameterName);
                } else {
                    modeParameterString += (", " + parameterName);
                    typedModeParameterString += (", real " + parameterName);
                }
            }
        }

        if (outputString != "") {
            outputString += ";";
        }

        initString = "init { " + initString + " }";

        modeString = "mode top = " + actor.getName() + "TopMode" + " ( "
            + modeParameterString + " ) ;";

        codeString += (" " + actor.getName() + " ( " + parameterString + " )"
                + _endLine + "{" + _endLine + "  " + outputString + _endLine
                + "  " + inputString + _endLine + "  " + initString + _endLine
                + "  " + modeString + _endLine + "}" + _endLine);

        if (FSMDirector.class.isInstance(actor.getDirector())) {
            // mode code generation goes here.
            _modeCode += ("mode " + actor.getName() + "TopMode" + " ( "
                    + typedModeParameterString + " )" + _endLine + "{"
                    + _endLine + "  " + outputString + _endLine + "  "
                    + inputString + _endLine);

            // notice the _fsmModeCode(actor) will modify the _modeCode with transitions code
            // and return the mode code for each sub mode.(refinement)
            String subModeString = _fsmModeCode(((FSMDirector) actor
                            .getDirector()).getController(), inputString,
                    outputString);
            _modeCode += ("  " + invariantString + _endLine + "}" + _endLine
                    + subModeString);
        } else {
            flowString = _graphToText(actor);
            _modeCode += ("mode " + actor.getName() + "TopMode" + " ( "
                    + typedModeParameterString + " )" + _endLine + "{"
                    + _endLine + "  " + outputString + _endLine + "  "
                    + inputString + _endLine + "  " + flowString + _endLine
                    + "  " + invariantString + _endLine + "}" + _endLine);
        }

        return codeString;
    }

    /** Generate mode code for the FSM. Notice the refinements share
     *  same inputs and outputs. Here, the mode, state and refinement have same meaning.
     *
     *  @param fsm The FSM generated mode code from.
     *  @param inputPorts The inputs of fsm model.
     *  @param outputPorts The outputs of fsm model.
     *  @return The fsm submode code.
     */
    private String _fsmModeCode(FSMActor fsm, String inputPorts,
        String outputPorts) throws IllegalActionException {
        String subModeCode = "";
        String transitionString = "";

        transitionString += ("  trans from default to "
                + fsm.getInitialState().getName() + _endLine
                + "  when ( true ) do { } " + _endLine);

        ListIterator stateIterator = fsm.entityList().listIterator();

        while (stateIterator.hasNext()) {
            State st = (State) stateIterator.next();

            if (st.getRefinement() != null) {
                Actor[] refinements = st.getRefinement();
                CompositeActor refinement = (CompositeActor) refinements[0];
                String stParameters = _agentParameters(refinement, false);
                String typedStParameters = _agentParameters(refinement, true);
                String flowString = "";
                String invariantString = "";

                _modeCode += ("  mode " + st.getName() + " = " + st.getName()
                        + "Mode" + " ( " + stParameters + " );" + _endLine);

                flowString = _graphToText(refinement);

                ListIterator assertions = _assertions(refinement).listIterator();

                while (assertions.hasNext()) {
                    Assertion assertion = (Assertion) assertions.next();

                    if (invariantString.length() == 0) {
                        invariantString = "inv { "
                            + assertion.assertion.getExpression();
                    } else {
                        invariantString += (" ; "
                                + assertion.assertion.getExpression());
                    }

                    invariantString += " } ";
                }

                subModeCode += ("mode " + st.getName() + "Mode " + "( "
                        + typedStParameters + " )" + _endLine + "{" + _endLine
                        + "  " + outputPorts + _endLine + "  " + inputPorts
                        + _endLine + "  " + flowString + _endLine + "  "
                        + invariantString + _endLine + "}" + _endLine);

                LinkedList transitionList = new LinkedList();
                transitionList.addAll(st.preemptiveTransitionList());
                transitionList.addAll(st.nonpreemptiveTransitionList());

                ListIterator transitionItr = transitionList.listIterator();

                while (transitionItr.hasNext()) {
                    Transition tr = (Transition) transitionItr.next();
                    State newState = tr.destinationState();
                    String guardString = tr.getGuardExpression();
                    transitionString += ("  trans from " + st.getName()
                            + " to " + newState.getName() + _endLine
                            + "  when ( " + guardString + ") " + _endLine
                            + "  do {}" + _endLine);
                }
            }
        }

        _modeCode += (transitionString + _endLine);

        return subModeCode;
    }

    /** Transform the graphic block diagram to text expression.
     *  Assume container only contains atomic actors.
     *
     *  @param container contains actors.
     *  @return txtExpression of graphic block diagram.
     */
    private String _graphToText(CompositeActor container)
        throws IllegalActionException {
        // It is not trivial to transform graph to text.
        // Here, we assume there is only one Integrator and one expression actor.
        String txtString = "";
        LinkedList actors = new LinkedList(container.entityList());
        ListIterator actorIterator = actors.listIterator();

        // we begin with Integrator.
        AtomicActor beginActor = new AtomicActor();

        while (actorIterator.hasNext()) {
            Actor actor = (Actor) actorIterator.next();

            if (Integrator.class.isInstance(actor)) {
                beginActor = (AtomicActor) actor;
                break;
            }
        }

        if (beginActor == null) {
            throw new IllegalActionException("Integrator is needed!");
        } else {
            // we trace the output of the Integrator
            // we assume the output of the integrator is connectted
            // to the container output directly and they have same names
            // for simplicity at this time.
            // FIXME: we really need to reconsider the methods of ports.
            List outputs = beginActor.outputPortList();
            ListIterator outputIterator = outputs.listIterator();
            String outputName = "";

            if (outputs.size() != 1) {
                throw new IllegalActionException(
                    "Integrator only have one output!  " + outputs.size());
            } else {
                TypedIOPort output = (TypedIOPort) outputIterator.next();
                ListIterator sinkIterator = output.connectedPortList()
                                                          .listIterator();

                while (sinkIterator.hasNext()) {
                    TypedIOPort sink = (TypedIOPort) sinkIterator.next();

                    if (sink.isOutput()) {
                        //FIXME: we need to consider depth in hierarchy
                        //to avoid two outputs connected to same output
                        //of composite actor
                        outputName = sink.getName();
                    }
                }

                txtString += ("diff { d(" + outputName + ") == ");
            }

            // we trace the input of the integrator
            List inputs = beginActor.inputPortList();
            ListIterator inputIterator = inputs.listIterator();

            if (inputs.size() != 1) {
                throw new IllegalActionException(
                    "Integrator only have one input!");
            } else {
                TypedIOPort input = (TypedIOPort) inputIterator.next();
                List sources = input.connectedPortList();

                if (sources.size() != 1) {
                    throw new IllegalActionException(
                        "There is only one connection to the input!");
                } else {
                    TypedIOPort source = (TypedIOPort) sources.get(0);

                    // if there is just an integrator
                    if (source.isInput()) {
                        txtString += (source.getName() + " ; }" + _endLine);
                    }
                    // if there is some expression actor
                    else {
                        AtomicActor expressionActor = (AtomicActor) source
                                    .getContainer();

                        if (Expression.class.isInstance(expressionActor)) {
                            Parameter expPara = (Parameter) expressionActor
                                        .getAttribute("expression");
                            txtString += (expPara.getExpression() + " ; } "
                                    + _endLine);
                        } else {
                            throw new IllegalActionException(
                                "This should be Expression Atomic Actor!");
                        }
                    }
                }
            }
        }

        return txtString;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private FSMActor _modeSwitchController;
    private TypedCompositeActor _container;
    private String _endLine = "\n";
    private Iterator _inPorts;
    private Iterator _outPorts;
    private LinkedList _agents;
    private String _modeCode = "";

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    private class CharonEditorFactory extends EditorFactory {
        public CharonEditorFactory(NamedObj _container, String name)
            throws IllegalActionException, NameDuplicationException {
            super(_container, name);
        }

        /** Create an editor for configuring the specified object with the
         *  specified parent window.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        public void createEditor(NamedObj object, Frame parent) {
            try {
                Configuration configuration = ((TableauFrame) parent)
                            .getConfiguration();

                NamedObj _container = (NamedObj) object.getContainer();
                TextEffigy codeEffigy = TextEffigy.newTextEffigy(configuration
                                .getDirectory(), generateCode());
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                    "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
