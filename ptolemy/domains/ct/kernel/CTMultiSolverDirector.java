/* A CT director that utilizes multiple ODE solvers.

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
//// CTMultiSolverDirector
/**
A CTDirector that uses multiple ODE solvers. The reason for switching
solvers is that when abrupt changes in the signal occurs (also called
breakpoints), the initial state and its derivative have to be recalculated.
At these points, a special ODE solver, called the "breakpointODESolver"
is used with the minimum step size. The simulation is executed as
if the breakpoint is a new starting point.
<P>
This director handles both predictable breakpoints, which are breakpoints
that are registered in the breakpoint table, and unpredictable breakpoints,
which are breakpoints that are not know before hand.
<P>
This director can only be a top-level director. For a CT domain inside
an opaque composite actor, use CTMixedSignalDirector (if the outter
domain is discrete) or CTEmbeddedDirector (if the outter domain is
a CT domain or a HS domain.)
<P>
This director has two more parameters than the CTDirector base
class.<BR>
<UL>
<LI><I>ODESolver</I>: This is the name of the normal ODE solver
used in nonbreakpoint iterations. The default is a String
"ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver"
<LI><I>breakpointODESolver</I>: This is the name of the ODE solver that
is used in the iterations just after the breakpoint. The breakpoint
ODE solvers should not require history information (this property
is called self-start). The default is
"ptolemy.domains.ct.kernel.solver.DerivaticeResolver"
If there are Dirac impulses in the system, the
"ptolemy.domains.ct.kernel.solver.ImpulseBESolver" may give
a better result.
<LI>All other parameters are maintained by the CTDirector base class. And the
two solvers share them.

@author  Jie Liu
@version $Id$
@see ptolemy.domains.ct.kernel.CTDirector
*/
public class CTMultiSolverDirector extends CTDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters takes their default values.
     */
    public CTMultiSolverDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters takes their default values.
     *  @param workspace The workspace of this object.
     */
    public CTMultiSolverDirector(Workspace workspace) {
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
    public CTMultiSolverDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Parameter of the ODE solver.
     */
    public Parameter ODESolver;

    /** parameter of breakpoint ODE solver.
     */
    public Parameter breakpointODESolver;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  @param param The changed parameter.
     *  @exception IllegalActionException If the new solver that is specified
     *   is not valid.
     */
    public void attributeChanged(Attribute attr)
            throws IllegalActionException {
        Parameter param = (Parameter)attr;
        if(param == ODESolver) {
            if(_debugging) _debug(getFullName() + " updating  ODE solver...");
            _solverclassname =
                ((StringToken)((Parameter)param).getToken()).stringValue();
            _defaultSolver = _instantiateODESolver(_solverclassname);
            _setCurrentODESolver(_defaultSolver);
        } else if (param == breakpointODESolver) {
            if(_debugging) _debug(getName() +" updating breakpoint solver...");
            _bpsolverclassname =
                ((StringToken)param.getToken()).stringValue();
            _breakpointsolver =
                _instantiateODESolver(_bpsolverclassname);
        } else {
            super.attributeChanged(param);
        }
    }

    /** Return false always, since this director cannot be an inside director.
     *  @return false.
     */
    public boolean canBeInsideDirector() {
        return false;
    }

    /** Return true since this director can be top-level director.
     *  @return true.
     */
    public boolean canBeTopLevelDirector() {
        return true;
    }

    /** Clone the director into the specified workspace. This calls the
     *  super class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        CTMultiSolverDirector newobj =
            (CTMultiSolverDirector)(super.clone(ws));
        newobj.ODESolver =
            (Parameter)newobj.getAttribute("ODESolver");
        newobj.breakpointODESolver =
            (Parameter)newobj.getAttribute("breakpointODESolver");
        return newobj;
    }

    /**  Fire the system for one iteration. One iteration is defined as
     *   simulating the system at one time point, which includes
     *   processing discrete events, resolving states,
     *   and producing outputs.
     *   <P>
     *   An iteration begins with processing events, which includes
     *   that all waveform generators consume current input events
     *   and all event generators produce current output events.
     *   Then the new values of the state variables are resolved.
     *   If the state is resolved successfully, the outputs are produced.
     *   <P>
     *   The step size of one iteration is determined by the suggested
     *   next step size and the breakpoints. If the first breakpoint in
     *   the breakpoint table is in the middle of the "intended" step.
     *   Then the current step size is reduced to the breakpoint - current
     *   time. The result of such a step is the left limit of the states
     *   at the breakpoint.
     *   <PP
     *   The new state is resolved by the resolveStates() method of the
     *   current ODE solver. After that, the step size control actors
     *   in the dynamic actor schedule and the state transition schedule
     *   are checked for the success
     *   of the current integration. If any one of the step size control
     *   actors do not think it is succeeded, then the integration step
     *   will be restarted with a refined step size, which is the minimum
     *   of the refinedStepSize() from all step size control actors in
     *   the dynamic actor schedule and the state transition schedule.
     *   If all the actors in the dynamic actor and the state transition
     *   schedules think the current step is succeeded, then the actors
     *   in the output path will be fired according to the output
     *   schedule. Then the step size control actors in the output
     *   path will be asked for success. The above procedure is
     *   followed again to refine the step size and restart the iteration.
     *   The iteration is complete until all actors think the integration
     *   is successful.
     *   <P>
     *   All the actors are prefired before an iteration is begun. If
     *   any one of them returns false, then the iteration is
     *   cancelled, and the function returns.
     *
     *  @exception IllegalActionException If thrown by the ODE solver.
     */
    public void fire() throws IllegalActionException {
        // event phase;
        _eventPhaseExecution();
        // continuous phase;
        _setIterationBeginTime(getCurrentTime());
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

    /** Return the breakpoint ODE solver.
     *  @return The breakpoint ODE solver.
     */
    public ODESolver getBreakpointSolver() {
        return _breakpointsolver;
    }

    /** Initialization after type resolution.
     *  It sets the step size and the suggested next step size
     *  to the initial step size. The ODE solver  and the
     *  breakpoint ODE solver are instantiated.
     *  Set the current time to be the start time of the simulation.
     *  Both the current time and the stop time are registered
     *  as a breakpoint.
     *  It invoke the initialize() method for all the Actors in the
     *  container.
     *
     *  @exception IllegalActionException If the instantiation of the solvers
     *  are not succeded or one of the directed actors throw it.
     */
    public void initialize() throws IllegalActionException {
        if(_debugging) _debug(getFullName(), "initializing:");
        // synchronized on time and initialize all actors
        if(_debugging) _debug(getFullName() + " initialize directed actors: ");
        super.initialize();
        // set step sizes
        setCurrentStepSize(getInitialStepSize());
        if(_debugging) _debug(getFullName(), " set current step size to "
                + getCurrentStepSize());
        setSuggestedNextStepSize(getInitialStepSize());
        if(_debugging) {
            _debug(getFullName(), " set suggested next step size to "
                    + getSuggestedNextStepSize());
        }
        if(_debugging)
            _debug(getFullName(), " set the current time as a break point: " +
                    getCurrentTime());
        fireAt(null, getCurrentTime());
        if(_debugging)
            _debug(getFullName(), " set the stop time as a break point: " +
                    getStopTime());
        fireAt(null, getStopTime());

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
     *  is recomputed if there is any mutation.
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

    /** In additional to initialize all the actors,
     *  instantiate all the solvers.
     *  @exception IllegalActionException If thrown by one
     *  of the actors.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        // Instantiate ODE solvers
        if(_debugging) _debug(getFullName(), " instantiating the ODE solver ",
                _solverclassname);
        _defaultSolver = _instantiateODESolver(_solverclassname);
        _setCurrentODESolver(_defaultSolver);
        if(_debugging)
            _debug(getFullName(), "The current ODE solver is",
                    getCurrentODESolver().getName());

        if(_debugging) _debug(getFullName(), "instantiating the " +
                " breakpoint solver ", _bpsolverclassname);
        _breakpointsolver =
            _instantiateODESolver(_bpsolverclassname);
    }

    /** produce outputs. Fire all the actors in the output schedule.
     *  @exception IllegalActionException If the actor on the output
     *      schedule throws it.
     */
    public void produceOutput() throws IllegalActionException {
        CTScheduler scheduler = (CTScheduler) getScheduler();
        // Integrators emit output.
        Iterator integrators =
            scheduler.scheduledDynamicActorList().iterator();
        while(integrators.hasNext()) {
            CTDynamicActor dyn = (CTDynamicActor)integrators.next();
            if(_debugging) _debug("Emit tentative state: "+
                    ((Nameable)dyn).getName());
            dyn.emitTentativeOutputs();
        }
        Iterator outputactors =
            scheduler.scheduledOutputActorList().iterator();
        while(outputactors.hasNext()) {
            Actor nextoutputactor = (Actor)outputactors.next();
            if(_debugging) _debug("Fire output actor: "+
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

    /** Show the statistics of the simulation if requested. The statistics
     *  includes the number of steps simulated, the number of function
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


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Process discrete events in the system. All the event generators
     *  will produce current events, and event interpreters will
     *  consume current events.
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

    /** Fire one iteration. Return immediately if any actor returns false
     *  in their prefire() method. The time is advanced by the
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

    /** Initialize parameters to their default values.
     */
    protected void _initParameters() {
        super._initParameters();
        try {
            _solverclassname =
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver";
            ODESolver = new Parameter(
                    this, "ODESolver", new StringToken(_solverclassname));
            ODESolver.setTypeEquals(BaseType.STRING);
            _bpsolverclassname =
                "ptolemy.domains.ct.kernel.solver.DerivativeResolver";
            breakpointODESolver = new Parameter(
                    this, "breakpointODESolver",
                    new StringToken(_bpsolverclassname));
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
     *  schedule are satified with the current step.
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

    /** Return true if all step size control actors in the dynmic actor
     *  schedule and the state transition schedule are satisfied with
     *  the current step.
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
     *  @return The logical AND of the prefire() of all actors.
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

    /** Clear obsolete breakpoints, switch to breakpointODESolver if this
     *  is the first fire after a breakpoint, and adjust step sizes
     *  accordingly.
     *  @exception IllegalActionException If breakpoint solver is not
     *     illegal.
     */
    protected void _processBreakpoints() throws IllegalActionException  {
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        Double tnow = new Double(getCurrentTime());
        _setIsBPIteration(false);
        //choose ODE solver
        _setCurrentODESolver(getODESolver());
        // If now is a break point, remove the break point from table;
        if(breakPoints != null && !breakPoints.isEmpty()) {
            breakPoints.removeAllLessThan(tnow);
            if(breakPoints.contains(tnow)) {
                // now is the break point.
                breakPoints.removeFirst();
                _setIsBPIteration(true);
                _setCurrentODESolver(_breakpointsolver);
                setCurrentStepSize(0.0); //getMinStepSize());
                if(_debugging) _debug(getFullName(),
                        "IN BREAKPOINT iteration.");
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
        if(!isBPIteration()) {
            double predictedstep = 10.0*getCurrentStepSize();
            CTScheduler sched = (CTScheduler)getScheduler();
            Iterator sscs = sched.stateTransitionSSCActorList().iterator();
            while (sscs.hasNext()) {
                CTStepSizeControlActor a =
                    (CTStepSizeControlActor) sscs.next();
                predictedstep = Math.min(predictedstep, a.predictedStepSize());
            }
            sscs = sched.outputSSCActorList().iterator();
            while (sscs.hasNext()) {
                CTStepSizeControlActor a =
                    (CTStepSizeControlActor) sscs.next();
                predictedstep = Math.min(predictedstep, a.predictedStepSize());
            }
            return predictedstep;
        } else {
            return getInitialStepSize();
        }
    }

    /** Return the refined step size with respected to resolving the
     *  new state.
     *  It asks all the step size control actors in the state transition
     *  and dynamic actor schedule for the refined step size, and takes the
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

    // The classname of the default ODE solver
    private String _bpsolverclassname;

    // The default solver.
    private ODESolver _breakpointsolver = null;
}
