/* Director for the Synchronous Reactive model of computation.

 Copyright (c) 2000-2014 The Regents of the University of California.
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

 */
package ptolemy.domains.sr.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.PeriodicDirector;
import ptolemy.actor.util.PeriodicDirectorHelper;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SRDirector

/**
 A director for the Synchronous Reactive (SR) model of computation.
 The SR model of computation has a notion of a global "tick" of a
 clock, and at each tick of the clock, each port either has a value
 or is "absent." The job of this director is determine what that
 value is for each connection between ports. An iteration of this
 director is one tick of this global clock.
 <p>
 Execution proceeds as follows. The director checks
 each actor to determine whether it is <i>strict</i> or not by calling
 its isStrict() method (here, "strict" means that all inputs
 must be known before the actor can specify any outputs).
 By default, actors are strict. Strict actors are fired only
 once in an iteration. Their inputs are all known (and may
 absent) when prefire() is invoked. If prefire() returns true,
 the fire() and postfire() are invoked exactly once.
 <p>
 Specialized actors may be non-strict, meaning that they are able
 to produce outputs even their inputs are not known. Such actors
 must conform to certain requirements in order to ensure determinacy.
 First, such actors should check input ports by calling their
 isKnown() method before calling hasToken() to determine whether
 the port is "absent." Only if both isKnown() and hasToken() return
 true should the actor call get() on that port.
 A non-strict actor may be prefired and fired
 repeatedly in an iteration if some of the inputs are unknown.
 Once an actor is fired with all its inputs known, it will not
 be fired again in the same iteration.
 A composite actor containing this director is a non-strict actor.
 <p>
 Each actor's fire() method implements a (possibly state-dependent)
 function from input ports to output ports. At each tick of the
 clock, the fire() method of each non-strict actor may be evaluated multiple
 times, and each time, it <i>must</i> implement the same function.
 Thus, the actors are required to conform with the strict actor
 semantics, which means that they do not change their state in
 the prefire() or fire() methods, and only change their state
 in postfire(). This helps ensure that the actor is <i>monotonic</i>.
 Montonicity implies three constraints on the actor. First, if prefire()
 ever returns true during an iteration, then it will return true
 on all subsequent invocations of prefire() in the same iteration().
 In subsequent iterations, inputs may become known, but once they
 are known, the value of the input and whether it is present cannot
 change in subsequent firings in the same iteration.
 Second, if either prefire() or fire() call clear() on an output port,
 then no subsequent invocation in the same iteration can call
 put() on the port. Third, if prefire() or fire() call put() on an
 output port with some token, then no subsequent invocation in
 the same iteration can call clear() or put() with a token with
 a different value.
 These constraints ensure determinacy.
 </p><p>
 If <i>synchronizeToRealTime</i> is set to <code>true</code>,
 then the postfire() method stalls until the real time elapsed
 since the model started matches the current time.
 This ensures that the director does not get ahead of real time. However,
 of course, this does not ensure that the director keeps up with real time.
 Note that this synchronization occurs <i>after</i> actors have been fired,
 but before they have been postfired.
 <p>
 The SR director has a <i>period</i> parameter which specifies the
 amount of model time that elapses per iteration. If the value of
 <i>period</i> is 0.0 (the default), then it has no effect, and
 this director never increments time nor calls fireAt() on the
 enclosing director. If the period is greater than 0.0, then
 if this director is at the top level, it increments
 time by this amount in each invocation of postfire().
 If it is not at the top level, then it refuses to fire
 at times that do not match a multiple of the <i>period</i>
 (by returning false in prefire()).
 If it fires, then in postfire() it calls
 fireAt(currentTime + period) in postfire() to request
 the next firing.
  </p><p>
 Note that currentTime, as usual, means local time.
 If this director is inside a modal model, then that current
 time may lag behind the time of the environment in which
 the modal model executes because time does not elapse while
 a mode is inactive. In fact, time may bounce around, since
 if a <i>startTime</i> is specified and the modal model has
 a reset transition, then local time may be reinitialized
 to the start time during execution. Moreover, if the
 <i>startTime</i> of this director inside a modal is greater
 than the start time of the enclosing director, then local
 time may actually be <i>ahead</i> of environment time.
 </p><p>
 This behavior gives an interesting use of SR within DE or
 Continuous. In particular, if set a period other than 0.0,
 the composite actor with this SR director will fire periodically
 with the specified period.
 </p><p>
 If <i>period</i> is greater than 0.0 and the parameter
 <i>synchronizeToRealTime</i> is set to <code>true</code>,
 then the prefire() method stalls until the real time elapsed
 since the model started matches the period multiplied by
 the iteration count.
 This ensures that the director does not get ahead of real time. However,
 of course, this does not ensure that the director keeps up with real time.

 @author Paul Whitaker, Edward A. Lee, Contributor: Ivan Jeukens, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (pwhitake)
 @Pt.AcceptedRating Green (pwhitake)
 */
