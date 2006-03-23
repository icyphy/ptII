/* An CTCaseDirector governs the execution of the Case actor in CT.

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
package ptolemy.domains.ct.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.hoc.Case;
import ptolemy.actor.lib.hoc.CaseDirector;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CTCaseDirector

/**
 An CTCaseDirector governs the execution of the Case actor in CT.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (liuxj)
 */
public class CTCaseDirector extends CaseDirector implements CTTransparentDirector {

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
    public CTCaseDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Iterate the enbled refinements to emit the current states of their
     *  dynamic actors.
     *  @exception IllegalActionException If the current states can not
     *  be emitted.
     */
    public void emitCurrentStates() throws IllegalActionException {
        Case container = (Case)getContainer();
        Actor actor = container.getCurrentRefinement();
        if (actor instanceof CTDynamicActor) {
            ((CTDynamicActor) actor).emitCurrentStates();
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
            // an CTCaseDirector must be used inside a CT model.
            throw new InternalErrorException("A CTCase actor with "
                    + "an CTCaseDirector must be used inside a CT model.");
        }
    }

    /** Return error tolerance used for detecting enabled transitions.
     *  @return The error tolerance used for detecting enabled transitions.
     */
    public final double getErrorTolerance() {
        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();
        return executiveDirector.getErrorTolerance();
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
            // an CTCaseDirector must be used inside a CT model.
            throw new InternalErrorException("A modal model with "
                    + "an CTCaseDirector must be used inside a CT model.");
        }
    }

    /** Return the next iteration time obtained from the executive director.
     *  @return The next iteration time.
     */
    public Time getModelNextIterationTime() {
        CompositeActor cont = (CompositeActor) getContainer();
        Director execDir = cont.getExecutiveDirector();
        return execDir.getModelNextIterationTime();
    }

    /** Return the current time obtained from the executive director, if
     *  there is one, and otherwise return the local view of current time.
     *  @return The current time.
     */
    public Time getModelTime() {
        CompositeActor cont = (CompositeActor) getContainer();
        Director execDir = cont.getExecutiveDirector();

        if (execDir != null) {
            return execDir.getModelTime();
        } else {
            return super.getModelTime();
        }
    }

    /** Get the ODE solver class name.
     *  @return the name of the ODE solver class.
     */
    public String getODESolverClassName() {
        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();

        if (executiveDirector != null) {
            return executiveDirector.getODESolverClassName();
        } else {
            return null;
        }
    }

    /** Restore the states of all the enabled refinements to the
     *  previously marked states.
     */
    public void goToMarkedState() throws IllegalActionException {
        Case container = (Case)getContainer();
        Actor actor = container.getCurrentRefinement();
        if (actor instanceof CTStatefulActor) {
            ((CTStatefulActor) actor).goToMarkedState();
        }
    }

    /** Return true if the enabled refinements may produce events.
     *  @return True if the enabled refinements may produce events.
     */
    public boolean hasCurrentEvent() {
        Case container = (Case)getContainer();
        Actor actor = container.getCurrentRefinement();
        if (actor instanceof CTEventGenerator) {
            return ((CTEventGenerator) actor).hasCurrentEvent();
        }
        return false;
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
            // an CTCaseDirector must be used inside a CT model.
            throw new InternalErrorException("A CTCase actor with "
                    + "an CTCaseDirector must be used inside a CT model.");
        }
    }

    /** Retun true if all the current refinement has accurate output with
     *  the current step size.
     *  @return True if the current refinement has accurate output.
     */
    public boolean isOutputAccurate() {
        Case container = (Case)getContainer();
        Actor refinement = container.getCurrentRefinement();
        if (refinement instanceof CTStepSizeControlActor) {
            return ((CTStepSizeControlActor) refinement).isOutputAccurate();
        }
        return true;
    }

    /** Retun true if all the refinements can resolve their states with the
     *  current step size.
     *  @return True if all the refinements can resolve their states with the
     *  current step size.
     */
    public boolean isStateAccurate() {
        Case container = (Case)getContainer();
        Actor refinement = container.getCurrentRefinement();
        if (refinement instanceof CTStepSizeControlActor) {
            return ((CTStepSizeControlActor) refinement).isStateAccurate();
        }
        return true;
    }

    /** Make the current states of all the enabled refinements.
     */
    public void markState() {
        Case container = (Case)getContainer();
        Actor actor = container.getCurrentRefinement();
        if (actor instanceof CTStatefulActor) {
            ((CTStatefulActor) actor).markState();
        }
    }

    /** Return a CTReceiver. By default, the signal type is continuous.
     *  @return a new CTReceiver with signal type as continuous.
     */
    public Receiver newReceiver() {
        CTReceiver receiver = new CTReceiver();

        //FIXME: this is not right. Instead of blindly assigning a "continuous"
        //signal type, we need to derive the actual signal type from the
        //connections between ports.
        receiver.setSignalType(CTReceiver.CONTINUOUS);
        return receiver;
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
        Case container = (Case)getContainer();
        Actor refinement = container.getCurrentRefinement();
        if (refinement instanceof CTStepSizeControlActor) {
            return ((CTStepSizeControlActor) refinement).predictedStepSize();
        }
        return Double.MAX_VALUE;
    }

    /** Return true if all the dynamic actors contained by the enabled
     *  refinements return true from their prefire() method.
     *  @return True if all dynamic actors of enabled refinements can be
     *  prefired.
     *  @exception IllegalActionException If the local directors of refinements
     *  throw it.
     */
    public boolean prefireDynamicActors() throws IllegalActionException {
        Case container = (Case)getContainer();
        Actor refinement = container.getCurrentRefinement();
        if (refinement instanceof CTTransparentDirector) {
             return ((CTTransparentDirector) refinement).prefireDynamicActors();
        }
        return true;
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
        Case container = (Case)getContainer();
        Actor refinement = container.getCurrentRefinement();
        if (refinement instanceof CTStepSizeControlActor) {
            return ((CTStepSizeControlActor) refinement).refinedStepSize();
        }
        CTGeneralDirector executiveDirector = getExecutiveCTGeneralDirector();
        return executiveDirector.getCurrentStepSize();
    }
}
