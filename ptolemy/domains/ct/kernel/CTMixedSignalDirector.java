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
import ptolemy.actor.sched.*;
import ptolemy.data.expr.*;
import ptolemy.data.*;
import java.util.*;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// CTMixedSignalDirector
/**
This a CTDirector that supports the interaction of the continuous time 
simulation with event-based domains. This director can both serve as
a top-level director and an embedded director that is contained by
a composite actor in an event-based domain. If it is a top-level
director, it acts exactly like a CTMultiSolverDirector. If it is 
embedded in another event-based domain, it will run ahead of the global
time and prepare to roll back if necessary.
<P>
This class has an additional parameter than the CTMultiSolverDirector,
which is the maximum run ahead of time length (<code>MaxRunAheadLength</code>).
The default value is 1.0.
<P>
The run ahead of time is achieved by the following mechanism.<Br>
<UL>
<LI> At the initialize stage of the execution, the director will request
a fire at the global current time.
<LI> At each prefire stage the execution, the fire end time is computed
based on the current time of the executive director t1, the next iteration
time of the executive director t2, the value of the parameter 
<code>MaxRunAheadLength</code> t3. The fire end time is t1+min(t2, t3)
<LI> At the prefire stage, the local current time is compared with the
current time of the executive director. If the local time is later than 
the executive director time, then the directed system will rollback to a 
"known good" state.
<LI> The "known good" state is the state of the system at the time when
local time is equal to the current time of the executive director.
It is saved by during the fire stage of the execution.
<LI> At the fire stage, the director will stop at the first of the two times,
the fire end time and the first detected event time.
</UL>

@author  Jie Liu
@version $Id$
@see classname
@see full-classname
*/
public class CTMixedSignalDirector extends CTMultiSolverDirector{
    /** Construct a CTMixedSignalDirector with no name and no Container.
     *  All parameters take their default values. The scheduler is a
     *  CTScheduler.
     */
    public CTMixedSignalDirector () {
        super();
        _initParameters();
    }

    /** Construct a CTMixedSignalDirector in the default workspace 
     *  with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All parameters take their default values. The scheduler is a
     *  CTScheduler.
     *
     *  @param name The name of this director.
     */
    public CTMixedSignalDirector (String name) {
        super(name);
        _initParameters();
    }

    /** Construct a CTMixedSignalDirector in the given workspace with
     *  the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All parameters take their default values. The scheduler is a
     *  CTScheduler.   
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

    /** Return the end time of this director's firing.
     *  
     *  @return The fire end time.
     */
    public final double getFireEndTime() {
        return _fireEndTime;
    }

    /** Return the minimum of all the refined step size returned
     *  from event detectors.
     *  @return the minimum refined step size.
     */
    public double getRefineStepSize() {
        return _refineStepSize;
    }

