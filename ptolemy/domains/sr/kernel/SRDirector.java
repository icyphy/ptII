/* Director for the Synchronous Reactive model of computation.

 Copyright (c) 2000-2009 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// SRDirector

/**
 A director for the Synchronous Reactive (SR) model of computation.
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
 (by returning false in prefire()), and if it fires, it calls
 fireAt(currentTime + period) in postfire().
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
public class SRDirector extends FixedPointDirector {
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
     *  specified period.
     */
    public Parameter period;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     *  @return Either the requested time or the current time plus the
     *  period.
     */
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        // NOTE: This used to delegate to the enclosing director, but
        // prefire() will refuse to fire at that time if it isn't a multiple of
        // the period. So it is wrong to delegate to the enclosing
        // director. EAL 8/10/09
        Actor container = (Actor) getContainer();
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();
        Time currentTime = getModelTime();
        
        // Check the most common case first.
        if (periodValue == 0.0) {
            if (container != null) {
                Director executiveDirector = container.getExecutiveDirector();
                if (executiveDirector != null) {
                    _recordPendingFireAt(actor, time);
                    return executiveDirector.fireAt(container, time);
                }
            }
            // All subsequent firings will be at the current time.
            _recordPendingFireAt(actor, currentTime);
            return currentTime;
        }
        // Now we know the period is not 0.0.
        // Return current time plus the period,
        // or some multiple of the period.
        // NOTE: this is potentially very expensive to compute precisely
        // because the Time class has an infinite range and only supports
        // precise addition. Determining whether the argument satisfies
        // the criterion seems difficult. Hence, we check to be sure
        // that the test is worth doing.

        // First check to see whether we are in the initialize phase, in
        // which case, return the start time.
        if (container != null) {
            Manager manager = ((CompositeActor) container).getManager();
            if (manager.getState().equals(Manager.INITIALIZING)) {
                _recordPendingFireAt(actor, currentTime);
                return currentTime;
            }
        }
        if (time.isInfinite() || currentTime.compareTo(time) > 0) {
            // Either the requested time is infinite or it is in the past.
            Time result = currentTime.add(periodValue);
            _recordPendingFireAt(actor, result);
            return result;
        }
        Time futureTime = currentTime;
        while (time.compareTo(futureTime) > 0) {
            futureTime = futureTime.add(periodValue);
            if (futureTime.equals(time)) {
                _recordPendingFireAt(actor, time);
                return time;
            }
        }
        Time result = currentTime.add(periodValue);
        _recordPendingFireAt(actor, result);
        return result;
    }

    /** Return the time value of the next iteration.
     *  If this director is at the top level, then the returned value
     *  is the current time plus the period. Otherwise, this method
     *  delegates to the executive director.
     *  @return The time of the next iteration.
     */
    public Time getModelNextIterationTime() {
        if (!_isTopLevel()) {
            return super.getModelNextIterationTime();
        }
        try {
            double periodValue = ((DoubleToken) period.getToken())
                    .doubleValue();

            if (periodValue > 0.0) {
                return getModelTime().add(periodValue);
            } else {
                return _currentTime;
            }
        } catch (IllegalActionException exception) {
            // This should have been caught by now.
            throw new InternalErrorException(exception);
        }
    }

    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method. Reset all private variables.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _nextFiringTime = getModelTime();

        // In case we are embedded within a timed director, request a first
        // firing.
        Director executiveDirector = ((Actor) getContainer())
                .getExecutiveDirector();
        if (executiveDirector != null) {
            executiveDirector.fireAtCurrentTime((Actor) getContainer());
        }
    }

    /** Invoke super.prefire(), which will synchronize to real time, if appropriate.
     *  Then if the <i>period</i> parameter is zero, return whatever the superclass
     *  returns. Otherwise, return true only if the current time of the enclosing
     *  director (if there is one) matches a multiple of the period. If the
     *  current time of the enclosing director exceeds the time at which we
     *  next expected to be invoked, then adjust that time to the least multiple
     *  of the period that either matches or exceeds the time of the enclosing
     *  director.
     *  @exception IllegalActionException If port methods throw it.
     *  @return true If all of the input ports of the container of this
     *  director have enough tokens.
     */
    public boolean prefire() throws IllegalActionException {
        
        boolean result = super.prefire();
                
        // If the superdense time index is zero
        // then either this is the first iteration, or
        // it was set to zero in the last call to postfire().
        // In the latter case, we expect to be invoked at
        // the previous time plus the non-zero period.
        // If the enclosing time does not match or exceed
        // that (the latter could occur if we have been
        // dormant in a modal model), then return false.
        // If the enclosing time exceeds the local current
        // time, then we must have been dormant. We need to
        // catch up.
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();
        if (periodValue > 0.0) {
            Actor container = (Actor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            // If we are at the top level, then this check is not necessary
            // since presumably we will be assured that current time matches
            // the period.
            if (executiveDirector != null) {
                // Not at the top level.
                Time enclosingTime = executiveDirector.getModelTime();
                int comparison = _nextFiringTime.compareTo(enclosingTime);
                if (comparison == 0) {
                    // The enclosing time matches the time we expect to fire.
                    // If the enclosing director supports superdense time, then
                    // make sure we are at index zero before agreeing to fire.
                    if (executiveDirector instanceof SuperdenseTimeDirector) {
                        int index = ((SuperdenseTimeDirector) executiveDirector)
                                .getIndex();
                        if (index == 0) {
                            _updatePendingFireAts(enclosingTime);
                            return result;
                        }
                        // If the index is not zero, do not agree to fire, but request
                        // a refiring at the next multiple of the period.
                        _nextFiringTime = _nextFiringTime.add(periodValue);
                        _fireContainerAt(_nextFiringTime);
                        return false;
                    }
                } else if (comparison > 0) {
                    // Enclosing time has not yet reached our expected firing time.
                    // No need to call fireAt(), since presumably we already
                    // did that in postfire().
                    return false;
                } else {
                    // Enclosing time has exceeded our expected firing time.
                    // We must have been dormant in a modal model.
                    // Catch up.
                    while (_nextFiringTime.compareTo(enclosingTime) < 0) {
                        // FIXME: Any enclosed actors that called fireAt() need
                        // to be notified if their requested time has been
                        // skipped by calling fireAtSkipped.
                        _nextFiringTime = _nextFiringTime.add(periodValue);
                    }
                    if (_nextFiringTime.compareTo(enclosingTime) == 0) {
                        // The caught up time matches a multiple of the period.
                        // If the enclosing director supports superdense time, then
                        // make sure we are at index zero before agreeing to fire.
                        if (executiveDirector instanceof SuperdenseTimeDirector) {
                            int index = ((SuperdenseTimeDirector) executiveDirector)
                                    .getIndex();
                            if (index == 0) {
                                _updatePendingFireAts(enclosingTime);
                                return result;
                            }
                            // If the index is not zero, do not agree to fire, but request
                            // a refiring at the next multiple of the period.
                            _nextFiringTime = _nextFiringTime.add(periodValue);
                            _fireContainerAt(_nextFiringTime);
                            return false;
                        }
                        // If the enclosing director does not support superdense time,
                        // then agree to fire.
                        _updatePendingFireAts(enclosingTime);
                        return result;
                    } else {
                        // NOTE: The following throws an exception if the time
                        // requested cannot be honored by the enclosed director
                        // Presumably, if the user set the period, he or she wanted
                        // that behavior.
                        _fireContainerAt(_nextFiringTime);
                        return false;
                    }
                }
            }
        }
        // If period is zero, then just return the superclass result.
        _updatePendingFireAts(getModelTime());
        return result;
    }

    /** Call postfire() on all contained actors that were fired on the last
     *  invocation of fire().  If <i>synchronizeToRealTime</i> is true, then
     *  wait for real time elapse to match or exceed model time. Return false if the model
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
     *  future.
     *  @exception IllegalActionException If the iterations parameter
     *  does not contain a legal value.
     */
    public boolean postfire() throws IllegalActionException {
        // The super.postfire() method increments the superdense time index.
        boolean result = super.postfire();
        double periodValue = ((DoubleToken) period.getToken()).doubleValue();
        if (periodValue > 0.0) {
            Actor container = (Actor) getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            Time currentTime = getModelTime();
            _nextFiringTime = currentTime.add(periodValue);

            if (executiveDirector != null) {
                // Not at the top level.
                // NOTE: The following throws an exception if the time
                // requested cannot be honored by the enclosed director
                // Presumably, if the user set the period, he or she wanted
                // that behavior.
                _fireContainerAt(_nextFiringTime);
            } else {
                // Increment time to the next cycle.
                _currentTime = _nextFiringTime;
            }
            // Set the index to zero because the next firing will occur at
            // a strictly greater time.
            _index = 0;
        }
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
    }

    /** Record a pending fireAt().
     *  @param actor The actor requesting a firing.
     *  @param time The time returned by fireAt().
     */
    private void _recordPendingFireAt(Actor actor, Time time) {
        if (_pendingFireAts == null) {
            _pendingFireAts = new TreeMap<Time, Set<Actor>>();
        }
        Set<Actor> pending = _pendingFireAts.get(time);
        if (pending == null) {
            pending = new HashSet<Actor>();
        }
        pending.add(actor);
    }

    /** Notify any actors expecting fireAt() calls if the time
     *  they were told they would fire is in the past compared
     *  to the argument. Remove all pending fireAt() records
     *  up to and including the specified time.
     *  @param time The time returned by fireAt().
     *  @throws IllegalActionException If a notified actor throws it.
     */
    private void _updatePendingFireAts(Time time) throws IllegalActionException {
        if (_pendingFireAts != null && !_pendingFireAts.isEmpty()) {
            Time firstKey = _pendingFireAts.firstKey();
            while(firstKey != null && firstKey.compareTo(time) < 0) {
                Set<Actor> actors = _pendingFireAts.remove(firstKey);
                if (actors != null) {
                    for (Actor actor : actors) {
                        actor.fireAtSkipped(firstKey);
                    }
                }
            }
            _pendingFireAts.remove(time);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The expected next firing time. */
    private Time _nextFiringTime;
    
    /** Pending fireAt() calls, in case we have to call fireAtSkipped(). */
    private TreeMap<Time, Set<Actor>> _pendingFireAts;
}
