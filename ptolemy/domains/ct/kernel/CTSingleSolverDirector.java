/* A CTDirector that uses only one ODE solver.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (johnr@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.expr.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.*;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// CTSingleSolverDirector
/**
A CTDirector that uses only one ODE solver. The solver is a parameter
of the director called "ODESolver". The default solver is the
ForwardEulerSolver.
The solver of this director must be able to self start, so any solver
that uses history points can not be the solver for this director.
<P>
This director can handle explicit breakpoints, which are breakpoints
that are registered in the breakpoint table. It does not handle
unexpected breakpoints like event detections.  This director can
only be a top-level director. Since impulse backward Euler method
does not advance time, it should not be used as the solver for this
director. As a result, if the system contains impulse sources,
this director is not applicable. Please use CTMultiSolverDirector with
ImpulseBESolver as the breakpoint solver for better result.
<P>
Each iteration of the director simulates the system for one step.
It recruit ODE solver to solve the tentative new state, and then
control the step size according to error control and/or unpredicatable
breakpoint.
The size of the step is determined by the ODE solver as well as
the breakpoints. After each iteration, the execution control will be
returned to the manager, where possible mutations are taken care of.
At the end of the simulation, the postfire() method will return false,
telling the manager that the simulation finished.

@author Jie Liu
@version $Id$
*/
public abstract class CTSingleSolverDirector extends CTDirector {


    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     */
    public CTSingleSolverDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace The workspace of this object.
     */
    public CTSingleSolverDirector(Workspace workspace)  {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in derived classes.
     */
    public CTSingleSolverDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Parameter of the ODE solver.
     */
    public Parameter ODESolver;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  @param param The changed parameter.
     *  @exception IllegalActionException If the default solver that is
     *   specified is not valid.
     */
    public void attributeChanged(Attribute param)
            throws IllegalActionException {
        if(param == ODESolver) {
            if(_debugging) _debug(getFullName() + " solver updating...");
            _solverclassname =
                ((StringToken)((Parameter)param).getToken()).toString();
            _defaultSolver = _instantiateODESolver(_solverclassname);
            _setCurrentODESolver(_defaultSolver);
        } else {
            super.attributeChanged(param);
        }
    }

    /** Return true since this director can be top-level director.
     *  @return true always.
     */
    public boolean canBeTopLevelDirector() {
        return true;
    }

    /**  Fire the system for one iteration. One iteration is defined as
     *   simulating the system at one time point, which includes
     *   resolving states, and producing outputs.
     *   The step size of one iteration is determined by the suggested
     *   next step size and the breakpoints. If the first breakpoint in
     *   the breakpoint table is in the middle of the "intended" step.
     *   Then the current step size is reduced to breakpoint - current
     *   time. The result of such a step is the left limit of the states
     *   at the breakpoint.
     *   <P>
     *   All the actors are prefired before an iteration is begun. If
     *   any one of them returns false, then the iteration is
     *   cancelled, and the function returns.
     *
     *  @exception IllegalActionException If thrown by the ODE solver.
     */
    public void fire() throws IllegalActionException {
        //event phase:
        _eventPhaseExecution();
        _setIterationBeginTime(getCurrentTime());
        //Refine step size
        setCurrentStepSize(getSuggestedNextStepSize());
        _processBreakpoints();
        if(_debugging) _debug("execute the system from "+
                getCurrentTime() +" step size" + getCurrentStepSize()
                + " using solver " + getCurrentODESolver().getName());
        _fireOneIteration();
    }

    /** Return the ODE solver.
     *  @return The default ODE solver
     */
    public ODESolver getODESolver() {
        return _defaultSolver;
    }

    /** Initialization after type resolution. This
     *  is called exactly once at the start of the entire execution.
     *  It sets the step size and the suggested next step size
     *  to the initial step size. The ODE solver is instantiated.
     *  And the stop time is registered as a breakpoint.
     *  It invoke the initialize() method for all the Actors in the
     *  container.
     *  This method checks if it is the top level director, if not,
     *  it will throw an exception.
     *
     *  @exception IllegalActionException If it is not the top level
     *      director.
     */
    public void initialize() throws IllegalActionException {
        if(_debugging) _debug(getFullName(), "initializing:");
        // Instantiate ODE solver
        if(_debugging) _debug(getFullName(), " instantiating ODE solver ",
                _solverclassname);
        _defaultSolver = _instantiateODESolver(_solverclassname);
        _setCurrentODESolver(_defaultSolver);
        if(_debugging) _debug(getFullName(), "assert the current ODE solver ",
                getCurrentODESolver().getName());
        if(_debugging) _debug(getFullName() + " initialize directed actors: ");
        super.initialize();
        // set step sizes
        setCurrentStepSize(getInitialStepSize());
        if(_debugging) _debug(getFullName(), " set current step size to "
                + getCurrentStepSize());
        setSuggestedNextStepSize(getInitialStepSize());
        if(_debugging)
            _debug(getFullName(), " set suggested next step size to "
                    + getSuggestedNextStepSize());
        if(_debugging)
            _debug(getFullName(), " set the current time as a break point: " +
                    getCurrentTime());
        fireAt(null, getCurrentTime());
        if(_debugging)
            _debug(getFullName(), " set the stop time as a break point: " +
                    getStopTime());
        fireAt(null, getStopTime());
        //_first = true;
        
        if(_debugging) _debug(getFullName() + " End of Initialization.");
    }

    /** Return false if the simulation stop time is reached.
     *  Test if the current time is
     *  the stop time. If so, return false ( for stop further simulation).
     *  Otherwise, returns true.
     *  @return false If the simulation is finished.
     *  @exception IllegalActionException If thrown by registering
     *  breakpoint
     */
    public boolean postfire() throws IllegalActionException {
        if((getCurrentTime()+getSuggestedNextStepSize())>getStopTime()) {
            fireAt(null, getStopTime());
        }
        if(Math.abs(getCurrentTime() - getStopTime()) < getTimeResolution()) {
            return false;
        }
        if(getStopTime() < getCurrentTime()) {
            throw new InvalidStateException(this,
                    " stop time is less than the current time.");
        }
        return true;
    }

    /** Return true always, indicating that the system is always ready
     *  for one iteration. The schedule
     *  is recomputed if there is any mutation. The parameters are
     *  updated, since this is the safe place to change parameters.
     *
     *  @return True Always
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        if(_debugging) _debug(this.getFullName(), "prefire.");
        if(STAT) {
            NSTEP++;
        }
        if(!isScheduleValid()) {
            // mutation occurred, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                        "does not have a Scheduler.");
            }
            scheduler.schedule();
            setScheduleValid(true);
        }
        return true;
    }

    /** produce outputs. Fire all the actors in the output schedule.
     *  @exception IllegalActionException If the actor on the output
     *      schedule throws it.
     */
    public void produceOutput() throws IllegalActionException {
        CTScheduler scheduler = (CTScheduler) getScheduler();
        // Integrators emit output.
        // FIXME: Do we need this? If the last fire of the integrators
        //        has already emitted token, then the output actors
        //        can use them. That is at least true for implicit methods.
        Iterator integrators = scheduler.scheduledDynamicActorList().iterator();
        while(integrators.hasNext()) {
            CTDynamicActor dyn = (CTDynamicActor)integrators.next();
            if(_debugging) _debug("Excite State..."+
                    ((Nameable)dyn).getName());
            dyn.emitTentativeOutputs();
        }
        Iterator outputactors = scheduler.scheduledOutputActorList().iterator();
        while(outputactors.hasNext()) {
            Actor nextoutputactor = (Actor)outputactors.next();
            if(_debugging) _debug("Fire output..."+
                    ((Nameable)nextoutputactor).getName());
            nextoutputactor.fire();
        }
    }

    /** Call postfire() on all actors. For a correct CT simulation,
     *  the state of an actor can only change at this stage of an
     *  iteration.
     *  @exception IllegalActionException If any of the actors
     *      throws it.
     */
    public void updateStates() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        Iterator allactors = container.deepEntityList().iterator();
        while(allactors.hasNext()) {
            Actor nextactor = (Actor)allactors.next();
            nextactor.postfire();
        }
    }

