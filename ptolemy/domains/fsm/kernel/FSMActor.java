/* An actor containing a finite state machine (FSM).

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.TypedActor;

import ptolemy.kernel.Port;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// FSMActor
/**
An FSMActor contains a set of states and transitions. A transition has
a guard expression and a trigger expression. A transition is enabled and 
can be taken when its guard is true. A transition is triggered and must be 
taken when its trigger is true. A transition can contain a set of actions. 
The actions are executed when the FSMActor is fired or postfired and the 
transition is taken.
<p>
An FSMActor enters its initial state during initialization. There are two
ways to set the initial state: one is by calling setInitialState(), the 
other is by putting the name of the initial state in a StringToken, and 
setting the initialState parameter with this token. The first way has 
precedence.
<p>
For each input port, an FSMActor creates two variables which can be referenced
in guard and trigger expressions: a status variable with name portname$S, and 
a value variable with name portname$V. When an FSMActor is fired, it will set
portname$S = port.hasToken(); portname$V = portname$S ? port.getToken() : null.
<p>
An FSMActor does not change state during successive firings in one iteration.
In each firing, outgoing transitions from the current state are examined, and
actions on the enabled transition are executed. An FSMActor only changes state
in postfire(), where the new state is the destination state of the transition
enabled in the last firing.
<p>
An FSMActor can be used in a modal model to represent the mode control logic.
A state can have a TypedActor refinement. A transition in an FSMActor can be 
preemptive or non-preemptive. When a preemptive transition is taken, the 
refinement of its source state is not fired. A non-preemptive transition is 
only taken after the refinement of its source state is fired.

@author Xiaojun Liu
@version $Id$
@see State
@see Transition
@see Action
@see FSMDirector
*/
public class FSMActor extends CompositeEntity implements TypedActor {

    /** Create an FSMActor in the specified container with the specified
     *  name. The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public FSMActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        initialState = new Parameter(this, "InitialState");
        _inputVariableContainer = 
                new Attribute(this, "_InputVariableContainer");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the current refinement of this FSMActor.
     *  @return Current refinement.
     */
    public TypedActor currentRefinement() {
        return _currentState.getRefinement();
    }

    /** Return the current state of this FSMActor.
     *  @return Current state.
     */
    public State currentState() {
        return _currentState;
    }

    /** Set the values of input variables. Examine outgoing transitions
     *  from the current state. Throw an exception if there are more than
     *  one transitions enabled. Otherwise, execute the actions on the
     *  enabled transition.
     *  @exception IllegalActionException If there are more than one 
     *   transitions enabled.
     */
    public void fire() throws IllegalActionException {
        Iterator inports = inputPortList().iterator();
        while (inports.hasNext()) {
            TypedIOPort p = (TypedIOPort)inports.next();
            _setInputVariables(p);
        }
        _lastEnabledTransition = null;
        Transition tr1 = 
                _enabledTransition(currentState().preemptiveTransitionList());
        Transition tr2 = 
                _enabledTransition(currentState().nonpreemptiveTransitionList());
        if (tr1 == null) {
            _lastEnabledTransition = tr2;
        } else if (tr2 != null) {
            throw new IllegalActionException(this, currentState(),
                    "Multiple enabled transitions.");
        } else {
            _lastEnabledTransition = tr1;
        }
        if (_lastEnabledTransition != null) {
            _fireTransition(_lastEnabledTransition);
        }
    }

    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    public Director getDirector() {
        CompositeActor container = (CompositeActor)getContainer();
        if (container != null) {
            return container.getDirector();
        }
        return null;
    }

    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    public Manager getManager() {
	try {
	    _workspace.getReadAccess();
	    CompositeActor container = (CompositeActor)getContainer();
	    if (container != null) {
		return container.getManager();
	    }
	    return null;
	} finally {
	    _workspace.doneReading();
	}
    }

    /** Perform domain-specific initialization by calling the
     *  initialize(Actor) method of the director.
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        getDirector().initialize(this);
    }

    /** Return an enumeration of the input ports.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use inportList() instead;
     *  @return An enumeration of input TypedIOPort objects.
     */
    public Enumeration inputPorts() {
        return Collections.enumeration(inputPortList());
    }

