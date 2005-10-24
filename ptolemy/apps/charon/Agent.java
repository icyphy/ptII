/* An agent represents a general composite actor or a refinement in a modal model.

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

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Assertion;
import ptolemy.actor.lib.Expression;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTEmbeddedDirector;
import ptolemy.domains.ct.kernel.CTMixedSignalDirector;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.domains.fsm.modal.ModalModel;
import ptolemy.domains.fsm.modal.ModalPort;
import ptolemy.domains.fsm.modal.Refinement;
import ptolemy.domains.fsm.modal.RefinementPort;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;


public class Agent {
    /** Constructor for Agent with the content of agent
     *  The constructor parses the content of the agent,
     *  initializes agent name and agent parameters.
     *  @param content The content of agent.
     *  @exception IllegalActionException If NameDuplicationException is thrown inside.
     */
    public Agent(String content) throws IllegalActionException {
        //    System.out.println("Agent content: " + content);
        // save the string content of the agent
        _content = content;

        // global variables
        String token;
        StringTokenizer tokenST;

        // StringTokenizer for agent content
        _contentST = new StringTokenizer(content, "()");

        // get the agent name
        token = _contentST.nextToken();

        int nameIndex = token.trim().indexOf(" ");
        _name = token.substring(nameIndex).trim();

        // get the agent parameters
        token = _contentST.nextToken();

        // notice there may be no parameters, e.g. topLevel
        tokenST = new StringTokenizer(token, " ,\n\t\r");

        while (tokenST.hasMoreTokens()) {
            String tokenToken = tokenST.nextToken().trim();

            if (!tokenToken.equals("real")) {
                //  System.out.println("parameter: " + tokenToken);
                _parameters.add(tokenToken);
            }
        }

        // beginning of parsing
        while (_contentST.hasMoreTokens()) {
            // the stringTokenizer get token line by line
            // FIXME: this needs very strictly formated Charon code.
            token = _contentST.nextToken("\n");

            //System.out.println("    Token is: " + token);
            // when the token of the output ports is read
            if (token.trim().startsWith("write")) {
                tokenST = new StringTokenizer(token, " ,\n\t\r;");
                tokenST.nextToken(); // ignore write
                tokenST.nextToken(); // ignore analog

                while (tokenST.hasMoreTokens()) {
                    String tokenToken = tokenST.nextToken().trim();

                    if (!tokenToken.equals("real")) {
                        _outputs.add(tokenToken);
                    }
                }

                // when the token of the input ports is read
            } else if (token.trim().startsWith("read")) {
                tokenST = new StringTokenizer(token, " ,\n\t\r;");
                tokenST.nextToken(); // ignore read
                tokenST.nextToken(); // ignore analog

                while (tokenST.hasMoreTokens()) {
                    String tokenToken = tokenST.nextToken().trim();

                    if (!tokenToken.equals("real")) {
                        _inputs.add(tokenToken);
                    }
                }

                // when the token of the private variables is read
            } else if (token.trim().startsWith("private")) {
                tokenST = new StringTokenizer(token, " ,\n\t\r;");
                tokenST.nextToken(); // ignore private
                tokenST.nextToken(); // ignore analog

                while (tokenST.hasMoreTokens()) {
                    String tokenToken = tokenST.nextToken().trim();

                    if (!tokenToken.equals("real")) {
                        _privateVariables.add(tokenToken);
                    }
                }

                // when the token of the initial states is read
            } else if (token.trim().startsWith("init")) {
                // this token use "{};" as delimiter
                tokenST = new StringTokenizer(token, "{};");

                // get rid of "init"
                String tokenToken = tokenST.nextToken();

                // get initial variable expression
                tokenToken = tokenST.nextToken();
                _initialState = tokenToken.substring(tokenToken.indexOf("=")
                        + 1);

                // when the token of a sub agent is read
            } else if (token.trim().startsWith("agent")) {
                tokenST = new StringTokenizer(token);
                tokenST.nextToken("=");
                _subAgents.add(new Agent("agent " + tokenST.nextToken()));

                // when the token of the mode is read
            } else if (token.trim().startsWith("mode")) {
                tokenST = new StringTokenizer(token);
                tokenST.nextToken("=");
                tokenST.nextToken(" "); // ignore the "='
                _modeName = tokenST.nextToken("(").trim();

                //System.out.println("**************** the mode name is: " + _modeName);
                // when the token of the connection conditions is read
            } else if (token.trim().startsWith("[")) {
                //            System.out.println("Connection: " + token);
                int seperator = token.indexOf(":=");
                String inputs = token.substring(1, seperator - 1).trim();
                String outputs = token.substring(seperator + 2).trim();

                //            System.out.println("    Stirng pair is: " + inputs + " : " + outputs );
                StringTokenizer inputST = new StringTokenizer(inputs,
                        " \n\r\t,[]");
                StringTokenizer outputST = new StringTokenizer(outputs,
                        " \n\r\t,[]");

                while (inputST.hasMoreTokens()) {
                    String input = inputST.nextToken();
                    String output = outputST.nextToken();

                    //              System.out.println("    pair is: " + input + " . " + output );
                    // the connections list is composed of connections
                    // each connection is a pair, with
                    // the input port at left and output port at right.
                    _connections.add(new Pair(input, output));
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the content of the agent.
     */
    public String getContent() {
        return _content;
    }

    /** Return the name of the agent.
     */
    public String getName() {
        return _name;
    }

    /** Return the name of the agent.
     */
    public String getModeName() {
        return _modeName;
    }

    /** Set the name of the agent.
     *  @param name The new name for the agent.
     */
    public void setName(String name) {
        _name = name;
    }

    /** Get the list of the parameters of the agent.
     */
    public LinkedList getParameters() {
        return _parameters;
    }

    /** Get the list of the subAgents of the agent.
     */
    public LinkedList getSubAgents() {
        return _subAgents;
    }

    /** Get the list of the inputs of the agent.
     */
    public LinkedList getInputs() {
        return _inputs;
    }

    /** Get the list of the outputs of the agent.
     */
    public LinkedList getOutputs() {
        return _outputs;
    }

    /** Constructor for agent to build Ptolemy TypedCompositeActor.
     *  The returned composite actor is the top level of the Ptolemy model.
     *  This constructor constructs a top level workspace and
     *  a top level container with the name of the agent.
     *  @param workspace The workspace for the TypedCompositeActor.
     *  @exception IllegalActionException When a NameDuplicationException or
     *  an IllegalActionException is thrown inside.
     */
    public TypedCompositeActor constructor(Workspace workspace)
        throws IllegalActionException {
        _workspace = workspace;

        // construct a top level container
        _container = new TypedCompositeActor(_workspace);

        // indicates this composite actor is top level.
        _topLevel = true;

        try {
            _container.setName(getName());
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(e.getMessage());
        }

        // return the constructed top level composite actor.
        return _constructor();
    }

    /** Constructor for agent to build Ptolemy TypedCompositeActor.
     *  The returned composite actor is a refinement of modal model of the Ptolemy model.
     *  This constructor does not construct a composite actor since the refinement is already the one.
     *  @param refinement The refinement is just that will be returned TypedCompositeActor.
     *  @exception IllegalActionException When an IllegalActionException is thrown.
     */
    public TypedCompositeActor constructor(Refinement refinement)
        throws IllegalActionException {
        // the container is the refinement
        _container = refinement;

        // it is not the top level
        _topLevel = false;

        // return the constructed composite actor
        return _constructor();
    }

    /** Constructor for agent to build Ptolemy TypedCompositeActor.
     *  The returned composite actor is a composite actor of the Ptolemy model.
     *  This constructor constructs a composite actor inside the given container.
     *  @param container The container is just that will be returned TypedCompositeActor.
     *  @exception IllegalActionException When an IllegalActionException is thrown.
     */
    public TypedCompositeActor constructor(TypedCompositeActor container)
        throws IllegalActionException {
        // the container is the given container
        _container = container;

        // it is not the top level
        _topLevel = false;

        // return the constructed composite actor
        return _constructor();
    }

    /** Associate the mode from modesList to the agent.
     *  The mode is actually what the agent does, and it is usually a ODE.
     *  Usually it is composed of one or more expression actors and integrators.
     *  New modes may be discovered if the agent is actually a finite state machine.
     *  @param modeString The modeString is what the agent does.
     *  @exception IllegalActionException When an IllegalActionException is thrown inside.
     */
    public void addMode(String modeString) throws IllegalActionException {
        StringTokenizer modeST = new StringTokenizer(modeString, "\n");

        while (modeST.hasMoreTokens()) {
            String token = modeST.nextToken().trim();

            //System.out.println("   Mode String Token is: " + token);
            // The token begins with "diff" is an ODE.
            if (token.startsWith("diff")) {
                // get the output variable of the integrator (or the ODE)
                int startOfIntegratorOutput = token.indexOf("d(") + 2;
                int endOfIntegratorOutput = token.indexOf(")");
                _integratorOutput = token.substring(startOfIntegratorOutput,
                        endOfIntegratorOutput);

                //        System.out.println("      mode token is: " + token);
                // get the expression of the ODE
                int startOfExpression = token.indexOf("==") + 2;
                int endOfExpression = token.indexOf(";");
                _expression = token.substring(startOfExpression, endOfExpression);

                // Here, we only check if the expression contains the output of the integrator(s)
                // if it does, add an input port with the same name with the output port into the
                // _expressionInputs list
                // Note we assume that all the inputs to the agent will be used by the expression
                // actor. Actually, it does not hurt if Accidiently several inputs are not used.
                if (_expression.indexOf(_integratorOutput) != -1) {
                    _expressionInputs.add(_integratorOutput);
                }

                // when sub mode is read
            } else if (token.trim().startsWith("mode")) {
                if (token.indexOf("=") == -1) {
                    continue;
                }

                StringTokenizer tokenST = new StringTokenizer(token);

                System.out.println(" ^^^^^^^^^^^^^ " + token + " -------- "
                    + tokenST.nextToken("="));
                _subModes.add(new Agent("mode " + tokenST.nextToken()));

                // contruct assertion actors
            } else if (token.startsWith("inv")) {
                // get the expression of the ODE
                int startOfExpression = token.indexOf("{") + 1;
                int endOfExpression = token.indexOf("}");
                _assertion = token.substring(startOfExpression, endOfExpression)
                                          .trim();

                // construct the inputs for assertion actor
                ListIterator inputs = _inputs.listIterator();

                while (inputs.hasNext()) {
                    String input = (String) inputs.next();

                    if (_assertion.indexOf(input) != -1) {
                        _assertionInputs.add(input);
                    }
                }

                if (_assertion.indexOf(_integratorOutput) != -1) {
                    _assertionInputs.add(_integratorOutput);
                }

                System.out.println(
                    "=================================> Assertion has inputs number: "
                    + _assertionInputs.size());

                // the token begins with "trans" means the agent is a FSM
            } else if (token.startsWith("trans")) {
                // It should be a modal model.
                StringTokenizer tokenST = new StringTokenizer(token);
                String discard = tokenST.nextToken(); //discard trans
                discard = tokenST.nextToken(); //discard from

                String stateName = tokenST.nextToken();
                discard = tokenST.nextToken(); //discard to

                String nextStateName = tokenST.nextToken();
                token = modeST.nextToken().trim();

                // If the next token does not begin with "when",
                // something must be wrong witht the format of Charon code.
                if (!token.startsWith("when")) {
                    throw new IllegalActionException(stateName
                        + " mode switching without correct condition!");
                }
                // The token begins with "when" is a guardExpression.
                else {
                    // we assume when (....) and do {} are in SEPERATE lines
                    String condition = token.substring(token.indexOf("(") + 1,
                            token.lastIndexOf(")")).trim();
                    System.out.println("      condition is: " + condition);

                    // Transitions list is composed of transitions.
                    // To differentiate with the Transtion class in Ptolemy,
                    // TxtTransition is used. It has three parameters: currentState, nextState and guard.
                    TxtTransition transition = new TxtTransition(stateName,
                            nextStateName, condition);
                    _transitions.add(transition);
                }
            } else {
                continue;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // actual constructor for an agent
    // It parses the content of agent to get lists of the input ports, the output ports, the connections,
    // the private variables, the initial states and the sub agents.
    // Based on the agent name, it finds the mode for the agent.
    // It constructs a composite actor or a modal model. The exception is when it is a refinement,
    // no new composite actor or modal model is constructed.
    private TypedCompositeActor _constructor() throws IllegalActionException {
        try {
            // associate the agent with according mode
            String searchModeName = getModeName();

            // search a mode via name matching
            // and associate the mode to the agent or the refinement
            System.out.println("Search Mode: " + searchModeName);

            Agent mode = (Agent) _searchAgent(searchModeName,
                    CharonProcessor.modesList);

            if (mode != null) {
                addMode(mode.getContent());
            }

            String containerAgentName = _container.getName();
            Agent containerAgent = (Agent) _searchAgent(containerAgentName,
                    CharonProcessor.agentsList);

            if (containerAgent != null) {
                LinkedList referSubAgents = containerAgent.getSubAgents();

                if (referSubAgents.size() != 0) {
                    Agent subAgent = (Agent) _searchAgent(getName(),
                            referSubAgents);

                    if (subAgent != null) {
                        _referParameters = subAgent.getParameters();
                    }
                }
            }

            // update the container
            // if the agent contains some transitions, it should be a modal model
            // Note the transitions are discovered in addMode() part.
            if (_transitions.size() != 0) {
                _container = new ModalModel(_container, _name);
                System.out.println(" Container is an instance of "
                    + _container.getClass().toString());
            } else if (!(_container instanceof Refinement)) {
                _container = new TypedCompositeActor(_container, _name);
            }

            // status checking
            System.out.println("connections number: " + _connections.size());
            System.out.println("subAgents number: " + _subAgents.size());
            System.out.println("inputs number: " + _inputs.size());
            System.out.println("outputs number: " + _outputs.size());
            System.out.println("parameters Number: " + _parameters.size());

            /*      ListIterator iii = _parameters.listIterator();
                    while (iii.hasNext()) {
                    System.out.println((String) iii.next());
                    }
            */
            System.out.println("transitions Number: " + _transitions.size());

            System.out.println("constructing new Agent: " + getName());

            // add parameters to the composite actor or the modal model
            // An agent may be composed of several subAgents listed in the _subAgents list
            // Note that the element in the _subAgents list only has a name and
            // a list of parameters for the element (see constructor of Agent)
            if (!_topLevel) {
                // refinement should have a CTEmbeddedDirector
                if (_container instanceof Refinement) {
                    _director = new CTEmbeddedDirector(_container,
                            "CT Embedded Director");
                } else if (_referParameters.size() != 0) {
                    ListIterator paraIterator = _parameters.listIterator();
                    ListIterator referParaIterator = _referParameters
                                .listIterator();

                    while (referParaIterator.hasNext()) {
                        String paraName = (String) paraIterator.next();
                        String referParaName = (String) referParaIterator.next();

                        // Note that since it is not top level,
                        // the parameter does not have actual value.
                        // The actual value is evaluated during simulation
                        // referring to the upper level parameters.
                        Parameter parameter = new Parameter(_container, paraName);
                        parameter.setExpression(referParaName);
                    }
                } else {
                }
            } else if (!(_container instanceof ModalModel)) {
                // topLevel should have a CTMixedSignalDirector
                _director = new CTMixedSignalDirector(_container, "CT Director");

                /*        // This composite actor has the top Level as its container,
                // it should provide the actual values for the parameters.
                // However, the names of the parameters have to be associated
                // with parameters from subAgents.

                ListIterator subAgents = _subAgents.listIterator();

                //        System.out.println("      subAgents size: " + _subAgents.size());

                while (subAgents.hasNext()) {
                Agent subAgent = (Agent) subAgents.next();
                Agent agent = (Agent) _searchAgent(subAgent.getName(), CharonProcessor.agentsList);

                if (agent == null) {
                System.out.println("         Something wrong when looking for subAgent! " + subAgent.getName());
                } else {
                System.out.println("         Found subAgent " + subAgent.getName() +
                " with parameters number " + subAgent.getParameters().size());
                }

                ListIterator agentParas = agent.getParameters().listIterator();
                ListIterator subAgentParas = subAgent.getParameters().listIterator();

                while (subAgentParas.hasNext()) {
                String paraValue = (String) subAgentParas.next();
                String paraName = (String) agentParas.next();

                // Note that some parameters may be used by several sub agents,
                // so, there may be some name duplications in the following program.
                // However, it is bearable.
                try {
                Parameter parameter = new Parameter (_container, paraName, new DoubleToken(paraValue));
                } catch (NameDuplicationException e) {
                System.out.println("====> Warning! " + e.getMessage());
                }
                }

                }
                */
            }

            // add relations and links
            System.out.println("adding relations and links to Agent: "
                + getName());

            _relationsAndLinks();
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(e.getMessage());
        }

        return _container;
    }

    // search agent or mode by name matching
    private Agent _searchAgent(String agentName, LinkedList agentsList) {
        ListIterator agents = agentsList.listIterator();

        while (agents.hasNext()) {
            Agent agent = (Agent) agents.next();

            if (agent.getName().equals(agentName)) {
                System.out.println(" agent / mode name: " + agent.getName()
                    + " ---------> destName " + agentName);
                return agent;
            }
        }

        return null;
    }

    // This method configures the input/output ports of composite actor
    // and makes connections between ports.
    // There are three cases:
    // 1. An agent with only one mode, which is an ODE.
    //    It is composed of one expression actor and one integrator.
    //        (or more than one)
    // 2. An agent with several modes, which is an FSM.
    //    It is a modal model with several states.
    //    Each state has an refinement implementing an mode.
    // 3. An agent with several sub agents, which is a hierarchy structure.
    private void _relationsAndLinks() throws IllegalActionException {
        try {
            if (_subAgents.size() == 0) {
                if (_transitions.size() == 0) {
                    // An agent with single mode
                    // This mode has one expression actor and one integrator
                    // FIXME: There may be several integrators and expressions.
                    Integrator integrator = new Integrator(_container,
                            "integrator");
                    Expression expression = new Expression(_container,
                            "expression");
                    expression.expression.setExpression(_expression);

                    TypedIOPort expressionOutput = expression.output;
                    TypedIOPort integratorInput = integrator.input;

                    // if the expression actor has no inputs, its output type has to be
                    // configured
                    if (getInputs().size() == 0) {
                        TypeAttribute ta = new TypeAttribute(expressionOutput,
                                "_type");
                        ta.setExpression("double");

                        //expression.output.setTypeEquals(BaseType.DOUBLE);
                    }

                    Parameter signalType;

                    // Usually, there is one direction between the output port of the expression
                    // actor and the input port of the integrator.
                    TypedIORelation relation0 = new TypedIORelation(_container,
                            "relation0");
                    expressionOutput.link(relation0);
                    integratorInput.link(relation0);

                    integrator.initialState.setExpression(_initialState);

                    // If _expressionInputs.size() != 0, there is a loop from
                    // the integrator output to the input port of expression actor.
                    ListIterator exprInputs = _expressionInputs.listIterator();

                    while (exprInputs.hasNext()) {
                        String inputPortName = (String) exprInputs.next();
                        TypedIOPort expressionInput = new TypedIOPort(expression,
                                inputPortName, true, false);

                        // If the container is a refinement,
                        // We have to configure the signal Type of the inputs for expression actor
                        // as "CONTINUOUS"
                        if (_container instanceof Refinement) {
                            signalType = new Parameter(expressionInput,
                                    "signalType", new StringToken("CONTINUOUS"));
                        }
                    }

                    // The mode may have one invariant.
                    if (_assertion != "") {
                        Assertion assertion = new Assertion(_container,
                                "assertion");
                        assertion.assertion.setExpression(_assertion);

                        ListIterator asserInputs = _assertionInputs
                                    .listIterator();

                        while (asserInputs.hasNext()) {
                            String inputPortName = (String) asserInputs.next();
                            TypedIOPort expressionInput = new TypedIOPort(assertion,
                                    inputPortName, true, false);

                            // If the container is a refinement,
                            // We have to configure the signal Type of the inputs for expression actor
                            // as "CONTINUOUS"
                            if (_container instanceof Refinement) {
                                signalType = new Parameter(expressionInput,
                                        "signalType",
                                        new StringToken("CONTINUOUS"));
                            }
                        }
                    }

                    ListIterator outputs = getOutputs().listIterator();

                    // It is presumed that there is only one output for one agent.
                    if (getOutputs().size() > 1) {
                        throw new IllegalActionException(
                            "Can not handle actor with more than one output.");
                    }

                    while (outputs.hasNext()) {
                        String outputStr = (String) outputs.next();
                        TypedIOPort containerOutput = new TypedIOPort(_container,
                                outputStr, false, true);

                        // Usually there is direct connection between the integrator output port
                        // and the output port of the container.
                        TypedIORelation relation1 = new TypedIORelation(_container,
                                "relation1");
                        TypedIOPort integratorOutput = integrator.output;
                        integratorOutput.link(relation1);
                        containerOutput.link(relation1);
                    }

                    ListIterator inputs = getInputs().listIterator();

                    while (inputs.hasNext()) {
                        String inputStr = (String) inputs.next();
                        TypedIOPort _containerInput = new TypedIOPort(_container,
                                inputStr, true, false);

                        // It is presumed that the inputs of container are also inputs for expression actor.
                        // We have to configure the signal Type of the inputs for expression actor
                        // as "CONTINUOUS"
                        TypedIOPort expressionInput = new TypedIOPort(expression,
                                inputStr, true, false);

                        if (_container instanceof Refinement) {
                            signalType = new Parameter(expressionInput,
                                    "signalType", new StringToken("CONTINUOUS"));
                        }
                    }
                } else {
                    // The agent is a modal model.
                    // Note that the states and transitions have the controller as their container,
                    // while the refinements have the modal model as their container.
                    FSMActor controller = ((FSMDirector) _container.getDirector())
                                .getController();

                    ListIterator transitions = _transitions.listIterator();
                    int transitionIndex = 0;

                    while (transitions.hasNext()) {
                        TxtTransition transition = (TxtTransition) transitions
                                    .next();
                        System.out.println("transition from: "
                            + transition.getState() + " ----> "
                            + transition.getNextState());

                        // It is presumed that the first appeared state in Charon code is
                        // also the initial state of the controller.
                        try {
                            State initState = controller.getInitialState();
                        } catch (IllegalActionException e) {
                            controller.initialStateName.setExpression(transition
                                        .getState());
                        }

                        State state = _getState(controller,
                                transition.getState());
                        State nextState = _getState(controller,
                                transition.getNextState());

                        // Add the parameters to the current state and refinement.
                        Agent modeState = (Agent) _searchAgent(transition
                                        .getState() + "Mode",
                                CharonProcessor.modesList);
                        Agent referMode = (Agent) _searchAgent(transition
                                        .getState() + "Mode", _subModes);

                        if ((modeState != null) && (referMode != null)) {
                            if (state.getRefinement() == null) {
                                Refinement refinement = new Refinement(_container,
                                        transition.getState());
                            }

                            if (nextState.getRefinement() == null) {
                                Refinement refinement = new Refinement(_container,
                                        transition.getNextState());
                            }

                            state.refinementName.setExpression(transition
                                        .getState());
                            nextState.refinementName.setExpression(transition
                                        .getNextState());

                            Refinement stateRefinement = (Refinement) state
                                        .getRefinement()[0];

                            ListIterator parameters = modeState.getParameters()
                                                                       .listIterator();
                            ListIterator referParas = referMode.getParameters()
                                                                       .listIterator();

                            while (parameters.hasNext()) {
                                String parameter = (String) parameters.next();
                                String referPara = (String) referParas.next();

                                //Parameter para = new Parameter(state, parameter);
                                Parameter refinementPara = new Parameter(stateRefinement,
                                        parameter);

                                //para.setExpression(referPara);
                                refinementPara.setExpression(referPara);
                            }

                            // The modeState recursively call the constructor to
                            // construct the according refinement.
                            modeState.setName(transition.getState());
                            modeState.addMode(modeState.getContent());
                            modeState.constructor(stateRefinement);
                        }

                        Transition fsmTransition = new Transition(controller,
                                transition.getState() + "->"
                                + transition.getNextState());
                        fsmTransition.guardExpression.setExpression(transition
                                    .getCondition());
                        fsmTransition.reset.setExpression("true");

                        if (modeState != null) {
                            //            System.out.println("what are you doing? " + transition.getState());
                            fsmTransition.setActions.setExpression(transition
                                        .getNextState()
                                + ".integrator.initialState = "
                                + getOutputs().get(0));
                        }

                        state.outgoingPort.link(fsmTransition);
                        nextState.incomingPort.link(fsmTransition);
                    }

                    // Construct the input and output ports for the modal model.
                    ListIterator outputs = getOutputs().listIterator();

                    while (outputs.hasNext()) {
                        String outputStr = (String) outputs.next();
                        ModalPort _containerOutput = (ModalPort) _container
                                    .newPort(outputStr);
                        _containerOutput.setOutput(true);
                    }

                    ListIterator inputs = getInputs().listIterator();

                    while (inputs.hasNext()) {
                        String inputStr = (String) inputs.next();
                        ModalPort _containerInput = (ModalPort) _container
                                    .newPort(inputStr);
                        _containerInput.setInput(true);
                    }
                }
            } else {
                // An composite agent contains several sub agents.
                ListIterator outputs = getOutputs().listIterator();

                if (getOutputs().size() > 1) {
                    throw new IllegalActionException(
                        "Can not handle actor with more than one output.");
                }

                while (outputs.hasNext()) {
                    String outputStr = (String) outputs.next();
                    TypedIOPort _containerOutput = new TypedIOPort(_container,
                            outputStr, false, true);
                }

                ListIterator inputs = getInputs().listIterator();

                while (inputs.hasNext()) {
                    String inputStr = (String) inputs.next();
                    TypedIOPort _containerInput = new TypedIOPort(_container,
                            inputStr, true, false);
                }

                ListIterator agents = _subAgents.listIterator();

                while (agents.hasNext()) {
                    Agent subAgent = (Agent) agents.next();

                    //          TypedCompositeActor tca = new TypedCompositeActor(_container, subAgent.getName());
                    Agent agent = (Agent) _searchAgent(subAgent.getName(),
                            CharonProcessor.agentsList);

                    // The sub agent recursively calls its constructor.
                    agent.constructor(_container);
                }
            }

            // The composite actors and the atomic actors are constructed
            // and their ports are configured.
            // The following part is for the connections of the ports.
            System.out.println("make relations and links ... ");

            // Since index 0 and 1 may have been used in the agent as an ODE case.
            int relationIndex = 2;

            // link internal ports
            // The method is to trace source ports via iterating the input ports
            // of all the enties of the container.
            // If there is one relation associated with the input port,
            //    there is no reason to make the port link to another relation.
            // If there is no relation associated with the input port,
            //    but there is one relation associated with the source port,
            //    then, the input port should use the relation instead of
            //    construct and use a new relation.
            // Otherwise, make an new relation and link the relation to the input
            //    port and the source port.
            ListIterator entities = _container.entityList().listIterator();

            while (entities.hasNext()) {
                Actor actor = (Actor) entities.next();
                System.out.println("    dealing with Actor: "
                    + ((Nameable) actor).getFullName());

                ListIterator inputs = actor.inputPortList().listIterator();

                while (inputs.hasNext()) {
                    TypedIOPort input = (TypedIOPort) inputs.next();
                    System.out.println("      Input port name: "
                        + input.getFullName());

                    if (input.linkedRelationList().size() > 0) {
                        continue;
                    }

                    TypedIOPort source = _searchSource(input);
                    TypedIORelation relation;

                    if (source != null) {
                        List relations;

                        // the source port is either in the same level or in the upper level.
                        // if in the same level, linkedRelationList() works;
                        // if in the upper level, insideRelationsList() is used.
                        if (((NamedObj) source.getContainer()).depthInHierarchy() == ((NamedObj) input
                                    .getContainer()).depthInHierarchy()) {
                            relations = source.linkedRelationList();
                        } else {
                            relations = source.insideRelationList();
                        }

                        System.out.println("      Source port name: "
                            + source.getFullName() + " " + relations.size());

                        if (relations.size() > 1) {
                            throw new IllegalActionException(
                                "port has two relations!");
                        } else if (relations.size() == 1) {
                            relation = (TypedIORelation) relations.get(0);
                        } else {
                            relation = new TypedIORelation(_container,
                                    "relation" + relationIndex);
                            relationIndex++;
                            source.link(relation);
                        }

                        input.link(relation);
                    }
                }
            }

            if (_container instanceof ModalModel) {
                // link internal ports
                // If the model is ModalModel, all refinements use the same outputs
                // which means that we have connect all the output of refinements to model outputs
                ListIterator containerOutputs = _container.outputPortList()
                                                                  .listIterator();

                while (containerOutputs.hasNext()) {
                    TypedIOPort containerOutput = (TypedIOPort) containerOutputs
                                .next();
                    TypedIORelation containerOutputRelation = (TypedIORelation) containerOutput.insideRelationList()
                                                                                                       .get(0);

                    /*          TypedIORelation containerOutputRelation = new TypedIORelation();
                                if (containerOutput.linkedRelationList().size() == 0) {
                                System.out.println(containerOutput.insideRelationList().size());
                                continue;
                                } else {
                                containerOutputRelation = (TypedIORelation) containerOutput.insideRelationList().get(0);
                                }
                    */
                    entities = _container.entityList().listIterator();

                    while (entities.hasNext()) {
                        Actor actor = (Actor) entities.next();

                        if (!(actor instanceof ModalController)) {
                            System.out.println("    dealing with Refinement: "
                                + ((Nameable) actor).getFullName());

                            ListIterator outputs = actor.outputPortList()
                                                                .listIterator();

                            while (outputs.hasNext()) {
                                TypedIOPort output = (TypedIOPort) outputs.next();
                                System.out.println(
                                    "      Refinement Output port name: "
                                    + output.getFullName());

                                if (output.linkedRelationList().size() > 0) {
                                    ListIterator orelations = output.linkedRelationList()
                                                                            .listIterator();

                                    while (orelations.hasNext()) {
                                        //                    System.out.println(orelations.next());
                                        TypedIORelation tir = (TypedIORelation) orelations
                                                    .next();
                                        System.out.println(tir
                                                    .linkedSourcePortList());
                                    }

                                    //                  throw new IllegalActionException ("Should not have any relation in modal model level before adding relations.");
                                } else {
                                    output.link(containerOutputRelation);
                                }
                            }
                        }
                    }
                }
            } else {
                // link container output ports to the entities output ports of the container
                ListIterator sinks = _container.outputPortList().listIterator();

                while (sinks.hasNext()) {
                    TypedIOPort sink = (TypedIOPort) sinks.next();
                    System.out.println("      Output port name: "
                        + sink.getFullName());

                    TypedIOPort source = _searchSource(sink);
                    TypedIORelation relation;

                    if (source != null) {
                        List relations = source.linkedRelationList();
                        System.out.println("      Source port name: "
                            + source.getFullName() + " " + relations.size());

                        if (relations.size() > 1) {
                            throw new IllegalActionException(
                                "port has two relations!");
                        } else if (relations.size() == 1) {
                            relation = (TypedIORelation) relations.get(0);
                            sink.link(relation);
                        } else {
                            // If there is no relation associated with the entities outputs,
                            // while the container output has one relation linked,
                            // the entities output ports should be linked to the relation.
                            List myRelations = sink.insideRelationList();
                            System.out.println(sink.getName()
                                + " has relations: " + myRelations.size());

                            if (myRelations.size() > 1) {
                                throw new IllegalActionException(
                                    "port has two relations!");
                            } else if (myRelations.size() == 1) {
                                relation = (TypedIORelation) myRelations.get(0);
                            } else {
                                relation = new TypedIORelation(_container,
                                        "relation" + relationIndex);
                                relationIndex++;
                                sink.link(relation);
                            }

                            source.link(relation);
                        }

                        // Status checking ...
                        relations = sink.insideRelationList();
                        System.out.println("      After linking, Output name: "
                            + sink.getName() + " " + relations.size());
                        System.out.println("      relation has "
                            + relation.linkedPortList().size());
                    }
                }
            }
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    // Search source port by name mapping or
    // based on the connections list specified in agent content.
    private TypedIOPort _searchSource(TypedIOPort port) {
        String inputName = port.getName();
        String sourceName = inputName;

        // iterate the connections list
        ListIterator connections = _connections.listIterator();

        while (connections.hasNext()) {
            Pair pair = (Pair) connections.next();

            if (pair.getLeft().equals(inputName)) {
                sourceName = pair.getRight();
                break;
            }
        }

        // internal connections
        // search if there is some output port of some entity having the
        // same name with the input port name.
        ListIterator entities = _container.entityList().listIterator();

        while (entities.hasNext()) {
            ListIterator outputs = ((Actor) entities.next()).outputPortList()
                                            .listIterator();

            while (outputs.hasNext()) {
                TypedIOPort output = (TypedIOPort) outputs.next();

                if (output.getName().equals(sourceName)) {
                    return output;
                }
            }
        }

        // spcially for the container output port to the outside enviroment
        if (port.isOutput()) {
            entities = _container.entityList().listIterator();

            while (entities.hasNext()) {
                ListIterator inputs = ((Actor) entities.next()).inputPortList()
                                               .listIterator();

                while (inputs.hasNext()) {
                    TypedIOPort input = (TypedIOPort) inputs.next();

                    if (input.getName().equals(sourceName)) {
                        return input;
                    }
                }
            }
        } else {
            // If there is no inner outputs with the same name with
            // the input port name, iterate the container input ports.
            ListIterator inputs = _container.inputPortList().listIterator();

            while (inputs.hasNext()) {
                TypedIOPort enviromentInput = (TypedIOPort) inputs.next();

                if (enviromentInput.getName().equals(sourceName)) {
                    return enviromentInput;
                }
            }
        }

        return null;
    }

    // get the state from the controller by name matching
    private State _getState(FSMActor fsmActor, String stateName)
        throws IllegalActionException, NameDuplicationException {
        ListIterator states = fsmActor.entityList().listIterator();

        while (states.hasNext()) {
            State state = (State) states.next();

            if (state.getName().equals(stateName)) {
                return state;
            }
        }

        State newState = new State(fsmActor, stateName);
        return newState;
    }

    // Inner class Pair
    // It provides a utility which associate two elements together.
    // It is used for construct connection.
    // In one connection pair, left is for the input port and right for output port.
    private class Pair {
        public Pair(String left, String right) {
            _left = left;
            _right = right;
        }

        public String getLeft() {
            return _left;
        }

        public String getRight() {
            return _right;
        }

        public void setLeft(String left) {
            _left = left;
        }

        public void setRight(String right) {
            _right = right;
        }

        private String _left = "";
        private String _right = "";
    }

    // Inner class TxtTransition
    // It is different from the State class in FSM.kernel
    // It has three elements: currentState, nextState, guardExpression.
    // It is a utility for construct the FSM.
    // It is used to contruct the States and Transitions in the controller (FSMActor).
    private class TxtTransition {
        public TxtTransition(String state, String nextState, String condition) {
            _state = state;
            _nextState = nextState;
            _condition = condition;
        }

        public String getState() {
            return _state;
        }

        public String getNextState() {
            return _nextState;
        }

        public String getCondition() {
            return _condition;
        }

        private String _state = "";
        private String _nextState = "";
        private String _condition = "";
    }

    // StringTokenizer for content parsing
    // It is set into global variable since the class constructor of Agent
    // uses part of it to get agentName and agentParameters.
    // While _constructor uses the following part.
    private StringTokenizer _contentST;

    // Stores the agent content or the mode content.
    private String _content;

    // Workspace for the whole model.
    private Workspace _workspace;

    // _container for construct ports, connections.
    private TypedCompositeActor _container;

    // Indicates whether in top level
    private boolean _topLevel = false;

    // The director may be CTMixedSignalDirector, CTEmbeddedDirector.
    private Director _director;

    // used for an agent with simple ODEs inside.
    private String _integratorOutput;
    private String _initialState;
    private String _expression = "";
    private String _assertion = "";
    private LinkedList _expressionInputs = new LinkedList();
    private LinkedList _assertionInputs = new LinkedList();

    // The properties for the agent or the mode.
    private String _name = "";
    private String _modeName = "";
    private LinkedList _inputs = new LinkedList();
    private LinkedList _outputs = new LinkedList();
    private LinkedList _parameters = new LinkedList();
    private LinkedList _referParameters = new LinkedList();
    private LinkedList _modeParameters = new LinkedList();
    private LinkedList _initVariables = new LinkedList();
    private LinkedList _privateVariables = new LinkedList();
    private LinkedList _subModes = new LinkedList();
    private LinkedList _subAgents = new LinkedList();
    private LinkedList _connections = new LinkedList();
    private LinkedList _transitions = new LinkedList();
}
