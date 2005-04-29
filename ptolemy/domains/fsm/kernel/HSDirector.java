/* An HSDirector governs the execution of the discrete dynamics of a
   hybrid system model.

   Copyright (c) 1999-2005 The Regents of the University of California.
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
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEmbeddedDirector;
import ptolemy.domains.ct.kernel.CTExecutionPhase;
import ptolemy.domains.ct.kernel.CTGeneralDirector;
import ptolemy.domains.ct.kernel.CTReceiver;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.domains.ct.kernel.CTTransparentDirector;
import ptolemy.domains.ct.kernel.ODESolver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// HSDirector

/**
   An HSDirector governs the execution of the discrete dynamics of a hybrid
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
   @since Ptolemy II 1.0
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (liuxj)
*/
public class HSDirector extends FSMDirector implements CTTransparentDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HSDirector() {
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
    public HSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public HSDirector(Workspace workspace) {
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

    /** Set the values of input variables in the mode controller. Examine
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

        Transition transition;

        // NOTE: If an enabled transition is already found,
        // do not try to find another enabled transition.
        // This guarantees that each firing of a modal model produces at most
        // one discrete event.
        // Also, the refinements are not fired to prevent the output from the
        // enabled transitions being overwritten by the outputs from the
        // refinements.
        // This guarantees that only one event is produced at one discrete
        // phase of execution.
        if (_enabledTransition == null) {
            ///////////////////////////////////////////////////////////////////
            // Handle preemptive transitions
            // Only EXECUTE enabled transitions at the generating-event phase
            // and iterating-purely-discrete-actors phase during a
            // discrete-phase execution. In fact, if this director is used
            // inside CT models only, we can further constraint the enabled
            // transitions to be executed only in generating-event phase.
            // However, to support the backwards compatibility such that
            // HSDirector can be also used inside DE models, we also allow the
            // enabled transitions to be executed at the
            // iterating-purely-discrete-actors phase.
            // Check enabled transitions at the end of a continuous phase
            // execution where the accuracy of the current step size is checked.
            if ((getExecutionPhase() == CTExecutionPhase.GENERATING_EVENTS_PHASE)
                    || (getExecutionPhase() == CTExecutionPhase.ITERATING_PURELY_DISCRETE_ACTORS_PHASE)) {
                transition = _ctrl._chooseTransition(_currentState
                        .preemptiveTransitionList());
                _transitionHasEvent = false;
            } else {
                transition = null;
            }

            // NOTE: The refinements of a transition can not and must not
            // advance time. However, this requirement is not checked here.
            if (transition != null) {
                // record the enabled preemptive transition
                // for the postfire() method.
                _enabledTransition = transition;

                // Disable mutation because we are in the middle of an
                // iteration. The mutation will be enabled again in the
                // postfire() method when the current phase of execution is
                // updating continuous states.
                _mutationEnabled = false;

                Actor[] actors = transition.getRefinement();

                if ((actors != null) && (actors.length > 0)) {
                    for (int i = 0; i < actors.length; ++i) {
                        if (_stopRequested) {
                            break;
                        }

                        if (actors[i].prefire()) {
                            actors[i].fire();
                            actors[i].postfire();
                        }
                    }

                    _ctrl._readOutputsFromRefinement();
                }

                // An enabled preemptive transition preempts the
                // firing of the enabled refienements.
                return;
            }

            // Check whether there exits some enabled preemptive transition.
            // If so, we need to skip the firing of refinements and return
            // immediately.
            List enabledPreemptiveTransitions = _ctrl._checkTransition(_currentState
                    .preemptiveTransitionList());

            if (enabledPreemptiveTransitions.size() > 0) {
                return;
            }

            boolean visited = _currentState.isVisited();

            // Fire the refinements of the current state.
            Iterator actors = _enabledRefinements.iterator();

            while (actors.hasNext()) {
                Actor actor = (Actor) actors.next();

                if (_debugging && _verbose) {
                    _debug(getName(), " fire refinement",
                            ((NamedObj) actor).getName());
                }

                // If this is the first time this state is visited, check
                // whether the director for the refinement is a CT (Embedded)
                // director. If so, establish the initial states for this
                // refinement.
                if (!visited) {
                    Director director = actor.getDirector();

                    if (director instanceof CTEmbeddedDirector) {
                        ((CTEmbeddedDirector) director)
                            .setInitialStatesNotReady();
                    }
                }

                actor.fire();
            }

            _currentState.setVisited(true);

            _ctrl._readOutputsFromRefinement();

            //////////////////////////////////////////////////////////////////
            // Handle nonpreemptive transitions
            // Only EXECUTE enabled transitions at the generating-event phase
            // and iterating-purely-discrete-actors phase during a
            // discrete-phase execution. In fact, if this director is used
            // inside CT models only, we can further constraint the enabled
            // transitions to be executed only in generating-event phase.
            // However, to support the backwards
            // compatibility such that HSDirector can be also used inside DE
            // models, we also allow the enabled transitions to be executed at
            // the iterating-purely-discrete-actors phase.
            // Check enabled transitions at the end of a continuous
            // phase of execution to verify the accuracy of current step size.
            if ((getExecutionPhase() == CTExecutionPhase.GENERATING_EVENTS_PHASE)
                    || (getExecutionPhase() == CTExecutionPhase.ITERATING_PURELY_DISCRETE_ACTORS_PHASE)) {
                // Note that the output actions associated with the transition
                // are executed.
                transition = _ctrl._chooseTransition(_currentState
                        .nonpreemptiveTransitionList());
                _transitionHasEvent = false;
            } else {
                transition = null;
            }

            // execute the refinements of the enabled transition.
            if (transition != null) {
                // record the enabled nonpreemptive transition for
                // the postfire() method
                _enabledTransition = transition;

                // Disable mutation because we are in the middle of an
                // iteration. The mutation will be enabled again in the
                // postfire() method when the current phase of execution is
                // updating continuous states.
                _mutationEnabled = false;

                Actor[] transitionActors = transition.getRefinement();

                if ((transitionActors != null) && (transitionActors.length > 0)) {
                    for (int i = 0; i < transitionActors.length; ++i) {
                        if (_stopRequested) {
                            break;
                        }

                        if (transitionActors[i].prefire()) {
                            if (_debugging) {
                                _debug(getFullName(),
                                        " fire transition refinement",
                                        ((ptolemy.kernel.util.NamedObj) transitionActors[i])
                                        .getName());
                            }

                            transitionActors[i].fire();
                            transitionActors[i].postfire();
                        }

                        _ctrl._readOutputsFromRefinement();
                    }
                }
            }
        }

        return;
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
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an HSDirector must be used inside a CT model.");
        }
    }

    /** Return error tolerance used for detecting enabled transitions.
     *  @return The error tolerance used for detecting enabled transitions.
     */
    public final double getErrorTolerance() {
        CompositeActor container = (CompositeActor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();

        if (executiveDirector instanceof CTGeneralDirector) {
            return ((CTGeneralDirector) executiveDirector).getErrorTolerance();
        } else {
            // This should never happen because a modal model with
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an HSDirector must be used inside a CT model.");
        }
    }

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

    /** Get the current execution phase of this director.
     *  @return The current execution phase of this director.
     */
    public CTExecutionPhase getExecutionPhase() {
        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();

        if (executiveDirector != null) {
            return executiveDirector.getExecutionPhase();
        } else {
            // For any executive director that is not a CTGeneralDirector,
            // the current execution phase is always
            // ITERATING_PURELY_DISCRETE_PHASE.
            // Although the returned result is not used anywhere.
            return CTExecutionPhase.ITERATING_PURELY_DISCRETE_ACTORS_PHASE;
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
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an HSDirector must be used inside a CT model.");
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
     */
    public double getNextIterationTime() {
        return getModelNextIterationTime().getDoubleValue();
    }

    /** Return the ODE solver of the executive CT general director
     *  for normal integration.
     *  @return The ODE solver of the executive CT general director
     *  for normal integration.
     */
    public ODESolver getNormalODESolver() {
        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();

        if (executiveDirector != null) {
            return executiveDirector.getNormalODESolver();
        } else {
            // This should never happen because a modal model with
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an HSDirector must be used inside a CT model.");
        }
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
        boolean eventPresent = false;
        Iterator actors = _enabledRefinements.iterator();

        while (!eventPresent && actors.hasNext()) {
            Actor actor = (Actor) actors.next();

            if (actor instanceof CTCompositeActor) {
                eventPresent |= ((CTCompositeActor) actor).hasCurrentEvent();
            }
        }

        return _transitionHasEvent || eventPresent;
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
            // an HSDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an HSDirector must be used inside a CT model.");
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

        // Iterate all the enabled refinements to see whether they are
        // satisfied with the current step size.
        Actor container = (Actor) getContainer();
        CTDirector dir = (CTDirector) (container.getExecutiveDirector());

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
        // because even if a preemptive transition is enabled, the non-preemptive
        // transitions never even get a chance to be evaluated.
        try {
            // Check if there is any preemptive transition enabled.
            List preemptiveEnabledTransitions = _ctrl._checkTransition(_currentState
                    .preemptiveTransitionList());

            if (preemptiveEnabledTransitions.size() != 0) {
                if (_debugging && _verbose) {
                    _debug("Find enabled preemptive transitions.");
                }
            }

            // Check if there is any non-preemptive transition enabled.
            List nonpreemptiveEnabledTransitions = _ctrl._checkTransition(_currentState
                    .nonpreemptiveTransitionList());

            if (nonpreemptiveEnabledTransitions.size() != 0) {
                if (_debugging && _verbose) {
                    _debug("Find enabled non-preemptive transitions.");
                }
            }

            // Check if there is any event detected for preemptive transitions.
            Transition preemptiveTrWithEvent = _checkEvent(_currentState
                    .preemptiveTransitionList());

            if (preemptiveTrWithEvent != null) {
                if (_debugging) {
                    _debug("Detected event for transition:  "
                            + preemptiveTrWithEvent.getGuardExpression());
                }
            }

            // Check if there is any events detected for
            // nonpreemptive transitions.
            Transition nonPreemptiveTrWithEvent = _checkEvent(_currentState
                    .nonpreemptiveTransitionList());

            if (nonPreemptiveTrWithEvent != null) {
                if (_debugging) {
                    _debug("Detected event for transition:  "
                            + nonPreemptiveTrWithEvent.getGuardExpression());
                }
            }

            double errorTolerance = dir.getErrorTolerance();

            // If there is no transition enabled, the last step size is
            // accurate for transitions. The states will be committed at
            // the postfire method.
            if ((preemptiveEnabledTransitions.size() == 0)
                    && (nonpreemptiveEnabledTransitions.size() == 0)
                    && (preemptiveTrWithEvent == null)
                    && (nonPreemptiveTrWithEvent == null)) {
                _transitionHasEvent = false;
                _lastDistanceToBoundary = 0.0;
                _distanceToBoundary = 0.0;
                _outputAccurate = true;
            } else {
                Transition enabledTransition = null;

                // We check the maximum difference of the relations that change
                // their status for step size refinement.
                _distanceToBoundary = Double.MIN_VALUE;

                Iterator iterator = preemptiveEnabledTransitions.iterator();

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

                iterator = nonpreemptiveEnabledTransitions.iterator();

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

                if (preemptiveTrWithEvent != null) {
                    RelationList relationList = preemptiveTrWithEvent
                        .getRelationList();
                    double distanceToBoundary = relationList.maximumDifference();

                    if (distanceToBoundary > _distanceToBoundary) {
                        _distanceToBoundary = distanceToBoundary;
                        _lastDistanceToBoundary = relationList
                            .getPreviousMaximumDistance();
                        enabledTransition = preemptiveTrWithEvent;
                    }
                }

                if (nonPreemptiveTrWithEvent != null) {
                    RelationList relationList = nonPreemptiveTrWithEvent
                        .getRelationList();
                    double distanceToBoundary = relationList.maximumDifference();

                    if (distanceToBoundary > _distanceToBoundary) {
                        _distanceToBoundary = distanceToBoundary;
                        _lastDistanceToBoundary = relationList
                            .getPreviousMaximumDistance();
                        enabledTransition = nonPreemptiveTrWithEvent;
                    }
                }

                if (_debugging && _verbose) {
                    _debug("The guard "
                            + enabledTransition.getGuardExpression()
                            + " has the biggest difference to boundary as "
                            + _distanceToBoundary);
                }

                _outputAccurate = _distanceToBoundary < errorTolerance;

                if (!_outputAccurate) {
                    // NOTE: we do not set _hasEvent to true here because it is
                    // not the exact time the event happens. We need to refine
                    // the step size.
                    _transitionHasEvent = false;
                } else {
                    _transitionHasEvent = true;
                }
            }

            return result && _outputAccurate;
        } catch (Throwable throwable) {
            // Can not evaluate guard expression.
            throw new InternalErrorException(throwable);
        }
    }

    /** Retun true if all the refinements can resolve their states with the
     *  current step size.
     *  @return True if all the refinements can resolve their states with the
     *  current step size.
     */
    public boolean isStateAccurate() {
        boolean result = true;

        //CTDirector dir = (CTDirector) (((Actor) getContainer())
        //        .getExecutiveDirector());
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

        CompositeActor container = (CompositeActor) getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        Iterator refinements = _enabledRefinements.iterator();

        while (refinements.hasNext()) {
            Actor refinement = (Actor) refinements.next();
            postfireReturns = postfireReturns && refinement.postfire();

            // take out event outputs generated in ref.postfire()
            Iterator outports = refinement.outputPortList().iterator();

            while (outports.hasNext()) {
                IOPort p = (IOPort) outports.next();
                transferOutputs(p);
            }
        }

        // If there is one transition enabled, the HSDirector requests
        // to be fired again at the same time to see whether the next state
        // has some outgoing transition enabled.
        Transition tr = _enabledTransition;

        if (tr != null) {
            if (_debugging) {
                _debug("Postfire deals with enabled transition "
                        + tr.getGuardExpression());
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

            // If the top level of the model is modal model, the director
            // is null. We do not request to be fired again since no one in
            // the upper level of hierarchy will do that.
            if (executiveDirector != null) {
                if (_debugging) {
                    _debug(executiveDirector.getFullName()
                            + " requests refiring at " + getModelTime());
                }

                // If there is one transition enabled, the HSDirector requests
                // to be fired again at the same time to see whether the next
                // state has some outgoing transition enabled.
                executiveDirector.fireAt(container, getModelTime());
            }

            // If this iteration will not generate more events, (the
            // current phase of execution is neithter generating-event nor
            // iterating-purely-discrete-actors), or the executive director
            // is not a CT director, reset the _enabledTransition to null.
            // Here we reset the cached enabled transition at the
            // updating-continuous-states phase, indicating the end of a
            // complete iteration of the executive CT director.
            // This guarantees that at most one transition is taken in an
            // iteration of discrete phase of execution.
            // To be more specific, for each (t, n), there is at most
            // one event.
            if ((getExecutionPhase() == CTExecutionPhase.UPDATING_CONTINUOUS_STATES_PHASE)
                    || (executiveDirector == null)) {
                // Only clear the cached enabled transition when no more events
                // will be generated at the current discrete phase of execution.
                _enabledTransition = null;

                // Enable mutation when the current phase of execution is
                // updating continuous states.
                // This is to avoid unnecessary change requests made by
                // the super class FSMDirector.
                _mutationEnabled = true;
            }
        } else {
            if ((getExecutionPhase() == CTExecutionPhase.GENERATING_EVENTS_PHASE)
                    || (getExecutionPhase() == CTExecutionPhase.POSTFIRING_EVENT_GENERATORS_PHASE)) {
                // Only commit the current states of the relationlists
                // of all the transitions during these execution phases.
                Iterator iterator = _currentState.nonpreemptiveTransitionList()
                    .listIterator();

                while (iterator.hasNext()) {
                    Transition transition = (Transition) iterator.next();
                    transition.getRelationList().commitRelationValues();
                }

                iterator = _currentState.preemptiveTransitionList()
                    .listIterator();

                while (iterator.hasNext()) {
                    Transition transition = (Transition) iterator.next();
                    transition.getRelationList().commitRelationValues();
                }
            }
        }

        // execute the commit actions and change the current state
        // to the destination state.
        postfireReturns = postfireReturns && super.postfire();

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
        _ctrl = getController();
        _currentState = _ctrl.currentState();

        if (_debugging) {
            _debug(getName(),
                    " find FSMActor " + _ctrl.getName()
                    + " and the current state is " + _currentState.getName());
        }

        Actor[] actors = _currentState.getRefinement();
        _enabledRefinements = new LinkedList();

        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                _enabledRefinements.add(actors[i]);
                actors[i].prefire();
            }
        }

        _outputAccurate = true;
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
        CTDirector director = (CTDirector) (((Actor) getContainer())
                .getExecutiveDirector());
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
            refinedStepSize = (currentStepSize * (_lastDistanceToBoundary
                                       + (errorTolerance / 2))) / (_lastDistanceToBoundary
                                               + _distanceToBoundary);

            result = Math.min(result, refinedStepSize);
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // This method detects any events happened during one step size.
    private Transition _checkEvent(List transitionList) {
        Transition result = null;
        Iterator transitionRelations = transitionList.iterator();

        while (transitionRelations.hasNext() && !_stopRequested
                && (result == null)) {
            Transition transition = (Transition) transitionRelations.next();

            if (transition.getRelationList().hasEvent()) {
                result = transition;
            }
        }

        return result;
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
}
