/* An FSMDirector governs the execution of a modal model.

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
@AcceptedRating Yellow (liuxj@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.InvariantViolationException;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.data.Token;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.ModelErrorHandler;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


//////////////////////////////////////////////////////////////////////////
//// FSMDirector
/**
An FSMDirector governs the execution of a modal model. A modal model is
a TypedCompositeActor with a FSMDirector as local director. The mode
control logic is captured by a mode controller, an instance of FSMActor
contained by the composite actor. Each state of the mode controller
represents a mode of operation and can be refined by a TypedActor contained
by the same composite actor.
<p>
When a modal model is fired, this director first transfers the input tokens
from the outside domain to the mode controller and the refinement of its
current state. The preemptive transitions from the current state of the mode
controller are examined. If there is more than one transition enabled, an
exception is thrown. If there is exactly one preemptive transition enabled
then it is chosen and the choice actions contained by the transition are
executed. The refinement of the current state is not fired. Any output token
produced by the mode controller is transferred to the outside domain. If no
preemptive transition is enabled, the refinement of the current state is
fired. The non-preemptive transitions from the current state of the mode
controller are examined. If there is more than one transition enabled, an
exception is thrown. If there is exactly one non-preemptive transition
enabled then it is chosen and the choice actions contained by the transition
are executed. Any output token produced by the mode controller or the
refinement is transferred to the outside domain.
<p>
The mode controller does not change state during successive firings in one
iteration in order to support outside domains that iterate to a fixed point.
When the modal model is postfired, the chosen transition of the latest firing
is committed. The commit actions contained by the transition are executed and
the current state of the mode controller is set to the destination state of
the transition.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 0.4
@see FSMActor
*/
public class FSMDirector extends Director
    implements ModelErrorHandler, ExplicitChangeContext {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public FSMDirector() {
        super();
        _createAttribute();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public FSMDirector(Workspace workspace) {
        super(workspace);
        _createAttribute();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public FSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _createAttribute();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Attribute specifying the name of the mode controller in the
     *  container of this director. This director must have a mode
     *  controller that has the same container as this director,
     *  otherwise an IllegalActionException will be thrown when action
     *  methods of this director are called.
     */
    public StringAttribute controllerName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute is
     *  the <i>controllerName</i> attribute, record the change but do not
     *  check whether there is an FSMActor with the specified name in the
     *  container of this director.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *   attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);
        if (attribute == controllerName) {
            _controllerVersion = -1;
        }
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new director.
     *  @param workspace The workspace for the new director.
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        FSMDirector newObject = (FSMDirector)super.clone(workspace);
        newObject._controllerVersion = -1;
        newObject._localReceiverMaps = new HashMap();
        newObject._localReceiverMapsVersion = -1;
        return newObject;
    }

    /** Set the values of input variables in the mode controller. Examine
     *  the preemptive outgoing transitions of its current state. Throw an
     *  exception if there is more than one transition enabled. If there
     *  is exactly one preemptive transition enabled then it is chosen and
     *  the choice actions contained by the transition are executed. The
     *  refinement of the current state of the mode controller is not fired.
     *  If no preemptive transition is enabled and the refinement is ready
     *  to fire in the current iteration, fire the refinement. The
     *  non-preemptive transitions from the current state of the mode
     *  controller are examined. If there is more than one transition
     *  enabled, an exception is thrown. If there is exactly one
     *  non-preemptive transition enabled then it is chosen and the choice
     *  actions contained by the transition are executed.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or there is no controller, or thrown by any
     *   choice action.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug(getFullName(), "fire at time: " + getCurrentTime());
        }
        FSMActor ctrl = getController();
        ctrl._setInputVariables();
        if (_debugging) {
            _debug(getFullName(), " find FSMActor " + ctrl.getName());
        }
        State st = ctrl.currentState();
        Transition tr =
            ctrl._chooseTransition(st.preemptiveTransitionList());

        if (tr != null) {
            Actor[] actors = tr.destinationState().getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested) break;
                    if (actors[i].prefire()) {
                        actors[i].fire();
                        actors[i].postfire();
                    }
                }
            }

            actors = tr.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested) break;
                    if (actors[i].prefire()) {
                        actors[i].fire();
                        actors[i].postfire();
                    }
                }
            }
            return;
        }

        Actor[] actors = st.getRefinement();
        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                if (_stopRequested) break;
                if (actors[i].prefire()) {
                    if (_debugging) {
                        _debug(getFullName(), " fire refinement",
                                ((ptolemy.kernel.util.NamedObj)
                                        actors[i]).getName());
                    }
                    actors[i].fire();
                    actors[i].postfire();
                }
            }
        }

        ctrl._setInputsFromRefinement();

        tr = ctrl._chooseTransition(st.nonpreemptiveTransitionList());

        if (tr != null) {
            actors = tr.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested) break;
                    if (actors[i].prefire()) {
                        if (_debugging) {
                            _debug(getFullName(),
                                    " fire transition refinement",
                                    ((ptolemy.kernel.util.NamedObj)
                                            actors[i]).getName());
                        }
                        actors[i].fire();
                        actors[i].postfire();
                    }
                }
                ctrl._setInputsFromRefinement();
                //execute the output actions
                Iterator actions = tr.choiceActionList().iterator();
                while (actions.hasNext()) {
                    Action action = (Action)actions.next();
                    action.execute();
                }
            }
        }
        return;
    }

    /** Schedule a firing of the given actor at the given time. This
     *  is delegated to the executive director by scheduling with it
     *  a firing of the container of this director at the given time.
     *  The actor should be either the mode controller or the refinement
     *  of one of its states. For both cases firing the actor happens
     *  when the container of this director is fired at the given time.
     *  In the second case, if the actor is not the refinement of the
     *  state of the mode controller when the execution reaches the
     *  given time, the firing is ignored.
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @exception IllegalActionException If thrown in scheduling
     *   a firing of the container of this director at the given
     *   time with the executive director.
     */
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        // FIXME: Changed by liuj, not yet reviewed.
        Nameable container = getContainer();
        if (container instanceof Actor) {
            Actor cont = (Actor)container;
            Director executiveDirector = cont.getExecutiveDirector();
            if (executiveDirector != null) {
                executiveDirector.fireAt(cont, time);
            } else {
                setCurrentTime(time);
            }
        }
    }

    /** Return the mode controller of this director. The name of the
     *  mode controller is specified by the <i>controllerName</i>
     *  attribute. The mode controller must have the same container as
     *  this director.
     *  This method is read-synchronized on the workspace.
     *  @return The mode controller of this director.
     *  @exception IllegalActionException If no controller is found.
     */
    public FSMActor getController() throws IllegalActionException {
        if (_controllerVersion == workspace().getVersion()) {
            return _controller;
        }
        try {
            workspace().getReadAccess();
            String name = controllerName.getExpression();
            if (name == null) {
                throw new IllegalActionException(this, "No name for mode "
                        + "controller is set.");
            }
            Nameable container = getContainer();
            if (!(container instanceof CompositeActor)) {
                throw new IllegalActionException(this, "No controller found.");
            }
            CompositeActor cont = (CompositeActor)container;
            Entity entity = cont.getEntity(name);
            if (entity == null) {
                throw new IllegalActionException(this, "No controller found "
                        + "with name " + name);
            }
            if (!(entity instanceof FSMActor)) {
                throw new IllegalActionException(this, entity,
                        "mode controller must be an instance of FSMActor.");
            }
            _controller = (FSMActor)entity;
            _controllerVersion = workspace().getVersion();
            return _controller;
        } finally {
            workspace().doneReading();
        }
    }

    /** 
     * Return the change context being made explicit.  In this case,
     * the change context returned is the composite actor controlled
     * by this director.
     */
    public Entity getContext() {
        return (Entity)getContainer();
    }

    /** Return a list of variables that are modified in a modal model.
     * The variables are assumed to have a change context of the
     * container of this director.  This class returns all variables
     * that are assigned in the actions of transitions.
     * @return A list of variables.
     */
    public List getModifiedVariables() throws IllegalActionException {
        List list = new LinkedList();
        // Collect assignments from FSM transitions
        for (Iterator states = getController().entityList().iterator();
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

    /** Return the next iteration time provided by the refinement of the
     *  current state of the mode controller. If the refinement does not
     *  provide this, return that given by the superclass.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
        try {
            Actor[] actors = getController().currentState().getRefinement();
            if (actors == null || actors.length == 0) {
                return super.getNextIterationTime();
            }
            double result = Double.MAX_VALUE;
            boolean givenByRefinement = false;
            for (int i = 0; i < actors.length; ++i) {
                if (actors[i].getDirector() != this) {
                    // The refinement has a local director.
                    result = Math.min(result,
                            actors[i].getDirector().getNextIterationTime());
                    givenByRefinement = true;
                }
            }
            if (givenByRefinement) {
                return result;
            } else {
                return super.getNextIterationTime();
            }
        } catch (IllegalActionException ex) {
            // No mode controller, return that given by the superclass.
        }

        return super.getNextIterationTime();
    }


    /** Handle a model error by checking if there is any enabled
     *  non-preemptive transitions. If there is some one, the model
     *  error is ignored. Otherwise, the model error (exception) will
     *  be passed to higher level in hierarchy.
     *
     *  @param context The context where the model error happens.
     *  @param exception An exception that represents the model error.
     *  @return True if the error has been handled, or nothing if the
     *  model error is thrown to higher level.
     *  @exception IllegalActionException The model error exception.
     */
    public boolean handleModelError(
            NamedObj context,
            IllegalActionException exception)
            throws IllegalActionException {

        // If the exception is a MultipleEnabledTransitionsException
        // exception, handle it by refining the step size.
        if (exception instanceof MultipleEnabledTransitionsException) {
            // FIXME: handle the multiple enabled transitions
            throw exception;
        }
        
        // If the exception is an InvariantViolationException
        // exception, check if any transition is enabled.
        if (exception instanceof InvariantViolationException) {
        
            FSMActor fsm = getController();
            fsm._setInputsFromRefinement();
            State st = fsm.currentState();
            Transition tr = fsm._chooseTransition(st.nonpreemptiveTransitionList());
   
            if (tr == null) {
                ModelErrorHandler container = getContainer();
                if (container != null) {
                    
                    //The following statement leads to dead loop
                    // because the container will call this method again.
                    //return container.handleModelError(context, exception);
    
                    throw exception;
                }
            }

            if (_debugging) {
                _debug("ModelError: " + exception.getMessage() 
                     + " is handled and discarded.");
            }
            return true;
        }

        // else delegate the exception to upper level.
        return false;
    }

    /** Initialize the mode controller and all the refinements. Set the
     *  current time to 0.0 or the time of the executive director.
     *  If the container is not an instance of CompositeActor, do nothing.
     *  This method should typically be invoked once per execution, after
     *  the preinitialization phase, but before any iteration.  It may be
     *  invoked in the middle of an execution, if reinitialization is
     *  desired.  Since type resolution has been completed and the
     *  current time is set, the initialize() method of a contained
     *  actor may produce output or schedule events.  If stop() is
     *  called during this methods execution, then stop initializing
     *  actors immediately.  This method is
     *  <i>not</i> synchronized on the workspace, so the caller should
     *  be.
     *
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _buildLocalReceiverMaps();
    }

    /** Return a receiver that is a one-place buffer. A token put into the
     *  receiver will override any token already in the receiver.
     *  @return A receiver that is a one-place buffer.
     */
    public Receiver newReceiver() {
        return new Mailbox() {
                public boolean hasRoom() {
                    return true;
                }

                public void put(Token token) {
                    try {
                        if (hasToken() == true) {
                            get();
                        }
                        super.put(token);
                    } catch (NoRoomException ex) {
                        throw new InternalErrorException("One-place buffer: "
                                + ex.getMessage());
                    } catch (NoTokenException ex) {
                        throw new InternalErrorException("One-place buffer: "
                                + ex.getMessage());
                    }
                }
            };
    }

    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire the refinement of the current state
     *  of the mode controller if it is ready to fire in the current
     *  iteration. Execute the commit actions contained by the last
     *  chosen transition of the mode controller and set its current
     *  state to the destination state of the transition.
     *  If this director is at the top level, it will return the
     *  postfire() value of the current refinement.
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If thrown by any commit action
     *   or there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        // FIXME: Changed by liuj, not yet reviewed.
        if (_debugging) {
            _debug(getFullName(),
                    "postfire called at time: " + getCurrentTime());
        }
        FSMActor controller = getController();
        boolean result = controller.postfire();
        _currentLocalReceiverMap =
            (Map)_localReceiverMaps.get(controller.currentState());
        return result && !_stopRequested;
    }

    /** Return true if the mode controller is ready to fire.
     *  If this model is not at the top level and the current
     *  time of this director lags behind that of the executive director,
     *  update the current time to that of the executive director. Record
     *  whether the refinement of the current state of the mode controller
     *  is ready to fire.
     *  @exception IllegalActionException If there is no controller.
     *  FIXME: Changed by liuj, not yet reviewed.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug(getFullName(),
                    "prefire called at time: "+getCurrentTime());
        }
        // Clear the inside receivers of all output ports of the container.
        CompositeActor actor = (CompositeActor)getContainer();
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort p = (IOPort)outputPorts.next();
            Receiver[][] insideReceivers = p.getInsideReceivers();
            if (insideReceivers == null) continue;
            for (int i = 0; i < insideReceivers.length; i++) {
                if (insideReceivers[i] == null) continue;
                for (int j = 0; j < insideReceivers[i].length; j++) {
                    try {
                        if (insideReceivers[i][j].hasToken()) {
                            insideReceivers[i][j].get();
                        }
                    } catch (NoTokenException ex) {
                        throw new InternalErrorException(this, ex, null);
                    }
                }
            }
        }
        // Set the current time based on the enclosing class.
        super.prefire();
        _firstFire = true;
        return getController().prefire();
    }

    /** If the container is not null, register this director as the model
     *  error handler.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace, or
     *   if the protected method _checkContainer() throws it, or if
     *   a contained Settable becomes invalid and the error handler
     *   throws it.
     *  @exception NameDuplicationException If the name of this entity
     *   collides with a name already in the container.
     */
    public void setContainer(NamedObj container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            container.setModelErrorHandler(this);
        }
    }

    /** Set the current time of the model under this director.
     *  It allows the set time to be earlier than the current time.
     *  This feature is needed when switching between timed and untimed
     *  models.
     *
     *  @param newTime The new current simulation time.
     *  @exception IllegalActionException Not thrown in this base class.
     *  FIXME: Changed by liuj, not yet reviewed.
     */
    public void setCurrentTime(double newTime) throws IllegalActionException {
        _currentTime = newTime;
    }
    
    /** Transfer data from the input port of the container to the ports
     *  connected to the inside of the input port and on the mode controller
     *  or the refinement of its current state. This method will transfer
     *  exactly one token on each input channel that has at least one token
     *  available. The port argument must be an opaque input port. If any
     *  channel of the input port has no data, then that channel is ignored.
     *  Any token left not consumed in the ports to which data are transferred
     *  is discarded.
     *  @param port The input port to transfer tokens from.
     *  @return True if at least one data token is transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" +
                    "input port.");
        }
        boolean transferredToken = false;
        Receiver[][] insideReceivers = _currentLocalReceivers(port);
        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (port.hasToken(i)) {
                    Token t = port.get(i);
                    if (insideReceivers != null
                            && insideReceivers[i] != null) {
                        for (int j = 0; j < insideReceivers[i].length; j++) {
                            if (insideReceivers[i][j].hasToken()) {
                                insideReceivers[i][j].get();
                            }
                            insideReceivers[i][j].put(t);
                            if (_debugging) {
                                _debug(getFullName(),
                                        "transferring input from "
                                        + port.getFullName()
                                        + " to "
                                        + (insideReceivers[i][j])
                                        .getContainer().getFullName());
                            }
                        }
                        transferredToken = true;
                    }
                } else {
                    if (insideReceivers != null
                            && insideReceivers[i] != null) {
                        for (int j = 0; j < insideReceivers[i].length; j++) {
                            if (insideReceivers[i][j].hasToken()) {
                                insideReceivers[i][j].get();
                            }
                        }
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: " +
                        ex.getMessage());
            }
        }
        return transferredToken;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // Map from input ports of the modal model to the local receivers
    // for the current state.
    protected Map _currentLocalReceiverMap = null;

    // The list of enabled actors that refines the current state.
    protected List _enabledRefinements;

    // Flag that is set to true for the first fire in an iteration.
    protected boolean _firstFire;

    // True if the refinement of the current state of the mode controller
    // is ready to fire in an iteration.
    protected boolean _fireRefinement = false;

    // Stores for each state of the mode controller the map from input
    // ports of the modal model to the local receivers when the mode
    // controller is in that state.
    protected Map _localReceiverMaps = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    // FIXME: Alphabetize the following, and add javadoc comments.

    // Build for each state of the mode controller the map from input
    // ports of the modal model to the local receivers when the mode
    // controller is in that state.
    // This method is read-synchronized on the workspace.
    // @exception IllegalActionException If there is no mode controller.
    protected void _buildLocalReceiverMaps()
            throws IllegalActionException {
        try {
            workspace().getReadAccess();
            FSMActor controller = getController();
            // Remove any existing maps.
            _localReceiverMaps.clear();
            // Create a map for each state of the mode controller.
            Iterator states = controller.entityList().iterator();
            State state = null;
            while (states.hasNext()) {
                state = (State)states.next();
                _localReceiverMaps.put(state, new HashMap());
            }
            CompositeActor comp = (CompositeActor)getContainer();
            Iterator inPorts = comp.inputPortList().iterator();
            List resultsList = new LinkedList();
            while (inPorts.hasNext()) {
                IOPort port = (IOPort)inPorts.next();
                Receiver[][] allReceivers = port.deepGetReceivers();
                states = controller.entityList().iterator();
                while (states.hasNext()) {
                    state = (State)states.next();
                    TypedActor[] actors = state.getRefinement();
                    Receiver[][] allReceiversArray
                        = new Receiver[allReceivers.length][0];
                    for (int i = 0; i < allReceivers.length; ++i) {
                        resultsList.clear();
                        for (int j = 0; j < allReceivers[i].length; ++j) {
                            Receiver receiver = allReceivers[i][j];
                            Nameable cont = receiver.getContainer()
                                .getContainer();
                            if (cont == controller) {
                                resultsList.add(receiver);
                            } else {
                                // check transitions
                                Iterator transitions =
                                    state.nonpreemptiveTransitionList()
                                    .iterator();
                                while (transitions.hasNext()) {
                                    Transition transition =
                                        (Transition) transitions.next();
                                    _checkActorsForReceiver
                                        (transition.getRefinement(), cont,
                                                receiver, resultsList);
                                }
                                // check refinements
                                List stateList = new LinkedList();
                                stateList.add(state);
                                transitions =
                                    state.preemptiveTransitionList()
                                    .iterator();
                                while (transitions.hasNext()) {
                                    Transition transition =
                                        (Transition)transitions.next();
                                    stateList.add(transition
                                            .destinationState());
                                    _checkActorsForReceiver
                                        (transition.getRefinement(), cont,
                                                receiver, resultsList);
                                }
                                Iterator nextStates = stateList.iterator();
                                while (nextStates.hasNext()) {
                                    actors =
                                        ((State)nextStates.next())
                                        .getRefinement();
                                    _checkActorsForReceiver
                                        (actors,cont, receiver, resultsList);
                                }
                            }
                        }
                        allReceiversArray[i] =
                            new Receiver[resultsList.size()];
                        Object[] receivers = resultsList.toArray();
                        for (int j = 0; j < receivers.length; ++j) {
                            allReceiversArray[i][j] = (Receiver)receivers[j];
                        }
                    }
                    Map m = (HashMap)_localReceiverMaps.get(state);
                    m.put(port, allReceiversArray);
                }
            }
            _localReceiverMapsVersion = workspace().getVersion();
            _currentLocalReceiverMap =
                (Map)_localReceiverMaps.get(controller.currentState());
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the enabled transition among the given list of transitions.
     *  Throw an exception if there is more than one transition enabled.
     *
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or if thrown by any choice action contained
     *   by the enabled transition, or if there is no controller.
     */
    protected Transition _chooseTransition(List transitionList)
            throws IllegalActionException {
        if (_controller != null) {
            return _controller._chooseTransition(transitionList);
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }
    
    /** Return the receivers contained by ports connected to the inside
     *  of the given input port and on the mode controller or the
     *  refinement of its current state.
     *  @param port An input port of the container of this director.
     *  @return The receivers that currently get inputs from the given
     *   port.
     *  @exception IllegalActionException If there is no controller.
     */
    protected Receiver[][] _currentLocalReceivers(IOPort port)
            throws IllegalActionException {
        if (_localReceiverMapsVersion != workspace().getVersion()) {
            _buildLocalReceiverMaps();
        }
        return (Receiver[][])_currentLocalReceiverMap.get(port);
    }
    
    /** Return the last chosen transition.
     *  @return The last chosen transition, or null if there has been none.
     */
    protected Transition _getLastChosenTransition() {
        if (_controller != null) {
            return _controller._lastChosenTransition;
        } else {
            return null;
        }
    }
    
    /** Set the current state of this actor.
     *  @param state The state to set.
     *  @exception IllegalActionException If there is no controller.
     */
    protected void _setCurrentState(State state) throws IllegalActionException {
        if (_controller != null) {
            _controller._currentState = state;
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }

    /** Set the map from input ports to boolean flags indicating whether a
     *  channel is connected to an output port of the refinement of the
     *  current state. This method is called by HDFFSMDirector.
     *
     *  @exception IllegalActionException If the refinement specified
     *   for one of the states is not valid, or if there is no controller.
     */
    protected void _setCurrentConnectionMap() throws IllegalActionException {
        if (_controller != null) {
            _controller._setCurrentConnectionMap();
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }
    
    /** Set the value of the shadow variables for input ports of the controller
     *  actor that are defined by output ports of the refinement.
     *  @exception IllegalActionException If a shadow variable cannot take
     *   the token read from its corresponding channel (should not occur).
     */
    protected void _setInputsFromRefinement() throws IllegalActionException {
        if (_controller != null) {
            _controller._setInputsFromRefinement();
        }
    }
    
    /** Set the value of the shadow variables for input ports of the controller
     *  actor.
     *  @exception IllegalActionException If a shadow variable cannot take
     *   the token read from its corresponding channel (should not occur).
     */
    protected void _setInputVariables() throws IllegalActionException {
        if (_controller != null) {
            _controller._setInputVariables();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _checkActorsForReceiver(TypedActor[] actors,
            Nameable cont, Receiver receiver,
            List resultsList) {
        if (actors != null) {
            for (int k = 0; k < actors.length; ++k) {
                if (cont == actors[k]) {
                    if (!resultsList.contains(receiver)) {
                        resultsList.add(receiver);
                        break;
                    }
                }
            }
        }
    }

    // Create the controllerName attribute.
    private void _createAttribute() {
        try {
            Attribute a = getAttribute("controllerName");
            if (a != null) {
                a.setContainer(null);
            }
            controllerName = new StringAttribute(this, "controllerName");
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(getName() + "Cannot create "
                    + "controllerName attribute.");
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(getName() + "Cannot create "
                    + "controllerName attribute.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached reference to mode controller.
    private FSMActor _controller = null;

    // Version of cached reference to mode controller.
    private long _controllerVersion = -1;

    // Version of the local receiver maps.
    private long _localReceiverMapsVersion = -1;

}