    /** Execute the directed (sub)system to the fire end time.
     *  If this is a top-level director, the fire end time if the 
     *  current time at the beginning of the fire() method plus the 
     *  the step size of one successful step. 
     *  Otherwise, it executes until the one of the following conditions
     *  is satisfied. 1) The fire end time computed in the prefire()
     *  method is reached. 2) An event is generated.
     *  It saves the state of the system at the current time of the executive
     *  director as the "known good" state. And run further ahead of time.
     *  The "known good" state is used for roll back.
     *  @exception IllegalActionException If thrown by the ODE solver,
     *       or the prefire(), fire(), or postfire() methods of an actor.
     */
    public void fire() throws IllegalActionException {
        CompositeActor ca = (CompositeActor) getContainer();
        double timeAcc = getTimeResolution();
        boolean knownGood = false;
        boolean endOfFire = false;
        if (_first) {
            _first = false;
            produceOutput();
            //return;
        }
        updateStates();
        if(!_isTopLevel()) {
            if(_emitEvent) {
                Director exe = ca.getExecutiveDirector();
                exe.fireAt(ca, exe.getCurrentTime());
                _emitEvent = false;
                return;
            }
            _emitEvent = true;
        }
        while(!endOfFire) {
            if(!_isTopLevel()) {
                if(knownGood) {
                    _saveStates();
                    knownGood = false;
                }
            }
            //Refine step size and set ODE Solvers.
            setCurrentODESolver(getDefaultSolver());
            setCurrentStepSize(getSuggestedNextStepSize());
            double tnow = getCurrentTime();
            double bp;
            TotallyOrderedSet breakPoints = getBreakPoints();
            // clear break points in the past.
            if(breakPoints != null) {
                while(!breakPoints.isEmpty()) {
                    bp = ((Double)breakPoints.first()).doubleValue();
                    if (bp < (getCurrentTime()-timeAcc/2.0)) {
                        breakPoints.removeFirst();
                    } else {
                        break;
                    }
                }
            }

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
                    setCurrentODESolver(getBreakpointSolver());
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
            Enumeration actors = ca.deepGetEntities();
            while(actors.hasMoreElements()) {
                Actor a = (Actor) actors.nextElement();
                ready = ready && a.prefire();
            }
            if(ready) {
                while(true) {
                    getCurrentODESolver().proceedOneStep();
                    _detectEvent();
                    if(_hasMissedEvent()) {
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
                    System.out.println("Checking FireEndTime"+
                            getFireEndTime());
                }
                // If this is the stop time, request a refire.
                if(Math.abs(getCurrentTime()-getFireEndTime()) < timeAcc) {
                    exe.fireAt(ca, getCurrentTime());
                    if(DEBUG) {
                        System.out.println("Ask for refire at " +
                            getCurrentTime());
                    }
                    endOfFire = true;
                } else {
                    updateStates();
                }
            }
        }
    }

    /** Initialize the director parameters (including time) and initialize 
     *  all the actors in the container. This
     *  is called exactly once at the start of the entire execution.
     *  It checks if it has a container and an CTScheduler. 
     *  It invoke the initialize() method for all the Actors in the
     *  system. The ODE solver are instantiated, and the current solver
     *  is set to be the breakpoint solver. The breakpoint table is cleared,
     *  and the start time is set to be the first breakpoint. 
     *  Invalidate the schedule.
     *  If this is the top-level director, 
     *  It sets the current time to the start time of the and the current step
     *  size to the initial step size.
     *  Otherwise the start time is the current time of the outside
     *  domain. And it will request a refire at the current time from
     *  the executive director.
     *
     *  @exception IllegalActionException If this director has no container or
     *       no scheduler, or thrown by a contained actor.
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
        CTScheduler sch = (CTScheduler)getScheduler();
        if (sch == null) {
            if(DEBUG) {
                System.out.println("Director does not have a scheduler.");
            }
            throw new IllegalActionException( this,
            "does not have a scheduler.");
        }
        _first = true;
        if(!_isTopLevel()) {
            // clear the parameters and make sure the outside domain
            // parameters
            // overwrite the local parameter (e.g. start time).
            updateParameters();

            // this is an embedded director.
            // synchronize the start time and request a fire at the start time.
            Director exe = ca.getExecutiveDirector();
            double tnow = exe.getCurrentTime();
            setStartTime(tnow);
            exe.fireAt(ca, tnow);
        }
        if (VERBOSE) {
            System.out.println("Director.super initialize.");
        }
        // update parameter is called in _initialize. But if this is 
        // not a top-level director, the parameter should already be
        // processed by the code above. So the updateParameters() in
        // _initialize() will do nothing.
        //_initialize();
    }

    /** If this is a top-level director, returns true if the current time
     *  is less than the stop time, otherwise, return true always.
     *  If this is a top-level director, and the current time is greater
     *  than the stop time, an InvalidStateException is thrown. 
     *  @return True if this is not a top-level director, or the simulation
     *     is not finished.
     *  @exception IllegalActionException Never thrown.
     */
    public boolean postfire() throws IllegalActionException {
        if(_isTopLevel()) {
            if(Math.abs(getCurrentTime()-getStopTime()) 
                    < getTimeResolution()) {
                updateStates(); // call postfire on all actors
                return false;
            }
            if(getStopTime() < getCurrentTime()) {
                throw new InvalidStateException(this,
                " stop time is less than the current time.");
            }
            if((getCurrentTime()+getSuggestedNextStepSize())>getStopTime()) {
                fireAt(null, getStopTime());
            }
        }

        return true;
    }

    /** Returns true always, indicating that the (sub)system is always ready
     *  for one iteration. The schedule is recomputed if there are mutations
     *  occurred after last iteration. Note that mutations can only 
     *  occur between iterations in the CT domain.
     *  The parameters are updated.
     *  <P>
     *  If this is not a top-level director, some additional work is done
     *  to synchronize time with the executive director. In particular,
     *  it will compare its local time, say t, with the current time 
     *  of the executive director, say t0.
     *  If t==t0, does nothing. If t > t0, then rollback to the "know good"
     *  state. If t < t0, then throw an exception because the CT subsystem
     *  should always run ahead of time.
     *  <P> 
     *  The fire end time is computed. If this is a 
     *  top-level director, the fire end time is the (local) current
     *  time plus the (local) current step size.
     *  If this director is not a top-level director, the time is 
     *  resolved from the current time of the outside domains, say t1,
     *  the next iteration time of the outside domain, say t2, 
     *  the MaxRunAheadLength parameter of this director, say t3.
     *  The fire end time is set 
     *  to be <code>t1 + min(t2, t3)</code>. The fire end time may be
     *  further refined by the fire() method due to event detection.
     *  In particular, when the first event is detected, say at t4,
     *  then the fire end time is set to t4.
     *  @return true Always
     *  @exception IllegalActionException If the local time is
     *       less than the current time of the executive director,
     *       or thrown by a directed actor.
     */
    public boolean prefire() throws IllegalActionException {
        if (VERBOSE) {
            System.out.println("Director prefire.");
        }
        if(DEBUG) {
            NSTEP++;
        }
        if(!scheduleValid()) {
            // mutation occurred, redo the schedule;
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
            // ca should have beed checked in _isTopLevel()
            Director exe = ca.getExecutiveDirector();
            _outsideTime = exe.getCurrentTime();
            if(DEBUG) {
                System.out.println("Outside Time =" + _outsideTime);
            }
            double timeAcc = getTimeResolution();
            double nextIterTime = exe.getNextIterationTime();
            double runlength = nextIterTime - _outsideTime;
            if((runlength != 0.0)&& (runlength < timeAcc)) {
                exe.fireAt(ca, nextIterTime);
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
                _rollback();
            }
            if (_outsideTime > getCurrentTime()) {
                throw new IllegalActionException(this, exe,
                        " time collapse.");
            } 
            runlength = Math.min(runlength, _runAheadLength);
            setFireEndTime(_outsideTime + runlength);
            fireAt(null,_outsideTime);
            fireAt(null,getFireEndTime());
            if(DEBUG) {
                System.out.println("Fire end time="+getFireEndTime());
            }
        }
        return true;
    }

    /** Set the stop time for this iteration. 
     *  For a director that is not at the top-level, set the fire end time
     *  to be the current time  will result 
     *  in that the local director
     *  release the control to the executive director.
     *  @param The fire end time.
     */
    public void setFireEndTime(double time ) {
        if(time < getCurrentTime()) {
            throw new InvalidStateException(this,
                " Fire end time should be greater than or equal to" +
                " the current time.");
        }
        _fireEndTime = time;
    }

    /** Update the given parameter. If the parameter name is MaxRunAheadLength,
     *  update it. Otherwise pass it to the super class.
     *  @param param The parameter to be updated.
     *  @exception IllegalActionException If thrown by the super class.
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

    /** Detect events. Fire all the actors according to the event generating 
     *  schedule. 
     *  @exception IllegalActionException If thrown by an actor being fired.
     */
    protected void _detectEvent() throws IllegalActionException{
        if(VERBOSE) {
            System.out.println( "Detecting event...");
        }
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Enumeration integrators = scheduler.dynamicActorSchedule();
        while(integrators.hasMoreElements()) {
            CTDynamicActor integrator=
                (CTDynamicActor)integrators.nextElement();
            if(VERBOSE) {
                System.out.println("Excite State..."+
                    ((Nameable)integrator).getName());
            }
            integrator.emitTentativeOutputs();
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
     *  @return True if any of the event detectors has a missed event in
     *  the last step.
     */
    protected boolean _hasMissedEvent() {
        boolean result = false;
        _refineStepSize = getCurrentStepSize();
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Enumeration edators = scheduler.eventGenerators();
        while(edators.hasMoreElements()) {
            CTEventGenerator ed=
                (CTEventGenerator)edators.nextElement();
            if(DEBUG) {
                System.out.println("Ask for missed event from..."+
                    ((Nameable)ed).getName());
            }
            boolean answer = true; //ed.hasMissedEvent();
            if(DEBUG) {
                System.out.println("Answer is: " + answer);
            }
            if(answer == true) {
                result = true;
                //   _refineStepSize = Math.min(_refineStepSize, 
                //       ed.refineStepSize()); 
            }
        }
        return result;
    }

    /**Return true if this is a top-level director. A syntax sugar.
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

    /** Rollback the system to a "known good" state. All the actors with
     *  states are called to restore their saved states. The 
     *  current time of the director is set to the time of the "known
     *  good" state.
     *  @exception IllegalActionException If thrown by the restoreStates()
     *       method of an actor.
     */
    protected void _rollback() throws IllegalActionException{
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Enumeration memactors = scheduler.statefulActors();
        while(memactors.hasMoreElements()) {
            CTStatefulActor mem =(CTStatefulActor)memactors.nextElement();
            if(VERBOSE) {
                System.out.println("Restore State..."+
                    ((Nameable)mem).getName());
            }
            mem.goToMarkedState();
        }
        setCurrentTime(_knownGoodTime);
    }

    /** Save the current state as the known good state. Call the
     *  saveStates() method on all CTStatefulActors. Save the current time
     *  as the "known good" time.
     */
    protected void _saveStates() {
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Enumeration memactors = scheduler.statefulActors();
        while(memactors.hasMoreElements()) {
            CTStatefulActor mem =(CTStatefulActor)memactors.nextElement();
            if(VERBOSE) {
                System.out.println("Save State..."+
                    ((Nameable)mem).getName());
            }
            mem.markState();
        }
        _knownGoodTime = getCurrentTime();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Initialize parameters in addition to the parameters inherited
     *  from CTMultiSolverDirector. In this class the additional 
     *  parameter is the maximum run ahead time length 
     *  (<code>MaxRunAheadLength</code>). The default value is 1.0.
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
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // version of mutation. If this version is not the workspace
    // version then every thing related to mutation need to be updated.
    private long _mutationVersion = -1;

    // Illustrate if this is the top level director.
    private boolean _isTop;

    // indicate the first execution.
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

    // whether in the emit event phase;
    private boolean _emitEvent = false;
}
