/* An HSFSMDirector governs the execution of the discrete dynamics of a
 hybrid system model.

 Copyright (c) 2005 The Regents of the University of California.
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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.util.Time;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.ct.kernel.CTExecutionPhase;
import ptolemy.domains.ct.kernel.CTGeneralDirector;
import ptolemy.domains.ct.kernel.CTReceiver;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// HSFSMDirector

/**
 FIXME: this director is a new design of the HSFSMDirector. It has a lot of
 duplication. It is still very preliminary. This director will eventually
 replace the HSFSMDirector. When replacing, uncomment the getErrorTolerance()
 method.

 An HSFSMDirector governs the execution of the discrete dynamics of a hybrid
 system model.
 <p>
 <a href="
 http://ptolemy.eecs.berkeley.edu/publications/papers/99/hybridsimu/">
 Hierarchical Hybrid System Simulation</a> describes how hybrid system models
 are built and simulated in Ptolemy II. A detailed discussion about the
 underlying semantics can be found at <a href="
 http://ptolemy.eecs.berkeley.edu/publications/papers/05/OperationalSemantics
 ">Operational Semantics of Hybrid Systems</a>.

 @author Xiaojun Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (liuxj)
 */
public class NewHSFSMDirector extends HSFSMDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public NewHSFSMDirector() {
        super();
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
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public NewHSFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public NewHSFSMDirector(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Iterate the enbled refinements to emit the current states of their
     *  dynamic actors.
     *  @exception IllegalActionException If the current states can not
     *  be emitted.
     */
    public void emitCurrentStates() throws IllegalActionException {
        Iterator actors = _enabledRefinements.iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (actor instanceof CTCompositeActor) {
                ((CTCompositeActor) actor).emitCurrentStates();
            }
        }
    }

