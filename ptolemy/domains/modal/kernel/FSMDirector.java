/* An FSMDirector governs the execution of a modal model.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

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
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QuasiTransparentDirector;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.BooleanDependency;
import ptolemy.actor.util.CausalityInterface;
import ptolemy.actor.util.Dependency;
import ptolemy.actor.util.ExplicitChangeContext;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.Variable;
import ptolemy.domains.modal.modal.Refinement;
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
 * An FSMDirector governs the execution of a modal model. A modal
 * model is a TypedCompositeActor with a FSMDirector as local
 * director. The mode control logic is captured by a mode controller,
 * an instance of FSMActor contained by the composite actor. Each
 * state of the mode controller represents a mode of operation and can
 * be refined by an opaque CompositeActor contained by the same
 * composite actor.

 * <p> The mode controller contains a set of states and transitions. A
 * transition has a <i>guard expression</i>, any number of <i>output
 * actions</i>, and any number of <i>set actions</i>. It has an
 * <i>initial state</i>, which is the unique state whose
 * <i>isInitialState</i> parameter is true.  The states and
 * transitions can have <i>refinements</i>, which are composite
 * actors. In outline, a firing of this director is a sequence of
 * steps:
 *
 * <ol>
 * <li>Read inputs.
 * <li>Evaluate the guards of preemptive transition out of the current state.
 * <li>If no preemptive transition is enabled:
 * <ol>
 * <li>Fire the refinements of the current state (if any).
 * <li>Evaluate guards on non-preemptive transitions out of the current state.
 * </ol>
 * <li>Choose a transition whose guard is true.
 * <li>Execute the output actions of the chosen transition.
 * <li>Fire the transition refinements of the chosen transition.
 * </ol>
 * In postfire, the following steps are performed:
 * <ol>
 * <li>Postfire the refinements of the current state if they were fired.
 * <li>Initialize the refinements of the destination state if the transition is a reset
 * transition.
 * <li>Execute the set actions of the chosen transition.
 * <li>Postfire the transition refinements of the chosen transition.
 * <li>Change the current state to the destination of the chosen transition.
 * </ol>

 * Since this director makes no persistent state changes in its fire()
 * method, it conforms with the <i>actor abstract
 * semantics</i>. Assuming the state and transition refinements also
 * conform, this director can be used inside any Ptolemy II actor
 * model of computation. How it behaves in each domain, however, can
 * be somewhat subtle, particularly with domains that have fixed-point
 * semantics and when nondeterministic transitions are used. The
 * details are given below.</p>
 *
 * <p> When a modal model is fired, this director first transfers the
 * input tokens from the outside domain to the mode controller and the
 * refinement of its current state. The preemptive transitions from
 * the current state of the mode controller are examined. If there is
 * more than one transition enabled, and any of the enabled
 * transitions is not marked nondeterministic, an exception is
 * thrown. If there is exactly one preemptive transition enabled then
 * it is chosen. The choice actions (outputActions) contained by the
 * transition are executed. Any output token produced by the mode
 * controller is transferred to both the output ports of the modal
 * model and the input ports of the mode controller. Then the
 * refinements associated with the enabled transition are
 * executed. Any output token produced by the refinements is
 * transferred to both the output ports of the modal model and the
 * input ports of the mode controller. The refinements of the current
 * state will not be fired.</p>
 *
 * <p> If no preemptive transition is enabled, the refinements of the
 * current state are fired. Any output token produced by the
 * refinements is transferred to both the output ports of the modal
 * model and the input ports of the mode controller. After this, the
 * non-preemptive transitions from the current state of the mode
 * controller are examined. If there is more than one transition
 * enabled, and any of the enabled transitions is not marked
 * nondeterministic, an exception is thrown. If there is exactly one
 * non-preemptive transition enabled then it is chosen and the choice
 * actions contained by the transition are executed. Any output token
 * produced by the mode controller is transferred to the output ports
 * of the modal model and the input ports of the mode
 * controller. Then, the refinements of the enabled transition are
 * executed. Any output token produced by the refinements is
 * transferred to both the output ports of the modal model and the
 * input ports of the mode controller.</p>
 *
 * <p> In a firing, it is possible that the current state refinement
 * produces an output, and a transition that is taken also produces an
 * output on the same port. In this case, only the second of these
 * outputs will appear on the output of the composite actor containing
 * this director.  However, the first of these output values, the one
 * produced by the refinement, may affect whether the transition is
 * taken. That is, it can affect the guard. If in addition a
 * transition refinement writes to the output, then that value will be
 * produced, overwriting the value produced either by the state
 * refinement or the output action on the transition.</p>
 *
 * <p> At the end of one firing, the modal model transfers its outputs
 * to the outside model. The mode controller does not change state
 * during successive firings in one iteration of the top level in
 * order to support upper level domains that iterate to a fixed
 * point.</p>
 *
 * <p> When the modal model is postfired, the chosen transition of the
 * latest firing is committed. The commit actions contained by the
 * transition are executed and the current state of the mode
 * controller is set to the destination state of the transition.</p>
 *
 * <p>FIXME: If a state has multiple refinements, they are fired in the order defined.
 * If they write to the same output, then the "last one wins." It will be its value
 * produced. It might make more sense to require them to be consistent, giving something
 * closer to SR semantics. The same argument could apply when both a refinement and
 * a transition produce outputs.</p>
 *
 * @author Xiaojun Liu, Haiyang Zheng, Edward A. Lee, Christian Motika
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Yellow (hyzheng)
 * @Pt.AcceptedRating Red (hyzheng)
 * @see FSMActor
 */
