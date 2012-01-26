/* An FSMDirector governs the execution of a modal model.

 Copyright (c) 1999-2011 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.InvariantViolationException;
import ptolemy.actor.Mailbox;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QuasiTransparentDirector;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.data.expr.ParseTreeEvaluator;
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

///////////////////////////////////////////////////////////////////
//// FSMDirector

/**
 An FSMDirector governs the execution of a modal model. A modal model is
 a TypedCompositeActor with a FSMDirector as local director. The mode
 control logic is captured by a mode controller, an instance of FSMActor
 contained by the composite actor. Each state of the mode controller
 represents a mode of operation and can be refined by an opaque CompositeActor
 contained by the same composite actor.
 <p>
 When a modal model is fired, this director first transfers the input tokens
 from the outside domain to the mode controller and the refinement of its
 current state. The preemptive transitions from the current state of the mode
 controller are examined. If there is more than one transition enabled, and
 any of the enabled transitions is not marked nondeterministic, an
 exception is thrown. If there is exactly one preemptive transition enabled
 then it is chosen. The choice actions (outputActions) contained by the transition are
 executed. Any output token produced by the mode controller is transferred to
 both the output ports of the modal model and the input ports of the mode
 controller. Then the refinements associated with the enabled transition are
 executed. Any output token produced by the refinements is transferred to
 both the output ports of the modal model and the input ports of the mode
 controller. The refinements of the current state will not be fired.
 <p>
 If no preemptive transition is enabled, the refinements of the current state
 are fired. Any output token produced by the refinements is transferred to
 both the output ports of the modal model and the input ports of the mode
 controller. After this, the non-preemptive transitions from the current
 state of the mode controller are examined. If there is more than one
 transition enabled, and any of the enabled transitions is not marked
 nondeterministic, an exception is thrown. If there is exactly one
 non-preemptive transition enabled then it is chosen and the choice actions
 contained by the transition are executed. Any output token produced by the
 mode controller is transferred to the output ports of the modal model and
 the input ports of the mode controller. Then, the refinements of the
 enabled transition are executed. Any output token produced by the refinements
 is transferred to both the output ports of the modal model and the input
 ports of the mode controller.
 <p>
 At the end of one firing, the modal model transfers its outputs to the outside
 model. The mode controller does not change state during successive firings
 in one iteration of the top level in order to support upper level domains
 that iterate to a fixed point.
 <p>
 When the modal model is postfired, the chosen transition of the latest
 firing is committed. The commit actions contained by the transition are
 executed and the current state of the mode controller is set to the
 destination state of the transition.

 @author Xiaojun Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 @see FSMActor
 */
