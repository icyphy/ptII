/* A CT director that utilizes multiple ODE solvers

 Copyright (c) 1998 The Regents of the University of California.
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
@ProposedRating Red (liuj@eecs.berkeley.edu)

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
//// CTMultiSolverDirector
/**
A director that uses multiple ODE solvers. The reason of switching
solvers is that when abrupt changes in the signal occurs (also called
breakpoints), the history information is useless for further calculation.
For ODE solvers that depends on history points, it is essential to switch
to use low order implecit method
with minimum step size to rebuild the history points. For input signals
that contains Dirac impulses, it is also essential to switch to the 
impulse backward Euler solver to deal with them.
<P>
This class has two additional parameters than the CTDirector base class,
which are "defaultODESolver" and "breakpointODESolver". The values of the 
parameters are Strings that specifies the full class name of ODE solvers.
The default "defaultODESolver" is ExplicitRK23Solver. The default
"breakpointODESolver" is the BackwardEulerSolver.
All other parameters are maintained by the CTDirector base class. And the
two solvers share them.

@author  Jie Liu
@version $Id$
@see ptolemy.domains.ct.kernel.CTDirector
*/
public class CTMultiSolverDirector extends CTDirector {
    /** Construct a CTMultiSolverDirector with no name and no container.
     *  All parameters take their default values.
     */
    public CTMultiSolverDirector () {
        super();
        _initParameters();
    }

