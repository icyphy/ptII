/* An HSDirector governs the execution of the discrete dynamics of a
   hybrid system model.

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
@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.domains.ct.kernel.CTCompositeActor;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTEventGenerator;
import ptolemy.domains.ct.kernel.CTReceiver;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.domains.ct.kernel.CTTransparentDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// HSDirector
/**
An HSDirector governs the execution of the discrete dynamics of a hybrid
system model.
<p>
<a href="http://ptolemy.eecs.berkeley.edu/publications/papers/99/hybridsimu/">
Hierarchical Hybrid System Simulation</a> describes how hybrid system models
are built and simulated in Ptolemy II.
<p>
Note: this class is still under development.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 1.0
*/
public class HSDirector extends FSMDirector implements CTTransparentDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HSDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public HSDirector(Workspace workspace) {
        super(workspace);
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        if (_debugging) _debug(getName(), " fire.");
        if (_firstFire) {
            Actor[] actors = _st.getRefinement();
            _enabledRefinements = new LinkedList();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (actors[i].prefire()) {
                        _enabledRefinements.add(actors[i]);
                    }
                }
            }
            _firstFire = false;
        }
        _ctrl._readInputs();
        if (_debugging) _debug(getName(), " find FSMActor " + _ctrl.getName());
        Transition tr =
            _ctrl._chooseTransition(_st.preemptiveTransitionList());
        if (tr != null) {

            Actor[] actors = tr.destinationState().getRefinement();
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
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
        Iterator actors = _enabledRefinements.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (_debugging) _debug(getName(), " fire refinement",
                    ((ptolemy.kernel.util.NamedObj)actor).getName());
            actor.fire();
        }

        _ctrl._readOutputsFromRefinement();

        // Note that the output actions associated with the transition
        // are executed.
        tr = _ctrl._chooseTransition(_st.nonpreemptiveTransitionList());
        // record the enalbed transition
        _enabledTransition = tr;

        // execute the refinements of the enabled transition
        if (tr != null) {
            Actor[] transitionActors = tr.getRefinement();
            if (transitionActors != null) {
                for (int i = 0; i < transitionActors.length; ++i) {
                    if (_stopRequested) break;
                    if (transitionActors[i].prefire()) {
                        if (_debugging) {
                            _debug(getFullName(),
                                    " fire transition refinement",
                                    ((ptolemy.kernel.util.NamedObj)transitionActors[i]).getName());
                        }
                        transitionActors[i].fire();
                        transitionActors[i].postfire();
                    }
                }
                _ctrl._readOutputsFromRefinement();
                // execute the output actions, since these are normally
                // executed in chooseTransition, but the outputs may
                // have been changed by the transition refinemenets
                Iterator actions = tr.choiceActionList().iterator();
                while (actions.hasNext()) {
                    Action action = (Action)actions.next();
                    action.execute();
                }
            }
        }
        return;
    }

    /** Return the current time obtained from the executive director, if
     *  there is one, and otherwise return the local view of current time.
     *  @return The current time.
     */
    public double getCurrentTime() {
        CompositeActor cont = (CompositeActor)getContainer();
        Director execDir = (Director)cont.getExecutiveDirector();
        if (execDir != null) {
            return execDir.getCurrentTime();
        } else {
            return super.getCurrentTime();
        }
    }

    /** Return the next iteration time obtained from the executive director.
     *  @return The next iteration time.
     */
    public double getNextIterationTime() {
        CompositeActor cont = (CompositeActor)getContainer();
        Director execDir = (Director)cont.getExecutiveDirector();
        return execDir.getNextIterationTime();
    }

    /** Return true if the current refinement produces events.
     *  @return True if the current refinement produces events.
     */
    public boolean hasCurrentEvent() {
        // FIXME: delegate the method to the enabled refinement.
        boolean eventPresent = false;
        Iterator actors = _enabledRefinements.iterator();
        while (!eventPresent && actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (actor instanceof CTCompositeActor) {
                eventPresent |= 
                    ((CTCompositeActor)actor).hasCurrentEvent();
            }
        }
        return eventPresent;
    }

    /** Return true if there are no refinements, or if the current
     *  integration step is accurate with the respect of all the enabled
     *  refinements, which are refinements that returned true in their
     *  prefire() methods in this iteration, or if a refinement is not a
     *  CTStepSizeControlActor; and if the current time is exactly the same
     *  time the transition is enabled.
     *  @return True if the current step is accurate.
     */
    public boolean isThisStepAccurate() {
        boolean result = true;
        CTDirector dir= (CTDirector)(((Actor)getContainer()).getExecutiveDirector());
        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();
            while (refinements.hasNext()) {
                Actor refinement = (Actor) refinements.next();
                if (refinement instanceof CTStepSizeControlActor) {
                    result = result && ((CTStepSizeControlActor)
                            refinement).isThisStepAccurate();
                }
            }
        }

        // check if the transition is enabled at the current time
        // FIXME: only handle the non-preemptive transitions
        // Preemptive transitions need no step size refinement.
        try {
            // Check if there is any transition enabled.
            Transition tr = _ctrl._checkTransition(_st.nonpreemptiveTransitionList());
            if (tr != null) {
                if (_debugging) {
                    _debug("Find enabled transition:  " +
                            tr.getGuardExpression());
                }
            }
            // Check if there is any events detected.
            Transition trWithEvent = _checkEvent(_st.nonpreemptiveTransitionList());
            if (trWithEvent != null) {
                if (_debugging) {
                    _debug("Detected event for transition:  " +
                            trWithEvent.getGuardExpression());
                }
            }

            // If no transition is enabled, try to set "tr" as the transition
            // with event detected.
            if (tr == null && trWithEvent != null) {
                tr = trWithEvent;
            }

            // If there is no transition enabled, the last step size is accurate for
            // transitions. The status of the relations of the guard expressions are
            // committed into all the associated relation lists.
            if (tr == null && trWithEvent == null) {
                _lastTransitionAccurate = true;
                Iterator iterator = _st.nonpreemptiveTransitionList().listIterator();
                while (iterator.hasNext()) {
                    ((Transition) iterator.next()).getRelationList().commitRelationValues();
                }
                return result;
            }
            else {
                // There is one transition enabled. We check the maximum difference
                // of all relations that change their status with the current step size.
                RelationList relationList = tr.getRelationList();
                _distanceToBoundary = relationList.maximumDifference();

                _transitionAccurate = (_distanceToBoundary < dir.getErrorTolerance());

                if (_debugging) {
                    _debug("==> the guard " + tr.getGuardExpression() +
                            " has difference " + _distanceToBoundary);
                }

                if (_transitionAccurate) {
                    _lastTransitionAccurate = true;
                } else {
                    // Retrive the former distance of the relation which has the
                    // biggest difference with the current step size.
                    // The former distance will be used to refine the step size.
                    _lastDistanceToBoundary = relationList.getFormerMaximumDistance();
                }
                return result && _transitionAccurate;
            }
        } catch (Exception e) {
            //FIXME: handle the exception
            System.out.println(
                    "FIXME:: HSDirector.isThisStepAccurate() throws exception ");
            e.printStackTrace();
            return result;
        }
    }

    /** Return a CTReceiver.
     *  @return a new CTReceiver.
     */
    public Receiver newReceiver() {
        CTReceiver receiver = new CTReceiver();
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
        CompositeActor container = (CompositeActor)getContainer();
        Director dir= container.getExecutiveDirector();
        Iterator refinements = _enabledRefinements.iterator();
        while (refinements.hasNext()) {
            Actor refinement = (Actor)refinements.next();
            refinement.postfire();
            // take out event outputs generated in ref.postfire()
            Iterator outports = refinement.outputPortList().iterator();
            while (outports.hasNext()) {
                IOPort p = (IOPort)outports.next();
                transferOutputs(p);
            }
            _ctrl._readOutputsFromRefinement();
        }
        // FIXME: why do we need to execute the update actions again in postfire?
        // From Xiaojun: actions of the event triggered transition need to be 
        // executed. 
        // This is not necessary. hyzheng 08/14/2003
        // Transition tr = _ctrl._chooseTransition(_st.outgoingPort.linkedRelationList());
         Transition tr = _enabledTransition;

        // If there is one transition enabled, the HSDirector requests
        // fire again at the same time to see whether the next state
        // has some outgoing transition enabled.
        if (tr != null) {
            if (_debugging) {
                _debug("Postfire deals with enabled transition " +
                        tr.getGuardExpression());
            }
            Iterator iterator = _st.nonpreemptiveTransitionList().listIterator();
            // It is important to clear the history information of the
            // relation list since after this breakpoint, no history
            // information is valid.
            while (iterator.hasNext()) {
                ((Transition) iterator.next()).getRelationList().clearRelationList();
            }
            // If the top level of the model is modal model, the director
            // is null. We do not request to fire again since no one in upper
            // hierarchy will do that.
            if (dir != null) {
                if (_debugging) {
                    _debug("HSDirector requests refiring at " + getCurrentTime());
                }
                dir.fireAt(container, getCurrentTime());
            }
        }
        // clear the cached enabled transition
        _enabledTransition = null;
        return super.postfire();
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
                Actor refinement = (Actor)refinements.next();
                if (refinement instanceof CTStepSizeControlActor) {
                    result = Math.min(result, ((CTStepSizeControlActor)
                            refinement).predictedStepSize());
                }
            }
        }
        return result;
    }

    /** Set the controller. Call super.prefire().
     */
    public boolean prefire() throws IllegalActionException {
        _ctrl = getController();
        _st = _ctrl.currentState();

        return super.prefire();
    }

    /** Check if the modal model is embedded inside a CT model or another 
     *  modal model with a HSDirector.
     * 
     *  @exception IllegalActionException If parent class throws it or the 
     *  immediately upper director is neither a CTDirector nor a HSDirector. 
     */
