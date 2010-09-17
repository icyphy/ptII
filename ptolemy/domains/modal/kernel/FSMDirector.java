/* An FSMDirector governs the execution of a modal model.

 Copyright (c) 1999-2010 The Regents of the University of California.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * An FSMDirector governs the execution of a modal model. A modal model is a TypedCompositeActor
 * with a FSMDirector as local director. The mode control logic is captured by a mode controller, an
 * instance of FSMActor contained by the composite actor. Each state of the mode controller
 * represents a mode of operation and can be refined by an opaque CompositeActor contained by the
 * same composite actor.
 * <p>
 * The mode controller contains a set of states and transitions. A transition has a <i>guard
 * expression</i>, any number of <i>output actions</i>, and any number of <i>set actions</i>. It has
 * an <i>initial state</i>, which is the unique state whose <i>isInitialState</i> parameter is true.
 * The states and transitions can have <i>refinements</i>, which are composite actors. In outline, a
 * firing of this director is a sequence of steps:
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
 * <li>Initialize the refinements of the destination state if the transition is a reset transition.
 * <li>Execute the set actions of the chosen transition.
 * <li>Postfire the transition refinements of the chosen transition.
 * <li>Change the current state to the destination of the chosen transition.
 * </ol>
 * Since this director makes no persistent state changes in its fire() method, it conforms with the
 * <i>actor abstract semantics</i>. Assuming the state and transition refinements also conform, this
 * director can be used inside any Ptolemy II actor model of computation. How it behaves in each
 * domain, however, can be somewhat subtle, particularly with domains that have fixed-point
 * semantics and when nondeterministic transitions are used. The details are given below.
 * <p>
 * When a modal model is fired, this director first transfers the input tokens from the outside
 * domain to the mode controller and the refinement of its current state. The preemptive transitions
 * from the current state of the mode controller are examined. If there is more than one transition
 * enabled, and any of the enabled transitions is not marked nondeterministic, an exception is
 * thrown. If there is exactly one preemptive transition enabled then it is chosen. The choice
 * actions (outputActions) contained by the transition are executed. Any output token produced by
 * the mode controller is transferred to both the output ports of the modal model and the input
 * ports of the mode controller. Then the refinements associated with the enabled transition are
 * executed. Any output token produced by the refinements is transferred to both the output ports of
 * the modal model and the input ports of the mode controller. The refinements of the current state
 * will not be fired.
 * <p>
 * If no preemptive transition is enabled, the refinements of the current state are fired. Any
 * output token produced by the refinements is transferred to both the output ports of the modal
 * model and the input ports of the mode controller. After this, the non-preemptive transitions from
 * the current state of the mode controller are examined. If there is more than one transition
 * enabled, and any of the enabled transitions is not marked nondeterministic, an exception is
 * thrown. If there is exactly one non-preemptive transition enabled then it is chosen and the
 * choice actions contained by the transition are executed. Any output token produced by the mode
 * controller is transferred to the output ports of the modal model and the input ports of the mode
 * controller. Then, the refinements of the enabled transition are executed. Any output token
 * produced by the refinements is transferred to both the output ports of the modal model and the
 * input ports of the mode controller.
 * <p>
 * In a firing, it is possible that the current state refinement produces an output, and a
 * transition that is taken also produces an output on the same port. In this case, only the second
 * of these outputs will appear on the output of the composite actor containing this director.
 * However, the first of these output values, the one produced by the refinement, may affect whether
 * the transition is taken. That is, it can affect the guard. If in addition a transition refinement
 * writes to the output, then that value will be produced, overwriting the value produced either by
 * the state refinement or the output action on the transition.
 * <p>
 * At the end of one firing, the modal model transfers its outputs to the outside model. The mode
 * controller does not change state during successive firings in one iteration of the top level in
 * order to support upper level domains that iterate to a fixed point.
 * <p>
 * When the modal model is postfired, the chosen transition of the latest firing is committed. The
 * commit actions contained by the transition are executed and the current state of the mode
 * controller is set to the destination state of the transition.
 * 
 * @author Xiaojun Liu, Haiyang Zheng, Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Yellow (hyzheng)
 * @Pt.AcceptedRating Red (hyzheng)
 * @see FSMActor
 */
