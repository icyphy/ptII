/* A CT Director that handles the interaction with event based domains.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@ProposedRating Green (liuj@eecs.berkeley.edu)
@AcceptedRating Green (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;

import ptolemy.domains.ct.kernel.util.TotallyOrderedSet;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.DoubleToken;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// CTMixedSignalDirector
/**
This is a CTDirector that supports the interaction of the continuous-time
simulation with event-based domains. This director can both serve as
a top-level director and a inside director that is contained by
a composite actor in an event-based domain. If it is a top-level
director, it acts exactly like a CTMultiSolverDirector. If it is
embedded in another event-based domain, it will run ahead of the global
time and prepare to roll back if necessary.
<P>
This class has an extra parameter as compare to the CTMultiSolverDirector,
which is the maximum run ahead of time length (<code>runAheadLength</code>).
The default value is 1.0.
<P>
The running ahead of time is achieved by the following mechanism.<Br>
<UL>
<LI> At the initialize stage of the execution, the director will request
a fire at the global current time.
<LI> At each prefire stage of the execution, the fire end time is computed
based on the current time of the executive director, t1, the next iteration
time of the executive director, t2, and the value of the parameter
<code>runAheadLength</code>, t3. The fire end time is t1+min(t2, t3)
<LI> At the prefire stage, the local current time is compared with the
current time of the executive director. If the local time is later than
the executive director time, then the directed system will rollback to a
"known good" state.
<LI> The "known good" state is the state of the system at the time when
local time is equal to the current time of the executive director.
<LI> At the fire stage, the director will stop at the first of the
following two times, the fire end time and the first detected event time.
</UL>

@author  Jie Liu
@version $Id$
*/
public class CTMixedSignalDirector extends CTMultiSolverDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  All the parameters take their default values.
     */
    public CTMixedSignalDirector() {
        super();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace The workspace of this object.
     */
    public CTMixedSignalDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  All the parameters take their default values.
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.  May be thrown in a derived class.
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public CTMixedSignalDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                           parameters                      ////

    /** Parameter of the run ahead length. The default value is 1.0.
     */
    public Parameter runAheadLength;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in an attribute. If the changed attribute matches
     *  a parameter of the director, then the corresponding private copy of the
     *  parameter value will be updated.
     *  @param param The changed parameter.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if(attribute == runAheadLength) {
            if(_debugging) _debug("run ahead length updating.");
            double value =
                ((DoubleToken)runAheadLength.getToken()).doubleValue();
            if(value < 0) {
                throw new IllegalActionException(this,
                        " runAheadLength cannot be negative.");
            }
            _runAheadLength = value;
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Return true indicating that this director can be an inside director.
     *  @return True always.
     */
    public boolean canBeInsideDirector() {
        return true;
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  Note that ODE solvers are stateless, so we only clone the class
     *  name of the solvers.
     *  @param workspace The workspace for the new object.
     *  @return A new director.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        CTMixedSignalDirector newobj =
            (CTMixedSignalDirector)(super.clone(workspace));
        newobj.runAheadLength =
            (Parameter)newobj.getAttribute("runAheadLength");
        return newobj;
    }

    /** Return the end time of this director's current iteration.
     *  @return The fire end time.
     */
    public final double getIterationEndTime() {
        return _iterationEndTime;
    }

    /** Return the time of the outside domain. If this is the top level
     *  director return the current time.
     *  @return The outside current time.
     */
    public double getOutsideTime() {
        if(_isTopLevel()) {
            return getCurrentTime();
        }
        return _outsideTime;
    }

    /** Execute the directed (sub)system to the iteration end time.
     *  If the current phase is an event phase, (in the sense that
     *  discrete events will be produced or consumed),
     *  this director will consume all the input
     *  tokens, produce all output tokens, and then request a zero
     *  delay refire from it executive director.
     *  If this is a top-level director, the iteration end time is the
     *  current time at the beginning of the fire() method plus the
     *  the step size of one accurate step.
     *  Otherwise, it executes until one of the following conditions
     *  is satisfied. 1) The iteration end time computed in the prefire()
     *  method is reached. 2) An event is generated.
     *  It saves the state of the system at the current time of the executive
     *  director as the "known good" state, and runs ahead of that time.
     *  The "known good" state is used for roll back.
     *  @exception IllegalActionException If thrown by the ODE solver,
     *       or the prefire() or the fire() methods of an actor.
     */
    public void fire() throws IllegalActionException {
        if(_isTopLevel()) {
            super.fire();
            return;
        }
        CompositeActor container = (CompositeActor) getContainer();
        Director exe = container.getExecutiveDirector();
        // It must not be null.
        if (_isEventPhase()) {
            if(_debugging) _debug(getFullName(),
                    "In event phase execution.");
            _eventPhaseExecution();
            _setEventPhase(false);
            if(_debugging) _debug(getFullName(),
                    "Request a refire at the current time." +
                    exe.getCurrentTime(),
                    "--END of fire");
            exe.fireAt(container, exe.getCurrentTime());
            return;
        } else {
            _eventPhaseExecution();
            while(true) {
                if(isBreakpointIteration()) {
                    // Just after a breakpoint iteration. This is the known
                    // good state. Note that isBreakpointIteration is
                    // set to false in _processBreakpoints().
                    _markStates();
                }
                _setIterationBeginTime(getCurrentTime());
                // Guarantee to stop at the iteration end time.
                fireAt(null, getIterationEndTime());
                // Refine step size.
                setCurrentStepSize(getSuggestedNextStepSize());
                _processBreakpoints();
                if(_debugging) _debug(getName(),
                        "Resolved stepsize: " + getCurrentStepSize() +
                        " One iteration from " + getCurrentTime() +
                        " to " + (getCurrentStepSize()+getCurrentTime()));
                _fireOneIteration();
                if (_isStoppedByEvent()) {
                    if(_debugging) {
                        _debug(getFullName() + " fire stopped by event.",
                                "at " + getCurrentTime(),
                                "request refire at " + getCurrentTime(),
                                "set Event phase to TRUE");
                    }
                    exe.fireAt(container, getCurrentTime());
                    _setEventPhase(true);
                    return;
                } else if (Math.abs(getCurrentTime()- getIterationEndTime())
                        < getTimeResolution()) {
                    if(_debugging) {
                        _debug(getFullName() + " fire stopped regularly.",
                                "at " + getCurrentTime(),
                                "request refire at " + getIterationEndTime(),
                                "set Event phase to FALSE");
                    }
                    exe.fireAt(container, getIterationEndTime());
                    _setEventPhase(false);
                    return;
                }
            }
        }
    }

    /** First initialize the execution as in CTMultiSolverDirector.
     *  If this director is not at the top-level, also
     *  request to fire at the current time from the executive director.
     *  @see CTMultiSolverDirector#initialize()
     *  @exception IllegalActionException If this director has no container or
     *       no scheduler, or thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        if(_debugging) _debug(getFullName() + " initialize.");
        super.initialize();
        if(!_isTopLevel()) {
            TypedCompositeActor container
                = (TypedCompositeActor)getContainer();
            Director exe = container.getExecutiveDirector();
            exe.fireAt(container, getCurrentTime());
        }
        }


    /** If this is a top-level director, behave exactly as a
     *  CTMultiSolverDirector, otherwise always return true.
     *  @return True if this is not a top-level director or the simulation
     *     is not finished.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean postfire() throws IllegalActionException {
        if(_isTopLevel()) {
            return super.postfire();
        } else {
            return true;
        }
    }

    /** Always returns true, indicating that the (sub)system is always ready
     *  for one iteration. The schedule is recomputed if there are mutations
     *  that occurred after last iteration. Note that mutations can only
     *  occur between iterations in the CT domain.
     *  <P>
     *  If this is not a top-level director, some additional work is done
     *  to synchronize time with the executive director. In particular,
     *  it will compare its local time, say t, with the current time
     *  of the executive director, say t0.
     *  If t == t0, do nothing. <BR>
     *  If t > t0, then rollback to the "known good" time (which should be
     *  less than the outside time) and catch up
     *  to the outside time. <BR>
     *  If t < t0, then throw an exception because the CT subsystem
     *  should always run ahead of time. <BR>
     *  <P>
     *  The iteration end time is computed. If this is a
     *  top-level director, the iteration end time is the (local) current
     *  time plus the (local) current step size.
     *  If this director is not a top-level director, the time is
     *  resolved from the current time of the outside domains, say t1,
     *  the next iteration time of the outside domain, say t2, and
     *  the runAheadLength parameter of this director, say t3.
     *  The iteration end time is set
     *  to be <code>t1 + min(t2, t3)</code>. The iteration end time may be
     *  further refined by the fire() method due to event detection.
     *  In particular, when the first event is detected, say at t4,
     *  then the iteration end time is set to t4.
     *  @return true Always
     *  @exception IllegalActionException If the local time is
     *       less than the current time of the executive director,
     *       or thrown by a directed actor.
     */
    public boolean prefire() throws IllegalActionException {

        super.prefire(); // always returns true.
        if(!_isTopLevel()) {
            // synchronize time.
            CompositeActor container = (CompositeActor) getContainer();
            // ca should have beed checked in _isTopLevel()
            Director exe = container.getExecutiveDirector();
            _outsideTime = exe.getCurrentTime();
            double timeResolution = getTimeResolution();
            double nextIterationTime = exe.getNextIterationTime();
            double aheadLength = nextIterationTime - _outsideTime;
            if(_debugging) _debug(getName(), "Outside Time = " + _outsideTime,
                    "NextIterationTime = " + nextIterationTime,
                    "Inferred run length = " + aheadLength);
            if(aheadLength < 0 ) {
                throw new InvalidStateException(this, "Outside domain"
                        + " time going backward."
                        + " Current time = " + _outsideTime
                        + ", but the next iteration time = "
                        + nextIterationTime);
            }
            if (aheadLength == 0 ) {
                // This only happens when the current time of the outside
                // domain is the stop time. So return false and stop
                // executing.
                return false;
            }

            if(_debugging) _debug( "Current Time " + getCurrentTime()
                    + "Outside domain current time " + _outsideTime
                    + " next iteration time " + nextIterationTime
                    + "run length "+ aheadLength);

            // Synchronization, handle round up error.
            if(aheadLength < timeResolution) {
                exe.fireAt(container, nextIterationTime);
                if(_debugging) _debug("Next iteration is too close" +
                        " (but not sync). Request a refire at: "
                        + nextIterationTime);
                return false;
            }
            if(Math.abs(_outsideTime - getCurrentTime()) < timeResolution) {
                if(_debugging) _debug("Round up current time " +
                        getCurrentTime() + " to outside time " +_outsideTime);
                setCurrentTime(_outsideTime);
            }
            if (_outsideTime > getCurrentTime()) {
                throw new InvalidStateException(this, exe,
                        "Outside time is later than the CT time. " +
                        "This should never happen in mixed-signal modeling");
            }
            // Check for rollback.
            if (_outsideTime < getCurrentTime()) {
                if(_debugging) _debug(getName() + " rollback from: " +
                        getCurrentTime() + " to: " +_knownGoodTime +
                        "due to outside time " +_outsideTime );
                if(STAT) {
                    _Nroll ++;
                }
                _rollback();
                // Set a catch-up destination time.
                fireAt(null, _outsideTime);
                _catchUp();
            }
            if(aheadLength < _runAheadLength) {
                _setIterationEndTime(nextIterationTime);
            } else {
                _setIterationEndTime(_outsideTime + _runAheadLength );
            }
            // Now it's guaranteed that the current time is the outside time.
            if(_debugging) _debug(getName(), "Iteration end time = " +
                    getIterationEndTime(), "End of Prefire");
        }
        return true;
    }

    /** Return true if it
     *  transfers data from an input port of the container to the
     *  ports it is connected to on the inside.  The port argument must
     *  be an opaque input port.  If any channel of the input port
     *  has no data, then that channel is ignored. The execution
     *  phase is set to event phase if there's any data transferred.
     *  In an event phase, discrete events will be produced or
     *  consumed.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True if there are data transferred.
     */
    public boolean transferInputs(IOPort port)
            throws IllegalActionException {
        boolean transfer = super.transferInputs(port);
        if (transfer) {
            _setEventPhase(true);
        }
        return transfer;
    }

    /** Show the statistics of the simulation if requested and wrapup
     *  call actors. The statistics,
     *  in addition to those in CTDirector, is the number of rollbacks.
     *
     *
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if(STAT) {
            if(_debugging) {
                _debug(getName() + ": Total # of ROLLBACK " + _Nroll);

            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Catch up the simulation from a known good state to the outside
     *  current time. There should be no breakpoints of any kind
     *  in this process. If the current time is greater than or equal
     *  to the outside time, then do nothing.
     *  @exception IllegalActionException If thrown from the execution
     *  methods from any actor.
     */
    protected void _catchUp() throws IllegalActionException {
        if (getCurrentTime() >= getOutsideTime()) {
            return;
        }
        _setIterationBeginTime(getCurrentTime());
        while(getCurrentTime() < getOutsideTime()) {
            setCurrentStepSize(getSuggestedNextStepSize());
            _processBreakpoints();
            _fireOneIteration();
        }
        if(_debugging)
            _debug(getFullName() + " Catch up time" + getCurrentTime());
    }

    /** Initialize parameters in addition to the parameters inherited
     *  from CTMultiSolverDirector. In this class the additional
     *  parameter is the maximum run ahead time length
     *  (<code>runAheadLength</code>). The default value is 1.0.
     */
    protected void _initParameters() {
        super._initParameters();
        try {
            _runAheadLength = 0.1;
            runAheadLength = new Parameter(this,
                    "runAheadLength", new DoubleToken(_runAheadLength));
            runAheadLength.setTypeEquals(BaseType.DOUBLE);
        } catch (IllegalActionException e) {
            //Should never happens. The parameters are always compatible.
            throw new InternalErrorException("Parameter creation error.");
        } catch (NameDuplicationException ex) {
            throw new InvalidStateException(this,
                    "Parameter name duplication.");
        }
    }

    /** Return true if the current phase of fire is an event phase.
     *  @return True if the current phase is an event phase.
     */
    protected boolean _isEventPhase() {
        return _inEventPhase ;
    }

    /** Return true if the current fire phase is stopped due to
     *  the occurrence of events (predictable or unpredictable).
     *  @return True if the current fire phase is stopped by an event.
     */
    protected boolean _isStoppedByEvent() {
        // predictable breakpoints
        double breakpoint;
        TotallyOrderedSet table = getBreakPoints();
        double now = getCurrentTime();
        if(table != null) {
            while (!table.isEmpty()) {
                breakpoint = ((Double)table.first()).doubleValue();
                if(breakpoint < (now-getTimeResolution())) {
                    // The breakpoints in the past or at now.
                    table.removeFirst();
                } else if(Math.abs(breakpoint - now) < getTimeResolution() &&
                        breakpoint < getIterationEndTime()){
                    // break point now! stopped by event
                    return true;
                } else {
                    break;
                }
            }
        }
        // unpredictable breakpoints. Detect current events.
        CTScheduler scheduler = (CTScheduler)getScheduler();
        Iterator generators = scheduler.eventGeneratorList().iterator();
        while(generators.hasNext()) {
            CTEventGenerator generator = (CTEventGenerator)generators.next();
            if(generator.hasCurrentEvent()) {
                return true;
            }
        }
        return false;
    }

    /**Return true if this is a top-level director. This is a syntactic sugar.
     * @return True if this director is at the top-level.
     */
    protected final boolean _isTopLevel() {
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

    /** Mark the current state as the known good state. Call the
     *  markStates() method on all CTStatefulActors. Save the current time
     *  as the "known good" time.
     */
    protected void _markStates() {
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Iterator actors = scheduler.statefulActorList().iterator();
        while(actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor)actors.next();
            if(_debugging) _debug("Save State..."+
                    ((Nameable)actor).getName());
            actor.markState();
        }
        _knownGoodTime = getCurrentTime();
    }

    /** Rollback the system to a "known good" state. All the actors with
     *  states are called to restore their saved states. The
     *  current time of the director is set to the time of the "known
     *  good" state.
     *  @exception IllegalActionException If thrown by the goToMarkedState()
     *       method of an actor.
     */
    protected void _rollback() throws IllegalActionException{
        CTScheduler scheduler = (CTScheduler) getScheduler();
        Iterator actors = scheduler.statefulActorList().iterator();
        while(actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor)actors.next();
            if(_debugging) _debug("Restore State..."+
                    ((Nameable)actor).getName());
            actor.goToMarkedState();
        }
        setCurrentTime(_knownGoodTime);
    }

    /** True argument sets the phase to be an event phase.
     *  @param eventPhase True to set the current phase to an event phase.
     */
    protected void _setEventPhase(boolean eventPhase) {
        _inEventPhase = eventPhase;
    }

    /** Set the end time for this iteration. If the argument is
     *  less than the current time, then an InvalidStateException
     *  will be thrown.
     *  @param The fire end time.
     */
    protected void _setIterationEndTime(double time) {
        if(time < getCurrentTime()) {
            throw new InvalidStateException(this,
                    " Iteration end time" + time + " is less than" +
                    " the current time." + getCurrentTime());
        }
        _iterationEndTime = time;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** The number of rollbacks. Used for statistics.
     */
    protected int _Nroll = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The version of mutation. If this version is not the workspace
    // version then every thing related to mutation need to be updated.
    private long _mutationVersion = -1;

    // Indicate if this is the top level director.
    private boolean _isTop;

    // The time for the "known good" state.
    private double _knownGoodTime;

    // The current outside time.
    private double _outsideTime;

    // The local variable of the run ahead length parameter.
    private double _runAheadLength;

    // The end time of an iteration.
    private double _iterationEndTime;

    // Indicate whether this is an event phase;
    private boolean _inEventPhase = false;
}
