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

    /** Return the time of the outside domain. If this is the top level
     *  return the current time.
     *  @return The outside current time.
     */
    public double getOutsideTime() {
        if(_isTopLevel()) {
            return getCurrentTime();
        }
        return _outsideTime;
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
        if(_isTopLevel()) {
            super.fire();
            return;
        }
        if (_first) {
            _first = false;
            produceOutput();
            updateStates();
            //return;
        }
        CompositeActor ca = (CompositeActor) getContainer();
        Director exe = ca.getExecutiveDirector(); // it may be null.
        double timeAcc = getTimeResolution();
        if (_isEventPhase()) {
            if(VERBOSE) {
                System.out.println("In event phase execution.");
            }
            _eventPhaseExecution();
            exe.fireAt(ca, exe.getCurrentTime());
            _setEventPhase(false);
            return;
        }
        // Not event phase.
        while(true) {
            // Just after a breakpoint iteration. This is the known
            // good state.
            if(isBPIteration()) { 
                _markStates();
            }
            _setFireBeginTime(getCurrentTime());
            //Refine step size
            setCurrentStepSize(getSuggestedNextStepSize());
            _processBreakpoints();
            if(DEBUG) {
                System.out.println("Resolved stepsize: "+getCurrentStepSize());
            }
            _fireOneIteration();
            if (_stopByEvent()) {
                exe.fireAt(ca, getCurrentTime());
                _setEventPhase(true);
                return;
            } else if (getCurrentTime()>=getFireEndTime()) {
                exe.fireAt(ca, getCurrentTime());
                return;
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
        sch.setValid(false);
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
        _initialize();
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
            return super.postfire();
        } else {
            return true;
        }
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
        super.prefire(); // always returns true.
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
            if(runlength < 0 ) {
                throw new InvalidStateException(this, "Outside domain" 
                        + " time collapse."
                        + " current time " + _outsideTime
                        + " next iteration time " + nextIterTime);
            }
            // synchronization, handle round up error.
            if(runlength < timeAcc) {
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
            if (_outsideTime > getCurrentTime()) {
                throw new IllegalActionException(this, exe,
                        " time collapse.");
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
                fireAt(null,_outsideTime);
                _catchUp();
            }
            //runlength = Math.min(runlength, _runAheadLength);
            System.out.println( "Current Time " + getCurrentTime() 
                    + "Outside domain current time " + _outsideTime
                    + " next iteration time " + nextIterTime
                    + "run length "+ runlength);
            if(runlength < _runAheadLength) {
                _setFireEndTime(nextIterTime);
            } else {
                _setFireEndTime(_outsideTime + _runAheadLength );
            }
            // fireAt(null, _outsideTime);
            // fireAt(null, getFireEndTime());
            // Now it's guranteed that the current time is the outside time.
            if(DEBUG) {
                System.out.println("Fire end time="+getFireEndTime());
            }
        }
        return true;
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

    /** Return true if the current phase of fire is event phase.
     */
    protected boolean _isEventPhase() {
        return _eventPhase;
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

    /** Catch up the simulation from a knowGood point to the outside
     *  current time. There's should be no breakpoints of any kind
     *  in this process. If the current time is greater than or equal
     *  to the outside time, then do nothing.
     */
    protected void _catchUp() throws IllegalActionException {
        if (getCurrentTime() >= getOutsideTime()) {
            return;
        }
        // Don't need to consider breakpoint.
        _setFireBeginTime(getCurrentTime());
        while(getCurrentTime() < getOutsideTime()) {
            _fireOneIteration();
        }
    }

    /** Mark the current state as the known good state. Call the
     *  markStates() method on all CTStatefulActors. Save the current time
     *  as the "known good" time.
     */
    protected void _markStates() {
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

    /** Set the stop time for this iteration. 
     *  For a director that is not at the top-level, set the fire end time
     *  to be the current time  will result 
     *  in that the local director
     *  release the control to the executive director.
     *  @param The fire end time.
     */
    protected void _setFireEndTime(double time ) {
        if(time < getCurrentTime()) {
            throw new InvalidStateException(this,
                " Fire end time should be greater than or equal to" +
                " the current time.");
        }
        _fireEndTime = time;
    }

    /** Return true if the current fire phase need to stop due to
     *  the occurrence of events (expected or unexpected).
     */
    protected boolean _stopByEvent() {
        // expected events
        double bp;
        TotallyOrderedSet breakPoints = getBreakPoints();
        double tnow = getCurrentTime();
        if(breakPoints != null) {
            while (!breakPoints.isEmpty()) {
                bp = ((Double)breakPoints.first()).doubleValue();
                if(bp < (tnow-getTimeResolution())) {
                    // break point in the past or at now.
                    breakPoints.removeFirst();
                } else if(Math.abs(bp-tnow) < getTimeResolution()){
                    // break point now!
                    return true;
                } else {
                    break;
                }
            }
        }
        // detected events
        CTScheduler sched = (CTScheduler)getScheduler();
        Enumeration evgens = sched.eventGenerators();
        while(evgens.hasMoreElements()) {
            CTEventGenerator evg = (CTEventGenerator) evgens.nextElement();
            if(evg.hasCurrentEvent()) {
                return true;
            }
        }
        return false;
    }

    /** True argument sets the phase to be event phase.
     */
    protected void _setEventPhase(boolean eph) {
        _eventPhase = eph;
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

    // the end time of a fire.
    private double _fireEndTime;

    // whether in the emit event phase;
    private boolean _eventPhase = false;
}