    /** Return a list of the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of input TypedIOPort objects.
     */
    public List inputPortList() {
        if(_inputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                // Update the cache.
                LinkedList inports = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if (p.isInput()) {
                        inports.add(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedInputPorts;
    }

    /** Create a new TypedIOPort with the specified name.
     *  The container of the port is set to this actor.
     *  This method is write-synchronized on the workspace.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.
     */
    public Port newPort(String name) throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();
            TypedIOPort port = new TypedIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " +
		    ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return a new receiver of a type compatible with the director.
     *
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }
        return dir.newReceiver();
    }

    /** Return an enumeration of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @deprecated Use outputPortList() instead.
     *  @return An enumeration of output TypedIOPort objects.
     */
    public Enumeration outputPorts() {
        return Collections.enumeration(outputPortList());
    }

    /** Return a list of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of output TypedIOPort objects.
     */
    public List outputPortList() {
        if(_outputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                _cachedOutputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if( p.isOutput()) {
                        _cachedOutputPorts.add(p);
                    }
                }
                _outputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedOutputPorts;
    }

    /** Execute actions on the last enabled transition. Change state
     *  to the destination state of the last enabled transition.
     *
     *  @return True.
     *  @exception IllegalActionException If any action throws it.
     */
    public boolean postfire() throws IllegalActionException {
        if (_lastEnabledTransition != null) {
            _postfireTransition(_lastEnabledTransition);
        }
        return true;
    }

    /** Return true. 
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Create receivers and input variables. Initialize transitions.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void preinitialize() throws IllegalActionException {
        _createReceivers();
        Enumeration inports = inputPorts();
        while (inports.hasMoreElements()) {
            TypedIOPort p = (TypedIOPort)inports.nextElement();
            _createInputVariables(p);
        }
        Iterator trs = relationList().iterator();
        while (trs.hasNext()) {
            Transition tr = (Transition)trs.next();
            tr._initialize();
        }
        _gotoInitialState();
    }

    /** Reset state to initial state.
     *  @exception IllegalActionException If initial state is not set.
     */
    public void reset() throws IllegalActionException {
        _gotoInitialState();
    }

    /** Override the base class to ensure that the proposed container
     *  is an instance of TypedCompositeActor or null. If it is, call the
     *  base class setContainer() method. A null argument will remove
     *  the actor from its container.
     *
     *  @param entity The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this actor and container are not in the same workspace, or
     *   if the argument is not a TypedCompositeActor or null.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this actor.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof TypedCompositeActor) &&
                (container != null)) {
            throw new IllegalActionException(container, this,
                    "TypedAtomicActor can only be contained by instances of " +
                    "TypedCompositeActor.");
        }
        super.setContainer(container);
    }

    /** Do nothing.
     */
    public void stopFire() {
    }

    /** Try wrap up.
     */
    public void terminate() {
        try {
            wrapup();
        }
        catch (IllegalActionException e) {
            // Do not pass go, do not collect $200.  Most importantly,
            // just ignore everything and terminate.
        }
    }

    /** Return the type constraints of this actor.
     *  The constraints have the form of a list of inequalities.
     *  In this base class, the implementation of type constraints
     *  is that the type of any input port that does not have its type
     *  declared must be less than or equal to the type of any output port
     *  that does not have its type declared.
     *  In addition, this method also collects type constraints from the
     *  contained Typeables (ports, variables, and parameters).
     *  This method is read-synchronized on the workspace.
     *  @return a list of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList()  {
        /*try {
	    _workspace.getReadAccess();

	    List result = new LinkedList();
	    Enumeration inPorts = inputPorts();
	    while (inPorts.hasMoreElements()) {
	        TypedIOPort inport = (TypedIOPort)inPorts.nextElement();
		boolean isUndeclared = inport.getTypeTerm().isSettable();
		if (isUndeclared) {
		    // inport has undeclared type
		    Enumeration outPorts = outputPorts();
	    	    while (outPorts.hasMoreElements()) {
		    	TypedIOPort outport =
                            (TypedIOPort)outPorts.nextElement();

			isUndeclared = outport.getTypeTerm().isSettable();
		    	if (isUndeclared && inport != outport) {
			    // output also undeclared, not bi-directional port,
		            Inequality ineq = new Inequality(
                                    inport.getTypeTerm(),
                                    outport.getTypeTerm());
			    result.add(ineq);
			}
		    }
		}
	    }

	    // collect constraints from contained Typeables
	    Enumeration ports = getPorts();
	    while (ports.hasMoreElements()) {
		Typeable port = (Typeable)ports.nextElement();
		result.addAll(port.typeConstraintList());
	    }

	    Enumeration attrib = getAttributes();
	    while (attrib.hasMoreElements()) {
		Attribute att = (Attribute)attrib.nextElement();
		if (att instanceof Typeable) {
		    result.addAll(((Typeable)att).typeConstraintList());
		}
	    }

	    return result;

	} finally {
	    _workspace.doneReading();
	}*/
        return new LinkedList();
    }

