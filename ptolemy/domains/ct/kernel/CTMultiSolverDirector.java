/* A CT director that utilizes multiple ODE solvers.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTMultiSolverDirector
/**
   A CTDirector that uses multiple ODE solvers. The reason for switching
   solvers is that when abrupt changes in signals or actor functions
   (also called breakpoints) occur, the state of the system
   has to be recalculated.
   At these points, a special ODE solver, called the "breakpointODESolver"
   is used. The simulation is executed as if the breakpoint is a new
   starting point. Typically, breakpointODESolvers do not advance time.
   <P>
   This director handles both predictable breakpoints, which are breakpoints
   that are registered in the breakpoint table, and unpredictable breakpoints,
   which are breakpoints that are not known before hand.
   <P>
   This director can only be a top-level director. For a CT domain inside
   an opaque composite actor, use CTMixedSignalDirector (if the outer
   domain is discrete) or CTEmbeddedDirector (if the outer domain is
   a CT domain or a HS domain.)
   <P>
   This director recognizes actors that implement the CTStepSizeControlActor
   interface. To adjust step sizes, it polls such actors.  If all are
   content with the current step size, then it attempts to raise the
   step size.  If any is not content, then it reduces the step size.
   If there are no such actors, then it leaves the step size where it is.
   <P>
   This director has two more parameters than the CTDirector base
   class.<BR>
   <UL>
   <LI><I>ODESolver</I>: This is the name of the normal ODE solver
   used in nonbreakpoint iterations.
   <LI><I>breakpointODESolver</I>: This is the name of the ODE solver that
   is used in the iterations just after the breakpoint. The breakpoint
   ODE solvers should not require history information (this property
   is called self-start). The default is
   "ptolemy.domains.ct.kernel.solver.DerivativeResolver"
   If there are Dirac impulses in the system, the
   "ptolemy.domains.ct.kernel.solver.ImpulseBESolver" may give
   a better result.
   <LI>All other parameters are maintained by the CTDirector base class. And the
   two solvers share them.
   <UL>

   @author  Jie Liu, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (liuj)
   @Pt.AcceptedRating Green (chf)
   @see ptolemy.domains.ct.kernel.CTDirector
*/
public class CTMultiSolverDirector extends CTDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public CTMultiSolverDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public CTMultiSolverDirector(Workspace workspace) {
        super(workspace);
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
     *   with the specified container.  May be thrown in derived classes.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CTMultiSolverDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the normal ODE solver
     *  used in nonbreakpoint iterations. The default is a String
     *  "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver"
     */
    public Parameter ODESolver;

