/* An embedded director for CT inside CT or FSM.

Copyright (c) 1999-2004 The Regents of the University of California.
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

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTEmbeddedDirector
/**
   An embedded director for CT inside CT or FSM. Conceptually, this director
   interacts with a continuous outside domain. As a consequence, this
   director exposes its step size control information to the outer domain 
   through its container, which must be a CTCompositeActor.
   <P>
   Unlike the CTMixedSignalDirector, this director does not run ahead
   of the global time and rollback, simply because the step size control
   information is accessible from outer domain which has a continuous
   time and understands the meaning of step size.

   @see CTMultiSolverDirector
   @see CTTransparentDirector
   @author  Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class CTEmbeddedDirector extends CTMultiSolverDirector
    implements CTTransparentDirector {
    
    // FIXME: constrain this director to inherit the parameters of
    // the top-level CT director.
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public CTEmbeddedDirector() {
        super();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param container The container.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CTEmbeddedDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public CTEmbeddedDirector(Workspace workspace)  {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Always return true indicating that this director can be
     *  an inside director.
     *  @return True always.
     */
    public boolean canBeInsideDirector() {
        return true;
    }

    /** Always return false indicating that this director cannot
     *  be a top-level director.
     *  @return False always.
     */
    public boolean canBeTopLevelDirector() {
        return false;
    }

    /** Emit the tentative outputs of dynamic actors.
     */
    public void emitTentativeOutputs() {
        try {
            Iterator dynamicActors = getScheduler().getSchedule().get(
                    CTSchedule.DYNAMIC_ACTORS).actorIterator();
            while (dynamicActors.hasNext() && !_stopRequested) {
                CTDynamicActor dynamicActor = 
                    (CTDynamicActor) dynamicActors.next();
                dynamicActor.emitTentativeOutputs();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException (e);
        }
    }

    /** Execute the subsystem for one iteration. An iteration includes
     *  an event phase and a state-resolving phase.
     *  If states cannot be accurately resolved, then subsystem
     *  may produce a meaningless output. It is the outside system's
     *  responsibility to check whether this subsystem is accurate in
     *  this iteration, by calling the isThisStepAccurate() method
     *  of the CTCompositeActor that contains this director.
     *  @exception IllegalActionException If there does not exits a schedule,
     *  or ODE solver cannot be set, or any actor throws it during this 
     *  iteration. 
     */
    public void fire() throws IllegalActionException {
        // Update the beginning time of this iteration 
        // based on the time the upper level.
        // This process can not be performed inside prefire method since 
        // this reset is necessary if the current step size is not accurate.
        
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        // The execution phase is the execution phase of the top-level director.
        // All directors at different levels of hierarchy must be synchronized
        // to that.
        CTExecutionPhase executionPhase = getExecutionPhase();
        
        // Use the correct solver. If the current phase of execution is a 
        // discrete one, use a breakpoint solver. Otherwise, use a normal ODE 
        // solver.
        if (isDiscretePhase()) {
            _setCurrentODESolver(getBreakpointSolver());
        } else {
            _setCurrentODESolver(getODESolver());
        }
        
        if (executionPhase 
            == CTExecutionPhase.ITERATING_PURELY_DISCRETE_PHASE) {
            super._iteratePurelyDiscreteActors(schedule);
        } else if (executionPhase 
            == CTExecutionPhase.FIRING_DYNAMIC_ACTORS_PHASE) {
            getCurrentODESolver().fireDynamicActors();
        } else if (executionPhase 
            == CTExecutionPhase.FIRING_EVENT_GENERATORS_PHASE) {
            super.fireEventGenerators();
        } else if (executionPhase 
            == CTExecutionPhase.FIRING_STATE_TRANSITION_ACTORS_PHASE) {
            getCurrentODESolver().fireStateTransitionActors();
            // There is not a seperate state for producing output, 
            // because a CT subsystem needs to produce output 
            // if it works as a state transition actor. 
            super.produceOutput();
        } else if (executionPhase 
            == CTExecutionPhase.GENERATING_EVENTS_PHASE) {
            super._iterateEventGenerators(schedule);
        } else if (executionPhase 
            == CTExecutionPhase.GENERATING_WAVEFORMS_PHASE) {
//            // NOTE: the time a discrete phase execution (waveform phase) 
//            // starts is the same time the iteration time starts.
//            // NOTE: A ct composite actor is also a waveform generator.
//                
//            // FIXME: why update here? should this go to the prefire method?
//            // The time update only happens once!
//            // FIXME: how to make prefire method more useful? do stuff 
//            // as change ODE solver and update time.
//            CompositeActor container = (CompositeActor)getContainer();
//            Director exe = container.getExecutiveDirector();
//            Time time = exe.getModelTime();
//            setModelTime(exe.getModelTime());
//            _setIterationBeginTime(exe.getModelTime());
//                
            super._iterateWaveformGenerators(schedule);
        } else if (executionPhase 
            == CTExecutionPhase.PREFIRING_DYNAMIC_ACTORS_PHASE) {
            super.prefireDynamicActors();
        }
    }

    /** Return the current integration step size. 
     *  @return The current step size.
     */
    public double getCurrentStepSize() {
        CTGeneralDirector executiveDirector = 
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().getCurrentStepSize();
        } else {
            // This should never happen because a CT model with
            // a CTEmbeddedDirector must be used inside another CT model.
            throw new InternalErrorException("A CT model with " +
                "a CTEmbeddedDirector must be used inside another CT model.");
        }
    }

    /** Return the enclosing CT general director of this director. 
     *  @return The enclosing CT general director of this director. 
     */
    public CTGeneralDirector getEnclosingCTGeneralDirector() {
        CompositeActor container = (CompositeActor)getContainer();
        Director executiveDirector = container.getExecutiveDirector();
        if (executiveDirector instanceof CTGeneralDirector) {
            return (CTGeneralDirector) executiveDirector;
        } else {
            // This should never happen because a CT model with
            // a CTEmbeddedDirector must be used inside another CT model.
            throw new InternalErrorException("A CT model with " +
                "a CTEmbeddedDirector must be used inside another CT model.");
        }
    }

    /** Get the current execution phase of this director.
     *  @return The current execution phase of this director.
     */
    public CTExecutionPhase getExecutionPhase() {
        CTGeneralDirector executiveDirector = 
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().getExecutionPhase();
        } else {
            // This should never happen because a CT model with
            // a CTEmbeddedDirector must be used inside another CT model.
            throw new InternalErrorException("A CT model with " +
                "a CTEmbeddedDirector must be used inside another CT model.");
        }
    }

    /** Return the begin time of the current iteration. 
     *  @return The begin time of the current iteration.
     */
    public Time getIterationBeginTime() {
        CTGeneralDirector executiveDirector = 
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().getIterationBeginTime();
        } else {
            // This should never happen because a CT model with
            // a CTEmbeddedDirector must be used inside another CT model.
            throw new InternalErrorException("A CT model with " +
                "a CTEmbeddedDirector must be used inside another CT model.");
        }
    }

    /** Restore the saved states, which include the iteration begin time and
     *  the states of stateful actors.
     */
    public void goToMarkedState() {
        try {
            setModelTime(_savedIterationBeginTime);
            Iterator statefulActors = getScheduler().getSchedule().get(
                    CTSchedule.STATEFUL_ACTORS).actorIterator();
            while (statefulActors.hasNext() && !_stopRequested) {
                CTStatefulActor statefulActor = 
                    (CTStatefulActor) statefulActors.next();
                statefulActor.goToMarkedState();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException (e);
        }
    }
    
    /** Call initialize method of super class. Remove the first breakpoint, 
     *  the model start time, from the break-point table. 
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
    }
    
    /** Return true if this is the discrete phase execution.
     *  @return True if this is the discrete phase execution.
     */
    public boolean isDiscretePhase() {
        CTGeneralDirector executiveDirector = 
            getEnclosingCTGeneralDirector();
        if (executiveDirector != null) {
            return getEnclosingCTGeneralDirector().isDiscretePhase();
        } else {
            // This should never happen because a CT model with
            // a CTEmbeddedDirector must be used inside another CT model.
            throw new InternalErrorException("A CT model with " +
            "a CTEmbeddedDirector must be used inside another CT model.");
        }
    }

    /** Return true if all output step size control actors are satisfied
     *  with the current step size. 
     *  @return True if the current step size is accurate with respect to
     *  output step size control actors.
     */
    public boolean isOutputAccurate() {
        _outputAcceptable = _isOutputAccurate();
        return _outputAcceptable;
    }

    /** Return true if all state step size control actors are satisfied
     *  with the current step size. 
     *  @return True if the current step size is accurate with respect to
     *  state step size control actors.
     */
    public boolean isStateAccurate() {
        _stateAcceptable = _isStateAccurate();
        return _stateAcceptable;
    }

    /** Return true if the current step size is accurate. 
     *  This is determined by asking all the step size control actors 
     *  in the state transition schedule and output schedule.
     *  @return True if the current step is accurate.
     */
    public boolean isThisStepAccurate() {
        if (_debugging && _verbose) {
            _debug(getName() + ": Checking the accuracy of this step size:");
        }
        if (!_isStateAccurate()) {
            _stateAcceptable = false;
            return false;
        } else if (!_isOutputAccurate()) {
            _stateAcceptable = true;
            _outputAcceptable = false;
            return false;
        } else {
            _stateAcceptable = true;
            _outputAcceptable = true;
            return true;
        }
    }
    
    /** Mark the known good states. Including the iteration begin time
     *  and the states of the stateful actors.
     */
    public void markState() {
        try {
            _savedIterationBeginTime = getModelTime();
            Iterator statefulActors = getScheduler().getSchedule().get(
                    CTSchedule.STATEFUL_ACTORS).actorIterator();
            while (statefulActors.hasNext() && !_stopRequested) {
                CTStatefulActor statefulActor = 
                    (CTStatefulActor) statefulActors.next();
                statefulActor.markState();
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException (e);
        }
    }

    /** Return the predicted next step size, which is the minimum
     *  of the prediction from all step size control actors.
     *  @return The predicted step size from this subsystem.
     */
    public double predictedStepSize() {
        try {
            double nextStepSize = _predictNextStepSize();
            if (_debugging) {
                _debug(getName(), "at " + getModelTime(),
                    " predicts next step size as " + nextStepSize);
            }
            return nextStepSize;
        } catch (IllegalActionException ex) {
            throw new InternalErrorException (
                    " Fail to predict the next step size." + ex.getMessage());
        }
    }

    /** Call the prefire of the super class. Recompute the schedules 
     *  if necessary. Synchronize time with the outer domain,
     *  and adjust the contents of the breakpoint table with
     *  respect to the current time.
     *  @return True if the super.prefire returns true.
     *  @exception IllegalActionException If schedule can not be achieved.
     */
    public boolean prefire() throws IllegalActionException {
        return super.prefire();
    }

    /** Check whether the container implements the CTStepSizeControlActor
     *  interface. If not, then throw an exception. Call the preinitialize
     *  method of the super class.
     *  @exception IllegalActionException If the container of this
     *  director does not implement CTStepSizeControlActor, or the 
     *  preinitialize method throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getContainer() instanceof CTStepSizeControlActor)) {
            throw new IllegalActionException(this, "can only be contained by "
                    + "a composite actor that implements "
                    + "the CTStepSizeControlActor "
                    + "interface, for example, a continuous "
                    + "time composite actor.");
        }
        super.preinitialize();
    }

    /** Return the refined step size if the current fire is not accurate.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        try {
            if (!_stateAcceptable) {
                return _refinedStepWRTState();
            } else if (!_outputAcceptable) {
                return _refinedStepWRTOutput();
            } else {
                return Double.MAX_VALUE;
            }
        } catch ( IllegalActionException ex) {
            throw new InternalErrorException (
                    "Fail to refine step size. " + ex.getMessage());
        }
    }

    /** Return false if some actor returns false from its postfire method 
     *  or a stop is requested. 
     *  @return false if some actor returns false from its postfire method or 
     *  a stop is requested.
     *  @exception IllegalActionException If thrown by any actor during its
     *  postfire method.
     */
    public boolean postfire() throws IllegalActionException {
        if (getExecutionPhase() 
            == CTExecutionPhase.UPDATING_CONTINUOUS_STATES_PHASE) {
            super.updateContinuousStates();
        } else if (getExecutionPhase() 
            == CTExecutionPhase.POSTFIRING_EVENT_GENERATORS_PHASE) {
            super.postfireEventGenerators();
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicates whether actors in the output schedule are satisfied with
    // the current step size.
    private boolean _outputAcceptable;

    // A private variable that stores the known good time.
    private Time _savedIterationBeginTime;

    // Indicates whether actors in the dynamic actor schedule and the
    // state transition schedule are satisfied with the current step size.
    private boolean _stateAcceptable;
}

