/*
@Copyright (c) 2008-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.apps.ptides.platform;

import java.util.List;
import java.util.Queue;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.apps.ptides.kernel.PtidesEmbeddedDirector;
import ptolemy.apps.ptides.lib.ScheduleListener.ScheduleEventType;
import ptolemy.kernel.util.IllegalActionException;

/**
 * This abstract class provides all methods that need to be implemented for a
 * specific platform execution strategy.
 *
 * For every execution strategy, a name has to be defined which can be used in
 * the execution strategy property field of the PtidesEmbeddedDirector.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 7.1
 *
 */
public abstract class PlatformExecutionStrategy {

    /**
     * The basic non-preemptive execution strategy is implemented by
     * NonPreemptivePlatformExecutionStrategy.
     */
    public static final String BASIC_NON_PREEMPTIVE = "basic non-preemptive";

    /**
     * The basic preemptive execution strategy is implemented by
     * PreemptivePlatformExecutionStrategy.
     */
    public static final String BASIC_PREEMPTIVE = "basic preemptive";

    /**
     * Director for this platform.
     */
    protected Director _director;

    /**
     * Selects one event out of a set of events that are safe to fire. This one
     * event can be fired or start firing.
     *
     * @param actorsFiring
     *                List of actors that are currently being fired. If this
     *                list contains more than one element, than all actors
     *                except for the last actor are preempted. If this method
     *                returns a new event, then also the last actor in the list
     *                will be preempted and the new actor will be added to
     *                actorsFiring.
     * @param eventsToFire
     *                List of events that are safe to fire. This list is
     *                computed by the PtidesEmbeddedDirector.
     * @param nextRealTimeEvent Smallest time stamp of events that have to be
     * fired at model time = real time.
     * @param physicalTime Current physical time of the model.
     * @return event that can be fired. if no event can be fired then it returns
     *         null
     * @exception IllegalActionException Thrown if the execution of an actor was
     * missed.
     */
    public abstract TimedEvent getNextEventToFire(
            Queue<TimedEvent> actorsFiring, List<TimedEvent> eventsToFire,
            Time nextRealTimeEvent, Time physicalTime)
            throws IllegalActionException;

    /**
     * Forward a schedule event to the enclosing director.
     *
     * @param actor
     *                The actor that has an event to be displayed.
     * @param time
     *                The physical time for the event.
     * @param scheduleEvent
     *                The type of the event.
     */
    protected final void _displaySchedule(Actor actor, double time,
            ScheduleEventType scheduleEvent) {
        ((PtidesEmbeddedDirector) _director).displaySchedule(actor, time,
                scheduleEvent);
    }

}