    /** FIXME: ideal design.
     *  In continuous phase of execution, fire all refinements. The
     *  isStateAccurate() and isOutputAccurate() methods are called to
     *  ensure the execution stops at the time where an event happens.
     *  In discrete phase of execution, execute the enabled transition
     *  and refinements. Note that if both transition and refinements assign
     *  an output values, the value from the refinement is kept.
     *  NOTE: we don't need refiring. the next time CT director prefires the
     *  model, the hasCurrentEvent() will be responsible to check whether
     *  a transition is enabled. The is*Accurate() method will set the local
     *  variable _transitionHasEvent to true if there is an event, just like the
     *  LevelCrossingDetector actor.
     *
     *  Set the values of input variables in the mode controller. Examine
     *  the preemptive outgoing transitions of its current state. Throw an
     *  exception if there is more than one transition enabled. If there
     *  is exactly one preemptive transition enabled then it is chosen and
     *  the choice actions contained by the transition are executed. The
     *  refinement of the current state of the mode controller will not be
     *  fired. If no preemptive transition is enabled and the refinement is
     *  ready to fire in the current iteration, fire the refinement. If this
     *  refinement has not been fired before, or needs initialization,
     *  establish the initial states of continuous variables. After this, the
     *  non-preemptive transitions from the current state of the mode
     *  controller are examined. If there is more than one transition
     *  enabled, an exception is thrown. If there is exactly one
     *  non-preemptive transition enabled then it is chosen and the choice
     *  actions contained by the transition are executed.
     *  @exception IllegalActionException If there is more than one
     *  transition enabled, or can not find the refinements associated with
     *  the current state or enabled transition, or can not read inputs or
     *  outputs from refinements.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug(getName(), " fire.");
        }

        _ctrl._readInputs();

        ///////////////////////////////////////////////////////////////////
        // Handle preemptive transitions
        // Only EXECUTE enabled transitions at the generating-event phase
        // and iterating-purely-discrete-actors phase during a
        // discrete-phase execution. In fact, if this director is used
        // inside CT models only, we can further constraint the enabled
        // transitions to be executed only in generating-event phase.
        // However, to support the backwards compatibility such that
        // HSFSMDirector can be also used inside DE models, we also allow the
        // enabled transitions to be executed at the
        // iterating-purely-discrete-actors phase.
        // Check enabled transitions at the end of a continuous phase
        // execution where the accuracy of the current step size is checked.
        if (isDiscretePhase()) {
            Transition transition = _ctrl._chooseTransition(_currentState
                    .preemptiveTransitionList());

            // record the enabled preemptive transition
            // for the postfire() method.
            _enabledTransition = transition;

            // Set the variable _transitionHasEvent to false indicating
            // the current event has been processed. This variable may be 
            // set to true when the hasCurrentEvent() method is called.
            _transitionHasEvent = false;

            // Disable mutation because we are in the middle of an
            // iteration. The mutation will be enabled again in the
            // postfire() method when the current phase of execution is
            // updating continuous states.
            _mutationEnabled = false;

            if (transition != null) {
                // When an enabled transition is taken, all changes will be
                // permanent. So, it is safe to iterate the refinements of 
                // enabled transitions.
                // NOTE: The refinements of a transition can not and must not
                // advance time. However, this requirement is not checked here.
                Actor[] transitionActors = transition.getRefinement();

                if ((transitionActors != null) && (transitionActors.length > 0)) {
                    for (int i = 0; i < transitionActors.length; ++i) {
                        if (_stopRequested) {
                            break;
                        }

                        if (transitionActors[i].prefire()) {
                            transitionActors[i].fire();
                            transitionActors[i].postfire();
                        }
                    }
                }

                // An enabled preemptive transition preempts the
                // firing of the enabled refienements.
                return;
            }
        }

        // Fire the refinements of the current state.
        Iterator actors = _enabledRefinements.iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            actor.fire();

            if (_debugging && _verbose) {
                _debug(getName(), " fired refinement", ((NamedObj) actor)
                        .getName());
            }
        }

        _ctrl._readOutputsFromRefinement();

        //////////////////////////////////////////////////////////////////
        // Handle nonpreemptive transitions
        // Only EXECUTE enabled transitions at the generating-event phase
        // and iterating-purely-discrete-actors phase during a
        // discrete-phase execution. In fact, if this director is used
        // inside CT models only, we can further constraint the enabled
        // transitions to be executed only in generating-event phase.
        // However, to support the backwards
        // compatibility such that HSFSMDirector can be also used inside DE
        // models, we also allow the enabled transitions to be executed at
        // the iterating-purely-discrete-actors phase.
        // Check enabled transitions at the end of a continuous
        // phase of execution to verify the accuracy of current step size.
        if (isDiscretePhase()) {
            // Note that the output actions associated with the transition
            // are executed.
            Transition transition = _ctrl._chooseTransition(_currentState
                    .nonpreemptiveTransitionList());

            _transitionHasEvent = false;

            // record the enabled nonpreemptive transition for
            // the postfire() method
            _enabledTransition = transition;

            // Disable mutation because we are in the middle of an
            // iteration. The mutation will be enabled again in the
            // postfire() method when the current phase of execution is
            // updating continuous states.
            _mutationEnabled = false;

            if (transition != null) {
                Actor[] transitionActors = transition.getRefinement();

                if ((transitionActors != null) && (transitionActors.length > 0)) {
                    for (int i = 0; i < transitionActors.length; ++i) {
                        if (_stopRequested) {
                            break;
                        }

                        if (transitionActors[i].prefire()) {
                            transitionActors[i].fire();
                            transitionActors[i].postfire();
                        }
                    }
                }
            }
        }
    }

    /** Ask for the current step size used by the solver from the
     *  executive CT director.
     *  @return The current step size.
     */
    public double getCurrentStepSize() {
        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();

        if (executiveDirector != null) {
            return executiveDirector.getCurrentStepSize();
        } else {
            // This should never happen because a modal model with
            // an HSFSMDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an HSFSMDirector must be used inside a CT model.");
        }
    }

    // Removed due to confilct.
    //    /** Return error tolerance used for detecting enabled transitions.
    //     *  @return The error tolerance used for detecting enabled transitions.
    //     */
    //    public final double getErrorTolerance() {
    //        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();
    //
    //        if (executiveDirector != null) {
    //            return executiveDirector.getErrorTolerance();
    //        } else {
    //            // This should never happen because a modal model with
    //            // an HSFSMDirector must be used inside a CT model.
    //            throw new InternalErrorException("A modal model with "
    //                    + "an HSFSMDirector must be used inside a CT model.");
    //        }
    //    }

