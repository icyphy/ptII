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
import ptolemy.apps.ptides.kernel.PtidesActorProperties;
import ptolemy.apps.ptides.lib.ScheduleListener.ScheduleEventType;
import ptolemy.domains.tdl.kernel.TDLModule;
import ptolemy.kernel.util.IllegalActionException;

/**
 * This execution strategy schedules events for a PTIDES platform considering that
 * actors can preempt each other.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 7.1
 *
 */
public class PreemptivePlatformExecutionStrategy extends
        PlatformExecutionStrategy {
    /**
     * Create new non-preemptive platform execution strategy.
     *
     * @param director
     *                required to display the schedule
     */
    public PreemptivePlatformExecutionStrategy(Director director) {
        _director = director;
    }

    /**
     * used to sort the set of events that are safe to fire so that the first
     * event in the list should be fired next.
     *
     * @author Patricia Derler
    @version $Id$
    @since Ptolemy II 7.1
     *
     */
    private static class WCETComparator implements Comparator {

        protected WCETComparator(Time physicalTime) {
        }

        /**
         * This compare method is used to sort all events.
         *
         * @param arg0
         *                First event.
         * @param arg1
         *                Second event.
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
            double wcet1 = PtidesActorProperties.getWCET(actor1);
            double wcet2 = PtidesActorProperties.getWCET(actor2);
            Time time1 = event1.timeStamp;
            Time time2 = event2.timeStamp;
            boolean fireAtRT1 = PtidesActorProperties
                    .mustBeFiredAtRealTime(event1.contents);
            boolean fireAtRT2 = PtidesActorProperties
                    .mustBeFiredAtRealTime(event2.contents);
            boolean fixedWCET1 = !(actor1 instanceof TDLModule);
            boolean fixedWCET2 = !(actor2 instanceof TDLModule);
            int index1 = -1;
            int index2 = -1;
            int priority1 = PtidesActorProperties.getPriority(actor1);
            int priority2 = PtidesActorProperties.getPriority(actor2);

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

            if (priority1 != priority2) {
                return priority2 - priority1;
            }
            if (wcet1 > 0 && wcet2 == 0) {
                return 1;
            } else if (wcet2 > 0 && wcet1 == 0) {
                return -1;
            }
            if (wcet1 == 0 && wcet2 == 0) {
                if (fixedWCET1 && !fixedWCET2) {
                    return -1;
                } else if (fixedWCET2 && !fixedWCET1) {
                    return 1;
                } else {
                    return index1 - index2;
                }
            } else { // wcet1 > 0 && wcet2 > 0
                if (fireAtRT1 && fireAtRT2) {
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
     * to fire. This is the case if - the list of eventsToFire is empty - the
     * next event that should be fired has to be fired at real time = model time
     * and real time is not there yet - the next event that could be fired has a
     * wcet > next real time event.
     *
     * @param actorsFiring
     *                Actors currently in execution.
     * @param eventsToFire
     *                Events that are safe to fire.
     * @param nextRealTimeEvent Smallest time stamp of events that have to be
     * fired at model time = real time.
     * @param physicalTime Current physical time of the model.
     * @return The next event that can be fired.
     * @exception IllegalActionException
     *                 Thrown if an execution was missed.
     */
    public TimedEvent getNextEventToFire(Queue<TimedEvent> actorsFiring,
            List<TimedEvent> eventsToFire, Time nextRealTimeEvent,
            Time physicalTime) throws IllegalActionException {
        if (eventsToFire.size() == 0) {
            return null;
        }
        Collections.sort(eventsToFire, new WCETComparator(physicalTime));
        TimedEvent event;
        int index = 0;
        while (index < eventsToFire.size()) {
            event = eventsToFire.get(index);

            Actor actorToFire = event.contents instanceof IOPort ? (Actor) ((IOPort) event.contents)
                    .getContainer()
                    : (Actor) event.contents;
            if (actorsFiring.size() > 0
                    && !_actorPreempts((Actor) actorsFiring.peek().contents,
                            actorToFire, event.timeStamp, physicalTime)) {
                index++;
                continue;
            }

            if (PtidesActorProperties.mustBeFiredAtRealTime(event.contents)) {
                if (physicalTime.compareTo(event.timeStamp) > 0) {
                    _displaySchedule(actorToFire, event.timeStamp
                            .getDoubleValue(),
                            ScheduleEventType.MISSEDEXECUTION);
                    throw new IllegalActionException("missed execution of "
                            + event);
                } else if (physicalTime.compareTo(event.timeStamp) < 0) {
                    index++;
                    continue;
                }
            }
            eventsToFire.remove(event);
            return new TimedEvent(event.timeStamp, actorToFire);

        }
        return null;
    }

    /**
     * Determine whether the currently executing actor can be
     * preempted by the new given actor.
     * @param currentlyExecuting Currently executing actor.
     * @param preemptingActor Actor that might preempt the currently executing actor.
     * @param preemptingActorTime Time stamp of the event that causes a firing of the
     * possibly preempting actor.
     * @param physicalTime Current physical time.
     * @return True if the currently executing actor can be preempted.
     */
    private boolean _actorPreempts(Actor currentlyExecuting,
            Actor preemptingActor, Time preemptingActorTime, Time physicalTime) {
        boolean fireAtRT1 = PtidesActorProperties
                .mustBeFiredAtRealTime(currentlyExecuting);
        boolean fireAtRT2 = PtidesActorProperties
                .mustBeFiredAtRealTime(preemptingActor);
        int prio1 = PtidesActorProperties.getPriority(currentlyExecuting);
        int prio2 = PtidesActorProperties.getPriority(preemptingActor);
        if (fireAtRT2 && PtidesActorProperties.getWCET(preemptingActor) == 0) {
            return true;
        }
        if ((!fireAtRT1 && fireAtRT2 && preemptingActorTime
                .equals(physicalTime))
                || fireAtRT1 && fireAtRT2 && prio2 > prio1) {
            return true;
        }
        if (prio2 > prio1) {
            return true;
        }
        return false;
    }

}