public class SRDirector extends FixedPointDirector implements PeriodicDirector {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SRDirector() throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public SRDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public SRDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The time period of each iteration.  This parameter has type double
     *  and default value 0.0, which means that this director does not
     *  increment model time and does not request firings by calling
     *  fireAt() on any enclosing director.  If the value is set to
     *  something greater than 0.0, then if this director is at the
     *  top level, it will increment model time by the specified
     *  amount in its postfire() method. If it is not at the top
     *  level, then it will call fireAt() on the enclosing executive
     *  director with the argument being the current time plus the
     *  specified period.  In addition,
     *  if it is not at the top level, then it refuses to fire
     *  at times that do not match a multiple of the <i>period</i>
     *  (by returning false in prefire()).
     */
    public Parameter period;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SRDirector newObject = (SRDirector) super.clone(workspace);
        try {
            newObject._periodicDirectorHelper = new PeriodicDirectorHelper(
                    newObject);
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException("Failed to clone helper: " + e);
        }
        return newObject;
    }

    /** Request a firing of the given actor at the given absolute
     *  time, and return the time at which the specified will be
     *  fired. If the <i>period</i> is 0.0 and there is no enclosing
     *  director, then this method returns the current time. If
     *  the period is 0.0 and there is an enclosing director, then
     *  this method delegates to the enclosing director, returning
     *  whatever it returns. If the <i>period</i> is not 0.0, then
     *  this method checks to see whether the
     *  requested time is equal to the current time plus an integer
     *  multiple of the period. If so, it returns the requested time.
     *  If not, it returns current time plus the period.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @param microstep The microstep (ignored by this director).
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     *  @return Either the requested time or the current time plus the
     *  period.
     */
    @Override
    public Time fireAt(Actor actor, Time time, int microstep)
            throws IllegalActionException {
        return _periodicDirectorHelper.fireAt(actor, time);
    }

    /** Return the time value of the next iteration.
     *  If this director is at the top level, then the returned value
     *  is the current time plus the period. Otherwise, this method
     *  delegates to the executive director.
     *  @return The time of the next iteration.
     *  @exception IllegalActionException If time objects cannot be created.
     */
    @Override
    public Time getModelNextIterationTime() throws IllegalActionException {
        if (!_isTopLevel()) {
            return super.getModelNextIterationTime();
        }
        try {
            double periodValue = periodValue();

            if (periodValue > 0.0) {
                return getModelTime().add(periodValue);
            } else {
                return getModelTime();
            }
        } catch (IllegalActionException exception) {
            // This should have been caught by now.
            throw new InternalErrorException(exception);
        }
    }

    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method.
     *  If the <i>period</i> parameter is greater than zero, then
     *  request a first firing of the executive director, if there
     *  is one.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _periodicDirectorHelper.initialize();

        // In preinitialize(), _startTime was set to the start time of
        // the executive director. However, this invocation of initialize()
        // may be occurring later in the execution of a model, as a result
        // for example of a reset transition. In that case, _startTime
        // may be in the past relative to the environment time.
        // The super.initialize() call above should have set our
        // current time to match the environment time, so we use
        // the maximum of _startTime and the current time as the
        // first firing time.
        // NOTE: If we are being reinitialized during execution (e.g. because
        // of a reset transition), then our current local time is set to match
        // the environment time if startTime is blank (the default), meaning that
        // start time is inherited from the environment. Otherwise, the accumulated
        // suspend time should be set to the difference between the current time
        // of the environment and the start time. In either case, this is the
        // difference between the current time and the environment time.
        Time startTime = getModelStartTime();
        Time currentTime = getModelTime();
        if (startTime.compareTo(currentTime) >= 0) {
            fireContainerAt(startTime, 1);
        } else {
            // Use a microstep of 1 here on the assumption
            // that initialization could create discontinuities.
            fireContainerAt(currentTime, 1);
        }
    }

    /** Return the value of the period as a double.
     *  @return The value of the period as a double.
     *  @exception IllegalActionException If the period parameter
     *   cannot be evaluated
     */
    @Override
    public double periodValue() throws IllegalActionException {
        return ((DoubleToken) period.getToken()).doubleValue();
    }

    /** Return true if we are ready to fire, meaning that either (1) we are
     *  at the top level, (2) the <i>period</i> parameter is zero (in which case we
     *  are always ready to fire, or (3) the <i>period</i> parameter is nonzero,
     *  the current time matches the next expected execution time, and the
     *  current microstep is 1. If this actor is enclosed within a director
     *  that does not understand microsteps, then we ignore the microstep value.
     *  This method will also synchronize to real time, if appropriate.
     *  @exception IllegalActionException If the <i>period</i>
     *   parameter cannot be evaluated.
     *  @return true If current time is appropriate for a firing.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // The first call below sets current time to match the environment
        // time. The second call, _periodicDirectorHelper.prefire(), checks
        // to see whether current time is the next expected firing time.
        boolean result = super.prefire() && _periodicDirectorHelper.prefire();

        // If we are going to fire, then set the microstep to match that
        // of the enclosing director, or set it to 1 if the enclosing
        // director does not understand superdense time.
        if (result && isEmbedded()) {
            Nameable container = getContainer();
            if (container instanceof CompositeActor) {
                Director executiveDirector = ((CompositeActor) container)
                        .getExecutiveDirector();
                if (isEmbedded()
                        && executiveDirector instanceof SuperdenseTimeDirector) {
                    _index = ((SuperdenseTimeDirector) executiveDirector)
                            .getIndex();
                } else {
                    _index = 1;
                }
                if (_debugging) {
                    _debug("SRDirector: Set microstep to " + _index);
                }
            } else {
                // No container. This should not happen.
                throw new IllegalActionException(this,
                        "SRDirector needs a CompositeActor container to execute");
            }
        } else {
            // If either we are not going to fire or are at the top level,
            // set the microstep to 1.
            _index = 1;
        }

        if (_debugging) {
            _debug("Prefire returns " + result);
        }
        return result;
    }

    /** Call postfire() on all contained actors that were fired on the last
     *  invocation of fire().  Return false if the model
     *  has finished executing, either by reaching the iteration limit, or if
     *  no actors in the model return true in postfire(), or if stop has
     *  been requested. This method is called only once for each iteration.
     *  Note that actors are postfired in arbitrary order.
     *  <p>
     *  If the <i>period</i> parameter is greater than 0.0, then
     *  if this director is at the top level, then increment time
     *  by the specified period, and otherwise request a refiring
     *  at the current time plus the period.
     *  @return True if the Director wants to be fired again in the
     *   future.
     *  @exception IllegalActionException If the iterations or
     *   period parameter does not contain a legal value.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // The super.postfire() method increments the superdense time index.
        boolean result = super.postfire();
        _periodicDirectorHelper.postfire();
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler, an iterations
     *  parameter and a vectorizationFactor parameter.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        period = new Parameter(this, "period");
        period.setTypeEquals(BaseType.DOUBLE);
        period.setExpression("0.0");

        _periodicDirectorHelper = new PeriodicDirectorHelper(this);

        // Put the stopTime at the end parameters, to encourage use
        // of iterations instead.
        stopTime.moveToLast();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Helper class supporting the <i>period</i> parameter. */
    private PeriodicDirectorHelper _periodicDirectorHelper;
}