    /** Return the type constraints of this actor.
     *  This method calls typeConstraintList() and convert the result into
     *  an enumeration.
     *  @return an enumeration of inequalities.
     *  @see ptolemy.graph.Inequality
     *  @deprecated Use typeConstraintList() instead.
     */
    public Enumeration typeConstraints()  {
	return Collections.enumeration(typeConstraintList());
    }

    /** Do nothing.  Derived classes override this method to define
     *  operations to be performed exactly once at the end of a complete
     *  execution of an application.  It typically closes
     *  files, displays final results, etc.
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Parameter containing name of initial state.
     */
    public Parameter initialState = null;	

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a state to this FSMActor. This overrides the base-class 
     *  method to make sure the argument is an instance of State.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @param entity State to contain.
     *  @exception IllegalActionException If the state has no name, or the
     *   action would result in a recursive containment structure, or the
     *   argument is not an instance of State.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the state list.
     */
    protected void _addEntity(ComponentEntity entity)
            throws IllegalActionException, NameDuplicationException {
        if (!(entity instanceof State)) {
            throw new IllegalActionException(this, entity,
                    "FSMActor can only contain entities that " +
                    "are instances of State.");
        }
        super._addEntity(entity);
    }

    /** Add a transition to this FSMActor. This method should not be used
     *  directly.  Call the setContainer() method of the transition instead.
     *  This method does not set the container of the transition to refer
     *  to this container. This method is <i>not</i> synchronized on the
     *  workspace, so the caller should be.
     *
     *  @param relation Transition to contain.
     *  @exception IllegalActionException If the transition has no name, or 
     *   is not an instance of Transition.
     *  @exception NameDuplicationException If the name collides with a name
     *   already on the contained transitions list.
     */
    protected void _addRelation(ComponentRelation relation)
            throws IllegalActionException, NameDuplicationException {
        if (!(relation instanceof Transition)) {
            throw new IllegalActionException(this, relation,
                    "FSMActor can only contain instances of Transition.");
        }
        super._addRelation(relation);
    }