public class FSMDirector extends Director implements ExplicitChangeContext,
        QuasiTransparentDirector, SuperdenseTimeDirector {
    /**
     * Construct a director in the default workspace with an empty string as its name. The director
     * is added to the list of objects in the workspace. Increment the version number of the
     * workspace.
     */
    public FSMDirector() {
        super();
        _createAttribute();
    }

    /**
     * Construct a director in the workspace with an empty name. The director is added to the list
     * of objects in the workspace. Increment the version number of the workspace.
     * 
     * @param workspace
     *            The workspace of this director.
     */
    public FSMDirector(Workspace workspace) {
        super(workspace);
        _createAttribute();
    }

    /**
     * Construct a director in the given container with the given name. The container argument must
     * not be null, or a NullPointerException will be thrown. If the name argument is null, then the
     * name is set to the empty string. Increment the version number of the workspace.
     * 
     * @param container
     *            Container of this director.
     * @param name
     *            Name of this director.
     * @exception IllegalActionException
     *                If the name has a period in it, or the director is not compatible with the
     *                specified container.
     * @exception NameDuplicationException
     *                If the container not a CompositeActor and the name collides with an entity in
     *                the container.
     */
    public FSMDirector(CompositeEntity container, String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        _createAttribute();
    }

    // /////////////////////////////////////////////////////////////////
    // // public variables ////

    /**
     * Attribute specifying the name of the mode controller in the container of this director. This
     * director must have a mode controller that has the same container as this director, otherwise
     * an IllegalActionException will be thrown when action methods of this director are called.
     */
    public StringAttribute controllerName = null;

    // /////////////////////////////////////////////////////////////////
    // // public methods ////

    /**
     * React to a change in an attribute. If the changed attribute is the <i>controllerName</i>
     * attribute, then make note that this has changed.
     * 
     * @param attribute
     *            The attribute that changed.
     * @exception IllegalActionException
     *                If thrown by the superclass attributeChanged() method.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == controllerName) {
            _controllerVersion = -1;
        }
    }

    /**
     * Clone the director into the specified workspace. This calls the base class and then sets the
     * attribute public members to refer to the attributes of the new director.
     * 
     * @param workspace
     *            The workspace for the new director.
     * @return A new director.
     * @exception CloneNotSupportedException
     *                If a derived class contains an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FSMDirector newObject = (FSMDirector) super.clone(workspace);
        // Protected variables.
        newObject._currentLocalReceiverMap = null;
        newObject._enabledRefinements = null;
        newObject._disabledActors = new HashSet();
        newObject._hadToken = new LinkedList<IOPort>();
        newObject._localReceiverMaps = new HashMap();
        newObject._stateRefinementsToPostfire = new LinkedList<Actor>();
        newObject._transitionRefinementsToPostfire = new LinkedList<Actor>();

        // Private variables.
        newObject._controller = null;
        newObject._controllerVersion = -1;
        newObject._lastChosenTransition = null;
        newObject._localReceiverMapsVersion = -1;

        newObject._currentOffset = null;

        newObject._portReferencedInTransitionMaps = new HashMap<IOPort, HashMap<Transition, Boolean>>();

        return newObject;
    }

    /**
     * Return a default dependency to use between input input ports and output ports. This overrides
     * the base class so that if there is an executive director, then we get the default dependency
     * from it.
     * 
     * @see Dependency
     * @see CausalityInterface
     * @see Actor#getCausalityInterface()
     * @return A default dependency between input ports and output ports.
     */
    public Dependency defaultDependency() {
        Director executiveDirector = ((Actor) getContainer()).getExecutiveDirector();
        if (executiveDirector != null) {
            return executiveDirector.defaultDependency();
        }
        return BooleanDependency.OTIMES_IDENTITY;
    }

    /**
     * Fire the modal model for one iteration. If there is a preemptive transition enabled, execute
     * its choice actions (outputActions). Otherwise, fire the refinement of the current state.
     * After this firing, if there is a transition enabled, execute its choice actions. If any
     * tokens are produced during this iteration, they are sent to both the output ports of the
     * model model but also the input ports of the mode controller.
     * 
     * @exception IllegalActionException
     *                If there is more than one transition enabled and nondeterminism is not
     *                permitted, or there is no controller, or it is thrown by any choice action.
     */
    public void fire() throws IllegalActionException {
        Time environmentTime = _getEnvironmentTime();
        setModelTime(environmentTime);
        _stateRefinementsToPostfire.clear();
        _transitionRefinementsToPostfire.clear();
        _lastChosenTransition = null;
        FSMActor controller = getController();
        State currentState = controller.currentState();
        if (_debugging) {
            _debug("*** Firing " + getFullName(), " at time " + getModelTime());
            _debug("Current state is:", currentState.getName());
        }
        controller.readInputs();

        List<Transition> enabledPreemptiveTransitions = controller.enabledTransitions(currentState
                .preemptiveTransitionList());
        if (enabledPreemptiveTransitions.size() > 0) {
            // Do not call the following unless there is actually a preemptive transition enabled
            // because in a fixed-point iteration you could get an exception if you call
            // chooseTransition
            // with the list of preemptive transitions after a non-preemptive transition was already
            // discovered to be enabled.
            _lastChosenTransition = controller.chooseTransition(enabledPreemptiveTransitions);
            if (_debugging) {
                _debug("Preemptive transition enabled:", _lastChosenTransition.getName());
            }
            // First execute the refinements of the transition.
            Actor[] transitionRefinements = _lastChosenTransition.getRefinement();

            if (transitionRefinements != null) {
                for (int i = 0; i < transitionRefinements.length; ++i) {
                    if (_stopRequested || _disabledActors.contains(transitionRefinements[i])) {
                        break;
                    }
                    if (_debugging) {
                        _debug("Fire transition refinement:", transitionRefinements[i].getName());
                    }
                    // FIXME: What should model time be for transition refinements?
                    // It is not reasonable for it to be the time of the originating
                    // refinement because multiple transitions may share a refinement
                    // and time will end up bouncing around...
                    if (transitionRefinements[i].prefire()) {
                        transitionRefinements[i].fire();
                        _transitionRefinementsToPostfire.add(transitionRefinements[i]);
                    }
                }
            }
            // FIXME: Next line needed?
            setModelTime(environmentTime);

            // If the current (preempted) state has a refinement, then we know
            // it cannot produce any outputs. All outputs of this state must be cleared
            // so that at least they not remain unknown in the end of the fixed point iteration.
            // If an output port is known because this preempted transition alreay set it
            // we do not send a clear.
            if (controller._currentState.getRefinement() != null) {
                TypedActor[] refinements = controller._currentState.getRefinement();
                // do the following for all refinements
                for (Actor refinementActor : refinements) {
                    if (refinementActor instanceof CompositeActor) {
                        CompositeActor refinement = (CompositeActor) refinementActor;
                        for (IOPort refinementPort : ((List<IOPort>) refinement.outputPortList())) {
                            if (!refinementPort.isKnown()) {
                                refinementPort.sendClear(0);
                            }
                        }// end for all ports
                    }// end if CompositeActor
                }// end for all refinements
            }// end if has refinement

            controller.readOutputsFromRefinement();
            return;
        }

        // ASSERT: no preemptive enabled transitions, all preemptive transitions (if present)
        // cannot be evaluated yet

        // If some preemptive transition guards could not be evaluated due to unknown
        // inputs, then return now. It is not correct to execute the refinements until
        // we know that all preemptive transitions are not enabled.
        if (controller.foundUnknown()) {
            // ATTENTION: It seems we canNOT assume anything about absent outputs yet.
            // This is because we don't know anything about the relation of input and output ports.
            return;
        }

        Actor[] stateRefinements = currentState.getRefinement();

        if (stateRefinements != null) {
            for (int i = 0; i < stateRefinements.length; ++i) {
                if (_stopRequested || _disabledActors.contains(stateRefinements[i])) {
                    break;
                }
                _setTimeForRefinement(stateRefinements[i]);
                if (stateRefinements[i].prefire()) {
                    if (_debugging) {
                        _debug("Fire state refinement:", stateRefinements[i].getName());
                    }
                    // NOTE: If the state refinement is an FSMActor, then the following
                    // fire() method doesn't do the right thing. That fire() method does
                    // much less than this fire() method, and in particular, does not
                    // invoke refinements! This is fixed by using ModalModel in a
                    // hierarchical state.
                    stateRefinements[i].fire();
                    _stateRefinementsToPostfire.add(stateRefinements[i]);
                }
            }
        }
        setModelTime(environmentTime);

        controller.readOutputsFromRefinement();

        _lastChosenTransition = controller.chooseTransition(currentState
                .nonpreemptiveTransitionList());
        if (_lastChosenTransition != null) {
            if (_debugging) {
                _debug("Nonpreemptive transition enabled:", _lastChosenTransition.getName());
            }
            stateRefinements = _lastChosenTransition.getRefinement();
            if (stateRefinements != null) {
                for (int i = 0; i < stateRefinements.length; ++i) {
                    if (_stopRequested || _disabledActors.contains(stateRefinements[i])) {
                        break;
                    }
                    // FIXME: What to set time to for the refinement?
                    if (stateRefinements[i].prefire()) {
                        if (_debugging) {
                            _debug("Fire transition refinement:", stateRefinements[i].getName());
                        }
                        stateRefinements[i].fire();
                        _transitionRefinementsToPostfire.add(stateRefinements[i]);
                    }
                }
                controller.readOutputsFromRefinement();
            }
        } else {
            // If no transition was chosen, then it still might be
            // possible to assert that certain outputs are absent.
            _assertAbsentOutputs(controller, true);
        }
    }

    /**
     * It might be possible to assert that certain outputs are absent. This methods helps finding
     * these outputs and clears them.
     * 
     * @param controller
     *            the controller
     * @param checkRefinement
     *            the check refinement
     * @return true, if successful
     * @throws IllegalActionException
     *             the illegal action exception
     */
    private boolean _assertAbsentOutputs(FSMActor controller, boolean checkRefinement)
            throws IllegalActionException {
        // Next, for each output port, we need to check whether
        // it is possible for an output to be produced on that port.
        // It will not be possible iff ALL transitions that can produce
        // the output have guards that evaluate to false.
        List<IOPort> outputs = controller.outputPortList();
        // FIXME: maybe ask refinements too and send a clear out ONLY

        boolean foundAbsentOutputs = false;

        for (IOPort port : outputs) {

            for (int channel = 0; channel < port.getWidth(); channel++) {
                if (!port.isKnown(channel)) {
                    // Check all transitions. If no transition can
                    // possibly produce this output, set the output absent.
                    boolean isSafeToClear = true;

                    // Only check refinements iff non-preemptive transition (weak abort).
                    // For strong aborts this method should be called w/ checkRefinement==false!
                    if (checkRefinement && controller._currentState.getRefinement() != null) {
                        // this states has a refinement
                        // in addition to the check below it is required that the refinement
                        // has also cleared a port
                        TypedActor[] refinements = controller._currentState.getRefinement();
                        // do the following for all refinements
                        for (Actor refinementActor : refinements) {
                            if (refinementActor instanceof CompositeActor) {
                                CompositeActor refinement = (CompositeActor) refinementActor;
                                for (IOPort refinementPort : ((List<IOPort>) refinement
                                        .outputPortList())) {
                                    if (!refinementPort.getName().equals(port.getName())) {
                                        // if not the same port name, don't inspect channels
                                        continue;
                                    }
                                    // if the correct port name, get the right channel
                                    for (int refinementChannel = 0; refinementChannel < refinementPort
                                            .getWidth(); refinementChannel++) {
                                        if (refinementChannel == channel) {
                                            // this is the corresponding refinement port and channel
                                            // check whether it is known (=> !cleared). if not,
                                            // prevent
                                            // clearing by setting guardsEvaluable to false
                                            if (!refinementPort.isKnown()) {
                                                // only if it is really cleared (= not known because
                                                // of a token)
                                                isSafeToClear = false;
                                            }
                                        }// end if channel and port fits
                                    }// end for all channels
                                }// end for all ports
                            }// end if CompositeActor
                        }// end for all refinements
                    }// end if has refinement

                    // Reason for "&&":
                    // If we know already that a refinement didn't cleared
                    // this channel[port], then we don't need to inspect any further
                    // because this already prevents us from clearing the channel safely.
                    isSafeToClear = isSafeToClear && _isSafeToClear(port, controller);
                    if (isSafeToClear) {
                        foundAbsentOutputs = true;
                        port.send(channel, null);
                        _debug("Asserting absence and clearing port " + port.getName());
                    }
                } else {
                    if (port.isInput() && port.hasToken(0)) {
                        _debug("Token at port " + port.getName());
                    } else {
                        _debug("Unknown token or output port " + port.getName());
                    }
                }
            }
        }

        // return if something has changed
        return foundAbsentOutputs;
    }

    /**
     * Schedule a firing of the given actor at the given time. If there exists an executive
     * director, this method delegates to the fireAt() method of the executive director by
     * requesting a firing of the container of this director at the given time adjusted by the
     * current offset between local time and the environment time.
     * <p>
     * If there is no executive director, then the request results in model time of this director
     * being set to the specified time. The reason for this latter behavior is to support models
     * where FSM is at the top level. A director inside the state refinements could be timed, and
     * expects time to advance in its environment between firings. It typically makes a call to
     * fireAt() at the conclusion of each iteration to specify the time value it expects to next
     * see. Such directors can thus be used inside top-level FSM models. For example, the DEDirector
     * and SDFDirector behave exactly this way.
     * 
     * @param actor
     *            The actor scheduled to be fired.
     * @param time
     *            The scheduled time.
     * @return The time at which the actor passed as an argument will be fired.
     * @exception IllegalActionException
     *                If thrown by the executive director.
     */
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        // Note that the actor parameter is ignored, because it does not
        // matter which actor requests firing.
        if (_currentOffset != null) {
            time = time.add(_currentOffset);
        }
        Nameable container = getContainer();
        if (container instanceof Actor) {
            Actor modalModel = (Actor) container;
            Director executiveDirector = modalModel.getExecutiveDirector();
            if (executiveDirector != null) {
                Time result = executiveDirector.fireAt(modalModel, time);
                if (_currentOffset != null) {
                    result = result.subtract(_currentOffset);
                }
                return result;
            }
        }
        // FIXME: Is this still valid?
        setModelTime(time);
        return time;
    }

    /**
     * Return the mode controller of this director. The name of the mode controller is specified by
     * the <i>controllerName</i> attribute. The mode controller must have the same container as this
     * director. This method is read-synchronized on the workspace.
     * 
     * @return The mode controller of this director.
     * @exception IllegalActionException
     *                If no controller is found.
     */
    public FSMActor getController() throws IllegalActionException {
        if (_controllerVersion == workspace().getVersion()) {
            return _controller;
        }

        try {
            workspace().getReadAccess();

            String name = controllerName.getExpression();

            if (name == null) {
                throw new IllegalActionException(this, "No name for mode " + "controller is set.");
            }

            Nameable container = getContainer();

            if (!(container instanceof CompositeActor)) {
                throw new IllegalActionException(this, "No controller found.");
            }

            CompositeActor cont = (CompositeActor) container;
            Entity entity = cont.getEntity(name);

            if (entity == null) {
                throw new IllegalActionException(this, "No controller found " + "with name " + name);
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
    public Entity getContext() {
        return (Entity) getContainer();
    }

    /**
     * Override the base class so that if any outgoing transition has a guard that evaluates to
     * true, then return the current time. Otherwise, delegate to the enclosing director.
     */
    public Time getModelNextIterationTime() {
        try {
            FSMActor controller = getController();
            List transitionList = controller.currentState().outgoingPort.linkedRelationList();
            List enabledTransitions = controller.enabledTransitions(transitionList);
            if (enabledTransitions.size() > 0) {
                return _currentTime;
            }
            // The result returned below needs to be adjusted by the current offset.
            Time result = super.getModelNextIterationTime();
            if (_currentOffset != null) {
                result = result.subtract(_currentOffset);
            }
            return result;
        } catch (IllegalActionException e) {
            // Any exception here should have shown up before now.
            throw new InternalErrorException(e);
        }
    }

    /**
     * Return a list of variables that are modified in a modal model. The variables are assumed to
     * have a change context of the container of this director. This class returns all variables
     * that are assigned in the actions of transitions.
     * 
     * @return A list of variables.
     * @exception IllegalActionException
     *                If no controller can be found, or the variables to be assigned by the actions
     *                can not be found.
     */
    public List getModifiedVariables() throws IllegalActionException {
        List list = new LinkedList();

        // Collect assignments from FSM transitions
        for (Iterator states = getController().entityList().iterator(); states.hasNext();) {
            State state = (State) states.next();

            for (Iterator transitions = state.outgoingPort.linkedRelationList().iterator(); transitions
                    .hasNext();) {
                Transition transition = (Transition) transitions.next();

                for (Iterator actions = transition.choiceActionList().iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions.next();

                    for (Iterator names = action.getDestinationNameList().iterator(); names
                            .hasNext();) {
                        String name = (String) names.next();
                        NamedObj object = action.getDestination(name);

                        if (object instanceof Variable) {
                            list.add(object);
                        }
                    }
                }

                for (Iterator actions = transition.commitActionList().iterator(); actions.hasNext();) {
                    AbstractActionsAttribute action = (AbstractActionsAttribute) actions.next();

                    for (Iterator names = action.getDestinationNameList().iterator(); names
                            .hasNext();) {
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
     * Return the parse tree evaluator used to evaluate guard expressions. In this base class, an
     * instance of {@link ParseTreeEvaluator} is returned. The derived classes may need to override
     * this method to return different parse tree evaluators.
     * 
     * @return ParseTreeEvaluator used to evaluate guard expressions.
     */
    public ParseTreeEvaluator getParseTreeEvaluator() {
        return new ParseTreeEvaluator();
    }

    /**
     * Return a superdense time index for the current time. This method delegates to the executive
     * director, if there is one that implements SuperdenseTimeDirector, and returns current time
     * with index 0 otherwise.
     * 
     * @return A superdense time index.
     * @see #setIndex(int)
     * @see ptolemy.actor.SuperdenseTimeDirector
     */
    public int getIndex() {
        Director executiveDirector = ((Actor) getContainer()).getExecutiveDirector();
        if (executiveDirector instanceof SuperdenseTimeDirector) {
            return ((SuperdenseTimeDirector) executiveDirector).getIndex() + _indexOffset;
        }
        return 0;
    }

    /**
     * Return true if the model errors are handled. Otherwise, return false and the model errors are
     * passed to the higher level in hierarchy.
     * <p>
     * In this method, model errors including multipleEnabledTransitionException and
     * InvariantViolationException are handled.
     * <p>
     * In the current design, if multiple enabled transitions are detected, an exception will be
     * thrown. For future designs, different ways to handle this situation will be introduced here.
     * <p>
     * When an invariant is violated, this method checks whether there exists an enabled
     * (non-preemptive) transition. If there is one, the model error is ignored and this director
     * will handle the enabled transition later. Otherwise, an exception will be thrown.
     * 
     * @param context
     *            The context where the model error happens.
     * @param exception
     *            An exception that represents the model error.
     * @return True if the error has been handled, false if the model error is passed to the higher
     *         level.
     * @exception IllegalActionException
     *                If multiple enabled transition is detected, or mode controller can not be
     *                found, or can not read outputs from refinements.
     */
    public boolean handleModelError(NamedObj context, IllegalActionException exception)
            throws IllegalActionException {
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

    /**
     * Return true if all state refinements have directors that implement the strict actor semantics
     * and if the enclosing executive director also returns true.
     * 
     * @return True if the director assumes and exports strict actor semantics.
     */
    public boolean implementsStrictActorSemantics() {
        Nameable container = getContainer();
        if (container instanceof Actor) {
            Actor modalModel = (Actor) container;
            Director executiveDirector = modalModel.getExecutiveDirector();
            if (executiveDirector != null && !executiveDirector.implementsStrictActorSemantics()) {
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
                        // and the call director.implementsStrictActorSemantics() would lead to a
                        // infinite
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

    /**
     * Initialize the mode controller and all the refinements by calling the initialize() method in
     * the super class. Build the local maps for receivers. Suspend all the refinements of states
     * that are not the current state.
     * 
     * @exception IllegalActionException
     *                If thrown by the initialize() method of the super class, or can not find mode
     *                controller, or can not find refinement of the current state.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _buildLocalReceiverMaps();
        _resetOutputReceivers();
        _disabledActors.clear();

        // Suspend the refinements of all non-initial states at the start time.
        List<State> states = getController().entityList();
        for (State state : states) {
            if (((BooleanToken) state.isInitialState.getToken()).booleanValue()) {
                continue;
            }
            TypedActor[] refinements = state.getRefinement();
            if (refinements != null) {
                for (TypedActor refinement : refinements) {
                    if (refinement instanceof Suspendable) {
                        ((Suspendable) refinement).suspend(_currentTime);
                    }
                }
            }
        }
    }

    /**
     * Indicate that a schedule for the model may no longer be valid, if there is a schedule. This
     * method should be called when topology changes are made, or for that matter when any change
     * that may invalidate the schedule is made. In this class, delegate to the executive director.
     * This is because changes in the FSM may affect the causality interface of the FSM, which may
     * be used in scheduling by the enclosing director.
     */
    public void invalidateSchedule() {
        Director executiveDirector = ((Actor) getContainer()).getExecutiveDirector();
        if (executiveDirector != null) {
            executiveDirector.invalidateSchedule();
        }
    }

    /**
     * Return false. This director checks inputs to see whether they are known before evaluating
     * guards, so it can fired even if it has unknown inputs.
     * 
     * @return False.
     * @exception IllegalActionException
     *                Not thrown in this base class.
     */
    public boolean isStrict() throws IllegalActionException {
        return false;
        /*
         * NOTE: This used to return a value as follows based on the causality interface. But this
         * is conservative and prevents using the director in some models. Actor container =
         * (Actor)getContainer(); CausalityInterface causality = container.getCausalityInterface();
         * int numberOfOutputs = container.outputPortList().size(); Collection<IOPort> inputs =
         * container.inputPortList(); for (IOPort input : inputs) { // If the input is also output,
         * skip it. // This is the output of a refinement. if (input.isOutput()) { continue; } try {
         * if (causality.dependentPorts(input).size() < numberOfOutputs) { return false; } } catch
         * (IllegalActionException e) { throw new InternalErrorException(e); } } return true;
         */
    }

    /**
     * Return a receiver that is a one-place buffer. A token put into the receiver will override any
     * token already in the receiver.
     * 
     * @return A receiver that is a one-place buffer.
     */
    public Receiver newReceiver() {
        return new FSMReceiver();
    }

    /**
     * Invoke postfire() on any state refinements that were fired, then execute the commit actions
     * contained by the last chosen transition, if any, then invoke postfire() on any transition
     * refinements that were fired, and finally set the current state to the destination state of
     * the transition. This will return false if any refinement that is postfired returns false.
     * <p>
     * If any transition was taken in this iteration, and if there is an executive director, and if
     * there is a transition from the new state that is currently enabled, then this method calls
     * fireAtCurrentTime(Actor) on that executive director (this call occurs indirectly in the
     * FSMActor controller). If there is an enabled transition, then the current state is transient,
     * and we will want to spend zero time in it.
     * 
     * @return True if the mode controller wishes to be scheduled for another iteration.
     * @exception IllegalActionException
     *                If thrown by any commit action or there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = true;
        if (_debugging) {
            _debug("*** postfire called at time: ", getModelTime().toString());
        }
        FSMActor controller = getController();

        Time environmentTime = _getEnvironmentTime();
        for (Actor stateRefinement : _stateRefinementsToPostfire) {
            if (_debugging) {
                _debug("Postfiring state refinment:", stateRefinement.getName());
            }
            _setTimeForRefinement(stateRefinement);
            if (!stateRefinement.postfire()) {
                _disabledActors.add(stateRefinement);
                // It is not correct for the modal model to return false
                // just because the refinement doesn't want to be fired anymore.
                // result = false;
            }
            setModelTime(environmentTime);
        }
        // Suspend all refinements of the current state, whether they were fired
        // or not. This is important because if a preemptive transition was taken,
        // then the refinement was not fired, but it should still be suspended.
        Actor[] refinements = controller.currentState().getRefinement();
        if (refinements != null) {
            for (Actor stateRefinement : refinements) {
                if (_lastChosenTransition != null && stateRefinement instanceof Suspendable) {
                    ((Suspendable) stateRefinement).suspend(environmentTime);
                }
            }
        }

        // Notify all the refinements of the destination state that they are being
        // resumed.
        if (_lastChosenTransition != null) {
            State destinationState = _lastChosenTransition.destinationState();
            if (destinationState != null) {
                TypedActor[] destinationRefinements = destinationState.getRefinement();
                if (destinationRefinements != null) {
                    for (TypedActor destinationRefinement : destinationRefinements) {
                        if (destinationRefinement instanceof Suspendable) {
                            ((Suspendable) destinationRefinement).resume(environmentTime);
                        }
                    }
                }
            }
        }

        result &= controller.postfire();

        for (Actor transitionRefinement : _transitionRefinementsToPostfire) {
            if (_debugging) {
                _debug("Postfiring transition refinment:", transitionRefinement.getName());
            }
            // FIXME: What to set time to?
            if (!transitionRefinement.postfire()) {
                _disabledActors.add(transitionRefinement);
                // It is not correct for the modal model to return false
                // just because the refinement doesn't want to be fired anymore.
                // result = false;
            }
        }

        _currentLocalReceiverMap = (Map) _localReceiverMaps.get(controller.currentState());

        // Reset all the receivers on the inside of output ports.
        // FIXME: Check derived classes. This may only make sense for FSMReceiver.
        _resetOutputReceivers();

        // clear this runtime list of ports to remember that a token has passed thru
        _hadToken.clear();

        return result && !_stopRequested && !_finishRequested;
    }

    /**
     * Return true if the mode controller is ready to fire. If this model is not at the top level
     * and the current time of this director lags behind that of the executive director, update the
     * current time to that of the executive director. Record whether the refinements of the current
     * state of the mode controller are ready to fire.
     * 
     * @exception IllegalActionException
     *                If there is no controller.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Prefire called at time: " + getModelTime());
        }
        // Set the current time based on the enclosing class.
        super.prefire();
        return getController().prefire();
    }

    /**
     * If the container is not null, register this director as the model error handler.
     * 
     * @param container
     *            The proposed container.
     * @exception IllegalActionException
     *                If the action would result in a recursive containment structure, or if this
     *                entity and container are not in the same workspace, or if the protected method
     *                _checkContainer() throws it, or if a contained Settable becomes invalid and
     *                the error handler throws it.
     * @exception NameDuplicationException
     *                If the name of this entity collides with a name already in the container.
     */
    public void setContainer(NamedObj container) throws IllegalActionException,
            NameDuplicationException {
        super.setContainer(container);

        if (container != null) {
            container.setModelErrorHandler(this);
        }
    }

    /**
     * Set the superdense time index by delegating to the directors of the refinements of the
     * current state, if any. This should only be called by an enclosing director.
     * 
     * @exception IllegalActionException
     *                Not thrown in this base class.
     * @see #getIndex()
     * @see ptolemy.actor.SuperdenseTimeDirector
     */
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
                    ((SuperdenseTimeDirector) destinationDirector).setIndex(index);
                }
            }
        }
    }

    /**
     * Set a new value to the current time of the model, where the new time can be earlier than the
     * current time. It allows the set time to be earlier than the current time. This feature is
     * needed when switching between timed and untimed models.
     * 
     * @param newTime
     *            The new current simulation time.
     * @exception IllegalActionException
     *                Not thrown in this base class.
     */
    public void setModelTime(Time newTime) throws IllegalActionException {
        _currentTime = newTime;
        // If you are setting model time to an offset, then you should
        // override the following after the call to here.
        _currentOffset = null;
    }

    /**
     * Transfer data from the input port of the container to the ports connected to the inside of
     * the input port and on the mode controller or the refinement of its current state. This method
     * will transfer exactly one token on each input channel that has at least one token available.
     * The port argument must be an opaque input port. If any channel of the input port has no data,
     * then that channel is ignored. Any token left not consumed in the ports to which data are
     * transferred is discarded.
     * 
     * @param port
     *            The input port to transfer tokens from.
     * @return True if at least one data token is transferred.
     * @exception IllegalActionException
     *                If the port is not an opaque input port.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferInputs: port argument is not an opaque" + "input port.");
        }

        boolean transferredToken = false;
        // NOTE: The following method does quite a song and dance with the "local
        // receivers map" to avoid putting the input data into all
        // refinements. Is this worth it? A much simpler design would
        // just make the input data available to all refinements, whether
        // they run or not.
        Receiver[][] insideReceivers = _currentLocalReceivers(port);

        for (int i = 0; i < port.getWidth(); i++) {
            try {
                if (port.isKnown(i)) {
                    if (port.hasToken(i)) {
                        Token t = port.get(i);
                        if ((insideReceivers != null) && (insideReceivers[i] != null)) {
                            for (int j = 0; j < insideReceivers[i].length; j++) {
                                insideReceivers[i][j].put(t);
                                if (_debugging) {
                                    _debug(getFullName(), "transferring input " + t + " from "
                                            + port.getFullName() + " to "
                                            + (insideReceivers[i][j]).getContainer().getFullName());
                                }
                            }
                            transferredToken = true;
                        }
                    } else {
                        /** Port does not have a token. */
                        if ((insideReceivers != null) && (insideReceivers[i] != null)) {
                            for (int j = 0; j < insideReceivers[i].length; j++) {
                                if (_debugging) {
                                    _debug(getName(), "input port has no token. Clearing "
                                            + (insideReceivers[i][j]).getContainer().getFullName());
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
                    if ((insideReceivers != null) && (insideReceivers[i] != null)) {
                        for (int j = 0; j < insideReceivers[i].length; j++) {
                            insideReceivers[i][j].reset();
                        }
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException("Director.transferInputs: Internal error: "
                        + ex.getMessage());
            }
        }
        return transferredToken;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected methods ////

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
                            Nameable cont = receiver.getContainer().getContainer();

                            if (cont == controller) {
                                resultsList.add(receiver);
                            } else {
                                // check transitions
                                Iterator transitions = state.nonpreemptiveTransitionList()
                                        .iterator();

                                while (transitions.hasNext()) {
                                    Transition transition = (Transition) transitions.next();
                                    _checkActorsForReceiver(transition.getRefinement(), cont,
                                            receiver, resultsList);
                                }

                                // check refinements
                                List stateList = new LinkedList();
                                stateList.add(state);
                                transitions = state.preemptiveTransitionList().iterator();

                                while (transitions.hasNext()) {
                                    Transition transition = (Transition) transitions.next();
                                    stateList.add(transition.destinationState());
                                    _checkActorsForReceiver(transition.getRefinement(), cont,
                                            receiver, resultsList);
                                }

                                Iterator nextStates = stateList.iterator();

                                while (nextStates.hasNext()) {
                                    actors = ((State) nextStates.next()).getRefinement();
                                    _checkActorsForReceiver(actors, cont, receiver, resultsList);
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
            _currentLocalReceiverMap = (Map) _localReceiverMaps.get(controller.currentState());
        } finally {
            workspace().doneReading();
        }
    }

    /**
     * If the execution stops, that should result in clearing the HashMaps because the user might
     * change the model (transition-labels).
     */
    public void stop() {
        // FIXME: This is not the right way to do this!!!
        super.stop();
        this._portReferencedInTransitionMaps.clear();
    }

    /**
     * Return the enabled transition among the given list of transitions. Throw an exception if
     * there is more than one transition enabled. This method is called by subclasses of FSMDirector
     * in other packages.
     * 
     * @param transitionList
     *            A list of transitions.
     * @return An enabled transition, or null if none is enabled.
     * @exception IllegalActionException
     *                If there is more than one transition enabled, or if thrown by any choice
     *                action contained by the enabled transition, or if there is no controller.
     */
    protected Transition _chooseTransition(List transitionList) throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            return controller.chooseTransition(transitionList);
        } else {
            throw new IllegalActionException(this, "No controller!");
        }
    }

    /**
     * Return the receivers contained by ports connected to the inside of the given input port and
     * on the mode controller or the refinement of its current state.
     * 
     * @param port
     *            An input port of the container of this director.
     * @return The receivers that currently get inputs from the given port.
     * @exception IllegalActionException
     *                If there is no controller.
     */
    protected Receiver[][] _currentLocalReceivers(IOPort port) throws IllegalActionException {
        if (_localReceiverMapsVersion != workspace().getVersion()) {
            _buildLocalReceiverMaps();
        }

        return (Receiver[][]) _currentLocalReceiverMap.get(port);
    }

    /**
     * If the specified actor is currently enabled (because it previously returned false from
     * postfire), then re-enable it. This is called by FSMActor upon taking a reset transition.
     * 
     * @param actor
     *            The actor to re-enable.
     */
    protected void _enableActor(Actor actor) {
        _disabledActors.remove(actor);
    }

    /**
     * Return the last chosen transition.
     * 
     * @return The last chosen transition, or null if there has been none.
     * @exception IllegalActionException
     *                If there is no controller.
     */
    public Transition _getLastChosenTransition() throws IllegalActionException {
        FSMActor controller = getController();

        if (controller != null) {
            return controller._lastChosenTransition;
        } else {
            return null;
        }
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
     * Set the value of the shadow variables for input ports of the controller actor that are
     * defined by output ports of the refinement.
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
     * Set the map from input ports to boolean flags indicating whether a channel is connected to an
     * output port of the refinement of the current state. This method is called by HDFFSMDirector.
     * 
     * @exception IllegalActionException
     *                If the refinement specified for one of the states is not valid, or if there is
     *                no controller.
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
     * Transfer at most one data token from the given output port of the container to the ports it
     * is connected to on the outside. If the receiver is known to be empty, then send a clear. If
     * the receiver status is not known, do nothing.
     * 
     * @exception IllegalActionException
     *                If the port is not an opaque output port.
     * @param port
     *            The port to transfer tokens from.
     * @return True if the port has an inside token that was successfully transferred. Otherwise
     *         return false (or throw an exception).
     * 
     */
    protected boolean _transferOutputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that " + "is not an opaque input port.");
        }

        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                if (port.isKnownInside(i)) {
                    if (port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        if (_debugging) {
                            _debug(getName(), "transferring output " + t + " from "
                                    + port.getName());
                        }
                        port.send(i, t);
                        // mark this port as we sent a token to prevent sending a clear afterwards
                        // in this fixed point iteration
                        _hadToken.add(port);
                        result = true;
                    } else {
                        if (!_hadToken.contains(port)) {
                            if (_debugging) {
                                _debug(getName(), "sending clear from " + port.getName());
                            }
                            // only send a clear (=absent) to the port, iff it is ensured that
                            // in the current state, there might not be an enabled transition (now
                            // or later)
                            // that then would produce a token on that port
                            FSMActor controller = getController();
                            // If a transition has been chosen, then it is safe to set
                            // the outputs absent. If no transition has been chosen,
                            // then we have to check to make sure that sometime later
                            // in the fixed-point iteration, it will not be possible
                            // for a transition to become enabled that might send data
                            // on this port.
                            if ((controller.getLastChosenTransition() != null) ||
                                    _isSafeToClear(port, controller)) {
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

    /**
     * Grab the HashMap for all transitions where this port is referenced in the output actions.
     * 
     * @param port
     *            the IOPort in question
     * @return the transition map
     */
    protected HashMap<Transition, Boolean> _getTransitionMap(IOPort port) {
        HashMap transitionMap = null;
        if (_portReferencedInTransitionMaps.containsKey(port)) {
            transitionMap = _portReferencedInTransitionMaps.get(port);
        } else {
            transitionMap = new HashMap<Transition, Boolean>();
            _portReferencedInTransitionMaps.put(port, transitionMap);
        }
        return transitionMap;
    }

    /**
     * Search the current state (controller level) for any outgoing
     * transition that could possibly output a token on the
     * port. Return true if there is any such transition, i.e., a
     * transition with a guard that cannot be evaluated to false (yet)
     * and an output action writing to the specific port. Return false
     * iff it safe to send a clear for this port (according to the
     * current hierarchy level). Note that refinements could possibly
     * have to be evaluated also.
     * 
     * <p>ATTENTION: This method should only be called if there are NO
     * enabled transitions.</p>
     * 
     * <p>FIXME: The implementation should not re-parse the output
     * actions and get the information from the parsed AST.</p>
     * 
     * @param port the IOPort in question
     * @param controller the controller for considering the current state
     * @return true, if successful
     */
    protected boolean _isSafeToClear(IOPort port, FSMActor controller) {
        if (_debugging) {
            _debug("Calling _isGuardsEvaluable on port: " + port.getFullName());
        }
        boolean isSafeToClear = true;
        // grab the HashMap for all transitions where this port is referenced
        // in the output actions
        HashMap transitionMap = _getTransitionMap(port);

        List<Transition> transitionList = controller._currentState.outgoingPort
                .linkedRelationList();
        for (Transition transition : transitionList) {
            // Determine whether the transition includes an assignment
            // to this port.  Use a HashMap for each port to save
            // booleans for each transition
            Boolean matches;
            if (false && transitionMap.containsKey(transition)) {
                matches = (Boolean) transitionMap.get(transition);
            } else {
                String outputActionsExpression = transition.outputActions.getExpression();
                String regexp = "(^|((.|\\s)*\\W))" + port.getName() + "\\s*=[^=](.|\\s)*";
                matches = (outputActionsExpression.trim().matches(regexp));
                transitionMap.put(transition, matches);
            }
            if (matches) {
                // Next check to see whether the guard evaluates to false.
                try {
                    if (!transition.isEnabled()) {
                        // if the transition is not enabled and NO
                        // unknown-variable-error occurs this means
                        // the transition guard could (already) be
                        // evaluated to false => from this perspective
                        // it is okay to clear outputs (so don't
                        // prevent that) guardsEvaluable = false;
                        // break;
                    } else if (transition.isEnabled()) {
                        // in this case the transition is for sure
                        // evaluable BUT not "possibly".  because the
                        // trigger is true, we must not set port to
                        // absent
                        isSafeToClear = false;
                        break;
                    }
                } catch (IllegalActionException ex) {
                    // Guard cannot be evaluated. Cannot set this port
                    // to absent (yet).
                    isSafeToClear = false;
                    break;
                }
            }
        }
        return isSafeToClear;
    }

    // /////////////////////////////////////////////////////////////////
    // // protected variables ////

    /**
     * Map from input ports of the modal model to the local receivers for the current state.
     */
    protected Map _currentLocalReceiverMap = null;

    /** Actors that have returned false in postfire(). */
    protected Set _disabledActors = new HashSet();

    /**
     * The list of enabled actors that refines the current state.
     */
    // FIXME: this will help to improve performance when firing. However,
    // it is only used in the HSFSMDirector. Modify the fire() method.
    // Or this may not be necessary.
    protected List _enabledRefinements;

    /**
     * The _indexOffset is set by FSMActor during initialization of destination refinements upon
     * committing to a reset transition in order to ensure that the destination refinement views its
     * index as one larger than the current index.
     */
    protected int _indexOffset = 0;

    /**
     * Stores for each state of the mode controller the map from input ports of the modal model to
     * the local receivers when the mode controller is in that state.
     */
    protected Map _localReceiverMaps = new HashMap();

    /**
     * A HashMap of transition-boolean-pairs for each port indicating if the port is contained in an
     * output action of the specific transition. This information is lazily inserted into the
     * HashMap during the simulation for performance reasons. It is not pre-done because during a
     * run we may never traverse all nodes defined.
     */
    protected HashMap<IOPort, HashMap<Transition, Boolean>> _portReferencedInTransitionMaps = new HashMap<IOPort, HashMap<Transition, Boolean>>();

    /** State refinements to postfire(), as determined by the fire() method. */
    protected List<Actor> _stateRefinementsToPostfire = new LinkedList<Actor>();

    /** Transition refinements to postfire(), as determined by the fire() method. */
    protected List<Actor> _transitionRefinementsToPostfire = new LinkedList<Actor>();

    // /////////////////////////////////////////////////////////////////
    // // private methods ////
    private void _checkActorsForReceiver(TypedActor[] actors, Nameable cont, Receiver receiver,
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

    /**
     * Return the environment time.
     * 
     * @return The current environment time, or the local modal time if there is no executive
     *         director.
     */
    private Time _getEnvironmentTime() {
        Actor container = (Actor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        if (executiveDirector != null) {
            Time environmentTime = executiveDirector.getModelTime();
            return environmentTime;
        }
        return getModelTime();
    }

    /**
     * Reset the output receivers, which are the inside receivers of the output ports of the
     * container.
     * 
     * @exception IllegalActionException
     *                If getting the receivers fails.
     */
    private void _resetOutputReceivers() throws IllegalActionException {
        List<IOPort> outputs = ((Actor) getContainer()).outputPortList();
        for (IOPort output : outputs) {
            if (_debugging) {
                _debug("Resetting inside receivers of output port: " + output.getName());
            }
            Receiver[][] receivers = output.getInsideReceivers();
            if (receivers != null) {
                for (int i = 0; i < receivers.length; i++) {
                    if (receivers[i] != null) {
                        for (int j = 0; j < receivers[i].length; j++) {
                            if (receivers[i][j] instanceof FSMReceiver) {
                                receivers[i][j].reset();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * If the specified refinement implements Suspendable, then set its current time equal to the
     * current environment time minus the refinement's total accumulated suspension time. Otherwise,
     * set current time to match that of the environment. If there is no environment, do nothing.
     * 
     * @param refinement
     *            The refinement.
     * @exception IllegalActionException
     *                If setModelTime() throws it.
     */
    private void _setTimeForRefinement(Actor refinement) throws IllegalActionException {
        Actor container = (Actor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        if (executiveDirector != null) {
            Time environmentTime = executiveDirector.getModelTime();
            if (refinement instanceof Suspendable) {
                // Adjust current time to be the environment time minus
                // the accumulated suspended time of the refinement.
                Time suspendedTime = ((Suspendable) refinement).accumulatedSuspendTime();
                if (suspendedTime != null) {
                    setModelTime(environmentTime.subtract(suspendedTime));
                    _currentOffset = suspendedTime;
                    return;
                }
            }
            setModelTime(environmentTime);
        }
    }

    // /////////////////////////////////////////////////////////////////
    // // private variables ////

    /** Cached reference to mode controller. */
    private FSMActor _controller = null;

    /** Version of cached reference to mode controller. */
    private long _controllerVersion = -1;

    /** The current offset indicating how far behind environment time local time is. */
    private Time _currentOffset;

    /** Ports that had seen a Token to prevent clearing them afterwards. */
    private LinkedList<IOPort> _hadToken = new LinkedList<IOPort>();

    /** The last chosen transition, or null if none was chosen. */
    private Transition _lastChosenTransition;

    /** Version of the local receiver maps. */
    private long _localReceiverMapsVersion = -1;
}
