/* A CTDirector that uses only one ODE solver.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTSingleSolverDirector
/**
A CTDirector that uses only one ODE solver. The solver is a parameter
of the director called "ODESolver". The default solver is the 
ForwardEulerSoler.
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
The size of the step is determined by the ODE solver as well as 
the breakpoints. After each iteration, the execution control will be
returned to the manager, where possible mutations are taken care of.
At the end of the simulation, the postfire() method will return false,
telling the manager that the simulation finished.
 

@author Jie Liu
@version $Id$
*/
public class CTSingleSolverDirector extends CTDirector {


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
     *  @exception It may be thrown in derived classes if the
     *      director is not compatible with the specified container.
     */
    public CTSingleSolverDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed atrribute matches
     *  a parameter of the director, then the coresponding private copy of the
     *  parameter value will be updated.
     *  @param param The changed parameter.
     *  @exception IllegalActionException If the default solver that is
     *   specified is not valid.
     */
    public void attributeChanged(Attribute param)
            throws IllegalActionException {
        if(param == ODESolver) {
            _debug(getFullName() + " solver updating...");
            _solverclass = 
                ((StringToken)((Parameter)param).getToken()).stringValue();
            _defaultSolver = _instantiateODESolver(_solverclass);
            setCurrentODESolver(_defaultSolver);
        } else {
            super.attributeChanged(param);
        }
    }

    /**  Fire the system for one iteration. One iteration is defined as
     *   simulating the system at one time point, which includes
     *   resolving states and producing outputs. For the first iteration
     *   it only produces the output, since the initial states are
     *   the "real" states of the system, and no more resolving is needed.
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
        // If this is the first fire, the states are not resolved.
        if (_first) {
            _first = false;
            _prefireSystem();
            produceOutput();
            updateStates();
            return;
        }
        //event phase:
        _eventPhaseExecution();
        _setFireBeginTime(getCurrentTime());
        //Refine step size
        setCurrentStepSize(getSuggestedNextStepSize());
        _processBreakpoints();
        _debug("execute the system from "+
                    getCurrentTime() +" step size" + getCurrentStepSize());
        _fireOneIteration();
    }
    
    /** Return the ODE solver.
     *  @return The default ODE solver
     */
    public ODESolver getODESolver() {
        return _defaultSolver;
    }

