/*
 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.apps.charon;

import ptolemy.*;
import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.data.DoubleToken;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.domains.ct.kernel.CTMixedSignalDirector;
import ptolemy.domains.ct.kernel.CTEmbeddedDirector;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Nameable;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.Relation;
import ptolemy.vergil.fsm.modal.ModalModel;
import ptolemy.vergil.fsm.modal.ModalPort;
import ptolemy.vergil.fsm.modal.Refinement;
import ptolemy.vergil.fsm.modal.RefinementPort;

import java.util.*;

public class Agent {

  public Agent(String content) {
//    System.out.println("Agent content: " + content);

    _content = content;

    String token;
    _contentST = new StringTokenizer(content,  "()");

    // get the agent name
    token = _contentST.nextToken();
    int nameIndex = token.trim().indexOf(" ");
    _name = token.substring(nameIndex).trim();

    // get the agent parameters
    token = _contentST.nextToken();

    // notice there may be no parameters, e.g. topLevel
    StringTokenizer tokenST = new StringTokenizer(token, " ,\n\t\r");

    while (tokenST.hasMoreTokens()) {
      String tokenToken = tokenST.nextToken().trim();
      if (!tokenToken.equals("real")) {
	_parameters.add(tokenToken);
      }
    }
 }

  public String getContent() {
    return _content;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public LinkedList getParameters() {
    return _parameters;
  }

  public LinkedList getInputs() {
    return _inputs;
  }

  public LinkedList getOutputs() {
    return _outputs;
  }

  public TypedCompositeActor constructor(Workspace workspace) throws IllegalActionException {
    _workspace = workspace;
    _container = new TypedCompositeActor(_workspace);
    _topLevel = true;
    try {
    _container.setName(_name);
    } catch (NameDuplicationException e) {
      throw new IllegalActionException(e.getMessage());
    }
    return _constructor(true);
  }

  public TypedCompositeActor constructor(Refinement refinement) throws IllegalActionException {
    _container = refinement;
    return _constructor(false);
  }

  public TypedCompositeActor constructor(TypedCompositeActor container) throws IllegalActionException {
    _container = container;
    _topLevel = false;
    return _constructor(false);
  }

  private TypedCompositeActor _constructor(boolean topLevel) throws IllegalActionException {

    try {

      String token;
      StringTokenizer tokenST;

      while (_contentST.hasMoreTokens()) {

	token = _contentST.nextToken("\n");

//	System.out.println("    Token is: " + token);

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
	} else if (token.trim().startsWith("init")) {
	    tokenST = new StringTokenizer(token, "{};");
	    // get rid of "init"
	    String tokenToken = tokenST.nextToken();
	    // get initial variable expression
	    tokenToken = tokenST.nextToken();
	    _initialState = tokenToken.substring(tokenToken.indexOf("=") + 1);
	} else if (token.trim().startsWith("agent")) {
	    tokenST = new StringTokenizer(token);
	    tokenST.nextToken("=");
	    _subAgents.add(new Agent("agent " + tokenST.nextToken()));
	} else if (token.trim().startsWith("[")) {
//	    System.out.println("Connection: " + token);
	    int seperator = token.indexOf(":=");
	    String inputs = token.substring(1, seperator - 1).trim();
	    String outputs = token.substring(seperator + 2).trim();
//	    System.out.println("    Stirng pair is: " + inputs + " : " + outputs );
	    StringTokenizer inputST = new StringTokenizer(inputs, " \n\r\t,[]");
	    StringTokenizer outputST = new StringTokenizer(outputs, " \n\r\t,[]");
	    while (inputST.hasMoreTokens()) {
	      String input = inputST.nextToken();
	      String output = outputST.nextToken();
//	      System.out.println("    pair is: " + input + " . " + output );
	      _connections.add(new Pair(input, output));
	    }
	}
      }

      // associate agent with mode
      String searchAgentName;
      if (getName().endsWith("Mode")) {
	searchAgentName = getName();
      } else {
	searchAgentName = getName() + "TopMode";
      }

      Agent mode = _searchAgent(searchAgentName, CharonProcessor.modesList);
      if (mode != null) {
	addMode(mode.getContent());
      }

      // update the container
      // if agent contains transitions, it should be a modal model
      if (_transitions.size() != 0) {
	_container = new ModalModel(_container, _name);
      } else if (!(_container instanceof Refinement)) {
        _container = new TypedCompositeActor(_container, _name);
      }

      // status checking
      System.out.println("connections number: " + _connections.size());
      System.out.println("subAgents number: " + _subAgents.size());
      System.out.println("inputs number: " + _inputs.size());
      System.out.println("outputs number: " + _outputs.size());
      System.out.println("parameters Number: " + _parameters.size());
      System.out.println("transitions Number: " + _transitions.size());

      System.out.println("constructing new Agent: " + getName());

      // add parameters

      // an agent may be composed of several subAgents which are in a _subAgents list
      // an element in _subAgents list only has a name and values of parameters for the element
      // an agent which can construct a TypedCompositeActor should refer to _agentsList
      // of CharonProcessor via name matching

     // FIXME only do this at topLevel.
      if (!_topLevel) {

	ListIterator paraIterator = _parameters.listIterator();
	while (paraIterator.hasNext()) {
	  String paraName = (String) paraIterator.next();
	  Parameter parameter = new Parameter(_container, paraName);
	  parameter.setExpression(paraName);
	}

	if (_container instanceof Refinement) {
	  _director = new CTEmbeddedDirector(_container, "CT Embedded Director");
	}
      } else {
	// topLevel
	_director = new CTMixedSignalDirector(_container, "CT Director");

	// its container is topLevel and should provide the values.
	// The parameters will be added with information from subAgents.

	ListIterator subAgents = _subAgents.listIterator();

	System.out.println("      subAgents size: " + _subAgents.size());

	while (subAgents.hasNext()) {
	  Agent subAgent = (Agent) subAgents.next();
	  Agent agent = _searchAgent(subAgent.getName(), CharonProcessor.agentsList);

	  if (agent == null) {
	    System.out.println("         Something wrong when finding subAgent! " + subAgent.getName());
	  } else {
	    System.out.println("         Found subAgent " + subAgent.getName() +
			       " with parameters number " + subAgent.getParameters().size());
	  }

	  ListIterator agentParas = agent.getParameters().listIterator();
	  ListIterator subAgentParas = subAgent.getParameters().listIterator();

	  while (subAgentParas.hasNext()) {
	    String paraValue = (String) subAgentParas.next();
	    String paraName = (String) agentParas.next();
	    try {
	      Parameter parameter = new Parameter (_container, paraName, new DoubleToken(paraValue));
	    } catch (NameDuplicationException e) {
	      System.out.println("====> Warning! " + e.getMessage());
	    }
	  }

	}
      }


      System.out.println("adding relations and links to Agent: " + getName());

      // add relations and links
      _relationsAndLinks();

    } catch (NameDuplicationException e) {
      throw new IllegalActionException (e.getMessage());
    }

    return _container;
  }

  public void addMode (String modeString) throws IllegalActionException {
    StringTokenizer modeST = new StringTokenizer(modeString,"\n");
    while (modeST.hasMoreTokens()) {
      String token = modeST.nextToken().trim();
      if (token.startsWith("diff")) {
	int startOfIntegratorOutput = token.indexOf("d(") + 2;
	int endOfIntegratorOutput = token.indexOf(")");
	_integratorOutput = token.substring(startOfIntegratorOutput,endOfIntegratorOutput);

//	System.out.println("      mode token is: " + token);
	int startOfExpression = token.indexOf("==") + 2;
	int endOfExpression = token.indexOf(";");
	_expression = token.substring(startOfExpression, endOfExpression);

//	_expressionInputs = (LinkedList) _inputs.clone();

	// we only check if the expression contains the output of integrator
	// if does, add an input port with a name same with the output port
	if (_expression.indexOf(_integratorOutput) != -1) {
	  _expressionInputs.add(_integratorOutput);
	}

      } else if (token.startsWith("trans")) {
	// about mode contains mode switching...
	// modal model
	// new TypedCompositeActor should be created with CTEmbeddedDirector
	StringTokenizer tokenST = new StringTokenizer(token);
	String discard = tokenST.nextToken(); //trans
	discard = tokenST.nextToken(); //from
	String stateName = tokenST.nextToken();
	discard = tokenST.nextToken(); // to
	String nextStateName = tokenST.nextToken();
	token = modeST.nextToken().trim();
	if (!token.startsWith("when")) {
	  throw new IllegalActionException (stateName + " mode switching without correct condition!");
	}
	else {
	  // we assume when (....) and do {} are in seperate lines
	  String condition = token.substring(token.indexOf("(") + 1, token.lastIndexOf(")")).trim();
	  System.out.println("      condition is: " + condition);
	  TxtTransition transition = new TxtTransition(stateName, nextStateName, condition);
	  _transitions.add(transition);
	}
      } else {
	continue;
      }
    }
  }

  private Agent _searchAgent(String agentName, LinkedList agentsList) {
    ListIterator agents = agentsList.listIterator();
    while (agents.hasNext()) {
      Agent agent = (Agent) agents.next();
      System.out.println(" agent / mode name: " + agent.getName() + " ---------> destName " + agentName);
      if (agent.getName().equals(agentName)) {
	return agent;
      }
    }
    return null;
  }

  private void _relationsAndLinks() throws IllegalActionException {
    try {

      if (_subAgents.size() == 0) {
	if (_transitions.size() == 0) {
	  // mode
	  Integrator integrator = new Integrator(_container, "integrator");
	  Expression expression = new Expression(_container, "expression");

	  expression.expression.setExpression(_expression);

	  TypedIOPort expressionOutput = expression.output;
	  TypedIOPort integratorInput = integrator.input;

	  Parameter signalType;

	  TypedIORelation relation0 = new TypedIORelation(_container, "relation0");
	  expressionOutput.link(relation0);
	  integratorInput.link(relation0);

	  // integrator.output.setName(_integratorOutput);
	  integrator.initialState.setExpression(_initialState);

	  ListIterator exprInputs = _expressionInputs.listIterator();
	  while (exprInputs.hasNext()) {
	    String inputPortName = (String) exprInputs.next();
	    TypedIOPort expressionInput = new TypedIOPort(expression, inputPortName, true, false);
	    if (_container instanceof Refinement) {
	      signalType = new Parameter(expressionInput, "signalType", new StringToken("CONTINUOUS"));
	    }
      	  }

	  ListIterator outputs = getOutputs().listIterator();
	  if (getOutputs().size() > 1) throw new IllegalActionException ("Can not handle actor with more than one output.");
	  while(outputs.hasNext()) {
	    String outputStr = (String) outputs.next();
	    TypedIOPort containerOutput = new TypedIOPort(_container, outputStr, false, true);
	    TypedIORelation relation1 = new TypedIORelation(_container, "relation1");
	    TypedIOPort integratorOutput = integrator.output;
	    integratorOutput.link(relation1);
	    containerOutput.link(relation1);
	  }

	  ListIterator inputs = getInputs().listIterator();
	  while(inputs.hasNext()) {
	    String inputStr = (String) inputs.next();
	    TypedIOPort _containerInput = new TypedIOPort(_container, inputStr, true, false);

	    // the inputs of composite actor are also inputs for expression actor
	    TypedIOPort expressionInput = new TypedIOPort(expression, inputStr, true, false);
	    if (_container instanceof Refinement) {
	      signalType = new Parameter(expressionInput, "signalType", new StringToken("CONTINUOUS"));
	    }
	  }
	} else {
	  // modal model
    	  ListIterator transitions = _transitions.listIterator();
	  int transitionIndex = 0;
	  while (transitions.hasNext()) {
	    TxtTransition transition = (TxtTransition) transitions.next();
	    System.out.println("transition from: " + transition.getState() + " ----> " + transition.getNextState());

	    Refinement refinement = new Refinement(_container, transition.getState());
	    FSMActor controller = ((FSMDirector) _container.getDirector()).getController();

	    try {
	      State initState = controller.getInitialState();
	    } catch (IllegalActionException e) {
	      controller.initialStateName.setExpression(transition.getState());
	    }

	    State state = _getState(controller, transition.getState());
	    State nextState = _getState(controller, transition.getNextState());

	    state.refinementName.setExpression(transition.getState());
	    nextState.refinementName.setExpression(transition.getNextState());
	    // add parameters
	    Agent modeState = _searchAgent(transition.getState() + "Mode", CharonProcessor.modesList);
	    if (modeState != null) {
	      ListIterator parameters = modeState.getParameters().listIterator();
	      while (parameters.hasNext()) {
		String parameter = (String) parameters.next();
		Parameter para = new Parameter(state, parameter);
		para.setExpression(parameter);
	      }
	      modeState.setName(transition.getState());
	      modeState.addMode(modeState.getContent());
	      modeState.constructor(refinement);
	    }

	    Transition fsmTransition = new Transition(controller, transition.getState() + "->" + transition.getNextState());
	    fsmTransition.guardExpression.setExpression(transition.getCondition());

	    state.outgoingPort.link(fsmTransition);
	    nextState.incomingPort.link(fsmTransition);
	  }

	  ListIterator outputs = getOutputs().listIterator();
	  while(outputs.hasNext()) {
	    String outputStr = (String) outputs.next();
	    ModalPort _containerOutput = (ModalPort) _container.newPort(outputStr);
	    _containerOutput.setOutput(true);
	  }

	  ListIterator inputs = getInputs().listIterator();
	  while(inputs.hasNext()) {
	    String inputStr = (String) inputs.next();
	    ModalPort _containerInput = (ModalPort) _container.newPort(inputStr);
	    _containerInput.setInput(true);
	  }

	}
      } else {
	// agent
	ListIterator outputs = getOutputs().listIterator();
	if (getOutputs().size() > 1) throw new IllegalActionException ("Can not handle actor with more than one output.");
	while(outputs.hasNext()) {
	  String outputStr = (String) outputs.next();
	  TypedIOPort _containerOutput = new TypedIOPort(_container, outputStr, false, true);

	}

	ListIterator inputs = getInputs().listIterator();
	while(inputs.hasNext()) {
	  String inputStr = (String) inputs.next();
	  TypedIOPort _containerInput = new TypedIOPort(_container, inputStr, true, false);
	}

    	ListIterator agents = _subAgents.listIterator();
	while (agents.hasNext()) {
	  Agent subAgent = (Agent) agents.next();
//	  TypedCompositeActor tca = new TypedCompositeActor(_container, subAgent.getName());
	  Agent agent = _searchAgent(subAgent.getName(), CharonProcessor.agentsList);
	  agent.constructor(_container);
	}
      }

      System.out.println("make relations and links ... ");

      // since index 0 and 1 may be used.
      int relationIndex = 2;

      // link internal ports
      ListIterator entities = _container.entityList().listIterator();
      while (entities.hasNext()) {
	Actor actor = (Actor) entities.next();
	System.out.println("    dealing with Actor: " + ((Nameable) actor).getFullName());
	ListIterator inputs = actor.inputPortList().listIterator();
	while (inputs.hasNext()) {
	  TypedIOPort input = (TypedIOPort) inputs.next();
	  System.out.println("      Input name: " + input.getFullName());
	  if(input.linkedRelationList().size() > 0)
	    continue;

	  TypedIOPort source = _searchSource(input);
	  TypedIORelation relation;

	  if (source != null) {

	    List relations;
	    // the source port is either in same level or in upper level
	    // if same level, linkedRelationList() works
	    // if upper level, insideRelationsList() is used
	    if (((NamedObj)source.getContainer()).depthInHierarchy() == ((NamedObj) input.getContainer()).depthInHierarchy()) {
	      relations = source.linkedRelationList();
	    } else {
	      relations = source.insideRelationList();
	    }

	    System.out.println("      Source name: " + source.getFullName() + " " + relations.size());
	    if(relations.size() > 1) {
	      throw new IllegalActionException("port has two relations!");
	    } else if (relations.size() == 1) {
	      relation = (TypedIORelation) relations.get(0);
	    } else {
	      relation = new TypedIORelation(_container, "relation" + relationIndex);
	      relationIndex++;
	      source.link(relation);
	    }
	    input.link(relation);

/*	    relations = source.RelationList();
	    System.out.println("      After linking, Source name: " + source.getFullName() + " " + relations.size());
	    System.out.println("      relation has " + relation.linkedPortList().size());
*/	  }
	}
      }

      // link output ports to enviroment
      ListIterator sinks = _container.outputPortList().listIterator();
      while (sinks.hasNext()) {
	TypedIOPort sink = (TypedIOPort) sinks.next();
	System.out.println("      Output name: " + sink.getName());
//	if(sink.insideRelationList().size() > 0)
//	  continue;

	TypedIOPort source = _searchSource(sink);
	TypedIORelation relation;

	if (source != null) {
	  List relations = source.linkedRelationList();
	  System.out.println("      Source name: " + source.getName() + " " + relations.size());
	  if(relations.size() > 1) {
	    throw new IllegalActionException("port has two relations!");
	  } else if (relations.size() == 1) {
	    relation = (TypedIORelation) relations.get(0);
	    sink.link(relation);
	  } else {
	      List myRelations = sink.insideRelationList();
	      System.out.println(sink.getName() + " has relations: " + myRelations.size());
	      if (myRelations.size() > 1) {
		throw new IllegalActionException ("port has two relations!");
	      } else if (myRelations.size() == 1) {
		relation = (TypedIORelation) myRelations.get(0);
	      } else {
		relation = new TypedIORelation(_container, "relation" + relationIndex);
		relationIndex++;
		sink.link(relation);
	      }
	      source.link(relation);
	  }


	  relations = source.insideRelationList();
	  System.out.println("      After linking, Source name: " + source.getName() + " " + relations.size());
	  System.out.println("      relation has " + relation.linkedPortList().size());
	}
      }
    } catch (NameDuplicationException e) {
      throw new IllegalActionException (e.getMessage());
    }
  }


  private TypedIOPort _searchSource(TypedIOPort port) {
    String inputName = port.getName();
    String sourceName = inputName;

    ListIterator connections = _connections.listIterator();
    while (connections.hasNext()) {
      Pair pair = (Pair) connections.next();
      if (pair.getLeft().equals(inputName)) {
	sourceName = pair.getRight();
	break;
      }
    }

    // internal connections
    ListIterator entities = _container.entityList().listIterator();
    while (entities.hasNext()) {
      ListIterator outputs = ((Actor) entities.next()).outputPortList().listIterator();
      while (outputs.hasNext()) {
	TypedIOPort output = (TypedIOPort) outputs.next();
	if (output.getName().equals(sourceName)) {
	  return output;
	}
      }
    }

    // spcially for the output ports to enviroment
    if (port.isOutput()) {
      entities = _container.entityList().listIterator();
      while (entities.hasNext()) {
	ListIterator inputs = ((Actor) entities.next()).inputPortList().listIterator();
	while (inputs.hasNext()) {
	  TypedIOPort input = (TypedIOPort) inputs.next();
	  if (input.getName().equals(sourceName)) {
	    return input;
	  }
	}
      }
    } else {

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

  private State _getState (FSMActor fsmActor, String stateName)
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

  private class Pair {

    public Pair(String left, String right) {
      _left = left;
      _right = right;
    }

    public String getLeft() {
      return _left;
    }

    public String getRight () {
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

  private class TxtTransition {

    public TxtTransition(String state, String nextState, String condition) {
      _state = state;
      _nextState = nextState;
      _condition = condition;
    }

    public String getState() {
      return _state;
    }

    public String getNextState () {
      return _nextState;
    }

    public String getCondition() {
      return _condition;
    }

    private String _state = "";
    private String _nextState = "";
    private String _condition = "";
  }

  private StringTokenizer _contentST;
  private String _content;
  private Workspace _workspace;

  private String _integratorOutput;
  private String _initialState;
  private LinkedList _expressionInputs = new LinkedList();
  private String _expression = "";

  private TypedCompositeActor _container;
  private Director _director;
  private boolean _topLevel = false;

  private String _name;
  private String _modeName;
  private LinkedList _inputs = new LinkedList();
  private LinkedList _outputs = new LinkedList();
  private LinkedList _parameters = new LinkedList();
  private LinkedList _modeParameters = new LinkedList();
  private LinkedList _initVariables = new LinkedList();
  private LinkedList _privateVariables = new LinkedList();
  private LinkedList _subAgents = new LinkedList();
  private LinkedList _connections = new LinkedList();
  private LinkedList _transitions = new LinkedList();
}