    /** Show the statistics of the simulation if needed. The statistics
     *  includes the number of step simulated, the number of function
     *  evaluations (firing all actors in the state transition schedule),
     *  and the number of failed steps (due to error control).
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException{
        if(STAT) {
            if(_debugging) {
                _debug("################STATISTICS################");
                _debug(getName() + ": Total # of STEPS "+NSTEP);
                _debug(getName() + ": Total # of Function Evaluation "+NFUNC);
                _debug(getName() + ": Total # of Failed Steps "+NFAIL);
            }
        }
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Process discrete events in the system. All the event generators
     *  will produce events, and event interpreters will consume events.
     */
    protected void _eventPhaseExecution() throws IllegalActionException {
        CTScheduler sched = (CTScheduler)getScheduler();
        Iterator evgens = sched.eventGeneratorList().iterator();
        while(evgens.hasNext()) {
            CTEventGenerator evg = (CTEventGenerator) evgens.next();
            evg.emitCurrentEvents();
        }
        // fire all the discrete actors?
        Iterator wfgens = sched.waveformGeneratorList().iterator();
        while(wfgens.hasNext()) {
            CTWaveformGenerator wfg = (CTWaveformGenerator) wfgens.next();
            wfg.consumeCurrentEvents();
        }
    }

    /** Fire one iteration. Return directly if any actors return false
     *  in their prefire() method. The the time is advanced by the
     *  current step size.
     */
    protected void _fireOneIteration() throws IllegalActionException {
        if(_debugging) _debug(getFullName(),
                "Fire one iteration from " + getCurrentTime(),
                "Using step size" + getCurrentStepSize());
        ODESolver solver = getCurrentODESolver();
        if(_debugging) _debug( "Using ODE solver", solver.getName());
        while (true) {
            while (_prefireSystem()) {
                if (solver.resolveStates()) {
                    if(_debugging) _debug("state resolved.");
                    // ask if this step is acceptable
                    if (!_isStateAcceptable()) {
                        setCurrentTime(getIterationBeginTime());
                        setCurrentStepSize(_refinedStepWRTState());
                        if(_debugging) _debug("execute the system from "+
                                getCurrentTime() +" step size" +
                                getCurrentStepSize());
                        if(STAT) {
                            NFAIL++;
                        }
                    } else {
                        break;
                    }
                } else { // resolve state failed, e.g. in implicit methods.
                    if(getCurrentStepSize() < 0.5*getMinStepSize()) {
                        throw new IllegalActionException(this,
                                "Cannot resolve new states even using "+
                                "the minimum step size, at time "+
                                getCurrentTime());
                    }
                    setCurrentTime(getIterationBeginTime());
                    setCurrentStepSize(0.5*getCurrentStepSize());
                }
            }
            produceOutput();
            if (!_isOutputAcceptable()) {
                //if(_debugging) _debug("Output not satisfied.");
                setCurrentTime(getIterationBeginTime());
                setCurrentStepSize(_refinedStepWRTOutput());
                if(STAT) {
                    NFAIL++;
                }
            }else {
                break;
            }
        }
        setSuggestedNextStepSize(_predictNextStepSize());
        updateStates(); // call postfire on all actors
    }