    /** Initialization for the entire system. This
     *  is called exactly once at the start of the entire execution.
     *  It set the current time to the start time and the suggested 
     *  next step size to the initial step size.
     *  It invoke the initialize() method for all the Actors in the
     *  system. Parameters are updated, so that the parameters 
     *  set after the creation of the actors are evaluated and ready
     *  for use. The stop time is registered as a breakpoint.
     *  This method checks if there is a composite actor for this 
     *  director to direct, and if there is a proper scheduler for this
     *  director. If not, an exception is throw. 
     *  The ODE solver is instantiated.
     *  
     *  @exception IllegalActionException If there's no scheduler or
     *       thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        _debug(getFullName() + "initializing.");
        CompositeActor ca = (CompositeActor) getContainer();
        if (ca == null) {
            throw new IllegalActionException(this, "Has no container.");
        }
        if (ca.getContainer() != null) {
            throw new IllegalActionException(this,
            " can only serve as the top level director.");
        }
        CTScheduler sch = (CTScheduler)getScheduler();
        if (sch == null) {
            throw new IllegalActionException( this,
            "does not have a scheduler.");
        }
        sch.setValid(false);
        _initialize();
        fireAt(null, getCurrentTime());
        fireAt(null, getStopTime());
    }

    /** Return false if simulation stop time is reached.
     *  Test if the current time is 
     *  the stop time. If so, return false ( for stop further simulation).
     *  Otherwise, returns true.
     *  @return false If the simulation is finished.
     *  @exception IllegalActionException Never thrown
     */
    public boolean postfire() throws IllegalActionException {
        if((getCurrentTime()+getSuggestedNextStepSize())>getStopTime()) {
            fireAt(null, getStopTime());
        }
        if(Math.abs(getCurrentTime() - getStopTime()) < getTimeResolution()) {
            updateStates(); // call postfire on all actors
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
     *  @exception IllegalActionException Never thrown in this director.
     */
    public boolean prefire() throws IllegalActionException {
        _debug(this.getFullName() + "prefire.");
        if(STAT) {
            NSTEP++;
        }
        //(Thread.currentThread()).yield();
        if(!isScheduleValid()) {
            // mutation occurred, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                "does not have a Scheuler.");
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
        Enumeration integrators = scheduler.dynamicActorSchedule();
        while(integrators.hasMoreElements()) {
            CTDynamicActor dyn =(CTDynamicActor)integrators.nextElement();
            _debug("Excite State..."+
                    ((Nameable)dyn).getName());
            dyn.emitTentativeOutputs();
        }
        // outputSchdule.fire()
        Enumeration outputactors = scheduler.outputSchedule();
        while(outputactors.hasMoreElements()) {
            Actor nextoutputactor = (Actor)outputactors.nextElement();
            _debug("Fire output..."+
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
        Enumeration allactors = container.deepGetEntities();
        while(allactors.hasMoreElements()) {
            Actor nextactor = (Actor)allactors.nextElement();
            nextactor.postfire();
        }
    }

    /** Show the statistics of the simulation if needed. The statistics
     *  includes the number of step simulated, the number of function
     *  evaluations (firing all actors in the state transition schedule),
     *  and the number of failed steps (due to error control).
     *  
     *  @exception IllegalActionException Never thrown.
     */
    public void wrapup() throws IllegalActionException{
        if(STAT) {
            System.out.println("################STATISTICS################");
            System.out.println("Total # of STEPS "+NSTEP);
            System.out.println("Total # of Function Evaluation "+NFUNC);
            System.out.println("Total # of Failed Steps "+NFAIL);
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
        Enumeration evgens = sched.eventGenerators();
        while(evgens.hasMoreElements()) {
            CTEventGenerator evg = (CTEventGenerator) evgens.nextElement();
            evg.emitCurrentEvents();
        }
        // fire all the discrete actors?
        Enumeration evints = sched.eventInterpreters();
        while(evints.hasMoreElements()) {
            CTEventInterpreter evg = (CTEventInterpreter) evints.nextElement();
            evg.consumeCurrentEvents();
        }
    }

    /** Return true if the prefire() methods of all the actors in the system
     *  return true.
     *  @return True if the prefire() methods of all actors returns true.
     */
    protected boolean _prefireSystem() throws IllegalActionException {
        boolean ready = true;
        CompositeActor ca = (CompositeActor) getContainer();
        Enumeration actors = ca.deepGetEntities();
        while(actors.hasMoreElements()) {
            Actor a = (Actor) actors.nextElement();
            ready = ready && a.prefire();
            _debug("Prefire "+((Nameable)a).getName() +
                        " returns" + ready);
        }
        return ready;
    }        
    
    /** Clean old breakpoints in the breakpoint table, and adjust 
     *  the the current step size according to it.
     *  @exception IllegalActionException Not thrown in this class,
     *      may be thrown by derived classes.
     */
    protected void _processBreakpoints() throws IllegalActionException {
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        double tnow = getCurrentTime();
        _setIsBPIteration(false);
        // If now is a break point, remove the break point from table;
        if(breakPoints != null) {
            while (!breakPoints.isEmpty()) {
                bp = ((Double)breakPoints.first()).doubleValue();
                if(bp < (tnow-getTimeResolution())) {
                    // break point in the past or at now.
                    breakPoints.removeFirst();
                } else if(Math.abs(bp-tnow) < getTimeResolution()){
                    // break point now!
                    breakPoints.removeFirst();
                    _setIsBPIteration(true);
                    break;
                } else {
                    double iterEndTime = getCurrentTime()+getCurrentStepSize();
                    if (iterEndTime > bp) {
                        setCurrentStepSize(bp-getCurrentTime());
                    }
                    break;
                }
            }
        }
    }

    /** Fire one iteration. Return directly if any actors return false
     *  in their prefire() method. The the time is advanced by the 
     *  current step size.
     */
    protected void _fireOneIteration() throws IllegalActionException {   
        _debug(this.getFullName() +"Fire one iteration from " +
                    getCurrentTime() + " step size" +
                    getCurrentStepSize());
        ODESolver solver = getCurrentODESolver();
        while (true) {
            while (_prefireSystem()) {
                if (solver.resolveStates()) {
                    _debug("state resolved.");
                    // ask if this step is acceptable
                    if (!_isStateAcceptable()) {
                        setCurrentTime(getFireBeginTime());
                        setCurrentStepSize(_refinedStepWRTState());
                        _debug("execute the system from "+
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
                    setCurrentTime(getFireBeginTime());
                    setCurrentStepSize(0.5*getCurrentStepSize());
                }
            }
            produceOutput();
            if (!_isOutputAcceptable()) {
                //_debug("Output not satisfied.");
                setCurrentTime(getFireBeginTime());
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

    /** Real initialize method.
     */
    protected void _initialize() throws IllegalActionException {
        if(STAT) {
            NSTEP=0;
            NFUNC=0;
            NFAIL=0;
        }
        _debug("updating parameters");
        // Instantiate ODE solver
        if(_defaultSolver == null) {
            _debug("instantiating ODE solver "+_solverclass);
            _defaultSolver = _instantiateODESolver(_solverclass);
        }
        // set time
        //_debug(this.getFullName() + 
        //        "_init get State Time " + getStartTime());

        setCurrentTime(getStartTime());
        setSuggestedNextStepSize(getInitialStepSize());
        setCurrentStepSize(getInitialStepSize());
        setCurrentODESolver(_defaultSolver);
        TotallyOrderedSet bps = getBreakPoints();
        if(bps != null) {
            bps.clear();
        }
        _first = true;
        _debug(getFullName() + ".super initialize.");
        super.initialize();
    }

    /** Initialize parameters to their default values. */
    protected void _initParameters() {
        super._initParameters();
        try {
            _solverclass=
                "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver";
            ODESolver = new Parameter(
                this, "ODESolver", new StringToken(_solverclass));
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
    protected boolean _isStateAcceptable() throws IllegalActionException {
        boolean successful = true;
        CTScheduler sched = (CTScheduler)getScheduler();
        Enumeration sscs = sched.stateTransitionSSCActors();
        while (sscs.hasMoreElements()) {
            CTStepSizeControlActor a = 
                (CTStepSizeControlActor) sscs.nextElement();
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
    protected boolean _isOutputAcceptable() throws IllegalActionException {
        boolean successful = true;
        CTScheduler sched = (CTScheduler)getScheduler();
        Enumeration sscs = sched.outputSSCActors();
        while (sscs.hasMoreElements()) {
            CTStepSizeControlActor a = 
                (CTStepSizeControlActor) sscs.nextElement();
            successful = successful && a.isThisStepSuccessful();
        }
        return successful;
    }
    
    /** Predict the next step size. This method should be called if the
     *  current integration step is acceptable. The predicted step size
     *  is the minimum of all predictions from step size control actors.
     *  @exception IllegalActionException If the scheduler throws it.
     */
    protected double _predictNextStepSize() throws IllegalActionException {
        double predictedstep = getMaxStepSize();
        CTScheduler sched = (CTScheduler)getScheduler();
        Enumeration sscs = sched.stateTransitionSSCActors();
        while (sscs.hasMoreElements()) {
            CTStepSizeControlActor a = 
                (CTStepSizeControlActor) sscs.nextElement();
            predictedstep = Math.min(predictedstep, a.predictedStepSize());
        }
        sscs = sched.outputSSCActors();
        while (sscs.hasMoreElements()) {
            CTStepSizeControlActor a = 
                (CTStepSizeControlActor) sscs.nextElement();
            predictedstep = Math.min(predictedstep, a.predictedStepSize());
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
        _debug(getFullName() + "refine step wrt state.");
        double refinedstep = getCurrentStepSize();
        CTScheduler sched = (CTScheduler)getScheduler();
        Enumeration sscs = sched.stateTransitionSSCActors();
        while (sscs.hasMoreElements()) {
            CTStepSizeControlActor a = 
                (CTStepSizeControlActor) sscs.nextElement();
            _debug(((Nameable)a).getName() + "refine..."
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
        Enumeration sscs = sched.outputSSCActors();
        while (sscs.hasMoreElements()) {
            CTStepSizeControlActor a = 
                (CTStepSizeControlActor) sscs.nextElement();
            refinedstep = Math.min(refinedstep, a.refinedStepSize());
        }
        return refinedstep;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // parameter of ODE solver
    public Parameter ODESolver;
    // The classname of the ODE solver
    private String _solverclass;

    // The default solver.
    private ODESolver _defaultSolver = null;

    //indicate the first round of execution.
    private boolean _first;

}
