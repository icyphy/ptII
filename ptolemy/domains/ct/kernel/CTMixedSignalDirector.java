/* A CT Director that handles the interaction with event based domains.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.DoubleToken;
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
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// CTMixedSignalDirector
/**
This is a CTDirector that supports the interaction of the continuous-time
simulation with event-based domains. This director can both serve as
a top-level director and an inside director that is contained by
a composite actor in an event-based domain. If it is a top-level
director, it acts exactly like a CTMultiSolverDirector. If it is
embedded in another event-based domain, it will run ahead of the global
time and prepare to roll back if necessary.
<P>
This class has an extra parameter as compare to the CTMultiSolverDirector,
which is the maximum run ahead of time length (<code>runAheadLength</code>).
The default value is 1.0.
<P>
The running ahead of time is achieved by the following mechanism.<br>
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
@since Ptolemy II 0.2
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
     *  @param container The container.
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
     *  @param attribute The changed attribute.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == runAheadLength) {
            if (_debugging) _debug("run ahead length updating.");
            double value =
                ((DoubleToken)runAheadLength.getToken()).doubleValue();
            if (value < 0) {
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
        if (_isTopLevel()) {
            super.fire();
            return;
        }
        CompositeActor container = (CompositeActor) getContainer();
        Director exe = container.getExecutiveDirector();
        // Allow waveform generators to consume events if there is any.
        _setDiscretePhase(true);
        Iterator waveGenerators = getScheduler().getSchedule().get(
                CTSchedule.WAVEFORM_GENERATORS).actorIterator();
        while (waveGenerators.hasNext() && !_stopRequested) {
            CTWaveformGenerator generator =
                (CTWaveformGenerator) waveGenerators.next();
            if (_debugging) {
                _debug("Prefire generator actor: "
                        + ((Nameable)generator).getName()
                        + " at time "
                        + getCurrentTime());
            }
            if (generator.prefire()) {
                if (_debugging) {
                    _debug("Fire generator actor: "
                            + ((Nameable)generator).getName()
                            + " at time "
                            + getCurrentTime());
                }
                generator.fire();
                if (_debugging) {
                    _debug("Postfire generator actor: "
                            + ((Nameable)generator).getName()
                            + " at time "
                            + getCurrentTime());
                }
                _postfireReturns = _postfireReturns && generator.postfire();
            }
        }
        _setDiscretePhase(false);
        while (!_stopRequested) {
            if (isBreakpointIteration()) {
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
            if (_debugging) _debug(getName(),
                    "Resolved stepsize: " + getCurrentStepSize() +
                    " One iteration from " + getCurrentTime() +
                    " to " + (getCurrentStepSize()+getCurrentTime()));
            _fireOneIteration();
            if (_isStoppedByEvent()) {
                if (_debugging) {
                    _debug("Fire stopped by event."
                            + " at " + getCurrentTime()
                            + "; request refire at "
                            + getCurrentTime()
                            + "; set Event phase to TRUE");
                }
                _hasDiscreteEvents = true;
                //hold Outputs;
                exe.fireAt(container, getCurrentTime());
                return;
            } else if (Math.abs(getCurrentTime()- getIterationEndTime())
                    < getTimeResolution()) {
                if (_debugging) {
                    _debug("Fire stopped normally."
                            + " at " + getCurrentTime()
                            + "; request refire at "
                            + getIterationEndTime()
                            + "; set Event phase to FALSE");
                }
                _hasDiscreteEvents = false;
                exe.fireAt(container, getIterationEndTime());
                return;
            }
        }
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
        if (_isTopLevel()) {
            return getCurrentTime();
        }
        return _outsideTime;
    }

    /** First initialize the execution as in CTMultiSolverDirector.
     *  If this director is not at the top-level, also
     *  request to fire at the current time from the executive director.
     *  @see CTMultiSolverDirector#initialize()
     *  @exception IllegalActionException If this director has no container or
     *       no scheduler, or thrown by a contained actor.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) _debug(getFullName() + " initialize.");
        super.initialize();
        if (!_isTopLevel()) {
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
        if (_isTopLevel()) {
            return super.postfire();
        } else {
            _secondPrefire = false;
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
        if (_debugging) _debug(getName(), " prefire: ");
        if (!_isTopLevel()) {
            // synchronize time.
            CompositeActor container = (CompositeActor) getContainer();
            // ca should have beed checked in _isTopLevel()
            Director exe = container.getExecutiveDirector();
            _outsideTime = exe.getCurrentTime();
            double timeResolution = getTimeResolution();
            double nextIterationTime = exe.getNextIterationTime();

            if (_debugging) _debug("Outside time is " + _outsideTime,
                    "\nNext iteration time is " + nextIterationTime,
                    "\nCurrent local time is " + getCurrentTime());

            // Now, check the next iteration time.
            if (nextIterationTime < _outsideTime) {
                throw new InvalidStateException(this, "Outside domain"
                        + " time is going backward."
                        + " Current outside time = " + _outsideTime
                        + ", but the next iteration time = "
                        + nextIterationTime);
            }

            // If the outside time and the next iteration time are so close
            // that the difference is less than the time resolution, then
            // we simply omit this firing and refire at the next iteration
            //time.
            // If outside next iteration time is equal to the outside
            // time, then request for a zero delay refire.
            // Notice that DE will post this zero delay refire to be the
            // last one with the same time stamp. So we expect that
            // next time we wake up, the next iteration time is not
            // the same as the current time.
            // But there exists an additional subtlety, which is that
            // we can have more than two CT composite actors in DE.
            // So we need to check that after the second time we wake
            // up at the same outside time, we should proceed anyway.

            // FIXME: need to double check stop time.
            if ((nextIterationTime - _outsideTime) < timeResolution
                    && (_secondPrefire == false)) {
                exe.fireAt(container, nextIterationTime);
                _secondPrefire = true;
                return false;
            }

            // Ideally, the outside time should equal to the current
            // local time. If the outside time is less than the local
            // time, then rollback is needed. If the outside time
            // is greater than the local time, we will complain.
            if (Math.abs(_outsideTime - getCurrentTime()) < timeResolution) {
                // We are woke up as we requested.
                // Roundup the current time to the outside time
                if (_debugging) _debug("Outside time is the current time.",
                        " So we check whether there are outputs.");
                _currentTime = _outsideTime;
                // Process local discrete events and emit outputs
                // if there are any. If there are any outputs emitted,
                // request for a zero delay refire and return false.
                if (_hasDiscreteEvents) {
                    _discretePhaseExecution();
                    boolean hasOutput = false;
                    Iterator outports = container.outputPortList().iterator();
                    while (outports.hasNext()) {
                        IOPort p = (IOPort)outports.next();
                        if (exe.transferOutputs(p)) {
                            hasOutput = true;
                        }
                    }
                    _hasDiscreteEvents = false;
                    if (hasOutput) {
                        if (_debugging) _debug(getName(),
                                " produces output to the outside domain.",
                                " Requesting zero delay refiring",
                                " Prefire() returns false.");
                        exe.fireAt(container, _outsideTime);
                        return false;
                    }
                }
            } else if (_outsideTime > getCurrentTime()) {
                throw new InvalidStateException(this, exe,
                        "Outside time is later than the CT time. " +
                        "This should never happen in mixed-signal modeling");
            } else if (_outsideTime < getCurrentTime()) {

                // Outside time less than the local time. Rollback!
                if (_debugging) _debug(getName() + " rollback from: " +
                        getCurrentTime() + " to: " +_knownGoodTime +
                        "due to outside time " +_outsideTime );
                _rollback();
                // Set a catch-up destination time.
                fireAt(null, _outsideTime);
                _catchUp();
                if (_debugging) _debug("After catch up, the current time is "
                        + getCurrentTime());
            }

            // Now, we have outside time equals to the curren time,
            // and there are no discrete events. So we consider
            // how far we should run ahead of the outside time.


            // Now, either this the second time that we wake up,
            // or we have a none zero run-ahead-time.
            double aheadLength = nextIterationTime - _outsideTime;
            if (_debugging) _debug(getName(),
                    " current time = " + getCurrentTime(),
                    " Outside Time = " + _outsideTime,
                    " NextIterationTime = " + nextIterationTime +
                    " Inferred run length = " + aheadLength);

            if (aheadLength < timeResolution ) {
                // We should use the runAheadLength parameter.
                _setIterationEndTime(_outsideTime + _runAheadLength);
                if (_debugging) _debug( "Outside next iteration length",
                        " is zero. We proceed any way with length "
                        + _runAheadLength);
            } else if (aheadLength < _runAheadLength) {
                _setIterationEndTime(nextIterationTime);
            } else {
                // aheadLength > _runAheadLength parameter.
                _setIterationEndTime(_outsideTime + _runAheadLength );
            }
            // Now it is safe to execute the continuous part.
            if (_debugging) _debug(getName(), "Iteration end time = " +
                    getIterationEndTime(), "End of Prefire");
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Catch up the simulation from a known good state to the outside
     *  current time. There should be no breakpoints of any kind
     *  in this process. If the current time is greater than or equal
     *  to the outside time, then do nothing.
     *  @exception IllegalActionException If thrown from the execution
     *  methods from any actor.
     */
    protected void _catchUp() throws IllegalActionException {
        double outsideTime = getOutsideTime();
        if (getCurrentTime() >= outsideTime) {
            return;
        }
        _setIterationBeginTime(getCurrentTime());
        while (getCurrentTime() < (outsideTime - getTimeResolution())) {
            setCurrentStepSize(getSuggestedNextStepSize());
            _processBreakpoints();
            if (_debugging) _debug("Catch up: ending..." +
                    (getCurrentTime() + getCurrentStepSize()));
            _fireOneIteration();
            if (_debugging) _debug("Catch up one step: current time is"
                    + getCurrentTime());
        }
        _currentTime = outsideTime;
        if (_debugging)
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

    /** Return true if the current fire phase is stopped due to
     *  the occurrence of events (predictable or unpredictable).
     *  @return True if the current fire phase is stopped by an event.
     *  @exception IllegalActionException If thrown by the scheduler.
     */
    protected boolean _isStoppedByEvent() throws IllegalActionException {
        // predictable breakpoints
        double breakpoint;
        TotallyOrderedSet table = getBreakPoints();
        double now = getCurrentTime();
        if (table != null) {
            while (!table.isEmpty()) {
                breakpoint = ((Double)table.first()).doubleValue();
                if (breakpoint < (now-getTimeResolution())) {
                    // The breakpoints in the past or at now.
                    table.removeFirst();
                } else if (Math.abs(breakpoint - now) < getTimeResolution() &&
                        breakpoint < getIterationEndTime()){
                    // break point now! stopped by event
                    return true;
                } else {
                    break;
                }
            }
        }
        // unpredictable breakpoints. Detect current events.
        CTSchedule schedule = (CTSchedule)getScheduler().getSchedule();
        Iterator generators = schedule.get(
                CTSchedule.EVENT_GENERATORS).actorIterator();
        while (generators.hasNext()) {
            CTEventGenerator generator = (CTEventGenerator)generators.next();
            if (generator.hasCurrentEvent()) {
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
            if (container.getExecutiveDirector() == null) {
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
     *  @exception IllegalActionException If thrown by the scheduler.
     */
    protected void _markStates() throws IllegalActionException {
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.STATEFUL_ACTORS).actorIterator();
        while (actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor)actors.next();
            if (_debugging) _debug("Save State..."+
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
        CTSchedule schedule = (CTSchedule) getScheduler().getSchedule();
        Iterator actors = schedule.get(
                CTSchedule.STATEFUL_ACTORS).actorIterator();
        while (actors.hasNext()) {
            CTStatefulActor actor = (CTStatefulActor)actors.next();
            if (_debugging) _debug("Restore State..."+
                    ((Nameable)actor).getName());
            actor.goToMarkedState();
        }
        _currentTime = _knownGoodTime;
    }

    /** True argument sets the phase to be an event phase.
     *  @param eventPhase True to set the current phase to an event phase.
     *
     protected void _setEventPhase(boolean eventPhase) {
     _inEventPhase = eventPhase;
     }*/

    /** Set the end time for this iteration. If the argument is
     *  less than the current time, then an InvalidStateException
     *  will be thrown.
     *  @param time The fire end time.
     */
    protected void _setIterationEndTime(double time) {
        if (time < getCurrentTime()) {
            throw new InvalidStateException(this,
                    " Iteration end time" + time + " is less than" +
                    " the current time." + getCurrentTime());
        }
        _iterationEndTime = time;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The number of rollbacks. Used for statistics.
     */
    protected int _Nroll = 0;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The held output tokens. It maps receivers to the tokens they
    // contains.
    // private HashMap _heldTokens = new HashMap();

    // Indicate whether there are pending discrete events.
    private boolean _hasDiscreteEvents;

    // Indicate whether this is the second time that prefire has been
    // called in a row.
    private boolean _secondPrefire = false;

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
    //private boolean _inEventPhase = false;
}
