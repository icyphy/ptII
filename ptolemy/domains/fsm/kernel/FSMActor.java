/* An actor containing a finite state machine (FSM).

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Yellow (liuxj@eecs.berkeley.edu)
@AcceptedRating Yellow (kienhuis@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.IODependence;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.Typeable;
import ptolemy.domains.sdf.kernel.SDFScheduler;
import ptolemy.graph.Inequality;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.util.Hashtable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// FSMActor
/**
An FSMActor contains a set of states and transitions. A transition has
a guard expression and a trigger expression. A transition is enabled and
can be taken when its guard is true. A transition is triggered and must be
taken when its trigger is true. A transition can contain a set of actions.
<p>
When an FSMActor is fired, the outgoing transitions of the current state
are examined. An IllegalActionException is thrown if there is more than one
enabled transition. If there is exactly one enabled transition then it is
chosen and the choice actions contained by the transition are executed.
An FSMActor does not change state during successive firings in one iteration
in order to support domains that iterate to a fixed point. When the FSMActor
is postfired, the chosen transition of the latest firing of the actor is
committed. The commit actions contained by the transition are executed and
the current state of the actor is set to the destination state of the
transition.
<p>
An FSMActor enters its initial state during initialization. The name of the
initial state is specified by the <i>initialStateName</i> string attribute.
<p>
An FSMActor contains a set of variables for the input ports that can be
referenced in the guard and trigger expressions of transitions. If an input
port is a single port, three variables are created: one is an input status
variable with name "<i>portName</i>_isPresent"; the second is input value 
variable with name "<i>portName</i>"; the third is input array variable
with name "<i>portName</i>Array".
The input status variable always contains 
a BooleanToken. When this actor is fired, the status variable is set to true
if the port has token(s), false otherwise. The input value variable always
contains the latest token received from the port. The input array variable
contains an ArrayToken, which contains an array of tokens if the port rate 
is greater than one. By default, its last token is the latest token, and 
hence is the same token in input value variable.
If the given port is a multiport, a status variable, a value variable and
a array variable are created for each channel. The status variable is named
"<i>portName</i>_<i>channelIndex</i>_isPresent". The value variable is named
"<i>portName</i>_<i>channelIndex</i>". The array variable is named
"<i>portName</i>_<i>channelIndex</i>Array".
<p>
An FSMActor can be used in a modal model to represent the mode control logic.
A state can have a TypedActor refinement. A transition in an FSMActor can be
preemptive or non-preemptive. When a preemptive transition is chosen, the
refinement of its source state is not fired. A non-preemptive transition can
only be chosen after the refinement of its source state is fired.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 0.4
@see State
@see Transition
@see Action
@see FSMDirector
*/
public class FSMActor extends CompositeEntity 
    implements TypedActor, ExplicitChangeContext {

    /** Construct an FSMActor in the default workspace with an empty string
     *  as its name. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     */
    public FSMActor() {
        super();
        _init();
    }

    /** Construct an FSMActor in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public FSMActor(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Create an FSMActor in the specified container with the specified
     *  name. The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public FSMActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Attribute specifying the names of the final states of this
     *  actor. The names are separated by commas. Space characters
     *  at the beginning, the end, and around commas are ignored.
     *  Space characters within a name are preserved.
     *  For example, the value " final, my state 5" designates
     *  states "final" and "my state 5" as final.
     */
    public StringAttribute finalStateNames = null;

    /** Attribute specifying the name of the initial state of this
     *  actor.
     */
    public StringAttribute initialStateName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>initialStateName</i> attribute, record the change but do
     *  not check whether this actor contains a state with the specified
     *  name.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initialStateName) {
            _initialStateVersion = -1;
        } else if (attribute == finalStateNames) {
            _checkFinalStates(finalStateNames.getExpression());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new actor.
     *  @param workspace The workspace for the new actor.
     *  @return A new FSMActor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        FSMActor newObject = (FSMActor)super.clone(workspace);
        newObject._inputPortsVersion = -1;
        newObject._outputPortsVersion = -1;
        newObject._connectionMapsVersion = -1;
        newObject._connectionMaps = null;
        newObject._initialStateVersion = -1;
        newObject._inputVariableMap = new HashMap();
        newObject._inputVariableVersion = -1;
        return newObject;
    }

    /** React to notification that the links to the specified port have
     *  been altered.  This method overrides the base class so that if
     *  the specified port is an input port, it deletes any previously
     *  created shadow variables, and then recreates them.
     *  @param port The port to which connections have changed.
     */
    public void connectionsChanged(Port port) {
        IOPort castPort = (IOPort)port;
        if (castPort.isInput()) {
            _removeInputVariables(castPort);
            try {
                _createInputVariables(castPort);
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(ex);
            }
        }
    }

    /** Return the current state of this actor.
     *  @return The current state of this actor.
     */
    public State currentState() {
        return _currentState;
    }

    /** Set the values of input variables. Choose the enabled transition
     *  among the outgoing transitions of the current state. Throw an
     *  exception if there is more than one transition enabled.
     *  Otherwise, execute the choice actions contained by the chosen
     *  transition.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    public void fire() throws IllegalActionException {
        _setInputVariables();
        List trList = _currentState.outgoingPort.linkedRelationList();
        _chooseTransition(trList);
    }

    /** Return the director responsible for the execution of this actor.
     *  In this class, this is always the executive director.
     *  Return null if either there is no container or the container has no
     *  director.
     *  @return The director that invokes this actor.
     */
    public Director getDirector() {
        CompositeEntity container = (CompositeEntity)getContainer();
        if (container instanceof CompositeActor) {
            return ((CompositeActor)container).getDirector();
        }
        return null;
    }

    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return getDirector();
    }

    /** Return the initial state of this actor. The name of the initial
     *  state is specified by the <i>initialStateName</i> attribute. An
     *  exception is thrown if this actor does not contain a state with
     *  the specified name.
     *  This method is read-synchronized on the workspace.
     *  @return The initial state of this actor.
     *  @exception IllegalActionException If this actor does not contain
     *   a state with the specified name.
     */
    public State getInitialState() throws IllegalActionException {
        if (_initialStateVersion == workspace().getVersion()) {
            return _initialState;
        }
        try {
            workspace().getReadAccess();
            String name = initialStateName.getExpression();
            if (name == null || name.trim().equals("")) {
                throw new IllegalActionException(this,
                        "No initial state has been specified.");
            }
            State st = (State)getEntity(name);
            if (st == null) {
                throw new IllegalActionException(this, "Cannot find "
                        + "initial state with name \"" + name
                        + "\".");
            }
            _initialState = st;
            _initialStateVersion = workspace().getVersion();
            return _initialState;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the Manager responsible for execution of this actor,
     *  if there is one. Otherwise, return null.
     *  @return The manager.
     */
    public Manager getManager() {
        try {
            _workspace.getReadAccess();
            CompositeEntity container = (CompositeEntity)getContainer();
            if (container instanceof CompositeActor) {
                return ((CompositeActor)container).getManager();
            }
            return null;
        } finally {
            _workspace.doneReading();
        }
    }

    /** Return a list of variables that this entity modifies.  The
     * variables are assumed to have a change context of the given
     * entity.
     * @return A list of variables.
     */
    public List getModifiedVariables() throws IllegalActionException {
        List list = new LinkedList();
        // Collect assignments from FSM transitions
        for (Iterator states = entityList().iterator();
             states.hasNext();) {
            State state = (State)states.next();
            for (Iterator transitions =
                     state.outgoingPort.linkedRelationList().iterator();
                 transitions.hasNext();) {
                Transition transition = (Transition)transitions.next();
                for (Iterator actions =
                         transition.choiceActionList().iterator();
                     actions.hasNext();) {
                    AbstractActionsAttribute action =
                        (AbstractActionsAttribute)actions.next();
                    for (Iterator names = 
                             action.getDestinationNameList().iterator();
                         names.hasNext();) {
                        String name = (String)names.next();
                        NamedObj object = action.getDestination(name);
                        if (object instanceof Variable) {
                            list.add(object);
                        }
                    }
                }
                for (Iterator actions =
                         transition.commitActionList().iterator();
                     actions.hasNext();) {
                    AbstractActionsAttribute action =
                        (AbstractActionsAttribute)actions.next();
                    
                    for (Iterator names = 
                             action.getDestinationNameList().iterator();
                         names.hasNext();) {
                        String name = (String)names.next();
                        NamedObj object = action.getDestination(name);
                        if (object instanceof Variable) {
                            list.add(object);
                        }
                    }
                }
            }
        }      
        return list;
    }

    /** Initialize this actor.  Goto initial state.
     *  @exception IllegalActionException If a derived class throws it.
     */
    public void initialize() throws IllegalActionException {
        //System.out.println(this.getName() + "reset to initial state");
        reset();
    }

    /** Return a list of the input ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of input IOPort objects.
     */
    public List inputPortList() {
        if (_inputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                // Update the cache.
                LinkedList inPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while (ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if (p.isInput()) {
                        inPorts.add(p);
                    }
                }
                _cachedInputPorts = inPorts;
                _inputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedInputPorts;
    }

    /** Return true.
     *  @return True.
     */
    public boolean isOpaque() {
        return true;
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED. If stop() is called while
     *  this is executing, then cease executing and return STOP_ITERATING.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException {
        int n = 0;
        while (n++ < count && !_stopRequested) {
            if (prefire()) {
                fire();
                if (!postfire()) return STOP_ITERATING;
            } else {
                return NOT_READY;
            }
        }
        if (_stopRequested) {
            return Executable.STOP_ITERATING;
        } else {
            return Executable.COMPLETED;
        }
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
            //TypedIOPort p = new TypedIOPort(this, name);
            //return p;
            return new TypedIOPort(this, name);
        } catch (IllegalActionException ex) {
            // This exception should not occur.
            throw new InternalErrorException(
                    "TypedAtomicActor.newPort: Internal error: " +
                    ex.getMessage());
        } finally {
            _workspace.doneWriting();
        }
    }

    /** Return a new receiver obtained from the director.
     *  @exception IllegalActionException If there is no director.
     *  @return A new object implementing the Receiver interface.
     */
    public Receiver newReceiver() throws IllegalActionException {
        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }
        return director.newReceiver();
    }

    /** Create a new instance of Transition with the specified name in
     *  this actor, and return it.
     *  This method is write-synchronized on the workspace.
     *  @param name The name of the new transition.
     *  @return A transition with the given name.
     *  @exception IllegalActionException If the name argument is null.
     *  @exception NameDuplicationException If name collides with that
     *   of a transition already in this actor.
     */
    public ComponentRelation newRelation(String name)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            //Director director = getDirector();
            Transition tr = new Transition(this, name);
            if (_HDFFSMActor){
                (tr.preemptive).setVisibility(Settable.NONE);
            }
            return tr;
        } finally {
            workspace().doneWriting();
        }
    }

    /** Return a list of the output ports.
     *  This method is read-synchronized on the workspace.
     *  @return A list of output IOPort objects.
     */
    public List outputPortList() {
        if (_outputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                _cachedOutputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while (ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if ( p.isOutput()) {
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

    /** Execute actions on the last chosen transition. Change state
     *  to the destination state of the last chosen transition.
     *  @return True, unless stop() has been called, in which case, false.
     *  @exception IllegalActionException If any action throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _commitLastChosenTransition();
        return !_reachedFinalState && !_stopRequested;
    }

    /** Return true.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        _lastChosenTransition = null;
        return true;
    }

    /** Create receivers and input variables for the input ports of
     *  this actor, and validate attributes of this actor, and
     *  attributes of the ports of this actor. Set current state to
     *  the initial state. Throw an exception if this actor does not
     *  contain a state with name specified by the
     *  <i>initialStateName</i> attribute.
     *  @exception IllegalActionException If this actor does not contain a
     *   state with name specified by the <i>initialStateName</i> attribute.
     */
    public void preinitialize() throws IllegalActionException {
        _stopRequested = false;
        _reachedFinalState = false;
        _createReceivers();
        _hdfArrays = new Hashtable();
        
// FIXME: Modification according to the following changes:
// Refactoring the IODependence class to two classes, one for atomic actor,
// the other for composite actor. 
// Change of the way to access IODependence attribute, which removes the
// local private variable _ioDependence.
    
//        try {
//            if (_ioDependence != null) {
//                _ioDependence.setContainer(null);
//            }
//            _ioDependence = new IODependence(this, "_IODependence");
//            //TODO:
//            // Analyze io depencence (conservative):
//            // For an output port O, and input port I, if there is an output
//            // action that writes to O, and either the corresponding guard or
//            // the output value expression depends on input from I, then O
//            // depends on I.
//        } catch (NameDuplicationException ex) {
//            throw new InternalErrorException(this, ex,
//                    "Unable to create IODepedence attribute.");
//        }

        // NOTE: We used to have a strategy of removing the input
        // variables and then recreating them here.  But this is a
        // fairly brute force approach, so we have moved this to the
        // connectionsChanged() method.
        // This has the advantage that mutations during execution
        // are supported.  EAL 5/21/02.
        //         Iterator inputPorts = inputPortList().iterator();
        //         while (inputPorts.hasNext()) {
        //             IOPort inPort = (IOPort)inputPorts.next();
        //              _removeInputVariables(inPort);
        //         }
        //         inputPorts.reset();
        //         while (inputPorts.hasNext()) {
        //             IOPort inPort = (IOPort)inputPorts.next();
        //              _createInputVariables(inPort);
        //         }

        // Note: reset() (gotoInitialState()) is called from
        // initialize() now (zk 2002/09/11)`
    }

    /** Reset current state to the initial state. Throw an
     *  IllegalActionException if this actor does not contain a state with
     *  name specified by the <i>initialStateName</i> attribute.
     *  @exception IllegalActionException If this actor does not contain a
     *   state with name specified by the <i>initialStateName</i> attribute.
     */
    public void reset() throws IllegalActionException {
        _gotoInitialState();
    }

    /** Set the HDFFSMActor flag.
     *  @param flag Indicator that whether the FSMActor is under a
     *  HDFFSMDirector.
     */
    public void setHDFFSMActor (boolean flag) {
        _HDFFSMActor = flag;
    }

    /** Set the total number of firings of the modal model in the
     *  current iteration.
     *  @param firings The number of firings of the modal model in
     *  the current iteration.
     */
    public void setFiringsPerIteration(int firings) {
        _firingsPerIteration = firings;
    }

    /** Set the number of firings so far in the current iteration.
     *  @param firings The number of firings that the modal model
     *  has fired in the current iteration. 
     */
    public void setFiringsSoFar(int firings) {
        _firingsSoFar = firings;
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible.  In this class, we set a flag indicating that
     *  this request has been made (the protected variable _stopRequested).
     *  This will result in postfire() returning false.
     */
    public void stop() {
        _stopRequested = true;
    }

    /** Do nothing.
     */
    public void stopFire() {
    }

    /** Call stop().
     */
    public void terminate() {
        stop();
    }

    /** Return the type constraints of this actor. The constraints
     *  have the form of a list of inequalities. This method first
     *  creates constraints such that the type of any input port that
     *  does not have its type declared must be less than or equal to
     *  the type of any output port that does not have its type
     *  declared. Type constraints from the contained Typeables
     *  (ports, variables, and parameters) are collected. In addition,
     *  type constraints from all the transitions are added. These
     *  constraints are determined by the guard and trigger expressions
     *  of transitions, and actions contained by the transitions.
     *  This method is read-synchronized on the workspace.
     *  @return A list of inequalities.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() {
        try {
            _workspace.getReadAccess();

            List result = new LinkedList();
            Iterator inPorts = inputPortList().iterator();
            while (inPorts.hasNext()) {
                TypedIOPort inPort = (TypedIOPort)inPorts.next();
                boolean isUndeclared = inPort.getTypeTerm().isSettable();
                if (isUndeclared) {
                    // inPort has undeclared type
                    Iterator outPorts = outputPortList().iterator();
                    while (outPorts.hasNext()) {
                        TypedIOPort outport = (TypedIOPort)outPorts.next();
                        isUndeclared = outport.getTypeTerm().isSettable();
                        if (isUndeclared && inPort != outport) {
                            // outport also has undeclared type
                            Inequality ineq = new Inequality(
                                    inPort.getTypeTerm(),
                                    outport.getTypeTerm());
                            result.add(ineq);
                        }
                    }
                }
            }

            // Collect constraints from contained Typeables.
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                Typeable port = (Typeable)ports.next();
                result.addAll(port.typeConstraintList());
            }

            Iterator attributes = attributeList(Typeable.class).iterator();
            while (attributes.hasNext()) {
                Typeable typeableAttribute = (Typeable)attributes.next();
                result.addAll(typeableAttribute.typeConstraintList());
            }

            // Collect constraints from all transitions.
            Iterator transitionRelations = relationList().iterator();
            while (transitionRelations.hasNext()) {
                Relation tr = (Relation)transitionRelations.next();
                attributes = tr.attributeList(Typeable.class).iterator();
                while (attributes.hasNext()) {
                    Typeable typeableAttribute = (Typeable)attributes.next();
                    result.addAll(typeableAttribute.typeConstraintList());
                }
            }

            return result;

        } finally {
            _workspace.doneReading();
        }
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
        if (_debugging) relation.addDebugListener(new StreamListener());
    }

    /** Return the enabled transition among the given list of transitions.
     *  Throw an exception if there is more than one transition enabled.
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    protected Transition _checkTransition(List transitionList)
            throws IllegalActionException {
        Transition result = null;
        Iterator transitionRelations = transitionList.iterator();
        while (transitionRelations.hasNext() && !_stopRequested) {
            Transition transition = (Transition) transitionRelations.next();
            if (!transition.isEnabled()) {
                continue;
            }
            if (result != null) {
                throw new MultipleEnabledTransitionsException(currentState(),
                        "Multiple enabled transitions: "
                        + result.getName() + " and "
                        + transition.getName() + ".");
            }
            else {
                result = transition;
            }
        }
        return result;
    }

    /** Return the enabled transition among the given list of transitions.
     *  Execute the choice actions contained by the transition.
     *  Throw an exception if there is more than one transition enabled.
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled.
     */
    protected Transition _chooseTransition(List transitionList)
            throws IllegalActionException {
        Transition result = _checkTransition(transitionList);

        if (result != null) {
            if (_debugging) _debug("Enabled transition: "+ result.getFullName());
            Iterator actions = result.choiceActionList().iterator();
            while (actions.hasNext()) {
                Action action = (Action)actions.next();
                action.execute();
            }
        }

        _lastChosenTransition = result;
        return result;
    }

    /** Execute all commit actions contained by the transition chosen
     *  during the last call to _chooseTransition(). Change current state
     *  to the destination state of the transition. Reset the refinement
     *  of the destination state if the <i>reset</i> parameter of the
     *  transition is true.
     *  @exception IllegalActionException If any commit action throws it,
     *   or the last chosen transition does not have a destination state.
     */
    protected void _commitLastChosenTransition()
            throws IllegalActionException {
        if (_lastChosenTransition == null) {
            return;
        }

        if (_debugging) {
            _debug("Commit transition ", _lastChosenTransition.getFullName());
        }

        Iterator actions = _lastChosenTransition.commitActionList().iterator();
        while (actions.hasNext() && !_stopRequested) {
            Action action = (Action)actions.next();
            action.execute();
        }
        if (_lastChosenTransition.destinationState() == null) {
            throw new IllegalActionException(this, _lastChosenTransition,
                    "The transition is enabled but does not have a "
                    + "destination state.");
        }
        _currentState = _lastChosenTransition.destinationState();
        if (_finalStateNames != null
                && _finalStateNames.contains(_currentState.getName())) {
            _reachedFinalState = true;
        }
        if (_debugging) {
            _debug(new StateEvent(this, _currentState));
        }
        BooleanToken resetToken =
            (BooleanToken)_lastChosenTransition.reset.getToken();
        if (resetToken.booleanValue()) {
            Actor[] actors = _currentState.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_debugging)
                        _debug(getFullName()+" initialize refinement: "+
                                ((NamedObj)actors[i]).getName());
                    actors[i].initialize();
                }
            }
        }
        _setCurrentConnectionMap();
    }

    /** Create shadow variables for the port, if it is an input port,
     *  and otherwise do nothing. The shadow variables are contained
     *  by this actor and can be referenced in the guard and trigger
     *  expressions of transitions.  The shadow variables are lazy
     *  so that they are not evaluated until their values are needed,
     *  so the guard and trigger transitions should also be lazy.
     *  <p>
     *  If the given port is not a multiport, but is connected to
     *  something, then three variables are created:
     *  one is input status variable with name "<i>portName</i>_isPresent";
     *  the second is input value variable with name "<i>portName</i>";
     *  The third is input array variable with name "<i>portName</i>Array".
     *  The input status variable always contains a BooleanToken. When this
     *  actor is fired, the status variable is set to <i>true</i> if the port
     *  has a token, and to <i>false</i> otherwise. The input value variable
     *  always contains the latest token received from the port. The input
     *  array variable contains an ArrayToken, which contains an array of
     *  tokens when the port rate is greater than one. By default, its last
     *  token is the latest token, and  hence is the same token in input 
     *  value variable.
     *  <p>
     *  If the given port is a multiport, a status variable, a value
     *  variable, and an array variable are created for each channel. 
     *  The status variable is named 
     *  "<i>portName</i>_<i>channelIndex</i>_isPresent".
     *  The value variable is named "<i>portName</i>_<i>channelIndex</i>".
     *  The array variable is named "<i>portName</i>_<i>channelIndex</i>Array".
     *  <p>
     *  If a variable to be created has the same name as an attribute
     *  already contained by this actor, that attribute will be removed
     *  from this actor by setting its container to null.
     *  @param port The port for which to create a shadow variable.
     *  @exception IllegalActionException If the port is not contained
     *   by this FSMActor.
     */
    protected void _createInputVariables(IOPort port)
            throws IllegalActionException {
        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot create input variables for port "
                    + "not contained by this FSMActor.");
        }
        if (!port.isInput()) {
            return;
        }
        // If there are input variables already created for the port,
        // remove them.
        if (_inputVariableMap.get(port) != null) {
            _removeInputVariables(port);
        }
        int width = port.getWidth();
        if (width == 0) {
            return;
        }
        
        // Array in which to store the shadow variables.
        //Variable[][] shadowVariables = new Variable[width][2];
        Variable[][] shadowVariables = new Variable[width][3];
        
        String portName = port.getName();
        for (int channelIndex = 0; channelIndex < width; ++channelIndex) {
            // NOTE: The following check results in a bug because the
            // _stopRequested flag remains true after a model execution
            // has halted.  Hence, after a run, changes to connections
            // fail to result in variables being created.  EAL 7/22/02.
            // if (_stopRequested) break;
            String shadowName = null;
            // NOTE: This used to add the channel index only if the width
            // was greater than 1.  This doesn't match the documentation,
            // and seems inconsistent. So I changed it.  EAL 5/23/02
            if (port.isMultiport()) {
                shadowName = portName + "_" + channelIndex;
            } else {
                shadowName = portName;
            }
            String predicateName = shadowName + "_isPresent";
            Attribute previousAttribute = getAttribute(predicateName);
            try {
                if (previousAttribute != null) {
                    previousAttribute.setContainer(null);
                }
                shadowVariables[channelIndex][0]
                    = new Variable(this, predicateName);
                // Make the variable lazy since it will often have
                // an expression that cannot be evaluated.
                shadowVariables[channelIndex][0].setLazy(true);
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(getName() + ": "
                        + "Error creating shadow variables for port "
                        + portName + ".\n"
                        + ex.getMessage());
            }
            previousAttribute = getAttribute(shadowName);
            try {
                if (previousAttribute != null) {
                    previousAttribute.setContainer(null);
                }
                shadowVariables[channelIndex][1]
                    = new Variable(this, shadowName);
                // Make the variable lazy since it will often have
                // an expression that cannot be evaluated.
                shadowVariables[channelIndex][1].setLazy(true);
                
            } catch (NameDuplicationException ex) {
                throw new InvalidStateException(this,
                        "Error creating input variables for port.\n"
                        + ex.getMessage());
            }
            String shadowArrayName = shadowName + "Array";
            previousAttribute = getAttribute(shadowArrayName);
            try {
                if (previousAttribute != null) {
                    previousAttribute.setContainer(null);
                }
                shadowVariables[channelIndex][2]
                    = new Variable(this, shadowArrayName);
                // Make the variable lazy since it will often have
                // an expression that cannot be evaluated.
                shadowVariables[channelIndex][2].setLazy(true);
            } catch (NameDuplicationException ex) {
                throw new InvalidStateException(this,
                    "Error creating input variables for port.\n"
                    + ex.getMessage());
            }
            
        }
        _inputVariableMap.put(port, shadowVariables);
        _inputVariableVersion = _workspace.getVersion();
    }
    
    /** Return true if the channel of the port is connected to an output
     *  port of the refinement of current state. If the current state
     *  does not have refinement, return false.
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @return True if the channel of the port is connected to an output
     *   port of the refinement of current state.
     *  @exception IllegalActionException If the refinement specified for
     *   one of the states is not valid.
     */
    protected boolean _isRefinementOutput(IOPort port, int channel)
            throws IllegalActionException {
        TypedActor[] refinements = _currentState.getRefinement();
        if (refinements == null || refinements.length == 0) {
            return false;
        }
        if (_connectionMapsVersion != workspace().getVersion()) {
            _setCurrentConnectionMap();
        }
        boolean[] flags = (boolean[])_currentConnectionMap.get(port);
        return flags[channel];
    }

    /** Remove any shadow variables that have been created for the
     *  specified port.  Note that this will not normally trigger
     *  expression evaluation errors in dependent variables because
     *  those are lazy.
     *  @see #_createInputVariables(IOPort port)
     *  @param port The port to remove shadow variables for.
     */
    protected void _removeInputVariables(IOPort port) {
        Variable[][] shadowVariables =
            (Variable[][])_inputVariableMap.get(port);
        if (shadowVariables == null) {
            return;
        }
        for (int index = 0; index < shadowVariables.length; ++index) {
            try {
                Variable v = shadowVariables[index][0];
                if (v != null) {
                    v.setContainer(null);
                }
                v = shadowVariables[index][1];
                if (v != null) {
                    v.setContainer(null);
                }
                v =  shadowVariables[index][2];
                if (v!= null) {
                    v.setContainer(null);
                }
            } catch (NameDuplicationException ex) {
                throw new InternalErrorException(getName() + ": "
                        + "Error removing input variables for port "
                        + port.getName() + ".\n"
                        + ex.getMessage());
            } catch (IllegalActionException ex) {
                // Shouldn't occur because dependent variables are lazy.
                throw new InternalErrorException(getName() + ": "
                        + "Error removing input variables for port "
                        + port.getName() + ".\n"
                        + ex.getMessage());
            }
        }
    }

    /*  Set the map from input ports to boolean flags indicating whether a
     *  channel is connected to an output port of the refinement of the
     *  current state.
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    protected void _setCurrentConnectionMap() throws IllegalActionException {
        if (_connectionMapsVersion != workspace().getVersion()) {
            _buildConnectionMaps();
        }
        _currentConnectionMap = (Map)_connectionMaps.get(_currentState);
    }

    /** Set the value of the shadow variables for input ports of this actor.
     *  @exception IllegalActionException If a shadow variable cannot take
     *   the token read from its corresponding channel (should not occur).
     */
    protected void _setInputVariables() throws IllegalActionException {
        // If the workspace version is different from when the last input
        // variables were created, possibly due to changing the name of a
        // port, update the input variables to reflect the name change.
        if (_workspace.getVersion() != _inputVariableVersion) {
            _updateInputVariables();
        }
        CompositeActor container = (CompositeActor)getContainer();
        Director director = container.getDirector();
        //if (director instanceof HDFFSMDirector) {
        //    _firingsSoFar = ((HDFFSMDirector)director).getFiringsSoFar();
        //    _firingsPerScheduleIteration = 
        //        ((HDFFSMDirector)director).getFiringsPerIteration();
        //}
        Iterator inPorts = inputPortList().iterator();
        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort)inPorts.next();
            int width = p.getWidth();
            for (int channel = 0; channel < width; ++channel) {
                _setInputVariables(p, channel);
            }
        }
    }

    /** Set the value of the shadow variables for the channel of the port.
     *  If the specified port is not an input port, then do nothing.
     *  @see #_createInputVariables(IOPort port)
     *  @param port An input port of this actor.
     *  @param channel A channel of the input port.
     *  @exception IllegalActionException If the port is not contained by
     *   this actor, or if the shadow variable cannot take the token
     *   read from the channel (should not occur).
     */
    protected void _setInputVariables(IOPort port, int channel)
            throws IllegalActionException {
        
        if (port.getContainer() != this) {
            throw new IllegalActionException(this, port,
                    "Cannot set input variables for port "
                    + "not contained by this FSMActor.");
        }
        if (!port.isInput()) {
            return;
        }
        int width = port.getWidth();
        Variable[][] shadowVariables
            = (Variable[][])_inputVariableMap.get(port);
        if (shadowVariables == null) {
            throw new InternalErrorException(getName() + ": "
                    + "Cannot find input variables for port "
                    + port.getName() + ".\n");
        }
        if (port.isKnown(channel)) {
            int portRate = SDFScheduler.getTokenConsumptionRate(port);
            if (_debug_info) {
                System.out.println(port.getFullName() + " port rate = " + portRate);
            }
            if (_firingsSoFar == 0 && channel == 0) {
                Token[][] a_of_p = new Token[width][portRate * _firingsPerIteration];
                _hdfArrays.put(port, a_of_p);
            }
            int index = portRate * _firingsSoFar;
            // Update the value variable if there is/are token(s) in the channel.
            // FIXME: What if there are not enough tokens?
            // In HDF(SDF) this shouldn't happen.
            int flag = 0;
            while (port.hasToken(channel)) {
                Token token = port.get(channel);
                if (_debugging) {
                    _debug("---", port.getName(),"("+channel+
                    ") has ", token.toString());
                }
                flag ++;
                if (index < portRate * (_firingsSoFar + 1)) {
                    // Set the value of tokens here.
                    Token[][] a_of_p = (Token[][])_hdfArrays.get(port);
                    a_of_p[channel][index] = token;
                    if (_debug_info) {
                        System.out.println("hdfArray index = " + index + 
                            " value = " + a_of_p[channel][index].toString());
                    }
                    index ++;
                }
            }
            if (_debug_info) {
                System.out.println("total tokens available at port: "
                    + port.getFullName() + "  "+ flag);
            }

            // The "portName_isPresent" is true only if there are 
            // enough tokens. FIXME.
            if (index == portRate * _firingsPerIteration && index > 0) {
                Token[][] a_of_p = (Token[][])_hdfArrays.get(port);
                shadowVariables[channel][0].setToken(BooleanToken.TRUE);
                shadowVariables[channel][1].setToken(a_of_p[channel][index - 1]);
                shadowVariables[channel][2].setToken(new ArrayToken(a_of_p[channel]));
            } else {
                shadowVariables[channel][0].setToken(BooleanToken.FALSE);
                if (_debugging) {
                    _debug("---", port.getName(), "("+channel+
                        ") has no token.");
                }
            }
        } else {
            shadowVariables[channel][0].setUnknown(true);
            shadowVariables[channel][1].setUnknown(true);
            shadowVariables[channel][2].setUnknown(true);
        }
    }

    /** Set the input variables for channels that are connected to an
     *  output port of the refinement of current state.
     *  @exception IllegalActionException If a value variable cannot take
     *   the token read from its corresponding channel.
     */
    protected void _setInputsFromRefinement()
            throws IllegalActionException {
        Iterator inPorts = inputPortList().iterator();
        while (inPorts.hasNext() && !_stopRequested) {
            IOPort p = (IOPort)inPorts.next();
            int width = p.getWidth();
            for (int channel = 0; channel < width; ++channel) {
                if (_isRefinementOutput(p, channel)) {
                    _setInputVariables(p, channel);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // Current state.
    protected State _currentState = null;

    // A map from ports to corresponding input variables.
    protected Map _inputVariableMap = new HashMap();

    // The last chosen transition.
    protected Transition _lastChosenTransition = null;

    /** Indicator that a stop has been requested by a call to stop(). */
    protected boolean _stopRequested = false;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Build for each state a map from input ports to boolean flags
     *  indicating whether a channel is connected to an output port
     *  of the refinement of the state.
     *  This method is read-synchronized on the workspace.
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid.
     */
    private void _buildConnectionMaps() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            if (_connectionMaps == null) {
                _connectionMaps = new HashMap();
            } else {
                // Remove any existing maps.
                _connectionMaps.clear();
            }
            // Create a map for each state.
            Iterator states = entityList().iterator();
            State state = null;
            while (states.hasNext()) {
                state = (State)states.next();
                Map stateMap = new HashMap();
                TypedActor[] actors = state.getRefinement();
                // Determine the boolean flags for each input port.
                Iterator inPorts = inputPortList().iterator();
                while (inPorts.hasNext()) {
                    IOPort inPort = (IOPort)inPorts.next();
                    boolean[] flags = new boolean[inPort.getWidth()];
                    if (actors == null || actors.length == 0) {
                        java.util.Arrays.fill(flags, false);
                        stateMap.put(inPort, flags);
                        continue;
                    }
                    Iterator relations =
                        inPort.linkedRelationList().iterator();
                    int channelIndex = 0;
                    while (relations.hasNext()) {
                        IORelation relation = (IORelation)relations.next();
                        boolean linked = false;
                        for (int i = 0; i < actors.length; ++i) {
                            Iterator outports =
                                actors[i].outputPortList().iterator();
                            while (outports.hasNext()) {
                                IOPort outport = (IOPort)outports.next();
                                linked = linked | outport.isLinked(relation);
                            }
                        }
                        for (int j = 0; j < relation.getWidth(); ++j) {
                            flags[channelIndex+j] = linked;
                        }
                        channelIndex += relation.getWidth();
                    }
                    stateMap.put(inPort, flags);
                }
                _connectionMaps.put(state, stateMap);
            }
            _connectionMapsVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    // Check that the comma-separated list of state names is valid.
    private void _checkFinalStates(String names)
            throws IllegalActionException {
        HashSet stateNames = new HashSet();
        StringTokenizer nameTokens =
            new StringTokenizer(names, ",");
        while (nameTokens.hasMoreElements()) {
            String name = (String)nameTokens.nextElement();
            name = name.trim();
            stateNames.add(name);
        }
        _finalStateNames = stateNames;
    }

    /*  Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _createReceivers() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inPort = (IOPort)inputPorts.next();
            inPort.createReceivers();
        }
    }

    /*  Set the current state to the initial state. The name of the initial
     *  state is specified by the <i>initialStateName</i> attribute. An
     *  exception is thrown if this actor does not contain a state with
     *  the specified name.
     *  @exception IllegalActionException If this actor does not contain
     *   a state with the specified name.
     */
    private void _gotoInitialState() throws IllegalActionException {
        _currentState = getInitialState();
        if (_debugging) {
            _debug(new StateEvent(this, _currentState));
        }
        _setCurrentConnectionMap();
    }

    /*  Initialize the actor.
     *  @exception IllegalActionException If any port throws it.
     */
    private void _init() {
        // Create a more reasonable default icon.
        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-30\" y=\"-20\" width=\"60\" " +
                "height=\"40\" style=\"fill:red\"/>\n" +
                "<rect x=\"-28\" y=\"-18\" width=\"56\" " +
                "height=\"36\" style=\"fill:lightgrey\"/>\n" +
                "<ellipse cx=\"0\" cy=\"0\"" +
                " rx=\"15\" ry=\"10\"/>\n" +
                "<circle cx=\"-15\" cy=\"0\"" +
                " r=\"5\" style=\"fill:white\"/>\n" +
                "<circle cx=\"15\" cy=\"0\"" +
                " r=\"5\" style=\"fill:white\"/>\n" +
                "</svg>\n");
        try {
            initialStateName = new StringAttribute(this, "initialStateName");
            initialStateName.setExpression("");
            finalStateNames = new StringAttribute(this, "finalStateNames");
            finalStateNames.setExpression("");
            new Attribute(this, "_nonStrictMarker");
        } catch (KernelException ex) {
            // This should never happen.
            throw new InternalErrorException("Constructor error "
                    + ex.getMessage());
        }
        /*
        try {
            tokenHistorySize = 
                new Parameter(this, "tokenHistorySize", new IntToken(1));
        } catch (Exception e) {
            throw new InternalErrorException(
                "cannot create default tokenHistorySize parameter:\n" + e);
        }
        */
    }
    
    // Check whether the names of the input variables match the names of the
    // input ports. If a mismatch is found, create new input variables for
    // the port.
    private void _updateInputVariables() throws IllegalActionException {
        Iterator inPorts = inputPortList().iterator();
        while (inPorts.hasNext() && !_stopRequested) {
            IOPort port = (IOPort)inPorts.next();
            if (port.getWidth() == 0) {
                continue;
            } 
            Variable[][] shadowVariables
                    = (Variable[][])_inputVariableMap.get(port);
            if (shadowVariables == null) {
                _createInputVariables(port);
                continue;
            }
            // NOTE: this shadow name is the name of the input variable 
            // that stores the last token read from channel 0 of the port.
            // See _createInputVariables().
            String shadowName = port.getName();
            if (port.isMultiport()) {
                shadowName += "_" + 0;
            }
            if (shadowVariables[0][0].getName() != shadowName) {
                _createInputVariables(port);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //private Parameter _portRate;

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient LinkedList _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient LinkedList _cachedOutputPorts;

    // Stores for each state a map from input ports to boolean flags
    // indicating whether a channel is connected to an output port
    // of the refinement of the state.
    private Map _connectionMaps = null;

    // Version of the connection maps.
    private long _connectionMapsVersion = -1;

    // The map from input ports to boolean flags indicating whether a
    // channel is connected to an output port of the refinement of the
    // current state.
    private Map _currentConnectionMap = null;

    // Cached reference to the initial state.
    private State _initialState = null;

    // Version of the reference to the initial state.
    private long _initialStateVersion = -1;
    
    // Version of the workspace when the last input variables were created.
    private long _inputVariableVersion = -1;

    // The set of names of final states.
    private HashSet _finalStateNames;

    // True if the current state is a final state.
    private boolean _reachedFinalState;
    
    // Set to true to enable debugging.
    private boolean _debug_info = false;
    //private boolean _debug_info = true;

    // A flag indicating whether the controller 
    // is under an HDFFSMDirector.
    private boolean _HDFFSMActor = false;
    
    // Firing counts for HDF/SDF. 
    // For other domains, _firingsPerScheduleIteration will always be 1
    // and _firingsSoFar will always be 0.
    private int _firingsSoFar = 0;
    private int _firingsPerIteration = 1;
    
    // Hashtable to save an array of tokens for each port.
    // This is used in HDF when multiple tokens are consumed
    // by the FSMActor in one iteration.
    private Hashtable _hdfArrays;
    
    // The IODependence attribute of this actor.
    private IODependence _ioDependence;

}
