/* An attribute that manages generation of Giotto code.

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

// Ptolemy imports.
import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.gui.TextEditor;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.*;
import ptolemy.actor.NoTokenException;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.CTDirector;
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
import ptolemy.kernel.util.StringUtilities;
import ptolemy.kernel.util.Workspace;

// Java imports.
import java.awt.Frame;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collections;
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

	_attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-50\" y=\"-20\" width=\"100\" height=\"40\" "
                + "style=\"fill:blue\"/>"
                + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:white\">"
                + "Double click to\ngenerate code.</text></svg>");
        new SingletonAttribute(this, "_hideName");
        new CharonEditorFactory(this, "_editorFactory");

    }
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate Giotto code for the _container model.
     *  @return The Giotto code.
     */
    public String generateCode() throws IllegalActionException {
	try {
	    // initialization
	    generatedCode = "";
	    if (!_initialize()) {
	        return "Can not generate code for this model!";
    	    }

	    _currentDepth = depthInHierarchy();
	    String containerName = _container.getName();

	    generatedCode += _compositeAgentCode(_container)
			   + _modeCode;


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
    ////                         private methods                    ////

    /** Topology analysis and initialization.
     *
     * @ return Ture if in giotto domain, False if in other domains.
     */
    private boolean _initialize() throws IllegalActionException {
	_container = (TypedCompositeActor) getContainer();
	return true;
    }

    /** Topology analysis to get a list of agents.
     *  The agents should be TypedCompositeActor with parameter charonMode
     *  as null (default) or true.
     *
     * @param actor Container to be analyzed to return agent list.
     * @return List of agents defined in model.
     */
    private LinkedList _agents(CompositeActor actor) throws IllegalActionException {
	LinkedList agentList = new LinkedList();
	ListIterator agentsIterator =  actor.entityList(TypedCompositeActor.class).listIterator();
	while (agentsIterator.hasNext()) {
	    TypedCompositeActor agent = (TypedCompositeActor) agentsIterator.next();
	    Parameter charonAgent = (Parameter) agent.getAttribute("charonAgent");
	    if (charonAgent == null || ((BooleanToken) charonAgent.getToken()).booleanValue()) {
		agentList.add(agent);
	    }
      	}
	return agentList;
    }

    /** Return a list of source ports connected to this port on the
     *  same layer that can send data to this port.  This includes
     *  output ports that are connected on the outside to this port,
     *  and input ports that are connected on the inside to this port.
     *
     *  @param input TypedIOPort
     *  @return A list of IOPort objects.
     */
    private LinkedList shallowSourcePortList(TypedIOPort input) {
        try {
            _workspace.getReadAccess();
            Actor container = (Actor)input.getContainer();
            Director excDirector = ((Actor) container).getExecutiveDirector();
	    //int depthOfDirector = excDirector.depthInHierarchy();
	    int depthOfContainer = ((NamedObj) container).depthInHierarchy();
//System.out.println(input.getName()+ " ======== " + depthOfContainer);
            LinkedList result = new LinkedList();
	    Iterator ports = input.connectedPortList().iterator();
            while (ports.hasNext()) {
		IOPort port = (IOPort)ports.next();
                int depth = port.depthInHierarchy();
//System.out.println(port.getName() + " " + depth);
                if (port.isInput() && depth <= depthOfContainer) {
                    result.addLast(port);
//System.out.println("input-----" + port.getName() + " " + depth);
                } else if (port.isOutput() && (depth == (depthOfContainer + 1))) {
                    result.addLast(port);
//System.out.println("output----" + port.getName() + " " + depth);
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
    private String _compositeAgentCode(CompositeActor actor) throws IllegalActionException {

	if (FSMDirector.class.isInstance(actor.getDirector())) {
	    // mode code generation goes here.
	    return _agentCode(actor);
	}

	LinkedList subAgents = _agents(actor);
	if (subAgents.size() == 0) return _agentCode(actor);

	String compositeCodeString = "";
	String subAgentCode = "";
	String privateVariables = "";

	ListIterator subAgentsIterator = subAgents.listIterator();

	while (subAgentsIterator.hasNext()) {

	    String subAgentConnectionInputs = "";
	    String subAgentConnectionOutputs = "";

	    CompositeActor subAgent = (CompositeActor)subAgentsIterator.next();

	    subAgentCode += "  agent "
			  + subAgent.getName().toLowerCase() + " = "
			  + subAgent.getName()
			  + " ( "
			  + _agentParameters((NamedObj)subAgent, false)
			  + " );"
			  + _endLine;

	    ListIterator subAgentInputs = subAgent.inputPortList().listIterator();

	    while (subAgentInputs.hasNext()) {
		TypedIOPort input = (TypedIOPort) subAgentInputs.next();
//		System.out.println(subAgent.getName() + " " + input.getName() + " start");
		LinkedList sourceList = shallowSourcePortList(input);
		ListIterator sources = sourceList.listIterator();
//		System.out.println("finish");
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
			    subAgentConnectionOutputs += ", "
						       + source.getName();
			    subAgentConnectionInputs += ", "
						      + input.getName();
			}
		    }
		}
		if (privateVariable) {
		    if (privateVariables == "") {
		        privateVariables += "private analog real "
					  + input.getName();
		    } else {
		        privateVariables += ", "
					  + input.getName();
		    }
		}
	    }

	    if (subAgentConnectionInputs.length() != 0) {
		subAgentCode += "       [ "
			      + subAgentConnectionInputs
			      + " := "
			      + subAgentConnectionOutputs
			      + " ] ;"
			      + _endLine;
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

	LinkedList parameterList = (LinkedList)actor.attributeList(Parameter.class);
	int parameterNumber = parameterList.size();
	ListIterator parameters = parameterList.listIterator();

	_inPorts = actor.inputPortList().iterator();
	while (_inPorts.hasNext()) {
	    if (inputString == "") {
	        inputString += "read analog real "
			         + ((NamedObj)_inPorts.next()).getName();
	    } else {
	        inputString += ", "
			         + ((NamedObj)_inPorts.next()).getName();
	    }
	}
	if (inputString != "") inputString += ";";


	_outPorts = actor.outputPortList().iterator();

	if (_outPorts.hasNext()) {
	    String outportName = ((NamedObj)_outPorts.next()).getName();
	    if (outputString == "") {
		outputString += "write analog real "
				 + outportName;

	    } else {
		outputString += ", "
				 + outportName;
	    }
	}

	if (privateVariables.length() != 0) privateVariables += ";";

	compositeCodeString += " "
		    + actor.getName()
		    + " ( "
		    + _agentParameters((NamedObj)actor, true)
		    + " )" + _endLine
		    + "{" + _endLine
		    + "  " + outputString + _endLine
		    + "  " + inputString + _endLine
		    + "  " + privateVariables + _endLine
		    + subAgentCode + _endLine
		    + "}" + _endLine;

        return compositeCodeString;
    }

    /** Generate string of parameters of the agent.
     *
     *  @param agent whether the parameters belong to.
     *  @param typed indicates whether the parameters have type.
     *  @return string of parameters.
     */
     private String _agentParameters(NamedObj agent, boolean typed) {

	LinkedList parameterList = (LinkedList)agent.attributeList(Parameter.class);
	ListIterator parameters = parameterList.listIterator();

	String prefix = "";
	if (typed) prefix = "real ";

	String parameterString = "";

	while (parameters.hasNext()) {

	    String parameterName = ((NamedObj)parameters.next()).getName();

	    if (parameterName.startsWith("_")) continue;

	    if (parameterString == "") {
	        parameterString += prefix
			         + parameterName;

	    } else {
	        parameterString += ", "
		                 + prefix
			         + parameterName;
	    }
	}

	return parameterString;
     }

    /** Generate code for the agent.
     *
     *  @param CompositeActor The agent generated code from.
     *  @return The agent code.
     */
     private String _agentCode(CompositeActor actor) throws IllegalActionException {

	String codeString = "agent";
	String parameterString = "";
	String inputString = "";
	String outputString = "";
	String initString = "";
	String modeString = "";
	String modeParameterString = "";
	String typedModeParameterString = "";
	String flowString = _graphToText(actor);
	String invariantString = "";

	LinkedList parameterList = (LinkedList)actor.attributeList(Parameter.class);
	int parameterNumber = parameterList.size();
	ListIterator parameters = parameterList.listIterator();

	_inPorts = actor.inputPortList().iterator();
	while (_inPorts.hasNext()) {
	    if (inputString == "") {
	        inputString += "read analog real "
			         + ((NamedObj)_inPorts.next()).getName();
	    } else {
	        inputString += ", "
			         + ((NamedObj)_inPorts.next()).getName();
	    }
	}
	if (inputString != "") inputString += ";";


	_outPorts = actor.outputPortList().iterator();
	int outportNumber = actor.outputPortList().size();

	// since the parameters are either for mode or for outport,
	// we assume the number of the parameters for outport is the same
	// with the number of outport(s)
	boolean parameterForOutport = false;

	while (parameters.hasNext()) {

	    if (parameters.nextIndex() >= (parameterNumber - outportNumber)) {
		parameterForOutport = true;
      	    }

	    String parameterName = ((NamedObj)parameters.next()).getName();

	    if (parameterName.startsWith("_")) continue;

	    if (parameterString == "") {
	        parameterString += "real "
			         + parameterName;

	    } else {
	        parameterString += ", real "
			         + parameterName;
	    }

	    if (parameterForOutport) {

		if (_outPorts.hasNext()) {
		    String outportName = ((NamedObj)_outPorts.next()).getName();
		    if (outputString == "") {
			outputString += "write analog real "
					 + outportName;

		    } else {
			outputString += ", "
					 + outportName;
		    }
		    initString += outportName
				+ " = "
				+ parameterName
				+ " ;";
		} else {
		    // this should never happen!
		}
	    } else {

	        if (modeParameterString == "") {
		    modeParameterString += parameterName;
		    typedModeParameterString += "real "
					      + parameterName;
		} else {
		    modeParameterString += ", "
					 + parameterName;
		    typedModeParameterString += ", real "
					      + parameterName;
		}

	    }

	}

	if (outputString != "") outputString += ";";
	initString = "init { " + initString + " }";
	modeString = "mode top = "
		   + actor.getName()
		   + "TopMode"
		   + " ( "
		   + modeParameterString
		   + " ) ;";

	codeString += " "
		    + actor.getName()
		    + " ( "
		    + parameterString
		    + " )" + _endLine
		    + "{" + _endLine
		    + "  " + outputString + _endLine
		    + "  " + inputString + _endLine
		    + "  " + initString + _endLine
		    + "  " + modeString + _endLine
		    + "}" + _endLine;

	if (FSMDirector.class.isInstance(actor.getDirector())) {
	    // mode code generation goes here.
	    _modeCode += "mode "
		       + actor.getName()
		       + "TopMode"
		       + " ( "
		       + typedModeParameterString
		       + " )" + _endLine
		       + "{" + _endLine
		       + "  " + outputString + _endLine
		       + "  " + inputString + _endLine;
	    // notice the _fsmModeCode(actor) will modify the _modeCode with transitions code
	    // and return the mode code for each sub mode.(refinement)
	    String subModeString = _fsmModeCode(((FSMDirector)actor.getDirector()).getController(), inputString, outputString);
       	    _modeCode +="  " + invariantString + _endLine
		       + "}" + _endLine
		       + subModeString;

	} else {
	    _modeCode += "mode "
		       + actor.getName()
		       + "TopMode"
		       + " ( "
		       + typedModeParameterString
		       + " )" + _endLine
		       + "{" + _endLine
		       + "  " + outputString + _endLine
		       + "  " + inputString + _endLine
		       + "  " + flowString + _endLine
		       + "  " + invariantString + _endLine
		       + "}" + _endLine;
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

     private String _fsmModeCode(FSMActor fsm, String inputPorts, String outputPorts) throws IllegalActionException {
	String subModeCode = "";
	String transitionString = "";

	transitionString += "  trans from default to "
		        + fsm.getInitialState().getName() + _endLine
			+ "  when ( true ) do { } " + _endLine;

        ListIterator stateIterator = fsm.entityList().listIterator();

	while (stateIterator.hasNext()) {
	    State st = (State) stateIterator.next();
	    String stParameters = _agentParameters(st, false);
	    String typedStParameters = _agentParameters(st, true);
	    String flowString = "";
	    String invariantString = "";

	    _modeCode += "  mode "
		       + st.getName() + " = "
		       + st.getName() + "Mode"
		       + " ( "
		       + stParameters
		       + " );"
		       + _endLine;

	    subModeCode += "mode "
		         + st.getName() + "Mode "
			 + "( "
			 + typedStParameters
			 + " )" + _endLine
			 + "{" + _endLine
			 + "  " + outputPorts + _endLine
			 + "  " + inputPorts + _endLine
			 + "  " + flowString + _endLine
			 + "  " + invariantString + _endLine
			 + "}" + _endLine;

	    LinkedList transitionList = new LinkedList();
	    transitionList.addAll(st.preemptiveTransitionList());
	    transitionList.addAll(st.nonpreemptiveTransitionList());
	    ListIterator transitionItr = transitionList.listIterator();
	    while (transitionItr.hasNext()) {
		Transition tr = (Transition) transitionItr.next();
		State newState = tr.destinationState();
		String guardString = tr.getGuardExpression();
		transitionString += "  trans from "
			        + st.getName() + " to "
				+ newState.getName() + _endLine
				+ "  when ( " + guardString + ") " + _endLine
				+ "  do {}" + _endLine;
	    }
	}

	_modeCode += transitionString + _endLine;

	return subModeCode;
     }

    /** Transform the graphic block diagram to text expression.
     *  Assume container only contains atomic actors.
     *
     *  @param container contains actors.
     *  @return txtExpression of graphic block diagram.
     */

     private String _graphToText(CompositeActor container) throws IllegalActionException {
	String txtString = "";
	List actors = container.entityList();

	return txtString;
     }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private FSMActor _modeSwitchController;
    private TypedCompositeActor _container;
    private String _endLine = "\n";
    private int _currentDepth;
    private Iterator _inPorts, _outPorts;
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
                Configuration configuration
                        = ((TableauFrame)parent).getConfiguration();

                NamedObj _container = (NamedObj)object.getContainer();
                TextEffigy codeEffigy = TextEffigy.newTextEffigy(
                        configuration.getDirectory(), generateCode());
                codeEffigy.setModified(true);
                configuration.createPrimaryTableau(codeEffigy);
            } catch (Exception ex) {
                throw new InternalErrorException(object, ex,
                "Cannot generate code. Perhaps outside Vergil?");
            }
        }
    }
}
