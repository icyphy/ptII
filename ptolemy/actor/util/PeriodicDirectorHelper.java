/* Helper class containing utility methods for directors with a period parameter.

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

package ptolemy.actor.util;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// PeriodicDirectorHelper

/**
 This is a helper class for directors implementing PeriodicDirector.
 It collects common functionality to avoid code duplication.

 @see PeriodicDirector
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class PeriodicDirectorHelper {

    /** Construct a new helper.
     *  @param director The associated director.
     *  @exception IllegalActionException If the argument is not an instance of
     *   Director.
     */
    public PeriodicDirectorHelper(PeriodicDirector director)
            throws IllegalActionException {
        if (!(director instanceof Director)) {
            throw new IllegalActionException(director,
                    "Helper must be passed a Director");
        }
        _director = director;
    }

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
     *  If not, it returns the earliest future time that exceeds the
     *  requested time.
     *  @param actor The actor scheduled to be fired.
     *  @param time The requested time.
     *  @exception IllegalActionException If the operation is not
     *    permissible (e.g. the given time is in the past).
     *  @return Either the requested time or the current time plus the
     *   period or whatever the enclosing director returns.
     */
    public Time fireAt(Actor actor, Time time) throws IllegalActionException {
        // NOTE: It is not correct to just delegate to the enclosing director, because
        // prefire() will refuse to fire at that time if it isn't a multiple of
        // the period.
        Actor container = (Actor) _director.getContainer();
        double periodValue = _director.periodValue();
        Time currentTime = ((Director) _director).getModelTime();

        // Check the most common case first.
        if (periodValue == 0.0) {
            if (container != null) {
                Director executiveDirector = container.getExecutiveDirector();
                // Some composites, such as RunCompositeActor want to be treated
                // as if they are at the top level even though they have an executive
                // director, so be sure to check _isTopLevel().
                if (executiveDirector != null && _director.isEmbedded()) {
                    return executiveDirector.fireAt(container, time);
                }
            }
            // All subsequent firings will be at the current time.
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
                return currentTime;
            }
        }
        if (time.isInfinite() || currentTime.compareTo(time) > 0) {
            // Either the requested time is infinite or it is in the past.
            Time result = currentTime.add(periodValue);
            return result;
        }
        Time futureTime = currentTime;
        while (time.compareTo(futureTime) > 0) {
            futureTime = futureTime.add(periodValue);
            if (futureTime.equals(time)) {
                return time;
            }
        }
        Time result = futureTime;
        return result;
    }

    /** If the <i>period</i> parameter is greater than zero, then
     *  request a first firing of the executive director, if there
     *  is one.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        if (_director.periodValue() > 0.0) {
            _nextFiringTime = ((Director) _director).getModelTime();

            // In case we are embedded within a timed director, request a first
            // firing.
            Actor container = (Actor) _director.getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            // Some composites, such as RunCompositeActor want to be treated
            // as if they are at the top level even though they have an executive
            // director, so be sure to check _isTopLevel().
            if (executiveDirector != null && _director.isEmbedded()) {
                executiveDirector.fireAtCurrentTime(container);
            }
        }
    }

    /** If the <i>period</i> parameter is greater than 0.0, then
     *  if the associated director is at the top level, then increment
     *  its time by the specified period, and otherwise request a refiring
     *  at the current time plus the period.
     *  @exception IllegalActionException If the <i>period</i> parameter
     *   cannot be evaluated.
     */
    public void postfire() throws IllegalActionException {
        double periodValue = _director.periodValue();
        if (periodValue > 0.0) {
            Actor container = (Actor) _director.getContainer();
            Director executiveDirector = container.getExecutiveDirector();
            Time currentTime = ((Director) _director).getModelTime();
            _nextFiringTime = currentTime.add(periodValue);

            // Some composites, such as RunCompositeActor want to be treated
            // as if they are at the top level even though they have an executive
            // director, so be sure to check _isTopLevel().
            if (executiveDirector != null && _director.isEmbedded()) {
                // Not at the top level.
                // NOTE: The following throws an exception if the time
                // requested cannot be honored by the enclosed director
                // Presumably, if the user set the period, he or she wanted
                // that behavior.
                _fireContainerAt(_nextFiringTime);
            } else {
                // Increment time to the next cycle.
                ((Director) _director).setModelTime(_nextFiringTime);
            }
            // Set the microstep to 1 for the next firing
            // because the next firing will occur at
            // a strictly greater time, and the microstep is always
            // 1 when this director fires.
            if (_director instanceof SuperdenseTimeDirector) {
                ((SuperdenseTimeDirector) _director).setIndex(1);
            }
        }
    }

    /** If the <i>period</i> value is greater than zero, then return
     *  true if the current time is a multiple of the value and the
     *  current microstep is 1. The associated director expects
     *  to always be fired at microstep 1.  If there is an enclosing
     *  director that does not understand superdense time, then we
     *  ignore that microstep and agree to fire anyway. This means
     *  simply that we will fire the first time that current time
     *  matches a multiple of the period.
     *  @exception IllegalActionException If the <i>period</i>
     *   parameter cannot be evaluated.
     *  @return true If either the <i>period</i> has value 0.0 or
     *   the current time is a multiple of the period.
     */
    public boolean prefire() throws IllegalActionException {
        // If the superdense time index is zero
        // then either this is the first iteration, or
        // it was set to zero in the last call to postfire().
        // In the latter case, we expect to be invoked at
        // the previous time plus the non-zero period.
        // If the enclosing time does not match or exceed
        // that (the latter could occur if we have been
        // dormant in an old-style modal model), then return false.
        // If the enclosing time exceeds the local current
        // time, then we must have been dormant. We need to
        // catch up.
        double periodValue = _director.periodValue();
        if (periodValue > 0.0) {
            Time enclosingTime = ((Director) _director).getModelTime();
            int comparison = _nextFiringTime.compareTo(enclosingTime);
            if (comparison == 0) {
                // The enclosing time matches the time we expect to fire.
                // If either these is no enclosing director or it does
                // not understand superdense time, then we ignore the
                // microstep. Otherwise, we insist that it be 1 in order
                // to fire.
                Director executiveDirector = ((Actor) _director.getContainer())
                        .getExecutiveDirector();
                // Some composites, such as RunCompositeActor want to be treated
                // as if they are at the top level even though they have an executive
                // director, so be sure to check _isTopLevel().
                if (executiveDirector instanceof SuperdenseTimeDirector
                        && _director.isEmbedded()) {
                    int index = ((SuperdenseTimeDirector) executiveDirector)
                            .getIndex();
                    // NOTE: Normally, we expect the index to be 1 for a discrete
                    // event, but it could be greater than 1.
                    // E.g., if a destination mode contains a DE system with the period
                    // parameter set to something non-zero, then it will want to fire
                    // at the time that the transition is taken, but the microstep will
                    // be 2, not 1, because the transition is taken in microstep 1.
                    if (index < 1) {
                        // No need to call fireContainerAt() because
                        // presumably we already did that.
                        return false;
                    }
                    return true;
                }
            } else if (comparison > 0) {
                // If the enclosing director is a Ptides director, firing out of timestamp
                // order is possible, thus return true here.
                CompositeActor container = (CompositeActor) _director
                        .getContainer();
                while (container.getContainer() != null) {
                    container = (CompositeActor) container.getContainer();
                    if (container.getDirector().getName().startsWith("Ptides")) {
                        return true;
                    }
                }

                // Enclosing time has not yet reached our expected firing time.
                // No need to call fireAt(), since presumably we already
                // did that in postfire().
                return false;
            } else {
                // Enclosing time has exceeded our expected firing time.
                // This should not happen with an enclosing director with
                // full support for fireAt(). The enclosing director
                // could be another periodic director. In this case,
                // we should just increase the next firing time.
                // Or alternatively, we might actually have been prefired
                // at the expected firing time but refused to fire, e.g.,
                // if there were not sufficient input tokens avaialble.
                while (comparison < 0) {
                    _nextFiringTime = _nextFiringTime.add(periodValue);
                    comparison = _nextFiringTime.compareTo(enclosingTime);
                }
                if (comparison == 0) {
                    return true;
                } else {
                    _fireContainerAt(_nextFiringTime);
                    return false;
                }
                // An alternative would be to throw an exception.
                /*
                throw new IllegalActionException(_director,
                        "Director expected to be fired at time "
                        + _nextFiringTime
                        + " but instead is being fired at time "
                        + enclosingTime);
                 */
                // NOTE: An alternative would be to catch up. The code
                // to do that is here.
                /*
                while (_nextFiringTime.compareTo(enclosingTime) < 0) {
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
                            return true;
                        }
                        // If the index is not zero, do not agree to fire, but request
                        // a refiring at the next multiple of the period.
                        _nextFiringTime = _nextFiringTime.add(periodValue);
                        _fireContainerAt(_nextFiringTime);
                        return false;
                    }
                    // If the enclosing director does not support superdense time,
                    // then agree to fire.
                    return true;
                } else {
                    // NOTE: The following throws an exception if the time
                    // requested cannot be honored by the enclosed director
                    // Presumably, if the user set the period, he or she wanted
                    // that behavior.
                    _fireContainerAt(_nextFiringTime);
                    return false;
                }
                 */
            }
        }
        // If period is zero, then just return true.
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Request a firing of the container of the director at the specified time
     *  and throw an exception if the executive director does not agree to
     *  do it at the requested time. If there is no executive director (this
     *  director is at the top level), then ignore the request.
     *  This method is essentially a duplicate of the method in Director,
     *  which is not accessible.
     *  @param time The requested time.
     *  @return The time that the executive director indicates it will fire this
     *   director, or an instance of Time with value Double.NEGATIVE_INFINITY
     *   if there is no executive director.
     *  @exception IllegalActionException If the director does not
     *   agree to fire the actor at the specified time, or if there
     *   is no director.
     */
    private Time _fireContainerAt(Time time) throws IllegalActionException {
        Actor container = (Actor) _director.getContainer();
        if (container != null) {
            // Use microstep 1 because periodic directors are always discrete.
            Time result = ((Director) _director).fireContainerAt(time, 1);
            if (!result.equals(time)) {
                throw new IllegalActionException(_director,
                        "Timing incompatibility error: "
                                + " enclosing director is unable to fire "
                                + container.getName()
                                + " at the requested time: " + time
                                + ". It responds it will fire it at: " + result
                                + ".");
            }
            return result;
        }
        return new Time((Director) _director, Double.NEGATIVE_INFINITY);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated director. */
    private PeriodicDirector _director;

    /** The expected next firing time. */
    private Time _nextFiringTime;
}