    /** Construct a CTMultiSolverDirector in the default workspace with
     *  the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All parameters take their default values.
     *
     *  @param name The name of this director.
     */
    public CTMultiSolverDirector (String name) {
        super(name);
        _initParameters();
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All parameters take their default values.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public CTMultiSolverDirector (Workspace workspace, String name) {
        super(workspace, name);
        _initParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
     *   at the breakpoint. If the start time of the integration step
     *   equals to the first breakpoint in the breakpoint table, i.e.
     *   this is the first step after a breakpoint, then the ODE solver
     *   is changed to breakpointODESolver and the step size is set
     *   to the minimum step size. The breakpoint is then removed
     *   from the breakpoint table.
     *   <P>
     *   All the actors are prefired before an iteration is begun. If 
     *   any one of them returns false, then the iteratin is not 
     *   proceeded, and the function returns.
     *
     *  @exception IllegalActionException If thrown by the ODE solver.
     *//**  Fire the system for one iteration.
     *
     *  @exception IllegalActionException If thrown by the ODE solver.
     */
    public void fire() throws IllegalActionException {
        if (_first) {
            _first = false;
            produceOutput();
            return;
        }
        updateStates(); // call postfire on all actors

        //Refine step size ans set ODE Solvers.
        setCurrentODESolver(_defaultSolver);
        setCurrentStepSize(getSuggestedNextStepSize());
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        //chhose ODE solver
        // If now is a break point, remove the break point from table;
        if((breakPoints != null) && !breakPoints.isEmpty()) {
            bp = ((Double)breakPoints.first()).doubleValue();
            if(Math.abs(bp-getCurrentTime()) < getTimeResolution()) {
                // break point now!
                breakPoints.removeFirst();
                setCurrentODESolver(_breakpointSolver);
                setCurrentStepSize(getMinStepSize());
            }
            //adjust step size;
            if(!breakPoints.isEmpty()) {
                bp = ((Double)breakPoints.first()).doubleValue();
                double iterEndTime = getCurrentTime()+getCurrentStepSize();
                if (iterEndTime > bp) {
                    setCurrentStepSize(bp-getCurrentTime());
                }
            }
        }
        // prefire all the actors.
        boolean ready = true;
        CompositeActor ca = (CompositeActor) getContainer();
        Enumeration actors = ca.deepGetEntities();
        while(actors.hasMoreElements()) {
            Actor a = (Actor) actors.nextElement();
            ready = ready && a.prefire();
        }

        if(ready) {
            ODESolver solver = getCurrentODESolver();
            solver.proceedOneStep();
            produceOutput();
        }
    }

    /** Return the default ODE solver.
     *  @return The default ODE solver
     */
    public ODESolver getDefaultSolver() {
        return _defaultSolver;
    }

    /** Return the breakpoint ODE solver.
     *  @return The breakpoint ODE solver.
     */
    public ODESolver getBreakpointSolver() {
        return _breakpointSolver;
    }

    /** This does the initialization for the entire subsystem. This
     *  is called exactly once at the start of the entire execution.
     *  It checks if it contianer and its scheduler is correct. 
     *  Otherwise throw an exception. It then calls _intialize()
     *  method to initialize parameters and times.
     *
     *  @exception IllegalActionException If there's no scheduler or
     *       thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        if (VERBOSE||DEBUG) {
            System.out.println("MultiSolverDirector initialize.");
        }
        CompositeActor ca = (CompositeActor) getContainer();
        if (ca == null) {
            if(DEBUG) {
                System.out.println("Director has no container.");
            }
            throw new IllegalActionException(this, "Has no container.");
        }
        if (ca.getContainer() != null) {
            if(DEBUG) {
                System.out.println("Director can only be the top director.");
            }
            throw new IllegalActionException(this,
            " can only serve as the top level director.");
        }
        CTScheduler sch = (CTScheduler)getScheduler();
        if (sch == null) {
            if(DEBUG) {
                System.out.println("Director does not have a scheduler.");
            }
            throw new IllegalActionException( this,
            "does not have a scheduler.");
        }
        _initialize();
    }

    /** Return false if the current time reaches the step time. 
     *  @return false If the simulation stop time expires.
     *  @exception IllegalActionException If there is no ODE solver, or
     *        thrown by the solver.
     */
    public boolean postfire() throws IllegalActionException {
        if((getCurrentTime()+getSuggestedNextStepSize())>getStopTime()) {
            fireAt(null, getStopTime());
        }
        if(Math.abs(getCurrentTime() - getStopTime()) < getTimeResolution()) {
            updateStates(); // call postfire on all actors.
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
     *  @exception IllegalActionException Never thrown in this method.
     */
    public boolean prefire() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println("Director prefire.");
        }
        if(STAT) {
            NSTEP++;
        }
        if(!scheduleValid()) {
            // mutation occured, redo the schedule;
            CTScheduler scheduler = (CTScheduler)getScheduler();
            if (scheduler == null) {
                throw new IllegalActionException (this,
                "does not have a Scheuler.");
            }
            scheduler.schedule();
            setScheduleValid(true);
        }
        updateParameters();
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
            CTDynamicActor integrator=(CTDynamicActor)integrators.nextElement();
            if(VERBOSE) {
                System.out.println("Excite State..."+
                    ((Nameable)integrator).getName());
            }
            integrator.emitPotentialStates();
        }
        // outputSchdule.fire()
        Enumeration outputactors = scheduler.outputSchedule();
        while(outputactors.hasMoreElements()) {
            Actor nextoutputactor = (Actor)outputactors.nextElement();
            if(VERBOSE) {
                System.out.println("Fire output..."+
                    ((Nameable)nextoutputactor).getName());
            }
            nextoutputactor.fire();
        }
    }

    /** Update given paramter. If the parameter does not exist, 
     *  throws an exception.
     *  @param param The parameter.
     *  @exception IllegalActionException If the parameter does not exist.
     */
    public void updateParameter(Parameter param)
            throws IllegalActionException {
        if(param == _paramDefaultODESolver) {
            if(VERBOSE) {
                System.out.println("default solver updating.");
            }
            _defaultsolverclass =
            ((StringToken)param.getToken()).stringValue();
            _defaultSolver = _instantiateODESolver(_defaultsolverclass);
        } else if (param == _paramBreakpointODESolver) {
            if(VERBOSE) {
                System.out.println("breakpoint solver updating.");
            }
            _breakpointsolverclass =
            ((StringToken)param.getToken()).stringValue();
            _breakpointSolver =
            _instantiateODESolver(_breakpointsolverclass);
        } else {
            super.updateParameter(param);
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
            if(DEBUG) {
                System.out.println("Postfire:"+((NamedObj)nextactor).getName());
            }
            nextactor.postfire();
        }
    }

    /** Wrapup the simulation. Show the statistics if needed. The statistics
     *  includes the number of step simulated, the number of funciton
     *  evaluations (firing all actors in the state transition schedule),
     *  and the number of failed steps (due to error control).
     *  
     *  @exception IllegalActionException Never thrown.
     *  
     */
    public void wrapup() throws IllegalActionException{
        if(STAT) {
            System.out.println("**************STATISTICS***************");
            System.out.println("Total # of STEPS "+NSTEP);
            System.out.println("Total # of Function Evaluation "+NFUNC);
            System.out.println("Total # of Failed Steps "+NFAIL);
        }
        super.wrapup();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Set the current time to the start time and the suggested
     *  next step
     *  size to the initial step size. The start time is
     *  set as a breakpoint in the breakpoint table.
     *  It invoke the initialize() method for all the Actors in the
     *  system. The ODE solver are instanciated, and the current solver
     *  is set to be the breakpoint solver. The breakpoint table is cleared,
     *  and the start time is set to be the first breakpoint. 
     *  Invalidate the schedule.
     *  This method does not 
     *  check the container and the scheduler, so the 
     *  caller should check.
     *  @exception IllegalActionException If thrown by director actors.
     */
    protected void _initialize() throws IllegalActionException{
        if(STAT) {
            NSTEP=0;
            NFUNC=0;
            NFAIL=0;
        }
        if(VERBOSE) {
            System.out.println("updating parameters");
        }
        updateParameters();
        // Instanciate ODE solvers
        if(VERBOSE) {
            System.out.println("instantiating Defalut ODE solver "+
            _defaultsolverclass);
        }
        if(_defaultSolver == null) {
            _defaultSolver = _instantiateODESolver(_defaultsolverclass);
        }
        if(VERBOSE) {
            System.out.println("instantiating Breakpoint ODE solver "+
            _breakpointsolverclass);
        }
        if(_breakpointSolver == null) {
            _breakpointSolver = _instantiateODESolver(_breakpointsolverclass);
        }
        setCurrentODESolver(_breakpointSolver);
        // set time
        setCurrentTime(getStartTime());
        setSuggestedNextStepSize(getInitialStepSize());
        TotallyOrderedSet bps = getBreakPoints();
        if(bps != null) {
            bps.clear();
        }
        fireAt(null, getCurrentTime());
        setScheduleValid(false);
        _first = true;
        if (VERBOSE) {
            System.out.println("Director.super initialize.");
        }
        super.initialize();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private methods                      ////
    private void _initParameters() {
        try {
            _defaultsolverclass=
                "ptolemy.domains.ct.kernel.solver.ExplicitRK23Solver";
            _paramDefaultODESolver = new CTParameter(
                this, "DefaultODESolver", new StringToken(_defaultsolverclass));
            _breakpointsolverclass=
                "ptolemy.domains.ct.kernel.solver.BackwardEulerSolver";
            _paramBreakpointODESolver = new CTParameter(
                this, "BreakpointODESolver",
                new StringToken(_breakpointsolverclass));
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,"Parameter name duplication.");
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // parameter of default ODE solver
    private CTParameter _paramDefaultODESolver;
    // The classname of the default ODE solver
    private String _defaultsolverclass;
    // The default solver.
    private ODESolver _defaultSolver = null;

    // parameter of breakpoint ODE solver
    private CTParameter _paramBreakpointODESolver;
    // The classname of the default ODE solver
    private String _breakpointsolverclass;
    // The default solver.
    private ODESolver _breakpointSolver = null;
    //indicate the first round of execution.

    private boolean _first;
}