    /** Create input variables for the port.
     *  @param port A port.
     *  @exception IllegalActionException If the port is not contained
     *   by this FSMActor.
     */
    protected void _createInputVariables(TypedIOPort port) 
            throws IllegalActionException {
        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot create input variables for port "
                    + "not contained by this FSMActor.");
        }
        Variable sVar = null;
        String vName = port.getName() + "$S";
        Attribute a = _inputVariableContainer.getAttribute(vName);
        try {
            if(a != null) {
                a.setContainer(null);
            }
            sVar = new Variable(_inputVariableContainer, vName);
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    _inputVariableContainer,
                    "Error creating input variables for port "
                    + port.getName());
        }
        Variable vVar = null;
        vName = port.getName() + "$V";
        a = _inputVariableContainer.getAttribute(vName);
        try {
            if(a != null) {
                a.setContainer(null);
            }
            vVar = new Variable(_inputVariableContainer, vName);
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    _inputVariableContainer,
                    "Error creating input variables for port "
                    + port.getName());
        }
        Variable[] pVars = new Variable[2];
        pVars[0] = sVar;
        pVars[1] = vVar;
        _inputVariableMap.put(port, pVars);
    }   

    /** Return null if none of the transitions in the list is enabled.
     *  Throw an exception if more than one transitions are enabled.
     *  Otherwise return the enabled transition.
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If more than one transitions 
     *   are enabled.
     */
    protected Transition _enabledTransition(List transitionList)
            throws IllegalActionException {
        Transition result = null;
        Iterator trs = transitionList.iterator();
        while (trs.hasNext()) {
            Transition tr = (Transition)trs.next();
            if (!tr.isEnabled()) {
                continue;
            } 
            if (result != null) {
                throw new IllegalActionException(this, currentState(),
                        "Multiple enabled transitions.");
            } else {
                result = tr;
            }
        }
        return result;
    }

    /** Execute all actions on the transition that are set to execute when
     *  fire.
     *  @param transition A transition.
     *  @exception IllegalActionException If any action throws it.
     */
    protected void _fireTransition(Transition transition) 
            throws IllegalActionException {
        Iterator actions = transition.actionList().iterator();
        while (actions.hasNext()) {
            Action action = (Action)actions.next();
            if (action.isExecuteWhenFire()) {
                action.execute();
            }
        }
    }

    /** Execute all actions on the transition that are set to execute when
     *  postfire. Change state to the destination state of the transition.
     *  @param transition A transition.
     *  @exception IllegalActionException If any action throws it.
     */
    protected void _postfireTransition(Transition transition) 
            throws IllegalActionException {
        Iterator actions = transition.actionList().iterator();
        while (actions.hasNext()) {
            Action action = (Action)actions.next();
            if (action.isExecuteWhenPostfire()) {
                action.execute();
            }
        }
        _currentState = transition.destinationState();
    }

    /** Set the input variables for the port.
     *  @param port A port.
     *  @exception IllegalActionException If the port is not contained by
     *   this FSMActor, or if the value variable cannot take the token read 
     *   from the port.
     */
    protected void _setInputVariables(TypedIOPort port) 
            throws IllegalActionException {
        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port, 
                    "Cannot set input variables for port "
                    + "not contained by this FSMActor.");
        }
        Variable[] pVars = (Variable[])_inputVariableMap.get(port);
        if (pVars == null) {
            throw new InvalidStateException(this, port,
                    "Cannot find input variables for port.");
        }
        boolean t = port.hasToken(0);
        Token tok = t ? BooleanToken.TRUE : BooleanToken.FALSE;
        pVars[0].setToken(tok);
        if (t == false) {
            pVars[1].setToken(null);
        } else {
            pVars[1].setToken(port.get(0));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // The attribute containing the input variables.
    protected Attribute _inputVariableContainer;

    // A map from ports to corresponding input variables.
    protected Map _inputVariableMap = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        Enumeration inputPorts = inputPorts();
        while (inputPorts.hasMoreElements()) {
            IOPort inport = (IOPort)inputPorts.nextElement();
            inport.createReceivers();
        }
    }

    /** Set the current state to initial state. There are two ways 
     *  to set the initial state: one is by calling setInitialState(), 
     *  the other is by putting the name of the initial state in a 
     *  StringToken, and setting the initialState parameter with this 
     *  token. The first way has precedence. An exception will be
     *  thrown if initial state is not set.
     *  @exception IllegalActionException If initial state is not set.
     */
    private void _gotoInitialState() throws IllegalActionException {
        if (_setInitialState != null) {
            _currentState = _setInitialState;
        }
        Token tok = initialState.getToken();
        if (tok == null) {
            throw new IllegalActionException(this,
                    "Initial state is not set.");
        }
        State st = (State)this.getEntity(tok.toString());
        if (st == null) {
            throw new IllegalActionException(this,
                    "Invalid setting of initial state: " 
                    + tok.toString());
        }
        _currentState = st;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient LinkedList _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient LinkedList _cachedOutputPorts;

    // Current state.
    private State _currentState = null;

    // Initial state set by calling setInitialState().
    private State _setInitialState = null;
    
    // The last enabled transition.
    private Transition _lastEnabledTransition = null;

}
