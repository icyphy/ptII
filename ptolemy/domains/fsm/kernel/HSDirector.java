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

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.Variable;
import ptolemy.domains.ct.kernel.CTTransparentDirector;
import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTReceiver;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.domains.ct.lib.Integrator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Director;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;

import java.util.Iterator;
import java.util.LinkedList;

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
        _ctrl._setInputVariables();
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

        _ctrl._setInputsFromRefinement();

        tr = _ctrl._chooseTransition(_st.nonpreemptiveTransitionList());

        // execute the refinements of the enabled transition
        if(tr != null) {
            Actor[] transitionActors = tr.getRefinement();
            if (transitionActors != null) {
                for (int i = 0; i < transitionActors.length; ++i) {
                    if (_stopRequested) break;
                    if (transitionActors[i].prefire()) {
                        if(_debugging) {
                            _debug(getFullName(), " fire transition refinement",
                                  ((ptolemy.kernel.util.NamedObj)transitionActors[i]).getName());
                        }
                        transitionActors[i].fire();
                        transitionActors[i].postfire();
                    }
                }
                _ctrl._setInputsFromRefinement();
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

    /** Return true if the current integration step is accurate with
     *  the respect of all the enabled refinements, which are refinements
     *  that returned true in their prefire() methods in this iteration.
     *  Also return true if there
     *  are no refinements. If a refinement is not a
     *  CTStepSizeControlActor, we assume the refinement thinks this
     *  step to be accurate.
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
        // check if the transition is accurate
        // FIXME: only handle the non-preemptive transitions
        try {
            Transition tr = _ctrl._checkTransition(_st.
                nonpreemptiveTransitionList());
            if (tr == null) {
                _lastTransitionAccurate = true;
                return result;
            }
            else {
                Variable guard = tr._guard2;
                _distanceToBoundary = ( (DoubleToken) guard.getToken()).
                    doubleValue();
                //System.out.println("the distance to guard "+ guard.getExpression() + " is " +  _distanceToBoundary);

                // the returned value as -1.0 indicating this transition is
                // always enabled, return true. The HSDirector is responsible
                // to refire itself at the current time.

                if (_distanceToBoundary == -1.0) {
                    //System.out.println("==>the guard " + guard.getExpression() + " is true.");
                    return true;
                }

                _transitionAccurate = (_distanceToBoundary < dir.getErrorTolerance());

                if (_transitionAccurate) {
                    _lastTransitionAccurate = true;
                }
                return result && _transitionAccurate;
            }
        } catch (Exception e) {
            //FIXME: handle the exception
            System.out.println("FIXME:: " + e.getMessage());
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
     *  refinements generate. Examine the outgoing transitions of the
     *  current state. Throw an exception if there is more than one
     *  transition enabled. If there is exactly one transition enabled
     *  then it is chosen and the choice actions contained by the
     *  transition are executed. Execute the commit actions contained
     *  by the last chosen transition of the mode controller and set
     *  its current state to the destination state of the transition.
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If thrown by any action, or
     *   there is no controller, or there is more than one transition
     *   enabled.
     */
    public boolean postfire() throws IllegalActionException {
        Director dir= ((Actor)getContainer()).getExecutiveDirector();
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
            _ctrl._setInputsFromRefinement();
        }
        // FIXME: why do we need to execute the update actions again in postfire?
        Transition tr =
            _ctrl._chooseTransition(_st.outgoingPort.linkedRelationList());
        if (tr != null) {
            CompositeActor container = (CompositeActor)getContainer();
            dir.fireAt(container, getCurrentTime());
        }
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

    /** Return the step size refined by all the enabled refinements,
     *  which are refinements that returned true
     *  in their prefire() methods in this iteration.
     *  If there are no refinements, then return the current step size
     *  of the executive director.
     *  If a refinement is not a CTStepSizeControlActor, then
     *  its refined step size is the current step size of the
     *  executive director..
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
            // FIXME: needs better algorithm
            // FIXME: only handles one integrator

            // If last step size is not accurate, we use a linear interpolation
            // approach to get the refined step size; otherwise, we use the
            // maximum value of current derivatives of state variables to refine the step size.

            // Notice, we try to refine the step size such that the distance
            // to boundary is errorTolerance/2.
            double possibleStepSize = result;
            double errorTolerance = dir.getErrorTolerance();

            if (!_lastTransitionAccurate) {

                //System.out.println();
                //System.out.println("====> using linear interopolation.");
                //System.out.println("====> current step size " + result + " former step size " + _lastStepSize);
                //System.out.println("====> current distance to boundary " + _distanceToBoundary + " former " + _lastDistanceToBoundary);

                possibleStepSize = _lastStepSize - (_lastStepSize - result)
                    *( _lastDistanceToBoundary - errorTolerance/2) / (_lastDistanceToBoundary - _distanceToBoundary);
            } else {

                //System.out.println();
                //System.out.println("***** using derivative.");
                double maximumDerivative = 0.0;

                Iterator actors = _enabledRefinements.iterator();
                while (actors.hasNext()) {
                    CompositeActor actor = (CompositeActor) actors.next();
                    Iterator integrators = actor.entityList(Integrator.class).
                        iterator();
                    while (integrators.hasNext()) {
                        Integrator integrator = (Integrator) integrators.next();
                        try {
                            double input = ( (DoubleToken) integrator.input.get(
                                0)).
                                absolute().doubleValue();
                            if (input > maximumDerivative) {
                                maximumDerivative = input;
                            }
                        }
                        catch (IllegalActionException e) {
                            //FIXME: how to handle the exception.
                            System.out.println("FIXME" + e.getMessage());
                            maximumDerivative = 1.0;
                        }
                    }
                }

                possibleStepSize = result - (_distanceToBoundary - errorTolerance/2) /maximumDerivative;
            }

            // save for next step size checking.
            _lastDistanceToBoundary = _distanceToBoundary;
            _lastTransitionAccurate = false;
            _lastStepSize = result;

            //System.out.println("           refined current Step size " + possibleStepSize);

            if (possibleStepSize < 0.0) {
                // This refined step size does not make sense.
                return result/2.0;
            } else {
                // step size is always reduced.
                return possibleStepSize;
            }
        }
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

    // Lcoal variable to indicate the last transition accurate or not.
    private boolean _lastTransitionAccurate = true;
}
