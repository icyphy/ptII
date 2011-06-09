/* An implementation of a non-preemptive platform execution strategy.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.apps.ptides.platform;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.CausalityInterfaceForComposites;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.apps.ptides.lib.ScheduleListener.ScheduleEventType;
import ptolemy.domains.ptides.kernel.PtidesActorProperties;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

/**
 * An implementation of a non-preemptive platform execution strategy.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 7.1
 */
public class NonPreemptivePlatformExecutionStrategy extends
        PlatformExecutionStrategy {

    /**
     * Create new non-preemptive platform execution strategy.
     *
     * @param director
     *            required to display the schedule
     */
    public NonPreemptivePlatformExecutionStrategy(Director director) {
        _director = director;
    }

    /**
     * used to sort the set of events that are safe to fire so that the first
     * event in the list should be fired next.
     *
     * @author Patricia Derler
     * @version $Id$
     * @since Ptolemy II 7.1
     */
    private static class WCETComparator implements Comparator {

        protected WCETComparator(Time physicalTime) {
            _physicalTime = physicalTime;
        }

        private Time _physicalTime;

        /**
         * This compare method is used to sort all events.
         *
         * @param arg0
         *            First event.
         * @param arg1
         *            Second event.
         * @return -1 if event arg0 should be processed before event arg1 and
         *         vice versa.
         */
        public int compare(Object arg0, Object arg1) {
            TimedEvent event1 = (TimedEvent) arg0;
            TimedEvent event2 = (TimedEvent) arg1;
            Actor actor1 = event1.contents instanceof IOPort ? (Actor) ((IOPort) event1.contents)
                    .getContainer()
                    : (Actor) event1.contents;
            Actor actor2 = event2.contents instanceof IOPort ? (Actor) ((IOPort) event2.contents)
                    .getContainer()
                    : (Actor) event2.contents;

            double wcet1 = 0.0;
            double wcet2 = 0.0;
            try {
            	wcet1 = PtidesActorProperties.getWCET(actor1);
            	wcet2 = PtidesActorProperties.getWCET(actor2);
            } catch (IllegalActionException ex) {
            	// FIXME: this seems wrong, but compare() does not throw IllegalActionException
            	throw new InternalErrorException(actor1, ex, "Can't get the wcet of "
            				+ actor1.getFullName() + " or " + actor2.getFullName()
            				+ ".");
            }
            Time time1 = event1.timeStamp;
            Time time2 = event2.timeStamp;
            boolean fireAtRT1 = PtidesActorProperties
                    .mustBeFiredAtRealTime(event1.contents);
            boolean fireAtRT2 = PtidesActorProperties
                    .mustBeFiredAtRealTime(event2.contents);
            int index1 = -1;
            int index2 = -1;

            CompositeActor compositeActor = (CompositeActor) actor1
                    .getContainer();
            CausalityInterfaceForComposites causalityInterface = (CausalityInterfaceForComposites) compositeActor
                    .getCausalityInterface();
            try {
                index1 = causalityInterface.getDepthOfActor(actor1);
                index2 = causalityInterface.getDepthOfActor(actor2);
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (wcet1 == 0 && (!fireAtRT1 || time1.equals(_physicalTime))
                    && wcet2 > 0) {
                return -1;
            }
            if (wcet1 > 0 && wcet2 == 0) {
                if (!fireAtRT2 || time2.equals(_physicalTime)) {
                    return 1;
                }
            }
            if (wcet1 == 0 && wcet2 == 0) {
                if (fireAtRT1 && time1.equals(_physicalTime) && !fireAtRT2) {
                    return -1;
                }
                if (fireAtRT1 && time1.compareTo(_physicalTime) > 0
                        && !fireAtRT2) {
                    return 1;
                }
                if (fireAtRT2 && time2.equals(_physicalTime) && !fireAtRT1) {
                    return 1;
                }
                if (fireAtRT2 && time2.compareTo(_physicalTime) > 0
                        && !fireAtRT1) {
                    return -1;
                }
                if (fireAtRT1 && fireAtRT2 && time1.equals(_physicalTime)
                        && time2.equals(_physicalTime)) {
                    return 0;
                }
                if (time1.compareTo(time2) < 0) {
                    return -1;
                }
                if (time2.compareTo(time1) < 0) {
                    return 1;
                } else {
                    return index2 - index1;
                }
            } else { // wcet1 > 0 && wcet2 > 0
                if (fireAtRT1 && !fireAtRT2) {
                    // if execution of non real time actor can fit before real
                    // time actor
                    if ((_physicalTime.getDoubleValue() + wcet2 <= time1
                            .getDoubleValue())) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (fireAtRT2 && !fireAtRT1) {
                    // if execution of non real time actor can fit before real
                    // time actor
                    if ((_physicalTime.getDoubleValue() + wcet1 <= time2
                            .getDoubleValue())) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (fireAtRT1 && fireAtRT2) {
                    if (time1.compareTo(time2) < 0) {
                        return -1;
                    } else if (time1.compareTo(time2) > 0) {
                        return 1;
                    } else {
                        // two actors with WCET > 0 require to be fired at the
                        // same physical time
                    }
                } else {
                    if (time1.compareTo(time2) < 0) {
                        return -1;
                    } else if (time1.compareTo(time2) > 0) {
                        return 1;
                    } else {
                        if (index1 < index2) {
                            return -1;
                        } else if (index1 > index2) {
                            return 1;
                        }
                    }
                }
            }
            return 0;
        }
    }

    /**
     * Return next event that can be fired out of a list of events that are safe
     * to fire. This execution strategy is non preemptive, so it returns falls
     * if the list of actorsFiring is not empty, i.e. an actor is in execution
     * or if no event can be fired now. This is the case if - the list of
     * eventsToFire is empty - the next event that should be fired has to be
     * fired at real time = model time and real time is not there yet - the next
     * event that could be fired has a wcet > next real time event.
     *
     * @param actorsFiring
     *            Actors currently in execution.
     * @param eventsToFire
     *            Events that are safe to fire.
     * @param nextRealTimeEvent
     *            Smallest time stamp of events that have to be fired at model
     *            time = real time.
     * @param physicalTime
     *            Current physical time of the model.
     * @return The next event that can be fired.
     * @exception IllegalActionException
     *                Thrown if an execution was missed.
     */
    public TimedEvent getNextEventToFire(Queue<TimedEvent> actorsFiring,
            List<TimedEvent> eventsToFire, Time nextRealTimeEvent,
            Time physicalTime) throws IllegalActionException {

        Collections.sort(eventsToFire, new WCETComparator(physicalTime));
        TimedEvent event;
        int index = 0;

        while (index < eventsToFire.size()) {
            event = eventsToFire.get(index);
            Actor actorToFire = event.contents instanceof IOPort ? (Actor) ((IOPort) event.contents)
                    .getContainer()
                    : (Actor) event.contents;

            if (PtidesActorProperties.mustBeFiredAtRealTime(event.contents)) {
                if (physicalTime.compareTo(event.timeStamp) > 0) {
                    _displaySchedule(actorToFire, event.timeStamp
                            .getDoubleValue(),
                            ScheduleEventType.MISSEDEXECUTION);
                    throw new IllegalActionException("missed execution, "
                            + event.contents + " wanted to " + "be fired at "
                            + event.timeStamp + " but it is already "
                            + physicalTime);
                } else if (physicalTime.compareTo(event.timeStamp) < 0) {
                    index++;
                    continue;
                }
            } else if (physicalTime.add(
                    PtidesActorProperties.getWCET(actorToFire)).compareTo(
                    nextRealTimeEvent) > 0) {
                index++;
                continue;
            }
            // assuming that sensors and actuators have WCET == 0.0, they should be able to preempt.
            if (!((PtidesActorProperties.isSensor(actorToFire) || PtidesActorProperties
                    .isActuator(actorToFire)) && PtidesActorProperties
                    .getWCET(actorToFire) == 0.0)
                    && actorsFiring.size() > 0 || eventsToFire.size() == 0) {
                return null;
            }
            eventsToFire.remove(event);
            return new TimedEvent(event.timeStamp, actorToFire);

        }
        return null;
    }

}
