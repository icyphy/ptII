/* Base class for top level CT directors.

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
@ProposedRating red (liuj@eecs.berkeley.edu)
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
A CT director that does not change its ODE solver.
@author Jie Liu
@version $Id$
*/
public class CTSingleSolverDirector extends CTDirector {


    /** Construct a CTDirector with no name and no Container.
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
     */
    public CTSingleSolverDirector () {
        super();
        this._initParameters();
    }

    /** Construct a CTDirector in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
     *
     *  @param name The name of this director.
     */
    public CTSingleSolverDirector (String name) {
        super(name);
        this._initParameters();
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public CTSingleSolverDirector (Workspace workspace, String name) {
        super(workspace, name);
        this._initParameters();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////


    /** This does the initialization for the entire subsystem. This
     *  is called exactly once at the start of the entire execution.
     *  It set the current time to the start time and the current step
     *  size to the initial step size.
     *  It invoke the initialize() method for all the Actors in the
     *  system. The ODE solver is not checked here, since in some
     *  cases, the ODE solver may be set after the initialize phase.
     *
     *  @exception IllegalActionException If there's no scheduler or
     *       thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        if (VERBOSE||DEBUG) {
            System.out.println("Director initialize.");
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
        if(STAT) {
            NSTEP=0;
            NFUNC=0;
            NFAIL=0;
        }
        if(VERBOSE) {
            System.out.println("updating parameters");
        }
        updateParameters();
        // Instanciate ODE solver
        if(VERBOSE) {
            System.out.println("instantiating ODE solver"+_solverclass);
        }
        if(_defaultSolver == null) {
            _defaultSolver = _instantiateODESolver(_solverclass);
        }
        // set time
        setCurrentTime(getStartTime());
        setSuggestedNextStepSize(getInitialStepSize());
        fireAt(null, getStopTime());
        sch.setValid(false);
        _first = true;
        if (VERBOSE) {
            System.out.println("Director.super initialize.");
        }
        super.initialize();
    }


    /** Perform mutation and process pause/stop request.
     *  If the CTSubSystem is requested a stop (if CTSubSystem.isPaused()
     *  returns true) then pause the thread.
     *  The pause can be wake up by notify(), at that time if the
     *  CTSubSystem is not paused (isPaused() returns false) then
     *  resume the simulation. So the simulation can only be
     *  paused at the prefire stage.
     *  If stop is requested return false, otherwise return true.
     *  Transfer time from the outer domain, if there is any. If this
     *  is the toplevel domain, if the currenttime + currentstepsize
     *  is greater than the stop time, set the currentStepSize to be
     *  stopTime-currentTime.
     *
     *  @return true If stop is not requested
     *  @exception IllegalActionException If the pause is interrupted or it
     *       is thrown by a contained actor.
     *  @exception NameDuplicationException If thrown by a contained actor.
     */
    public boolean prefire() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println("Director prefire.");
        }
        if(DEBUG) {
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

   /**  Fire the system for one iteration.
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
        //Refine step size
        setCurrentStepSize(getSuggestedNextStepSize());
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        // If now is a break point, remove the break point from table;
        if((breakPoints != null) && !breakPoints.isEmpty()) {
            bp = ((Double)breakPoints.first()).doubleValue();
            if(bp <= getCurrentTime()) {
                // break point now!
                breakPoints.removeFirst();
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
        //chhose ODE solver
        setCurrentODESolver(_defaultSolver);
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


    /** Test if the current time is the stop time.
     *  If so, return false ( for stop further simulaiton).
     *  @return false If the simulation time expires.
     *  @exception IllegalActionException If there is no ODE solver, or
     *        thrown by the solver.
     */
    public boolean postfire() throws IllegalActionException {
        if((getCurrentTime()+getSuggestedNextStepSize())>getStopTime()) {
            fireAt(null, getStopTime());
        }
        if(Math.abs(getCurrentTime() - getStopTime()) < getTimeResolution()) {
            updateStates(); // call postfire on all actors
            return false;
        }
        return true;
    }

    /** wrapup . Show the statistics.
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


    /** produce outputs
     *  @exception IllegalActionException If the actor on the output
     *      path throws it.
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

    /** update States
     */
    public void updateStates() throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();
        Enumeration allactors = container.deepGetEntities();
        while(allactors.hasMoreElements()) {
            Actor nextactor = (Actor)allactors.nextElement();
            nextactor.postfire();
        }
    }

    /** Update paramters.
     */
    public void updateParameter(Parameter param)
            throws IllegalActionException {
        if(param == _paramODESolver) {
            if(VERBOSE) {
                System.out.println("solver updating.");
            }
            _solverclass =
            ((StringToken)param.getToken()).stringValue();
            _defaultSolver = _instantiateODESolver(_solverclass);
        } else {
            super.updateParameter(param);
        }
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////
    private void _initParameters() {
        try {
            _solverclass= "ptolemy.domains.ct.kernel.solver.ForwardEulerSolver";
            _paramODESolver = new CTParameter(
                this, "ODESolver", new StringToken(_solverclass));
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,"Parameter name duplication.");
        }

    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // parameter of ODE solver
    private CTParameter _paramODESolver;
    // The classname of the ODE solver
    private String _solverclass;

    // The default solver.
    private ODESolver _defaultSolver = null;

    //indicate the first round of execution.

    private boolean _first;
}