    /** Return the executive CT director of this director, or null if
     *  this director is at the top level or the executive director is
     *  not a CT general director.
     *
     *  @return The executive CT general director of this director, if there
     *  is any.
     */
    public CTGeneralDirector getExecutiveCTGeneralDirector() {
        CompositeActor container = (CompositeActor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();

        if (executiveDirector instanceof CTGeneralDirector) {
            return (CTGeneralDirector) executiveDirector;
        } else {
            return null;
        }
    }

    /** Return the begin time of the current iteration, this method only
     *  makes sense in continuous-time domain.
     *  @return The begin time of the current iteration.
     */
    public Time getIterationBeginTime() {
        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();

        if (executiveDirector != null) {
            return executiveDirector.getIterationBeginTime();
        } else {
            // This should never happen because a modal model with
            // an HSFSMDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an HSFSMDirector must be used inside a CT model.");
        }
    }

    /** Return the next iteration time obtained from the executive director.
     *  @return The next iteration time.
     */
    public Time getModelNextIterationTime() {
        CompositeActor cont = (CompositeActor) getContainer();
        Director execDir = (Director) cont.getExecutiveDirector();
        return execDir.getModelNextIterationTime();
    }

    /** Return the current time obtained from the executive director, if
     *  there is one, and otherwise return the local view of current time.
     *  @return The current time.
     */
    public Time getModelTime() {
        CompositeActor cont = (CompositeActor) getContainer();
        Director execDir = (Director) cont.getExecutiveDirector();

        if (execDir != null) {
            return execDir.getModelTime();
        } else {
            return super.getModelTime();
        }
    }

    /** Return the next iteration time obtained from the executive director.
     *  @return The next iteration time.
     *  @deprecated As of Ptolemy II 5.1, replaced by
     *  {@link #getModelNextIterationTime}
     */
    public double getNextIterationTime() {
        return getModelNextIterationTime().getDoubleValue();
    }

    /** Restore the states of all the enabled refinements to the
     *  previously marked states.
     */
    public void goToMarkedState() throws IllegalActionException {
        Iterator actors = _enabledRefinements.iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (actor instanceof CTCompositeActor) {
                ((CTCompositeActor) actor).goToMarkedState();
            }
        }
    }

