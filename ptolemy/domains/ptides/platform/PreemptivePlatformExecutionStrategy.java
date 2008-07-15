/*
@Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.domains.ptides.platform;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.util.FunctionDependencyOfCompositeActor;
import ptolemy.actor.util.Time;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.ptides.kernel.PtidesGraphUtilities;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.domains.ptides.lib.ScheduleListener.ScheduleEventType;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**
 * Untested, the only difference to the preemptive execution strategy is that in
 * any case, an event of the set of safe to fire events is selected, even if
 * currently an event is in execution.
 * 
 * @author Patricia Derler
 * 
 */
public class PreemptivePlatformExecutionStrategy extends
        PlatformExecutionStrategy {
    /**
     * Create new non-preemptive platform execution strategy.
     * 
     * @param physicalTime
     *                required to sort events that are safe to process and
     *                determine which event can be fired next
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
     * 
     */
    private class WCETComparator implements Comparator {
        
        protected WCETComparator(Time physicalTime) {
            _physicalTime = physicalTime;
        }

        private Time _physicalTime;
        

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
            DEEvent event1 = (DEEvent) arg0;
            DEEvent event2 = (DEEvent) arg1;
            Actor actor1 = event1.actor();
            Actor actor2 = event2.actor();
            double wcet1 = PtidesGraphUtilities.getWCET(actor1);
            double wcet2 = PtidesGraphUtilities.getWCET(actor2);
            Time time1 = event1.timeStamp();
            Time time2 = event2.timeStamp();
            boolean fireAtRT1 = PtidesGraphUtilities.mustBeFiredAtRealTime(
                    actor1, event1.ioPort());
            boolean fireAtRT2 = PtidesGraphUtilities.mustBeFiredAtRealTime(
                    actor2, event2.ioPort());
            int index1 = -1;
            int index2 = -1;
            int priority1 = getPriority(actor1);
            int priority2 = getPriority(actor2);

            CompositeActor actor = (CompositeActor) actor1.getContainer();
            FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) (actor)
                    .getFunctionDependency();
            DirectedAcyclicGraph graph = functionDependency
                    .getDetailedDependencyGraph().toDirectedAcyclicGraph();
            Object[] objects = graph.topologicalSort();
            for (int i = 0; i < objects.length; i++) {
                if (((IOPort) objects[i]).getContainer() == actor1) {
                    index1 = i;
                } else if (((IOPort) objects[i]).getContainer() == actor2) {
                    index2 = i;
                }
            }

            if (priority1 != priority2) {
                return priority2 - priority1;
            }
            if (wcet1 == 0
                    && (!fireAtRT1 || (fireAtRT1 && time1.equals(_physicalTime)))) {
                return -1;
            }
            if (wcet2 == 0
                    && (!fireAtRT2 || (fireAtRT2 && time2.equals(_physicalTime)))) {
                return 1;
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
                    if (event1.depth() < event2.depth()) {
                        return -1;
                    } else if (event1.depth() > event2.depth()) {
                        return 1;
                    }
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
     * 
     * @return The next event that can be fired.
     * @exception IllegalActionException
     *                 Thrown if an execution was missed.
     */
    public DEEvent getNextEventToFire(List<DEEvent> actorsFiring,
            List<DEEvent> eventsToFire, Time nextRealTimeEvent, 
            Time physicalTime) throws IllegalActionException {
        if (eventsToFire.size() == 0) {
            return null;
        }
        Collections.sort(eventsToFire, new WCETComparator(physicalTime));
        DEEvent event;
        int index = 0;
        while (index < eventsToFire.size()) {
            event = (DEEvent) eventsToFire.get(index);

            Actor actorToFire = event.actor();
            if (actorsFiring.size() > 0
                    && !actorPreempts(actorsFiring.get(0).actor(), actorToFire,
                            event.timeStamp(), physicalTime)) {
                index++;
                continue;
            }

            if (PtidesGraphUtilities.mustBeFiredAtRealTime(actorToFire, event
                    .ioPort())) {
                if (physicalTime.compareTo(event.timeStamp()) > 0) {
                    _displaySchedule(actorToFire, event.timeStamp()
                            .getDoubleValue(), ScheduleEventType.missedexecution);
                    throw new IllegalActionException("missed execution!");
                } else if (physicalTime.compareTo(event.timeStamp()) < 0) {
                    index++;
                    continue;
                }
            }
            return event;

        }
        return null;
    }

    private boolean actorPreempts(Actor currentlyExecuting, Actor actor2,
            Time time, Time physicalTime) {
        boolean fireAtRT1 = PtidesGraphUtilities.mustBeFiredAtRealTime(
                currentlyExecuting, null);
        boolean fireAtRT2 = PtidesGraphUtilities.mustBeFiredAtRealTime(actor2,
                null);
        int prio1 = getPriority(currentlyExecuting);
        int prio2 = getPriority(actor2);
        if (fireAtRT2 && PtidesGraphUtilities.getWCET(actor2) == 0) {
            return true;
        }
        if ((!fireAtRT1 && fireAtRT2 && time.equals(physicalTime))
                || fireAtRT1 && fireAtRT2 && prio2 > prio1) {
            return true;
        }
        if (prio2 > prio1) {
            return true;
        }
        return false;
    }

    private int getPriority(Actor actor) {
        try {
            Parameter parameter = (Parameter) ((NamedObj) actor)
                    .getAttribute("priority");

            if (parameter != null) {
                IntToken token = (IntToken) parameter.getToken();

                return token.intValue();
            } else {
                return 0;
            }
        } catch (ClassCastException ex) {
            return 0;
        } catch (IllegalActionException ex) {
            return 0;
        }
    }
}