public class FSMDirector extends Director implements ExplicitChangeContext,
QuasiTransparentDirector, SuperdenseTimeDirector {
    /**
     * Construct a director in the default workspace with an empty
     * string as its name. The director is added to the list of
     * objects in the workspace. Increment the version number of the
     * workspace.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public FSMDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
        _createAttribute();
    }

    /**
     * Construct a director in the workspace with an empty name. The
     * director is added to the list of objects in the
     * workspace. Increment the version number of the workspace.
     * @param workspace The workspace of this director.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public FSMDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _createAttribute();
    }

    /**
     * Construct a director in the given container with the given
     * name. The container argument must not be null, or a
     * NullPointerException will be thrown. If the name argument is
     * null, then the name is set to the empty string. Increment the
     * version number of the workspace.
     *
     * @param container Container of this director.
     * @param name Name of this director.
     * @exception IllegalActionException If the name has a period in it, or the director
     * is not compatible with the specified container.
     * @exception NameDuplicationException If the container not a CompositeActor and the
     * name collides with an entity in the container.
     */
    public FSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _createAttribute();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Attribute specifying the name of the mode controller in the
     * container of this director. This director must have a mode
     * controller that has the same container as this director,
     * otherwise an IllegalActionException will be thrown when action
     * methods of this director are called.
     */
    public StringAttribute controllerName = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * React to a change in an attribute. If the changed attribute is
     * the <i>controllerName</i> attribute, then make note that this
     * has changed.
     * @param attribute The attribute that changed.
     * @exception IllegalActionException If thrown by the superclass attributeChanged()
     * method.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == controllerName) {
            _controllerVersion = -1;
        }
    }

    /**
     * Clone the director into the specified workspace. This calls the
     * base class and then sets the attribute public members to refer
     * to the attributes of the new director.
     *
     * @param workspace The workspace for the new director.
     * @return A new director.
     * @exception CloneNotSupportedException If a derived class contains an attribute that
     * cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FSMDirector newObject = (FSMDirector) super.clone(workspace);
        // Protected variables.
        newObject._currentLocalReceiverMap = null;
        newObject._localReceiverMaps = new HashMap();

        // Private variables.
        newObject._controller = null;
        newObject._controllerVersion = -1;
        newObject._localReceiverMapsVersion = -1;

        return newObject;
    }

    /**
     * Return a default dependency to use between input input ports
     * and output ports. This overrides the base class so that if
     * there is an executive director, then we get the default
     * dependency from it.
     *
     * @see Dependency
     * @see CausalityInterface
     * @see Actor#getCausalityInterface()
     * @return A default dependency between input ports and output ports.
     */
    @Override
    public Dependency defaultDependency() {
        Director executiveDirector = ((Actor) getContainer())
                .getExecutiveDirector();
        if (isEmbedded() && executiveDirector != null) {
            return executiveDirector.defaultDependency();
        }
        return BooleanDependency.OTIMES_IDENTITY;
    }

    /**
     * Fire the modal model for one iteration. If there is a
     * preemptive transition enabled, execute its choice actions
     * (outputActions). Otherwise, fire the refinement of the current
     * state.  After this firing, if there is a transition enabled,
     * execute its choice actions. If any tokens are produced during
     * this iteration, they are sent to the output ports of the
     * model model and also the input ports of the mode controller.
     *
     * @exception IllegalActionException If there is more than one
     *                transition enabled and nondeterminism is not
     *                permitted, or there is no controller, or it is
     *                thrown by any choice action.
     */
    @Override
    public void fire() throws IllegalActionException {
        FSMActor controller = getController();
        State currentState = controller.currentState();
        if (_debugging) {
            _debug("*** Firing " + getFullName(), " at time " + getModelTime());
            _debug("Current state is:", currentState.getName());
        }
        controller.fire();
    }

    /**
     * Schedule a firing of the given actor at the given time
     * and microstep. If there exists an executive
     * director, this method delegates to the fireAt() method of the executive director by
     * requesting a firing of the container of this director at the given time adjusted by the
     * current offset between local time and the environment time.
     *
     * <p> If there is no executive director, then the request results
     * in model time of this director being set to the specified
     * time. The reason for this latter behavior is to support models
     * where FSM is at the top level. A director inside the state
     * refinements could be timed, and expects time to advance in its
     * environment between firings. It typically makes a call to
     * fireAt() at the conclusion of each iteration to specify the
     * time value it expects to next see. Such directors can thus be
     * used inside top-level FSM models. For example, the DEDirector
     * and SDFDirector behave exactly this way.</p>
     *
     * @param actor The actor scheduled to be fired.
     * @param time The scheduled time.
     * @param microstep The microstep.
     * @return The time at which the actor passed as an argument will be fired.
     * @exception IllegalActionException If thrown by the executive director.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        Actor container = (Actor) getContainer();
        if (container != null) {
            Director director = container.getExecutiveDirector();
            if (isEmbedded() && director != null) {
                if (_debugging) {
                    _debug("**** Requesting that enclosing director refire me at "
                            + time + " with microstep " + microstep);
                }
                // Translate the local time into an environment time.
                Time environmentTime = localClock
                        .getEnvironmentTimeForLocalTime(time);
                Time result = director.fireAt(container, environmentTime,
                        microstep);

                // Translate the response from the environment into a local time.
                return localClock.getLocalTimeForEnvironmentTime(result);
            }

        }
        setModelTime(time);
        return time;
    }

    /**
     * Return the mode controller of this director. The name of the
     * mode controller is specified by the <i>controllerName</i>
     * attribute. The mode controller must have the same container as
     * this director. This method is read-synchronized on the
     * workspace.
     *
     * @return The mode controller of this director.
     * @exception IllegalActionException If no controller is found.
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

    /**
     * Return the explicit change context. In this case, the change context returned is the
     * composite actor controlled by this director.
     *
     * @return The explicit change context.
     */
    @Override
    public Entity getContext() {
        return (Entity) getContainer();
    }

    /**
     * Override the base class so that if any outgoing transition has
     * a guard that evaluates to true, then return the current
     * time. Otherwise, delegate to the enclosing director.
     */
    @Override
    public Time getModelNextIterationTime() {
        try {
            FSMActor controller = getController();
            List transitionList = controller.currentState().outgoingPort
                    .linkedRelationList();
            // First check preemptive transitions, then non-preemptive ones.
            List enabledTransitions = controller.enabledTransitions(
                    transitionList, true, false);
            if (enabledTransitions.size() > 0) {
                return getModelTime();
            }
            enabledTransitions = controller.enabledTransitions(transitionList,
                    false, false);
            if (enabledTransitions.size() > 0) {
                return getModelTime();
            }
            // The result returned below needs to be adjusted by the current offset.
            Time result = localClock.getLocalTimeForEnvironmentTime(super
                    .getModelNextIterationTime());
            return result;
        } catch (IllegalActionException e) {
            // Any exception here should have shown up before now.
            throw new InternalErrorException(e);
        }
    }

    /**
     * Return a list of variables that are modified in a modal
     * model. The variables are assumed to have a change context of
     * the container of this director. This class returns all
     * variables that are assigned in the actions of transitions.
     *
     * @return A list of variables.
     * @exception IllegalActionException If no controller can be
     *                found, or the variables to be assigned by the
     *                actions can not be found.
     */
    @Override
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

    /**
     * Return the parse tree evaluator used to evaluate guard
     * expressions. In this base class, an instance of {@link
     * ParseTreeEvaluator} is returned. The derived classes may need
     * to override this method to return different parse tree
     * evaluators.
     *
     * @return ParseTreeEvaluator used to evaluate guard expressions.
     */
    public ParseTreeEvaluator getParseTreeEvaluator() {
        return new ParseTreeEvaluator();
    }

    /**
     * Return a superdense time index for the current time. This
     * method delegates to the executive director, if there is one
     * that implements SuperdenseTimeDirector, and returns current
     * time with index 1 otherwise.
     *
     * @return A superdense time index.
     * @see #setIndex(int)
     * @see ptolemy.actor.SuperdenseTimeDirector
     */
    @Override
    public int getIndex() {
        Director executiveDirector = ((Actor) getContainer())
                .getExecutiveDirector();
        if (isEmbedded() && executiveDirector instanceof SuperdenseTimeDirector) {
            return ((SuperdenseTimeDirector) executiveDirector).getIndex()
                    + _indexOffset;
        }
        return 1;
    }

    /**
     * Return true if the model errors are handled. Otherwise, return
     * false and the model errors are passed to the higher level in
     * hierarchy.
     *
     * <p> In this method, model errors including
     * multipleEnabledTransitionException and
     * InvariantViolationException are handled.</p>
     *
     * <p> In the current design, if multiple enabled transitions are
     * detected, an exception will be thrown. For future designs,
     * different ways to handle this situation will be introduced
     * here.</p>
     *
     * <p> When an invariant is violated, this method checks whether
     * there exists an enabled (non-preemptive) transition. If there
     * is one, the model error is ignored and this director will
     * handle the enabled transition later. Otherwise, an exception
     * will be thrown.</p>
     *
     * @param context The context where the model error happens.
     * @param exception An exception that represents the model error.
     * @return True if the error has been handled, false if the model error is passed
     * to the higher level.
     * @exception IllegalActionException If multiple enabled transition is detected,
     * or mode controller can not be found, or can not read outputs from refinements.
     */
    @Override
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
        // FIXME: This whole mechanism needs to be checked...
        if (exception instanceof InvariantViolationException) {
            FSMActor controller = getController();
            controller.readOutputsFromRefinement();

            State currentState = controller.currentState();
            // FIXME: Need to understand how error transitions work
            // in combination with immediate transitions and model errors.
            // Note that this only makes sense for non-preemptive transitions.
            List enabledTransitions = controller.enabledTransitions(
                    currentState.outgoingPort.linkedRelationList(), false,
                    false);

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

    /** Return true if all state refinements have directors that
     *  implement the strict actor semantics.
     *  @return True if the director exports strict actor semantics.
     */
    @Override
    public boolean implementsStrictActorSemantics() {
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
                    for (TypedActor refinement : refinements) {
                        Director director = refinement.getDirector();
                        // Added director != this since it might be
                        // possible that the refinement is a Modal
                        // Model without its own director. In this
                        // case director == this, and the call
                        // director.implementsStrictActorSemantics()
                        // would lead to a infinite loop.
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

    /**
     * Initialize the mode controller and all the refinements by
     * calling the initialize() method in the super class. Build the
     * local maps for receivers. Suspend all the refinements of states
     * that are not the current state.
     *
     * @exception IllegalActionException If thrown by the initialize()
     *                method of the super class, or can not find mode
     *                controller, or can not find refinement of the
     *                current state.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        // Transitions may produce initial outputs that need to be transferred
        // to the outside. Make sure to do this before resetting receivers.
        transferOutputs();

        resetOutputReceivers();

        // Suspend the refinements of all non-initial states at the start time.
        // NOTE: Perhaps this could be avoided by doing a suspend in the loop
        // over refinements above.
        List<State> states = getController().entityList();
        for (State state : states) {
            if (((BooleanToken) state.isInitialState.getToken()).booleanValue()) {
                continue;
            }
            TypedActor[] refinements = state.getRefinement();
            if (refinements != null) {
                for (TypedActor refinement : refinements) {
                    Director refinementDirector = refinement.getDirector();
                    if (refinementDirector != this) {
                        refinementDirector.suspend();
                    }
                }
            }
        }
    }

    /**
     * Indicate that a schedule for the model may no longer be valid,
     * if there is a schedule. This method should be called when
     * topology changes are made, or for that matter when any change
     * that may invalidate the schedule is made. In this class,
     * delegate to the executive director.  This is because changes in
     * the FSM may affect the causality interface of the FSM, which
     * may be used in scheduling by the enclosing director.
     */
    @Override
    public void invalidateSchedule() {
        Director executiveDirector = ((Actor) getContainer())
                .getExecutiveDirector();
        if (isEmbedded() && executiveDirector != null) {
            executiveDirector.invalidateSchedule();
        }
    }

    /**
     * Return false. This director checks inputs to see whether they
     * are known before evaluating guards, so it can fired even if it
     * has unknown inputs.
     *
     * @return False.
     * @exception IllegalActionException
     *                Not thrown in this base class.
     */
    @Override
    public boolean isStrict() throws IllegalActionException {
        return false;
        /*
         * NOTE: This used to return a value as follows based on the
         * causality interface. But this is conservative and prevents
         * using the director in some models. Actor container =
         * (Actor)getContainer(); CausalityInterface causality =
         * container.getCausalityInterface(); int numberOfOutputs =
         * container.outputPortList().size(); Collection<IOPort>
         * inputs = container.inputPortList(); for (IOPort input :
         * inputs) { // If the input is also output, skip it. // This
         * is the output of a refinement. if (input.isOutput()) {
         * continue; } try { if
         * (causality.dependentPorts(input).size() < numberOfOutputs)
         * { return false; } } catch (IllegalActionException e) {
         * throw new InternalErrorException(e); } } return true;
         */
    }

    /**
     * Return a receiver that is a one-place buffer. A token put into
     * the receiver will override any token already in the receiver.
     *
     * @return A receiver that is a one-place buffer.
     */
    @Override
    public Receiver newReceiver() {
        return new FSMReceiver();
    }

    /**
     * Invoke postfire() on any state refinements that were fired,
     * then execute the commit actions contained by the last chosen
     * transition, if any, and finally set the current state
     * to the destination state of the transition. This will return
     * false if any refinement that is postfired returns false.
     * <p>
     * If any transition was taken in this iteration, and if there is
     * an executive director, and if there is a transition from the
     * new state that is currently enabled, then this method calls
     * fireAtCurrentTime(Actor) on that executive director (this call
     * occurs indirectly in the FSMActor controller). If there is an
     * enabled transition, then the current state is transient, and we
     * will want to spend zero time in it.
     *
     * @return True if the mode controller wishes to be scheduled for another iteration.
     * @exception IllegalActionException
     *                If thrown by any commit action or there is no controller.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        boolean result = true;
        if (_debugging) {
            _debug("*** postfire called at time: ", getModelTime().toString());
        }
        FSMActor controller = getController();
        result &= controller.postfire();
        _currentLocalReceiverMap = (Map) _localReceiverMaps.get(controller
                .currentState());

        // Reset all the receivers on the inside of output ports.
        // NOTE: This only has an effect for FSMReceiver.
        _resetOutputReceivers();

        return result && !_stopRequested && !_finishRequested;
    }

    /** Check whether contained refinements have a director.
     *  @exception IllegalActionException If a contained refinement
     *  does not have a director.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        Iterator<?> actors = ((CompositeActor) getContainer()).deepEntityList()
                .iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (!(actor instanceof Refinement)
                    && actor.getContainer() instanceof Refinement) {
                throw new IllegalActionException(actor.getContainer(),
                        "Refinement is missing a director!");
            }
        }

        super.preinitialize();
    }

    /**
     * Return true if the mode controller is ready to fire. If this
     * model is not at the top level and the current time of this
     * director lags behind that of the executive director, update the
     * current time to that of the executive director. Record whether
     * the refinements of the current state of the mode controller are
     * ready to fire.
     *
     * @exception IllegalActionException
     *                If there is no controller.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Prefire called at time: " + getModelTime());
        }
        super.prefire();
        return getController().prefire();
    }

    /** Rebuild the output receivers map and reset the output receivers.
     *  @exception IllegalActionException If there is no mode
     *  controller, or can not find refinements for states or if
     *  getting the receivers fails.
     */
    public void resetOutputReceivers() throws IllegalActionException {
        _buildLocalReceiverMaps();
        _resetOutputReceivers();
    }

    /**
     * If the container is not null, register this director as the model error handler.
     *
     * @param container The proposed container.
     * @exception IllegalActionException If the action would result in
     *                a recursive containment structure, or if this
     *                entity and container are not in the same
     *                workspace, or if the protected method
     *                _checkContainer() throws it, or if a contained
     *                Settable becomes invalid and the error handler
     *                throws it.
     * @exception NameDuplicationException If the name of this entity
     * collides with a name already in the container.
     */
    @Override
    public void setContainer(NamedObj container) throws IllegalActionException,
    NameDuplicationException {
        super.setContainer(container);

        if (container != null) {
            container.setModelErrorHandler(this);
        }
    }

    /**
     * Set the superdense time index by delegating to the directors of
     * the refinements of the current state, if any. This should only
     * be called by an enclosing director.
     *
     * @exception IllegalActionException
     *                Not thrown in this base class.
     * @see #getIndex()
     * @see ptolemy.actor.SuperdenseTimeDirector
     */
    @Override
    public void setIndex(int index) throws IllegalActionException {
        // FIXME: Is this right?
        Actor[] actors = _controller.currentState().getRefinement();
        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                Director destinationDirector = actors[i].getDirector();
                // If the refinement doesn't have a director, then the
                // destinationDirector would be this one! This is an error,
                // but we tolerate it here.
                if (destinationDirector != this
                        && destinationDirector instanceof SuperdenseTimeDirector) {
                    ((SuperdenseTimeDirector) destinationDirector)
                    .setIndex(index);
                }
            }
        }
    }

    /**
     * Transfer data from the input port of the container to the ports
     * connected to the inside of the input port and on the mode
     * controller or the refinement of its current state. This method
     * will transfer exactly one token on each input channel that has
     * at least one token available.  The port argument must be an
     * opaque input port. If any channel of the input port has no
     * data, then that channel is ignored. Any token left not consumed
     * in the ports to which data are transferred is discarded.
     *
     * @param port
     *            The input port to transfer tokens from.
     * @return True if at least one data token is transferred.
     * @exception IllegalActionException
     *                If the port is not an opaque input port.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque"
                            + "input port.");
        }

        boolean transferredToken = false;
        // NOTE: The following method does quite a song and dance with the "local
        // receivers map" to avoid putting the input data into all
        // refinements. Is this worth it? A much simpler design would
        // just make the input data available to all refinements, whether
        // they run or not.
        Receiver[][] insideReceivers = _currentLocalReceivers(port);

        // If the port is not connected, then the input is known to be absent
        // and we can safely send a clear to the inside.
        int numberOfSources = port.numberOfSources();
        if (numberOfSources < insideReceivers.length) {
            // At least one inside channel has no viable sources.
            for (int i = numberOfSources; i < insideReceivers.length; i++) {
                for (Receiver receiver : insideReceivers[i]) {
                    receiver.clear();
                }
            }
        }

        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (port.isKnown(i)) {
                    if (port.hasToken(i)) {
                        Token t = port.get(i);
                        // FindBugs: insideReceivers can't be null;
                        if (i < insideReceivers.length
                                && insideReceivers[i] != null) {
                            for (int j = 0; j < insideReceivers[i].length; j++) {
                                insideReceivers[i][j].put(t);
                                if (_debugging) {
                                    _debug(getFullName(), "transferring input "
                                            + t
                                            + " from "
                                            + port.getFullName()
                                            + " to "
                                            + insideReceivers[i][j]
                                                    .getContainer()
                                                    .getFullName());
                                }
                            }
                            transferredToken = true;
                        }
                    } else {
                        /** Port does not have a token. */
                        // FindBugs: insideReceivers can't be null;
                        if (insideReceivers[i] != null) {
                            for (int j = 0; j < insideReceivers[i].length; j++) {
                                if (_debugging) {
                                    _debug(getName(),
                                            "input port has no token. Clearing "
                                                    + insideReceivers[i][j]
                                                            .getContainer()
                                                            .getFullName());
                                }
                                insideReceivers[i][j].clear();
                            }
                        }
                    }
                } else {
                    /** Port status is not known. */
                    if (_debugging) {
                        _debug("Input port status is not known. Resetting inside receivers of "
                                + port.getName());
                    }
                    // FindBugs: insideReceivers can't be null;
                    if (insideReceivers[i] != null) {
                        for (int j = 0; j < insideReceivers[i].length; j++) {
                            insideReceivers[i][j].reset();
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

    /**
     * Build for each state of the mode controller the map from input ports of the modal model to
     * the local receivers when the mode controller is in that state. This method is
     * read-synchronized on the workspace.
     *
     * @exception IllegalActionException
     *                If there is no mode controller, or can not find refinements for states.
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

    /**
     * Return the receivers contained by ports connected to the inside
     * of the given input port and on the mode controller or the
     * refinement of its current state.
     *
     * @param port
     *            An input port of the container of this director.
     * @return The receivers that currently get inputs from the given port.
     * @exception IllegalActionException
     *                If there is no controller.
     */
    protected Receiver[][] _currentLocalReceivers(IOPort port)
            throws IllegalActionException {
        if (_localReceiverMapsVersion != workspace().getVersion()) {
            _buildLocalReceiverMaps();
        }

        return (Receiver[][]) _currentLocalReceiverMap.get(port);
    }

    /**
     * Return the last chosen transitions.
     *
     * @return The last chosen transitions, or null if there has been none.
     * @exception IllegalActionException
     *                If there is no controller.
     */
    protected Map<State, Transition> _getLastChosenTransition()
            throws IllegalActionException {
        FSMActor controller = getController();
        if (controller != null) {
            return controller._lastChosenTransitions;
        } else {
            return null;
        }
    }

    /** Return the list used to keep track of refinements that have been
     *  fired. This is protected so that FSMDirector can mirror it with
     *  its own protected method so that subclasses of FSMDirector can
     *  access it.
     *  @return A list of actors to postfire.
     *  @exception IllegalActionException If can't get the controller.
     */
    protected List<Actor> _getStateRefinementsToPostfire()
            throws IllegalActionException {
        FSMActor controller = getController();
        return controller._getStateRefinementsToPostfire();
    }

    /** Return the list used to keep track of refinements that have been
     *  fired. This is protected so that FSMDirector can mirror it with
     *  its own protected method so that subclasses of FSMDirector can
     *  access it.
     *  @return A list of actors to postfire.
     *  @exception IllegalActionException If can't get the controller.
     */
    protected List<Actor> _getTransitionRefinementsToPostfire()
            throws IllegalActionException {
        FSMActor controller = getController();
        return controller._getTransitionRefinementsToPostfire();
    }

    /**
     * Set the value of the shadow variables for input ports of the controller actor.
     *
     * @exception IllegalActionException
     *                If a shadow variable cannot take the token read from its corresponding channel
     *                (should not occur).
     */
    protected void _readInputs() throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            controller.readInputs();
        }
    }

    /**
     * Set the value of the shadow variables for input ports of the
     * controller actor that are defined by output ports of the
     * refinement.
     *
     * @exception IllegalActionException
     *                If a shadow variable cannot take the token read from its corresponding channel
     *                (should not occur).
     */
    protected void _readOutputsFromRefinement() throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            controller.readOutputsFromRefinement();
        }
    }

    /**
     * Set the map from input ports to boolean flags indicating
     * whether a channel is connected to an output port of the
     * refinement of the current state. This method is called by
     * HDFFSMDirector.
     *
     * @exception IllegalActionException If the refinement specified
     *                for one of the states is not valid, or if there
     *                is no controller.
     */
    protected void _setCurrentConnectionMap() throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            controller._setCurrentConnectionMap();
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }

    /**
     * Set the current state of this actor.
     *
     * @param state
     *            The state to set.
     * @exception IllegalActionException
     *                If there is no controller.
     */
    protected void _setCurrentState(State state) throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            controller._currentState = state;
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }

    /**
     * Transfer at most one data token from the given output port of
     * the container to the ports it is connected to on the
     * outside. If the receiver is known to be empty, then send a
     * clear. If the receiver status is not known, do nothing.
     *
     * @param port The port to transfer tokens from.
     * @return True if the port has an inside token that was
     *         successfully transferred. Otherwise return false (or
     *         throw an exception).
     * @exception IllegalActionException
     *                If the port is not an opaque output port.
     *
     */
    @Override
    protected boolean _transferOutputs(IOPort port)
            throws IllegalActionException {
        boolean result = false;
        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque input port.");
        }

        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                if (port.isKnownInside(i)) {
                    if (port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        if (_debugging) {
                            _debug(getName(), "transferring output " + t
                                    + " from " + port.getName());
                        }
                        port.send(i, t);
                        // mark this port as we sent a token to
                        // prevent sending a clear afterwards in this
                        // fixed point iteration
                        result = true;
                    } else {
                        // Set the port to be absent only if it is not known
                        // or not connected.
                        if (port.getWidth() <= i || !port.isKnown(i)) {
                            if (_debugging) {
                                _debug(getName(),
                                        "sending clear from " + port.getName());
                            }
                            // only send a clear (=absent) to the
                            // port, iff it is ensured that in the
                            // current state, there might not be an
                            // enabled transition (now or later) that
                            // then would produce a token on that port
                            FSMActor controller = getController();
                            // If a transition has been chosen, then it is safe to set
                            // the outputs absent. If no transition has been chosen,
                            // then we have to check to make sure that sometime later
                            // in the fixed-point iteration, it will not be possible
                            // for a transition to become enabled that might send data
                            // on this port.
                            if (controller._lastChosenTransitions.size() > 0
                                    && !controller.foundUnknown()
                                    || controller._isSafeToClear(port, i,
                                            controller._currentState, false,
                                            null)) {
                                port.send(i, null);
                            }
                        }
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Map from input ports of the modal model to the local receivers
     *  for the current state.
     */
    protected Map _currentLocalReceiverMap = null;

    /** The _indexOffset is set by FSMActor during initialization of
     *  destination refinements upon committing to a reset transition
     *  in order to ensure that the destination refinement views its
     *  index as one larger than the current index.
     */
    protected int _indexOffset = 0;

    /** Record for each state of the mode controller the map from input
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

    /** Reset the output receivers, which are the inside receivers of
     *  the output ports of the container.
     *  @exception IllegalActionException If getting the receivers fails.
     */
    private void _resetOutputReceivers() throws IllegalActionException {
        List<IOPort> outputs = ((Actor) getContainer()).outputPortList();
        for (IOPort output : outputs) {
            if (_debugging) {
                _debug("Resetting inside receivers of output port: "
                        + output.getName());
            }
            Receiver[][] receivers = output.getInsideReceivers();
            if (receivers != null) {
                for (Receiver[] receiver : receivers) {
                    if (receiver != null) {
                        for (int j = 0; j < receiver.length; j++) {
                            if (receiver[j] instanceof FSMReceiver) {
                                receiver[j].reset();
                            }
                        }
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Cached reference to mode controller. */
    private FSMActor _controller = null;

    /** Version of cached reference to mode controller. */
    private long _controllerVersion = -1;

    /** Version of the local receiver maps. */
    private long _localReceiverMapsVersion = -1;
}
