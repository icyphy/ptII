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
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.domains.ptides.kernel.PtidesGraphUtilities;
import ptolemy.domains.ptides.lib.ScheduleListener;
import ptolemy.graph.DirectedAcyclicGraph;
import ptolemy.kernel.util.IllegalActionException;

/**
 * This is an implementation of a non-preemptive platform execution strategy.
 * 
 * @author Patricia Derler
 * 
 */
public class NonPreemptivePlatformExecutionStrategy extends
		PlatformExecutionStrategy {

	/**
	 * Create new non-preemptive platform execution strategy.
	 * 
	 * @param physicalTime
	 *            required to sort events that are safe to process and determine
	 *            which event can be fired next
	 * @param director
	 *            required to display the schedule
	 */
	public NonPreemptivePlatformExecutionStrategy(Time physicalTime,
			Director director) {
		_director = director;
		_physicalTime = physicalTime;
	}

	/**
	 * sort the list of events that are safe to fire
	 * 
	 * @param list
	 */
	public void sort(List list) {
		Collections.sort(list, new WCETComparator());
	}

	/**
	 * used to sort the set of events that are safe to fire so that the first
	 * event in the list should be fired next.
	 * 
	 * @author Patricia Derler
	 * 
	 */
	private class WCETComparator implements Comparator {

		private boolean _preemptive = false;

		public int compare(Object arg0, Object arg1) {
			DEEvent event1 = (DEEvent) arg0;
			DEEvent event2 = (DEEvent) arg1;
			Actor actor1 = event1.actor();
			Actor actor2 = event2.actor();
			double wcet1 = PtidesGraphUtilities.getWCET(actor1);
			double wcet2 = PtidesGraphUtilities.getWCET(actor2);
			Time time1 = event1.timeStamp();
			Time time2 = event2.timeStamp();
			boolean fireAtRT1 = PtidesGraphUtilities
					.mustBeFiredAtRealTime(actor1, event1.ioPort());
			boolean fireAtRT2 = PtidesGraphUtilities
					.mustBeFiredAtRealTime(actor2, event2.ioPort());
			int index1 = -1;
			int index2 = -1;

			// TODO wrong!!!
			CompositeActor actor = (CompositeActor) actor1.getContainer();
			FunctionDependencyOfCompositeActor functionDependency = (FunctionDependencyOfCompositeActor) (actor)
					.getFunctionDependency();
			DirectedAcyclicGraph graph = functionDependency
					.getDetailedDependencyGraph().toDirectedAcyclicGraph();
			Object[] objects = graph.topologicalSort();
			for (int i = 0; i < objects.length; i++) {
                            // FIXME: FindBugs says: Call to equals()
                            // comparing different types
				if (((IOPort) objects[i]).equals(actor1))
					index1 = i;
				else if (((IOPort) objects[i]).equals(actor2))
					index2 = i;
			}

			if (wcet1 == 0 && (!fireAtRT1 || time1.equals(_physicalTime))
					&& wcet2 > 0)
				return -1;
			if (wcet1 > 0 && wcet2 == 0
					&& (!fireAtRT2 || time2.equals(_physicalTime)))
				return 1;
			if (wcet1 == 0 && wcet2 == 0) {
				if (fireAtRT1 && time1.equals(_physicalTime) && !fireAtRT2)
					return -1;
				if (fireAtRT1 && time1.compareTo(_physicalTime) > 0
						&& !fireAtRT2)
					return 1;
				if (fireAtRT2 && time2.equals(_physicalTime) && !fireAtRT1)
					return 1;
				if (fireAtRT2 && time2.compareTo(_physicalTime) > 0
						&& !fireAtRT1)
					return -1;
				if (fireAtRT1 && fireAtRT2 && time1.equals(_physicalTime)
						&& time2.equals(_physicalTime))
					return 0;
				if (time1.compareTo(time2) < 0)
					return -1;
				if (time2.compareTo(time1) < 0)
					return 1;
				else {
					if (event1.depth() < event2.depth())
						return -1;
					else if (event1.depth() > event2.depth())
						return 1;
				}
			} else { // wcet1 > 0 && wcet2 > 0
				if (fireAtRT1 && !fireAtRT2) {
					// if execution of non real time actor can fit before real
					// time actor
					if ((!_preemptive && _physicalTime.getDoubleValue() + wcet2 <= time1
							.getDoubleValue())
							|| (_preemptive && _physicalTime.getDoubleValue() <= time1
									.getDoubleValue())) {
						return 1;
					} else {
						return -1;
					}
				} else if (fireAtRT2 && !fireAtRT1) {
					// if execution of non real time actor can fit before real
					// time actor
					if ((!_preemptive && _physicalTime.getDoubleValue() + wcet1 <= time2
							.getDoubleValue())
							|| (_preemptive && _physicalTime.getDoubleValue() <= time2
									.getDoubleValue()))
						return -1;
					else {
						return 1;
					}
				} else if (fireAtRT1 && fireAtRT2) {
					if (time1.compareTo(time2) < 0)
						return -1;
					else if (time1.compareTo(time2) > 0)
						return 1;
					else {
						// two actors with WCET > 0 require to be fired at the
						// same physical time
					}
				} else {
					if (time1.compareTo(time2) < 0)
						return -1;
					else if (time1.compareTo(time2) > 0)
						return 1;
					else {
						if (index1 < index2)
							return -1;
						else if (index1 > index2)
							return 1;
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
	 * event that could be fired has a wcet > next real time event
	 */
	public DEEvent getNextEventToFire(List actorsFiring, List eventsToFire)
			throws IllegalActionException {
		if (actorsFiring.size() > 0 || eventsToFire.size() == 0)
			return null;

		Time _nextRealTimeEvent = Time.POSITIVE_INFINITY;
		Collections.sort(eventsToFire, new WCETComparator());
		for (int i = 0; i < eventsToFire.size(); i++) {
			DEEvent event = (DEEvent) eventsToFire.get(i);
			Actor actor = event.actor();
			if (PtidesGraphUtilities.mustBeFiredAtRealTime(actor, event.ioPort())) {
				if (event.timeStamp().compareTo(_nextRealTimeEvent) < 0)
					_nextRealTimeEvent = event.timeStamp();
			}
		}
		DEEvent event = null;
		int index = 0;
		while (index < eventsToFire.size()) {
			event = (DEEvent) eventsToFire.get(index);
			Actor actorToFire = event.actor();

			if (PtidesGraphUtilities.mustBeFiredAtRealTime(actorToFire, event.ioPort())) {
				if (_physicalTime.compareTo(event.timeStamp()) > 0) {
					_displaySchedule(actorToFire, event.timeStamp()
							.getDoubleValue(), ScheduleListener.MISSEDEXECUTION);
					throw new IllegalActionException("missed execution!");
				} else if (_physicalTime.compareTo(event.timeStamp()) < 0) {
					index++;
					continue;
				}
			} else if (_physicalTime.add(
					PtidesGraphUtilities.getWCET(actorToFire)).compareTo(
					_nextRealTimeEvent) > 0) {
				index++;
				continue;
			}
			return event;

		}
		return null;
	}
}