    /** Initialize parameters to their default values. */
    protected void _initParameters() {
        super._initParameters();
        try {
            _solverclassname =
                "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver";
            ODESolver = new Parameter(
                    this, "ODESolver", new StringToken(_solverclassname));
            ODESolver.setTypeEquals(BaseType.STRING);
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    /** Return true if the newly resolved state is acceptable.
     *  It does it by asking all the step control actors in the
     *  state transition and dynamic schedule.
     *  If one of them returns false, then the method returns
     *  false.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected boolean _isOutputAcceptable() throws IllegalActionException {
        boolean successful = true;
        CTScheduler sched = (CTScheduler)getScheduler();
        Iterator sscs = sched.outputSSCActorList().iterator();
        while (sscs.hasNext()) {
            CTStepSizeControlActor a =
                (CTStepSizeControlActor) sscs.next();
            if(_debugging) _debug("Checking Output Step Size Control Actor: "
                    + ((NamedObj)a).getName());
            successful = successful && a.isThisStepSuccessful();
        }
        return successful;
    }

    /** Return true if the newly resolved state is acceptable.
     *  It does it by asking all the step control actors in the
     *  state transition and dynamic schedule.
     *  If one of them returns false, then the method returns
     *  false.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected boolean _isStateAcceptable() throws IllegalActionException {
        boolean successful = true;
        CTScheduler sched = (CTScheduler)getScheduler();
        Iterator sscs = sched.stateTransitionSSCActorList().iterator();
        while (sscs.hasNext()) {
            CTStepSizeControlActor a =
                (CTStepSizeControlActor) sscs.next();
            if(_debugging) _debug("Checking State Step Size Control Actor: " +
                    ((NamedObj)a).getName());
            successful = successful && a.isThisStepSuccessful();
        }
        return successful;
    }

    /** Return true if the prefire() methods of all the actors in the system
     *  return true.
     *  @return True if the prefire() methods of all actors returns true.
     */
    protected boolean _prefireSystem() throws IllegalActionException {
        boolean ready = true;
        CompositeActor ca = (CompositeActor) getContainer();
        Iterator actors = ca.deepEntityList().iterator();
        while(actors.hasNext()) {
            Actor a = (Actor) actors.next();
            ready = ready && a.prefire();
            if(_debugging) _debug("Prefire "+((Nameable)a).getName() +
                    " returns" + ready);
        }
        return ready;
    }

    /** Clean old breakpoints in the breakpoint table, and adjust
     *  the the current step size according to the first breakpoint.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _processBreakpoints() throws IllegalActionException {
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        Double tnow = new Double(getCurrentTime());
        _setIsBPIteration(false);
        // If now is a break point, remove the break point from table;
        if(breakPoints != null && !breakPoints.isEmpty()) {
            breakPoints.removeAllLessThan(tnow);
            if(breakPoints.contains(tnow)) {
                // now is the break point.
                breakPoints.removeFirst();
                _setIsBPIteration(true);
            }else {
                // adjust step size according to the first break point.
                bp = ((Double)breakPoints.first()).doubleValue();
                double iterEndTime = getCurrentTime() + getCurrentStepSize();
                if (iterEndTime > bp) {
                    setCurrentStepSize(bp-getCurrentTime());
                }
            }
        }
    }

    /** Predict the next step size. This method should be called if the
     *  current integration step is acceptable. The predicted step size
     *  is the minimum of all predictions from step size control actors.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _predictNextStepSize() throws IllegalActionException {
        double predictedstep = getMaxStepSize();
        CTScheduler sched = (CTScheduler)getScheduler();
        Iterator sscs = sched.stateTransitionSSCActorList().iterator();
        while (sscs.hasNext()) {
            CTStepSizeControlActor a =
                (CTStepSizeControlActor) sscs.next();
            double pre = a.predictedStepSize();
            if(_debugging)
                _debug(((NamedObj)a).getName(), "predict step " + pre);
            predictedstep = Math.min(predictedstep, pre);
        }
        sscs = sched.outputSSCActorList().iterator();
        while (sscs.hasNext()) {
            CTStepSizeControlActor a =
                (CTStepSizeControlActor) sscs.next();
            double pre = a.predictedStepSize();
            if(_debugging)
                _debug(((NamedObj)a).getName(), "predict step " + pre);
            predictedstep = Math.min(predictedstep, pre);
        }
        return predictedstep;
    }

    /** Return the refined the step size with respected to the new state.
     *  It asks all the step size control actors in the state transition
     *  and dynamic schedule for the refined step size, and take the
     *  minimum of them.
     *  @return the refined step size.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _refinedStepWRTState() throws IllegalActionException {
        if(_debugging) _debug(getFullName() + "refine step wrt state.");
        double refinedstep = getCurrentStepSize();
        CTScheduler sched = (CTScheduler)getScheduler();
        Iterator sscs = sched.stateTransitionSSCActorList().iterator();
        while (sscs.hasNext()) {
            CTStepSizeControlActor a =
                (CTStepSizeControlActor) sscs.next();
            if(_debugging) _debug(((Nameable)a).getName() + "refine..."
                    + a.refinedStepSize());
            refinedstep = Math.min(refinedstep, a.refinedStepSize());
        }
        return refinedstep;
    }

    /** Return the refined the step size with respected to the outputs.
     *  It asks all the step size control actors in the state transition
     *  and dynamic schedule for the refined step size, and take the
     *  minimum of them.
     *  @return the refined step size.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _refinedStepWRTOutput() throws IllegalActionException {
        double refinedstep = getCurrentStepSize();
        CTScheduler sched = (CTScheduler)getScheduler();
        Iterator sscs = sched.outputSSCActorList().iterator();
        while (sscs.hasNext()) {
            CTStepSizeControlActor a =
                (CTStepSizeControlActor) sscs.next();
            refinedstep = Math.min(refinedstep, a.refinedStepSize());
        }
        return refinedstep;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The classname of the ODE solver.
    private String _solverclassname;

    // The default solver.
    private ODESolver _defaultSolver = null;
}