    /** The name of the ODE solver that
     *  is used in the iterations just after the breakpoint. The default is
     *  "ptolemy.domains.ct.kernel.solver.DerivativeResolver"
     */
    public Parameter breakpointODESolver;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  In particular, if the <i>ODESolver</i> or the
     *  <i>breakpointODESolver</i> parameters are changed, then
     *  the corresponding solvers will be instantiated. If the ODEsolver
     *  instantiated is an instance of BreakpointODESolver, then
     *  an IllegalActionException will be thrown, and the original
     *  ODESolver will be unchanged.
     *  @param attribute The changed attribute.
     *  @exception IllegalActionException If the new solver that is specified
     *   is not valid.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == ODESolver) {
            if (_debugging) _debug(getFullName() + " updating  ODE solver...");
            _solverClassName =
                ((StringToken)ODESolver.getToken()).stringValue();
            ODESolver defaultSolver = _instantiateODESolver(_solverClassName);
            if (defaultSolver instanceof BreakpointODESolver) {
                throw new IllegalActionException(this, _solverClassName +
                        " can only be used as a breakpoint ODE solver.");
            }
            _defaultSolver = defaultSolver;
            _setCurrentODESolver(_defaultSolver);
        } else if (attribute == breakpointODESolver) {
            if (_debugging) _debug(getName() +" updating breakpoint solver...");
            _breakpointSolverClassName =
                ((StringToken)breakpointODESolver.getToken()).stringValue();
            _breakpointSolver =
                _instantiateODESolver(_breakpointSolverClassName);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return false always, since this director cannot be an inside director.
     *  @return false.
     */
    public boolean canBeInsideDirector() {
        return false;
    }

    /** Return true since this director can be a top-level director.
     *  @return true.
     */
    public boolean canBeTopLevelDirector() {
        return true;
    }

    /** Fire the system for one iteration. One iteration is defined as
     *  simulating the system at one time point, which includes
     *  processing discrete events, resolving states,
     *  and producing outputs.
     *  <P>
     *  An iteration begins with processing events, which includes
     *  that all waveform generators consuming current input events
     *  and all event generators producing current output events.
     *  Then the new values of the state variables are resolved.
     *  If the state is resolved successfully, the outputs are produced.
     *  <P>
     *  The step size of one iteration is determined by the suggested
     *  next step size and the breakpoints. If the first breakpoint in
     *  the breakpoint table is in the middle of the "intended" step.
     *  Then the current step size is reduced to the <code>
     *  breakpoint - current time </code>.
     *  The result of such a step is the left limit of the states
     *  at the breakpoint.
     *  <P>
     *  The new state is resolved by the resolveStates() method of the
     *  current ODE solver. After that, the step size control actors
     *  in the dynamic actor schedule and the state transition schedule
     *  are checked for the accuracy
     *  of the this step. If any one of the step size control
     *  actors do not think it is accurate, then the integration step
     *  will be restarted with a refined step size, which is the minimum
     *  of the refinedStepSize() from all step size control actors in
     *  the dynamic actor schedule and the state transition schedule.
     *  If all the actors in the dynamic actor and the state transition
     *  schedules think the current step is accurate, then the actors
     *  in the output path will be fired according to the output
     *  schedule. Then the step size control actors in the output
     *  path will be checked for accuracy. The above procedure is
     *  followed again to refine the step size and restart the iteration.
     *  The iteration is complete when all actors agree that the step
     *  is accurate.
     *  <P>
     *  All the actors are prefired before an iteration begins. If
     *  any one of them returns false, then the iteration is
     *  cancelled, and the function returns.
     *
     *  @exception IllegalActionException If thrown by the ODE solver.
     */
    public void fire() throws IllegalActionException {
        // FIXME: depend on the execution phase, perform discrete
        // or continuous phase execution.
        // For each phase execution, define a fixed point.
        if (_debugging) _debug(getName(), " fire: <<< ");

        // FIXME: do we need to do this?
        // Presummably, whenever _postfireReturns is true,
        // the execution stops after the postfire method of the director
        // is called. 
        // Also, the prefire method resets it to true already.
        // Reset this, since we may be at a new time.
        _postfireReturns = true;

        // set the start time of the current iteration
        _setIterationBeginTime(getCurrentTimeObject());
        
        // discrete phase execution to process discrete events.
        _discretePhaseExecution();

        // If the current time is the stop time, the fire method 
        // should return because no further execution is necessary.
        if (getCurrentTimeObject().equalTo(getStopTimeObject()) || !_postfireReturns) {
            return;
        }
        
        // continuous phase execution
        _continuousPhaseExecution();

        if (_debugging) _debug(getName(), " end of fire. >>>");
    }

    /** Return the breakpoint ODE solver.
     *  @return The breakpoint ODE solver.
     */
    public ODESolver getBreakpointSolver() {
        return _breakpointSolver;
    }

    /** Return the ODE solver.
     *  @return The default ODE solver
     */
    public ODESolver getODESolver() {
        return _defaultSolver;
    }

    /** Return true if there is an event at current time.
     *  @return True if there is an event at current time.
     */
    public boolean hasCurrentEvent() {
        // FIXME: this can simplified by using the breakpoints table
        // Such as: getBreakPoints().contains(getCurrentTime())
        // As long as we implement the event generators in such a way
        // that events are generated only in discrete phase execution
        // and when an event is detected, a breakpoint is inserted.
        // FIXME: this causes a problem that multiple events may be 
        // generated during one continuous phase execution, while only
        // the first one counts and the following ones may not even
        // occer in reality. So, we still a mechanism to tell whether
        // events are detected. 
        // In other words, break points and unpredicted events have to 
        // be dinstinguished.
        
        try {
            CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
            Iterator eventGenerators =
                schedule.get(CTSchedule.EVENT_GENERATORS).actorIterator();
            boolean hasDiscreteEvents = false;
            while (eventGenerators.hasNext()) {
                CTEventGenerator generator =
                    (CTEventGenerator) eventGenerators.next();
                // NOTE: We need to call hasCurrentEvent on all event generators,
                // since some event generator may rely on it.
                // NOTE: This is probably not right... actors should not be
                // relying on this.
                if (generator.hasCurrentEvent()) {
                    hasDiscreteEvents = true;
                }
            }
            // Also check breakpoint table for explicit requests from discrete
            // actors.
            if (getBreakPoints().contains(getCurrentTimeObject())) {
            if (_debugging) _debug(
                    " <<< First breakpoint in the break-point list is at "
                    + getBreakPoints().first());
                hasDiscreteEvents = true;
            }
            return hasDiscreteEvents;
        } catch (IllegalActionException ex) {
            throw new InternalErrorException (
                    "Can not get a valid schedule: " +
                    ex.getMessage());
        }
    }

    public boolean isPureDiscretePhase() {
        return _inPureDiscretePhase;
    }
    public boolean isWaveformGeneratingPhase() {
        return _inWaveformGeneratingPhase;
    }
    public boolean isEventGeneratingPhase() {
        return _inEventGeneratingPhase;
    }
    public boolean isCreatingIterationStartingStatesPhase() {
        return _inCreatingIterationStartingStatesPhase;
    }
    
    /** Construct a valid schedule. Invoke the initialize() method of the 
     *  super class to set current time and initialize all the actors directed
     *  by this director. Set the step size and the suggested next 
     *  step size for the first firing. Register both the current time and 
     *  the stop time as breakpoints.
     *  
     *  This method is called after types are resolved.
     *
     *  @exception IllegalActionException If the initialize method of the
     *  super class throws it, or the start time or stop time can not be
     *  registered as break points.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) _debug("----- Initializing: " + getFullName());

        // Schedule has to be computed before calling initialize() on
        // actors since some actors may call fireAt() in their initialize()
        // such as EventSource.

        if (_debugging) {
            // Display schedule
            _debug(getFullName(), " A schedule is computed.");
            _debug(getScheduler().getSchedule().toString());
        } else {
            getScheduler().getSchedule();
        }

        // Initialize protected variables and the contained actors.
        super.initialize();

        // set step sizes
        setCurrentStepSize(getInitialStepSize());
        if (_debugging) {
            _debug("Set current step size to " + getCurrentStepSize());
        }
        
        setSuggestedNextStepSize(getInitialStepSize());
        if (_debugging) {
            _debug("Set suggested next step size to "
                    + getSuggestedNextStepSize());
        }

        // register two breakpoints, the start time and stop time.
        if (_debugging) {
            _debug("Set the start time (the current time) as a break point: "
                    + getCurrentTimeObject());
        }
        fireAt(null, getCurrentTimeObject());
        
        if (_debugging) {
            _debug("Set the stop time as a break point: "
                    + getStopTimeObject());
        }
        fireAt(null, getStopTimeObject());
        
        if (_debugging) {
            _debug("----- End of Initialization of: " + getFullName());
        }
    }

    /** Return false if the stop time is reached or if any actor returned
     *  false to postfire().  Otherwise, returns true.
     *  @return false If the simulation is finished.
     *  @exception IllegalActionException If thrown by registering
     *  breakpoint
     */
    public boolean postfire() throws IllegalActionException {

        if (getCurrentTimeObject().compareTo(getStopTimeObject()) > 0) {
            // This should never happen. Otherwise, there is a bug. 
            throw new InternalErrorException("Execution can not exceed " +
                "stop time.");
        }
        
        // Now, the current time is equal to the stop time. 
        // If the breakpoints does not contain the current time, return false.
        if (getCurrentTimeObject().equalTo(getStopTimeObject()) &&
            !getBreakPoints().contains(getCurrentTimeObject()))  {
            if (_debugging) 
                _debug("Postfire returns false at: " + getCurrentTimeObject());
            return false;
        }
        
        if (_debugging) {
            _debug("Postfire returns " + (_postfireReturns && !_stopRequested) 
                + " at: " + getCurrentTimeObject());
        }
        return super.postfire();
    }

    /** Return true always, indicating that the system is always ready
     *  for one iteration. Note that no actors are prefired in this method.
     *
     *  @return True Always.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        // FIXME: check whether there are events, and decide the current
        // execution is in discrete phase or continuous phase.
        if (_debugging) _debug("Prefire returns true at: " + getCurrentTimeObject());
        _postfireReturns = true;
        return true;
    }

    /** After performing the preinitialize() method of the super class,
     *  instantiate all the solvers.
     *  @exception IllegalActionException If thrown by the super class,
     *  or not all solvers can be instantiated.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Instantiate ODE solvers
        if (_debugging) _debug(getFullName(), " instantiating the ODE solver ",
                _solverClassName);
        _defaultSolver = _instantiateODESolver(_solverClassName);
        _setCurrentODESolver(_defaultSolver);

        if (_debugging) _debug(getFullName(), "instantiating the " +
                " breakpoint solver ", _breakpointSolverClassName);
        _breakpointSolver =
            _instantiateODESolver(_breakpointSolverClassName);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Continuous phase execution. Solve the states and generate outputs
     *  by firing the continuous actors with the current step size.
     *  @exception IllegalActionException If one of the continuous actors
     *  throws it.
     */
    protected void _continuousPhaseExecution() throws IllegalActionException {
        if (_debugging) _debug(" !!! " + getName(), 
            ": continuous phase execution at " + getCurrentTimeObject());
        
        // Choose a suggested step size        
        setCurrentStepSize(getSuggestedNextStepSize());
        // Choose ODE solver
        _setCurrentODESolver(getODESolver());
        // Refine the correct step size for the continuous phase execution
        setCurrentStepSize(_refinedStepWRTBreakpoints());
        
        // Fire one iteration with the given step size.
        if (_debugging) _debug("execute the system from "+
        getCurrentTimeObject() + " step size " + getCurrentStepSize()
                + " using solver " + getCurrentODESolver().getName());
        _fireOneIteration();
    }
    
    /** Use breakpoint ODE solver to create the starting states for the 
     *  following continuous phase execution.
     *  @exception IllegalActionException If one of actors throws it or
     *  the ODE solver can not be set.
     */
    protected void _createIterationStartingStates() 
        throws IllegalActionException {
        if (_debugging) _debug("       creating the starting states for "
            + "the following continuous execution...");
        _setBreakpointIteration(true);
        _setCurrentODESolver(_breakpointSolver);
        _inCreatingIterationStartingStatesPhase = true;
        // NOTE: The step size in the breakpoint iteration is controlled
        // by the breakpoint ODE solver. There is no need to set the 
        // step size here.
        // FIXME: a simplified _fireOneIteration may be necessary, where
        // no step size refinement is needed. Two important points: 
        // states can always be resolved; outputs are always correct.
        _fireExactlyOneIteration();
        _inCreatingIterationStartingStatesPhase = false;
        _setBreakpointIteration(false);
    }
    
    
    /** Process discrete events in the system. The execution repeats executing 
     *  the discrete actors, wavefore generators, and event generators until 
     *  the execution reachs a fix point, where no more events happen at the 
     *  current time. 
     *  @exception IllegalActionException If one of the actors throws it.
     */
    protected void _discretePhaseExecution() throws IllegalActionException {
        if (_debugging) _debug(" !!! " + getName(), 
            ": discrete phase execution at " + getCurrentTimeObject());
        _setDiscretePhase(true);

        // Choose a suggested step size        
        setCurrentStepSize(getSuggestedNextStepSize());

        // A flag that indicates that a discrete phase execution reaches 
        // a fixed point. 
        boolean discreteFixpointReached = false;
        
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();

        // If the current time is not a breakpoint, 
        // a fix point is already reached. This method simply returns.
        if(!hasCurrentEvent()) {
            if (_debugging) {
                _debug(" >>> The fix point of the current discrete " +
                    "phase execution is already reached.");
            }
            discreteFixpointReached = true;
            _setDiscretePhase(false);
            return;
        }

        // The while loop will not end until one more iteration is performed
        // after the current time breakpoint is handled.
        // FIXME: the || hasCurrentEvent will be removed if the next two 
        // FIXMES are fixed.
        while ((!discreteFixpointReached || hasCurrentEvent()) &&
            _postfireReturns) {
            // Current time is a break point, remove it from the break points
            // table.
            _processBreakpoints();
            
            if (_debugging) {
                _debug("     Iterate the discrete phase execution once ...");
            }
            // FIXME: a schedule where all the actors are topologically sorted
            // may be necessary to accelerate the process to reach a fix point.
            if (_debugging) _debug("  ---> " + getName(), 
                ": iterating pure discrete actors (discrete -> discrete)");
            _iterateDiscreteActors(schedule);
            
            if (_debugging) _debug("  ---> " + getName(), 
                ": iterating waveform generators (discrete -> continuous)");
            _iterateWaveformGenerators(schedule);
            
            if (_debugging) _debug("  ---> " + getName(), 
                ": creating starting states for continuous phase execution");
            _createIterationStartingStates();

            // FIXME: output schedule should not contain event generators.

            if (_debugging) _debug("  ---> " + getName(), 
                ": generating more events (if any) (continuous -> discrete)");
            _iterateEventGenerators(schedule);
            
            // FIXME: we need an explicit iteration of all event generators.
            // We do not implement the event generators in such a way that 
            // break points are created when events happen. The reason for
            // this is that multiple event generators can generate more than
            // one events, while only the first one counts and the rest may
            // never occur in reality. Instead, we use the existing method 
            // hasCurrentEvent to check the existence of events.
            
            // FIXME: we can not use the general _iterateSchedule method,
            // instead, we have to explictly tell what actors to iterate.
            // One possible design is to use _iterateDiscreteActors, etc.
            // And the CTCompositeActor actor and HSDirector need to 
            // implement these methods. The implementation is simple. 
            // CTCompositeActor: getDirector()._iterateDiscreteActors.
            // HSDirector: iterate _enabledRefinements  to perform 
            // _iterateDiscreteActors.             
            
            // NOTE: if any more events are created by event generators, 
            // a new breakpoint at the current time should exist. Therefore,
            // we need another iteration. Otherwise, a fix point is reached.
            // As a side effect of the _createIterationStartingStates() method,
            // the starting states for the following continuous phase execution
            // are also ready.
            
            // FIXME: make all event generators call fireAt when events are
            // to be generated. The events are only generated at discrete phase.
            discreteFixpointReached = !_processBreakpoints();
            if (discreteFixpointReached && _debugging) {
                _debug(
                    "     >>> The next breakpoint is at " 
                    + getBreakPoints().first() + ", which is in the future.");
                _debug("     >>> So, the fix point of the current discrete " +
                    "phase execution is reached.");
            }
        }
        
        // We are leaving discrete phase execution...
        _setDiscretePhase(false);
    }

    protected void _fireExactlyOneIteration() throws IllegalActionException {
        if (_debugging && _verbose) _debug(
                "Fire one iteration from " + getCurrentTimeObject(),
                "using step size " + getCurrentStepSize());
        ODESolver solver = getCurrentODESolver();
        if (_debugging && _verbose) _debug( "Using ODE solver: " + solver.getName());
        // NOTE: This used to execute prefire() methods all at once before
        // anything else happened.  But this was too early.
        // The input data is not yet present.
        // Instead, we execute it before the first fire() in the iteration
        // for all actors except DYNAMIC actors (like integrators).
        // NOTE: the call to resolveStates() below increments time prior
        // to calling prefire() for the actors, so current time when
        // prefire is not (as it was before) the start time of the
        // iteration.  It is (unfortunately) the first guess as to the
        // end time of the iteration.  If this needs to be changed,
        // then probably the easiest place to do this is where prefire()
        // is called (which includes in ODESolver), by setting time back
        // to the start of the iteration. EAL 1/13/03
        // FIXME: what are the comments above about???
        prefireClear();
        // prefire dynamic actors (intergrators actually) to produce temporary
        // inputs for state transition actors.
        _prefireDynamicActors();

        solver.resolveStates();
        
        // Iterate output actors. 
        produceOutput();

        // postfire all the continuous actors.
        updateContinuousStates();

        // calculate a possible next step size.
        setSuggestedNextStepSize(_predictNextStepSize());
    }

    /** Fire one iteration. Return immediately if any actor returns false
     *  in their prefire() method. Time is advanced by the
     *  step size used.
     *  @exception IllegalActionException If one the actors throws it
     *    in its execution methods.
     */
    protected void _fireOneIteration() throws IllegalActionException {
        if (_debugging && _verbose) _debug(
                "Fire one iteration from " + getCurrentTimeObject(),
                "using step size " + getCurrentStepSize());
        ODESolver solver = getCurrentODESolver();
        if (_debugging && _verbose) _debug( "Using ODE solver: " + solver.getName());
        // NOTE: This used to execute prefire() methods all at once before
        // anything else happened.  But this was too early.
        // The input data is not yet present.
        // Instead, we execute it before the first fire() in the iteration
        // for all actors except DYNAMIC actors (like integrators).
        // NOTE: the call to resolveStates() below increments time prior
        // to calling prefire() for the actors, so current time when
        // prefire is not (as it was before) the start time of the
        // iteration.  It is (unfortunately) the first guess as to the
        // end time of the iteration.  If this needs to be changed,
        // then probably the easiest place to do this is where prefire()
        // is called (which includes in ODESolver), by setting time back
        // to the start of the iteration. EAL 1/13/03
        // FIXME: what are the comments above about???
        prefireClear();
        // prefire dynamic actors (intergrators actually) to produce temporary
        // inputs for state transition actors.
        _prefireDynamicActors();
        // If stop is not requested, keep trying to resolve states, where
        // the current step size may get refined due to states and outputs
        // constraints. 
        while (!_stopRequested) {
            while (!_stopRequested) {
                _inSolvingStates = true;
                
                // repeating resolving states until states converge.
                solver.resetRoundCount();
                solver.setConvergence(false);
                while (!solver.isConverged() && solver.resolveStates()) {
                    // solver.fireOneRound();
                    _inFiringDynamicActors = true;
                    solver.fireDynamicActors();
                    _inFiringDynamicActors = false;
                    _inFiringStateTransitionActors = true;
                    solver.fireStateTransitionActors();
                    _inFiringStateTransitionActors = false;
                }
                // FIXME: solver.resolveStates() returns true if the states are
                // resolved and returns false if the maximum iterations have 
                // been reached.
                if (solver.resolveStates()) {
                    if (_debugging && _verbose) 
                        _debug("State resolved by solver.");
                    // Check whether this step is acceptable
                    if (!_isStateAccurate()) {
                        setCurrentTimeObject(getIterationBeginTime());
                        setCurrentStepSize(_refinedStepWRTState());
                        if (_debugging && _verbose) {
                            _debug("Execute the system from "
                                    + getCurrentTimeObject()
                                    + " with step size" +
                                    getCurrentStepSize());
                        }
                    } else {
                        _inSolvingStates = false;
                        // States have be resolved. Note that the current
                        // time has been increased by the amount of the
                        // step size used in this iteration.
                        break;
                    }
                } else {
                    // Resolve state failed, e.g. in implicit methods.
                    if (getCurrentStepSize() < 0.5*getMinStepSize()) {
                        throw new IllegalActionException(this,
                                "Cannot resolve new states even using "+
                                "the minimum step size, at time "+
                        getCurrentTimeObject());
                    }
                    setCurrentTimeObject(getIterationBeginTime());
                    setCurrentStepSize(0.5*getCurrentStepSize());
                }
            }
            // If the while loop was caused by the stop request,
            // jump out of the loop in spite of whether the states 
            // have been resolved or not.
            if (_stopRequested) {
                break;
            }
            
            // Iterate output actors. 
            produceOutput();
            
            // FIXME: fire event generators but not iterate them
            fireEventGenerators();
            
            if (!_isOutputAccurate()) {
                setCurrentTimeObject(getIterationBeginTime());
                setCurrentStepSize(_refinedStepWRTOutput());
            } else {
                break;
            }
        }
        
        // calculate a possible next step size.
        setSuggestedNextStepSize(_predictNextStepSize());

        // postfire all the continuous actors.
        updateContinuousStates();

    }

    /** Initialize parameters to their default values.
     */
    protected void _initParameters() {
        super._initParameters();
        try {
            _solverClassName =
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver";
            ODESolver = new Parameter(
                    this, "ODESolver", new StringToken(_solverClassName));
            ODESolver.setTypeEquals(BaseType.STRING);
            _breakpointSolverClassName =
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver";
            breakpointODESolver = new Parameter(this, "breakpointODESolver",
                    new StringToken(_breakpointSolverClassName));
            breakpointODESolver.setTypeEquals(BaseType.STRING);
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    /** Return true if all step size control actors in the output
     *  schedule agree that the current step is accurate.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected boolean _isOutputAccurate() {
        if (_debugging) {
            _debug("Check accuracy of outputs for step size control actors:");
        }
        boolean accurate = true;
        Iterator actors;
        try {
            CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
            actors = schedule.get(
                    CTSchedule.STATE_STEP_SIZE_CONTROL_ACTORS).actorIterator();
        } catch (IllegalActionException e) {
            throw new InternalErrorException("Can not get schedule.");
        }
        while (actors.hasNext()) {
            CTStepSizeControlActor actor =
                (CTStepSizeControlActor) actors.next();
            boolean thisAccurate = actor.isOutputAccurate();
            if (_debugging) {
                _debug("  Checking output step size control actor: "
                        + ((NamedObj)actor).getName()
                        + ", which returns " + thisAccurate);
            }
            accurate = accurate && thisAccurate;
        }
        if (_debugging) {
            _debug("Overall output accuracy result: " + accurate);
        }
        return accurate;
    }

    /** Return true if all step size control actors in the dynamic actor
     *  schedule and the state transition schedule agree that
     *  the current step is accurate.
     */
    protected boolean _isStateAccurate() {
        if (_debugging) {
            _debug("Checking state accuracy for state step size "
                    + "control actors:");
        }
        boolean accurate = true;
        Iterator actors;
        try {
            CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
            actors = schedule.get(
                    CTSchedule.STATE_STEP_SIZE_CONTROL_ACTORS).actorIterator();
        } catch (IllegalActionException e) {
            throw new InternalErrorException("Can not get schedule.");
        }
        while (actors.hasNext()) {
            CTStepSizeControlActor actor =
                (CTStepSizeControlActor) actors.next();
            boolean thisAccurate = actor.isStateAccurate();
            if (_debugging) {
                _debug(((Nameable)actor).getName()
                        + " is accurate? "
                        + thisAccurate);
            }
            accurate = accurate && thisAccurate;
        }
        if (_debugging) {
            _debug("Overall state accuracy result: " + accurate);
        }
        return accurate;
    }


    protected void _iterateDiscreteActors(CTSchedule schedule) 
        throws IllegalActionException {
        _inPureDiscretePhase = true;
        _iterateSchedule(schedule.get(CTSchedule.DISCRETE_ACTORS));
        _inPureDiscretePhase = false;
    }

    protected void _iterateWaveformGenerators(CTSchedule schedule) 
        throws IllegalActionException {
        _inWaveformGeneratingPhase = true;
        _iterateSchedule(schedule.get(CTSchedule.WAVEFORM_GENERATORS));
        _inWaveformGeneratingPhase = false;
    }

    protected void _iterateEventGenerators(CTSchedule schedule) 
        throws IllegalActionException {
        _inEventGeneratingPhase = true;
        _iterateSchedule(schedule.get(CTSchedule.EVENT_GENERATORS));
        _inEventGeneratingPhase = false;
    }

    /** Iterate all the actors inside the given schedule, by prefiring, firing and 
     *  postfiring them. 
     *  @param schedule The given schedule that contains actors.
     *  @throws IllegalActionException If any actor throws it. 
     */
    protected void _iterateSchedule(ScheduleElement schedule) 
        throws IllegalActionException {
        Iterator actors = schedule.actorIterator();
        _verbose = true;
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (_debugging && _verbose) {
                _debug("Prefire actor: "
                        + ((Nameable)actor).getName()
                        + " at time "
                        + getCurrentTimeObject());
            }
            if (actor.prefire()) {
                if (_debugging && _verbose) {
                    _debug("Fire actor: "
                            + ((Nameable)actor).getName()
                            + " at time "
                            + getCurrentTimeObject());
                }
                actor.fire();
                if (_debugging && _verbose) {
                    _debug("Postfire actor: "
                            + ((Nameable)actor).getName()
                            + " at time "
                            + getCurrentTimeObject());
                }
                _postfireReturns = actor.postfire() && _postfireReturns;
            }
        }
    }



    /** Predict the next step size. This method should be called if the
     *  current integration step is accurate to estimate the step size
     *  for the next step. The predicted step size
     *  is the minimum of all predictions from step size control actors,
     *  and it never exceeds 10 times this step size.
     *  If there are no step-size control actors at all, then return
     *  the current step size.  This results in leaving the step size
     *  at its initial value.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _predictNextStepSize() throws IllegalActionException {
        if (!isBreakpointIteration()) {
            double predictedStep = 10.0*getCurrentStepSize();
            boolean foundOne = false;
            CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
            Iterator actors = schedule.get(
                    CTSchedule.STATE_STEP_SIZE_CONTROL_ACTORS).actorIterator();
            while (actors.hasNext()) {
                CTStepSizeControlActor actor =
                    (CTStepSizeControlActor) actors.next();
                predictedStep = Math.min(predictedStep,
                        actor.predictedStepSize());
                foundOne = true;
            }
            actors = schedule.get(
                    CTSchedule.OUTPUT_STEP_SIZE_CONTROL_ACTORS).actorIterator();
            while (actors.hasNext()) {
                CTStepSizeControlActor actor =
                    (CTStepSizeControlActor) actors.next();
                predictedStep = Math.min(predictedStep,
                        actor.predictedStepSize());
                foundOne = true;
            }
            if (foundOne) {
                return predictedStep;
            } else {
                // FIXME: This is a little bit conservative...
                return getCurrentStepSize();
            }
        } else {
            // The first iteration after a breakpoint iteration.
            // Use the initial step size.
            return getInitialStepSize();
        }
    }

    /** Invoke prefire() on all DYNAMIC_ACTORS, such as integrators,
     *  and emit their tentative outputs.
     *  Return true if all the prefire() methods return true and stop()
     *  is not called. Otherwise, return false.  Note that prefire()
     *  is called on all actors even if one returns false.
     *  @return The logical AND of the prefire() of dynamic actors, or
     *   false if stop() is called.
     */
    protected boolean _prefireDynamicActors()
            throws IllegalActionException {
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        boolean result = true;
        Iterator actors = schedule.get(
                CTSchedule.DYNAMIC_ACTORS).actorIterator();
        _inPrefiringDynamicActors = true;
        while (actors.hasNext() && !_stopRequested) {
            Actor actor = (Actor)actors.next();
            if (_debugging) _debug("Prefire dynamic actor: "
                    + ((Nameable)actor).getName());
            boolean ready = actor.prefire();
            if (!ready) {
                _inPrefiringDynamicActors = false;
                throw new IllegalActionException((Nameable)actor,
                        "Actor is not ready to fire. In the CT domain, all "
                        + "dynamic actors should be ready to fire at "
                        + "all times.\n "
                        + "Does the actor only operate on sequence of tokens?");
            }
            if (_debugging) _debug("Prefire of "
                    + ((Nameable)actor).getName()
                    + " returns " + ready);
            result = result && ready;
        }
        _inPrefiringDynamicActors = false;
        
        // NOTE: Need for integrators emit their current output so that
        // the state transition actors operate on the most current, up-to
        // date information.  Also, without this, on the first round of
        // firing, the state transition actors will have stale data.
        
        // FIXME: this may not be necessary since the solvers are responsible
        // to generate history imformations...
        Iterator integrators =
            schedule.get(CTSchedule.DYNAMIC_ACTORS).actorIterator();
        while (integrators.hasNext() && !_stopRequested) {
            CTDynamicActor dynamic = (CTDynamicActor)integrators.next();
            if (_debugging) _debug("Emit tentative state: "
                    + ((Nameable)dynamic).getName());
            dynamic.emitTentativeOutputs();
        }

        return result && !_stopRequested;
    }

    /** Return true if the breakpoint that has the same time as the 
     *  current time and remove that breakpoint.
     *  @return true if the current time is a breakpoint.
     *  @exception IllegalActionException If the breakpoint solver is
     *     illegal.
     */
    protected boolean _processBreakpoints() throws IllegalActionException  {
        boolean currentTimeIsABreakpoint = false;
        TotallyOrderedSet breakPoints = getBreakPoints();
        Time now = getCurrentTimeObject();
        // If now is a break point, remove the break point from table;
        if (breakPoints != null && !breakPoints.isEmpty()) {
            // FIXME: the following statement is unnecessary and may
            // hide some bugs...
            // breakPoints.removeAllLessThan(now);
            if (breakPoints.contains(now)) {
                // It is at a break point now.
                currentTimeIsABreakpoint = true;
                Time time = (Time) breakPoints.removeFirst();
                if (time.compareTo(now) < 0) {
                    throw new InternalErrorException("The first break point " +
                        "is in the past.");
                }
                if (_debugging) _debug(
                    "     >>> Remove " + now + " from the break-point list.");
            } 
        }
        return currentTimeIsABreakpoint;
    }

    /** Return the refined the step size with respect to the breakpoints.
     *  If the sum of the current step size plus current time exceeds the
     *  time of next breakpoint, reduce the step size such that the next
     *  breakpoint is the end time of the current iteration. 
     *  @return The refined step size.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _refinedStepWRTBreakpoints() 
        throws IllegalActionException {
        TotallyOrderedSet breakPoints = getBreakPoints();
        Time now = getCurrentTimeObject();
        double currentStepSize = getCurrentStepSize();
        Time iterationEndTime =
        getCurrentTimeObject().add(currentStepSize);
        _setIterationEndTime(iterationEndTime);
            
        if (breakPoints != null && !breakPoints.isEmpty()) {
            if (_debugging) _debug(
                    "The first breakpoint in the break-point list is at "
                    + breakPoints.first());
            // Adjust step size so that the first breakpoint is
            // not in the middle of this step.
            Time point = ((Time)breakPoints.first());
            if (iterationEndTime.compareTo(point) > 0) {
                if (_debugging) _debug(
                    "Refining the current step size w.r.t. " 
                    + "the next breakpoint:");
                currentStepSize 
                    = point.subtract(getCurrentTimeObject()).getTimeValue();
                _setIterationEndTime(point);
            }
        }
        return currentStepSize;
    }
    
    
    /** Return the refined the step size with respect to the outputs.
     *  It asks all the step size control actors in the state transition
     *  and dynamic schedule for the refined step size, and take the
     *  minimum of them.
     *  @return The refined step size.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _refinedStepWRTOutput() throws IllegalActionException {

        if (_debugging) _debug(
            "Refining the current step size w.r.t. all output actors:");

        double refinedStep = getCurrentStepSize();
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.OUTPUT_STEP_SIZE_CONTROL_ACTORS).actorIterator();
        while (actors.hasNext()) {
            CTStepSizeControlActor actor =
                (CTStepSizeControlActor)actors.next();
            refinedStep = Math.min(refinedStep, actor.refinedStepSize());
        }
        //FIXME: output constraints are different from the state constraints.
        if (refinedStep < getTimeResolution()) {
            throw new IllegalActionException(this,
                    "The refined step size is less than the minimum time "
                    + "resolution, at time " + getCurrentTimeObject());
        }

        if (_debugging)
            _debug(getFullName(), "refine step with respect to output to"
                    + refinedStep);

        _setIterationEndTime(getCurrentTimeObject().add(refinedStep));
        return refinedStep;
    }

    /** Return the refined step size with respect to resolving the
     *  new state.
     *  It asks all the step size control actors in the state transition
     *  and dynamic actor schedule for the refined step size, and takes the
     *  minimum of them. This method does not check whether the
     *  refined step size is less than the minimum step size.
     *  @return The refined step size.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _refinedStepWRTState() throws IllegalActionException {
        if (_debugging) _debug(
            "Refining the current step size w.r.t. all state actors:");
        double refinedStep = getCurrentStepSize();
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.STATE_STEP_SIZE_CONTROL_ACTORS).actorIterator();
        while (actors.hasNext()) {
            CTStepSizeControlActor actor =
                (CTStepSizeControlActor)actors.next();
            double size = actor.refinedStepSize();
            if (_debugging) _debug(((NamedObj)actor).getName(),
                    "refines step size to "
                    + size);
            refinedStep = Math.min(refinedStep, size);
        }
        if (refinedStep < 0.5*getMinStepSize()) {
            throw new IllegalActionException(this,
                    "Cannot resolve new states even using "+
                    "the minimum step size, at time "+
            getCurrentTimeObject());
        }
        _setIterationEndTime(getCurrentTimeObject().add(refinedStep));
        return refinedStep;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The classname of the ODE solver.
    private String _solverClassName;

    // The default solver.
    private ODESolver _defaultSolver;

    // The classname of the default ODE solver
    private String _breakpointSolverClassName;

    // The default solver.
    private ODESolver _breakpointSolver;  

    private boolean _inPureDiscretePhase = false;

    private boolean _inWaveformGeneratingPhase = false;

    private boolean _inEventGeneratingPhase = false;
    
    private boolean _inCreatingIterationStartingStatesPhase = false;
    
    private boolean _inSolvingStates = false;
    
    private boolean _inProducingOutputs = false;
    
    private boolean _inUpdatingContinuousStates = false;

    private boolean _inPrefiringDynamicActors= false;
    
    private boolean _inFiringEventGenerators = false;
    
    private boolean _inFiringDynamicActors = false;
    
    private boolean _inFiringStateTransitionActors = false;
    
    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTDirector#isSolvingStatesPhase()
     */
    public boolean isPrefiringDynamicActorsPhase() {
        return _inPrefiringDynamicActors;
    }

    public boolean isFiringEventGeneratorsPhase() {
        return _inFiringEventGenerators;
    }

    public boolean isSolvingStatesPhase() {
        return _inSolvingStates;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTDirector#isProducingOutputsPhase()
     */
    public boolean isProducingOutputsPhase() {
        return _inProducingOutputs;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTDirector#isUpdatingContinuousStatesPhase()
     */
    public boolean isUpdatingContinuousStatesPhase() {
        return _inUpdatingContinuousStates;
    }

    /** Fire all the actors in the output schedule.  If they have not
     *  had prefire() called in the current simulation cycle, then first
     *  call prefire().  The abstract semantics of Ptolemy II require that
     *  prefire() be called exactly once in an iteration.  This is important
     *  because, for example, time can only be tested reliably in prefire().
     *  It indicates the starting point of an integration step.
     *  During the multiple iterations of fires, time may progress
     *  in micro steps, depending on the ODE solver used. Hierarchies
     *  in CT and hybrid systems cases actually rely on this fact to
     *  control internal step sizes.
     *  @exception IllegalActionException If an actor in the output
     *   schedule throws it.
     */
    public void produceOutput() throws IllegalActionException {
        _inProducingOutputs = true;
        // FIXME: the OUTPUT_ACTORS schedule is neither complete
        // nor accurate (including sink actors...)
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors =
            schedule.get(CTSchedule.OUTPUT_ACTORS).actorIterator();
        while (actors.hasNext() && !_stopRequested) {
            Actor actor = (Actor)actors.next();
            if (!isPrefireComplete(actor)) {
                setPrefireComplete(actor);
                if (_debugging && _verbose) {
                    _debug("Prefire output actor: "
                            + ((Nameable)actor).getName()
                            + " at time "
                            + getCurrentTimeObject());
                }
                if (!actor.prefire()) {
                    throw new IllegalActionException((Nameable)actor,
                            "Actor is not ready to fire. In the CT domain, "
                            + "all continuous actors should be ready to fire "
                            + "at all times.\n"
                            + "Does the actor only operate on sequence "
                            + "of tokens?");
                }
            }
            if (_debugging) {
                _debug("Fire output actor: "
                        + ((Nameable)actor).getName()
                        + " at time "
                        + getCurrentTimeObject());
            }
            actor.fire();
        }
        _inProducingOutputs = false;
    }

    public void fireEventGenerators() throws IllegalActionException {
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors =
            schedule.get(CTSchedule.EVENT_GENERATORS).actorIterator();
        _inFiringEventGenerators = true;
        while (actors.hasNext() && !_stopRequested) {
            Actor actor = (Actor)actors.next();
            if (actor instanceof CTCompositeActor) {
                // CTCompositeActor has been fired during the produceOutput
                // method. It can not be fired twice in continuous phase
                // execution.
                // FIXME: why?
                continue;
            }
            if (!isPrefireComplete(actor)) {
                setPrefireComplete(actor);
                if (_debugging && _verbose) {
                    _debug("Prefire event generator: "
                            + ((Nameable)actor).getName()
                            + " at time "
                            + getCurrentTimeObject());
                }
                if (!actor.prefire()) {
                    _inFiringEventGenerators = false;
                    throw new IllegalActionException((Nameable)actor,
                            "Actor is not ready to fire. In the CT domain, "
                            + "all event generators should be ready to fire "
                            + "at all times.\n"
                            + "Does the actor only operate on sequence "
                            + "of tokens?");
                }
            }
            if (_debugging) {
                _debug("Fire event generator : "
                        + ((Nameable)actor).getName()
                        + " at time "
                        + getCurrentTimeObject());
            }
            actor.fire();
        }
        _inFiringEventGenerators = false;
    }

    /** Call postfire() on all actors in the continuous part of the model.
     *  For a correct CT simulation,
     *  the state of an actor can only change at this stage of an
     *  iteration. If the <i>synchronizeToRealTime</i> parameter is
     *  <i>true</i>,  then this method will block until the real time
     *  catches up to current modeling time.
     *  @exception IllegalActionException If any of the actors
     *   throws it.
     */
    public void updateContinuousStates() throws IllegalActionException {
        // Synchronize to real time if necessary.
        if (((BooleanToken)synchronizeToRealTime.getToken()).booleanValue()) {
            long realTime = System.currentTimeMillis()-_timeBase;
            long simulationTime = 
                (long)((getCurrentTimeObject()
                    .subtract(getStartTimeObject()).getTimeValue())*1000);
            if (_debugging) _debug("real time " + realTime,
                    "simulation time " + simulationTime);
            long timeDifference = simulationTime-realTime;
            if (timeDifference > 20) {
                try {
                    if (_debugging) _debug("Sleep for " + timeDifference
                            + "ms");
                    Thread.sleep(timeDifference - 20);
                }  catch (Exception e) {
                    throw new IllegalActionException(this,
                            "Sleep Interrupted" + e.getMessage());
                }
            } else {
                if (_debugging) _debug("Warning: " + getFullName(),
                        " cannot achieve real-time performance",
                        " at simulation time " + getCurrentTimeObject());
            }
        }
        _inUpdatingContinuousStates = true;
        // Call the postfire method of the continuous actors.
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.CONTINUOUS_ACTORS).actorIterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            if (_debugging) _debug("Postfire " + (Nameable)actor);
            _postfireReturns = _postfireReturns && actor.postfire();
        }
        _inUpdatingContinuousStates = false;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTGeneralDirector#isFiringDynamicActorsPhase()
     */
    public boolean isFiringDynamicActorsPhase() {
        return _inFiringDynamicActors;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.ct.kernel.CTGeneralDirector#isFiringStateTransitionActorsPhase()
     */
    public boolean isFiringStateTransitionActorsPhase() {
        return _inFiringStateTransitionActors;
    }
}
