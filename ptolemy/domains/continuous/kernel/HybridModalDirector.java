/* An HybridModalDirector governs the execution of the discrete dynamics of a
 hybrid system model.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.domains.continuous.kernel;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.FSMDirector;
import ptolemy.domains.modal.kernel.ParseTreeEvaluatorForGuardExpression;
import ptolemy.domains.modal.kernel.RelationList;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// HybridModalDirector

/**
 An HybridModalDirector governs the execution of the discrete dynamics of a hybrid
 system model. It extends ModalDirector by implementing the ContinuousStatefulComponent
 and ContinuousStepSizeController interfaces by delegating the function of those
 interfaces to the currently active state refinement.
 <p>
 This director is based on HSFSMDirector by Xiaojun Liu and Haiyang Zheng.

 @author Edward A. Lee, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (liuxj)
 */
public class HybridModalDirector extends FSMDirector implements
        ContinuousStatefulComponent, ContinuousStepSizeController {

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
    public HybridModalDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new actor.
     *  @param workspace The workspace for the new actor.
     *  @return A new FSMActor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HybridModalDirector newObject = (HybridModalDirector) super.clone(workspace);
        newObject._enclosingContinuousDirector = null;
        newObject._enclosingContinuousDirectorVersion = -1;
        /* FIXME
        newObject._receivers = new LinkedList();
        */
        return newObject;
    }

    /** Return error tolerance used for detecting enabled transitions.
     *  If there is an enclosing continuous director, then get the
     *  error tolerance from that director. Otherwise, return 1e-4.
     *  @return The error tolerance used for detecting enabled transitions.
     */
    public final double getErrorTolerance() {
        ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
        if (enclosingDirector == null) {
            return 1e-4;
        }
        return enclosingDirector.getErrorTolerance();
    }

    /** Return the parse tree evaluator used to evaluate guard expressions.
     *  In this class, an instance
     *  of {@link ParseTreeEvaluatorForGuardExpression} is returned.
     *  The parse tree evaluator is set to construction mode.
     *  @return ParseTreeEvaluator used to evaluate guard expressions.
     */
    public ParseTreeEvaluator getParseTreeEvaluator() {
        RelationList relationList = new RelationList();
        ParseTreeEvaluatorForGuardExpression evaluator = new ParseTreeEvaluatorForGuardExpression(
                relationList, getErrorTolerance());
        evaluator.setConstructionMode();
        return evaluator;
    }

    /** Return true if all actors that were fired in the current iteration
     *  report that the step size is accurate.
     *  @return True if the current step is accurate.
     */
    public boolean isStepSizeAccurate() {
        boolean result = true;
        _lastDistanceToBoundary = 0.0;
        _distanceToBoundary = 0.0;
                
        // Double iterator over two lists.
        Iterator actors = new ActorsFiredIterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof ContinuousStepSizeController) {
                if (!((ContinuousStepSizeController) actor)
                        .isStepSizeAccurate()) {
                    return false;
                }
            } else if (actor instanceof CompositeActor) {
                // Delegate to the director.
                Director director = actor.getDirector();
                if (director instanceof ContinuousStepSizeController) {
                    if (!((ContinuousStepSizeController) director)
                            .isStepSizeAccurate()) {
                        return false;
                    }
                }
            }
        }
        // Next check for enabled transitions.  Do this even if the above
        // result is false, because as a side effect we calculate the
        // data needed to suggest the next step size when refinedStepSize()
        // is called.
        // All non-preemptive and preemptive transitions are checked below,
        // because even if a preemptive transition is enabled, the non-preemptive
        // transitions never even get a chance to be evaluated.
        // However, do this only if there is an enclosing
        // ContinuousDirector.
        ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
        if (enclosingDirector == null) {
            return result;
        }
        try {
            // Check whether there is any preemptive transition enabled.
            FSMActor controller = getController();
            State currentState = controller.currentState();
            List preemptiveEnabledTransitions = controller
                    .enabledTransitions(currentState.preemptiveTransitionList());

            // Check whether there is any non-preemptive transition enabled.
            List nonpreemptiveEnabledTransitions = controller
                    .enabledTransitions(currentState
                            .nonpreemptiveTransitionList());

            // Check whether there is any event detected for preemptive transitions.
            Transition preemptiveTrWithEvent = _checkEvent(currentState
                    .preemptiveTransitionList());

            // Check whether there is any event detected for
            // nonpreemptive transitions.
            Transition nonPreemptiveTrWithEvent = _checkEvent(currentState
                    .nonpreemptiveTransitionList());

            if (_debugging && _verbose) {
                if (preemptiveEnabledTransitions.size() != 0) {
                    _debug("Found enabled preemptive transitions.");
                }
                if (nonpreemptiveEnabledTransitions.size() != 0) {
                    _debug("Found enabled non-preemptive transitions.");
                }
                if (preemptiveTrWithEvent != null) {
                    _debug("Detected event for transition:  "
                            + preemptiveTrWithEvent.getGuardExpression());
                }
                if (nonPreemptiveTrWithEvent != null) {
                    _debug("Detected event for transition:  "
                            + nonPreemptiveTrWithEvent.getGuardExpression());
                }
            }

            // If there is no transition enabled, the last step size is
            // accurate for transitions. The states will be committed at
            // the postfire method.
            // Set the local variables to be used to suggest a step
            // size in the next call to refinedStepSize().
            if ((preemptiveEnabledTransitions.size() == 0)
                    && (nonpreemptiveEnabledTransitions.size() == 0)
                    && (preemptiveTrWithEvent == null)
                    && (nonPreemptiveTrWithEvent == null)) {
                _lastDistanceToBoundary = 0.0;
                _distanceToBoundary = 0.0;
                return result;
            } else {
                Transition enabledTransition = null;

                // We check the maximum difference of the relations that change
                // their status for step size refinement.
                _distanceToBoundary = Double.MIN_VALUE;

                Iterator iterator = preemptiveEnabledTransitions.iterator();

                while (iterator.hasNext()) {
                    Transition transition = (Transition) iterator.next();
                    ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) transition
                            .getParseTreeEvaluator();
                    RelationList relationList = parseTreeEvaluator
                            .getRelationList();

                    double distanceToBoundary = relationList
                            .getMaximumDifference();
                    // The distance to boundary is the difference between
                    // the value of a variable in a relation (comparison
                    // operation) and the threshold value against which it
                    // is being compared. The previous distance is the last
                    // committed distance.
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
                    ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) transition
                            .getParseTreeEvaluator();
                    RelationList relationList = parseTreeEvaluator
                            .getRelationList();

                    double distanceToBoundary = relationList
                            .getMaximumDifference();

                    if (distanceToBoundary > _distanceToBoundary) {
                        _distanceToBoundary = distanceToBoundary;
                        _lastDistanceToBoundary = relationList
                                .getPreviousMaximumDistance();
                        enabledTransition = transition;
                    }
                }

                if (preemptiveTrWithEvent != null) {
                    ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) preemptiveTrWithEvent
                            .getParseTreeEvaluator();
                    RelationList relationList = parseTreeEvaluator
                            .getRelationList();
                    double distanceToBoundary = relationList
                            .getMaximumDifference();

                    if (distanceToBoundary > _distanceToBoundary) {
                        _distanceToBoundary = distanceToBoundary;
                        _lastDistanceToBoundary = relationList
                                .getPreviousMaximumDistance();
                        enabledTransition = preemptiveTrWithEvent;
                    }
                }

                if (nonPreemptiveTrWithEvent != null) {
                    ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) nonPreemptiveTrWithEvent
                            .getParseTreeEvaluator();
                    RelationList relationList = parseTreeEvaluator
                            .getRelationList();
                    double distanceToBoundary = relationList
                            .getMaximumDifference();

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

                // If we are close enough, then the flipping of the guard is OK.
                double errorTolerance = enclosingDirector.getErrorTolerance();
                if (_distanceToBoundary < errorTolerance) {
                    _distanceToBoundary = 0.0;
                    _lastDistanceToBoundary = 0.0;
                    return result;
                } else {
                    return false;
                }
            }
        } catch (Throwable throwable) {
            // Can not evaluate guard expression.
            throw new InternalErrorException(throwable);
        }
    }

    /** Override the base class so that if there is no enabled transition
     *  then we record for each relation (comparison operation) in each
     *  guard expression the distance between the current value of the
     *  variable being compared and the threshold.
     *  @exception IllegalActionException If thrown by any commit action
     *  or there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        FSMActor controller = getController();
        State currentState = controller.currentState();
        Transition lastChosenTransition = controller.getLastChosenTransition();
        if (lastChosenTransition == null) {
            // Only commit the current states of the relationlists
            // of all the transitions during these execution phases.
            Iterator iterator = currentState.nonpreemptiveTransitionList()
                    .listIterator();
            while (iterator.hasNext()) {
                Transition transition = (Transition) iterator.next();
                ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) transition
                        .getParseTreeEvaluator();
                RelationList relationList = parseTreeEvaluator
                        .getRelationList();
                relationList.commitRelationValues();
            }

            iterator = currentState.preemptiveTransitionList().listIterator();
            while (iterator.hasNext()) {
                Transition transition = (Transition) iterator.next();
                ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) transition
                        .getParseTreeEvaluator();
                RelationList relationList = parseTreeEvaluator
                        .getRelationList();
                relationList.commitRelationValues();
            }
        } else {
            // It is important to clear the history information of the
            // relation list since after this breakpoint, no history
            // information is valid.
            Iterator iterator = currentState.nonpreemptiveTransitionList()
                    .listIterator();
            while (iterator.hasNext()) {
                Transition transition = (Transition) iterator.next();
                ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) transition
                        .getParseTreeEvaluator();
                RelationList relationList = parseTreeEvaluator
                        .getRelationList();
                relationList.resetRelationList();
            }

            iterator = currentState.preemptiveTransitionList().listIterator();
            while (iterator.hasNext()) {
                Transition transition = (Transition) iterator.next();
                ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) transition
                        .getParseTreeEvaluator();
                RelationList relationList = parseTreeEvaluator
                        .getRelationList();
                relationList.resetRelationList();
            }
            // The superclass only calls fireAtCurrentTime() if there is an enabled
            // transition in the new state. We want to always call it after taking
            // a transition to get the effect of superdense time. That is, we will
            // fire again at the current time in the new state so that outputs from
            // the new state.
            NamedObj container = getContainer();
            if (container instanceof Actor) {
                Director executiveDirector = ((Actor)container).getExecutiveDirector();
                executiveDirector.fireAtCurrentTime((Actor)container);
            }
        }
        return super.postfire();
    }

    /** Override the base class to set current time to match that of
     *  the enclosing executive director, if there is one, regardless
     *  of whether that time is in the future or past. The superclass
     *  sets current time only if the local time is less than the
     *  environment time.
     *  Initialize the firing of the director by resetting all receivers to
     *  unknown.
     *  @return Whatever the superclass returns.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("HybridModalDirector: Called prefire().");
        }
        /* FIXME
        _resetAllReceivers();
        */
        Nameable container = getContainer();
        if (container instanceof Actor) {
            Director executiveDirector = ((Actor) container)
                    .getExecutiveDirector();
            if (executiveDirector != null) {
                Time outTime = executiveDirector.getModelTime();
                setModelTime(outTime);
                if (_debugging) {
                    _debug("HybridModalDirector: Setting local current time to: "
                            + outTime);
                }
            }
        }

        boolean result = true;
        // if any actor is not ready to fire, stop prefiring the
        // remaining actors, call super.prefire(), and return false;
        State st = getController().currentState();
        Actor[] actors = st.getRefinement();
        if (actors != null) {
            for (int i = 0; i < actors.length; ++i) {
                if (_stopRequested) {
                    break;
                }
                if (_debugging) {
                    _debug("Prefire the refinement of the current state: ",
                            actors[i].getFullName());
                }
                if (!actors[i].prefire()) {
                    result = false;
                    break;
                }
            }
        }
        return super.prefire() && result;
    }

    /** Return the minimum of the step sizes suggested by any
     *  actors that were fired in the current iteration.
     *  @return The suggested refined step size.
     *  @exception IllegalActionException If the step size cannot be further refined.
     */
    public double refinedStepSize() throws IllegalActionException {
        double result = Double.POSITIVE_INFINITY;
        Iterator actors = new ActorsFiredIterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof ContinuousStepSizeController) {
                double candidate = ((ContinuousStepSizeController) actor)
                        .refinedStepSize();
                if (candidate < result) {
                    result = candidate;
                }
            } else if (actor instanceof CompositeActor) {
                // Delegate to the director.
                Director director = actor.getDirector();
                if (director instanceof ContinuousStepSizeController) {
                    double candidate = ((ContinuousStepSizeController) director)
                            .refinedStepSize();
                    if (candidate < result) {
                        result = candidate;
                    }
                }
            }
        }

        // If this is inside a ContinuousDirector and there was a guard that
        // became enabled, then guess as to the new step size based on the
        // linear interpolation performed in the last invocation of
        // isStepSizeAccurate().
        if (_distanceToBoundary > 0.0) {
            ContinuousDirector enclosingDirector = _enclosingContinuousDirector();
            if (enclosingDirector != null) {
                double errorTolerance = enclosingDirector.getErrorTolerance();
                double currentStepSize = enclosingDirector.getCurrentStepSize();

                // Linear interpolation to refine the step size.
                // The "distances" here are not in time, but in the value
                // of continuous variables. The "last distance" is the distance
                // as of the previous postfire(), i.e., the previously committed
                // values of the continuous variables.
                // Note the step size is refined such that the distanceToBoundary
                // expected at the new step size is half of errorTolerance.
                double refinedStepSize = (currentStepSize * (_lastDistanceToBoundary + (errorTolerance / 2)))
                        / (_lastDistanceToBoundary + _distanceToBoundary);

                result = Math.min(result, refinedStepSize);
                // Note: To see how well this algorithm is working, you can
                // uncomment the following line.
                // System.out.println("refined step size: " + result);
            }
        }

        return result;
    }

    /** Roll back to committed state.
     *  This will roll back any actors that were fired in the current iteration.
     */
    public void rollBackToCommittedState() {
        Iterator actors = new ActorsFiredIterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof ContinuousStatefulComponent) {
                ((ContinuousStatefulComponent) actor)
                        .rollBackToCommittedState();
            } else if (actor instanceof CompositeActor) {
                // Delegate to the director.
                Director director = actor.getDirector();
                if (director instanceof ContinuousDirector) {
                    ((ContinuousDirector) director).rollBackToCommittedState();
                }
            }
        }
        // Reset the last chosen transition of the FSM controller to null
        // because upon moving forward again we may choose a different
        // transition.
        try {
            FSMActor controller = getController();
            if (controller != null) {
                controller.setLastChosenTransition(null);
            }
        } catch (IllegalActionException e) {
            // This should not occur.
            throw new InternalErrorException(e);
        }
    }

    /** Return the minimum of the step sizes suggested by any
     *  actors that were fired in current iteration.
     *  @return The suggested next step size.
     *  @exception IllegalActionException If an actor requests an
     *   illegal step size.
     */
    public double suggestedStepSize() throws IllegalActionException {
        double result = Double.POSITIVE_INFINITY;
        Iterator actors = new ActorsFiredIterator();
        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            if (actor instanceof ContinuousStepSizeController) {
                double candidate = ((ContinuousStepSizeController) actor)
                        .suggestedStepSize();
                if (candidate < result) {
                    result = candidate;
                }
            } else if (actor instanceof CompositeActor) {
                // Delegate to the director.
                Director director = actor.getDirector();
                if (director instanceof ContinuousStepSizeController) {
                    double candidate = ((ContinuousStepSizeController) director)
                            .suggestedStepSize();
                    if (candidate < result) {
                        result = candidate;
                    }
                }
            }
        }
        return result;
    }

    /** Transfer data from the specified input port of the
     *  container to the ports it is connected to on the inside.
     *  If there is no data on the specified input port, then
     *  set the ports on the inside to absent by calling sendClearInside().
     *  This method delegates the data transfer
     *  operation to the transferInputs method of the super class.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one token is transferred.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        // A special case: if the input port is not connected from outside,
        // meaning the !port.isConnected(), then we send clear all the (input)
        // ports awaiting inputs from this input port.
        // Also note that it is guaranteed that there exists only one relation
        // from the input port since only single port is supported for modal
        // model, we can call sendClearInside() with an argument 0.
        if (!port.isOutsideConnected()) {
            port.sendClearInside(0);
        }
        for (int i = 0; i < port.getWidth(); i++) {
            if (port.isKnown(i)) {
                if (port.hasToken(i)) {
                    result = super.transferInputs(port) || result;
                } else {
                    port.sendClearInside(i);
                }
            }
        }
        return result;
    }

    /** Transfer data from the specified output port of the
     *  container to the ports it is connected to on the outside.
     *  If there is no data on the specified output port, then
     *  set the ports on the outside to absent by calling sendClear().
     *  This method delegates the data transfer
     *  operation to the transferOutputs method of the super class.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if at least one token is transferred.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        for (int i = 0; i < port.getWidthInside(); i++) {
            if (port.isKnownInside(i)) {
                if (port.hasTokenInside(i)) {
                    result = super.transferOutputs(port) || result;
                } else {
                    port.sendClear(i);
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return the enclosing continuous director, or null if there
     *  is none.  The enclosing continuous director is a director
     *  above this in the hierarchy, possibly separated by composite
     *  actors with other foreign directors.
     *  @return The enclosing ContinuousDirector, or null if there is none.
     */
    protected ContinuousDirector _enclosingContinuousDirector() {
        if (_enclosingContinuousDirectorVersion != _workspace.getVersion()) {
            // Update the cache.
            _enclosingContinuousDirector = null;
            NamedObj container = getContainer();
            while (container != null) {
                // On the first pass, the container will be the immediate
                // container, whose director is this one, so update the
                // container first.
                container = container.getContainer();
                if (container instanceof Actor) {
                    Director director = ((Actor) container).getDirector();
                    if (director instanceof ContinuousDirector) {
                        _enclosingContinuousDirector = (ContinuousDirector) director;
                        break;
                    }
                }
            }
            _enclosingContinuousDirectorVersion = _workspace.getVersion();
        }
        return _enclosingContinuousDirector;
    }

    /** Reset all receivers to unknown status.
     */
    /* FIXME
    protected void _resetAllReceivers() {
        if (_debugging) {
            _debug("    HybridModalDirector is resetting all receivers");
        }

        Iterator receiverIterator = _receivers.iterator();
        while (receiverIterator.hasNext()) {
            ((FixedPointReceiver) receiverIterator.next()).reset();
        }
    }
    */

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return the first transition in the specified list that has
     *  an "event", which is a change in the boolean value of its guard
     *  since the last evaluation.
     *  @return A transition with a change in the value of the guard.
     */
    private Transition _checkEvent(List transitionList) {
        Iterator transitionRelations = transitionList.iterator();

        while (transitionRelations.hasNext() && !_stopRequested) {
            Transition transition = (Transition) transitionRelations.next();
            ParseTreeEvaluatorForGuardExpression parseTreeEvaluator = (ParseTreeEvaluatorForGuardExpression) transition
                    .getParseTreeEvaluator();
            RelationList relationList = parseTreeEvaluator.getRelationList();
            if (relationList.hasEvent()) {
                return transition;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Local variable to indicate the distance to boundary. */
    private double _distanceToBoundary = 0.0;

    /** The enclosing continuous director, if there is one. */
    private ContinuousDirector _enclosingContinuousDirector = null;

    /** The version for __enclosingContinuousDirector. */
    private long _enclosingContinuousDirectorVersion = -1;

    /** Local variable to indicate the last committed distance to boundary. */
    private double _lastDistanceToBoundary = 0.0;

    /** List of all receivers this director has created. */
    /* FIXME
    private List _receivers = new LinkedList();
    */

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Iterator over _stateRefinementsToPostfire followed by
     *  _transitionRefinementsToPostfire.
     */
    private class ActorsFiredIterator implements Iterator {
        public ActorsFiredIterator() {
            _iterator = _stateRefinementsToPostfire.iterator();
        }
        public boolean hasNext() {
            if (_iterator.hasNext()) {
                return true;
            }
            _iterator = _transitionRefinementsToPostfire.iterator();
            return _iterator.hasNext();
        }
        public Object next() {
            return _iterator.next();
        }
        public void remove() {
            // Ignore.
        }
        private Iterator _iterator;
    }
}
