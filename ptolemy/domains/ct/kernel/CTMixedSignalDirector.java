/* A CT Director that handles the interaction with event based domains.

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
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTMixedSignalDirector
/**
This class adds the event detection capability to the MultiSolverDirector.
FIXME: Consider just use this as the MultiSolverDirector.
@author  Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class CTMixedSignalDirector extends CTMultiSolverDirector{
    /** Construct a CTDirector with no name and no Container.
     *  The default startTime and stopTime are all zeros. There's no
     *  scheduler associated.
     */
    public CTMixedSignalDirector () {
        super();
        _initParameters();
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
    public CTMixedSignalDirector (String name) {
        super(name);
        _initParameters();
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
    public CTMixedSignalDirector (Workspace workspace, String name) {
        super(workspace, name);
        _initParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the end time of this director fire.
     */
    public final double getFireEndTime() {
        return _fireEndTime;
    }

    /** This does the initialization for the entire subsystem. This
     *  is called exactly once at the start of the entire execution.
     *  It set the current time to the start time and the current step
     *  size to the initial step size.
     *  It invoke the initialize() method for all the Actors in the
     *  system. The ODE solver are instanciated, and the current solver
     *  is set to be the breakpoint solver.
     *
     *  @exception IllegalActionException If there's no scheduler or
     *       thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        if (VERBOSE||DEBUG) {
            System.out.println("MixedSignalDirector initialize.");
        }
        CompositeActor ca = (CompositeActor) getContainer();
        if (ca == null) {
            if(DEBUG) {
                System.out.println("Director has no container.");
            }
            throw new IllegalActionException(this, "Has no container.");
        }
        _first = true;
        if(!_isTopLevel()) {
            // clear the parameters and make sure the outside parameters
            // override the local parameter (start time).
            updateParameters();

            // this is an embedded director.
            // synchronize the start time and request a fire at the start time.
            Director exe = ca.getExecutiveDirector();
            setStartTime(exe.getCurrentTime());
            exe.fireAfterDelay(ca, 0.0);
        }
        if (VERBOSE) {
            System.out.println("Director.super initialize.");
        }
        _initialize();
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
        if(!_isTopLevel()) {
            // synchronize time.
            CompositeActor ca = (CompositeActor) getContainer();
            // ca should have beed checked in isTopLevel()
            Director exe = ca.getExecutiveDirector();
            _outsideTime = exe.getCurrentTime();
            if(DEBUG) {
                System.out.println("Outside Time =" + _outsideTime);
            }
            double timeAcc = getTimeAccuracy();
            double nextIterTime = exe.getNextIterationTime();
            double runlength = nextIterTime - _outsideTime;
            if((runlength != 0.0)&& (runlength < timeAcc)) {
                exe.fireAfterDelay(ca, runlength);
                if(DEBUG) {
                    System.out.println("Next iteration is too near" +
                        " (but not sync). Request a refire at:"+nextIterTime);
                }
                return false;
            }
            if(Math.abs (_outsideTime -getCurrentTime()) < timeAcc) {
                if(DEBUG) {
                    System.out.println("Round up current time " +
                        getCurrentTime() + " to outside time " +_outsideTime);
                }
                setCurrentTime(_outsideTime);
            }

            // check for roll back.
            if (_outsideTime < getCurrentTime()) {
                if(DEBUG) {
                    System.out.println(getName() + " rollback from: " +
                        getCurrentTime() + " to: " +_knownGoodTime +
                        "due to outside time " +_outsideTime );
                }
                if(STAT) {
                    NROLL ++;
                }
                rollback();
            }

            runlength = Math.min(runlength, _runAheadLength);
            setFireEndTime(_outsideTime + runlength);
            fireAfterDelay(null,_outsideTime - getCurrentTime());
            fireAfterDelay(null,getFireEndTime()-getCurrentTime());
            if(DEBUG) {
                System.out.println("Fire end time="+getFireEndTime());
            }
        }
        return true;
    }

    /** Rollback the system to a knowGood state.
     */
    public void rollback() throws IllegalActionException{
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Enumeration memactors = scheduler.memarisActors();
        while(memactors.hasMoreElements()) {
            CTMemarisActor mem =(CTMemarisActor)memactors.nextElement();
            if(VERBOSE) {
                System.out.println("Restore State..."+
                    ((Nameable)mem).getName());
            }
            mem.restoreStates();
        }
        setCurrentTime(_knownGoodTime);
    }

    /** save the know good state.
     */
    public void saveStates() {
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Enumeration memactors = scheduler.memarisActors();
        while(memactors.hasMoreElements()) {
            CTMemarisActor mem =(CTMemarisActor)memactors.nextElement();
            if(VERBOSE) {
                System.out.println("Save State..."+
                    ((Nameable)mem).getName());
            }
            mem.saveStates();
        }
        _knownGoodTime = getCurrentTime();
    }

    /**  Fire the system until the next break point. If this is the
     *   top level, then first just one successful iteration.
     *
     *  @exception IllegalActionException If thrown by the ODE solver.
     */
    public void fire() throws IllegalActionException {
        // FIXME: what should these do?
        // prepare some variables.
        double timeAcc = getTimeAccuracy();
        boolean knownGood = false;
        boolean endOfFire = false;
        while(!endOfFire) {
            if (_first) {
                _first = false;
                produceOutput();
                //return;
            }
            updateStates(); // call postfire on all actors
            if(!_isTopLevel()) {
                if(knownGood) {
                    saveStates();
                    knownGood = false;
                }
            }



            //Refine step size and set ODE Solvers.
            setCurrentODESolver(_getDefaultSolver());
            setCurrentStepSize(getSuggestedNextStepSize());
            double tnow = getCurrentTime();
            double bp;
            TotallyOrderedSet breakPoints = getBreakPoints();
            //choose ODE solver
            // If now is a break point, remove the break point from table;
            if((breakPoints != null) && !breakPoints.isEmpty()) {
                bp = ((Double)breakPoints.first()).doubleValue();
                if(DEBUG) {
                    System.out.println("Next break point " + bp);
                }
                if(Math.abs(bp-_outsideTime) < timeAcc) {
                    knownGood = true;
                    // The result of this iteration should be saved.
                }
                if(Math.abs(bp-tnow) < timeAcc) {
                    // break point now!
                    breakPoints.removeFirst();
                    setCurrentTime(bp);
                    setCurrentODESolver(_getBreakpointSolver());
                    setCurrentStepSize(getMinStepSize());
                    if(DEBUG) {
                        System.out.println("Change to BP solver with stepsize"
                        + getCurrentStepSize());
                    }
                }
                //adjust step size;
                while(!breakPoints.isEmpty()) {
                    bp = ((Double)breakPoints.first()).doubleValue();
                    if(Math.abs(bp-tnow) < timeAcc) {
                        setCurrentTime(bp);
                        breakPoints.removeFirst();
                    } else {
                        double iterEndTime = tnow+getCurrentStepSize();
                        if (iterEndTime > bp) {
                            setCurrentStepSize(bp-tnow);
                        }
                        break;
                    }
                }
            }
            if(DEBUG) {
                System.out.println("Resolved stepsize: "+getCurrentStepSize());
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
                while(true) {
                    getCurrentODESolver().proceedOneStep();
                    detectEvent();
                    if(hasMissedEvent()) {
                        setCurrentTime(getCurrentTime()-getCurrentStepSize());
                        setCurrentStepSize(getRefineStepSize());
                    } else {
                        break;
                    }
                }
                produceOutput();
            }
            // one iteration finished.
            // exit condition.
            if(_isTopLevel()) {
                endOfFire = true;
            } else {
                Director exe = ca.getExecutiveDirector();
                if(DEBUG) {
                    System.out.println("Checking FireEndTime"+getFireEndTime());
                }
                // If this is the stop time, request a refire.
                if(Math.abs(getCurrentTime()-getFireEndTime()) < timeAcc) {
                    exe = ca.getExecutiveDirector();
                    exe.fireAfterDelay(ca, getCurrentTime()-_outsideTime);
                    if(DEBUG) {
                        System.out.println("Ask for refire at " +
                            getCurrentTime());
                    }
                    endOfFire = true;
                }
            }
        }
    }

    /** Test if the current time is the stop time.
     *  If so, return false ( for stop further simulaiton).
     *  @return false If the simulation time expires.
     *  @exception IllegalActionException If there is no ODE solver, or
     *        thrown by the solver.
     */
    public boolean postfire() throws IllegalActionException {
        if(_isTopLevel()) {
            if(Math.abs(getCurrentTime()-getStopTime()) < getTimeAccuracy()) {
                updateStates(); // call postfire on all actors
                return false;
            }
            if(getStopTime() < getCurrentTime()) {
                throw new InvalidStateException(this,
                " stop time is less than the current time.");
            }
            if((getCurrentTime()+getSuggestedNextStepSize())>getStopTime()) {
                fireAfterDelay(null, getStopTime()-getCurrentTime());
            }
        }

        return true;
    }


    /** Set the stop time for this iteration.
     *  For internal director, this will result in that the local director
     *  release the control to the executive director.
     */
    public void setFireEndTime(double time ) {
        if(time < getCurrentTime()) {
            throw new InvalidStateException(this,
                " Fire end time should be greater than or equal to" +
                " the current time.");
        }
        _fireEndTime = time;
    }

    /** detect event. fire all the actors along the event detector path.
     */
    public void detectEvent() throws IllegalActionException{
        if(VERBOSE) {
            System.out.println( "Detecting event...");
        }
        CTScheduler scheduler = (CTScheduler) getScheduler();
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
        Enumeration edactors = scheduler.eventGenerationSchedule();
        while(edactors.hasMoreElements()) {
            Actor nextactor = (Actor)edactors.nextElement();
            if(VERBOSE) {
                System.out.println("Fire event detection path..."+
                    ((Nameable)nextactor).getName());
            }
            nextactor.fire();
        }
    }

    /** Return true if any of the event detectors has a missed event in
     *  the last step.
     */
    public boolean hasMissedEvent() {

        boolean result = false;
        _refineStepSize = getCurrentStepSize();
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Enumeration edators = scheduler.eventGenerateActors();
        while(edators.hasMoreElements()) {
            CTEventGenerateActor ed=(CTEventGenerateActor)edators.nextElement();
            if(DEBUG) {
                System.out.println("Ask for missed event from..."+
                    ((Nameable)ed).getName());
            }
            boolean answer = ed.hasMissedEvent();
            if(DEBUG) {
                System.out.println("Answer is: " + answer);
            }
            if(answer == true) {
                result = true;
                _suggestRefineStepSize(ed.refineStepSize());
            }
        }
        return result;
    }

    /** Return the minimum of all the suggested refined step size
     *  from event detectors.
     */
    public double getRefineStepSize() {
        return _refineStepSize;
    }

    /** Update the given parameter. If the parameter is RunAheadLength
     *  update it. Otherwise pass it to the super class.
     */
    public void updateParameters(Parameter param)
            throws IllegalActionException {
        if(param == _paramRunAheadLength) {
            if(VERBOSE) {
                System.out.println("run ahead time updating.");
            }
            _runAheadLength =
            ((DoubleToken)param.getToken()).doubleValue();
        } else {
            super.updateParameter(param);
        }
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /**Return true if this is a toplevel director. A syntax suger.
     */
    protected boolean _isTopLevel() {
        long version = workspace().getVersion();
        if (version == _mutationVersion) {
            return _isTop;
        }
        try {
            workspace().getReadAccess();
            CompositeActor container = (CompositeActor)getContainer();
            if(container.getExecutiveDirector() == null) {
                _isTop = true;
            } else {
                _isTop = false;
            }
            _mutationVersion = version;
        } finally {
            workspace().doneReading();
            return _isTop;
        }
    }

    /** Initialize parameters.
     */
    private void _initParameters() {
        try {
            _runAheadLength = 1.0;
            _paramRunAheadLength = new CTParameter(this,
                "MaxRunAheadLength", new DoubleToken(_runAheadLength));
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,"Parameter name duplication.");
        }
    }

    /** suggest a refined step size for event detection.
     */
    protected void _suggestRefineStepSize(double refine) {
        _refineStepSize = Math.min(_refineStepSize, refine);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // version of mutation. If this version is not the workspace
    // version then every thing related to mutation need to be updated.
    private long _mutationVersion = -1;

    // Illustrate if this is the top level director.
    private boolean _isTop;

    // indeicate the first execution.
    private boolean _first;

    // The time for the "known good" state.
    private double _knownGoodTime;

    // The current outside time.
    private double _outsideTime;

    // parameter of default runaheadlength
    private CTParameter _paramRunAheadLength;

    // variable of runaheadlength
    private double _runAheadLength;

    // refined step size for event detection.
    private double _refineStepSize;

    // the end time of a fire.
    private double _fireEndTime;
}