    /** Return true if the enabled refinements may produce events.
     *  @return True if the enabled refinements may produce events.
     */
    public boolean hasCurrentEvent() {
        // how to report and handle an enabled transition in the discrete
        // phase of execution???
        if (_transitionHasEvent) {
            return true;
        }

        // NOTE: We only need to find an event.
        boolean hasCurrentEvent = false;

        // check enabled transitions.
        try {
            List enabledTransitions = _ctrl._checkTransition(_currentState
                    .preemptiveTransitionList());

            if (enabledTransitions.size() != 0) {
                hasCurrentEvent = true;
            } else {
                enabledTransitions = _ctrl._checkTransition(_currentState
                        .nonpreemptiveTransitionList());

                if (enabledTransitions.size() != 0) {
                    hasCurrentEvent = true;
                }
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        if (hasCurrentEvent) {
            return true;
        }

        Iterator actors = _enabledRefinements.iterator();

        while (!hasCurrentEvent && actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (actor instanceof CTCompositeActor) {
                hasCurrentEvent |= ((CTCompositeActor) actor).hasCurrentEvent();
            }
        }

        return hasCurrentEvent;
    }

    /** Call the initialize method of the supper class. Get the controller
     *  and the current state. Get a set of the refinements associated
     *  with this state.
     *  @exception IllegalActionException If the enabled refinements or
     *  the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _ctrl = getController();
        _currentState = _ctrl.currentState();
        _enabledRefinements = new LinkedList();

        Actor[] actors = _currentState.getRefinement();

        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                actors[i].initialize();
                _enabledRefinements.add(actors[i]);
            }
        }
    }

    /** Return true if this is the discrete phase execution.
     *  @return True if this is the discrete phase execution.
     */
    public boolean isDiscretePhase() {
        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();

        if (executiveDirector != null) {
            return executiveDirector.isDiscretePhase();
        } else {
            // This should never happen because a modal model with
            // an HSFSMDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an HSFSMDirector must be used inside a CT model.");
        }
    }

    /** Retun true if all the output-step-size-control actors of the enabled
     *  refinements are satisfied with the current step size and there is
     *  no enabled transition detected.
     *  @return True if all the refinements are satisfied with the
     *  current step size and there is no enabled transition detected.
     */
    public boolean isOutputAccurate() {
        boolean result = true;

        // NOTE: we need to check all possible cases where an event may arise.
        // Iterate all the enabled refinements to see whether they are
        // satisfied with the current step size.
        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();

            while (refinements.hasNext()) {
                Actor refinement = (Actor) refinements.next();

                if (refinement instanceof CTStepSizeControlActor) {
                    result = result
                            && ((CTStepSizeControlActor) refinement)
                                    .isOutputAccurate();
                }
            }
        }

        // Even if the result is false, this method does not return immediately.
        // Instead, we continue to check whether there is any transition
        // enabled with respect to the current inputs.
        // The reason is that when refining step size, we want to find the
        // largest step size that satisfies all the step size constraints to
        // reduce the computation cost.
        // All non-preemptive and preemptive transitions are checked below,
        // because even if a preemptive transition is enabled, the 
        // non-preemptive transitions never even get a chance to be evaluated.
        try {
            // We need to read the inputs because FSMActor uses shadow
            // variables... We need to transport the newest output, (in fact,
            // the correct output at the current time), inside the FSMActor.
            // Unfortunately, we need to perform two steps to achieve this goal.
            // The first step is to transport the input from the receivers of
            // the modal model ports into the local receivers of the refinement
            // ports of FSMActor and Refinements. The second step is to build
            // input map for the FSMActor. 
            // FIXME: This design is really cumbersome. Find a new design.
            // Well, if I have extra time after tuning up the performance of
            // CT and DE... (Haiyang Zheng, 1:06am, 10/23/2005)
            // First step:
            Iterator modalPorts = ((CompositeActor) getContainer())
                    .inputPortList().iterator();

            while (modalPorts.hasNext()) {
                transferInputs((IOPort) modalPorts.next());
            }

            // Second step:
            _ctrl._readInputs();

            // FIXME: make some private methods to do the following repeated 
            // operations.
            // It seems very time consuming, try to optimize it.
            // Check if there is any preemptive transition enabled.
            List preemptiveEnabledTransitions = _ctrl
                    ._checkTransition(_currentState.preemptiveTransitionList());

            if (preemptiveEnabledTransitions.size() != 0) {
                if (_debugging && _verbose) {
                    _debug("Found enabled preemptive transitions.");
                }
            }

            // Check if there is any non-preemptive transition enabled.
            List nonpreemptiveEnabledTransitions = _ctrl
                    ._checkTransition(_currentState
                            .nonpreemptiveTransitionList());

            if (nonpreemptiveEnabledTransitions.size() != 0) {
                if (_debugging && _verbose) {
                    _debug("Found enabled non-preemptive transitions.");
                }
            }

            // NOTE: CheckEvent is used for guard expression like a == 1. 
            // Check if there is any event detected for preemptive transitions.
            List preemptiveTrWithEvents = _checkEvent(_currentState
                    .preemptiveTransitionList());

            if (preemptiveTrWithEvents.size() != 0) {
                if (_debugging) {
                    _debug("Detected transitions with events.");
                }
            }

            // Check if there is any events detected for non-preemptive 
            // transitions.
            List nonPreemptiveTrWithEvents = _checkEvent(_currentState
                    .nonpreemptiveTransitionList());

            if (nonPreemptiveTrWithEvents.size() != 0) {
                if (_debugging) {
                    _debug("Detected transitions with events.");
                }
            }

            if ((preemptiveEnabledTransitions.size() == 0)
                    && (nonpreemptiveEnabledTransitions.size() == 0)
                    && (preemptiveTrWithEvents.size() == 0)
                    && (nonPreemptiveTrWithEvents.size() == 0)) {
                // If there is no transition enabled, the last step size is
                // accurate for transitions. The states will be committed at
                // the postfire method.
                _outputAccurate = true;
                _transitionHasEvent = false;
            } else {
                Transition enabledTransition = null;

                // We check the maximum difference of the relations that change
                // their status for step size refinement.
                _distanceToBoundary = Double.MIN_VALUE;

                enabledTransition = _getTransitionWithMaximumDistance(preemptiveEnabledTransitions);
                enabledTransition = _getTransitionWithMaximumDistance(nonpreemptiveEnabledTransitions);
                enabledTransition = _getTransitionWithMaximumDistance(preemptiveTrWithEvents);
                enabledTransition = _getTransitionWithMaximumDistance(nonPreemptiveTrWithEvents);

                if (_debugging && _verbose) {
                    _debug("The guard "
                            + enabledTransition.getGuardExpression()
                            + " has the biggest difference to boundary as "
                            + _distanceToBoundary);
                }

                _outputAccurate = _distanceToBoundary < getErrorTolerance();

                if (_outputAccurate) {
                    _transitionHasEvent = true;
                } else {
                    // NOTE: we do not set _transitionHasEvent to true here 
                    // because it is not the exact time the event happens. 
                    // We need to refine the step size.
                    _transitionHasEvent = false;
                }
            }

            // NOTE: _outputAccurate here is only used for the transitions but
            // not the refinements. This makes the refinedStepSize() method
            // more efficient in that if the _outputAccurate is true, no step
            // size refinement for transitions are necessary.
            return _outputAccurate && result;
        } catch (Throwable throwable) {
            // Can not evaluate guard expression, 
            throw new InternalErrorException(
                    this,
                    throwable,
                    "All continuous-time variables must have values from "
                            + "the beginning of simulation. Set the initial values.");
        }
    }

    /** Retun true if all the refinements can resolve their states with the
     *  current step size.
     *  @return True if all the refinements can resolve their states with the
     *  current step size.
     */
    public boolean isStateAccurate() {
        boolean result = true;

        // NOTE: we have to check all refinements for the similar reason 
        // presented in the isOutputAccurate() method.
        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();

            while (refinements.hasNext()) {
                Actor refinement = (Actor) refinements.next();

                if (refinement instanceof CTStepSizeControlActor) {
                    result = result
                            && ((CTStepSizeControlActor) refinement)
                                    .isStateAccurate();
                }
            }
        }

        return result;
    }