public class FSMDirector extends Director implements ExplicitChangeContext,
        QuasiTransparentDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @throws NameDuplicationException If construction of Time objects fails.
     *  @throws IllegalActionException If construction of Time objects fails.
     */
    public FSMDirector() throws IllegalActionException, NameDuplicationException {
        super();
        _createAttribute();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     *  @throws NameDuplicationException If construction of Time objects fails.
     *  @throws IllegalActionException If construction of Time objects fails.
     */
    public FSMDirector(Workspace workspace) throws IllegalActionException, NameDuplicationException {
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
     *  the <i>controllerName</i> attribute, then make note that this
     *  has changed.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the superclass
     *  attributeChanged() method.
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
     *  an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FSMDirector newObject = (FSMDirector) super.clone(workspace);
        newObject._controllerVersion = -1;
        newObject._localReceiverMaps = new HashMap();
        newObject._localReceiverMapsVersion = -1;
        return newObject;
    }

    /** Return a default dependency to use between input input
     *  ports and output ports.
     *  This overrides the base class so that if there
     *  is an executive director, then we get the default
     *  dependency from it.
     *  @see Dependency
     *  @see CausalityInterface
     *  @see Actor#getCausalityInterface()
     *  @return A default dependency between input ports
     *   and output ports.
     */
    public Dependency defaultDependency() {
        Director executiveDirector = ((Actor) getContainer())
                .getExecutiveDirector();
        if (executiveDirector != null) {
            return executiveDirector.defaultDependency();
        }
        return BooleanDependency.OTIMES_IDENTITY;
    }

    /** Fire the model model for one iteration.
     *  If there is a preemptive transition enabled, execute its choice
     *  actions (outputActions). Otherwise,
     *  fire the refinement of the current state. After this firing,
     *  if there is a transition enabled, execute its choice actions.
     *  If any tokens are produced during this iteration, they are sent to
     *  both the output ports of the model model but also the input ports of
     *  the mode controller.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled and nondeterminism is not permitted,
     *   or there is no controller, or it is thrown by any
     *   choice action.
     */
    public void fire() throws IllegalActionException {
        _stateRefinementsToPostfire.clear();
        _transitionRefinementsToPostfire.clear();
        FSMActor controller = getController();
        controller.readInputs();
        State currentState = controller.currentState();
        if (_debugging) {
            _debug("*** Firing " + getFullName(), " at time " + getModelTime());
            _debug("Current state is:", currentState.getName());
        }

        Transition chosenTransition = controller.chooseTransition(currentState
                .preemptiveTransitionList());
        _enabledTransition = chosenTransition;

        if (chosenTransition != null) {
            if (_debugging) {
                _debug("Preemptive transition enabled:",
                        chosenTransition.getName());
            }
            // First execute the refinements of the transition.
            Actor[] actors = chosenTransition.getRefinement();

            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested) {
                        break;
                    }
                    if (_debugging) {
                        _debug("Fire transition refinement:",
                                actors[i].getName());
                    }
                    if (actors[i].prefire()) {
                        actors[i].fire();
                        _transitionRefinementsToPostfire.add(actors[i]);
                    }
                }
            }

            controller.readOutputsFromRefinement();
            return;
        }

        Actor[] actors = currentState.getRefinement();

        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                if (_stopRequested) {
                    break;
                }
                if (actors[i].prefire()) {
                    if (_debugging) {
                        _debug("Fire state refinement:", actors[i].getName());
                    }
                    actors[i].fire();
                    _stateRefinementsToPostfire.add(actors[i]);
                }
            }
        }
        currentState.setVisited(true);
        controller.readOutputsFromRefinement();

        chosenTransition = controller.chooseTransition(currentState
                .nonpreemptiveTransitionList());
        _enabledTransition = chosenTransition;
        if (chosenTransition != null) {
            if (_debugging) {
                _debug("Nonpreemptive transition enabled:",
                        chosenTransition.getName());
            }
            actors = chosenTransition.getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested) {
                        break;
                    }
                    if (actors[i].prefire()) {
                        if (_debugging) {
                            _debug("Fire transition refinement:",
                                    actors[i].getName());
                        }
                        actors[i].fire();
                        _transitionRefinementsToPostfire.add(actors[i]);
                    }
                }
                controller.readOutputsFromRefinement();
            }
        }
    }

    /** Schedule a firing of the given actor at the given time.
     *  If there exists an executive director, this method delegates to
     *  the fireAt() method of the executive director by requesting
     *  a firing of the container of this director at the given time.
     *  <p>
     *  If there is no executive director, then the request results
     *  in model time of this director being set to the specified time.
     *  The reason for this latter behavior is to support models
     *  where FSM is at the top level. A director inside the state
     *  refinements could be timed, and expects time to advance
     *  in its environment between firings. It typically makes a call
     *  to fireAt() at the conclusion of each iteration to specify
     *  the time value it expects to next see. Such directors can
     *  thus be used inside top-level FSM models. For example, the
     *  DEDirector and SDFDirector behave exactly this way.
     *  @param actor The actor scheduled to be fired.
     *  @param time The scheduled time.
     *  @param microstep The microstep.
     *  @return The time at which the actor passed as an argument
     *   will be fired.
     *  @exception IllegalActionException If thrown by the executive director.
     */
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        // Note that the actor parameter is ignored, because it does not
        // matter which actor requests firing.
        Nameable container = getContainer();
        if (container instanceof Actor) {
            Actor modalModel = (Actor) container;
            Director executiveDirector = modalModel.getExecutiveDirector();
            if (executiveDirector != null) {
                return executiveDirector.fireAt(modalModel, time, microstep);
            }
        }
        setModelTime(time);
        return time;
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

            CompositeActor cont = (CompositeActor) container;
            Entity entity = cont.getEntity(name);

            if (entity == null) {
                throw new IllegalActionException(this, "No controller found "
                        + "with name " + name);
            }

            if (!(entity instanceof FSMActor)) {
                throw new IllegalActionException(this, entity,
                        "mode controller must be an instance of FSMActor.");
            }

            _controller = (FSMActor) entity;
            _controllerVersion = workspace().getVersion();
            return _controller;
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the explicit change context.  In this case,
     *  the change context returned is the composite actor controlled
     *  by this director.
     *  @return The explicit change context.
     */
    public Entity getContext() {
        return (Entity) getContainer();
    }

    /** Override the base class so that if any outgoing transition
     *  has a guard that evaluates to true, then return the current
     *  time. Otherwise, delegate to the enclosing director.
     */
    public Time getModelNextIterationTime() {
        try {
            FSMActor controller = getController();
            List transitionList = controller.currentState().outgoingPort
                    .linkedRelationList();
            List enabledTransitions = controller
                    .enabledTransitions(transitionList);
            if (enabledTransitions.size() > 0) {
                return getModelTime();
            }
            return super.getModelNextIterationTime();
        } catch (IllegalActionException e) {
            // Any exception here should have shown up before now.
            throw new InternalErrorException(e);
        }
    }

    /** Return a list of variables that are modified in a modal model.
     *  The variables are assumed to have a change context of the
     *  container of this director.  This class returns all variables
     *  that are assigned in the actions of transitions.
     *  @return A list of variables.
     *  @exception IllegalActionException If no controller can be found,
     *  or the variables to be assigned by the actions can not be found.
     */
    public List getModifiedVariables() throws IllegalActionException {
        List list = new LinkedList();

        // Collect assignments from FSM transitions
        for (Iterator states = getController().entityList().iterator(); states
                .hasNext();) {
            State state = (State) states.next();

            for (Iterator transitions = state.outgoingPort.linkedRelationList()
                    .iterator(); transitions.hasNext();) {
                Transition transition = (Transition) transitions.next();

                for (Iterator actions = transition.choiceActionList()
                        .iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions
                            .next();

                    for (Iterator names = action.getDestinationNameList()
                            .iterator(); names.hasNext();) {
                        String name = (String) names.next();
                        NamedObj object = action.getDestination(name);

                        if (object instanceof Variable) {
                            list.add(object);
                        }
                    }
                }

                for (Iterator actions = transition.commitActionList()
                        .iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions
                            .next();

                    for (Iterator names = action.getDestinationNameList()
                            .iterator(); names.hasNext();) {
                        String name = (String) names.next();
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

    /** Return the parse tree evaluator used to evaluate guard expressions.
     *  In this base class, an instance
     *  of {@link ParseTreeEvaluator} is returned. The derived classes may need
     *  to override this method to return different parse tree evaluators.
     *  @return ParseTreeEvaluator used to evaluate guard expressions.
     */
    public ParseTreeEvaluator getParseTreeEvaluator() {
        return new ParseTreeEvaluator();
    }

    /** Return true if the model errors are handled. Otherwise, return false
     *  and the model errors are passed to the higher level in hierarchy.
     *  <p>
     *  In this method, model errors including
     *  multipleEnabledTransitionException and InvariantViolationException
     *  are handled.
     *  <p>
     *  In the current design, if multiple enabled transitions are detected,
     *  an exception will be thrown. For future designs, different ways to
     *  handle this situation will be introduced here.
     *  <p>
     *  When an invariant is violated, this method checks whether there exists
     *  an enabled (non-preemptive) transition. If there is one, the model
     *  error is ignored and this director will handle the enabled transition
     *  later. Otherwise, an exception will be thrown.
     *
     *  @param context The context where the model error happens.
     *  @param exception An exception that represents the model error.
     *  @return True if the error has been handled, false if the
     *  model error is passed to the higher level.
     *  @exception IllegalActionException If multiple enabled transition is
     *  detected, or mode controller can not be found, or can not read
     *  outputs from refinements.
     */
    public boolean handleModelError(NamedObj context,
            IllegalActionException exception) throws IllegalActionException {
        // NOTE: Besides throwing exception directly, we can handle
        // multiple enabled transitions in different ways by the derived
        // sub classes.
        if (exception instanceof MultipleEnabledTransitionsException) {
            throw exception;
        }

        // If the exception is an InvariantViolationException
        // exception, check if any transition is enabled.
        if (exception instanceof InvariantViolationException) {
            FSMActor controller = getController();
            controller.readOutputsFromRefinement();

            State st = controller.currentState();
            List enabledTransitions = controller.enabledTransitions(st
                    .nonpreemptiveTransitionList());

            if (enabledTransitions.size() == 0) {
                ModelErrorHandler container = getContainer();

                if (container != null) {
                    // We can not call the handleModelError() method of the
                    // super class, because the container will call this
                    // method again and it will lead to a dead loop.
                    // return container.handleModelError(context, exception);
                    throw exception;
                }
            }

            if (_debugging && _verbose) {
                _debug("ModelError " + exception.getMessage() + " is handled.");
            }

            return true;
        }

        // else delegate the exception to upper level.
        return false;
    }

    /** Return true if all state refinements have directors that implement
     *  the strict actor semantics and if the enclosing executive director
     *  also returns true.
     *  @return True if the director assumes and exports strict actor semantics.
     */
    public boolean implementsStrictActorSemantics() {
        Nameable container = getContainer();
        if (container instanceof Actor) {
            Actor modalModel = (Actor) container;
            Director executiveDirector = modalModel.getExecutiveDirector();
            if (executiveDirector != null
                    && !executiveDirector.implementsStrictActorSemantics()) {
                return false;
            }
        }
        // Iterate over the state refinements.
        try {
            FSMActor controller = getController();
            List<State> states = controller.entityList();
            for (State state : states) {
                TypedActor[] refinements;
                try {
                    refinements = state.getRefinement();
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(e);
                }
                if (refinements != null) {
                    for (int i = 0; i < refinements.length; i++) {
                        Director director = refinements[i].getDirector();
                        // Added director != this since it might be possible that the refinement
                        // is a Modal Model without its own director. In this case director == this,
                        // and the call director.implementsStrictActorSemantics() would lead to a infinite
                        // loop.
                        if (director != null && director != this
                                && !director.implementsStrictActorSemantics()) {
                            return false;
                        }
                    }
                }
            }
        } catch (IllegalActionException e1) {
            throw new InternalErrorException(e1);
        }
        return true;
    }

    /** Initialize the mode controller and all the refinements by calling the
     *  initialize() method in the super class. Build the local maps for
     *  receivers.
     *
     *  @exception IllegalActionException If thrown by the initialize() method
     *  of the super class, or can not find mode controller, or can not find
     *  refinement of the current state.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _buildLocalReceiverMaps();
    }

    /** Return false if there is any output of the container does not depend
     *  directly on all inputs of the container.
     *  @return False if there is any output that does not
     *   depend directly on an input.
     *  @exception IllegalActionException Thrown if causality interface
     *  cannot be computed.
     */
    public boolean isStrict() throws IllegalActionException {
        Actor container = (Actor) getContainer();
        CausalityInterface causality = container.getCausalityInterface();
        int numberOfOutputs = container.outputPortList().size();
        Collection<IOPort> inputs = container.inputPortList();
        for (IOPort input : inputs) {
            // If the input is also output, skip it.
            // This is the output of a refinement.
            if (input.isOutput()) {
                continue;
            }
            try {
                if (causality.dependentPorts(input).size() < numberOfOutputs) {
                    return false;
                }
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }
        return true;
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

    /** Invoke postfire() on any state refinements that were fired,
     *  then execute the commit actions contained by the last
     *  chosen transition, if any, then invoke postfire() on any transition
     *  refinements that were fired, and finally set the current
     *  state to the destination state of the transition.
     *  This will return false if any refinement that is postfired
     *  returns false.
     *  <p>
     *  If any transition was taken in this iteration, and if there
     *  is an executive director, then this method calls fireAtCurrentTime(Actor)
     *  on that executive director. This requests a refiring in case the
     *  there is an enabled transition. If there is, then the current
     *  state is transient, and we will want to spend zero time in it.
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If thrown by any commit action
     *  or there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = true;
        if (_debugging) {
            _debug("*** postfire called at time: ", getModelTime().toString());
        }
        for (Actor stateRefinement : _stateRefinementsToPostfire) {
            if (_debugging) {
                _debug("Postfiring state refinment:", stateRefinement.getName());
            }
            result &= stateRefinement.postfire();
        }

        FSMActor controller = getController();
        result &= controller.postfire();

        for (Actor transitionRefinement : _transitionRefinementsToPostfire) {
            if (_debugging) {
                _debug("Postfiring transition refinment:",
                        transitionRefinement.getName());
            }
            result &= transitionRefinement.postfire();
        }

        _currentLocalReceiverMap = (Map) _localReceiverMaps.get(controller
                .currentState());

        // If a transition was taken, then request a refiring at the current time
        // in case the destination state is a transient state.
        if (_enabledTransition != null) {
            CompositeActor container = (CompositeActor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            if (executiveDirector != null) {
                if (_debugging) {
                    _debug("Request refiring by "
                            + executiveDirector.getFullName() + " at "
                            + getModelTime());
                }
                executiveDirector.fireAtCurrentTime(container);
            }
        }
        return result && !_stopRequested && !_finishRequested;
    }

    /** Return true if the mode controller is ready to fire.
     *  If this model is not at the top level and the current
     *  time of this director lags behind that of the executive director,
     *  update the current time to that of the executive director. Record
     *  whether the refinements of the current state of the mode controller
     *  are ready to fire.
     *  @exception IllegalActionException If there is no controller.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Prefire called at time: " + getModelTime());
        }
        // Clear the inside receivers of all output ports of the container.
        // FIXME: why here? should this happen at the postfire() method?
        // Note that  ct LevelCrossingDetectorDetectsGlitches.xml needs this.
        // See https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=296
        CompositeActor actor = (CompositeActor) getContainer();
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort p = (IOPort) outputPorts.next();
            Receiver[][] insideReceivers = p.getInsideReceivers();

            if (insideReceivers == null) {
                continue;
            }

            for (int i = 0; i < insideReceivers.length; i++) {
                if (insideReceivers[i] == null) {
                    continue;
                }

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
        return getController().prefire();
    }

    /** If the container is not null, register this director as the model
     *  error handler.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *  recursive containment structure, or if this entity and container are
     *  not in the same workspace, or if the protected method _checkContainer()
     *  throws it, or if a contained Settable becomes invalid and the error
     *  handler throws it.
     *  @exception NameDuplicationException If the name of this entity
     *  collides with a name already in the container.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);

        if (container != null) {
            container.setModelErrorHandler(this);
        }
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
     *  input port.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque"
                            + "input port.");
        }

        boolean transferredToken = false;
        Receiver[][] insideReceivers = _currentLocalReceivers(port);

        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (port.isKnown(i)) {
                    if (port.hasToken(i)) {
                        Token t = port.get(i);

                        if ((insideReceivers != null)
                                && (insideReceivers[i] != null)) {
                            for (int j = 0; j < insideReceivers[i].length; j++) {
                                //  r45276 removed the code below, which cause
                                // problems with
                                // ct LevelCrossingDetectorDetectsGlitches.xml.
                                // However, the change below is not necessary to
                                // fix this bug.
                                // See https://chess.eecs.berkeley.edu/bugzilla/show_bug.cgi?id=296
                                // if (insideReceivers[i][j].hasToken()) {
                                // insideReceivers[i][j].get();
                                // }

                                insideReceivers[i][j].put(t);
                                if (_debugging) {
                                    _debug(getFullName(),
                                            "transferring input from "
                                                    + port.getFullName()
                                                    + " to "
                                                    + (insideReceivers[i][j])
                                                            .getContainer()
                                                            .getFullName());
                                }
                            }

                            transferredToken = true;
                        }
                    } else {
                        if ((insideReceivers != null)
                                && (insideReceivers[i] != null)) {
                            for (int j = 0; j < insideReceivers[i].length; j++) {
                                if (insideReceivers[i][j].hasToken()) {
                                    insideReceivers[i][j].get();
                                }
                            }
                        }
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(
                        "Director.transferInputs: Internal error: "
                                + ex.getMessage());
            }
        }

        return transferredToken;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Build for each state of the mode controller the map from input
     *  ports of the modal model to the local receivers when the mode
     *  controller is in that state.
     *  This method is read-synchronized on the workspace.
     *  @exception IllegalActionException If there is no mode controller,
     *  or can not find refinements for states.
     */
    protected void _buildLocalReceiverMaps() throws IllegalActionException {
        try {
            workspace().getReadAccess();

            FSMActor controller = getController();

            // Remove any existing maps.
            _localReceiverMaps.clear();

            // Create a map for each state of the mode controller.
            Iterator states = controller.entityList().iterator();
            State state = null;

            while (states.hasNext()) {
                state = (State) states.next();
                _localReceiverMaps.put(state, new HashMap());
            }

            CompositeActor comp = (CompositeActor) getContainer();
            Iterator inPorts = comp.inputPortList().iterator();
            List resultsList = new LinkedList();

            while (inPorts.hasNext()) {
                IOPort port = (IOPort) inPorts.next();
                Receiver[][] allReceivers = port.deepGetReceivers();
                states = controller.entityList().iterator();

                while (states.hasNext()) {
                    state = (State) states.next();

                    TypedActor[] actors = state.getRefinement();
                    Receiver[][] allReceiversArray = new Receiver[allReceivers.length][0];

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
                                Iterator transitions = state
                                        .nonpreemptiveTransitionList()
                                        .iterator();

                                while (transitions.hasNext()) {
                                    Transition transition = (Transition) transitions
                                            .next();
                                    _checkActorsForReceiver(
                                            transition.getRefinement(), cont,
                                            receiver, resultsList);
                                }

                                // check refinements
                                List stateList = new LinkedList();
                                stateList.add(state);
                                transitions = state.preemptiveTransitionList()
                                        .iterator();

                                while (transitions.hasNext()) {
                                    Transition transition = (Transition) transitions
                                            .next();
                                    stateList
                                            .add(transition.destinationState());
                                    _checkActorsForReceiver(
                                            transition.getRefinement(), cont,
                                            receiver, resultsList);
                                }

                                Iterator nextStates = stateList.iterator();

                                while (nextStates.hasNext()) {
                                    actors = ((State) nextStates.next())
                                            .getRefinement();
                                    _checkActorsForReceiver(actors, cont,
                                            receiver, resultsList);
                                }
                            }
                        }

                        allReceiversArray[i] = new Receiver[resultsList.size()];

                        Object[] receivers = resultsList.toArray();

                        for (int j = 0; j < receivers.length; ++j) {
                            allReceiversArray[i][j] = (Receiver) receivers[j];
                        }
                    }

                    Map m = (HashMap) _localReceiverMaps.get(state);
                    m.put(port, allReceiversArray);
                }
            }

            _localReceiverMapsVersion = workspace().getVersion();
            _currentLocalReceiverMap = (Map) _localReceiverMaps.get(controller
                    .currentState());
        } finally {
            workspace().doneReading();
        }
    }

    /** Return a list of enabled transitions among the given list of
     *  transitions.
     *  This method is called by subclasses of FSMDirector in other packages.
     *  @param transitionList A list of transitions.
     *  @return A list of enabled transition.
     *  @exception IllegalActionException If the guard expression of any
     *  transition can not be evaluated.
     */
    protected List _checkTransition(List transitionList)
            throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            return controller.enabledTransitions(transitionList);
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }

    /** Return the enabled transition among the given list of transitions.
     *  Throw an exception if there is more than one transition enabled.
     *  This method is called by subclasses of FSMDirector in other packages.
     *
     *  @param transitionList A list of transitions.
     *  @return An enabled transition, or null if none is enabled.
     *  @exception IllegalActionException If there is more than one
     *   transition enabled, or if thrown by any choice action contained
     *   by the enabled transition, or if there is no controller.
     */
    protected Transition _chooseTransition(List transitionList)
            throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            return controller.chooseTransition(transitionList);
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

        return (Receiver[][]) _currentLocalReceiverMap.get(port);
    }

    /** Return the last chosen transition.
     *  @return The last chosen transition, or null if there has been none.
     *  @exception IllegalActionException If there is no controller.
     */
    protected Transition _getLastChosenTransition()
            throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            return controller._lastChosenTransition;
        } else {
            return null;
        }
    }

    /** Set the value of the shadow variables for input ports of the controller
     *  actor.
     *  @exception IllegalActionException If a shadow variable cannot take
     *   the token read from its corresponding channel (should not occur).
     */
    protected void _readInputs() throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            controller.readInputs();
        }
    }

    /** Set the value of the shadow variables for input ports of the controller
     *  actor that are defined by output ports of the refinement.
     *  @exception IllegalActionException If a shadow variable cannot take
     *   the token read from its corresponding channel (should not occur).
     */
    protected void _readOutputsFromRefinement() throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            controller.readOutputsFromRefinement();
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
        FSMActor controller = getController();

        if (controller != null) {
            controller._setCurrentConnectionMap();
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }

    /** Set the current state of this actor.
     *  @param state The state to set.
     *  @exception IllegalActionException If there is no controller.
     */
    protected void _setCurrentState(State state) throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            controller._currentState = state;
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Map from input ports of the modal model to the local receivers
     *  for the current state.
     */
    protected Map _currentLocalReceiverMap = null;

    /** The list of enabled actors that refines the current state.
     */

    // FIXME: this will help to improve performance when firing. However,
    // it is only used in the HSFSMDirector. Modify the fire() method.
    // Or this may not be necessary.
    protected List _enabledRefinements;

    /** cached enabled transition.
     */
    protected Transition _enabledTransition = null;

    /** Stores for each state of the mode controller the map from input
     *  ports of the modal model to the local receivers when the mode
     *  controller is in that state.
     */
    protected Map _localReceiverMaps = new HashMap();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _checkActorsForReceiver(TypedActor[] actors, Nameable cont,
            Receiver receiver, List resultsList) {
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

    /** Create the controllerName attribute. */
    private void _createAttribute() {
        try {
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

    /** Cached reference to mode controller. */
    private FSMActor _controller = null;

    /** Version of cached reference to mode controller. */
    private long _controllerVersion = -1;

    // Version of the local receiver maps.
    private long _localReceiverMapsVersion = -1;

    /** State refinements to postfire(), as determined by the fire() method. */
    private List<Actor> _stateRefinementsToPostfire = new LinkedList<Actor>();

    /** Transition refinements to postfire(), as determined by the fire() method. */
    private List<Actor> _transitionRefinementsToPostfire = new LinkedList<Actor>();
}
