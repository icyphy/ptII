/* An embedded director for CT inside CT/FSM.

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
   An embedded director for CT inside CT/FSM. Conceptually, this director
   interacts with a continuous outer domain. As a consequence, this
   director exposes its step size control information. If the container
   of this director is a CTCompositeActor, then this information is
   further exposed to the outer domain.
   <P>
   Unlike the CTMixedSignalDirector, this director does not run ahead
   of the global time and rollback, simply because the step size control
   information is accessible from outer domain which has a continuous
   time and understands the meaning of step size.

   @author  Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Yellow (liuj)
   @Pt.AcceptedRating Yellow (chf)
   @see CTMultiSolverDirector
   @see CTTransparentDirector
*/
public class CTEmbeddedDirector extends CTMultiSolverDirector
    implements CTTransparentDirector {
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
        //addDebugListener(new StreamListener());
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
     *  be a top level director.
     *  @return False always.
     */
    public boolean canBeTopLevelDirector() {
        return false;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#emitTentativeOutputs()
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
     *  an event phase, a state resolving phase, and a output phase.
     *  If the state cannot be accurately resolved, then subsystem
     *  may produce a meaningless output. It is the outer domain's
     *  responsibility to check if this subsystem is accurate in
     *  this iteration, by calling the isThisStepAccurate() method
     *  of the CTCompositeActor that contains this director.
     *  @exception IllegalActionException If one of the actors throw
     *  it during one iteration.
     */
    public void fire() throws IllegalActionException {
        // update local iteration begin time by the top level.
        // this can not be put inside prefire method since 
        // this reset is necessary if the current step size
        // is not accurate.
        _setIterationBeginTime(getIterationBeginTime());
        
        // FIXME: A rough design.
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        CTExecutionPhase executionPhase = getExecutionPhase();
        
        // Use the correct solver
        if (isDiscretePhase()) {
            _setCurrentODESolver(getBreakpointSolver());
        } else {
            _setCurrentODESolver(getODESolver());
        }
        
        if (executionPhase 
            == CTExecutionPhase.FIRINGPURELYDISCRETE_PHASE) {
            super._iteratePurelyDiscreteActors(schedule);
        } else if (executionPhase 
            == CTExecutionPhase.FIRINGDYNAMICACTORS_PHASE) {
            getCurrentODESolver().fireDynamicActors();
        } else if (executionPhase 
            == CTExecutionPhase.FIRINGEVENTGENERATORS_PHASE) {
            super.fireEventGenerators();
        } else if (executionPhase 
            == CTExecutionPhase.FIRINGSTATETRANSITIONACTORS_PHASE) {
            getCurrentODESolver().fireStateTransitionActors();
            // No seperate phase for producing output, because
            // a CT subsystem needs to produce output if it works
            // as a state transition actor. 
            super.produceOutput();
        } else if (executionPhase 
            == CTExecutionPhase.GENERATINGEVENTS_PHASE) {
            super._iterateEventGenerators(schedule);
        } else if (executionPhase 
            == CTExecutionPhase.GENERATINGWAVEFORMS_PHASE) {
            // NOTE: the time a discrete phase execution (waveform phase) 
            // starts is the same time the iteration time starts.
            // NOTE: A ct composite actor is also a waveform generator.
                
            // FIXME: why update here? should this go to the prefire method?
            // The time update only happens once!
            // FIXME: how to make prefire method more useful? do stuff 
            // as change ODE solver and update time.
            CompositeActor container = (CompositeActor)getContainer();
            Director exe = container.getExecutiveDirector();
            Time time = exe.getModelTime();
            setModelTime(exe.getModelTime());
            _setIterationBeginTime(exe.getModelTime());
                
            super._iterateWaveformGenerators(schedule);
        } else if (executionPhase 
            == CTExecutionPhase.PREFIRINGDYNAMICACTORS_PHASE) {
            super._prefireDynamicActors();
        }
    }

    /** Return the current integration step size. This method is final
     *  for performance reason.
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
     *
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

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#goToMarkedState()
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
    
    /** Call initialize method of super class.
     *  Remove the first breakpoint, the model start time, from the break 
     *  point table. 
     *  @see ptolemy.actor.Executable#initialize()
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        getBreakPoints().removeFirst();
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

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#isOutputAccurate()
     */
    public boolean isOutputAccurate() {
        _outputAcceptable = _isOutputAccurate();
        return _outputAcceptable;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#isStateAccurate()
     */
    public boolean isStateAccurate() {
        _stateAcceptable = _isStateAccurate();
        return _stateAcceptable;
    }

      /** Return true if the current integration step
     *  is accurate. This is determined by asking all the
     *  step size control actors in the state transition schedule and
     *  output schedule.
     *  @return True if the current step is accurate.
     */
    public boolean isThisStepAccurate() {
        _debug(getName() + ": Checking local actors for success.");
        if (!_isStateAccurate()) {
            //if (_debugging) _debug(getFullName() +
            //        " current step not successful because of STATE.");
            _stateAcceptable = false;
            return false;
        } else if (!_isOutputAccurate()) {
            //if (_debugging) _debug(getFullName() +
            //        " current step not successful because of OUTPUT.");
            _stateAcceptable = true;
            _outputAcceptable = false;
            return false;
        } else {
            _stateAcceptable = true;
            _outputAcceptable = true;
            return true;
        }
    }
    
    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTTransparentDirector#markState()
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

    /** Update the states of actors directed by this director.
     *  Discrete events at current time will be consumed and produced.
     *  @return True if this is not a top-level director, or the simulation
     *     is not finished and stop() has not been called.
     *  @exception IllegalActionException Not thrown in this base class.
     */
//    public boolean postfire() throws IllegalActionException {
//        if (_debugging) _debug(getFullName(), " postfire.");
//        // FIXME: this postfire method produces outputs
//        //_discretePhaseExecution();
//        updateContinuousStates();
//        // The current time will be the begin time of the next iteration.
//        _setIterationBeginTime(getCurrentTime());
//        return !_stopRequested;
//    }

    /** Return the predicted next step size, which is the minimum
     *  of the prediction from step size control actors.
     *  @return The predicted step size from this subsystem.
     */
    public double predictedStepSize() {
        try {
            if (_debugging) _debug(getName(), "at " + getModelTime(),
                    " predict next step size" + _predictNextStepSize());
            return _predictNextStepSize();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException (
                    " Fail predict the next step size." + ex.getMessage());
        }
    }

    /** Return true always. Recompute the schedules if there
     *  was a mutation. Synchronize time with the outer domain,
     *  and adjust the contents of the breakpoint table with
     *  respect to the current time.
     *  @return True always.
     */
    public boolean prefire() throws IllegalActionException {
        // FIXME: the following code can be simplified into
        // getScheduler().getSchedule();
        // or 
        // return true, 
        // because the initialize
        // method is responsible to get a valid schedule.
        // FIXME: will this be affected by mobile models?
        // I guess not, if the mobile model requests an initialization
        // whenever a model change happens.
        
        getScheduler().getSchedule();

        return super.prefire();
    }

    /** Check whether the container implements the CTStepSizeControlActor
     *  interface. If not, then throw an exception.
     *  @exception IllegalActionException If the container of this
     *  director does not implement CTStepSizeControlActor, or one of
     *  the actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        if (!(getContainer() instanceof CTStepSizeControlActor)) {
            throw new IllegalActionException(this, "can only be contained by "
                    + "a composite actor that implements "
                    + "the CTStepSizeControlActor "
                    + "interface, for example, the continuous "
                    + "time composite actor or the modal model.");
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

    /** Return false if some actors return false from postfire method 
     *  or the stop is requested.
     *  @return false if some actor returns false from postfire method or 
     *  the stop is requested.
     */
    public boolean postfire() throws IllegalActionException {
        if (getExecutionPhase() 
            == CTExecutionPhase.UPDATINGCONTINUOUSSTATES_PHASE) {
            super.updateContinuousStates();
        } else if (getExecutionPhase() 
            == CTExecutionPhase.POSTFIRINGEVENTGENERATORS_PHASE) {
            super.postfireEventGenerators();
        }
        return super.postfire();
        // FIXME: why I did this before???
        //return _postfireReturns && !_stopRequested;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Indicates whether actors in the output schedule are satisfied.
    private boolean _outputAcceptable;

    // Step size of the outer domain.
    private double _outsideStepSize;

    // The current time of the outer domain.
    private Time _outsideTime;
    
    private Time _savedIterationBeginTime;

    // Indicates whether actors in the dynamic actor schedule and the
    // state transition schedule are satisfied.
    private boolean _stateAcceptable;


}