//    public void preinitialize() throws IllegalActionException {
//        super.preinitialize();
//        Nameable container = getContainer();
//        if (container instanceof CompositeActor) {
//            CompositeActor compositeActor = (CompositeActor)container; 
//            if (!(compositeActor.getExecutiveDirector() instanceof CTDirector 
//                  || compositeActor.getExecutiveDirector() instanceof 
//                  CTTransparentDirector)) {
//                throw new IllegalActionException("HSDirector is designed for " +
//                    "a modal model embedded inside a CT model or another " +
//                    "modal model with a HSDirector. If the modal " +
//                    "model is embedded inside other domains, like DE or SDF, " +
//                    "use FSMDirector instead. Right click the modal model and " +
//                    "configure the directorClass parameter.");  
//            }
//        }
//    }

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
        CTDirector dir= (CTDirector)(((Actor)getContainer()).getExecutiveDirector());
        double result = dir.getCurrentStepSize();
        if (_enabledRefinements != null) {
            Iterator refinements = _enabledRefinements.iterator();
            while (refinements.hasNext()) {
                Actor refinement = (Actor)refinements.next();
                if (refinement instanceof CTStepSizeControlActor) {
                    result = Math.min(result, ((CTStepSizeControlActor)
                            refinement).refinedStepSize());
                }
            }
        }

        if (_transitionAccurate) {
            return result;
        } else {
            double refinedStepSize = result;
            double errorTolerance = dir.getErrorTolerance();
            double currentStepSize = dir.getCurrentStepSize();

            // Linear interpolation to refine the step size.
            // Note the step size is refined such that the distanceToBoundary
            // is half of errorTolerance.
            refinedStepSize = currentStepSize * (_lastDistanceToBoundary + errorTolerance/2)
                / (_lastDistanceToBoundary + _distanceToBoundary);

            if (refinedStepSize > result) {
                return result;
            } else {
                return refinedStepSize;
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    // FIXME: where is the right position to put this method?
    // This method detects any events happened during one step size.
    private Transition _checkEvent(List transitionList) {
        Transition result = null;
        Iterator transitionRelations = transitionList.iterator();
        while (transitionRelations.hasNext() && !_stopRequested) {
            Transition transition = (Transition) transitionRelations.next();
            if (transition.getRelationList().hasEvent()) {
                return transition;
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached reference to mode controller.
    private FSMActor _ctrl = null;

    // Cached reference to current state.
    private State _st = null;

    // Lcoal variable to indicate whether the transition is accurate.
    private boolean _transitionAccurate = true;

    // Lcoal variable to indicate the distance to boundary.
    private double _distanceToBoundary = 0.0;

    // Lcoal variable to indicate the last distance to boundary.
    private double _lastDistanceToBoundary = 0.0;

    // Lcoal variable to indicate the last step size.
    private double _lastStepSize = 0.0;

    private Transition _enabledTransition;
    // Lcoal variable to indicate the last transition accurate or not.
    private boolean _lastTransitionAccurate = true;
}