    /** Make the current states of all the enabled refinements.
     */
    public void markState() {
        Iterator actors = _enabledRefinements.iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (actor instanceof CTCompositeActor) {
                ((CTCompositeActor) actor).markState();
            }
        }
    }

    /** Return a CTReceiver. By default, the signal type is discrete.
     *  @return a new CTReceiver with signal type as discrete.
     */
    public Receiver newReceiver() {
        CTReceiver receiver = new CTReceiver();

        //FIXME: this is not right. Instead of blindly assigning a "discrete"
        //signal type, we need to derive the actual signal type from the
        //connections between ports.
        receiver.setSignalType(CTReceiver.DISCRETE);

        //        receiver.setSignalType(CTReceiver.CONTINUOUS);
        return receiver;
    }

    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire the enabled refinements of the
     *  current state
     *  of the mode controller and take out event outputs that the
     *  refinements generate. Execute the commit actions contained
     *  by the last chosen transition of the mode controller and set
     *  its current state to the destination state of the transition.
     *  Clear the relation list associated with the enabled transition
     *  and request to be fired again at the current time.
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If thrown by any action, or
     *   there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        boolean postfireReturns = true;

        Iterator refinements = _enabledRefinements.iterator();

        while (refinements.hasNext()) {
            Actor refinement = (Actor) refinements.next();
            postfireReturns = postfireReturns && refinement.postfire();
        }

        if (_enabledTransition != null) {
            if (_debugging) {
                _debug("Postfire deals with enabled transition "
                        + _enabledTransition.getGuardExpression());
            }

            // It is important to clear the history information of the
            // relation list since after this breakpoint, no history
            // information is valid.
            Iterator iterator = _currentState.nonpreemptiveTransitionList()
                    .listIterator();

            while (iterator.hasNext()) {
                Transition transition = (Transition) iterator.next();
                transition.getRelationList().clearRelationList();
            }

            iterator = _currentState.preemptiveTransitionList().listIterator();

            while (iterator.hasNext()) {
                Transition transition = (Transition) iterator.next();
                transition.getRelationList().clearRelationList();
            }

            // Only clear the cached enabled transition when no more events
            // will be generated at the current discrete phase of execution.
            _enabledTransition = null;

            // Enable mutation when the current phase of execution is
            // updating continuous states.
            // This is to avoid unnecessary change requests made by
            // the super class FSMDirector.
            _mutationEnabled = true;

            // If there is one transition enabled, the HSFSMDirector requests
            // to be fired again at the same time to see whether the next state
            // has some outgoing transition enabled.
            // FIXME: we don't need the following. let the hasCurrentEvent() 
            // method handles transient states.
            // If the top level of the model is modal model, the director
            // is null. We do not request to be fired again since no one in
            // the upper level of hierarchy will do that.
            CompositeActor container = (CompositeActor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();

            if (getExecutiveCTGeneralDirector() != null) {
                if (_debugging) {
                    _debug(executiveDirector.getFullName()
                            + " requests refiring at " + getModelTime());
                }

                // If there is one transition enabled, the HSFSMDirector requests
                // to be fired again at the same time to see whether the next
                // state has some outgoing transition enabled.
                executiveDirector.fireAt(container, getModelTime());
            }

            // execute the commit actions and change the current state
            // to the destination state.
            postfireReturns = postfireReturns && super.postfire();

            // Update the states
            _currentState = _ctrl.currentState();

            // FIXME: if we remove _enabledRefinemnets, the following is
            // unnecessary.
            Actor[] actors = _currentState.getRefinement();
            _enabledRefinements = new LinkedList();

            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    _enabledRefinements.add(actors[i]);
                }
            }
        } else {
            // Commit the current states of the relationlists
            // of all the transitions during these execution phases.
            Iterator iterator = _currentState.nonpreemptiveTransitionList()
                    .listIterator();

            while (iterator.hasNext()) {
                Transition transition = (Transition) iterator.next();
                transition.getRelationList().commitRelationValues();
            }

            iterator = _currentState.preemptiveTransitionList().listIterator();

            while (iterator.hasNext()) {
                Transition transition = (Transition) iterator.next();
                transition.getRelationList().commitRelationValues();
            }
        }

        return postfireReturns;
    }

    /** Return the smallest next step size predicted by the all the
     *  enabled refinements, which are refinements that returned true
     *  in their prefire() methods in this iteration.
     *  If there are no refinements, then return Double.MAX_VALUE.
     *  If a refinement is not a CTStepSizeControlActor, then
     *  its prediction is Double.MAX_VALUE.
     *  @return The predicted next step size.
     */
    public double predictedStepSize() {
        double result = Double.MAX_VALUE;

        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();

            while (refinements.hasNext()) {
                Actor refinement = (Actor) refinements.next();

                if (refinement instanceof CTStepSizeControlActor) {
                    result = Math.min(result,
                            ((CTStepSizeControlActor) refinement)
                                    .predictedStepSize());
                }
            }
        }

        return result;
    }

    /** Set the controller and current state. Call super.prefire().
     *  @return True if the prefire() method of the super class returns true.
     *  @exception IllegalActionException If the controller does not exit,
     *  or can not find the specified refinements associated with the current
     *  state, or the prefire() method of refinements throw it, or the super
     *  class throws it.
     */
    public boolean prefire() throws IllegalActionException {
        // FIXME: if we remove _enabledRefinemnets, the following is
        // unnecessary.
        Iterator enabledRefinements = _enabledRefinements.iterator();

        while (enabledRefinements.hasNext()) {
            Actor refinement = (Actor) enabledRefinements.next();
            refinement.prefire();
        }

        return super.prefire();
    }

    /** Return true if all the dynamic actors contained by the enabled
     *  refinements return true from their prefire() method.
     *  @return True if all dynamic actors of enabled refinements can be
     *  prefired.
     *  @exception IllegalActionException If the local directors of refinements
     *  throw it.
     */
    public boolean prefireDynamicActors() throws IllegalActionException {
        boolean result = true;

        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();

            while (refinements.hasNext()) {
                Actor refinement = (Actor) refinements.next();

                if (refinement instanceof CTCompositeActor) {
                    result = result
                            && ((CTCompositeActor) refinement)
                                    .prefireDynamicActors();
                }
            }
        }

        return result;
    }

    /** Return the step size refined by all the enabled refinements,
     *  which are refinements that returned true
     *  in their prefire() methods in this iteration, or the enabled
     *  transition which requires the current time be the same with
     *  the time it is enabled.
     *  If there are no refinements, or no refinement is a
     *  CTStepSizeControlActor, then the refined step size is the smaller
     *  value between current step size of the executive director and
     *  refined step size from enabled transition.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        CTGeneralDirector director = getExecutiveCTGeneralDirector();
        double result = director.getCurrentStepSize();

        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();

            while (refinements.hasNext()) {
                Actor refinement = (Actor) refinements.next();

                if (refinement instanceof CTStepSizeControlActor) {
                    result = Math.min(result,
                            ((CTStepSizeControlActor) refinement)
                                    .refinedStepSize());
                }
            }
        }

        if (!_outputAccurate) {
            double refinedStepSize = result;
            double errorTolerance = director.getErrorTolerance();
            double currentStepSize = director.getCurrentStepSize();

            // Linear interpolation to refine the step size.
            // Note the step size is refined such that the distanceToBoundary
            // is half of errorTolerance.
            refinedStepSize = (currentStepSize * (_lastDistanceToBoundary + (errorTolerance / 2)))
                    / (_lastDistanceToBoundary + _distanceToBoundary);

            result = Math.min(result, refinedStepSize);
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // This method detects any events happened during one step size.
    // NOTE: CheckEvent is used for guard expression like a == 1. 
    private List _checkEvent(List transitionList) {
        LinkedList TransitionsWithEvents = new LinkedList();
        Iterator transitionRelations = transitionList.iterator();

        while (transitionRelations.hasNext() && !_stopRequested) {
            Transition transition = (Transition) transitionRelations.next();

            if (transition.getRelationList().hasEvent()) {
                TransitionsWithEvents.add(transition);
            }
        }

        return TransitionsWithEvents;
    }

    /** Return the transition with the maximum distance to the boundary from
     *  the given list of enabled transitions.
     *  @param preemptiveEnabledTransitions
     */
    private Transition _getTransitionWithMaximumDistance(List transitionList) {
        Transition enabledTransition = null;
        Iterator iterator = transitionList.iterator();

        while (iterator.hasNext()) {
            Transition transition = (Transition) iterator.next();
            RelationList relationList = transition.getRelationList();
            double distanceToBoundary = relationList.maximumDifference();

            if (distanceToBoundary > _distanceToBoundary) {
                _distanceToBoundary = distanceToBoundary;
                _lastDistanceToBoundary = relationList
                        .getPreviousMaximumDistance();
                enabledTransition = transition;
            }
        }

        return enabledTransition;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Cached reference to mode controller. */
    private FSMActor _ctrl = null;

    /** Cached reference to current state. */
    private State _currentState = null;

    /** Local variable to indicate the distance to boundary. */
    private double _distanceToBoundary = 0.0;

    /** Local variable to indicate the last distance to boundary. */
    private double _lastDistanceToBoundary = 0.0;

    /** Local variable to indicate whether the output is accurate. */
    private boolean _outputAccurate = true;

    // Boolean variable to indicate whether there is an enabled transtion to
    // produce an event at the current firing.
    private boolean _transitionHasEvent = false;

    public CTExecutionPhase getExecutionPhase() {
        // TODO Auto-generated method stub
        return null;
    }
}
